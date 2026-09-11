package com.payx.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import com.payx.app.ui.theme.PayxPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackerScreen(
    transferId: String = "px-demo-transfer",
    recipientName: String = "Priya Sharma",
    inrAmount: String = "₹1,79,761.50",
    usdAmount: String = "$2,000.00",
    timeTaken: String = "4.2s",
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val printAnim = remember { Animatable(0f) }
    val tickAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            printAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = LinearOutSlowInEasing)
            )
        }
        delay(250)
        tickAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
    }

    val currentDateStr = remember {
        val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.US)
        sdf.format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090D))
    ) {
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
                        text = "Anchor On-Chain Escrow #0x7A9B",
                        style = TextStyle(
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = PayxPalette.SoftLavender
                        )
                    )
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "PayX Transfer Receipt:\nAmount: $usdAmount ($inrAmount)\nRecipient: $recipientName\nTime Taken: $timeTaken\nTransfer ID: $transferId\nSettled via Solana Anchor Escrow."
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = PayxPalette.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Printer Dispenser Unit (Feeder Slot)
            PrinterSlotHeader(isPrinting = printAnim.value < 1f)

            // Printed Receipt Container with Downward Feeder Roll Animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .padding(horizontal = 24.dp)
            ) {
                val progress = printAnim.value
                val receiptFullHeight = 470.dp
                val currentHeight = receiptFullHeight * progress

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentHeight)
                        .clipToBounds()
                ) {
                    ThermalReceiptPaper(
                        transferId = transferId,
                        dateStr = currentDateStr,
                        recipientName = recipientName,
                        inrAmount = inrAmount,
                        usdAmount = usdAmount,
                        timeTaken = timeTaken,
                        tickProgress = tickAnim.value,
                        modifier = Modifier.fillMaxWidth()
                    )
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
private fun PrinterSlotHeader(isPrinting: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14131C),
        border = BorderStroke(1.dp, Color(0xFF242230)),
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
                    text = if (isPrinting) "PRINTING RECEIPT..." else "PAYX THERMAL DISPENSER",
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
                    .background(Color(0xFF22202E))
            )
        }
    }
}

/**
 * Thermal Receipt Paper Composable with serrated / jagged tear-off edge at bottom
 */
@Composable
private fun ThermalReceiptPaper(
    transferId: String,
    dateStr: String,
    recipientName: String = "Priya Sharma",
    inrAmount: String = "₹1,79,761.50",
    usdAmount: String = "$2,000.00",
    timeTaken: String = "4.2s",
    tickProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
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
        color = Color(0xFF13121A),
        border = BorderStroke(1.dp, Color(0xFF242230)),
        shadowElevation = 18.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Animated Emerald Checkmark Orb
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFF34D399),
                        ambientColor = Color(0xFF059669)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedCheckmarkCircle(
                    modifier = Modifier.size(54.dp),
                    tickProgress = tickProgress
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Amount Disbursed
            val displayInr = if (inrAmount.startsWith("₹")) inrAmount else "₹$inrAmount"
            val displayUsd = if (usdAmount.startsWith("$")) usdAmount else "$$usdAmount"
            Text(
                text = displayInr,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = PayxPalette.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$displayUsd USD sent via Solana Escrow",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = PayxPalette.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Perforated Dashed Line
            DashedSeparator(color = Color(0xFF282538))

            Spacer(modifier = Modifier.height(12.dp))

            // Corridor Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1B1924))
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
                    text = "1 USD = ₹92.90",
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
            val upiHandle = "${recipientName.lowercase().replace(" ", ".")}@upi"
            ReceiptDetailRow(label = "Destination UPI", value = upiHandle)
            ReceiptDetailRow(label = "Sender", value = "Priyanshu Singh")
            ReceiptDetailRow(label = "Transfer Fee (3.25%)", value = "-$0.00 USD (Promo)")
            val secStr = if (timeTaken.endsWith("s", ignoreCase = true)) "${timeTaken.dropLast(1)} sec" else "$timeTaken sec"
            ReceiptDetailRow(label = "Time Taken", value = "$secStr (Instant)")
            ReceiptDetailRow(label = "Date & Time", value = dateStr)
            val shortId = if (transferId.length > 16) transferId.take(16).uppercase() else transferId.uppercase()
            ReceiptDetailRow(label = "Transfer ID", value = shortId)
            ReceiptDetailRow(label = "Escrow PDA", value = "8zB3...4xK2 (Solscan)")

            Spacer(modifier = Modifier.height(14.dp))

            // Perforated Dashed Line
            DashedSeparator(color = Color(0xFF282538))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "* SETTLED IN ",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = PayxPalette.TextTertiary
                    )
                )
                val highlightTime = if (timeTaken.endsWith("s", ignoreCase = true)) {
                    "${timeTaken.dropLast(1)} SEC"
                } else {
                    "$timeTaken SEC"
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF063A28),
                    border = BorderStroke(0.5.dp, Color(0xFF059669))
                ) {
                    Text(
                        text = highlightTime,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF34D399)
                        )
                    )
                }
                Text(
                    text = " VIA SOLANA ESCROW *",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = PayxPalette.TextTertiary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "* 0 8 4 9 2 0 1 7 3 5 *",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
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
 * Proportional animated checkmark circle with glowing emerald disc, rim, pulse halo,
 * and two-phase checkmark stroke animation.
 */
@Composable
private fun AnimatedCheckmarkCircle(
    modifier: Modifier = Modifier,
    tickProgress: Float
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w / 2f

        // 1. Glowing Emerald Background Disc
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            radius = radius
        )

        // 2. High-glow outer rim
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF6EE7B7), Color(0xFF10B981))
            ),
            radius = radius,
            style = Stroke(width = 2.dp.toPx())
        )

        // 3. Expanding Pulse Halo
        if (tickProgress > 0.4f) {
            val haloFrac = (tickProgress - 0.4f) / 0.6f
            drawCircle(
                color = Color(0xFF34D399).copy(alpha = (1f - haloFrac) * 0.45f),
                radius = radius + (radius * 0.35f) * haloFrac,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // 4. Animated Checkmark Stroke
        val p0 = Offset(cx - radius * 0.38f, cy + radius * 0.02f)
        val p1 = Offset(cx - radius * 0.10f, cy + radius * 0.30f)
        val p2 = Offset(cx + radius * 0.42f, cy - radius * 0.25f)

        val strokeW = radius * 0.16f

        if (tickProgress > 0f) {
            if (tickProgress <= 0.35f) {
                val frac = tickProgress / 0.35f
                val currentP = Offset(
                    p0.x + (p1.x - p0.x) * frac,
                    p0.y + (p1.y - p0.y) * frac
                )
                drawLine(
                    color = Color.White,
                    start = p0,
                    end = currentP,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            } else {
                // First segment fully drawn
                drawLine(
                    color = Color.White,
                    start = p0,
                    end = p1,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                // Second segment animating
                val frac = ((tickProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                val currentP = Offset(
                    p1.x + (p2.x - p1.x) * frac,
                    p1.y + (p2.y - p1.y) * frac
                )
                drawLine(
                    color = Color.White,
                    start = p1,
                    end = currentP,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

