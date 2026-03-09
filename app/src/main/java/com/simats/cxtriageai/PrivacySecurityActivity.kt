package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrivacySecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_security)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.privacy_security_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Toggles
        val switchAnalytics = findViewById<SwitchCompat>(R.id.switch_analytics)
        switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Usage analytics enabled" else "Usage analytics disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        val switchStatus = findViewById<SwitchCompat>(R.id.switch_status)
        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Status visibility enabled" else "Status visibility disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        val switchBiometric = findViewById<SwitchCompat>(R.id.switch_biometric)
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Biometric login enabled" else "Biometric login disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
