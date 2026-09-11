import type { Corridor } from "./corridors.js";
import type { OffRampQuote } from "./models.js";

export interface Quote {
  corridorId: string;
  amountSource: number;
  feeSource: number;
  amountUsdc: number;
  fxRate: number;
  amountDest: number;
  destCurrency: string;
  etaSeconds: number;
  quotedAt: string;
}

// Fee in basis points of the source amount; USDC pegged 1:1 to the
// post-fee source value before FX conversion.
export function computeQuote(corridor: Corridor, amountSource: number, fxRate: number): Quote {
  if (!Number.isFinite(amountSource) || amountSource <= 0) {
    throw new Error("amountSource must be a positive number.");
  }
  if (!Number.isFinite(fxRate) || fxRate <= 0) {
    throw new Error("fxRate must be a positive number.");
  }
  const feeSource = round2((amountSource * corridor.feeBps) / 10_000);
  const netSource = round2(amountSource - feeSource);
  return {
    corridorId: corridor.id,
    amountSource: round2(amountSource),
    feeSource,
    amountUsdc: netSource,
    fxRate,
    amountDest: round2(netSource * fxRate),
    destCurrency: corridor.destCurrency,
    etaSeconds: corridor.etaSeconds,
    quotedAt: new Date().toISOString()
  };
}

export interface OffRampQuoteParams {
  sourceAmount: number;
  exchangeRate?: number;
  feeBps?: number;
  estimatedNetworkFeeUsdc?: number;
  expirySeconds?: number;
  quoteId?: string;
}

export const DEFAULT_USDC_INR_RATE = 87.20;
export const DEFAULT_OFFRAMP_FEE_BPS = 50; // 0.50%
export const DEFAULT_NETWORK_FEE_USDC = 0.01;
export const DEFAULT_QUOTE_EXPIRY_SECONDS = 30; // PRD Section 7: "Quote expires in 30 seconds"

// Computes pure money math for USDC -> INR off-ramp (PRD Section 7).
export function computeOffRampQuote(params: OffRampQuoteParams): OffRampQuote {
  const {
    sourceAmount,
    exchangeRate = DEFAULT_USDC_INR_RATE,
    feeBps = DEFAULT_OFFRAMP_FEE_BPS,
    estimatedNetworkFeeUsdc = DEFAULT_NETWORK_FEE_USDC,
    expirySeconds = DEFAULT_QUOTE_EXPIRY_SECONDS,
    quoteId = `QT-${Date.now().toString(36).toUpperCase()}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`
  } = params;

  if (!Number.isFinite(sourceAmount) || sourceAmount <= 0) {
    throw new Error("sourceAmount must be a positive number.");
  }
  if (!Number.isFinite(exchangeRate) || exchangeRate <= 0) {
    throw new Error("exchangeRate must be a positive number.");
  }

  const grossDestinationAmount = round2(sourceAmount * exchangeRate);
  const offRampFee = round2((grossDestinationAmount * feeBps) / 10_000);
  const recipientAmount = round2(grossDestinationAmount - offRampFee);
  const now = new Date();
  const expiresAt = new Date(now.getTime() + expirySeconds * 1000).toISOString();

  return {
    quoteId,
    sourceAsset: "USDC",
    destinationCurrency: "INR",
    sourceAmount: round2(sourceAmount),
    exchangeRate,
    grossDestinationAmount,
    offRampFee,
    estimatedNetworkFee: round2(estimatedNetworkFeeUsdc),
    recipientAmount,
    estimatedMinutesMin: 5,
    estimatedMinutesMax: 30,
    quotedAt: now.toISOString(),
    expiresAt
  };
}

export function isQuoteExpired(expiresAt: string, now: Date = new Date()): boolean {
  const expiryTime = new Date(expiresAt).getTime();
  if (Number.isNaN(expiryTime)) return true;
  return now.getTime() >= expiryTime;
}

export function round2(n: number): number {
  return Math.round(n * 100) / 100;
}
