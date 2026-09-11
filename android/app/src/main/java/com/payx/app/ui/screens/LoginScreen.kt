package com.payx.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import com.payx.app.ui.theme.PayxPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
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
        // Ambient background topography curves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val purpleGlow = Color(0x18B866FC)
            val path1 = Path().apply {
                moveTo(0f, size.height * 0.16f)
                cubicTo(
                    size.width * 0.35f, size.height * 0.10f,
                    size.width * 0.7f, size.height * 0.24f,
                    size.width, size.height * 0.18f
                )
            }
            drawPath(path1, color = purpleGlow, style = Stroke(width = 1.5.dp.toPx()))

            val path2 = Path().apply {
                moveTo(0f, size.height * 0.42f)
                cubicTo(
                    size.width * 0.4f, size.height * 0.48f,
                    size.width * 0.65f, size.height * 0.36f,
                    size.width, size.height * 0.44f
                )
            }
            drawPath(path2, color = Color(0x10A855F7), style = Stroke(width = 1.2.dp.toPx()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with PayX wordmark & logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PayxMiniLogo()
                    Text(
                        text = "payx",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = Color.White
                        )
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = PayxPalette.TextTertiary
                    )
                }
            }

            // Center Dynamic Animated Sticker Cluster
            AnimatedHeroCluster(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            )

            // Headline & Information
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trust pill badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1630),
                    border = BorderStroke(1.dp, Color(0xFF352752)),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingDot()
                        Text(
                            text = "Backed by Solana Escrow & Neon EVM",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.4.sp,
                                color = PayxPalette.SoftLavender
                            )
                        )
                    }
                }

                // Bold Headline
                Text(
                    text = "Global Wealth,\nSeamlessly Curated.",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 36.sp,
                        lineHeight = 42.sp,
                        letterSpacing = (-0.6).sp,
                        color = PayxPalette.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                Text(
                    text = "Experience cross-border remittance with instant settlement and non-custodial cryptographic guarantees.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = PayxPalette.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Bottom Actions Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Terms of Service
                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextTertiary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Continue with Google Button
                val buttonGradient = Brush.horizontalGradient(
                    listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                )

                Button(
                    onClick = onSignedIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(27.dp),
                            spotColor = Color(0x66A855F7),
                            ambientColor = Color(0x33A855F7)
                        ),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(buttonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GoogleWhiteGlyph(modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.2).sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Connect Wallet / Secondary Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1B1528))
                        .border(1.dp, Color(0xFF2E2345), RoundedCornerShape(24.dp))
                        .clickable(onClick = onSignedIn),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Connect Solana Wallet",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PayxPalette.SoftLavender
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

/**
 * Animated Sticker Cluster with multiple floating micro-animated elements
 */
@Composable
private fun AnimatedHeroCluster(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hero_cluster")

    // Micro-animation floating offsets with varied durations and phase shifts
    val floatY1 by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY1"
    )
    val floatY2 by transition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY2"
    )
    val floatY3 by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY3"
    )
    val floatY4 by transition.animateFloat(
        initialValue = 7f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY4"
    )
    val floatY5 by transition.animateFloat(
        initialValue = -9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY5"
    )

    // Rotations & pulses
    val starRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "starRotation"
    )
    val centerScale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "centerScale"
    )
    val rotSway1 by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotSway1"
    )
    val rotSway2 by transition.animateFloat(
        initialValue = 6f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotSway2"
    )
    val auraGlow by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "auraGlow"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Ambient Radial Glow behind the central hub
        Canvas(modifier = Modifier.size(240.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.35f * auraGlow),
                        Color(0xFF6B21A8).copy(alpha = 0.15f * auraGlow),
                        Color.Transparent
                    )
                ),
                radius = size.width / 2f
            )
        }

        // 1. Central Hero Orb: Dark Obsidian with Glowing Surge Remittance Arrow
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(centerScale)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    spotColor = Color(0x88A855F7),
                    ambientColor = Color(0x44A855F7)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E1733), Color(0xFF120E1E))
                    )
                )
                .border(2.dp, Color(0x66B55CF8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CentralGrowthSurgeCanvas(modifier = Modifier.size(110.dp))
        }

        // 2. Top-Left: Playful PayX Mascot / Ghost shape
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-64).dp, y = (-78).dp + floatY1.dp)
                .rotate(rotSway1)
        ) {
            PayxGhostMascot(modifier = Modifier.size(54.dp, 60.dp))
        }

        // 3. Top-Right: 8-Pointed Golden Amber Starburst
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 68.dp, y = (-74).dp + floatY2.dp)
                .rotate(starRotation)
        ) {
            AmberStarburst(modifier = Modifier.size(46.dp))
        }

        // 4. Top-Right / Mid-Right: Solana 3-Bar Gradient Badge
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 88.dp, y = (-18).dp + floatY3.dp)
                .rotate(rotSway2)
        ) {
            SolanaBadge(modifier = Modifier.size(44.dp))
        }

        // 5. Far Right: Financial Candlesticks Capsule
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 82.dp, y = 42.dp + floatY4.dp)
        ) {
            CandlestickCapsule(modifier = Modifier.size(46.dp, 46.dp))
        }

        // 6. Bottom-Right: UPI Instant Settlement Speech Bubble
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 36.dp, y = 84.dp + floatY1.dp)
        ) {
            UpiChatBubble(modifier = Modifier.size(58.dp, 40.dp))
        }

        // 7. Bottom-Left: Indian Rupee Coin Token
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-56).dp, y = 76.dp + floatY5.dp)
                .rotate(-rotSway1)
        ) {
            CurrencyCoinBadge(symbol = "₹", color = Color(0xFF10B981), modifier = Modifier.size(48.dp))
        }

        // 8. Mid-Left: USD Dollar Coin Token
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-88).dp, y = (-14).dp + floatY4.dp)
                .rotate(rotSway2)
        ) {
            CurrencyCoinBadge(symbol = "$", color = Color(0xFF38BDF8), modifier = Modifier.size(40.dp))
        }

        // 9. Floating Little Coral Mini-Star Accent (Upper Left)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-98).dp, y = (-62).dp + floatY2.dp)
                .rotate(-starRotation * 0.7f)
        ) {
            MiniStar(color = Color(0xFFFB7185), size = 18.dp)
        }

        // 10. Floating Small Google G Coin (Lower Center)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-8).dp, y = 82.dp + floatY3.dp)
        ) {
            MiniGoogleCoin(modifier = Modifier.size(28.dp))
        }
    }
}

