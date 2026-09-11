import cors from "cors";
import express from "express";
import { errorHandler } from "./auth.js";
import { env } from "./env.js";
import { authRouter } from "./routes/auth.js";
import { corridorRouter } from "./routes/corridors.js";
import { rampRouter } from "./routes/ramps.js";
import { transferRouter } from "./routes/transfers.js";
import { startEscrowListener } from "./solana.js";

const app = express();
app.use(cors({ origin: env.corsOrigin }));
app.use(express.json({ limit: "1mb" }));

app.get("/health", (_req, res) => {
  res.json({ ok: true, service: "payx-backend" });
});

app.use(authRouter);
app.use(corridorRouter);
app.use(transferRouter);
app.use(rampRouter);

app.use((_req, res) => {
  res.status(404).json({ error: "Route not found." });
});
app.use(errorHandler);

if (env.enableBlockchain) {
  startEscrowListener().catch((err: unknown) => {
    console.warn("[server] Escrow listener idle:", err instanceof Error ? err.message : err);
  });
} else {
  console.log("[server] Blockchain disabled. Set ENABLE_BLOCKCHAIN=true to connect.");
}

app.listen(env.port, "0.0.0.0", () => {
  console.log(`PayX backend listening on http://localhost:${env.port}`);
});
