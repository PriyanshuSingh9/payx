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
- [x] API tests for quote, transfer, and Dev Console routes
- [x] Timed USDC -> INR mock pipeline simulation
- [x] `solana-test-validator` local flow via `dev.sh`
- [x] Prisma migrate against Neon branch

## Phase 2 — Anchor program on devnet

- [x] Install Solana CLI + Anchor toolchain (Solana CLI 4.2.2 / Anchor 0.31.0 via avm)
- [x] `payx_escrow` contract logic and security hardening (access control, PDA vault validation, 24h timelock)
- [x] `payx_escrow` unit tests (Rust `cargo test`) + integration test suite (`tests/payx_escrow.test.ts`)
- [x] Deploy to devnet, record program ID in backend env + contracts doc (`CTFbnKuiHpg5PR5vHpBXJzvLyCGrhMuQp4PbK8ZRGboa`)
- [x] Hybrid settlement ingestion: Helius webhooks (`POST /webhooks/helius`) with secret auth and deduplication + background poller reconciler (`PaymentPollerService`)
- [ ] Timelock refund path exercised on devnet

## Phase 3 — Android app

- [x] Android Studio + SDK 36, Compose BOM, MWA dependency
- [x] Auth (Google sign-in, Keystore wallet, session storage)
- [x] Opening intro animation with dynamic component merging and page transition to Login
- [x] Send / tracker / dashboard live payment APIs (screens wired to live PaymentRepository, ViewModels, and pipeline APIs)
- [x] Receiver view live inbound list (screen wired to live ReceiverDashboard API, ReceiverViewModel, and PaymentRepository)
- [x] Corridor picker, quote card, rail validation, 4-stage tracker, receipt
- [x] Receiver view, address book, settings/logout
- [x] `adb reverse` device testing against local backend
- [x] Physical device deployment and verification
- [x] Branded PayX launcher icon with adaptive vector layers and multi-density mipmaps

## Phase 4 — Ramps, hardening, mainnet readiness

- [x] Mock off-ramp adapter and rate limiting for payment pipeline
- [ ] Real provider integration after KYB and operational readiness
- [ ] KYC hooks, reconciliation export, audit logging
- [ ] Mainnet program deploy, USDC mainnet mint wiring, Solscan links
- [ ] Load test quote path; chaos test poller/restart recovery
