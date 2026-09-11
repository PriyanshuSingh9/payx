package com.payx.app.data

import android.app.Activity
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.payx.app.BuildConfig
import com.payx.app.wallet.KeystoreWallet
import org.json.JSONObject

class AuthRepository(
    private val api: ApiClient,
    private val sessionStore: SessionStore,
    private val wallet: KeystoreWallet
) {
    suspend fun signInWithGoogle(activity: Activity, country: String): SessionUser {
        val idToken = requestGoogleIdToken(activity)
        val subject = googleSubject(idToken)
            ?: throw AuthException("Google ID token was missing a subject.")
        val walletAddress = wallet.addressFor(subject)
        val response = api.post<AuthGoogleResponse, AuthGoogleRequest>(
            "/auth/google",
            AuthGoogleRequest(
                idToken = idToken,
                walletAddress = walletAddress,
                country = country
            )
        )
        sessionStore.save(response.token, response.user)
        return response.user
    }

    fun signOut() {
        sessionStore.clear()
    }

    private suspend fun requestGoogleIdToken(activity: Activity): String {
        val serverClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
        if (serverClientId.isBlank()) {
            throw AuthException("GOOGLE_SERVER_CLIENT_ID is not configured.")
        }

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(serverClientId).build()
            )
            .build()

        val response = try {
            CredentialManager.create(activity).getCredential(activity, request)
        } catch (error: GetCredentialCancellationException) {
            throw AuthException("Google sign-in was cancelled.", error)
        } catch (error: NoCredentialException) {
            throw AuthException(
                "No Google account on this device. Add one in system settings, then try again.",
                error
            )
        } catch (error: GetCredentialException) {
            throw AuthException(developerHint(error), error)
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = google.idToken
            if (idToken.isBlank()) {
                throw AuthException(
                    "Google sign-in completed, but no ID token was returned. Check the Web client ID."
                )
            }
            return idToken
        }
        throw AuthException("Google sign-in returned an unexpected credential type.")
    }

    private fun googleSubject(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size < 2) return null
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                Charsets.UTF_8
            )
            val sub = JSONObject(payload).optString("sub")
            sub.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun developerHint(error: GetCredentialException): String {
        val raw = error.message.orEmpty()
        val misconfigured = raw.contains("DEVELOPER_ERROR", ignoreCase = true) ||
            raw.contains("[10]") ||
            raw.contains("10:") ||
            raw.contains("matching credential", ignoreCase = true)
        return if (misconfigured) {
            "Google rejected PayX's OAuth client. In the PayX Google Cloud project, add an Android " +
                "OAuth client for package com.payx.app with this machine's debug SHA-1."
        } else {
            raw.ifBlank { "Google sign-in failed." }
        }
    }
}

class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
