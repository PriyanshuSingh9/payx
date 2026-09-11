package com.payx.app.ui.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.R
import com.payx.app.data.SessionUser
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

private val MontaguSlab = FontFamily(Font(R.font.montagu_slab))

@Composable
fun DashboardScreen(
    user: SessionUser?,
    onSend: () -> Unit,
    onTrack: (String) -> Unit,
    onSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val sendAgainScrollState = rememberScrollState()
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
                .padding(bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Clean Flat Header with Distinct Typographic Hierarchy
            DashboardHeader(
                userName = user?.firstName ?: "there",
                balance = "$2,450.00",
                savedAmount = "$66.85",
                onSettings = onSettings
            )

            Spacer(modifier = Modifier.height(46.dp))

            // 2. Quick Send Contacts Tray ("Send Again") with Updated Payees
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Send Again",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(sendAgainScrollState)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SendAgainContact(
                        initial = "P",
                        name = "Priya",
                        backgroundColor = Color(0xFFE91E63),
                        showActiveDot = true,
                        onClick = { onTrack("tx_priya_500") }
                    )

                    SendAgainContact(
                        initial = "R",
                        name = "Rahul",
                        backgroundColor = Color(0xFF2E7D32),
                        onClick = { onTrack("tx_rahul_250") }
                    )

                    SendAgainContact(
                        initial = "S",
                        name = "Sarah",
                        backgroundColor = Color(0xFFF4511E),
                        onClick = onSend
                    )

                    SendAgainAddContact(onClick = onSend)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Recent Activity Section with Synchronized Identities
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "See all",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAE9EF8)
                        ),
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 1: Priya Sharma
                TransactionCard(
                    title = "Priya Sharma",
                    subtitle = "Yesterday, 17:45 • Solana Settled",
                    fiatAmount = "-$500.00",
                    inrEquivalent = "≈ ₹41,950",
                    initial = "P",
                    avatarColor = Color(0xFFE91E63),
                    onClick = { onTrack("tx_priya_500") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card 2: Rahul Verma
                TransactionCard(
                    title = "Rahul Verma",
                    subtitle = "Apr 11, 14:20 • Solana Settled",
                    fiatAmount = "-$250.00",
                    inrEquivalent = "≈ ₹20,975",
                    initial = "R",
                    avatarColor = Color(0xFF2E7D32),
                    onClick = { onTrack("tx_rahul_250") }
                )
            }
        }

        // 4. Solid Bottom Navigation Dock with Animated Transfer Action
        BottomNavigationDock(
            onSend = onSend,
            onSettings = onSettings,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Clean Flat Header with High-Impact Typographic Hierarchy
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
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Profile & Corridor Indicator Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: User Greeting & Avatar with two-tier hierarchy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable(onClick = onSettings)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16151E))
                        .border(1.dp, Color(0xFF2E2C3D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1),
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAE9EF8)
                        )
                    )
                }

                Column {
                    Text(
                        text = "Hello,",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7E7B8D)
                        )
                    )
                    Text(
                        text = userName,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Right: Minimalist Corridor Flags Chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF121118),
                border = BorderStroke(1.dp, Color(0xFF22202E))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UsaFlag(width = 16.dp, height = 11.dp)
                    Text(
                        text = "⇄",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAE9EF8)
                        )
                    )
                    IndiaFlag(width = 16.dp, height = 11.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center: Section Sub-label with Wide Tracking
        Text(
            text = "TOTAL TRANSFERRED",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.8.sp,
                color = Color(0xFF8E8B9C)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Big, Confident, Hero Balance
        Text(
            text = balance,
            style = TextStyle(
                fontFamily = MontaguSlab,
                fontSize = 46.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-1.5).sp,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Adjusted Saved Metric (Removed 'vs bank wire fees')
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Saved $savedAmount",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34D399)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Exchange Rate Bridge Line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UsaFlag(width = 16.dp, height = 11.dp)

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "1 USD",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD4D2E0)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "⇄",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAE9EF8)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "₹92.90",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.width(7.dp))

            IndiaFlag(width = 16.dp, height = 11.dp)
        }
    }
}

/**
 * Send Again Solid Circular Contact
 * Matches the reference style with vibrant solid flat colors and top-right indicator dot.
 */
@Composable
private fun SendAgainContact(
    initial: String,
    name: String,
    backgroundColor: Color,
    showActiveDot: Boolean = false,
    onClick: () -> Unit
) {
    val screenBackground = Color(0xFF09090D)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main solid circular avatar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                )
            }

            // Top-right subtle status dot
            if (showActiveDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF93C5FD))
                        .border(2.dp, screenBackground, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = name,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}

