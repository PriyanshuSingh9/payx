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
