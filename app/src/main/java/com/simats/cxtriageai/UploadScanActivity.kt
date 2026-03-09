package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UploadScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_scan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upload_scan_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // Get Patient Data
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "John Doe"
        val patientInfo = intent.getStringExtra("PATIENT_MRN") ?: "P-1024"
        
        findViewById<TextView>(R.id.tv_patient_name).text = patientName
        findViewById<TextView>(R.id.tv_patient_id).text = "$patientInfo • Chest PA View"

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<android.view.View>(R.id.btn_take_photo).setOnClickListener {
            // Simulate Camera
            startActivity(Intent(this, ScannerPreviewActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_browse).setOnClickListener {
            // Simulate Gallery
            val intentToReview = Intent(this, ScanQualityActivity::class.java)
            startActivity(intentToReview)
        }

        findViewById<AppCompatButton>(R.id.btn_proceed).setOnClickListener {
            val intentToReview = Intent(this, ScanQualityActivity::class.java)
            startActivity(intentToReview)
        }
    }
}
