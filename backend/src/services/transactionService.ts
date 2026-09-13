import { prisma } from "../prisma.js";
import { computeQuote, findCorridor, CORRIDORS, type Corridor, type DestRail } from "../lib/index.js";
import type { TransactionStatus } from "../lib/status.js";

export interface CreateTransactionInput {
  senderId: string;
  receiverId?: string;
  recipientDetails?: {
    name?: string;
    phone?: string;
    walletAddress?: string;
    country?: string;
    email?: string;
  };
  corridorId: string; // UUID or corridorKey (e.g. "USD-INR-UPI")
  amountSource: number;
  customFxRate?: number;
}

export interface UpdateEscrowInput {
  escrowId?: bigint;
  escrowPda?: string;
  escrowState?: string;
  escrowTxHash?: string;
  releaseTxHash?: string;
  solanaSignature?: string;
  lockedSourceToUsdc?: number;
  lockedUsdcToDest?: number;
}

export interface ListTransactionsOptions {
  role?: "sender" | "receiver" | "all";
  limit?: number;
  offset?: number;
}

export interface CreateRampOrderInput {
  transactionId: string;
  type: "onramp" | "offramp";
  externalOrderId?: string;
  status?: string;
  fiatCurrency: string;
  fiatAmount: number;
  cryptoCurrency?: string;
  cryptoAmount: number;
  walletAddress: string;
  bankDetails?: string;
  txHash?: string;
  metadata?: string;
}

// Ensure the corridor exists in database by matching id or corridorKey.
// If missing, seed it from the pure domain corridor registry.
async function resolveCorridor(corridorKeyOrId: string) {
  let corridor = await prisma.corridor.findFirst({
    where: {
      OR: [{ id: corridorKeyOrId }, { corridorKey: corridorKeyOrId }]
    }
  });

  if (!corridor) {
    const staticDef = findCorridor(corridorKeyOrId) || CORRIDORS.find((c: Corridor) => c.id === corridorKeyOrId);
    if (!staticDef) {
      throw new Error(`Corridor not found or disabled: ${corridorKeyOrId}`);
    }

    corridor = await prisma.corridor.upsert({
      where: { corridorKey: staticDef.id },
      update: {
        feeBps: staticDef.feeBps,
        etaSeconds: staticDef.etaSeconds,
        enabled: staticDef.enabled,
        inProvider: staticDef.inProvider,
        outProvider: staticDef.outProvider
      },
      create: {
        corridorKey: staticDef.id,
        sourceCurrency: staticDef.sourceCurrency,
        destCurrency: staticDef.destCurrency,
        destRail: staticDef.destRail,
        inProvider: staticDef.inProvider,
        outProvider: staticDef.outProvider,
        feeBps: staticDef.feeBps,
        etaSeconds: staticDef.etaSeconds,
        enabled: staticDef.enabled
      }
    });
  }

  return corridor;
}

// Fetch exchange rate for the currency pair, with fallback to default rates
async function resolveExchangeRate(baseCurrency: string, quoteCurrency: string, customRate?: number): Promise<number> {
  if (customRate !== undefined && customRate > 0) {
    return customRate;
  }

  const rateRecord = await prisma.exchangeRate.findUnique({
    where: {
      baseCurrency_quoteCurrency: {
        baseCurrency,
        quoteCurrency
      }
    }
  });

  if (rateRecord) {
    return Number(rateRecord.rate);
  }

  // Known fallback rates if exchange_rates table has not been populated yet
  if (baseCurrency === "USD" && quoteCurrency === "INR") return 83.42;
  if (baseCurrency === "EUR" && quoteCurrency === "INR") return 90.15;
  if (baseCurrency === "GBP" && quoteCurrency === "INR") return 105.80;
  if (baseCurrency === "USD" && quoteCurrency === "BRL") return 5.45;
  if (baseCurrency === "USD" && quoteCurrency === "EUR") return 0.92;
  if (baseCurrency === "GBP" && quoteCurrency === "GBP") return 1.0;
  if (baseCurrency === "CAD" && quoteCurrency === "INR") return 61.20;
  if (baseCurrency === "AUD" && quoteCurrency === "INR") return 54.75;
  if (baseCurrency === "USD" && quoteCurrency === "MXN") return 17.50;

  throw new Error(`No exchange rate found for pair ${baseCurrency}/${quoteCurrency}.`);
}

