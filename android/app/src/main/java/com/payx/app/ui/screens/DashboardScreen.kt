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

    // Clean, deep obsidian matte background
    val screenBackground = Color(0xFF090710)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {
        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Solid Architectural Semi-Circle Header
            SolidSemiCircleVaultHeader(
                userName = "Priyanshu",
                balance = "$2,450.00",
                savedAmount = "$66.85",
                onSettings = onSettings
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Clean Real-time Exchange Rate Ticker
            CorridorRateCapsule()

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Human-Crafted Action Buttons (Solid Lavender Send, Solid Dark Receive)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action: Send Money (Solid Lavender, Bold, High-Contrast)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFAE9EF8),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable(onClick = onSend)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionUpRightArrow(color = Color(0xFF130B24))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Money",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF130B24)
                            )
                        )
                    }
                }

                // Secondary Action: Receive (Solid Dark Plum Card with Clean Border)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF171126),
                    border = BorderStroke(1.dp, Color(0xFF2C2044)),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable(onClick = onSend)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionDownLeftArrow(color = Color(0xFFAE9EF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Receive",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 4. Quick Send Contacts Tray ("Send Again")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Send Again",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8A7FA6)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickContactItem(
                        initials = "PS",
                        name = "Priya",
                        rail = "UPI",
                        color = Color(0xFF7C3AED),
                        onClick = { onTrack("tx_priya_500") }
                    )

                    QuickContactItem(
                        initials = "RV",
                        name = "Rahul",
                        rail = "IMPS",
                        color = Color(0xFF4F46E5),
                        onClick = { onTrack("tx_rahul_250") }
                    )

                    QuickContactItem(
                        initials = "AS",
                        name = "Arjun",
                        rail = "UPI",
                        color = Color(0xFF9333EA),
                        onClick = onSend
                    )

                    QuickAddContactItem(onClick = onSend)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 5. Recent Activity List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "See all",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAE9EF8)
                        ),
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 1: Priya Sharma
                TransactionCard(
                    title = "Priya Sharma",
                    subtitle = "Yesterday, 17:45 • Solana Settled",
                    fiatAmount = "-$500.00",
                    inrEquivalent = "≈ ₹41,950",
                    initials = "PS",
                    avatarColor = Color(0xFF7C3AED),
                    onClick = { onTrack("tx_priya_500") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card 2: Rahul Verma
                TransactionCard(
                    title = "Rahul Verma",
                    subtitle = "Apr 11, 14:20 • Solana Settled",
                    fiatAmount = "-$250.00",
                    inrEquivalent = "≈ ₹20,975",
                    initials = "RV",
                    avatarColor = Color(0xFF4F46E5),
                    onClick = { onTrack("tx_rahul_250") }
                )
            }
        }

        // 6. Solid Bottom Navigation Dock
        BottomNavigationDock(
            onSend = onSend,
            onSettings = onSettings,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Solid Architectural Semi-Circle Vault Dome Header
 * A tangible, solid dark-plum architectural dome that covers the top of the homepage
 * and smoothly curves down, framing the balance cleanly without any fake sci-fi HUD elements.
 */
@Composable
private fun SolidSemiCircleVaultHeader(
    userName: String,
    balance: String,
    savedAmount: String,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(265.dp)
    ) {
        // 1. Solid Geometric Semi-Circle Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val curveHeight = size.height - 10.dp.toPx()
            val cornerY = 70.dp.toPx()

            // Solid Vault Dome Path
            val domePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, cornerY)
                // Smooth, sweeping semi-circular curve dipping to center bottom
                cubicTo(
                    w * 0.95f, curveHeight * 0.76f,
                    w * 0.68f, curveHeight,
                    w * 0.50f, curveHeight
                )
                cubicTo(
                    w * 0.32f, curveHeight,
                    w * 0.05f, curveHeight * 0.76f,
                    0f, cornerY
                )
                lineTo(0f, 0f)
                close()
            }

            // Soft natural drop shadow under the solid curve
            val shadowPath = Path().apply {
                moveTo(0f, cornerY + 2.dp.toPx())
                cubicTo(
                    w * 0.05f, curveHeight * 0.76f + 3.dp.toPx(),
                    w * 0.32f, curveHeight + 4.dp.toPx(),
                    w * 0.50f, curveHeight + 4.dp.toPx()
                )
                cubicTo(
                    w * 0.68f, curveHeight + 4.dp.toPx(),
                    w * 0.95f, curveHeight * 0.76f + 3.dp.toPx(),
                    w, cornerY + 2.dp.toPx()
                )
            }
            drawPath(
                path = shadowPath,
                color = Color.Black.copy(alpha = 0.55f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Solid surface fill: Deep rich plum to midnight violet
            drawPath(
                path = domePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1535),
                        Color(0xFF18112A),
                        Color(0xFF130C22)
                    ),
                    startY = 0f,
                    endY = curveHeight
                )
            )

            // Clean, crisp solid hairline border along the curve
            val borderCurve = Path().apply {
                moveTo(0f, cornerY)
                cubicTo(
                    w * 0.05f, curveHeight * 0.76f,
                    w * 0.32f, curveHeight,
                    w * 0.50f, curveHeight
                )
                cubicTo(
                    w * 0.68f, curveHeight,
                    w * 0.95f, curveHeight * 0.76f,
                    w, cornerY
                )
            }
            drawPath(
                path = borderCurve,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x3344336A),
                        Color(0xFF6B549E),
                        Color(0xFF8B72C4),
                        Color(0xFF6B549E),
                        Color(0x3344336A)
                    )
                ),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 2. Content overlay inside the Solid Semi-Circle Header
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Top Profile & Corridor Indicator Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: User Greeting & Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.clickable(onClick = onSettings)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF332352))
                            .border(1.dp, Color(0xFF4C3678), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Text(
                        text = "Hello, $userName",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }

                // Right: Minimalist Corridor Flags Chip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF22173B),
                    border = BorderStroke(1.dp, Color(0xFF352658))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UsaFlag(width = 15.dp, height = 10.dp)
                        Text(
                            text = "⇄",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFAE9EF8)
                            )
                        )
                        IndiaFlag(width = 15.dp, height = 10.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Center: Balance & Financial Context
            Text(
                text = "TOTAL TRANSFERRED",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF9E8FB8)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Big, Confident, Solid Balance
            Text(
                text = balance,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Clean Savings Badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF211638),
                border = BorderStroke(1.dp, Color(0xFF332454))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Saved $savedAmount",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    )
                    Text(
                        text = "vs bank wire fees",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFB5A8D8)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Clean Realtime Exchange Rate Pill
 */
@Composable
private fun CorridorRateCapsule() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF140E22),
        border = BorderStroke(1.dp, Color(0xFF261A3C))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UsaFlag(width = 16.dp, height = 11.dp)

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "1 USD",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "⇄",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFFAE9EF8)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "₹92.90",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.width(7.dp))

            IndiaFlag(width = 16.dp, height = 11.dp)

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = "Real-time",
                style = TextStyle(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34D399)
                )
            )
        }
    }
}

