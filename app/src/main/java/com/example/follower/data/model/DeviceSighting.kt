package com.example.follower.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single sighting/detection of a device at a specific time and location.
 * Multiple sightings build up the history used for tracking detection.
 */
@Entity(
    tableName = "device_sightings",
    foreignKeys = [
        ForeignKey(
            entity = DetectedDevice::class,
            parentColumns = ["macAddress"],
            childColumns = ["deviceMacAddress"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deviceMacAddress"]),
        Index(value = ["timestamp"]),
        Index(value = ["latitude", "longitude"])
    ]
)
data class DeviceSighting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val deviceMacAddress: String,

    val timestamp: Long,

    // Location at time of sighting
    val latitude: Double,
    val longitude: Double,
    val locationAccuracy: Float,

    // Signal strength
    val rssi: Int,

    // Device type at time of sighting
    val deviceType: DeviceType,

    // For WiFi probes: the SSID being probed
    val probedSsid: String? = null,

    // For WiFi APs: the SSID of the access point
    val apSsid: String? = null,

    // Channel/frequency info
    val channel: Int? = null,
    val frequency: Int? = null
)
