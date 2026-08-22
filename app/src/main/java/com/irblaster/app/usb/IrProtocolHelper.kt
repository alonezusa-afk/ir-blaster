package com.irblaster.app.usb

import com.irblaster.app.model.IrSignal
import kotlin.math.roundToInt

/**
 * Production-grade IR Protocol Engine matching the exact reverse-engineered
 * Ocrustar / ElkSmart / Tiqiaa / ZaZa (iodn/android-ir-blaster & deadboy18) specifications.
 */
object IrProtocolHelper {

    val HANDSHAKE_WAKE = byteArrayOf(
        0xFC.toByte(), 0xFC.toByte(), 0xFC.toByte(), 0xFC.toByte()
    )

    val HANDSHAKE_CONFIRM = byteArrayOf(
        0xFA.toByte(), 0xFA.toByte(), 0xFA.toByte(), 0xFA.toByte()
    )

    val LEARN_START = byteArrayOf(
        0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte()
    )

    val LEARN_STOP = byteArrayOf(
        0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte()
    )

    val PREAMBLE_FF = byteArrayOf(
        0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
    )

    /**
     * Mangles a protocol header byte by bit-reversing and performing bitwise NOT.
     * Required by Ocrustar / ElkSmart / ZaZa hardware firmware.
     */
    fun mangleByte(b: Int): Byte {
        val rev = Integer.reverse(b and 0xFF) ushr 24
        return (rev.inv() and 0xFF).toByte()
    }

    /**
     * Builds the native Ocrustar / ElkSmart transmission packet:
     * - 4-byte Preamble: [0xFF, 0xFF, 0xFF, 0xFF]
     * - 3-byte Carrier Frequency: [freq_hi, freq_mid, freq_lo] (Mangled)
     * - 2-byte Payload Length: [len_hi, len_lo] (Mangled)
     * - Variable Payload: 16µs compressed timings with 0xFF (4080µs) carry markers.
     */
    fun encodeOcrustarPayload(frequencyHz: Int = 38000, timings: List<Int>): ByteArray {
        val rawPayload = ArrayList<Byte>()

        // Compress microsecond pulses into 16µs units with 0xFF overflow markers
        for (duration in timings) {
            var rem = duration
            while (rem >= 4080) {
                rawPayload.add(0xFF.toByte())
                rem -= 4080
            }
            val units = (rem / 16.0).roundToInt().coerceIn(1, 0xFE)
            rawPayload.add(units.toByte())
        }

        val payloadLen = rawPayload.size
        val freq = frequencyHz.coerceIn(20000, 60000)

        val totalSize = 4 + 3 + 2 + payloadLen
        val packet = ByteArray(totalSize)

        // 1. Preamble [0xFF, 0xFF, 0xFF, 0xFF]
        packet[0] = 0xFF.toByte()
        packet[1] = 0xFF.toByte()
        packet[2] = 0xFF.toByte()
        packet[3] = 0xFF.toByte()

        // 2. Mangled Frequency (3 bytes: Hi, Mid, Lo)
        packet[4] = mangleByte((freq shr 16) and 0xFF)
        packet[5] = mangleByte((freq shr 8) and 0xFF)
        packet[6] = mangleByte(freq and 0xFF)

        // 3. Mangled Length (2 bytes: Hi, Lo)
        packet[7] = mangleByte((payloadLen shr 8) and 0xFF)
        packet[8] = mangleByte(payloadLen and 0xFF)

        // 4. Encoded Timings Payload
        for (i in 0 until payloadLen) {
            packet[9 + i] = rawPayload[i]
        }

        return packet
    }

    /**
     * Fallback standard 16-bit big-endian payload for generic USB UART dongles.
     */
    fun encodeStandard16BitPayload(frequencyHz: Int = 38000, timings: List<Int>): ByteArray {
        val freqScaled = (frequencyHz / 100).coerceIn(1, 0xFFFF)
        val length = timings.size.coerceIn(0, 0xFFFF)
        val buffer = ByteArray(8 + (length * 2))

        buffer[0] = 0xFF.toByte()
        buffer[1] = 0xFF.toByte()
        buffer[2] = 0xFF.toByte()
        buffer[3] = 0xFF.toByte()

        buffer[4] = ((freqScaled shr 8) and 0xFF).toByte()
        buffer[5] = (freqScaled and 0xFF).toByte()

        buffer[6] = ((length shr 8) and 0xFF).toByte()
        buffer[7] = (length and 0xFF).toByte()

        var offset = 8
        for (timing in timings) {
            val clamped = timing.coerceIn(0, 0xFFFF)
            buffer[offset] = ((clamped shr 8) and 0xFF).toByte()
            buffer[offset + 1] = (clamped and 0xFF).toByte()
            offset += 2
        }

        return buffer
    }

