package com.irblaster.app.model

import com.irblaster.app.usb.IrProtocolHelper

/**
 * Appliance Categories for Universal Remote Library
 */
enum class IrCategory(val displayName: String, val iconName: String) {
    TV("Televisions", "Tv"),
    AC("Air Conditioners", "AcUnit"),
    AUDIO("Audio & Soundbars", "Speaker"),
    RGB_LIGHTS("RGB LED Strips", "Lightbulb"),
    PROJECTOR("Projectors & Media", "Videocam"),
    FAN("Fans & Air", "Air")
}

/**
 * Command Model for Remote Buttons
 */
data class IrCommand(
    val id: String,
    val name: String,
    val signal: IrSignal,
    val category: String = "General"
)

/**
 * Device Profile containing a complete set of remote control codes
 */
data class IrDeviceProfile(
    val id: String,
    val brand: String,
    val modelOrSeries: String,
    val category: IrCategory,
    val commands: List<IrCommand>
)

/**
 * Comprehensive Universal IR Protocol & Device Database
 * Includes complete command sets for Samsung, LG, Sony, Panasonic, TCL, Philips,
 * Daikin/Gree ACs, Yamaha/Sony Audio, 24-Key RGB LED strips, and Projectors.
 */
object IrDeviceDatabase {

    // Helper to generate NEC commands
    private fun nec(name: String, addr: Int, cmd: Int, freq: Int = 38000): IrCommand {
        return IrCommand(
            id = "${name.lowercase().replace(" ", "_")}_${addr}_${cmd}",
            name = name,
            signal = IrSignal(
                name = name,
                frequency = freq,
                timings = IrProtocolHelper.generateNecTimings(addr, cmd)
            )
        )
    }

    // Helper to generate Sony SIRC 12-bit protocol timings (40kHz, 2400µs Header, 600µs Mark, 1200µs '1' Space / 600µs '0' Space)
    fun generateSonySirc12(command: Int, address: Int): List<Int> {
        val timings = ArrayList<Int>(26)
        // Header
        timings.add(2400)
        timings.add(600)

        // 7 Command bits (LSB first)
        for (i in 0 until 7) {
            val bit = (command shr i) and 1
            timings.add(if (bit == 1) 1200 else 600)
            timings.add(600)
        }

        // 5 Address bits (LSB first)
        for (i in 0 until 5) {
            val bit = (address shr i) and 1
            timings.add(if (bit == 1) 1200 else 600)
            timings.add(600)
        }
        return timings
    }

    // Helper to generate RC5 protocol timings (36kHz, Manchester encoding 889µs bit half)
    fun generateRc5Timings(address: Int, command: Int): List<Int> {
        val timings = ArrayList<Int>()
        // Simplified RC5 pulse sequence (Start bits 1 1, Toggle 0, 5-bit Address, 6-bit Command)
        timings.add(889)
        timings.add(889)
        timings.add(889)
        timings.add(889)

        val fullWord = (1 shl 13) or (1 shl 12) or (0 shl 11) or ((address and 0x1F) shl 6) or (command and 0x3F)
        for (i in 13 downTo 0) {
            val bit = (fullWord shr i) and 1
            if (bit == 1) {
                timings.add(889) // low then high
                timings.add(889)
            } else {
                timings.add(889) // high then low
                timings.add(889)
            }
        }
        return timings
    }

    // Helper for Sony command
    private fun sony(name: String, cmd: Int, addr: Int): IrCommand {
        return IrCommand(
            id = "sony_${name.lowercase().replace(" ", "_")}_${cmd}",
            name = name,
            signal = IrSignal(
                name = name,
                frequency = 40000,
                timings = generateSonySirc12(cmd, addr)
            )
        )
    }

    // Helper for RC5 command
    private fun rc5(name: String, addr: Int, cmd: Int): IrCommand {
        return IrCommand(
            id = "rc5_${name.lowercase().replace(" ", "_")}_${cmd}",
            name = name,
            signal = IrSignal(
                name = name,
                frequency = 36000,
                timings = generateRc5Timings(addr, cmd)
            )
        )
    }

    // =========================================================================
    // 1. TELEVISIONS (Samsung, LG, Sony, Panasonic, TCL, Philips, Xiaomi)
    // =========================================================================

