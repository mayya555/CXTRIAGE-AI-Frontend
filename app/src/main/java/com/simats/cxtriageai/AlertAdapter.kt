package com.simats.cxtriageai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlertAdapter(
    private var alertList: List<AlertResponse>,
    private val onItemClick: (AlertResponse) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCaseId: TextView = itemView.findViewById(R.id.tv_case_id)
        val tvTime: TextView = itemView.findViewById(R.id.tv_alert_time)
        val tvPatientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvPriority: TextView = itemView.findViewById(R.id.tv_priority)
        val tvFinding: TextView = itemView.findViewById(R.id.tv_finding)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_critical_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alertList[position]

        holder.tvCaseId.text = alert.caseCode ?: "SC-5501"
        holder.tvTime.text = alert.createdAt ?: "10:30 AM"
        holder.tvPatientName.text = alert.patientName ?: "Unknown Patient"
        
        val priority = alert.priority?.uppercase() ?: "HIGH"
        holder.tvPriority.text = alert.priority ?: "High"
        holder.tvFinding.text = "AI: ${alert.aiResult ?: "Detection"}"

        // Priority Badge Styling
        if (priority == "CRITICAL" || priority == "HIGH") {
            holder.tvPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF1F2"))
            holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#BE123C"))
        } else {
            holder.tvPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#ECFDF5"))
            holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#059669"))
        }

        // AI Finding Badge Styling
        if (holder.tvFinding.text.toString().contains("Normal", ignoreCase = true)) {
            holder.tvFinding.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9"))
            holder.tvFinding.setTextColor(android.graphics.Color.parseColor("#475569"))
        } else {
            holder.tvFinding.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CCFBF1"))
            holder.tvFinding.setTextColor(android.graphics.Color.parseColor("#065F46"))
        }

        holder.itemView.setOnClickListener { onItemClick(alert) }
    }

    override fun getItemCount() = alertList.size

    fun updateData(newList: List<AlertResponse>) {
        alertList = newList
        notifyDataSetChanged()
    }
}
