package com.example.follower.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a cluster of locations (100m radius by default).
 * Used for spatial correlation in tracking detection.
 */
@Entity(tableName = "location_clusters")
data class LocationCluster(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Center of the cluster
    val centerLatitude: Double,
    val centerLongitude: Double,

    // Cluster radius in meters
    val radiusMeters: Float = 100f,

    // Time range this cluster covers
    val firstVisitTimestamp: Long,
    val lastVisitTimestamp: Long,

    // Number of times user visited this cluster
    val visitCount: Int = 1,

    // Human-readable name (if geocoded)
    val placeName: String? = null
)
