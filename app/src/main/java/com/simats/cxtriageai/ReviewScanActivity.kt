package com.simats.cxtriageai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReviewScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review_scan)

        val patientName = intent.getStringExtra("PATIENT_NAME")
        val patientId = intent.getIntExtra("PATIENT_ID", -1)
        val fileUriString = intent.getStringExtra("FILE_URI")

        if (fileUriString != null) {
            val uri = Uri.parse(fileUriString)
            findViewById<ImageView>(R.id.iv_scan_image).setImageURI(uri)
        }

        findViewById<TextView>(R.id.tv_scan_details).text = "Patient: $patientName"

        findViewById<Button>(R.id.btn_retake).setOnClickListener {
            finish() // Go back to capture/upload
        }

        findViewById<Button>(R.id.btn_accept).setOnClickListener {
            val intentProcessing = Intent(this, ProcessingStudyActivity::class.java)
            intentProcessing.putExtras(intent) // Keep patient name, ID, and file URI
            intentProcessing.putExtra("SCAN_ID", (100..999).random())
            startActivity(intentProcessing)
        }
    }
}
