package com.payx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.ui.theme.PayxPalette

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    var selectedCorridor by remember { mutableStateOf("USA") }

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
        // Decorative background topography curves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val purpleGlow = Color(0x18B866FC)
            val path1 = Path().apply {
                moveTo(0f, size.height * 0.18f)
                cubicTo(
                    size.width * 0.35f, size.height * 0.12f,
                    size.width * 0.7f, size.height * 0.28f,
                    size.width, size.height * 0.22f
                )
            }
            drawPath(path1, color = purpleGlow, style = Stroke(width = 1.5.dp.toPx()))

            val path2 = Path().apply {
                moveTo(0f, size.height * 0.22f)
                cubicTo(
                    size.width * 0.4f, size.height * 0.16f,
                    size.width * 0.65f, size.height * 0.32f,
                    size.width, size.height * 0.26f
                )
            }
            drawPath(path2, color = Color(0x10A855F7), style = Stroke(width = 1.2.dp.toPx()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Hero Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                // PayX App Logo in Neon Purple Theme
                PayxDarkLogo()

                Spacer(modifier = Modifier.height(44.dp))

                // Headline
                Text(
                    text = "Global Wealth,\nSeamlessly\nCurated.",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 44.sp,
                        lineHeight = 50.sp,
                        letterSpacing = (-0.8).sp,
                        color = PayxPalette.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subtitle
                Text(
                    text = "Experience the modern way to manage cross-border remittances. Beautiful, secure, and now backed by live Neon data.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.1).sp,
                        color = PayxPalette.TextSecondary
                    )
                )
            }

            // Bottom Actions Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Segmented Country Corridor Selector
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF1A1626),
                    border = BorderStroke(1.dp, Color(0xFF2C253D)),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DarkCorridorSegment(
                            title = "USA",
                            flag = { UsaFlag() },
                            isSelected = selectedCorridor == "USA",
                            onClick = { selectedCorridor = "USA" }
                        )
                        DarkCorridorSegment(
                            title = "India",
                            flag = { IndiaFlag() },
                            isSelected = selectedCorridor == "India",
                            onClick = { selectedCorridor = "India" }
                        )
                    }
                }

                // Continue with Google Button
                val buttonGradient = Brush.horizontalGradient(
                    listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                )

                Button(
                    onClick = onSignedIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(29.dp),
                            spotColor = Color(0x66A855F7),
                            ambientColor = Color(0x33A855F7)
                        ),
                    shape = RoundedCornerShape(29.dp),
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

                Spacer(modifier = Modifier.height(18.dp))

                // Terms of Service Disclaimer
                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextTertiary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DarkCorridorSegment(
    title: String,
    flag: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedGradient = Brush.horizontalGradient(
        listOf(Color(0xFF332154), Color(0xFF281944))
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (isSelected) Color(0xFF2F214E) else Color.Transparent
            )
            .then(
                if (isSelected) {
                    Modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x40A855F7))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            flag()
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.5.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else PayxPalette.TextSecondary
                )
            )
        }
    }
}

@Composable
private fun PayxDarkLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(66.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF201735))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x4DA855F7)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(34.dp)) {
            val neonPurple = Color(0xFFD8B4FE)
            val strokeWidth = 3.6.dp.toPx()

            // Card peeking at the top
            drawRoundRect(
                color = neonPurple,
                topLeft = Offset(size.width * 0.18f, size.height * 0.14f),
                size = Size(size.width * 0.64f, size.height * 0.22f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Main wallet body
            drawRoundRect(
                color = neonPurple,
                topLeft = Offset(size.width * 0.10f, size.height * 0.30f),
                size = Size(size.width * 0.80f, size.height * 0.58f),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Right flap
            val flapW = size.width * 0.34f
            val flapH = size.height * 0.25f
            val flapL = size.width * 0.56f
            val flapT = size.height * 0.465f

            drawRoundRect(
                color = Color(0xFF201735),
                topLeft = Offset(flapL, flapT),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
            drawRoundRect(
                color = neonPurple,
                topLeft = Offset(flapL, flapT),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
            drawCircle(
                color = neonPurple,
                radius = 2.4.dp.toPx(),
                center = Offset(flapL + flapW * 0.40f, flapT + flapH / 2f)
            )
        }
    }
}

@Composable
private fun GoogleWhiteGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 3.dp.toPx()
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

@Composable
private fun UsaFlag(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(width = 20.dp, height = 14.dp)
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

@Composable
private fun IndiaFlag(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(width = 20.dp, height = 14.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        val bandH = size.height / 3f
        drawRect(
            color = Color(0xFFFF9933),
            topLeft = Offset.Zero,
            size = Size(size.width, bandH)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, bandH),
            size = Size(size.width, bandH)
        )
        drawRect(
            color = Color(0xFF138808),
            topLeft = Offset(0f, bandH * 2),
            size = Size(size.width, bandH)
        )
        drawCircle(
            color = Color(0xFF000080),
            radius = bandH * 0.32f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}
