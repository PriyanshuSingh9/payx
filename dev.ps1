# ──────────────────────────────────────────────────────────────────
# PayX Local Development Environment (Windows)
# ──────────────────────────────────────────────────────────────────
# PowerShell equivalent of dev.sh. Run from the repo root:
#   .\dev.ps1

$ErrorActionPreference = "Stop"

# Ensure adb is findable even if PATH was not refreshed
$sdkPlatformTools = "$env:LOCALAPPDATA\Android\Sdk\platform-tools"
if ($env:Path -notlike "*$sdkPlatformTools*") {
    $env:Path = "$sdkPlatformTools;$env:Path"
}

Write-Host ""
Write-Host "Starting PayX Local Environment..."
Write-Host ""

# 1. Start Solana test validator
Write-Host "Starting Solana test validator..."
$validatorProc = $null
if (Get-Command solana-test-validator -ErrorAction SilentlyContinue) {
    $validatorProc = Start-Process -FilePath "solana-test-validator" `
        -ArgumentList "--reset","--rpc-port","8899" `
        -WindowStyle Hidden -PassThru
    Write-Host "   Validator running on http://localhost:8899 (PID $($validatorProc.Id))"
} else {
    Write-Host "   solana-test-validator not found. Install the Solana CLI to enable on-chain flows."
    Write-Host "   Backend will still start; blockchain calls will fail until the validator is up."
}

# 2. Start Backend API
Write-Host "Starting Backend API..."
if (Test-Path "backend\package.json") {
    $backendProc = Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c","pnpm","--dir","backend","dev" `
        -WindowStyle Hidden -PassThru
    Write-Host "   Backend starting on http://localhost:8787 (PID $($backendProc.Id))"
} else {
    Write-Host "   backend/package.json not found. Run this script from the repo root."
    exit 1
}

# Give the backend a moment to start
Start-Sleep -Seconds 2

# 3. Setup ADB Reverse Tunneling
Write-Host "Setting up ADB reverse tunneling for Android devices..."
if (Get-Command adb -ErrorAction SilentlyContinue) {
    adb reverse tcp:8787 tcp:8787 2>$null
    adb reverse tcp:8899 tcp:8899 2>$null
    Write-Host "   Port 8787 (Backend) and 8899 (Validator) forwarded to Android device"
} else {
    Write-Host "   adb not found in PATH, skipping reverse tunneling"
}

Write-Host ""
Write-Host "All services launched."
Write-Host "   - Backend API:  http://localhost:8787"
Write-Host "   - Validator:    http://localhost:8899"
Write-Host ""
Write-Host "Open android/ in Android Studio and run on an emulator or device."
Write-Host "Press Ctrl+C to shut down everything."
Write-Host ""

try {
    # Block until Ctrl+C
    while ($true) {
        Start-Sleep -Seconds 2
        # Check if backend is still alive
        if ($backendProc.HasExited) {
            Write-Host "Backend process exited unexpectedly."
            break
        }
    }
} finally {
    Write-Host ""
    Write-Host "Stopping local services..."
    if ($backendProc -and -not $backendProc.HasExited) {
        Stop-Process -Id $backendProc.Id -Force -ErrorAction SilentlyContinue
    }
    if ($validatorProc -and -not $validatorProc.HasExited) {
        Stop-Process -Id $validatorProc.Id -Force -ErrorAction SilentlyContinue
    }
    # Kill anything still on the ports
    foreach ($port in @(8787, 8899)) {
        $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        foreach ($c in $conns) {
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    }
    Write-Host "Cleaned up successfully."
}
