package com.irblaster.app.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.usb.UsbDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.irblaster.app.model.IrSignal
import com.irblaster.app.ui.theme.AppThemePalette
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
    val statusBanner: String = "Dongle Disconnected",
    // Settings & Liquid Glass Customizations
    val activeTheme: AppThemePalette = AppThemePalette.CYBER_GLASS,
    val isAmoledMode: Boolean = false,
    val enableHaptics: Boolean = true,
    val enableParticleBlast: Boolean = true,
    val defaultCarrierFrequency: Int = 38000,
    val chunkDelayMs: Int = 8
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ir_blaster_settings", Context.MODE_PRIVATE)
    val usbIrManager = UsbIrManager.getInstance(application)
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val _uiState = MutableStateFlow(
        UiState(
            activeTheme = AppThemePalette.fromId(prefs.getString("theme_id", AppThemePalette.CYBER_GLASS.id) ?: AppThemePalette.CYBER_GLASS.id),
            isAmoledMode = prefs.getBoolean("amoled_mode", false),
            enableHaptics = prefs.getBoolean("haptics", true),
            enableParticleBlast = prefs.getBoolean("particle_blast", true),
            defaultCarrierFrequency = prefs.getInt("carrier_freq", 38000),
            chunkDelayMs = prefs.getInt("chunk_delay", 8)
        )
    )
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

    fun setTheme(theme: AppThemePalette) {
        prefs.edit().putString("theme_id", theme.id).apply()
        _uiState.update { it.copy(activeTheme = theme) }
        addLog("Theme changed to: ${theme.title}")
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean("amoled_mode", enabled).apply()
        _uiState.update { it.copy(isAmoledMode = enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        prefs.edit().putBoolean("haptics", enabled).apply()
        _uiState.update { it.copy(enableHaptics = enabled) }
    }

    fun setParticleBlast(enabled: Boolean) {
        prefs.edit().putBoolean("particle_blast", enabled).apply()
        _uiState.update { it.copy(enableParticleBlast = enabled) }
    }

    fun setDefaultCarrierFrequency(frequencyHz: Int) {
        prefs.edit().putInt("carrier_freq", frequencyHz).apply()
        _uiState.update { it.copy(defaultCarrierFrequency = frequencyHz) }
    }

    fun setChunkDelay(delayMs: Int) {
        prefs.edit().putInt("chunk_delay", delayMs).apply()
        _uiState.update { it.copy(chunkDelayMs = delayMs) }
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
    }

    fun onUsbPermissionGranted(device: UsbDevice) {
        addLog("USB permission confirmed for ${device.deviceName}")
        connectToDevice(device)
    }

    fun onUsbPermissionDenied(device: UsbDevice) {
        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)
        val msg = "USB permission denied by user for $vidHex:$pidHex"
        addLog("Error: $msg")
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.Error(msg),
                statusBanner = "Permission Denied"
            )
        }
    }

    fun onUsbDeviceDetached() {
        addLog("USB device detached from host")
        usbIrManager.disconnect()
        detectedUsbDevice = null
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.Disconnected,
                isListening = false,
                isTransmitting = false,
                statusBanner = "Dongle Disconnected"
            )
        }
    }

    fun connectToDevice(device: UsbDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusBanner = "Connecting to ${device.deviceName}...") }
            val result = usbIrManager.connect(device)
            val vidHex = "0x%04X".format(device.vendorId)
            val pidHex = "0x%04X".format(device.productId)

            result.fold(
                onSuccess = { modeString ->
                    addLog("✓ Successfully connected to $vidHex:$pidHex ($modeString)")
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Connected(
                                deviceName = device.deviceName,
                                vidHex = vidHex,
                                pidHex = pidHex,
                                isSerial = usbIrManager.isSerialMode
                            ),
                            statusBanner = "Connected ($vidHex:$pidHex)"
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Failed to open USB endpoint"
                    addLog("✗ Connection failed: $errorMsg")
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Error(errorMsg),
                            statusBanner = "Connection Failed"
                        )
                    }
                }
            )
        }
    }

    fun toggleListen() {
        if (_uiState.value.isListening) {
            usbIrManager.stopListening()
            _uiState.update {
                it.copy(
                    isListening = false,
                    statusBanner = "Listen Mode Stopped"
                )
            }
            addLog("Stopped listening")
        } else {
            usbIrManager.startListening()
            _uiState.update {
                it.copy(
                    isListening = true,
                    statusBanner = "Listening on IR Photodiode..."
                )
            }
            addLog("Started listening for IR carrier (38kHz)")
        }
    }

    fun transmitSignal(signal: IrSignal) {
        if (_uiState.value.isTransmitting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTransmitting = true,
                    statusBanner = "Transmitting '${signal.name}'..."
                )
            }
            addLog("Blasting: ${signal.name} (${signal.timings.size} pulses)")

            val result = usbIrManager.transmit(signal)
            result.fold(
                onSuccess = {
                    addLog("✓ Blast delivered successfully")
                    _uiState.update {
                        it.copy(
                            isTransmitting = false,
                            statusBanner = "Transmitted '${signal.name}'"
                        )
                    }
                },
                onFailure = { error ->
                    addLog("✗ Transmit error: ${error.message}")
                    _uiState.update {
                        it.copy(
                            isTransmitting = false,
                            statusBanner = "Transmit Failed"
                        )
                    }
                }
            )
        }
    }

    fun transmitRaw(frequencyHz: Int, timingsString: String) {
        val parsedTimings = parseTimingsString(timingsString)
        if (parsedTimings.isEmpty()) {
            addLog("Error: Invalid timings format. Example: +9000 -4500 +560 -560")
            _uiState.update { it.copy(statusBanner = "Invalid timing sequence") }
            return
        }

        val signal = IrSignal(
            name = "Raw_${System.currentTimeMillis() % 1000}",
            frequency = frequencyHz,
            timings = parsedTimings
        )
        transmitSignal(signal)
    }

    fun saveCapturedSignal(customName: String) {
        val lastSignal = _uiState.value.lastCapturedSignal ?: return
        val finalName = customName.ifBlank { lastSignal.name }
        val savedSignal = lastSignal.copy(name = finalName)

        _uiState.update { state ->
            state.copy(
                capturedSignals = listOf(savedSignal) + state.capturedSignals.filter { it.name != lastSignal.name },
                lastCapturedSignal = savedSignal,
                statusBanner = "Saved '$finalName' to Library"
            )
        }
        addLog("Saved captured signal as '$finalName'")
    }

    fun deleteSignal(signal: IrSignal) {
        _uiState.update { state ->
            state.copy(
                capturedSignals = state.capturedSignals.filter { it != signal },
                lastCapturedSignal = if (state.lastCapturedSignal == signal) null else state.lastCapturedSignal,
                statusBanner = "Removed '${signal.name}'"
            )
        }
        addLog("Deleted signal '${signal.name}'")
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    private fun addLog(entry: String) {
        val timestamp = timeFormatter.format(Date())
        val formatted = "[$timestamp] $entry"
        _uiState.update { state ->
            val updated = state.logs.takeLast(99) + formatted
            state.copy(logs = updated)
        }
    }

    private fun parseTimingsString(input: String): List<Int> {
        return try {
            input.replace("[,\\[\\]]".toRegex(), " ")
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .map { token ->
                    val clean = token.replace("+", "").replace("-", "")
                    clean.toInt()
                }
                .filter { it > 0 }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        usbIrManager.disconnect()
    }
}
