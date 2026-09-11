# PayX — Architecture

## 1. Module map (pnpm monorepo)

```
payx/
  backend/          # Express REST API (pnpm, TypeScript)
  backend/src/lib/  # Domain: corridors, quotes, rails, statuses (no I/O)
  programs/payx_escrow/  # Anchor escrow program (Rust)
  android/               # Native Kotlin app (Jetpack Compose, MVVM)
  docs/                  # prd, architecture, contracts, decisions, milestones
  dev.sh                 # One-command local environment
```

Ownership: backend owns orchestration and fiat; program owns custody;
`src/lib` owns shared math and validation (no I/O); android owns
presentation and keys.

## 2. Runtime topology

```
Android app (Kotlin)
  |  HTTPS :8787, Bearer JWT
Backend API (Express :8787)
  |-- Neon Postgres (Prisma) .... users, corridors, transactions, ramp_orders
  |-- Solana RPC ................ devnet / mainnet / local test validator :8899
  |     PayX escrow program (PDA per escrow, SPL USDC vault)
  |-- On-ramp providers ......... Transak / Stripe / MoonPay (+ mock in dev)
  |-- Off-ramp providers ........ OnMeta / regional rails (+ mock in dev)
  |-- Google Identity ........... ID-token verification at /auth/google only
```

## 3. Payment lifecycle

1. Client requests quote (`GET /rates`): corridor, live FX, fee breakdown, ETA.
2. Client validates recipient rail (`POST /recipients/validate`).
3. Client creates transfer intent (`POST /transfers`): reserves quote, status `pending`.
4. Sender completes on-ramp; provider webhook (or mock completion) credits USDC
   to the sender's wallet; backend moves USDC into the escrow PDA (`escrow_locked`).
5. Backend watches program events; on `EscrowLocked`, fires off-ramp payout
   (`offramp_pending`).
6. Off-ramp confirms readiness; backend calls `confirm_funding`, then on fiat
   dispatch calls `release` (`escrow_released` → `completed`).
7. Any failure before release triggers `refund` and sender balance restore
   (`refunded` / `failed`).

Rate lock: quote binds `lockedUsdToUsdc` and `lockedSourceToDest` at intent time.
Escrow timeout (24h) lets anyone permissionlessly refund a stuck `Deposited`
escrow.

## 4. Data flow invariants

- Backend is the only writer of fiat-side state; the program is the only
  custodian of in-flight USDC.
- DB transaction rows mirror on-chain escrow state; the poller reconciles drift.
- Mobile never holds ramp secrets; all provider calls go through the backend.
- Money math lives in `backend/src/lib` and is shared verbatim by backend and tests.

## 5. Local development

`dev.sh` starts: solana-test-validator `:8899` (if installed), backend
`pnpm --filter backend dev` on `:8787`, `adb reverse tcp:8787` for devices, then
prints the Android Studio run hint. Ports: 8787 API, 8899 validator.
