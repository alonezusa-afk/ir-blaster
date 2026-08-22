package com.irblaster.app.usb

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.irblaster.app.model.IrSignal
import com.irblaster.app.usb.IrProtocolHelper.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Universal USB IR Hardware Manager with Dual-Layer Communication:
 *
 * Layer A (Native Raw Bulk Endpoint):
 *   - Used for Getzget 7th Gen / Ocrustar / ZaZa / ElkSmart (VID: 0x045C) and Tiqiaa (0x10C4:0x8468).
 *   - Direct 64-byte packet transfers on Bulk OUT (0x01) and Bulk IN (0x81).
 *
 * Layer B (USB Serial CDC/UART):
 *   - Used for CH340, CP2102, FTDI serial bridge chips at 115200 8N1.
 */
class UsbIrManager(private val context: Context) {

    companion object {
        private const val TAG = "UsbIrManager"
        private const val BAUD_RATE = 115200
        private const val DATA_BITS = 8
        private const val READ_TIMEOUT_MS = 1000
        private const val WRITE_TIMEOUT_MS = 1000
        private const val BUFFER_SIZE = 4096

        @Volatile
        private var instance: UsbIrManager? = null

        fun getInstance(context: Context): UsbIrManager {
            return instance ?: synchronized(this) {
                instance ?: UsbIrManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val scope = CoroutineScope(Dispatchers.IO)

    // Hardware handles
    private var serialPort: UsbSerialPort? = null
    private var rawConnection: UsbDeviceConnection? = null
    private var rawInterface: UsbInterface? = null
    private var bulkEndpointIn: UsbEndpoint? = null
    private var bulkEndpointOut: UsbEndpoint? = null

    var activeDevice: UsbDevice? = null
        private set

    @Volatile
    var isConnected: Boolean = false
        private set

    @Volatile
    var isSerialMode: Boolean = false
        private set

    @Volatile
    var isListening: Boolean = false
        private set

    private var listenJob: Job? = null

    // Flows
    private val _capturedSignals = MutableSharedFlow<IrSignal>(extraBufferCapacity = 10)
    val capturedSignals: SharedFlow<IrSignal> = _capturedSignals.asSharedFlow()

    private val _liveWaterfallFlow = MutableSharedFlow<Float>(extraBufferCapacity = 100)
    val liveWaterfallFlow: SharedFlow<Float> = _liveWaterfallFlow.asSharedFlow()

    private val _logFlow = MutableSharedFlow<String>(extraBufferCapacity = 100)
    val logFlow: SharedFlow<String> = _logFlow.asSharedFlow()

    private fun log(message: String) {
        Log.d(TAG, message)
        _logFlow.tryEmit(message)
    }

    /**
     * Connects to the USB IR device, prioritizing Raw Bulk Endpoints for 0x045C/Tiqiaa,
     * and USB Serial for standard UART chips.
     */
    suspend fun connect(device: UsbDevice): Result<String> = withContext(Dispatchers.IO) {
        disconnect()

        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)
        log("Connecting to USB IR Dongle: ${device.deviceName} ($vidHex : $pidHex)")

        val isKnownRawDongle = device.vendorId == 0x045C ||
                (device.vendorId == 0x10C4 && device.productId == 0x8468)

        // -------------------------------------------------------------
        // Strategy 1: Raw USB Bulk Endpoints (Preferred for ZaZa / Getzget)
        // -------------------------------------------------------------
        if (isKnownRawDongle || device.interfaceCount > 0) {
            val rawResult = connectRawBulk(device)
            if (rawResult.isSuccess) {
                return@withContext rawResult
            }
        }

        // -------------------------------------------------------------
        // Strategy 2: USB Serial (Fallback for CH340 / CP2102 / FTDI)
        // -------------------------------------------------------------
        val serialResult = connectSerial(device)
        if (serialResult.isSuccess) {
            return@withContext serialResult
        }

        // Strategy 3: Try Raw Bulk again if serial was attempted first
        val fallbackRaw = connectRawBulk(device)
        if (fallbackRaw.isSuccess) {
            return@withContext fallbackRaw
        }

        val err = "Unable to bind USB endpoints (Raw & Serial failed)"
        log("Connection error: $err")
        Result.failure(IllegalStateException(err))
    }

    private suspend fun connectRawBulk(device: UsbDevice): Result<String> {
        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)

        try {
            log("Attempting Direct Raw USB Bulk Interface...")
            val connection = usbManager.openDevice(device)
                ?: return Result.failure(SecurityException("Unable to open UsbDeviceConnection"))

            var targetInterface: UsbInterface? = null
            var inEp: UsbEndpoint? = null
            var outEp: UsbEndpoint? = null

            for (i in 0 until device.interfaceCount) {
                val intf = device.getInterface(i)
                var tempIn: UsbEndpoint? = null
                var tempOut: UsbEndpoint? = null

                for (e in 0 until intf.endpointCount) {
                    val ep = intf.getEndpoint(e)
                    if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK || ep.type == UsbConstants.USB_ENDPOINT_XFER_INT) {
                        if (ep.direction == UsbConstants.USB_DIR_IN && tempIn == null) {
                            tempIn = ep
                        } else if (ep.direction == UsbConstants.USB_DIR_OUT && tempOut == null) {
                            tempOut = ep
                        }
                    }
                }

                if (tempIn != null || tempOut != null) {
                    targetInterface = intf
                    inEp = tempIn
                    outEp = tempOut
                    break
                }
            }

            if (targetInterface == null || (inEp == null && outEp == null)) {
                connection.close()
                return Result.failure(IllegalStateException("No Bulk/Interrupt endpoints found"))
            }

            val claimed = connection.claimInterface(targetInterface, true)
            if (!claimed) {
                connection.close()
                return Result.failure(IllegalStateException("Failed to claim interface ${targetInterface.id}"))
            }

            rawConnection = connection
            rawInterface = targetInterface
            bulkEndpointIn = inEp
            bulkEndpointOut = outEp
            activeDevice = device
            isConnected = true
            isSerialMode = false

            log("Raw USB Bulk Endpoints bound (IN: ${inEp?.address}, OUT: ${outEp?.address})")
            performHandshake()
            return Result.success("Connected via Raw USB Bulk ($vidHex:$pidHex)")
        } catch (e: Exception) {
            log("Raw Bulk connection failed: ${e.message}")
            cleanup()
            return Result.failure(e)
        }
    }

