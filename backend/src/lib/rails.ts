import type { DestRail } from "./corridors.js";
import { PublicKey } from "@solana/web3.js";

export interface RailCheck {
  ok: boolean;
  reason?: string;
}

const UPI_HANDLE = /^[\w.\-]{2,256}@[a-zA-Z]{2,64}$/;
const IBAN = /^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$/;
const CLABE = /^\d{18}$/;
const UK_SORT = /^\d{6}$/;
const UK_ACCOUNT = /^\d{8}$/;
const INDIAN_PHONE = /^(\+91|91|0)?[6-9]\d{9}$/;
const INDIAN_ACCOUNT = /^\d{9,18}$/;
const IFSC_CODE = /^[A-Z]{4}0[A-Z0-9]{6}$/;

export function validateRail(rail: DestRail, details: Record<string, string>): RailCheck {
  switch (rail) {
    case "UPI": {
      const handle = (details["upiHandle"] ?? details["upiId"] ?? "").trim();
      const acct = (details["account"] ?? details["bankAccount"] ?? "").trim();
      const ifsc = (details["ifsc"] ?? "").trim().toUpperCase();
      if (UPI_HANDLE.test(handle)) return { ok: true };
      if (INDIAN_ACCOUNT.test(acct) && IFSC_CODE.test(ifsc)) return { ok: true };
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

export function validateIndianPhone(phone: string): boolean {
  return INDIAN_PHONE.test(phone.replace(/[\s\-]/g, ""));
}

export function validateIndianRecipient(details: {
  phone?: string;
  upiId?: string;
  bankAccount?: string;
  ifsc?: string;
}): RailCheck {
  if (details.phone && !validateIndianPhone(details.phone)) {
    return { ok: false, reason: "Invalid Indian phone number format. Expected +91XXXXXXXXXX." };
  }
  if (details.upiId) {
    if (UPI_HANDLE.test(details.upiId.trim())) return { ok: true };
    return { ok: false, reason: "Invalid UPI ID format. Expected name@bank." };
  }
  if (details.bankAccount && details.ifsc) {
    if (!INDIAN_ACCOUNT.test(details.bankAccount.trim())) {
      return { ok: false, reason: "Invalid Indian bank account number. Expected 9-18 digits." };
    }
    if (!IFSC_CODE.test(details.ifsc.trim().toUpperCase())) {
      return { ok: false, reason: "Invalid IFSC format. Expected 4 letters, 0, followed by 6 alphanumeric characters." };
    }
    return { ok: true };
  }
  return { ok: false, reason: "Recipient destination requires either a valid UPI ID or bank account + IFSC." };
}

export function validateSolanaAddress(address: string): boolean {
  try {
    const pk = new PublicKey(address);
    return PublicKey.isOnCurve(pk.toBuffer());
  } catch {
    return false;
  }
}
