package com.irblaster.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.components.FlipperIrWaveformGraph
import com.irblaster.app.ui.components.IrBlastParticleOverlay
import com.irblaster.app.ui.components.SonarRadarVisualizer
import com.irblaster.app.ui.components.shimmerBorder
import com.irblaster.app.ui.components.springBounceClickable
import com.irblaster.app.ui.theme.AppThemePalette
import com.irblaster.app.ui.theme.LiquidAmbientBackground
import com.irblaster.app.ui.theme.NeonAmber
import com.irblaster.app.ui.theme.NeonCyan
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.NeonRed
import com.irblaster.app.ui.theme.SurfaceVariant
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextPrimary
import com.irblaster.app.ui.theme.TextSecondary
import com.irblaster.app.ui.theme.liquidGlass
import com.irblaster.app.usb.IrProtocolHelper
import com.irblaster.app.viewmodel.ConnectionState
import com.irblaster.app.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val theme = uiState.activeTheme
    var selectedTab by remember { mutableIntStateOf(0) }
    var isDebugConsoleExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveSignalName by remember { mutableStateOf("") }

    LiquidAmbientBackground(
        theme = theme,
        isAmoledMode = uiState.isAmoledMode,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // ---------------------------------------------------------
                // 1. LIQUID GLASS CONNECTION HEADER
                // ---------------------------------------------------------
                ConnectionHeader(
                    connectionState = uiState.connectionState,
                    theme = theme,
                    statusBanner = uiState.statusBanner,
                    isListening = uiState.isListening,
                    isTransmitting = uiState.isTransmitting,
                    onRequestPermission = onRequestPermission
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---------------------------------------------------------
                // NAVIGATION TABS (4 Tabs: Clone/Learn, Remote Library, Presets, Settings)
                // ---------------------------------------------------------
                val tabs = listOf(
                    Triple("Learn", Icons.Filled.Hearing, 0),
                    Triple("Remotes", Icons.Filled.SettingsRemote, 1),
                    Triple("Presets", Icons.AutoMirrored.Filled.Send, 2),
                    Triple("Settings", Icons.Filled.Tune, 3)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = theme.primaryAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = theme.primaryAccent,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .liquidGlass(
                            backgroundColor = theme.cardSurface,
                            borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.35f), Color.Transparent, theme.secondaryAccent.copy(alpha = 0.35f))
                        )
                ) {
                    tabs.forEachIndexed { index, (title, icon, _) ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) theme.primaryAccent else TextSecondary,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (selectedTab == index) theme.primaryAccent else TextMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ---------------------------------------------------------
                // MAIN TAB CONTENT (Smooth Hardware-Accelerated Transitions)
                // ---------------------------------------------------------
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn(tween(260))).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut(tween(160))
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(tween(260))).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut(tween(160))
                            )
                        }.using(SizeTransform(clip = false))
                    },
                    label = "tabTransition",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> CloneListenTab(
                            isConnected = uiState.connectionState is ConnectionState.Connected,
                            isListening = uiState.isListening,
                            lastCapturedSignal = uiState.lastCapturedSignal,
                            theme = theme,
                            onToggleListen = { viewModel.toggleListen() },
                            onReplaySignal = { signal -> viewModel.transmitSignal(signal) },
                            onOpenSaveDialog = {
                                saveSignalName = uiState.lastCapturedSignal?.name ?: ""
                                showSaveDialog = true
                            }
                        )
                        1 -> UniversalRemoteScreen(
                            isConnected = uiState.connectionState is ConnectionState.Connected,
                            isTransmitting = uiState.isTransmitting,
                            theme = theme,
                            onTransmitSignal = { viewModel.transmitSignal(it) }
                        )
                        2 -> TransmitTab(
                            isConnected = uiState.connectionState is ConnectionState.Connected,
                            isTransmitting = uiState.isTransmitting,
                            signals = uiState.capturedSignals,
                            lastCapturedSignal = uiState.lastCapturedSignal,
                            theme = theme,
                            onTransmitSignal = { viewModel.transmitSignal(it) },
                            onTransmitRaw = { freq, raw -> viewModel.transmitRaw(freq, raw) },
                            onDeleteSignal = { viewModel.deleteSignal(it) }
                        )
                        3 -> SettingsTab(
                            uiState = uiState,
                            onSelectTheme = { viewModel.setTheme(it) },
                            onToggleAmoled = { viewModel.setAmoledMode(it) },
                            onToggleHaptics = { viewModel.setHaptics(it) },
                            onToggleParticleBlast = { viewModel.setParticleBlast(it) },
                            onSelectCarrierFreq = { viewModel.setDefaultCarrierFrequency(it) },
                            onSelectChunkDelay = { viewModel.setChunkDelay(it) },
                            onTriggerPermission = { viewModel.triggerPermissionRequest() }
                        )
                    }
                }

                // ---------------------------------------------------------
                // REAL-TIME DEBUG CONSOLE (COLLAPSIBLE)
                // ---------------------------------------------------------
                DebugConsole(
                    logs = uiState.logs,
                    isExpanded = isDebugConsoleExpanded,
                    theme = theme,
                    onToggle = { isDebugConsoleExpanded = !isDebugConsoleExpanded },
                    onClear = { viewModel.clearLogs() }
                )
            }
        }

        // Save Signal Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save Signal to Library", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                                focusedBorderColor = theme.primaryAccent,
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
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent, contentColor = Color.Black)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Global IR Photon Blast Laser Animation Overlay
        if (uiState.enableParticleBlast) {
            IrBlastParticleOverlay(isTransmitting = uiState.isTransmitting)
        }
    }
}

