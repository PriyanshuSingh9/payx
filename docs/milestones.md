# PayX — Milestones

## Phase 0 — Kickoff (this change)

- [x] Scope locked: multi-corridor, native Compose, Express, Neon+Prisma
- [x] docs/: prd, architecture, contracts, decisions, milestones
- [x] pnpm backend boots: install + typecheck + /health green
- [x] Anchor escrow skeleton compiles under `cargo check`
- [x] Android module structure in place (needs SDK to assemble)
- [x] Local AGENTS.md bootstrapped

## Phase 1 — Backend + domain + local chain

- [x] Port reference orchestration (intents, mock ramps, poller) to Solana client
- [x] Unit tests for `src/lib` (quotes, rails, corridors, unified state machine)
- [x] API tests for quote/transfer/webhook routes and Dev Console
- [x] USDC -> USDC -> INR pipeline simulation engine with Mock and Onmeta adapters
- [x] Webhook processing with idempotency and duplicate event protection
- [x] `solana-test-validator` local flow via `dev.sh`
- [x] Prisma migrate against Neon branch

## Phase 2 — Anchor program on devnet

- [x] Install Solana CLI + Anchor toolchain (Solana CLI 4.2.2 / Anchor 0.31.0 via avm)
- [x] `payx_escrow` contract logic and security hardening (access control, PDA vault validation, 24h timelock)
- [x] `payx_escrow` unit tests (Rust `cargo test`) + integration test suite (`tests/payx_escrow.test.ts`)
- [ ] Deploy to devnet, record program ID in backend env + contracts doc
- [ ] Backend event listener switches from polling to websocket
- [ ] Timelock refund path exercised on devnet

## Phase 3 — Android app

- [x] Android Studio + SDK 36, Compose BOM, MWA dependency
- [x] Auth (Google sign-in, Keystore wallet, session storage)
- [x] Opening intro animation with dynamic component merging and page transition to Login
- [x] Corridor picker, quote card, rail validation, 4-stage tracker, receipt
- [x] Receiver view, address book, settings/logout
- [x] Physical device deployment and verification

## Phase 4 — Ramps, hardening, mainnet readiness

- [x] Provider adapter architecture (OffRampProvider, OnmetaAdapter, MockOffRampAdapter)
- [x] Webhook delay reconciliation and rate limiting for payment pipeline
- [ ] Real Transak/Stripe on-ramp + Onmeta off-ramp behind corridor config
- [ ] KYC hooks, reconciliation export, audit logging
- [ ] Mainnet program deploy, USDC mainnet mint wiring, Solscan links
- [ ] Load test quote path; chaos test poller/restart recovery
