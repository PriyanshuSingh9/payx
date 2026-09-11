import type { Corridor } from "./corridors.js";

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

function round2(n: number): number {
  return Math.round(n * 100) / 100;
}
