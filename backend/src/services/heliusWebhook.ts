import { env } from "../env.js";
import {
  generateEventId,
  type Payment,
  type TimelineEvent
} from "../lib/index.js";
import { globalPaymentStore, type IPaymentStore } from "./paymentStore.js";
import { globalPipelineService, PaymentPipelineService, DEFAULT_DEMO_SETTLEMENT_VAULT } from "./paymentPipeline.js";

export interface HeliusTokenTransfer {
  fromUserAccount?: string;
  toUserAccount?: string;
  fromTokenAccount?: string;
  toTokenAccount?: string;
  tokenAmount?: number;
  tokenMint?: string;
}

export interface HeliusInstruction {
  accounts?: string[];
  data?: string;
  programId?: string;
}

export interface HeliusEnhancedTransaction {
  signature: string;
  type?: string;
  source?: string;
  slot?: number;
  timestamp?: number;
  fee?: number;
  feePayer?: string;
  description?: string;
  tokenTransfers?: HeliusTokenTransfer[];
  instructions?: HeliusInstruction[];
  transactionError?: unknown;
}

export interface ProcessWebhookResult {
  ok: boolean;
  processedCount: number;
  matchedPayments: Array<{
    paymentId: string;
    status: string;
    signature: string;
  }>;
  duplicatesSkipped: number;
  errors: string[];
}

export class HeliusWebhookService {
  private store: IPaymentStore;
  private pipeline: PaymentPipelineService;
  private webhookSecret: string;
  private processedSignatures = new Set<string>();

  constructor(options?: {
    store?: IPaymentStore;
    pipeline?: PaymentPipelineService;
    webhookSecret?: string;
  }) {
    this.store = options?.store ?? globalPaymentStore;
    this.pipeline = options?.pipeline ?? globalPipelineService;
    this.webhookSecret = options?.webhookSecret ?? env.heliusWebhookSecret;
  }

  // Verifies the Authorization header against the configured webhook secret.
  verifyAuthorization(authHeader?: string): boolean {
    if (!this.webhookSecret) {
      return true; // No secret configured in dev/test, allow all requests
    }
    if (!authHeader) {
      return false;
    }
    const cleanHeader = authHeader.replace(/^Bearer\s+/i, "").trim();
    return cleanHeader === this.webhookSecret.trim();
  }

  // Clears the deduplication cache (useful in test suites).
  clearCache(): void {
    this.processedSignatures.clear();
  }

