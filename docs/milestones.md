# PayX — Milestones

## Phase 0 — Kickoff (this change)

- [x] Scope locked: multi-corridor, native Compose, Express, Neon+Prisma
- [x] docs/: prd, architecture, contracts, decisions, milestones
- [x] pnpm backend boots: install + typecheck + /health green
- [x] Anchor escrow skeleton compiles under `cargo check`
- [x] Android module structure in place (needs SDK to assemble)
- [x] Local AGENTS.md bootstrapped

## Phase 1 — Backend + domain + local chain

- [ ] Port reference orchestration (intents, mock ramps, poller) to Solana client
- [ ] Unit tests for `src/lib` (quotes, rails, corridors)
- [ ] API tests for quote/transfer/webhook routes
- [ ] `solana-test-validator` local flow via `dev.sh`
- [ ] Prisma migrate against Neon branch

## Phase 2 — Anchor program on devnet

- [ ] Install Solana CLI + Anchor toolchain (see verification notes)
- [ ] `payx_escrow` unit + integration tests (`anchor test`, local validator)
- [ ] Deploy to devnet, record program ID in backend env + contracts doc
- [ ] Backend event listener switches from polling to состави websocket
- [ ] Timelock refund path exercised on devnet

## Phase 3 — Android app

- [ ] Android Studio + SDK 36, Compose BOM, MWA dependency
- [ ] Auth (Google sign-in, Keystore wallet, session storage)
- [ ] Corridor picker, quote card, rail validation, 4-stage tracker, receipt
- [ ] Receiver view, address book, settings/logout
- [ ] `adb reverse` device testing against local backend

## Phase 4 — Ramps, hardening, mainnet readiness

- [ ] Real Transak/Stripe on-ramp + OnMeta off-ramp behind corridor config
- [ ] KYC hooks, reconciliation export, rate-limiting, audit logging
- [ ] Mainnet program deploy, USDC mainnet mint wiring, Solscan links
- [ ] Load test quote path; chaos test poller/restart recovery
