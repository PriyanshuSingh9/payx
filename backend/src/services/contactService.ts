import { prisma } from "../prisma.js";
import { validateIndianRecipient } from "../lib/rails.js";

export interface ContactDto {
  id: string;
  name: string;
  phone: string;
  upiId?: string;
  accountNumber?: string;
  ifscCode?: string;
  email?: string;
  country: string;
  avatarInitials: string;
  createdAt: string;
}

export const DEFAULT_CONTACTS: Array<Omit<ContactDto, "id" | "createdAt">> = [
  {
    name: "Priya Sharma",
    phone: "+919876543210",
    upiId: "priya.sharma@oksbi",
    avatarInitials: "PS",
    country: "IN"
  },
  {
    name: "Rahul Verma",
    phone: "+919876543211",
    upiId: "rahul.verma@oksbi",
    avatarInitials: "RV",
    country: "IN"
  },
  {
    name: "Sarah Smith",
    phone: "+919876543212",
    upiId: "sarah.smith@oksbi",
    avatarInitials: "SS",
    country: "IN"
  },
  {
    name: "Aarav Patel",
    phone: "+919876543213",
    upiId: "aarav.patel@oksbi",
    avatarInitials: "AP",
    country: "IN"
  }
];

export function computeInitials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

let tableEnsured = false;

export async function ensureContactsTable(): Promise<void> {
  if (tableEnsured) return;
  try {
    await prisma.$executeRawUnsafe(`
      CREATE TABLE IF NOT EXISTS contacts (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        wallet_address TEXT,
        name TEXT NOT NULL,
        phone TEXT NOT NULL,
        upi_id TEXT,
        account_number TEXT,
        ifsc_code TEXT,
        email TEXT,
        country TEXT NOT NULL DEFAULT 'IN',
        avatar_initials TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );
      CREATE INDEX IF NOT EXISTS idx_contacts_user_id ON contacts(user_id);
      CREATE INDEX IF NOT EXISTS idx_contacts_wallet_address ON contacts(wallet_address);
      CREATE INDEX IF NOT EXISTS idx_contacts_created_at ON contacts(created_at DESC);
    `);
    tableEnsured = true;
  } catch (err) {
    console.warn("[contactService] Failed to ensure contacts table:", err);
  }
}

export async function listContacts(params: {
  userId?: string;
  walletAddress?: string;
  q?: string;
}): Promise<ContactDto[]> {
  await ensureContactsTable();

  const query = (params.q || "").toLowerCase().trim();
  let dbContacts: any[] = [];

  try {
    const whereConditions: any[] = [];
    if (params.userId) {
      whereConditions.push({ userId: params.userId });
    }
    if (params.walletAddress) {
      whereConditions.push({ walletAddress: params.walletAddress });
    }

    const where = whereConditions.length > 0
      ? { OR: whereConditions }
      : {};

    dbContacts = await prisma.contact.findMany({
      where,
      orderBy: { createdAt: "desc" }
    });
  } catch (err) {
    console.warn("[contactService] Error fetching contacts from DB:", err);
  }

  const mappedDbContacts: ContactDto[] = dbContacts.map((c) => ({
    id: c.id,
    name: c.name,
    phone: c.phone,
    upiId: c.upiId || undefined,
    accountNumber: c.accountNumber || undefined,
    ifscCode: c.ifscCode || undefined,
    email: c.email || undefined,
    country: c.country,
    avatarInitials: c.avatarInitials || computeInitials(c.name),
    createdAt: c.createdAt.toISOString()
  }));

  // Fallback defaults to ensure out-of-the-box demo contacts exist
  const existingNames = new Set(mappedDbContacts.map((c) => c.name.toLowerCase()));
  const defaultList: ContactDto[] = DEFAULT_CONTACTS
    .filter((d) => !existingNames.has(d.name.toLowerCase()))
    .map((d, index) => ({
      id: `rec_default_${index + 1}`,
      name: d.name,
      phone: d.phone,
      upiId: d.upiId,
      country: d.country,
      avatarInitials: d.avatarInitials,
      createdAt: new Date(Date.now() - (index + 1) * 3600000).toISOString()
    }));

  let combined = [...mappedDbContacts, ...defaultList];

  if (query) {
    combined = combined.filter(
      (c) =>
        c.name.toLowerCase().includes(query) ||
        c.phone.includes(query) ||
        (c.upiId && c.upiId.toLowerCase().includes(query)) ||
        (c.email && c.email.toLowerCase().includes(query))
    );
  }

  return combined;
}

