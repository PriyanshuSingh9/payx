import path from "node:path";
import { fileURLToPath } from "node:url";
import cors from "cors";
import express from "express";
import { errorHandler } from "./auth.js";
import { env } from "./env.js";
import { authRouter } from "./routes/auth.js";
import { corridorRouter } from "./routes/corridors.js";
import { rampRouter } from "./routes/ramps.js";
import { transferRouter } from "./routes/transfers.js";
import { paymentRouter } from "./routes/payments.js";
import { contactRouter } from "./routes/contacts.js";
import { webhookRouter } from "./routes/webhooks.js";
import { pollerRouter } from "./routes/poller.js";
import { globalPaymentPollerService } from "./services/paymentPoller.js";
import { startEscrowListener } from "./solana.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
app.use(cors({ origin: env.corsOrigin }));
app.use(
  express.json({
    limit: "1mb",
    verify: (req: any, _res, buf) => {
      req.rawBody = buf.toString();
    }
  })
);

// Health check
app.get("/health", (_req, res) => {
  res.json({ ok: true, service: "payx-backend", timestamp: new Date().toISOString() });
});

// Static public UI (Dashboard & Dev Console)
const publicDir = path.join(__dirname, "public");
app.use(express.static(publicDir));
app.get(["/", "/console", "/demo"], (_req, res) => {
  res.sendFile(path.join(publicDir, "index.html"));
});

// API Routes
app.use(webhookRouter);
app.use(pollerRouter);
app.use(paymentRouter);
app.use(contactRouter);
app.use(authRouter);
app.use(corridorRouter);
app.use(transferRouter);
app.use(rampRouter);

// 404 handler
app.use((_req, res) => {
  res.status(404).json({ error: "Route not found." });
});

// Error handling middleware
app.use(errorHandler);

if (env.enableBlockchain) {
  startEscrowListener().catch((err: unknown) => {
    console.warn("[server] Escrow listener idle:", err instanceof Error ? err.message : err);
  });
} else {
  console.log("[server] Blockchain disabled. Set ENABLE_BLOCKCHAIN=true to connect.");
}

if (env.enablePoller && process.env["NODE_ENV"] !== "test") {
  globalPaymentPollerService.start();
  console.log(`[server] Background payment poller started (interval: ${env.pollIntervalMs}ms).`);
}

process.on("SIGINT", () => {
  globalPaymentPollerService.stop();
});
process.on("SIGTERM", () => {
  globalPaymentPollerService.stop();
});

export { app };

// Start HTTP server only if run directly and not in test environment
if (process.env["NODE_ENV"] !== "test" && process.argv[1] && (process.argv[1].endsWith("server.ts") || process.argv[1].endsWith("server.js"))) {
  app.listen(env.port, "0.0.0.0", () => {
    console.log(`PayX backend listening on http://localhost:${env.port}`);
  });
}
