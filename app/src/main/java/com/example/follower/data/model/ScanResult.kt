package com.example.follower.data.model

/**
 * Represents a raw scan result from any scanner (Bluetooth, WiFi, USB adapter).
 * This is a transient object used to pass data from scanners to the detection engine.
 */
data class ScanResult(
    val macAddress: String,
    val deviceType: DeviceType,
    val deviceName: String? = null,
    val rssi: Int,
    val timestamp: Long = System.currentTimeMillis(),

    // Location at time of scan
    val latitude: Double,
    val longitude: Double,
    val locationAccuracy: Float,

    // WiFi-specific
    val ssid: String? = null,
    val channel: Int? = null,
    val frequency: Int? = null,

    // Bluetooth-specific
    val bluetoothClass: Int? = null,
    val bondState: Int? = null,

    // Probe request specific (Tier 2)
    val isProbeRequest: Boolean = false,
    val probedSsids: List<String>? = null,

    // Raw frame data (Tier 2)
    val rawFrameData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanResult

        if (macAddress != other.macAddress) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = macAddress.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
