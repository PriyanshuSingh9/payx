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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
            0.0f to Color(0xFF181226),
            0.35f to Color(0xFF100D18),
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
                moveTo(0f, size.height * 0.44f)
                cubicTo(
                    size.width * 0.4f, size.height * 0.50f,
                    size.width * 0.65f, size.height * 0.38f,
                    size.width, size.height * 0.46f
                )
            }
            drawPath(path2, color = Color(0x10A855F7), style = Stroke(width = 1.2.dp.toPx()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Premium PayX Logo & Wordmark
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
                    PayxHeaderLogo()
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PAY",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "X",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    brush = Brush.horizontalGradient(
                                        listOf(PayxPalette.VividPurple, PayxPalette.SoftLavender)
                                    )
                                )
                            )
                        }
                    }
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

            // Crazy Animated PayX Holographic Escrow & Remittance Cluster
            PayxHolographicCluster(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            )

            // Luxury Headings Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Global Wealth,\nSeamlessly Curated.",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 38.sp,
                        lineHeight = 44.sp,
                        letterSpacing = (-0.8).sp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF3E8FF),
                                Color(0xFFD8B4FE)
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Instant non-custodial remittance protocol powered by on-chain escrow custody and automated fiat settlement.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = PayxPalette.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }

            // Bottom Action Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextTertiary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Continue with Google Button
                val buttonGradient = Brush.horizontalGradient(
                    listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                )

                Button(
                    onClick = onSignedIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0x66A855F7),
                            ambientColor = Color(0x33A855F7)
                        ),
                    shape = RoundedCornerShape(28.dp),
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
                            GoogleWhiteGlyph(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.2).sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Unique PayX Holographic Remittance Cluster with crazy multi-layered micro-animations
 */
@Composable
private fun PayxHolographicCluster(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "payx_crazy_cluster")

    // Continuous 360° Orbital Rotations
    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbitAngle"
    )
    val counterAngle by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "counterAngle"
    )

    // Breathing Pulses & Halos
    val coreScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "coreScale"
    )
    val glowIntensity by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowIntensity"
    )

    // Independent sinusoidal floating offsets for orbiting components
    val float1 by transition.animateFloat(
        initialValue = -9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float1"
    )
    val float2 by transition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(3100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float2"
    )
    val float3 by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float3"
    )
    val float4 by transition.animateFloat(
        initialValue = 7f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(tween(3400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float4"
    )

    // 3D Tilts & Sway
    val tilt3D_1 by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tilt3D_1"
    )
    val tilt3D_2 by transition.animateFloat(
        initialValue = 9f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tilt3D_2"
    )

    // Twinkle & shimmer
    val sparkleScale by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.30f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sparkleScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // LAYER 0: Ambient Radial Aura Behind Center Core
        Canvas(modifier = Modifier.size(260.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB55CF8).copy(alpha = 0.32f * glowIntensity),
                        Color(0xFF7E2AE8).copy(alpha = 0.15f * glowIntensity),
                        Color.Transparent
                    )
                ),
                radius = size.width / 2f
            )
        }

        // LAYER 1: Dual Tilted Dashed Orbital Gyroscope Rings with Traveling Particles
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .rotate(orbitAngle)
        ) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

            // Orbital Ellipse 1 (tilted 35 deg)
            drawOval(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFB55CF8).copy(alpha = 0.60f),
                        Color(0xFF38BDF8).copy(alpha = 0.15f),
                        Color(0xFFB55CF8).copy(alpha = 0.50f)
                    )
                ),
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashEffect)
            )

            // Traveling Escrow Energy Particle on Orbit 1
            val rad = orbitAngle * (PI / 180f).toFloat()
            val rx = size.width / 2f
            val ry = size.height / 2f
            val px = rx + rx * cos(rad)
            val py = ry + ry * sin(rad)

            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(px, py)
            )
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 7.dp.toPx(),
                center = Offset(px, py),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Canvas(
            modifier = Modifier
                .size(190.dp)
                .rotate(counterAngle)
        ) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
            drawOval(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.50f),
                        Color(0xFFB55CF8).copy(alpha = 0.20f),
                        Color(0xFF38BDF8).copy(alpha = 0.40f)
                    )
                ),
                style = Stroke(width = 1.2.dp.toPx(), pathEffect = dashEffect)
            )
        }

        // LAYER 2: Central Escrow Holographic Core
        Box(
            modifier = Modifier
                .size(136.dp)
                .scale(coreScale)
                .shadow(
                    elevation = 22.dp,
                    shape = CircleShape,
                    spotColor = Color(0x99B55CF8),
                    ambientColor = Color(0x44B55CF8)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2B1D45),
                            Color(0xFF1A132C),
                            Color(0xFF0F0B18)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFFB55CF8),
                                Color(0xFF38BDF8),
                                Color(0xFF7E2AE8),
                                Color(0xFFB55CF8)
                            )
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inside Central Core: Holographic Escrow Shield with Interlocking Currency Glyphs
            HolographicShieldCore(glow = glowIntensity)
        }

        // LAYER 3: PAYX-EXCLUSIVE SURROUNDING REMITTANCE ELEMENTS

        // 1. Top-Right: Live Cross-Border Currency Corridor Capsule ($ ⇄ ₹)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 76.dp, y = (-84).dp + float1.dp)
                .rotate(tilt3D_1)
        ) {
            LiveCorridorPill()
        }

        // 2. Top-Left: Faceted Instant Remittance Lightning Prism
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-82).dp, y = (-80).dp + float2.dp)
                .rotate(tilt3D_2)
        ) {
            InstantSettlementPrism()
        }

        // 3. Mid-Right: On-Chain Escrow Custody Vault Tag
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 94.dp, y = (-2).dp + float3.dp)
                .rotate(-tilt3D_1 * 0.7f)
        ) {
            EscrowVaultTag()
        }

        // 4. Mid-Left: Cross-Border Luminous Route Globe
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-94).dp, y = 4.dp + float4.dp)
                .rotate(tilt3D_2 * 0.8f)
        ) {
            LuminousRouteGlobe()
        }

        // 5. Bottom-Right: 3D Minted Remittance Gold Coin ($/₹)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 64.dp, y = 78.dp + float2.dp)
                .graphicsLayer {
                    rotationY = tilt3D_1 * 2f
                }
        ) {
            MintedRemittanceCoin()
        }

        // 6. Bottom-Left: Instant UPI Fast Settlement Tag
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-66).dp, y = 80.dp + float1.dp)
                .rotate(-tilt3D_2 * 0.6f)
        ) {
            UpiFastTag()
        }

        // 7. Twinkling Prismatic Diamond Sparkles
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-12).dp, y = (-104).dp)
                .scale(sparkleScale)
        ) {
            PrismDiamond(color = Color(0xFFC084FC), size = 16.dp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 104.dp, y = 56.dp)
                .scale(2f - sparkleScale)
        ) {
            PrismDiamond(color = Color(0xFF38BDF8), size = 13.dp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-106).dp, y = (-38).dp)
                .scale(sparkleScale * 0.85f)
        ) {
            PrismDiamond(color = Color(0xFFFBBF24), size = 11.dp)
        }
    }
}

