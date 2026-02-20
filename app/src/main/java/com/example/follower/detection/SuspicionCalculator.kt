package com.example.follower.detection

import android.location.Location
import com.example.follower.data.model.DetectedDevice
import com.example.follower.data.model.DeviceSighting
import com.example.follower.data.model.ThreatLevel
import kotlin.math.roundToInt

data class SuspicionResult(
    val totalScore: Int,
    val durationFactor: Float,
    val locationFactor: Float,
    val rssiModifier: Float,
    val correlationModifier: Float,
    val decayModifier: Float,
    val distinctLocationCount: Int,
    val totalExposureMinutes: Float,
    val longestStreakMinutes: Float,
    val level: ThreatLevel
)

/**
 * Calculates a 0-100 suspicion score for a device based on sighting history.
 *
 * Score breakdown:
 *   Duration factor:  0-50 pts (exposure time + longest continuous streak)
 *   Location factor:  0-50 pts (distinct locations via Haversine clustering)
 *   Modifiers:        RSSI (+10%), movement correlation (+20%), time decay
 */
class SuspicionCalculator(private val settings: SuspicionSettings) {

    fun calculate(device: DetectedDevice, sightings: List<DeviceSighting>): SuspicionResult {
        if (device.isWhitelisted || sightings.size < 2) {
            return emptySuspicion()
        }

        val sorted = sightings.sortedBy { it.timestamp }

        val duration = calculateDurationFactor(sorted)
        val location = calculateLocationFactor(sorted)

        val rawScore = (duration.score + location.score).coerceIn(0f, 100f)

        val rssiMod = calculateRssiModifier(sorted)
        val correlationMod = calculateCorrelationModifier(sorted)
        val decayMod = calculateDecayModifier(device)
        val staticPenalty = calculateStaticPenalty(location.distinctLocations, sorted)

        val finalScore = (rawScore * rssiMod * correlationMod * decayMod * staticPenalty)
            .roundToInt()
            .coerceIn(0, 100)

        return SuspicionResult(
            totalScore = finalScore,
            durationFactor = duration.score,
            locationFactor = location.score,
            rssiModifier = rssiMod,
            correlationModifier = correlationMod,
            decayModifier = decayMod,
            distinctLocationCount = location.distinctLocations,
            totalExposureMinutes = duration.totalExposureMinutes,
            longestStreakMinutes = duration.longestStreakMinutes,
            level = levelFromScore(finalScore)
        )
    }

    fun levelFromScore(score: Int): ThreatLevel = when {
        score >= settings.highThreshold -> ThreatLevel.HIGH
        score > settings.lowThreshold -> ThreatLevel.MEDIUM
        else -> ThreatLevel.LOW
    }

    // ---- Duration Factor (0-50 pts) ------------------------------------------------

    private data class DurationCalc(
        val score: Float,
        val totalExposureMinutes: Float,
        val longestStreakMinutes: Float
    )

    private fun calculateDurationFactor(sightings: List<DeviceSighting>): DurationCalc {
        if (sightings.size < 2) return DurationCalc(0f, 0f, 0f)

        val streakGapMs = settings.streakGapThresholdMinutes * 60_000L

        var totalExposureMs = 0L
        var currentStreakMs = 0L
        var longestStreakMs = 0L

        for (i in 1 until sightings.size) {
            val gap = sightings[i].timestamp - sightings[i - 1].timestamp
            if (gap in 1..streakGapMs) {
                totalExposureMs += gap
                currentStreakMs += gap
            } else {
                longestStreakMs = maxOf(longestStreakMs, currentStreakMs)
                currentStreakMs = 0L
            }
        }
        longestStreakMs = maxOf(longestStreakMs, currentStreakMs)

        val totalMin = totalExposureMs / 60_000f
        val streakMin = longestStreakMs / 60_000f

        // Base: linear scale 0-20 pts over 0-60 min total exposure
        val base = ((totalMin / 60f) * 20f).coerceAtMost(20f)

        // Bonus: linear scale 0-30 pts over 0-30 min longest streak
        val bonus = (streakMin).coerceAtMost(30f)

        return DurationCalc(
            score = (base + bonus).coerceAtMost(50f),
            totalExposureMinutes = totalMin,
            longestStreakMinutes = streakMin
        )
    }

