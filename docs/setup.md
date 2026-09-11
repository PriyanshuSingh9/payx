# PayX — Agent & Developer Setup Guide

Onboarding runbook for a teammate (or their agent) to reach a working local
development environment across Linux, macOS, and Windows. Follow top to bottom; each step has an expected result.

## 1. Prerequisites

| Tool | Required | Notes |
|---|---|---|
| Node.js | 20+ | via nvm (Linux/macOS) or nvm-windows; repo verified on Node 24 |
| pnpm | 10+ | `corepack enable` or standalone install; run `pnpm run setup` (never npm/yarn/bun) |
| Rust | stable via rustup | `cargo --version`; requires `rustfmt` (`rustup component add rustfmt`) |
| Solana CLI | 2.x | Agave v2.1.x; provides `solana-test-validator` (`:8899` in dev scripts) |
| Anchor | 0.31 via `avm` or binary | must match `anchor-lang 0.31` in `programs/payx_escrow/Cargo.toml` |
| Java JDK | 17 (Eclipse Temurin) | required by Gradle 8.14 and AGP 8.x; point `JAVA_HOME` to this JDK |
| Android Studio | Narwhal (2025.1) or newer | provides SDK Manager and emulator (see section 5) |
| Neon account | free tier works | pooled `DATABASE_URL` + direct `DIRECT_URL` |
| Google OAuth client | Web client ID **and** Android client | Web ID verifies tokens at `/auth/google`; Android client is package `com.payx.app` + SHA-1 of `android/keystore/debug.keystore` |

### OS Space and Virtualization Notes

- **Linux**: Install tools into user-space (`~/development`, `~/Android/Sdk`, `~/.jdks`). Emulator acceleration requires KVM (`/dev/kvm`). Verify read/write permissions (`test -r /dev/kvm && test -w /dev/kvm`) and add your user to the `kvm` group if necessary.
- **macOS**: Install tools into user-space (`~/Library/Android/sdk`, `~/.jdks`). Hardware virtualization is provided natively by the Apple Hypervisor framework.
- **Windows**: Install tools into user-space (`%LOCALAPPDATA%\Android\Sdk`, `%USERPROFILE%\.jdks`). Hardware virtualization requires Windows Hypervisor Platform (WHPX) or Hyper-V enabled in Windows Features. PowerShell 7+ or Windows Terminal is recommended. For Solana/Anchor on-chain compilation, native Windows binaries or WSL2 (Ubuntu) are supported.

## 2. Clone and install

Notice: `pnpm setup` is a reserved internal command for the pnpm CLI (which configures global paths). To invoke the project setup script defined in `package.json`, use `pnpm run setup`.

### Linux / macOS

```bash
git clone <payx-remote> payx && cd payx
pnpm run setup          # installs backend/node_modules + backend/pnpm-lock.yaml
```

### Windows (PowerShell)

```powershell
git clone <payx-remote> payx; cd payx
pnpm run setup          # installs backend/node_modules + backend/pnpm-lock.yaml
```

Expected: `pnpm run setup` completes with only peer-dependency warnings.

## 3. Backend

### Linux / macOS

```bash
cp backend/.env.example backend/.env
# edit backend/.env: DATABASE_URL, DIRECT_URL, GOOGLE_CLIENT_ID, JWT_SECRET
pnpm db:generate             # generates Prisma client
pnpm --dir backend db:push   # syncs schema to Neon database (or `pnpm db:migrate`)
pnpm --dir backend db:seed   # seeds mock corridors and rates
pnpm dev                     # serves http://localhost:8787
```

### Windows (PowerShell)

```powershell
Copy-Item backend\.env.example backend\.env
# edit backend\.env: DATABASE_URL, DIRECT_URL, GOOGLE_CLIENT_ID, JWT_SECRET
pnpm db:generate             # generates Prisma client
pnpm --dir backend db:push   # syncs schema to Neon database (or `pnpm db:migrate`)
pnpm --dir backend db:seed   # seeds mock corridors and rates
pnpm dev                     # serves http://localhost:8787
```

### Verification

```bash
curl localhost:8787/health
# {"ok":true,"service":"payx-backend"}
curl localhost:8787/corridors
# {"error":"Missing Authorization bearer token."}  <- auth guard working
```

`pnpm typecheck` must pass before committing. Transfer/webhook routes return 501 until Phase 1 lands; that is expected, not a bug.

## 4. Anchor program

### Installation by OS

#### Linux

