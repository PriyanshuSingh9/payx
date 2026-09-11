import crypto from "node:crypto";
import type { BlockchainTransaction, SimulationMode } from "../lib/index.js";

// Generates a mock 88-character base58 Solana transaction signature.
function generateMockSignature(): string {
  const chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
  let sig = "";
  const bytes = crypto.randomBytes(64);
  for (let i = 0; i < 64; i++) {
    sig += chars[bytes[i]! % chars.length];
  }
  return sig;
}

export interface SettleParams {
  paymentId: string;
  sender: string;
  recipient: string;
  amount: number;
  token?: "USDC";
  transactionSignature?: string;
  mode?: SimulationMode;
}

export class SolanaSettlementService {
  async executeOrRecordSettlement(params: SettleParams): Promise<BlockchainTransaction> {
    // The backend is intentionally simulation-only. This is a mock receipt, not
    // an on-chain transaction. Caller-supplied signatures are never accepted.
    const signature = generateMockSignature();
    const now = new Date().toISOString();

    return {
      id: `BTX-${Date.now().toString(36).toUpperCase()}`,
      paymentId: params.paymentId,
      chain: "solana",
      network: "simulator",
      token: "USDC",
      amount: params.amount,
      sender: params.sender,
      recipient: params.recipient,
      transactionSignature: signature,
      confirmationStatus: "confirmed",
      slot: 284910234,
      confirmedAt: now,
      explorerUrl: undefined,
      createdAt: now
    };
  }
}
