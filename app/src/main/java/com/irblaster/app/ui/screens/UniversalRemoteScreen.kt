package com.irblaster.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.BrandProfile
import com.irblaster.app.model.DeviceCategory
import com.irblaster.app.model.IrCommand
import com.irblaster.app.model.IrHeavyDatabase
import com.irblaster.app.model.IrSignal
import com.irblaster.app.model.SavedRemote
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
 * Universal Remote Control Center with:
 * 1. Category and Brand Selection across 100+ global brands.
 * 2. Ocrustar Smart Auto-Matching Wizard.
 * 3. My Saved Remotes drawer.
 * 4. Interactive Virtual Remotes with D-Pads, dials, and color swatches.
 */
@Composable
fun UniversalRemoteScreen(
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmitSignal: (IrSignal) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<DeviceCategory?>(null) }
    var pairingBrand by remember { mutableStateOf<BrandProfile?>(null) }
    var activeRemote by remember {
        mutableStateOf(
            SavedRemote(
                id = "default_samsung",
                customName = "Samsung Smart TV",
                category = DeviceCategory.TV,
                brandName = "Samsung",
                selectedVariant = IrHeavyDatabase.SAMSUNG_TV.variants.first()
            )
        )
    }

    val savedRemotes = remember {
        mutableStateListOf(
            SavedRemote(
                id = "default_samsung",
                customName = "Living Room TV",
                category = DeviceCategory.TV,
                brandName = "Samsung",
                selectedVariant = IrHeavyDatabase.SAMSUNG_TV.variants.first()
            ),
            SavedRemote(
                id = "default_daikin",
                customName = "Bedroom AC",
                category = DeviceCategory.AC,
                brandName = "Daikin",
                selectedVariant = IrHeavyDatabase.DAIKIN_AC.variants.first()
            ),
            SavedRemote(
                id = "default_rgb",
                customName = "Studio RGB Lights",
                category = DeviceCategory.RGB_LIGHTS,
                brandName = "24-Key RGB",
                selectedVariant = IrHeavyDatabase.RGB_24KEY.variants.first()
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var isBrowseModalOpen by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = pairingBrand,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "pairingTransition"
    ) { targetPairingBrand ->
        if (targetPairingBrand != null) {
            // Smart Auto-Matching Wizard Mode
            AutoMatchingWizard(
                brandProfile = targetPairingBrand,
                isConnected = isConnected,
                isTransmitting = isTransmitting,
                theme = theme,
                onBack = { pairingBrand = null },
                onPairSuccess = { newSavedRemote ->
                    savedRemotes.add(newSavedRemote)
                    activeRemote = newSavedRemote
                    pairingBrand = null
                    isBrowseModalOpen = false
                },
                onTransmitSignal = onTransmitSignal
            )
        } else {
            // Standard Remote Dashboard Mode
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // -------------------------------------------------------------
                // 1. MY SAVED REMOTES SHELF
                // -------------------------------------------------------------
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "My Remotes (${savedRemotes.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryAccent
                        )

                        FilledTonalButton(
                            onClick = { isBrowseModalOpen = !isBrowseModalOpen },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = theme.secondaryAccent.copy(alpha = 0.2f), contentColor = theme.secondaryAccent)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pair New Device", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(savedRemotes) { remote ->
                            val isSelected = remote.id == activeRemote.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.primaryAccent.copy(alpha = 0.22f) else Color(0x22121829))
                                    .border(1.2.dp, if (isSelected) theme.primaryAccent else Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .clickable { activeRemote = remote }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (remote.category) {
                                            DeviceCategory.TV -> Icons.Filled.Tv
                                            DeviceCategory.AC -> Icons.Filled.AcUnit
                                            DeviceCategory.AUDIO -> Icons.Filled.Speaker
                                            DeviceCategory.RGB_LIGHTS -> Icons.Filled.Lightbulb
                                            DeviceCategory.PROJECTOR -> Icons.Filled.Videocam
                                            DeviceCategory.SET_TOP_BOX -> Icons.Filled.LiveTv
                                            DeviceCategory.FAN -> Icons.Filled.Air
                                            DeviceCategory.CAMERA -> Icons.Filled.PhotoCamera
                                            DeviceCategory.ROBOT_VACUUM -> Icons.Filled.SmartToy
                                            else -> Icons.Filled.Tv
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) theme.primaryAccent else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = remote.customName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) theme.primaryAccent else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 2. SEARCH & PAIR DATABASE MODAL DRAWER
                // -------------------------------------------------------------
                if (isBrowseModalOpen) {
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
                                Text(
                                    "Universal IR Brand Library (100+ Brands)",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Category Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item {
                                        FilterChip(
                                            selected = selectedCategory == null,
                                            onClick = { selectedCategory = null },
                                            label = { Text("All", fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primaryAccent.copy(alpha = 0.25f), selectedLabelColor = theme.primaryAccent)
                                        )
                                    }
                                    items(DeviceCategory.entries) { cat ->
                                        FilterChip(
                                            selected = selectedCategory == cat,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat.title.take(12), fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primaryAccent.copy(alpha = 0.25f), selectedLabelColor = theme.primaryAccent)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Search brand (Samsung, Daikin, LG, Sony...)") },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = theme.primaryAccent) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = theme.primaryAccent,
                                        unfocusedBorderColor = TextMuted
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val filteredBrands = IrHeavyDatabase.searchBrands(searchQuery).filter {
                                    selectedCategory == null || it.category == selectedCategory
                                }

                                Text(
                                    "Select Brand to Start Smart Auto-Matching Wizard:",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(
                                    modifier = Modifier.height(200.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filteredBrands) { brand ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x22121829))
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                                .clickable { pairingBrand = brand }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(brand.brandName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                                Text("${brand.category.title} • ${brand.variants.size} code variants", color = theme.primaryAccent, fontSize = 11.sp)
                                            }
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Pair", tint = theme.primaryAccent, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3. ACTIVE VIRTUAL REMOTE CONTROLLER
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
                            // Active Remote Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = activeRemote.customName,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${activeRemote.brandName} • ${activeRemote.selectedVariant.description}",
                                        fontSize = 11.sp,
                                        color = theme.primaryAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text("PAIRED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dynamic Layout by Category
                            when (activeRemote.category) {
                                DeviceCategory.TV, DeviceCategory.SET_TOP_BOX, DeviceCategory.PROJECTOR -> {
                                    GenericCommandsRemote(
                                        commands = activeRemote.selectedVariant.fullCommands,
                                        isConnected = isConnected,
                                        isTransmitting = isTransmitting,
                                        theme = theme,
                                        onTransmit = { onTransmitSignal(it.signal) }
                                    )
                                }
                                DeviceCategory.AC -> {
                                    AcRemoteView(
                                        commands = activeRemote.selectedVariant.fullCommands,
                                        isConnected = isConnected,
                                        isTransmitting = isTransmitting,
                                        theme = theme,
                                        onTransmit = { onTransmitSignal(it.signal) }
                                    )
                                }
                                DeviceCategory.RGB_LIGHTS -> {
                                    RgbRemoteView(
                                        commands = activeRemote.selectedVariant.fullCommands,
                                        isConnected = isConnected,
                                        isTransmitting = isTransmitting,
                                        theme = theme,
                                        onTransmit = { onTransmitSignal(it.signal) }
                                    )
                                }
                                else -> {
                                    GenericCommandsRemote(
                                        commands = activeRemote.selectedVariant.fullCommands,
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
    }
}

@Composable
fun GenericCommandsRemote(
    commands: List<IrCommand>,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    fun find(vararg names: String): IrCommand? = commands.firstOrNull { cmd ->
        names.any { cmd.name.equals(it, true) || cmd.name.contains(it, true) }
    }

    val pwr = find("Power", "Power ON", "Power Toggle")
    val mute = find("Mute")
    val volUp = find("Vol +", "Volume Up")
    val volDown = find("Vol -", "Volume Down")
    val chUp = find("CH +", "Channel Up")
    val chDown = find("CH -", "Channel Down")
    val up = find("Up")
    val down = find("Down")
    val left = find("Left")
    val right = find("Right")
    val ok = find("OK", "Enter", "Select")
    val home = find("Home")
    val back = find("Back", "Return")
    val input = find("Source", "Input")

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Power, Input, Mute Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pwr?.let { cmd ->
                Button(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Power", tint = Color.White)
                }
            }

            input?.let { cmd ->
                FilledTonalButton(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(cmd.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            mute?.let { cmd ->
                Button(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Filled.VolumeOff, contentDescription = "Mute", tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Circular Navigation D-Pad
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0x22121829))
                .border(1.5.dp, theme.primaryAccent.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            up?.let {
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).size(40.dp).clip(CircleShape).springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Up", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }
            down?.let {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).size(40.dp).clip(CircleShape).springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Down", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }
            left?.let {
                Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp).size(40.dp).clip(CircleShape).springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Left", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }
            right?.let {
                Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(40.dp).clip(CircleShape).springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Right", tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                }
            }
            ok?.let {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(theme.primaryAccent).springBounceClickable { if (isConnected && !isTransmitting) onTransmit(it) }, contentAlignment = Alignment.Center) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vol / Channel Rockers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                modifier = Modifier.width(62.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x33121829)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                volUp?.let { IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "+", tint = theme.primaryAccent) } }
                Text("VOL", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextMuted)
                volDown?.let { IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) { Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "-", tint = theme.primaryAccent) } }
            }

            Column(
                modifier = Modifier.width(62.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x33121829)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                chUp?.let { IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) { Icon(Icons.Filled.ArrowDropUp, contentDescription = "+", tint = theme.secondaryAccent) } }
                Text("CH", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextMuted)
                chDown?.let { IconButton(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting) { Icon(Icons.Filled.ArrowDropDown, contentDescription = "-", tint = theme.secondaryAccent) } }
            }
        }
    }
}

@Composable
fun AcRemoteView(
    commands: List<IrCommand>,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    fun find(vararg names: String): IrCommand? = commands.firstOrNull { cmd ->
        names.any { cmd.name.contains(it, true) }
    }

    val pwr = find("Power", "Toggle")
    val up = find("Temp Up", "+1")
    val down = find("Temp Down", "-1")

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF060910)).border(1.dp, theme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp)).padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("INVERTER CLIMATE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text("AUTO COOL // 24°C", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.primaryAccent)
                }
                Text("24°C", fontSize = 34.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            down?.let {
                Button(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting, colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryAccent), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(54.dp)) {
                    Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            pwr?.let {
                Button(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting, colors = ButtonDefaults.buttonColors(containerColor = NeonRed), shape = CircleShape, modifier = Modifier.size(58.dp)) {
                    Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Power", tint = Color.White)
                }
            }

            up?.let {
                Button(onClick = { onTransmit(it) }, enabled = isConnected && !isTransmitting, colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent, contentColor = Color.Black), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(54.dp)) {
                    Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(commands.filter { it != pwr && it != up && it != down }) { cmd ->
                FilledTonalButton(
                    onClick = { onTransmit(cmd) },
                    enabled = isConnected && !isTransmitting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(cmd.name.take(12), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun RgbRemoteView(
    commands: List<IrCommand>,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onTransmit: (IrCommand) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(commands) { cmd ->
            FilledTonalButton(
                onClick = { onTransmit(cmd) },
                enabled = isConnected && !isTransmitting,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(cmd.name.take(10), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
