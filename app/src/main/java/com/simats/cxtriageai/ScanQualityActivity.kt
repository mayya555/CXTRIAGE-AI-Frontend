package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ScanQualityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_quality)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scan_quality_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<android.view.View>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<android.view.View>(R.id.btn_retake).setOnClickListener {
            // Go back to Upload/Scanner
            finish()
        }

        findViewById<android.view.View>(R.id.btn_accept).setOnClickListener {
            // Proceed to upload/processing
            startActivity(Intent(this, ProcessingStudyActivity::class.java))
        }
    }
}