/**
 * Center Orb Canvas: Glowing upward remittance growth arrow
 */
@Composable
private fun CentralGrowthSurgeCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Remittance growth surge line (upward trend curve)
        val surgePath = Path().apply {
            moveTo(w * 0.18f, h * 0.72f)
            lineTo(w * 0.32f, h * 0.58f)
            lineTo(w * 0.44f, h * 0.68f)
            lineTo(w * 0.78f, h * 0.30f)
        }

        // Outer glow path
        drawPath(
            path = surgePath,
            color = Color(0x66A855F7),
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
        )

        // Bright electric purple-to-teal stroke
        drawPath(
            path = surgePath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB55CF8),
                    Color(0xFF34D399),
                    Color(0xFF10B981)
                )
            ),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Arrow head pointing upward-right
        val arrowHead = Path().apply {
            moveTo(w * 0.58f, h * 0.30f)
            lineTo(w * 0.82f, h * 0.28f)
            lineTo(w * 0.80f, h * 0.52f)
        }
        drawPath(
            path = arrowHead,
            color = Color(0xFF10B981),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Playful Mascot/Ghost shape in electric violet
 */
@Composable
private fun PayxGhostMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val ghostPath = Path().apply {
            moveTo(w * 0.15f, h * 0.90f)
            cubicTo(w * 0.10f, h * 0.40f, w * 0.25f, h * 0.05f, w * 0.60f, h * 0.05f)
            cubicTo(w * 0.95f, h * 0.05f, w * 0.95f, h * 0.50f, w * 0.85f, h * 0.90f)
            cubicTo(w * 0.70f, h * 0.80f, w * 0.55f, h * 0.95f, w * 0.40f, h * 0.85f)
            cubicTo(w * 0.28f, h * 0.95f, w * 0.18f, h * 0.85f, w * 0.15f, h * 0.90f)
            close()
        }

        drawPath(
            path = ghostPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFC084FC), Color(0xFF8B5CF6))
            )
        )

        // Playful cartoon eyes
        val eyeRadius = w * 0.08f
        drawCircle(
            color = Color(0xFF1E1436),
            radius = eyeRadius,
            center = Offset(w * 0.52f, h * 0.38f)
        )
        drawCircle(
            color = Color(0xFF1E1436),
            radius = eyeRadius,
            center = Offset(w * 0.74f, h * 0.36f)
        )
    }
}

/**
 * 8-Pointed Golden Amber Starburst
 */
@Composable
private fun AmberStarburst(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.width / 2f
        val innerR = outerR * 0.42f
        val points = 8

        val path = Path()
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * PI / points).toFloat()
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
            )
        )
    }
}

/**
 * Solana 3-Bar Gradient Badge
 */