// =====================================================================
// 1. LIQUID GLASS CONNECTION HEADER
// =====================================================================
@Composable
fun ConnectionHeader(
    connectionState: ConnectionState,
    theme: AppThemePalette,
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
                if (isListening) theme.primaryAccent else if (isTransmitting) NeonAmber else NeonGreen,
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
                theme.primaryAccent,
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
                "Plug in USB-C IR Blaster (Auto-detecting)",
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                backgroundColor = theme.cardSurface,
                borderGradient = listOf(animatedColor.copy(alpha = 0.6f), Color.Transparent, theme.primaryAccent.copy(alpha = 0.3f))
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Status Dot with Ripple
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(animatedColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .scale(if (connectionState is ConnectionState.Connected || isListening || connectionState is ConnectionState.DeviceDetected) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }

                // Status Badge Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(animatedColor.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            isTransmitting -> "TX BLAST"
                            isListening -> "RX LEARN"
                            connectionState is ConnectionState.Connected -> "READY"
                            else -> "AUTO-SCAN"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Interactive Grant Permission / Connect Button if ungranted
            if (showPermissionBtn) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryAccent)
                ) {
                    Icon(Icons.Filled.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Grant USB Permission / Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =====================================================================
// 2. CLONE & LISTEN TAB (WITH COMPACT FLIPPER ZERO WAVEFORM GRAPH)
// =====================================================================
@Composable
fun CloneListenTab(
    isConnected: Boolean,
    isListening: Boolean,
    lastCapturedSignal: IrSignal?,
    theme: AppThemePalette,
    onToggleListen: () -> Unit,
    onReplaySignal: (IrSignal) -> Unit,
    onOpenSaveDialog: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "listenRipple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rippleScale"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Listen Action Button
        Button(
            onClick = onToggleListen,
            enabled = isConnected,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .scale(if (isListening) rippleScale else 1f)
                .springBounceClickable(onClick = onToggleListen),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) NeonRed else theme.primaryAccent,
                contentColor = if (isListening) Color.White else Color.Black,
                disabledContainerColor = SurfaceVariant
            )
        ) {
            if (isListening) {
                SonarRadarVisualizer(isListening = true, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.Hearing,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isListening) "Stop Listening (Receiver Active)" else "Start Listening / Flipper Learn",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Flipper Zero Style Compact IR Waveform Graph Analyzer (Small Pillars)
        FlipperIrWaveformGraph(
            signal = lastCapturedSignal,
            isListening = isListening,
            accentColor = theme.primaryAccent
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Captured Signal Details & Actions Card (with animated Shimmer Border)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .liquidGlass(
                    backgroundColor = theme.cardSurface,
                    borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.4f), Color.Transparent, theme.secondaryAccent.copy(alpha = 0.3f))
                )
                .shimmerBorder(enabled = lastCapturedSignal != null),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (lastCapturedSignal != null) {
                    Column(
                        modifier = Modifier.fillMaxSize()
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
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Replay", fontSize = 12.sp)
                                }

                                FilledTonalButton(
                                    onClick = onOpenSaveDialog,
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", color = theme.primaryAccent, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Pulse Timing Hex/Raw Preview
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF04060C))
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                item {
                                    Text(
                                        text = lastCapturedSignal.previewString,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = theme.primaryAccent,
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
                            .background(Color(0xFF04060C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.SettingsRemote,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isListening) "Listening on photodiode (aim remote 2cm away)..." else "Tap 'Start Listening' to capture IR waveforms",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isListening) theme.primaryAccent else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 3. TRANSMIT & PRESETS TAB
// =====================================================================
@Composable
fun TransmitTab(
    isConnected: Boolean,
    isTransmitting: Boolean,
    signals: List<IrSignal>,
    lastCapturedSignal: IrSignal?,
    theme: AppThemePalette,
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
                color = theme.primaryAccent
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PresetButton(
                        label = "TV Power",
                        icon = Icons.Filled.PowerSettingsNew,
                        color = NeonRed,
                        enabled = isConnected && !isTransmitting,
                        onClick = { onTransmitSignal(IrProtocolHelper.PRESET_SIGNALS[0]) }
                    )
                }
                item {
                    PresetButton(
                        label = "Mute",
                        icon = Icons.Filled.VolumeOff,
                        color = NeonAmber,
                        enabled = isConnected && !isTransmitting,
                        onClick = { onTransmitSignal(IrProtocolHelper.PRESET_SIGNALS[1]) }
                    )
                }
                item {
                    PresetButton(
                        label = "Vol +",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        color = NeonGreen,
                        enabled = isConnected && !isTransmitting,
                        onClick = { onTransmitSignal(IrProtocolHelper.PRESET_SIGNALS[2]) }
                    )
                }
                item {
                    PresetButton(
                        label = "Vol -",
                        icon = Icons.AutoMirrored.Filled.VolumeDown,
                        color = theme.primaryAccent,
                        enabled = isConnected && !isTransmitting,
                        onClick = { onTransmitSignal(IrProtocolHelper.PRESET_SIGNALS[3]) }
                    )
                }
                item {
                    PresetButton(
                        label = "Samsung Power",
                        icon = Icons.Filled.Tv,
                        color = theme.secondaryAccent,
                        enabled = isConnected && !isTransmitting,
                        onClick = { onTransmitSignal(IrProtocolHelper.PRESET_SIGNALS[4]) }
                    )
                }
            }
        }

        // Active Captured Signal Quick Replay
        if (lastCapturedSignal != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            backgroundColor = theme.cardSurface,
                            borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.5f), Color.Transparent)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lastCapturedSignal.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("${lastCapturedSignal.timings.size} pulses • ${lastCapturedSignal.frequency}Hz", color = theme.primaryAccent, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onTransmitSignal(lastCapturedSignal) },
                            enabled = isConnected && !isTransmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replay", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Manual Raw Timing Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.secondaryAccent.copy(alpha = 0.35f), Color.Transparent)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                            color = theme.primaryAccent,
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
                                    focusedBorderColor = theme.primaryAccent,
                                    unfocusedBorderColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = rawTimingsText,
                                onValueChange = { rawTimingsText = it },
                                label = { Text("Timings (+mark -space in µs)") },
                                placeholder = { Text("+9000 -4500 +560 -560...", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primaryAccent,
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
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent, contentColor = Color.Black)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Blast Raw Sequence", fontWeight = FontWeight.Bold)
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
                color = theme.primaryAccent,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(signals) { signal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.25f), Color.Transparent)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                            containerColor = theme.secondaryAccent,
                            contentColor = Color.White
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
fun PresetButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.springBounceClickable(scaleDown = 0.88f, onClick = { if (enabled) onClick() }),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color.copy(alpha = 0.18f),
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
    theme: AppThemePalette,
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
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hardware Debug Console (${logs.size})",
                style = MaterialTheme.typography.bodySmall,
                color = if (isExpanded) theme.primaryAccent else TextMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (isExpanded && logs.isNotEmpty()) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonRed,
                    modifier = Modifier
                        .clickable { onClear() }
                        .padding(horizontal = 8.dp)
                )
            }

            Text(
                text = if (isExpanded) "▲ Hide" else "▼ Show",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        if (isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF04060C))
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No log entries yet. Connect dongle to view serial bus traffic.", color = TextMuted, fontSize = 11.sp)
                    }
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(logs) { logEntry ->
                            Text(
                                text = logEntry,
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = when {
                                    logEntry.contains("TX") -> NeonCyan
                                    logEntry.contains("RX") -> NeonAmber
                                    logEntry.contains("✓") -> NeonGreen
                                    logEntry.contains("✗") || logEntry.contains("Error") -> NeonRed
                                    else -> TextSecondary
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
