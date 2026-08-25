package com.irblaster.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irblaster.app.model.BrandProfile
import com.irblaster.app.model.RemoteCodeVariant
import com.irblaster.app.model.SavedRemote
import com.irblaster.app.ui.components.springBounceClickable
import com.irblaster.app.ui.theme.AppThemePalette
import com.irblaster.app.ui.theme.NeonAmber
import com.irblaster.app.ui.theme.NeonCyan
import com.irblaster.app.ui.theme.NeonGreen
import com.irblaster.app.ui.theme.NeonRed
import com.irblaster.app.ui.theme.SurfaceVariant
import com.irblaster.app.ui.theme.TextMuted
import com.irblaster.app.ui.theme.TextPrimary
import com.irblaster.app.ui.theme.TextSecondary
import com.irblaster.app.ui.theme.liquidGlass
import kotlinx.coroutines.delay

/**
 * Ocrustar / ZaZa Smart Auto-Matching & Device Pairing Wizard.
 * Features:
 * - Step-by-step interactive testing with visual prompt.
 * - Auto-Search / Bruteforce Scan Radar that automatically steps through code variants.
 * - One-tap Lock & Save to My Remotes.
 */
@Composable
fun AutoMatchingWizard(
    brandProfile: BrandProfile,
    isConnected: Boolean,
    isTransmitting: Boolean,
    theme: AppThemePalette,
    onBack: () -> Unit,
    onPairSuccess: (SavedRemote) -> Unit,
    onTransmitSignal: (com.irblaster.app.model.IrSignal) -> Unit
) {
    var currentVariantIdx by remember { mutableIntStateOf(0) }
    var isAutoSearching by remember { mutableStateOf(false) }

    val variants = brandProfile.variants
    val currentVariant = variants.getOrElse(currentVariantIdx) { variants.first() }

    // Auto-Search / Bruteforce timer
    LaunchedEffect(isAutoSearching, currentVariantIdx) {
        if (isAutoSearching) {
            onTransmitSignal(currentVariant.testSignal)
            delay(1600)
            if (currentVariantIdx < variants.size - 1) {
                currentVariantIdx++
            } else {
                currentVariantIdx = 0 // loop back
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isAutoSearching) isAutoSearching = false
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Pairing Wizard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${brandProfile.brandName} • ${brandProfile.category.title}",
                    fontSize = 11.5.sp,
                    color = theme.primaryAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Pairing Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    backgroundColor = theme.cardSurface,
                    borderGradient = listOf(theme.primaryAccent.copy(alpha = 0.5f), Color.Transparent, theme.secondaryAccent.copy(alpha = 0.4f))
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Code Set Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.primaryAccent.copy(alpha = 0.18f))
                        .border(1.dp, theme.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CODE SET ${currentVariantIdx + 1} OF ${variants.size}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = theme.primaryAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentVariant.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Big Interactive Test Pulse Button
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(if (isAutoSearching || isTransmitting) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isAutoSearching) NeonAmber else theme.primaryAccent,
                                    theme.secondaryAccent.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        .springBounceClickable {
                            if (isConnected && !isTransmitting) {
                                onTransmitSignal(currentVariant.testSignal)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PowerSettingsNew,
                            contentDescription = "Test Key",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentVariant.testKeyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Point dongle at appliance & tap '${currentVariant.testKeyName}'",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Did your ${brandProfile.brandName} device respond (turn on / beep)?",
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // YES / NO Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // NO -> Next Code
                    FilledTonalButton(
                        onClick = {
                            if (isAutoSearching) isAutoSearching = false
                            if (currentVariantIdx < variants.size - 1) {
                                currentVariantIdx++
                            } else {
                                currentVariantIdx = 0
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SurfaceVariant,
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No (Next Code)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // YES -> Save & Pair
                    Button(
                        onClick = {
                            if (isAutoSearching) isAutoSearching = false
                            val saved = SavedRemote(
                                id = "remote_${System.currentTimeMillis()}",
                                customName = "${brandProfile.brandName} ${brandProfile.category.title}",
                                category = brandProfile.category,
                                brandName = brandProfile.brandName,
                                selectedVariant = currentVariant
                            )
                            onPairSuccess(saved)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yes, It Responded!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Search / Bruteforce Scan Radar Button (Signature ZaZa Feature)
                Button(
                    onClick = { isAutoSearching = !isAutoSearching },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAutoSearching) NeonRed else theme.secondaryAccent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isAutoSearching) Icons.Filled.Stop else Icons.Filled.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAutoSearching) "STOP SCAN (LOCK THIS CODE)" else "Auto-Search / Bruteforce Radar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
