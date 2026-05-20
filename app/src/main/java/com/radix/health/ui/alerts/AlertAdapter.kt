package com.radix.health.ui.alerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.radix.health.R
import com.radix.health.data.model.Alert
import com.radix.health.databinding.ItemAlertBinding
import com.radix.health.util.Formatters

class AlertAdapter(
    private val onResolve: (Alert) -> Unit
) : ListAdapter<Alert, AlertAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    inner class VH(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: Alert) {
            binding.tvType.text = alert.alertType
            binding.tvMessage.text = alert.message
            binding.tvTime.text =
                "${Formatters.shortDate(alert.createdAt)} · ${Formatters.time(alert.createdAt)}"

            val badge = when {
                alert.isResolved -> R.drawable.badge_success
                alert.alertType.contains("RADIATION", true) ||
                    alert.alertType.contains("DANGER", true) -> R.drawable.badge_danger
                else -> R.drawable.badge_warning
            }
            binding.badgeStatus.setBackgroundResource(badge)
            binding.btnResolve.visibility = if (alert.isResolved)
                android.view.View.GONE else android.view.View.VISIBLE
            binding.btnResolve.setOnClickListener { onResolve(alert) }
        }
    }

    object Diff : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(a: Alert, b: Alert) = a.id == b.id
        override fun areContentsTheSame(a: Alert, b: Alert) = a == b
    }
}
