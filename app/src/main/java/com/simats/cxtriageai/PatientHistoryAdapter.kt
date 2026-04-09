package com.simats.cxtriageai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class PatientHistoryItem(
    val name: String,
    val id: String,
    val caseId: Int?, // Added caseId to track actual database id
    val details: String, // "Last scan: 2 weeks ago • Dr. Bennett"
    val tag1: String?,
    val tag2: String?
)

class PatientHistoryAdapter(private val patientList: List<PatientHistoryItem>) : RecyclerView.Adapter<PatientHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvId: TextView = itemView.findViewById(R.id.tv_patient_id)
        val tvDetails: TextView = itemView.findViewById(R.id.tv_last_scan)
        val tvTag1: TextView = itemView.findViewById(R.id.tv_tag_1)
        val tvTag2: TextView = itemView.findViewById(R.id.tv_tag_2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = patientList[position]
        holder.tvName.text = item.name
        holder.tvId.text = item.id
        holder.tvDetails.text = item.details

        // Tag Logic
        if (item.tag1 != null) {
            holder.tvTag1.visibility = View.VISIBLE
            holder.tvTag1.text = item.tag1
        } else {
            holder.tvTag1.visibility = View.GONE
        }

        if (item.tag2 != null) {
            holder.tvTag2.visibility = View.VISIBLE
            holder.tvTag2.text = item.tag2
        } else {
            holder.tvTag2.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, PatientDetailActivity::class.java)
            if (item.caseId != null) {
                intent.putExtra("CASE_ID", item.caseId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = patientList.size
}
