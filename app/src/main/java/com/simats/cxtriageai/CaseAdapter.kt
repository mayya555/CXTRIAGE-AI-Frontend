package com.simats.cxtriageai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CaseItem(
    val name: String,
    val condition: String,
    val priority: String, // "CRITICAL", "URGENT", "ROUTINE"
    val waitTime: String,
    val progress: Int
)

class CaseAdapter(private val caseList: List<CaseItem>) : RecyclerView.Adapter<CaseAdapter.CaseViewHolder>() {

    class CaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvCondition: TextView = itemView.findViewById(R.id.tv_condition)
        val tvPriority: TextView = itemView.findViewById(R.id.tv_priority_badge)
        val tvWaitTime: TextView = itemView.findViewById(R.id.tv_wait_time)
        val priorityLine: View = itemView.findViewById(R.id.priority_line)
        val ivAlert: ImageView = itemView.findViewById(R.id.iv_alert_overlay)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_priority_case, parent, false)
        return CaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val currentItem = caseList[position]

        holder.tvName.text = currentItem.name
        holder.tvCondition.text = currentItem.condition
        holder.tvPriority.text = currentItem.priority
        holder.tvWaitTime.text = currentItem.waitTime

        // Reset styles
        holder.ivAlert.visibility = View.GONE

        val context = holder.itemView.context

        when (currentItem.priority) {
            "CRITICAL" -> {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_badge_light_red)
                holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                holder.priorityLine.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"))
                holder.ivAlert.visibility = View.VISIBLE
            }
            "URGENT" -> {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_badge_light_orange)
                holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#D97706"))
                holder.priorityLine.setBackgroundColor(android.graphics.Color.parseColor("#F59E0B"))
            }
            "ROUTINE" -> {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_badge_light_green)
                holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#10B981"))
                holder.priorityLine.setBackgroundColor(android.graphics.Color.parseColor("#10B981"))
            }
        }
    }

    override fun getItemCount() = caseList.size
}
