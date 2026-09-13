import { describe, it, before, after } from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import { app } from "../backend/src/server.js";
import { globalPaymentPollerService } from "../backend/src/services/paymentPoller.js";
import { prisma } from "../backend/src/prisma.js";

describe("PayX Contacts & Recipients Integration Suite", () => {
  let server: http.Server;
  let baseUrl: string;
  let createdContactId: string | null = null;

  before(async () => {
    globalPaymentPollerService.stop();
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
    if (createdContactId) {
      await prisma.contact.delete({ where: { id: createdContactId } }).catch(() => {});
    }
    globalPaymentPollerService.stop();
    await new Promise<void>((resolve, reject) => {
      server.close((err) => (err ? reject(err) : resolve()));
    });
  });

  it("GET /api/v1/contacts returns default or saved contacts", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { contacts: Array<{ id: string; name: string; phone: string }> };
    assert.ok(Array.isArray(body.contacts));
    assert.ok(body.contacts.length >= 3);
    const names = body.contacts.map((c) => c.name);
    assert.ok(names.includes("Priya Sharma"));
  });

  it("POST /api/v1/contacts creates a contact with valid UPI", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: "Vikram Malhotra",
        phone: "+919876543299",
        upiId: "vikram.malhotra@oksbi",
        country: "IN"
      })
    });
    assert.equal(res.status, 201);
    const body = (await res.json()) as { contact: { id: string; name: string; upiId: string; avatarInitials: string } };
    assert.ok(body.contact.id);
    assert.equal(body.contact.name, "Vikram Malhotra");
    assert.equal(body.contact.upiId, "vikram.malhotra@oksbi");
    assert.equal(body.contact.avatarInitials, "VM");
    createdContactId = body.contact.id;
  });

  it("POST /api/v1/contacts rejects invalid UPI format", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: "Bad Recipient",
        phone: "+919876543298",
        upiId: "not-a-valid-upi-id",
        country: "IN"
      })
    });
    assert.equal(res.status, 400);
    const body = (await res.json()) as { error: string };
    assert.ok(body.error.includes("UPI"));
  });

  it("GET /api/v1/contacts?q=Vikram filters by query", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts?q=Vikram`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { contacts: Array<{ id: string; name: string }> };
    assert.ok(body.contacts.some((c) => c.name === "Vikram Malhotra"));
  });

  it("GET /recipients compatibility endpoint returns contacts list", async () => {
    const res = await fetch(`${baseUrl}/recipients`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { recipients: Array<{ id: string; name: string }> };
    assert.ok(Array.isArray(body.recipients));
    assert.ok(body.recipients.some((r) => r.name === "Vikram Malhotra"));
  });

  it("GET /api/v1/contacts/lookup finds contact by phone number", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts/lookup?phone=9876543299`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { found: boolean; contact?: { name: string; phone: string } };
    assert.equal(body.found, true);
    assert.equal(body.contact?.name, "Vikram Malhotra");
  });

  it("GET /api/v1/contacts/lookup finds default contact by phone number", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts/lookup?phone=9876543210`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { found: boolean; contact?: { name: string } };
    assert.equal(body.found, true);
    assert.equal(body.contact?.name, "Priya Sharma");
  });

  it("GET /api/v1/contacts/lookup returns found=false for unknown number", async () => {
    const res = await fetch(`${baseUrl}/api/v1/contacts/lookup?phone=9111111111`);
    assert.equal(res.status, 200);
    const body = (await res.json()) as { found: boolean };
    assert.equal(body.found, false);
  });

  it("DELETE /api/v1/contacts/:id removes contact", async () => {
    if (!createdContactId) return;
    const res = await fetch(`${baseUrl}/api/v1/contacts/${createdContactId}`, {
      method: "DELETE"
    });
    assert.equal(res.status, 200);
    const body = (await res.json()) as { success: boolean; id: string };
    assert.equal(body.success, true);
    assert.equal(body.id, createdContactId);
    createdContactId = null;
  });
});
