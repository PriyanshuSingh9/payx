# PayX — Architecture Decision Records

## ADR-001: Solana + Anchor for settlement (not EVM)

Status: accepted. The reference implementation settled on EVM/Foundry as a
prototype shortcut while its own PRDs specified Solana. PayX implements the
specified stack: SPL USDC on Solana clears in ~400ms slots at ~$0.00025 per
transaction, which the 30-90s / 0.95% product targets depend on. PDA escrows give
per-payment custody without deploying a contract per transfer.

## ADR-002: Native Kotlin Android app (not Flutter)

Status: accepted. The defined stack is Kotlin. A native Jetpack Compose MVVM app
uses the Android Keystore directly for Ed25519 keys and the Solana Mobile Wallet
Adapter for signing, with no bridge layer. iOS is out of scope for kickoff.

## ADR-003: Single-package backend, domain as `src/lib` (no workspace)

Status: accepted (supersedes the earlier pnpm-workspace proposal). With one
TypeScript consumer, a workspace added indirection (build hooks, dist
resolution) without benefit. Domain code (corridors, quotes, rail validators,
statuses) lives in `backend/src/lib/` as dependency-free modules. If a
second consumer (web admin, CLI, indexer) appears, re-extract `src/lib` into a
workspace package; the module boundaries are already drawn.

## ADR-004: Express backend (ported from reference)

Status: accepted. The reference backend's route surface, two-token auth, and
orchestration logic are proven. Porting to Express preserves that behavior while
swapping `ethers` for `@solana/web3.js` and generalizing single-corridor fields
to the corridor model.

## ADR-005: Neon Postgres + Prisma (ported schema, generalized)

Status: accepted. Same operational profile as the reference (serverless Postgres,
pooled runtime URL + direct migration URL). Schema generalizes `amountInr` to
`amountDest`, adds the `Corridor` table, and records Solana-native escrow fields
(PDA, signatures) instead of EVM hashes.

## ADR-006: Local mock workflow before provider onboarding

Status: accepted. The backend runs a timed local mock workflow for every
USDC-to-INR payment. Provider APIs and provider webhook routes remain absent
until KYB registration and production operations are in place.

## ADR-007: Deterministic Keystore wallets, server never sees keys

Status: accepted. Device derives Ed25519 from the Google subject inside the
Android Keystore; only the base58 address is sent to the backend. Loss model and
rotation are documented in Phase 3.
