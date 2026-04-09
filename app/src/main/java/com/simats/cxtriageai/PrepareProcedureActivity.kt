package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrepareProcedureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prepare_procedure)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.prepare_procedure_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val checkStates = BooleanArray(6) { false }
        val checkViews = arrayOf(
            findViewById<android.view.View>(R.id.cb_1),
            findViewById<android.view.View>(R.id.cb_2),
            findViewById<android.view.View>(R.id.cb_3),
            findViewById<android.view.View>(R.id.cb_4),
            findViewById<android.view.View>(R.id.cb_5),
            findViewById<android.view.View>(R.id.cb_6)
        )
        val cards = arrayOf(
            findViewById<android.view.View>(R.id.card_item_1),
            findViewById<android.view.View>(R.id.card_item_2),
            findViewById<android.view.View>(R.id.card_item_3),
            findViewById<android.view.View>(R.id.card_item_4),
            findViewById<android.view.View>(R.id.card_item_5),
            findViewById<android.view.View>(R.id.card_item_6)
        )

        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                checkStates[index] = !checkStates[index]
                checkViews[index].setBackgroundResource(
                    if (checkStates[index]) R.drawable.bg_checkbox_selected 
                    else R.drawable.bg_checkbox_unselected
                )
            }
        }

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<AppCompatButton>(R.id.btn_start_scan).setOnClickListener {
            if (checkStates.all { it }) {
                val patientId = intent.getIntExtra("PATIENT_ID", -1)
                if (patientId == -1) {
                    Toast.makeText(this, "Error: Missing Patient ID", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val technicianId = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE).getInt("technician_id", -1)
                
                if (technicianId == -1) {
                    Toast.makeText(this, "Error: Technician session expired. Please login again.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    return@setOnClickListener
                }

                // 1. Start the Scan
                ApiClient.apiService.startScan(patientId, technicianId).enqueue(object : retrofit2.Callback<StartScanResponse> {
                    override fun onResponse(call: retrofit2.Call<StartScanResponse>, response: retrofit2.Response<StartScanResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val scanId = response.body()!!.scanId
                            
                            // 2. Save Preparations
                            val prepRequest = ScanPreparationRequest(
                                positionPatient = true,
                                properDistance = true,
                                radiationSafety = true,
                                removeMetal = true,
                                calibrationVerified = true,
                                exposureSettings = true
                            )

                            ApiClient.apiService.saveScanPreparation(scanId, prepRequest).enqueue(object : retrofit2.Callback<ScanPreparationResponse> {
                                override fun onResponse(pCall: retrofit2.Call<ScanPreparationResponse>, pResponse: retrofit2.Response<ScanPreparationResponse>) {
                                    if (pResponse.isSuccessful) {
                                        // 3. Navigate to Capture Method Activity
                                        val intentToCapture = Intent(this@PrepareProcedureActivity, CaptureMethodActivity::class.java)
                                        intentToCapture.putExtra("SCAN_ID", scanId)
                                        intentToCapture.putExtra("PATIENT_NAME", intent.getStringExtra("PATIENT_NAME"))
                                        intentToCapture.putExtra("PATIENT_MRN", intent.getStringExtra("PATIENT_MRN"))
                                        intentToCapture.putExtra("PATIENT_ID", patientId)
                                        startActivity(intentToCapture)
                                    } else {
                                        Toast.makeText(this@PrepareProcedureActivity, "Failed to save preparations", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(pCall: retrofit2.Call<ScanPreparationResponse>, t: Throwable) {
                                    Toast.makeText(this@PrepareProcedureActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                            
                        } else {
                            Toast.makeText(this@PrepareProcedureActivity, "Failed to start scan", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<StartScanResponse>, t: Throwable) {
                        Toast.makeText(this@PrepareProcedureActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            } else {
                Toast.makeText(this, "Please complete all preparation steps first.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