/**
 * Quick Send Contact Avatar Item
 */
@Composable
private fun QuickContactItem(
    initials: String,
    name: String,
    rail: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF171126))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = name,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )

        Text(
            text = rail,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                color = Color(0xFF7A6F94)
            )
        )
    }
}

/**
 * Quick Add Contact Item
 */
@Composable
private fun QuickAddContactItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF130E20))
                .border(1.dp, Color(0xFF2C2046), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFAE9EF8)
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "New",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PayxPalette.TextSecondary
            )
        )

        Text(
            text = "Direct",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                color = Color(0xFF7A6F94)
            )
        )
    }
}

/**
 * Clean Transaction Card
 */
@Composable
private fun TransactionCard(
    title: String,
    subtitle: String,
    fiatAmount: String,
    inrEquivalent: String,
    initials: String,
    avatarColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF130E20),
        border = BorderStroke(1.dp, Color(0xFF221735)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.20f))
                        .border(1.dp, avatarColor.copy(alpha = 0.40f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7A6F94)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = fiatAmount,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = inrEquivalent,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.5.sp,
                        color = Color(0xFF9E8FB8)
                    )
                )
            }
        }
    }
}

/**
 * Solid Bottom Navigation Dock
 */
@Composable
private fun BottomNavigationDock(
    onSend: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F0A1C),
        border = BorderStroke(1.dp, Color(0xFF221738)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 9.dp),
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
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Home",
                    style = TextStyle(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Center Solid TRANSFER Pill Button
            Box(
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(Color(0xFFAE9EF8))
                    .clickable(onClick = onSend)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionUpRightArrow(color = Color(0xFF130B24))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRANSFER",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF130B24)
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
                    tint = Color(0xFF7A6F94),
                    modifier = Modifier.size(23.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Settings",
                    style = TextStyle(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7A6F94)
                    )
                )
            }
        }
    }
}

/**
 * Minimal Canvas Arrow: Up-Right Remittance Arrow
 */
@Composable
private fun ActionUpRightArrow(color: Color) {
    Canvas(modifier = Modifier.size(12.dp)) {
        val stroke = 2.dp.toPx()
        val cap = StrokeCap.Round

        // Diagonal shaft
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.85f),
            end = Offset(size.width * 0.85f, size.height * 0.15f),
            strokeWidth = stroke,
            cap = cap
        )

        // Top horizontal wing
        drawLine(
            color = color,
            start = Offset(size.width * 0.40f, size.height * 0.15f),
            end = Offset(size.width * 0.85f, size.height * 0.15f),
            strokeWidth = stroke,
            cap = cap
        )

        // Right vertical wing
        drawLine(
            color = color,
            start = Offset(size.width * 0.85f, size.height * 0.60f),
            end = Offset(size.width * 0.85f, size.height * 0.15f),
            strokeWidth = stroke,
            cap = cap
        )
    }
}

/**
 * Minimal Canvas Arrow: Down-Left Inflow Arrow
 */
@Composable
private fun ActionDownLeftArrow(color: Color) {
    Canvas(modifier = Modifier.size(12.dp)) {
        val stroke = 2.dp.toPx()
        val cap = StrokeCap.Round

        // Diagonal shaft
        drawLine(
            color = color,
            start = Offset(size.width * 0.85f, size.height * 0.15f),
            end = Offset(size.width * 0.15f, size.height * 0.85f),
            strokeWidth = stroke,
            cap = cap
        )

        // Left horizontal wing
        drawLine(
            color = color,
            start = Offset(size.width * 0.60f, size.height * 0.85f),
            end = Offset(size.width * 0.15f, size.height * 0.85f),
            strokeWidth = stroke,
            cap = cap
        )

        // Bottom vertical wing
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.40f),
            end = Offset(size.width * 0.15f, size.height * 0.85f),
            strokeWidth = stroke,
            cap = cap
        )
    }
}
