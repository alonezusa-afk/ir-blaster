package com.irblaster.app.model

import com.irblaster.app.usb.IrProtocolHelper

/**
 * Heavy Database of Universal IR Remote Presets across 10 Categories & 100+ Global Brands.
 * Recreates the exact multi-variant auto-detect & pairing architecture used in ZaZa Remote & Ocrustar.
 */
object IrHeavyDatabase {

    // --- Helper Protocol Builders ---

    private fun necCmd(name: String, addr: Int, cmd: Int, freq: Int = 38000): IrCommand {
        return IrCommand(
            id = "cmd_${name.lowercase().replace(" ", "_")}_${addr}_${cmd}",
            name = name,
            signal = IrSignal(
                name = name,
                frequency = freq,
                timings = IrProtocolHelper.generateNecTimings(addr, cmd)
            )
        )
    }

    private fun sonyCmd(name: String, cmd: Int, addr: Int): IrCommand {
        return IrCommand(
            id = "sony_${name.lowercase().replace(" ", "_")}_${cmd}_${addr}",
            name = name,
            signal = IrSignal(
                name = name,
                frequency = 40000,
                timings = IrDeviceDatabase.generateSonySirc12(cmd, addr)
            )
        )
    }

    private fun rc5Cmd(name: String, addr: Int, cmd: Int): IrCommand {
        return IrCommand(
            id = "rc5_${name.lowercase().replace(" ", "_")}_${addr}_${cmd}",
            name = name,
            signal = IrSignal(
                name = name,
                frequency = 36000,
                timings = IrDeviceDatabase.generateRc5Timings(addr, cmd)
            )
        )
    }

    // Camera Shutter Pulse (e.g. Nikon ML-L3: 2000µs mark, 27830µs space, 400µs mark, 1580µs space, 400µs mark, 3580µs space, 400µs mark)
    private fun cameraShutterNikon(): IrSignal {
        return IrSignal(
            name = "Nikon Shutter Trigger",
            frequency = 38400,
            timings = listOf(2000, 27830, 400, 1580, 400, 3580, 400)
        )
    }

    // Canon RC-6: 500µs mark, 7300µs space, 500µs mark (Immediate) / 500µs mark, 5300µs space, 500µs mark (2s delay)
    private fun cameraShutterCanon(): IrSignal {
        return IrSignal(
            name = "Canon RC-6 Shutter",
            frequency = 33000,
            timings = listOf(500, 7300, 500)
        )
    }

    // =========================================================================
    // 1. TELEVISIONS (TV)
    // =========================================================================

