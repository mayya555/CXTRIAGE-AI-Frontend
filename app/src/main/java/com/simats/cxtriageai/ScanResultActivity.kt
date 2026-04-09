package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val caseId = intent.getIntExtra("CASE_ID", -1)
        Log.d("DEBUG", "STEP2 caseId: $caseId")

        val acceptBtn = findViewById<AppCompatButton>(R.id.btn_accept_case)
        val generateBtn = findViewById<AppCompatButton>(R.id.btn_generate_report)
        val backBtn = findViewById<AppCompatButton>(R.id.btn_back_dashboard)

        // ✅ ACCEPT CASE
        acceptBtn.setOnClickListener {
            if (caseId == -1) {
                Toast.makeText(this, "Case ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.apiService.acceptCase(caseId, mapOf("doctor_name" to "Dr. Test"))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ScanResultActivity, "Case Accepted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ScanResultActivity, "Accept failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@ScanResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // ✅ GENERATE REPORT
        generateBtn.setOnClickListener {
            if (caseId == -1) {
                Toast.makeText(this, "Case ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // STEP 1: FINALIZE
            ApiClient.apiService.finalizeCase(
                caseId,
                mapOf(
                    "doctor_name" to "Dr. Test",
                    "impression" to "Final diagnosis",
                    "recommendation" to "Follow-up"
                )
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("DEBUG", "Finalized")

                        // STEP 2: DOWNLOAD REPORT
                        ApiClient.apiService.downloadReport(caseId)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@ScanResultActivity, "Report Generated Successfully", Toast.LENGTH_SHORT).show()
                                        
                                        // Navigate back to Dashboard based on role
                                        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                        val role = prefs.getString("user_role", "technician")
                                        val intent = if (role == "doctor" || role == "surgeon" || role == "radiologist") {
                                            Intent(this@ScanResultActivity, DashboardActivity::class.java)
                                        } else {
                                            Intent(this@ScanResultActivity, TechnicianDashboardActivity::class.java)
                                        }
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this@ScanResultActivity, "Report failed", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(this@ScanResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })

                    } else {
                        Toast.makeText(this@ScanResultActivity, "Finalize failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ScanResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // BACK BUTTON
        backBtn.setOnClickListener {
            val intent = Intent(this, TechnicianDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
