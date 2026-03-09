package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CaseQueueActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_case_queue)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.case_queue_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Back Navigation
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Bottom Navigation
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_cases
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, RadiologistDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_cases -> true
                R.id.nav_alerts -> {
                    startActivity(Intent(this, AlertsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    // startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Card Clicks - handled by Adapter in a real app, but for now we'll just populate the list
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_case_queue)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val caseList = listOf(
            QueueItem("SC-5501", "2023-10-24 10:30 AM", "Patient Name", "High", "AI: Pneumonia Detected", true),
            QueueItem("SC-5502", "2023-10-24 11:15 AM", "Patient Name", "Normal", "AI: Normal", false),
            QueueItem("SC-5503", "2023-10-24 11:45 AM", "Patient Name", "Normal", "AI: Normal", false),
            QueueItem("SC-5504", "2023-10-24 12:00 PM", "Patient Name", "High", "AI: Pleural Effusion", true)
        )

        val adapter = CaseQueueAdapter(caseList)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
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
