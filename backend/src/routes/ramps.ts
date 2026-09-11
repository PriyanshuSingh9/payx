import { Router } from "express";
import { HttpError, requireSession } from "../auth.js";
import { env } from "../env.js";
import { globalPaymentStore } from "../services/paymentStore.js";
import { globalPipelineService } from "../services/paymentPipeline.js";
import { generateEventId } from "../lib/index.js";

export const rampRouter = Router();

rampRouter.get("/onramp/widget", (_req, res) => {
  res.type("html").send(`<!doctype html>
<html>
<head><meta charset="utf-8"><title>PayX On-Ramp Simulation</title></head>
<body style="font-family: monospace; background: #121212; color: #fff; padding: 20px;">
  <h2>PayX Embedded On-Ramp Widget</h2>
  <p>Simulation active. Ready to ingest fiat to USDC.</p>
</body>
</html>`);
});

rampRouter.post("/onramp/complete/:orderId", async (req, res, next) => {
  try {
    requireSession(req);
    const payment = await globalPaymentStore.getPayment(req.params.orderId);
    if (!payment) {
      throw new HttpError(`Order not found: ${req.params.orderId}`, 404);
    }
    // Advance payment state if confirmed
    if (payment.status === "AWAITING_CONFIRMATION") {
      await globalPipelineService.confirmPayment(payment.id);
    }
    res.json({ ok: true, paymentId: payment.id, status: payment.status });
  } catch (err) {
    next(err);
  }
});

rampRouter.post("/webhooks/onramp", async (req, res) => {
  const eventId = (req.headers["x-event-id"] as string) || (req.body.eventId as string) || generateEventId("EVT-ON-");
  const result = await globalPipelineService.processWebhookEvent({
    eventId,
    provider: "onramp",
    eventType: req.body.event || req.body.status || "cryptoInit",
    orderId: req.body.orderId,
    payload: req.body,
    receivedAt: new Date().toISOString(),
    status: "processed"
  });

  res.status(200).json({ ok: true, duplicate: result.duplicate, payment: result.payment });
});

rampRouter.post("/demo/force-release", async (req, res, next) => {
  try {
    if (!env.enableDemoAdmin) throw new HttpError("Demo admin routes are disabled.", 404);
    requireSession(req);
    const { paymentId } = req.body;
    if (!paymentId) throw new HttpError("paymentId is required.", 400);

    const payment = await globalPaymentStore.getPayment(paymentId);
    if (!payment) throw new HttpError("Payment not found.", 404);

    payment.status = "COMPLETED";
    payment.completedAt = new Date().toISOString();
    await globalPaymentStore.savePayment(payment);

    res.json({ ok: true, payment });
  } catch (err) {
    next(err);
  }
});
