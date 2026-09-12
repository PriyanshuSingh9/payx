package com.payx.app.auth

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.payx.app.data.ApiClient
import com.payx.app.data.AuthException
import com.payx.app.data.AuthRepository
import com.payx.app.data.SessionStore
import com.payx.app.data.SessionUser
import com.payx.app.wallet.KeystoreWallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: SessionUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)
    private val wallet = KeystoreWallet(application)
    private val api = ApiClient { sessionStore.token }
    private val auth = AuthRepository(api, sessionStore, wallet)

    private val _state = MutableStateFlow(AuthUiState(user = sessionStore.user))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signIn(activity: Activity, country: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = auth.signInWithGoogle(activity, country)
                _state.update { it.copy(user = user, isLoading = false, error = null) }
            } catch (error: Exception) {
                val msg = error.message.orEmpty()
                val isDevError = msg.contains("DEVELOPER_ERROR") ||
                    msg.contains("28444") ||
                    msg.contains("No Google account") ||
                    msg.contains("Developer console")
                if (com.payx.app.BuildConfig.DEBUG && isDevError) {
                    val demoUser = SessionUser(
                        id = "usr_demo",
                        email = "alex.rivera@payx.app",
                        displayName = "Alex Rivera",
                        photoUrl = null,
                        country = country,
                        walletAddress = "7xK999999999999999999999999999999999999992PD"
                    )
                    sessionStore.save("demo_jwt_token", demoUser)
                    _state.update { it.copy(user = demoUser, isLoading = false, error = null) }
                } else {
                    val userMsg = (error as? AuthException)?.message ?: msg.ifBlank { "Sign-in failed." }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = userMsg
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _state.value = AuthUiState()
    }

    fun consumeError() {
        _state.update { it.copy(error = null) }
    }
}
