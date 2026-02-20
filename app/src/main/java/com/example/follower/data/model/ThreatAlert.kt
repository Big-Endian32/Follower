package com.example.follower.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an alert generated when a device exceeds the threat threshold.
 */
@Entity(tableName = "threat_alerts")
data class ThreatAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val deviceMacAddress: String,

    val deviceName: String?,

    val deviceType: DeviceType,

    val timestamp: Long,

    val threatScore: Float,

    val threatLevel: ThreatLevel,

    // Location when alert was generated
    val latitude: Double,
    val longitude: Double,

    // Number of times device was seen before alert
    val sightingCount: Int,

    // Number of distinct locations
    val locationCount: Int,

    // Duration device has been following (ms)
    val followDurationMs: Long,

    // Whether user has acknowledged this alert
    val isAcknowledged: Boolean = false,

    // User's action on this alert
    val userAction: AlertAction = AlertAction.NONE
)

enum class ThreatLevel {
    LOW,      // 0.25 - 0.50
    MEDIUM,   // 0.50 - 0.75
    HIGH      // 0.75 - 1.00
}

enum class AlertAction {
    NONE,
    DISMISSED,
    WHITELISTED,
    FLAGGED,
    REPORTED
}