export async function createContact(input: {
  userId?: string;
  walletAddress?: string;
  name: string;
  phone: string;
  upiId?: string;
  accountNumber?: string;
  ifscCode?: string;
  email?: string;
  country?: string;
}): Promise<ContactDto> {
  await ensureContactsTable();

  const name = input.name?.trim();
  const phone = input.phone?.trim();
  const upiId = input.upiId?.trim() || null;
  const accountNumber = input.accountNumber?.trim() || null;
  const ifscCode = input.ifscCode?.trim() || null;
  const email = input.email?.trim() || null;
  const country = (input.country?.trim() || "IN").toUpperCase();

  if (!name) {
    throw new Error("Recipient name is required.");
  }
  if (!phone) {
    throw new Error("Recipient phone number is required.");
  }

  // Validate Indian recipient details if destination is India
  if (country === "IN") {
    const railCheck = validateIndianRecipient({
      upiId: upiId || undefined,
      bankAccount: accountNumber || undefined,
      ifsc: ifscCode || undefined
    });
    if (!railCheck.ok) {
      throw new Error(railCheck.reason || "Invalid UPI ID or Indian bank details.");
    }
  }

  const avatarInitials = computeInitials(name);

  // Validate that user exists if userId provided
  let validUserId: string | null = null;
  if (input.userId) {
    try {
      const userExists = await prisma.user.findUnique({ where: { id: input.userId }, select: { id: true } });
      if (userExists) validUserId = userExists.id;
    } catch {
      validUserId = null;
    }
  }

  const created = await prisma.contact.create({
    data: {
      userId: validUserId,
      walletAddress: input.walletAddress || null,
      name,
      phone,
      upiId,
      accountNumber,
      ifscCode,
      email,
      country,
      avatarInitials
    }
  });

  return {
    id: created.id,
    name: created.name,
    phone: created.phone,
    upiId: created.upiId || undefined,
    accountNumber: created.accountNumber || undefined,
    ifscCode: created.ifscCode || undefined,
    email: created.email || undefined,
    country: created.country,
    avatarInitials: created.avatarInitials || avatarInitials,
    createdAt: created.createdAt.toISOString()
  };
}

export async function deleteContact(id: string): Promise<boolean> {
  await ensureContactsTable();
  try {
    await prisma.contact.delete({ where: { id } });
    return true;
  } catch {
    return false;
  }
}

export interface ContactLookupResult {
  found: boolean;
  contact: ContactDto | null;
  message?: string;
}

export async function lookupContactByPhone(rawPhone: string): Promise<ContactLookupResult> {
  await ensureContactsTable();

  const digits = rawPhone.replace(/\D/g, "");
  if (digits.length < 10) {
    return { found: false, contact: null, message: "Enter at least 10 digits to search." };
  }

  const local10 = digits.slice(-10);

  // 1. Search existing Contact table
  try {
    const contactMatch = await prisma.contact.findFirst({
      where: {
        phone: { contains: local10 }
      },
      orderBy: { createdAt: "desc" }
    });

    if (contactMatch) {
      return {
        found: true,
        contact: {
          id: contactMatch.id,
          name: contactMatch.name,
          phone: contactMatch.phone,
          upiId: contactMatch.upiId || undefined,
          accountNumber: contactMatch.accountNumber || undefined,
          ifscCode: contactMatch.ifscCode || undefined,
          email: contactMatch.email || undefined,
          country: contactMatch.country,
          avatarInitials: contactMatch.avatarInitials || computeInitials(contactMatch.name),
          createdAt: contactMatch.createdAt.toISOString()
        }
      };
    }
  } catch (err) {
    console.warn("[lookupContactByPhone] Error querying contact table:", err);
  }

  // 2. Search registered User table
  try {
    const userMatch = await prisma.user.findFirst({
      where: {
        phoneNumber: { contains: local10 }
      }
    });

    if (userMatch) {
      const name = userMatch.displayName || userMatch.email.split("@")[0];
      const initials = computeInitials(name);
      return {
        found: true,
        contact: {
          id: userMatch.id,
          name,
          phone: userMatch.phoneNumber || `+91${local10}`,
          upiId: `${name.toLowerCase().replace(/\s+/g, "")}@oksbi`,
          email: userMatch.email,
          country: userMatch.country || "IN",
          avatarInitials: initials,
          createdAt: userMatch.createdAt.toISOString()
        }
      };
    }
  } catch (err) {
    console.warn("[lookupContactByPhone] Error querying user table:", err);
  }

  // 3. Search default contacts
  const defaultMatch = DEFAULT_CONTACTS.find((d) => d.phone.replace(/\D/g, "").slice(-10) === local10);
  if (defaultMatch) {
    return {
      found: true,
      contact: {
        id: `rec_${defaultMatch.name.toLowerCase().replace(/\s+/g, "_")}`,
        name: defaultMatch.name,
        phone: defaultMatch.phone,
        upiId: defaultMatch.upiId,
        country: defaultMatch.country,
        avatarInitials: defaultMatch.avatarInitials,
        createdAt: new Date().toISOString()
      }
    };
  }

  return {
    found: false,
    contact: null,
    message: "No PayX user found for this phone number. You can enter details to add them."
  };
}

