# PayX Handoff — Architecture, Authentication, and UI Integration

## 1. Executive Summary & Repository Status

This handoff documents the current state of **PayX** (USDC on Solana to INR cross-border settlement pipeline) following the complete wiring of the Android presentation layer with the live backend pipeline and resolution of authentication and wallet security mechanisms.

- **Branch**: `main` (commit `4f55ffc`: `feat(ui): wire android presentation layer with live backend pipeline`).
- **Test Suite**: 55/55 unit, domain, and simulation integration tests passing (`pnpm --filter backend test`).
- **TypeScript Compilation**: `pnpm --filter backend build` and `tsc --noEmit` pass with zero errors.
- **Android Compilation**: `./gradlew :app:compileDebugKotlin` and `./gradlew :app:assembleDebug` pass cleanly (`app-debug.apk` built).
- **Environment & Local Services**:
  - Express backend running on `:8787` (`{"ok":true,"service":"payx-backend"}`).
  - Solana test validator on `:8899`.
  - Android port reverse: `adb reverse tcp:8787 tcp:8787` and `adb reverse tcp:8899 tcp:8899`.
  - Shared debug keystore: `android/keystore/debug.keystore` (SHA-1: `1A:CD:8F:0D:29:8A:CD:BD:42:6E:07:A3:7B:64:0C:75:29:1D:96:94`).

---

## 2. Core Architecture Invariants

1. **Package Management**: Strictly `pnpm` exclusively (never `npm`, `yarn`, or `bun`).
2. **Styling & Copy**: Zero emojis across all code, comments, documentation, and commits.
3. **Module Boundaries**:
   - `backend/src/lib/`: Pure domain logic only (money math, quotes, rails, state machine) with zero I/O.
   - `backend/`: Express orchestration, off-ramp adapters, and database writes.
   - `programs/payx_escrow/`: Anchor custody on Solana.
   - `android/`: Native Jetpack Compose presentation (MVVM + StateFlow); private keys never leave Android Keystore.
4. **Typing**: Strict typing with explicit boundaries (no `any` across module edges).
5. **Documentation Synchronization**: Keep `docs/milestones.md` and `docs/contracts.md` synchronized in every commit.

---

## 3. Authentication Flow Architecture

PayX uses Google Identity tokens for user verification and issues 30-day session JWTs for API access.

```
+-------------------+              +-------------------+              +-------------------+
|  Android Device   |              |  Google Identity  |              |  Express Backend  |
+-------------------+              +-------------------+              +-------------------+
          |                                  |                                  |
          | 1. CredentialManager.getCredential                                  |
          |--------------------------------->|                                  |
          | 2. Google ID Token (sub, email)  |                                  |
          |<---------------------------------|                                  |
          |                                                                     |
          | 3. Derive deterministic Solana address (KeystoreWallet)             |
          |                                                                     |
          | 4. POST /auth/google { idToken, walletAddress, country }            |
          |-------------------------------------------------------------------->|
          |                                  | 5. verifyIdToken(idToken)        |
          |                                  |<---------------------------------|
          |                                  | 6. Validated Identity payload    |
          |                                  |--------------------------------->|
          |                                  |                                  |
          |                                  | 7. Upsert User in Postgres       |
          |                                  | 8. Issue Session JWT (30d)       |
          | 9. Response { token, user }      |                                  |
          |<--------------------------------------------------------------------|
          |                                                                     |
          | 10. Store JWT in EncryptedSharedPreferences (SessionStore)          |
          | 11. Subsequent requests: Authorization: Bearer <token>              |
          |-------------------------------------------------------------------->|
```

- **Client Implementation**:
  - `AuthRepository.kt`: Invokes Android `CredentialManager` with `GetSignInWithGoogleOption` using `BuildConfig.GOOGLE_SERVER_CLIENT_ID`. Parses `sub` from the unverified token payload to initialize the local wallet.
  - `SessionStore.kt`: Encrypts and persists session tokens and user profiles using `EncryptedSharedPreferences` with `MasterKey.KeyScheme.AES256_GCM`.
  - `ApiClient.kt`: Automatically attaches `Authorization: Bearer <token>` to all HTTP GET and POST requests.
- **Backend Implementation**:
  - `auth.ts`: Verifies incoming ID tokens via `OAuth2Client.verifyIdToken({ idToken, audience })`. Issues signed JWTs via `signSession(user)` and authenticates requests via `requireSession(req)`.
  - `routes/auth.ts`: Handles `POST /auth/google` (upserting user by `googleSubject`) and `GET /auth/me`.

---

## 4. Wallet Key Generation & Security Model

The mobile wallet is deterministic and non-custodial. Private keys never leave the Android device, and the backend only receives the public Solana Base58 address (ADR-007).

