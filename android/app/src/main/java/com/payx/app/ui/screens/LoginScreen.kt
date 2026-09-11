package com.payx.app.ui.screens
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.R
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import com.payx.app.ui.theme.PayxPalette
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val MontaguSlab = FontFamily(
    Font(R.font.montagu_slab)
)

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans)
)

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    var selectedCorridor by remember { mutableStateOf("US") }

    val ambientBackground = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF17122E),
            0.35f to Color(0xFF0E0B1E),
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
            val purpleGlow = Color(0x18C4BAF9)
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
            drawPath(path2, color = Color(0x10AE9EF8), style = Stroke(width = 1.2.dp.toPx()))
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
            // 1. Top Bar: Clean PayX Logo & Wordmark (No 3 dots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PayxHeaderLogo()
                    Text(
                        text = "PayX",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            brush = Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.White,
                                    0.55f to Color(0xFFD0C4FC),
                                    1.0f to Color(0xFFAE9EF8)
                                )
                            )
                        )
                    )
                }
            }

            // 2. Center Dynamic Animated Spinning Globe & Active Components Cluster
            PayxAnimatedGlobeCluster(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            // 3. Luxury Headings Section — polished typography hierarchy
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Global Wealth,\nSeamlessly Curated.",
                    style = TextStyle(
                        fontFamily = MontaguSlab,
                        fontWeight = FontWeight.Normal,
                        fontSize = 34.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.6).sp,
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Instant non-custodial remittance protocol powered by on-chain custody and automated fiat settlement.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = PayxPalette.TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 4. Bottom Action Area: US/IND Switching Tab, Google Login Button, Legal Notice
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // US and IND Switching Tab above login
                CorridorSwitchTab(
                    selectedCorridor = selectedCorridor,
                    onCorridorSelected = { selectedCorridor = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            elevation = 14.dp,
                            shape = RoundedCornerShape(27.dp),
                            spotColor = Color(0x66AE9EF8),
                            ambientColor = Color(0x33AE9EF8)
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
                            GoogleOfficialLogo(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.2).sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legal Terms Disclaimer placed below button
                Text(
                    text = "By continuing, you agree to our Terms of Service & Privacy Policy.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextTertiary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
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
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "globeRotation"
    )

    // 2. Gyroscope Trajectory Ring Rotation
    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7500, easing = LinearEasing), RepeatMode.Restart),
        label = "orbitAngle"
    )

    // 3. Genuine 3D Coin Rotation on Y-Axis
    val coin3DY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "coin3DY"
    )

    // 4. Lightning crackle phase — smoothed so only lightning is adjusted
    val lightningPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "lightningPhase"
    )

    // 5. Lightning outer glow pulse
    val lightningGlow by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lightningGlow"
    )

    // 6. Sonar Security Radar Wave — two offset waves
    val radarPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "radarPulse"
    )

    // 7. Escrow scan bar — 0f to 1f sweeps top-to-bottom inside the tag
    val escrowScan by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "escrowScan"
    )

    // 8. Corridor data arrow sweep — 0f to 1f slides the animated arrow group
    val arrowSweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "arrowSweep"
    )

    // 9. Live dot pulse for corridor and UPI
    val liveDotPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "liveDotPulse"
    )

    // 10. UPI speed lines scroll — 0f to 1f
    val upiScroll by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Restart),
        label = "upiScroll"
    )

    // 11. Coin specular shine sweep
    val coinShine by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "coinShine"
    )

    // Float offsets for organic natural drift — each uses a unique CubicBezier easing and period
    val floatEase1 = androidx.compose.animation.core.CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
    val floatEase2 = androidx.compose.animation.core.CubicBezierEasing(0.45f, 0.05f, 0.55f, 0.95f)
    val floatEase3 = androidx.compose.animation.core.CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

    val float1 by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            tween(3200, easing = floatEase1), RepeatMode.Reverse
        ),
        label = "float1"
    )
    val float2 by transition.animateFloat(
        initialValue = 11f,
        targetValue = -11f,
        animationSpec = infiniteRepeatable(
            tween(2700, easing = floatEase2), RepeatMode.Reverse
        ),
        label = "float2"
    )
    val float3 by transition.animateFloat(
        initialValue = -9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            tween(3800, easing = floatEase3), RepeatMode.Reverse
        ),
        label = "float3"
    )

    // Twinkle scale — asymmetric easing gives a sharp blink-in / slow fade-out feel
    val sparkleEase = androidx.compose.animation.core.CubicBezierEasing(0.17f, 0.67f, 0.83f, 0.67f)
    val sparkleScale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            tween(1600, easing = sparkleEase), RepeatMode.Reverse
        ),
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
                        Color(0xFFAE9EF8).copy(alpha = 0.38f),
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
                        Color(0xFFAE9EF8).copy(alpha = 0.70f),
                        Color(0xFF38BDF8).copy(alpha = 0.20f),
                        Color(0xFFAE9EF8).copy(alpha = 0.60f)
                    )
                ),
                style = Stroke(width = 1.6.dp.toPx(), pathEffect = dashEffect)
            )

            // Traveling Energy Node on the Ring (orbits naturally at 1x speed with the Canvas)
            val rx = size.width / 2f
            val ry = size.height / 2f
            val px = rx + rx * 0.98f
            val py = ry

            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = Offset(px, py))
            drawCircle(
                color = Color(0xFFC4BAF9),
                radius = 7.dp.toPx(),
                center = Offset(px, py),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // LAYER 2: THE ANIMATED 3D NATURAL EARTH PARTICLE GLOBE
        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    spotColor = Color(0xAAAE9EF8),
                    ambientColor = Color(0x448478D6)
                )
                .clip(CircleShape)
                .background(Color(0xFF070512)),
            contentAlignment = Alignment.Center
        ) {
            SpinningHolographicGlobeCanvas(
                globeRotation = globeRotation,
                modifier = Modifier.fillMaxSize()
            )
        }

        // LAYER 3: INTERNALLY REACTIVE & CRAZY ANIMATED COMPONENTS

        // 1. Top-Right: Live Corridor Capsule — animated data flow arrows + glowing live dot
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 76.dp, y = (-86).dp + float1.dp)
        ) {
            ReactiveCorridorCapsule(
                arrowSweep = arrowSweep,
                liveDotPulse = liveDotPulse
            )
        }

        // 2. Top-Left: High-Voltage Lightning Prism — crackling arc paths + flickering glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-84).dp, y = (-82).dp + float2.dp)
        ) {
            HighVoltageLightningPrism(
                phase = lightningPhase,
                glow = lightningGlow
            )
        }

        // 3. Mid-Right: Escrow Security Vault Tag — dual sonar rings + scanning bar
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 96.dp, y = (-4).dp + float3.dp)
        ) {
            ActiveEscrowVaultTag(
                sonarProgress = radarPulse,
                scanProgress = escrowScan
            )
        }

        // 4. Bottom-Right: Genuine 3D Spinning Minted Coin + specular shine sweep
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 68.dp, y = 78.dp + float2.dp)
                .graphicsLayer {
                    rotationY = coin3DY
                    cameraDistance = 12f * density.density
                }
        ) {
            Spinning3DCoin(rotationY = coin3DY, shine = coinShine)
        }

        // 5. Bottom-Left: UPI Velocity Hub — streaming speed lines + neon scanning + dot glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-70).dp, y = 80.dp + float1.dp)
        ) {
            UpiVelocityHub(
                scrollPhase = upiScroll,
                dotPulse = liveDotPulse
            )
        }

        // 6. Prismatic Twinkling Diamond Accents
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-14).dp, y = (-106).dp)
                .scale(sparkleScale)
        ) {
            PrismDiamond(color = Color(0xFFAE9EF8), size = 16.dp)
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
 * 3D Rotating Natural Earth Particle Matrix Globe with Atmospheric Rim Glow
 */
@Composable
private fun SpinningHolographicGlobeCanvas(
    globeRotation: Float,
    modifier: Modifier = Modifier
) {
    val points = remember { GlobeData.points }
    val density = LocalDensity.current.density

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w / 2f) * 0.88f

        // 1. Atmospheric Outer Rim Halo (brand #AE9EF8 bloom)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF8478D6).copy(alpha = 0.05f),
                    Color(0xFFAE9EF8).copy(alpha = 0.28f),
                    Color(0xFFC4BAF9).copy(alpha = 0.60f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r * 1.15f
            ),
            radius = r * 1.15f,
            center = Offset(cx, cy)
        )

        // 2. Deep Cosmic Sphere Body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0C091D),
                    Color(0xFF070514),
                    Color(0xFF03020A)
                ),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // 3. Inner Atmospheric Rim Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color(0xFFAE9EF8).copy(alpha = 0.22f),
                    Color(0xFFD8D0FB).copy(alpha = 0.60f)
                ),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // 4. Atmospheric Boundary Ring
        drawCircle(
            color = Color(0xFFD8D0FB).copy(alpha = 0.85f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 1.4.dp.toPx())
        )

        // 5. Rotating 3D Natural Earth Particle Point Cloud
        val rotAngle = globeRotation * (2f * PI.toFloat())
        val tilt = -0.28f // ~-16 degrees axial tilt
        val cosTilt = cos(tilt)
        val sinTilt = sin(tilt)

        val pointCount = points.size / 2
        for (i in 0 until pointCount) {
            val latRad = points[i * 2]
            val lonRad = points[i * 2 + 1]

            val lonRot = lonRad + rotAngle
            val cosLat = cos(latRad)
            val x0 = cosLat * sin(lonRot)
            val y0 = -sin(latRad)
            val z0 = cosLat * cos(lonRot)

            // Apply axial tilt
            val x = x0
            val y = y0 * cosTilt - z0 * sinTilt
            val z = y0 * sinTilt + z0 * cosTilt

            // Render front hemisphere
            if (z > 0.02f) {
                val px = cx + x * r
                val py = cy + y * r
                val alpha = (0.20f + 0.80f * z).coerceIn(0.12f, 1f)
                val ptRadius = (0.75f + 0.85f * z) * density

                drawCircle(
                    color = Color(0xFFD8D0FB).copy(alpha = alpha),
                    radius = ptRadius,
                    center = Offset(px, py)
                )
            }
        }
    }
}

