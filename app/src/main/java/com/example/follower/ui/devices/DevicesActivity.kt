package com.example.follower.ui.devices

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.databinding.ActivityDevicesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DevicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevicesBinding
    private lateinit var viewModel: DevicesViewModel

    private lateinit var nearbyAdapter: DeviceAdapter
    private lateinit var suspiciousAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            DevicesViewModelFactory(application)
        )[DevicesViewModel::class.java]

        setupToolbar()
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        suspiciousAdapter = DeviceAdapter(showThreatInfo = true)
        binding.rvSuspicious.apply {
            layoutManager = LinearLayoutManager(this@DevicesActivity)
            adapter = suspiciousAdapter
        }

        nearbyAdapter = DeviceAdapter(showThreatInfo = false)
        binding.rvNearby.apply {
            layoutManager = LinearLayoutManager(this@DevicesActivity)
            adapter = nearbyAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.suspiciousDevices.collectLatest { devices ->
                suspiciousAdapter.submitList(devices)
                binding.tvSuspiciousEmpty.visibility =
                    if (devices.isEmpty()) View.VISIBLE else View.GONE
                binding.rvSuspicious.visibility =
                    if (devices.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.nearbyDevices.collectLatest { devices ->
                nearbyAdapter.submitList(devices)
                binding.tvNearbyEmpty.visibility =
                    if (devices.isEmpty()) View.VISIBLE else View.GONE
                binding.rvNearby.visibility =
                    if (devices.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
}