/**
 * Send Again Add Contact Button
 */
@Composable
private fun SendAgainAddContact(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF14131C))
                .border(1.dp, Color(0xFF2E2C3D), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFAE9EF8)
                )
            )
        }

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = "New",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8E8B9C)
            )
        )
    }
}

/**
 * Clean Transaction Card with High Typographic Clarity
 */
@Composable
private fun TransactionCard(
    title: String,
    subtitle: String,
    fiatAmount: String,
    inrEquivalent: String,
    initial: String,
    avatarColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF121118),
        border = BorderStroke(1.dp, Color(0xFF201E2B)),
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
                // Avatar circle matching payee identity
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7E7B8D)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = fiatAmount,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = inrEquivalent,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E8B9C)
                    )
                )
            }
        }
    }
}

/**
 * Solid Bottom Navigation Dock with Premium Animated Transfer Action
 */
@Composable
private fun BottomNavigationDock(
    onSend: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isTransferring by remember { mutableStateOf(false) }

    // Arrow flight across the entire button width to exit the right edge
    val arrowFlightEasing = remember { CubicBezierEasing(0.32f, 0f, 0.20f, 1f) }

    val arrowOffsetX by animateDpAsState(
        targetValue = if (isTransferring) 126.dp else 0.dp,
        animationSpec = tween(durationMillis = 360, easing = arrowFlightEasing),
        label = "arrowOffsetX"
    )

    val arrowScale by animateFloatAsState(
        targetValue = if (isTransferring) 1.12f else 1f,
        animationSpec = tween(durationMillis = 260, easing = arrowFlightEasing),
        label = "arrowScale"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isTransferring) 0.95f else 1f,
        animationSpec = tween(durationMillis = 130, easing = arrowFlightEasing),
        label = "buttonScale"
    )

    Surface(
        color = Color(0xFF0C0B10),
        border = BorderStroke(1.dp, Color(0xFF1B1A24)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab Container
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
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
                        tint = Color(0xFFAE9EF8),
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Home",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAE9EF8)
                        )
                    )
                }
            }

            // Center Animated TRANSFER Pill Button
            Box(
                modifier = Modifier
                    .width(152.dp)
                    .height(48.dp)
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Main pill container with solid background
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFAE9EF8))
                        .clickable(
                            enabled = !isTransferring,
                            onClick = {
                                if (!isTransferring) {
                                    isTransferring = true
                                    coroutineScope.launch {
                                        delay(370)
                                        onSend()
                                        delay(400)
                                        isTransferring = false
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Dart Arrow on the left with animated rear thruster wake
                        Box(
                            modifier = Modifier.graphicsLayer {
                                translationX = arrowOffsetX.toPx()
                                scaleX = arrowScale
                                scaleY = arrowScale
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            TransferDartArrowWithThruster(
                                color = Color(0xFF0E091A),
                                isTransferring = isTransferring
                            )
                        }

                        Spacer(modifier = Modifier.width(7.dp))

                        // TRANSFER text that cascades down character-by-character
                        StaggeredTransferText(
                            isTransferring = isTransferring
                        )

                        // Balance the 17.dp of empty space reserved for the thruster wake on the left side of the arrow
                        Spacer(modifier = Modifier.width(17.dp))
                    }
                }
            }

            // Settings Tab Container
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
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
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B6878)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Staggered Character-by-Character Cascading Wave for TRANSFER text
 */
@Composable
private fun StaggeredTransferText(
    isTransferring: Boolean,
    modifier: Modifier = Modifier
) {
    val text = "TRANSFER"
    val fastAccelerate = remember { CubicBezierEasing(0.38f, 0f, 0.24f, 1f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        text.forEachIndexed { index, char ->
            val charOffsetY by animateDpAsState(
                targetValue = if (isTransferring) 32.dp else 0.dp,
                animationSpec = tween(
                    durationMillis = 180,
                    delayMillis = index * 16,
                    easing = fastAccelerate
                ),
                label = "charOffsetY_$index"
            )

            val charAlpha by animateFloatAsState(
                targetValue = if (isTransferring) 0f else 1f,
                animationSpec = tween(
                    durationMillis = 140,
                    delayMillis = index * 16,
                    easing = LinearEasing
                ),
                label = "charAlpha_$index"
            )

            val charScale by animateFloatAsState(
                targetValue = if (isTransferring) 0.80f else 1f,
                animationSpec = tween(
                    durationMillis = 180,
                    delayMillis = index * 16,
                    easing = fastAccelerate
                ),
                label = "charScale_$index"
            )

            Text(
                text = char.toString(),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF0F0A1C)
                ),
                modifier = Modifier.graphicsLayer {
                    translationY = charOffsetY.toPx()
                    alpha = charAlpha
                    scaleX = charScale
                    scaleY = charScale
                }
            )
        }
    }
}

