package com.payx.app.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.payx.app.ui.theme.PayxPalette

@Composable
fun DashboardScreen(
    onSend: () -> Unit,
    onTrack: (String) -> Unit,
    onSettings: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PayxPalette.Obsidian)
    ) {
        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Top Bar: Profile Avatar + Search + Notifications
            DashboardHeader(onProfileClick = onSettings)

            Spacer(modifier = Modifier.height(28.dp))

            // 2. Total Balance Hero
            Text(
                text = "Total Balance",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    color = PayxPalette.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$8,890.00",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                )

                // Currency Tag Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1A2C),
                    border = BorderStroke(1.dp, Color(0xFF322A45))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UsaMiniFlag()
                        Text(
                            text = "USD",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Money hold: 4,000.00",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = PayxPalette.TextTertiary
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Purple Gradient Virtual Card (Eva K. - 9154)
            HeroVirtualCard(
                onSendClick = onSend
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Two Column Metrics: Cash Savings & Top Spend Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Cash Savings",
                    value = "€0.00",
                    badgeColor = PayxPalette.CyberGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Top Spend Day",
                    value = "Wed - $480",
                    badgeColor = PayxPalette.CoralAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Earn Statistic Bar Chart Card
            EarnStatisticCard()

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Cash Flow Progress Card
            CashFlowCard()

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 7. Floating Bottom Navigation Bar
        FloatingBottomBar(
            activeTab = activeTab,
            onTabSelected = { tab ->
                activeTab = tab
                if (tab == 4) onSettings()
                if (tab == 2) onSend()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun DashboardHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Avatar Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1A2C))
                .border(1.dp, Color(0xFF2F2843), CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color(0xFFD8B4FE),
                modifier = Modifier.size(22.dp)
            )
        }

        // Search & Notifications Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1A2C))
                    .border(1.dp, Color(0xFF2F2843), CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1A2C))
                    .border(1.dp, Color(0xFF2F2843), CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                // Notification Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .clip(CircleShape)
                        .background(PayxPalette.CoralAccent)
                )
            }
        }
    }
}

