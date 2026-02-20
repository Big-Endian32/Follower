package com.example.follower.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GPS location tracker using Google Play Services Fused Location Provider.
 * Provides current location for correlating device sightings with positions.
 */
class LocationTracker(private val context: Context) {

    companion object {
        private const val TAG = "LocationTracker"

        // Location request settings
        private const val UPDATE_INTERVAL_MS = 5_000L    // 5 seconds
        private const val FASTEST_INTERVAL_MS = 2_000L   // 2 seconds
        private const val PRIORITY = Priority.PRIORITY_HIGH_ACCURACY
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val locationRequest = LocationRequest.Builder(PRIORITY, UPDATE_INTERVAL_MS)
        .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
        .setWaitForAccurateLocation(true)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                _currentLocation.value = location
                Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude} (accuracy: ${location.accuracy}m)")
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            Log.d(TAG, "Location availability: ${availability.isLocationAvailable}")
        }
    }

    /**
     * Get current location as a coordinate pair, or null if not available.
     */
    fun getLocation(): Pair<Double, Double>? {
        return _currentLocation.value?.let { Pair(it.latitude, it.longitude) }
    }

    /**
     * Get current location accuracy in meters.
     */
    fun getAccuracy(): Float {
        return _currentLocation.value?.accuracy ?: Float.MAX_VALUE
    }

    /**
     * Start location tracking.
     */
    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (_isTracking.value) return

        Log.d(TAG, "Starting location tracking")

        // Get last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _currentLocation.value = it
                Log.d(TAG, "Initial location: ${it.latitude}, ${it.longitude}")
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        _isTracking.value = true
    }

    /**
     * Stop location tracking.
     */
    fun stopTracking() {
        if (!_isTracking.value) return

        Log.d(TAG, "Stopping location tracking")

        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isTracking.value = false
    }

    /**
     * Calculate distance between two points in meters.
     */
    fun distanceBetween(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * Check if two locations are within a specified radius.
     */
    fun isWithinRadius(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        radiusMeters: Float
    ): Boolean {
        return distanceBetween(lat1, lon1, lat2, lon2) <= radiusMeters
    }
}
