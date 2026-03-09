package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ScannerPreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner_preview)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scanner_preview_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.top_bar).setPadding(24, systemBars.top + 16, 24, 16)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_close).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.btn_capture).setOnClickListener {
            // Simulate capture and go to Quality Check or Processing
            // Flow: Scanner -> Processing -> Success? 
            // Or Scanner -> Quality Check (Retake/Accept) -> Processing
            // Let's assume generic flow for now: DIRECT TO PROCESSING per previous flow, 
            // OR ideally to ScanQualityActivity if it existed.
            // checking task.md: 30. Scan Quality Validation Screen (ScanQualityActivity) is [ ]
            // So I should navigate there if I implement it, or Processing for now.
            // Let's go to ProcessingStudyActivity for immediate feedback, or create ScanQuality next.
            // User asked for Technician workflow. I will update this to ScanQualityActivity once implemented.
            // For now, let's go to ProcessingStudyActivity to keep flow working.
            startActivity(Intent(this, ScanQualityActivity::class.java))
        }
    }
}
