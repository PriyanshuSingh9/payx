package com.payx.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json

class SessionStore(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Volatile
    var token: String? = prefs.getString(KEY_TOKEN, null)
        private set

    @Volatile
    var user: SessionUser? = prefs.getString(KEY_USER, null)?.let { decodeUser(it) }
        private set

    val isSignedIn: Boolean
        get() = !token.isNullOrBlank() && user != null

    @Synchronized
    fun save(token: String, user: SessionUser) {
        this.token = token
        this.user = user
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, json.encodeToString(SessionUser.serializer(), user))
            .apply()
    }

    @Synchronized
    fun clear() {
        token = null
        user = null
        prefs.edit().clear().apply()
    }

    private fun decodeUser(raw: String): SessionUser? =
        runCatching { json.decodeFromString(SessionUser.serializer(), raw) }.getOrNull()

    companion object {
        private const val PREFS_NAME = "payx_session"
        private const val KEY_TOKEN = "session_token"
        private const val KEY_USER = "session_user"
    }
}
