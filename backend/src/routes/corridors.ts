import { Router } from "express";
import { computeQuote, findCorridor, listCorridors, validateRail } from "../lib/index.js";
import { HttpError, requireSession } from "../auth.js";
import { fetchFxRate } from "../fx.js";

export const corridorRouter = Router();

corridorRouter.get("/corridors", (req, res, next) => {
  try {
    requireSession(req);
    res.json({ corridors: listCorridors() });
  } catch (err) {
    next(err);
  }
});

corridorRouter.get("/rates", async (req, res, next) => {
  try {
    requireSession(req);
    const corridorId = String(req.query["corridor"] ?? "");
    const amount = Number(req.query["amount"] ?? NaN);
    const corridor = findCorridor(corridorId);
    if (!corridor) throw new HttpError("Unknown or disabled corridor.", 404);
    if (!Number.isFinite(amount) || amount <= 0) {
      throw new HttpError("amount must be a positive number.", 400);
    }
    const fxRate = await fetchFxRate(corridor.sourceCurrency, corridor.destCurrency);
    res.json({ quote: computeQuote(corridor, amount, fxRate) });
  } catch (err) {
    next(err);
  }
});

corridorRouter.post("/recipients/validate", (req, res, next) => {
  try {
    requireSession(req);
    const { corridorId, details } = req.body as {
      corridorId?: string;
      details?: Record<string, string>;
    };
    const corridor = corridorId ? findCorridor(corridorId) : undefined;
    if (!corridor) throw new HttpError("Unknown or disabled corridor.", 404);
    res.json({ check: validateRail(corridor.destRail, details ?? {}) });
  } catch (err) {
    next(err);
  }
});
