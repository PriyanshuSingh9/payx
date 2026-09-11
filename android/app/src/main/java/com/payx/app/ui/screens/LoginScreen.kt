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
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.abs
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
            // Top Bar: PayX Logo & Wordmark
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

            // Center Dynamic Animated Spinning Globe & Active Components Cluster
            PayxAnimatedGlobeCluster(
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

            // Bottom Action Area (Only Continue with Google)
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
 * Animated 3D Spinning Globe Hero Cluster with internally reactive micro-animated components
 */
@Composable
private fun PayxAnimatedGlobeCluster(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "globe_cluster_master")
    val density = LocalDensity.current

    // 1. Globe 3D Longitude Rotation (Continuous 0f to 1f)
    val globeRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "globeRotation"
    )

    // 2. Flight Arc Beam Travel (0f to 1f)
    val beamProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "beamProgress"
    )

    // 3. Gyroscope Trajectory Ring Rotation
    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7500, easing = LinearEasing), RepeatMode.Restart),
        label = "orbitAngle"
    )

    // 4. Genuine 3D Coin Rotation on Y-Axis (0 to 360 degrees)
    val coin3DY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "coin3DY"
    )

    // 5. Lightning High-Voltage Crackle & Pulse
    val lightningIntensity by transition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lightningIntensity"
    )

    // 6. Sonar Security Radar Wave on Escrow Vault (0f to 1f)
    val radarPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "radarPulse"
    )

    // 7. Shimmer Sweep on Live Corridor
    val shimmerOffset by transition.animateFloat(
        initialValue = -100f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerOffset"
    )

    // 8. UPI Velocity Pulse & Lightning Shift
    val upiPulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "upiPulse"
    )

    // Float offsets for organic natural drift
    val float1 by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float1"
    )
    val float2 by transition.animateFloat(
        initialValue = 9f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(tween(3100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float2"
    )
    val float3 by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(2900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float3"
    )

    // Twinkle scale
    val sparkleScale by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sparkleScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // LAYER 0: Ambient Radial Glow behind the Globe
        Canvas(modifier = Modifier.size(260.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB55CF8).copy(alpha = 0.38f),
                        Color(0xFF38BDF8).copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                radius = size.width / 2f
            )
        }

        // LAYER 1: Orbital Trajectory Rings with Traveling Energy Satellite
        Canvas(
            modifier = Modifier
                .size(230.dp)
                .rotate(orbitAngle)
        ) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

            // Outer Dashed Trajectory
            drawOval(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFB55CF8).copy(alpha = 0.70f),
                        Color(0xFF38BDF8).copy(alpha = 0.20f),
                        Color(0xFFB55CF8).copy(alpha = 0.60f)
                    )
                ),
                style = Stroke(width = 1.6.dp.toPx(), pathEffect = dashEffect)
            )

            // Traveling Energy Node on the Ring
            val rad = orbitAngle * (PI / 180f).toFloat()
            val rx = size.width / 2f
            val ry = size.height / 2f
            val px = rx + rx * cos(rad)
            val py = ry + ry * sin(rad)

            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = Offset(px, py))
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 8.dp.toPx(),
                center = Offset(px, py),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // LAYER 2: THE ANIMATED 3D SPINNING HOLOGRAPHIC GLOBE
        Box(
            modifier = Modifier
                .size(142.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = Color(0x99B55CF8),
                    ambientColor = Color(0x44B55CF8)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF281A42),
                            Color(0xFF140D24),
                            Color(0xFF090610)
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
                                Color(0xFF34D399),
                                Color(0xFFB55CF8)
                            )
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            SpinningHolographicGlobeCanvas(
                globeRotation = globeRotation,
                beamProgress = beamProgress,
                modifier = Modifier.fillMaxSize()
            )
        }

        // LAYER 3: INTERNALLY REACTIVE & CRAZY ANIMATED COMPONENTS

        // 1. Top-Right: Live Corridor Capsule with Active Shimmer & Sliding Arrows
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 76.dp, y = (-86).dp + float1.dp)
        ) {
            ReactiveCorridorCapsule(shimmerOffset = shimmerOffset)
        }

        // 2. Top-Left: High-Voltage Lightning Prism that crackles and pulses
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-84).dp, y = (-82).dp + float2.dp)
        ) {
            HighVoltageLightningPrism(intensity = lightningIntensity)
        }

        // 3. Mid-Right: Escrow Security Vault Tag with Expanding Sonar Radar Ping
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 96.dp, y = (-4).dp + float3.dp)
        ) {
            ActiveEscrowVaultTag(sonarProgress = radarPulse)
        }

        // 4. Bottom-Right: Genuine 3D Spinning Minted Coin (spins 360° on Y-axis)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 68.dp, y = 78.dp + float2.dp)
                .graphicsLayer {
                    rotationY = coin3DY
                    cameraDistance = 12f * density.density
                }
        ) {
            Spinning3DCoin(rotationY = coin3DY)
        }

        // 5. Bottom-Left: Re-engineered UPI Velocity Hub with Speed Flash
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-70).dp, y = 80.dp + float1.dp)
                .scale(upiPulse)
        ) {
            UpiVelocityHub()
        }

        // 6. Prismatic Twinkling Diamond Accents
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-14).dp, y = (-106).dp)
                .scale(sparkleScale)
        ) {
            PrismDiamond(color = Color(0xFFC084FC), size = 16.dp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 106.dp, y = 54.dp)
                .scale(2f - sparkleScale)
        ) {
            PrismDiamond(color = Color(0xFF38BDF8), size = 13.dp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-104).dp, y = (-36).dp)
                .scale(sparkleScale * 0.85f)
        ) {
            PrismDiamond(color = Color(0xFFFBBF24), size = 11.dp)
        }
    }
}

