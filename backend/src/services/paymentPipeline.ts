import {
  canTransitionPayment,
  generateEventId,
  generatePaymentId,
  isQuoteExpired,
  isTerminalStatus,
  mapProviderStatusToPaymentStatus,
  validateIndianRecipient,
  validateSolanaAddress,
  type BlockchainTransaction,
  type OffRampOrder,
  type Payment,
  type PaymentStatus,
  type RecipientInfo,
  type SimulationMode,
  type TimelineEvent,
  type WebhookEventRecord
} from "../lib/index.js";
import { MockOffRampAdapter, OnmetaAdapter, type OffRampProvider } from "../adapters/offramp/index.js";
import { SolanaSettlementService } from "./solanaSettlement.js";
import { globalPaymentStore, type IPaymentStore } from "./paymentStore.js";

// Canonical on-curve demo wallets conforming to PRD Section 6 (7xK...92P) and Ed25519 standard.
export const DEFAULT_DEMO_SENDER_WALLET = "7xK999999999999999999999999999999999999992PD";
export const DEFAULT_DEMO_SETTLEMENT_VAULT = "4vvzXwGLvriT9WuDJmTBcwVxiebLE5z9YUVQ6SZwiSDc";

export interface CreatePaymentInput {
  senderWallet: string;
  sourceAmount: number;
  recipient: {
    name: string;
    phone: string;
    upiId?: string;
    bankAccount?: string;
    ifsc?: string;
  };
  mode?: SimulationMode;
  idempotencyKey?: string;
  senderUsdcBalance?: number; // Optional simulated sender balance check
}

export class PaymentPipelineService {
  private store: IPaymentStore;
  private customOffRampProvider?: OffRampProvider;
  private mockProvider: MockOffRampAdapter;
  private onmetaProvider: OnmetaAdapter;
  private solanaService: SolanaSettlementService;

  constructor(options?: {
    store?: IPaymentStore;
    offRampProvider?: OffRampProvider;
    solanaService?: SolanaSettlementService;
  }) {
    this.store = options?.store ?? globalPaymentStore;
    this.customOffRampProvider = options?.offRampProvider;
    this.mockProvider = new MockOffRampAdapter();
    this.onmetaProvider = new OnmetaAdapter();
    this.solanaService = options?.solanaService ?? new SolanaSettlementService();
  }

  setOffRampProvider(provider: OffRampProvider): void {
    this.customOffRampProvider = provider;
  }

  getOffRampProvider(mode?: SimulationMode): OffRampProvider {
    return this.getProviderForPayment(undefined, mode);
  }

  private getProviderForPayment(payment?: Payment, mode?: SimulationMode): OffRampProvider {
    if (this.customOffRampProvider) {
      return this.customOffRampProvider;
    }
    const targetMode = mode || payment?.mode;
    if (targetMode === "live_testnet") {
      return this.onmetaProvider;
    }
    return this.mockProvider;
  }

  private addTimelineEvent(
    payment: Payment,
    status: PaymentStatus,
    title: string,
    description: string,
    metadata?: Record<string, unknown>
  ): void {
    const event: TimelineEvent = {
      id: generateEventId(),
      timestamp: new Date().toISOString(),
      status,
      title,
      description,
      metadata
    };
    payment.timeline.push(event);
    payment.status = status;
    payment.updatedAt = event.timestamp;
  }

  async createPayment(input: CreatePaymentInput): Promise<Payment> {
    if (input.idempotencyKey) {
      return this.store.reserveIdempotencyKey(input.idempotencyKey, () => this.doCreatePayment(input));
    }
    return this.doCreatePayment(input);
  }

