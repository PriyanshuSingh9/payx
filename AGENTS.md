# Project Agent Directives — PayX

## Architecture Invariants
- pnpm single package: `backend` (Express orchestration + `src/lib`
  pure domain: corridors, quotes, rail validation — no I/O), `programs/payx_escrow`
  (Anchor custody), `android` (Compose presentation + keys).
- Backend is the only writer of fiat-side state; the program is the only
  custodian of in-flight USDC. DB rows mirror on-chain escrow state.
- Money math lives in `backend/src/lib` exactly once; backend and tests import it.
- Mobile never holds ramp secrets; private keys never leave the Android Keystore.

## Environment & Tooling
- Package Manager: `pnpm` exclusively (never npm/yarn/bun).
- Node 20+ via nvm; TypeScript strict; Express 5.
- Rust via rustup/cargo; Anchor program edition 2021; `cargo fmt` clean.
- Kotlin 2.x, AGP 8.x, compileSdk 36; MVVM + StateFlow, no logic in composables.
- Testing: Execute tests before committing changes.
- Sync Docs: Keep `docs/milestones.md` and `docs/contracts.md` synchronized in every commit.

## Code Quality & Style
- Zero emojis across all code, comments, docs, and commits.
- Strict typing with explicit boundaries (no `any` across module edges).
- Conventional commits with technical rationale.
- Secrets only via `.env` (gitignored); commit `.env.example` updates instead.
