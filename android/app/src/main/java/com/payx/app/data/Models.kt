package com.payx.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionUser(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val walletAddress: String,
    val country: String
) {
    val firstName: String
        get() = displayName?.trim()?.substringBefore(" ")?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@")

    val initial: String
        get() = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

@Serializable
data class AuthGoogleRequest(
    val idToken: String,
    val walletAddress: String,
    val country: String? = null
)

@Serializable
data class AuthGoogleResponse(
    val token: String,
    val user: SessionUser
)

@Serializable
data class ApiErrorBody(
    val error: String? = null
)

@Serializable
data class HealthResponse(
    val ok: Boolean = false,
    val service: String? = null
)

@Serializable
data class Corridor(
    val id: String,
    val sourceCurrency: String,
    val destCurrency: String,
    val destRail: String,
    val inProvider: String,
    val outProvider: String,
    val feeBps: Int,
    val etaSeconds: Int
)

@Serializable
data class Quote(
    val corridorId: String,
    val amountSource: Double,
    val feeSource: Double,
    val amountUsdc: Double,
    val fxRate: Double,
    val amountDest: Double,
    val destCurrency: String,
    val etaSeconds: Int,
    val quotedAt: String
)

@Serializable
data class TransferIntent(
    val id: String,
    val status: String,
    @SerialName("escrowPda") val escrowPda: String? = null,
    @SerialName("solanaSignature") val solanaSignature: String? = null
)

@Serializable
data class AddressBookRecipient(
    val id: String,
    val name: String,
    val phone: String = "",
    val upiId: String? = null,
    val bankAccount: String? = null,
    val ifsc: String? = null,
    val avatarInitials: String = "",
    val country: String = "IN"
) {
    val railLabel: String
        get() = if (!upiId.isNullOrBlank()) "UPI" else "IMPS"

    val subtitle: String
        get() = upiId?.takeIf { it.isNotBlank() }
            ?: phone.takeIf { it.isNotBlank() }
            ?: bankAccount.orEmpty()

    val initials: String
        get() = avatarInitials.ifBlank {
            name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
        }
}

@Serializable
data class RecipientsResponse(
    val recipients: List<AddressBookRecipient> = emptyList()
)

@Serializable
data class OffRampQuoteDto(
    val quoteId: String = "",
    val sourceAsset: String = "USDC",
    val destinationCurrency: String = "INR",
    val sourceAmount: Double = 0.0,
    val exchangeRate: Double = 0.0,
    val grossDestinationAmount: Double = 0.0,
    val offRampFee: Double = 0.0,
    val estimatedNetworkFee: Double = 0.0,
    val recipientAmount: Double = 0.0,
    val estimatedMinutesMin: Int = 5,
    val estimatedMinutesMax: Int = 30,
    val quotedAt: String = "",
    val expiresAt: String = ""
)

@Serializable
data class QuotePreviewResponse(
    val quote: OffRampQuoteDto
)

@Serializable
data class PaymentRecipientInput(
    val name: String,
    val phone: String,
    val upiId: String? = null,
    val bankAccount: String? = null,
    val ifsc: String? = null
)

@Serializable
data class CreatePaymentRequest(
    val senderWallet: String,
    val sourceAmount: Double,
    val recipient: PaymentRecipientInput,
    val mode: String = "full_simulation"
)

@Serializable
data class SimulateStepRequest(
    val step: String
)

@Serializable
data class PaymentRecipientDto(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val country: String = "IN",
    val currency: String = "INR",
    val bankAccount: String? = null,
    val ifsc: String? = null,
    val upiId: String? = null
)

@Serializable
data class PaymentFeesDto(
    val offRampFee: Double = 0.0,
    val networkFee: Double = 0.0,
    val totalFee: Double = 0.0
)

@Serializable
data class BlockchainTransactionDto(
    val transactionSignature: String = "",
    val confirmationStatus: String = "",
    val explorerUrl: String? = null,
    val network: String = "simulator"
)

@Serializable
data class OffRampOrderDto(
    val provider: String = "mock",
    val providerOrderId: String = "",
    val status: String = "",
    val fiatAmount: Double = 0.0,
    val payoutReference: String? = null
)

@Serializable
data class TimelineEventDto(
    val id: String = "",
    val timestamp: String = "",
    val status: String = "",
    val title: String = "",
    val description: String = ""
)

@Serializable
data class PaymentDto(
    val id: String,
    val status: String,
    val mode: String = "full_simulation",
    val senderWallet: String = "",
    val recipient: PaymentRecipientDto = PaymentRecipientDto(),
    val sourceAsset: String = "USDC",
    val sourceAmount: Double = 0.0,
    val destinationCurrency: String = "INR",
    val destinationAmount: Double? = null,
    val exchangeRate: Double? = null,
    val fees: PaymentFeesDto? = null,
    val quote: OffRampQuoteDto? = null,
    val blockchainTransaction: BlockchainTransactionDto? = null,
    val offRampOrder: OffRampOrderDto? = null,
    val timeline: List<TimelineEventDto> = emptyList(),
    val failureReason: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val completedAt: String? = null
) {
    val isTerminal: Boolean
        get() = status in TERMINAL_PAYMENT_STATUSES

    val statusLabel: String
        get() = when (status) {
            "CREATED", "QUOTE_PENDING" -> "PREPARING QUOTE"
            "AWAITING_CONFIRMATION" -> "QUOTE LOCKED"
            "SETTLEMENT_PENDING", "SETTLEMENT_SUBMITTED" -> "SETTLING ON SOLANA"
            "SETTLEMENT_CONFIRMED" -> "SETTLEMENT CONFIRMED"
            "OFFRAMP_CREATED", "OFFRAMP_PROCESSING" -> "OFF-RAMP PROCESSING"
            "FIAT_PAYOUT_PENDING" -> "INR PAYOUT PENDING"
            "COMPLETED" -> "PAYMENT COMPLETED"
            "QUOTE_EXPIRED" -> "QUOTE EXPIRED"
            "PAYMENT_FAILED", "SETTLEMENT_FAILED", "OFFRAMP_FAILED", "PAYOUT_FAILED" -> "PAYMENT FAILED"
            else -> status.replace('_', ' ')
        }

    val destinationRail: String
        get() = recipient.upiId?.takeIf { it.isNotBlank() } ?: recipient.phone
}

@Serializable
data class PaymentResponse(
    val payment: PaymentDto
)

@Serializable
data class PaymentsListResponse(
    val payments: List<PaymentDto> = emptyList()
)

val TERMINAL_PAYMENT_STATUSES = setOf(
    "COMPLETED",
    "PAYMENT_FAILED",
    "SETTLEMENT_FAILED",
    "OFFRAMP_FAILED",
    "PAYOUT_FAILED"
)
