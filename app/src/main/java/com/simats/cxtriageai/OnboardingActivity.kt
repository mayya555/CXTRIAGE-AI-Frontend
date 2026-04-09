package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val btnGetStarted = findViewById<AppCompatButton>(R.id.btn_get_started)
        btnGetStarted.setOnClickListener {
            navigateToHospitalSelection()
        }

        // Auto-transition after 5 seconds for "page to page automatically" request
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                navigateToHospitalSelection()
            }
        }, 5000)
    }

    private fun navigateToHospitalSelection() {
        val intent = Intent(this, HospitalSelectionActivity::class.java)
        startActivity(intent)
        finish() // Finish onboarding so user doesn't go back to it
    }
}
