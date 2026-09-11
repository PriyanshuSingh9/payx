package com.payx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object PayxPalette {
    val Obsidian = Color(0xFF0E0C15)
    val DarkSurface = Color(0xFF181523)
    val DarkSurfaceElevated = Color(0xFF221E30)
    val BorderSubtle = Color(0xFF2E283F)

    // Primary brand: #AE9EF8 and tonal range
    val VividPurple = Color(0xFFAE9EF8)
    val NeonViolet = Color(0xFFC4BAF9)
    val SoftLavender = Color(0xFFD8D0FB)
    val LightLavender = Color(0xFFEDE9FD)

    val CardGradientStart = Color(0xFFAE9EF8)
    val CardGradientEnd = Color(0xFF8478D6)

    val CoralAccent = Color(0xFFFF5B79)
    val CyberGreen = Color(0xFF00E676)
    val GoldAccent = Color(0xFFFFB800)
    val NeonLime = Color(0xFFD4FF32)
    val NeonLimeMuted = Color(0x33D4FF32)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF9E97AA)
    val TextTertiary = Color(0xFF6B6478)
}

private val PayxColors = darkColorScheme(
    primary = PayxPalette.VividPurple,
    onPrimary = Color.White,
    background = PayxPalette.Obsidian,
    onBackground = PayxPalette.TextPrimary,
    surface = PayxPalette.DarkSurface,
    onSurface = PayxPalette.TextPrimary,
    surfaceVariant = PayxPalette.DarkSurfaceElevated,
    onSurfaceVariant = PayxPalette.TextSecondary,
    outline = PayxPalette.BorderSubtle
)

@Composable
fun PayxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PayxColors, content = content)
}
