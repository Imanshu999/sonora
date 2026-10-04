package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NavItem
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.glassSurface

/**
 * Floating pill-shaped glass bottom bar.
 *
 * The caller positions it (side margins + bottom margin + navigation-bar inset);
 * this composable only draws the pill, the sliding glass indicator and the items.
 */
@Composable
fun FloatingGlassNav(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalGlassTokens.current
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier
            .height(68.dp)
            .glassSurface(tokens, RoundedCornerShape(34.dp))
            .padding(6.dp)
            .testTag("bottom_nav_bar")
    ) {
        val itemWidth = maxWidth / items.size

        // Sliding glass indicator (spring) behind the selected item.
        val indicatorX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
            label = "navIndicator",
        )
        Box(
            modifier = Modifier
                .offset(x = indicatorX)
                .width(itemWidth)
                .fillMaxHeight()
                .glassSurface(
                    tokens = tokens,
                    shape = RoundedCornerShape(28.dp),
                    accent = ElectricPurple,
                    elevation = 0.dp,
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.18f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "navIconScale",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (!selected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onSelect(index)
                        }
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (selected) NeonViolet else tokens.contentMuted,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                    )
                    Text(
                        text = item.title,
                        color = if (selected) tokens.content else tokens.contentMuted,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