/**
 * 3D Spinning Holographic Wireframe Globe with cross-border remittance beam
 */
@Composable
private fun SpinningHolographicGlobeCanvas(
    globeRotation: Float,
    beamProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w / 2f) * 0.92f

        // Ambient edge fresnel glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF38BDF8).copy(alpha = 0.08f),
                    Color(0xFFB55CF8).copy(alpha = 0.35f)
                ),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // Equator
        drawLine(
            color = Color(0xFF38BDF8).copy(alpha = 0.35f),
            start = Offset(cx - r, cy),
            end = Offset(cx + r, cy),
            strokeWidth = 1.2.dp.toPx()
        )

        // Latitudes (Horizontal parallels)
        val latRatios = listOf(0.40f, 0.72f)
        for (ratio in latRatios) {
            val yOffset = r * ratio
            val parallelHalfW = (r * r - yOffset * yOffset).let { if (it > 0) kotlin.math.sqrt(it) else 0f }

            // North parallel
            drawOval(
                color = Color(0xFFB55CF8).copy(alpha = 0.25f),
                topLeft = Offset(cx - parallelHalfW, cy - yOffset - 1.5.dp.toPx()),
                size = Size(parallelHalfW * 2f, 3.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            // South parallel
            drawOval(
                color = Color(0xFFB55CF8).copy(alpha = 0.25f),
                topLeft = Offset(cx - parallelHalfW, cy + yOffset - 1.5.dp.toPx()),
                size = Size(parallelHalfW * 2f, 3.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Longitudes: Rotating 3D Meridians
        val meridianCount = 6
        for (i in 0 until meridianCount) {
            val phase = ((i.toFloat() / meridianCount.toFloat()) + globeRotation) % 1f
            val angleRad = phase * 2f * PI
            val cosVal = cos(angleRad).toFloat()
            val sinVal = sin(angleRad).toFloat()

            // Only draw lines with proper depth: higher alpha on front hemisphere (sinVal > 0)
            val isFront = sinVal >= 0f
            val alpha = if (isFront) 0.55f else 0.15f
            val meridianW = abs(cosVal) * r

            drawOval(
                color = if (isFront) Color(0xFF38BDF8).copy(alpha = alpha) else Color(0xFFB55CF8).copy(alpha = alpha),
                topLeft = Offset(cx - meridianW, cy - r),
                size = Size(meridianW * 2f, r * 2f),
                style = Stroke(width = if (isFront) 1.2.dp.toPx() else 0.8.dp.toPx())
            )
        }

        // Cross-Border Active Remittance Flight Beam: USA (North-West) to India (South-East)
        val pStart = Offset(cx - r * 0.46f, cy - r * 0.28f) // North America node
        val pEnd = Offset(cx + r * 0.44f, cy + r * 0.22f)   // India node
        val pControl = Offset(cx, cy - r * 0.82f)          // High atmospheric orbit arc

        val flightPath = Path().apply {
            moveTo(pStart.x, pStart.y)
            quadraticTo(pControl.x, pControl.y, pEnd.x, pEnd.y)
        }

        // Background Flight Track (dashed cyan/purple)
        drawPath(
            path = flightPath,
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF34D399).copy(alpha = 0.7f))
            ),
            style = Stroke(
                width = 1.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                cap = StrokeCap.Round
            )
        )

        // Traveling Energy Photon Beam along Quadratic Bezier
        val t = beamProgress
        val bx = (1 - t) * (1 - t) * pStart.x + 2 * (1 - t) * t * pControl.x + t * t * pEnd.x
        val by = (1 - t) * (1 - t) * pStart.y + 2 * (1 - t) * t * pControl.y + t * t * pEnd.y
        val beamCenter = Offset(bx, by)

        // Trailing glow
        drawCircle(
            color = Color(0xFF34D399).copy(alpha = 0.4f),
            radius = 6.dp.toPx(),
            center = beamCenter
        )
        drawCircle(
            color = Color.White,
            radius = 2.8.dp.toPx(),
            center = beamCenter
        )

        // USA Node Beacon
        drawCircle(color = Color(0xFF38BDF8), radius = 3.2.dp.toPx(), center = pStart)
        drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.4f), radius = 6.dp.toPx(), center = pStart, style = Stroke(1.dp.toPx()))

        // India Destination Pulse Radar
        drawCircle(color = Color(0xFF34D399), radius = 3.6.dp.toPx(), center = pEnd)
        val pingR = 4.dp.toPx() + (beamProgress * 12.dp.toPx())
        val pingAlpha = (1f - beamProgress).coerceIn(0f, 1f)
        drawCircle(
            color = Color(0xFF34D399).copy(alpha = pingAlpha * 0.7f),
            radius = pingR,
            center = pEnd,
            style = Stroke(1.2.dp.toPx())
        )
    }
}

