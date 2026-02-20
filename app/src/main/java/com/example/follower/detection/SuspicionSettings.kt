package com.example.follower.detection

import android.content.Context
import android.content.SharedPreferences

class SuspicionSettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("suspicion_settings", Context.MODE_PRIVATE)

    // --- Location Clustering ---

    var locationClusterThresholdMeters: Float
        get() = prefs.getFloat(KEY_LOCATION_CLUSTER_THRESHOLD, DEFAULT_LOCATION_CLUSTER_THRESHOLD)
        set(value) = prefs.edit().putFloat(KEY_LOCATION_CLUSTER_THRESHOLD, value).apply()

    // --- Streak Detection ---

    var streakGapThresholdMinutes: Int
        get() = prefs.getInt(KEY_STREAK_GAP_THRESHOLD, DEFAULT_STREAK_GAP_THRESHOLD)
        set(value) = prefs.edit().putInt(KEY_STREAK_GAP_THRESHOLD, value).apply()

    // --- Suspicion Thresholds (0-100 scale) ---

    var lowThreshold: Int
        get() = prefs.getInt(KEY_LOW_THRESHOLD, DEFAULT_LOW_THRESHOLD)
        set(value) = prefs.edit().putInt(KEY_LOW_THRESHOLD, value).apply()

    var highThreshold: Int
        get() = prefs.getInt(KEY_HIGH_THRESHOLD, DEFAULT_HIGH_THRESHOLD)
        set(value) = prefs.edit().putInt(KEY_HIGH_THRESHOLD, value).apply()

    // --- Initial Alert Thresholds ---

    var continuousFollowingMinutes: Int
        get() = prefs.getInt(KEY_CONTINUOUS_FOLLOWING, DEFAULT_CONTINUOUS_FOLLOWING)
        set(value) = prefs.edit().putInt(KEY_CONTINUOUS_FOLLOWING, value).apply()

    var totalFollowingMinutes: Int
        get() = prefs.getInt(KEY_TOTAL_FOLLOWING, DEFAULT_TOTAL_FOLLOWING)
        set(value) = prefs.edit().putInt(KEY_TOTAL_FOLLOWING, value).apply()

    var minDistinctLocations: Int
        get() = prefs.getInt(KEY_MIN_LOCATIONS, DEFAULT_MIN_LOCATIONS)
        set(value) = prefs.edit().putInt(KEY_MIN_LOCATIONS, value).apply()

    // --- Signal Strength ---

    var highRssiThreshold: Int
        get() = prefs.getInt(KEY_HIGH_RSSI, DEFAULT_HIGH_RSSI)
        set(value) = prefs.edit().putInt(KEY_HIGH_RSSI, value).apply()

    // --- Movement Correlation ---

    var movementCorrelationWindowMinutes: Int
        get() = prefs.getInt(KEY_CORRELATION_WINDOW, DEFAULT_CORRELATION_WINDOW)
        set(value) = prefs.edit().putInt(KEY_CORRELATION_WINDOW, value).apply()

    // --- Calibration Mode ---

    var calibrationModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALIBRATION_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_CALIBRATION_MODE, value).apply()

    // --- Battery Optimization ---

    var scanOnlyWhenMoving: Boolean
        get() = prefs.getBoolean(KEY_SCAN_ONLY_MOVING, false)
        set(value) = prefs.edit().putBoolean(KEY_SCAN_ONLY_MOVING, value).apply()

    // --- Theme ---

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_DARK) ?: THEME_DARK
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    fun resetToDefaults() {
        val currentTheme = themeMode
        prefs.edit().clear().apply()
        themeMode = currentTheme
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
    }
}
