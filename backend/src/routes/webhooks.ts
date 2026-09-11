import { Router } from "express";
import { globalPipelineService } from "../services/paymentPipeline.js";
import { generateEventId } from "../lib/index.js";
import { OnmetaAdapter } from "../adapters/offramp/index.js";

export const webhookRouter = Router();

const onmetaAdapter = new OnmetaAdapter();

// POST /webhooks/onmeta - Primary Onmeta webhook endpoint (PRD Section 13, 19, 20)
webhookRouter.post("/webhooks/onmeta", async (req, res) => {
  try {
    const signature = req.headers["x-onmeta-signature"] as string | undefined;
    const eventId = (req.headers["x-event-id"] as string) || (req.body.eventId as string) || generateEventId("EVT-WEB-");
    const eventType = req.body.event || req.body.eventType || req.body.status || "unknown";

    // Signature verification (PRD Section 19 & 26)
    if (process.env["ONMETA_WEBHOOK_SECRET"]) {
      if (!signature) {
        res.status(401).json({ error: "Missing x-onmeta-signature header." });
        return;
      }
      const rawBody = (req as any).rawBody || JSON.stringify(req.body);
      const isValid = onmetaAdapter.verifyWebhookSignature(rawBody, signature, process.env["ONMETA_WEBHOOK_SECRET"]);
      if (!isValid) {
        res.status(401).json({ error: "Invalid webhook signature." });
        return;
      }
    }

    const result = await globalPipelineService.processWebhookEvent({
      eventId,
      provider: "onmeta",
      eventType,
      orderId: req.body.orderId || req.body.order_id,
      payload: req.body,
      receivedAt: new Date().toISOString(),
      status: "processed"
    });

    if (result.duplicate) {
      // 200 OK for idempotency (PRD Section 20)
      res.status(200).json({ ok: true, duplicate: true, message: "Event already processed." });
      return;
    }

    if (result.errorMessage && !result.payment) {
      res.status(404).json({ error: result.errorMessage });
      return;
    }

    res.status(200).json({ ok: true, duplicate: false, payment: result.payment });
  } catch (err) {
    const message = err instanceof Error ? err.message : "Webhook processing error.";
    res.status(500).json({ error: message });
  }
});

// POST /webhooks/offramp - Alternate route for offramp webhook
webhookRouter.post("/webhooks/offramp", async (req, res) => {
  const eventId = (req.headers["x-event-id"] as string) || (req.body.eventId as string) || generateEventId("EVT-OFF-");
  const result = await globalPipelineService.processWebhookEvent({
    eventId,
    provider: "offramp",
    eventType: req.body.event || req.body.status || "unknown",
    orderId: req.body.orderId,
    payload: req.body,
    receivedAt: new Date().toISOString(),
    status: "processed"
  });

  res.status(200).json({ ok: true, duplicate: result.duplicate, payment: result.payment });
});
