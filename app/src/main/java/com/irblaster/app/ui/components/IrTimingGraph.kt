package com.irblaster.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.theme.NeonCyan
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.SurfaceVariant
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextSecondary
import kotlin.math.max

// High-tech Instrument Palette
val GraphCyan = Color(0xFF00E5FF)
val GraphAmber = Color(0xFFFFB300)
val GraphGreen = Color(0xFF00E676)
val GraphDarkBg = Color(0xFF0A0E17)
val GraphGridLine = Color(0xFF1B2433)

/**
 * Clean Digital Logic Timing Graph & Pulse Duration Bar Analyzer.
 *
 * Features:
 * - Top Pane: Precision Digital Logic Timing Diagram (HIGH 3.3V / LOW 0.0V transitions).
 * - Bottom Pane: Pulse Duration Bar Histogram & Multi-segment Optical Signal Meter.
 * - Millisecond timeline crosshair markings (0ms, 10ms, 20ms, 30ms, 40ms, 50ms...).
 * - Zoom controls (1x to 8x) and smooth horizontal scrolling.
 */
@Composable
fun IrTimingGraph(
    signal: IrSignal?,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    val scrollState = rememberScrollState()

    // Smooth scanline cursor animation during listening mode
    val transition = rememberInfiniteTransition(label = "scanTransition")
    val scanProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanProgress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GraphDarkBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, GraphCyan.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header Bar: Title, Metric Readout, and Zoom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isListening) GraphAmber else if (signal != null) GraphGreen else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DIGITAL TIMING ANALYZER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GraphCyan,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = GraphCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        if (signal != null) {
                            val totalMs = signal.timings.sum() / 1000.0
                            Text(
                                text = "${signal.timings.size} pulses • ${"%.2f".format(totalMs)} ms • ${(zoomScale * 100).toInt()}% Zoom",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = if (isListening) "RECEIVER ACTIVE • Listening on Photodiode..." else "STANDBY • Ready to Learn",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isListening) GraphAmber else TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Zoom Control Buttons
                if (signal != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalIconButton(
                            onClick = { zoomScale = (zoomScale * 1.35f).coerceAtMost(8.0f) },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceVariant,
                                contentColor = GraphCyan
                            )
                        ) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { zoomScale = (zoomScale / 1.35f).coerceAtLeast(0.4f) },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceVariant,
                                contentColor = GraphCyan
                            )
                        ) {
                            Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { zoomScale = 1.0f },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceVariant,
                                contentColor = GraphCyan
                            )
                        ) {
                            Icon(Icons.Filled.FitScreen, contentDescription = "Reset Zoom", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Dual Graph View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF060911))
                    .border(1.dp, GraphGridLine, RoundedCornerShape(8.dp))
            ) {
                if (signal != null && signal.timings.isNotEmpty()) {
                    val totalUs = signal.timings.sum().toFloat()
                    val baseWidthDp = 340.dp
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
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            drawDigitalGrid(totalUs = totalUs)
                            // Top half: Digital Logic Waveform
                            drawLogicAnalyzerTrace(timings = signal.timings)
                            // Bottom half: Pulse Duration Bar Histogram
                            drawPulseDurationBars(timings = signal.timings)
                        }
                    }
                } else {
                    // Receiver Standby / Active Scanning Graph
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        drawDigitalGrid(totalUs = 50000f)
                        if (isListening) {
                            drawLiveListeningBars(progress = scanProgress)
                        }
                    }

                    if (!isListening) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Sensors,
                                contentDescription = null,
                                tint = GraphCyan.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Point remote control at dongle (1-3 cm) & press button",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Bottom Legend
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(GraphCyan, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("HIGH (3.3V Mark)", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).background(GraphAmber, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duration Bar", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                }

                Text("Logic Analyzer ▾", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

/**
 * Draws digital logic grid with time markings (0ms, 10ms, 20ms, 30ms...).
 */
private fun DrawScope.drawDigitalGrid(totalUs: Float) {
    val midY = size.height * 0.50f
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

    // Center divider
    drawLine(
        color = GraphGridLine.copy(alpha = 0.8f),
        start = Offset(0f, midY),
        end = Offset(size.width, midY),
        strokeWidth = 1.dp.toPx()
    )

    // Voltage rail reference lines
    val highRail = size.height * 0.12f
    val lowRail = size.height * 0.42f

    drawLine(color = GraphGridLine, start = Offset(0f, highRail), end = Offset(size.width, highRail), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
    drawLine(color = GraphGridLine, start = Offset(0f, lowRail), end = Offset(size.width, lowRail), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)

    // Time divisions (10ms steps)
    val totalMs = max(1f, totalUs / 1000f)
    val divisions = (totalMs / 5f).toInt().coerceIn(5, 30)
    val stepX = size.width / divisions

    for (i in 1 until divisions) {
        val x = stepX * i
        drawLine(
            color = GraphGridLine,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
    }
}

/**
 * Draws the digital logic square-wave timing trace (Top half).
 */
private fun DrawScope.drawLogicAnalyzerTrace(timings: List<Int>) {
    if (timings.isEmpty()) return

    val totalUs = timings.sum().toFloat().coerceAtLeast(1f)
    val widthPx = size.width
    val highY = size.height * 0.12f
    val lowY = size.height * 0.42f

    val path = Path()
    var currentX = 0f
    var isHigh = true

    path.moveTo(0f, lowY)

    for (duration in timings) {
        val pulseWidthPx = (duration / totalUs) * widthPx
        val nextX = currentX + pulseWidthPx
        val targetY = if (isHigh) highY else lowY

        path.lineTo(currentX, targetY)
        path.lineTo(nextX, targetY)

        currentX = nextX
        isHigh = !isHigh
    }

    path.lineTo(currentX, lowY)

    // Glow line
    drawPath(
        path = path,
        color = GraphCyan.copy(alpha = 0.3f),
        style = Stroke(width = 4.dp.toPx())
    )

    // Core crisp line
    drawPath(
        path = path,
        color = GraphCyan,
        style = Stroke(width = 1.8.dp.toPx())
    )
}

/**
 * Draws the pulse duration bar histogram (Bottom half).
 */
private fun DrawScope.drawPulseDurationBars(timings: List<Int>) {
    if (timings.isEmpty()) return

    val totalUs = timings.sum().toFloat().coerceAtLeast(1f)
    val widthPx = size.width
    val baseY = size.height * 0.95f
    val maxBarHeight = size.height * 0.38f

    val maxDuration = (timings.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)
    var currentX = 0f

    for ((index, duration) in timings.withIndex()) {
        val pulseWidthPx = (duration / totalUs) * widthPx
        val barHeight = (duration / maxDuration) * maxBarHeight
        val isMark = index % 2 == 0

        val barColor = if (isMark) GraphCyan else GraphAmber

        // Draw bar
        drawRect(
            color = barColor.copy(alpha = 0.85f),
            topLeft = Offset(currentX, baseY - barHeight),
            size = Size(max(1.5f, pulseWidthPx - 1f), barHeight)
        )

        currentX += pulseWidthPx
    }
}

/**
 * Draws active optical level indicator bars during live listening mode.
 */
private fun DrawScope.drawLiveListeningBars(progress: Float) {
    val startY = size.height * 0.58f
    val totalHeight = size.height * 0.34f
    val barCount = 24
    val barWidth = (size.width / barCount) - 3f

    for (i in 0 until barCount) {
        val x = i * (barWidth + 3f)
        val normalizedIdx = i.toFloat() / barCount
        val dist = kotlin.math.abs(normalizedIdx - progress)
        val intensity = if (dist < 0.2f) (1.0f - (dist / 0.2f)) else 0.1f

        val barHeight = totalHeight * intensity
        val color = if (intensity > 0.6f) GraphCyan else GraphAmber

        drawRect(
            color = color.copy(alpha = intensity.coerceIn(0.2f, 0.95f)),
            topLeft = Offset(x, (startY + totalHeight) - barHeight),
            size = Size(barWidth, barHeight)
        )
    }
}
