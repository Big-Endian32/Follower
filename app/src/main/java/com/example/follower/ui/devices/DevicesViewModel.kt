package com.example.follower.ui.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.follower.FollowerApplication
import com.example.follower.data.model.DetectedDevice
import com.example.follower.data.repository.DeviceRepository
import com.example.follower.detection.SuspicionSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class DevicesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeviceRepository =
        (application as FollowerApplication).repository

    private val settings: SuspicionSettings =
        (application as FollowerApplication).suspicionSettings

    companion object {
        private const val NEARBY_WINDOW_MS = 2 * 60 * 1000L
        private const val SUSPICIOUS_WINDOW_MS = 30 * 60 * 1000L
        private const val REFRESH_INTERVAL_MS = 5_000L
    }

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(REFRESH_INTERVAL_MS)
        }
    }

    val nearbyDevices: StateFlow<List<DetectedDevice>> = ticker
        .flatMapLatest { now ->
            repository.getNearbyDevices(now - NEARBY_WINDOW_MS)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suspiciousDevices: StateFlow<List<DetectedDevice>> = ticker
        .flatMapLatest { now ->
            repository.getSuspiciousDevicesDetailed(
                settings.lowThreshold.toFloat() + 1f,
                now - SUSPICIOUS_WINDOW_MS
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class DevicesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevicesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
