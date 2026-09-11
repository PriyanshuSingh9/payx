import { describe, it, beforeEach, afterEach } from "node:test";
import assert from "node:assert/strict";
import { Connection } from "@solana/web3.js";
import { InMemoryPaymentStore } from "../backend/src/services/paymentStore.js";
import { PaymentPipelineService } from "../backend/src/services/paymentPipeline.js";
import { MockOffRampAdapter } from "../backend/src/adapters/offramp/index.js";
import { HeliusWebhookService, type HeliusEnhancedTransaction } from "../backend/src/services/heliusWebhook.js";
import { PaymentPollerService } from "../backend/src/services/paymentPoller.js";

const VALID_SOLANA_WALLET = "7xK999999999999999999999999999999999999992PD";

describe("Helius Webhooks & Payment Polling Architecture Suite", () => {
  let store: InMemoryPaymentStore;
  let mockAdapter: MockOffRampAdapter;
  let pipeline: PaymentPipelineService;
  let heliusService: HeliusWebhookService;
  let pollerService: PaymentPollerService;

  beforeEach(() => {
    store = new InMemoryPaymentStore();
    mockAdapter = new MockOffRampAdapter();
    pipeline = new PaymentPipelineService({ store, mockProvider: mockAdapter });
    heliusService = new HeliusWebhookService({
      store,
      pipeline,
      webhookSecret: "secret_helius_test_key_123"
    });
    pollerService = new PaymentPollerService({
      store,
      pipeline,
      pollIntervalMs: 50,
      settlementTimeoutMs: 100 // Short timeout for test
    });
  });

  afterEach(() => {
    pollerService.stop();
  });

  describe("Helius Webhook Authentication & Deduplication", () => {
    it("rejects unauthorized webhook when secret is required", () => {
      assert.equal(heliusService.verifyAuthorization(undefined), false);
      assert.equal(heliusService.verifyAuthorization("Bearer wrong_secret"), false);
      assert.equal(heliusService.verifyAuthorization("wrong_secret"), false);
    });

    it("authorizes valid webhook with raw secret or Bearer prefix", () => {
      assert.equal(heliusService.verifyAuthorization("secret_helius_test_key_123"), true);
      assert.equal(heliusService.verifyAuthorization("Bearer secret_helius_test_key_123"), true);
    });

    it("processes valid webhook and ignores duplicate delivery idempotently", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aditi Rao", phone: "+919876543210", upiId: "aditi@oksbi" }
      });
      await pipeline.confirmPayment(payment.id);

      const signature = "5wHuG8j9H3K7L8P9Q1R2S3T4U5V6W7X8Y9Z1A2B3C4D5E6F7G8H9J1K2L3M4N5P6Q7R8S9T1U2V3W4X5Y6Z7";
      const txPayload: HeliusEnhancedTransaction = {
        signature,
        slot: 496880000,
        timestamp: Math.floor(Date.now() / 1000),
        feePayer: VALID_SOLANA_WALLET,
        type: "TRANSFER",
        description: `Settlement deposit for ${payment.id}`,
        tokenTransfers: [
          {
            fromUserAccount: VALID_SOLANA_WALLET,
            toUserAccount: "4vvzXwGLvriT9WuDJmTBcwVxiebLE5z9YUVQ6SZwiSDc",
            tokenAmount: 100,
            tokenMint: "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU"
          }
        ]
      };

      // First webhook delivery
      const res1 = await heliusService.processWebhook([txPayload]);
      assert.equal(res1.processedCount, 1);
      assert.equal(res1.duplicatesSkipped, 0);
      assert.equal(res1.matchedPayments.length, 1);
      assert.equal(res1.matchedPayments[0].paymentId, payment.id);

      const updated1 = await store.getPayment(payment.id);
      assert.ok(updated1);
      // Confirmed on Solana and automatically initiated off-ramp
      assert.equal(updated1.status, "OFFRAMP_PROCESSING");
      assert.equal(updated1.blockchainTransaction?.transactionSignature, signature);
      assert.equal(updated1.blockchainTransaction?.confirmationStatus, "confirmed");

      // Second webhook delivery (duplicate)
      const res2 = await heliusService.processWebhook([txPayload]);
      assert.equal(res2.processedCount, 0);
      assert.equal(res2.duplicatesSkipped, 1);
      assert.equal(res2.matchedPayments.length, 0);
    });

    it("handles on-chain transaction error reported by Helius", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 50,
        recipient: { name: "Vikram Singh", phone: "+919876543210", upiId: "vikram@icici" }
      });
      await pipeline.confirmPayment(payment.id);

      const signature = "4kLmNoPqRsTuVwXyZ123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz123456789";
      const txPayload: HeliusEnhancedTransaction = {
        signature,
        slot: 496880005,
        timestamp: Math.floor(Date.now() / 1000),
        feePayer: VALID_SOLANA_WALLET,
        description: `Failed transfer for ${payment.id}`,
        transactionError: { InstructionError: [0, "Custom: 1"] }
      };

      const res = await heliusService.processWebhook([txPayload]);
      assert.equal(res.processedCount, 1);
      assert.equal(res.matchedPayments.length, 1);
      assert.equal(res.matchedPayments[0].status, "SETTLEMENT_FAILED");

      const updated = await store.getPayment(payment.id);
      assert.equal(updated?.status, "SETTLEMENT_FAILED");
      assert.ok(updated?.failureReason?.includes("Custom: 1"));
    });
  });

  describe("Polling Architecture Reconciler", () => {
    it("reconciles expired quotes across pending payments", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Test User", phone: "+919876543210", upiId: "test@upi" }
      });

      // Force quote to be expired in the past
      payment.quote!.expiresAt = new Date(Date.now() - 60000).toISOString();
      await store.savePayment(payment);

      const summary = await pollerService.pollOnce();
      assert.equal(summary.quotesExpired, 1);

      const updated = await store.getPayment(payment.id);
      assert.equal(updated?.status, "QUOTE_EXPIRED");
    });

    it("reconciles advanced off-ramp orders when webhooks are delayed", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Test Recipient", phone: "+919876543210", upiId: "rec@oksbi" }
      });
      await pipeline.confirmPayment(payment.id);
      await pipeline.settleSolana(payment.id);
      const offramp = await pipeline.initiateOffRamp(payment.id);

      assert.equal(offramp.status, "OFFRAMP_PROCESSING");

      // Provider advances asynchronously in background
      mockAdapter.advanceOrderStatus(offramp.offRampOrder!.providerOrderId, "payoutSuccess");

      // Poller runs reconciliation
      const summary = await pollerService.pollOnce();
      assert.equal(summary.offrampReconciled, 1);

      const completed = await store.getPayment(payment.id);
      assert.equal(completed?.status, "COMPLETED");
      assert.ok(completed?.completedAt);
    });

    it("marks settlement timed out if stuck in SETTLEMENT_SUBMITTED beyond timeout", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Slow Tx", phone: "+919876543210", upiId: "slow@bank" }
      });
      payment.status = "SETTLEMENT_SUBMITTED";
      // Simulate submitted 10 minutes ago
      payment.updatedAt = new Date(Date.now() - 600000).toISOString();
      payment.blockchainTransaction = {
        id: "BTX-MOCK-STUCK",
        paymentId: payment.id,
        chain: "solana",
        network: "devnet",
        token: "USDC",
        amount: 100,
        sender: payment.senderWallet,
        recipient: "4vvzXwGLvriT9WuDJmTBcwVxiebLE5z9YUVQ6SZwiSDc",
        transactionSignature: "sig_stuck_unconfirmed_signature",
        confirmationStatus: "pending",
        createdAt: payment.updatedAt
      };
      await store.savePayment(payment);

      const summary = await pollerService.pollOnce();
      assert.equal(summary.solanaReconciled, 1);

      const failed = await store.getPayment(payment.id);
      assert.equal(failed?.status, "SETTLEMENT_FAILED");
      assert.ok(failed?.failureReason?.includes("timed out"));
    });

    it("tracks poller metrics and lifecycle cleanly", async () => {
      const metricsBefore = pollerService.getMetrics();
      assert.equal(metricsBefore.isRunning, false);
      assert.equal(metricsBefore.totalPollRuns, 0);

      pollerService.start(20);
      assert.equal(pollerService.getMetrics().isRunning, true);

      // Wait for at least 2 ticks
      await new Promise<void>((resolve) => setTimeout(resolve, 80));

      const metricsAfter = pollerService.getMetrics();
      assert.ok(metricsAfter.totalPollRuns >= 1);
      assert.ok(metricsAfter.lastPollAt);

      pollerService.stop();
      assert.equal(pollerService.getMetrics().isRunning, false);
    });
  });
});
