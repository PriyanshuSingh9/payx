import { Connection } from "@solana/web3.js";
import { env } from "../env.js";
import {
  generateEventId,
  isQuoteExpired,
  type Payment,
  type TimelineEvent
} from "../lib/index.js";
import { getConnection } from "../solana.js";
import { globalPipelineService, PaymentPipelineService } from "./paymentPipeline.js";
import { globalPaymentStore, type IPaymentStore } from "./paymentStore.js";

export interface PollSummary {
  timestamp: string;
  durationMs: number;
  activePaymentsScanned: number;
  solanaReconciled: number;
  offrampReconciled: number;
  quotesExpired: number;
  errors: string[];
}

export interface PollerMetrics {
  isRunning: boolean;
  pollIntervalMs: number;
  totalPollRuns: number;
  lastPollAt: string | null;
  lastSummary: PollSummary | null;
  totalSolanaReconciled: number;
  totalOfframpReconciled: number;
  totalQuotesExpired: number;
  consecutiveFailures: number;
}

const DEFAULT_SETTLEMENT_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

export class PaymentPollerService {
  private store: IPaymentStore;
  private pipeline: PaymentPipelineService;
  private connectionSupplier: () => Connection;
  private timer: NodeJS.Timeout | null = null;
  private isPolling = false;
  private pollIntervalMs: number;
  private settlementTimeoutMs: number;

  private metrics: PollerMetrics = {
    isRunning: false,
    pollIntervalMs: env.pollIntervalMs,
    totalPollRuns: 0,
    lastPollAt: null,
    lastSummary: null,
    totalSolanaReconciled: 0,
    totalOfframpReconciled: 0,
    totalQuotesExpired: 0,
    consecutiveFailures: 0
  };

  constructor(options?: {
    store?: IPaymentStore;
    pipeline?: PaymentPipelineService;
    connectionSupplier?: () => Connection;
    pollIntervalMs?: number;
    settlementTimeoutMs?: number;
  }) {
    this.store = options?.store ?? globalPaymentStore;
    this.pipeline = options?.pipeline ?? globalPipelineService;
    this.connectionSupplier = options?.connectionSupplier ?? getConnection;
    this.pollIntervalMs = options?.pollIntervalMs ?? env.pollIntervalMs;
    this.settlementTimeoutMs = options?.settlementTimeoutMs ?? DEFAULT_SETTLEMENT_TIMEOUT_MS;
    this.metrics.pollIntervalMs = this.pollIntervalMs;
  }

  // Starts the background polling interval.
  start(intervalMs?: number): void {
    if (this.timer) {
      this.stop();
    }
    if (intervalMs) {
      this.pollIntervalMs = intervalMs;
      this.metrics.pollIntervalMs = intervalMs;
    }
    this.metrics.isRunning = true;

    this.timer = setInterval(() => {
      this.pollOnce().catch((err) => {
        console.error("[poller] Background poll run encountered uncaught error:", err);
      });
    }, this.pollIntervalMs);

    if (typeof this.timer.unref === "function") {
      this.timer.unref();
    }
  }

  // Stops the background polling loop.
  stop(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.metrics.isRunning = false;
  }

  getMetrics(): PollerMetrics {
    return { ...this.metrics };
  }

  // Executes a single reconciliation sweep across all active payments.
  async pollOnce(): Promise<PollSummary> {
    if (this.isPolling) {
      return {
        timestamp: new Date().toISOString(),
        durationMs: 0,
        activePaymentsScanned: 0,
        solanaReconciled: 0,
        offrampReconciled: 0,
        quotesExpired: 0,
        errors: ["Poll already in progress; skipped overlapping cycle."]
      };
    }

    this.isPolling = true;
    const startMs = Date.now();
    const summary: PollSummary = {
      timestamp: new Date().toISOString(),
      durationMs: 0,
      activePaymentsScanned: 0,
      solanaReconciled: 0,
      offrampReconciled: 0,
      quotesExpired: 0,
      errors: []
    };

    try {
      const allPayments = await this.store.listPayments(100);
      summary.activePaymentsScanned = allPayments.length;

      for (const payment of allPayments) {
        try {
          // Phase 1: Reconcile Solana Settlements
          if (payment.status === "SETTLEMENT_SUBMITTED" || payment.status === "SETTLEMENT_PENDING") {
            const didReconcile = await this.reconcileSolanaSettlement(payment);
            if (didReconcile) {
              summary.solanaReconciled++;
              this.metrics.totalSolanaReconciled++;
            }
          }

          // Phase 2: Reconcile Off-Ramp Orders
          if (payment.status === "OFFRAMP_PROCESSING" || payment.status === "FIAT_PAYOUT_PENDING") {
            const result = await this.pipeline.reconcilePayment(payment.id);
            if (result.reconciled) {
              summary.offrampReconciled++;
              this.metrics.totalOfframpReconciled++;
            }
          }

          // Phase 3: Reconcile Expired Quotes
          if (payment.status === "AWAITING_CONFIRMATION" || payment.status === "QUOTE_PENDING") {
            if (payment.quote && isQuoteExpired(payment.quote.expiresAt)) {
              payment.status = "QUOTE_EXPIRED";
              payment.failureReason = "Quote expired before sender confirmed.";
              const event: TimelineEvent = {
                id: generateEventId(),
                timestamp: new Date().toISOString(),
                status: "QUOTE_EXPIRED",
                title: "Quote Expired (via Poller)",
                description: payment.failureReason
              };
              payment.timeline.push(event);
              payment.updatedAt = event.timestamp;
              await this.store.savePayment(payment);

              summary.quotesExpired++;
              this.metrics.totalQuotesExpired++;
            }
          }
        } catch (err) {
          summary.errors.push(
            `Error polling payment ${payment.id}: ${err instanceof Error ? err.message : String(err)}`
          );
        }
      }

      this.metrics.consecutiveFailures = 0;
    } catch (err) {
      this.metrics.consecutiveFailures++;
      summary.errors.push(
        `Fatal error during poller sweep: ${err instanceof Error ? err.message : String(err)}`
      );
    } finally {
      summary.durationMs = Date.now() - startMs;
      this.metrics.totalPollRuns++;
      this.metrics.lastPollAt = summary.timestamp;
      this.metrics.lastSummary = summary;
      this.isPolling = false;
    }

    return summary;
  }

