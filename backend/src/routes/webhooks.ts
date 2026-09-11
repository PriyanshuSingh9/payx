import { Router } from "express";
import { globalHeliusWebhookService } from "../services/heliusWebhook.js";

export const webhookRouter = Router();

// GET /webhooks/helius/health - Endpoint verification check
webhookRouter.get("/webhooks/helius/health", (_req, res) => {
  res.json({ ok: true, webhook: "helius", timestamp: new Date().toISOString() });
});

// POST /webhooks/helius - Ingest enhanced Solana transaction events from Helius
webhookRouter.post("/webhooks/helius", async (req, res, next) => {
  try {
    const authHeader = req.headers["authorization"];
    if (!globalHeliusWebhookService.verifyAuthorization(authHeader)) {
      res.status(401).json({ error: "Unauthorized Helius webhook request." });
      return;
    }

    const result = await globalHeliusWebhookService.processWebhook(req.body);
    res.status(200).json(result);
  } catch (err) {
    next(err);
  }
});
