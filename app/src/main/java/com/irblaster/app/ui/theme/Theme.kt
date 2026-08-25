package com.irblaster.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val LocalAppTheme = staticCompositionLocalOf { AppThemePalette.CYBER_GLASS }

// Base UI Colors
val DeepBackground = Color(0xFF070913)
val SurfaceDark = Color(0xFF101424)
val SurfaceVariant = Color(0x33121829)
val CardSurface = Color(0x33162038)

val VioletPrimary = Color(0xFF7C4DFF)
val VioletLight = Color(0xFFB388FF)
val VioletDark = Color(0xFF651FFF)

val NeonGreen = Color(0xFF00E676)
val NeonRed = Color(0xFFFF1744)
val NeonAmber = Color(0xFFFFB300)
val NeonCyan = Color(0xFF00E5FF)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        lineHeight = 20.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
        lineHeight = 16.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun IrBlasterTheme(
    palette: AppThemePalette = AppThemePalette.CYBER_GLASS,
    isAmoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = darkColorScheme(
        primary = palette.primaryAccent,
        onPrimary = Color.Black,
        primaryContainer = palette.secondaryAccent,
        onPrimaryContainer = Color.White,
        secondary = palette.secondaryAccent,
        onSecondary = Color.Black,
        background = if (isAmoledMode) Color.Black else palette.backgroundBase,
        onBackground = TextPrimary,
        surface = if (isAmoledMode) Color(0xFF0A0A0A) else palette.cardSurface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = TextSecondary,
        error = NeonRed,
        onError = Color.White
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barBg = if (isAmoledMode) Color.Black else palette.backgroundBase
            window.statusBarColor = barBg.toArgb()
            window.navigationBarColor = barBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalAppTheme provides palette) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
