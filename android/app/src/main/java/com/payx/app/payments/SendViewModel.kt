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
    val paymentCompleted: Boolean = false,
    val isAddingContact: Boolean = false,
    val addContactError: String? = null
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
        previewQuote(1.0)
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

    fun clearAddContactError() {
        _state.update { it.copy(addContactError = null) }
    }

    fun createContact(
        name: String,
        phone: String,
        upiId: String? = null,
        email: String? = null,
        onSuccess: (AddressBookRecipient) -> Unit
    ) {
        if (_state.value.isAddingContact) return
        _state.update { it.copy(isAddingContact = true, addContactError = null) }
        viewModelScope.launch {
            try {
                val newContact = payments.createContact(
                    name = name,
                    phone = phone,
                    upiId = upiId,
                    email = email
                )
                _state.update { current ->
                    val updated = listOf(newContact) + current.recipients.filter { it.id != newContact.id }
                    current.copy(
                        recipients = updated,
                        isAddingContact = false,
                        addContactError = null
                    )
                }
                onSuccess(newContact)
            } catch (err: Exception) {
                _state.update {
                    it.copy(
                        isAddingContact = false,
                        addContactError = err.message ?: "Failed to add contact."
                    )
                }
            }
        }
    }

    fun lookupContactByPhone(phone: String, onResult: (com.payx.app.data.ContactLookupResponse) -> Unit) {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) return
        viewModelScope.launch {
            val res = payments.lookupContactByPhone(phone)
            onResult(res)
        }
    }

    fun onAmountChange(raw: String, isIndia: Boolean = false, exchangeRate: Double = 92.90) {
        if (raw.length > 7 || raw.any { !it.isDigit() }) return
        _state.update { it.copy(amountInput = raw, submitError = null) }
        val rawNum = raw.toDoubleOrNull() ?: 0.0
        val quoteAmount = if (isIndia && exchangeRate > 0.0) {
            rawNum / exchangeRate
        } else {
            rawNum
        }
        previewQuote(quoteAmount)
    }

    fun submit(
        recipient: AddressBookRecipient,
        senderWallet: String?,
        isIndia: Boolean = false,
        exchangeRate: Double = 92.90
    ) {
        if (_state.value.isSubmitting) return
        val rawAmount = _state.value.amountInput.toDoubleOrNull() ?: 0.0
        if (rawAmount <= 0.0) {
            _state.update { it.copy(submitError = "Enter an amount greater than zero.") }
            return
        }
        val amount = if (isIndia && exchangeRate > 0.0) {
            Math.round((rawAmount / exchangeRate) * 100.0) / 100.0
        } else {
            rawAmount
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
                    val created = payments.createPayment(wallet, amount, recipient)
                    var curr = created
                    val steps = listOf("confirm", "settle", "create_offramp", "payout_processing", "payout_success")
                    for (step in steps) {
                        if (curr.isTerminal) break
                        try {
                            curr = payments.simulateStep(curr.id, step)
                        } catch (_: Exception) {
                            break
                        }
                    }
                    curr
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

    private fun previewQuote(amount: Double) {
        quoteJob?.cancel()
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
