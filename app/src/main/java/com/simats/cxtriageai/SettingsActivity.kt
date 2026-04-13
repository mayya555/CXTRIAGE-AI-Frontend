package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        userRole = intent.getStringExtra("ROLE")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_bg).layoutParams.height = 100.dpToPx() + systemBars.top
            findViewById<android.view.View>(R.id.header_bg).requestLayout()
            
            // Apply padding to all navigation versions to handle system bottom bars
            findViewById<android.view.View>(R.id.bottom_nav_technician)?.setPadding(0, 0, 0, systemBars.bottom)
            findViewById<android.view.View>(R.id.bottom_nav_custom)?.setPadding(0, 0, 0, systemBars.bottom)
            
            insets
        }

        setupListeners()
        applyRoleBranding()
        setupBottomNavigation()

        // ✅ MANDATORY SESSION CHECK for Doctors
        if (userRole == "Doctor" || userRole == "Radiologist") {
            val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            val doctorId = prefs.getInt("doctor_id", -1)
            if (doctorId <= 0) {
                android.widget.Toast.makeText(this, "Invalid doctor session. Please login again.", android.widget.Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }
            android.util.Log.d("Settings", "Doctor session active: $doctorId")
        }
    }

    override fun onResume() {
        super.onResume()
        if (userRole == "Doctor") {
            loadDoctorProfile()
            applyRoleBranding()
        } else if (userRole == "Technician" || userRole == null) {
            loadProfileData()
        } else {
            applyRoleBranding()
        }
        loadSavedImage()
    }

    private fun loadDoctorProfile() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val name = prefs.getString("doctor_name", "Doctor")
        val email = prefs.getString("doctor_email", "doctor@hospital.com")
        
        // Assuming IDs from activity_settings.xml or similar
        findViewById<android.widget.TextView>(R.id.tv_profile_name)?.text = name
        findViewById<android.widget.TextView>(R.id.tv_profile_email)?.text = email
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val technicianEmail = prefs.getString("technician_email", null)

        if (technicianEmail == null) {
            // Only show toast if specifically expected
            if (userRole == "Technician") {
                android.util.Log.e("Settings", "Technician email not found in preferences")
            }
            applyRoleBranding()
            return
        }

        ApiClient.apiService.getTechnicianProfile(technicianEmail).enqueue(object : Callback<TechnicianProfileResponse> {
            override fun onResponse(call: Call<TechnicianProfileResponse>, response: Response<TechnicianProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // Cache or handle profile data if needed, but views are removed from this screen
                } else {
                    android.util.Log.e("Settings", "Sync Failed: ${response.code()}")
                    applyRoleBranding()
                }
            }

            override fun onFailure(call: Call<TechnicianProfileResponse>, t: Throwable) {
                android.util.Log.e("Settings", "Network Error: ${t.message}")
                applyRoleBranding()
            }
        })
    }

    private fun loadSavedImage() {
        // Reserved for future use or removal if not needed in new design
    }

    private fun setupListeners() {
        findViewById<android.view.View>(R.id.iv_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<android.view.View>(R.id.iv_header_logout)?.setOnClickListener {
            showSignOutDialog()
        }

        findViewById<android.view.View>(R.id.cv_profile_card)?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.row_theme)?.setOnClickListener {
            startActivity(Intent(this, ThemeActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_notifications)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_privacy)?.setOnClickListener {
            startActivity(Intent(this, PrivacySecurityActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_language)?.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_password)?.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_about)?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<android.view.View>(R.id.row_help)?.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_sign_out)?.setOnClickListener {
            showSignOutDialog()
        }

        findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_biometric)?.setOnCheckedChangeListener { _, _ ->
            // Save preference or implement logic
        }
    }

    private fun applyRoleBranding() {
        val headerBg = findViewById<android.view.View>(R.id.header_bg)
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)

        // Since the profile details are removed from this screen in the new design,
        // we'll just handle the role branding for the header.
        if (userRole == "Doctor" || userRole == "Radiologist") {
            headerBg?.setBackgroundColor(android.graphics.Color.parseColor("#10B981"))
            headerTitle?.text = "Settings"
        } else {
            // Default Technician branding
            headerBg?.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green))
            headerTitle?.text = "Settings"
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavTechnician = findViewById<android.view.View>(R.id.bottom_nav_technician)
        val bottomNavCustom = findViewById<android.widget.LinearLayout>(R.id.bottom_nav_custom)

        if (userRole == "Doctor" || userRole == "Radiologist") {
            bottomNavTechnician?.visibility = View.GONE
            bottomNavCustom?.visibility = View.VISIBLE
            setupCustomNavigationListeners()
        } else {
            bottomNavTechnician?.visibility = View.VISIBLE
            bottomNavCustom?.visibility = View.GONE
            setupTechnicianNavigationListeners()
        }
    }

    private fun setupCustomNavigationListeners() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        findViewById<View>(R.id.nav_cases)?.setOnClickListener {
            val intent = Intent(this, CaseQueueActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        findViewById<View>(R.id.nav_alerts)?.setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        findViewById<View>(R.id.nav_history)?.setOnClickListener {
            val intent = Intent(this, PatientHistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        // nav_settings_custom is already active
    }

    private fun setupTechnicianNavigationListeners() {
        findViewById<View>(R.id.btn_nav_home)?.setOnClickListener {
            startActivity(Intent(this, TechnicianDashboardActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.btn_nav_scan)?.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.btn_nav_history)?.setOnClickListener {
            startActivity(Intent(this, ScanHistoryActivity::class.java))
            finish()
        }
        // btn_nav_settings is already active
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

        btnLogout?.setOnClickListener {
            dialog.dismiss()
            logout()
        }

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logout() {
        // Clear all session data
        SessionManager.clearUserData(this)

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
