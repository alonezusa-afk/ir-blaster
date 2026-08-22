package com.irblaster.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.irblaster.app.ui.screens.MainScreen
import com.irblaster.app.ui.theme.IrBlasterTheme
import com.irblaster.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * MainActivity managing the USB hardware lifecycle, USB host permissions,
 * and Jetpack Compose UI binding.
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val ACTION_USB_PERMISSION = "com.irblaster.app.USB_PERMISSION"
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var usbManager: UsbManager

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(TAG, "onReceive Broadcast action: $action")

            when (action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }

                        val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        Log.d(TAG, "USB Permission callback for device: ${device?.deviceName}, granted = $granted")

                        if (granted && device != null) {
                            viewModel.onUsbPermissionGranted(device)
                        } else if (device != null) {
                            viewModel.onUsbPermissionDenied(device)
                        } else {
                            // Fallback: check device list if device was null in intent
                            inspectConnectedUsbDevices()
                        }
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }

                    device?.let {
                        Log.d(TAG, "USB Device Attached: ${it.deviceName} (VID: 0x${"%04X".format(it.vendorId)} PID: 0x${"%04X".format(it.productId)})")
                        checkAndRequestPermission(it)
                    } ?: inspectConnectedUsbDevices()
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.d(TAG, "USB Device Detached")
                    viewModel.onUsbDetached()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Register Dynamic BroadcastReceiver with RECEIVER_EXPORTED for system USB broadcasts
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }

        // Pass permission requester function to ViewModel
        viewModel.setPermissionRequester { device ->
            checkAndRequestPermission(device)
        }

        // Handle cold-start intent
        handleIntent(intent)

        setContent {
            IrBlasterTheme {
                MainScreen(
                    viewModel = viewModel,
                    onRequestPermission = {
                        val device = viewModel.detectedUsbDevice ?: usbManager.deviceList.values.firstOrNull()
                        if (device != null) {
                            checkAndRequestPermission(device)
                        } else {
                            inspectConnectedUsbDevices()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Allow the Activity window to gain complete foreground focus before triggering system dialog
        lifecycleScope.launch {
            delay(300)
            inspectConnectedUsbDevices()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }
            device?.let { checkAndRequestPermission(it) }
        }
    }

    fun inspectConnectedUsbDevices() {
        val deviceList = usbManager.deviceList
        Log.d(TAG, "Scanning USB devices: ${deviceList.size} connected")

        if (deviceList.isEmpty()) {
            Log.d(TAG, "No USB devices detected.")
            return
        }

        for ((_, device) in deviceList) {
            Log.d(TAG, "Discovered USB Device: ${device.deviceName} (VID: 0x${"%04X".format(device.vendorId)} PID: 0x${"%04X".format(device.productId)})")
            checkAndRequestPermission(device)
            break // Handle first matching device
        }
    }

    fun checkAndRequestPermission(device: UsbDevice) {
        viewModel.onUsbDeviceDiscovered(device)

        if (usbManager.hasPermission(device)) {
            Log.d(TAG, "Permission already granted for ${device.deviceName}. Connecting...")
            viewModel.onUsbPermissionGranted(device)
        } else {
            Log.d(TAG, "Requesting USB permission popup for ${device.deviceName} (VID: 0x${"%04X".format(device.vendorId)})")
            viewModel.onUsbPermissionRequested(device)

            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION).apply {
                    setPackage(packageName)
                },
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            usbManager.requestPermission(device, permissionIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver already unregistered: ${e.message}")
        }
    }
}
