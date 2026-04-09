package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.graphics.drawable.Animatable

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            insets
        }

        // Start logo animation
        val logoImageView = findViewById<ImageView>(R.id.iv_logo)
        val drawable = logoImageView.drawable
        if (drawable is Animatable) {
            drawable.start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // Bypass Terms and Conditions as per user request
            val appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            if (!appPrefs.getBoolean("terms_accepted", false)) {
                appPrefs.edit().putBoolean("terms_accepted", true).apply()
            }

            // Navigate to OnboardingActivity which leads to the login flow
            // Removed auto-login check to ensure the app opens to the login process as requested
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}