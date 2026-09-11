export type DestRail = "UPI" | "PIX" | "SEPA" | "FPS" | "SPEI";

export interface Corridor {
  id: string;
  sourceCurrency: string;
  destCurrency: string;
  destRail: DestRail;
  inProvider: string;
  outProvider: string;
  feeBps: number;
  etaSeconds: number;
  enabled: boolean;
}

// Launch corridor table. New corridors ship as data, not code.
export const CORRIDORS: Corridor[] = [
  { id: "USD-INR-UPI", sourceCurrency: "USD", destCurrency: "INR", destRail: "UPI", inProvider: "transak", outProvider: "onmeta", feeBps: 95, etaSeconds: 60, enabled: true },
  { id: "USD-BRL-PIX", sourceCurrency: "USD", destCurrency: "BRL", destRail: "PIX", inProvider: "transak", outProvider: "onmeta", feeBps: 110, etaSeconds: 75, enabled: true },
  { id: "USD-EUR-SEPA", sourceCurrency: "USD", destCurrency: "EUR", destRail: "SEPA", inProvider: "stripe", outProvider: "onmeta", feeBps: 95, etaSeconds: 90, enabled: true },
  { id: "EUR-INR-UPI", sourceCurrency: "EUR", destCurrency: "INR", destRail: "UPI", inProvider: "transak", outProvider: "onmeta", feeBps: 100, etaSeconds: 75, enabled: true },
  { id: "GBP-INR-UPI", sourceCurrency: "GBP", destCurrency: "INR", destRail: "UPI", inProvider: "moonpay", outProvider: "onmeta", feeBps: 100, etaSeconds: 75, enabled: true },
  { id: "GBP-GBP-FPS", sourceCurrency: "GBP", destCurrency: "GBP", destRail: "FPS", inProvider: "stripe", outProvider: "onmeta", feeBps: 80, etaSeconds: 45, enabled: true },
  { id: "CAD-INR-UPI", sourceCurrency: "CAD", destCurrency: "INR", destRail: "UPI", inProvider: "transak", outProvider: "onmeta", feeBps: 105, etaSeconds: 90, enabled: true },
  { id: "AUD-INR-UPI", sourceCurrency: "AUD", destCurrency: "INR", destRail: "UPI", inProvider: "transak", outProvider: "onmeta", feeBps: 105, etaSeconds: 90, enabled: true },
  { id: "USD-MXN-SPEI", sourceCurrency: "USD", destCurrency: "MXN", destRail: "SPEI", inProvider: "transak", outProvider: "onmeta", feeBps: 120, etaSeconds: 90, enabled: false }
];

export function listCorridors(): Corridor[] {
  return CORRIDORS.filter((c) => c.enabled);
}

export function findCorridor(id: string): Corridor | undefined {
  return CORRIDORS.find((c) => c.id === id && c.enabled);
}