/**
 * 1. Live Corridor Capsule with sweep shimmer
 */
@Composable
private fun ReactiveCorridorCapsule(shimmerOffset: Float) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1B132A),
        border = BorderStroke(1.dp, Color(0xFF4C3672)),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
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

            // Shimmer highlight effect
            Canvas(modifier = Modifier.matchParentSize()) {
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerOffset, 0f),
                    end = Offset(shimmerOffset + 40.dp.toPx(), size.height)
                )
                drawRect(brush = shimmerBrush)
            }
        }
    }
}

/**
 * 2. High-Voltage Crackling Lightning Prism
 */
@Composable
private fun HighVoltageLightningPrism(intensity: Float) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(intensity)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF261942), Color(0xFF150D26))
                )
            )
            .border(
                1.dp,
                Color(0xFFF59E0B).copy(alpha = (intensity - 0.7f).coerceIn(0.4f, 1f)),
                RoundedCornerShape(14.dp)
            ),
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
 * 3. Escrow Vault Tag with expanding sonar radar ping
 */
@Composable
private fun ActiveEscrowVaultTag(sonarProgress: Float) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1C142B),
        border = BorderStroke(1.dp, Color(0xFF3F2B60)),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(11.dp)
                    )

                    // Sonar Radar Ring
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val maxR = size.width / 2f
                        val currR = maxR * sonarProgress
                        val alpha = (1f - sonarProgress).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFFC084FC).copy(alpha = alpha * 0.6f),
                            radius = currR,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

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
}

/**
 * 4. Genuine 3D Spinning Remittance Coin with specular shine
 */
@Composable
private fun Spinning3DCoin(rotationY: Float) {
    // Face angle: when cos(angle) > 0 front ($), else back (₹)
    val rad = rotationY * (PI / 180f).toFloat()
    val isFront = cos(rad) >= 0f

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFDE047),
                        Color(0xFFF59E0B),
                        Color(0xFFB45309)
                    )
                )
            )
            .border(1.5.dp, Color(0xFFFEF3C7), CircleShape)
            .shadow(12.dp, CircleShape, spotColor = Color(0x99F59E0B)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFront) "$" else "₹",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF451A03)
            )
        )
    }
}

/**
 * 5. Re-engineered Luxury UPI Velocity Hub
 */
@Composable
private fun UpiVelocityHub() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF121F24),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(Color(0xFF10B981), Color(0xFF38BDF8))
            )
        ),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Speed Lightning Glyphs
            Canvas(modifier = Modifier.size(10.dp, 12.dp)) {
                val bolt = Path().apply {
                    moveTo(size.width * 0.65f, 0f)
                    lineTo(size.width * 0.15f, size.height * 0.55f)
                    lineTo(size.width * 0.55f, size.height * 0.55f)
                    lineTo(size.width * 0.35f, size.height)
                    lineTo(size.width * 0.85f, size.height * 0.45f)
                    lineTo(size.width * 0.50f, size.height * 0.45f)
                    close()
                }
                drawPath(bolt, color = Color(0xFF34D399))
            }

            Text(
                text = "UPI RAIL",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    color = Color.White
                )
            )

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
 * Prismatic Diamond Sparkle
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
