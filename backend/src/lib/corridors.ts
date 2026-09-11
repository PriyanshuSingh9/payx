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

// The local demo supports one USDC-to-INR route only.
export const CORRIDORS: Corridor[] = [
  { id: "USDC-INR-UPI", sourceCurrency: "USDC", destCurrency: "INR", destRail: "UPI", inProvider: "mock", outProvider: "mock", feeBps: 50, etaSeconds: 1, enabled: true }
];

export function listCorridors(): Corridor[] {
  return CORRIDORS.filter((c) => c.enabled);
}

export function findCorridor(id: string): Corridor | undefined {
  return CORRIDORS.find((c) => c.id === id && c.enabled);
}
