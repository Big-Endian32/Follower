package com.example.follower.detection

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists all detection configuration in SharedPreferences with in-memory
 * caching.  Settings are read from disk once on construction and refreshed
 * only when a write occurs or [refreshCache] is called explicitly, avoiding
 * repeated disk I/O during scoring hot-paths.
 */
class SuspicionSettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("suspicion_settings", Context.MODE_PRIVATE)

    // ---- In-memory cache (populated on construction, updated on writes) ----

    private var _locationClusterThresholdMeters: Float = 0f
    private var _streakGapThresholdMinutes: Int = 0
    private var _lowThreshold: Int = 0
    private var _highThreshold: Int = 0
    private var _continuousFollowingMinutes: Int = 0
    private var _totalFollowingMinutes: Int = 0
    private var _minDistinctLocations: Int = 0
    private var _highRssiThreshold: Int = 0
    private var _movementCorrelationWindowMinutes: Int = 0
    private var _calibrationModeEnabled: Boolean = false
    private var _scanOnlyWhenMoving: Boolean = false
    private var _themeMode: String = THEME_DARK
    private var _scanThrottleMs: Long = 0
    private var _scoreThrottleMs: Long = 0

    init {
        refreshCache()
    }

    /**
     * Reload all cached values from SharedPreferences.
     */
    fun refreshCache() {
        _locationClusterThresholdMeters = prefs.getFloat(KEY_LOCATION_CLUSTER_THRESHOLD, DEFAULT_LOCATION_CLUSTER_THRESHOLD)
        _streakGapThresholdMinutes = prefs.getInt(KEY_STREAK_GAP_THRESHOLD, DEFAULT_STREAK_GAP_THRESHOLD)
        _lowThreshold = prefs.getInt(KEY_LOW_THRESHOLD, DEFAULT_LOW_THRESHOLD)
        _highThreshold = prefs.getInt(KEY_HIGH_THRESHOLD, DEFAULT_HIGH_THRESHOLD)
        _continuousFollowingMinutes = prefs.getInt(KEY_CONTINUOUS_FOLLOWING, DEFAULT_CONTINUOUS_FOLLOWING)
        _totalFollowingMinutes = prefs.getInt(KEY_TOTAL_FOLLOWING, DEFAULT_TOTAL_FOLLOWING)
        _minDistinctLocations = prefs.getInt(KEY_MIN_LOCATIONS, DEFAULT_MIN_LOCATIONS)
        _highRssiThreshold = prefs.getInt(KEY_HIGH_RSSI, DEFAULT_HIGH_RSSI)
        _movementCorrelationWindowMinutes = prefs.getInt(KEY_CORRELATION_WINDOW, DEFAULT_CORRELATION_WINDOW)
        _calibrationModeEnabled = prefs.getBoolean(KEY_CALIBRATION_MODE, false)
        _scanOnlyWhenMoving = prefs.getBoolean(KEY_SCAN_ONLY_MOVING, false)
        _themeMode = prefs.getString(KEY_THEME_MODE, THEME_DARK) ?: THEME_DARK
        _scanThrottleMs = prefs.getLong(KEY_SCAN_THROTTLE, DEFAULT_SCAN_THROTTLE_MS)
        _scoreThrottleMs = prefs.getLong(KEY_SCORE_THROTTLE, DEFAULT_SCORE_THROTTLE_MS)
    }

    // --- Location Clustering ---

    var locationClusterThresholdMeters: Float
        get() = _locationClusterThresholdMeters
        set(value) {
            _locationClusterThresholdMeters = value
            prefs.edit().putFloat(KEY_LOCATION_CLUSTER_THRESHOLD, value).apply()
        }

    // --- Streak Detection ---

    var streakGapThresholdMinutes: Int
        get() = _streakGapThresholdMinutes
        set(value) {
            _streakGapThresholdMinutes = value
            prefs.edit().putInt(KEY_STREAK_GAP_THRESHOLD, value).apply()
        }

    // --- Suspicion Thresholds (0-100 scale) ---

    var lowThreshold: Int
        get() = _lowThreshold
        set(value) {
            _lowThreshold = value
            prefs.edit().putInt(KEY_LOW_THRESHOLD, value).apply()
        }

    var highThreshold: Int
        get() = _highThreshold
        set(value) {
            _highThreshold = value
            prefs.edit().putInt(KEY_HIGH_THRESHOLD, value).apply()
        }

    // --- Initial Alert Thresholds ---

    var continuousFollowingMinutes: Int
        get() = _continuousFollowingMinutes
        set(value) {
            _continuousFollowingMinutes = value
            prefs.edit().putInt(KEY_CONTINUOUS_FOLLOWING, value).apply()
        }

    var totalFollowingMinutes: Int
        get() = _totalFollowingMinutes
        set(value) {
            _totalFollowingMinutes = value
            prefs.edit().putInt(KEY_TOTAL_FOLLOWING, value).apply()
        }

    var minDistinctLocations: Int
        get() = _minDistinctLocations
        set(value) {
            _minDistinctLocations = value
            prefs.edit().putInt(KEY_MIN_LOCATIONS, value).apply()
        }

    // --- Signal Strength ---

    var highRssiThreshold: Int
        get() = _highRssiThreshold
        set(value) {
            _highRssiThreshold = value
            prefs.edit().putInt(KEY_HIGH_RSSI, value).apply()
        }

    // --- Movement Correlation ---

    var movementCorrelationWindowMinutes: Int
        get() = _movementCorrelationWindowMinutes
        set(value) {
            _movementCorrelationWindowMinutes = value
            prefs.edit().putInt(KEY_CORRELATION_WINDOW, value).apply()
        }

    // --- Calibration Mode ---

    var calibrationModeEnabled: Boolean
        get() = _calibrationModeEnabled
        set(value) {
            _calibrationModeEnabled = value
            prefs.edit().putBoolean(KEY_CALIBRATION_MODE, value).apply()
        }

    // --- Battery Optimization ---

    var scanOnlyWhenMoving: Boolean
        get() = _scanOnlyWhenMoving
        set(value) {
            _scanOnlyWhenMoving = value
            prefs.edit().putBoolean(KEY_SCAN_ONLY_MOVING, value).apply()
        }

    // --- Throttling ---

    /** Minimum interval between processing scan results for the same device (ms). */
    var scanThrottleMs: Long
        get() = _scanThrottleMs
        set(value) {
            _scanThrottleMs = value
            prefs.edit().putLong(KEY_SCAN_THROTTLE, value).apply()
        }

    /** Minimum interval between re-scoring the same device (ms). */
    var scoreThrottleMs: Long
        get() = _scoreThrottleMs
        set(value) {
            _scoreThrottleMs = value
            prefs.edit().putLong(KEY_SCORE_THROTTLE, value).apply()
        }

    // --- Theme ---

    var themeMode: String
        get() = _themeMode
        set(value) {
            _themeMode = value
            prefs.edit().putString(KEY_THEME_MODE, value).apply()
        }

    fun resetToDefaults() {
        val currentTheme = themeMode
        prefs.edit().clear().apply()
        themeMode = currentTheme
        refreshCache()
    }

    companion object {
        private const val KEY_LOCATION_CLUSTER_THRESHOLD = "location_cluster_threshold"
        private const val KEY_STREAK_GAP_THRESHOLD = "streak_gap_threshold"
        private const val KEY_LOW_THRESHOLD = "low_threshold"
        private const val KEY_HIGH_THRESHOLD = "high_threshold"
        private const val KEY_CONTINUOUS_FOLLOWING = "continuous_following_minutes"
        private const val KEY_TOTAL_FOLLOWING = "total_following_minutes"
        private const val KEY_MIN_LOCATIONS = "min_distinct_locations"
        private const val KEY_HIGH_RSSI = "high_rssi_threshold"
        private const val KEY_CORRELATION_WINDOW = "correlation_window_minutes"
        private const val KEY_CALIBRATION_MODE = "calibration_mode"
        private const val KEY_SCAN_ONLY_MOVING = "scan_only_when_moving"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SCAN_THROTTLE = "scan_throttle_ms"
        private const val KEY_SCORE_THROTTLE = "score_throttle_ms"

        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"

        const val DEFAULT_LOCATION_CLUSTER_THRESHOLD = 500f
        const val DEFAULT_STREAK_GAP_THRESHOLD = 10
        const val DEFAULT_LOW_THRESHOLD = 30
        const val DEFAULT_HIGH_THRESHOLD = 61
        const val DEFAULT_CONTINUOUS_FOLLOWING = 15
        const val DEFAULT_TOTAL_FOLLOWING = 45
        const val DEFAULT_MIN_LOCATIONS = 3
        const val DEFAULT_HIGH_RSSI = -60
        const val DEFAULT_CORRELATION_WINDOW = 5
        const val DEFAULT_SCAN_THROTTLE_MS = 5_000L
        const val DEFAULT_SCORE_THROTTLE_MS = 60_000L
    }
}
