package com.payx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MidnightHull = Color(0xFF0F0F1C)
private val ClayEmber = Color(0xFFBC7155)

private val PayxColors = darkColorScheme(
    primary = ClayEmber,
    onPrimary = Color.White,
    background = MidnightHull,
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF17171F),
    onSurface = Color(0xFFE0E0E0)
)

@Composable
fun PayxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PayxColors, content = content)
}
