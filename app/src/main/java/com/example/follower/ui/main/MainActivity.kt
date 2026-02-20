package com.example.follower.ui.main

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.follower.R
import com.example.follower.databinding.ActivityMainBinding
import com.example.follower.detection.DetectionEngine
import com.example.follower.service.ScanningService
import com.example.follower.ui.alerts.AlertsActivity
import com.example.follower.ui.devices.DevicesActivity
import com.example.follower.ui.settings.SettingsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"

        private val REQUIRED_PERMISSIONS = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        private val BACKGROUND_LOCATION_PERMISSION =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            } else null
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var scanningService: ScanningService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ScanningService.LocalBinder
            scanningService = binder.getService()
            serviceBound = true
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            scanningService = null
            serviceBound = false
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            requestBackgroundLocationIfNeeded()
        } else {
            Toast.makeText(this, R.string.permission_rationale_location, Toast.LENGTH_LONG).show()
        }
    }

    private val backgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startScanningService()
        } else {
            // Can still work without background location, just with limitations
            startScanningService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application)
        )[MainViewModel::class.java]

        setupUI()
        observeViewModel()
        checkPermissions()
    }

    override fun onStart() {
        super.onStart()
        bindToService()
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun setupUI() {
        binding.apply {
            // Toggle scanning button
            btnToggleScan.setOnClickListener {
                if (viewModel.isScanning.value) {
                    stopScanningService()
                } else {
                    startScanningService()
                }
            }

            // View devices button
            btnViewDevices.setOnClickListener {
                startActivity(Intent(this@MainActivity, DevicesActivity::class.java))
            }

            // View alerts button
            btnViewAlerts.setOnClickListener {
                startActivity(Intent(this@MainActivity, AlertsActivity::class.java))
            }

            // Settings button
            btnSettings.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isScanning.collectLatest { isScanning ->
                updateScanningUI(isScanning)
            }
        }

        lifecycleScope.launch {
            viewModel.currentTier.collectLatest { tier ->
                updateTierUI(tier)
            }
        }

        lifecycleScope.launch {
            viewModel.deviceCount.collectLatest { count ->
                binding.tvDeviceCount.text = count.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.alertCount.collectLatest { count ->
                binding.tvAlertCount.text = count.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.suspiciousDevices.collectLatest { devices ->
                binding.tvSuspiciousCount.text = devices.size.toString()
                updateThreatDisplay(devices)
            }
        }
    }

    private fun observeServiceState() {
        scanningService?.let { service ->
            lifecycleScope.launch {
                service.isScanning.collectLatest { isScanning ->
                    viewModel.updateScanningState(isScanning)
                }
            }

            lifecycleScope.launch {
                service.currentTier.collectLatest { tier ->
                    viewModel.updateTier(tier)
                }
            }
        }
    }

    private fun updateThreatDisplay(devices: List<com.example.follower.data.model.DetectedDevice>) {
        val maxScore = devices.maxOfOrNull { it.threatScore.toInt() } ?: 0

        val (text, color) = when {
            maxScore >= 61 -> getString(R.string.threat_high) to R.color.threat_high
            maxScore >= 31 -> getString(R.string.threat_medium) to R.color.threat_medium
            maxScore >= 1 -> getString(R.string.threat_low) to R.color.threat_low
            else -> getString(R.string.threat_none) to R.color.threat_none
        }

        binding.tvThreatLevel.text = text
        binding.tvThreatLevel.setTextColor(getColor(color))

        val bar = binding.threatBar
        val params = bar.layoutParams as android.widget.LinearLayout.LayoutParams
        params.weight = maxScore.coerceIn(0, 100).toFloat()
        bar.layoutParams = params
        bar.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(color))
    }

    private fun updateScanningUI(isScanning: Boolean) {
        binding.apply {
            if (isScanning) {
                tvStatus.text = getString(R.string.scanning_active)
                tvStatus.setTextColor(getColor(R.color.threat_none))
                btnToggleScan.text = getString(R.string.action_stop_scan)
                scanIndicator.setBackgroundColor(getColor(R.color.threat_none))
            } else {
                tvStatus.text = getString(R.string.scanning_paused)
                tvStatus.setTextColor(getColor(R.color.gray))
                btnToggleScan.text = getString(R.string.action_start_scan)
                scanIndicator.setBackgroundColor(getColor(R.color.gray))
            }
        }
    }

    private fun updateTierUI(tier: DetectionEngine.Tier) {
        binding.apply {
            when (tier) {
                DetectionEngine.Tier.STANDARD -> {
                    tvMode.text = getString(R.string.mode_standard)
                    tvMode.setTextColor(getColor(R.color.mode_standard))
                    ivUsbIndicator.setColorFilter(getColor(R.color.gray))
                }
                DetectionEngine.Tier.ENHANCED -> {
                    tvMode.text = getString(R.string.mode_enhanced)
                    tvMode.setTextColor(getColor(R.color.mode_enhanced))
                    ivUsbIndicator.setColorFilter(getColor(R.color.mode_enhanced))
                }
            }
        }
    }

    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            requestBackgroundLocationIfNeeded()
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (BACKGROUND_LOCATION_PERMISSION != null &&
            ContextCompat.checkSelfPermission(this, BACKGROUND_LOCATION_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            backgroundLocationLauncher.launch(BACKGROUND_LOCATION_PERMISSION)
        } else {
            startScanningService()
        }
    }

    private fun bindToService() {
        Intent(this, ScanningService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun startScanningService() {
        Log.d(TAG, "Starting scanning service")

        val intent = Intent(this, ScanningService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        if (!serviceBound) {
            bindToService()
        }
    }

    private fun stopScanningService() {
        Log.d(TAG, "Stopping scanning service")

        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }

        stopService(Intent(this, ScanningService::class.java))
        viewModel.updateScanningState(false)
    }
}