    private suspend fun connectSerial(device: UsbDevice): Result<String> {
        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)

        try {
            val table = ProbeTable()
            table.addProduct(0x045C, 0x0134, CdcAcmSerialDriver::class.java)
            table.addProduct(0x045C, 0x02AA, CdcAcmSerialDriver::class.java)
            table.addProduct(0x1A86, 0x7523, Ch34xSerialDriver::class.java)
            table.addProduct(0x1A86, 0x5523, Ch34xSerialDriver::class.java)
            table.addProduct(0x10C4, 0xEA60, Cp21xxSerialDriver::class.java)
            table.addProduct(0x10C4, 0x8468, Cp21xxSerialDriver::class.java)
            table.addProduct(0x0403, 0x6001, FtdiSerialDriver::class.java)

            val customProber = UsbSerialProber(table)
            var drivers = customProber.findAllDrivers(usbManager)
            if (drivers.isEmpty()) {
                drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
            }

            val driver = drivers.firstOrNull { it.device.deviceId == device.deviceId }
                ?: return Result.failure(IllegalStateException("No matching serial driver"))

            val connection = usbManager.openDevice(device)
                ?: return Result.failure(SecurityException("Unable to open USB connection for serial"))

            val port = driver.ports[0]
            port.open(connection)
            port.setParameters(BAUD_RATE, DATA_BITS, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port.dtr = true
            port.rts = true

            serialPort = port
            rawConnection = connection
            activeDevice = device
            isConnected = true
            isSerialMode = true

            log("USB Serial port opened: 115200 8N1")
            performHandshake()
            return Result.success("Connected via USB Serial ($vidHex:$pidHex)")
        } catch (e: Exception) {
            log("Serial connection attempt failed: ${e.message}")
            cleanup()
            return Result.failure(e)
        }
    }

