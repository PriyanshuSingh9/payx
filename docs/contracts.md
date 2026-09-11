# PayX — Contracts

## 1. REST API (base `http://localhost:8787`)

Auth: Google ID token only at `POST /auth/google`. Everything else uses
`Authorization: Bearer <session JWT>` (30-day expiry). Errors are
`{ "error": "<message>" }` with standard HTTP status codes.

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/health` | none | `{ ok, service }` |
| POST | `/auth/google` | idToken in body | Verify Google token, sync user, return `{ token, user }` |
| GET | `/auth/me` | JWT | Current user profile |
| GET | `/me/dashboard` | JWT | User, balances, live rate, last 5 sent transfers |
| GET | `/me/receiver-dashboard` | JWT | Received totals + last 20 inbound transfers |
| GET | `/corridors` | JWT | Enabled corridors with rails and fee bps |
| GET | `/rates?source=&target=&amount=` | JWT | Locked quote: rate, fee, receive amount, ETA |
| POST | `/recipients/validate` | JWT | `{ corridor, details }` rail pre-check |
| GET | `/recipients?q=` | JWT | Address book search (max 12) |
| POST | `/transfers` | JWT | `{ corridorId, recipientId, amountSource }` → `201` intent |
| GET | `/transfers/:id` | JWT | Transfer + escrow + ramp statuses (sender/receiver only) |
| GET | `/onramp/widget?...` | none | Embedded provider widget HTML |
| POST | `/onramp/complete/:orderId` | JWT | Dev/mock completion hook |
| POST | `/webhooks/onramp` | provider secret | On-ramp settlement notification |
| POST | `/webhooks/offramp` | provider secret | Off-ramp settlement notification |
| POST | `/demo/force-release` | JWT, `ENABLE_DEMO_ADMIN` | Force-release a stuck escrow (dev only) |

## 2. Database (Neon Postgres via Prisma)

- `User`: id, googleSubject (unique), email (unique), displayName, photoUrl,
  phoneNumber, walletAddress (unique, Solana base58), country, bankDetails,
  availableBalanceUsd, lifetimeSavingsUsd, timestamps.
- `Corridor`: sourceCurrency, destCurrency, destRail (UPI/PIX/SEPA/FPS/SPEI),
  inProvider, outProvider, feeBps, etaSeconds, enabled, unique(source, dest).
- `ExchangeRate`: baseCurrency, quoteCurrency, rate, cheaperPercentage, asOf,
  unique(base, quote).
- `Transaction`: sender, receiver, corridor, amountSource, amountUsdc,
  amountDest, feeSource, status (`pending | escrow_locked | offramp_pending |
  offramp_ready | escrow_released | completed | failed | refunded`),
  lockedSourceToUsdc, lockedUsdcToDest, escrowPda, escrowState, escrowTxHash,
  releaseTxHash, solanaSignature, timestamps + indexes.
- `RampOrder`: type (`onramp | offramp`), transaction, externalOrderId (unique),
  status, fiatCurrency, fiatAmount, cryptoCurrency (USDC), cryptoAmount,
  walletAddress, bankDetails, txHash, metadata.

## 3. Anchor program `payx_escrow`

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


## 4. `backend/src/lib` API

- `listCorridors(): Corridor[]` — launch corridor table.
- `computeQuote(corridor, amountSource, fxRate): Quote` — fee bps math, exact
  receive amount, ETA.
- `validateRail(rail, details): RailCheck` — UPI / PIX / IBAN / FPS / CLABE.
- `TransactionStatus` — string union mirroring the DB enum.