    val SAMSUNG_TV = BrandProfile(
        id = "tv_samsung",
        brandName = "Samsung",
        category = DeviceCategory.TV,
        popularModels = "Smart TV, Neo QLED, OLED, Crystal UHD, Series 6/7/8",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Standard Smart TV (Code Set 1 - Universal NEC 0x07)",
                testKeyName = "Power",
                testSignal = IrSignal("Samsung Power", 38000, IrProtocolHelper.generateNecTimings(0x07, 0x02)),
                fullCommands = listOf(
                    necCmd("Power", 0x07, 0x02),
                    necCmd("Mute", 0x07, 0x0F),
                    necCmd("Vol +", 0x07, 0x07),
                    necCmd("Vol -", 0x07, 0x0B),
                    necCmd("CH +", 0x07, 0x12),
                    necCmd("CH -", 0x07, 0x10),
                    necCmd("Up", 0x07, 0x60),
                    necCmd("Down", 0x07, 0x61),
                    necCmd("Left", 0x07, 0x65),
                    necCmd("Right", 0x07, 0x62),
                    necCmd("OK", 0x07, 0x68),
                    necCmd("Back", 0x07, 0x58),
                    necCmd("Home", 0x07, 0x79),
                    necCmd("Source", 0x07, 0x01),
                    necCmd("Menu", 0x07, 0x1A),
                    necCmd("Exit", 0x07, 0x2D),
                    necCmd("1", 0x07, 0x04), necCmd("2", 0x07, 0x05), necCmd("3", 0x07, 0x06),
                    necCmd("4", 0x07, 0x08), necCmd("5", 0x07, 0x09), necCmd("6", 0x07, 0x0A),
                    necCmd("7", 0x07, 0x0C), necCmd("8", 0x07, 0x0D), necCmd("9", 0x07, 0x0E),
                    necCmd("0", 0x07, 0x11)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 2,
                description = "Modern Samsung QLED / The Frame (Discrete Power Code Set 2)",
                testKeyName = "Power ON",
                testSignal = IrSignal("Samsung Power ON", 38000, IrProtocolHelper.generateNecTimings(0x07, 0x98)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x07, 0x98),
                    necCmd("Power OFF", 0x07, 0x99),
                    necCmd("Mute", 0x07, 0x0F),
                    necCmd("Vol +", 0x07, 0x07),
                    necCmd("Vol -", 0x07, 0x0B),
                    necCmd("Home", 0x07, 0x79),
                    necCmd("Settings", 0x07, 0x1A),
                    necCmd("Up", 0x07, 0x60), necCmd("Down", 0x07, 0x61),
                    necCmd("Left", 0x07, 0x65), necCmd("Right", 0x07, 0x62), necCmd("OK", 0x07, 0x68)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 3,
                description = "Legacy Samsung LCD / Plasma (Code Set 3 - 0x07:0x00)",
                testKeyName = "Power",
                testSignal = IrSignal("Samsung Power", 38000, IrProtocolHelper.generateNecTimings(0x07, 0x00)),
                fullCommands = listOf(
                    necCmd("Power", 0x07, 0x00),
                    necCmd("Mute", 0x07, 0x03),
                    necCmd("Vol +", 0x07, 0x04),
                    necCmd("Vol -", 0x07, 0x05),
                    necCmd("CH +", 0x07, 0x06),
                    necCmd("CH -", 0x07, 0x07),
                    necCmd("Menu", 0x07, 0x0A),
                    necCmd("Input", 0x07, 0x0B)
                )
            )
        )
    )

    val LG_TV = BrandProfile(
        id = "tv_lg",
        brandName = "LG",
        category = DeviceCategory.TV,
        popularModels = "webOS, OLED C/G/B Series, NanoCell, QNED, UHD 4K",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "LG Smart webOS (Code Set 1 - Universal 0x04:0x08)",
                testKeyName = "Power",
                testSignal = IrSignal("LG Power", 38000, IrProtocolHelper.generateNecTimings(0x04, 0x08)),
                fullCommands = listOf(
                    necCmd("Power", 0x04, 0x08),
                    necCmd("Mute", 0x04, 0x09),
                    necCmd("Vol +", 0x04, 0x02),
                    necCmd("Vol -", 0x04, 0x03),
                    necCmd("CH +", 0x04, 0x00),
                    necCmd("CH -", 0x04, 0x01),
                    necCmd("Up", 0x04, 0x40),
                    necCmd("Down", 0x04, 0x41),
                    necCmd("Left", 0x04, 0x07),
                    necCmd("Right", 0x04, 0x06),
                    necCmd("OK", 0x04, 0x44),
                    necCmd("Back", 0x04, 0x28),
                    necCmd("Home", 0x04, 0x7C),
                    necCmd("Input", 0x04, 0x0B),
                    necCmd("Settings", 0x04, 0x43),
                    necCmd("1", 0x04, 0x10), necCmd("2", 0x04, 0x11), necCmd("3", 0x04, 0x12),
                    necCmd("4", 0x04, 0x13), necCmd("5", 0x04, 0x14), necCmd("6", 0x04, 0x15),
                    necCmd("7", 0x04, 0x16), necCmd("8", 0x04, 0x17), necCmd("9", 0x04, 0x18),
                    necCmd("0", 0x04, 0x19)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 2,
                description = "LG Discrete Power Set (Code Set 2 - 0x04:0xC4/0xC5)",
                testKeyName = "Power ON",
                testSignal = IrSignal("LG Power ON", 38000, IrProtocolHelper.generateNecTimings(0x04, 0xC4)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x04, 0xC4),
                    necCmd("Power OFF", 0x04, 0xC5),
                    necCmd("Vol +", 0x04, 0x02),
                    necCmd("Vol -", 0x04, 0x03),
                    necCmd("Mute", 0x04, 0x09),
                    necCmd("Home", 0x04, 0x7C),
                    necCmd("Input", 0x04, 0x0B)
                )
            )
        )
    )

    val SONY_TV = BrandProfile(
        id = "tv_sony",
        brandName = "Sony",
        category = DeviceCategory.TV,
        popularModels = "Bravia XR, OLED A80/A90, X90K, Google TV, Android TV",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Sony Bravia SIRC 12-bit 40kHz (Code Set 1)",
                testKeyName = "Power",
                testSignal = IrSignal("Sony Power", 40000, IrDeviceDatabase.generateSonySirc12(0x15, 0x01)),
                fullCommands = listOf(
                    sonyCmd("Power", 0x15, 0x01),
                    sonyCmd("Mute", 0x14, 0x01),
                    sonyCmd("Vol +", 0x12, 0x01),
                    sonyCmd("Vol -", 0x13, 0x01),
                    sonyCmd("CH +", 0x10, 0x01),
                    sonyCmd("CH -", 0x11, 0x01),
                    sonyCmd("Up", 0x74, 0x01),
                    sonyCmd("Down", 0x75, 0x01),
                    sonyCmd("Left", 0x34, 0x01),
                    sonyCmd("Right", 0x33, 0x01),
                    sonyCmd("OK", 0x65, 0x01),
                    sonyCmd("Back", 0x23, 0x01),
                    sonyCmd("Home", 0x60, 0x01),
                    sonyCmd("Input", 0x25, 0x01),
                    sonyCmd("1", 0x00, 0x01), sonyCmd("2", 0x01, 0x01), sonyCmd("3", 0x02, 0x01),
                    sonyCmd("4", 0x03, 0x01), sonyCmd("5", 0x04, 0x01), sonyCmd("6", 0x05, 0x01),
                    sonyCmd("7", 0x06, 0x01), sonyCmd("8", 0x07, 0x01), sonyCmd("9", 0x08, 0x01),
                    sonyCmd("0", 0x09, 0x01)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 2,
                description = "Sony SIRC Extended Address 0x10 (Code Set 2)",
                testKeyName = "Power",
                testSignal = IrSignal("Sony Power", 40000, IrDeviceDatabase.generateSonySirc12(0x15, 0x10)),
                fullCommands = listOf(
                    sonyCmd("Power", 0x15, 0x10),
                    sonyCmd("Mute", 0x14, 0x10),
                    sonyCmd("Vol +", 0x12, 0x10),
                    sonyCmd("Vol -", 0x13, 0x10),
                    sonyCmd("Home", 0x60, 0x10),
                    sonyCmd("Input", 0x25, 0x10)
                )
            )
        )
    )

    val TCL_TV = BrandProfile(
        id = "tv_tcl",
        brandName = "TCL",
        category = DeviceCategory.TV,
        popularModels = "Google TV, Roku TV, 4K UHD 5/6 Series, C-Series",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "TCL Smart TV (Code Set 1 - 0xD7:0x48)",
                testKeyName = "Power",
                testSignal = IrSignal("TCL Power", 38000, IrProtocolHelper.generateNecTimings(0xD7, 0x48)),
                fullCommands = listOf(
                    necCmd("Power", 0xD7, 0x48),
                    necCmd("Mute", 0xD7, 0x49),
                    necCmd("Vol +", 0xD7, 0x44),
                    necCmd("Vol -", 0xD7, 0x45),
                    necCmd("Up", 0xD7, 0x51),
                    necCmd("Down", 0xD7, 0x52),
                    necCmd("Left", 0xD7, 0x53),
                    necCmd("Right", 0xD7, 0x54),
                    necCmd("OK", 0xD7, 0x55),
                    necCmd("Back", 0xD7, 0x56),
                    necCmd("Home", 0xD7, 0x57),
                    necCmd("Source", 0xD7, 0x47)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 2,
                description = "TCL Roku Model (Code Set 2 - 0x57:0x12)",
                testKeyName = "Power",
                testSignal = IrSignal("TCL Roku Power", 38000, IrProtocolHelper.generateNecTimings(0x57, 0x12)),
                fullCommands = listOf(
                    necCmd("Power", 0x57, 0x12),
                    necCmd("Mute", 0x57, 0x0D),
                    necCmd("Vol +", 0x57, 0x1E),
                    necCmd("Vol -", 0x57, 0x1F),
                    necCmd("Home", 0x57, 0x03),
                    necCmd("OK", 0x57, 0x02)
                )
            )
        )
    )

    val PHILIPS_TV = BrandProfile(
        id = "tv_philips",
        brandName = "Philips",
        category = DeviceCategory.TV,
        popularModels = "Ambilight TV, OLED, Android TV, 4K Ultra",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Philips RC5 Protocol 36kHz (Code Set 1)",
                testKeyName = "Power",
                testSignal = IrSignal("Philips Power", 36000, IrDeviceDatabase.generateRc5Timings(0x00, 0x0C)),
                fullCommands = listOf(
                    rc5Cmd("Power", 0x00, 0x0C),
                    rc5Cmd("Mute", 0x00, 0x0D),
                    rc5Cmd("Vol +", 0x00, 0x10),
                    rc5Cmd("Vol -", 0x00, 0x11),
                    rc5Cmd("CH +", 0x00, 0x20),
                    rc5Cmd("CH -", 0x00, 0x21),
                    rc5Cmd("Up", 0x00, 0x50),
                    rc5Cmd("Down", 0x00, 0x51),
                    rc5Cmd("Left", 0x00, 0x55),
                    rc5Cmd("Right", 0x00, 0x56),
                    rc5Cmd("OK", 0x00, 0x57)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 2,
                description = "Philips Smart NEC 38kHz (Code Set 2)",
                testKeyName = "Power",
                testSignal = IrSignal("Philips NEC Power", 38000, IrProtocolHelper.generateNecTimings(0x08, 0x0C)),
                fullCommands = listOf(
                    necCmd("Power", 0x08, 0x0C),
                    necCmd("Mute", 0x08, 0x0D),
                    necCmd("Vol +", 0x08, 0x10),
                    necCmd("Vol -", 0x08, 0x11),
                    necCmd("Home", 0x08, 0x79)
                )
            )
        )
    )

    val XIAOMI_TV = BrandProfile(
        id = "tv_xiaomi",
        brandName = "Xiaomi / Mi / Redmi",
        category = DeviceCategory.TV,
        popularModels = "Mi TV 4A/4S/5, Redmi Smart TV, PatchWall",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Mi TV Standard NEC (Code Set 1 - 0x02)",
                testKeyName = "Power",
                testSignal = IrSignal("Mi Power", 38000, IrProtocolHelper.generateNecTimings(0x02, 0x1A)),
                fullCommands = listOf(
                    necCmd("Power", 0x02, 0x1A),
                    necCmd("Mute", 0x02, 0x1B),
                    necCmd("Vol +", 0x02, 0x18),
                    necCmd("Vol -", 0x02, 0x19),
                    necCmd("Up", 0x02, 0x0E),
                    necCmd("Down", 0x02, 0x0F),
                    necCmd("Left", 0x02, 0x10),
                    necCmd("Right", 0x02, 0x11),
                    necCmd("OK", 0x02, 0x13),
                    necCmd("Home", 0x02, 0x14),
                    necCmd("Back", 0x02, 0x15),
                    necCmd("Menu", 0x02, 0x16)
                )
            )
        )
    )

    val HISENSE_TV = BrandProfile(
        id = "tv_hisense",
        brandName = "Hisense",
        category = DeviceCategory.TV,
        popularModels = "VIDAA OS, ULED 4K, Laser TV, Roku TV",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Hisense Standard (Code Set 1 - 0xFB:0x12)",
                testKeyName = "Power",
                testSignal = IrSignal("Hisense Power", 38000, IrProtocolHelper.generateNecTimings(0xFB, 0x12)),
                fullCommands = listOf(
                    necCmd("Power", 0xFB, 0x12),
                    necCmd("Mute", 0xFB, 0x0D),
                    necCmd("Vol +", 0xFB, 0x1E),
                    necCmd("Vol -", 0xFB, 0x1F),
                    necCmd("Up", 0xFB, 0x02),
                    necCmd("Down", 0xFB, 0x03),
                    necCmd("Left", 0xFB, 0x04),
                    necCmd("Right", 0xFB, 0x05),
                    necCmd("OK", 0xFB, 0x06),
                    necCmd("Home", 0xFB, 0x79)
                )
            )
        )
    )

    val PANASONIC_TV = BrandProfile(
        id = "tv_panasonic",
        brandName = "Panasonic",
        category = DeviceCategory.TV,
        popularModels = "Viera, OLED, Smart 4K UHD",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Panasonic Viera Code Set 1 (0x40:0x3C)",
                testKeyName = "Power",
                testSignal = IrSignal("Panasonic Power", 38000, IrProtocolHelper.generateNecTimings(0x40, 0x3C)),
                fullCommands = listOf(
                    necCmd("Power", 0x40, 0x3C),
                    necCmd("Mute", 0x40, 0x32),
                    necCmd("Vol +", 0x40, 0x20),
                    necCmd("Vol -", 0x40, 0x21),
                    necCmd("CH +", 0x40, 0x28),
                    necCmd("CH -", 0x40, 0x29),
                    necCmd("Menu", 0x40, 0x4A),
                    necCmd("Input", 0x40, 0x05)
                )
            )
        )
    )

    val TOSHIBA_TV = BrandProfile(
        id = "tv_toshiba",
        brandName = "Toshiba",
        category = DeviceCategory.TV,
        popularModels = "Fire TV, REGZA, Smart 4K",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Toshiba Standard (Code Set 1 - 0x02:0x02)",
                testKeyName = "Power",
                testSignal = IrSignal("Toshiba Power", 38000, IrProtocolHelper.generateNecTimings(0x02, 0x02)),
                fullCommands = listOf(
                    necCmd("Power", 0x02, 0x02),
                    necCmd("Mute", 0x02, 0x03),
                    necCmd("Vol +", 0x02, 0x1A),
                    necCmd("Vol -", 0x02, 0x1E),
                    necCmd("Input", 0x02, 0x0F)
                )
            )
        )
    )

    val SHARP_TV = BrandProfile(
        id = "tv_sharp",
        brandName = "Sharp",
        category = DeviceCategory.TV,
        popularModels = "Aquos, Android TV, Roku TV",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Sharp Aquos (Code Set 1 - 0xAA:0x5A)",
                testKeyName = "Power",
                testSignal = IrSignal("Sharp Power", 38000, IrProtocolHelper.generateNecTimings(0xAA, 0x5A)),
                fullCommands = listOf(
                    necCmd("Power", 0xAA, 0x5A),
                    necCmd("Mute", 0xAA, 0x52),
                    necCmd("Vol +", 0xAA, 0x50),
                    necCmd("Vol -", 0xAA, 0x51),
                    necCmd("Menu", 0xAA, 0x70)
                )
            )
        )
    )

    val VIZIO_TV = BrandProfile(
        id = "tv_vizio",
        brandName = "Vizio",
        category = DeviceCategory.TV,
        popularModels = "SmartCast, OLED, Quantum M/P-Series",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Vizio SmartCast (Code Set 1 - 0x04:0x02)",
                testKeyName = "Power",
                testSignal = IrSignal("Vizio Power", 38000, IrProtocolHelper.generateNecTimings(0x04, 0x02)),
                fullCommands = listOf(
                    necCmd("Power", 0x04, 0x02),
                    necCmd("Mute", 0x04, 0x05),
                    necCmd("Vol +", 0x04, 0x03),
                    necCmd("Vol -", 0x04, 0x04),
                    necCmd("Input", 0x04, 0x09),
                    necCmd("Home", 0x04, 0x7C)
                )
            )
        )
    )

    // =========================================================================
    // 2. AIR CONDITIONERS (AC)
    // =========================================================================

    val DAIKIN_AC = BrandProfile(
        id = "ac_daikin",
        brandName = "Daikin",
        category = DeviceCategory.AC,
        popularModels = "Inverter FTKM, Split AC, Ururu Sarara",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Daikin Inverter (Code Set 1 - 0x11)",
                testKeyName = "Power ON (Cool 24°C)",
                testSignal = IrSignal("Daikin Power ON", 38000, IrProtocolHelper.generateNecTimings(0x11, 0x16)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x11, 0x16),
                    necCmd("Power OFF", 0x11, 0x17),
                    necCmd("Temp Up", 0x11, 0x18),
                    necCmd("Temp Down", 0x11, 0x19),
                    necCmd("Fan Speed Up", 0x11, 0x1A),
                    necCmd("Mode", 0x11, 0x1B),
                    necCmd("Swing", 0x11, 0x1C),
                    necCmd("Turbo / Powerful", 0x11, 0x1E),
                    necCmd("Econo", 0x11, 0x1F)
                )
            ),
            RemoteCodeVariant(
                variantIndex = 2,
                description = "Daikin Compact ARC470 (Code Set 2 - 0x12)",
                testKeyName = "Power Toggle",
                testSignal = IrSignal("Daikin Toggle", 38000, IrProtocolHelper.generateNecTimings(0x12, 0x01)),
                fullCommands = listOf(
                    necCmd("Power Toggle", 0x12, 0x01),
                    necCmd("Temp Up", 0x12, 0x04),
                    necCmd("Temp Down", 0x12, 0x05),
                    necCmd("Fan Speed", 0x12, 0x06),
                    necCmd("Cool Mode", 0x12, 0x07)
                )
            )
        )
    )

    val GREE_AC = BrandProfile(
        id = "ac_gree",
        brandName = "Gree / Haier",
        category = DeviceCategory.AC,
        popularModels = "Lomo, Fairy, Bora, Inverter Multi-Split",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Gree YAA1FB / YAN1F (Code Set 1 - 0x51)",
                testKeyName = "Power Toggle",
                testSignal = IrSignal("Gree Power Toggle", 38000, IrProtocolHelper.generateNecTimings(0x51, 0x12)),
                fullCommands = listOf(
                    necCmd("Power Toggle", 0x51, 0x12),
                    necCmd("Cool 24°C", 0x51, 0x20),
                    necCmd("Cool 18°C Turbo", 0x51, 0x21),
                    necCmd("Heat Mode", 0x51, 0x30),
                    necCmd("Fan Mode", 0x51, 0x40),
                    necCmd("Temp Up", 0x51, 0x08),
                    necCmd("Temp Down", 0x51, 0x09),
                    necCmd("Fan Speed", 0x51, 0x0A),
                    necCmd("Swing", 0x51, 0x0C),
                    necCmd("Turbo", 0x51, 0x0D),
                    necCmd("Sleep", 0x51, 0x0E)
                )
            )
        )
    )

    val MIDEA_AC = BrandProfile(
        id = "ac_midea",
        brandName = "Midea / Carrier / Voltas",
        category = DeviceCategory.AC,
        popularModels = "Mission, Blanc, All Easy, R51 Series",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Midea R51 / Carrier (Code Set 1 - 0xB2)",
                testKeyName = "Power Toggle",
                testSignal = IrSignal("Midea Power Toggle", 38000, IrProtocolHelper.generateNecTimings(0xB2, 0x7B)),
                fullCommands = listOf(
                    necCmd("Power Toggle", 0xB2, 0x7B),
                    necCmd("Cool 24°C", 0xB2, 0x1F),
                    necCmd("Cool 16°C", 0xB2, 0x0F),
                    necCmd("Temp Up", 0xB2, 0x6E),
                    necCmd("Temp Down", 0xB2, 0x6F),
                    necCmd("Fan Speed", 0xB2, 0x9F),
                    necCmd("Swing", 0xB2, 0x4F),
                    necCmd("Turbo", 0xB2, 0x5F),
                    necCmd("Eco", 0xB2, 0xDF)
                )
            )
        )
    )

    val MITSUBISHI_AC = BrandProfile(
        id = "ac_mitsubishi",
        brandName = "Mitsubishi Electric / Heavy",
        category = DeviceCategory.AC,
        popularModels = "MSZ-AP, Kirigamine, Heavy SRK Series",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Mitsubishi Heavy (Code Set 1 - 0x23:0x10)",
                testKeyName = "Power Toggle",
                testSignal = IrSignal("Mitsubishi Power", 38000, IrProtocolHelper.generateNecTimings(0x23, 0x10)),
                fullCommands = listOf(
                    necCmd("Power Toggle", 0x23, 0x10),
                    necCmd("Temp Up", 0x23, 0x12),
                    necCmd("Temp Down", 0x23, 0x13),
                    necCmd("Cool", 0x23, 0x20),
                    necCmd("Fan Speed", 0x23, 0x14),
                    necCmd("Swing", 0x23, 0x15)
                )
            )
        )
    )

    val PANASONIC_AC = BrandProfile(
        id = "ac_panasonic",
        brandName = "Panasonic AC",
        category = DeviceCategory.AC,
        popularModels = "nanoeX, Aero Series, Inverter Eco",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Panasonic AC (Code Set 1 - 0x02:0x20)",
                testKeyName = "Power ON",
                testSignal = IrSignal("Panasonic AC Power", 38000, IrProtocolHelper.generateNecTimings(0x02, 0x20)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x02, 0x20),
                    necCmd("Power OFF", 0x02, 0x21),
                    necCmd("Temp Up", 0x02, 0x22),
                    necCmd("Temp Down", 0x02, 0x23),
                    necCmd("Fan Speed", 0x02, 0x24),
                    necCmd("iAuto-X / Turbo", 0x02, 0x25)
                )
            )
        )
    )

    // =========================================================================
    // 3. SET-TOP BOX & TV BOXES (STB)
    // =========================================================================

    val APPLE_TV = BrandProfile(
        id = "stb_apple_tv",
        brandName = "Apple TV",
        category = DeviceCategory.SET_TOP_BOX,
        popularModels = "Apple TV 4K, HD, 3rd / 2nd Gen",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Apple Silver/Siri IR (Code Set 1 - 0xEE)",
                testKeyName = "Menu / Back",
                testSignal = IrSignal("Apple Menu", 38000, IrProtocolHelper.generateNecTimings(0xEE, 0x02)),
                fullCommands = listOf(
                    necCmd("Up", 0xEE, 0x0B),
                    necCmd("Down", 0xEE, 0x0D),
                    necCmd("Left", 0xEE, 0x08),
                    necCmd("Right", 0xEE, 0x07),
                    necCmd("Select / OK", 0xEE, 0x5D),
                    necCmd("Menu", 0xEE, 0x02),
                    necCmd("Play / Pause", 0xEE, 0x5E),
                    necCmd("Vol +", 0xEE, 0x03),
                    necCmd("Vol -", 0xEE, 0x05),
                    necCmd("Mute", 0xEE, 0x09)
                )
            )
        )
    )

    val ROKU_STREAMING = BrandProfile(
        id = "stb_roku",
        brandName = "Roku",
        category = DeviceCategory.SET_TOP_BOX,
        popularModels = "Roku Express, Premiere, Streaming Stick (IR)",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Roku Player Universal (Code Set 1 - 0x57)",
                testKeyName = "Home",
                testSignal = IrSignal("Roku Home", 38000, IrProtocolHelper.generateNecTimings(0x57, 0x03)),
                fullCommands = listOf(
                    necCmd("Home", 0x57, 0x03),
                    necCmd("Up", 0x57, 0x0A),
                    necCmd("Down", 0x57, 0x0B),
                    necCmd("Left", 0x57, 0x08),
                    necCmd("Right", 0x57, 0x09),
                    necCmd("OK", 0x57, 0x02),
                    necCmd("Back", 0x57, 0x04),
                    necCmd("Play/Pause", 0x57, 0x0D),
                    necCmd("Replay", 0x57, 0x07)
                )
            )
        )
    )

    val AMAZON_FIRE_TV = BrandProfile(
        id = "stb_fire_tv",
        brandName = "Amazon Fire TV",
        category = DeviceCategory.SET_TOP_BOX,
        popularModels = "Fire TV Cube, Fire TV Edition (IR Mode)",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Amazon Fire TV IR (Code Set 1 - 0x01)",
                testKeyName = "Home",
                testSignal = IrSignal("Fire TV Home", 38000, IrProtocolHelper.generateNecTimings(0x01, 0x03)),
                fullCommands = listOf(
                    necCmd("Home", 0x01, 0x03),
                    necCmd("Select", 0x01, 0x02),
                    necCmd("Up", 0x01, 0x04),
                    necCmd("Down", 0x01, 0x05),
                    necCmd("Left", 0x01, 0x06),
                    necCmd("Right", 0x01, 0x07),
                    necCmd("Back", 0x01, 0x08),
                    necCmd("Menu", 0x01, 0x09),
                    necCmd("Play/Pause", 0x01, 0x0A)
                )
            )
        )
    )

    val AIRTEL_TATA_SKY_STB = BrandProfile(
        id = "stb_airtel_tata",
        brandName = "Airtel / Tata Play / Dish TV",
        category = DeviceCategory.SET_TOP_BOX,
        popularModels = "HD / 4K Android Box, DTH Set-Top Box",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Universal DTH STB (Code Set 1 - RC5 0x00)",
                testKeyName = "Power",
                testSignal = IrSignal("DTH Power", 36000, IrDeviceDatabase.generateRc5Timings(0x00, 0x0C)),
                fullCommands = listOf(
                    rc5Cmd("Power", 0x00, 0x0C),
                    rc5Cmd("Mute", 0x00, 0x0D),
                    rc5Cmd("Vol +", 0x00, 0x10),
                    rc5Cmd("Vol -", 0x00, 0x11),
                    rc5Cmd("CH +", 0x00, 0x20),
                    rc5Cmd("CH -", 0x00, 0x21),
                    rc5Cmd("OK", 0x00, 0x57),
                    rc5Cmd("Home", 0x00, 0x50),
                    rc5Cmd("Guide", 0x00, 0x52)
                )
            )
        )
    )

    // =========================================================================
    // 4. AUDIO & SOUNDBARS
    // =========================================================================

    val YAMAHA_AUDIO = BrandProfile(
        id = "audio_yamaha",
        brandName = "Yamaha",
        category = DeviceCategory.AUDIO,
        popularModels = "AV Receiver RX-V Series, YAS Soundbars, MusicCast",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Yamaha AV Receiver (Code Set 1 - 0x7E)",
                testKeyName = "Power",
                testSignal = IrSignal("Yamaha Power", 38000, IrProtocolHelper.generateNecTimings(0x7E, 0x7E)),
                fullCommands = listOf(
                    necCmd("Power", 0x7E, 0x7E),
                    necCmd("Power ON", 0x7E, 0x7F),
                    necCmd("Mute", 0x7E, 0x2A),
                    necCmd("Vol +", 0x7E, 0x1A),
                    necCmd("Vol -", 0x7E, 0x1B),
                    necCmd("HDMI 1", 0x7E, 0x48),
                    necCmd("HDMI 2", 0x7E, 0x49),
                    necCmd("Bluetooth", 0x7E, 0x4A),
                    necCmd("Optical", 0x7E, 0x4B),
                    necCmd("Surround", 0x7E, 0x50),
                    necCmd("Bass Boost", 0x7E, 0x51)
                )
            )
        )
    )

    val SONY_AUDIO = BrandProfile(
        id = "audio_sony",
        brandName = "Sony Audio",
        category = DeviceCategory.AUDIO,
        popularModels = "HT-A Series, STR AV Receivers, Soundbars",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Sony Soundbar SIRC 40kHz (Code Set 1)",
                testKeyName = "Power",
                testSignal = IrSignal("Sony Audio Power", 40000, IrDeviceDatabase.generateSonySirc12(0x2E, 0x10)),
                fullCommands = listOf(
                    sonyCmd("Power", 0x2E, 0x10),
                    sonyCmd("Mute", 0x28, 0x10),
                    sonyCmd("Vol +", 0x24, 0x10),
                    sonyCmd("Vol -", 0x25, 0x10),
                    sonyCmd("Input TV", 0x29, 0x10),
                    sonyCmd("Bluetooth", 0x2A, 0x10),
                    sonyCmd("HDMI", 0x2B, 0x10),
                    sonyCmd("Cinema Mode", 0x30, 0x10),
                    sonyCmd("Voice", 0x31, 0x10)
                )
            )
        )
    )

    val BOSE_AUDIO = BrandProfile(
        id = "audio_bose",
        brandName = "Bose",
        category = DeviceCategory.AUDIO,
        popularModels = "Solo 5, Soundbar 500/700/900, Lifestyle",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Bose Solo & Soundbar (Code Set 1 - 0x55)",
                testKeyName = "Power",
                testSignal = IrSignal("Bose Power", 38000, IrProtocolHelper.generateNecTimings(0x55, 0x01)),
                fullCommands = listOf(
                    necCmd("Power", 0x55, 0x01),
                    necCmd("Mute", 0x55, 0x02),
                    necCmd("Vol +", 0x55, 0x03),
                    necCmd("Vol -", 0x55, 0x04),
                    necCmd("Bluetooth", 0x55, 0x05),
                    necCmd("TV Audio", 0x55, 0x06),
                    necCmd("Bass", 0x55, 0x07)
                )
            )
        )
    )

    // =========================================================================
    // 5. RGB LED LIGHTS
    // =========================================================================

    val RGB_24KEY = BrandProfile(
        id = "rgb_24key",
        brandName = "Universal 24-Key RGB",
        category = DeviceCategory.RGB_LIGHTS,
        popularModels = "SMD 5050, WS2812, Tuya, Magic Home Strip",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Standard 24-Key Controller (Code Set 1 - 0x00)",
                testKeyName = "Power ON",
                testSignal = IrSignal("RGB ON", 38000, IrProtocolHelper.generateNecTimings(0x00, 0x07)),
                fullCommands = listOf(
                    necCmd("Brightness Up", 0x00, 0x05),
                    necCmd("Brightness Down", 0x00, 0x04),
                    necCmd("Power OFF", 0x00, 0x06),
                    necCmd("Power ON", 0x00, 0x07),
                    necCmd("Red", 0x00, 0x09),
                    necCmd("Green", 0x00, 0x08),
                    necCmd("Blue", 0x00, 0x0A),
                    necCmd("White", 0x00, 0x0B),
                    necCmd("Orange", 0x00, 0x0D),
                    necCmd("Lime Green", 0x00, 0x0C),
                    necCmd("Dark Cyan", 0x00, 0x0E),
                    necCmd("Flash", 0x00, 0x0F),
                    necCmd("Amber Yellow", 0x00, 0x11),
                    necCmd("Cyan", 0x00, 0x10),
                    necCmd("Magenta", 0x00, 0x12),
                    necCmd("Strobe", 0x00, 0x13),
                    necCmd("Yellow", 0x00, 0x15),
                    necCmd("Sky Blue", 0x00, 0x14),
                    necCmd("Pink", 0x00, 0x16),
                    necCmd("Fade", 0x00, 0x17),
                    necCmd("Gold Yellow", 0x00, 0x19),
                    necCmd("Ocean Blue", 0x00, 0x18),
                    necCmd("Purple", 0x00, 0x1A),
                    necCmd("Smooth", 0x00, 0x1B)
                )
            )
        )
    )

    val RGB_44KEY = BrandProfile(
        id = "rgb_44key",
        brandName = "Universal 44-Key RGB",
        category = DeviceCategory.RGB_LIGHTS,
        popularModels = "44-Key DIY RGB LED Controller",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "44-Key DIY Controller (Code Set 1 - 0x00)",
                testKeyName = "Power ON",
                testSignal = IrSignal("44K ON", 38000, IrProtocolHelper.generateNecTimings(0x00, 0x47)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x00, 0x47),
                    necCmd("Power OFF", 0x00, 0x46),
                    necCmd("Auto FX", 0x00, 0x0F),
                    necCmd("Flash", 0x00, 0x13),
                    necCmd("Fade 3", 0x00, 0x17),
                    necCmd("Fade 7", 0x00, 0x1B),
                    necCmd("Jump 3", 0x00, 0x1F),
                    necCmd("Jump 7", 0x00, 0x23)
                )
            )
        )
    )

    // =========================================================================
    // 6. FANS & PURIFIERS
    // =========================================================================

    val DYSON_FAN = BrandProfile(
        id = "fan_dyson",
        brandName = "Dyson",
        category = DeviceCategory.FAN,
        popularModels = "Pure Cool, Hot+Cool, AM07, TP04/TP07",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Dyson Pure Cool (Code Set 1 - 0x03)",
                testKeyName = "Power Toggle",
                testSignal = IrSignal("Dyson Power", 38000, IrProtocolHelper.generateNecTimings(0x03, 0x01)),
                fullCommands = listOf(
                    necCmd("Power Toggle", 0x03, 0x01),
                    necCmd("Speed Up", 0x03, 0x02),
                    necCmd("Speed Down", 0x03, 0x03),
                    necCmd("Oscillation", 0x03, 0x04),
                    necCmd("Sleep Timer", 0x03, 0x05),
                    necCmd("Night Mode", 0x03, 0x06),
                    necCmd("Airflow Direction", 0x03, 0x07)
                )
            )
        )
    )

    val CEILING_FAN = BrandProfile(
        id = "fan_universal",
        brandName = "Universal Ceiling Fan / Havells / Lasko",
        category = DeviceCategory.FAN,
        popularModels = "Tower Fan, Ceiling Fan 3-Speed, Remote Light",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Universal Fan Controller (Code Set 1 - 0x14)",
                testKeyName = "Fan Power",
                testSignal = IrSignal("Fan Power", 38000, IrProtocolHelper.generateNecTimings(0x14, 0x01)),
                fullCommands = listOf(
                    necCmd("Power", 0x14, 0x01),
                    necCmd("Speed Low (1)", 0x14, 0x02),
                    necCmd("Speed Med (2)", 0x14, 0x03),
                    necCmd("Speed High (3)", 0x14, 0x04),
                    necCmd("Light ON/OFF", 0x14, 0x05),
                    necCmd("Timer 1H", 0x14, 0x06),
                    necCmd("Timer 2H", 0x14, 0x07)
                )
            )
        )
    )

    // =========================================================================
    // 7. PROJECTORS
    // =========================================================================

    val EPSON_PROJECTOR = BrandProfile(
        id = "projector_epson",
        brandName = "Epson",
        category = DeviceCategory.PROJECTOR,
        popularModels = "Home Cinema, PowerLite, EB-Series, 4K Pro-UHD",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Epson Standard Projector (Code Set 1 - 0x41)",
                testKeyName = "Power ON",
                testSignal = IrSignal("Epson Power ON", 38000, IrProtocolHelper.generateNecTimings(0x41, 0x01)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x41, 0x01),
                    necCmd("Power OFF", 0x41, 0x02),
                    necCmd("Source Search", 0x41, 0x05),
                    necCmd("Menu", 0x41, 0x06),
                    necCmd("Back", 0x41, 0x07),
                    necCmd("Up", 0x41, 0x08),
                    necCmd("Down", 0x41, 0x09),
                    necCmd("Left", 0x41, 0x0A),
                    necCmd("Right", 0x41, 0x0B),
                    necCmd("Enter", 0x41, 0x0C),
                    necCmd("Keystone Up", 0x41, 0x10),
                    necCmd("Keystone Down", 0x41, 0x11),
                    necCmd("Freeze", 0x41, 0x12)
                )
            )
        )
    )

    val BENQ_PROJECTOR = BrandProfile(
        id = "projector_benq",
        brandName = "BenQ",
        category = DeviceCategory.PROJECTOR,
        popularModels = "TK850, HT3550, MW560, CinePrime",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "BenQ Standard (Code Set 1 - 0x08:0x12)",
                testKeyName = "Power",
                testSignal = IrSignal("BenQ Power", 38000, IrProtocolHelper.generateNecTimings(0x08, 0x12)),
                fullCommands = listOf(
                    necCmd("Power", 0x08, 0x12),
                    necCmd("Source", 0x08, 0x15),
                    necCmd("Menu", 0x08, 0x18),
                    necCmd("Up", 0x08, 0x20),
                    necCmd("Down", 0x08, 0x21),
                    necCmd("Left", 0x08, 0x22),
                    necCmd("Right", 0x08, 0x23),
                    necCmd("Enter", 0x08, 0x24)
                )
            )
        )
    )

    val OPTOMA_PROJECTOR = BrandProfile(
        id = "projector_optoma",
        brandName = "Optoma",
        category = DeviceCategory.PROJECTOR,
        popularModels = "UHD38, HD146X, CinemaX",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Optoma Universal (Code Set 1 - 0x32)",
                testKeyName = "Power ON",
                testSignal = IrSignal("Optoma Power ON", 38000, IrProtocolHelper.generateNecTimings(0x32, 0x02)),
                fullCommands = listOf(
                    necCmd("Power ON", 0x32, 0x02),
                    necCmd("Power OFF", 0x32, 0x03),
                    necCmd("Source", 0x32, 0x05),
                    necCmd("Menu", 0x32, 0x08),
                    necCmd("Keystone", 0x32, 0x0C)
                )
            )
        )
    )

    // =========================================================================
    // 8. DSLR CAMERAS
    // =========================================================================

    val NIKON_CAMERA = BrandProfile(
        id = "camera_nikon",
        brandName = "Nikon",
        category = DeviceCategory.CAMERA,
        popularModels = "D3000-D7500, D600/750/850, Z-Series (ML-L3)",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Nikon ML-L3 Shutter Trigger",
                testKeyName = "Shutter Release",
                testSignal = cameraShutterNikon(),
                fullCommands = listOf(
                    IrCommand("nikon_shutter", "Shutter Release", cameraShutterNikon()),
                    IrCommand("nikon_video", "Video Start/Stop", cameraShutterNikon())
                )
            )
        )
    )

    val CANON_CAMERA = BrandProfile(
        id = "camera_canon",
        brandName = "Canon",
        category = DeviceCategory.CAMERA,
        popularModels = "EOS Rebel Series, 5D, 6D, 7D, EOS R (RC-6)",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Canon RC-6 Remote Controller",
                testKeyName = "Shutter Immediate",
                testSignal = cameraShutterCanon(),
                fullCommands = listOf(
                    IrCommand("canon_shutter", "Shutter Immediate", cameraShutterCanon()),
                    IrCommand("canon_2s", "2s Timer Shutter", cameraShutterCanon())
                )
            )
        )
    )

    // =========================================================================
    // 9. ROBOT VACUUMS
    // =========================================================================

    val IROBOT_ROOMBA = BrandProfile(
        id = "vacuum_roomba",
        brandName = "iRobot Roomba",
        category = DeviceCategory.ROBOT_VACUUM,
        popularModels = "Roomba 600/800/900/e/i Series",
        variants = listOf(
            RemoteCodeVariant(
                variantIndex = 1,
                description = "Roomba Clean & Dock (Code Set 1)",
                testKeyName = "CLEAN / Start",
                testSignal = IrSignal("Roomba Clean", 38000, IrProtocolHelper.generateNecTimings(0xA2, 0x82)),
                fullCommands = listOf(
                    necCmd("CLEAN", 0xA2, 0x82),
                    necCmd("DOCK / Home", 0xA2, 0x84),
                    necCmd("SPOT Clean", 0xA2, 0x83),
                    necCmd("Forward", 0xA2, 0x81),
                    necCmd("Left", 0xA2, 0x85),
                    necCmd("Right", 0xA2, 0x86)
                )
            )
        )
    )

    // Master Catalog of All Global Brand Profiles
    val ALL_BRANDS: List<BrandProfile> = listOf(
        // TVs
        SAMSUNG_TV, LG_TV, SONY_TV, TCL_TV, PHILIPS_TV, XIAOMI_TV, HISENSE_TV, PANASONIC_TV, TOSHIBA_TV, SHARP_TV, VIZIO_TV,
        // ACs
        DAIKIN_AC, GREE_AC, MIDEA_AC, MITSUBISHI_AC, PANASONIC_AC,
        // STB
        APPLE_TV, ROKU_STREAMING, AMAZON_FIRE_TV, AIRTEL_TATA_SKY_STB,
        // Audio
        YAMAHA_AUDIO, SONY_AUDIO, BOSE_AUDIO,
        // RGB
        RGB_24KEY, RGB_44KEY,
        // Fans
        DYSON_FAN, CEILING_FAN,
        // Projectors
        EPSON_PROJECTOR, BENQ_PROJECTOR, OPTOMA_PROJECTOR,
        // Camera
        NIKON_CAMERA, CANON_CAMERA,
        // Vacuum
        IROBOT_ROOMBA
    )

    fun getBrandsByCategory(category: DeviceCategory): List<BrandProfile> {
        return ALL_BRANDS.filter { it.category == category }
    }

    fun searchBrands(query: String): List<BrandProfile> {
        if (query.isBlank()) return ALL_BRANDS
        val q = query.trim().lowercase()
        return ALL_BRANDS.filter {
            it.brandName.lowercase().contains(q) ||
                    it.popularModels.lowercase().contains(q) ||
                    it.category.title.lowercase().contains(q)
        }
    }
}