/**
 * Inside the central core: Holographic Escrow Shield with Interlocking Currency Glyphs
 */
@Composable
private fun HolographicShieldCore(glow: Float) {
    Canvas(modifier = Modifier.size(92.dp)) {
        val w = size.width
        val h = size.height

        // Shield Path
        val shieldPath = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            cubicTo(w * 0.78f, h * 0.12f, w * 0.88f, h * 0.20f, w * 0.88f, h * 0.44f)
            cubicTo(w * 0.88f, h * 0.72f, w * 0.68f, h * 0.88f, w * 0.50f, h * 0.96f)
            cubicTo(w * 0.32f, h * 0.88f, w * 0.12f, h * 0.72f, w * 0.12f, h * 0.44f)
            cubicTo(w * 0.12f, h * 0.20f, w * 0.22f, h * 0.12f, w * 0.50f, h * 0.12f)
            close()
        }

        // Outer glow on shield
        drawPath(
            path = shieldPath,
            color = Color(0xFFB55CF8).copy(alpha = 0.30f * glow),
            style = Stroke(width = 6.dp.toPx())
        )

        // Gradient Shield outline
        drawPath(
            path = shieldPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFD8B4FE), Color(0xFF7E2AE8))
            ),
            style = Stroke(width = 2.4.dp.toPx())
        )

        // Shield inner dark crystal fill
        drawPath(
            path = shieldPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3B2763).copy(alpha = 0.8f),
                    Color(0xFF1E1436).copy(alpha = 0.9f)
                ),
                center = Offset(w * 0.5f, h * 0.5f)
            )
        )

        // Dynamic Curvature Upward Currency Flow Path Inside Shield
        val flowLine = Path().apply {
            moveTo(w * 0.28f, h * 0.64f)
            cubicTo(w * 0.36f, h * 0.46f, w * 0.58f, h * 0.62f, w * 0.72f, h * 0.36f)
        }

        drawPath(
            path = flowLine,
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF38BDF8), Color(0xFFB55CF8))
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Arrow head pointing upward
        val arrow = Path().apply {
            moveTo(w * 0.58f, h * 0.36f)
            lineTo(w * 0.74f, h * 0.34f)
            lineTo(w * 0.72f, h * 0.50f)
        }
        drawPath(
            path = arrow,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Center Lock Keyhole Icon Accent
        drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.50f, h * 0.44f)
        )
        val keyholeBody = Path().apply {
            moveTo(w * 0.47f, h * 0.44f)
            lineTo(w * 0.45f, h * 0.56f)
            lineTo(w * 0.55f, h * 0.56f)
            lineTo(w * 0.53f, h * 0.44f)
            close()
        }
        drawPath(keyholeBody, color = Color.White)
    }
}

