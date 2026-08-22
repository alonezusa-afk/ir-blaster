package com.irblaster.app.usb

import android.content.Context
import com.irblaster.app.model.IrSignal
import kotlinx.coroutines.flow.SharedFlow

/**
 * Backward-compatible repository wrapper around UsbIrManager.
 */
class UsbSerialRepository(context: Context) {
    private val manager = UsbIrManager.getInstance(context)

    val isConnected: Boolean get() = manager.isConnected
    val isListening: Boolean get() = manager.isListening
    val capturedSignals: SharedFlow<IrSignal> get() = manager.capturedSignals

    suspend fun transmit(signal: IrSignal): Result<Unit> = manager.transmit(signal)
    fun startListening() = manager.startListening()
    fun stopListening() = manager.stopListening()
    fun disconnect() = manager.disconnect()
}
