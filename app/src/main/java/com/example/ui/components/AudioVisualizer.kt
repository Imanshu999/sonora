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

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    color: Color = MaterialTheme.colorScheme.primary,
    maxHeight: Dp = 18.dp,
    barWidth: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_anim")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isPlaying) 1.0f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )

    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 0.85f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )

    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isPlaying) 0.95f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, delayMillis = 60, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isPlaying) 0.75f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(410, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )

    val heights = listOf(h1, h2, h3, h4)

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val factor = heights[i % heights.size]
            val currentHeight = maxHeight * factor
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentHeight.coerceAtLeast(3.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
