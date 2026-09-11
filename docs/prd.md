# PayX — Product Requirements Document

## 1. Vision

PayX is a universal cross-currency payment platform: pay anyone, anywhere, in any
supported currency. Fast. Fair. Borderless.

Senders pay in local fiat. Funds bridge through SPL USDC on Solana, clear through a
trustless Anchor escrow, and land as local fiat on destination rails (UPI, PIX,
SEPA, FPS, SPEI). Settlement target is 30-90 seconds end to end at a headline fee
of ~0.95%.

PayX evolves the RemitFlow prototype (bilateral US-to-IN P2P, single ramp, ~1.5%)
into a multi-corridor enterprise engine covering P2P, contractor payouts, B2B
invoicing, and merchant checkout.

## 2. Personas

| Persona | Needs |
|---|---|
| Peer sender (US/EU/UK) | Send money home in 1 minute, know the exact received amount up front |
| Cross-border contractor | Get paid in local currency without PayPal-scale fees or 5-day waits |
| SME finance lead | Batch contractor payroll across corridors with one reconciliation export |
| Merchant | Accept cross-currency checkout with instant fiat settlement, no chargebacks |
| Receiver (IN/BR/EU/GB/MX) | Nothing to install; money lands in bank/UPI/PIX with SMS-level tracking |

## 3. Corridors (launch set)

Sources: USD, EUR, GBP, CAD, AUD.
Destinations and rails:

| Destination | Rail | Format validated |
|---|---|---|
| INR | UPI / IMPS | `name@bank` handle or account+IFSC |
| BRL | PIX | chave PIX (email/phone/CPF/UUID) |
| EUR | SEPA | IBAN |
| GBP | FPS | sort code + account |
| MXN | SPEI | 18-digit CLABE |

Each corridor record carries its in-ramp provider, out-ramp provider, fee in basis
points, and enabled flag. New corridors ship as data, not code.

## 4. Fee model

Headline 0.95% on $1,000 ($9.50), decomposed as ingest + clearing + network gas.
Quote endpoint returns the full breakdown before confirmation. Receiver gets the
quoted destination amount exactly; slippage beyond tolerance aborts to refund.

## 5. Core flows

### F1. P2P send (sender)

1. Select source and destination currency, amount, recipient (address book or new).
2. Pre-validate recipient rail details before any money moves.
3. Confirm the locked quote (rate, fee, ETA, exact receive amount).
4. Complete fiat-to-USDC on-ramp in the embedded provider widget.
5. Track 4 stages: on-ramp → escrow locked → off-ramp → completed, with Solscan link.
6. Share receipt.

### F2. Contractor payout (SME)

1. Import or pick contractors with validated rails.
2. Batch quote across corridors, single funding checkout.
3. Per-payee escrow and per-payee tracking; one CSV reconciliation export.

### F3. Merchant checkout

1. Merchant creates invoice (fiat amount + currency).
2. Payer completes on-ramp; escrow locks on confirmation.
3. The local mock off-ramp advances payout status after a short simulated delay.

### F4. Receive (no app required)

1. Receiver gets link/SMS with tracking view.
2. Backend validates rail once; payout lands; status flips to completed.

## 6. Wallet abstraction (keyless)

Sign in with Google. The app derives a deterministic Ed25519 keypair bound to the
Google subject, held in the Android Keystore (hardware-backed where available).
No seed phrases, no browser extensions. The public key is the on-chain identity;
the private key never leaves the device and is never sent to the backend.

## 7. Non-functional requirements

- Quote-to-completion p50 under 90 seconds on healthy ramps.
- Every state transition is idempotent and recoverable after backend restart.
- Timed-out escrows are refundable without operator intervention.
- Demo mode must never touch mainnet funds (devnet/test-validator only).
