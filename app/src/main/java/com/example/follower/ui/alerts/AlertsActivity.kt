package com.example.follower.ui.alerts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.data.model.ThreatAlert
import com.example.follower.databinding.ActivityAlertsBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding
    private lateinit var viewModel: AlertsViewModel
    private lateinit var adapter: AlertAdapter

    private var showingAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            AlertsViewModelFactory(application)
        )[AlertsViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupTabs()
        observeAlerts(viewModel.unacknowledgedAlerts)
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(
            onDismiss = { viewModel.dismissAlert(it) },
            onWhitelist = { viewModel.whitelistDevice(it) },
            onFlag = { viewModel.flagDevice(it) }
        )
        binding.rvAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvAlerts.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showingAll = tab.position == 1
                val source = if (showingAll) viewModel.allAlerts else viewModel.unacknowledgedAlerts
                observeAlerts(source)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun observeAlerts(source: StateFlow<List<ThreatAlert>>) {
        lifecycleScope.launch {
            source.collectLatest { alerts ->
                adapter.submitList(alerts)
                binding.tvEmpty.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
                binding.rvAlerts.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
}
