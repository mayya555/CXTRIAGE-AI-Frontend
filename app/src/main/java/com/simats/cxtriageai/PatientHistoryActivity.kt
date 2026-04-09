package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PatientHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patient_history_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupRecyclerView()
        loadPatientHistory()
    }

    private fun setupNavigation() {
        findViewById<android.view.View>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.nav_cases).setOnClickListener {
            val intent = Intent(this, CaseQueueActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.nav_alerts).setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // nav_history_active is current

        findViewById<android.view.View>(R.id.nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_patient_history)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val historyList = listOf(
            PatientHistoryItem("John Doe", "P-1024", null, "Last scan: 2 weeks ago • Dr. Bennett", "Pneumonia History", "Smoker"),
            PatientHistoryItem("Jane Smith", "P-1025", null, "Last scan: 2 weeks ago • Dr. Bennett", "Pneumonia History", "Smoker"),
            PatientHistoryItem("Robert Johnson", "P-1026", null, "Last scan: 2 weeks ago • Dr. Bennett", "Pneumonia History", "Smoker")
        )

        val adapter = PatientHistoryAdapter(historyList)
        recyclerView.adapter = adapter
    }

    private fun loadPatientHistory() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId <= 0) {
            android.widget.Toast.makeText(this, "Invalid doctor session. Please login again.", android.widget.Toast.LENGTH_LONG).show()
            android.util.Log.e("PatientHistory", "Invalid doctor_id: $doctorId")
            
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // ✅ LOG doctor_id before call
        android.util.Log.d("PatientHistory", "Loading history for doctor_id = $doctorId")

        // ✅ LOG doctor_id before history fetch
        android.util.Log.d("History", "Loading case-history for doctor_id = $doctorId")

        if (doctorId <= 0) {
            android.widget.Toast.makeText(this, "Invalid doctor session", android.widget.Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return
        }

        ApiClient.apiService.getCaseHistory(doctorId = doctorId).enqueue(object : retrofit2.Callback<List<TriageCaseResponse>> {
            override fun onResponse(call: retrofit2.Call<List<TriageCaseResponse>>, response: retrofit2.Response<List<TriageCaseResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val completedCases = response.body()!!
                    if (completedCases.isNotEmpty()) {
                        val adapter = PatientHistoryAdapter(completedCases.map { 
                            PatientHistoryItem(
                                it.patientName ?: "Unknown",
                                it.caseCode ?: "P-${it.id}",
                                it.id,
                                "Diagnosis: ${it.finalDiagnosis ?: it.diagnosis ?: "N/A"}",
                                "Status: ${it.status}",
                                "Priority: ${it.priority}"
                            )
                        })
                        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_patient_history).adapter = adapter
                    }
                } else {
                    android.util.Log.e("PatientHistory", "API Error ${response.code()}: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: retrofit2.Call<List<TriageCaseResponse>>, t: Throwable) {
                android.util.Log.e("PatientHistory", "Failure: ${t.message}")
            }
        })
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            
            // Clear Session
            SessionManager.clearUserData(this@PatientHistoryActivity)
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
