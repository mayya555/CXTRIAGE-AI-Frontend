package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TermsPrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms_privacy)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.terms_privacy_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val isViewOnly = intent.hasExtra("TYPE")
        val consentLayout = findViewById<android.view.View>(R.id.layout_consent)
        
        if (isViewOnly) {
            consentLayout.visibility = android.view.View.GONE
            val type = intent.getStringExtra("TYPE")
            findViewById<android.widget.TextView>(R.id.tv_header_title).text = 
                if (type == "TERMS") "Terms of Service" else "Privacy Policy"
        }

        findViewById<android.widget.ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val acceptButton = findViewById<Button>(R.id.btn_accept)
        val agreeCheckbox = findViewById<CheckBox>(R.id.cb_agree)
        val boxAgreement = findViewById<android.view.View>(R.id.box_agreement)

        boxAgreement.setOnClickListener {
            agreeCheckbox.isChecked = !agreeCheckbox.isChecked
        }

        acceptButton.setOnClickListener {
            if (agreeCheckbox.isChecked) {
                // Determine destination based on role (default to Radiologist/Doctor)
                val role = intent.getStringExtra("ROLE") ?: "Doctor"
                val targetActivity = if (role == "Technician") {
                    TechnicianDashboardActivity::class.java
                } else {
                    RadiologistDashboardActivity::class.java
                }
                
                val intent = Intent(this, targetActivity)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please accept the terms to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
