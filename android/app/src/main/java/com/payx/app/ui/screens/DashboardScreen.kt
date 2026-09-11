package com.payx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import com.payx.app.ui.theme.PayxPalette

@Composable
fun DashboardScreen(
    onSend: () -> Unit,
    onTrack: (String) -> Unit,
    onSettings: () -> Unit
) {
    val scrollState = rememberScrollState()

    val ambientBackground = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF191328),
            0.35f to Color(0xFF110E1A),
            0.70f to PayxPalette.Obsidian,
            1.0f to PayxPalette.Obsidian
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientBackground)
    ) {
        // Decorative background topography curves matching LoginScreen
        Canvas(modifier = Modifier.fillMaxSize()) {
            val purpleGlow = Color(0x14B866FC)
            val path1 = Path().apply {
                moveTo(0f, size.height * 0.20f)
                cubicTo(
                    size.width * 0.35f, size.height * 0.14f,
                    size.width * 0.7f, size.height * 0.28f,
                    size.width, size.height * 0.24f
                )
            }
            drawPath(path1, color = purpleGlow, style = Stroke(width = 1.5.dp.toPx()))

            val path2 = Path().apply {
                moveTo(0f, size.height * 0.26f)
                cubicTo(
                    size.width * 0.4f, size.height * 0.18f,
                    size.width * 0.65f, size.height * 0.34f,
                    size.width, size.height * 0.28f
                )
            }
            drawPath(path2, color = Color(0x0EB55CF8), style = Stroke(width = 1.2.dp.toPx()))
        }

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Greeting: "Hello [P] Priyanshu"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hello",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Profile Avatar Circle with Letter P in Login Theme Gradient
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                            )
                        )
                        .border(1.dp, Color(0x55B55CF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Priyanshu",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // 2. Central Hero: Purple Glow Aura & Big Savings Total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Purple radial glow aura background
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x40B55CF8),
                                Color(0x227E2AE8),
                                Color(0x0A0E0C15),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width * 0.45f
                        ),
                        radius = size.width * 0.45f,
                        center = center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Text(
                        text = "You have saved",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = PayxPalette.TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Savings Pill: [$66.85] -> on a transfer of (in sleek purple gradient)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color(0x66B55CF8)),
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$66.85",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "→",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = PayxPalette.TextTertiary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "on a transfer of",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = PayxPalette.TextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Giant Amount: $ 2,450.00 in Serif
                    Text(
                        text = "$ 2,450.00",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.5).sp,
                            color = PayxPalette.TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Live Currency Ticker Capsule
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PayxPalette.DarkSurfaceElevated,
                border = BorderStroke(1.dp, PayxPalette.BorderSubtle),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UsaFlag(width = 20.dp, height = 14.dp)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "1 USD",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PayxPalette.TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "⇄",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = PayxPalette.TextTertiary
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "₹92.90",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PayxPalette.TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IndiaFlag(width = 20.dp, height = 14.dp)

                    Spacer(modifier = Modifier.width(14.dp))

                    // Pulsing Purple LIVE dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PayxPalette.VividPurple)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "LIVE",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = PayxPalette.SoftLavender
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // 4. Recent Transactions Header: "Recent Transactions" VIEW ALL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PayxPalette.TextPrimary
                    )
                )

                Text(
                    text = "VIEW ALL",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = PayxPalette.SoftLavender
                    ),
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Transaction 1: Sent to Priya Sharma (-$500.00)
            TransactionRow(
                title = "Sent to Priya Sharma",
                subtitle = "Apr 16, 17:45",
                amount = "-$500.00",
                onClick = { onTrack("tx_priya_500") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Transaction 2: Sent to Rahul Verma (-$250.00)
            TransactionRow(
                title = "Sent to Rahul Verma",
                subtitle = "Apr 11, 17:45",
                amount = "-$250.00",
                onClick = { onTrack("tx_rahul_250") }
            )
        }

        // 5. Bottom Navigation Bar with Center Violet Gradient TRANSFER Pill
        Surface(
            color = PayxPalette.DarkSurface,
            border = BorderStroke(1.dp, PayxPalette.BorderSubtle),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = PayxPalette.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Home",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PayxPalette.TextPrimary
                        )
                    )
                }

                // Center High-Energy Transfer Pill Button with Login Theme Gradient
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = Color(0x66A855F7),
                            ambientColor = Color(0x33A855F7)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                            )
                        )
                        .clickable(onClick = onSend)
                        .padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TransferArrowIcon()

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "TRANSFER",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                // Settings Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(onClick = onSettings)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = PayxPalette.TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Settings",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PayxPalette.TextTertiary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    title: String,
    subtitle: String,
    amount: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sleek Dark Purple Circle with Soft Violet Upward Arrow
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF231A36))
                    .border(1.dp, Color(0xFF382956), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                UpwardTransactionArrow()
            }

            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PayxPalette.TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = PayxPalette.TextTertiary
                    )
                )
            }
        }

        Text(
            text = amount,
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PayxPalette.TextPrimary
            )
        )
    }
}

@Composable
private fun UpwardTransactionArrow() {
    Canvas(modifier = Modifier.size(16.dp)) {
        val stroke = 2.dp.toPx()
        val cap = StrokeCap.Round
        val color = Color(0xFFD8B4FE)

        // Vertical stem
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.85f),
            end = Offset(size.width / 2f, size.height * 0.15f),
            strokeWidth = stroke,
            cap = cap
        )

        // Left wing
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.45f),
            end = Offset(size.width / 2f, size.height * 0.15f),
            strokeWidth = stroke,
            cap = cap
        )

        // Right wing
        drawLine(
            color = color,
            start = Offset(size.width * 0.8f, size.height * 0.45f),
            end = Offset(size.width / 2f, size.height * 0.15f),
            strokeWidth = stroke,
            cap = cap
        )
    }
}

@Composable
private fun TransferArrowIcon() {
    Canvas(modifier = Modifier.size(14.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, size.height * 0.5f)
            lineTo(0f, size.height)
            lineTo(size.width * 0.3f, size.height * 0.5f)
            close()
        }
        drawPath(path = path, color = Color.White)
    }
}
