package com.payx.app.data

class PaymentRepository(private val api: ApiClient) {
    suspend fun listRecipients(query: String = ""): List<AddressBookRecipient> {
        val params = if (query.isBlank()) emptyMap() else mapOf("q" to query)
        return api.get<RecipientsResponse>("/recipients", params).recipients
    }

    suspend fun previewQuote(amountUsdc: Double): OffRampQuoteDto {
        return api.get<QuotePreviewResponse>(
            "/api/v1/quote",
            mapOf("amount" to amountUsdc.toString())
        ).quote
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
        return response.payment
    }

    suspend fun getPayment(id: String): PaymentDto {
        return api.get<PaymentResponse>("/api/v1/payments/$id").payment
    }

    suspend fun listPayments(limit: Int = 50): List<PaymentDto> {
        return api.get<PaymentsListResponse>(
            "/api/v1/payments",
            mapOf("limit" to limit.toString())
        ).payments
    }

    suspend fun refreshQuote(id: String): PaymentDto {
        return api.post<PaymentResponse, Map<String, String>>(
            "/api/v1/payments/$id/quote",
            emptyMap()
        ).payment
    }

    suspend fun simulateStep(id: String, step: String): PaymentDto {
        return api.post<PaymentResponse, SimulateStepRequest>(
            "/api/v1/payments/$id/simulate-step",
            SimulateStepRequest(step)
        ).payment
    }

    suspend fun getReceiverDashboard(recipient: String = ""): ReceiverDashboardDto {
        val params = if (recipient.isBlank()) emptyMap() else mapOf("recipient" to recipient)
        return api.get<ReceiverDashboardDto>("/api/v1/receiver/dashboard", params)
    }
}

