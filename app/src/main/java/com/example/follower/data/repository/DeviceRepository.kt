package com.example.follower.data.repository

import com.example.follower.data.database.DeviceDao
import com.example.follower.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing device data.
 * Abstracts data operations from the rest of the app.
 */
class DeviceRepository(private val deviceDao: DeviceDao) {

    // ==================== Device Operations ====================

    val allDevices: Flow<List<DetectedDevice>> = deviceDao.getAllDevicesFlow()

    fun getSuspiciousDevices(minScore: Float = 0.25f): Flow<List<DetectedDevice>> =
        deviceDao.getSuspiciousDevicesFlow(minScore)

    suspend fun getDevice(macAddress: String): DetectedDevice? =
        deviceDao.getDeviceByMac(macAddress)

    suspend fun getRecentDevices(limit: Int = 100): List<DetectedDevice> =
        deviceDao.getRecentDevices(limit)

    suspend fun getThreateningDevices(threshold: Float = 0.5f): List<DetectedDevice> =
        deviceDao.getThreateningDevices(threshold)

    suspend fun getDevicesSeenSince(since: Long): List<DetectedDevice> =
        deviceDao.getDevicesSeenSince(since)

    fun getNearbyDevices(since: Long): Flow<List<DetectedDevice>> =
        deviceDao.getNearbyDevicesFlow(since)

    fun getSuspiciousDevicesDetailed(minScore: Float, since: Long): Flow<List<DetectedDevice>> =
        deviceDao.getSuspiciousDevicesDetailedFlow(minScore, since)

    suspend fun insertOrUpdateDevice(device: DetectedDevice) {
        deviceDao.insertDevice(device)
    }

    suspend fun updateDevice(device: DetectedDevice) {
        deviceDao.updateDevice(device)
    }

    suspend fun whitelistDevice(macAddress: String, whitelist: Boolean = true) {
        deviceDao.setWhitelisted(macAddress, whitelist)
    }

    suspend fun flagDevice(macAddress: String, flag: Boolean = true) {
        deviceDao.setFlagged(macAddress, flag)
    }

    suspend fun deleteOldDevices(olderThanMs: Long): Int {
        val cutoff = System.currentTimeMillis() - olderThanMs
        return deviceDao.deleteOldDevices(cutoff)
    }

    // ==================== Sighting Operations ====================

    suspend fun recordSighting(sighting: DeviceSighting): Long =
        deviceDao.insertSighting(sighting)

    suspend fun getSightingsForDevice(macAddress: String): List<DeviceSighting> =
        deviceDao.getSightingsForDevice(macAddress)

    suspend fun getRecentSightingsForDevice(macAddress: String, windowMs: Long): List<DeviceSighting> {
        val since = System.currentTimeMillis() - windowMs
        return deviceDao.getRecentSightingsForDevice(macAddress, since)
    }

    suspend fun getAllSightingsSince(since: Long): List<DeviceSighting> =
        deviceDao.getAllSightingsSince(since)

    suspend fun getDistinctLocationCount(macAddress: String): Int =
        deviceDao.getDistinctLocationCountForDevice(macAddress)

    suspend fun deleteOldSightings(olderThanMs: Long): Int {
        val cutoff = System.currentTimeMillis() - olderThanMs
        return deviceDao.deleteOldSightings(cutoff)
    }

    // ==================== Cluster Operations ====================

    suspend fun findOrCreateCluster(latitude: Double, longitude: Double): LocationCluster {
        val existing = deviceDao.findNearbyCluster(latitude, longitude)
        if (existing != null) {
            // Update visit count
            val updated = existing.copy(
                lastVisitTimestamp = System.currentTimeMillis(),
                visitCount = existing.visitCount + 1
            )
            deviceDao.insertCluster(updated)
            return updated
        }

        // Create new cluster
        val newCluster = LocationCluster(
            centerLatitude = latitude,
            centerLongitude = longitude,
            firstVisitTimestamp = System.currentTimeMillis(),
            lastVisitTimestamp = System.currentTimeMillis()
        )
        val id = deviceDao.insertCluster(newCluster)
        return newCluster.copy(id = id)
    }

    suspend fun getAllClusters(): List<LocationCluster> =
        deviceDao.getAllClusters()

    // ==================== Alert Operations ====================

    val allAlerts: Flow<List<ThreatAlert>> = deviceDao.getAllAlertsFlow()

    val unacknowledgedAlerts: Flow<List<ThreatAlert>> = deviceDao.getUnacknowledgedAlertsFlow()

    suspend fun createAlert(alert: ThreatAlert): Long =
        deviceDao.insertAlert(alert)

    suspend fun getLatestAlertForDevice(macAddress: String): ThreatAlert? =
        deviceDao.getLatestAlertForDevice(macAddress)

    suspend fun acknowledgeAlert(alertId: Long) =
        deviceDao.acknowledgeAlert(alertId)

    suspend fun setAlertAction(alertId: Long, action: AlertAction) =
        deviceDao.setAlertAction(alertId, action)

    // ==================== Statistics ====================

    suspend fun getTotalDeviceCount(): Int = deviceDao.getTotalDeviceCount()

    suspend fun getSuspiciousDeviceCount(): Int = deviceDao.getSuspiciousDeviceCount()

    suspend fun getSightingCountSince(since: Long): Int = deviceDao.getSightingCountSince(since)

    suspend fun getUnacknowledgedAlertCount(): Int = deviceDao.getUnacknowledgedAlertCount()

    suspend fun getUniqueDeviceCountSince(since: Long): Int =
        deviceDao.getUniqueDeviceCountSince(since)
}
