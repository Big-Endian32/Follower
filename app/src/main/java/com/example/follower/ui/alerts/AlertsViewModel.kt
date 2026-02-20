package com.example.follower.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.follower.FollowerApplication
import com.example.follower.data.model.AlertAction
import com.example.follower.data.model.ThreatAlert
import com.example.follower.data.repository.DeviceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeviceRepository =
        (application as FollowerApplication).repository

    val allAlerts: StateFlow<List<ThreatAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unacknowledgedAlerts: StateFlow<List<ThreatAlert>> = repository.unacknowledgedAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun dismissAlert(alert: ThreatAlert) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alert.id)
            repository.setAlertAction(alert.id, AlertAction.DISMISSED)
        }
    }

    fun whitelistDevice(alert: ThreatAlert) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alert.id)
            repository.setAlertAction(alert.id, AlertAction.WHITELISTED)
            repository.whitelistDevice(alert.deviceMacAddress, true)
        }
    }

    fun flagDevice(alert: ThreatAlert) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alert.id)
            repository.setAlertAction(alert.id, AlertAction.FLAGGED)
            repository.flagDevice(alert.deviceMacAddress, true)
        }
    }
}

class AlertsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
