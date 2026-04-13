package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        userRole = intent.getStringExtra("ROLE") ?: getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE).getString("user_role", null)
        if (userRole?.equals("technician", ignoreCase = true) == true) userRole = "Technician"
        if (userRole?.equals("doctor", ignoreCase = true) == true) userRole = "Doctor"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_bg).layoutParams.height = 100.dpToPx() + systemBars.top
            findViewById<android.view.View>(R.id.header_bg).requestLayout()
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        applyRoleBranding()
        loadProfileData()

        // Navigation Listeners
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_header_logout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<ImageView>(R.id.iv_header_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.row_edit_details).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.row_change_photo).setOnClickListener {
            val intent = Intent(this, ProfilePhotoActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }
        
        findViewById<android.view.View>(R.id.btn_change_photo_overlay).setOnClickListener {
            val intent = Intent(this, ProfilePhotoActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
        loadSavedImage()
    }

    private fun applyRoleBranding() {
        val headerBg = findViewById<android.view.View>(R.id.header_bg)
        val roleChip = findViewById<TextView>(R.id.tv_profile_role_chip)
        
        if (userRole == "Doctor" || userRole == "Radiologist") {
            headerBg?.setBackgroundColor(android.graphics.Color.parseColor("#10B981"))
            roleChip?.text = "Doctor"
            roleChip?.setTextColor(android.graphics.Color.parseColor("#10B981"))
            roleChip?.setBackgroundResource(R.drawable.bg_badge_light_green)
        } else {
            // Default Technician branding
            headerBg?.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green))
            roleChip?.text = "Technician"
            roleChip?.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green))
            roleChip?.setBackgroundResource(R.drawable.bg_badge_light_green)
        }
    }

    private fun loadSavedImage() {
        val uriStr = getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
            .getString("profile_image_uri", null)
        val imageView = findViewById<ImageView>(R.id.iv_profile_img)
        
        if (uriStr != null) {
            try {
                imageView.setImageURI(android.net.Uri.parse(uriStr))
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                // SecurityException or missing file - will be handled by loadProfileData() fallback
                imageView.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        
        if (userRole == "Doctor") {
            val doctorEmail = prefs.getString("doctor_email", null)
            if (doctorEmail == null) {
                // Fallback for Doctor UI testing
                findViewById<TextView>(R.id.tv_profile_name).text = "Dr. Sarah Bennett"
                findViewById<TextView>(R.id.tv_profile_email).text = "sarah.bennett@hospital.org"
                return
            }

            ApiClient.apiService.getDoctorProfile(doctorEmail).enqueue(object : Callback<DoctorProfileResponse> {
                override fun onResponse(call: Call<DoctorProfileResponse>, response: Response<DoctorProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        findViewById<TextView>(R.id.tv_profile_name).text = profile.fullName
                        findViewById<TextView>(R.id.tv_profile_email).text = profile.email
                        
                        if (!profile.profilePhotoUrl.isNullOrEmpty()) {
                            val fullUrl = "${ApiClient.GET_STATIC_URL}${profile.profilePhotoUrl}"
                            loadProfilePhotoFromServer(fullUrl)
                        }
                    }
                }

                override fun onFailure(call: Call<DoctorProfileResponse>, t: Throwable) {
                    android.util.Log.e("Profile", "Network Error: ${t.message}")
                }
            })
        } else {
            val technicianEmail = prefs.getString("technician_email", null)
            if (technicianEmail == null) {
                // Fallback for Technician UI testing
                findViewById<TextView>(R.id.tv_profile_name).text = "Technician User"
                findViewById<TextView>(R.id.tv_profile_email).text = "tech@hospital.org"
                return
            }

            ApiClient.apiService.getTechnicianProfile(technicianEmail).enqueue(object : Callback<TechnicianProfileResponse> {
                override fun onResponse(call: Call<TechnicianProfileResponse>, response: Response<TechnicianProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        findViewById<TextView>(R.id.tv_profile_name).text = profile.fullName
                        findViewById<TextView>(R.id.tv_profile_email).text = profile.email
                        
                        if (!profile.profilePhotoUrl.isNullOrEmpty()) {
                            val fullUrl = "${ApiClient.GET_STATIC_URL}${profile.profilePhotoUrl}"
                            loadProfilePhotoFromServer(fullUrl)
                        }
                    }
                }

                override fun onFailure(call: Call<TechnicianProfileResponse>, t: Throwable) {
                    android.util.Log.e("Profile", "Network Error: ${t.message}")
                }
            })
        }
    }

    private fun loadProfilePhotoFromServer(url: String) {
        val imageView = findViewById<ImageView>(R.id.iv_profile_img)
        
        // Simple thread-based loading since we don't have Glide/Picasso easily available
        Thread {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } catch (e: Exception) {
                android.util.Log.e("Profile", "Failed to load image: ${e.message}")
            }
        }.start()
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.view.View>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            // Clear all session data
            SessionManager.clearUserData(this@ProfileActivity)
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.view.View>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
