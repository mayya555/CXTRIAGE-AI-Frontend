package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Header Navigation
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Application Links
        findViewById<android.view.View>(R.id.btn_terms).setOnClickListener {
            val intent = Intent(this, TermsPrivacyActivity::class.java)
            intent.putExtra("TYPE", "TERMS")
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.btn_privacy).setOnClickListener {
            val intent = Intent(this, TermsPrivacyActivity::class.java)
            intent.putExtra("TYPE", "PRIVACY")
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.btn_licenses).setOnClickListener {
            // Placeholder for licenses activity or dialog
            android.widget.Toast.makeText(this, "Open Source Licenses", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
