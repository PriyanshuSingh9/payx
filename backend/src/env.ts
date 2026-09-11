import "dotenv/config";

const DEFAULT_PORT = 8787;

function read(name: string): string | undefined {
  const value = process.env[name];
  return value && value.trim().length > 0 ? value : undefined;
}

function requireInProduction(name: string, fallback?: string): string {
  const value = read(name);
  if (value) return value;
  if (process.env["NODE_ENV"] === "production") {
    throw new Error(`${name} is required in production.`);
  }
  if (fallback === undefined) {
    throw new Error(`${name} is required.`);
  }
  return fallback;
}

export const env = {
  port: Number(read("PORT") ?? DEFAULT_PORT),
  enableDemoBootstrap: (read("ENABLE_DEMO_BOOTSTRAP") ?? "true").toLowerCase() !== "false",
  enableBlockchain: (read("ENABLE_BLOCKCHAIN") ?? "true").toLowerCase() !== "false",
  enableDemoAdmin: (read("ENABLE_DEMO_ADMIN") ?? "false").toLowerCase() === "true",
  googleClientId: requireInProduction("GOOGLE_CLIENT_ID"),
  jwtSecret: requireInProduction("JWT_SECRET", "super_secret_dev_key_for_payx"),
  corsOrigin: requireInProduction("CORS_ORIGIN", "*"),

  // ─── Solana ───────────────────────────────────────────────────
  solanaRpcUrl: read("SOLANA_RPC_URL") ?? "http://127.0.0.1:8899",
  programId: read("PAYX_PROGRAM_ID") ?? "",
  usdcMint: read("USDC_MINT_ADDRESS") ?? "",
  operatorKeypairPath: read("OPERATOR_KEYPAIR_PATH") ?? "",

  // ─── Ramps ────────────────────────────────────────────────────
  mockRampDelayMs: Number(read("MOCK_RAMP_DELAY_MS") ?? "250"),

  // ─── Webhooks & Polling ────────────────────────────────────────
  heliusWebhookSecret: read("HELIUS_WEBHOOK_SECRET") ?? "",
  enablePoller: (read("ENABLE_POLLER") ?? "true").toLowerCase() !== "false",
  pollIntervalMs: Number(read("POLL_INTERVAL_MS") ?? "5000")
};


