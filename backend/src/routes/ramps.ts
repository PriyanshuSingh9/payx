import { Router } from "express";
import { HttpError, requireSession } from "../auth.js";
import { env } from "../env.js";
import { prisma } from "../prisma.js";
import { updateTransactionStatus, updateRampOrderStatus } from "../services/transactionService.js";

export const rampRouter = Router();

rampRouter.get("/onramp/widget", (_req, res) => {
  res.type("html").send("<!doctype html><html><body>PayX on-ramp widget (Phase 1).</body></html>");
});

// Complete on-ramp order and advance transaction to escrow_locked
rampRouter.post("/onramp/complete/:orderId", async (req, res, next) => {
  try {
    requireSession(req);
    const { orderId } = req.params;
    const { txHash } = req.body as { txHash?: string };

    const rampOrder = await prisma.rampOrder.findUnique({
      where: { id: orderId }
    });

    if (!rampOrder) {
      throw new HttpError(`Ramp order not found: ${orderId}`, 404);
    }

    await updateRampOrderStatus(orderId, "COMPLETED", { txHash });
    const transaction = await updateTransactionStatus(rampOrder.transactionId, "escrow_locked");

    res.json({ success: true, transaction });
  } catch (err) {
    next(err);
  }
});

// Inbound webhook for on-ramp provider updates
rampRouter.post("/webhooks/onramp", async (req, res, next) => {
  try {
    const { orderId, externalOrderId, status, txHash } = req.body as {
      orderId?: string;
      externalOrderId?: string;
      status?: string;
      txHash?: string;
    };

    const rampOrder = await prisma.rampOrder.findFirst({
      where: {
        OR: [
          ...(orderId ? [{ id: orderId }] : []),
          ...(externalOrderId ? [{ externalOrderId }] : [])
        ]
      }
    });

    if (!rampOrder) {
      res.status(404).json({ error: "Ramp order not found." });
      return;
    }

    const nextStatus = (status || "COMPLETED").toUpperCase();
    await updateRampOrderStatus(rampOrder.id, nextStatus, { txHash });

    if (nextStatus === "COMPLETED") {
      await updateTransactionStatus(rampOrder.transactionId, "escrow_locked");
    } else if (nextStatus === "FAILED") {
      await updateTransactionStatus(rampOrder.transactionId, "failed", "On-ramp provider reported failure.");
    }

    res.json({ received: true });
  } catch (err) {
    next(err);
  }
});

// Inbound webhook for off-ramp provider settlement updates
rampRouter.post("/webhooks/offramp", async (req, res, next) => {
  try {
    const { orderId, externalOrderId, status, txHash } = req.body as {
      orderId?: string;
      externalOrderId?: string;
      status?: string;
      txHash?: string;
    };

    const rampOrder = await prisma.rampOrder.findFirst({
      where: {
        OR: [
          ...(orderId ? [{ id: orderId }] : []),
          ...(externalOrderId ? [{ externalOrderId }] : [])
        ]
      }
    });

    if (!rampOrder) {
      res.status(404).json({ error: "Ramp order not found." });
      return;
    }

    const nextStatus = (status || "COMPLETED").toUpperCase();
    await updateRampOrderStatus(rampOrder.id, nextStatus, { txHash });

    if (nextStatus === "COMPLETED" || nextStatus === "SUCCESS") {
      await updateTransactionStatus(rampOrder.transactionId, "completed");
    } else if (nextStatus === "FAILED") {
      await updateTransactionStatus(rampOrder.transactionId, "failed", "Off-ramp provider reported payout failure.");
    }

    res.json({ received: true });
  } catch (err) {
    next(err);
  }
});

// Force release a stuck transaction (demo / testing only)
rampRouter.post("/demo/force-release", async (req, res, next) => {
  try {
    if (!env.enableDemoAdmin) {
      throw new HttpError("Demo admin routes are disabled.", 404);
    }
    requireSession(req);

    const { transactionId, status } = req.body as {
      transactionId?: string;
      status?: "completed" | "refunded" | "failed";
    };

    if (!transactionId) {
      throw new HttpError("transactionId is required.", 400);
    }

    const targetStatus = status ?? "completed";
    const transaction = await updateTransactionStatus(transactionId, targetStatus, "Force-released via demo admin.");

    res.json({ success: true, transaction });
  } catch (err) {
    next(err);
  }
});