    // ---- Location Factor (0-50 pts) ------------------------------------------------

    private data class LocationCalc(val score: Float, val distinctLocations: Int)

    private fun calculateLocationFactor(sightings: List<DeviceSighting>): LocationCalc {
        val clusters = clusterByLocation(sightings)
        val n = clusters.size
        val score = ((n - 1) * 10f).coerceIn(0f, 50f)
        return LocationCalc(score, n)
    }

    private fun clusterByLocation(
        sightings: List<DeviceSighting>
    ): List<SightingCluster> {
        val clusters = mutableListOf<SightingCluster>()
        val threshold = settings.locationClusterThresholdMeters

        for (s in sightings) {
            val matched = clusters.firstOrNull { c ->
                distanceMeters(s.latitude, s.longitude, c.centerLat, c.centerLon) <= threshold
            }
            if (matched != null) {
                matched.sightings.add(s)
            } else {
                clusters.add(SightingCluster(s.latitude, s.longitude, mutableListOf(s)))
            }
        }
        return clusters
    }

    // ---- Modifiers ------------------------------------------------------------------

    private fun calculateRssiModifier(sightings: List<DeviceSighting>): Float {
        if (sightings.isEmpty()) return 1f
        val avgRssi = sightings.map { it.rssi }.average()
        return if (avgRssi > settings.highRssiThreshold) 1.1f else 1f
    }

    /**
     * Awards a 20 % bonus when the device re-appears at new locations shortly after
     * the user moves there (time between last sighting in old cluster and first in
     * new cluster is within the configured window).
     */
    private fun calculateCorrelationModifier(sightings: List<DeviceSighting>): Float {
        val clusters = clusterByLocation(sightings)
        if (clusters.size < 2) return 1f

        val windowMs = settings.movementCorrelationWindowMinutes * 60_000L
        val sorted = clusters.sortedBy { c -> c.sightings.minOf { it.timestamp } }

        var correlated = 0
        var total = 0

        for (i in 1 until sorted.size) {
            val lastInPrev = sorted[i - 1].sightings.maxOf { it.timestamp }
            val firstInCurr = sorted[i].sightings.minOf { it.timestamp }
            total++
            if ((firstInCurr - lastInPrev) in 0..windowMs) {
                correlated++
            }
        }

        return if (total > 0 && correlated.toFloat() / total >= 0.5f) 1.2f else 1f
    }

    /**
     * Score decays linearly to zero over 24 hours since the device was last seen.
     */
    private fun calculateDecayModifier(device: DetectedDevice): Float {
        val hoursSince =
            (System.currentTimeMillis() - device.lastSeenTimestamp) / 3_600_000f
        return (1f - hoursSince / 24f).coerceIn(0f, 1f)
    }

    /**
     * Penalises devices that never change location while the user is moving —
     * these are likely static infrastructure (routers, beacons).
     */
    private fun calculateStaticPenalty(
        distinctLocations: Int,
        sightings: List<DeviceSighting>
    ): Float {
        if (distinctLocations > 1) return 1f
        if (sightings.size < 2) return 1f

        val first = sightings.first()
        val last = sightings.last()
        val userDisplacement = distanceMeters(
            first.latitude, first.longitude,
            last.latitude, last.longitude
        )
        // User moved >200 m but device stayed in one spot → likely static
        return if (userDisplacement > 200f) 0.3f else 1f
    }

    // ---- Helpers ---------------------------------------------------------------------

    private fun distanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val out = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, out)
        return out[0]
    }

    private fun emptySuspicion() = SuspicionResult(
        totalScore = 0,
        durationFactor = 0f,
        locationFactor = 0f,
        rssiModifier = 1f,
        correlationModifier = 1f,
        decayModifier = 1f,
        distinctLocationCount = 0,
        totalExposureMinutes = 0f,
        longestStreakMinutes = 0f,
        level = ThreatLevel.LOW
    )

    private class SightingCluster(
        val centerLat: Double,
        val centerLon: Double,
        val sightings: MutableList<DeviceSighting>
    )
}
