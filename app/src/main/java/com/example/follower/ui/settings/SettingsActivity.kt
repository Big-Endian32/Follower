package com.example.follower.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.follower.FollowerApplication
import com.example.follower.R
import com.example.follower.databinding.ActivitySettingsBinding
import com.example.follower.detection.CalibrationManager
import com.example.follower.detection.SuspicionSettings
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settings: SuspicionSettings
    private lateinit var calibration: CalibrationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as FollowerApplication
        settings = app.suspicionSettings
        calibration = app.calibrationManager

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadCurrentValues()
        bindThemeToggle()
        bindSliders()
        bindToggles()
        bindResetButton()
    }

    private fun loadCurrentValues() {
        binding.apply {
            // Theme toggle
            val checkedId = when (settings.themeMode) {
                SuspicionSettings.THEME_LIGHT -> R.id.btnThemeLight
                SuspicionSettings.THEME_SYSTEM -> R.id.btnThemeSystem
                else -> R.id.btnThemeDark
            }
            toggleTheme.check(checkedId)

            sliderLowThreshold.value = settings.lowThreshold.toFloat()
            tvLowThresholdValue.text = settings.lowThreshold.toString()

            sliderHighThreshold.value = settings.highThreshold.toFloat()
            tvHighThresholdValue.text = settings.highThreshold.toString()

            sliderClusterRadius.value = settings.locationClusterThresholdMeters
            tvClusterRadiusValue.text = getString(R.string.settings_meters_fmt, settings.locationClusterThresholdMeters.toInt())

            sliderMinLocations.value = settings.minDistinctLocations.toFloat()
            tvMinLocationsValue.text = settings.minDistinctLocations.toString()

            sliderContinuousFollowing.value = settings.continuousFollowingMinutes.toFloat()
            tvContinuousFollowingValue.text = getString(R.string.settings_minutes_fmt, settings.continuousFollowingMinutes)

            sliderTotalFollowing.value = settings.totalFollowingMinutes.toFloat()
            tvTotalFollowingValue.text = getString(R.string.settings_minutes_fmt, settings.totalFollowingMinutes)

            switchCalibration.isChecked = settings.calibrationModeEnabled
            tvCalibrationSamples.text = getString(R.string.settings_calibration_samples, calibration.getSampleCount())

            switchScanOnlyMoving.isChecked = settings.scanOnlyWhenMoving
        }
    }

    private fun bindThemeToggle() {
        binding.toggleTheme.addOnButtonCheckedListener(
            MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@OnButtonCheckedListener
                val mode = when (checkedId) {
                    R.id.btnThemeLight -> SuspicionSettings.THEME_LIGHT
                    R.id.btnThemeSystem -> SuspicionSettings.THEME_SYSTEM
                    else -> SuspicionSettings.THEME_DARK
                }
                settings.themeMode = mode
                FollowerApplication.applyTheme(mode)
            }
        )
    }

    private fun bindSliders() {
        fun Slider.onChange(save: (Float) -> Unit, format: (Float) -> String, label: android.widget.TextView) {
            addOnChangeListener { _, value, _ ->
                save(value)
                label.text = format(value)
            }
        }

        binding.apply {
            sliderLowThreshold.onChange(
                save = { settings.lowThreshold = it.toInt() },
                format = { it.toInt().toString() },
                label = tvLowThresholdValue
            )
            sliderHighThreshold.onChange(
                save = { settings.highThreshold = it.toInt() },
                format = { it.toInt().toString() },
                label = tvHighThresholdValue
            )
            sliderClusterRadius.onChange(
                save = { settings.locationClusterThresholdMeters = it },
                format = { getString(R.string.settings_meters_fmt, it.toInt()) },
                label = tvClusterRadiusValue
            )
            sliderMinLocations.onChange(
                save = { settings.minDistinctLocations = it.toInt() },
                format = { it.toInt().toString() },
                label = tvMinLocationsValue
            )
            sliderContinuousFollowing.onChange(
                save = { settings.continuousFollowingMinutes = it.toInt() },
                format = { getString(R.string.settings_minutes_fmt, it.toInt()) },
                label = tvContinuousFollowingValue
            )
            sliderTotalFollowing.onChange(
                save = { settings.totalFollowingMinutes = it.toInt() },
                format = { getString(R.string.settings_minutes_fmt, it.toInt()) },
                label = tvTotalFollowingValue
            )
        }
    }

    private fun bindToggles() {
        binding.switchCalibration.setOnCheckedChangeListener { _, checked ->
            settings.calibrationModeEnabled = checked
        }
        binding.switchScanOnlyMoving.setOnCheckedChangeListener { _, checked ->
            settings.scanOnlyWhenMoving = checked
        }
    }

    private fun bindResetButton() {
        binding.btnResetDefaults.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_reset_confirm_title)
                .setMessage(R.string.settings_reset_confirm_message)
                .setPositiveButton(R.string.settings_reset_defaults) { _, _ ->
                    settings.resetToDefaults()
                    calibration.clearSamples()
                    loadCurrentValues()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
}
