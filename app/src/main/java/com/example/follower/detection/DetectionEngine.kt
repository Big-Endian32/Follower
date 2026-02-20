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
 * Improvements over the original:
 *  - **Scan throttling**: same-device results are deduplicated within a configurable window.
 *  - **Score throttling**: a device is only re-scored after a minimum interval.
 *  - **BLE identity resolution**: randomised BLE MACs are mapped to stable IDs.
 *  - **Known tracker detection**: AirTags / Tiles / SmartTags are elevated immediately.
 *  - **Ambient density**: the number of unique devices recently seen is fed into the
 *    [SuspicionCalculator] so crowded environments produce lower scores.
 *  - **Movement gating**: when [SuspicionSettings.scanOnlyWhenMoving] is set, scoring
 *    and alerting are suppressed while the user is stationary.
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

        /** Sightings older than this are not loaded for scoring. */
        private const val SCORING_WINDOW_MS = 6 * 60 * 60 * 1000L // 6 hours

        /** Minimum user displacement (metres) to consider the user "moving". */
        private const val MOVEMENT_THRESHOLD_METERS = 50f
    }

    enum class Tier { STANDARD, ENHANCED }

    private val _currentTier = MutableStateFlow(Tier.STANDARD)
    val currentTier: StateFlow<Tier> = _currentTier.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    var onThreatDetected: ((ThreatAlert) -> Unit)? = null

    private val calculator = SuspicionCalculator(settings)

    val identityResolver = DeviceIdentityResolver()
    val knownTrackerDetector = KnownTrackerDetector()

    // ---- Throttling state -----------------------------------------------------------

    /** Last time a scan result was fully processed for a given device (resolved ID). */
    private val lastScanProcessed = HashMap<String, Long>()

    /** Last time scoring was computed for a given device (resolved ID). */
    private val lastScoreComputed = HashMap<String, Long>()

    // ---- Ambient density tracking ---------------------------------------------------

    /** Set of unique device IDs seen in the last 5 minutes, for density calculation. */
    private val recentDeviceIds = LinkedHashMap<String, Long>()

    // ---- Movement tracking ----------------------------------------------------------

    /** Anchor location for stationary detection. */
    private var anchorLat: Double? = null
    private var anchorLon: Double? = null

    // ---- Scan Processing ------------------------------------------------------------

    suspend fun processScanResult(result: ScanResult) {
        if (!_isRunning.value) return

        // Resolve BLE MAC randomisation to a stable identity
        val resolvedId = identityResolver.resolveIdentity(result)

        // --- Scan-level throttle: skip if we processed this device too recently ---
        val now = System.currentTimeMillis()
        val lastProcessed = lastScanProcessed[resolvedId] ?: 0L
        if (now - lastProcessed < settings.scanThrottleMs) return
        lastScanProcessed[resolvedId] = now

        // Track ambient density
        recentDeviceIds[resolvedId] = now
        pruneRecentDevices(now)
        calculator.ambientDeviceCount = recentDeviceIds.size

        // --- Known tracker fast-path: elevate immediately ---
        val trackerMatch = knownTrackerDetector.detect(result)

        // --- Movement gating ---
        if (settings.scanOnlyWhenMoving && !isUserMoving()) {
            // Still record the sighting (for history) but skip scoring/alerting
            recordSightingOnly(result, resolvedId)
            return
        }

        // --- Normal processing pipeline ---
        var device = repository.getDevice(resolvedId)

        if (device == null) {
            device = DetectedDevice(
                macAddress = resolvedId,
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
            val newLocationCount = repository.getDistinctLocationCount(resolvedId)
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
            deviceMacAddress = resolvedId,
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

        // --- Known tracker: immediate HIGH alert regardless of score ---
        if (trackerMatch != null && trackerMatch.confidence >= 0.7f) {
            device = device.copy(threatScore = 100f)
            repository.insertOrUpdateDevice(device)

            val alert = buildKnownTrackerAlert(device, trackerMatch)
            if (alert != null) {
                repository.createAlert(alert)
                Log.w(TAG, "Known tracker alert: ${device.macAddress} " +
                    "(${trackerMatch.trackerType}, confidence=${trackerMatch.confidence})")
                onThreatDetected?.invoke(alert)
            }
            return
        }

        // --- Score-level throttle: only re-score after the configured interval ---
        val lastScored = lastScoreComputed[resolvedId] ?: 0L
        if (now - lastScored < settings.scoreThrottleMs) return
        lastScoreComputed[resolvedId] = now

        // Load sightings within the scoring window (not the full history)
        val recentSightings = repository.getRecentSightingsForDevice(
            resolvedId, SCORING_WINDOW_MS
        )
        val suspicion = calculator.calculate(device, recentSightings)

        device = device.copy(threatScore = suspicion.totalScore.toFloat())
        repository.insertOrUpdateDevice(device)

        // Feed calibration manager (records only when calibration mode is on)
        calibrationManager?.recordSample(
            suspicion,
            avgRssi = recentSightings.map { it.rssi }.average().toFloat()
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

    private suspend fun buildKnownTrackerAlert(
        device: DetectedDevice,
        match: KnownTrackerDetector.TrackerMatch
    ): ThreatAlert? {
        // Respect cooldown
        val lastAlert = repository.getLatestAlertForDevice(device.macAddress)
        if (lastAlert != null) {
            val elapsed = System.currentTimeMillis() - lastAlert.timestamp
            if (elapsed < ALERT_COOLDOWN_MS) return null
        }

        val location = locationTracker.getLocation() ?: return null

        return ThreatAlert(
            deviceMacAddress = device.macAddress,
            deviceName = match.description,
            deviceType = device.deviceType,
            timestamp = System.currentTimeMillis(),
            threatScore = 100f,
            threatLevel = ThreatLevel.HIGH,
            latitude = location.first,
            longitude = location.second,
            sightingCount = device.detectionCount,
            locationCount = device.locationCount,
            followDurationMs = device.lastSeenTimestamp - device.firstSeenTimestamp
        )
    }

    // ---- Helpers --------------------------------------------------------------------

    /**
     * Record a sighting without scoring — used when movement gating suppresses
     * scoring (user is stationary).  The sighting is still persisted so that
     * when the user starts moving, the full history is available.
     */
    private suspend fun recordSightingOnly(result: ScanResult, resolvedId: String) {
        var device = repository.getDevice(resolvedId)
        if (device == null) {
            device = DetectedDevice(
                macAddress = resolvedId,
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
            device = device.copy(
                lastSeenTimestamp = result.timestamp,
                detectionCount = device.detectionCount + 1,
                lastRssi = result.rssi
            )
        }
        repository.insertOrUpdateDevice(device)

        val sighting = DeviceSighting(
            deviceMacAddress = resolvedId,
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
    }

    /**
     * Determine if the user is currently moving (has moved > [MOVEMENT_THRESHOLD_METERS]
     * from the anchor point).
     */
    private fun isUserMoving(): Boolean {
        val loc = locationTracker.getLocation() ?: return false

        if (anchorLat == null || anchorLon == null) {
            anchorLat = loc.first
            anchorLon = loc.second
            return false
        }

        val displacement = locationTracker.distanceBetween(
            anchorLat!!, anchorLon!!,
            loc.first, loc.second
        )

        if (displacement > MOVEMENT_THRESHOLD_METERS) {
            // User has moved — update anchor
            anchorLat = loc.first
            anchorLon = loc.second
            return true
        }

        return false
    }

    private fun pruneRecentDevices(now: Long) {
        val cutoff = now - 5 * 60 * 1000L  // 5-minute window
        recentDeviceIds.entries.removeAll { it.value < cutoff }
    }

    // ---- On-demand re-scoring (for device list views) --------------------------------

    suspend fun recalculateScore(device: DetectedDevice): SuspicionResult {
        val sightings = repository.getRecentSightingsForDevice(
            device.macAddress, SCORING_WINDOW_MS
        )
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
        val oneWeekAgo = 7 * 24 * 60 * 60 * 1000L

        val deletedSightings = repository.deleteOldSightings(oneWeekAgo)
        val deletedDevices = repository.deleteOldDevices(oneWeekAgo)
        Log.d(TAG, "Maintenance: deleted $deletedSightings sightings, $deletedDevices devices")

        // Cleanup identity resolver caches
        identityResolver.cleanup()

        // Prune throttle maps
        val now = System.currentTimeMillis()
        val throttleCutoff = now - 60 * 60 * 1000L
        lastScanProcessed.entries.removeAll { it.value < throttleCutoff }
        lastScoreComputed.entries.removeAll { it.value < throttleCutoff }
    }
}