  private async doCreatePayment(input: CreatePaymentInput): Promise<Payment> {
    // 1. Validate sender wallet
    if (!validateSolanaAddress(input.senderWallet)) {
      throw new Error("Invalid Solana sender wallet address.");
    }

    // 2. Validate recipient details (PRD Section 4 & 6)
    const railCheck = validateIndianRecipient(input.recipient);
    if (!railCheck.ok) {
      throw new Error(railCheck.reason || "Invalid recipient details.");
    }

    if (!Number.isFinite(input.sourceAmount) || input.sourceAmount <= 0) {
      throw new Error("sourceAmount must be a positive number.");
    }

    const paymentId = generatePaymentId("PX-");
    const mode = input.mode ?? "full_simulation";
    const now = new Date().toISOString();

    const recipient: RecipientInfo = {
      id: `RCP-${Date.now().toString(36).toUpperCase()}`,
      name: input.recipient.name,
      phone: input.recipient.phone,
      country: "IN",
      currency: "INR",
      bankAccount: input.recipient.bankAccount,
      ifsc: input.recipient.ifsc,
      upiId: input.recipient.upiId
    };

    const payment: Payment = {
      id: paymentId,
      status: "CREATED",
      mode,
      senderWallet: input.senderWallet,
      recipient,
      sourceAsset: "USDC",
      sourceAmount: input.sourceAmount,
      destinationCurrency: "INR",
      timeline: [],
      idempotencyKey: input.idempotencyKey,
      createdAt: now,
      updatedAt: now
    };

    this.addTimelineEvent(
      payment,
      "CREATED",
      "Payment Created",
      `Payment initiated for ${input.sourceAmount} USDC to ${recipient.name} (${recipient.upiId || recipient.bankAccount}).`
    );

    // Check simulated balance if specified (PRD Section 25 failure test)
    if (input.senderUsdcBalance !== undefined && input.senderUsdcBalance < input.sourceAmount) {
      payment.failureReason = `Insufficient USDC balance. Required: ${input.sourceAmount} USDC, Available: ${input.senderUsdcBalance} USDC.`;
      this.addTimelineEvent(
        payment,
        "PAYMENT_FAILED",
        "Payment Failed",
        payment.failureReason
      );
      await this.store.savePayment(payment);
      if (input.idempotencyKey) {
        await this.store.recordIdempotency(input.idempotencyKey, payment.id);
      }
      return payment;
    }

    // Immediately fetch quote (PRD Section 7)
    this.addTimelineEvent(
      payment,
      "QUOTE_PENDING",
      "Requesting Quote",
      "Fetching off-ramp quote from provider."
    );

    try {
      const provider = this.getProviderForPayment(payment, payment.mode);
      const quote = await provider.getQuote(input.sourceAmount);
      payment.quoteId = quote.quoteId;
      payment.quote = quote;
      payment.destinationAmount = quote.recipientAmount;
      payment.exchangeRate = quote.exchangeRate;
      payment.fees = {
        offRampFee: quote.offRampFee,
        networkFee: quote.estimatedNetworkFee,
        totalFee: Math.round((quote.offRampFee + quote.estimatedNetworkFee * quote.exchangeRate) * 100) / 100
      };

      this.addTimelineEvent(
        payment,
        "AWAITING_CONFIRMATION",
        "Quote Locked",
        `Rate: ₹${quote.exchangeRate} / USDC. Recipient receives ₹${quote.recipientAmount.toLocaleString("en-IN")}. Quote expires in 30 seconds.`
      );
    } catch (err) {
      payment.failureReason = err instanceof Error ? err.message : "Failed to obtain off-ramp quote.";
      this.addTimelineEvent(payment, "PAYMENT_FAILED", "Quote Failed", payment.failureReason);
    }

    await this.store.savePayment(payment);
    if (input.idempotencyKey) {
      await this.store.recordIdempotency(input.idempotencyKey, payment.id);
    }

    return payment;
  }

  async refreshQuote(paymentId: string): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    if (payment.status !== "AWAITING_CONFIRMATION" && payment.status !== "QUOTE_EXPIRED") {
      throw new Error(`Cannot refresh quote from status: ${payment.status}`);
    }

    this.addTimelineEvent(
      payment,
      "QUOTE_PENDING",
      "Refreshing Quote",
      "Requesting a fresh off-ramp quote."
    );

    const provider = this.getProviderForPayment(payment);
    const quote = await provider.getQuote(payment.sourceAmount);
    payment.quoteId = quote.quoteId;
    payment.quote = quote;
    payment.destinationAmount = quote.recipientAmount;
    payment.exchangeRate = quote.exchangeRate;
    payment.fees = {
      offRampFee: quote.offRampFee,
      networkFee: quote.estimatedNetworkFee,
      totalFee: Math.round((quote.offRampFee + quote.estimatedNetworkFee * quote.exchangeRate) * 100) / 100
    };

    this.addTimelineEvent(
      payment,
      "AWAITING_CONFIRMATION",
      "Quote Locked",
      `Refreshed rate: ₹${quote.exchangeRate} / USDC. Recipient receives ₹${quote.recipientAmount.toLocaleString("en-IN")}.`
    );

