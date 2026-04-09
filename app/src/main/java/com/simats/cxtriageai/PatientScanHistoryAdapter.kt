package com.simats.cxtriageai

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class PatientScanHistoryAdapter(private val scans: List<TriageCaseResponse>) :
    RecyclerView.Adapter<PatientScanHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_history_title)
        val tvDate: TextView = view.findViewById(R.id.tv_history_date)
        val tvDesc: TextView = view.findViewById(R.id.tv_history_desc)
        val tvStatus: TextView = view.findViewById(R.id.tv_history_status)
        val timelineLine: View = view.findViewById(R.id.timeline_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_scan_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scan = scans[position]

        // Fix timeline visual for last item
        if (position == scans.size - 1) {
            holder.timelineLine.layoutParams.height = 0 // Hide tail of timeline for the last item
        }

        holder.tvTitle.text = scan.caseCode ?: "Scan #${scan.id}"
        
        // Format Date
        holder.tvDate.text = try {
            val inFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = scan.createdAt?.let { inFormat.parse(it) }
            date?.let { outFormat.format(it) } ?: scan.createdAt
        } catch (e: Exception) {
            scan.createdAt ?: "Unknown"
        }

        holder.tvDesc.text = scan.aiFindings ?: scan.aiResult ?: "No findings available."

        // Status Badge Logic
        holder.tvStatus.text = scan.aiResult ?: "Normal"
        val context = holder.itemView.context
        
        when (scan.priority?.uppercase()) {
            "CRITICAL" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_abnormal)
                holder.tvStatus.setTextColor(Color.parseColor("#B91C1C")) // Red
            }
            "URGENT" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_abnormal)
                holder.tvStatus.setTextColor(Color.parseColor("#B91C1C"))
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_normal)
                holder.tvStatus.setTextColor(Color.parseColor("#166534")) // Dark Green
            }
        }
    }

    override fun getItemCount(): Int = scans.size
}