  private async reconcileSolanaSettlement(payment: Payment): Promise<boolean> {
    const signature = payment.blockchainTransaction?.transactionSignature;
    const now = Date.now();
    const updatedTime = new Date(payment.updatedAt).getTime();

    // 1. Check for timeout if transaction is stuck in submitted
    if (payment.status === "SETTLEMENT_SUBMITTED" && now - updatedTime > this.settlementTimeoutMs) {
      payment.status = "SETTLEMENT_FAILED";
      payment.failureReason = "Settlement timed out awaiting on-chain confirmation.";
      const failEvent: TimelineEvent = {
        id: generateEventId(),
        timestamp: new Date().toISOString(),
        status: "SETTLEMENT_FAILED",
        title: "Settlement Timeout (via Poller)",
        description: payment.failureReason,
        metadata: { signature }
      };
      payment.timeline.push(failEvent);
      payment.updatedAt = failEvent.timestamp;
      await this.store.savePayment(payment);
      return true;
    }

    if (!signature) {
      return false;
    }

    // 2. Query cluster for transaction signature status
    try {
      const conn = this.connectionSupplier();
      const statusResponse = await conn.getSignatureStatus(signature, {
        searchTransactionHistory: true
      });

      const val = statusResponse?.value;
      if (!val) {
        return false; // Still pending or not yet processed
      }

      if (val.err) {
        payment.status = "SETTLEMENT_FAILED";
        payment.failureReason = `Solana transaction failed on-chain: ${JSON.stringify(val.err)}`;
        const failEvent: TimelineEvent = {
          id: generateEventId(),
          timestamp: new Date().toISOString(),
          status: "SETTLEMENT_FAILED",
          title: "Solana Settlement Failed (via Poller)",
          description: payment.failureReason,
          metadata: { signature, error: val.err }
        };
        payment.timeline.push(failEvent);
        payment.updatedAt = failEvent.timestamp;
        await this.store.savePayment(payment);
        return true;
      }

      if (val.confirmationStatus === "confirmed" || val.confirmationStatus === "finalized") {
        const confirmedAt = new Date().toISOString();
        if (payment.blockchainTransaction) {
          payment.blockchainTransaction.confirmationStatus = "confirmed";
          payment.blockchainTransaction.slot = val.slot;
          payment.blockchainTransaction.confirmedAt = confirmedAt;
        }

        payment.status = "SETTLEMENT_CONFIRMED";
        const confirmEvent: TimelineEvent = {
          id: generateEventId(),
          timestamp: confirmedAt,
          status: "SETTLEMENT_CONFIRMED",
          title: "Solana Settlement Confirmed (via Poller)",
          description: `Transaction confirmed at slot ${val.slot}. Signature: ${signature.substring(0, 12)}...`,
          metadata: { signature, slot: val.slot }
        };
        payment.timeline.push(confirmEvent);
        payment.updatedAt = confirmedAt;
        await this.store.savePayment(payment);

        // Hand off to off-ramp creation
        try {
          await this.pipeline.initiateOffRamp(payment.id);
        } catch {
          // Off-ramp poller will retry if initial trigger fails
        }
        return true;
      }
    } catch {
      // Network/RPC glitch, will retry on next poll interval
      return false;
    }

    return false;
  }
}

export const globalPaymentPollerService = new PaymentPollerService();