    await this.store.savePayment(payment);
    return payment;
  }

  async confirmPayment(paymentId: string): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    if (payment.status !== "AWAITING_CONFIRMATION") {
      throw new Error(`Cannot confirm payment from status: ${payment.status}`);
    }

    // Check quote expiration (PRD Section 7 & 25)
    if (!payment.quote || isQuoteExpired(payment.quote.expiresAt)) {
      payment.failureReason = "Quote expired before confirmation.";
      this.addTimelineEvent(
        payment,
        "QUOTE_EXPIRED",
        "Quote Expired",
        "The locked off-ramp quote expired before confirmation. Please request a new quote."
      );
      await this.store.savePayment(payment);
      return payment;
    }

    this.addTimelineEvent(
      payment,
      "SETTLEMENT_PENDING",
      "Payment Confirmed",
      `Sender confirmed payment of ${payment.sourceAmount} USDC. Initiating Solana settlement.`
    );

    await this.store.savePayment(payment);
    return payment;
  }

  async settleSolana(paymentId: string, transactionSignature?: string): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    if (payment.status !== "SETTLEMENT_PENDING") {
      throw new Error(`Cannot settle from status: ${payment.status}. Expected: SETTLEMENT_PENDING`);
    }

    this.addTimelineEvent(
      payment,
      "SETTLEMENT_SUBMITTED",
      "Solana Settlement Submitted",
      "USDC transfer transaction submitted to Solana network."
    );

    const btx = await this.solanaService.executeOrRecordSettlement({
      paymentId: payment.id,
      sender: payment.senderWallet,
      recipient: DEFAULT_DEMO_SETTLEMENT_VAULT,
      amount: payment.sourceAmount,
      transactionSignature,
      mode: payment.mode
    });

    payment.blockchainTransaction = btx;

    if (btx.confirmationStatus === "failed") {
      payment.failureReason = btx.errorMessage || "Solana transaction failed on-chain.";
      this.addTimelineEvent(
        payment,
        "SETTLEMENT_FAILED",
        "Settlement Failed",
        payment.failureReason,
        { signature: btx.transactionSignature }
      );
      await this.store.savePayment(payment);
      return payment;
    }

    this.addTimelineEvent(
      payment,
      "SETTLEMENT_CONFIRMED",
      "Solana Confirmed",
      `Transaction confirmed on Solana ${btx.network}. Signature: ${btx.transactionSignature.substring(0, 12)}...`,
      { signature: btx.transactionSignature, explorerUrl: btx.explorerUrl }
    );

    await this.store.savePayment(payment);
    return payment;
  }

  async initiateOffRamp(paymentId: string): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    if (payment.status !== "SETTLEMENT_CONFIRMED") {
      throw new Error(`Cannot initiate off-ramp from status: ${payment.status}. Expected: SETTLEMENT_CONFIRMED`);
    }

    if (!payment.quote) {
      throw new Error("Missing quote for off-ramp initiation.");
    }

    const provider = this.getProviderForPayment(payment);

    // 1. Create order on Off-Ramp provider (PRD Section 11)
    const orderResult = await provider.createOrder({
      paymentId: payment.id,
      quoteId: payment.quote.quoteId,
      sourceAmount: payment.sourceAmount,
      recipient: {
        name: payment.recipient.name,
        phone: payment.recipient.phone,
        bankAccount: payment.recipient.bankAccount,
        ifsc: payment.recipient.ifsc,
        upiId: payment.recipient.upiId
      }
    });

    if (orderResult.status === "failed") {
      payment.failureReason = orderResult.errorMessage || "Off-ramp order rejected by provider.";
      this.addTimelineEvent(
        payment,
        "OFFRAMP_FAILED",
        "Off-Ramp Order Failed",
        payment.failureReason
      );
      await this.store.savePayment(payment);
      return payment;
    }

    const offRampOrder: OffRampOrder = {
      id: `ORO-${Date.now().toString(36).toUpperCase()}`,
      paymentId: payment.id,
      provider: provider.providerName as "onmeta" | "mock",
      providerOrderId: orderResult.orderId,
      quoteId: payment.quote.quoteId,
      asset: "USDC",
      assetAmount: payment.sourceAmount,
      fiatCurrency: "INR",
      fiatAmount: orderResult.fiatAmount,
      status: "cryptoInit",
      depositAddress: orderResult.depositAddress,
      createdAt: orderResult.createdAt,
      updatedAt: new Date().toISOString()
    };

    payment.offRampOrder = offRampOrder;

    this.addTimelineEvent(
      payment,
      "OFFRAMP_CREATED",
      "Off-Ramp Order Created",
      `Off-ramp order ${orderResult.orderId} created with ${provider.providerName.toUpperCase()}.`
    );

    // 2. Submit transaction hash to Onmeta (PRD Section 12)
    const txHash = payment.blockchainTransaction?.transactionSignature || "sig_simulated_placeholder";
    const submitResult = await provider.submitTransaction(orderResult.orderId, txHash);

    if (submitResult.status === "rejected") {
      payment.failureReason = submitResult.errorMessage || "Transaction hash submission rejected.";
      this.addTimelineEvent(
        payment,
        "OFFRAMP_FAILED",
        "Hash Submission Failed",
        payment.failureReason
      );
      await this.store.savePayment(payment);
      return payment;
    }

    offRampOrder.transactionHash = txHash;
    offRampOrder.status = "cryptoInit";
    offRampOrder.updatedAt = submitResult.submittedAt;

    this.addTimelineEvent(
      payment,
      "OFFRAMP_PROCESSING",
      "Off-Ramp Processing",
      `Solana transaction linked to off-ramp order ${orderResult.orderId}. Provider processing crypto deposit.`
    );

    await this.store.savePayment(payment);
    return payment;
  }

  // Processes webhook updates from Onmeta (PRD Section 13, 19, 20)
  async processWebhookEvent(record: WebhookEventRecord): Promise<{
    processed: boolean;
    duplicate: boolean;
    payment?: Payment;
    errorMessage?: string;
  }> {
    // 1. Idempotency & duplicate event protection (PRD Section 19 & 20)
    const acquired = await this.store.acquireWebhookLock(record.eventId);
    if (!acquired) {
      record.status = "duplicate";
      await this.store.recordWebhook(record);
      return { processed: false, duplicate: true };
    }

    try {
      // 2. Find associated payment by orderId or correlation ID
      const orderId = record.orderId || (record.payload["orderId"] as string) || (record.payload["order_id"] as string);
      const correlationId = (record.payload["paymentId"] as string) || (record.payload["correlation_id"] as string);

      let payment: Payment | null = null;
      if (correlationId) {
        payment = await this.store.getPayment(correlationId);
      }
      if (!payment && orderId) {
        const all = await this.store.listPayments(100);
        payment = all.find((p) => p.offRampOrder?.providerOrderId === orderId) ?? null;
      }

      if (!payment) {
        record.status = "error";
        record.errorMessage = `No matching payment found for webhook event: ${record.eventId}`;
        await this.store.recordWebhook(record);
        return { processed: false, duplicate: false, errorMessage: record.errorMessage };
      }

      // 3. Map provider status to PayX payment state
      const providerStatus = (record.payload["status"] as string) || record.eventType;
      const targetStatus = mapProviderStatusToPaymentStatus(providerStatus);

      if (!targetStatus) {
        record.status = "ignored";
        await this.store.recordWebhook(record);
        return { processed: false, duplicate: false, payment };
      }

      if (!canTransitionPayment(payment.status, targetStatus)) {
        // If payment is already completed or ahead, ignore out-of-order webhook gracefully
        record.status = "ignored";
        record.errorMessage = `Transition from ${payment.status} to ${targetStatus} is illegal.`;
        await this.store.recordWebhook(record);
        return { processed: false, duplicate: false, payment, errorMessage: record.errorMessage };
      }

      // 4. Advance payment status
      if (payment.offRampOrder) {
        payment.offRampOrder.status = providerStatus as OffRampOrder["status"];
        if (record.payload["payoutReference"]) {
          payment.offRampOrder.payoutReference = String(record.payload["payoutReference"]);
        }
        payment.offRampOrder.updatedAt = new Date().toISOString();
      }

      if (targetStatus === "OFFRAMP_PROCESSING") {
        this.addTimelineEvent(
          payment,
          "OFFRAMP_PROCESSING",
          "Off-Ramp Processing",
          `USDC deposit recognized by ${record.provider.toUpperCase()}. Converting to INR.`
        );
      } else if (targetStatus === "FIAT_PAYOUT_PENDING") {
        this.addTimelineEvent(
          payment,
          "FIAT_PAYOUT_PENDING",
          "INR Payout Initiated",
          `INR payout initiated to recipient bank account / UPI (${payment.recipient.name}).`
        );
      } else if (targetStatus === "COMPLETED") {
        payment.completedAt = new Date().toISOString();
        const utr = payment.offRampOrder?.payoutReference || (record.payload["utr"] as string) || `UTR${Date.now().toString().slice(-8)}`;
        this.addTimelineEvent(
          payment,
          "COMPLETED",
          "Payment Completed",
          `₹${payment.destinationAmount?.toLocaleString("en-IN")} delivered to ${payment.recipient.name}. Reference: ${utr}`,
          { utr }
        );
      } else if (targetStatus === "PAYOUT_FAILED") {
        payment.failureReason = (record.payload["failureReason"] as string) || "Bank payout rejected by Indian banking system.";
        this.addTimelineEvent(
          payment,
          "PAYOUT_FAILED",
          "Payout Failed",
          payment.failureReason
        );
      }

      record.status = "processed";
      record.processedAt = new Date().toISOString();
      await this.store.recordWebhook(record);
      await this.store.savePayment(payment);

      return { processed: true, duplicate: false, payment };
    } finally {
      await this.store.releaseWebhookLock(record.eventId);
    }
  }

  // Reconciles payment state against off-ramp provider when webhooks are delayed (PRD Section 25 & 32)
  async reconcilePayment(paymentId: string): Promise<{ payment: Payment; reconciled: boolean }> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    if (isTerminalStatus(payment.status)) {
      return { payment, reconciled: false };
    }

    if (!payment.offRampOrder?.providerOrderId) {
      return { payment, reconciled: false };
    }

    const provider = this.getProviderForPayment(payment);
    const statusResult = await provider.getStatus(payment.offRampOrder.providerOrderId);

    if (payment.offRampOrder) {
      payment.offRampOrder.status = statusResult.providerStatus as OffRampOrder["status"];
      if (statusResult.payoutReference) {
        payment.offRampOrder.payoutReference = statusResult.payoutReference;
      }
      payment.offRampOrder.updatedAt = statusResult.updatedAt || new Date().toISOString();
    }

    const targetStatus = statusResult.unifiedStatus;
    if (targetStatus && targetStatus !== payment.status && canTransitionPayment(payment.status, targetStatus)) {
      if (targetStatus === "OFFRAMP_PROCESSING") {
        this.addTimelineEvent(
          payment,
          "OFFRAMP_PROCESSING",
          "Off-Ramp Processing (Reconciled)",
          `Off-ramp provider recognized deposit for order ${payment.offRampOrder.providerOrderId}.`
        );
      } else if (targetStatus === "FIAT_PAYOUT_PENDING") {
        this.addTimelineEvent(
          payment,
          "FIAT_PAYOUT_PENDING",
          "INR Payout Initiated (Reconciled)",
          `Off-ramp provider confirmed INR payout initiated for order ${payment.offRampOrder.providerOrderId}.`
        );
      } else if (targetStatus === "COMPLETED") {
        payment.completedAt = new Date().toISOString();
        const utr = payment.offRampOrder?.payoutReference || `UTR${Date.now().toString().slice(-8)}`;
        this.addTimelineEvent(
          payment,
          "COMPLETED",
          "Payment Completed (Reconciled)",
          `Payment reconciled via provider status check. Delivery UTR: ${utr}`,
          { utr }
        );
      } else if (targetStatus === "PAYOUT_FAILED") {
        payment.failureReason = statusResult.errorMessage || "Bank payout rejected as reported by provider.";
        this.addTimelineEvent(payment, "PAYOUT_FAILED", "Payout Failed (Reconciled)", payment.failureReason);
      }

      await this.store.savePayment(payment);
      return { payment, reconciled: true };
    }

    await this.store.savePayment(payment);
    return { payment, reconciled: false };
  }

  // Executes the complete pipeline from start to finish
  async executeFullPipeline(input: CreatePaymentInput): Promise<Payment> {
    const payment = await this.createPayment(input);
    if (payment.status === "PAYMENT_FAILED") return payment;

    const confirmed = await this.confirmPayment(payment.id);
    if (confirmed.status === "QUOTE_EXPIRED" || confirmed.status === "PAYMENT_FAILED") return confirmed;

    const settled = await this.settleSolana(confirmed.id);
    if (settled.status === "SETTLEMENT_FAILED") return settled;

    const offramp = await this.initiateOffRamp(settled.id);
    if (offramp.status === "OFFRAMP_FAILED") return offramp;

    // Simulate provider asynchronous status updates
    await this.processWebhookEvent({
      eventId: generateEventId("EVT-ONM-1-"),
      provider: "onmeta",
      eventType: "cryptoInit",
      orderId: offramp.offRampOrder?.providerOrderId,
      payload: { status: "cryptoInit", orderId: offramp.offRampOrder?.providerOrderId, paymentId: offramp.id },
      receivedAt: new Date().toISOString(),
      status: "processed"
    });

    await this.processWebhookEvent({
      eventId: generateEventId("EVT-ONM-2-"),
      provider: "onmeta",
      eventType: "fiatPending",
      orderId: offramp.offRampOrder?.providerOrderId,
      payload: { status: "fiatPending", orderId: offramp.offRampOrder?.providerOrderId, paymentId: offramp.id },
      receivedAt: new Date().toISOString(),
      status: "processed"
    });

    const completed = await this.processWebhookEvent({
      eventId: generateEventId("EVT-ONM-3-"),
      provider: "onmeta",
      eventType: "payoutSuccess",
      orderId: offramp.offRampOrder?.providerOrderId,
      payload: {
        status: "payoutSuccess",
        orderId: offramp.offRampOrder?.providerOrderId,
        paymentId: offramp.id,
        payoutReference: `UTR${Date.now().toString().slice(-8)}`
      },
      receivedAt: new Date().toISOString(),
      status: "processed"
    });

    return completed.payment || (await this.store.getPayment(payment.id))!;
  }

  // Dev Console Step-by-Step Simulator (PRD Section 23)
  async simulateStep(
    paymentId: string,
    step: "confirm" | "simulate_usdc_received" | "settle" | "confirm_solana" | "create_offramp" | "payout_processing" | "payout_success" | "reconcile"
  ): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    switch (step) {
      case "confirm":
      case "simulate_usdc_received":
        return this.confirmPayment(paymentId);

      case "settle":
      case "confirm_solana":
        return this.settleSolana(paymentId);

      case "create_offramp":
        return this.initiateOffRamp(paymentId);

      case "reconcile":
        return (await this.reconcilePayment(paymentId)).payment;

      case "payout_processing": {
        const orderId = payment.offRampOrder?.providerOrderId;
        const res = await this.processWebhookEvent({
          eventId: generateEventId("EVT-SIM-PROC-"),
          provider: "onmeta",
          eventType: "fiatPending",
          orderId,
          payload: { status: "fiatPending", orderId, paymentId: payment.id },
          receivedAt: new Date().toISOString(),
          status: "processed"
        });
        return res.payment || (await this.store.getPayment(paymentId))!;
      }

      case "payout_success": {
        const orderId = payment.offRampOrder?.providerOrderId;
        const res = await this.processWebhookEvent({
          eventId: generateEventId("EVT-SIM-SUCC-"),
          provider: "onmeta",
          eventType: "payoutSuccess",
          orderId,
          payload: {
            status: "payoutSuccess",
            orderId,
            paymentId: payment.id,
            payoutReference: `UTR${Date.now().toString().slice(-8)}`
          },
          receivedAt: new Date().toISOString(),
          status: "processed"
        });
        return res.payment || (await this.store.getPayment(paymentId))!;
      }

      default:
        throw new Error(`Unknown simulation step: ${step}`);
    }
  }

  // Failure Injection (PRD Section 25)
  async injectFailure(
    paymentId: string,
    failureType: "solana_failed" | "offramp_failed" | "payout_failed" | "quote_expired"
  ): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    switch (failureType) {
      case "quote_expired": {
        payment.failureReason = "Injected failure: Quote expired before confirmation.";
        this.addTimelineEvent(payment, "QUOTE_EXPIRED", "Quote Expired", payment.failureReason);
        await this.store.savePayment(payment);
        return payment;
      }
      case "solana_failed": {
        payment.failureReason = "Injected failure: Solana transaction rejected (insufficient gas or timeout).";
        this.addTimelineEvent(payment, "SETTLEMENT_FAILED", "Settlement Failed", payment.failureReason);
        await this.store.savePayment(payment);
        return payment;
      }
      case "offramp_failed": {
        payment.failureReason = "Injected failure: Off-ramp provider rejected order (KYC or sanction block).";
        this.addTimelineEvent(payment, "OFFRAMP_FAILED", "Off-Ramp Failed", payment.failureReason);
        await this.store.savePayment(payment);
        return payment;
      }
      case "payout_failed": {
        payment.failureReason = "Injected failure: Destination bank account invalid or frozen.";
        this.addTimelineEvent(payment, "PAYOUT_FAILED", "Payout Failed", payment.failureReason);
        await this.store.savePayment(payment);
        return payment;
      }
      default:
        throw new Error(`Unknown failure injection type: ${failureType}`);
    }
  }
}

export const globalPipelineService = new PaymentPipelineService();
