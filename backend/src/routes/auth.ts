import { Router } from "express";
import { HttpError, requireSession, signSession, verifyGoogleIdentityToken } from "../auth.js";
import { prisma } from "../prisma.js";

export const authRouter = Router();

authRouter.post("/auth/google", async (req, res, next) => {
  try {
    const { idToken, walletAddress, country } = req.body as {
      idToken?: string;
      walletAddress?: string;
      country?: string;
    };
    if (!idToken) throw new HttpError("idToken is required.", 400);
    if (!walletAddress) throw new HttpError("walletAddress is required.", 400);

    const identity = await verifyGoogleIdentityToken(idToken);
    const user = await prisma.user.upsert({
      where: { googleSubject: identity.sub },
      update: {
        displayName: identity.name,
        photoUrl: identity.picture,
        ...(country ? { country } : {})
      },
      create: {
        googleSubject: identity.sub,
        email: identity.email,
        displayName: identity.name,
        photoUrl: identity.picture,
        walletAddress,
        country: country ?? "US"
      }
    });

    res.json({
      token: signSession({ id: user.id, googleSubject: identity.sub, email: user.email }),
      user
    });
  } catch (err) {
    next(err);
  }
});

authRouter.get("/auth/me", async (req, res, next) => {
  try {
    const session = requireSession(req);
    const user = await prisma.user.findUniqueOrThrow({ where: { id: session.userId } });
    res.json({ user });
  } catch (err) {
    next(err);
  }
});