/**
 * 1. Live Corridor Capsule — animated streaming data arrows + glowing live dot
 */
@Composable
private fun ReactiveCorridorCapsule(
    arrowSweep: Float,
    liveDotPulse: Float
) {
    val dotGlowAlpha = 0.3f + 0.7f * liveDotPulse
    val dotScale = 0.85f + 0.3f * liveDotPulse

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1A1230),
        border = BorderStroke(1.dp, Color(0xFF7060B8).copy(alpha = 0.6f + 0.4f * liveDotPulse)),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                UsaFlag(width = 14.dp, height = 10.dp)

                // Animated streaming arrows canvas — 3 staggered chevrons slide left to right
                Canvas(modifier = Modifier.size(22.dp, 11.dp)) {
                    val w = size.width
                    val h = size.height
                    val arrowW = w / 4f
                    // Draw 3 chevrons at staggered positions, wrapping with arrowSweep
                    for (i in 0..2) {
                        val baseX = ((arrowSweep + i / 3f) % 1f) * w
                        val alpha = when {
                            baseX < arrowW -> baseX / arrowW
                            baseX > w - arrowW -> (w - baseX) / arrowW
                            else -> 1f
                        }.coerceIn(0f, 1f)
                        val chevronPath = Path().apply {
                            moveTo(baseX, h * 0.15f)
                            lineTo(baseX + arrowW * 0.55f, h * 0.5f)
                            lineTo(baseX, h * 0.85f)
                            moveTo(baseX + arrowW * 0.5f, h * 0.15f)
                            lineTo(baseX + arrowW, h * 0.5f)
                            lineTo(baseX + arrowW * 0.5f, h * 0.85f)
                        }
                        drawPath(
                            path = chevronPath,
                            color = Color(0xFFAE9EF8).copy(alpha = alpha * 0.85f),
                            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                IndiaFlag(width = 14.dp, height = 10.dp)

                // Live dot with breathing glow ring
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        // Outer glow ring
                        drawCircle(
                            color = Color(0xFF34D399).copy(alpha = dotGlowAlpha * 0.5f),
                            radius = 5.dp.toPx() * dotScale
                        )
                        // Core dot
                        drawCircle(
                            color = Color(0xFF34D399),
                            radius = 2.5.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Minted Golden Lightning Coin — identical styling and background gradient to the 3D currency coin
 */
@Composable
private fun HighVoltageLightningPrism(phase: Float, glow: Float) {
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
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height

            // Classic 6-point electric bolt geometry
            val boltPath = Path().apply {
                moveTo(w * 0.62f, h * 0.06f) // Top sharp apex
                lineTo(w * 0.26f, h * 0.52f) // Slanted left shoulder
                lineTo(w * 0.48f, h * 0.52f) // Horizontal shelf stepping right
                lineTo(w * 0.38f, h * 0.94f) // Slanted needle tip at bottom
                lineTo(w * 0.74f, h * 0.46f) // Slanted right shoulder
                lineTo(w * 0.52f, h * 0.46f) // Horizontal shelf stepping left
                close()
            }

            // Minted bolt stamped in the same dark amber-brown as the $ / ₹ symbol
            drawPath(
                path = boltPath,
                color = Color(0xFF451A03)
            )

            // Embossed edge highlight
            val highlightPath = Path().apply {
                moveTo(w * 0.62f, h * 0.10f)
                lineTo(w * 0.29f, h * 0.51f)
                lineTo(w * 0.46f, h * 0.51f)
                lineTo(w * 0.39f, h * 0.90f)
            }
            drawPath(
                path = highlightPath,
                color = Color(0xFFFFE082).copy(alpha = 0.50f),
                style = Stroke(width = 0.9.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Specular shine band that sweeps across the coin surface
        Canvas(modifier = Modifier.fillMaxSize()) {
            val shineX = size.width * (glow * 0.5f + 0.5f)
            val shineWidth = size.width * 0.35f
            val shineBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                start = Offset(shineX - shineWidth, 0f),
                end = Offset(shineX + shineWidth, size.height)
            )
            drawCircle(
                brush = shineBrush,
                radius = size.minDimension / 2f
            )
        }
    }
}

/**
 * 3. Escrow Vault Tag — dual offset sonar rings, animated scan bar, pulsing lock glow
 */
@Composable
private fun ActiveEscrowVaultTag(sonarProgress: Float, scanProgress: Float) {
    // Second sonar ring offset by half a cycle
    val sonarProgress2 = (sonarProgress + 0.5f) % 1f

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1230),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF6B5CB8).copy(alpha = 0.5f + 0.5f * sonarProgress),
                    Color(0xFFAE9EF8).copy(alpha = 0.8f),
                    Color(0xFF6B5CB8).copy(alpha = 0.5f + 0.5f * sonarProgress2)
                )
            )
        ),
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
                        tint = Color(0xFFAE9EF8).copy(alpha = 0.6f + 0.4f * sonarProgress),
                        modifier = Modifier.size(11.dp)
                    )

                    // Dual offset sonar rings
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val maxR = size.width / 2f

                        // Ring 1
                        val r1 = maxR * sonarProgress
                        val a1 = (1f - sonarProgress).coerceIn(0f, 1f)
                        if (r1 > 0f) {
                            drawCircle(
                                color = Color(0xFFAE9EF8).copy(alpha = a1 * 0.7f),
                                radius = r1,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }

                        // Ring 2 (offset phase)
                        val r2 = maxR * sonarProgress2
                        val a2 = (1f - sonarProgress2).coerceIn(0f, 1f)
                        if (r2 > 0f) {
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = a2 * 0.5f),
                                radius = r2,
                                style = Stroke(width = 0.8.dp.toPx())
                            )
                        }
                    }
                }

                Text(
                    text = "Secured",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = Color.White
                    )
                )
            }

            // Animated horizontal scanning bar — sweeps vertically through the entire tag
            Canvas(modifier = Modifier.matchParentSize()) {
                val scanY = scanProgress * size.height
                val scanBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFAE9EF8).copy(alpha = 0.22f),
                        Color(0xFFAE9EF8).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = scanY - size.height * 0.18f,
                    endY = scanY + size.height * 0.18f
                )
                drawRect(brush = scanBrush)
            }
        }
    }
}

