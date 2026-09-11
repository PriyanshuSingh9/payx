import { Router } from "express";
import { globalPaymentPollerService } from "../services/paymentPoller.js";

export const pollerRouter = Router();

// GET /api/v1/poller/status - Observability metrics for background poller
pollerRouter.get("/api/v1/poller/status", (_req, res) => {
  const metrics = globalPaymentPollerService.getMetrics();
  res.json({ ok: true, metrics });
});

// POST /api/v1/poller/run - Manually trigger an immediate reconciliation sweep
pollerRouter.post("/api/v1/poller/run", async (_req, res, next) => {
  try {
    const summary = await globalPaymentPollerService.pollOnce();
    res.json({ ok: true, summary });
  } catch (err) {
    next(err);
  }
});
