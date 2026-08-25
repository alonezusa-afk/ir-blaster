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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.theme.AppThemePalette
import com.irblaster.app.ui.theme.LocalAppTheme
import com.irblaster.app.ui.theme.NeonAmber
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextPrimary
import com.irblaster.app.ui.theme.TextSecondary
import com.irblaster.app.ui.theme.liquidGlass

/**
 * Compact Flipper Zero Style IR Waveform Graph Analyzer.
 * Features:
 * - Slim compact profile (100dp) matching Flipper Zero 128x64 LCD instrument display.
 * - Crisp square-wave logic timing with small compact mark pillars.
 * - Live sweep cursor during listening/receiving.
 * - Automatic protocol identification tag (e.g. NEC, Samsung, SIRC, RC5, or RAW).
 */
@Composable
fun FlipperIrWaveformGraph(
    signal: IrSignal?,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonAmber
) {
    val theme = LocalAppTheme.current
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var scrollOffset by remember { mutableFloatStateOf(0.0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "flipperSweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                backgroundColor = Color(0xFF06090E),
                borderGradient = listOf(accentColor.copy(alpha = 0.5f), Color(0x22121829), accentColor.copy(alpha = 0.3f)),
                borderWidth = 1.dp
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // -------------------------------------------------------------
            // FLIPPER ZERO COMPACT HEADER BAR
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isListening) NeonGreen else accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isListening) "FLIPPER IR RX // LIVE" else "FLIPPER IR ANALYZER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isListening) NeonGreen else accentColor
                    )
                }

                // Summary Metadata Pill
                if (signal != null && signal.timings.isNotEmpty()) {
                    val totalDurationMs = signal.timings.sum() / 1000f
                    val detectedProtocol = detectProtocolName(signal.timings)
                    Text(
                        text = "$detectedProtocol • ${signal.timings.size}p • ${"%.1f".format(totalDurationMs)}ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = if (isListening) "LISTENING 38kHz..." else "IDLE // READY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isListening) NeonGreen else TextMuted
                    )
                }

                // Compact Zoom Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel / 1.4f).coerceAtLeast(0.5f) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Filled.ZoomOut, contentDescription = "-", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${"%.1f".format(zoomLevel)}x",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel * 1.4f).coerceAtMost(8.0f) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Filled.ZoomIn, contentDescription = "+", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // -------------------------------------------------------------
            // FLIPPER RETRO LCD WAVEFORM CANVAS
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF040609))
                    .border(0.8.dp, Color(0xFF131F2E), RoundedCornerShape(6.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 10.0f)
                            scrollOffset = (scrollOffset + pan.x).coerceIn(-5000f, 0f)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Subtle LCD Grid Matrix
                    val gridSpacingX = 24.dp.toPx()
                    val gridSpacingY = 14.dp.toPx()
                    var xGrid = 0f
                    while (xGrid < w) {
                        drawLine(
                            color = Color(0x1200E5FF),
                            start = Offset(xGrid, 0f),
                            end = Offset(xGrid, h),
                            strokeWidth = 0.5f
                        )
                        xGrid += gridSpacingX
                    }

                    var yGrid = 0f
                    while (yGrid < h) {
                        drawLine(
                            color = Color(0x1200E5FF),
                            start = Offset(0f, yGrid),
                            end = Offset(w, yGrid),
                            strokeWidth = 0.5f
                        )
                        yGrid += gridSpacingY
                    }

                    val baselineY = h - 14f
                    val highY = 16f
                    val pillarHeight = baselineY - highY

                    // Baseline reference line (Space 0V)
                    drawLine(
                        color = Color(0x334A6572),
                        start = Offset(0f, baselineY),
                        end = Offset(w, baselineY),
                        strokeWidth = 1f
                    )

                    // 2. Render IR Pulse Waveform with Flipper Style Compact Pillars
                    val timings = signal?.timings ?: emptyList()

                    if (timings.isNotEmpty()) {
                        val totalDuration = timings.sum().toFloat().coerceAtLeast(1000f)
                        val totalWidthPx = (w * zoomLevel).coerceAtLeast(w)
                        val usToPx = totalWidthPx / totalDuration

                        var currentX = scrollOffset

                        for (i in timings.indices) {
                            val durationUs = timings[i].toFloat()
                            val pulseWidth = (durationUs * usToPx).coerceAtLeast(1.5f)
                            val isMark = (i % 2 == 0) // Even = Mark (High carrier), Odd = Space (Low pause)

                            if (currentX + pulseWidth >= 0 && currentX <= w) {
                                if (isMark) {
                                    // Filled Compact Pillar with Specular Top Sheen
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.9f),
                                                accentColor.copy(alpha = 0.45f)
                                            ),
                                            startY = highY,
                                            endY = baselineY
                                        ),
                                        topLeft = Offset(currentX, highY),
                                        size = Size(pulseWidth, pillarHeight)
                                    )

                                    // Top High Line of Pillar
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.9f),
                                        start = Offset(currentX, highY),
                                        end = Offset(currentX + pulseWidth, highY),
                                        strokeWidth = 1.5f
                                    )

                                    // Vertical Pillar Edges
                                    drawLine(
                                        color = accentColor,
                                        start = Offset(currentX, baselineY),
                                        end = Offset(currentX, highY),
                                        strokeWidth = 1.2f
                                    )
                                    drawLine(
                                        color = accentColor,
                                        start = Offset(currentX + pulseWidth, highY),
                                        end = Offset(currentX + pulseWidth, baselineY),
                                        strokeWidth = 1.2f
                                    )
                                } else {
                                    // Space: flat line at baseline
                                    drawLine(
                                        color = Color(0xFF1E293B),
                                        start = Offset(currentX, baselineY),
                                        end = Offset(currentX + pulseWidth, baselineY),
                                        strokeWidth = 1.5f
                                    )
                                }
                            }
                            currentX += pulseWidth
                        }
                    } else if (isListening) {
                        // 3. Live Flipper Radar Sweep / Oscilloscope Scan Line
                        val sweepX = w * sweepProgress
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, NeonGreen, Color.Transparent),
                                startX = sweepX - 25f,
                                endX = sweepX + 25f
                            ),
                            start = Offset(sweepX, 0f),
                            end = Offset(sweepX, h),
                            strokeWidth = 2f
                        )

                        // Center Waiting Indicator
                        drawCircle(
                            color = NeonGreen.copy(alpha = 0.3f),
                            center = Offset(w / 2f, h / 2f),
                            radius = 12.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Protocol recognition helper
 */
private fun detectProtocolName(timings: List<Int>): String {
    if (timings.isEmpty()) return "RAW"
    val leader = timings[0]
    val second = if (timings.size > 1) timings[1] else 0

    return when {
        leader in 8000..10000 && second in 4000..5000 -> "NEC (38kHz)"
        leader in 8000..10000 && second in 2000..3000 -> "NEC REPEAT"
        leader in 4000..5000 && second in 4000..5000 -> "SAMSUNG (38kHz)"
        leader in 2000..2800 && second in 400..800 -> "SONY SIRC (40kHz)"
        leader in 800..1000 -> "RC5 (36kHz)"
        timings.size > 100 -> "AC RAW (Multi-byte)"
        else -> "RAW (${timings.size}p)"
    }
}
