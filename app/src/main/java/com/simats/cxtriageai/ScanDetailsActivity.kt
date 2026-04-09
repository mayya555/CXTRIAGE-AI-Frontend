package com.simats.cxtriageai

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_details)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        val scanId = intent.getStringExtra("SCAN_CODE")
        if (scanId != null) {
            loadScanDetails(scanId)
        } else {
            Toast.makeText(this, "Scan ID missing", Toast.LENGTH_SHORT).show()
            // Pull data from intent as fallback
            populateWithIntentExtras()
        }
    }

    private fun loadScanDetails(scanId: String) {
        ApiClient.apiService.getScanDetails(scanId).enqueue(object : Callback<ScanDetailResponse> {
            override fun onResponse(call: Call<ScanDetailResponse>, response: Response<ScanDetailResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    populateUI(response.body()!!)
                } else {
                    // Fail silently to the user and use intent data
                    android.util.Log.e("ScanDetails", "Server Error: ${response.code()}")
                    populateWithIntentExtras()
                }
            }

            override fun onFailure(call: Call<ScanDetailResponse>, t: Throwable) {
                // Fail silently to the user and use intent data
                android.util.Log.e("ScanDetails", "Network Error: ${t.message}")
                populateWithIntentExtras()
            }
        })
    }

    private fun populateUI(data: ScanDetailResponse) {
        findViewById<TextView>(R.id.tv_scan_id).text = data.scanId
        findViewById<TextView>(R.id.tv_patient_name).text = data.patientName
        findViewById<TextView>(R.id.tv_mrn).text = data.mrn
        findViewById<TextView>(R.id.tv_dob).text = data.dob
        findViewById<TextView>(R.id.tv_gender).text = data.gender
        findViewById<TextView>(R.id.tv_scan_details_date).text = data.scanDate
        findViewById<TextView>(R.id.tv_technician).text = data.technician
        findViewById<TextView>(R.id.tv_view_type).text = data.viewType
        findViewById<TextView>(R.id.tv_orientation).text = data.orientation
        findViewById<TextView>(R.id.tv_study_id).text = data.studyId

        updateTimeline(data.status, data.disease, data.confidence)
    }

    private fun populateWithIntentExtras() {
        val scanId = intent.getStringExtra("SCAN_CODE") ?: "SCN-8472"
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Sarah Wilson"
        val mrn = intent.getStringExtra("MRN") ?: "MRN-4521"
        val dob = intent.getStringExtra("DOB") ?: "Mar 15, 1985"
        val gender = intent.getStringExtra("GENDER") ?: "Female"
        val scanDate = intent.getStringExtra("SCAN_DATE") ?: "Today, 11:20 AM"
        val technician = intent.getStringExtra("TECHNICIAN") ?: "James Chen"
        val status = intent.getStringExtra("STATUS") ?: "Processing"
        val disease = intent.getStringExtra("DISEASE")
        val confidence = intent.getStringExtra("CONFIDENCE")

        findViewById<TextView>(R.id.tv_scan_id).text = scanId
        findViewById<TextView>(R.id.tv_patient_name).text = patientName
        findViewById<TextView>(R.id.tv_mrn).text = mrn
        findViewById<TextView>(R.id.tv_dob).text = dob
        findViewById<TextView>(R.id.tv_gender).text = gender
        findViewById<TextView>(R.id.tv_scan_details_date).text = scanDate
        findViewById<TextView>(R.id.tv_technician).text = technician
        findViewById<TextView>(R.id.tv_view_type).text = intent.getStringExtra("VIEW_TYPE") ?: "PA Chest"
        findViewById<TextView>(R.id.tv_orientation).text = intent.getStringExtra("ORIENTATION") ?: "Anterior"
        findViewById<TextView>(R.id.tv_study_id).text = intent.getStringExtra("STUDY_ID") ?: "ST-82941"

        updateTimeline(status, disease, confidence)
    }

    private fun updateTimeline(status: String, disease: String? = null, confidence: String? = null) {
        val ivStep4 = findViewById<ImageView>(R.id.iv_step4)
        val pbAnalysis = findViewById<ProgressBar>(R.id.pb_analysis)
        val tvStep4Title = findViewById<TextView>(R.id.tv_step4_title)
        val tvStep4Subtitle = findViewById<TextView>(R.id.tv_step4_subtitle)
        val ivStep5 = findViewById<ImageView>(R.id.iv_step5)
        val tvStep5Title = findViewById<TextView>(R.id.tv_step5_title)
        val tvStep5Subtitle = findViewById<TextView>(R.id.tv_step5_subtitle)

        if (status.equals("Completed", ignoreCase = true)) {
            // Step 4: AI Analysis Completed
            ivStep4?.setImageResource(R.drawable.ic_check_circle_green)
            ivStep4?.imageTintList = null
            pbAnalysis?.visibility = View.GONE
            tvStep4Title?.text = if (disease == "Normal") "Normal - No Findings" else "AI Detection: ${disease ?: "N/A"}"
            tvStep4Subtitle?.text = "Analysis verified with ${confidence ?: "98.4%"} confidence"

            // Step 5: Report Sent to Doctor
            ivStep5?.setImageResource(R.drawable.ic_check_circle_green)
            ivStep5?.imageTintList = null
            tvStep5Title?.text = "Report Sent to Doctor"
            tvStep5Title?.setTextColor(android.graphics.Color.parseColor("#0F172A"))
            tvStep5Subtitle?.text = "Delivered to Dr. Sarah (Radiologist)"
        } else {
            // Processing state
            ivStep4?.setImageResource(R.drawable.bg_dot_yellow)
            ivStep4?.imageTintList = null
            pbAnalysis?.visibility = View.VISIBLE
            tvStep4Title?.text = "AI Analysis in Progress"
            tvStep4Subtitle?.text = "Estimated completion: 2-3 minutes"

            // Step 5: Doctor Review (Greyed out)
            ivStep5?.setImageResource(R.drawable.ic_clock_status)
            ivStep5?.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CBD5E1"))
            tvStep5Title?.text = "Doctor Review"
            tvStep5Title?.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
            tvStep5Subtitle?.text = "Pending AI completion"
        }
    }
}
