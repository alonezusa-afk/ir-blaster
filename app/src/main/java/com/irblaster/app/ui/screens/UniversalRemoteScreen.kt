package com.irblaster.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.IrCategory
import com.irblaster.app.model.IrCommand
import com.irblaster.app.model.IrDeviceDatabase
import com.irblaster.app.model.IrDeviceProfile
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.components.springBounceClickable
import com.irblaster.app.ui.theme.AppThemePalette
import com.irblaster.app.ui.theme.NeonAmber
import com.irblaster.app.ui.theme.NeonCyan
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.NeonRed
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextPrimary
import com.irblaster.app.ui.theme.TextSecondary
import com.irblaster.app.ui.theme.liquidGlass

/**
 * Universal Remote Control Center with extensive pre-built brand profiles,
 * virtual D-Pad controllers, climate sliders, and RGB color grids.
 */
@Composable
fun UniversalRemoteScreen(
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmitSignal: (IrSignal) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<IrCategory?>(null) }
    var selectedProfile by remember { mutableStateOf<IrDeviceProfile>(IrDeviceDatabase.SAMSUNG_TV) }
    var searchQuery by remember { mutableStateOf("") }
    var showNumericKeypad by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // -------------------------------------------------------------
        // 1. APPLIANCE CATEGORY SELECTOR CAROUSEL
        // -------------------------------------------------------------
        item {
            Text(
                "Universal IR Remote Library",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = theme.primaryAccent
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Devices", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primaryAccent.copy(alpha = 0.25f),
                            selectedLabelColor = theme.primaryAccent
                        )
                    )
                }

                items(IrCategory.entries) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category
                            val firstInCat = IrDeviceDatabase.getProfilesByCategory(category).firstOrNull()
                            if (firstInCat != null) selectedProfile = firstInCat
                        },
                        label = { Text(category.displayName, fontSize = 12.sp) },
                        leadingIcon = {
                            val icon = when (category) {
                                IrCategory.TV -> Icons.Filled.Tv
                                IrCategory.AC -> Icons.Filled.AcUnit
                                IrCategory.AUDIO -> Icons.Filled.Speaker
                                IrCategory.RGB_LIGHTS -> Icons.Filled.Lightbulb
                                IrCategory.PROJECTOR -> Icons.Filled.Videocam
                                IrCategory.FAN -> Icons.Filled.Air
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primaryAccent.copy(alpha = 0.25f),
                            selectedLabelColor = theme.primaryAccent
                        )
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 2. BRAND PROFILE SELECTOR
        // -------------------------------------------------------------
        item {
            val availableProfiles = if (selectedCategory != null) {
                IrDeviceDatabase.getProfilesByCategory(selectedCategory!!)
            } else {
                IrDeviceDatabase.ALL_PROFILES
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = theme.cardSurface,
                        borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.4f), Color.Transparent, theme.secondaryAccent.copy(alpha = 0.3f))
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Active Profile: ${selectedProfile.brand} (${selectedProfile.modelOrSeries})",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableProfiles) { profile ->
                            val isSelected = profile.id == selectedProfile.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) theme.primaryAccent.copy(alpha = 0.25f) else Color(0x22121829))
                                    .border(1.dp, if (isSelected) theme.primaryAccent else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { selectedProfile = profile }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = profile.brand,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.primaryAccent else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 3. INTERACTIVE REMOTE CONTROLLER INTERFACE
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        backgroundColor = Color(0x44080D1A),
                        borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.5f), Color.Transparent, theme.secondaryAccent.copy(alpha = 0.35f))
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (selectedProfile.category) {
                        IrCategory.TV, IrCategory.PROJECTOR -> {
                            TvRemoteLayout(
                                profile = selectedProfile,
                                isConnected = isConnected,
                                isTransmitting = isTransmitting,
                                theme = theme,
                                showNumeric = showNumericKeypad,
                                onToggleNumeric = { showNumericKeypad = !showNumericKeypad },
                                onTransmit = { onTransmitSignal(it.signal) }
                            )
                        }
                        IrCategory.AC -> {
                            AcRemoteLayout(
                                profile = selectedProfile,
                                isConnected = isConnected,
                                isTransmitting = isTransmitting,
                                theme = theme,
                                onTransmit = { onTransmitSignal(it.signal) }
                            )
                        }
                        IrCategory.RGB_LIGHTS -> {
                            RgbRemoteLayout(
                                profile = selectedProfile,
                                isConnected = isConnected,
                                isTransmitting = isTransmitting,
                                theme = theme,
                                onTransmit = { onTransmitSignal(it.signal) }
                            )
                        }
                        IrCategory.AUDIO -> {
                            AudioRemoteLayout(
                                profile = selectedProfile,
                                isConnected = isConnected,
                                isTransmitting = isTransmitting,
                                theme = theme,
                                onTransmit = { onTransmitSignal(it.signal) }
                            )
                        }
                        else -> {
                            GenericButtonsGrid(
                                commands = selectedProfile.commands,
                                isConnected = isConnected,
                                isTransmitting = isTransmitting,
                                theme = theme,
                                onTransmit = { onTransmitSignal(it.signal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 1. TV REMOTE LAYOUT (D-Pad, Volume/Channel Rocker, Power, Input)
// =========================================================================
@Composable
fun TvRemoteLayout(
    profile: IrDeviceProfile,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    showNumeric: Boolean,
    onToggleNumeric: () -> Unit,
    onTransmit: (IrCommand) -> Unit
) {
    fun findCmd(vararg names: String): IrCommand? {
        return profile.commands.firstOrNull { cmd ->
            names.any { n -> cmd.name.equals(n, ignoreCase = true) || cmd.name.contains(n, ignoreCase = true) }
        }
    }

    val pwr = findCmd("Power", "Power Toggle")
    val mute = findCmd("Mute")
    val input = findCmd("Source", "Input")
    val up = findCmd("Up")
    val down = findCmd("Down")
    val left = findCmd("Left")
    val right = findCmd("Right")
    val ok = findCmd("OK", "Enter", "Select")
    val back = findCmd("Back", "Return", "Esc")
    val home = findCmd("Home")
    val volUp = findCmd("Volume Up", "Vol +")
    val volDown = findCmd("Volume Down", "Vol -")
    val chUp = findCmd("Channel Up", "Ch +")
    val chDown = findCmd("Channel Down", "Ch -")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row: Power, Mute, Source, 123 Keypad Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pwr?.let { cmd ->
                RemoteRoundButton(icon = Icons.Filled.PowerSettingsNew, color = NeonRed, enabled = isConnected && !isTransmitting) {
                    onTransmit(cmd)
                }
            }

            input?.let { cmd ->
                RemoteRoundButton(icon = Icons.Filled.Input, color = theme.primaryAccent, enabled = isConnected && !isTransmitting) {
                    onTransmit(cmd)
                }
            }

            mute?.let { cmd ->
                RemoteRoundButton(icon = Icons.Filled.VolumeOff, color = NeonAmber, enabled = isConnected && !isTransmitting) {
                    onTransmit(cmd)
                }
            }

            RemoteRoundButton(
                icon = Icons.Filled.Keyboard,
                color = if (showNumeric) theme.primaryAccent else TextSecondary,
                enabled = isConnected
            ) {
                onToggleNumeric()
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Numeric Keypad Grid (when toggled)
        AnimatedVisibility(visible = showNumeric) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val digitCommands = (1..9).map { findCmd("$it") } + listOf(null, findCmd("0"), null)
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (col in 0 until 3) {
                            val cmd = digitCommands[row * 3 + col]
                            if (cmd != null) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33121829))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(cmd) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cmd.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.size(46.dp))
                            }
                        }
                    }
                }
            }
        }

        // Circular Navigation D-Pad
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0x22121829))
                .border(1.5.dp, theme.primaryAccent.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Up
            up?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Up", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }

            // Down
            down?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Down", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }

            // Left
            left?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Left", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }

            // Right
            right?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Right", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }

            // Center OK Button
            ok?.let {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent)
                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Back, Home, Menu Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            back?.let { cmd ->
                FilledTonalButton(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", fontSize = 12.sp)
                }
            }

            home?.let { cmd ->
                FilledTonalButton(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Volume & Channel Rockers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Volume Rocker
            Column(
                modifier = Modifier
                    .width(62.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x33121829))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                volUp?.let {
                    IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "+", tint = theme.primaryAccent)
                    }
                }
                Text("VOL", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextMuted)
                volDown?.let {
                    IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) {
                        Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "-", tint = theme.primaryAccent)
                    }
                }
            }

            // Channel Rocker
            Column(
                modifier = Modifier
                    .width(62.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x33121829))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                chUp?.let {
                    IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) {
                        Icon(Icons.Filled.ArrowDropUp, contentDescription = "+", tint = theme.secondaryAccent)
                    }
                }
                Text("CH", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextMuted)
                chDown?.let {
                    IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "-", tint = theme.secondaryAccent)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. AIR CONDITIONER (AC) REMOTE LAYOUT
