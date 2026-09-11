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
  type TimelineEvent
} from "../lib/index.js";
import { MockOffRampAdapter } from "../adapters/offramp/index.js";
import { env } from "../env.js";
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
  private mockProvider: MockOffRampAdapter;
  private solanaService: SolanaSettlementService;

  constructor(options?: {
    store?: IPaymentStore;
    mockProvider?: MockOffRampAdapter;
    solanaService?: SolanaSettlementService;
  }) {
    this.store = options?.store ?? globalPaymentStore;
    this.mockProvider = options?.mockProvider ?? new MockOffRampAdapter();
    this.solanaService = options?.solanaService ?? new SolanaSettlementService();
  }

  private getMockProvider(): MockOffRampAdapter {
    return this.mockProvider;
  }

  private async waitForMockStage(): Promise<void> {
    const delayMs = Math.min(Math.max(env.mockRampDelayMs, 0), 500);
    if (delayMs === 0) return;
    await new Promise<void>((resolve) => setTimeout(resolve, delayMs));
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
    const mode: SimulationMode = "full_simulation";
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
      const provider = this.getMockProvider();
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

    const provider = this.getMockProvider();
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

    const provider = this.getMockProvider();

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
      provider: "mock",
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

    // 2. Submit the simulated transaction receipt to the mock off-ramp.
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

  async advanceMockOffRamp(
    paymentId: string,
    status: "cryptoInit" | "fiatPending" | "payoutSuccess" | "payoutFailed"
  ): Promise<Payment> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment?.offRampOrder) {
      throw new Error("Cannot advance mock off-ramp before an order is created.");
    }

    const provider = this.getMockProvider();

    provider.advanceOrderStatus(payment.offRampOrder.providerOrderId, status);
    return (await this.reconcilePayment(paymentId)).payment;
  }

  // Reconciles the stored payment state against the local mock adapter.
  async reconcilePayment(paymentId: string): Promise<{ payment: Payment; reconciled: boolean }> {
    const payment = await this.store.getPayment(paymentId);
    if (!payment) throw new Error(`Payment not found: ${paymentId}`);

    if (isTerminalStatus(payment.status)) {
      return { payment, reconciled: false };
    }

    if (!payment.offRampOrder?.providerOrderId) {
      return { payment, reconciled: false };
    }

    const provider = this.getMockProvider();
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

  // Executes the complete local mock pipeline from start to finish.
  async executeFullPipeline(input: CreatePaymentInput): Promise<Payment> {
    const payment = await this.createPayment(input);
    if (payment.status === "PAYMENT_FAILED") return payment;

    const confirmed = await this.confirmPayment(payment.id);
    if (confirmed.status === "QUOTE_EXPIRED" || confirmed.status === "PAYMENT_FAILED") return confirmed;

    await this.waitForMockStage();
    const settled = await this.settleSolana(confirmed.id);
    if (settled.status === "SETTLEMENT_FAILED") return settled;

    await this.waitForMockStage();
    const offramp = await this.initiateOffRamp(settled.id);
    if (offramp.status === "OFFRAMP_FAILED") return offramp;

    await this.waitForMockStage();
    const payoutPending = await this.advanceMockOffRamp(offramp.id, "fiatPending");
    if (payoutPending.status === "PAYOUT_FAILED") return payoutPending;

    await this.waitForMockStage();
    return this.advanceMockOffRamp(offramp.id, "payoutSuccess");
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
        return this.advanceMockOffRamp(paymentId, "fiatPending");
      }

      case "payout_success": {
        return this.advanceMockOffRamp(paymentId, "payoutSuccess");
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