/**
 * Premium Solid Dart Arrow with Dynamic Back-Side Thrust Wake Trails
 */
@Composable
private fun TransferDartArrowWithThruster(
    color: Color,
    isTransferring: Boolean,
    modifier: Modifier = Modifier
) {
    val smoothDecel = remember { CubicBezierEasing(0.16f, 1f, 0.3f, 1f) }

    // Dynamic thruster wake trail stretch & fade
    val trailProgress by animateFloatAsState(
        targetValue = if (isTransferring) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = smoothDecel),
        label = "trailProgress"
    )

    val trailAlpha by animateFloatAsState(
        targetValue = if (isTransferring) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = LinearEasing),
        label = "trailAlpha"
    )

    Canvas(modifier = modifier.size(32.dp, 16.dp)) {
        val dartWidth = 15.dp.toPx()
        val dartHeight = 13.dp.toPx()
        val dartLeft = size.width - dartWidth
        val dartTop = (size.height - dartHeight) / 2f
        val dartCenterY = size.height / 2f

        val notchX = dartLeft + dartWidth * 0.36f
        val topWingX = dartLeft + dartWidth * 0.05f
        val topWingY = dartTop + dartHeight * 0.06f
        val botWingX = dartLeft + dartWidth * 0.05f
        val botWingY = dartTop + dartHeight * 0.94f

        // Draw animated rear supersonic thrust wake & particles when transferring
        if (trailProgress > 0.01f && trailAlpha > 0.01f) {
            val maxTrailPx = 14.dp.toPx() * trailProgress
            val strokeWidth = 1.6.dp.toPx()

            // Center main thruster jet streak
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.5f * trailAlpha),
                        color.copy(alpha = 0.85f * trailAlpha)
                    ),
                    startX = (notchX - maxTrailPx).coerceAtLeast(0f),
                    endX = notchX
                ),
                start = Offset((notchX - maxTrailPx).coerceAtLeast(0f), dartCenterY),
                end = Offset(notchX, dartCenterY),
                strokeWidth = strokeWidth * 1.25f,
                cap = StrokeCap.Round
            )

            // Top wing vortex trail
            val topTrailPx = maxTrailPx * 0.65f
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.35f * trailAlpha),
                        color.copy(alpha = 0.7f * trailAlpha)
                    ),
                    startX = (topWingX - topTrailPx).coerceAtLeast(0f),
                    endX = topWingX
                ),
                start = Offset((topWingX - topTrailPx).coerceAtLeast(0f), topWingY),
                end = Offset(topWingX, topWingY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom wing vortex trail
            val botTrailPx = maxTrailPx * 0.65f
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.35f * trailAlpha),
                        color.copy(alpha = 0.7f * trailAlpha)
                    ),
                    startX = (botWingX - botTrailPx).coerceAtLeast(0f),
                    endX = botWingX
                ),
                start = Offset((botWingX - botTrailPx).coerceAtLeast(0f), botWingY),
                end = Offset(botWingX, botWingY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Dynamic mini exhaust spark particles
            val spark1X = notchX - (maxTrailPx * 0.45f)
            if (spark1X > 0f) {
                drawCircle(
                    color = color.copy(alpha = 0.65f * trailAlpha),
                    radius = 1.1.dp.toPx(),
                    center = Offset(spark1X, dartCenterY)
                )
            }
            val spark2X = notchX - (maxTrailPx * 0.8f)
            if (spark2X > 0f) {
                drawCircle(
                    color = color.copy(alpha = 0.35f * trailAlpha),
                    radius = 0.85.dp.toPx(),
                    center = Offset(spark2X, dartCenterY)
                )
            }
        }

        // Draw solid stealth dart arrow matching reference geometry
        val dartPath = Path().apply {
            moveTo(dartLeft + dartWidth * 0.95f, dartCenterY)
            lineTo(topWingX, topWingY)
            lineTo(notchX, dartCenterY)
            lineTo(botWingX, botWingY)
            close()
        }
        drawPath(
            path = dartPath,
            color = color
        )
    }
}
