package com.example.follower.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a detected wireless device (Bluetooth, WiFi AP, or WiFi Probe).
 * This is the core entity for tracking potential followers.
 */
@Entity(
    tableName = "detected_devices",
    indices = [
        Index(value = ["macAddress"]),
        Index(value = ["deviceType"]),
        Index(value = ["lastSeenTimestamp"])
    ]
)
data class DetectedDevice(
    @PrimaryKey
    val macAddress: String,

    val deviceType: DeviceType,

    val deviceName: String? = null,

    // First and last seen timestamps
    val firstSeenTimestamp: Long,
    val lastSeenTimestamp: Long,

    // Total number of times this device was detected
    val detectionCount: Int = 1,

    // Number of distinct locations where device was seen
    val locationCount: Int = 1,

    // Current threat score (0.0 - 1.0)
    val threatScore: Float = 0f,

    // Signal strength at last detection (dBm)
    val lastRssi: Int = 0,

    // WiFi-specific: SSIDs this device probed for (JSON array for probes)
    val probedSsids: String? = null,

    // Bluetooth-specific: device class/type info
    val bluetoothClass: Int? = null,

    // Whether user has marked this as safe/known
    val isWhitelisted: Boolean = false,

    // Whether user has marked this as suspicious
    val isFlagged: Boolean = false,

    // Additional metadata (JSON)
    val metadata: String? = null
)

enum class DeviceType {
    BLUETOOTH_CLASSIC,
    BLUETOOTH_LE,
    WIFI_ACCESS_POINT,
    WIFI_PROBE_REQUEST
}
