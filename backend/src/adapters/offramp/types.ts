import type { OffRampQuote } from "../../lib/index.js";

export interface CreateOrderParams {
  paymentId: string;
  quoteId: string;
  sourceAmount: number;
  recipient: {
    name: string;
    phone: string;
    bankAccount?: string;
    ifsc?: string;
    upiId?: string;
  };
}

export interface OffRampOrderResult {
  orderId: string;
  paymentId: string;
  depositAddress: string;
  status: "order_created" | "pending_deposit" | "failed";
  fiatAmount: number;
  fiatCurrency: "INR";
  createdAt: string;
  errorMessage?: string;
}

export interface SubmitTxResult {
  orderId: string;
  txHash: string;
  status: "processing" | "accepted" | "rejected";
  submittedAt: string;
  errorMessage?: string;
}

export interface OffRampStatusResult {
  orderId: string;
  providerStatus: "cryptoInit" | "fiatPending" | "payoutSuccess" | "payoutFailed" | "rejected";
  unifiedStatus: "OFFRAMP_PROCESSING" | "FIAT_PAYOUT_PENDING" | "COMPLETED" | "PAYOUT_FAILED" | "OFFRAMP_FAILED";
  txHash?: string;
  fiatAmount: number;
  fiatCurrency: "INR";
  payoutReference?: string;
  updatedAt: string;
  errorMessage?: string;
}

export interface OffRampProvider {
  readonly providerName: string;
  getQuote(amountUsdc: number): Promise<OffRampQuote>;
  createOrder(params: CreateOrderParams): Promise<OffRampOrderResult>;
  submitTransaction(orderId: string, txHash: string): Promise<SubmitTxResult>;
  getStatus(orderId: string): Promise<OffRampStatusResult>;
}
