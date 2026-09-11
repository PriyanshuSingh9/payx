package com.payx.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.data.PaymentDto
import com.payx.app.payments.TrackerUiState
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import com.payx.app.ui.theme.PayxPalette
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackerScreen(
    state: TrackerUiState,
    onDone: () -> Unit
) {
    val printAnim = remember { Animatable(0f) }
    val payment = state.payment

    LaunchedEffect(payment?.id, payment?.status) {
        printAnim.snapTo(0f)
        printAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing)
        )
    }

    val currentDateStr = remember(payment?.updatedAt, payment?.createdAt) {
        val raw = payment?.completedAt ?: payment?.updatedAt ?: payment?.createdAt
        val parsed = raw?.let { runCatching { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(it) }.getOrNull() }
        val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.US)
        sdf.format(parsed ?: Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF191328),
                        Color(0xFF110E1A),
                        PayxPalette.Obsidian
                    )
                )
            )
    ) {
        // Decorative ambient curved topography background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val path1 = Path().apply {
                moveTo(0f, h * 0.12f)
                cubicTo(w * 0.35f, h * 0.08f, w * 0.7f, h * 0.18f, w, h * 0.11f)
            }
            drawPath(path1, Color(0x18B55CF8), style = Stroke(width = 1.5f))

            val path2 = Path().apply {
                moveTo(0f, h * 0.40f)
                cubicTo(w * 0.4f, h * 0.46f, w * 0.75f, h * 0.34f, w, h * 0.42f)
            }
            drawPath(path2, Color(0x12B55CF8), style = Stroke(width = 1.5f))

            val path3 = Path().apply {
                moveTo(0f, h * 0.75f)
                cubicTo(w * 0.3f, h * 0.70f, w * 0.65f, h * 0.82f, w, h * 0.77f)
            }
            drawPath(path3, Color(0x10A855F7), style = Stroke(width = 1.5f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDone,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PayxPalette.TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Payment Receipt",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = PayxPalette.TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = payment?.id ?: state.paymentId,
                        style = TextStyle(
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = PayxPalette.SoftLavender
                        )
                    )
                }

                IconButton(
                    onClick = {
                        // reprint is driven by payment status changes
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reprint",
                        tint = PayxPalette.VividPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Printer Dispenser Unit (Feeder Slot)
            PrinterSlotHeader(
                isPrinting = printAnim.value < 1f || state.isAdvancing,
                label = if (state.isAdvancing) "ADVANCING PIPELINE..." else null
            )

            // Printed Receipt Container with Downward Feeder Roll Animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .padding(horizontal = 24.dp)
            ) {
                val progress = printAnim.value
                val receiptFullHeight = 540.dp
                val currentHeight = receiptFullHeight * progress

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentHeight)
                        .clipToBounds()
                ) {
                    if (state.isLoading && payment == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PayxPalette.VividPurple)
                        }
                    } else if (state.error != null && payment == null) {
                        Text(
                            text = state.error,
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFFF87171)),
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        ThermalReceiptPaper(
                            payment = payment,
                            paymentId = state.paymentId,
                            dateStr = currentDateStr,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Done Button (Login-style gradient pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(27.dp),
                            spotColor = Color(0x66A855F7),
                            ambientColor = Color(0x33A855F7)
                        )
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                            )
                        )
                        .clickable(onClick = onDone),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DONE",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Modern Thermal Printer Feeder Slot Header
 */
@Composable
private fun PrinterSlotHeader(isPrinting: Boolean, label: String? = null) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F0B18),
        border = BorderStroke(1.dp, Color(0xFF2C2240)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .height(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPrinting) PayxPalette.VividPurple else PayxPalette.SoftLavender
                        )
                )
                Text(
                    text = label
                        ?: if (isPrinting) "PRINTING RECEIPT..." else "PAYX THERMAL DISPENSER",
                    style = TextStyle(
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPrinting) PayxPalette.VividPurple else PayxPalette.TextTertiary
                    )
                )
            }

            // Slit bar
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF251C35))
            )
        }
    }
}

/**
 * Thermal Receipt Paper Composable with serrated / jagged tear-off edge at bottom
 */
