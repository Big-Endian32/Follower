package com.example.follower.detection

import android.util.Log
import com.example.follower.data.model.DeviceType
import com.example.follower.data.model.ScanResult
import java.security.MessageDigest

/**
 * Correlates BLE devices across MAC address randomization by building
 * stable fingerprints from advertising data that persists across rotations.
 *
 * Modern BLE devices (Android 8+, iOS 14+, tracking devices) rotate their
 * MAC address every ~15 minutes.  Without this resolver, each rotated
 * address is treated as a brand-new device and never accumulates enough
 * history to trigger an alert.
 *
 * Fingerprint signals (in priority order):
 *  1. Manufacturer-specific data (company ID + payload prefix)
 *  2. Advertised service UUIDs
 *  3. TX power level + advertising interval pattern
 *  4. RSSI continuity (a new MAC appearing at the same signal strength
 *     immediately after an old MAC disappears)
 */
class DeviceIdentityResolver {

    companion object {
        private const val TAG = "DeviceIdentityResolver"

        /** Window within which a disappearing MAC can be linked to a new one via RSSI. */
        private const val RSSI_CONTINUITY_WINDOW_MS = 30_000L

        /** Maximum RSSI difference to consider two observations "continuous". */
        private const val RSSI_CONTINUITY_THRESHOLD_DBM = 8

        /** How long a fingerprint-to-stableId mapping stays cached. */
        private const val CACHE_TTL_MS = 4 * 60 * 60 * 1000L // 4 hours
    }

    /**
     * Lightweight advertising fingerprint derived from data that does NOT
     * change when the MAC rotates.
     */
    data class AdvertisingFingerprint(
        val manufacturerCompanyId: Int?,
        val manufacturerPayloadPrefix: ByteArray?,
        val serviceUuids: List<String>,
        val txPowerLevel: Int?,
        val deviceType: DeviceType
    ) {
        /**
         * Produce a deterministic hex key from the fingerprint fields.
         * Two observations with the same key are assumed to be the same
         * physical device.
         */
        fun toKey(): String? {
            // Need at least manufacturer data or service UUIDs to fingerprint
            if (manufacturerCompanyId == null && serviceUuids.isEmpty()) return null

            val sb = StringBuilder()
            sb.append(deviceType.name).append('|')
            manufacturerCompanyId?.let { sb.append("mfr:").append(it).append('|') }
            manufacturerPayloadPrefix?.let { bytes ->
                sb.append("pfx:")
                bytes.forEach { sb.append(String.format("%02x", it)) }
                sb.append('|')
            }
            if (serviceUuids.isNotEmpty()) {
                sb.append("svc:").append(serviceUuids.sorted().joinToString(",")).append('|')
            }
            txPowerLevel?.let { sb.append("tx:").append(it) }

            // Hash for a fixed-length key
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(sb.toString().toByteArray())
            return hash.take(16).joinToString("") { String.format("%02x", it) }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AdvertisingFingerprint) return false
            return toKey() == other.toKey()
        }

