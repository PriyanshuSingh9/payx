// Pure domain models for PayX cross-border payment orchestration.
// No I/O or external side-effects allowed in this module.

export type PaymentStatus =
  | "CREATED"
  | "QUOTE_PENDING"
  | "AWAITING_CONFIRMATION"
  | "SETTLEMENT_PENDING"
  | "SETTLEMENT_SUBMITTED"
  | "SETTLEMENT_CONFIRMED"
  | "OFFRAMP_CREATED"
  | "OFFRAMP_PROCESSING"
  | "FIAT_PAYOUT_PENDING"
  | "COMPLETED"
  | "PAYMENT_FAILED"
  | "SETTLEMENT_FAILED"
  | "OFFRAMP_FAILED"
  | "PAYOUT_FAILED"
  | "QUOTE_EXPIRED";

export type SimulationMode = "live_testnet" | "full_simulation";

export interface RecipientInfo {
  id: string;
  name: string;
  phone: string;
  country: string;
  currency: string;
  bankAccount?: string;
  ifsc?: string;
  upiId?: string;
  kycReference?: string;
  bankReference?: string;
}

export interface OffRampQuote {
  quoteId: string;
  sourceAsset: "USDC";
  destinationCurrency: "INR";
  sourceAmount: number;
  exchangeRate: number;
  grossDestinationAmount: number;
  offRampFee: number;
  estimatedNetworkFee: number;
  recipientAmount: number;
  estimatedMinutesMin: number;
  estimatedMinutesMax: number;
  quotedAt: string;
  expiresAt: string;
}

export interface BlockchainTransaction {
  id: string;
  paymentId: string;
  chain: "solana";
  network: "devnet" | "mainnet" | "simulator";
  token: "USDC";
  amount: number;
  sender: string;
  recipient: string;
  transactionSignature: string;
  confirmationStatus: "pending" | "confirmed" | "finalized" | "failed";
  slot?: number;
  confirmedAt?: string;
  errorMessage?: string;
  explorerUrl?: string;
  createdAt: string;
}

export interface OffRampOrder {
  id: string;
  paymentId: string;
  provider: "onmeta" | "mock";
  providerOrderId: string;
  quoteId: string;
  asset: "USDC";
  assetAmount: number;
  fiatCurrency: "INR";
  fiatAmount: number;
  status: "order_created" | "cryptoInit" | "fiatPending" | "payoutSuccess" | "payoutFailed";
  depositAddress: string;
  transactionHash?: string;
  payoutReference?: string;
  failureReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TimelineEvent {
  id: string;
  timestamp: string;
  status: PaymentStatus;
  title: string;
  description: string;
  metadata?: Record<string, unknown>;
}

export interface Payment {
  id: string; // e.g. PX-8F29A1
  status: PaymentStatus;
  mode: SimulationMode;
  senderWallet: string;
  recipient: RecipientInfo;
  sourceAsset: "USDC";
  sourceAmount: number;
  destinationCurrency: "INR";
  destinationAmount?: number;
  exchangeRate?: number;
  fees?: {
    offRampFee: number;
    networkFee: number;
    totalFee: number;
  };
  quoteId?: string;
  quote?: OffRampQuote;
  blockchainTransaction?: BlockchainTransaction;
  offRampOrder?: OffRampOrder;
  timeline: TimelineEvent[];
  failureReason?: string;
  idempotencyKey?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
}

export interface WebhookEventRecord {
  eventId: string;
  provider: string;
  eventType: string;
  orderId?: string;
  payload: Record<string, unknown>;
  receivedAt: string;
  processedAt?: string;
  status: "processed" | "duplicate" | "ignored" | "error";
  errorMessage?: string;
}