    /**
     * Executes the mandatory hardware wake-up and confirmation handshake.
     */
    suspend fun performHandshake(): Result<Unit> = withContext(Dispatchers.IO) {
        log("Executing Handshake / Wake-up sequence...")

        val wakeBytes = IrProtocolHelper.HANDSHAKE_WAKE
        log("TX Wake: [${wakeBytes.toHex()}]")
        writeRaw(wakeBytes)

        delay(120)

        val responseBuffer = ByteArray(BUFFER_SIZE)
        val readCount = readRaw(responseBuffer, 500)
        if (readCount > 0) {
            val responseSlice = responseBuffer.copyOfRange(0, readCount)
            log("RX Wake Response: [${responseSlice.toHex()}] ($readCount bytes)")
        } else {
            log("RX Wake Response: No echo (device ready in blast mode)")
        }

        delay(80)

        val confirmBytes = IrProtocolHelper.HANDSHAKE_CONFIRM
        log("TX Confirm: [${confirmBytes.toHex()}]")
        writeRaw(confirmBytes)

        delay(100)

        val confirmAckCount = readRaw(responseBuffer, 500)
        if (confirmAckCount > 0) {
            log("RX Confirm Ack: [${responseBuffer.copyOfRange(0, confirmAckCount).toHex()}]")
        }

        log("Hardware initialized and ready.")
        Result.success(Unit)
    }

