package com.example.follower.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.follower.FollowerApplication
import com.example.follower.data.model.DetectedDevice
import com.example.follower.data.model.ThreatAlert
import com.example.follower.data.repository.DeviceRepository
import com.example.follower.detection.DetectionEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeviceRepository =
        (application as FollowerApplication).repository

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentTier = MutableStateFlow(DetectionEngine.Tier.STANDARD)
    val currentTier: StateFlow<DetectionEngine.Tier> = _currentTier.asStateFlow()

    // Device counts
    val deviceCount: StateFlow<Int> = repository.allDevices
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Alert counts
    val alertCount: StateFlow<Int> = repository.unacknowledgedAlerts
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Suspicious devices (threat score > low threshold on 0-100 scale)
    val suspiciousDevices: StateFlow<List<DetectedDevice>> = repository
        .getSuspiciousDevices(31f)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All alerts
    val alerts: StateFlow<List<ThreatAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent devices
    private val _recentDevices = MutableStateFlow<List<DetectedDevice>>(emptyList())
    val recentDevices: StateFlow<List<DetectedDevice>> = _recentDevices.asStateFlow()

    init {
        refreshRecentDevices()
    }

    fun updateScanningState(isScanning: Boolean) {
        _isScanning.value = isScanning
    }

    fun updateTier(tier: DetectionEngine.Tier) {
        _currentTier.value = tier
    }

    fun refreshRecentDevices() {
        viewModelScope.launch {
            _recentDevices.value = repository.getRecentDevices(50)
        }
    }

    fun whitelistDevice(macAddress: String) {
        viewModelScope.launch {
            repository.whitelistDevice(macAddress, true)
        }
    }

    fun flagDevice(macAddress: String) {
        viewModelScope.launch {
            repository.flagDevice(macAddress, true)
        }
    }

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
