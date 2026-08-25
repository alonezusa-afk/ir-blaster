package com.irblaster.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.ui.components.springBounceClickable
import com.irblaster.app.ui.theme.AppThemePalette
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextPrimary
import com.irblaster.app.ui.theme.TextSecondary
import com.irblaster.app.ui.theme.liquidGlass
import com.irblaster.app.viewmodel.ConnectionState
import com.irblaster.app.viewmodel.UiState

/**
 * Liquid Glass Settings & System Configuration Tab.
 */
@Composable
fun SettingsTab(
    uiState: UiState,
    onSelectTheme: (AppThemePalette) -> Unit,
    onToggleAmoled: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleParticleBlast: (Boolean) -> Unit,
    onSelectCarrierFreq: (Int) -> Unit,
    onSelectChunkDelay: (Int) -> Unit,
    onTriggerPermission: () -> Unit
) {
    val theme = uiState.activeTheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // 1. LIQUID GLASS THEME SELECTOR
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.5f), Color.Transparent, theme.secondaryAccent.copy(alpha = 0.4f))
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liquid Glass Themes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Choose your customized glassmorphic color palette",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Theme Swatch Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppThemePalette.entries.forEach { palette ->
                            val isSelected = palette == theme
                            val animatedBorder by animateColorAsState(
                                targetValue = if (isSelected) palette.primaryAccent else Color.White.copy(alpha = 0.1f),
                                label = "borderAnim"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.cardSurface.copy(alpha = 0.6f) else Color(0x22121829))
                                    .border(1.2.dp, animatedBorder, RoundedCornerShape(12.dp))
                                    .springBounceClickable { onSelectTheme(palette) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Palette Color Circles Preview
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.primaryAccent))
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.secondaryAccent))
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.backgroundBase))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = palette.title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) palette.primaryAccent else TextPrimary,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = palette.subtitle,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(palette.primaryAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Active",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. DISPLAY & MOTION TOGGLES
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.35f), theme.secondaryAccent.copy(alpha = 0.2f))
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DisplaySettings,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Display & Motion FX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // AMOLED Pure Black Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.DarkMode, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("AMOLED Pure Black", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                Text("Turns off ambient mesh glow for OLED battery savings", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = uiState.isAmoledMode,
                            onCheckedChange = onToggleAmoled,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryAccent, checkedTrackColor = theme.primaryAccent.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // IR Laser Particle Blast Effect
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("IR Photon Shockwave Blast", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                Text("Visual laser energy burst when firing remote codes", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = uiState.enableParticleBlast,
                            onCheckedChange = onToggleParticleBlast,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryAccent, checkedTrackColor = theme.primaryAccent.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tactile Haptic Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Vibration, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Tactile Spring Haptics", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                Text("Spring-bounce compression feedback on remote buttons", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = uiState.enableHaptics,
                            onCheckedChange = onToggleHaptics,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryAccent, checkedTrackColor = theme.primaryAccent.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 3. HARDWARE TRANSCEIVER TUNING
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.secondaryAccent.copy(alpha = 0.35f), theme.primaryAccent.copy(alpha = 0.2f))
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = theme.secondaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hardware Transceiver Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Default Carrier Frequency:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(38000 to "38 kHz (Standard)", 36000 to "36 kHz (RC5)", 40000 to "40 kHz (Sony)", 56000 to "56 kHz (B&O)").forEach { (freq, label) ->
                            val isSelected = uiState.defaultCarrierFrequency == freq
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCarrierFreq(freq) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryAccent.copy(alpha = 0.25f),
                                    selectedLabelColor = theme.primaryAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "USB Bulk Chunk Spacing Delay:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4 to "4 ms (Fast)", 8 to "8 ms (Standard)", 14 to "14 ms (Stable)").forEach { (delayMs, label) ->
                            val isSelected = uiState.chunkDelayMs == delayMs
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectChunkDelay(delayMs) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.secondaryAccent.copy(alpha = 0.25f),
                                    selectedLabelColor = theme.secondaryAccent
                                )
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 4. DIAGNOSTICS & ABOUT INFO
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.2f), Color.Transparent)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "USB Hardware Diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val connectionInfo = when (val state = uiState.connectionState) {
                        is ConnectionState.Connected -> "Connected (${state.vidHex}:${state.pidHex}) • ${state.deviceName} • ${if (state.isSerial) "Serial UART" else "Raw Bulk EP 0x01/0x81"}"
                        is ConnectionState.PermissionRequested -> "Awaiting Permission (${state.vidHex}:${state.pidHex})"
                        is ConnectionState.DeviceDetected -> "Device Detected (${state.vidHex}:${state.pidHex})"
                        is ConnectionState.Error -> "Error: ${state.message}"
                        is ConnectionState.Disconnected -> "No USB Dongle Attached"
                    }

                    Text(
                        text = connectionInfo,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = theme.primaryAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF04060C))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onTriggerPermission,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryAccent)
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-scan & Request Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