```bash
# Solana CLI 2.x
sh -c "$(curl -sSfL https://release.anza.xyz/v2.1.18/install)"

# Anchor CLI 0.31.0 pre-built binary
curl -sSfL https://github.com/coral-xyz/anchor/releases/download/v0.31.0/anchor-0.31.0-x86_64-unknown-linux-gnu -o ~/.cargo/bin/anchor
chmod +x ~/.cargo/bin/anchor

# Or install via avm
cargo install --git https://github.com/coral-xyz/anchor avm --locked --force
avm install 0.31.0
avm use 0.31.0
```

#### macOS

```bash
# Solana CLI 2.x
sh -c "$(curl -sSfL https://release.anza.xyz/v2.1.18/install)"

# Anchor CLI 0.31.0 pre-built binary (Apple Silicon)
curl -sSfL https://github.com/coral-xyz/anchor/releases/download/v0.31.0/anchor-0.31.0-aarch64-apple-darwin -o ~/.cargo/bin/anchor
chmod +x ~/.cargo/bin/anchor

# (On Intel macOS, use anchor-0.31.0-x86_64-apple-darwin instead)
```

#### Windows

Download the Solana / Agave CLI release archive from `https://github.com/anza-xyz/agave/releases` and extract the binary folder to a location on your `PATH`.
Download `anchor-0.31.0-x86_64-pc-windows-msvc.exe` from `https://github.com/coral-xyz/anchor/releases/tag/v0.31.0`, rename it to `anchor.exe`, and place it in `%USERPROFILE%\.cargo\bin`.
Alternatively, run Solana and Anchor builds inside WSL2 Ubuntu.

### Initialize Local Solana Configuration

```bash
solana-keygen new --no-bip39-passphrase --outfile ~/.config/solana/id.json
solana config set --url localhost
solana config set --keypair ~/.config/solana/id.json
```

*(On Windows PowerShell, use `$env:USERPROFILE\.config\solana\id.json`)*

### Verification and Program Checks

```bash
cargo check -p payx_escrow   # must be error-free
cargo fmt --check -p payx_escrow
solana-test-validator --reset --rpc-port 8899 &
anchor test                  # local validator integration tests (Phase 2)
anchor keys sync             # replaces the placeholder program ID
```

After deploy, put the real program ID in `backend/.env` as `PAYX_PROGRAM_ID` and in `docs/contracts.md`. USDC mint for local testing is created per validator session; devnet uses the Circle devnet mint.
Local validator runs generate a `test-ledger/` folder at repository root; this is gitignored.

## 5. Android Studio & SDK

Install Android Studio Narwhal or newer, or install standalone Android command-line tools.

### JDK 17 Configuration

Gradle 8.14 and AGP 8.x require JDK 17. Use Eclipse Temurin 17:
- **Linux**: Extract to `~/.jdks/temurin-17`
- **macOS**: Extract to `~/.jdks/temurin-17` or install via pkg
- **Windows**: Extract to `%USERPROFILE%\.jdks\temurin-17` or use MSI

Configure environment variables:
- **Linux** (`~/.bashrc` or `~/.zshrc`):
  ```bash
  export JAVA_HOME="$HOME/.jdks/temurin-17"
  export ANDROID_HOME="$HOME/Android/Sdk"
  export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
  export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
  ```
- **macOS** (`~/.zshrc`):
  ```bash
  export JAVA_HOME="$HOME/.jdks/temurin-17"
  export ANDROID_HOME="$HOME/Library/Android/sdk"
  export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
  export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
  ```
- **Windows** (PowerShell):
  ```powershell
  [Environment]::SetEnvironmentVariable("JAVA_HOME", "$env:USERPROFILE\.jdks\temurin-17", "User")
  [Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
  [Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", "$env:LOCALAPPDATA\Android\Sdk", "User")
  ```

### Required SDK Components

Install the following packages via Android Studio SDK Manager or `sdkmanager`:
- `platforms;android-36` (Android SDK Platform 36)
- `build-tools;36.0.0` (Android SDK Build-Tools 36)
- `platform-tools` (provides `adb`)
- `emulator` (Android Emulator)
- System images for emulator:
  - Linux / Windows / Intel macOS: `system-images;android-36;google_apis;x86_64`
  - Apple Silicon macOS: `system-images;android-36;google_apis;arm64-v8a`

Command-line installation:
```bash
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0" "emulator"
```

### Opening the Project

Open the **`android/` directory** (not the repo root) as the project in Android Studio so Gradle syncs the `PayX` settings file. The initial sync downloads Gradle 8.14 and Compose dependencies.

Run config: `app` module on a Pixel emulator (API 36). The app discovers the backend at `10.0.2.2:8787` on emulator automatically. For a physical device connected via USB:

