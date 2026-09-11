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