// Create a new transaction in the database
export async function createTransaction(input: CreateTransactionInput) {
  if (!input.senderId) {
    throw new Error("senderId is required.");
  }
  if (!input.amountSource || input.amountSource <= 0) {
    throw new Error("amountSource must be a positive number.");
  }

  const corridor = await resolveCorridor(input.corridorId);
  if (!corridor.enabled) {
    throw new Error(`Corridor ${corridor.corridorKey} is currently disabled.`);
  }

  // Resolve or create recipient user
  let receiverId = input.receiverId;
  if (!receiverId) {
    const details = input.recipientDetails;
    if (!details?.walletAddress && !details?.phone && !details?.name) {
      throw new Error("Either receiverId or recipientDetails is required.");
    }

    const walletAddress = details.walletAddress || `rec_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
    const email = details.email || `${walletAddress}@payx.internal`;

    const recipientUser = await prisma.user.upsert({
      where: { walletAddress },
      update: {
        displayName: details.name,
        phoneNumber: details.phone,
        country: details.country ?? (corridor.destCurrency === "INR" ? "IN" : "US")
      },
      create: {
        walletAddress,
        email,
        displayName: details.name ?? "Recipient",
        phoneNumber: details.phone,
        country: details.country ?? (corridor.destCurrency === "INR" ? "IN" : "US")
      }
    });

    receiverId = recipientUser.id;
  }

  const fxRate = await resolveExchangeRate(corridor.sourceCurrency, corridor.destCurrency, input.customFxRate);

  // Pure domain money math from src/lib/quotes.ts
  const quote = computeQuote(
    {
      id: corridor.corridorKey,
      sourceCurrency: corridor.sourceCurrency,
      destCurrency: corridor.destCurrency,
      destRail: corridor.destRail as DestRail,
      inProvider: corridor.inProvider,
      outProvider: corridor.outProvider,
      feeBps: corridor.feeBps,
      etaSeconds: corridor.etaSeconds,
      enabled: corridor.enabled
    },
    input.amountSource,
    fxRate
  );

  return prisma.transaction.create({
    data: {
      senderId: input.senderId,
      receiverId,
      corridorId: corridor.id,
      amountSource: quote.amountSource,
      amountUsdc: quote.amountUsdc,
      amountDest: quote.amountDest,
      feeSource: quote.feeSource,
      status: "pending",
      lockedSourceToUsdc: quote.amountUsdc,
      lockedUsdcToDest: quote.fxRate
    },
    include: {
      corridor: true,
      rampOrders: true
    }
  });
}

// Retrieve transaction by ID
export async function getTransactionById(id: string) {
  return prisma.transaction.findUnique({
    where: { id },
    include: {
      corridor: true,
      rampOrders: true
    }
  });
}

// Update transaction status and record failure reason or completion timestamp
export async function updateTransactionStatus(
  id: string,
  status: TransactionStatus,
  failureReason?: string
) {
  const isTerminal = status === "completed" || status === "failed" || status === "refunded";

  return prisma.transaction.update({
    where: { id },
    data: {
      status,
      completedAt: isTerminal ? new Date() : undefined
    },
    include: {
      corridor: true,
      rampOrders: true
    }
  });
}

// Update Solana on-chain escrow custody fields
export async function updateTransactionEscrow(id: string, input: UpdateEscrowInput) {
  return prisma.transaction.update({
    where: { id },
    data: {
      ...(input.escrowId !== undefined ? { escrowId: input.escrowId } : {}),
      ...(input.escrowPda !== undefined ? { escrowPda: input.escrowPda } : {}),
      ...(input.escrowState !== undefined ? { escrowState: input.escrowState } : {}),
      ...(input.escrowTxHash !== undefined ? { escrowTxHash: input.escrowTxHash } : {}),
      ...(input.releaseTxHash !== undefined ? { releaseTxHash: input.releaseTxHash } : {}),
      ...(input.solanaSignature !== undefined ? { solanaSignature: input.solanaSignature } : {}),
      ...(input.lockedSourceToUsdc !== undefined ? { lockedSourceToUsdc: input.lockedSourceToUsdc } : {}),
      ...(input.lockedUsdcToDest !== undefined ? { lockedUsdcToDest: input.lockedUsdcToDest } : {})
    },
    include: {
      corridor: true,
      rampOrders: true
    }
  });
}

// Query transactions for a given user (sender, receiver, or either)
export async function listUserTransactions(userId: string, options?: ListTransactionsOptions) {
  const role = options?.role ?? "all";
  const limit = Math.min(options?.limit ?? 50, 100);
  const offset = options?.offset ?? 0;

  let where: Record<string, unknown>;
  if (role === "sender") {
    where = { senderId: userId };
  } else if (role === "receiver") {
    where = { receiverId: userId };
  } else {
    where = {
      OR: [{ senderId: userId }, { receiverId: userId }]
    };
  }

  return prisma.transaction.findMany({
    where,
    orderBy: { createdAt: "desc" },
    take: limit,
    skip: offset,
    include: {
      corridor: true,
      rampOrders: true
    }
  });
}

// Create a ramp order associated with a transaction
export async function createRampOrder(input: CreateRampOrderInput) {
  return prisma.rampOrder.create({
    data: {
      transactionId: input.transactionId,
      type: input.type,
      externalOrderId: input.externalOrderId,
      status: input.status ?? "CREATED",
      fiatCurrency: input.fiatCurrency,
      fiatAmount: input.fiatAmount,
      cryptoCurrency: input.cryptoCurrency ?? "USDC",
      cryptoAmount: input.cryptoAmount,
      walletAddress: input.walletAddress,
      bankDetails: input.bankDetails,
      txHash: input.txHash,
      metadata: input.metadata
    }
  });
}

// Update ramp order status and external metadata
export async function updateRampOrderStatus(
  orderId: string,
  status: string,
  details?: { txHash?: string; externalOrderId?: string; metadata?: string }
) {
  return prisma.rampOrder.update({
    where: { id: orderId },
    data: {
      status,
      ...(details?.txHash ? { txHash: details.txHash } : {}),
      ...(details?.externalOrderId ? { externalOrderId: details.externalOrderId } : {}),
      ...(details?.metadata ? { metadata: details.metadata } : {})
    }
  });
}
