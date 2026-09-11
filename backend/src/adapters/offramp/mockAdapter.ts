import {
  computeOffRampQuote,
  generateOrderId,
  mapProviderStatusToPaymentStatus,
  type OffRampQuote
} from "../../lib/index.js";
import type {
  CreateOrderParams,
  OffRampOrderResult,
  OffRampProvider,
  OffRampStatusResult,
  SubmitTxResult
} from "./types.js";

// Deterministic mock settlement wallet for the simulation bridge (valid on-curve Solana address).
const MOCK_SETTLEMENT_DEPOSIT_ADDRESS = "DaxETCdkR5cNgWNBN3Su6dAepQtnPVuae4v1D5T5b9u6";

export type MockFailureType =
  | "none"
  | "quote_failure"
  | "order_rejected"
  | "submit_rejected"
  | "payout_failed";

interface InternalMockOrder {
  orderId: string;
  paymentId: string;
  quoteId: string;
  sourceAmount: number;
  fiatAmount: number;
  fiatCurrency: "INR";
  depositAddress: string;
  status: "cryptoInit" | "fiatPending" | "payoutSuccess" | "payoutFailed" | "rejected";
  txHash?: string;
  payoutReference?: string;
  createdAt: string;
  updatedAt: string;
}

export class MockOffRampAdapter implements OffRampProvider {
  readonly providerName = "mock";
  private orders = new Map<string, InternalMockOrder>();
  private failureMode: MockFailureType = "none";
  private customRate?: number;

  constructor(options?: { failureMode?: MockFailureType; customRate?: number }) {
    if (options?.failureMode) this.failureMode = options.failureMode;
    if (options?.customRate) this.customRate = options.customRate;
  }

  setFailureMode(mode: MockFailureType): void {
    this.failureMode = mode;
  }

  getFailureMode(): MockFailureType {
    return this.failureMode;
  }

  setCustomRate(rate: number): void {
    this.customRate = rate;
  }

  async getQuote(amountUsdc: number): Promise<OffRampQuote> {
    if (this.failureMode === "quote_failure") {
      throw new Error("Simulated mock off-ramp quote failure: upstream provider unavailable.");
    }
    return computeOffRampQuote({
      sourceAmount: amountUsdc,
      exchangeRate: this.customRate
    });
  }

  async createOrder(params: CreateOrderParams): Promise<OffRampOrderResult> {
    if (this.failureMode === "order_rejected") {
      return {
        orderId: "",
        paymentId: params.paymentId,
        depositAddress: "",
        status: "failed",
        fiatAmount: 0,
        fiatCurrency: "INR",
        createdAt: new Date().toISOString(),
        errorMessage: "Simulated off-ramp order rejection: compliance check failed."
      };
    }

    const orderId = generateOrderId("MOCK-");
    const quote = computeOffRampQuote({
      sourceAmount: params.sourceAmount,
      exchangeRate: this.customRate
    });

    const order: InternalMockOrder = {
      orderId,
      paymentId: params.paymentId,
      quoteId: params.quoteId,
      sourceAmount: params.sourceAmount,
      fiatAmount: quote.recipientAmount,
      fiatCurrency: "INR",
      depositAddress: MOCK_SETTLEMENT_DEPOSIT_ADDRESS,
      status: "cryptoInit",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    this.orders.set(orderId, order);

    return {
      orderId,
      paymentId: params.paymentId,
      depositAddress: order.depositAddress,
      status: "order_created",
      fiatAmount: order.fiatAmount,
      fiatCurrency: "INR",
      createdAt: order.createdAt
    };
  }

  async submitTransaction(orderId: string, txHash: string): Promise<SubmitTxResult> {
    if (this.failureMode === "submit_rejected") {
      return {
        orderId,
        txHash,
        status: "rejected",
        submittedAt: new Date().toISOString(),
        errorMessage: "Simulated transaction submission rejected by off-ramp provider."
      };
    }

    const order = this.orders.get(orderId);
    if (!order) {
      throw new Error(`Order not found: ${orderId}`);
    }

    order.txHash = txHash;
    order.status = "fiatPending";
    order.updatedAt = new Date().toISOString();

    return {
      orderId,
      txHash,
      status: "accepted",
      submittedAt: order.updatedAt
    };
  }

  async getStatus(orderId: string): Promise<OffRampStatusResult> {
    const order = this.orders.get(orderId);
    if (!order) {
      throw new Error(`Order not found: ${orderId}`);
    }

    if (this.failureMode === "payout_failed" && order.status === "fiatPending") {
      order.status = "payoutFailed";
      order.updatedAt = new Date().toISOString();
    }

    const unified = mapProviderStatusToPaymentStatus(order.status) ?? "OFFRAMP_PROCESSING";

    return {
      orderId: order.orderId,
      providerStatus: order.status,
      unifiedStatus: unified,
      txHash: order.txHash,
      fiatAmount: order.fiatAmount,
      fiatCurrency: order.fiatCurrency,
      payoutReference: order.payoutReference ?? `UTR${Date.now().toString().slice(-8)}`,
      updatedAt: order.updatedAt
    };
  }

  // Simulation controls for demo mode:
  advanceOrderStatus(
    orderId: string,
    nextStatus: "cryptoInit" | "fiatPending" | "payoutSuccess" | "payoutFailed"
  ): OffRampStatusResult {
    const order = this.orders.get(orderId);
    if (!order) {
      throw new Error(`Order not found: ${orderId}`);
    }
    order.status = this.failureMode === "payout_failed" && nextStatus === "payoutSuccess"
      ? "payoutFailed"
      : nextStatus;
    if (order.status === "payoutSuccess") {
      order.payoutReference = `UTR${Date.now().toString().slice(-8)}`;
    }
    order.updatedAt = new Date().toISOString();
    const unified = mapProviderStatusToPaymentStatus(nextStatus) ?? "OFFRAMP_PROCESSING";
    return {
      orderId: order.orderId,
      providerStatus: order.status,
      unifiedStatus: unified,
      txHash: order.txHash,
      fiatAmount: order.fiatAmount,
      fiatCurrency: order.fiatCurrency,
      payoutReference: order.payoutReference,
      updatedAt: order.updatedAt
    };
  }
}
