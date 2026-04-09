package com.simats.cxtriageai

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CaseQueueAdapter(private var caseList: List<TriageCaseResponse>) : RecyclerView.Adapter<CaseQueueAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCaseId: TextView = itemView.findViewById(R.id.tv_case_id)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val ivAlert: ImageView = itemView.findViewById(R.id.iv_alert_badge)
        val tvPatientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvPriority: TextView = itemView.findViewById(R.id.tv_priority_badge)
        val tvAiBadge: TextView = itemView.findViewById(R.id.tv_ai_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_case_queue, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = caseList[position]

        holder.tvCaseId.text = item.caseCode ?: "SC-${5500 + item.id}"
        holder.tvTimestamp.text = item.createdAt ?: "2023-10-24 10:30 AM"
        holder.tvPatientName.text = item.patientName ?: "Patient Name"
        
        val priority = item.priority?.uppercase() ?: "ROUTINE"
        holder.tvPriority.text = if (priority == "CRITICAL" || priority == "URGENT") "High" else "Normal"

        holder.tvAiBadge.text = "AI: ${item.aiFindings ?: item.aiResult ?: "Normal"}"

        // Styling based on priority
        if (priority == "CRITICAL" || priority == "URGENT") {
            holder.tvPriority.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF1F2"))
            holder.tvPriority.setTextColor(Color.parseColor("#BE123C"))
            holder.ivAlert.visibility = View.VISIBLE
        } else {
            holder.tvPriority.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ECFDF5"))
            holder.tvPriority.setTextColor(Color.parseColor("#059669"))
            holder.ivAlert.visibility = View.GONE
        }

        // AI Result Styling
        if (holder.tvAiBadge.text.toString().contains("Normal", ignoreCase = true)) {
            holder.tvAiBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
            holder.tvAiBadge.setTextColor(Color.parseColor("#475569"))
        } else if (holder.tvAiBadge.text.toString().contains("Detected", ignoreCase = true)) {
            holder.tvAiBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCFBF1"))
            holder.tvAiBadge.setTextColor(Color.parseColor("#065F46"))
        } else {
            holder.tvAiBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
            holder.tvAiBadge.setTextColor(Color.parseColor("#475569"))
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, CaseReviewActivity::class.java)
            intent.putExtra("CASE_ID", item.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = caseList.size

    fun updateData(newList: List<TriageCaseResponse>) {
        caseList = newList
        notifyDataSetChanged()
    }
}
