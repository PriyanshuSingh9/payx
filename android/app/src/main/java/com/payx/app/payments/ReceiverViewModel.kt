package com.payx.app.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.payx.app.data.ApiClient
import com.payx.app.data.PaymentRepository
import com.payx.app.data.ReceiverDashboardDto
import com.payx.app.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReceiverUiState(
    val dashboard: ReceiverDashboardDto = ReceiverDashboardDto(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
)

class ReceiverViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)
    private val payments = PaymentRepository(ApiClient { sessionStore.token })

    private val _state = MutableStateFlow(ReceiverUiState())
    val state: StateFlow<ReceiverUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh(query: String = _state.value.searchQuery) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.dashboard.payments.isEmpty(), error = null) }
            try {
                val data = payments.getReceiverDashboard(query)
                _state.update {
                    it.copy(
                        dashboard = data,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Could not load received payments."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        refresh(query)
    }
}
