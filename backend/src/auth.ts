import type { NextFunction, Request, Response } from "express";
import { OAuth2Client } from "google-auth-library";
import jwt from "jsonwebtoken";
import { env } from "./env.js";

const googleClient = new OAuth2Client(env.googleClientId);

export interface SessionClaims {
  userId: string;
  subject: string;
  email: string;
}

export interface GoogleIdentity {
  sub: string;
  email: string;
  name: string | null;
  picture: string | null;
}

export async function verifyGoogleIdentityToken(idToken: string): Promise<GoogleIdentity> {
  try {
    const ticket = await googleClient.verifyIdToken({ idToken, audience: env.googleClientId });
    const payload = ticket.getPayload();
    if (!payload?.sub || !payload.email) {
      throw new Error("Invalid Google token payload.");
    }
    return {
      sub: payload.sub,
      email: payload.email,
      name: payload.name ?? null,
      picture: payload.picture ?? null
    };
  } catch {
    throw new HttpError("Invalid or expired Google ID token.", 401);
  }
}

export function signSession(user: { id: string; googleSubject: string | null; email: string }): string {
  return jwt.sign(
    { userId: user.id, subject: user.googleSubject, email: user.email },
    env.jwtSecret,
    { expiresIn: "30d" }
  );
}

export class HttpError extends Error {
  constructor(
    message: string,
    readonly statusCode: number
  ) {
    super(message);
  }
}

export function requireSession(req: Request): SessionClaims {
  const header = req.headers.authorization;
  if (!header || !header.startsWith("Bearer ")) {
    throw new HttpError("Missing Authorization bearer token.", 401);
  }
  try {
    const session = jwt.verify(header.slice("Bearer ".length).trim(), env.jwtSecret) as Partial<SessionClaims>;
    if (!session.userId || !session.subject || !session.email) {
      throw new Error("Invalid session payload.");
    }
    return { userId: session.userId, subject: session.subject, email: session.email };
  } catch {
    throw new HttpError("Invalid or expired session token.", 401);
  }
}

export function errorHandler(err: unknown, _req: Request, res: Response, _next: NextFunction): void {
  if (err instanceof HttpError) {
    res.status(err.statusCode).json({ error: err.message });
    return;
  }
  const message = err instanceof Error ? err.message : "Unknown server error.";
  res.status(500).json({ error: message });
}
