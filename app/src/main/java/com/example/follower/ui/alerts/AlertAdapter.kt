package com.example.follower.ui.alerts

import android.graphics.PorterDuff
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.follower.R
import com.example.follower.data.model.ThreatAlert
import com.example.follower.data.model.ThreatLevel
import com.example.follower.databinding.ItemAlertBinding

class AlertAdapter(
    private val onDismiss: (ThreatAlert) -> Unit,
    private val onWhitelist: (ThreatAlert) -> Unit,
    private val onFlag: (ThreatAlert) -> Unit
) : ListAdapter<ThreatAlert, AlertAdapter.AlertViewHolder>(AlertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(
        private val binding: ItemAlertBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: ThreatAlert) {
            val ctx = binding.root.context

            // Device name
            binding.tvDeviceName.text = alert.deviceName ?: ctx.getString(R.string.alert_unknown_device)

            // MAC address
            binding.tvMacAddress.text = alert.deviceMacAddress

            // Relative timestamp
            binding.tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
                alert.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )

            // Threat level color
            val color = when (alert.threatLevel) {
                ThreatLevel.HIGH -> R.color.threat_high
                ThreatLevel.MEDIUM -> R.color.threat_medium
                ThreatLevel.LOW -> R.color.threat_low
            }
            binding.viewThreatDot.background.setColorFilter(
                ContextCompat.getColor(ctx, color),
                PorterDuff.Mode.SRC_IN
            )

            // Threat level label
            val levelText = when (alert.threatLevel) {
                ThreatLevel.HIGH -> ctx.getString(R.string.threat_high)
                ThreatLevel.MEDIUM -> ctx.getString(R.string.threat_medium)
                ThreatLevel.LOW -> ctx.getString(R.string.threat_low)
            }
            binding.tvThreatLevel.text = levelText
            binding.tvThreatLevel.setTextColor(ContextCompat.getColor(ctx, color))

            // Score
            binding.tvScore.text = ctx.getString(R.string.alert_score_fmt, alert.threatScore.toInt())
            binding.tvScore.setTextColor(ContextCompat.getColor(ctx, color))

            // Details line
            val durationMin = (alert.followDurationMs / 60_000).toInt()
            binding.tvDetails.text = ctx.getString(
                R.string.alert_details_fmt,
                alert.sightingCount,
                alert.locationCount,
                durationMin
            )

            // Dimmed if already acknowledged
            binding.root.alpha = if (alert.isAcknowledged) 0.55f else 1f

            // Action buttons
            binding.btnDismiss.setOnClickListener { onDismiss(alert) }
            binding.btnWhitelist.setOnClickListener { onWhitelist(alert) }
            binding.btnFlag.setOnClickListener { onFlag(alert) }
        }
    }

    class AlertDiffCallback : DiffUtil.ItemCallback<ThreatAlert>() {
        override fun areItemsTheSame(oldItem: ThreatAlert, newItem: ThreatAlert): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ThreatAlert, newItem: ThreatAlert): Boolean =
            oldItem == newItem
    }
}
