package com.example.follower.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.example.follower.data.model.DeviceType
import com.example.follower.data.model.ScanResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Scanner for Bluetooth Classic and Bluetooth Low Energy devices.
 * This is the primary detection method for Tier 1 functionality.
 */
class BluetoothScanner(
    private val context: Context,
    private val scope: CoroutineScope,
    private val locationProvider: () -> Pair<Double, Double>?,
    private val locationAccuracyProvider: () -> Float
) : AbstractScanner() {

    companion object {
        private const val TAG = "BluetoothScanner"

        // BLE scan settings
        private const val SCAN_MODE = ScanSettings.SCAN_MODE_LOW_LATENCY
        private const val REPORT_DELAY_MS = 0L
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bleScanner: BluetoothLeScanner? = null

    // Classic Bluetooth discovery receiver
    private val classicReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()

                    device?.let {
                        handleClassicDevice(it, rssi)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Restart discovery to keep scanning
                    if (_isScanning) {
                        startClassicDiscovery()
                    }
                }
            }
        }
    }

    // BLE scan callback
    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            handleBleDevice(result)
        }

        override fun onBatchScanResults(results: List<android.bluetooth.le.ScanResult>) {
            results.forEach { handleBleDevice(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed with error code: $errorCode")
        }
    }

    override fun isAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    override fun startScanning() {
        if (_isScanning) return
        if (!isAvailable()) {
            Log.w(TAG, "Bluetooth not available")
            return
        }

        _isScanning = true
        Log.d(TAG, "Starting Bluetooth scanning")

        // Register receiver for Classic Bluetooth
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(classicReceiver, filter)

        // Start Classic Bluetooth discovery
        startClassicDiscovery()

        // Start BLE scanning
        startBleScan()
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        if (!_isScanning) return

        _isScanning = false
        Log.d(TAG, "Stopping Bluetooth scanning")

        // Stop Classic Bluetooth discovery
        try {
            bluetoothAdapter?.cancelDiscovery()
            context.unregisterReceiver(classicReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping classic discovery", e)
        }

        // Stop BLE scanning
        try {
            bleScanner?.stopScan(bleScanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE scan", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startClassicDiscovery() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter?.startDiscovery()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting classic discovery", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        if (bleScanner == null) {
            Log.w(TAG, "BLE scanner not available")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(SCAN_MODE)
            .setReportDelay(REPORT_DELAY_MS)
            .build()

        // Empty filter list = scan for all devices
        val filters = emptyList<ScanFilter>()

        try {
            bleScanner?.startScan(filters, settings, bleScanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE scan", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleClassicDevice(device: BluetoothDevice, rssi: Int) {
        val location = locationProvider() ?: return
        val accuracy = locationAccuracyProvider()

        val result = ScanResult(
            macAddress = device.address,
            deviceType = DeviceType.BLUETOOTH_CLASSIC,
            deviceName = device.name,
            rssi = rssi,
            latitude = location.first,
            longitude = location.second,
            locationAccuracy = accuracy,
            bluetoothClass = device.bluetoothClass?.deviceClass,
            bondState = device.bondState
        )

        scope.launch(Dispatchers.IO) {
            emitResult(result)
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleBleDevice(scanResult: android.bluetooth.le.ScanResult) {
        val location = locationProvider() ?: return
        val accuracy = locationAccuracyProvider()

        val device = scanResult.device
        val result = ScanResult(
            macAddress = device.address,
            deviceType = DeviceType.BLUETOOTH_LE,
            deviceName = scanResult.scanRecord?.deviceName ?: device.name,
            rssi = scanResult.rssi,
            latitude = location.first,
            longitude = location.second,
            locationAccuracy = accuracy,
            bluetoothClass = device.bluetoothClass?.deviceClass,
            bondState = device.bondState
        )

        scope.launch(Dispatchers.IO) {
            emitResult(result)
        }
    }
}
