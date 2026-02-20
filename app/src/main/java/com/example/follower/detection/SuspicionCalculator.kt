package com.example.follower.detection

import android.location.Location
import com.example.follower.data.model.DetectedDevice
import com.example.follower.data.model.DeviceSighting
import com.example.follower.data.model.ThreatLevel
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class SuspicionResult(
    val totalScore: Int,
    val durationFactor: Float,
    val locationFactor: Float,
    val patternFactor: Float,
    val rssiModifier: Float,
    val correlationModifier: Float,
    val decayModifier: Float,
    val densityModifier: Float,
    val distinctLocationCount: Int,
    val totalExposureMinutes: Float,
    val longestStreakMinutes: Float,
    val level: ThreatLevel,
    val isKnownTracker: Boolean = false
)

/**
 * Calculates a 0-100 suspicion score for a device based on sighting history.
 *
 * Score breakdown:
 *   Duration factor:  0-30 pts  (total exposure time + longest continuous streak)
 *   Location factor:  0-50 pts  (distinct locations via nearest-centroid clustering)
 *   Pattern factor:   0-20 pts  (intermittent reappearance / leapfrog + route order)
 *
 * Modifiers:
 *   RSSI trajectory:       1.0-1.15x  (constant RSSI during movement = following)
 *   Movement correlation:  1.0-1.3x   (proportional to correlated transitions)
 *   Time decay:            0.0-1.0x   (linear decay over 24h since last seen)
 *   Density normalization: 0.5-1.0x   (reduces score in high-device-density areas)
 *   Static penalty:        0.3x       (device stays put while user moves)
 */
class SuspicionCalculator(private val settings: SuspicionSettings) {

    /**
     * Ambient device count at the time of scoring, set by [DetectionEngine].
     * Used for density normalization — crowded environments reduce scores.
     */
    var ambientDeviceCount: Int = 0

