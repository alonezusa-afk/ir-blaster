package com.irblaster.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass UI Modifier providing realistic translucent glassmorphism:
 * - Frosted acrylic / liquid translucent fill.
 * - Specular gradient highlight along top edge.
 * - Dual-tone subtle neon rim lighting.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x33121829),
    borderGradient: List<Color> = listOf(Color(0x6600E5FF), Color(0x117C4DFF), Color(0x4400E5FF)),
    borderWidth: Dp = 1.2.dp,
    hasSpecularSheen: Boolean = true
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .drawWithContent {
        drawContent()
        if (hasSpecularSheen) {
            // Top-edge light refraction sheen
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent)
                ),
                start = Offset(0f, 1f),
                end = Offset(size.width, 1f),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = borderGradient,
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = shape
    )

/**
 * Ambient Animated Mesh Gradient Container for the whole screen background.
 */
@Composable
fun LiquidAmbientBackground(
    theme: AppThemePalette,
    isAmoledMode: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    if (isAmoledMode) {
        Box(
            modifier = modifier
                .background(Color(0xFF000000))
        ) {
            content()
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "ambientMesh")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPhase"
    )

    Box(
        modifier = modifier
            .drawBehind {
                // Base background fill
                drawRect(theme.backgroundBase)

                // Ambient Radial Light Spot 1 (Top-Left Accent Orb)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.primaryAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * (0.2f + phase * 0.15f), size.height * (0.15f + phase * 0.1f)),
                        radius = size.width * 0.85f
                    ),
                    center = Offset(size.width * (0.2f + phase * 0.15f), size.height * (0.15f + phase * 0.1f)),
                    radius = size.width * 0.85f
                )

                // Ambient Radial Light Spot 2 (Bottom-Right Accent Orb)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.secondaryAccent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * (0.85f - phase * 0.15f), size.height * (0.8f - phase * 0.1f)),
                        radius = size.width * 0.75f
                    ),
                    center = Offset(size.width * (0.85f - phase * 0.15f), size.height * (0.8f - phase * 0.1f)),
                    radius = size.width * 0.75f
                )
            }
    ) {
        content()
    }
}