        override fun hashCode(): Int = toKey()?.hashCode() ?: 0
    }

    /** Maps a fingerprint key → stable synthetic device ID. */
    private val fingerprintToStableId = LinkedHashMap<String, String>()

    /** Maps an observed MAC → stable device ID (for quick lookups). */
    private val macToStableId = LinkedHashMap<String, String>()

    /** Tracks the last observation per MAC for RSSI-continuity matching. */
    private data class LastObservation(
        val rssi: Int,
        val timestamp: Long,
        val stableId: String
    )
    private val lastObservations = LinkedHashMap<String, LastObservation>()

    /** Tracks recently disappeared MACs for RSSI-continuity matching. */
    private data class DisappearedMac(
        val lastRssi: Int,
        val lastTimestamp: Long,
        val stableId: String
    )
    private val recentlyDisappeared = LinkedHashMap<String, DisappearedMac>()

    private var nextSyntheticId = 1L

    /**
     * Resolve a scan result to a stable device identity.
     *
     * @return The stable device ID to use as the primary key instead of
     *         the raw MAC address.  Returns the original MAC for non-BLE
     *         devices or when no fingerprint is available.
     */
    fun resolveIdentity(result: ScanResult): String {
        // Only apply to BLE devices — Classic BT and WiFi MACs are stable
        if (result.deviceType != DeviceType.BLUETOOTH_LE) {
            return result.macAddress
        }

        // 1. Check if we already mapped this MAC
        macToStableId[result.macAddress]?.let { stableId ->
            updateObservation(result.macAddress, result.rssi, result.timestamp, stableId)
            return stableId
        }

        // 2. Try fingerprint-based matching
        val fingerprint = buildFingerprint(result)
        val fpKey = fingerprint.toKey()

        if (fpKey != null) {
            val stableId = fingerprintToStableId.getOrPut(fpKey) {
                generateStableId()
            }
            macToStableId[result.macAddress] = stableId
            updateObservation(result.macAddress, result.rssi, result.timestamp, stableId)
            Log.d(TAG, "Fingerprint match: ${result.macAddress} → $stableId (key=$fpKey)")
            return stableId
        }

        // 3. Try RSSI-continuity matching (new MAC appears with similar RSSI
        //    shortly after an old MAC disappears)
        val rssiMatch = findRssiContinuityMatch(result)
        if (rssiMatch != null) {
            macToStableId[result.macAddress] = rssiMatch
            updateObservation(result.macAddress, result.rssi, result.timestamp, rssiMatch)
            Log.d(TAG, "RSSI-continuity match: ${result.macAddress} → $rssiMatch")
            return rssiMatch
        }

        // 4. No match — create a new stable ID for this MAC
        val newId = generateStableId()
        macToStableId[result.macAddress] = newId
        updateObservation(result.macAddress, result.rssi, result.timestamp, newId)
        return newId
    }

    /**
     * Notify the resolver that a MAC has not been seen for a while,
     * so it can be moved to the "recently disappeared" pool for
     * RSSI-continuity matching.
     */
    fun markDisappeared(macAddress: String) {
        val obs = lastObservations.remove(macAddress) ?: return
        recentlyDisappeared[macAddress] = DisappearedMac(
            lastRssi = obs.rssi,
            lastTimestamp = obs.timestamp,
            stableId = obs.stableId
        )
    }

    /**
     * Get the stable ID previously assigned to a MAC, if any.
     */
    fun getStableId(macAddress: String): String? = macToStableId[macAddress]

    /**
     * Periodic cleanup of stale entries.
     */
    fun cleanup() {
        val now = System.currentTimeMillis()
        val cutoff = now - CACHE_TTL_MS

        lastObservations.entries.removeAll { it.value.timestamp < cutoff }
        recentlyDisappeared.entries.removeAll { it.value.lastTimestamp < cutoff }

        // Trim caches if they grow too large
        if (macToStableId.size > 5000) {
            val toRemove = macToStableId.keys.take(macToStableId.size - 3000)
            toRemove.forEach { macToStableId.remove(it) }
        }
        if (fingerprintToStableId.size > 2000) {
            val toRemove = fingerprintToStableId.keys.take(fingerprintToStableId.size - 1000)
            toRemove.forEach { fingerprintToStableId.remove(it) }
        }
    }

    // ---- Internals ------------------------------------------------------------------

    private fun buildFingerprint(result: ScanResult): AdvertisingFingerprint {
        var companyId: Int? = null
        var payloadPrefix: ByteArray? = null

        result.manufacturerData?.let { data ->
            if (data.size >= 2) {
                // First 2 bytes are company ID (little-endian)
                companyId = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)
                // Take up to 6 bytes of payload as prefix (enough to identify device type)
                if (data.size > 2) {
                    payloadPrefix = data.copyOfRange(2, minOf(data.size, 8))
                }
            }
        }

        return AdvertisingFingerprint(
            manufacturerCompanyId = companyId,
            manufacturerPayloadPrefix = payloadPrefix,
            serviceUuids = result.serviceUuids ?: emptyList(),
            txPowerLevel = result.txPowerLevel,
            deviceType = result.deviceType
        )
    }

    private fun findRssiContinuityMatch(result: ScanResult): String? {
        val now = result.timestamp
        val candidates = recentlyDisappeared.entries.filter { (_, disappeared) ->
            val age = now - disappeared.lastTimestamp
            val rssiDiff = kotlin.math.abs(result.rssi - disappeared.lastRssi)
            age in 0..RSSI_CONTINUITY_WINDOW_MS && rssiDiff <= RSSI_CONTINUITY_THRESHOLD_DBM
        }

        // Pick the closest RSSI match
        return candidates
            .minByOrNull { kotlin.math.abs(result.rssi - it.value.lastRssi) }
            ?.value?.stableId
    }

    private fun updateObservation(mac: String, rssi: Int, timestamp: Long, stableId: String) {
        lastObservations[mac] = LastObservation(rssi, timestamp, stableId)
    }

    private fun generateStableId(): String {
        return "ble_${nextSyntheticId++}"
    }
}
