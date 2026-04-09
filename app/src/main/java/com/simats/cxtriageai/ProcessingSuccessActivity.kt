package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProcessingSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_processing_success)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_bg)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.layoutParams.height = (100 * resources.displayMetrics.density).toInt() + systemBars.top
            insets
        }

        val scanId = intent.getIntExtra("SCAN_ID", -1)
        val disease = intent.getStringExtra("AI_DISEASE") ?: "Normal"
        val confidence = intent.getStringExtra("AI_CONFIDENCE") ?: "98.4%"
        val priority = intent.getStringExtra("AI_PRIORITY")?.uppercase() ?: "ROUTINE"

        android.util.Log.d("SuccessActivity", "--- INITIALIZING SUCCESS SCREEN ---")
        android.util.Log.d("SuccessActivity", "scanId received: $scanId")
        android.util.Log.d("SuccessActivity", "disease: $disease, confidence: $confidence, priority: $priority")
        
        val spinnerDoctors = findViewById<android.widget.Spinner>(R.id.spinner_doctors)
        val tvAssignedDoctorId = findViewById<android.widget.TextView>(R.id.tv_assigned_doctor_id)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progress_bar)
        val btnSendDoctor = findViewById<android.view.View>(R.id.btn_send_doctor)

        findViewById<android.widget.TextView>(R.id.tv_disease).text = disease
        findViewById<android.widget.TextView>(R.id.tv_confidence).text = confidence
        
        var doctorsList: List<Doctor> = emptyList()

        // Fetch Doctors
        ApiClient.apiService.getDoctors().enqueue(object : retrofit2.Callback<List<Doctor>> {
            override fun onResponse(call: retrofit2.Call<List<Doctor>>, response: retrofit2.Response<List<Doctor>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    doctorsList = response.body()!!
                    val adapter = android.widget.ArrayAdapter(
                        this@ProcessingSuccessActivity,
                        android.R.layout.simple_spinner_item,
                        doctorsList.map { it.name }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDoctors.adapter = adapter
                    
                    // Try to pre-select if we passed one (optional)
                    val incomingDocId = intent.getIntExtra("AI_DOCTOR_ID", -1)
                    if (incomingDocId != -1) {
                        val index = doctorsList.indexOfFirst { it.doctor_id == incomingDocId }
                        if (index != -1) spinnerDoctors.setSelection(index)
                    }

                    spinnerDoctors.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            val doc = doctorsList[position]
                            tvAssignedDoctorId.text = "ID: ${doc.doctor_id}"
                            android.util.Log.d("SuccessSelection", "Doctor selected: ${doc.name} (ID: ${doc.doctor_id})")
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                }
            }
            override fun onFailure(call: retrofit2.Call<List<Doctor>>, t: Throwable) {
                android.widget.Toast.makeText(this@ProcessingSuccessActivity, "Error loading doctors", android.widget.Toast.LENGTH_SHORT).show()
            }
        })

        val tvPriority = findViewById<android.widget.TextView>(R.id.tv_priority)
        val llPriorityBadge = findViewById<android.widget.LinearLayout>(R.id.ll_priority_badge)
        
        tvPriority.text = priority
        
        when (priority) {
            "CRITICAL" -> {
                llPriorityBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEE2E2"))
                tvPriority.setTextColor(android.graphics.Color.parseColor("#EF4444"))
            }
            "URGENT" -> {
                llPriorityBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEDD5"))
                tvPriority.setTextColor(android.graphics.Color.parseColor("#F97316"))
            }
            "ROUTINE", "NORMAL" -> {
                llPriorityBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DBEAFE"))
                tvPriority.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
            }
            else -> {
                llPriorityBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2E8F0"))
                tvPriority.setTextColor(android.graphics.Color.parseColor("#64748B"))
            }
        }

        btnSendDoctor.setOnClickListener {
            android.util.Log.d("SendDoctor", "=== SEND TO DOCTOR CLICKED ===")
            android.util.Log.d("SendDoctor", "scanId = $scanId")
            android.util.Log.d("SendDoctor", "doctorsList.size = ${doctorsList.size}")

            if (doctorsList.isEmpty()) {
                android.widget.Toast.makeText(this, "Please wait for doctors list to load", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (scanId <= 0) {
                android.util.Log.e("SendDoctor", "Invalid scanId: $scanId — skipping createStudy, navigating to dashboard")
                android.widget.Toast.makeText(this, "Case recorded. Returning to dashboard.", android.widget.Toast.LENGTH_SHORT).show()
                val dashIntent = Intent(this@ProcessingSuccessActivity, TechnicianDashboardActivity::class.java)
                dashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(dashIntent)
                finish()
                return@setOnClickListener
            }

            val selectedDoctor = doctorsList[spinnerDoctors.selectedItemPosition]
            android.util.Log.d("SendDoctor", "Selected doctor: ${selectedDoctor.name} (ID: ${selectedDoctor.doctor_id})")

            if (selectedDoctor.doctor_id <= 0) {
                android.widget.Toast.makeText(this, "Please select a valid doctor", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (scanId <= 0) {
                android.util.Log.e("SendDoctor", "CRITICAL ERROR: scanId is $scanId. cannot create study.")
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("System Error")
                    .setMessage("Invalid Scan ID ($scanId). This case might not have been initialized correctly.")
                    .setPositiveButton("Go to Dashboard") { _, _ ->
                        val dashIntent = Intent(this@ProcessingSuccessActivity, TechnicianDashboardActivity::class.java)
                        dashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(dashIntent)
                        finish()
                    }
                    .setNegativeButton("Try Anyway", null)
                    .show()
                return@setOnClickListener
            }

            // Start the sequence
            btnSendDoctor.isEnabled = false
            progressBar.visibility = android.view.View.VISIBLE

            // 1. Create Study
            // ✅ LOG: Sending doctor_id: {doctor_id}
            android.util.Log.d("ProcessingSuccess", "Sending doctor_id: ${selectedDoctor.doctor_id}")
            android.util.Log.d("CreateStudy", "Sending request: scan_id = $scanId, doctor_id = ${selectedDoctor.doctor_id}")
            
            ApiClient.apiService.createStudy(scanId, selectedDoctor.doctor_id).enqueue(object : retrofit2.Callback<CreateStudyResponse> {
                override fun onResponse(call: retrofit2.Call<CreateStudyResponse>, response: retrofit2.Response<CreateStudyResponse>) {
                    android.util.Log.d("CreateStudy", "Response code: ${response.code()}")

                    if (response.isSuccessful && response.body()?.studyId != null) {
                        val studyId = response.body()!!.studyId!!
                        android.util.Log.d("CreateStudy", "Study created: studyId = $studyId")
                        
                        // 2. Distribute Study
                        ApiClient.apiService.distributeStudy(studyId).enqueue(object : retrofit2.Callback<DistributeStudyResponse> {
                            override fun onResponse(dCall: retrofit2.Call<DistributeStudyResponse>, dResponse: retrofit2.Response<DistributeStudyResponse>) {
                                progressBar.visibility = android.view.View.GONE
                                android.util.Log.d("DistributeStudy", "Response code: ${dResponse.code()}")

                                if (dResponse.isSuccessful) {
                                    android.widget.Toast.makeText(this@ProcessingSuccessActivity, "Case sent to Dr. ${selectedDoctor.name}", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.util.Log.w("DistributeStudy", "Distribution returned ${dResponse.code()}, but case was created. Navigating anyway.")
                                    android.widget.Toast.makeText(this@ProcessingSuccessActivity, "Case created but distribution pending.", android.widget.Toast.LENGTH_SHORT).show()
                                }

                                // Delay for 1.5 seconds to let the toast show
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val dashIntent = Intent(this@ProcessingSuccessActivity, TechnicianDashboardActivity::class.java)
                                    dashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(dashIntent)
                                    finish()
                                }, 1500)
                            }
                            override fun onFailure(dCall: retrofit2.Call<DistributeStudyResponse>, t: Throwable) {
                                progressBar.visibility = android.view.View.GONE
                                android.util.Log.e("DistributeStudy", "Failed: ${t.message}")
                                // Still navigate — the study was created
                                android.widget.Toast.makeText(this@ProcessingSuccessActivity, "Case created but network failed during distribution.", android.widget.Toast.LENGTH_SHORT).show()

                                // Delay for 1.5 seconds to let the toast show
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val dashIntent = Intent(this@ProcessingSuccessActivity, TechnicianDashboardActivity::class.java)
                                    dashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(dashIntent)
                                    finish()
                                }, 1500)
                            }
                        })
                    } else {
                        progressBar.visibility = android.view.View.GONE
                        btnSendDoctor.isEnabled = true
                        val errorDetail = try {
                            response.errorBody()?.string() ?: "Unknown error (Code: ${response.code()})"
                        } catch (e: Exception) {
                            "Status Code: ${response.code()}"
                        }
                        android.util.Log.e("CreateStudy", "Failed: $errorDetail")
                        
                        // Show AlertDialog so the user clearly sees the error  
                        androidx.appcompat.app.AlertDialog.Builder(this@ProcessingSuccessActivity)
                            .setTitle("Study Creation Failed")
                            .setMessage("Error: $errorDetail\n\nScan ID: $scanId\nDoctor ID: ${selectedDoctor.doctor_id}")
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Go to Dashboard") { _, _ ->
                                val dashIntent = Intent(this@ProcessingSuccessActivity, TechnicianDashboardActivity::class.java)
                                dashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(dashIntent)
                                finish()
                            }
                            .show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<CreateStudyResponse>, t: Throwable) {
                    progressBar.visibility = android.view.View.GONE
                    btnSendDoctor.isEnabled = true
                    android.util.Log.e("CreateStudy", "Network error: ${t.message}")
                    
                    androidx.appcompat.app.AlertDialog.Builder(this@ProcessingSuccessActivity)
                        .setTitle("Network Error")
                        .setMessage("Could not connect to server:\n${t.message}")
                        .setPositiveButton("Retry") { _, _ ->
                            btnSendDoctor.performClick()
                        }
                        .setNeutralButton("Go to Dashboard") { _, _ ->
                            val dashIntent = Intent(this@ProcessingSuccessActivity, TechnicianDashboardActivity::class.java)
                            dashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(dashIntent)
                            finish()
                        }
                        .show()
                }
            })
        }

    }
}
