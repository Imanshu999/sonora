package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WavySeekBar(
    progress: Float, // 0f to 1f
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    waveAmplitude: Float = 7f,
    wavelength: Float = 36f
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val effectiveProgress = if (isDragging) dragProgress else progress.coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2 * Math.PI).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .testTag("wavy_seek_bar")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newP = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newP)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val delta = dragAmount / size.width
                        dragProgress = (dragProgress + delta).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val strokeWidth = 4.dp.toPx()

            val progressX = (effectiveProgress * width).coerceIn(0f, width)

            // 1. Draw inactive straight background line
            if (progressX < width) {
                drawLine(
                    color = inactiveColor.copy(alpha = 0.45f),
                    start = Offset(progressX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw active wavy path up to progressX
            if (progressX > 0f) {
                val wavePath = Path()
                wavePath.moveTo(0f, centerY)

                val step = 4f
                var x = 0f
                while (x <= progressX) {
                    val y = centerY + sin((x / wavelength) + phase) * waveAmplitude
                    wavePath.lineTo(x, y)
                    x += step
                }
                wavePath.lineTo(progressX, centerY)

                drawPath(
                    path = wavePath,
                    color = activeColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Draw thumb knob with glow
            val thumbRadius = if (isDragging) 8.dp.toPx() else 6.5.dp.toPx()
            drawCircle(
                color = activeColor.copy(alpha = 0.25f),
                radius = thumbRadius + 5.dp.toPx(),
                center = Offset(progressX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(progressX, centerY)
            )
        }
    }
}
