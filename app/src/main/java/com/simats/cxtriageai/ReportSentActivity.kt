package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.cxtriageai.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportSentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_sent)

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_sent_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔥 FIXED: Use the singleton apiService from ApiClient
        val api = ApiClient.apiService

        // 🔥 Get caseId (Check both common variations)
        var caseId = intent.getIntExtra("CASE_ID", -1)
        if (caseId == -1) {
            caseId = intent.getIntExtra("case_id", -1)
        }

        // 🔥 UI references - FIXED: Matching IDs in activity_report_sent.xml
        val fileNameText = findViewById<TextView>(R.id.tv_filename)
        val fileSizeText = findViewById<TextView>(R.id.tv_filesize)

        // 🔥 Fetch REAL data
        if (caseId != -1) {
            // 🔥 FIXED: use getReportSentDetails to match renamed method in ApiService.kt
            api.getReportSentDetails(caseId).enqueue(object : Callback<ReportResponse> {

                override fun onResponse(
                    call: Call<ReportResponse>,
                    response: Response<ReportResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        // ✅ Real file name
                        fileNameText?.text = data.file_name

                        // ✅ Convert bytes → MB
                        val sizeMB = data.file_size_bytes / (1024.0 * 1024.0)

                        // ✅ Real size + time
                        fileSizeText?.text = getString(R.string.report_file_size_format, sizeMB, data.generated_at)

                        // ✅ Setup Download & Share
                        findViewById<android.view.View>(R.id.btn_download).setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(data.download_url))
                            startActivity(intent)
                        }

                        findViewById<android.view.View>(R.id.btn_share).setOnClickListener {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Medical Report: ${data.file_name}")
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "View report here: ${data.download_url}")
                            startActivity(Intent.createChooser(shareIntent, "Share Report"))
                        }

                    } else {
                        Toast.makeText(
                            this@ReportSentActivity,
                            getString(R.string.failed_load_report),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ReportSentActivity,
                        getString(R.string.error_prefix, t.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        } else {
            Toast.makeText(this, getString(R.string.invalid_case_id), Toast.LENGTH_SHORT).show()
            // Default placeholder if case ID is missing
            fileNameText?.text = getString(R.string.default_report_name)
            fileSizeText?.text = getString(R.string.error_loading_details)
        }

        // Dashboard button
        findViewById<android.view.View>(R.id.btn_dashboard).setOnClickListener {
            val role = intent.getStringExtra("ROLE")
            val target = if (role == "Doctor" || role == "Radiologist" || role == null) {
                CaseQueueActivity::class.java
            } else {
                TechnicianDashboardActivity::class.java
            }

            val intentDashboard = Intent(this, target)
            intentDashboard.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intentDashboard)
            finish()
        }
    }
}
