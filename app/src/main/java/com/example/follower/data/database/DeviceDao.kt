package com.example.follower.data.database

import androidx.room.*
import com.example.follower.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    // ==================== DetectedDevice Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DetectedDevice)

    @Update
    suspend fun updateDevice(device: DetectedDevice)

    @Delete
    suspend fun deleteDevice(device: DetectedDevice)

    @Query("SELECT * FROM detected_devices WHERE macAddress = :macAddress")
    suspend fun getDeviceByMac(macAddress: String): DetectedDevice?

    @Query("SELECT * FROM detected_devices ORDER BY lastSeenTimestamp DESC")
    fun getAllDevicesFlow(): Flow<List<DetectedDevice>>

    @Query("SELECT * FROM detected_devices ORDER BY lastSeenTimestamp DESC LIMIT :limit")
    suspend fun getRecentDevices(limit: Int): List<DetectedDevice>

    @Query("SELECT * FROM detected_devices WHERE threatScore >= :minScore ORDER BY threatScore DESC")
    fun getSuspiciousDevicesFlow(minScore: Float): Flow<List<DetectedDevice>>

    @Query("SELECT * FROM detected_devices WHERE isWhitelisted = 0 AND threatScore >= :threshold ORDER BY threatScore DESC")
    suspend fun getThreateningDevices(threshold: Float): List<DetectedDevice>

    @Query("SELECT * FROM detected_devices WHERE lastSeenTimestamp >= :since")
    suspend fun getDevicesSeenSince(since: Long): List<DetectedDevice>

    @Query("SELECT * FROM detected_devices WHERE lastSeenTimestamp >= :since ORDER BY lastSeenTimestamp DESC")
    fun getNearbyDevicesFlow(since: Long): Flow<List<DetectedDevice>>

    @Query("SELECT * FROM detected_devices WHERE threatScore >= :minScore AND lastSeenTimestamp >= :since ORDER BY threatScore DESC")
    fun getSuspiciousDevicesDetailedFlow(minScore: Float, since: Long): Flow<List<DetectedDevice>>

    @Query("UPDATE detected_devices SET isWhitelisted = :whitelisted WHERE macAddress = :macAddress")
    suspend fun setWhitelisted(macAddress: String, whitelisted: Boolean)

    @Query("UPDATE detected_devices SET isFlagged = :flagged WHERE macAddress = :macAddress")
    suspend fun setFlagged(macAddress: String, flagged: Boolean)

    @Query("DELETE FROM detected_devices WHERE lastSeenTimestamp < :before AND isWhitelisted = 0 AND isFlagged = 0")
    suspend fun deleteOldDevices(before: Long): Int

    // ==================== DeviceSighting Operations ====================

    @Insert
    suspend fun insertSighting(sighting: DeviceSighting): Long

    @Query("SELECT * FROM device_sightings WHERE deviceMacAddress = :macAddress ORDER BY timestamp DESC")
    suspend fun getSightingsForDevice(macAddress: String): List<DeviceSighting>

    @Query("SELECT * FROM device_sightings WHERE deviceMacAddress = :macAddress AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecentSightingsForDevice(macAddress: String, since: Long): List<DeviceSighting>

    @Query("SELECT * FROM device_sightings WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getAllSightingsSince(since: Long): List<DeviceSighting>

    @Query("""
        SELECT COUNT(DISTINCT
            CAST((latitude * 10000) AS INTEGER) || '_' || CAST((longitude * 10000) AS INTEGER)
        ) FROM device_sightings
        WHERE deviceMacAddress = :macAddress
    """)
    suspend fun getDistinctLocationCountForDevice(macAddress: String): Int

    @Query("DELETE FROM device_sightings WHERE timestamp < :before")
    suspend fun deleteOldSightings(before: Long): Int

    // ==================== LocationCluster Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCluster(cluster: LocationCluster): Long

    @Query("SELECT * FROM location_clusters ORDER BY lastVisitTimestamp DESC")
    suspend fun getAllClusters(): List<LocationCluster>

    @Query("""
        SELECT * FROM location_clusters
        WHERE ABS(centerLatitude - :lat) < 0.001
        AND ABS(centerLongitude - :lon) < 0.001
        LIMIT 1
    """)
    suspend fun findNearbyCluster(lat: Double, lon: Double): LocationCluster?

    // ==================== ThreatAlert Operations ====================

    @Insert
    suspend fun insertAlert(alert: ThreatAlert): Long

    @Update
    suspend fun updateAlert(alert: ThreatAlert)

    @Query("SELECT * FROM threat_alerts ORDER BY timestamp DESC")
    fun getAllAlertsFlow(): Flow<List<ThreatAlert>>

    @Query("SELECT * FROM threat_alerts WHERE isAcknowledged = 0 ORDER BY timestamp DESC")
    fun getUnacknowledgedAlertsFlow(): Flow<List<ThreatAlert>>

    @Query("SELECT * FROM threat_alerts WHERE deviceMacAddress = :macAddress ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAlertForDevice(macAddress: String): ThreatAlert?

    @Query("UPDATE threat_alerts SET isAcknowledged = 1 WHERE id = :alertId")
    suspend fun acknowledgeAlert(alertId: Long)

    @Query("UPDATE threat_alerts SET userAction = :action WHERE id = :alertId")
    suspend fun setAlertAction(alertId: Long, action: AlertAction)

    // ==================== Statistics ====================

    @Query("SELECT COUNT(*) FROM detected_devices")
    suspend fun getTotalDeviceCount(): Int

    @Query("SELECT COUNT(*) FROM detected_devices WHERE threatScore >= 31")
    suspend fun getSuspiciousDeviceCount(): Int

    @Query("SELECT COUNT(*) FROM device_sightings WHERE timestamp >= :since")
    suspend fun getSightingCountSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM threat_alerts WHERE isAcknowledged = 0")
    suspend fun getUnacknowledgedAlertCount(): Int
}
