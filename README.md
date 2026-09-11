# PayX — Universal Cross-Currency Payments on Solana

Pay anyone, anywhere, in any supported currency. Fast. Fair. Borderless.

PayX bridges local fiat through SPL USDC on Solana, clears through a trustless
Anchor escrow, and pays out on destination rails (UPI, PIX, SEPA, FPS, SPEI).

## Stack

| Layer | Technology |
|---|---|
| Mobile app | Native Android (Kotlin, Jetpack Compose, MVVM) |
| Chain | Solana (local validator / devnet / mainnet) |
| Program | Rust + Anchor (`programs/payx_escrow`) |
| Backend | Node + Express + TypeScript, pnpm (`backend`) |
| Domain | Dependency-free modules under `backend/src/lib` |
| Database | Neon Postgres + Prisma |

Reference implementation: `hackcraft/remitflow` (route surface, auth scheme, and
orchestration ported from its Express/EVM prototype to Solana).

## Quick start

Prerequisites: Node 20+, pnpm 10, Solana CLI + Anchor (Phase 2), Android Studio
(Phase 3), a Neon Postgres database.

```bash
pnpm setup
cp backend/.env.example backend/.env   # fill in values
pnpm dev                                          # backend on :8787
./dev.sh                                          # validator + backend + adb
```

## Deployed Smart Contracts

### Solana Devnet

| Component | Value |
|---|---|
| Program ID (`payx_escrow`) | [`CTFbnKuiHpg5PR5vHpBXJzvLyCGrhMuQp4PbK8ZRGboa`](https://explorer.solana.com/address/CTFbnKuiHpg5PR5vHpBXJzvLyCGrhMuQp4PbK8ZRGboa?cluster=devnet) |
| Anchor IDL Account | `86DwBCSfGbVxYS5wbNJNjD21QMnZaP9WMJebK9zt1CFW` |
| Deployment Transaction | [`2FgbskvnU3bfc18F2o4iviyQofvHPywQc7eyiJ1UvqvgmD4wtKsAcLD4YfykPxDZEyCDCjhPDSFmsqisRYh3jUX6`](https://explorer.solana.com/tx/2FgbskvnU3bfc18F2o4iviyQofvHPywQc7eyiJ1UvqvgmD4wtKsAcLD4YfykPxDZEyCDCjhPDSFmsqisRYh3jUX6?cluster=devnet) |
| Circle Devnet USDC Mint | [`4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU`](https://explorer.solana.com/address/4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU?cluster=devnet) |

## Docs

`docs/prd.md`, `docs/architecture.md`, `docs/contracts.md`, `docs/decisions.md`,
`docs/milestones.md`, `docs/setup.md` (teammate/agent onboarding). Keep `contracts.md` and `milestones.md` in sync with every
change.