- **Implementation**: `android/app/src/main/java/com/payx/app/wallet/KeystoreWallet.kt`.
- **Key Derivation Process**:
  1. **Deterministic Entropy**:
     `Seed = HMAC-SHA256(key = "payx-wallet-v1", data = googleSubject)`
     Produces a 32-byte seed uniquely bound to the user's immutable Google subject identifier.
  2. **Ed25519 Solana Address**:
     The seed initializes BouncyCastle's `Ed25519PrivateKeyParameters`. The corresponding 32-byte public key is generated and Base58-encoded.
  3. **Memory Cleansing**:
     The raw seed byte array in memory is explicitly overwritten using `seed.fill(0)` inside a `finally` block immediately after use.
  4. **Hardware-Backed AES Wrapping**:
     A hardware-backed AES-256 key (`KeyProperties.KEY_ALGORITHM_AES`, `BLOCK_MODE_GCM`, `ENCRYPTION_PADDING_NONE`) is generated inside `AndroidKeyStore` with alias `payx_wallet_<subject>`.
     The 32-byte seed is encrypted using AES/GCM and stored as `<iv>:<ciphertext>` inside `EncryptedSharedPreferences` (`payx_wallet_wrap`).
  5. **Decryption on Demand**:
     Subsequent wallet address lookups read the encrypted wrap and decrypt via the Keystore key inside the Android Secure Element / TEE.

---

## 5. UI and Backend Pipeline Wiring (Phase 3 Completed)

All primary screens in the native Android app are connected to live backend APIs via ViewModels and `PaymentRepository`.

### A. Dashboard (`DashboardScreen.kt`, `DashboardViewModel.kt`)
- Fetches live FX rate (USDC -> INR), corridor fees, and recent transactions via `PaymentRepository.getRecentPayments()`.
- Displays real user wallet address, balances, and savings metrics.
- "See all" and corridor chip actions navigate to `ReceiverScreen`.
- Quick-action buttons launch `SendScreen`.

### B. Send Flow (`SendScreen.kt`, `SendViewModel.kt`)
- **Debounced Quotes**: Text inputs for amount debounced to query `GET /api/v1/quote?amount=`.
- **Recipient Search**: Live search queries `GET /api/v1/recipients?q=` across contacts, phone numbers, and UPI handles.
- **Execution**: Swipe-to-confirm triggers `POST /api/v1/payments/execute` with default demo sender wallet fallback (`7xK999999999999999999999999999999999999992PD`).
- **Error Handling**: Catches network/validation errors, dismisses the processing overlay, displays an error banner, and prevents false navigation.

### C. Payment Tracker (`TrackerScreen.kt`, `TrackerViewModel.kt`)
- Polls `GET /api/v1/payments/:id` every 800ms.
- Tracks stages: `CREATED` -> `SETTLEMENT_SUBMITTED` -> `SETTLEMENT_CONFIRMED` -> `OFFRAMP_PROCESSING` -> `FIAT_PAYOUT_PENDING` -> `COMPLETED`.
- Prevents false completion: Requires an actual `COMPLETED` status or verified terminal state before displaying the checkmark and printing the thermal receipt.
- Shows actionable retry banners on 404 or connection failures.

### D. Receiver Dashboard (`ReceiverScreen.kt`, `ReceiverViewModel.kt`)
- Full inbound payment monitoring view.
- Hero metrics: Total received in INR and USD, transaction count.
- Filter and search bar to filter incoming transfers by recipient name or UPI handle.
- Backend support: Added `GET /api/v1/receiver/dashboard` in `backend/src/routes/payments.ts` and enhanced `GET /me/receiver-dashboard` in `backend/src/routes/transfers.ts`.

### E. Network Optimization (`ApiClient.kt`)
- Cached resolved base URL in `@Volatile var cachedBaseUrl` to avoid repetitive `/health` latency on every keystroke and polling tick.

---

## 6. Verification Record

- **Backend Automated Tests**: 55 tests passed in 6.4s (`pnpm --filter backend test`).
  - Money math & quotes (30s window, fee calculation).
  - Escrow Anchor program simulation (PDA generation, authority, timelocks, access controls).
  - Rail & address validation (UPI, IFSC, Solana Base58).
  - Payment state machine transitions & idempotency locks.
- **Backend Build & Typecheck**:
  - `pnpm --filter backend build` exited with code 0.
  - `pnpm --filter backend typecheck` exited with code 0.
- **Android Compilation & Packaging**:
  - `./gradlew :app:compileDebugKotlin` completed with 0 errors in 24s.
  - `./gradlew :app:assembleDebug` completed with 0 errors in 42s.

---

## 7. Next Steps & Recommended Actions

1. **End-to-End Runtime Validation on Physical Device**:
   - Start local services: `./dev.sh` (or `solana-test-validator` + `pnpm --filter backend dev`).
   - Run `adb reverse tcp:8787 tcp:8787` and `adb reverse tcp:8899 tcp:8899`.
   - Install `android/app/build/outputs/apk/debug/app-debug.apk` onto an Android device or emulator.
   - Run a live transfer from SendScreen -> TrackerScreen and verify thermal receipt generation.

2. **Database Persistence for Payment State Machine (Priority 2)**:
   - `backend/src/services/paymentStore.ts` currently uses `InMemoryPaymentStore`.
   - Transition to `PostgresPaymentStore` using Prisma models (`Transaction`, `RampOrder`, `Corridor`) so pipeline payments survive backend restarts.

3. **Solana Devnet Live Settlement (Priority 3)**:
   - Anchor escrow program is deployed at `CTFbnKuiHpg5PR5vHpBXJzvLyCGrhMuQp4PbK8ZRGboa`.
   - In `backend/src/services/solanaSettlement.ts`, switch from simulated signatures to live Anchor client RPC calls when `SOLANA_RPC_URL` points to devnet.