    /**
     * Transmits an IR timing sequence to the blaster hardware.
     * Uses 64-byte frame chunking to ensure reliable delivery across all USB OTG chipsets.
     */
    suspend fun transmit(signal: IrSignal): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            val error = "Transmit failed: Dongle not connected"
            log(error)
            return@withContext Result.failure(IllegalStateException(error))
        }

        try {
            log("Transmitting IR signal: '${signal.name}' (${signal.timings.size} pulses @ ${signal.frequency}Hz)")

            // Generate payload matching hardware mode
            val payload = if (!isSerialMode) {
                IrProtocolHelper.encodeOcrustarPayload(signal.frequency, signal.timings)
            } else {
                IrProtocolHelper.encodeStandard16BitPayload(signal.frequency, signal.timings)
            }
            log("TX Payload (${payload.size} bytes): [${payload.toHex()}]")

            // Send in 64-byte USB chunks
            var offset = 0
            val chunkSize = 64
            var totalWritten = 0

            while (offset < payload.size) {
                val end = minOf(offset + chunkSize, payload.size)
                val chunk = payload.copyOfRange(offset, end)
                val bytesSent = writeRaw(chunk)
                if (bytesSent >= 0) {
                    totalWritten += bytesSent
                }
                offset += chunkSize
                if (offset < payload.size) {
                    delay(8)
                }
            }

            log("TX Complete: $totalWritten bytes delivered to IR hardware.")

            // Feed the live waterfall animation
            _liveWaterfallFlow.tryEmit(1.0f)

            // Read device echo / ACK (e.g. 0xFF 0xFF 0xFF 0xFF)
            delay(50)
            val ackBuf = ByteArray(256)
            val ackLen = readRaw(ackBuf, 300)
            if (ackLen > 0) {
                log("RX Blaster Ack: [${ackBuf.copyOfRange(0, ackLen).toHex()}]")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            log("Transmit Exception: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Starts continuous listening on USB IN endpoint.
     * Dispatches hardware command [0xFE, 0xFE, 0xFE, 0xFE] to activate the receiver photodiode.
     */
    fun startListening() {
        if (!isConnected || isListening) return
        isListening = true
        log("Listen / Learn Mode Started — Initializing Photodiode Hardware...")

        listenJob = scope.launch {
            // 1. Send LEARN_START command to power ON the photodiode circuit
            val startCmd = IrProtocolHelper.LEARN_START
            log("TX Learn Start: [${startCmd.toHex()}]")
            writeRaw(startCmd)
            delay(50)

            val readBuffer = ByteArray(BUFFER_SIZE)
            val ackLen = readRaw(readBuffer, 200)
            if (ackLen > 0) {
                log("RX Learn Start Ack: [${readBuffer.copyOfRange(0, ackLen).toHex()}]")
            }

            log("Photodiode Active — Point physical remote at dongle (1-3 cm)...")

            val accumulator = ArrayList<Byte>(BUFFER_SIZE)
            var lastByteTime = 0L

            while (isListening && isActive) {
                try {
                    val bytesRead = readRaw(readBuffer, 80)

                    if (bytesRead > 0) {
                        lastByteTime = System.currentTimeMillis()
                        val slice = readBuffer.copyOfRange(0, bytesRead)
                        log("RX Stream ($bytesRead bytes): [${slice.toHex()}]")

                        // Signal detected: notify flow
                        _liveWaterfallFlow.emit(1.0f)

                        for (b in slice) {
                            accumulator.add(b)
                        }

                        // Try to decode as packets stream in
                        if (accumulator.size >= 4) {
                            val accArray = accumulator.toByteArray()
                            val timings = IrProtocolHelper.decodeReceivedPayload(accArray, accArray.size)
                            if (timings.size >= 4) {
                                val captured = IrSignal(
                                    name = "Captured_${System.currentTimeMillis() % 10000}",
                                    frequency = 38000,
                                    timings = timings
                                )
                                log("✓ Captured Signal Decoded: ${timings.size} pulses! [${captured.previewString}]")
                                _capturedSignals.emit(captured)
                                accumulator.clear()
                            }
                        }
                    } else {
                        // Inactivity flush check (if bytes arrived then paused for >120ms)
                        if (accumulator.isNotEmpty() && (System.currentTimeMillis() - lastByteTime > 120)) {
                            val accArray = accumulator.toByteArray()
                            val timings = IrProtocolHelper.decodeReceivedPayload(accArray, accArray.size)
                            if (timings.size >= 4) {
                                val captured = IrSignal(
                                    name = "Captured_${System.currentTimeMillis() % 10000}",
                                    frequency = 38000,
                                    timings = timings
                                )
                                log("✓ Captured Signal Flushed: ${timings.size} pulses! [${captured.previewString}]")
                                _capturedSignals.emit(captured)
                            }
                            accumulator.clear()
                        }
                    }
                } catch (e: Exception) {
                    if (isListening) {
                        log("Listen read error: ${e.message}")
                    }
                }
                delay(15)
            }

            // Power down photodiode when loop finishes
            try {
                val stopCmd = IrProtocolHelper.LEARN_STOP
                log("TX Learn Stop: [${stopCmd.toHex()}]")
                writeRaw(stopCmd)
            } catch (e: Exception) {
                // Ignore during shutdown
            }
            log("Listen Mode Stopped.")
        }
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        listenJob?.cancel()
        listenJob = null

        scope.launch {
            try {
                val stopCmd = IrProtocolHelper.LEARN_STOP
                log("TX Learn Stop: [${stopCmd.toHex()}]")
                writeRaw(stopCmd)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun writeRaw(data: ByteArray): Int {
        return if (!isSerialMode && rawConnection != null && bulkEndpointOut != null) {
            rawConnection!!.bulkTransfer(bulkEndpointOut, data, data.size, WRITE_TIMEOUT_MS)
        } else if (isSerialMode && serialPort != null) {
            serialPort!!.write(data, WRITE_TIMEOUT_MS)
            data.size
        } else if (rawConnection != null && bulkEndpointOut != null) {
            rawConnection!!.bulkTransfer(bulkEndpointOut, data, data.size, WRITE_TIMEOUT_MS)
        } else {
            -1
        }
    }

    private fun readRaw(buffer: ByteArray, timeoutMs: Int): Int {
        return if (!isSerialMode && rawConnection != null && bulkEndpointIn != null) {
            rawConnection!!.bulkTransfer(bulkEndpointIn, buffer, buffer.size, timeoutMs)
        } else if (isSerialMode && serialPort != null) {
            try {
                serialPort!!.read(buffer, timeoutMs)
            } catch (e: Exception) {
                -1
            }
        } else if (rawConnection != null && bulkEndpointIn != null) {
            rawConnection!!.bulkTransfer(bulkEndpointIn, buffer, buffer.size, timeoutMs)
        } else {
            -1
        }
    }

    fun disconnect() {
        stopListening()
        cleanup()
        log("USB Connection Closed.")
    }

    private fun cleanup() {
        try {
            serialPort?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Serial close: ${e.message}")
        }
        try {
            rawInterface?.let { rawConnection?.releaseInterface(it) }
            rawConnection?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Raw close: ${e.message}")
        }

        serialPort = null
        rawConnection = null
        rawInterface = null
        bulkEndpointIn = null
        bulkEndpointOut = null
        activeDevice = null
        isConnected = false
        isSerialMode = false
    }
}
