# PayX Handoff — Final Run Towards Project Completion

## 1. Context & Executive Summary

This handoff prepares a fresh agent for the final run to bring **PayX** (USDC -> Solana -> Onmeta -> INR Cross-Border Settlement Pipeline) to full operational completion.

### Current Repository State
- **Branch**: `main` (working tree clean, fast-forward merged with `blockchain` and `origin/rudra`).
- **Pending Action**: `git push origin main` requires manual execution by the developer due to environment deny rules.
- **Authentication**: Google Sign-In with Credential Manager verified on physical Android device (`SM-S711B`, Samsung Galaxy S23 FE).
- **Keystore**: Shared debug keystore committed at `android/keystore/debug.keystore` (SHA-1: `1A:CD:8F:0D:29:8A:CD:BD:42:6E:07:A3:7B:64:0C:75:29:1D:96:94`), ensuring consistent builds across Linux, macOS, and Windows.
- **Local Services**:
  - Express backend running on `:8787` (`{"ok":true,"service":"payx-backend"}`).
  - Solana test validator running on `:8899`.
  - ADB reverse port tunnels active for `8787` and `8899`.
- **Test Suite**: 52/52 integration and unit tests passing (`pnpm --filter backend test`).

---

## 2. Invariants & Repo Rules (Must Retain)

1. **Package Manager**: Strictly `pnpm` exclusively (never `npm`, `yarn`, or `bun`).
2. **Emojis**: Strictly zero emojis across all code, comments, documentation, and commits.
3. **Architecture Boundary**:
   - `backend/src/lib/`: Pure domain logic only (money math, quotes, rails, state machine) — zero I/O.
   - `backend/`: Express orchestration, off-ramp adapters, and database writes.
   - `programs/payx_escrow/`: Anchor custody on Solana.
   - `android/`: Native Jetpack Compose presentation; private keys never leave Android Keystore.
4. **Documentation Synchronization**: Keep `docs/milestones.md` and `docs/contracts.md` synchronized in every commit.

---

## 3. Work Remaining for Project Completion

### Priority 1: Connect Android App to Live Backend APIs
Done. Send loads `GET /recipients` and live `GET /api/v1/quote`; confirm calls
`POST /api/v1/payments`. Tracker polls `GET /api/v1/payments/:id` and drives
`simulate-step` (`AWAITING_CONFIRMATION` -> `SETTLEMENT_CONFIRMED` ->
`OFFRAMP_PROCESSING` -> `COMPLETED`). Dashboard reads `GET /api/v1/payments`.
On-ramp/off-ramp partners are out of scope for the hackathon; the app uses
`full_simulation`.

### Priority 2: Database Persistence for Payment State Machine
- `backend/src/services/paymentStore.ts` currently uses `InMemoryPaymentStore` backed by in-memory `Map` instances.
- Implement `PostgresPaymentStore` utilizing Prisma (`backend/prisma/schema.prisma`) to persist payments, off-ramp orders, timeline logs, and idempotency locks in PostgreSQL (Neon) across process restarts.

### Priority 3: Deploy Anchor Escrow to Solana Devnet
- `programs/payx_escrow/src/lib.rs` currently contains placeholder `declare_id!("11111111111111111111111111111111")`.
- Steps:
  1. Generate keypair: `solana-keygen new -o target/deploy/payx_escrow-keypair.json --force --no-bip39-passphrase`
  2. Sync keys: `anchor keys sync`
  3. Build & Deploy: `anchor build && anchor deploy --provider.cluster devnet`
  4. Update `PAYX_PROGRAM_ID` in `backend/.env`.
  5. Connect Anchor client in `backend/src/services/solanaSettlement.ts` to execute real escrow initialization, deposits, and releases.

### Priority 4: Live Off-Ramp API Credentials & Webhook Security
- `backend/src/adapters/offramp/onmetaAdapter.ts` currently falls back to `computeOffRampQuote` simulation because `ONMETA_API_KEY` is unset.
- Add staging credentials to `.env` (`ONMETA_API_KEY`, `ONMETA_CLIENT_SECRET`, `ONMETA_WEBHOOK_SECRET`).
- Verify live webhook signature verification under `POST /webhooks/onmeta`.

---

## 4. Suggested Skills for the Next Agent

1. **`doc-sync`**: Call before committing changes to maintain strict consistency between code changes and `docs/milestones.md` / `docs/contracts.md`.
2. **`tdd`**: Call when implementing `PostgresPaymentStore` and wiring Anchor client calls to ensure state machine transitions remain completely covered by automated tests.
3. **`blast-radius`**: Call before altering database schema or core API contracts to verify downstream compatibility with both Android and the Web Dev Console.
4. **`review-security`**: Call when handling keypair signing, secret management, or webhook HMAC verification.

---

## 5. Key File Index

- **PRD & Architecture**: `docs/milestones.md`, `docs/contracts.md`, `docs/setup.md`
- **Smart Contract**: `programs/payx_escrow/src/lib.rs`, `Anchor.toml`
- **Backend Pipeline & Adapters**:
  - `backend/src/services/paymentPipeline.ts`
  - `backend/src/services/solanaSettlement.ts`
  - `backend/src/services/paymentStore.ts`
  - `backend/src/adapters/offramp/onmetaAdapter.ts`
  - `backend/src/routes/payments.ts`
- **Web Simulation Console**: `backend/src/public/index.html` (serves at `http://localhost:8787`)
- **Android App**:
  - `android/app/src/main/java/com/payx/app/ui/PayxApp.kt`
  - `android/app/src/main/java/com/payx/app/ui/screens/` (`DashboardScreen.kt`, `SendScreen.kt`, `TrackerScreen.kt`)
  - `android/app/src/main/java/com/payx/app/data/ApiClient.kt`
  - `android/app/src/main/java/com/payx/app/wallet/KeystoreWallet.kt`
