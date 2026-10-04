package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.SonoraTheme

/**
 * Central glassmorphism tokens. One instance per theme variant (dark / amoled / light).
 * Everything glass-related in the app reads from here so the look can be tuned in one place.
 */
@Immutable
data class GlassTokens(
    /** Base colour of the frosted surface. */
    val tint: Color,
    /** Opacity of [tint]. Lower = more see-through. */
    val tintAlpha: Float,
    /** Top / bottom colours of the 1dp translucent border gradient. */
    val borderTop: Color,
    val borderBottom: Color,
    /** Soft inner highlight line drawn along the top edge. */
    val highlight: Color,
    /** Shadow colour under floating glass. */
    val shadow: Color,
    val cornerRadius: Dp,
    val elevation: Dp,
    /** Blur radius for blurred artwork backgrounds (API 31+). */
    val blurRadius: Dp,
    /** Text/icon colour that reads well on this glass. */
    val content: Color,
    val contentMuted: Color,
)

val GlassDark = GlassTokens(
    tint = Color(0xFF1B1F3A),
    tintAlpha = 0.52f,
    borderTop = Color.White.copy(alpha = 0.30f),
    borderBottom = Color.White.copy(alpha = 0.06f),
    highlight = Color.White.copy(alpha = 0.35f),
    shadow = Color.Black.copy(alpha = 0.55f),
    cornerRadius = 28.dp,
    elevation = 18.dp,
    blurRadius = 60.dp,
    content = Color(0xFFF8FAFC),
    contentMuted = Color(0xFFB4BED0),
)

val GlassAmoled = GlassTokens(
    tint = Color(0xFF0E0E0E),
    tintAlpha = 0.60f,
    borderTop = Color.White.copy(alpha = 0.22f),
    borderBottom = Color.White.copy(alpha = 0.04f),
    highlight = Color.White.copy(alpha = 0.28f),
    shadow = Color.Black,
    cornerRadius = 28.dp,
    elevation = 20.dp,
    blurRadius = 60.dp,
    content = Color.White,
    contentMuted = Color(0xFFA7B0C0),
)

val GlassLight = GlassTokens(
    tint = Color.White,
    tintAlpha = 0.58f,
    borderTop = Color.White.copy(alpha = 0.90f),
    borderBottom = Color(0xFF94A3B8).copy(alpha = 0.25f),
    highlight = Color.White.copy(alpha = 0.95f),
    shadow = Color(0xFF334155).copy(alpha = 0.35f),
    cornerRadius = 28.dp,
    elevation = 14.dp,
    blurRadius = 50.dp,
    content = Color(0xFF0F172A),
    contentMuted = Color(0xFF475569),
)

fun glassTokensFor(theme: SonoraTheme, isDark: Boolean): GlassTokens = when {
    theme == SonoraTheme.AMOLED -> GlassAmoled
    isDark -> GlassDark
    else -> GlassLight
}

val LocalGlassTokens = staticCompositionLocalOf { GlassDark }

/**
 * Frosted-glass look without needing a backdrop-blur library:
 * soft shadow, translucent vertical gradient fill, optional accent wash,
 * 1dp gradient border and a thin inner highlight on the top edge.
 *
 * Real "see-through blur" comes from the blurred artwork background
 * (see [com.example.ui.components.GlassBackground]) sitting behind these surfaces.
 */
fun Modifier.glassSurface(
    tokens: GlassTokens,
    shape: Shape = RoundedCornerShape(tokens.cornerRadius),
    accent: Color = Color.Unspecified,
    elevation: Dp = tokens.elevation,
): Modifier {
    var m: Modifier = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = tokens.shadow,
            spotColor = tokens.shadow,
        )
        .background(
            brush = Brush.verticalGradient(
                listOf(
                    tokens.tint.copy(alpha = (tokens.tintAlpha + 0.08f).coerceAtMost(1f)),
                    tokens.tint.copy(alpha = tokens.tintAlpha),
                )
            ),
            shape = shape,
        )
    if (accent != Color.Unspecified) {
        m = m.background(accent.copy(alpha = 0.14f), shape)
    }
    return m
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(listOf(tokens.borderTop, tokens.borderBottom)),
            shape = shape,
        )
        .drawWithContent {
            drawContent()
            val y = 1.dp.toPx()
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, tokens.highlight, Color.Transparent)
                ),
                start = Offset(size.width * 0.10f, y),
                end = Offset(size.width * 0.90f, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
}
