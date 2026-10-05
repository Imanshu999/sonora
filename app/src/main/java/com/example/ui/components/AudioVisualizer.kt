package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lightweight 12-bar flowing visualizer. It deliberately uses only Compose
 * animations so it stays smooth on low-end devices and does not require
 * microphone/audio-recording permissions.
 */
@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 12,
    color: Color = MaterialTheme.colorScheme.primary,
    maxHeight: Dp = 18.dp,
    barWidth: Dp = 2.5.dp
) {
    val transition = rememberInfiniteTransition(label = "visualizer_flow")
    val count = barCount.coerceIn(4, 15)
    val heights = List(count) { index ->
        val phase = (index * 37) % 170
        val low = 0.16f + (index % 3) * 0.04f
        val high = if (isPlaying) 0.55f + ((index * 17) % 45) / 100f else 0.22f
        transition.animateFloat(
            initialValue = low,
            targetValue = high.coerceAtMost(1f),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 360 + (index % 5) * 85,
                    delayMillis = phase,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        ).value
    }

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { factor ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height((maxHeight * factor).coerceAtLeast(2.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
