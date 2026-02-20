package com.example.follower.scanner

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.follower.data.model.DeviceType
import com.example.follower.data.model.ScanResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Scanner for WiFi Access Points.
 * Detects nearby APs which can help identify mobile hotspots following the user.
 */
class WifiApScanner(
    private val context: Context,
    private val scope: CoroutineScope,
    private val locationProvider: () -> Pair<Double, Double>?,
    private val locationAccuracyProvider: () -> Float
) : AbstractScanner() {

    companion object {
        private const val TAG = "WifiApScanner"
        private const val SCAN_INTERVAL_MS = 10_000L // 10 seconds between scans
    }

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val handler = Handler(Looper.getMainLooper())

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    processScanResults()
                }
            }
        }
    }

    private val scanRunnable = object : Runnable {
        override fun run() {
            if (_isScanning) {
                triggerScan()
                handler.postDelayed(this, SCAN_INTERVAL_MS)
            }
        }
    }

    override fun isAvailable(): Boolean {
        return wifiManager != null && wifiManager.isWifiEnabled
    }

    override fun startScanning() {
        if (_isScanning) return

        _isScanning = true
        Log.d(TAG, "Starting WiFi AP scanning")

        // Register receiver for scan results
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(scanReceiver, filter)

        // Start periodic scanning
        handler.post(scanRunnable)
    }

    override fun stopScanning() {
        if (!_isScanning) return

        _isScanning = false
        Log.d(TAG, "Stopping WiFi AP scanning")

        handler.removeCallbacks(scanRunnable)

        try {
            context.unregisterReceiver(scanReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun triggerScan() {
        if (wifiManager == null) return

        @Suppress("DEPRECATION")
        val success = wifiManager.startScan()
        if (!success) {
            // Scan failed, use cached results
            processScanResults()
        }
    }

    @SuppressLint("MissingPermission")
    private fun processScanResults() {
        val location = locationProvider() ?: return
        val accuracy = locationAccuracyProvider()

        val results = wifiManager?.scanResults ?: return

        for (ap in results) {
            val result = ScanResult(
                macAddress = ap.BSSID,
                deviceType = DeviceType.WIFI_ACCESS_POINT,
                deviceName = ap.SSID.takeIf { it.isNotEmpty() },
                rssi = ap.level,
                latitude = location.first,
                longitude = location.second,
                locationAccuracy = accuracy,
                ssid = ap.SSID,
                channel = frequencyToChannel(ap.frequency),
                frequency = ap.frequency
            )

            scope.launch(Dispatchers.IO) {
                emitResult(result)
            }
        }
    }

    private fun frequencyToChannel(frequency: Int): Int {
        return when {
            frequency in 2412..2484 -> (frequency - 2412) / 5 + 1
            frequency in 5170..5825 -> (frequency - 5170) / 5 + 34
            frequency in 5955..7115 -> (frequency - 5955) / 5 + 1 // 6 GHz
            else -> 0
        }
    }
}
