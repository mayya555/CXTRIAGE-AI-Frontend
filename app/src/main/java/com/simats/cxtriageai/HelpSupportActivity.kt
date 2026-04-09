package com.simats.cxtriageai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HelpSupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help_support)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.help_support_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, 0) // Padding handled by NestedScrollView content
            insets
        }

        // Header Navigation
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Support Actions
        findViewById<AppCompatButton>(R.id.btn_start_chat).setOnClickListener {
            Toast.makeText(this, "Connecting to Support Agent...", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.btn_call_it).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+1234567890")
            }
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.btn_email).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@cxrtai.com")
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - CXRT AI")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        // Common Topics
        findViewById<android.view.View>(R.id.topic_scanner).setOnClickListener {
            Toast.makeText(this, "Troubleshooting Scanner Connection...", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.topic_patient_id).setOnClickListener {
            Toast.makeText(this, "Patient ID Retrieval Guide...", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.topic_upload).setOnClickListener {
            Toast.makeText(this, "Image Upload Troubleshooting...", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.topic_scheduling).setOnClickListener {
            Toast.makeText(this, "Shift Management Guide...", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.topic_password).setOnClickListener {
            Toast.makeText(this, "Password Reset Instructions...", Toast.LENGTH_SHORT).show()
        }
    }
}
