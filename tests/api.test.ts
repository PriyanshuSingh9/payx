import { describe, it, before, after } from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import crypto from "node:crypto";
import { app } from "../backend/src/server.js";
import { checkPaymentRateLimit, clearPaymentRateLimits } from "../backend/src/routes/payments.js";

const VALID_SOLANA_WALLET = "7xK999999999999999999999999999999999999992PD";

describe("PayX HTTP API & Dev Console Integration Suite", () => {
  let server: http.Server;
  let baseUrl: string;

  before(async () => {
    await new Promise<void>((resolve) => {
      server = http.createServer(app);
      server.listen(0, "127.0.0.1", () => {
        const addr = server.address() as { port: number };
        baseUrl = `http://127.0.0.1:${addr.port}`;
        resolve();
      });
    });
  });

  after(async () => {
    await new Promise<void>((resolve, reject) => {
      server.close((err) => (err ? reject(err) : resolve()));
    });
  });

  it("GET /health returns 200 ok", async () => {
    const res = await fetch(`${baseUrl}/health`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { ok: boolean; service: string };
    assert.equal(body.ok, true);
    assert.equal(body.service, "payx-backend");
  });

  it("GET / serves the PayX Dashboard & Dev Console UI", async () => {
    const res = await fetch(`${baseUrl}/`);
    assert.equal(res.status, 200);
    const html = await res.text();
    assert.ok(html.includes("PAYX DEV CONSOLE"));
    assert.ok(html.includes("USDC to INR Pipeline"));
  });

  it("GET /api/v1/quote calculates pure off-ramp quote", async () => {
    const res = await fetch(`${baseUrl}/api/v1/quote?amount=100`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { quote: { recipientAmount: number; exchangeRate: number } };
    assert.equal(body.quote.exchangeRate, 87.20);
    assert.equal(body.quote.recipientAmount, 8676.40);
  });

  it("POST /api/v1/payments creates payment intent with quote locked", async () => {
    const res = await fetch(`${baseUrl}/api/v1/payments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: {
          name: "Priya Sharma",
          phone: "+919876543210",
          upiId: "priya@oksbi"
        }
      })
    });

    assert.equal(res.status, 201);
    const body = (await res.json()) as { payment: { id: string; status: string; quote: object } };
    assert.match(body.payment.id, /^PX-[0-9A-F]{6}$/);
    assert.equal(body.payment.status, "AWAITING_CONFIRMATION");
    assert.ok(body.payment.quote);
  });

  it("POST /api/v1/payments/execute runs the full 10-stage pipeline to COMPLETED", async () => {
    const res = await fetch(`${baseUrl}/api/v1/payments/execute`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 100,
        recipient: {
          name: "Rahul Verma",
          phone: "+919812345678",
          upiId: "rahul@paytm"
        }
      })
    });

    assert.equal(res.status, 201);
    const body = (await res.json()) as {
      payment: {
        id: string;
        status: string;
        blockchainTransaction: { confirmationStatus: string; transactionSignature: string };
        offRampOrder: { status: string; payoutReference: string };
      };
    };

    assert.equal(body.payment.status, "COMPLETED");
    assert.equal(body.payment.blockchainTransaction.confirmationStatus, "confirmed");
    assert.ok(body.payment.blockchainTransaction.transactionSignature);
    assert.equal(body.payment.offRampOrder.status, "payoutSuccess");
    assert.ok(body.payment.offRampOrder.payoutReference);
  });

  it("POST /api/v1/payments/:id/simulate-step steps through Dev Console controls", async () => {
    // 1. Create
    const createRes = await fetch(`${baseUrl}/api/v1/payments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 50,
        recipient: { name: "Aarav", phone: "+919876543210", upiId: "aarav@upi" }
      })
    });
    const { payment } = (await createRes.json()) as { payment: { id: string } };

    // 2. Confirm
    const confirmRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "confirm" })
    });
    const confirmData = (await confirmRes.json()) as { payment: { status: string } };
    assert.equal(confirmData.payment.status, "SETTLEMENT_PENDING");

    // 3. Settle
    const settleRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "settle" })
    });
    const settleData = (await settleRes.json()) as { payment: { status: string } };
    assert.equal(settleData.payment.status, "SETTLEMENT_CONFIRMED");

    // 4. Create Offramp
    const offrampRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "create_offramp" })
    });
    const offrampData = (await offrampRes.json()) as { payment: { status: string } };
    assert.equal(offrampData.payment.status, "OFFRAMP_PROCESSING");

    // 5. Payout Processing
    const procRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "payout_processing" })
    });
    const procData = (await procRes.json()) as { payment: { status: string } };
    assert.equal(procData.payment.status, "FIAT_PAYOUT_PENDING");

    // 6. Payout Success
    const succRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "payout_success" })
    });
    const succData = (await succRes.json()) as { payment: { status: string } };
    assert.equal(succData.payment.status, "COMPLETED");
  });

  it("POST /api/v1/payments/:id/fail injects failure states properly", async () => {
    const createRes = await fetch(`${baseUrl}/api/v1/payments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 50,
        recipient: { name: "Aarav", phone: "+919876543210", upiId: "aarav@upi" }
      })
    });
    const { payment } = (await createRes.json()) as { payment: { id: string } };

    const failRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/fail`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ failureType: "offramp_failed" })
    });
    const failData = (await failRes.json()) as { payment: { status: string; failureReason: string } };
    assert.equal(failData.payment.status, "OFFRAMP_FAILED");
    assert.ok(failData.payment.failureReason);
  });

  it("POST /webhooks/onmeta handles duplicate webhooks idempotently", async () => {
    const eventId = "EVT-HTTP-DUP-001";
    const res1 = await fetch(`${baseUrl}/webhooks/onmeta`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ eventId, status: "payoutSuccess", orderId: "ORD-NONEXISTENT" })
    });
    assert.equal(res1.status, 404); // no payment associated

    // Now test with valid payment
    const fullRes = await fetch(`${baseUrl}/api/v1/payments/execute`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 25,
        recipient: { name: "Demo User", phone: "+919876543210", upiId: "demo@upi" }
      })
    });
    const { payment } = (await fullRes.json()) as { payment: { offRampOrder: { providerOrderId: string } } };
    const orderId = payment.offRampOrder.providerOrderId;

    const res2 = await fetch(`${baseUrl}/webhooks/onmeta`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ eventId: "EVT-REAL-001", status: "payoutSuccess", orderId })
    });
    const body2 = (await res2.json()) as { duplicate: boolean; ok: boolean };
    assert.equal(body2.ok, true);
    assert.equal(body2.duplicate, false);

    // Duplicate call
    const res3 = await fetch(`${baseUrl}/webhooks/onmeta`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ eventId: "EVT-REAL-001", status: "payoutSuccess", orderId })
    });
    const body3 = (await res3.json()) as { duplicate: boolean; ok: boolean };
    assert.equal(body3.ok, true);
    assert.equal(body3.duplicate, true);
  });

  it("POST /api/v1/payments/execute works with default demo wallet without validation errors", async () => {
    const res = await fetch(`${baseUrl}/api/v1/payments/execute`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        sourceAmount: 100,
        recipient: {
          name: "Canonical User",
          phone: "+919876543210",
          upiId: "canonical@upi"
        }
      })
    });

    assert.equal(res.status, 201);
    const data = (await res.json()) as { payment: { id: string; status: string; senderWallet: string } };
    assert.equal(data.payment.status, "COMPLETED");
    assert.equal(data.payment.senderWallet, VALID_SOLANA_WALLET);
  });

  it("POST /api/v1/payments/:id/reconcile successfully resolves status when webhook is delayed", async () => {
    // 1. Create and step to offramp
    const createRes = await fetch(`${baseUrl}/api/v1/payments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 50,
        recipient: { name: "Aarav", phone: "+919876543210", upiId: "aarav@upi" }
      })
    });
    const { payment } = (await createRes.json()) as { payment: { id: string } };

    await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "confirm" })
    });

    await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "settle" })
    });

    await fetch(`${baseUrl}/api/v1/payments/${payment.id}/simulate-step`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ step: "create_offramp" })
    });

    // 2. Call reconcile route (PRD Section 25 & 32)
    const recRes = await fetch(`${baseUrl}/api/v1/payments/${payment.id}/reconcile`, {
      method: "POST"
    });
    assert.equal(recRes.status, 200);
    const recData = (await recRes.json()) as { reconciled: boolean; payment: { status: string } };
    assert.ok(recData.payment);
  });

  it("POST /api/v1/payments enforces rate limiting when threshold exceeded", async () => {
    clearPaymentRateLimits();
    const testIp = "test-rate-limit-ip";

    // Exhaust rate limit
    for (let i = 0; i < 60; i++) {
      checkPaymentRateLimit(testIp, 60);
    }

    const res = await fetch(`${baseUrl}/api/v1/payments`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-forwarded-for": testIp
      },
      body: JSON.stringify({
        senderWallet: VALID_SOLANA_WALLET,
        sourceAmount: 10,
        recipient: { name: "Test User", phone: "+919876543210", upiId: "test@upi" }
      })
    });

    assert.equal(res.status, 429);
    const body = (await res.json()) as { error: string };
    assert.match(body.error, /Rate limit exceeded/i);
    clearPaymentRateLimits();
  });

  it("POST /webhooks/onmeta enforces signature verification when secret is configured", async () => {
    const originalSecret = process.env["ONMETA_WEBHOOK_SECRET"];
    try {
      process.env["ONMETA_WEBHOOK_SECRET"] = "secure_test_webhook_secret_key_123";

      // 1. Missing signature header -> 401
      const res1 = await fetch(`${baseUrl}/webhooks/onmeta`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ eventId: "EVT-SIG-1", status: "payoutSuccess" })
      });
      assert.equal(res1.status, 401);

      // 2. Invalid / malformed signature -> 401 without 500 crash
      const res2 = await fetch(`${baseUrl}/webhooks/onmeta`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-onmeta-signature": "short_invalid_sig"
        },
        body: JSON.stringify({ eventId: "EVT-SIG-2", status: "payoutSuccess" })
      });
      assert.equal(res2.status, 401);

      // 3. Valid HMAC-SHA256 signature
      const payload = JSON.stringify({
        eventId: "EVT-SIG-VALID",
        status: "payoutSuccess",
        orderId: "ORD-TEST-DUMMY"
      });
      const validSig = crypto
        .createHmac("sha256", "secure_test_webhook_secret_key_123")
        .update(payload)
        .digest("hex");

      const res3 = await fetch(`${baseUrl}/webhooks/onmeta`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-onmeta-signature": validSig
        },
        body: payload
      });

      // Signature was valid; order doesn't exist so 404 (not 401 unauthorized or 500 error)
      assert.notEqual(res3.status, 401);
      assert.notEqual(res3.status, 500);
      assert.equal(res3.status, 404);
    } finally {
      if (originalSecret) {
        process.env["ONMETA_WEBHOOK_SECRET"] = originalSecret;
      } else {
        delete process.env["ONMETA_WEBHOOK_SECRET"];
      }
    }
  });
});
