package com.payx.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IndiaFlag(
    width: Dp = 22.dp,
    height: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(3.dp))
    ) {
        val stripeH = size.height / 3f

        // Top saffron stripe
        drawRect(
            color = Color(0xFFFF9933),
            topLeft = Offset(0f, 0f),
            size = Size(size.width, stripeH)
        )

        // Middle white stripe
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, stripeH),
            size = Size(size.width, stripeH)
        )

        // Bottom green stripe
        drawRect(
            color = Color(0xFF138808),
            topLeft = Offset(0f, stripeH * 2),
            size = Size(size.width, stripeH)
        )

        // Ashoka Chakra Navy circle in center
        val center = Offset(size.width / 2f, size.height / 2f)
        val chakraRadius = stripeH * 0.38f
        drawCircle(
            color = Color(0xFF000080),
            radius = chakraRadius,
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF000080),
            radius = 1.dp.toPx(),
            center = center
        )
    }
}

@Composable
fun UsaFlag(
    width: Dp = 22.dp,
    height: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(3.dp))
    ) {
        val stripeH = size.height / 7f
        for (i in 0 until 7) {
            drawRect(
                color = if (i % 2 == 0) Color(0xFFB22234) else Color.White,
                topLeft = Offset(0f, i * stripeH),
                size = Size(size.width, stripeH)
            )
        }
        // Blue canton on top left
        drawRect(
            color = Color(0xFF3C3B6E),
            topLeft = Offset.Zero,
            size = Size(size.width * 0.44f, stripeH * 4)
        )
    }
}
