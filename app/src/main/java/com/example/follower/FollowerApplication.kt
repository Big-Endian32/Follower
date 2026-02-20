package com.example.follower

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.follower.data.database.FollowerDatabase
import com.example.follower.data.repository.DeviceRepository
import com.example.follower.detection.CalibrationManager
import com.example.follower.detection.SuspicionSettings

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class FollowerApplication : Application() {

    val database by lazy { FollowerDatabase.getDatabase(this) }
    val repository by lazy { DeviceRepository(database.deviceDao()) }
    val dataStore: DataStore<Preferences> get() = settingsDataStore

    val suspicionSettings by lazy { SuspicionSettings(this) }
    val calibrationManager by lazy { CalibrationManager(this, suspicionSettings) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        applyTheme(suspicionSettings.themeMode)
    }

    companion object {
        private lateinit var instance: FollowerApplication

        fun getInstance(): FollowerApplication = instance

        fun applyTheme(mode: String) {
            AppCompatDelegate.setDefaultNightMode(
                when (mode) {
                    SuspicionSettings.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    SuspicionSettings.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }
    }
}
