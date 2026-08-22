package com.irblaster.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.components.IrTimingGraph
import com.irblaster.app.ui.components.IrWaterfallGraph
import com.irblaster.app.ui.components.IrWaveformGraph
import com.irblaster.app.ui.theme.CardSurface
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
import com.irblaster.app.ui.theme.VioletPrimary
import com.irblaster.app.viewmodel.ConnectionState
import com.irblaster.app.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isDebugConsoleExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveSignalName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DeepBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ---------------------------------------------------------
            // 1. CONNECTION HEADER
            // ---------------------------------------------------------
            ConnectionHeader(
                connectionState = uiState.connectionState,
                statusBanner = uiState.statusBanner,
                isListening = uiState.isListening,
                isTransmitting = uiState.isTransmitting,
                onRequestPermission = onRequestPermission
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // NAVIGATION TABS
            // ---------------------------------------------------------
            val tabs = listOf("Clone / Listen", "Transmit & Presets")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceVariant,
                contentColor = VioletLight,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = VioletPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) VioletLight else TextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Filled.Hearing else Icons.AutoMirrored.Filled.Send,
                                contentDescription = title,
                                tint = if (selectedTab == index) VioletLight else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // MAIN TAB CONTENT
            // ---------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> CloneListenTab(
                        isConnected = uiState.connectionState is ConnectionState.Connected,
                        isListening = uiState.isListening,
                        lastCapturedSignal = uiState.lastCapturedSignal,
                        onToggleListen = { viewModel.toggleListen() },
                        onReplaySignal = { signal -> viewModel.transmitSignal(signal) },
                        onOpenSaveDialog = {
                            saveSignalName = uiState.lastCapturedSignal?.name ?: ""
                            showSaveDialog = true
                        }
                    )
                    1 -> TransmitTab(
                        isConnected = uiState.connectionState is ConnectionState.Connected,
                        isTransmitting = uiState.isTransmitting,
                        signals = uiState.capturedSignals,
                        lastCapturedSignal = uiState.lastCapturedSignal,
                        onTransmitSignal = { viewModel.transmitSignal(it) },
                        onTransmitRaw = { freq, raw -> viewModel.transmitRaw(freq, raw) },
                        onDeleteSignal = { viewModel.deleteSignal(it) }
                    )
                }
            }

            // ---------------------------------------------------------
            // 4. REAL-TIME DEBUG CONSOLE
            // ---------------------------------------------------------
            DebugConsole(
                logs = uiState.logs,
                isExpanded = isDebugConsoleExpanded,
                onToggle = { isDebugConsoleExpanded = !isDebugConsoleExpanded },
                onClear = { viewModel.clearLogs() }
            )
        }
    }

    // Save Captured Signal Dialog
    if (showSaveDialog && uiState.lastCapturedSignal != null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Save Captured IR Signal", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Give this captured signal a recognizable name:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = saveSignalName,
                        onValueChange = { saveSignalName = it },
                        label = { Text("Signal Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCapturedSignal(saveSignalName)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// =====================================================================
// 1. CONNECTION HEADER
// =====================================================================
@Composable
fun ConnectionHeader(
    connectionState: ConnectionState,
    statusBanner: String,
    isListening: Boolean,
    isTransmitting: Boolean,
    onRequestPermission: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "headerPulse")

    val (badgeColor, statusTitle, statusSubtitle, showPermissionBtn) = when (connectionState) {
        is ConnectionState.Connected -> {
            val mode = if (connectionState.isSerial) "Serial CDC" else "Raw Bulk"
            Quadruple(
                if (isListening) NeonCyan else if (isTransmitting) NeonAmber else NeonGreen,
                "Connected: ${connectionState.vidHex}:${connectionState.pidHex}",
                "$mode • ${connectionState.deviceName}",
                false
            )
        }
        is ConnectionState.PermissionRequested -> {
            Quadruple(
                NeonAmber,
                "Permission Requested (${connectionState.vidHex}:${connectionState.pidHex})",
                "Tap 'Grant Permission' below if system dialog is not visible",
                true
            )
        }
        is ConnectionState.DeviceDetected -> {
            Quadruple(
                NeonCyan,
                "Device Found (${connectionState.vidHex}:${connectionState.pidHex})",
                "Tap 'Connect Device' to request USB access",
                true
            )
        }
        is ConnectionState.Error -> {
            Quadruple(
                NeonRed,
                "Connection Error",
                connectionState.message,
                true
            )
        }
        is ConnectionState.Disconnected -> {
            Quadruple(
                NeonRed,
                "Dongle Disconnected",
                "Plug in USB-C IR Blaster (or tap scan)",
                false
            )
        }
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val animatedColor by animateColorAsState(
        targetValue = badgeColor,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "badgeColorAnim"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawCircle(
                    color = animatedColor.copy(alpha = 0.12f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Status Dot with Ripple
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(animatedColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(if (connectionState is ConnectionState.Connected || isListening || connectionState is ConnectionState.DeviceDetected) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (connectionState is ConnectionState.Connected) Icons.Filled.Usb else Icons.Filled.UsbOff,
                    contentDescription = "Status Icon",
                    tint = animatedColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Direct Permission Request Button if device is detected or requested
            if (showPermissionBtn) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletPrimary
                    )
                ) {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grant USB Permission / Connect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// =====================================================================
// 2. CLONING / LISTEN TAB
// =====================================================================
@Composable
fun CloneListenTab(
    isConnected: Boolean,
    isListening: Boolean,
    lastCapturedSignal: IrSignal?,
    onToggleListen: () -> Unit,
    onReplaySignal: (IrSignal) -> Unit,
    onOpenSaveDialog: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "listeningRipple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rippleScale"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Hardware Diagnostic Hint
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Requires receiver photodiode on dongle (2 diodes on tip) to learn signals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Listen Action Button
        Button(
            onClick = onToggleListen,
            enabled = isConnected,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .scale(if (isListening) rippleScale else 1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) NeonRed else VioletPrimary,
                disabledContainerColor = SurfaceVariant
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Hearing,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isListening) "Stop Listening" else "Start Listening / Learn Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Live Digital Logic Timing Diagram & Pulse Duration Bar Graph
        IrTimingGraph(
            signal = lastCapturedSignal,
            isListening = isListening,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.35f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Captured Signal Details & Actions Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (lastCapturedSignal != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lastCapturedSignal.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${lastCapturedSignal.timings.size} pulses @ ${lastCapturedSignal.frequency}Hz (${lastCapturedSignal.timings.sum() / 1000}ms)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonAmber
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onReplaySignal(lastCapturedSignal) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Replay", fontSize = 12.sp)
                                }

                                FilledTonalButton(
                                    onClick = onOpenSaveDialog,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = NeonCyan.copy(alpha = 0.2f),
                                        contentColor = NeonCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Raw Microsecond Pulse Intervals
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepBackground)
                                .padding(8.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = lastCapturedSignal.formattedString,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.5.sp,
                                        color = NeonCyan,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.SettingsRemote,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isListening) "Listening on IR photodiode..." else "Click 'Start Listening' to capture IR waveforms",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isListening) NeonCyan else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 3. TRANSMIT TAB (PRESETS & SAVED SIGNALS)
// =====================================================================
@Composable
fun TransmitTab(
    isConnected: Boolean,
    isTransmitting: Boolean,
    signals: List<IrSignal>,
    lastCapturedSignal: IrSignal?,
    onTransmitSignal: (IrSignal) -> Unit,
    onTransmitRaw: (Int, String) -> Unit,
    onDeleteSignal: (IrSignal) -> Unit
) {
    var rawFreq by remember { mutableStateOf("38000") }
    var rawTimingsText by remember { mutableStateOf("") }
    var isManualExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Presets Bar
        item {
            Text(
                "Standard Test Presets (NEC 38kHz)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VioletLight
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PresetButton(
                        label = "TV Power",
                        icon = Icons.Filled.PowerSettingsNew,
                        color = NeonRed,
                        enabled = isConnected && !isTransmitting,
                        onClick = {
                            onTransmitRaw(38000, "9000, 4500, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560")
                        }
                    )
                }
                item {
                    PresetButton(
                        label = "Mute",
                        icon = Icons.AutoMirrored.Filled.VolumeMute,
                        color = NeonAmber,
                        enabled = isConnected && !isTransmitting,
                        onClick = {
                            onTransmitRaw(38000, "9000, 4500, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560")
                        }
                    )
                }
                item {
                    PresetButton(
                        label = "Vol +",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        color = NeonCyan,
                        enabled = isConnected && !isTransmitting,
                        onClick = {
                            onTransmitRaw(38000, "9000, 4500, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560")
                        }
                    )
                }
                item {
                    PresetButton(
                        label = "Vol -",
                        icon = Icons.AutoMirrored.Filled.VolumeDown,
                        color = NeonCyan,
                        enabled = isConnected && !isTransmitting,
                        onClick = {
                            onTransmitRaw(38000, "9000, 4500, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560")
                        }
                    )
                }
            }
        }

        // Replay Last Captured
        if (lastCapturedSignal != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VioletDark.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Last Captured Code", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("${lastCapturedSignal.timings.size} pulses", color = NeonCyan, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onTransmitSignal(lastCapturedSignal) },
                            enabled = isConnected && !isTransmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = DeepBackground, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replay", color = DeepBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Manual Raw Timing Section
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isManualExpanded = !isManualExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Manual Raw Timings Input",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = VioletLight,
                            modifier = Modifier.weight(1f)
                        )
                        Text(if (isManualExpanded) "▲" else "▼", color = TextMuted, fontSize = 12.sp)
                    }

                    AnimatedVisibility(visible = isManualExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = rawFreq,
                                onValueChange = { rawFreq = it },
                                label = { Text("Frequency (Hz)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletPrimary,
                                    unfocusedBorderColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = rawTimingsText,
                                onValueChange = { rawTimingsText = it },
                                label = { Text("Timings (+mark -space in µs)") },
                                placeholder = { Text("+9000 -4500 +560 -560...", color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletPrimary,
                                    unfocusedBorderColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val freq = rawFreq.toIntOrNull() ?: 38000
                                    onTransmitRaw(freq, rawTimingsText)
                                },
                                enabled = isConnected && !isTransmitting && rawTimingsText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                            ) {
                                Text("Send Raw Pulses", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Signals Library
        item {
            Text(
                "Signals Library (${signals.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VioletLight,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(signals) { signal ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = signal.name,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${signal.frequency / 1000}kHz • ${signal.timings.size} pulses",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = { onTransmitSignal(signal) },
                        enabled = isConnected && !isTransmitting,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = VioletDark,
                            contentColor = VioletLight
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "TX", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TX", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { onDeleteSignal(signal) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = NeonRed.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// =====================================================================
// 4. REAL-TIME DEBUG CONSOLE
// =====================================================================
@Composable
fun DebugConsole(
    logs: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && isExpanded) {
            scrollState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))
    ) {
        HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.BugReport, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Hardware Console & Hex Payloads (${logs.size})",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )

            if (isExpanded) {
                IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ClearAll, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Text(if (isExpanded) "▼" else "▲", color = TextMuted, fontSize = 11.sp)
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(150)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF07070E))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        "No serial packets logged yet.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(state = scrollState) {
                        items(logs) { log ->
                            val color = when {
                                "error" in log.lowercase() || "failed" in log.lowercase() -> NeonRed.copy(alpha = 0.9f)
                                "success" in log.lowercase() || "connected" in log.lowercase() -> NeonGreen.copy(alpha = 0.9f)
                                "tx" in log.lowercase() -> NeonAmber.copy(alpha = 0.9f)
                                "rx" in log.lowercase() -> NeonCyan.copy(alpha = 0.9f)
                                else -> TextMuted
                            }
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                color = color,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
