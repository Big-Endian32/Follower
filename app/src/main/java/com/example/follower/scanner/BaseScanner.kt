package com.example.follower.scanner

import com.example.follower.data.model.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Base interface for all device scanners.
 */
interface BaseScanner {

    /**
     * Flow of scan results. Subscribers receive scan results as they're detected.
     */
    val scanResults: Flow<ScanResult>

    /**
     * Whether the scanner is currently active.
     */
    val isScanning: Boolean

    /**
     * Start scanning for devices.
     */
    fun startScanning()

    /**
     * Stop scanning for devices.
     */
    fun stopScanning()

    /**
     * Check if this scanner is available on this device.
     */
    fun isAvailable(): Boolean
}

/**
 * Abstract base implementation with common functionality.
 */
abstract class AbstractScanner : BaseScanner {

    protected val _scanResults = MutableSharedFlow<ScanResult>(
        replay = 0,
        extraBufferCapacity = 100
    )

    override val scanResults: Flow<ScanResult> = _scanResults

    protected var _isScanning = false
    override val isScanning: Boolean get() = _isScanning

    protected suspend fun emitResult(result: ScanResult) {
        _scanResults.emit(result)
    }
}
