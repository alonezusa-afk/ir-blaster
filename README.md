# USB Type-C IR Blaster & Receiver (Android)

> ⚠️ **UNDER CONSTRUCTION / ACTIVE DEVELOPMENT**
> This project is currently under active development. Hardware protocols, transmission routines, and UI instruments are continuously being updated and refined.

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-blue.svg)](https://android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)](https://kotlinlang.org)
[![Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Status](https://img.shields.io/badge/Status-Under%20Construction-orange.svg)]()

A high-performance Android application built with **Jetpack Compose** that interfaces natively with USB Type-C IR Blaster & Learning dongles (Getzget 7th Gen, Ocrustar, ZaZa Remote, Tiqiaa, and ElkSmart) using raw USB Bulk endpoints and custom vendor communication protocols.

---

## ⚡ Features

- **Direct USB Bulk Endpoint Engine**: Communicates over native USB Bulk IN (`0x81`) and OUT (`0x01`) endpoints without relying on Android's restricted `ConsumerIrManager`.
- **Integrated Protocol Support**:
  - **Handshake Sequence**: Hardware wake-up (`0xFC`) and activation confirmation (`0xFA`).
  - **Learn Mode Photodiode Control**: Actively powers ON the photodiode receiver (`0xFE`) and powers down on stop (`0xFD`).
  - **Mangled Header Encoding**: Preamble (`0xFF 0xFF 0xFF 0xFF`) with bit-reversed/inverted carrier frequency and length bytes + 16µs compressed timings.
- **Digital Logic Timing Analyzer**:
  - Crisp high-contrast square-wave digital oscilloscope trace (`3.3V Mark` vs `0.0V Space`).
  - Pulse duration bar histogram showing timing distributions.
  - Live optical signal power meter and millisecond timeline markings.
  - Interactive time-axis zoom (`1x` to `8x`) with smooth horizontal panning.
- **Pre-configured Test Remote Presets**: Quick transmit buttons for NEC TV Power, Mute, Volume Up/Down, and Samsung TV.
- **Signal Cloning & Learning**: Capture and decode raw microsecond pulses from physical remote controls.

---

## 🔌 Hardware Compatibility

| Dongle Model | USB VID / PID | Interface Type | Supported Modes |
| :--- | :--- | :--- | :--- |
| **Getzget 7th Gen / Ocrustar** | `0x045C` : `0x0134` / `0x02AA` | USB Bulk Transfer | Transmit & Receive / Learn |
| **ZaZa / Tiqiaa Tview** | `0x10C4` : `0x8468` | USB Bulk Transfer | Transmit & Receive / Learn |
| **CH340 / CP2102 / FTDI Dongles** | `0x1A86` / `0x10C4` / `0x0403` | USB Serial (CDC/UART) | Transmit & Receive |

---

## 🛠️ Build & Installation

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android Device running Android 8.0 (API 26) or higher with USB OTG support

### Building from Source
```bash
# Clone the repository
git clone https://github.com/cid-moosa/ir-blaster.git

# Navigate to project directory
cd ir-blaster

# Compile debug APK
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Quickstart

1. Install the APK on your Android smartphone.
2. Connect your USB Type-C IR dongle.
3. Tap **"Grant USB Permission / Connect"** on the connection prompt.
4. Go to **Listen / Learn** mode to capture remote signals, or use **Transmit** mode to blast IR codes.

---

## 👤 Author

Developed by **[cid-moosa](https://github.com/cid-moosa)**.
Contributions and feedback are welcome!