/**
 * 4. Genuine 3D Spinning Remittance Coin with animated specular shine sweep
 */
@Composable
private fun Spinning3DCoin(rotationY: Float, shine: Float) {
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
            text = if (isFront) "$" else "\u20B9",
            modifier = if (!isFront) Modifier.graphicsLayer { scaleX = -1f } else Modifier,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF451A03)
            )
        )
        // Specular shine band that sweeps across the coin surface
        Canvas(modifier = Modifier.fillMaxSize()) {
            val shineX = size.width * (shine * 0.5f + 0.5f)
            val shineWidth = size.width * 0.35f
            val shineBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                start = Offset(shineX - shineWidth, 0f),
                end = Offset(shineX + shineWidth, size.height)
            )
            drawCircle(
                brush = shineBrush,
                radius = size.minDimension / 2f
            )
        }
    }
}

/**
 * 5. UPI Velocity Hub — streaming speed lines + neon scanning line + multi-ring dot glow
 */
@Composable
private fun UpiVelocityHub(scrollPhase: Float, dotPulse: Float) {
    val dotAlpha = 0.35f + 0.65f * dotPulse

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0E1C20),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF10B981).copy(alpha = 0.5f + 0.5f * dotPulse),
                    Color(0xFF38BDF8).copy(alpha = 0.7f)
                )
            )
        ),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Streaming speed lines behind the content
            Canvas(modifier = Modifier.size(90.dp, 26.dp)) {
                val w = size.width
                val h = size.height
                // 4 horizontal speed lines that scroll left-to-right
                val lineLengths = listOf(0.28f, 0.18f, 0.22f, 0.14f)
                val lineYPositions = listOf(h * 0.25f, h * 0.45f, h * 0.62f, h * 0.80f)
                val lineAlphas = listOf(0.45f, 0.25f, 0.35f, 0.20f)
                val lineSpeeds = listOf(1.0f, 1.4f, 0.85f, 1.2f)

                for (i in lineLengths.indices) {
                    val len = lineLengths[i] * w
                    val startX = ((scrollPhase * lineSpeeds[i]) % 1.2f - 0.2f) * w
                    val endX = (startX + len).coerceAtMost(w)
                    val clampedStart = startX.coerceAtLeast(0f)
                    if (endX > clampedStart) {
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF10B981).copy(alpha = lineAlphas[i]),
                                    Color(0xFF38BDF8).copy(alpha = lineAlphas[i] * 0.6f),
                                    Color.Transparent
                                ),
                                startX = clampedStart,
                                endX = endX
                            ),
                            start = Offset(clampedStart, lineYPositions[i]),
                            end = Offset(endX, lineYPositions[i]),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Lightning bolt glyph
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
                    drawPath(
                        bolt,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF34D399),
                                Color(0xFF6EE7B7).copy(alpha = 0.6f + 0.4f * dotPulse)
                            )
                        )
                    )
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

                // Multi-ring pulsing live dot
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        // Outer glow ring
                        drawCircle(
                            color = Color(0xFF34D399).copy(alpha = dotAlpha * 0.35f),
                            radius = 6.dp.toPx()
                        )
                        // Mid ring
                        drawCircle(
                            color = Color(0xFF34D399).copy(alpha = dotAlpha * 0.6f),
                            radius = 4.dp.toPx(),
                            style = Stroke(width = 0.8.dp.toPx())
                        )
                        // Core dot
                        drawCircle(
                            color = Color(0xFF34D399),
                            radius = 2.5.dp.toPx()
                        )
                    }
                }
            }
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
 * Official PayX Top Header Logo matching wallet design in purple obsidian theme
 */
