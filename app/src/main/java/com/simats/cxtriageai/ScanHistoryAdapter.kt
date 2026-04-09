package com.simats.cxtriageai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScanHistoryAdapter(
    private var scans: List<ScanHistoryItem>,
    private val onItemClick: (ScanHistoryItem) -> Unit
) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tv_scan_id)
        val tvPatient: TextView = view.findViewById(R.id.tv_patient_name)
        val tvMrn: TextView = view.findViewById(R.id.tv_mrn)
        val tvDate: TextView = view.findViewById(R.id.tv_scan_date)
        val tvStatus: TextView = view.findViewById(R.id.tv_status_badge)
        val vStatusBg: View = view.findViewById(R.id.v_status_bg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scan = scans[position]
        holder.tvId.text = scan.id
        holder.tvPatient.text = scan.patientName
        holder.tvMrn.text = scan.mrn ?: "Unknown MRN"
        holder.tvDate.text = scan.date
        holder.tvStatus.text = scan.status

        when (scan.status.lowercase()) {
            "completed" -> {
                holder.vStatusBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7"))
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#166534"))
            }
            "pending", "processing", "started" -> {
                holder.vStatusBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7"))
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#92400E"))
            }
            "retake" -> {
                holder.vStatusBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEE2E2"))
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#991B1B"))
            }
            else -> {
                holder.vStatusBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9"))
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#475569"))
            }
        }

        holder.itemView.setOnClickListener { onItemClick(scan) }
    }

    private fun parentContextColor(view: View, colorRes: Int): Int {
        return view.context.getColor(colorRes)
    }

    override fun getItemCount() = scans.size

    fun updateData(newScans: List<ScanHistoryItem>) {
        scans = newScans
        notifyDataSetChanged()
    }
}
