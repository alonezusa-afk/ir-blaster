package com.irblaster.app.model

/**
 * Represents an IR signal with its carrier frequency and timing transitions.
 *
 * @property name Human-readable label for the signal (e.g. "Samsung TV Power")
 * @property frequency Carrier frequency in Hertz (typically 38000 Hz)
 * @property timings Raw mark/space durations in microseconds (e.g. [9000, 4500, 560, 560...])
 * @property timestamp Unix epoch millis when recorded or created
 */
data class IrSignal(
    val name: String,
    val frequency: Int = 38000,
    val timings: List<Int>,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Formats timings into standard signed transition notation (+mark -space).
     */
    val formattedString: String
        get() {
            if (timings.isEmpty()) return "Empty Signal"
            val sb = StringBuilder()
            timings.forEachIndexed { index, duration ->
                val sign = if (index % 2 == 0) "+" else "-"
                sb.append("$sign$duration ")
            }
            return sb.toString().trim()
        }

    val previewString: String
        get() {
            val previewCount = 8
            val sub = timings.take(previewCount)
            val sb = StringBuilder()
            sub.forEachIndexed { index, duration ->
                val sign = if (index % 2 == 0) "+" else "-"
                sb.append("$sign$duration ")
            }
            if (timings.size > previewCount) {
                sb.append("... (${timings.size} pulses)")
            }
            return sb.toString().trim()
        }
}
