package com.simats.cxtriageai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView

data class QueueItem(
    val id: String,
    val timestamp: String,
    val patientName: String,
    val priority: String, // "High", "Normal"
    val aiResult: String, // "AI: Pneumonia Detected", "AI: Normal"
    val isCritical: Boolean
)

class CaseQueueAdapter(private val caseList: List<QueueItem>) : RecyclerView.Adapter<CaseQueueAdapter.QueueViewHolder>() {

    class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tv_case_id)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val tvPatientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvPriority: TextView = itemView.findViewById(R.id.tv_priority_badge)
        val tvAiResult: TextView = itemView.findViewById(R.id.tv_ai_result)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val ivAlert: ImageView = itemView.findViewById(R.id.iv_alert_overlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_case_queue, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val item = caseList[position]

        holder.tvId.text = "ID:\n${item.id}"
        holder.tvTimestamp.text = item.timestamp
        holder.tvPatientName.text = item.patientName
        holder.tvPriority.text = item.priority
        holder.tvAiResult.text = item.aiResult

        // Priority Logic
        // Priority Logic
        if (item.priority == "High") {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_badge_light_red)
            holder.tvPriority.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.critical_red))
        } else {
             holder.tvPriority.setBackgroundResource(R.drawable.bg_badge_light_gray) // Or specific normal color
             holder.tvPriority.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_dark))
        }

        // Alert Overlay
        if (item.isCritical) {
            holder.ivAlert.visibility = View.VISIBLE
        } else {
            holder.ivAlert.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, CaseReviewActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = caseList.size
}