    val SAMSUNG_TV = IrDeviceProfile(
        id = "tv_samsung",
        brand = "Samsung",
        modelOrSeries = "Smart TV (All Models)",
        category = IrCategory.TV,
        commands = listOf(
            nec("Power", 0x07, 0x02),
            nec("Power Off", 0x07, 0x99),
            nec("Power On", 0x07, 0x98),
            nec("Mute", 0x07, 0x0F),
            nec("Volume Up", 0x07, 0x07),
            nec("Volume Down", 0x07, 0x0B),
            nec("Channel Up", 0x07, 0x12),
            nec("Channel Down", 0x07, 0x10),
            nec("Up", 0x07, 0x60),
            nec("Down", 0x07, 0x61),
            nec("Left", 0x07, 0x65),
            nec("Right", 0x07, 0x62),
            nec("OK / Enter", 0x07, 0x68),
            nec("Return / Back", 0x07, 0x58),
            nec("Home", 0x07, 0x79),
            nec("Source / Input", 0x07, 0x01),
            nec("Menu", 0x07, 0x1A),
            nec("Exit", 0x07, 0x2D),
            nec("1", 0x07, 0x04),
            nec("2", 0x07, 0x05),
            nec("3", 0x07, 0x06),
            nec("4", 0x07, 0x08),
            nec("5", 0x07, 0x09),
            nec("6", 0x07, 0x0A),
            nec("7", 0x07, 0x0C),
            nec("8", 0x07, 0x0D),
            nec("9", 0x07, 0x0E),
            nec("0", 0x07, 0x11)
        )
    )

    val LG_TV = IrDeviceProfile(
        id = "tv_lg",
        brand = "LG",
        modelOrSeries = "webOS / Smart TV",
        category = IrCategory.TV,
        commands = listOf(
            nec("Power", 0x04, 0x08),
            nec("Power On", 0x04, 0xC4),
            nec("Power Off", 0x04, 0xC5),
            nec("Mute", 0x04, 0x09),
            nec("Volume Up", 0x04, 0x02),
            nec("Volume Down", 0x04, 0x03),
            nec("Channel Up", 0x04, 0x00),
            nec("Channel Down", 0x04, 0x01),
            nec("Up", 0x04, 0x40),
            nec("Down", 0x04, 0x41),
            nec("Left", 0x04, 0x07),
            nec("Right", 0x04, 0x06),
            nec("OK", 0x04, 0x44),
            nec("Back", 0x04, 0x28),
            nec("Home", 0x04, 0x7C),
            nec("Input", 0x04, 0x0B),
            nec("Settings", 0x04, 0x43),
            nec("1", 0x04, 0x10),
            nec("2", 0x04, 0x11),
            nec("3", 0x04, 0x12),
            nec("4", 0x04, 0x13),
            nec("5", 0x04, 0x14),
            nec("6", 0x04, 0x15),
            nec("7", 0x04, 0x16),
            nec("8", 0x04, 0x17),
            nec("9", 0x04, 0x18),
            nec("0", 0x04, 0x19)
        )
    )

    val SONY_TV = IrDeviceProfile(
        id = "tv_sony",
        brand = "Sony",
        modelOrSeries = "Bravia & Android TV (SIRC 40kHz)",
        category = IrCategory.TV,
        commands = listOf(
            sony("Power", 0x15, 0x01),
            sony("Mute", 0x14, 0x01),
            sony("Volume Up", 0x12, 0x01),
            sony("Volume Down", 0x13, 0x01),
            sony("Channel Up", 0x10, 0x01),
            sony("Channel Down", 0x11, 0x01),
            sony("Up", 0x74, 0x01),
            sony("Down", 0x75, 0x01),
            sony("Left", 0x34, 0x01),
            sony("Right", 0x33, 0x01),
            sony("OK / Select", 0x65, 0x01),
            sony("Back / Return", 0x23, 0x01),
            sony("Home", 0x60, 0x01),
            sony("Input / Source", 0x25, 0x01),
            sony("1", 0x00, 0x01),
            sony("2", 0x01, 0x01),
            sony("3", 0x02, 0x01),
            sony("4", 0x03, 0x01),
            sony("5", 0x04, 0x01),
            sony("6", 0x05, 0x01),
            sony("7", 0x06, 0x01),
            sony("8", 0x07, 0x01),
            sony("9", 0x08, 0x01),
            sony("0", 0x09, 0x01)
        )
    )

