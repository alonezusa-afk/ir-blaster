package com.irblaster.app.model

import com.irblaster.app.usb.IrProtocolHelper

/**
 * Universal Appliance Category Types matching ZaZa Remote / Ocrustar taxonomy.
 */
enum class DeviceCategory(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String
) {
    TV("tv", "Television (TV)", "Smart TVs, OLED, QLED, LCD", "Tv"),
    AC("ac", "Air Conditioner (AC)", "Inverter, Split, Window, Portable", "AcUnit"),
    SET_TOP_BOX("stb", "Set-Top Box / TV Box", "Cable, Satellite, IPTV, Android Box", "LiveTv"),
    AUDIO("audio", "Audio & Soundbars", "AV Receivers, Amplifiers, Soundbars", "Speaker"),
    PROJECTOR("projector", "Projectors", "Home Theater & Office Projectors", "Videocam"),
    RGB_LIGHTS("rgb", "RGB LED Lights", "24-Key, 44-Key, Magic Home Strips", "Lightbulb"),
    FAN("fan", "Fans & Purifiers", "Dyson, Tower Fans, Ceiling Fans", "Air"),
    DVD("dvd", "DVD & Blu-ray", "Media Players & Disc Decks", "Album"),
    CAMERA("camera", "DSLR Cameras", "Nikon, Canon, Sony IR Shutter", "PhotoCamera"),
    ROBOT_VACUUM("vacuum", "Robot Vacuums", "Roomba, Roborock, Eufy Vacuums", "SmartToy")
}

/**
 * Single IR Code Preset Variant for a specific brand/model family.
 */
data class RemoteCodeVariant(
    val variantIndex: Int,
    val description: String,
    val testKeyName: String,
    val testSignal: IrSignal,
    val fullCommands: List<IrCommand>
)

/**
 * Brand Profile with multiple Code Variants for smart auto-pairing / searching.
 */
data class BrandProfile(
    val id: String,
    val brandName: String,
    val category: DeviceCategory,
    val popularModels: String,
    val variants: List<RemoteCodeVariant>
)

/**
 * Saved User Remote in their custom room collection.
 */
data class SavedRemote(
    val id: String,
    val customName: String,
    val category: DeviceCategory,
    val brandName: String,
    val selectedVariant: RemoteCodeVariant
)
