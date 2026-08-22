package com.irblaster.app.viewmodel

import android.app.Application
import android.hardware.usb.UsbDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.irblaster.app.model.IrSignal
import com.irblaster.app.usb.IrProtocolHelper
import com.irblaster.app.usb.UsbIrManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class DeviceDetected(
        val deviceName: String,
        val vidHex: String,
        val pidHex: String
    ) : ConnectionState
    data class PermissionRequested(
        val deviceName: String,
        val vidHex: String,
        val pidHex: String
    ) : ConnectionState
    data class Connected(
        val deviceName: String,
        val vidHex: String,
        val pidHex: String,
        val isSerial: Boolean
    ) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class UiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val isListening: Boolean = false,
    val isTransmitting: Boolean = false,
    val capturedSignals: List<IrSignal> = IrProtocolHelper.PRESET_SIGNALS,
    val lastCapturedSignal: IrSignal? = null,
    val logs: List<String> = emptyList(),
    val statusBanner: String = "Dongle Disconnected"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val usbIrManager = UsbIrManager.getInstance(application)
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var detectedUsbDevice: UsbDevice? = null
        private set

    private var permissionRequester: ((UsbDevice) -> Unit)? = null

    init {
        // Collect captured signals from hardware
        viewModelScope.launch {
            usbIrManager.capturedSignals.collect { signal ->
                _uiState.update { state ->
                    state.copy(
                        lastCapturedSignal = signal,
                        capturedSignals = listOf(signal) + state.capturedSignals,
                        statusBanner = "Signal Captured: ${signal.timings.size} pulses"
                    )
                }
            }
        }

        // Collect raw hardware logs
        viewModelScope.launch {
            usbIrManager.logFlow.collect { entry ->
                addLog(entry)
            }
        }
    }

    fun setPermissionRequester(requester: (UsbDevice) -> Unit) {
        this.permissionRequester = requester
    }

    fun triggerPermissionRequest() {
        detectedUsbDevice?.let { device ->
            addLog("Manual permission trigger for ${device.deviceName}")
            permissionRequester?.invoke(device)
        }
    }

    fun onUsbDeviceDiscovered(device: UsbDevice) {
        detectedUsbDevice = device
        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)
        addLog("Discovered USB Device: ${device.deviceName} ($vidHex : $pidHex)")

        if (_uiState.value.connectionState !is ConnectionState.Connected) {
            _uiState.update {
                it.copy(
                    connectionState = ConnectionState.DeviceDetected(device.deviceName, vidHex, pidHex),
                    statusBanner = "Device Detected: $vidHex:$pidHex"
                )
            }
        }
    }

    fun onUsbPermissionRequested(device: UsbDevice) {
        detectedUsbDevice = device
        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.PermissionRequested(device.deviceName, vidHex, pidHex),
                statusBanner = "Permission Requested ($vidHex:$pidHex)"
            )
        }
        addLog("Awaiting USB permission grant for ${device.deviceName} ($vidHex:$pidHex)...")
    }

    fun onUsbPermissionGranted(device: UsbDevice) {
        detectedUsbDevice = device
        addLog("USB permission granted. Connecting to ${device.deviceName}...")
        viewModelScope.launch {
            val result = usbIrManager.connect(device)
            val vidHex = "0x%04X".format(device.vendorId)
            val pidHex = "0x%04X".format(device.productId)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        connectionState = ConnectionState.Connected(
                            deviceName = device.deviceName,
                            vidHex = vidHex,
                            pidHex = pidHex,
                            isSerial = usbIrManager.isSerialMode
                        ),
                        statusBanner = "Connected ($vidHex:$pidHex) [${if (usbIrManager.isSerialMode) "Serial" else "Raw Bulk"}]"
                    )
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Connection failed"
                _uiState.update {
                    it.copy(
                        connectionState = ConnectionState.Error(error),
                        statusBanner = "Error: $error"
                    )
                }
            }
        }
    }

    fun onUsbPermissionDenied(device: UsbDevice) {
        addLog("USB permission denied for ${device.deviceName}")
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.Error("Permission Denied for ${device.deviceName}"),
                statusBanner = "USB Permission Denied. Tap to retry."
            )
        }
    }

    fun onUsbDetached() {
        detectedUsbDevice = null
        usbIrManager.disconnect()
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.Disconnected,
                isListening = false,
                isTransmitting = false,
                statusBanner = "Dongle Disconnected"
            )
        }
        addLog("USB device detached.")
    }

    fun toggleListen() {
        if (!usbIrManager.isConnected) {
            addLog("Cannot listen: Dongle not connected")
            return
        }

        if (_uiState.value.isListening) {
            usbIrManager.stopListening()
            _uiState.update { it.copy(isListening = false, statusBanner = "Listening Stopped") }
        } else {
            usbIrManager.startListening()
            _uiState.update { it.copy(isListening = true, statusBanner = "Listening for IR Signals...") }
        }
    }

    fun transmitSignal(signal: IrSignal) {
        if (!usbIrManager.isConnected) {
            addLog("Cannot transmit: Dongle not connected")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTransmitting = true, statusBanner = "Transmitting '${signal.name}'...") }
            val result = usbIrManager.transmit(signal)
            _uiState.update {
                it.copy(
                    isTransmitting = false,
                    statusBanner = if (result.isSuccess) "Transmitted '${signal.name}'" else "Transmit Failed"
                )
            }
        }
    }

    fun transmitRaw(frequencyHz: Int, rawTimingsText: String) {
        try {
            val timings = rawTimingsText
                .replace("+", "")
                .replace("-", "")
                .split(",", " ", "\n", "\t")
                .filter { it.isNotBlank() }
                .map { it.trim().toInt() }

            if (timings.isEmpty()) {
                addLog("Invalid raw timings format")
                return
            }

            val signal = IrSignal(
                name = "Manual_TX_${System.currentTimeMillis() % 1000}",
                frequency = frequencyHz,
                timings = timings
            )
            transmitSignal(signal)
        } catch (e: Exception) {
            addLog("Raw timing parse error: ${e.message}")
        }
    }

    fun saveCapturedSignal(customName: String) {
        val last = _uiState.value.lastCapturedSignal ?: return
        val updated = last.copy(name = customName.ifBlank { "Saved_${System.currentTimeMillis() % 1000}" })

        _uiState.update { state ->
            val list = state.capturedSignals.filter { it !== last }
            state.copy(
                capturedSignals = listOf(updated) + list,
                lastCapturedSignal = updated,
                statusBanner = "Saved signal as '${updated.name}'"
            )
        }
        addLog("Signal saved as '${updated.name}'")
    }

    fun deleteSignal(signal: IrSignal) {
        _uiState.update { state ->
            state.copy(capturedSignals = state.capturedSignals.filter { it !== signal })
        }
        addLog("Deleted signal '${signal.name}'")
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    private fun addLog(message: String) {
        val timestamp = timeFormatter.format(Date())
        val entry = "[$timestamp] $message"
        _uiState.update { state ->
            val maxLogs = 250
            val newLogs = state.logs + entry
            state.copy(logs = if (newLogs.size > maxLogs) newLogs.takeLast(maxLogs) else newLogs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        usbIrManager.disconnect()
    }
}
