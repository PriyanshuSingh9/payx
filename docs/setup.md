# PayX — Agent Setup Guide

Onboarding runbook for a teammate (or their agent) to reach a working local
environment. Follow top to bottom; each step has an expected result.

## 1. Prerequisites

| Tool | Required | Notes |
|---|---|---|
| Node.js | 20+ | via nvm; repo verified on 24 |
| pnpm | 10+ | `corepack enable` or standalone install; never npm/yarn/bun |
| Rust | stable via rustup | `cargo --version`; needs `rustfmt` (`rustup component add rustfmt`) |
| Solana CLI | 2.x | provides `solana-test-validator` (`:8899` in dev.sh) |
| Anchor | 0.31 via `avm` | must match `anchor-lang 0.31` in `programs/payx_escrow/Cargo.toml` |
| Android Studio | Narwhal (2025.1) or newer | see section 5 |
| Neon account | free tier works | pooled `DATABASE_URL` + direct `DIRECT_URL` |
| Google OAuth client | Web client ID | used to verify ID tokens at `/auth/google` |

No sudo is required if you install to user-space (`~/development`, `~/Android` on Linux,
or `~/Library/Android/sdk` on macOS).
On Linux, KVM (`/dev/kvm`) is required for emulator acceleration. On macOS Apple Silicon,
hardware virtualization is provided natively by the Apple Hypervisor framework.

## 2. Clone and install

```bash
git clone <payx-remote> payx && cd payx
pnpm setup          # installs backend/node_modules + backend/pnpm-lock.yaml
```

Expected: `pnpm setup` completes with only peer-dependency warnings.

## 3. Backend

```bash
cp backend/.env.example backend/.env
# edit backend/.env: DATABASE_URL, DIRECT_URL, GOOGLE_CLIENT_ID, JWT_SECRET
pnpm db:generate    # needs DIRECT_URL set (generate does not connect)
pnpm db:migrate     # or `pnpm --dir backend db:push` for a fresh Neon branch
pnpm --dir backend db:seed
pnpm dev            # serves http://localhost:8787
```

Verify:

```bash
curl localhost:8787/health
# {"ok":true,"service":"payx-backend"}
curl localhost:8787/corridors
# {"error":"Missing Authorization bearer token."}  <- auth guard working
```

`pnpm typecheck` must pass before committing. Transfer/webhook routes return
501 until Phase 1 lands; that is expected, not a bug.

## 4. Anchor program

```bash
cargo check -p payx_escrow   # must be error-free
cargo fmt --check -p payx_escrow
solana-test-validator --reset --rpc-port 8899 &
anchor test                  # local validator integration tests (Phase 2)
anchor keys sync             # replaces the 1111... placeholder program ID
```

After deploy, put the real program ID in `backend/.env` as `PAYX_PROGRAM_ID`
and in `docs/contracts.md`. USDC mint for local testing is created per
validator session; devnet uses the Circle devnet mint.

## 5. Android Studio note

Install Android Studio Narwhal or newer, then in SDK Manager install:

- SDK Platform Android 36, Build-Tools 36, Platform-Tools, Emulator
- A `google_apis | arm64-v8a` system image for API 36 on Apple Silicon Macs
  (or `google_apis | x86_64` on Intel/Linux)

JDK 17 is required by Gradle/AGP 8.x; Studio's bundled JBR works if
`JAVA_HOME` is unset. Open the **`android/` directory** (not the repo root)
as the project so Gradle syncs the `PayX` settings file. First sync downloads
the Gradle 8.14 distribution and Compose dependencies.

Run config: `app` module on a Pixel emulator (API 36). The app discovers the
backend at `10.0.2.2:8787` on emulator automatically. For a physical device:

```bash
adb reverse tcp:8787 tcp:8787
```

(`dev.sh` does this for you; see section 6.)

## 6. `dev.sh` (one-command local env)

```bash
./dev.sh
```

Starts: validator `:8899` (skipped with a notice if the Solana CLI is absent),
backend `:8787`, adb reverse tunnels, then blocks until Ctrl+C (which frees
both ports). It does not launch the app; run that from Android Studio.
Run it from the repo root.

## 7. Ports

| Port | Service |
|---|---|
| 8787 | Backend API |
| 8899 | Solana test validator |

## 8. Troubleshooting

- `EADDRINUSE :8787/:8899` — a previous `dev.sh` died; re-run `dev.sh` (whose
  trap cleans up on exit), or run `lsof -ti:8787 -ti:8899 | xargs kill -9` on macOS
  (`fuser -k 8787/tcp` on Linux).
- `PrismaConfigEnvError: Cannot resolve DIRECT_URL` — export a dummy URL for
  generate-only runs, or fill `backend/.env` properly.
- `anchor test` version errors — Anchor CLI minor must equal `anchor-lang`
  minor (0.31.x); use `avm` to switch.
- Gradle `Unsupported class file version` — Gradle is running on the wrong
  JDK; point Studio/Gradle at JDK 17.
- Emulator painfully slow — On Linux, `/dev/kvm` missing (enable virtualization in BIOS).
  On macOS Apple Silicon, ensure you use the `arm64-v8a` system image.