    /**
     * Decodes incoming USB packets using the native Ocrustar/ElkSmart 16µs decoding algorithm,
     * with fallback to 16-bit big endian.
     */
    fun decodeReceivedPayload(buffer: ByteArray, length: Int): List<Int> {
        if (length < 2) return emptyList()

        var offset = 0

        // Strip leading 0xFF preamble if present
        while (offset < length - 3 &&
            buffer[offset] == 0xFF.toByte() &&
            buffer[offset + 1] == 0xFF.toByte() &&
            buffer[offset + 2] == 0xFF.toByte() &&
            buffer[offset + 3] == 0xFF.toByte()
        ) {
            offset += 4
        }

        // Skip 5-byte header (freq 3B + len 2B) if present
        if (length - offset >= 5) {
            val possibleFreq = ((buffer[offset].toInt() and 0xFF) shl 16) or
                    ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
                    (buffer[offset + 2].toInt() and 0xFF)
            if (possibleFreq != 0) {
                offset += 5
            }
        }

        // 1. Native Ocrustar 16µs accumulator: b < 0xFF -> b * 16 + carry, b == 0xFF -> carry += 4080
        val timingsOcrustar = ArrayList<Int>()
        var carry = 0

        for (i in offset until length) {
            val b = buffer[i].toInt() and 0xFF
            if (b == 0xFF) {
                carry += 4080
            } else if (b > 0) {
                val duration = (b * 16) + carry
                timingsOcrustar.add(duration)
                carry = 0
            }
        }

        if (timingsOcrustar.size >= 4) {
            return timingsOcrustar
        }

        // 2. Fallback 16-bit Big-Endian Pairs
        val timings16Bit = ArrayList<Int>()
        val count = (length - offset) / 2
        for (i in 0 until count) {
            val hi = buffer[offset + (i * 2)].toInt() and 0xFF
            val lo = buffer[offset + (i * 2) + 1].toInt() and 0xFF
            val value = (hi shl 8) or lo
            if (value in 40..65000) {
                timings16Bit.add(value)
            }
        }

        return if (timings16Bit.size >= 4) timings16Bit else emptyList()
    }

    /**
     * Generates standard NEC 38kHz protocol raw pulse timings for given address and command.
     */
    fun generateNecTimings(address: Int, command: Int): List<Int> {
        val timings = ArrayList<Int>(68)

        // Leader: 9000µs MARK, 4500µs SPACE
        timings.add(9000)
        timings.add(4500)

        fun appendByte(value: Int) {
            for (bit in 0 until 8) {
                val isOne = ((value shr bit) and 1) == 1
                timings.add(560) // Pulse
                timings.add(if (isOne) 1690 else 560) // Space
            }
        }

        val addr = address and 0xFF
        val addrInv = (addr.inv()) and 0xFF
        val cmd = command and 0xFF
        val cmdInv = (cmd.inv()) and 0xFF

        appendByte(addr)
        appendByte(addrInv)
        appendByte(cmd)
        appendByte(cmdInv)

        // Stop bit
        timings.add(560)
        return timings
    }

    val PRESET_SIGNALS: List<IrSignal> = listOf(
        IrSignal(
            name = "NEC TV Power (0x00, 0x12)",
            frequency = 38000,
            timings = generateNecTimings(address = 0x00, command = 0x12)
        ),
        IrSignal(
            name = "NEC TV Mute (0x00, 0x0D)",
            frequency = 38000,
            timings = generateNecTimings(address = 0x00, command = 0x0D)
        ),
        IrSignal(
            name = "NEC Volume Up (0x00, 0x1E)",
            frequency = 38000,
            timings = generateNecTimings(address = 0x00, command = 0x1E)
        ),
        IrSignal(
            name = "NEC Volume Down (0x00, 0x1F)",
            frequency = 38000,
            timings = generateNecTimings(address = 0x00, command = 0x1F)
        ),
        IrSignal(
            name = "Samsung TV Power (0x07, 0x02)",
            frequency = 38000,
            timings = generateNecTimings(address = 0x07, command = 0x02)
        )
    )

    fun ByteArray.toHex(separator: String = " "): String {
        return joinToString(separator) { "%02X".format(it) }
    }
}
