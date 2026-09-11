import { Router } from "express";
import { HttpError, requireSession } from "../auth.js";
import {
  createTransaction,
  getTransactionById,
  listUserTransactions
} from "../services/transactionService.js";
import { prisma } from "../prisma.js";

export const transferRouter = Router();

// POST /transfers - Create transfer intent and persist transaction in database
transferRouter.post("/transfers", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const { corridorId, recipientId, recipientDetails, amountSource, customFxRate } = req.body as {
      corridorId?: string;
      recipientId?: string;
      recipientDetails?: {
        name?: string;
        phone?: string;
        walletAddress?: string;
        country?: string;
        email?: string;
      };
      amountSource?: number | string;
      customFxRate?: number | string;
    };

    if (!corridorId) {
      throw new HttpError("corridorId is required.", 400);
    }
    const parsedAmount = Number(amountSource);
    if (!amountSource || Number.isNaN(parsedAmount) || parsedAmount <= 0) {
      throw new HttpError("amountSource must be a positive number.", 400);
    }

    const transaction = await createTransaction({
      senderId: session.userId,
      receiverId: recipientId,
      recipientDetails,
      corridorId,
      amountSource: parsedAmount,
      customFxRate: customFxRate ? Number(customFxRate) : undefined
    });

    res.status(201).json({ transaction });
  } catch (err) {
    next(err);
  }
});

// GET /transfers/:id - Get transfer details by ID
transferRouter.get("/transfers/:id", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const transaction = await getTransactionById(req.params.id);

    if (!transaction) {
      throw new HttpError(`Transfer not found: ${req.params.id}`, 404);
    }

    if (transaction.senderId !== session.userId && transaction.receiverId !== session.userId) {
      throw new HttpError("Unauthorized access to this transaction.", 403);
    }

    res.json({ transaction });
  } catch (err) {
    next(err);
  }
});

// GET /transfers - List transfers for authenticated user
transferRouter.get("/transfers", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const limit = req.query.limit ? Number(req.query.limit) : 50;
    const offset = req.query.offset ? Number(req.query.offset) : 0;

    const transactions = await listUserTransactions(session.userId, {
      role: "all",
      limit,
      offset
    });

    res.json({ transactions });
  } catch (err) {
    next(err);
  }
});

export const DEFAULT_RECIPIENTS = [
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

// GET /recipients - List distinct recipient contacts previously transferred to (or defaults)
transferRouter.get("/recipients", async (req, res, next) => {
  try {
    let recipientsList: Array<{
      id: string;
      name: string;
      phone: string;
      upiId?: string;
      avatarInitials?: string;
      country?: string;
    }> = [];

    try {
      const authHeader = req.headers["authorization"];
      if (authHeader) {
        const session = requireSession(req);
        const sent = await prisma.transaction.findMany({
          where: { senderId: session.userId },
          select: { receiverId: true },
          distinct: ["receiverId"],
          take: 20
        });

        const receiverIds = sent.map((s) => s.receiverId);
        if (receiverIds.length > 0) {
          const users = await prisma.user.findMany({
            where: { id: { in: receiverIds } },
            select: {
              id: true,
              displayName: true,
              email: true,
              phoneNumber: true,
              walletAddress: true,
              country: true
            }
          });
          recipientsList = users.map((u) => {
            const name = u.displayName || u.email || "Recipient";
            const initials = name
              .split(" ")
              .map((w) => w[0])
              .join("")
              .slice(0, 2)
              .toUpperCase();
            return {
              id: u.id,
              name,
              phone: u.phoneNumber || "+919876543210",
              upiId: u.email ? `${name.toLowerCase().replace(/\s+/g, "")}@upi` : undefined,
              avatarInitials: initials,
              country: u.country || "IN"
            };
          });
        }
      }
    } catch {
      // Ignore DB or session error and fall back to default demo recipients
    }

    if (recipientsList.length === 0) {
      recipientsList = DEFAULT_RECIPIENTS;
    }

    const q = req.query.q ? String(req.query.q).toLowerCase().trim() : "";
    if (q) {
      recipientsList = recipientsList.filter(
        (r) =>
          r.name.toLowerCase().includes(q) ||
          r.phone.includes(q) ||
          (r.upiId && r.upiId.toLowerCase().includes(q))
      );
    }

    res.json({ recipients: recipientsList });
  } catch (err) {
    next(err);
  }
});

// GET /me/dashboard - Sender dashboard view of active and recent transfers
transferRouter.get("/me/dashboard", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const transactions = await listUserTransactions(session.userId, { role: "sender", limit: 20 });
    res.json({ transactions });
  } catch (err) {
    next(err);
  }
});

// GET /me/receiver-dashboard - Receiver dashboard view of incoming transfers
transferRouter.get("/me/receiver-dashboard", async (req, res, next) => {
  try {
    let transactions: Awaited<ReturnType<typeof listUserTransactions>> = [];
    try {
      const session = requireSession(req);
      transactions = await listUserTransactions(session.userId, { role: "receiver", limit: 20 });
    } catch {
      // Fallback for unauthenticated / demo sessions
    }

    const completed = transactions.filter((t) => t.status === "completed");
    const totalReceivedInr = completed.reduce((sum, t) => sum + Number(t.amountDest || 0), 0);
    const totalReceivedUsd = completed.reduce((sum, t) => sum + Number(t.amountSource || 0), 0);

    res.json({
      totalReceivedInr,
      totalReceivedUsd,
      count: transactions.length,
      transactions
    });
  } catch (err) {
    next(err);
  }
});