  // Processes an array of enhanced transactions delivered by Helius Webhook.
  async processWebhook(payload: unknown): Promise<ProcessWebhookResult> {
    const txs: HeliusEnhancedTransaction[] = Array.isArray(payload)
      ? payload
      : typeof payload === "object" && payload !== null
        ? [payload as HeliusEnhancedTransaction]
        : [];

    const result: ProcessWebhookResult = {
      ok: true,
      processedCount: 0,
      matchedPayments: [],
      duplicatesSkipped: 0,
      errors: []
    };

    if (txs.length === 0) {
      return result;
    }

    const activePayments = await this.store.listPayments(100);

    for (const tx of txs) {
      if (!tx || !tx.signature) {
        continue;
      }

      // 1. Idempotency & Deduplication
      if (this.processedSignatures.has(tx.signature)) {
        result.duplicatesSkipped++;
        continue;
      }
      this.recordSignature(tx.signature);
      result.processedCount++;

      try {
        const matchedPayment = this.findMatchingPayment(tx, activePayments);
        if (!matchedPayment) {
          continue;
        }

        // 2. Handle transaction failure on-chain
        if (tx.transactionError) {
          matchedPayment.failureReason = `Solana transaction failed on-chain: ${JSON.stringify(tx.transactionError)}`;
          const failureEvent: TimelineEvent = {
            id: generateEventId(),
            timestamp: new Date().toISOString(),
            status: "SETTLEMENT_FAILED",
            title: "Solana Settlement Failed (Helius Webhook)",
            description: matchedPayment.failureReason,
            metadata: { signature: tx.signature, error: tx.transactionError }
          };
          matchedPayment.timeline.push(failureEvent);
          matchedPayment.status = "SETTLEMENT_FAILED";
          matchedPayment.updatedAt = failureEvent.timestamp;
          await this.store.savePayment(matchedPayment);

          result.matchedPayments.push({
            paymentId: matchedPayment.id,
            status: matchedPayment.status,
            signature: tx.signature
          });
          continue;
        }

        // 3. Handle confirmed on-chain transaction
        if (
          matchedPayment.status === "SETTLEMENT_PENDING" ||
          matchedPayment.status === "SETTLEMENT_SUBMITTED"
        ) {
          const now = new Date(tx.timestamp ? tx.timestamp * 1000 : Date.now()).toISOString();

          matchedPayment.blockchainTransaction = {
            id: matchedPayment.blockchainTransaction?.id || `BTX-${Date.now().toString(36).toUpperCase()}`,
            paymentId: matchedPayment.id,
            chain: "solana",
            network: "devnet",
            token: "USDC",
            amount: matchedPayment.sourceAmount,
            sender: matchedPayment.senderWallet,
            recipient: DEFAULT_DEMO_SETTLEMENT_VAULT,
            transactionSignature: tx.signature,
            confirmationStatus: "confirmed",
            slot: tx.slot || 0,
            confirmedAt: now,
            explorerUrl: `https://explorer.solana.com/tx/${tx.signature}?cluster=devnet`,
            createdAt: matchedPayment.blockchainTransaction?.createdAt || now
          };

          const confirmEvent: TimelineEvent = {
            id: generateEventId(),
            timestamp: now,
            status: "SETTLEMENT_CONFIRMED",
            title: "Solana Settlement Confirmed (via Helius Webhook)",
            description: `On-chain settlement confirmed at slot ${tx.slot || "confirmed"}. Signature: ${tx.signature.substring(0, 12)}...`,
            metadata: {
              signature: tx.signature,
              slot: tx.slot,
              explorerUrl: matchedPayment.blockchainTransaction.explorerUrl
            }
          };

          matchedPayment.timeline.push(confirmEvent);
          matchedPayment.status = "SETTLEMENT_CONFIRMED";
          matchedPayment.updatedAt = now;
          await this.store.savePayment(matchedPayment);

          result.matchedPayments.push({
            paymentId: matchedPayment.id,
            status: matchedPayment.status,
            signature: tx.signature
          });

          // 4. Trigger the next pipeline stage (Off-Ramp order initiation)
          try {
            await this.pipeline.initiateOffRamp(matchedPayment.id);
          } catch (err) {
            result.errors.push(
              `Error initiating off-ramp for ${matchedPayment.id}: ${err instanceof Error ? err.message : String(err)}`
            );
          }
        }
      } catch (err) {
        result.errors.push(
          `Error processing tx ${tx.signature}: ${err instanceof Error ? err.message : String(err)}`
        );
      }
    }

    return result;
  }

  private findMatchingPayment(
    tx: HeliusEnhancedTransaction,
    payments: Payment[]
  ): Payment | undefined {
    // 1. Direct signature match
    const bySignature = payments.find(
      (p) => p.blockchainTransaction?.transactionSignature === tx.signature
    );
    if (bySignature) return bySignature;

    // 2. Correlation ID in memo / description
    if (tx.description) {
      const match = tx.description.match(/PX-[0-9A-F]{6}/i);
      if (match) {
        const byId = payments.find((p) => p.id.toUpperCase() === match[0].toUpperCase());
        if (byId) return byId;
      }
    }

    // 3. Sender wallet and token transfer match for pending settlements
    return payments.find((p) => {
      if (p.status !== "SETTLEMENT_PENDING" && p.status !== "SETTLEMENT_SUBMITTED") {
        return false;
      }

      // Check feePayer match
      if (tx.feePayer && tx.feePayer === p.senderWallet) {
        return true;
      }

      // Check token transfers from sender wallet
      if (tx.tokenTransfers && tx.tokenTransfers.length > 0) {
        return tx.tokenTransfers.some((tt) => tt.fromUserAccount === p.senderWallet);
      }

      return false;
    });
  }

  private recordSignature(sig: string): void {
    if (this.processedSignatures.size > 10000) {
      const firstKey = this.processedSignatures.values().next().value;
      if (firstKey) this.processedSignatures.delete(firstKey);
    }
    this.processedSignatures.add(sig);
  }
}

export const globalHeliusWebhookService = new HeliusWebhookService();
