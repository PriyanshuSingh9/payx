import crypto from "node:crypto";

// Generates correlation ID format required by PRD Section 6 & 28:
// Example: "PX-8F29A1"
export function generatePaymentId(prefix = "PX-"): string {
  const randomHex = crypto.randomBytes(3).toString("hex").toUpperCase();
  return `${prefix}${randomHex}`;
}

export function generateOrderId(prefix = "ORD-"): string {
  const randomHex = crypto.randomBytes(4).toString("hex").toUpperCase();
  return `${prefix}${randomHex}`;
}

export function generateEventId(prefix = "EVT-"): string {
  const randomHex = crypto.randomBytes(4).toString("hex").toUpperCase();
  return `${prefix}${randomHex}`;
}