// =========================================================================
@Composable
fun AcRemoteLayout(
    profile: IrDeviceProfile,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    fun findCmd(vararg names: String): IrCommand? = profile.commands.firstOrNull { cmd ->
        names.any { cmd.name.contains(it, ignoreCase = true) }
    }

    val power = findCmd("Power")
    val tempUp = findCmd("Temp Up", "+1")
    val tempDown = findCmd("Temp Down", "-1")
    val cool = findCmd("Cool")
    val heat = findCmd("Heat")
    val fan = findCmd("Fan")
    val swing = findCmd("Swing")
    val turbo = findCmd("Turbo", "Super")
    val eco = findCmd("Sleep", "Eco")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AC Display Dial Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF060910))
                .border(1.dp, theme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("INVERTER AC", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text("COOL // AUTO FAN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.primaryAccent)
                }

                Text(
                    text = "24°C",
                    fontSize = 36.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Power and Temp Up / Down Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tempDown?.let { cmd ->
                Button(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(54.dp)
                ) {
                    Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            power?.let { cmd ->
                Button(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Power", tint = Color.White)
                }
            }

            tempUp?.let { cmd ->
                Button(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(54.dp)
                ) {
                    Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mode and Functions Grid
        val acButtons = listOfNotNull(cool, heat, fan, swing, turbo, eco)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(acButtons) { cmd ->
                FilledTonalButton(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cmd.name.take(12),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =========================================================================
// 3. RGB LED STRIP CONTROLLER (24-Key Color Matrix)
// =========================================================================
@Composable
fun RgbRemoteLayout(
    profile: IrDeviceProfile,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    val colorMap = mapOf(
        "Red" to Color(0xFFFF1744),
        "Green" to Color(0xFF00E676),
        "Blue" to Color(0xFF2979FF),
        "White" to Color(0xFFFFFFFF),
        "Orange" to Color(0xFFFF9100),
        "Lime Green" to Color(0xFF76FF03),
        "Dark Cyan" to Color(0xFF00B0FF),
        "Amber Yellow" to Color(0xFFFFD600),
        "Cyan" to Color(0xFF00E5FF),
        "Magenta" to Color(0xFFF50057),
        "Yellow" to Color(0xFFFFEA00),
        "Sky Blue" to Color(0xFF40C4FF),
        "Pink" to Color(0xFFFF80AB),
        "Gold Yellow" to Color(0xFFFFC400),
        "Ocean Blue" to Color(0xFF0091EA),
        "Purple" to Color(0xFFD500F9)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brightness & Power Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            profile.commands.find { it.name.contains("Brightness Up", true) }?.let { cmd ->
                FilledTonalButton(onClick = { onTransmit(cmd) }, enabled = isConnected && !isTransmitting) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Bright +", fontSize = 11.sp)
                }
            }

            profile.commands.find { it.name.contains("Power OFF", true) }?.let { cmd ->
                Button(onClick = { onTransmit(cmd) }, enabled = isConnected && !isTransmitting, colors = ButtonDefaults.buttonColors(containerColor = NeonRed)) {
                    Text("OFF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            profile.commands.find { it.name.contains("Power ON", true) }?.let { cmd ->
                Button(onClick = { onTransmit(cmd) }, enabled = isConnected && !isTransmitting, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)) {
                    Text("ON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4x4 Color Swatch Buttons Grid
        val colorCommands = profile.commands.filter { colorMap.containsKey(it.name) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colorCommands) { cmd ->
                val bg = colorMap[cmd.name] ?: Color.Gray
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .springBounceClickable { if (isConnected && !isTransmitting) onTransmit(cmd) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cmd.name.take(6),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bg == Color.White || bg == Color(0xFFFFEA00) || bg == Color(0xFFFFD600)) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FX Modes (Flash, Strobe, Fade, Smooth)
        val fxCommands = profile.commands.filter { it.name in listOf("Flash", "Strobe", "Fade", "Smooth") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            fxCommands.forEach { cmd ->
                FilledTonalButton(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(cmd.name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================
// 4. AUDIO & SOUNDBAR CONTROLLER
// =========================================================================
@Composable
fun AudioRemoteLayout(
    profile: IrDeviceProfile,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GenericButtonsGrid(
            commands = profile.commands,
            isConnected = isConnected,
            isTransmitting = isTransmitting,
            theme = theme,
            onTransmit = onTransmit
        )
    }
}

// =========================================================================
// 5. GENERIC BUTTONS GRID
// =========================================================================
@Composable
fun GenericButtonsGrid(
    commands: List<IrCommand>,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(commands) { cmd ->
            val isPwr = cmd.name.contains("Power", true)
            FilledTonalButton(
                onClick = { onTransmit(cmd) },
                enabled = isConnected && !isTransmitting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isPwr) NeonRed.copy(alpha = 0.2f) else theme.primaryAccent.copy(alpha = 0.15f),
                    contentColor = if (isPwr) NeonRed else theme.primaryAccent
                ),
                modifier = Modifier.springBounceClickable(onClick = { if (isConnected && !isTransmitting) onTransmit(cmd) })
            ) {
                Text(cmd.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RemoteRoundButton(
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.2.dp, color.copy(alpha = 0.5f), CircleShape)
            .springBounceClickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
    }
}
