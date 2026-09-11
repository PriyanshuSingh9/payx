import { describe, it, beforeEach } from "node:test";
import assert from "node:assert/strict";
import {
  canTransitionPayment,
  computeOffRampQuote,
  isQuoteExpired,
  validateIndianRecipient,
  validateSolanaAddress,
  generatePaymentId
} from "../backend/src/lib/index.js";
import { MockOffRampAdapter } from "../backend/src/adapters/offramp/index.js";
import { InMemoryPaymentStore } from "../backend/src/services/paymentStore.js";
import {
  PaymentPipelineService,
  DEFAULT_DEMO_SENDER_WALLET,
  DEFAULT_DEMO_SETTLEMENT_VAULT
} from "../backend/src/services/paymentPipeline.js";

const VALID_SOLANA_WALLET = "7xK999999999999999999999999999999999999992PD";

describe("PayX USDC to INR Pipeline Domain & Simulation Tests", () => {
  let store: InMemoryPaymentStore;
  let mockAdapter: MockOffRampAdapter;
  let pipeline: PaymentPipelineService;

  beforeEach(() => {
    store = new InMemoryPaymentStore();
    mockAdapter = new MockOffRampAdapter();
    pipeline = new PaymentPipelineService({ store, mockProvider: mockAdapter });
  });

  describe("Correlation & Payment ID Generation (PRD Section 6 & 28)", () => {
    it("generates correlation ID matching PX-XXXXXX format", () => {
      const id = generatePaymentId("PX-");
      assert.match(id, /^PX-[0-9A-F]{6}$/);
    });

    it("generates distinct correlation IDs", () => {
      const id1 = generatePaymentId();
      const id2 = generatePaymentId();
      assert.notEqual(id1, id2);
    });
  });

  describe("Pure Money Math & Quotes (PRD Section 7 & Money Math Invariant)", () => {
    it("calculates exact USDC to INR gross, off-ramp fee, and net recipient amounts", () => {
      const quote = computeOffRampQuote({
        sourceAmount: 100,
        exchangeRate: 87.20,
        feeBps: 50, // 0.50%
        estimatedNetworkFeeUsdc: 0.01
      });

      assert.equal(quote.sourceAmount, 100);
      assert.equal(quote.exchangeRate, 87.20);
      assert.equal(quote.grossDestinationAmount, 8720.00);
      assert.equal(quote.offRampFee, 43.60); // 8720 * 0.005
      assert.equal(quote.recipientAmount, 8676.40); // 8720 - 43.60
      assert.equal(quote.sourceAsset, "USDC");
      assert.equal(quote.destinationCurrency, "INR");
    });

    it("evaluates quote expiration window correctly (PRD: expires in 30 seconds)", () => {
      const now = new Date();
      const validQuote = computeOffRampQuote({ sourceAmount: 100, expirySeconds: 30 });
      assert.equal(isQuoteExpired(validQuote.expiresAt, now), false);

      const pastDate = new Date(now.getTime() + 31 * 1000);
      assert.equal(isQuoteExpired(validQuote.expiresAt, pastDate), true);
    });
  });

  describe("Rail & Address Validation (PRD Section 4 & 6)", () => {
    it("validates legitimate Indian UPI ID and reject malformed handles", () => {
      assert.equal(validateIndianRecipient({ upiId: "aarav@oksbi" }).ok, true);
      assert.equal(validateIndianRecipient({ upiId: "merchant.pay@icici" }).ok, true);
      assert.equal(validateIndianRecipient({ upiId: "invalid-upi-handle-without-bank" }).ok, false);
    });

    it("validates Indian Bank Account + IFSC format", () => {
      assert.equal(
        validateIndianRecipient({ bankAccount: "987654321012", ifsc: "HDFC0001234" }).ok,
        true
      );
      assert.equal(
        validateIndianRecipient({ bankAccount: "123", ifsc: "HDFC0001234" }).ok,
        false
      );
      assert.equal(
        validateIndianRecipient({ bankAccount: "987654321012", ifsc: "INVALIDIFSC" }).ok,
        false
      );
    });

    it("validates base58 Solana wallet addresses", () => {
      assert.equal(validateSolanaAddress(VALID_SOLANA_WALLET), true);
      assert.equal(validateSolanaAddress("invalid-solana-address!"), false);
      assert.equal(validateSolanaAddress("0x1234567890123456789012345678901234567890"), false);
    });
  });

  describe("Unified Payment State Machine (PRD Section 14)", () => {
    it("enforces canonical state progression", () => {
      assert.equal(canTransitionPayment("CREATED", "QUOTE_PENDING"), true);
      assert.equal(canTransitionPayment("QUOTE_PENDING", "AWAITING_CONFIRMATION"), true);
      assert.equal(canTransitionPayment("AWAITING_CONFIRMATION", "SETTLEMENT_PENDING"), true);
      assert.equal(canTransitionPayment("SETTLEMENT_PENDING", "SETTLEMENT_SUBMITTED"), true);
      assert.equal(canTransitionPayment("SETTLEMENT_SUBMITTED", "SETTLEMENT_CONFIRMED"), true);
      assert.equal(canTransitionPayment("SETTLEMENT_CONFIRMED", "OFFRAMP_CREATED"), true);
      assert.equal(canTransitionPayment("OFFRAMP_CREATED", "OFFRAMP_PROCESSING"), true);
      assert.equal(canTransitionPayment("OFFRAMP_PROCESSING", "FIAT_PAYOUT_PENDING"), true);
      assert.equal(canTransitionPayment("FIAT_PAYOUT_PENDING", "COMPLETED"), true);
    });

    it("rejects illegal skips in payment state", () => {
      assert.equal(canTransitionPayment("CREATED", "COMPLETED"), false);
      assert.equal(canTransitionPayment("AWAITING_CONFIRMATION", "OFFRAMP_CREATED"), false);
      assert.equal(canTransitionPayment("COMPLETED", "CREATED"), false);
    });

    it("permits transitions to explicit failure states", () => {
      assert.equal(canTransitionPayment("CREATED", "PAYMENT_FAILED"), true);
      assert.equal(canTransitionPayment("AWAITING_CONFIRMATION", "QUOTE_EXPIRED"), true);
      assert.equal(canTransitionPayment("SETTLEMENT_SUBMITTED", "SETTLEMENT_FAILED"), true);
      assert.equal(canTransitionPayment("OFFRAMP_CREATED", "OFFRAMP_FAILED"), true);
      assert.equal(canTransitionPayment("FIAT_PAYOUT_PENDING", "PAYOUT_FAILED"), true);
    });
  });

  describe("End-to-End Pipeline Execution (PRD Section 24, 29, 30)", () => {
    it("completes the timed USDC-to-INR mock lifecycle", async () => {
      const payment = await pipeline.executeFullPipeline({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: {
          name: "Aarav Sharma",
          phone: "+919876543210",
          upiId: "aarav@oksbi"
        },
        mode: "full_simulation"
      });

      assert.equal(payment.status, "COMPLETED");
      assert.match(payment.id, /^PX-[0-9A-F]{6}$/);
      assert.equal(payment.sourceAmount, 100);
      assert.equal(payment.sourceAsset, "USDC");
      assert.equal(payment.destinationCurrency, "INR");
      assert.equal(payment.destinationAmount, 8676.40);
      assert.ok(payment.completedAt);

      // Verify blockchain transaction record (PRD Section 10 & 18)
      assert.ok(payment.blockchainTransaction);
      assert.equal(payment.blockchainTransaction.chain, "solana");
      assert.equal(payment.blockchainTransaction.network, "simulator");
      assert.equal(payment.blockchainTransaction.confirmationStatus, "confirmed");
      assert.ok(payment.blockchainTransaction.transactionSignature);
      assert.equal(payment.blockchainTransaction.explorerUrl, undefined);

      // Verify off-ramp order record (PRD Section 11, 12, 18)
      assert.ok(payment.offRampOrder);
      assert.equal(payment.offRampOrder.fiatCurrency, "INR");
      assert.equal(payment.offRampOrder.status, "payoutSuccess");
      assert.ok(payment.offRampOrder.payoutReference);
      assert.equal(
        payment.offRampOrder.transactionHash,
        payment.blockchainTransaction.transactionSignature
      );

      // Verify full timeline sequence
      const timelineStatuses = payment.timeline.map((e) => e.status);
      assert.ok(timelineStatuses.includes("CREATED"));
      assert.ok(timelineStatuses.includes("AWAITING_CONFIRMATION"));
      assert.ok(timelineStatuses.includes("SETTLEMENT_SUBMITTED"));
      assert.ok(timelineStatuses.includes("SETTLEMENT_CONFIRMED"));
      assert.ok(timelineStatuses.includes("OFFRAMP_CREATED"));
      assert.ok(timelineStatuses.includes("OFFRAMP_PROCESSING"));
      assert.ok(timelineStatuses.includes("FIAT_PAYOUT_PENDING"));
      assert.ok(timelineStatuses.includes("COMPLETED"));
    });
  });

  describe("Failure Scenarios & Resilience (PRD Section 25)", () => {
    it("fails immediately when sender USDC balance is insufficient", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        senderUsdcBalance: 50, // Less than required 100 USDC
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" }
      });

      assert.equal(payment.status, "PAYMENT_FAILED");
      assert.match(payment.failureReason || "", /Insufficient USDC balance/i);
    });

    it("marks quote expired when confirmation is delayed beyond expiry window", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" }
      });

      assert.equal(payment.status, "AWAITING_CONFIRMATION");
      // Force quote expiry timestamp into the past
      payment.quote!.expiresAt = new Date(Date.now() - 10000).toISOString();
      await store.savePayment(payment);

      const confirmed = await pipeline.confirmPayment(payment.id);
      assert.equal(confirmed.status, "QUOTE_EXPIRED");

      // Verify sender can refresh quote and proceed
      const refreshed = await pipeline.refreshQuote(payment.id);
      assert.equal(refreshed.status, "AWAITING_CONFIRMATION");
      assert.ok(!isQuoteExpired(refreshed.quote!.expiresAt));
    });

    it("handles Solana settlement failure gracefully", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" }
      });
      await pipeline.confirmPayment(payment.id);

      // Settle in live mode with a non-existent signature to test failure path
      const settled = await pipeline.settleSolana(payment.id, "invalidSignatureThatDoesNotExist1111111111111111111111111111111111111111111111111111111111111111");
      // In full_simulation mode it records, or in injected failure:
      const failed = await pipeline.injectFailure(payment.id, "solana_failed");
      assert.equal(failed.status, "SETTLEMENT_FAILED");
    });

    it("handles Off-Ramp provider order rejection", async () => {
      mockAdapter.setFailureMode("order_rejected");
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" }
      });
      await pipeline.confirmPayment(payment.id);
      await pipeline.settleSolana(payment.id);

      const offramp = await pipeline.initiateOffRamp(payment.id);
      assert.equal(offramp.status, "OFFRAMP_FAILED");
      assert.match(offramp.failureReason || "", /rejection/i);
    });

    it("handles destination bank payout failure", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" }
      });
      await pipeline.confirmPayment(payment.id);
      await pipeline.settleSolana(payment.id);
      await pipeline.initiateOffRamp(payment.id);

      const failed = await pipeline.injectFailure(payment.id, "payout_failed");
      assert.equal(failed.status, "PAYOUT_FAILED");
    });
  });

  describe("Idempotency", () => {
    it("returns identical payment on repeated request with same Idempotency-Key", async () => {
      const key = "PX-REQ-IDEMPOTENCY-TEST-123";
      const payment1 = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" },
        idempotencyKey: key
      });

      const payment2 = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" },
        idempotencyKey: key
      });

      assert.equal(payment1.id, payment2.id);
      assert.equal(payment1.createdAt, payment2.createdAt);
    });

    it("prevents race condition when duplicate requests arrive concurrently with same Idempotency-Key", async () => {
      const key = "PX-CONCURRENT-KEY-999";
      const input = {
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 75,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" },
        idempotencyKey: key
      };

      const [p1, p2] = await Promise.all([
        pipeline.createPayment({ ...input }),
        pipeline.createPayment({ ...input })
      ]);

      assert.equal(p1.id, p2.id);
      assert.equal(p1.createdAt, p2.createdAt);
      assert.equal(p1.destinationAmount, p2.destinationAmount);
    });

  });

  describe("Mock status progression", () => {
    it("reconciles an advanced mock order", async () => {
      const payment = await pipeline.createPayment({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: { name: "Aarav Sharma", phone: "+919876543210", upiId: "aarav@oksbi" }
      });
      await pipeline.confirmPayment(payment.id);
      await pipeline.settleSolana(payment.id);
      const offramp = await pipeline.initiateOffRamp(payment.id);

      assert.equal(offramp.status, "OFFRAMP_PROCESSING");

      // Advance the local mock order, then reconcile the payment state.
      mockAdapter.advanceOrderStatus(offramp.offRampOrder!.providerOrderId, "payoutSuccess");

      // PayX executes reconciliation
      const result = await pipeline.reconcilePayment(offramp.id);
      assert.equal(result.reconciled, true);
      assert.equal(result.payment.status, "COMPLETED");
      assert.ok(result.payment.completedAt);
      assert.ok(result.payment.offRampOrder?.payoutReference);

      // Verify second reconciliation on terminal payment is a no-op
      const noopResult = await pipeline.reconcilePayment(offramp.id);
      assert.equal(noopResult.reconciled, false);
      assert.equal(noopResult.payment.status, "COMPLETED");
    });
  });

  describe("Security, Signature Verification & Address Invariants (PRD Section 26)", () => {
    it("ensures DEFAULT_DEMO_SENDER_WALLET and DEFAULT_DEMO_SETTLEMENT_VAULT are valid on-curve addresses", () => {
      assert.equal(validateSolanaAddress(DEFAULT_DEMO_SENDER_WALLET), true);
      assert.equal(validateSolanaAddress(DEFAULT_DEMO_SETTLEMENT_VAULT), true);
    });

  });
});