@Composable
private fun SolanaBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF1A1428))
            .border(1.dp, Color(0xFF382A56), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val barH = size.height * 0.16f
            val barW = size.width * 0.75f
            val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            val solanaBrush = Brush.linearGradient(
                listOf(Color(0xFF9945FF), Color(0xFF14F195))
            )

            // Top bar
            drawRoundRect(
                brush = solanaBrush,
                topLeft = Offset(size.width * 0.12f, size.height * 0.18f),
                size = Size(barW, barH),
                cornerRadius = corner
            )

            // Mid bar
            drawRoundRect(
                brush = solanaBrush,
                topLeft = Offset(size.width * 0.18f, size.height * 0.42f),
                size = Size(barW, barH),
                cornerRadius = corner
            )

            // Bottom bar
            drawRoundRect(
                brush = solanaBrush,
                topLeft = Offset(size.width * 0.12f, size.height * 0.66f),
                size = Size(barW, barH),
                cornerRadius = corner
            )
        }
    }
}

/**
 * Mini Financial Candlestick Capsule
 */
@Composable
private fun CandlestickCapsule(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF191326))
            .border(1.dp, Color(0xFF32264C), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val stroke = 1.5.dp.toPx()

            // Left orange candlestick
            drawLine(
                color = Color(0xFFF97316),
                start = Offset(size.width * 0.35f, size.height * 0.20f),
                end = Offset(size.width * 0.35f, size.height * 0.80f),
                strokeWidth = stroke
            )
            drawRoundRect(
                color = Color(0xFFF97316),
                topLeft = Offset(size.width * 0.24f, size.height * 0.34f),
                size = Size(size.width * 0.22f, size.height * 0.36f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Right emerald candlestick
            drawLine(
                color = Color(0xFF10B981),
                start = Offset(size.width * 0.72f, size.height * 0.15f),
                end = Offset(size.width * 0.72f, size.height * 0.75f),
                strokeWidth = stroke
            )
            drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(size.width * 0.61f, size.height * 0.26f),
                size = Size(size.width * 0.22f, size.height * 0.38f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/**
 * UPI Instant Settlement Speech Bubble in Emerald / Cyan
 */
@Composable
private fun UpiChatBubble(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF059669), Color(0xFF10B981))
                )
            )
            .border(1.dp, Color(0x6634D399), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "UPI",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            )
            Canvas(modifier = Modifier.size(9.dp, 12.dp)) {
                val bolt = Path().apply {
                    moveTo(size.width * 0.65f, 0f)
                    lineTo(size.width * 0.15f, size.height * 0.55f)
                    lineTo(size.width * 0.55f, size.height * 0.55f)
                    lineTo(size.width * 0.35f, size.height)
                    lineTo(size.width * 0.85f, size.height * 0.45f)
                    lineTo(size.width * 0.50f, size.height * 0.45f)
                    close()
                }
                drawPath(bolt, color = Color.White)
            }
        }
    }
}

/**
 * Round Currency Coin Token (USD or INR)
 */
@Composable
private fun CurrencyCoinBadge(
    symbol: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF261D3B), Color(0xFF140F22))
                )
            )
            .border(1.dp, color.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

/**
 * Mini 4-point Star Accent
 */
@Composable
private fun MiniStar(color: Color, size: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.width / 2f
        val innerR = r * 0.3f

        val path = Path()
        for (i in 0 until 8) {
            val currentR = if (i % 2 == 0) r else innerR
            val angle = (i * PI / 4).toFloat()
            val x = center.x + currentR * cos(angle)
            val y = center.y + currentR * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(path, color = color)
    }
}

/**
 * Mini Google Coin Badge
 */
@Composable
private fun MiniGoogleCoin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF241C38))
            .border(1.dp, Color(0xFF4C3872), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        GoogleWhiteGlyph(modifier = Modifier.size(14.dp))
    }
}

/**
 * Pulsing Green/Purple Live Dot
 */
@Composable
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "pulse_dot")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(PayxPalette.VividPurple.copy(alpha = alpha))
    )
}

@Composable
private fun PayxMiniLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF221838))
            .border(1.dp, Color(0xFF3E2D5E), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val neonPurple = Color(0xFFD8B4FE)
            val strokeWidth = 2.dp.toPx()

            drawRoundRect(
                color = neonPurple,
                topLeft = Offset(size.width * 0.10f, size.height * 0.25f),
                size = Size(size.width * 0.80f, size.height * 0.60f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Right flap
            val flapW = size.width * 0.36f
            val flapH = size.height * 0.26f
            val flapL = size.width * 0.54f
            val flapT = size.height * 0.42f

            drawRoundRect(
                color = Color(0xFF221838),
                topLeft = Offset(flapL, flapT),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            drawRoundRect(
                color = neonPurple,
                topLeft = Offset(flapL, flapT),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

@Composable
private fun GoogleWhiteGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.8.dp.toPx()
        val r = (size.minDimension - stroke) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawArc(
            color = Color.White,
            startAngle = 40f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color.White,
            start = Offset(center.x - 0.5.dp.toPx(), center.y),
            end = Offset(center.x + r, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
