package com.example.follower.ui.devices

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.follower.R
import com.example.follower.data.model.DetectedDevice
import com.example.follower.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val showThreatInfo: Boolean = false,
    private val onItemClick: ((DetectedDevice) -> Unit)? = null
) : ListAdapter<DetectedDevice, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: DetectedDevice) {
            binding.tvDeviceName.text = device.deviceName ?: "Unknown Device"
            binding.tvMacAddress.text = device.macAddress

            if (device.lastRssi != 0) {
                binding.tvRssi.text = "${device.lastRssi} dBm"
                binding.tvRssi.visibility = View.VISIBLE
            } else {
                binding.tvRssi.visibility = View.GONE
            }

            if (showThreatInfo) {
                val score = device.threatScore.toInt()
                val ctx = binding.root.context

                // Badge dot color
                binding.viewThreatBadge.visibility = View.VISIBLE
                val badgeColor = colorForScore(score)
                binding.viewThreatBadge.background.setColorFilter(
                    ContextCompat.getColor(ctx, badgeColor),
                    PorterDuff.Mode.SRC_IN
                )

                // Score text
                binding.tvThreatScore.visibility = View.VISIBLE
                binding.tvThreatScore.text = "Score: $score/100"
                binding.tvThreatScore.setTextColor(ContextCompat.getColor(ctx, badgeColor))

                // Progress bar
                binding.progressThreat.visibility = View.VISIBLE
                binding.progressThreat.progress = score
                binding.progressThreat.progressDrawable.setColorFilter(
                    ContextCompat.getColor(ctx, badgeColor),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.viewThreatBadge.visibility = View.GONE
                binding.tvThreatScore.visibility = View.GONE
                binding.progressThreat.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick?.invoke(device) }
        }

        private fun colorForScore(score: Int): Int = when {
            score >= 61 -> R.color.threat_high
            score >= 31 -> R.color.threat_medium
            score >= 1 -> R.color.threat_low
            else -> R.color.threat_none
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<DetectedDevice>() {
        override fun areItemsTheSame(oldItem: DetectedDevice, newItem: DetectedDevice): Boolean =
            oldItem.macAddress == newItem.macAddress

        override fun areContentsTheSame(oldItem: DetectedDevice, newItem: DetectedDevice): Boolean =
            oldItem == newItem
    }
}