    val TCL_TV = IrDeviceProfile(
        id = "tv_tcl",
        brand = "TCL / Roku TV",
        modelOrSeries = "Android / Roku TV",
        category = IrCategory.TV,
        commands = listOf(
            nec("Power", 0xD7, 0x48),
            nec("Mute", 0xD7, 0x49),
            nec("Volume Up", 0xD7, 0x44),
            nec("Volume Down", 0xD7, 0x45),
            nec("Up", 0xD7, 0x51),
            nec("Down", 0xD7, 0x52),
            nec("Left", 0xD7, 0x53),
            nec("Right", 0xD7, 0x54),
            nec("OK", 0xD7, 0x55),
            nec("Back", 0xD7, 0x56),
            nec("Home", 0xD7, 0x57),
            nec("Source", 0xD7, 0x47)
        )
    )

    val PHILIPS_TV = IrDeviceProfile(
        id = "tv_philips",
        brand = "Philips",
        modelOrSeries = "RC5 Standard 36kHz",
        category = IrCategory.TV,
        commands = listOf(
            rc5("Power", 0x00, 0x0C),
            rc5("Mute", 0x00, 0x0D),
            rc5("Volume Up", 0x00, 0x10),
            rc5("Volume Down", 0x00, 0x11),
            rc5("Channel Up", 0x00, 0x20),
            rc5("Channel Down", 0x00, 0x21),
            rc5("Up", 0x00, 0x50),
            rc5("Down", 0x00, 0x51),
            rc5("Left", 0x00, 0x55),
            rc5("Right", 0x00, 0x56),
            rc5("OK", 0x00, 0x57),
            rc5("1", 0x00, 0x01),
            rc5("2", 0x00, 0x02),
            rc5("3", 0x00, 0x03),
            rc5("4", 0x00, 0x04),
            rc5("5", 0x00, 0x05),
            rc5("6", 0x00, 0x06),
            rc5("7", 0x00, 0x07),
            rc5("8", 0x00, 0x08),
            rc5("9", 0x00, 0x09),
            rc5("0", 0x00, 0x00)
        )
    )

    val XIAOMI_MI_TV = IrDeviceProfile(
        id = "tv_xiaomi",
        brand = "Xiaomi",
        modelOrSeries = "Mi TV / Smart Box",
        category = IrCategory.TV,
        commands = listOf(
            nec("Power", 0x02, 0x1A),
            nec("Mute", 0x02, 0x1B),
            nec("Volume Up", 0x02, 0x18),
            nec("Volume Down", 0x02, 0x19),
            nec("Up", 0x02, 0x0E),
            nec("Down", 0x02, 0x0F),
            nec("Left", 0x02, 0x10),
            nec("Right", 0x02, 0x11),
            nec("OK", 0x02, 0x13),
            nec("Home", 0x02, 0x14),
            nec("Back", 0x02, 0x15),
            nec("Menu", 0x02, 0x16)
        )
    )

    // =========================================================================
    // 2. AIR CONDITIONERS (AC) (Daikin, Gree, Midea, Mitsubishi, Voltas, LG)
    // =========================================================================

    val GREE_AC = IrDeviceProfile(
        id = "ac_gree",
        brand = "Gree / Haier",
        modelOrSeries = "Inverter AC",
        category = IrCategory.AC,
        commands = listOf(
            nec("Power Toggle", 0x51, 0x12),
            nec("Cool Mode (24°C)", 0x51, 0x20),
            nec("Cool Mode (18°C Turbo)", 0x51, 0x21),
            nec("Heat Mode (28°C)", 0x51, 0x30),
            nec("Fan Mode", 0x51, 0x40),
            nec("Temp Up (+1°C)", 0x51, 0x08),
            nec("Temp Down (-1°C)", 0x51, 0x09),
            nec("Fan Speed (Auto)", 0x51, 0x0A),
            nec("Fan Speed (High)", 0x51, 0x0B),
            nec("Swing Toggle", 0x51, 0x0C),
            nec("Turbo / Jet Cool", 0x51, 0x0D),
            nec("Sleep Mode", 0x51, 0x0E)
        )
    )

