package com.payx.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag

@Composable
fun DashboardScreen(
    onSend: () -> Unit,
    onTrack: (String) -> Unit,
    onSettings: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Pure clean matte obsidian background with zero purplish glow
    val screenBackground = Color(0xFF09090D)

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
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Clean Flat Minimal Header (No semi-circle, no purple aura)
            DashboardHeader(
                userName = "Priyanshu",
                balance = "$2,450.00",
                savedAmount = "$66.85",
                onSettings = onSettings
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Real-time Exchange Rate Ticker Pill
            CorridorRateCapsule()

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Quick Send Contacts Tray ("Send Again")
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
                        color = Color(0xFF7E7B8D)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Recent Activity Section
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

        // 5. Clean Bottom Navigation Dock with Central Transfer Pill
        BottomNavigationDock(
            onSend = onSend,
            onSettings = onSettings,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Clean Flat Minimal Header
 * Displays user greeting, corridor switcher, balance, and savings badge with zero curves or background glows.
 */
@Composable
private fun DashboardHeader(
    userName: String,
    balance: String,
    savedAmount: String,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Profile & Corridor Indicator Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: User Greeting & Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable(onClick = onSettings)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1D26))
                        .border(1.dp, Color(0xFF2E2C3A), CircleShape),
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
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }

            // Right: Minimalist Corridor Flags Chip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF14131A),
                border = BorderStroke(1.dp, Color(0xFF22212C))
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

        Spacer(modifier = Modifier.height(38.dp))

        // Center: Balance & Context
        Text(
            text = "TOTAL TRANSFERRED",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFF7E7B8D)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Big, Confident, Solid Balance
        Text(
            text = balance,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.8).sp,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Clean Savings Badge
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF14131A),
            border = BorderStroke(1.dp, Color(0xFF22212C))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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
                        color = Color(0xFF7E7B8D)
                    )
                )
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
        color = Color(0xFF14131A),
        border = BorderStroke(1.dp, Color(0xFF22212C))
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
                .background(Color(0xFF14131A))
                .border(1.dp, Color(0xFF24222E), CircleShape),
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
                color = Color(0xFF6B6878)
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
                .background(Color(0xFF111016))
                .border(1.dp, Color(0xFF24222E), CircleShape),
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
                color = Color(0xFF8E8B9C)
            )
        )

        Text(
            text = "Direct",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                color = Color(0xFF6B6878)
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
        color = Color(0xFF14131A),
        border = BorderStroke(1.dp, Color(0xFF22212C)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                        .background(avatarColor.copy(alpha = 0.18f))
                        .border(1.dp, avatarColor.copy(alpha = 0.35f), CircleShape),
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
                            color = Color(0xFF6B6878)
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
                        color = Color(0xFF8E8B9C)
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
        color = Color(0xFF0F0E14),
        border = BorderStroke(1.dp, Color(0xFF1C1B24)),
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
                    .padding(horizontal = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
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
                    tint = Color(0xFF6B6878),
                    modifier = Modifier.size(23.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Settings",
                    style = TextStyle(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B6878)
                    )
                )
            }
        }
    }
}
