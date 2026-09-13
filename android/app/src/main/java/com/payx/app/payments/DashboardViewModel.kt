package com.payx.app.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.payx.app.data.ApiClient
import com.payx.app.data.PaymentDto
import com.payx.app.data.PaymentRepository
import com.payx.app.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val payments: List<PaymentDto> = emptyList(),
    val liveRate: Double? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val totalTransferredUsd: Double
        get() = payments
            .filter { it.status != "PAYMENT_FAILED" }
            .sumOf { it.sourceAmount }

    val savedUsd: Double
        get() = payments.sumOf { payment ->
            val wireFee = payment.sourceAmount * 0.03
            val network = payment.fees?.networkFee ?: 0.01
            (wireFee - network).coerceAtLeast(0.0)
        }

    val totalTransferredInr: Double
        get() = payments
            .filter { it.status != "PAYMENT_FAILED" }
            .sumOf { payment ->
                payment.destinationAmount ?: (payment.sourceAmount * (payment.exchangeRate ?: liveRate ?: 92.90))
            }

    val savedInr: Double
        get() = payments.sumOf { payment ->
            val rate = payment.exchangeRate ?: liveRate ?: 92.90
            val wireFee = payment.sourceAmount * 0.03
            val network = payment.fees?.networkFee ?: 0.01
            ((wireFee - network).coerceAtLeast(0.0)) * rate
        }

    val recentRecipients: List<PaymentDto>
        get() = payments.distinctBy { it.recipient.name }.take(3)
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)
    private val payments = PaymentRepository(ApiClient { sessionStore.token })

    private val _state = MutableStateFlow(run {
        PaymentRepository.initCache(application)
        DashboardUiState(
            payments = PaymentRepository.getCachedPayments() ?: emptyList(),
            liveRate = PaymentRepository.getCachedLiveRate(),
            isLoading = PaymentRepository.getCachedPayments() == null
        )
    })
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_state.value.payments.isEmpty()) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val list = payments.listPayments(20, forceRefresh = true)
                val rate = runCatching { payments.previewQuote(1.0, forceRefresh = true).exchangeRate }.getOrNull()
                _state.update {
                    it.copy(
                        payments = list,
                        liveRate = rate ?: it.liveRate,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = if (it.payments.isEmpty()) (error.message ?: "Could not load payments.") else null
                    )
                }
            }
        }
    }
}
