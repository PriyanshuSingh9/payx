import type { DestRail } from "./corridors.js";

export interface RailCheck {
  ok: boolean;
  reason?: string;
}

const UPI_HANDLE = /^[\w.\-]{2,256}@[a-zA-Z]{2,64}$/;
const IBAN = /^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$/;
const CLABE = /^\d{18}$/;
const UK_SORT = /^\d{6}$/;
const UK_ACCOUNT = /^\d{8}$/;

export function validateRail(rail: DestRail, details: Record<string, string>): RailCheck {
  switch (rail) {
    case "UPI": {
      const handle = (details["upiHandle"] ?? "").trim();
      const acct = (details["account"] ?? "").trim();
      const ifsc = (details["ifsc"] ?? "").trim().toUpperCase();
      if (UPI_HANDLE.test(handle)) return { ok: true };
      if (acct.length >= 6 && /^[A-Z]{4}0[A-Z0-9]{6}$/.test(ifsc)) return { ok: true };
      return { ok: false, reason: "Provide a valid UPI handle (name@bank) or account + IFSC." };
    }
    case "PIX": {
      const key = (details["pixKey"] ?? "").trim();
      if (key.length >= 5) return { ok: true };
      return { ok: false, reason: "Provide a valid PIX key." };
    }
    case "SEPA": {
      const iban = (details["iban"] ?? "").replace(/\s+/g, "").toUpperCase();
      if (IBAN.test(iban)) return { ok: true };
      return { ok: false, reason: "Provide a valid IBAN." };
    }
    case "FPS": {
      if (UK_SORT.test(details["sortCode"] ?? "") && UK_ACCOUNT.test(details["account"] ?? "")) {
        return { ok: true };
      }
      return { ok: false, reason: "Provide a 6-digit sort code and 8-digit account number." };
    }
    case "SPEI": {
      if (CLABE.test((details["clabe"] ?? "").trim())) return { ok: true };
      return { ok: false, reason: "Provide an 18-digit CLABE." };
    }
  }
}
