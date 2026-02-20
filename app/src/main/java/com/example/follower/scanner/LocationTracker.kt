package com.example.follower.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GPS location tracker with dual-provider strategy:
 *
 *  1. **Google Play Services** `FusedLocationProviderClient` — used when GMS
 *     is present (stock Android, microG).
 *  2. **Platform `LocationManager`** — used as a fallback on de-Googled ROMs
 *     like GrapheneOS where GMS is absent.
 *
 * The provider is chosen once at [startTracking] time based on runtime
 * availability of the GMS classes.  This ensures the app works on any
 * Android device that has a GPS chip, regardless of whether Google Play
 * Services are installed.
 */
class LocationTracker(private val context: Context) {

    companion object {
        private const val TAG = "LocationTracker"

        // Location request settings
        private const val UPDATE_INTERVAL_MS = 5_000L    // 5 seconds
        private const val FASTEST_INTERVAL_MS = 2_000L   // 2 seconds
        private const val MIN_DISTANCE_METERS = 0f       // report every update
    }

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    /** True if we successfully initialised the GMS provider. */
    private var usingGms = false

    // ---- GMS provider (nullable — only set when GMS is available) -------------------

    private var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient? = null
    private var gmsLocationCallback: com.google.android.gms.location.LocationCallback? = null

    // ---- Platform provider ----------------------------------------------------------

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val platformLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location
            Log.d(TAG, "Platform location: ${location.latitude}, ${location.longitude} " +
                "(accuracy: ${location.accuracy}m, provider: ${location.provider})")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "Provider enabled: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "Provider disabled: $provider")
        }

        @Deprecated("Required for API < 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    // ---- Public API -----------------------------------------------------------------

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
     * Start location tracking.  Tries GMS first; falls back to platform
     * LocationManager if GMS classes are not available.
     */
    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (_isTracking.value) return

        Log.d(TAG, "Starting location tracking")

        if (isGmsAvailable()) {
            startGmsTracking()
        } else {
            Log.i(TAG, "GMS not available, using platform LocationManager")
            startPlatformTracking()
        }

        _isTracking.value = true
    }

    /**
     * Stop location tracking.
     */
    fun stopTracking() {
        if (!_isTracking.value) return

        Log.d(TAG, "Stopping location tracking")

        if (usingGms) {
            stopGmsTracking()
        } else {
            stopPlatformTracking()
        }

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

    // ---- GMS provider ---------------------------------------------------------------

    private fun isGmsAvailable(): Boolean {
        return try {
            // Check if the GMS Location class is loadable at runtime
            Class.forName("com.google.android.gms.location.LocationServices")
            // Also verify the API is actually functional (microG, etc.)
            com.google.android.gms.common.GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGmsTracking() {
        try {
            val client = com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(context)
            fusedLocationClient = client

            // Grab last known location immediately
            client.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _currentLocation.value = it
                    Log.d(TAG, "GMS initial location: ${it.latitude}, ${it.longitude}")
                }
            }

            val request = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                UPDATE_INTERVAL_MS
            )
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                .setWaitForAccurateLocation(true)
                .build()

            val callback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    result.lastLocation?.let { location ->
                        _currentLocation.value = location
                        Log.d(TAG, "GMS location: ${location.latitude}, ${location.longitude} " +
                            "(accuracy: ${location.accuracy}m)")
                    }
                }

                override fun onLocationAvailability(
                    availability: com.google.android.gms.location.LocationAvailability
                ) {
                    if (!availability.isLocationAvailable) {
                        Log.w(TAG, "GMS location unavailable, falling back to platform")
                        stopGmsTracking()
                        startPlatformTracking()
                    }
                }
            }
            gmsLocationCallback = callback

            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            usingGms = true
            Log.i(TAG, "Using GMS FusedLocationProvider")
        } catch (e: Exception) {
            Log.w(TAG, "GMS location init failed, falling back to platform", e)
            startPlatformTracking()
        }
    }

    private fun stopGmsTracking() {
        gmsLocationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        fusedLocationClient = null
        gmsLocationCallback = null
    }

    // ---- Platform provider ----------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startPlatformTracking() {
        usingGms = false

        // Grab last known from any provider
        val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        // Pick the most recent
        val bestLast = listOfNotNull(lastGps, lastNetwork).maxByOrNull { it.time }
        bestLast?.let {
            _currentLocation.value = it
            Log.d(TAG, "Platform initial location: ${it.latitude}, ${it.longitude} " +
                "(provider: ${it.provider})")
        }

        // Request updates from GPS
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                UPDATE_INTERVAL_MS,
                MIN_DISTANCE_METERS,
                platformLocationListener,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Registered GPS_PROVIDER updates")
        }

        // Also request network provider for faster initial fix
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                UPDATE_INTERVAL_MS,
                MIN_DISTANCE_METERS,
                platformLocationListener,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Registered NETWORK_PROVIDER updates")
        }

        Log.i(TAG, "Using platform LocationManager")
    }

    private fun stopPlatformTracking() {
        locationManager.removeUpdates(platformLocationListener)
    }
}
