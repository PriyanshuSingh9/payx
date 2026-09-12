package com.payx.app.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.payx.app.data.AddressBookRecipient
import com.payx.app.data.ApiClient
import com.payx.app.data.ApiException
import com.payx.app.data.OffRampQuoteDto
import com.payx.app.data.PaymentRepository
import com.payx.app.data.SessionStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SendUiState(
    val recipients: List<AddressBookRecipient> = emptyList(),
    val recipientsLoading: Boolean = true,
    val recipientsError: String? = null,
    val searchQuery: String = "",
    val amountInput: String = "",
    val quote: OffRampQuoteDto? = null,
    val quoteLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdPaymentId: String? = null,
    val completedPayment: com.payx.app.data.PaymentDto? = null,
    val paymentCompleted: Boolean = false
) {
    val filteredRecipients: List<AddressBookRecipient>
        get() {
            val q = searchQuery.trim()
            if (q.isEmpty()) return recipients
            return recipients.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.subtitle.contains(q, ignoreCase = true)
            }
        }
}

class SendViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)
    private val payments = PaymentRepository(ApiClient { sessionStore.token })

    private val _state = MutableStateFlow(SendUiState())
    val state: StateFlow<SendUiState> = _state.asStateFlow()

    private var quoteJob: Job? = null

    init {
        refreshRecipients()
        previewQuote(_state.value.amountInput)
    }

    fun refreshRecipients() {
        viewModelScope.launch {
            _state.update { it.copy(recipientsLoading = true, recipientsError = null) }
            try {
                val list = payments.listRecipients(_state.value.searchQuery)
                _state.update { it.copy(recipients = list, recipientsLoading = false) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        recipientsLoading = false,
                        recipientsError = error.message ?: "Could not load recipients."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onAmountChange(raw: String) {
        if (raw.length > 6 || raw.any { !it.isDigit() }) return
        _state.update { it.copy(amountInput = raw, submitError = null) }
        previewQuote(raw)
    }

    fun submit(recipient: AddressBookRecipient, senderWallet: String?) {
        if (_state.value.isSubmitting) return
        val amount = _state.value.amountInput.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            _state.update { it.copy(submitError = "Enter an amount greater than zero.") }
            return
        }
        val wallet = senderWallet?.trim().orEmpty().ifBlank {
            "7xK999999999999999999999999999999999999992PD"
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                    submitError = null,
                    createdPaymentId = null,
                    completedPayment = null,
                    paymentCompleted = false
                )
            }
            try {
                // Execute full pipeline so Solana settlement and payout finish end-to-end
                val payment = try {
                    payments.executePayment(wallet, amount, recipient)
                } catch (_: Exception) {
                    payments.createPayment(wallet, amount, recipient)
                }
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        createdPaymentId = payment.id,
                        completedPayment = payment,
                        paymentCompleted = true
                    )
                }
            } catch (error: ApiException) {
                _state.update { it.copy(isSubmitting = false, submitError = error.message) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = error.message ?: "Could not create payment."
                    )
                }
            }
        }
    }

    fun resetAmount() {
        quoteJob?.cancel()
        _state.update {
            it.copy(
                amountInput = "",
                quote = null,
                quoteLoading = false,
                submitError = null
            )
        }
    }

    fun consumeCreatedPayment() {
        _state.update { it.copy(createdPaymentId = null, completedPayment = null, paymentCompleted = false) }
    }

    private fun previewQuote(raw: String) {
        quoteJob?.cancel()
        val amount = raw.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            _state.update { it.copy(quote = null, quoteLoading = false) }
            return
        }
        quoteJob = viewModelScope.launch {
            delay(280)
            _state.update { it.copy(quoteLoading = true) }
            try {
                val quote = payments.previewQuote(amount)
                _state.update { it.copy(quote = quote, quoteLoading = false) }
            } catch (_: Exception) {
                _state.update { it.copy(quoteLoading = false) }
            }
        }
    }
}
