package com.example.follower.detection

import android.util.Log
import com.example.follower.data.model.*
import com.example.follower.data.repository.DeviceRepository
import com.example.follower.scanner.LocationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core detection engine that processes scan results and generates threat alerts.
 *
 * Scoring is delegated to [SuspicionCalculator] which produces a 0-100 score
 * based on duration, location diversity, signal strength, movement correlation,
 * and time-decay.  Thresholds are user-configurable via [SuspicionSettings].
 */
class DetectionEngine(
    private val repository: DeviceRepository,
    private val locationTracker: LocationTracker,
    private val scope: CoroutineScope,
    private val settings: SuspicionSettings,
    private val calibrationManager: CalibrationManager? = null
) {
    companion object {
        private const val TAG = "DetectionEngine"
        private const val ALERT_COOLDOWN_MS = 15 * 60 * 1000L
    }

    enum class Tier { STANDARD, ENHANCED }

    private val _currentTier = MutableStateFlow(Tier.STANDARD)
    val currentTier: StateFlow<Tier> = _currentTier.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    var onThreatDetected: ((ThreatAlert) -> Unit)? = null

    private val calculator = SuspicionCalculator(settings)

    // ---- Scan Processing ------------------------------------------------------------

    suspend fun processScanResult(result: ScanResult) {
        if (!_isRunning.value) return

        var device = repository.getDevice(result.macAddress)

        if (device == null) {
            device = DetectedDevice(
                macAddress = result.macAddress,
                deviceType = result.deviceType,
                deviceName = result.deviceName,
                firstSeenTimestamp = result.timestamp,
                lastSeenTimestamp = result.timestamp,
                detectionCount = 1,
                locationCount = 1,
                lastRssi = result.rssi,
                probedSsids = result.probedSsids?.joinToString(",")
            )
        } else {
            val newLocationCount = repository.getDistinctLocationCount(result.macAddress)
            device = device.copy(
                lastSeenTimestamp = result.timestamp,
                detectionCount = device.detectionCount + 1,
                locationCount = newLocationCount,
                lastRssi = result.rssi,
                deviceName = result.deviceName ?: device.deviceName,
                probedSsids = result.probedSsids?.joinToString(",") ?: device.probedSsids
            )
        }

        repository.insertOrUpdateDevice(device)

        val sighting = DeviceSighting(
            deviceMacAddress = result.macAddress,
            timestamp = result.timestamp,
            latitude = result.latitude,
            longitude = result.longitude,
            locationAccuracy = result.locationAccuracy,
            rssi = result.rssi,
            deviceType = result.deviceType,
            probedSsid = result.ssid,
            apSsid = if (result.deviceType == DeviceType.WIFI_ACCESS_POINT) result.ssid else null,
            channel = result.channel,
            frequency = result.frequency
        )
        repository.recordSighting(sighting)

        // Score the device using the full sighting history
        val allSightings = repository.getSightingsForDevice(result.macAddress)
        val suspicion = calculator.calculate(device, allSightings)

        device = device.copy(threatScore = suspicion.totalScore.toFloat())
        repository.insertOrUpdateDevice(device)

        // Feed calibration manager (records only when calibration mode is on)
        calibrationManager?.recordSample(
            suspicion,
            avgRssi = allSightings.map { it.rssi }.average().toFloat()
        )

        // Alert check
        if (!device.isWhitelisted && suspicion.totalScore > settings.lowThreshold) {
            checkAndGenerateAlert(device, suspicion)
        }
    }

    // ---- Alert Generation -----------------------------------------------------------

    private suspend fun checkAndGenerateAlert(
        device: DetectedDevice,
        suspicion: SuspicionResult
    ) {
        val lastAlert = repository.getLatestAlertForDevice(device.macAddress)
        if (lastAlert != null) {
            val elapsed = System.currentTimeMillis() - lastAlert.timestamp
            if (elapsed < ALERT_COOLDOWN_MS) return
        }

        val location = locationTracker.getLocation() ?: return

        val alert = ThreatAlert(
            deviceMacAddress = device.macAddress,
            deviceName = device.deviceName,
            deviceType = device.deviceType,
            timestamp = System.currentTimeMillis(),
            threatScore = suspicion.totalScore.toFloat(),
            threatLevel = suspicion.level,
            latitude = location.first,
            longitude = location.second,
            sightingCount = device.detectionCount,
            locationCount = suspicion.distinctLocationCount,
            followDurationMs = device.lastSeenTimestamp - device.firstSeenTimestamp
        )

        repository.createAlert(alert)
        Log.w(TAG, "Threat: ${device.macAddress} (${device.deviceName}) " +
            "score=${suspicion.totalScore} level=${suspicion.level}")

        onThreatDetected?.invoke(alert)
    }

    // ---- On-demand re-scoring (for device list views) --------------------------------

    suspend fun recalculateScore(device: DetectedDevice): SuspicionResult {
        val sightings = repository.getSightingsForDevice(device.macAddress)
        return calculator.calculate(device, sightings)
    }

    // ---- Tier Management ------------------------------------------------------------

    fun enableTier2() {
        _currentTier.value = Tier.ENHANCED
        Log.i(TAG, "Detection engine upgraded to Tier 2 (Enhanced)")
    }

    fun disableTier2() {
        _currentTier.value = Tier.STANDARD
        Log.i(TAG, "Detection engine reverted to Tier 1 (Standard)")
    }

    // ---- Lifecycle ------------------------------------------------------------------

    fun start() {
        _isRunning.value = true
        Log.i(TAG, "Detection engine started (${_currentTier.value})")
    }

    fun stop() {
        _isRunning.value = false
        Log.i(TAG, "Detection engine stopped")
    }

    suspend fun performMaintenance() {
        val oneDayAgo = 24 * 60 * 60 * 1000L
        val oneWeekAgo = 7 * oneDayAgo

        val deletedSightings = repository.deleteOldSightings(oneWeekAgo)
        val deletedDevices = repository.deleteOldDevices(oneWeekAgo)
        Log.d(TAG, "Maintenance: deleted $deletedSightings sightings, $deletedDevices devices")
    }
}