@Composable
private fun HeroVirtualCard(onSendClick: () -> Unit) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            PayxPalette.CardGradientStart,
            PayxPalette.CardGradientEnd
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(204.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x66A855F7),
                ambientColor = Color(0x33A855F7)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(cardGradient)
    ) {
        // Subtle decorative topographic lines across the card
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeColor = Color(0x22FFFFFF)
            val path = Path().apply {
                moveTo(0f, size.height * 0.4f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.2f,
                    size.width * 0.6f, size.height * 0.6f,
                    size.width, size.height * 0.35f
                )
            }
            drawPath(path, color = strokeColor, style = Stroke(width = 2.dp.toPx()))

            val path2 = Path().apply {
                moveTo(0f, size.height * 0.6f)
                cubicTo(
                    size.width * 0.4f, size.height * 0.35f,
                    size.width * 0.7f, size.height * 0.75f,
                    size.width, size.height * 0.5f
                )
            }
            drawPath(path2, color = strokeColor, style = Stroke(width = 1.5.dp.toPx()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card Top Row: Card number, Expiry, Mastercard Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•••• 9154",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "12/24",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xCCFFFFFF)
                        )
                    )
                    // Mastercard Badge (overlapping circles)
                    MastercardGlyph()
                }
            }

            // Card Middle: Total Budget
            Column {
                Text(
                    text = "Total Budget",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xD9FFFFFF)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$2,320.00",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "USD",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xE6FFFFFF)
                        )
                    )
                }
            }

            // Card Bottom Actions: Deposit & Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deposit Pill Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(Color(0x28FFFFFF))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Deposit",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }

                // Send Pill Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(Color(0x28FFFFFF))
                        .clickable(onClick = onSendClick),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SendDiagonalArrow(modifier = Modifier.size(14.dp))
                        Text(
                            text = "Send",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = PayxPalette.DarkSurface,
        border = BorderStroke(1.dp, PayxPalette.BorderSubtle),
        modifier = modifier.height(108.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = PayxPalette.TextSecondary
                    )
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
            }

            Text(
                text = value,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun EarnStatisticCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PayxPalette.DarkSurface,
        border = BorderStroke(1.dp, PayxPalette.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Earn Statistic",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF221C32),
                    border = BorderStroke(1.dp, Color(0xFF352C4D))
                ) {
                    Text(
                        text = "Week ▾",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = PayxPalette.TextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Colorful Stacked Bar Chart
            val days = listOf("Sun", "Mon", "Tue", "Wed", "Thr", "Fri", "Sat")
            val coralRatios = listOf(0.4f, 0.65f, 0.3f, 0.5f, 0.8f, 0.6f, 0.45f)
            val purpleRatios = listOf(0.5f, 0.25f, 0.6f, 0.4f, 0.15f, 0.35f, 0.5f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in days.indices) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(60.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF262035))
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Coral segment
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(coralRatios[i])
                                        .background(PayxPalette.CoralAccent)
                                )
                                // Purple segment
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(purpleRatios[i])
                                        .background(PayxPalette.VividPurple)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = days[i],
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = PayxPalette.TextTertiary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashFlowCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PayxPalette.DarkSurface,
        border = BorderStroke(1.dp, PayxPalette.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash Flow",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "$2,000.00 / $3,200.00",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = PayxPalette.CyberGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cyber Green Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF242035))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PayxPalette.CyberGreen)
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF141020),
        border = BorderStroke(1.dp, Color(0xFF2D2540)),
        shadowElevation = 18.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Home
            IconButton(onClick = { onTabSelected(0) }) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (activeTab == 0) PayxPalette.VividPurple else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (activeTab == 0) Color.White else PayxPalette.TextTertiary
                    )
                }
            }

            // Tab 1: Cards / Wallet
            IconButton(onClick = { onTabSelected(1) }) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Cards",
                    tint = if (activeTab == 1) PayxPalette.VividPurple else PayxPalette.TextTertiary
                )
            }

            // Center Action Button (Circular FAB for Send / Swap)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                        )
                    )
                    .clickable { onTabSelected(2) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Tab 3: Analytics
            IconButton(onClick = { onTabSelected(3) }) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Analytics",
                    tint = if (activeTab == 3) PayxPalette.VividPurple else PayxPalette.TextTertiary
                )
            }

            // Tab 4: Settings
            IconButton(onClick = { onTabSelected(4) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (activeTab == 4) PayxPalette.VividPurple else PayxPalette.TextTertiary
                )
            }
        }
    }
}

@Composable
private fun MastercardGlyph() {
    Canvas(modifier = Modifier.size(width = 28.dp, height = 18.dp)) {
        val radius = size.height / 2f
        drawCircle(
            color = Color(0xFFEB001B),
            radius = radius,
            center = Offset(radius, radius)
        )
        drawCircle(
            color = Color(0xFFF79E1B),
            radius = radius,
            center = Offset(size.width - radius, radius)
        )
    }
}

@Composable
private fun SendDiagonalArrow(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        // Line from bottom-left to top-right
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.2f, size.height * 0.8f),
            end = Offset(size.width * 0.8f, size.height * 0.2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrow head
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.45f, size.height * 0.2f),
            end = Offset(size.width * 0.8f, size.height * 0.2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.8f, size.height * 0.2f),
            end = Offset(size.width * 0.8f, size.height * 0.55f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun UsaMiniFlag(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(width = 18.dp, height = 12.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        val stripeH = size.height / 7f
        for (i in 0 until 7) {
            drawRect(
                color = if (i % 2 == 0) Color(0xFFB22234) else Color.White,
                topLeft = Offset(0f, i * stripeH),
                size = Size(size.width, stripeH)
            )
        }
        drawRect(
            color = Color(0xFF3C3B6E),
            topLeft = Offset.Zero,
            size = Size(size.width * 0.45f, stripeH * 4)
        )
    }
}