@Composable
private fun PayxHeaderLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF231660), Color(0xFF130E2E))
                )
            )
            .border(1.2.dp, Color(0xFF907EC8), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val walletW = w * 0.90f
            val walletH = h * 0.74f
            val left = (w - walletW) / 2f
            val top = (h - walletH) / 2f
            val right = left + walletW
            val bottom = top + walletH

            val stroke = walletH * 0.22f
            val outerR = 4.5.dp.toPx()
            val innerR = 2.dp.toPx()

            val walletBrush = Brush.linearGradient(
                listOf(Color(0xFFEDE9FD), Color(0xFFD8D0FB), Color(0xFFAE9EF8))
            )
            val bgColor = Color(0xFF160D26)

            // Outer wallet body ("C" shape)
            drawRoundRect(
                brush = walletBrush,
                topLeft = Offset(left, top),
                size = Size(walletW, walletH),
                cornerRadius = CornerRadius(outerR, outerR)
            )

            // Cavity cutout (inside cavity)
            drawRoundRect(
                color = bgColor,
                topLeft = Offset(left + stroke, top + stroke),
                size = Size(walletW - stroke + 2.dp.toPx(), walletH - stroke * 2f),
                cornerRadius = CornerRadius(innerR, innerR)
            )

            // Wallet Flap
            val gapY = walletH * 0.08f
            val flapLeft = left + stroke + (walletW * 0.10f)
            val flapTop = top + stroke + gapY
            val flapBottom = bottom - stroke - gapY
            val flapRight = right
            val flapW = flapRight - flapLeft
            val flapH = flapBottom - flapTop

            drawRoundRect(
                brush = walletBrush,
                topLeft = Offset(flapLeft, flapTop),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
            )

            // Snap Button Hole
            val snapCenter = Offset((flapLeft + flapRight) / 2f, (flapTop + flapBottom) / 2f)
            val snapR = walletH * 0.075f
            drawCircle(
                color = bgColor,
                radius = snapR,
                center = snapCenter
            )
        }
    }
}

