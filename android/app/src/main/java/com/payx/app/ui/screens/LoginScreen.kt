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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    var selectedCorridor by remember { mutableStateOf("USA") }

    val backgroundGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFE9F5ED),
            0.22f to Color(0xFFF9FCFA),
            0.45f to Color(0xFFFFFFFF),
            1.0f to Color(0xFFFFFFFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Hero Content
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                // PayX App Squircle Logo
                PayxLogo()

                Spacer(modifier = Modifier.height(48.dp))

                // Headline
                Text(
                    text = "Global Wealth,\nSeamlessly\nCurated.",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 44.sp,
                        lineHeight = 49.sp,
                        letterSpacing = (-0.8).sp,
                        color = Color(0xFF141714)
                    )
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Subtitle
                Text(
                    text = "Experience the modern way to manage cross-border remittances. Beautiful, secure, and now backed by live Neon data.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.1).sp,
                        color = Color(0xFF4A524B)
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
                    color = Color(0xFFF2F4F2),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CorridorSegment(
                            title = "USA",
                            flag = { UsaFlag() },
                            isSelected = selectedCorridor == "USA",
                            onClick = { selectedCorridor = "USA" }
                        )
                        CorridorSegment(
                            title = "India",
                            flag = { IndiaFlag() },
                            isSelected = selectedCorridor == "India",
                            onClick = { selectedCorridor = "India" }
                        )
                    }
                }

                // Continue with Google Button
                Button(
                    onClick = onSignedIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF141714)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E6E3)),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 1.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GoogleGlyph(modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp,
                                color = Color(0xFF141714)
                            )
                        )
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
                        color = Color(0xFF7E867F),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CorridorSegment(
    title: String,
    flag: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0x20000000)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(26.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 9.dp),
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
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF141714) else Color(0xFF555E56)
                )
            )
        }
    }
}

@Composable
private fun PayxLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF133824)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(32.dp)) {
            val lime = Color(0xFFCEF437)
            val strokeWidth = 3.6.dp.toPx()

            // Card peeking at the top
            drawRoundRect(
                color = lime,
                topLeft = Offset(size.width * 0.18f, size.height * 0.14f),
                size = Size(size.width * 0.64f, size.height * 0.22f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Main wallet rectangle
            drawRoundRect(
                color = lime,
                topLeft = Offset(size.width * 0.10f, size.height * 0.30f),
                size = Size(size.width * 0.80f, size.height * 0.58f),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Right latch flap
            val flapW = size.width * 0.34f
            val flapH = size.height * 0.25f
            val flapL = size.width * 0.56f
            val flapT = size.height * 0.465f

            // Clean background behind flap
            drawRoundRect(
                color = Color(0xFF133824),
                topLeft = Offset(flapL, flapT),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
            // Flap outline
            drawRoundRect(
                color = lime,
                topLeft = Offset(flapL, flapT),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
            // Center circular rivet
            drawCircle(
                color = lime,
                radius = 2.4.dp.toPx(),
                center = Offset(flapL + flapW * 0.40f, flapT + flapH / 2f)
            )
        }
    }
}

@Composable
private fun GoogleGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.8.dp.toPx()
        val r = (size.minDimension - stroke) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawArc(
            color = Color(0xFF141714),
            startAngle = 40f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color(0xFF141714),
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
