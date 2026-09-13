package com.payx.app.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.payx.app.data.ApiClient
import com.payx.app.data.PaymentDto
import com.payx.app.data.PaymentRepository
import com.payx.app.data.SessionStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.isActive

data class TrackerUiState(
    val paymentId: String = "",
    val payment: PaymentDto? = null,
    val isLoading: Boolean = true,
    val isAdvancing: Boolean = false,
    val error: String? = null
)

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)
    private val payments = PaymentRepository(ApiClient { sessionStore.token })

    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state.asStateFlow()

    private var pollJob: Job? = null
    private var driveJob: Job? = null
    private var startedFor: String? = null

    fun start(paymentId: String) {
        if (paymentId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "Missing payment id.") }
            return
        }
        if (startedFor == paymentId) return
        startedFor = paymentId
        pollJob?.cancel()
        driveJob?.cancel()
        val cached = PaymentRepository.getCachedPayments()?.find { it.id == paymentId }
        _state.update {
            TrackerUiState(
                paymentId = paymentId,
                payment = cached,
                isLoading = cached == null,
                isAdvancing = false,
                error = null
            )
        }
        if (cached?.isTerminal == true) {
            return
        }
        pollJob = viewModelScope.launch { poll(paymentId) }
        driveJob = viewModelScope.launch { drivePipeline(paymentId) }
    }

    private suspend fun poll(paymentId: String) {
        while (coroutineContext.isActive) {
            try {
                val payment = payments.getPayment(paymentId)
                _state.update {
                    it.copy(payment = payment, isLoading = false, error = null)
                }
                if (payment.isTerminal) return
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Could not load payment."
                    )
                }
                return
            }
            delay(800)
        }
    }

    private suspend fun drivePipeline(paymentId: String) {
        try {
            var payment = payments.getPayment(paymentId)
            _state.update { it.copy(payment = payment, isLoading = false) }
            if (payment.isTerminal) return

            _state.update { it.copy(isAdvancing = true) }

            if (payment.status == "QUOTE_EXPIRED") {
                payment = payments.refreshQuote(paymentId)
                _state.update { it.copy(payment = payment) }
            }

            val steps = listOf(
                "confirm",
                "settle",
                "create_offramp",
                "payout_processing",
                "payout_success"
            )
            for (step in steps) {
                payment = payments.getPayment(paymentId)
                if (payment.isTerminal) break
                if (!shouldRun(step, payment.status)) continue
                if (step == "confirm" && payment.status == "QUOTE_EXPIRED") {
                    payments.refreshQuote(paymentId)
                }
                payment = payments.simulateStep(paymentId, step)
                _state.update { it.copy(payment = payment) }
            }
        } catch (error: Exception) {
            _state.update { it.copy(error = error.message ?: "Pipeline step failed.") }
        } finally {
            _state.update { it.copy(isAdvancing = false) }
        }
    }

    private fun shouldRun(step: String, status: String): Boolean {
        return when (step) {
            "confirm" -> status == "AWAITING_CONFIRMATION" || status == "QUOTE_EXPIRED"
            "settle" -> status == "SETTLEMENT_PENDING"
            "create_offramp" -> status == "SETTLEMENT_CONFIRMED"
            "payout_processing" -> status == "OFFRAMP_CREATED" || status == "OFFRAMP_PROCESSING"
            "payout_success" -> status == "OFFRAMP_PROCESSING" || status == "FIAT_PAYOUT_PENDING"
            else -> false
        }
    }
}
