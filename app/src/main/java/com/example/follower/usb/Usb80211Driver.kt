package com.example.follower.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.util.Log
import com.example.follower.data.model.DeviceType
import com.example.follower.data.model.ScanResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * User-space driver for RTL8187-based USB WiFi adapters.
 *
 * This is a simplified implementation based on the liber80211 concept.
 * It puts the adapter into monitor mode and captures raw 802.11 frames.
 *
 * NOTE: This is a framework/stub that would need the actual USB protocol
 * implementation for the RTL8187 chipset to be fully functional.
 */
class Usb80211Driver(
    private val device: UsbDevice,
    private val connection: UsbDeviceConnection
) {
    companion object {
        private const val TAG = "Usb80211Driver"

        // RTL8187 USB endpoints (typical values, may vary)
        private const val BULK_IN_ENDPOINT = 0x81
        private const val BULK_OUT_ENDPOINT = 0x02

        // 802.11 Frame Types
        private const val FRAME_TYPE_MANAGEMENT = 0x00
        private const val FRAME_TYPE_CONTROL = 0x01
        private const val FRAME_TYPE_DATA = 0x02

        // Management Frame Subtypes
        private const val SUBTYPE_PROBE_REQUEST = 0x04
        private const val SUBTYPE_PROBE_RESPONSE = 0x05
        private const val SUBTYPE_BEACON = 0x08
    }

    private val _probeRequests = MutableSharedFlow<ProbeRequestFrame>(
        replay = 0,
        extraBufferCapacity = 100
    )
    val probeRequests: SharedFlow<ProbeRequestFrame> = _probeRequests.asSharedFlow()

    private var isRunning = false
    private var receiveJob: Job? = null

    private var usbInterface: UsbInterface? = null
    private var bulkInEndpoint: UsbEndpoint? = null
    private var bulkOutEndpoint: UsbEndpoint? = null

    data class ProbeRequestFrame(
        val sourceMac: String,
        val destinationMac: String,
        val ssid: String?,
        val rssi: Int,
        val channel: Int,
        val timestamp: Long = System.currentTimeMillis(),
        val rawFrame: ByteArray? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ProbeRequestFrame
            return sourceMac == other.sourceMac && timestamp == other.timestamp
        }

        override fun hashCode(): Int {
            var result = sourceMac.hashCode()
            result = 31 * result + timestamp.hashCode()
            return result
        }
    }

    /**
     * Initialize the driver and claim the USB interface.
     */
    fun initialize(): Boolean {
        Log.d(TAG, "Initializing USB 802.11 driver")

        try {
            // Find and claim the USB interface
            for (i in 0 until device.interfaceCount) {
                val iface = device.getInterface(i)

                // Look for bulk transfer interface
                for (j in 0 until iface.endpointCount) {
                    val endpoint = iface.getEndpoint(j)

                    if (endpoint.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (endpoint.direction == android.hardware.usb.UsbConstants.USB_DIR_IN) {
                            bulkInEndpoint = endpoint
                        } else {
                            bulkOutEndpoint = endpoint
                        }
                    }
                }

                if (bulkInEndpoint != null && bulkOutEndpoint != null) {
                    usbInterface = iface
                    break
                }
            }

            if (usbInterface == null) {
                Log.e(TAG, "Could not find suitable USB interface")
                return false
            }

            // Claim the interface
            if (!connection.claimInterface(usbInterface, true)) {
                Log.e(TAG, "Failed to claim USB interface")
                return false
            }

            Log.i(TAG, "USB interface claimed successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing driver", e)
            return false
        }
    }

    /**
     * Put the adapter into monitor mode.
     *
     * NOTE: This requires sending specific USB control transfers to the RTL8187
     * chipset. The actual implementation would need the complete USB protocol.
     */
    fun enableMonitorMode(): Boolean {
        Log.d(TAG, "Enabling monitor mode")

        // TODO: Implement RTL8187-specific USB control transfers
        // This would involve:
        // 1. Reset the device
        // 2. Set operational mode to monitor
        // 3. Configure channel
        // 4. Enable frame reception

        // Placeholder - in a real implementation, this would send USB control messages
        try {
            // Example USB control transfer (not actual RTL8187 protocol)
            // connection.controlTransfer(
            //     USB_TYPE_VENDOR | USB_DIR_OUT,
            //     RTL8187_CMD_SET_MONITOR_MODE,
            //     0, 0, null, 0, 1000
            // )

            Log.i(TAG, "Monitor mode enabled (simulated)")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error enabling monitor mode", e)
            return false
        }
    }

    /**
     * Set the WiFi channel to monitor.
     */
    fun setChannel(channel: Int): Boolean {
        Log.d(TAG, "Setting channel to $channel")

        // TODO: Implement channel setting via USB control transfer
        return true
    }

    /**
     * Start receiving frames.
     */
    fun startCapture(scope: CoroutineScope) {
        if (isRunning) return

        isRunning = true
        Log.i(TAG, "Starting frame capture")

        receiveJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(2048) // Max 802.11 frame size

            while (isRunning && isActive) {
                try {
                    // Read from bulk endpoint
                    val bytesRead = connection.bulkTransfer(
                        bulkInEndpoint,
                        buffer,
                        buffer.size,
                        1000 // 1 second timeout
                    )

                    if (bytesRead > 0) {
                        processFrame(buffer.copyOf(bytesRead))
                    }

                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "Error reading USB data", e)
                    }
                }
            }
        }
    }

    /**
     * Stop receiving frames.
     */
    fun stopCapture() {
        isRunning = false
        receiveJob?.cancel()
        receiveJob = null
        Log.i(TAG, "Frame capture stopped")
    }

    /**
     * Process a received 802.11 frame.
     */
    private suspend fun processFrame(data: ByteArray) {
        if (data.size < 24) return // Minimum 802.11 header size

        try {
            // Parse 802.11 MAC header
            val frameControl = ((data[1].toInt() and 0xFF) shl 8) or (data[0].toInt() and 0xFF)
            val frameType = (frameControl shr 2) and 0x03
            val frameSubtype = (frameControl shr 4) and 0x0F

            // We're interested in probe requests
            if (frameType == FRAME_TYPE_MANAGEMENT && frameSubtype == SUBTYPE_PROBE_REQUEST) {
                parseProbeRequest(data)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        }
    }

    /**
     * Parse a probe request frame.
     */
    private suspend fun parseProbeRequest(data: ByteArray) {
        try {
            // Extract addresses from 802.11 header
            // Address 1 (bytes 4-9): Destination (usually broadcast)
            // Address 2 (bytes 10-15): Source MAC
            // Address 3 (bytes 16-21): BSSID

            val sourceMac = extractMacAddress(data, 10)
            val destMac = extractMacAddress(data, 4)

            // Parse information elements to get SSID
            var ssid: String? = null
            var offset = 24 // Start of IEs after MAC header

            while (offset + 2 < data.size) {
                val elementId = data[offset].toInt() and 0xFF
                val elementLen = data[offset + 1].toInt() and 0xFF

                if (offset + 2 + elementLen > data.size) break

                if (elementId == 0 && elementLen > 0) {
                    // SSID element
                    ssid = String(data, offset + 2, elementLen, Charsets.UTF_8)
                }

                offset += 2 + elementLen
            }

            val frame = ProbeRequestFrame(
                sourceMac = sourceMac,
                destinationMac = destMac,
                ssid = ssid,
                rssi = -50, // Would need to extract from radiotap header
                channel = 0, // Would need to track current channel
                rawFrame = data
            )

            _probeRequests.emit(frame)
            Log.v(TAG, "Probe request from $sourceMac for SSID: ${ssid ?: "(broadcast)"}")

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing probe request", e)
        }
    }

    /**
     * Extract MAC address from frame data.
     */
    private fun extractMacAddress(data: ByteArray, offset: Int): String {
        return String.format(
            "%02X:%02X:%02X:%02X:%02X:%02X",
            data[offset].toInt() and 0xFF,
            data[offset + 1].toInt() and 0xFF,
            data[offset + 2].toInt() and 0xFF,
            data[offset + 3].toInt() and 0xFF,
            data[offset + 4].toInt() and 0xFF,
            data[offset + 5].toInt() and 0xFF
        )
    }

    /**
     * Convert a probe request to a ScanResult for the detection engine.
     */
    fun probeToScanResult(
        probe: ProbeRequestFrame,
        latitude: Double,
        longitude: Double,
        accuracy: Float
    ): ScanResult {
        return ScanResult(
            macAddress = probe.sourceMac,
            deviceType = DeviceType.WIFI_PROBE_REQUEST,
            deviceName = null,
            rssi = probe.rssi,
            timestamp = probe.timestamp,
            latitude = latitude,
            longitude = longitude,
            locationAccuracy = accuracy,
            ssid = probe.ssid,
            channel = probe.channel,
            isProbeRequest = true,
            probedSsids = probe.ssid?.let { listOf(it) },
            rawFrameData = probe.rawFrame
        )
    }

    /**
     * Release resources.
     */
    fun close() {
        stopCapture()

        usbInterface?.let {
            connection.releaseInterface(it)
        }

        Log.i(TAG, "Driver closed")
    }
}
