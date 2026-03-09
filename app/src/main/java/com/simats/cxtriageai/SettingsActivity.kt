package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        userRole = intent.getStringExtra("ROLE")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Initialize Listeners
        findViewById<android.view.View>(R.id.row_appearance).setOnClickListener {
            startActivity(Intent(this, ThemeActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_sign_out).setOnClickListener {
            showSignOutDialog()
        }

        setupBottomNavigation()
    }


    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        
        // Dynamically load menu based on role
        when (userRole) {
            "Doctor", "Radiologist" -> {
                bottomNav.menu.clear()
                bottomNav.inflateMenu(R.menu.radiologist_bottom_nav)
            }
            else -> {
                // Default to technician menu (already in XML or re-inflate)
                bottomNav.menu.clear()
                bottomNav.inflateMenu(R.menu.technician_bottom_nav)
            }
        }

        // Handle navigation clicks based on role
        bottomNav.selectedItemId = R.id.navigation_settings // Standard ID for settings in tech/base
        // Note: For Radiologist menu, settings isn't actually in the menu usually, 
        // but we keep it highlighted if it exists or handle it gracefully.
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home, R.id.nav_home -> {
                    val target = if (userRole == "Doctor") RadiologistDashboardActivity::class.java else TechnicianDashboardActivity::class.java
                    startActivity(Intent(this, target))
                    finish()
                    true
                }
                R.id.navigation_scan -> {
                    startActivity(Intent(this, RegistrationActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_history, R.id.nav_history -> {
                    startActivity(Intent(this, PatientHistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cases -> {
                    startActivity(Intent(this, CaseQueueActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_alerts -> {
                    startActivity(Intent(this, AlertsActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }
    }

    private fun showSignOutDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_sign_out)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnLogout = dialog.findViewById<android.view.View>(R.id.btn_confirm_logout)
        val btnCancel = dialog.findViewById<android.view.View>(R.id.btn_cancel_logout)

        btnLogout.setOnClickListener {
            dialog.dismiss()
            logout()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
