package com.irblaster.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.theme.DeepBackground
import com.irblaster.app.ui.theme.NeonAmber
import com.irblaster.app.ui.theme.NeonCyan
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.NeonRed
import com.irblaster.app.ui.theme.SurfaceDark
import com.irblaster.app.ui.theme.SurfaceVariant
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextPrimary
import com.irblaster.app.ui.theme.TextSecondary
import com.irblaster.app.ui.theme.VioletDark
import com.irblaster.app.ui.theme.VioletLight
import kotlin.math.max

/**
 * Real-time Logic Analyzer & Oscilloscope waveform graph for IR signals.
 *
 * Renders pulse (HIGH / Mark) and space (LOW / Gap) transitions over time with:
 * - Oscilloscope gridlines and time ticks (µs / ms)
 * - Neon glowing digital square-wave trace
 * - Interactive Horizontal pan & Zoom scale
 * - Live scanning sweep animation while awaiting signals
 */
@Composable
fun IrWaveformGraph(
    signal: IrSignal?,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    val scrollState = rememberScrollState()

    // Live scanning animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscopeScan")
    val scanSweepX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanSweepX"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090A14))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header Bar: Title, Metrics, and Zoom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isListening) NeonCyan else if (signal != null) NeonGreen else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (signal != null) "Signal Waveform (${signal.timings.size} pulses)" else "Logic Analyzer",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = VioletLight
                        )
                    }

                    if (signal != null) {
                        val totalDurationMs = signal.timings.sum() / 1000.0
                        Text(
                            text = "Total Time: ${"%.2f".format(totalDurationMs)} ms • Scale: ${(zoomScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    } else if (isListening) {
                        Text(
                            text = "Live Scanner Active • Awaiting IR Pulses...",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                // Zoom Control Buttons
                if (signal != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalIconButton(
                            onClick = { zoomScale = (zoomScale * 1.35f).coerceAtMost(8.0f) },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceVariant,
                                contentColor = VioletLight
                            )
                        ) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { zoomScale = (zoomScale / 1.35f).coerceAtLeast(0.4f) },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceVariant,
                                contentColor = VioletLight
                            )
                        ) {
                            Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { zoomScale = 1.0f },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceVariant,
                                contentColor = VioletLight
                            )
                        ) {
                            Icon(Icons.Filled.FitScreen, contentDescription = "Reset Zoom", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Waveform Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF04040A))
                    .border(1.dp, SurfaceVariant, RoundedCornerShape(8.dp))
            ) {
                if (signal != null && signal.timings.isNotEmpty()) {
                    val totalUs = signal.timings.sum().toFloat()
                    val baseWidthDp = 340.dp
                    // Scaled content width
                    val canvasWidthDp = max(baseWidthDp.value, (baseWidthDp.value * (totalUs / 45000f) * zoomScale)).dp

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(canvasWidthDp)
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            drawOscilloscopeGrid()
                            drawDigitalIrWaveform(signal.timings)
                        }
                    }
                } else {
                    // Empty or Live Listening Scanning Animation
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        drawOscilloscopeGrid()

                        if (isListening) {
                            // Draw animated laser radar/sweep line
                            val sweepX = size.width * scanSweepX
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.8f),
                                start = Offset(sweepX, 0f),
                                end = Offset(sweepX, size.height),
                                strokeWidth = 2.dp.toPx()
                            )

                            // Subtle radar trail gradient
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.15f)),
                                    startX = max(0f, sweepX - 80.dp.toPx()),
                                    endX = sweepX
                                ),
                                topLeft = Offset(max(0f, sweepX - 80.dp.toPx()), 0f),
                                size = Size(minOf(sweepX, 80.dp.toPx()), size.height)
                            )
                        } else {
                            // Center Idle Baseline
                            val midY = size.height * 0.75f
                            drawLine(
                                color = TextMuted.copy(alpha = 0.4f),
                                start = Offset(0f, midY),
                                end = Offset(size.width, midY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    if (!isListening) {
                        Text(
                            text = "Awaiting IR Reception (Aim Remote at Dongle)",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Legend / Time Marker Hint
            if (signal != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(NeonCyan, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("HIGH = IR Carrier Pulse (Mark)", fontSize = 10.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(8.dp).background(VioletLight.copy(alpha = 0.5f), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOW = Space (Gap)", fontSize = 10.sp, color = TextSecondary)
                    }

                    Text("Scroll horizontally to inspect →", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

/**
 * Draws digital logic analyzer gridlines with voltage / state labels.
 */
private fun DrawScope.drawOscilloscopeGrid() {
    val gridColor = Color(0xFF161726)
    val highY = size.height * 0.22f
    val lowY = size.height * 0.78f

    // High / Low reference dashed lines
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    drawLine(
        color = gridColor,
        start = Offset(0f, highY),
        end = Offset(size.width, highY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
    )

    drawLine(
        color = gridColor,
        start = Offset(0f, lowY),
        end = Offset(size.width, lowY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
    )

    // Vertical time division lines
    val divisions = 10
    val stepX = size.width / divisions
    for (i in 1 until divisions) {
        val x = stepX * i
        drawLine(
            color = gridColor.copy(alpha = 0.5f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
    }
}

/**
 * Renders digital IR square wave transitions (Mark / Space).
 */
private fun DrawScope.drawDigitalIrWaveform(timings: List<Int>) {
    if (timings.isEmpty()) return

    val totalUs = timings.sum().toFloat().coerceAtLeast(1f)
    val widthPx = size.width
    val heightPx = size.height

    val highY = heightPx * 0.22f
    val lowY = heightPx * 0.78f

    val wavePath = Path()
    val fillPath = Path()

    var currentX = 0f
    var isHigh = true // First pulse is always MARK (HIGH)

    wavePath.moveTo(0f, lowY)
    fillPath.moveTo(0f, lowY)

    for ((index, duration) in timings.withIndex()) {
        val pulseWidthPx = (duration / totalUs) * widthPx
        val nextX = currentX + pulseWidthPx
        val targetY = if (isHigh) highY else lowY

        // Vertical Edge transition
        wavePath.lineTo(currentX, targetY)

        // Horizontal Pulse level
        wavePath.lineTo(nextX, targetY)

        if (isHigh) {
            // Add pulse fill polygon
            fillPath.lineTo(currentX, targetY)
            fillPath.lineTo(nextX, targetY)
            fillPath.lineTo(nextX, lowY)
            fillPath.lineTo(currentX, lowY)
        }

        currentX = nextX
        isHigh = !isHigh // Alternate between Mark (HIGH) and Space (LOW)
    }

    wavePath.lineTo(currentX, lowY)
    fillPath.lineTo(currentX, lowY)
    fillPath.close()

    // 1. Draw Subtle Pulse Fill
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent),
            startY = highY,
            endY = lowY
        )
    )

    // 2. Draw Glow Behind Trace
    drawPath(
        path = wavePath,
        color = NeonCyan.copy(alpha = 0.35f),
        style = Stroke(width = 6.dp.toPx())
    )

    // 3. Draw Sharp Digital Trace Line
    drawPath(
        path = wavePath,
        color = NeonCyan,
        style = Stroke(width = 2.dp.toPx())
    )
}
