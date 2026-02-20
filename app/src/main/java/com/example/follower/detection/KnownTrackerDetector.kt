package com.example.follower.detection

import android.util.Log
import com.example.follower.data.model.ScanResult

/**
 * Identifies known commercial tracking devices (AirTags, Tiles, SmartTags, etc.)
 * by matching BLE advertising data against known signatures.
 *
 * When a match is found the device should be immediately elevated to HIGH threat
 * and bypass the gradual scoring pipeline — these devices have no legitimate reason
 * to follow someone who doesn't own them.
 */
class KnownTrackerDetector {

    companion object {
        private const val TAG = "KnownTrackerDetector"

        // Bluetooth SIG company IDs
        const val APPLE_COMPANY_ID = 0x004C
        const val TILE_COMPANY_ID = 0x03E0
        const val SAMSUNG_COMPANY_ID = 0x0075
        const val CHIPOLO_COMPANY_ID = 0x02FF

        // Apple Find My accessory type bytes
        const val AIRTAG_TYPE_BYTE: Byte = 0x12
        const val FINDMY_ACCESSORY_TYPE: Byte = 0x07

        // Tile service UUID prefix
        const val TILE_SERVICE_UUID = "0000feed"

        // Samsung SmartTag service UUID prefix
        const val SAMSUNG_SMARTTAG_UUID = "0000fd5a"
    }

    /**
     * Result of a tracker signature match.
     */
    data class TrackerMatch(
        val trackerType: TrackerType,
        val confidence: Float,  // 0.0 – 1.0
        val description: String
    )

    enum class TrackerType {
        APPLE_AIRTAG,
        APPLE_FINDMY_ACCESSORY,
        TILE_TRACKER,
        SAMSUNG_SMARTTAG,
        CHIPOLO_TRACKER,
        GENERIC_FINDMY_NETWORK,
        UNKNOWN
    }

    /**
     * Check whether a scan result matches a known tracking device signature.
     *
     * @return A [TrackerMatch] if the device matches, or null otherwise.
     */
    fun detect(result: ScanResult): TrackerMatch? {
        // Check manufacturer data first (most reliable)
        result.manufacturerData?.let { data ->
            val match = matchManufacturerData(data)
            if (match != null) {
                Log.i(TAG, "Known tracker detected: ${match.trackerType} " +
                    "(${result.macAddress}, confidence=${match.confidence})")
                return match
            }
        }

        // Check service UUIDs
        result.serviceUuids?.let { uuids ->
            val match = matchServiceUuids(uuids)
            if (match != null) {
                Log.i(TAG, "Known tracker by UUID: ${match.trackerType} " +
                    "(${result.macAddress}, confidence=${match.confidence})")
                return match
            }
        }

        return null
    }

    private fun matchManufacturerData(data: ByteArray): TrackerMatch? {
        if (data.size < 2) return null

        val companyId = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)

        return when (companyId) {
            APPLE_COMPANY_ID -> matchAppleDevice(data)
            TILE_COMPANY_ID -> TrackerMatch(
                trackerType = TrackerType.TILE_TRACKER,
                confidence = 0.9f,
                description = "Tile Bluetooth tracker"
            )
            SAMSUNG_COMPANY_ID -> matchSamsungDevice(data)
            CHIPOLO_COMPANY_ID -> TrackerMatch(
                trackerType = TrackerType.CHIPOLO_TRACKER,
                confidence = 0.85f,
                description = "Chipolo Bluetooth tracker"
            )
            else -> null
        }
    }

    private fun matchAppleDevice(data: ByteArray): TrackerMatch? {
        if (data.size < 3) return null

        val typeByte = data[2]

        return when (typeByte) {
            AIRTAG_TYPE_BYTE -> TrackerMatch(
                trackerType = TrackerType.APPLE_AIRTAG,
                confidence = 0.95f,
                description = "Apple AirTag"
            )
            FINDMY_ACCESSORY_TYPE -> TrackerMatch(
                trackerType = TrackerType.APPLE_FINDMY_ACCESSORY,
                confidence = 0.85f,
                description = "Apple Find My network accessory"
            )
            else -> {
                // Some Apple Find My devices use other type bytes but still
                // have a recognisable payload structure
                if (data.size >= 5 && isLikelyFindMyPayload(data)) {
                    TrackerMatch(
                        trackerType = TrackerType.GENERIC_FINDMY_NETWORK,
                        confidence = 0.6f,
                        description = "Possible Apple Find My network device"
                    )
                } else null
            }
        }
    }

    private fun matchSamsungDevice(data: ByteArray): TrackerMatch? {
        // Samsung SmartTags advertise with Samsung company ID and specific payload
        if (data.size < 4) return null

        // SmartTag payloads typically have a specific header pattern
        val possibleSmartTag = data.size >= 6 &&
            (data[2].toInt() and 0xFF) == 0x01

        return if (possibleSmartTag) {
            TrackerMatch(
                trackerType = TrackerType.SAMSUNG_SMARTTAG,
                confidence = 0.8f,
                description = "Samsung SmartTag"
            )
        } else null
    }

    private fun matchServiceUuids(uuids: List<String>): TrackerMatch? {
        for (uuid in uuids) {
            val lower = uuid.lowercase()

            if (lower.startsWith(TILE_SERVICE_UUID)) {
                return TrackerMatch(
                    trackerType = TrackerType.TILE_TRACKER,
                    confidence = 0.9f,
                    description = "Tile tracker (service UUID)"
                )
            }

            if (lower.startsWith(SAMSUNG_SMARTTAG_UUID)) {
                return TrackerMatch(
                    trackerType = TrackerType.SAMSUNG_SMARTTAG,
                    confidence = 0.85f,
                    description = "Samsung SmartTag (service UUID)"
                )
            }
        }

        return null
    }

    /**
     * Heuristic check for Apple Find My network payload structure.
     * Find My accessories share a common advertising format with
     * a rotating public key in the payload.
     */
    private fun isLikelyFindMyPayload(data: ByteArray): Boolean {
        // Find My payloads are typically 29+ bytes with specific structure
        // Byte 3 typically encodes status, bytes 4+ contain the rotating key
        return data.size >= 29 &&
            (data[3].toInt() and 0x10) != 0  // Status bit indicating separated state
    }
}