/**
 * Official Google 4-Color Paths
 */
private object GooglePaths {
    val bluePath = PathParser.createPathFromPathData("M 23.75 12.27 c 0 -0.7 -.06 -1.4 -.19 -2.07 H 12 v 4.51 h 6.6 c -0.29 1.52 -1.14 2.82 -2.4 3.68 v 3.05 h 3.88 c 2.27 -2.09 3.66 -5.17 3.66 -9.17 z").asComposePath()
    val greenPath = PathParser.createPathFromPathData("M 12 24 c 3.24 0 5.95 -1.08 7.93 -2.91 l -3.88 -3.05 c -1.08 0.72 -2.45 1.16 -4.05 1.16 c -3.12 0 -5.77 -2.1 -6.72 -4.93 H 1.25 v 3.15 C 3.26 21.36 7.33 24 12 24 z").asComposePath()
    val yellowPath = PathParser.createPathFromPathData("M 5.28 14.27 c -0.25 -0.72 -0.38 -1.49 -0.38 -2.27 s 0.13 -1.55 0.38 -2.27 V 6.58 H 1.25 C 0.45 8.18 0 9.99 0 12 s 0.45 3.82 1.25 5.42 l 4.03 -3.15 z").asComposePath()
    val redPath = PathParser.createPathFromPathData("M 12 4.75 c 1.77 0 3.35 0.61 4.6 1.8 l 3.42 -3.42 C 17.95 1.19 15.24 0 12 0 C 7.33 0 3.26 2.64 1.25 6.58 l 4.03 3.15 c 0.95 -2.83 3.6 -4.98 6.72 -4.98 z").asComposePath()
}

