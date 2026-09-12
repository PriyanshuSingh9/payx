import { prisma } from "./prisma.js";

interface CachedFxRate {
  rate: number;
  expiresAt: number;
}

const fxCache = new Map<string, CachedFxRate>();
const FX_CACHE_TTL_MS = 60 * 1000; // 60 seconds TTL

function round4(n: number): number {
  return Math.round(n * 10_000) / 10_000;
}

function round2(n: number): number {
  return Math.round(n * 100) / 100;
}

// Live FX via open.er-api.com with in-memory caching, Neon DB sync, and resilient fallback.
export async function fetchFxRate(base: string, quote: string): Promise<number> {
  if (base === quote) return 1;
  const cacheKey = `${base}_${quote}`;
  const now = Date.now();

  const cached = fxCache.get(cacheKey);
  if (cached && now < cached.expiresAt) {
    return cached.rate;
  }

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 4000);
    const res = await fetch(`https://open.er-api.com/v6/latest/${base}`, {
      signal: controller.signal
    });
    clearTimeout(timeoutId);

    if (!res.ok) throw new Error(`FX request failed: ${res.status}`);
    const data = (await res.json()) as { rates?: Record<string, number> };
    const rawRate = data.rates?.[quote];
    if (!rawRate || !Number.isFinite(rawRate) || rawRate <= 0) {
      throw new Error("FX rate missing or invalid in API response.");
    }

    const rate = round4(rawRate);
    fxCache.set(cacheKey, {
      rate,
      expiresAt: now + FX_CACHE_TTL_MS
    });

    // Asynchronously synchronize live rate to Neon Postgres exchange_rates table
    prisma.exchangeRate.upsert({
      where: {
        baseCurrency_quoteCurrency: {
          baseCurrency: base,
          quoteCurrency: quote
        }
      },
      update: {
        rate,
        asOf: new Date()
      },
      create: {
        baseCurrency: base,
        quoteCurrency: quote,
        rate,
        cheaperPercentage: 2.3,
        asOf: new Date()
      }
    }).catch(() => {
      // Non-blocking background sync failure ignored
    });

    return rate;
  } catch (networkError) {
    // If cache has an expired entry, use it rather than failing
    if (cached) {
      return cached.rate;
    }

    // Attempt to read latest rate persisted in Neon DB
    try {
      const dbRecord = await prisma.exchangeRate.findUnique({
        where: {
          baseCurrency_quoteCurrency: {
            baseCurrency: base,
            quoteCurrency: quote
          }
        }
      });
      if (dbRecord && Number(dbRecord.rate) > 0) {
        const dbRate = Number(dbRecord.rate);
        fxCache.set(cacheKey, {
          rate: dbRate,
          expiresAt: now + 30_000 // Retry in 30s
        });
        return dbRate;
      }
    } catch {
      // DB read error handled by final fallback
    }

    if (base === "USD" && quote === "INR") return 95.50;
    throw new Error(`No FX rate available for ${base}/${quote}.`);
  }
}

// Convenience helper for USDC -> INR live exchange rate
export async function getLiveUsdcToInrRate(): Promise<number> {
  const rate = await fetchFxRate("USD", "INR");
  return round2(rate);
}

