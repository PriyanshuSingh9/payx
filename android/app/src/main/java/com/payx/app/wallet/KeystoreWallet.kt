package com.payx.app.wallet

import android.content.Context
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

// Phase 3: deterministic Ed25519 wallet bound to the Google subject, held in
// the Android Keystore (hardware-backed where available). Only the base58
// address ever leaves the device. Signing goes through the Solana Mobile
// Wallet Adapter; this file owns key storage, never key export.
class KeystoreWallet(private val context: Context) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS_PREFIX = "payx_wallet_"
    }

    fun hasWallet(subject: String): Boolean {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return ks.containsAlias(ALIAS_PREFIX + subject)
    }

    fun ensureWallet(subject: String) {
        if (hasWallet(subject)) return
        // Placeholder curve: swap to Ed25519 via Tink/Keystore once the
        // Solana signing path lands in Phase 3.
        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        kpg.generateKeyPair()
    }
}
