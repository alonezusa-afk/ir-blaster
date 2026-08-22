package com.irblaster.app.usb

import com.irblaster.app.model.IrSignal

/**
 * Backward-compatible bridge delegating to IrProtocolHelper.
 */
object IrProtocol {
    fun buildHandshakeWake(): ByteArray = IrProtocolHelper.HANDSHAKE_WAKE
    fun buildHandshakeConfirm(): ByteArray = IrProtocolHelper.HANDSHAKE_CONFIRM
    fun buildTransmitPayload(frequency: Int, timings: List<Int>): ByteArray =
        IrProtocolHelper.encodeOcrustarPayload(frequency, timings)
    fun parseReceivedData(buffer: ByteArray, length: Int): List<Int> =
        IrProtocolHelper.decodeReceivedPayload(buffer, length)
}
