import { Router } from "express";
import { globalPipelineService, DEFAULT_DEMO_SENDER_WALLET } from "../services/paymentPipeline.js";
import { globalPaymentStore } from "../services/paymentStore.js";
import { computeOffRampQuote, validateIndianRecipient } from "../lib/index.js";

export const paymentRouter = Router();

// In-memory sliding-window rate limiter for payment creation (PRD Section 26)
const paymentRateLimits = new Map<string, { count: number; resetAt: number }>();
const MAX_PAYMENTS_PER_MINUTE = 60;
const RATE_WINDOW_MS = 60 * 1000;

export function checkPaymentRateLimit(key: string, limit = MAX_PAYMENTS_PER_MINUTE): boolean {
  const now = Date.now();
  const entry = paymentRateLimits.get(key);
  if (!entry || now > entry.resetAt) {
    paymentRateLimits.set(key, { count: 1, resetAt: now + RATE_WINDOW_MS });
    return true;
  }
  if (entry.count >= limit) {
    return false;
  }
  entry.count++;
  return true;
}

export function clearPaymentRateLimits(): void {
  paymentRateLimits.clear();
}

// POST /api/v1/payments - Create payment intent (PRD Section 4, 6, 20, 26)
paymentRouter.post("/api/v1/payments", async (req, res, next) => {
  try {
    const clientKey = (req.headers["x-forwarded-for"] as string) || req.ip || req.body?.senderWallet || "default";
    if (!checkPaymentRateLimit(clientKey)) {
      res.status(429).json({ error: "Rate limit exceeded. Too many payment creation requests. Try again in 1 minute." });
      return;
    }

    const idempotencyKey = (req.headers["idempotency-key"] as string) || req.body.idempotencyKey;
    const { senderWallet, sourceAmount, recipient, mode, senderUsdcBalance } = req.body;

    if (!senderWallet) {
      res.status(400).json({ error: "senderWallet is required." });
      return;
    }
    if (!recipient || !recipient.name || !recipient.phone) {
      res.status(400).json({ error: "recipient with name and phone is required." });
      return;
    }
    if (!sourceAmount || Number(sourceAmount) <= 0) {
      res.status(400).json({ error: "sourceAmount must be a positive number." });
      return;
    }

    const payment = await globalPipelineService.createPayment({
      senderWallet,
      sourceAmount: Number(sourceAmount),
      recipient,
      mode,
      idempotencyKey,
      senderUsdcBalance: senderUsdcBalance !== undefined ? Number(senderUsdcBalance) : undefined
    });

    res.status(201).json({ payment });
  } catch (err) {
    next(err);
  }
});