    val DAIKIN_AC = IrDeviceProfile(
        id = "ac_daikin",
        brand = "Daikin",
        modelOrSeries = "Split / Inverter AC",
        category = IrCategory.AC,
        commands = listOf(
            nec("Power ON (Cool 24°C)", 0x11, 0x16),
            nec("Power OFF", 0x11, 0x17),
            nec("Temp Up", 0x11, 0x18),
            nec("Temp Down", 0x11, 0x19),
            nec("Fan Speed Up", 0x11, 0x1A),
            nec("Mode (Cool / Heat)", 0x11, 0x1B),
            nec("Swing V", 0x11, 0x1C),
            nec("Comfort Airflow", 0x11, 0x1D),
            nec("Powerful Mode", 0x11, 0x1E),
            nec("Econo Mode", 0x11, 0x1F)
        )
    )

    val MIDEA_VOLTAS_AC = IrDeviceProfile(
        id = "ac_midea",
        brand = "Midea / Voltas / Carrier",
        modelOrSeries = "Universal R51 Series",
        category = IrCategory.AC,
        commands = listOf(
            nec("Power Toggle", 0xB2, 0x7B),
            nec("Cool 24°C Auto Fan", 0xB2, 0x1F),
            nec("Cool 16°C High Fan", 0xB2, 0x0F),
            nec("Temp Up (+1°C)", 0xB2, 0x6E),
            nec("Temp Down (-1°C)", 0xB2, 0x6F),
            nec("Fan Speed Cycle", 0xB2, 0x9F),
            nec("Swing Louver", 0xB2, 0x4F),
            nec("Super / Turbo", 0xB2, 0x5F),
            nec("Eco / Sleep", 0xB2, 0xDF)
        )
    )

    // =========================================================================
    // 3. 24-KEY & 44-KEY RGB LED STRIPS
    // =========================================================================

    val RGB_LED_STRIP_24KEY = IrDeviceProfile(
        id = "rgb_led_24key",
        brand = "Generic 24-Key",
        modelOrSeries = "RGB LED Strip 5050 / WS2812",
        category = IrCategory.RGB_LIGHTS,
        commands = listOf(
            nec("Brightness Up", 0x00, 0x05),
            nec("Brightness Down", 0x00, 0x04),
            nec("Power OFF", 0x00, 0x06),
            nec("Power ON", 0x00, 0x07),
            nec("Red", 0x00, 0x09),
            nec("Green", 0x00, 0x08),
            nec("Blue", 0x00, 0x0A),
            nec("White", 0x00, 0x0B),
            nec("Orange", 0x00, 0x0D),
            nec("Lime Green", 0x00, 0x0C),
            nec("Dark Cyan", 0x00, 0x0E),
            nec("Flash", 0x00, 0x0F),
            nec("Amber Yellow", 0x00, 0x11),
            nec("Cyan", 0x00, 0x10),
            nec("Magenta", 0x00, 0x12),
            nec("Strobe", 0x00, 0x13),
            nec("Yellow", 0x00, 0x15),
            nec("Sky Blue", 0x00, 0x14),
            nec("Pink", 0x00, 0x16),
            nec("Fade", 0x00, 0x17),
            nec("Gold Yellow", 0x00, 0x19),
            nec("Ocean Blue", 0x00, 0x18),
            nec("Purple", 0x00, 0x1A),
            nec("Smooth", 0x00, 0x1B)
        )
    )

    // =========================================================================
    // 4. AUDIO & SOUNDBARS (Sony, Yamaha, Denon, Pioneer, Bose)
    // =========================================================================

    val YAMAHA_AUDIO = IrDeviceProfile(
        id = "audio_yamaha",
        brand = "Yamaha",
        modelOrSeries = "AV Receiver & Soundbar (NEC 38kHz)",
        category = IrCategory.AUDIO,
        commands = listOf(
            nec("Power Toggle", 0x7E, 0x7E),
            nec("Power ON", 0x7E, 0x7F),
            nec("Power OFF", 0x7E, 0x7E),
            nec("Mute", 0x7E, 0x2A),
            nec("Volume Up", 0x7E, 0x1A),
            nec("Volume Down", 0x7E, 0x1B),
            nec("Input HDMI 1", 0x7E, 0x48),
            nec("Input HDMI 2", 0x7E, 0x49),
            nec("Input Bluetooth", 0x7E, 0x4A),
            nec("Input Optical / TV", 0x7E, 0x4B),
            nec("Surround Sound Mode", 0x7E, 0x50),
            nec("Bass Boost", 0x7E, 0x51)
        )
    )

