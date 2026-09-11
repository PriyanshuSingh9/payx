import { Router } from "express";
import { HttpError, requireSession } from "../auth.js";
import { globalPipelineService, DEFAULT_DEMO_SENDER_WALLET } from "../services/paymentPipeline.js";
import { globalPaymentStore } from "../services/paymentStore.js";
import { findCorridor } from "../lib/index.js";

export const transferRouter = Router();

const DEFAULT_SENDER_WALLET = DEFAULT_DEMO_SENDER_WALLET;

// POST /transfers - Create transfer intent
transferRouter.post("/transfers", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const { corridorId, recipientId, amountSource, recipientDetails } = req.body;

    const corridor = corridorId ? findCorridor(corridorId) : null;
    if (!amountSource || Number(amountSource) <= 0) {
      throw new HttpError("amountSource must be a positive number.", 400);
    }

    const recipient = recipientDetails || {
      name: "Demo Recipient",
      phone: "+919876543210",
      upiId: "recipient@upi"
    };

    const payment = await globalPipelineService.createPayment({
      senderWallet: DEFAULT_SENDER_WALLET,
      sourceAmount: Number(amountSource),
      recipient,
      mode: "full_simulation"
    });

    res.status(201).json({
      transfer: {
        id: payment.id,
        status: payment.status,
        amountSource: payment.sourceAmount,
        amountDest: payment.destinationAmount,
        exchangeRate: payment.exchangeRate,
        corridorId: corridor?.id ?? "USD-INR",
        senderId: session.userId,
        recipientId: recipientId || payment.recipient.id,
        createdAt: payment.createdAt
      },
      payment
    });
  } catch (err) {
    next(err);
  }
});

// GET /transfers/:id - Get transfer details
transferRouter.get("/transfers/:id", async (req, res, next) => {
  try {
    requireSession(req);
    const payment = await globalPaymentStore.getPayment(req.params.id);
    if (!payment) {
      throw new HttpError(`Transfer not found: ${req.params.id}`, 404);
    }
    res.json({
      transfer: {
        id: payment.id,
        status: payment.status,
        amountSource: payment.sourceAmount,
        amountDest: payment.destinationAmount,
        exchangeRate: payment.exchangeRate,
        escrowPda: payment.blockchainTransaction?.transactionSignature,
        solanaSignature: payment.blockchainTransaction?.transactionSignature,
        createdAt: payment.createdAt,
        completedAt: payment.completedAt
      },
      payment
    });
  } catch (err) {
    next(err);
  }
});

// GET /recipients - Address book
transferRouter.get("/recipients", (req, res, next) => {
  try {
    requireSession(req);
    const recipients = [
      {
        id: "rcp-1",
        name: "Priya Sharma",
        phone: "+919876543210",
        upiId: "priya@oksbi",
        avatarInitials: "PS",
        country: "IN"
      },
      {
        id: "rcp-2",
        name: "Rahul Verma",
        phone: "+919812345678",
        bankAccount: "987654321012",
        ifsc: "HDFC0001234",
        avatarInitials: "RV",
        country: "IN"
      },
      {
        id: "rcp-3",
        name: "Sarah Smith",
        phone: "+919988776655",
        upiId: "sarah@icici",
        avatarInitials: "SS",
        country: "IN"
      }
    ];
    res.json({ recipients });
  } catch (err) {
    next(err);
  }
});

// GET /me/dashboard - Sender dashboard
transferRouter.get("/me/dashboard", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const recent = await globalPaymentStore.listPayments(5);
    res.json({
      user: {
        id: session.userId,
        email: session.email,
        walletAddress: DEFAULT_SENDER_WALLET,
        availableBalanceUsd: 1000.0,
        lifetimeSavingsUsd: 48.5
      },
      liveRate: 87.2,
      transfers: recent
    });
  } catch (err) {
    next(err);
  }
});

// GET /me/receiver-dashboard - Inbound transfers
transferRouter.get("/me/receiver-dashboard", async (req, res, next) => {
  try {
    requireSession(req);
    const recent = await globalPaymentStore.listPayments(20);
    const completed = recent.filter((p) => p.status === "COMPLETED");
    const totalReceivedInr = completed.reduce((sum, p) => sum + (p.destinationAmount || 0), 0);

    res.json({
      totalReceivedInr,
      count: completed.length,
      inboundTransfers: completed
    });
  } catch (err) {
    next(err);
  }
});
