package com.payx.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private val smoothDecel = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/**
 * Opening Intro Animation Screen featuring two distinct mechanical components
 * merging to assemble the PayX emblem, with PayX wordmark below the logo,
 * followed by a seamless transition to the Login screen.
 */
@Composable
fun IntroScreen(
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f))
        )
        delay(480)
        onFinished()
    }

    val t = progress.value

    // Animation timeline phases
    // 1. 0.00..0.44: Two distinct components glide from left and right into the center
    val convergeFrac = (t / 0.44f).coerceIn(0f, 1f)
    val convergeEase = smoothDecel.transform(convergeFrac)

    // 2. 0.22..0.44: Luxury badge chassis fades and scales into view
    val badgeAlpha = ((t - 0.22f) / 0.22f).coerceIn(0f, 1f)

    // 3. 0.40..0.56: Snap core lock & tactile impact bump
    val snapFrac = ((t - 0.40f) / 0.16f).coerceIn(0f, 1f)
    val snapEase = FastOutSlowInEasing.transform(snapFrac)

    val impactBump = if (t in 0.42f..0.58f) {
        val bumpT = ((t - 0.42f) / 0.16f)
        sin(bumpT * PI.toFloat()) * 0.08f
    } else {
        0f
    }
    val emblemScale = (0.75f + 0.25f * convergeEase) + impactBump

    // 4. 0.42..0.80: Shockwave impact expansion ring
    val shockwaveFrac = ((t - 0.42f) / 0.38f).coerceIn(0f, 1f)

    // 5. 0.44..0.72: PayX brand wordmark slides in from the right screen edge when components merge
    val textFrac = ((t - 0.44f) / 0.28f).coerceIn(0f, 1f)
    val textEase = smoothDecel.transform(textFrac)

    // 6. 0.46..0.74: Specular shimmer sweep across merged emblem
    val shimmerFrac = ((t - 0.46f) / 0.28f).coerceIn(0f, 1f)

    // 7. 0.42..0.54: Impact flash seam flare
    val flareAlpha = if (t in 0.42f..0.54f) {
        val flareT = (t - 0.42f) / 0.12f
        sin(flareT * PI.toFloat())
    } else {
        0f
    }

    val screenBg = Color(0xFF09090D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Radial Glow behind the emblem
        Canvas(modifier = Modifier.size(320.dp)) {
            val glowAlpha = (0.38f * convergeEase).coerceIn(0f, 0.38f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFAE9EF8).copy(alpha = glowAlpha),
                        Color(0xFF7C3AED).copy(alpha = glowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width / 2f
                ),
                radius = size.width / 2f
            )
        }

        // Shockwave impact expansion ring
        if (t > 0.42f && shockwaveFrac < 1f) {
            val haloAlpha = (1f - shockwaveFrac) * 0.80f
            Canvas(modifier = Modifier.size(300.dp)) {
                val baseR = 52.dp.toPx()
                val targetR = 145.dp.toPx()
                val currentR = baseR + (targetR - baseR) * shockwaveFrac
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFAE9EF8).copy(alpha = haloAlpha),
                            Color(0xFF818CF8).copy(alpha = haloAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = currentR + 4.dp.toPx()
                    ),
                    radius = currentR,
                    style = Stroke(width = (2.4f * (1f - shockwaveFrac)).dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Merging Logo Box strictly sized to 108.dp so spacing below it is accurate
            Box(
                modifier = Modifier.size(108.dp),
                contentAlignment = Alignment.Center
            ) {
                // Luxury Emblem Badge Base Container
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(emblemScale)
                        .alpha(badgeAlpha)
                        .shadow(
                            elevation = (26 * convergeEase).dp,
                            shape = RoundedCornerShape(30.dp),
                            spotColor = Color(0xFFAE9EF8).copy(alpha = 0.60f),
                            ambientColor = Color(0xFF5B349E).copy(alpha = 0.40f)
                        )
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF231660), Color(0xFF130E2E))
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFFC4B5FD).copy(alpha = 0.85f + 0.15f * flareAlpha),
                                    Color(0xFF6D28D9).copy(alpha = 0.65f)
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                )

                // The Two Merging Components Canvas (unclipped so travel across screen is fully visible)
                Canvas(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(emblemScale)
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Merged wallet emblem dimensions
                    val walletW = 62.dp.toPx()
                    val walletH = 50.dp.toPx()
                    val left = cx - walletW / 2f
                    val top = cy - walletH / 2f
                    val right = left + walletW
                    val bottom = top + walletH

                    val stroke = walletH * 0.22f
                    val outerR = 10.dp.toPx()
                    val innerR = 4.dp.toPx()

                    val walletBrush = Brush.linearGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFEDE9FD), Color(0xFFAE9EF8))
                    )
                    val flapBrush = Brush.linearGradient(
                        listOf(Color(0xFFEDE9FD), Color(0xFFD8D0FB), Color(0xFFAE9EF8))
                    )
                    val bgColor = Color(0xFF140D26)

                    // Trajectory offsets: starting far apart on opposite sides
                    val leftTravelDist = 120.dp.toPx()
                    val rightTravelDist = 120.dp.toPx()

                    val leftOffsetX = -leftTravelDist * (1f - convergeEase)
                    val leftRotation = -7f * (1f - convergeEase)

                    val rightOffsetX = rightTravelDist * (1f - convergeEase)
                    val rightRotation = 7f * (1f - convergeEase)

                    // ----------------------------------------------------
                    // COMPONENT 1: The Left Outer Wallet Chassis ("C" Body)
                    // ----------------------------------------------------
                    rotate(degrees = leftRotation, pivot = Offset(left + leftOffsetX + walletW / 2f, cy)) {
                        // Outer "C" Chassis Body
                        drawRoundRect(
                            brush = walletBrush,
                            topLeft = Offset(left + leftOffsetX, top),
                            size = Size(walletW, walletH),
                            cornerRadius = CornerRadius(outerR, outerR)
                        )

                        // Internal cavity cutout where the right component docks
                        drawRoundRect(
                            color = bgColor,
                            topLeft = Offset(left + stroke + leftOffsetX, top + stroke),
                            size = Size(walletW - stroke + 4.dp.toPx(), walletH - stroke * 2f),
                            cornerRadius = CornerRadius(innerR, innerR)
                        )
                    }

                    // ----------------------------------------------------
                    // COMPONENT 2: The Right Locking Tongue / Flap & Core
                    // ----------------------------------------------------
                    val gapY = walletH * 0.08f
                    val flapLeft = left + stroke + (walletW * 0.10f)
                    val flapTop = top + stroke + gapY
                    val flapBottom = bottom - stroke - gapY
                    val flapW = right - flapLeft
                    val flapH = flapBottom - flapTop

                    rotate(degrees = rightRotation, pivot = Offset(flapLeft + rightOffsetX + flapW / 2f, cy)) {
                        // Sliding tongue piece
                        drawRoundRect(
                            brush = flapBrush,
                            topLeft = Offset(flapLeft + rightOffsetX, flapTop),
                            size = Size(flapW, flapH),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                        )

                        // Center Snap Core Rivet (locks and pops in)
                        val snapCenter = Offset(
                            (flapLeft + right) / 2f + rightOffsetX,
                            (flapTop + flapBottom) / 2f
                        )
                        val snapR = walletH * 0.078f * (0.6f + 0.4f * snapEase)
                        drawCircle(
                            color = bgColor,
                            radius = snapR,
                            center = snapCenter
                        )
                        drawCircle(
                            color = Color(0xFFEDE9FD),
                            radius = snapR * 0.45f,
                            center = snapCenter
                        )
                    }

                    // ----------------------------------------------------
                    // IMPACT FLARE & SHIMMER (Occurs upon merging)
                    // ----------------------------------------------------
                    if (flareAlpha > 0f) {
                        // Vertical seam energy line where the two components join
                        val seamX = flapLeft
                        drawLine(
                            color = Color.White.copy(alpha = flareAlpha * 0.90f),
                            start = Offset(seamX, top - 6.dp.toPx()),
                            end = Offset(seamX, bottom + 6.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    // Metallic light shimmer sweep across the unified logo
                    if (t in 0.46f..0.74f) {
                        val shimmerX = left - walletW + (3f * walletW * shimmerFrac)
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.45f * (1f - shimmerFrac)),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX - 16.dp.toPx(), top),
                            end = Offset(shimmerX + 16.dp.toPx(), bottom)
                        )
                        drawRoundRect(
                            brush = shimmerBrush,
                            topLeft = Offset(left, top),
                            size = Size(walletW, walletH),
                            cornerRadius = CornerRadius(outerR, outerR)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // PayX Wordmark appearing from the right side of the screen when components merge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(x = (260 * (1f - textEase)).dp)
                    .alpha(textFrac)
            ) {
                Text(
                    text = "Pay",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "X",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFD8B4FE), Color(0xFFA855F7), Color(0xFF818CF8))
                        )
                    )
                )
            }
        }
    }
}


