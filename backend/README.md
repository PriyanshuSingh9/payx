# PayX Backend

Express REST API for the PayX platform: Google-authenticated sessions, corridor
quotes, transfer orchestration, Solana escrow listening, and ramp webhooks.

## Layout

- `src/server.ts` — app wiring and route mounting
- `src/env.ts` — environment loading with production guards
- `src/auth.ts` — Google ID-token verification, session JWT, error handler
- `src/prisma.ts` — Neon-backed Prisma client
- `src/solana.ts` — RPC connection, escrow PDA derivation, event listener
- `src/fx.ts` — live FX with static fallback
- `src/routes/` — auth, corridors, transfers, ramps
- `prisma/schema.prisma` — User, Corridor, ExchangeRate, Transaction, RampOrder

## Commands (from repo root)

```bash
pnpm install
pnpm --filter @payx/backend db:generate
pnpm dev
```