/**
 * Official Google "G" 4-Color Vector Logo
 * Completely transparent background — renders 4-color G directly on parent surface.
 */
@Composable
private fun GoogleOfficialLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            drawPath(GooglePaths.bluePath, color = Color(0xFF4285F4))
            drawPath(GooglePaths.greenPath, color = Color(0xFF34A853))
            drawPath(GooglePaths.yellowPath, color = Color(0xFFFBBC05))
            drawPath(GooglePaths.redPath, color = Color(0xFFEA4335))
        }
    }
}

/**
 * Interactive US and IND Corridor Switching Tab with Animated Sliding Back Pill
 */
@Composable
private fun CorridorSwitchTab(
    selectedCorridor: String,
    onCorridorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val targetIndex = if (selectedCorridor == "US") 0f else 1f
    val animatedIndex by animateFloatAsState(
        targetValue = targetIndex,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "corridor_pill_slide"
    )

    val usTextColor by animateColorAsState(
        targetValue = if (selectedCorridor == "US") Color.White else PayxPalette.TextTertiary,
        label = "us_text_color"
    )
    val indTextColor by animateColorAsState(
        targetValue = if (selectedCorridor == "IND") Color.White else PayxPalette.TextTertiary,
        label = "ind_text_color"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val tabWidth = 92.dp
        val tabHeight = 36.dp
        val padding = 4.dp
        val containerWidth = (tabWidth * 2) + (padding * 2)

        // Segmented pill container
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF140F24),
            border = BorderStroke(1.dp, Color(0xFF2B2245)),
            shadowElevation = 8.dp,
            modifier = Modifier.width(containerWidth)
        ) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .height(tabHeight)
            ) {
                // 1. Sliding Animated Background Pill
                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * animatedIndex)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0x66AE9EF8),
                            ambientColor = Color(0x33AE9EF8)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3B2968), Color(0xFF281A46))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFAE9EF8).copy(alpha = 0.75f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.45f)
                                )
                            ),
                            RoundedCornerShape(20.dp)
                        )
                )

                // 2. Clickable Tab Content Row
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // US Tab
                    Box(
                        modifier = Modifier
                            .width(tabWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onCorridorSelected("US") },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            UsaFlag(width = 17.dp, height = 11.dp)
                            Text(
                                text = "US",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (selectedCorridor == "US") FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.8.sp,
                                    color = usTextColor
                                )
                            )
                        }
                    }

                    // IND Tab
                    Box(
                        modifier = Modifier
                            .width(tabWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onCorridorSelected("IND") },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            IndiaFlag(width = 17.dp, height = 11.dp)
                            Text(
                                text = "IND",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (selectedCorridor == "IND") FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.8.sp,
                                    color = indTextColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
