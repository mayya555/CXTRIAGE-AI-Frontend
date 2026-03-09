package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReportGenerationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_generation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_generation_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
        
        // Remove padding from root to prevent double padding if header handles it
         ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_generation_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<Button>(R.id.btn_sign_submit).setOnClickListener {
            startActivity(Intent(this, ReportSentActivity::class.java))
            finish()
        }
        
        findViewById<Button>(R.id.btn_save_draft).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
