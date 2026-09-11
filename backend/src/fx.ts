// Live FX via open.er-api.com with a static fallback, mirroring the reference.
export async function fetchFxRate(base: string, quote: string): Promise<number> {
  if (base === quote) return 1;
  try {
    const res = await fetch(`https://open.er-api.com/v6/latest/${base}`);
    if (!res.ok) throw new Error(`FX request failed: ${res.status}`);
    const data = (await res.json()) as { rates?: Record<string, number> };
    const rate = data.rates?.[quote];
    if (!rate || !Number.isFinite(rate) || rate <= 0) throw new Error("FX rate missing.");
    return rate;
  } catch {
    if (base === "USD" && quote === "INR") return 83.42;
    throw new Error(`No FX rate available for ${base}/${quote}.`);
  }
}