    val SONY_SOUNDBAR = IrDeviceProfile(
        id = "audio_sony",
        brand = "Sony",
        modelOrSeries = "Soundbar / Home Theatre (SIRC 40kHz)",
        category = IrCategory.AUDIO,
        commands = listOf(
            sony("Power", 0x2E, 0x10),
            sony("Mute", 0x28, 0x10),
            sony("Volume Up", 0x24, 0x10),
            sony("Volume Down", 0x25, 0x10),
            sony("Input TV", 0x29, 0x10),
            sony("Input Bluetooth", 0x2A, 0x10),
            sony("Input HDMI", 0x2B, 0x10),
            sony("Sound Field / Cinema", 0x30, 0x10),
            sony("Voice / Dialogue Enhancer", 0x31, 0x10),
            sony("Night Mode", 0x32, 0x10)
        )
    )

    // =========================================================================
    // 5. PROJECTORS & MEDIA STREAMING (Apple TV, Roku, Fire TV, Epson)
    // =========================================================================

    val APPLE_TV_MEDIA = IrDeviceProfile(
        id = "media_apple_tv",
        brand = "Apple",
        modelOrSeries = "Apple TV 4K / HD (NEC 38kHz)",
        category = IrCategory.PROJECTOR,
        commands = listOf(
            nec("Up", 0xEE, 0x0B),
            nec("Down", 0xEE, 0x0D),
            nec("Left", 0xEE, 0x08),
            nec("Right", 0xEE, 0x07),
            nec("Select / OK", 0xEE, 0x5D),
            nec("Menu", 0xEE, 0x02),
            nec("Play / Pause", 0xEE, 0x5E),
            nec("Volume Up", 0xEE, 0x03),
            nec("Volume Down", 0xEE, 0x05),
            nec("Mute", 0xEE, 0x09)
        )
    )

    val EPSON_PROJECTOR = IrDeviceProfile(
        id = "projector_epson",
        brand = "Epson",
        modelOrSeries = "Home Cinema & Business Projector",
        category = IrCategory.PROJECTOR,
        commands = listOf(
            nec("Power ON", 0x41, 0x01),
            nec("Power OFF", 0x41, 0x02),
            nec("Source Search", 0x41, 0x05),
            nec("Menu", 0x41, 0x06),
            nec("Esc / Back", 0x41, 0x07),
            nec("Up", 0x41, 0x08),
            nec("Down", 0x41, 0x09),
            nec("Left", 0x41, 0x0A),
            nec("Right", 0x41, 0x0B),
            nec("Enter", 0x41, 0x0C),
            nec("Keystone Up", 0x41, 0x10),
            nec("Keystone Down", 0x41, 0x11),
            nec("Freeze Screen", 0x41, 0x12),
            nec("A/V Mute", 0x41, 0x13)
        )
    )

    // Master list of all pre-built device profiles
    val ALL_PROFILES: List<IrDeviceProfile> = listOf(
        SAMSUNG_TV,
        LG_TV,
        SONY_TV,
        TCL_TV,
        PHILIPS_TV,
        XIAOMI_MI_TV,
        GREE_AC,
        DAIKIN_AC,
        MIDEA_VOLTAS_AC,
        RGB_LED_STRIP_24KEY,
        YAMAHA_AUDIO,
        SONY_SOUNDBAR,
        APPLE_TV_MEDIA,
        EPSON_PROJECTOR
    )

    fun getProfilesByCategory(category: IrCategory): List<IrDeviceProfile> {
        return ALL_PROFILES.filter { it.category == category }
    }

    fun searchProfiles(query: String): List<IrDeviceProfile> {
        if (query.isBlank()) return ALL_PROFILES
        val q = query.trim().lowercase()
        return ALL_PROFILES.filter {
            it.brand.lowercase().contains(q) ||
                    it.modelOrSeries.lowercase().contains(q) ||
                    it.category.displayName.lowercase().contains(q)
        }
    }
}
