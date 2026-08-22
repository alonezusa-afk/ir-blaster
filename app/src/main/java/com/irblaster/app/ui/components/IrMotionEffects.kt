package com.irblaster.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.irblaster.app.ui.theme.NeonAmber
import com.irblaster.app.ui.theme.NeonCyan
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.NeonRed
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated IR Laser Blast & Photon Shockwave Effect.
 * Fires a radiant energy shockwave and glowing particle sparkles across the screen when transmitting.
 */
@Composable
fun IrBlastParticleOverlay(
    isTransmitting: Boolean,
    modifier: Modifier = Modifier
) {
    val shockwaveRadius = remember { Animatable(0f) }
    val shockwaveAlpha = remember { Animatable(0f) }

    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            shockwaveRadius.snapTo(0f)
            shockwaveAlpha.snapTo(1f)

            shockwaveRadius.animateTo(
                targetValue = 600f,
                animationSpec = tween(450, easing = FastOutSlowInEasing)
            )
            shockwaveAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(200, easing = LinearEasing)
            )
        }
    }

    if (shockwaveAlpha.value > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.25f)
            val currentRadius = shockwaveRadius.value

            // 1. Expanding Core Laser Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = shockwaveAlpha.value * 0.8f), NeonAmber.copy(alpha = shockwaveAlpha.value * 0.4f), Color.Transparent),
                    center = centerOffset,
                    radius = currentRadius + 50f
                ),
                center = centerOffset,
                radius = currentRadius
            )

            // 2. Crisp Outer Shockwave Edge
            drawCircle(
                color = NeonCyan.copy(alpha = shockwaveAlpha.value),
                center = centerOffset,
                radius = currentRadius,
                style = Stroke(width = 3.dp.toPx())
            )

            // 3. Radial Particle Sparks
            val sparkCount = 18
            for (i in 0 until sparkCount) {
                val angle = (i * (360f / sparkCount)) * (Math.PI / 180f)
                val sparkDist = currentRadius * 0.85f
                val sparkX = centerOffset.x + (cos(angle) * sparkDist).toFloat()
                val sparkY = centerOffset.y + (sin(angle) * sparkDist).toFloat()

                drawCircle(
                    color = NeonAmber.copy(alpha = shockwaveAlpha.value),
                    center = Offset(sparkX, sparkY),
                    radius = 3.dp.toPx()
                )
            }
        }
    }
}

/**
 * Animated Sonar Radar Scanner for Active Learning/Listening Mode.
 */
@Composable
fun SonarRadarVisualizer(
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sonarTransition")

    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (size.minDimension / 2f) - 6f

        // Concentric Range Rings
        drawCircle(color = NeonCyan.copy(alpha = 0.15f), center = center, radius = maxRadius * 0.33f, style = Stroke(1.dp.toPx()))
        drawCircle(color = NeonCyan.copy(alpha = 0.15f), center = center, radius = maxRadius * 0.66f, style = Stroke(1.dp.toPx()))
        drawCircle(color = NeonCyan.copy(alpha = 0.25f), center = center, radius = maxRadius, style = Stroke(1.5.dp.toPx()))

        if (isListening) {
            // Expanding Ping Ring
            drawCircle(
                color = NeonAmber.copy(alpha = (1f - pulseRadius).coerceIn(0f, 1f)),
                center = center,
                radius = maxRadius * pulseRadius,
                style = Stroke(2.dp.toPx())
            )

            // Rotating Radar Sweep Beam
            val sweepRad = (radarRotation * (Math.PI / 180f)).toFloat()
            val sweepEnd = Offset(
                x = center.x + (cos(sweepRad.toDouble()) * maxRadius).toFloat(),
                y = center.y + (sin(sweepRad.toDouble()) * maxRadius).toFloat()
            )

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(NeonAmber.copy(alpha = 0.9f), Color.Transparent),
                    start = sweepEnd,
                    end = center
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Center Blip
        drawCircle(
            color = if (isListening) NeonAmber else NeonCyan,
            center = center,
            radius = 3.5.dp.toPx()
        )
    }
}

/**
 * Spring Touch Bounce Modifier for physical tactical feedback.
 */
fun Modifier.springBounceClickable(
    scaleDown: Float = 0.92f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "springBounce"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Shimmer Border Modifier for highlighting newly captured signals or active cards.
 */
fun Modifier.shimmerBorder(
    enabled: Boolean = true,
    colors: List<Color> = listOf(NeonCyan, NeonAmber, NeonGreen, NeonCyan)
): Modifier = composed {
    if (!enabled) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPhase"
    )

    this.drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.sweepGradient(
                colors = colors,
                center = Offset(size.width / 2f, size.height / 2f)
            ),
            style = Stroke(width = 1.8.dp.toPx())
        )
    }
}
