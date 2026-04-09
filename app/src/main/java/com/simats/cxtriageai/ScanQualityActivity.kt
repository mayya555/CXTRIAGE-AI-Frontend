package com.simats.cxtriageai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge

class ScanQualityActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null   // ✅ IMPORTANT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_quality)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scan_quality_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container)
                .setPadding(0, systemBars.top, 0, 0)
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ GET FILE URI FROM PREVIOUS SCREEN
        val fileUriString = intent.getStringExtra("FILE_URI")
        if (fileUriString != null) {
            selectedImageUri = Uri.parse(fileUriString)
        }

        findViewById<android.view.View>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<android.view.View>(R.id.btn_retake).setOnClickListener {
            val scanId = intent.getIntExtra("SCAN_ID", -1)
            if (scanId != -1) {
                ApiClient.apiService.retakeScan(scanId)
                    .enqueue(object : retrofit2.Callback<ActionScanResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<ActionScanResponse>,
                            response: retrofit2.Response<ActionScanResponse>
                        ) {
                            finish()
                        }

                        override fun onFailure(
                            call: retrofit2.Call<ActionScanResponse>,
                            t: Throwable
                        ) {
                            finish()
                        }
                    })
            } else {
                finish()
            }
        }

        findViewById<android.view.View>(R.id.btn_accept).setOnClickListener {

            if (selectedImageUri == null) {
                Toast.makeText(this, "Image missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val scanId = intent.getIntExtra("SCAN_ID", -1)

            if (scanId != -1) {
                ApiClient.apiService.acceptScan(scanId)
                    .enqueue(object : retrofit2.Callback<ActionScanResponse> {

                        override fun onResponse(
                            call: retrofit2.Call<ActionScanResponse>,
                            response: retrofit2.Response<ActionScanResponse>
                        ) {
                            if (response.isSuccessful) {

                                val intentProcessing = Intent(
                                    this@ScanQualityActivity,
                                    ProcessingStudyActivity::class.java
                                )

                                // ✅ Forward old data
                                intent.extras?.let { intentProcessing.putExtras(it) }

                                // 🔥 CRITICAL FIX — PASS FILE
                                intentProcessing.putExtra(
                                    "FILE_URI",
                                    selectedImageUri.toString()
                                )

                                startActivity(intentProcessing)
                                finish()

                            } else {
                                Toast.makeText(
                                    this@ScanQualityActivity,
                                    "Failed to accept scan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: retrofit2.Call<ActionScanResponse>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@ScanQualityActivity,
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                val intentProcessing = Intent(
                    this@ScanQualityActivity,
                    ProcessingStudyActivity::class.java
                )

                intent.extras?.let { intentProcessing.putExtras(it) }

                // 🔥 CRITICAL FIX
                intentProcessing.putExtra(
                    "FILE_URI",
                    selectedImageUri.toString()
                )

                startActivity(intentProcessing)
                finish()
            }
        }
    }
}