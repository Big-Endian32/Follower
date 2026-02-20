package com.example.follower.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages USB WiFi adapter detection and connection.
 * Handles automatic tier switching when ALFA adapter is plugged in.
 */
class UsbAdapterManager(private val context: Context) {

    companion object {
        private const val TAG = "UsbAdapterManager"
        private const val ACTION_USB_PERMISSION = "com.example.follower.USB_PERMISSION"

        // Supported USB WiFi adapter identifiers
        val SUPPORTED_ADAPTERS = listOf(
            // ALFA AWUS036H (RTL8187)
            UsbDeviceId(vendorId = 0x0bda, productId = 0x8187, name = "ALFA AWUS036H"),
            // RTL8187B variant
            UsbDeviceId(vendorId = 0x0bda, productId = 0x8189, name = "RTL8187B"),
            // ALFA AWUS036NHA (Atheros AR9271) - future support
            UsbDeviceId(vendorId = 0x0cf3, productId = 0x9271, name = "ALFA AWUS036NHA"),
        )
    }

    data class UsbDeviceId(
        val vendorId: Int,
        val productId: Int,
        val name: String
    )

    enum class AdapterState {
        DISCONNECTED,
        CONNECTED_NO_PERMISSION,
        CONNECTED_READY,
        ERROR
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _adapterState = MutableStateFlow(AdapterState.DISCONNECTED)
    val adapterState: StateFlow<AdapterState> = _adapterState.asStateFlow()

    private val _connectedAdapter = MutableStateFlow<UsbDeviceId?>(null)
    val connectedAdapter: StateFlow<UsbDeviceId?> = _connectedAdapter.asStateFlow()

    private var currentDevice: UsbDevice? = null
    private var currentConnection: UsbDeviceConnection? = null

    // Callback when adapter is ready for use
    var onAdapterReady: ((UsbDevice) -> Unit)? = null
    var onAdapterDisconnected: (() -> Unit)? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = getUsbDeviceFromIntent(intent)
                    device?.let { handleDeviceAttached(it) }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = getUsbDeviceFromIntent(intent)
                    device?.let { handleDeviceDetached(it) }
                }

                ACTION_USB_PERMISSION -> {
                    val device = getUsbDeviceFromIntent(intent)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    handlePermissionResult(device, granted)
                }
            }
        }
    }

    private fun getUsbDeviceFromIntent(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    /**
     * Initialize the USB manager and start listening for events.
     */
    fun initialize() {
        Log.d(TAG, "Initializing USB adapter manager")

        // Register receiver for USB events
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }

        // Check for already connected devices
        checkConnectedDevices()
    }

    /**
     * Clean up resources.
     */
    fun shutdown() {
        Log.d(TAG, "Shutting down USB adapter manager")

        try {
            context.unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        closeConnection()
    }

    /**
     * Check for compatible adapters that are already connected.
     */
    private fun checkConnectedDevices() {
        val deviceList = usbManager.deviceList

        for (device in deviceList.values) {
            if (isCompatibleAdapter(device)) {
                Log.i(TAG, "Found compatible adapter: ${device.deviceName}")
                handleDeviceAttached(device)
                return
            }
        }
    }

    /**
     * Check if a USB device is a supported WiFi adapter.
     */
    fun isCompatibleAdapter(device: UsbDevice): Boolean {
        return SUPPORTED_ADAPTERS.any {
            it.vendorId == device.vendorId && it.productId == device.productId
        }
    }

    /**
     * Get the supported adapter info for a device.
     */
    private fun getAdapterInfo(device: UsbDevice): UsbDeviceId? {
        return SUPPORTED_ADAPTERS.find {
            it.vendorId == device.vendorId && it.productId == device.productId
        }
    }

    /**
     * Handle when a compatible adapter is attached.
     */
    private fun handleDeviceAttached(device: UsbDevice) {
        if (!isCompatibleAdapter(device)) return

        Log.i(TAG, "Compatible adapter attached: VID=${device.vendorId}, PID=${device.productId}")

        currentDevice = device
        _connectedAdapter.value = getAdapterInfo(device)

        // Check if we have permission
        if (usbManager.hasPermission(device)) {
            openConnection(device)
        } else {
            // Request permission
            _adapterState.value = AdapterState.CONNECTED_NO_PERMISSION
            requestPermission(device)
        }
    }

    /**
     * Handle when an adapter is detached.
     */
    private fun handleDeviceDetached(device: UsbDevice) {
        if (currentDevice?.deviceId == device.deviceId) {
            Log.i(TAG, "Adapter detached: ${device.deviceName}")

            closeConnection()

            currentDevice = null
            _connectedAdapter.value = null
            _adapterState.value = AdapterState.DISCONNECTED

            onAdapterDisconnected?.invoke()
        }
    }

    /**
     * Handle permission request result.
     */
    private fun handlePermissionResult(device: UsbDevice?, granted: Boolean) {
        if (device == null) return

        if (granted) {
            Log.i(TAG, "USB permission granted")
            openConnection(device)
        } else {
            Log.w(TAG, "USB permission denied")
            _adapterState.value = AdapterState.ERROR
        }
    }

    /**
     * Request permission to access the USB device.
     */
    private fun requestPermission(device: UsbDevice) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            flags
        )

        usbManager.requestPermission(device, permissionIntent)
    }

    /**
     * Open a connection to the USB device.
     */
    private fun openConnection(device: UsbDevice) {
        try {
            currentConnection = usbManager.openDevice(device)

            if (currentConnection != null) {
                Log.i(TAG, "USB connection opened successfully")
                _adapterState.value = AdapterState.CONNECTED_READY
                onAdapterReady?.invoke(device)
            } else {
                Log.e(TAG, "Failed to open USB connection")
                _adapterState.value = AdapterState.ERROR
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening USB connection", e)
            _adapterState.value = AdapterState.ERROR
        }
    }

    /**
     * Close the current USB connection.
     */
    private fun closeConnection() {
        currentConnection?.close()
        currentConnection = null
    }

    /**
     * Get the current USB device connection (for driver use).
     */
    fun getConnection(): UsbDeviceConnection? = currentConnection

    /**
     * Get the current USB device.
     */
    fun getDevice(): UsbDevice? = currentDevice
}
