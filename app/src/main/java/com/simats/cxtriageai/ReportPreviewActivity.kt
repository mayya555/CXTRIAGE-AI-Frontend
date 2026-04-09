package com.simats.cxtriageai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportPreviewActivity : AppCompatActivity() {
    private var isFinalized = false
    private var downloadUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_preview)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_preview_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val caseId = intent.getIntExtra("CASE_ID", -1)
        
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId <= 0) {
            Toast.makeText(this, "Invalid doctor session. Please login again.", Toast.LENGTH_LONG).show()
            Log.e("ReportPreview", "Invalid doctor_id: $doctorId")
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        Log.d("ReportPreview", "Finalizing report for doctor_id = $doctorId")

        loadCaseDetails(caseId)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.btn_finalize).setOnClickListener {
            val doctorId = prefs.getInt("doctor_id", -1)
            
            if (caseId != -1 && doctorId != -1) {
                val doctorName = prefs.getString("doctor_name", "Doctor") ?: "Doctor"
                val impression = intent.getStringExtra("DIAGNOSIS") ?: "Pending Assessment"
                val recommendation = intent.getStringExtra("NOTES") ?: "Awaiting follow-up"
                
                val requestMap = mapOf(
                    "doctor_name" to doctorName,
                    "doctor_id" to doctorId.toString(),
                    "impression" to impression,
                    "recommendation" to recommendation
                )

                Log.d("ReportPreview", "Finalizing case $caseId with body: $requestMap")
                ApiClient.apiService.finalizeCase(caseId, requestMap).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ReportPreviewActivity, "Report Signed Successfully", Toast.LENGTH_SHORT).show()
                            val nextIntent = Intent(this@ReportPreviewActivity, ReportSentActivity::class.java)
                            nextIntent.putExtra("CASE_ID", caseId)
                            startActivity(nextIntent)
                            finish()
                        } else {
                            val errorMsg = try { response.errorBody()?.string() ?: "Finalize Failed" } catch (e: Exception) { "Finalize Failed" }
                            Log.e("ReportPreview", "Finalize Failed: ${response.code()} - $errorMsg")
                            Toast.makeText(this@ReportPreviewActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("ReportPreview", "Failure: ${t.message}")
                        Toast.makeText(this@ReportPreviewActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_download).setOnClickListener {
            if (caseId != -1) {
                if (isFinalized && downloadUrl != null) {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(downloadUrl))
                    startActivity(intent)
                } else if (isFinalized) {
                    downloadReport(caseId) // Fallback to raw download
                } else {
                    Toast.makeText(this, "Report must be finalized before download", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun downloadReport(caseId: Int) {
        Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show()
        
        ApiClient.apiService.downloadPdf(caseId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReportPreviewActivity, "Report Downloaded (Simulated)", Toast.LENGTH_LONG).show()
                    Log.d("ReportPreview", "PDF Downloaded successfully for case $caseId")
                } else {
                    Toast.makeText(this@ReportPreviewActivity, "Download Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ReportPreviewActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCaseDetails(caseId: Int) {
        if (caseId <= 0) {
            Toast.makeText(this, "Invalid Case ID", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.apiService.getReportPreview(caseId).enqueue(object : Callback<ReportPreviewResponse> {
            override fun onResponse(call: Call<ReportPreviewResponse>, response: Response<ReportPreviewResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    bindReportPreviewData(data)
                } else {
                    val rawMsg = try { response.errorBody()?.string() ?: "Unknown error" } catch (e: Exception) { "Unknown error" }
                    Log.e("ReportPreview", "Data Load Error: ${response.code()} - $rawMsg")
                    Toast.makeText(this@ReportPreviewActivity, "Status: ${response.code()} ($rawMsg)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReportPreviewResponse>, t: Throwable) {
                Log.e("ReportPreview", "Failure loading case: ${t.message}")
                // Handle the case where the server returns a string starting with "{" causing GSON errors
                val msg = t.message ?: "Network error"
                if (msg.contains("Expected BEGIN_OBJECT")) {
                    Toast.makeText(this@ReportPreviewActivity, "Report not yet available for preview", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ReportPreviewActivity, "Network error: $msg", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun bindReportPreviewData(data: ReportPreviewResponse) {
        findViewById<TextView>(R.id.tv_patient_info).text = data.patient ?: "Unknown Patient"
        findViewById<TextView>(R.id.tv_findings_content).text = data.findings ?: "No findings available."
        findViewById<TextView>(R.id.tv_impression_content).text = data.impression ?: "Awaiting clinical impression"
        
        this.isFinalized = data.finalized == true
        
        val signedBy = data.signedBy ?: "Awaiting Signature"
        findViewById<TextView>(R.id.tv_signature_text).text = if (isFinalized) "Signed, $signedBy" else "Awaiting Signature"
        
        // Update Finalize button state (optional: disable if already finalized)
        if (isFinalized) {
            findViewById<TextView>(R.id.btn_finalize).apply {
                text = "Report Finalized"
                isEnabled = false
                alpha = 0.5f
            }
        }
        
        if (!data.reportId.isNullOrEmpty()) {
            findViewById<TextView>(R.id.tv_report_id).text = "#${data.reportId}"
        }
    }

    override fun onResume() {
        super.onResume()
        val caseId = intent.getIntExtra("CASE_ID", -1)
        if (caseId != -1) loadCaseDetails(caseId)
    }
}