@Composable
private fun ThermalReceiptPaper(
    payment: PaymentDto?,
    paymentId: String,
    dateStr: String,
    modifier: Modifier = Modifier
) {
    val inrFormatter = remember { DecimalFormat("#,##,##0.00") }
    val usdFormatter = remember { DecimalFormat("#,##0.00") }
    val receiveInr = payment?.destinationAmount ?: payment?.quote?.recipientAmount ?: 0.0
    val sendUsd = payment?.sourceAmount ?: 0.0
    val rate = payment?.exchangeRate ?: payment?.quote?.exchangeRate ?: 0.0
    val fee = payment?.fees?.offRampFee ?: payment?.quote?.offRampFee ?: 0.0
    val signature = payment?.blockchainTransaction?.transactionSignature.orEmpty()
    val shortSig = if (signature.length > 10) {
        signature.take(4) + "..." + signature.takeLast(4)
    } else {
        signature.ifBlank { "pending" }
    }
    val utr = payment?.offRampOrder?.payoutReference
    val recipientName = payment?.recipient?.name ?: "Recipient"
    val destination = payment?.recipient?.upiId
        ?: payment?.recipient?.phone
        ?: "—"
    val teethShape = remember {
        GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            val toothWidth = 14f
            val toothHeight = 8f

            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h - toothHeight)

            // Jagged teeth along the bottom edge
            var currentX = w
            while (currentX > 0f) {
                val nextPeak = (currentX - toothWidth / 2f).coerceAtLeast(0f)
                lineTo(nextPeak, h)
                val nextValley = (currentX - toothWidth).coerceAtLeast(0f)
                lineTo(nextValley, h - toothHeight)
                currentX -= toothWidth
            }

            lineTo(0f, 0f)
            close()
        }
    }

    Surface(
        shape = teethShape,
        color = Color(0xFF161222),
        border = BorderStroke(1.dp, Color(0xFF32274A)),
        shadowElevation = 18.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Receipt Header Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PX",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = "PAYX PROTOCOL",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = PayxPalette.TextPrimary
                        )
                    )
                    Text(
                        text = "INSTANT REMITTANCE SETTLEMENT",
                        style = TextStyle(
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp,
                            color = PayxPalette.TextTertiary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Success Stamp Pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF221838),
                border = BorderStroke(1.dp, Color(0xFF5B349E))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(PayxPalette.VividPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    Text(
                        text = payment?.statusLabel ?: "LOADING PAYMENT",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = PayxPalette.SoftLavender
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Amount Disbursed
            Text(
                text = "₹${inrFormatter.format(receiveInr)}",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = PayxPalette.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$${usdFormatter.format(sendUsd)} USDC sent via Solana escrow",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = PayxPalette.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Perforated Dashed Line
            DashedSeparator(color = Color(0xFF33274D))

            Spacer(modifier = Modifier.height(12.dp))

            // Corridor Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1F1732))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UsaFlag(width = 18.dp, height = 12.dp)
                    Text(
                        text = "USD",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PayxPalette.TextPrimary
                        )
                    )
                }

                Text(
                    text = if (rate == 0.0) "—" else "1 USDC = ₹${inrFormatter.format(rate)}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = PayxPalette.SoftLavender
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IndiaFlag(width = 18.dp, height = 12.dp)
                    Text(
                        text = "INR",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PayxPalette.TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Receipt Key-Values
            ReceiptDetailRow(label = "Recipient", value = recipientName)
            ReceiptDetailRow(label = "Destination", value = destination)
            ReceiptDetailRow(label = "Off-ramp fee", value = "₹${inrFormatter.format(fee)}")
            ReceiptDetailRow(label = "Date & Time", value = dateStr)
            ReceiptDetailRow(label = "Payment ID", value = payment?.id ?: paymentId)
            ReceiptDetailRow(label = "Solana sig", value = shortSig)
            if (!utr.isNullOrBlank()) {
                ReceiptDetailRow(label = "UTR", value = utr)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Perforated Dashed Line
            DashedSeparator(color = Color(0xFF33274D))

            Spacer(modifier = Modifier.height(12.dp))

            // Barcode Graphic Simulation
            BarcodeStrip(modifier = Modifier.fillMaxWidth().height(26.dp))

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "* 0 8 4 9 2 0 1 7 3 5 *",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = PayxPalette.TextTertiary
                )
            )
        }
    }
}

@Composable
private fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                color = PayxPalette.TextSecondary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = PayxPalette.TextPrimary
            )
        )
    }
}

/**
 * Dashed Line Separator
 */
@Composable
private fun DashedSeparator(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val dashWidth = 8f
        val dashGap = 6f
        var currentX = 0f
        while (currentX < size.width) {
            drawLine(
                color = color,
                start = Offset(currentX, 0f),
                end = Offset((currentX + dashWidth).coerceAtMost(size.width), 0f),
                strokeWidth = 2f
            )
            currentX += dashWidth + dashGap
        }
    }
}

/**
 * Barcode Strip Simulation with alternating bar widths
 */
@Composable
private fun BarcodeStrip(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val barPattern = listOf(
            2f, 4f, 1f, 3f, 5f, 2f, 1f, 4f, 2f, 3f, 1f, 5f, 2f, 4f, 1f, 3f, 2f, 5f, 1f, 4f,
            3f, 1f, 5f, 2f, 3f, 4f, 1f, 2f, 5f, 3f, 1f, 4f, 2f, 3f, 5f, 1f, 2f, 4f, 3f, 1f,
            2f, 5f, 3f, 1f, 4f, 2f, 3f, 1f, 5f, 2f, 4f, 1f, 3f, 2f, 5f, 1f, 4f, 3f, 1f, 2f
        )

        val totalWeight = barPattern.sum() + barPattern.size * 2f
        val step = size.width / totalWeight
        var cursor = 0f

        for (bar in barPattern) {
            val barWidth = bar * step
            drawRect(
                color = Color(0xFFD8B4FE).copy(alpha = 0.65f),
                topLeft = Offset(cursor, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, size.height)
            )
            cursor += barWidth + (2f * step)
        }
    }
}
