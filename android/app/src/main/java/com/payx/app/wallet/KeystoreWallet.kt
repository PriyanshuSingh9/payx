package com.payx.app.wallet

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Deterministic Ed25519 wallet bound to the Google subject.
 *
 * Seed = HMAC-SHA256("payx-wallet-v1", googleSubject). The seed is wrapped with
 * an AES key in Android Keystore and stored encrypted on device. Only the
 * Base58 Solana address ever leaves this class.
 */
class KeystoreWallet(context: Context) {
    private val wrapStore = EncryptedSharedPreferences.create(
        context,
        WRAP_PREFS,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun addressFor(subject: String): String {
        val seed = loadOrCreateSeed(subject)
        return try {
            val publicKey = Ed25519PrivateKeyParameters(seed).generatePublicKey().encoded
            Base58.encode(publicKey)
        } finally {
            seed.fill(0)
        }
    }

    fun hasWallet(subject: String): Boolean =
        wrapStore.contains(prefKey(subject)) && keyStore.containsAlias(wrapAlias(subject))

    private fun loadOrCreateSeed(subject: String): ByteArray {
        val stored = wrapStore.getString(prefKey(subject), null)
        if (stored != null && keyStore.containsAlias(wrapAlias(subject))) {
            return unwrapSeed(subject, stored)
        }
        val seed = deriveSeed(subject)
        wrapStore.edit().putString(prefKey(subject), wrapSeed(subject, seed.copyOf())).apply()
        return seed
    }

    private fun deriveSeed(subject: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(HMAC_SALT, "HmacSHA256"))
        return mac.doFinal(subject.toByteArray(Charsets.UTF_8))
    }

    private fun wrapSeed(subject: String, seed: ByteArray): String {
        val secret = wrappingKey(subject, createIfMissing = true)
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, secret)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(seed)
        seed.fill(0)
        return Base64.encodeToString(iv, Base64.NO_WRAP) +
            ":" +
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private fun unwrapSeed(subject: String, packed: String): ByteArray {
        val parts = packed.split(":")
        require(parts.size == 2) { "Corrupt wallet wrap for $subject" }
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey(subject, createIfMissing = false), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun wrappingKey(subject: String, createIfMissing: Boolean): SecretKey {
        val alias = wrapAlias(subject)
        if (!keyStore.containsAlias(alias)) {
            require(createIfMissing) { "Missing Keystore wrapping key for $subject" }
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGenerator.generateKey()
        }
        return keyStore.getKey(alias, null) as SecretKey
    }

    private val keyStore: KeyStore
        get() = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun wrapAlias(subject: String) = ALIAS_PREFIX + subject
    private fun prefKey(subject: String) = "seed_$subject"

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS_PREFIX = "payx_wallet_"
        private const val WRAP_PREFS = "payx_wallet_wrap"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private val HMAC_SALT = "payx-wallet-v1".toByteArray(Charsets.UTF_8)
    }
}

internal object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) zeros++

        val input58 = input.copyOf()
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < input58.size) {
            encoded[--outputStart] = ALPHABET[divmod(input58, inputStart, 256, 58)]
            while (inputStart < input58.size && input58[inputStart].toInt() == 0) {
                inputStart++
            }
        }
        while (outputStart < encoded.size && encoded[outputStart] == ALPHABET[0]) {
            outputStart++
        }
        repeat(zeros) { encoded[--outputStart] = ALPHABET[0] }
        return String(encoded, outputStart, encoded.size - outputStart)
    }

    private fun divmod(number: ByteArray, firstDigit: Int, base: Int, divisor: Int): Int {
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toInt() and 0xFF
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder
    }
}
