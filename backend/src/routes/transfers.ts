import { Router } from "express";
import { HttpError, requireSession } from "../auth.js";

export const transferRouter = Router();

// Phase 1: port intent creation, mock on-ramp completion, escrow deposit,
// off-ramp pipeline, and status polling from the reference orchestration.
transferRouter.post("/transfers", (req, res, next) => {
  try {
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});

transferRouter.get("/transfers/:id", (req, res, next) => {
  try {
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});

transferRouter.get("/recipients", (req, res, next) => {
  try {
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});

transferRouter.get("/me/dashboard", (req, res, next) => {
  try {
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});

transferRouter.get("/me/receiver-dashboard", (req, res, next) => {
  try {
    requireSession(req);
    next(new HttpError("Not implemented in kickoff. See docs/milestones.md Phase 1.", 501));
  } catch (err) {
    next(err);
  }
});
