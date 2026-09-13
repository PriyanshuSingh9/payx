import { Router } from "express";
import { requireSession, type SessionClaims } from "../auth.js";
import {
  listContacts,
  createContact,
  deleteContact,
  lookupContactByPhone
} from "../services/contactService.js";

export const contactRouter = Router();

function tryGetSession(req: any): SessionClaims | null {
  try {
    const header = req.headers?.authorization;
    if (header && header.startsWith("Bearer ")) {
      return requireSession(req);
    }
  } catch {
    // Session token absent or unparseable
  }
  return null;
}

// GET /api/v1/contacts/lookup - Search for registered user / contact by phone number
contactRouter.get(["/api/v1/contacts/lookup", "/contacts/lookup"], async (req, res, next) => {
  try {
    const phone = String(req.query.phone || "").trim();
    if (!phone) {
      res.status(400).json({ error: "Phone number query parameter is required." });
      return;
    }
    const result = await lookupContactByPhone(phone);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

// GET /api/v1/contacts - List contacts
contactRouter.get("/api/v1/contacts", async (req, res, next) => {
  try {
    const session = tryGetSession(req);
    const walletAddress = (req.query.walletAddress as string) || (req.headers["x-wallet-address"] as string);
    const q = req.query.q ? String(req.query.q).toLowerCase().trim() : "";

    const contacts = await listContacts({
      userId: session?.userId,
      walletAddress,
      q
    });

    res.json({ contacts, recipients: contacts });
  } catch (err) {
    next(err);
  }
});

// POST /api/v1/contacts - Create a new contact
contactRouter.post("/api/v1/contacts", async (req, res, next) => {
  try {
    const session = tryGetSession(req);
    const { name, phone, upiId, accountNumber, ifscCode, email, country, walletAddress } = req.body || {};

    const created = await createContact({
      userId: session?.userId,
      walletAddress: walletAddress || (req.headers["x-wallet-address"] as string),
      name,
      phone,
      upiId,
      accountNumber,
      ifscCode,
      email,
      country
    });

    res.status(201).json({ contact: created, recipient: created });
  } catch (err: any) {
    res.status(400).json({ error: err.message || "Failed to create contact." });
  }
});

// DELETE /api/v1/contacts/:id - Delete a contact
contactRouter.delete("/api/v1/contacts/:id", async (req, res, next) => {
  try {
    const { id } = req.params;
    const ok = await deleteContact(id);
    if (!ok) {
      res.status(404).json({ error: "Contact not found." });
      return;
    }
    res.json({ success: true, id });
  } catch (err) {
    next(err);
  }
});

// GET /recipients - Compatibility endpoint matching Android PaymentRepository call
contactRouter.get("/recipients", async (req, res, next) => {
  try {
    const session = tryGetSession(req);
    const walletAddress = (req.query.walletAddress as string) || (req.headers["x-wallet-address"] as string);
    const q = req.query.q ? String(req.query.q).toLowerCase().trim() : "";

    const contacts = await listContacts({
      userId: session?.userId,
      walletAddress,
      q
    });

    res.json({ recipients: contacts, contacts });
  } catch (err) {
    next(err);
  }
});

// POST /recipients - Compatibility endpoint for creating contact
contactRouter.post("/recipients", async (req, res, next) => {
  try {
    const session = tryGetSession(req);
    const { name, phone, upiId, accountNumber, ifscCode, email, country, walletAddress } = req.body || {};

    const created = await createContact({
      userId: session?.userId,
      walletAddress: walletAddress || (req.headers["x-wallet-address"] as string),
      name,
      phone,
      upiId,
      accountNumber,
      ifscCode,
      email,
      country
    });

    res.status(201).json({ recipient: created, contact: created });
  } catch (err: any) {
    res.status(400).json({ error: err.message || "Failed to create contact." });
  }
});
