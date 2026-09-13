package com.payx.app.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class PaymentRepository(private val api: ApiClient) {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val PREFS_NAME = "payx_cache_store"
        private const val KEY_CACHED_PAYMENTS = "cached_payments"
        private const val KEY_CACHED_RATE = "cached_live_rate"

        @Volatile
        private var appContext: Context? = null

        @Volatile
        private var cachedPaymentsList: List<PaymentDto>? = null
        @Volatile
        private var cachedPaymentsTimestamp: Long = 0L

        @Volatile
        private var cachedLiveRate: Double? = null
        @Volatile
        private var cachedLiveRateTimestamp: Long = 0L

        private val quoteCache = ConcurrentHashMap<Double, Pair<OffRampQuoteDto, Long>>()
        private val receiverDashboardCache = ConcurrentHashMap<String, Pair<ReceiverDashboardDto, Long>>()

        private const val PAYMENTS_CACHE_TTL_MS = 15_000L
        private const val QUOTE_CACHE_TTL_MS = 30_000L
        private const val RECEIVER_CACHE_TTL_MS = 15_000L

        fun initCache(context: Context) {
            val app = context.applicationContext
            appContext = app
            if (cachedPaymentsList == null) {
                try {
                    val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val raw = prefs.getString(KEY_CACHED_PAYMENTS, null)
                    if (!raw.isNullOrBlank()) {
                        val parsed = json.decodeFromString<List<PaymentDto>>(raw)
                        if (parsed.isNotEmpty()) {
                            cachedPaymentsList = parsed
                            cachedPaymentsTimestamp = System.currentTimeMillis()
                        }
                    }
                    val rateFloat = prefs.getFloat(KEY_CACHED_RATE, 0f)
                    if (rateFloat > 0f) {
                        cachedLiveRate = rateFloat.toDouble()
                        cachedLiveRateTimestamp = System.currentTimeMillis()
                    }
                } catch (_: Exception) {
                }
            }
        }

        private fun persistToDisk() {
            val app = appContext ?: return
            try {
                val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                val payments = cachedPaymentsList
                if (payments != null) {
                    editor.putString(KEY_CACHED_PAYMENTS, json.encodeToString(payments))
                } else {
                    editor.remove(KEY_CACHED_PAYMENTS)
                }
                val rate = cachedLiveRate
                if (rate != null && rate > 0.0) {
                    editor.putFloat(KEY_CACHED_RATE, rate.toFloat())
                }
                editor.apply()
            } catch (_: Exception) {
            }
        }

        fun getCachedPayments(): List<PaymentDto>? = cachedPaymentsList

        fun getCachedLiveRate(): Double? = cachedLiveRate

        fun updatePaymentInCache(payment: PaymentDto) {
            val current = cachedPaymentsList ?: emptyList()
            val index = current.indexOfFirst { it.id == payment.id }
            cachedPaymentsList = if (index >= 0) {
                current.toMutableList().apply { set(index, payment) }
            } else {
                listOf(payment) + current
            }
            cachedPaymentsTimestamp = System.currentTimeMillis()
            persistToDisk()
        }

        fun clearCache() {
            cachedPaymentsList = null
            cachedPaymentsTimestamp = 0L
            cachedLiveRate = null
            cachedLiveRateTimestamp = 0L
            quoteCache.clear()
            receiverDashboardCache.clear()
            appContext?.let {
                try {
                    it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
                } catch (_: Exception) {
                }
            }
        }
    }

    suspend fun listRecipients(query: String = ""): List<AddressBookRecipient> {
        val params = if (query.isBlank()) emptyMap() else mapOf("q" to query)
        return api.get<RecipientsResponse>("/recipients", params).recipients
    }

    suspend fun createContact(
        name: String,
        phone: String,
        upiId: String? = null,
        bankAccount: String? = null,
        ifsc: String? = null,
        email: String? = null,
        country: String = "IN"
    ): AddressBookRecipient {
        val request = CreateContactRequest(
            name = name.trim(),
            phone = phone.trim(),
            upiId = upiId?.trim()?.ifBlank { null },
            bankAccount = bankAccount?.trim()?.ifBlank { null },
            ifsc = ifsc?.trim()?.ifBlank { null },
            email = email?.trim()?.ifBlank { null },
            country = country
        )
        val response = api.post<CreateContactResponse, CreateContactRequest>("/api/v1/contacts", request)
        return response.contact ?: response.recipient ?: throw ApiException("Contact creation failed")
    }

    suspend fun lookupContactByPhone(phone: String): ContactLookupResponse {
        return try {
            api.get<ContactLookupResponse>("/api/v1/contacts/lookup", mapOf("phone" to phone))
        } catch (_: Exception) {
            ContactLookupResponse(found = false, message = "Lookup service unavailable.")
        }
    }

    suspend fun previewQuote(amountUsdc: Double, forceRefresh: Boolean = false): OffRampQuoteDto {
        val now = System.currentTimeMillis()
        val cached = quoteCache[amountUsdc]
        if (!forceRefresh && cached != null && (now - cached.second) < QUOTE_CACHE_TTL_MS) {
            return cached.first
        }

        return try {
            val quote = api.get<QuotePreviewResponse>(
                "/api/v1/quote",
                mapOf("amount" to amountUsdc.toString())
            ).quote
            quoteCache[amountUsdc] = Pair(quote, now)
            cachedLiveRate = quote.exchangeRate
            cachedLiveRateTimestamp = now
            persistToDisk()
            quote
        } catch (e: Exception) {
            cached?.first ?: throw e
        }
    }

    suspend fun createPayment(
        senderWallet: String,
        amountUsdc: Double,
        recipient: AddressBookRecipient
    ): PaymentDto {
        val response = api.post<PaymentResponse, CreatePaymentRequest>(
            "/api/v1/payments",
            CreatePaymentRequest(
                senderWallet = senderWallet,
                sourceAmount = amountUsdc,
                recipient = PaymentRecipientInput(
                    name = recipient.name,
                    phone = recipient.phone,
                    upiId = recipient.upiId,
                    bankAccount = recipient.bankAccount,
                    ifsc = recipient.ifsc
                ),
                mode = "full_simulation"
            )
        )
        updatePaymentInCache(response.payment)
        return response.payment
    }

    suspend fun executePayment(
        senderWallet: String,
        amountUsdc: Double,
        recipient: AddressBookRecipient
    ): PaymentDto {
        val response = api.post<PaymentResponse, CreatePaymentRequest>(
            "/api/v1/payments/execute",
            CreatePaymentRequest(
                senderWallet = senderWallet,
                sourceAmount = amountUsdc,
                recipient = PaymentRecipientInput(
                    name = recipient.name,
                    phone = recipient.phone,
                    upiId = recipient.upiId,
                    bankAccount = recipient.bankAccount,
                    ifsc = recipient.ifsc
                ),
                mode = "full_simulation"
            )
        )
        val payment = response.payment
        updatePaymentInCache(payment)
        return payment
    }

    suspend fun getPayment(id: String): PaymentDto {
        cachedPaymentsList?.find { it.id == id }?.let { cached ->
            if (cached.isTerminal) return cached
        }
        val payment = api.get<PaymentResponse>("/api/v1/payments/$id").payment
        updatePaymentInCache(payment)
        return payment
    }

    suspend fun listPayments(limit: Int = 50, forceRefresh: Boolean = false): List<PaymentDto> {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedPaymentsList != null && (now - cachedPaymentsTimestamp) < PAYMENTS_CACHE_TTL_MS) {
            return cachedPaymentsList!!
        }

        return try {
            val response = api.get<PaymentsListResponse>(
                "/api/v1/payments",
                mapOf("limit" to limit.toString())
            ).payments
            cachedPaymentsList = response
            cachedPaymentsTimestamp = now
            persistToDisk()
            response
        } catch (e: Exception) {
            cachedPaymentsList ?: throw e
        }
    }

    suspend fun refreshQuote(id: String): PaymentDto {
        val payment = api.post<PaymentResponse, Map<String, String>>(
            "/api/v1/payments/$id/quote",
            emptyMap()
        ).payment
        updatePaymentInCache(payment)
        return payment
    }

    suspend fun simulateStep(id: String, step: String): PaymentDto {
        val payment = api.post<PaymentResponse, SimulateStepRequest>(
            "/api/v1/payments/$id/simulate-step",
            SimulateStepRequest(step)
        ).payment
        updatePaymentInCache(payment)
        return payment
    }

    suspend fun getReceiverDashboard(recipient: String = "", forceRefresh: Boolean = false): ReceiverDashboardDto {
        val now = System.currentTimeMillis()
        val key = recipient.trim()
        val cached = receiverDashboardCache[key]
        if (!forceRefresh && cached != null && (now - cached.second) < RECEIVER_CACHE_TTL_MS) {
            return cached.first
        }

        return try {
            val params = if (recipient.isBlank()) emptyMap() else mapOf("recipient" to recipient)
            val result = api.get<ReceiverDashboardDto>("/api/v1/receiver/dashboard", params)
            receiverDashboardCache[key] = Pair(result, now)
            result
        } catch (e: Exception) {
            cached?.first ?: throw e
        }
    }
}

