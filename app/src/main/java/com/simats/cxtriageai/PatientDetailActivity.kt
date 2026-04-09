package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PatientDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patient_detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val caseId = intent.getIntExtra("CASE_ID", -1)
        if (caseId != -1) {
            fetchPatientDetails(caseId)
            fetchPatientHistory(caseId)
        } else {
            android.widget.Toast.makeText(this, "Error: Invalid case ID", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPatientDetails(caseId: Int) {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        ApiClient.apiService.getCaseDetails(caseId, doctorId).enqueue(object : retrofit2.Callback<TriageCaseResponse> {
            override fun onResponse(call: retrofit2.Call<TriageCaseResponse>, response: retrofit2.Response<TriageCaseResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    bindPatientDetails(response.body()!!)
                }
            }
            override fun onFailure(call: retrofit2.Call<TriageCaseResponse>, t: Throwable) {
                // Handle error implicitly
            }
        })
    }

    private fun bindPatientDetails(caseDetails: TriageCaseResponse) {
        val name = caseDetails.patientName ?: "Unknown"
        val firstChar = if (name.isNotEmpty()) name.substring(0, 1) else "P"
        val lastChar = if (name.contains(" ")) name.split(" ").last().substring(0, 1) else ""
        
        findViewById<TextView>(R.id.tv_initials).text = (firstChar + lastChar).uppercase()
        findViewById<TextView>(R.id.tv_patient_name).text = name
        
        val gender = caseDetails.gender ?: "Not specified"
        val dobString = caseDetails.dateOfBirth
        val age = if (dobString != null && dobString.length >= 4) {
            try {
                val year = dobString.substring(0, 4).toIntOrNull()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                if (year != null) "${currentYear - year} Years" else "Age N/A"
            } catch (e: Exception) {
                "Age N/A"
            }
        } else {
            "Age N/A"
        }
        
        val mrn = caseDetails.mrn ?: "N/A"
        findViewById<TextView>(R.id.tv_patient_meta).text = "$gender • $age • ID: $mrn"

        findViewById<TextView>(R.id.tv_patient_vital_height).text = if (caseDetails.height.isNullOrEmpty()) "N/A" else caseDetails.height
        findViewById<TextView>(R.id.tv_patient_vital_weight).text = if (caseDetails.weight.isNullOrEmpty()) "N/A" else caseDetails.weight
        findViewById<TextView>(R.id.tv_patient_vital_blood).text = if (caseDetails.bloodType.isNullOrEmpty()) "N/A" else caseDetails.bloodType
    }

    private fun fetchPatientHistory(caseId: Int) {
        ApiClient.apiService.getPatientHistory(caseId).enqueue(object : retrofit2.Callback<List<TriageCaseResponse>> {
            override fun onResponse(call: retrofit2.Call<List<TriageCaseResponse>>, response: retrofit2.Response<List<TriageCaseResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!
                    val adapter = PatientScanHistoryAdapter(history)
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_scan_history).adapter = adapter
                }
            }
            override fun onFailure(call: retrofit2.Call<List<TriageCaseResponse>>, t: Throwable) {
                // Handle failure implicitly
            }
        })
    }
}
