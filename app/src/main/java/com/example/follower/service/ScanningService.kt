package com.example.follower.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.follower.FollowerApplication
import com.example.follower.R
import com.example.follower.data.model.ThreatAlert
import com.example.follower.data.model.ThreatLevel
import com.example.follower.detection.DetectionEngine
import com.example.follower.scanner.BluetoothScanner
import com.example.follower.scanner.LocationTracker
import com.example.follower.scanner.WifiApScanner
import com.example.follower.ui.main.MainActivity
import com.example.follower.usb.Usb80211Driver
import com.example.follower.usb.UsbAdapterManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service that performs continuous scanning for tracking detection.
 */
class ScanningService : Service() {

    companion object {
        private const val TAG = "ScanningService"
        private const val NOTIFICATION_ID = 1
        private const val ALERT_NOTIFICATION_ID = 2
        private const val CHANNEL_ID_SCANNING = "scanning_channel"
        private const val CHANNEL_ID_ALERTS = "alerts_channel"
    }

    inner class LocalBinder : Binder() {
        fun getService(): ScanningService = this@ScanningService
    }

    private val binder = LocalBinder()

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Components
    private lateinit var locationTracker: LocationTracker
    private lateinit var bluetoothScanner: BluetoothScanner
    private lateinit var wifiApScanner: WifiApScanner
    private lateinit var detectionEngine: DetectionEngine
    private lateinit var usbAdapterManager: UsbAdapterManager

    private var usbDriver: Usb80211Driver? = null

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentTier = MutableStateFlow(DetectionEngine.Tier.STANDARD)
    val currentTier: StateFlow<DetectionEngine.Tier> = _currentTier.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        createNotificationChannels()
        initializeComponents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        startForegroundService()
        startScanning()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        stopScanning()
        serviceScope.cancel()
        usbAdapterManager.shutdown()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Scanning channel (low priority)
            val scanningChannel = NotificationChannel(
                CHANNEL_ID_SCANNING,
                getString(R.string.notification_channel_scanning),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when tracking detection is active"
                setShowBadge(false)
            }

            // Alerts channel (high priority)
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when potential trackers are detected"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(scanningChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    private fun startForegroundService() {
        val notification = createScanningNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createScanningNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val tierText = when (_currentTier.value) {
            DetectionEngine.Tier.STANDARD -> getString(R.string.mode_standard)
            DetectionEngine.Tier.ENHANCED -> getString(R.string.mode_enhanced)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_SCANNING)
            .setContentTitle(getString(R.string.notification_scanning_title))
            .setContentText(tierText)
            .setSmallIcon(R.drawable.ic_scan)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun initializeComponents() {
        val app = application as FollowerApplication

        // Initialize location tracker
        locationTracker = LocationTracker(this)

        // Initialize scanners
        bluetoothScanner = BluetoothScanner(
            context = this,
            scope = serviceScope,
            locationProvider = { locationTracker.getLocation() },
            locationAccuracyProvider = { locationTracker.getAccuracy() }
        )

        wifiApScanner = WifiApScanner(
            context = this,
            scope = serviceScope,
            locationProvider = { locationTracker.getLocation() },
            locationAccuracyProvider = { locationTracker.getAccuracy() }
        )

        // Initialize detection engine
        detectionEngine = DetectionEngine(
            repository = app.repository,
            locationTracker = locationTracker,
            scope = serviceScope,
            settings = app.suspicionSettings,
            calibrationManager = app.calibrationManager
        )

        // Set up threat callback
        detectionEngine.onThreatDetected = { alert ->
            showThreatNotification(alert)
        }

        // Initialize USB adapter manager
        usbAdapterManager = UsbAdapterManager(this)
        usbAdapterManager.onAdapterReady = { device ->
            enableTier2(device)
        }
        usbAdapterManager.onAdapterDisconnected = {
            disableTier2()
        }
        usbAdapterManager.initialize()

        // Collect scan results
        serviceScope.launch {
            bluetoothScanner.scanResults.collect { result ->
                try {
                    detectionEngine.processScanResult(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing Bluetooth scan result", e)
                }
            }
        }

        serviceScope.launch {
            wifiApScanner.scanResults.collect { result ->
                try {
                    detectionEngine.processScanResult(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing WiFi scan result", e)
                }
            }
        }

        // Update tier state
        serviceScope.launch {
            detectionEngine.currentTier.collect { tier ->
                _currentTier.value = tier
                updateNotification()
            }
        }
    }

    private fun startScanning() {
        Log.i(TAG, "Starting scanning")

        locationTracker.startTracking()
        bluetoothScanner.startScanning()
        wifiApScanner.startScanning()
        detectionEngine.start()

        _isScanning.value = true

        // Schedule maintenance
        serviceScope.launch {
            while (isActive) {
                delay(60 * 60 * 1000L) // Every hour
                detectionEngine.performMaintenance()
                // Flush any pending calibration samples to disk
                (application as FollowerApplication).calibrationManager.flushPendingSamples()
            }
        }
    }

    private fun stopScanning() {
        Log.i(TAG, "Stopping scanning")

        bluetoothScanner.stopScanning()
        wifiApScanner.stopScanning()
        locationTracker.stopTracking()
        detectionEngine.stop()

        usbDriver?.close()
        usbDriver = null

        // Flush any pending calibration samples before shutdown
        (application as FollowerApplication).calibrationManager.flushPendingSamples()

        _isScanning.value = false
    }

    private fun enableTier2(device: android.hardware.usb.UsbDevice) {
        Log.i(TAG, "Enabling Tier 2 with USB adapter")

        val connection = usbAdapterManager.getConnection() ?: return

        usbDriver = Usb80211Driver(device, connection)

        if (usbDriver?.initialize() == true && usbDriver?.enableMonitorMode() == true) {
            // Start capturing probe requests
            usbDriver?.startCapture(serviceScope)

            // Collect probe requests
            serviceScope.launch {
                usbDriver?.probeRequests?.collect { probe ->
                    val location = locationTracker.getLocation() ?: return@collect
                    val accuracy = locationTracker.getAccuracy()

                    val result = usbDriver?.probeToScanResult(
                        probe, location.first, location.second, accuracy
                    )
                    result?.let { detectionEngine.processScanResult(it) }
                }
            }

            detectionEngine.enableTier2()
        } else {
            Log.e(TAG, "Failed to initialize USB driver")
            usbDriver?.close()
            usbDriver = null
        }
    }

    private fun disableTier2() {
        Log.i(TAG, "Disabling Tier 2")

        usbDriver?.close()
        usbDriver = null

        detectionEngine.disableTier2()
    }

    private fun updateNotification() {
        val notification = createScanningNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showThreatNotification(alert: ThreatAlert) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alert_id", alert.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alert.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = getString(R.string.notification_threat_title)
        val text = buildString {
            append(alert.deviceName ?: alert.deviceMacAddress)
            append(" - ")
            append(when (alert.threatLevel) {
                ThreatLevel.LOW -> getString(R.string.threat_low)
                ThreatLevel.MEDIUM -> getString(R.string.threat_medium)
                ThreatLevel.HIGH -> getString(R.string.threat_high)
            })
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(ALERT_NOTIFICATION_ID + alert.id.toInt(), notification)
    }
}
