import type { PaymentStatus } from "./models.js";

export type { PaymentStatus };

// Legacy transaction status for backwards compatibility.
export type TransactionStatus =
  | "pending"
  | "escrow_locked"
  | "offramp_pending"
  | "offramp_ready"
  | "escrow_released"
  | "completed"
  | "failed"
  | "refunded";

export type EscrowState = "Deposited" | "ReadyForFunding" | "Released" | "Refunded";

// Legal on-chain state transitions mirrored in the backend poller.
export const ESCROW_TRANSITIONS: Record<EscrowState, EscrowState[]> = {
  Deposited: ["ReadyForFunding", "Refunded"],
  ReadyForFunding: ["Released", "Refunded"],
  Released: [],
  Refunded: []
};

export function canTransition(from: EscrowState, to: EscrowState): boolean {
  return ESCROW_TRANSITIONS[from].includes(to);
}

// Unified Payment State Machine transitions (PRD Section 14 & 25).
export const PAYMENT_STATUS_TRANSITIONS: Record<PaymentStatus, readonly PaymentStatus[]> = {
  CREATED: ["QUOTE_PENDING", "PAYMENT_FAILED"],
  QUOTE_PENDING: ["AWAITING_CONFIRMATION", "PAYMENT_FAILED"],
  AWAITING_CONFIRMATION: ["SETTLEMENT_PENDING", "QUOTE_EXPIRED", "PAYMENT_FAILED"],
  SETTLEMENT_PENDING: ["SETTLEMENT_SUBMITTED", "SETTLEMENT_FAILED", "PAYMENT_FAILED"],
  SETTLEMENT_SUBMITTED: ["SETTLEMENT_CONFIRMED", "SETTLEMENT_FAILED"],
  SETTLEMENT_CONFIRMED: ["OFFRAMP_CREATED", "OFFRAMP_FAILED"],
  OFFRAMP_CREATED: ["OFFRAMP_PROCESSING", "FIAT_PAYOUT_PENDING", "COMPLETED", "OFFRAMP_FAILED", "PAYOUT_FAILED"],
  OFFRAMP_PROCESSING: ["FIAT_PAYOUT_PENDING", "COMPLETED", "PAYOUT_FAILED", "OFFRAMP_FAILED"],
  FIAT_PAYOUT_PENDING: ["COMPLETED", "PAYOUT_FAILED"],
  COMPLETED: [],
  PAYMENT_FAILED: [],
  SETTLEMENT_FAILED: [],
  OFFRAMP_FAILED: [],
  PAYOUT_FAILED: [],
  QUOTE_EXPIRED: ["QUOTE_PENDING", "AWAITING_CONFIRMATION", "PAYMENT_FAILED"]
};

export function canTransitionPayment(from: PaymentStatus, to: PaymentStatus): boolean {
  if (from === to) return true;
  const allowed = PAYMENT_STATUS_TRANSITIONS[from];
  return allowed ? allowed.includes(to) : false;
}

export function isTerminalStatus(status: PaymentStatus): boolean {
  return (
    status === "COMPLETED" ||
    status === "PAYMENT_FAILED" ||
    status === "SETTLEMENT_FAILED" ||
    status === "OFFRAMP_FAILED" ||
    status === "PAYOUT_FAILED"
  );
}

export function isFailureStatus(status: PaymentStatus): boolean {
  return (
    status === "PAYMENT_FAILED" ||
    status === "SETTLEMENT_FAILED" ||
    status === "OFFRAMP_FAILED" ||
    status === "PAYOUT_FAILED" ||
    status === "QUOTE_EXPIRED"
  );
}

// Maps local mock off-ramp statuses to unified PayX payment statuses.
export function mapProviderStatusToPaymentStatus(
  providerStatus: string
): "OFFRAMP_PROCESSING" | "FIAT_PAYOUT_PENDING" | "COMPLETED" | "PAYOUT_FAILED" | "OFFRAMP_FAILED" | null {
  const normalized = providerStatus.toLowerCase();
  switch (normalized) {
    case "cryptoinit":
    case "crypto_init":
    case "order_processing":
      return "OFFRAMP_PROCESSING";
    case "fiatpending":
    case "fiat_pending":
    case "payout_initiated":
    case "payout_processing":
      return "FIAT_PAYOUT_PENDING";
    case "payoutsuccess":
    case "payout_success":
    case "completed":
    case "success":
      return "COMPLETED";
    case "payoutfailed":
    case "payout_failed":
      return "PAYOUT_FAILED";
    case "order_failed":
    case "rejected":
      return "OFFRAMP_FAILED";
    default:
      return null;
  }
}
