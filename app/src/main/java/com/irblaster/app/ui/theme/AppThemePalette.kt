package com.irblaster.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Liquid Glass Theme System with rich swappable aesthetic palettes.
 */
enum class AppThemePalette(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val highlightGlow: Color,
    val backgroundBase: Color,
    val cardSurface: Color,
    val borderStroke: Color,
    val ambientGradient: List<Color>
) {
    CYBER_GLASS(
        id = "cyber_glass",
        title = "Liquid Obsidian",
        subtitle = "Neon Cyan & Electric Violet",
        primaryAccent = Color(0xFF00E5FF),
        secondaryAccent = Color(0xFF7C4DFF),
        highlightGlow = Color(0xFF80D8FF),
        backgroundBase = Color(0xFF070913),
        cardSurface = Color(0x33121829),
        borderStroke = Color(0x4400E5FF),
        ambientGradient = listOf(Color(0xFF0B1021), Color(0xFF060812), Color(0xFF0D061A))
    ),
    FLIPPER_AMBER(
        id = "flipper_amber",
        title = "Flipper Amber Glass",
        subtitle = "Phosphor Amber & Cyber Gold",
        primaryAccent = Color(0xFFFF8C00),
        secondaryAccent = Color(0xFFFFC107),
        highlightGlow = Color(0xFFFFE082),
        backgroundBase = Color(0xFF090806),
        cardSurface = Color(0x33261908),
        borderStroke = Color(0x44FF8C00),
        ambientGradient = listOf(Color(0xFF140D05), Color(0xFF080603), Color(0xFF1A1208))
    ),
    MATRIX_EMERALD(
        id = "matrix_emerald",
        title = "Matrix Emerald Glass",
        subtitle = "Cyberpunk CRT Phosphor Green",
        primaryAccent = Color(0xFF00E676),
        secondaryAccent = Color(0xFF00B0FF),
        highlightGlow = Color(0xFFB9F6CA),
        backgroundBase = Color(0xFF040A07),
        cardSurface = Color(0x330A2416),
        borderStroke = Color(0x4400E676),
        ambientGradient = listOf(Color(0xFF06140D), Color(0xFF030805), Color(0xFF071C12))
    ),
    PLASMA_NEBULA(
        id = "plasma_nebula",
        title = "Plasma Nebula Glass",
        subtitle = "Cosmic Purple & Hot Magenta",
        primaryAccent = Color(0xFFE040FB),
        secondaryAccent = Color(0xFFFF4081),
        highlightGlow = Color(0xFFEA80FC),
        backgroundBase = Color(0xFF0A0512),
        cardSurface = Color(0x33260F33),
        borderStroke = Color(0x44E040FB),
        ambientGradient = listOf(Color(0xFF140824), Color(0xFF080310), Color(0xFF1F0B30))
    ),
    TITANIUM_ICE(
        id = "titanium_ice",
        title = "Titanium Frosted Ice",
        subtitle = "Monochrome Slate & Arctic Frost",
        primaryAccent = Color(0xFFE2E8F0),
        secondaryAccent = Color(0xFF38BDF8),
        highlightGlow = Color(0xFFFFFFFF),
        backgroundBase = Color(0xFF0A0D14),
        cardSurface = Color(0x331E293B),
        borderStroke = Color(0x4494A3B8),
        ambientGradient = listOf(Color(0xFF0F172A), Color(0xFF070A10), Color(0xFF1E293B))
    );

    companion object {
        fun fromId(id: String): AppThemePalette =
            entries.firstOrNull { it.id == id } ?: CYBER_GLASS
    }
}