    fun calculate(device: DetectedDevice, sightings: List<DeviceSighting>): SuspicionResult {
        if (device.isWhitelisted || sightings.size < 2) {
            return emptySuspicion()
        }

        val sorted = sightings.sortedBy { it.timestamp }

        // Compute clusters once and reuse everywhere
        val clusters = clusterByLocation(sorted)

        val duration = calculateDurationFactor(sorted)
        val location = calculateLocationFactor(clusters)
        val pattern = calculatePatternFactor(sorted, clusters)

        val rawScore = (duration.score + location.score + pattern).coerceIn(0f, 100f)

        val rssiMod = calculateRssiTrajectoryModifier(sorted)
        val correlationMod = calculateCorrelationModifier(clusters)
        val decayMod = calculateDecayModifier(device)
        val densityMod = calculateDensityModifier()
        val staticPenalty = calculateStaticPenalty(clusters.size, sorted)

        val finalScore = (rawScore * rssiMod * correlationMod * decayMod * densityMod * staticPenalty)
            .roundToInt()
            .coerceIn(0, 100)

        return SuspicionResult(
            totalScore = finalScore,
            durationFactor = duration.score,
            locationFactor = location.score,
            patternFactor = pattern,
            rssiModifier = rssiMod,
            correlationModifier = correlationMod,
            decayModifier = decayMod,
            densityModifier = densityMod,
            distinctLocationCount = clusters.size,
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

    // ---- Duration Factor (0-30 pts) ------------------------------------------------

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

        // Base: linear scale 0-15 pts over 0-60 min total exposure
        val base = ((totalMin / 60f) * 15f).coerceAtMost(15f)

        // Streak bonus: linear scale 0-15 pts over 0-30 min longest streak
        val bonus = ((streakMin / 30f) * 15f).coerceAtMost(15f)

        return DurationCalc(
            score = (base + bonus).coerceAtMost(30f),
            totalExposureMinutes = totalMin,
            longestStreakMinutes = streakMin
        )
    }

    // ---- Location Factor (0-50 pts) ------------------------------------------------

    private data class LocationCalc(val score: Float, val distinctLocations: Int)

    private fun calculateLocationFactor(clusters: List<SightingCluster>): LocationCalc {
        val n = clusters.size
        // Each additional location beyond the first adds 10 points, up to 50
        val score = ((n - 1) * 10f).coerceIn(0f, 50f)
        return LocationCalc(score, n)
    }

    /**
     * Nearest-centroid clustering with running centroid updates.
     *
     * Each sighting is assigned to the **nearest** existing cluster if within
     * the threshold distance; otherwise a new cluster is created.  After
     * assignment the cluster centroid is updated to the mean of all its
     * members.  This fixes the old greedy first-match approach which was
     * order-dependent and could misassign borderline sightings.
     */
    private fun clusterByLocation(
        sightings: List<DeviceSighting>
    ): List<SightingCluster> {
        val clusters = mutableListOf<SightingCluster>()
        val threshold = settings.locationClusterThresholdMeters

        for (s in sightings) {
            val nearest = clusters.minByOrNull { c ->
                distanceMeters(s.latitude, s.longitude, c.centerLat, c.centerLon)
            }

            if (nearest != null &&
                distanceMeters(s.latitude, s.longitude, nearest.centerLat, nearest.centerLon) <= threshold
            ) {
                nearest.sightings.add(s)
                // Update centroid to mean of all members
                nearest.centerLat = nearest.sightings.map { it.latitude }.average()
                nearest.centerLon = nearest.sightings.map { it.longitude }.average()
            } else {
                clusters.add(SightingCluster(s.latitude, s.longitude, mutableListOf(s)))
            }
        }
        return clusters
    }

    // ---- Pattern Factor (0-20 pts) -------------------------------------------------

    /**
     * Detects intermittent reappearance ("leapfrog") patterns and route ordering.
     *
     * A sophisticated follower may lose contact and reacquire the target at
     * the next location.  Each time a device disappears for longer than the
     * streak gap and then reappears at a *different* cluster, that counts as
     * a "reacquisition event" — a strong indicator of intentional following.
     *
     * Also awards points for route-sequence matching — the device visiting
     * the same clusters in the same temporal order as the user's movement.
     */
    private fun calculatePatternFactor(
        sightings: List<DeviceSighting>,
        clusters: List<SightingCluster>
    ): Float {
        if (clusters.size < 2 || sightings.size < 3) return 0f

        val streakGapMs = settings.streakGapThresholdMinutes * 60_000L
        var reacquisitions = 0

        // Identify gaps in the sighting timeline and check if the device
        // reappears at a different location afterwards
        for (i in 1 until sightings.size) {
            val gap = sightings[i].timestamp - sightings[i - 1].timestamp
            if (gap > streakGapMs) {
                val prevCluster = findCluster(sightings[i - 1], clusters)
                val nextCluster = findCluster(sightings[i], clusters)
                if (prevCluster != null && nextCluster != null && prevCluster !== nextCluster) {
                    reacquisitions++
                }
            }
        }

        // Reacquisition score: 5 pts each, up to 10 pts
        val reacquisitionScore = (reacquisitions * 5f).coerceAtMost(10f)

        // Route-sequence score: how well does the device's cluster visit order
        // match a sequential route?  Count in-order cluster transitions.
        val clusterSequence = buildClusterSequence(sightings, clusters)
        val inOrderTransitions = countInOrderTransitions(clusterSequence)
        val totalTransitions = (clusterSequence.size - 1).coerceAtLeast(1)
        val routeOrderRatio = inOrderTransitions.toFloat() / totalTransitions

        // Route order score: up to 10 pts
        val routeScore = (routeOrderRatio * 10f).coerceAtMost(10f)

        return (reacquisitionScore + routeScore).coerceAtMost(20f)
    }

    /**
     * Build the sequence of cluster indices visited over time (deduped
     * consecutive repeats so lingering at one cluster doesn't count as
     * multiple transitions).
     */
    private fun buildClusterSequence(
        sightings: List<DeviceSighting>,
        clusters: List<SightingCluster>
    ): List<Int> {
        val sequence = mutableListOf<Int>()
        for (s in sightings) {
            val idx = clusters.indexOfFirst { c ->
                distanceMeters(s.latitude, s.longitude, c.centerLat, c.centerLon) <=
                    settings.locationClusterThresholdMeters
            }
            if (idx >= 0 && (sequence.isEmpty() || sequence.last() != idx)) {
                sequence.add(idx)
            }
        }
        return sequence
    }

    /**
     * Count transitions where the cluster index increases (i.e. the device
     * visits clusters in the order they were first encountered, matching
     * the user's route direction).
     */
    private fun countInOrderTransitions(sequence: List<Int>): Int {
        var count = 0
        for (i in 1 until sequence.size) {
            if (sequence[i] > sequence[i - 1]) count++
        }
        return count
    }

    private fun findCluster(
        sighting: DeviceSighting,
        clusters: List<SightingCluster>
    ): SightingCluster? {
        return clusters.minByOrNull { c ->
            distanceMeters(sighting.latitude, sighting.longitude, c.centerLat, c.centerLon)
        }?.takeIf { c ->
            distanceMeters(sighting.latitude, sighting.longitude, c.centerLat, c.centerLon) <=
                settings.locationClusterThresholdMeters
        }
    }

    // ---- Modifiers ------------------------------------------------------------------

    /**
     * RSSI trajectory analysis.
     *
     * A following device maintains roughly constant RSSI as it moves with
     * the user.  A stationary device's RSSI changes predictably as the user
     * walks away or toward it.  Low RSSI variance combined with significant
     * user movement is a strong following indicator.
     *
     * Returns 1.0-1.15x.
     */
    private fun calculateRssiTrajectoryModifier(sightings: List<DeviceSighting>): Float {
        if (sightings.size < 4) return 1f

        // Calculate user total path length
        var pathLength = 0f
        for (i in 1 until sightings.size) {
            pathLength += distanceMeters(
                sightings[i - 1].latitude, sightings[i - 1].longitude,
                sightings[i].latitude, sightings[i].longitude
            )
        }

        // If user hasn't moved much, RSSI stability is not meaningful
        if (pathLength < 100f) return 1f

        // Compute RSSI standard deviation
        val rssiValues = sightings.map { it.rssi.toFloat() }
        val mean = rssiValues.average().toFloat()
        val variance = rssiValues.map { (it - mean) * (it - mean) }.average().toFloat()
        val stdDev = sqrt(variance)

        // Low RSSI std dev during significant movement = following
        return when {
            stdDev < 5f -> 1.15f
            stdDev < 8f -> 1.08f
            else -> 1f
        }
    }

    /**
     * Proportional movement correlation modifier.
     *
     * Awards a bonus proportional to how many location transitions are
     * correlated (device appears at new location within a time window
     * after the user moves there).
     *
     * Returns 1.0-1.3x (proportional, not binary).
     */
    private fun calculateCorrelationModifier(clusters: List<SightingCluster>): Float {
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

        if (total == 0) return 1f

        val correlationRatio = correlated.toFloat() / total
        // Scale proportionally: 0% → 1.0x, 100% → 1.3x
        return 1f + (correlationRatio * 0.3f)
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
     * Density normalization — reduces scores in high-device-density areas.
     *
     * In a crowded subway car, dozens of BLE devices travel the same route.
     * On an empty suburban road, even one device consistently nearby is
     * suspicious.  Logarithmic dampening prevents over-suppression.
     */
    private fun calculateDensityModifier(): Float {
        if (ambientDeviceCount <= 5) return 1f

        val dampening = 1f / (ln((ambientDeviceCount + 1).toFloat()) / ln(2f))
        return dampening.coerceIn(0.5f, 1f)
    }

    /**
     * Penalises devices that never change location while the user is moving.
     *
     * Uses **max displacement** from the first sighting at any point during
     * the observation window, not just first-vs-last (which fails for round
     * trips where the user returns to the starting location).
     */
    private fun calculateStaticPenalty(
        distinctLocations: Int,
        sightings: List<DeviceSighting>
    ): Float {
        if (distinctLocations > 1) return 1f
        if (sightings.size < 2) return 1f

        val first = sightings.first()

        val maxDisplacement = sightings.maxOf { s ->
            distanceMeters(first.latitude, first.longitude, s.latitude, s.longitude)
        }

        // User moved >200m at some point but device stayed in one cluster → likely static
        return if (maxDisplacement > 200f) 0.3f else 1f
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
        patternFactor = 0f,
        rssiModifier = 1f,
        correlationModifier = 1f,
        decayModifier = 1f,
        densityModifier = 1f,
        distinctLocationCount = 0,
        totalExposureMinutes = 0f,
        longestStreakMinutes = 0f,
        level = ThreatLevel.LOW
    )

    class SightingCluster(
        var centerLat: Double,
        var centerLon: Double,
        val sightings: MutableList<DeviceSighting>
    )
}