// GET /api/v1/payments/:id - Get payment details & timeline (PRD Section 4, 28)
paymentRouter.get("/api/v1/payments/:id", async (req, res, next) => {
  try {
    const payment = await globalPaymentStore.getPayment(req.params.id);
    if (!payment) {
      res.status(404).json({ error: `Payment not found: ${req.params.id}` });
      return;
    }
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// GET /api/v1/payments - List transaction history (PRD Section 4)
paymentRouter.get("/api/v1/payments", async (req, res, next) => {
  try {
    const limit = Number(req.query.limit || 50);
    const payments = await globalPaymentStore.listPayments(limit);
    res.json({ payments });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/quote - Request / Refresh quote (PRD Section 7)
paymentRouter.post("/api/v1/payments/:id/quote", async (req, res, next) => {
  try {
    const payment = await globalPipelineService.refreshQuote(req.params.id);
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/confirm - Confirm payment (PRD Section 8)
paymentRouter.post("/api/v1/payments/:id/confirm", async (req, res, next) => {
  try {
    const payment = await globalPipelineService.confirmPayment(req.params.id);
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/settle - Settle on Solana (PRD Section 9, 10)
paymentRouter.post("/api/v1/payments/:id/settle", async (req, res, next) => {
  try {
    const { transactionSignature } = req.body || {};
    const payment = await globalPipelineService.settleSolana(req.params.id, transactionSignature);
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/offramp - Initiate off-ramp order & submit tx hash (PRD Section 11, 12)
paymentRouter.post("/api/v1/payments/:id/offramp", async (req, res, next) => {
  try {
    const payment = await globalPipelineService.initiateOffRamp(req.params.id);
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/execute - End-to-end pipeline execution (PRD Section 29, 30)
paymentRouter.post("/api/v1/payments/execute", async (req, res, next) => {
  try {
    const clientKey = (req.headers["x-forwarded-for"] as string) || req.ip || req.body?.senderWallet || "default";
    if (!checkPaymentRateLimit(clientKey)) {
      res.status(429).json({ error: "Rate limit exceeded. Too many payment creation requests. Try again in 1 minute." });
      return;
    }

    const idempotencyKey = (req.headers["idempotency-key"] as string) || req.body.idempotencyKey;
    const { senderWallet, sourceAmount, recipient, mode, senderUsdcBalance } = req.body;

    const payment = await globalPipelineService.executeFullPipeline({
      senderWallet: senderWallet || DEFAULT_DEMO_SENDER_WALLET,
      sourceAmount: Number(sourceAmount || 100),
      recipient: recipient || {
        name: "Aarav Sharma",
        phone: "+919876543210",
        upiId: "aarav@oksbi"
      },
      mode: mode || "full_simulation",
      idempotencyKey,
      senderUsdcBalance: senderUsdcBalance !== undefined ? Number(senderUsdcBalance) : undefined
    });

    res.status(201).json({ payment });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/simulate-step - Step-by-step simulator for Demo Console (PRD Section 23)
paymentRouter.post("/api/v1/payments/:id/simulate-step", async (req, res, next) => {
  try {
    const { step } = req.body;
    if (!step) {
      res.status(400).json({ error: "step is required (confirm | simulate_usdc_received | settle | confirm_solana | create_offramp | payout_processing | payout_success | reconcile)." });
      return;
    }
    const payment = await globalPipelineService.simulateStep(req.params.id, step);
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/reconcile - Reconcile payment status against off-ramp provider (PRD Section 25, 32)
paymentRouter.post("/api/v1/payments/:id/reconcile", async (req, res, next) => {
  try {
    const result = await globalPipelineService.reconcilePayment(req.params.id);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/payments/:id/fail - Failure injection for testing (PRD Section 25)
paymentRouter.post("/api/v1/payments/:id/fail", async (req, res, next) => {
  try {
    const { failureType } = req.body;
    if (!failureType) {
      res.status(400).json({ error: "failureType is required (quote_expired | solana_failed | offramp_failed | payout_failed)." });
      return;
    }
    const payment = await globalPipelineService.injectFailure(req.params.id, failureType);
    res.json({ payment });
  } catch (err) {
    next(err);
  }
});

// GET /api/v1/quote - Direct quote preview
paymentRouter.get("/api/v1/quote", (req, res) => {
  const amount = Number(req.query.amount || 100);
  const quote = computeOffRampQuote({ sourceAmount: amount });
  res.json({ quote });
});

// POST /api/v1/recipients/validate - Validate recipient details
paymentRouter.post("/api/v1/recipients/validate", (req, res) => {
  const check = validateIndianRecipient(req.body || {});
  res.json({ check });
});

// GET /api/v1/recipients - List recipients preview
paymentRouter.get("/api/v1/recipients", (req, res) => {
  const q = req.query.q ? String(req.query.q).toLowerCase().trim() : "";
  let list = [
    {
      id: "rec_priya",
      name: "Priya Sharma",
      phone: "+919876543210",
      upiId: "priya.sharma@oksbi",
      avatarInitials: "PS",
      country: "IN"
    },
    {
      id: "rec_rahul",
      name: "Rahul Verma",
      phone: "+919876543211",
      upiId: "rahul.verma@oksbi",
      avatarInitials: "RV",
      country: "IN"
    },
    {
      id: "rec_sarah",
      name: "Sarah Smith",
      phone: "+919876543212",
      upiId: "sarah.smith@oksbi",
      avatarInitials: "SS",
      country: "IN"
    }
  ];
  if (q) {
    list = list.filter(
      (r) =>
        r.name.toLowerCase().includes(q) ||
        r.phone.includes(q) ||
        (r.upiId && r.upiId.toLowerCase().includes(q))
    );
  }
  res.json({ recipients: list });
});

// GET /api/v1/receiver/dashboard - Receiver dashboard view of incoming pipeline payments
paymentRouter.get("/api/v1/receiver/dashboard", async (req, res, next) => {
  try {
    const q = req.query.recipient ? String(req.query.recipient).toLowerCase().trim() : "";
    let payments = await globalPaymentStore.listPayments(50);
    if (q) {
      payments = payments.filter(
        (p) =>
          p.recipient.name.toLowerCase().includes(q) ||
          (p.recipient.upiId && p.recipient.upiId.toLowerCase().includes(q))
      );
    }
    const completed = payments.filter((p) => p.status === "COMPLETED");
    const totalReceivedInr = completed.reduce((sum, p) => sum + (p.destinationAmount || 0), 0);
    const totalReceivedUsd = completed.reduce((sum, p) => sum + (p.sourceAmount || 0), 0);

    res.json({
      totalReceivedInr,
      totalReceivedUsd,
      count: payments.length,
      payments
    });
  } catch (err) {
    next(err);
  }
});