```bash
adb reverse tcp:8787 tcp:8787
adb reverse tcp:8899 tcp:8899
```

(`dev.sh` and `dev.ps1` execute this automatically; see section 6.)

### Google Sign-In & Auth Configuration

Google Sign-In is PayX-only. Do not reuse another app's OAuth clients.

Put PayX's Google client ID in `backend/.env` as `GOOGLE_CLIENT_ID`. The
Android app reads that same value at build time (or
`GOOGLE_SERVER_CLIENT_ID` in `android/local.properties`). Do not put a
client secret in the app or in env; this flow only uses the client ID.

In the **PayX** Google Cloud project, create:

- **Web application** client — this ID goes in `GOOGLE_CLIENT_ID`
- **Android** client — package `com.payx.app`, SHA-1 of the **shared** debug
  keystore at `android/keystore/debug.keystore` (not this machine's
  `~/.android/debug.keystore`):

```bash
keytool -list -v -keystore android/keystore/debug.keystore \
  -alias androiddebugkey -storepass android
```

Gradle debug builds already sign with that file. After switching to it, uninstall
any existing `com.payx.app` install (`adb uninstall com.payx.app`) — Android will
not update an app signed with a different cert.

The emulator/device must have a Google account, Play Services, and a reachable
backend (`./dev.sh` or `pnpm dev`).

## 6. One-Command Local Environment

Run the launcher from the repository root:

### Linux / macOS

```bash
./dev.sh
```

### Windows (PowerShell)

```powershell
.\dev.ps1
```

Both launchers:
1. Start `solana-test-validator` on port `8899` (skipped with a notice if Solana CLI is absent).
2. Start the Express backend API on port `8787`.
3. Set up `adb reverse` tunnels for ports 8787 and 8899 to connected Android devices.
4. Block until `Ctrl+C`, triggering a cleanup trap that stops background services and frees ports.

## 7. Ports

| Port | Service | Notes |
|---|---|---|
| 8787 | Backend API | Express application |
| 8899 | Solana test validator | JSON-RPC endpoint |
| 8900 | Solana test validator | WebSocket pubsub endpoint |

## 8. Troubleshooting

- `EADDRINUSE :8787/:8899` — A previous dev script exited without cleaning up ports.
  - Linux: `fuser -k 8787/tcp && fuser -k 8899/tcp`
  - macOS: `lsof -ti:8787 -ti:8899 | xargs kill -9`
  - Windows:
    ```powershell
    foreach ($p in @(8787, 8899)) {
      $c = Get-NetTCPConnection -LocalPort $p -ErrorAction SilentlyContinue
      if ($c) { Stop-Process -Id $c.OwningProcess -Force }
    }
    ```
- `pnpm setup` CLI conflict — In pnpm CLI, `pnpm setup` initializes the pnpm executable path rather than executing the `setup` script from `package.json`. Always run `pnpm run setup`.
- Gradle download timeout (`SocketTimeoutException: timeout`) — When downloading Gradle 8.14 through the wrapper, default network timeouts can trigger on high-latency connections. Increase `networkTimeout=60000` in `android/gradle/wrapper/gradle-wrapper.properties` or download `gradle-8.14-bin.zip` directly into `~/.gradle/wrapper/dists/gradle-8.14-bin/<hash>/`.
- `PrismaConfigEnvError: Cannot resolve DIRECT_URL` — Ensure `backend/.env` exists and contains valid `DATABASE_URL` and `DIRECT_URL` strings (or export a dummy PostgreSQL connection string for generate-only runs).
- `anchor test` version mismatch — Anchor CLI minor version must equal `anchor-lang` minor version (0.31.x). Verify with `anchor --version` and switch versions using `avm use 0.31.0` if necessary.
- Gradle `Unsupported class file version` — Gradle is executing on an incompatible JDK version. Ensure `JAVA_HOME` points to JDK 17 (e.g. Eclipse Temurin 17) and Studio is set to JDK 17 in `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK`.
- Google Sign-In `DEVELOPER_ERROR` / 10 / reauth failure — the PayX GCP project is missing an Android OAuth client for package `com.payx.app` with the SHA-1 of `android/keystore/debug.keystore`. After adding it, wait a few minutes, then `adb uninstall com.payx.app` and reinstall.
- Emulator slow or failing to launch:
  - Linux: `/dev/kvm` missing or unreadable. Verify hardware virtualization is enabled in BIOS and user has access to `/dev/kvm`.
  - macOS: Ensure you use the `arm64-v8a` system image on Apple Silicon.
  - Windows: Ensure Windows Hypervisor Platform (WHPX) or Hyper-V is enabled in Windows Features.
