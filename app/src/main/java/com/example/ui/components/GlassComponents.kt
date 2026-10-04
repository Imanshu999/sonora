package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.theme.VividCyan
import com.example.ui.theme.glassSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Subtle spring "press" feedback shared by all glass controls. */
@Composable
private fun rememberPressScale(source: MutableInteractionSource, pressed: Float = 0.96f): Float {
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    return scale
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    accent: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val source = remember { MutableInteractionSource() }
    val scale = if (onClick != null) rememberPressScale(source, 0.98f) else 1f
    var m = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .glassSurface(tokens, shape, accent, elevation = 10.dp)
    if (onClick != null) {
        m = m.clickable(interactionSource = source, indication = null, onClick = onClick)
    }
    Column(modifier = m, content = content)
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = ElectricPurple,
) {
    val tokens = LocalGlassTokens.current
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glassSurface(tokens, CircleShape, accent, elevation = 8.dp)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tokens.content, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = tokens.content, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalGlassTokens.current
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glassSurface(
                tokens,
                CircleShape,
                accent = if (selected) ElectricPurple else Color.Unspecified,
                elevation = 4.dp,
            )
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) tokens.content else tokens.contentMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .glassSurface(tokens, RoundedCornerShape(32.dp), elevation = 24.dp)
                .padding(24.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(tokens, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), elevation = 0.dp)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 40.dp, height = 4.dp)
                    .glassSurface(tokens, CircleShape, elevation = 0.dp)
            )
            Spacer(Modifier.size(12.dp))
            content()
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = Icons.Filled.Search,
) {
    val tokens = LocalGlassTokens.current
    val leading: (@Composable () -> Unit)? = leadingIcon?.let { icon ->
        @Composable { Icon(icon, contentDescription = null, tint = tokens.contentMuted) }
    }
    Box(modifier = modifier.glassSurface(tokens, CircleShape, elevation = 6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = tokens.contentMuted) },
            leadingIcon = leading,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = ElectricPurple,
                focusedTextColor = tokens.content,
                unfocusedTextColor = tokens.content,
            ),
        )
    }
}

/**
 * Full-screen ambient background built from the current artwork.
 *  - 3 colours pulled with Palette, cross-faded when the song changes
 *  - a slowly drifting gradient (single infinite transition)
 *  - on Android 12+ a heavily blurred copy of the artwork on top (one blur layer only)
 *  - on older devices the gradient alone is used (no blur fallback)
 */
@Composable
fun GlassBackground(
    artworkUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tokens = LocalGlassTokens.current

    var colors by remember { mutableStateOf(listOf(ElectricPurple, VividCyan, DarkBackground)) }

    LaunchedEffect(artworkUrl) {
        if (artworkUrl.isNullOrBlank()) {
            colors = listOf(ElectricPurple, VividCyan, DarkBackground)
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(artworkUrl)
            .allowHardware(false) // Palette needs a software bitmap
            .size(128)
            .build()
        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            val picked = withContext(Dispatchers.Default) {
                val bmp = result.drawable.toBitmap()
                val p = Palette.from(bmp).maximumColorCount(12).generate()
                val a = p.getVibrantColor(ElectricPurple.toArgb())
                val b = p.getMutedColor(VividCyan.toArgb())
                val c = p.getDarkMutedColor(DarkBackground.toArgb())
                listOf(Color(a), Color(b), Color(c))
            }
            colors = picked
        }
    }

    val c1 by animateColorAsState(colors[0], tween(900), label = "bg1")
    val c2 by animateColorAsState(colors[1], tween(900), label = "bg2")
    val c3 by animateColorAsState(colors[2], tween(900), label = "bg3")

    val drift by rememberInfiniteTransition(label = "drift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse),
        label = "driftValue",
    )

    // Near-black base so text stays readable whatever the artwork colours are.
    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // drift the gradient centre gently
                    translationX = (drift - 0.5f) * 60f
                    translationY = (0.5f - drift) * 60f
                    scaleX = 1.25f
                    scaleY = 1.25f
                }
                .alpha(0.55f)
                .background(Brush.linearGradient(listOf(c1, c2, c3)))
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.28f)
                    .blur(tokens.blurRadius),
            )
        }
        // Dark veil keeps contrast high for glass surfaces and text on top.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.70f))
                    )
                )
        )
    }
}