/**
 * 1. Live Cross-Border Corridor Pill (USD ⇄ INR • LIVE)
 */
@Composable
private fun LiveCorridorPill() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1E1530),
        border = BorderStroke(1.dp, Color(0xFF4C3672)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            UsaFlag(width = 14.dp, height = 10.dp)
            Text(
                text = "⇄",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = PayxPalette.SoftLavender,
                    fontWeight = FontWeight.Bold
                )
            )
            IndiaFlag(width = 14.dp, height = 10.dp)
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
            )
        }
    }
}

/**
 * 2. Faceted Instant Remittance Lightning Prism
 */
@Composable
private fun InstantSettlementPrism() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF261942), Color(0xFF150D26))
                )
            )
            .border(1.dp, Color(0xFF6B42A6), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height

            // Faceted Lightning Prism
            val boltLeft = Path().apply {
                moveTo(w * 0.55f, 0f)
                lineTo(w * 0.20f, h * 0.52f)
                lineTo(w * 0.48f, h * 0.52f)
                lineTo(w * 0.35f, h)
                close()
            }
            drawPath(
                path = boltLeft,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
                )
            )

            val boltRight = Path().apply {
                moveTo(w * 0.55f, 0f)
                lineTo(w * 0.48f, h * 0.52f)
                lineTo(w * 0.35f, h)
                lineTo(w * 0.85f, h * 0.45f)
                lineTo(w * 0.52f, h * 0.45f)
                close()
            }
            drawPath(
                path = boltRight,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                )
            )
        }
    }
}

/**
 * 3. On-Chain Escrow Custody Vault Tag
 */
@Composable
private fun EscrowVaultTag() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1C142B),
        border = BorderStroke(1.dp, Color(0xFF3F2B60)),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFC084FC),
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = "ESCROW",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Color.White
                )
            )
        }
    }
}

/**
 * 4. Cross-Border Luminous Route Globe
 */
@Composable
private fun LuminousRouteGlobe() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF22173B), Color(0xFF120C22))
                )
            )
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            val stroke = 1.dp.toPx()
            // Latitude lines
            drawOval(
                color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                style = Stroke(width = stroke)
            )
            drawOval(
                color = Color(0xFFB55CF8).copy(alpha = 0.5f),
                topLeft = Offset(size.width * 0.18f, 0f),
                size = Size(size.width * 0.64f, size.height),
                style = Stroke(width = stroke)
            )
            // Equator
            drawLine(
                color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = stroke
            )
            // Route arc
            val routeArc = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.4f)
                cubicTo(size.width * 0.5f, size.height * 0.15f, size.width * 0.65f, size.height * 0.35f, size.width * 0.8f, size.height * 0.6f)
            }
            drawPath(
                routeArc,
                color = Color.White,
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(color = Color(0xFF38BDF8), radius = 2.2.dp.toPx(), center = Offset(size.width * 0.2f, size.height * 0.4f))
            drawCircle(color = Color(0xFF10B981), radius = 2.2.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.6f))
        }
    }
}

/**
 * 5. 3D Minted Remittance Coin with dual currency glyphs
 */
@Composable
private fun MintedRemittanceCoin() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFF92400E))
                )
            )
            .border(1.5.dp, Color(0xFFFDE68A), CircleShape)
            .shadow(10.dp, CircleShape, spotColor = Color(0x88F59E0B)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF451A03)
                )
            )
            Text(
                text = "₹",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF78350F)
                )
            )
        }
    }
}

/**
 * 6. Instant UPI Fast Settlement Tag
 */
@Composable
private fun UpiFastTag() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF13251E),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
            )
            Text(
                text = "UPI SETTLE",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF34D399)
                )
            )
        }
    }
}

/**
 * 7. Prismatic Diamond Sparkle
 */
@Composable
private fun PrismDiamond(color: Color, size: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)

        val path = Path().apply {
            moveTo(center.x, 0f)
            lineTo(w, center.y)
            lineTo(center.x, h)
            lineTo(0f, center.y)
            close()
        }
        drawPath(path, color = color)
        drawCircle(color = Color.White, radius = w * 0.15f, center = center)
    }
}

/**
 * Premium PayX Top Header Logo
 */
@Composable
private fun PayxHeaderLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2E1C4E), Color(0xFF1B112E))
                )
            )
            .border(1.dp, Color(0xFF4C3078), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val neonPurple = Color(0xFFD8B4FE)
            val strokeWidth = 2.2.dp.toPx()

            drawRoundRect(
                color = neonPurple,
                topLeft = Offset(size.width * 0.10f, size.height * 0.22f),
                size = Size(size.width * 0.80f, size.height * 0.62f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Right flap
            val flapW = size.width * 0.36f
            val flapH = size.height * 0.28f
            val flapL = size.width * 0.54f
            val flapT = size.height * 0.39f

            drawRoundRect(
                color = Color(0xFF22153B),
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
