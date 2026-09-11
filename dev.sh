#!/bin/bash

# ==========================================
# PayX Local Development Environment
# ==========================================

cleanup() {
    echo ""
    echo "Stopping local services..."
    kill $(jobs -p) 2>/dev/null

    fuser -k 8787/tcp 2>/dev/null  # Backend API
    fuser -k 8899/tcp 2>/dev/null  # Solana test validator

    echo "Cleaned up successfully."
    exit
}

trap cleanup SIGINT SIGTERM

echo "Starting PayX Local Environment..."
echo ""

# 1. Start Solana test validator
echo "Starting Solana test validator..."
if command -v solana-test-validator &> /dev/null; then
    solana-test-validator --reset --rpc-port 8899 > /dev/null 2>&1 &
    echo "   Validator running on http://localhost:8899"
else
    echo "   solana-test-validator not found. Install the Solana CLI to enable on-chain flows."
    echo "   Backend will still start; blockchain calls will fail until the validator is up."
fi

# 2. Start Backend API
echo "Starting Backend API..."
if [ -f "backend/package.json" ]; then
    pnpm --dir backend dev > /dev/null 2>&1 &
    echo "   Backend starting on http://localhost:8787"
else
    echo "   pnpm workspace not found. Run this script from the repo root."
    cleanup
fi

# 3. Setup ADB Reverse Tunneling
echo "Setting up ADB reverse tunneling for Android devices..."
if command -v adb &> /dev/null; then
    adb reverse tcp:8787 tcp:8787 2>/dev/null
    adb reverse tcp:8899 tcp:8899 2>/dev/null
    echo "   Port 8787 (Backend) and 8899 (Validator) forwarded to Android device"
else
    echo "   adb not found in PATH, skipping reverse tunneling"
fi

echo ""
echo "All background services launched."
echo "   - Backend API:  http://localhost:8787"
echo "   - Validator:    http://localhost:8899"
echo ""
echo "Open android/ in Android Studio and run on an emulator or device."
echo "Press Ctrl+C to shut down everything."
echo ""

wait
