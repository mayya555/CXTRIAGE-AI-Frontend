package com.simats.cxtriageai

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DashboardAdapter(private var caseList: List<TriageCaseResponse>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val topBorder: View = itemView.findViewById(R.id.view_top_border)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val ivAlert: ImageView = itemView.findViewById(R.id.iv_alert_overlay)
        val tvPatientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvWaitTime: TextView = itemView.findViewById(R.id.tv_wait_time)
        val tvPriority: TextView = itemView.findViewById(R.id.tv_priority_badge)
        val tvAiResult: TextView = itemView.findViewById(R.id.tv_ai_result)
        val progressBar: ProgressBar = itemView.findViewById(R.id.status_progress_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_case, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = caseList[position]

        holder.tvPatientName.text = item.patientName ?: "Unknown"
        holder.tvWaitTime.text = item.createdAt ?: "0m"
        
        // Clean the priority string (remove "PRIORITYENUM." prefix if it exists)
        val rawPriority = item.priority?.uppercase() ?: "ROUTINE"
        val priority = if (rawPriority.contains(".")) {
            rawPriority.substringAfterLast(".")
        } else {
            rawPriority
        }
        
        holder.tvPriority.text = priority
        holder.tvAiResult.text = item.aiFindings ?: item.aiResult ?: "Normal"

        // Styling based on priority
        when (priority) {
            "CRITICAL" -> {
                holder.topBorder.setBackgroundColor(Color.parseColor("#EF4444"))
                holder.tvPriority.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                holder.tvPriority.setTextColor(Color.parseColor("#EF4444"))
                holder.progressBar.progressDrawable = holder.itemView.context.getDrawable(R.drawable.progress_critical)
                holder.ivAlert.visibility = View.VISIBLE
            }
            "URGENT" -> {
                holder.topBorder.setBackgroundColor(Color.parseColor("#F97316"))
                holder.tvPriority.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEDD5"))
                holder.tvPriority.setTextColor(Color.parseColor("#F97316"))
                holder.progressBar.progressDrawable = holder.itemView.context.getDrawable(R.drawable.progress_urgent)
                holder.ivAlert.visibility = View.VISIBLE
            }
            else -> {
                holder.topBorder.setBackgroundColor(Color.parseColor("#22C55E"))
                holder.tvPriority.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                holder.tvPriority.setTextColor(Color.parseColor("#22C55E"))
                holder.progressBar.progressDrawable = holder.itemView.context.getDrawable(R.drawable.progress_routine)
                holder.ivAlert.visibility = View.GONE
            }
        }

        // Handle Progress based on status
        holder.progressBar.progress = when(item.status?.lowercase()) {
            "completed" -> 100
            "reviewing" -> 60
            "pending" -> 30
            else -> 10
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
