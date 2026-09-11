# PayX — Contracts

## 1. REST API (base `http://localhost:8787`)

Auth: Google ID token only at `POST /auth/google`. Session-protected routes use
`Authorization: Bearer <session JWT>` (30-day expiry). Pipeline orchestration routes
support idempotency via the `Idempotency-Key` header. Errors return
`{ "error": "<message>" }` with standard HTTP status codes.

### Core & Legacy Routes

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/health` | none | `{ ok: true, service: "payx-backend" }` |
| GET | `/` | none | Interactive PayX Dashboard & Dev Console UI |
| GET | `/console` | none | Alias for Dev Console UI |
| POST | `/auth/google` | idToken in body | Verify Google token, sync user, return `{ token, user }` |
| GET | `/auth/me` | JWT | Current user profile |
| GET | `/me/dashboard` | JWT | User, balances, live rate, last 5 sent transfers |
| GET | `/me/receiver-dashboard` | JWT | Received totals + last 20 inbound transfers |
| GET | `/corridors` | JWT | Enabled corridors with rails and fee bps |
| GET | `/rates?source=&target=&amount=` | JWT | Locked quote: rate, fee, receive amount, ETA |
| POST | `/recipients/validate` | JWT | `{ corridor, details }` rail pre-check |
| GET | `/recipients?q=` | JWT | Address book search (max 12) |
| POST | `/transfers` | JWT | `{ corridorId, recipientId, amountSource }` -> `201` intent |
| GET | `/transfers/:id` | JWT | Transfer + escrow + ramp statuses |
| GET | `/onramp/widget` | none | Embedded provider widget HTML |
| POST | `/onramp/complete/:orderId` | JWT | Dev/mock completion hook |
| POST | `/webhooks/onramp` | provider secret | On-ramp settlement notification |
| POST | `/webhooks/offramp` | provider secret | Off-ramp settlement notification |
| POST | `/demo/force-release` | JWT, `ENABLE_DEMO_ADMIN` | Force-release a stuck escrow (dev only) |

### USDC to INR Pipeline Routes (PRD Version 1.0)

| Method | Path | Headers | Description |
|---|---|---|---|
| POST | `/api/v1/payments` | `Idempotency-Key` (opt) | Create payment intent with locked off-ramp quote |
| GET | `/api/v1/payments/:id` | none | Get payment status, Solana tx, off-ramp order, and timeline |
| GET | `/api/v1/payments` | none | List recent payments (limit query parameter supported) |
| POST | `/api/v1/payments/:id/quote` | none | Refresh off-ramp quote |
| POST | `/api/v1/payments/:id/confirm` | none | Sender confirms payment before quote expires |
| POST | `/api/v1/payments/:id/settle` | none | Settle USDC on Solana (devnet verification or simulation) |
| POST | `/api/v1/payments/:id/offramp` | none | Create Onmeta off-ramp order and link Solana tx signature |
| POST | `/api/v1/payments/:id/reconcile` | none | Reconcile pending payment status against off-ramp provider when webhook is delayed |
| POST | `/api/v1/payments/execute` | `Idempotency-Key` (opt) | Execute full 10-stage pipeline to COMPLETED in one call |
| POST | `/api/v1/payments/:id/simulate-step` | none | Dev Console step driver (`confirm`, `settle`, `create_offramp`, `payout_processing`, `payout_success`) |
| POST | `/api/v1/payments/:id/fail` | none | Inject failure state (`insufficient_funds`, `quote_expired`, `solana_failed`, `offramp_failed`, `payout_failed`) |
| GET | `/api/v1/quote?amount=` | none | Pure quote preview for USDC to INR |
| POST | `/api/v1/recipients/validate` | none | Validate Indian UPI ID or Bank Account + IFSC format |
| POST | `/webhooks/onmeta` | `x-onmeta-signature`, `x-event-id` | Asynchronous Onmeta off-ramp webhook with duplicate protection |

## 2. Unified Payment State Machine

The pipeline implements the unified state machine defined in PRD Section 14:

```text
CREATED
   |
   v
QUOTE_PENDING
   |
   v
AWAITING_CONFIRMATION
   |
   v
SETTLEMENT_PENDING
   |
   v
SETTLEMENT_SUBMITTED
   |
   v
SETTLEMENT_CONFIRMED
   |
   v
