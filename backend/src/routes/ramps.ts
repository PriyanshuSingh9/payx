import { Router } from "express";
import { HttpError, requireSession } from "../auth.js";
import { env } from "../env.js";

export const rampRouter = Router();

rampRouter.get("/onramp/widget", (_req, res) => {
  // Phase 1: render the embedded provider widget for the corridor's inProvider.
  res.type("html").send("<!doctype html><html><body>PayX on-ramp widget (Phase 1).</body></html>");
});

rampRouter.post("/onramp/complete/:orderId", (req, res, next) => {
  try {
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});

rampRouter.post("/webhooks/onramp", (_req, res) => {
  // Phase 1: verify provider secret, credit USDC intent, start escrow deposit.
  res.status(501).json({ error: "Not implemented in kickoff. See docs/milestones.md Phase 1." });
});

rampRouter.post("/webhooks/offramp", (_req, res) => {
  // Phase 1: verify provider secret, advance off-ramp and release escrow.
  res.status(501).json({ error: "Not implemented in kickoff. See docs/milestones.md Phase 1." });
});

rampRouter.post("/demo/force-release", (req, res, next) => {
  try {
    if (!env.enableDemoAdmin) throw new HttpError("Demo admin routes are disabled.", 404);
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});