OFFRAMP_CREATED
   |
   v
OFFRAMP_PROCESSING
   |
   v
FIAT_PAYOUT_PENDING
   |
   v
COMPLETED
```

Failure states: `PAYMENT_FAILED`, `SETTLEMENT_FAILED`, `OFFRAMP_FAILED`, `PAYOUT_FAILED`, `QUOTE_EXPIRED`.

Provider status mapping:
- `cryptoInit` -> `OFFRAMP_PROCESSING`
- `fiatPending` -> `FIAT_PAYOUT_PENDING`
- `payoutSuccess` -> `COMPLETED`
- `payoutFailed` -> `PAYOUT_FAILED`

## 3. Provider Adapter Contract (`OffRampProvider`)

```typescript
export interface OffRampProvider {
  readonly providerName: string;
  getQuote(amountUsdc: number): Promise<OffRampQuote>;
  createOrder(params: CreateOrderParams): Promise<OffRampOrderResult>;
  submitTransaction(orderId: string, txHash: string): Promise<SubmitTxResult>;
  getStatus(orderId: string): Promise<OffRampStatusResult>;
}
```

Implementations:
- `MockOffRampAdapter`: Configurable simulation adapter with failure injection and manual status progression.
- `OnmetaAdapter`: Connects to Onmeta staging/production REST endpoints with HMAC-SHA256 signature verification and automatic simulation fallback.

## 4. Database (Neon Postgres via Prisma)

- `User`: id, googleSubject (unique), email (unique), displayName, photoUrl,
  phoneNumber, walletAddress (unique, Solana base58), country, bankDetails,
  availableBalanceUsd, lifetimeSavingsUsd, timestamps.
- `Corridor`: sourceCurrency, destCurrency, destRail (UPI/PIX/SEPA/FPS/SPEI),
  inProvider, outProvider, feeBps, etaSeconds, enabled, unique(source, dest).
- `ExchangeRate`: baseCurrency, quoteCurrency, rate, cheaperPercentage, asOf,
  unique(base, quote).
- `Transaction`: sender, receiver, corridor, amountSource, amountUsdc,
  amountDest, feeSource, status,
  lockedSourceToUsdc, lockedUsdcToDest, escrowPda, escrowState, escrowTxHash,
  releaseTxHash, solanaSignature, timestamps + indexes.
- `RampOrder`: type (`onramp | offramp`), transaction, externalOrderId (unique),
  status, fiatCurrency, fiatAmount, cryptoCurrency (USDC), cryptoAmount,
  walletAddress, bankDetails, txHash, metadata.

## 5. Anchor program `payx_escrow`

- `Escrow` PDA seeds: `["escrow", escrow_id.to_le_bytes()]`.
- `Vault` PDA seeds: `["vault", escrow_pda.as_ref()]`.
- Fields: id (u64), sender (Pubkey), receiver (Pubkey), mint (USDC Pubkey),
  amount (u64 micro-USDC), operator (Pubkey),
  state (`Initialized | Deposited | ReadyForFunding | Released | Refunded | Cancelled`),
  depositTimestamp (i64), bump (u8). Total space: 162 bytes (`Escrow::LEN`).
- Instructions: `initialize_escrow` (operator), `deposit` (operator or sender),
  `confirm_funding` (operator), `release` (operator, requires ReadyForFunding),
  `refund` (operator), `refund_timeout` (permissionless after 24h),
  `cancel` (operator or sender, requires Initialized).
- Events: `EscrowInitialized`, `EscrowDeposited`, `FundingConfirmed`,
  `EscrowReleased`, `EscrowRefunded`, `EscrowCancelled`.
- Errors: `ZeroAmount`, `BadMint`, `BadState`, `Unauthorized`, `TimeoutNotReached`.

## 6. `backend/src/lib` Domain API

- `computeOffRampQuote(params)`: Money math for gross INR, 0.50% fee, net INR, and 30s expiry.
- `isQuoteExpired(expiresAt, now)`: Temporal expiration check.
- `validateIndianRecipient(details)`: UPI handle (`name@bank`) and account + IFSC regex validator.
- `validateSolanaAddress(address)`: Curve validation for Solana base58 public keys.
- `generatePaymentId(prefix)`: Generates correlation IDs matching `PX-XXXXXX`.
- `canTransitionPayment(from, to)`: Enforces valid state machine progressions.
