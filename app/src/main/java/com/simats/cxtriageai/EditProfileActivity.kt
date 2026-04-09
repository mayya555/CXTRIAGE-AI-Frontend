package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        userRole = intent.getStringExtra("ROLE") ?: getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE).getString("user_role", null)
        if (userRole?.equals("technician", ignoreCase = true) == true) userRole = "Technician"
        if (userRole?.equals("doctor", ignoreCase = true) == true) userRole = "Doctor"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        applyRoleBranding()
        loadProfileData()

        val etFullName = findViewById<EditText>(R.id.et_full_name)
        
        // Rule 1: InputFilter to allow only alphabets and spaces
        val alphabetFilter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Character.isLetter(source[i]) && source[i] != ' ') {
                    return@InputFilter ""
                }
            }
            null
        }
        etFullName.filters = arrayOf(alphabetFilter)

        // Real-time error reporting
        etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.any { it.isDigit() }) {
                    etFullName.error = "Numbers are not allowed"
                } else if (input.isNotEmpty() && !input.matches("^[a-zA-Z\\s]*$".toRegex())) {
                    etFullName.error = "Only alphabets are allowed"
                } else {
                    etFullName.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Header Actions
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_header_logout).setOnClickListener {
             logout()
        }

        findViewById<ImageView>(R.id.iv_header_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_save_changes).setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun applyRoleBranding() {
        val headerBg = findViewById<android.view.View>(R.id.header_bg)
        val saveBtn = findViewById<Button>(R.id.btn_save_changes)
        
        if (userRole == "Doctor" || userRole == "Radiologist") {
            headerBg?.setBackgroundColor(android.graphics.Color.parseColor("#10B981"))
            saveBtn?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
            findViewById<EditText>(R.id.et_role).setText("Doctor")
        } else {
            // Default Technician branding
            headerBg?.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green))
            saveBtn?.backgroundTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green))
            findViewById<EditText>(R.id.et_role).setText("Technician")
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        
        if (userRole?.equals("Doctor", ignoreCase = true) == true) {
            val doctorId = prefs.getInt("doctor_id", -1)
            if (doctorId == -1) return

            ApiClient.apiService.getDoctorProfile(doctorId).enqueue(object : Callback<DoctorProfileResponse> {
                override fun onResponse(call: Call<DoctorProfileResponse>, response: Response<DoctorProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        findViewById<EditText>(R.id.et_full_name).setText(profile.fullName)
                        findViewById<EditText>(R.id.et_email).setText(profile.email)
                        findViewById<EditText>(R.id.et_phone_number).setText(profile.phoneNumber ?: "")
                    }
                }

                override fun onFailure(call: Call<DoctorProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Failed to load doctor profile", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            val technicianId = prefs.getInt("technician_id", -1)

            if (technicianId == -1) {
                // Fallback for UI testing
                findViewById<EditText>(R.id.et_full_name).setText("Dr. Sarah Bennett")
                findViewById<EditText>(R.id.et_email).setText("sarah.bennett@hospital.org")
                findViewById<EditText>(R.id.et_phone_number).setText("+1 (555) 234-5678")
                return
            }

            ApiClient.apiService.getTechnicianProfile(technicianId).enqueue(object : Callback<TechnicianProfileResponse> {
                override fun onResponse(call: Call<TechnicianProfileResponse>, response: Response<TechnicianProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        findViewById<EditText>(R.id.et_full_name).setText(profile.fullName)
                        findViewById<EditText>(R.id.et_email).setText(profile.email)
                        findViewById<EditText>(R.id.et_phone_number).setText(profile.phoneNumber ?: "")
                    }
                }

                override fun onFailure(call: Call<TechnicianProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Failed to load technician profile", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveProfileChanges() {
        val name = findViewById<EditText>(R.id.et_full_name).text.toString().trim()
        val email = findViewById<EditText>(R.id.et_email).text.toString().trim()
        val phone = findViewById<EditText>(R.id.et_phone_number).text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Rule: Regex validation for only alphabets
        val nameRegex = "^[a-zA-Z\\s]+$".toRegex()
        if (!name.matches(nameRegex)) {
            findViewById<EditText>(R.id.et_full_name).error = "Name should only contain alphabets"
            return
        }

        val nameParts = name.split(" ", limit = 2)
        val firstName = nameParts.getOrElse(0) { "" }
        val lastName = nameParts.getOrElse(1) { "" }

        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val btnSave = findViewById<Button>(R.id.btn_save_changes)

        if (userRole?.equals("Doctor", ignoreCase = true) == true) {
            val sessionEmail = prefs.getString("doctor_email", "") ?: ""
            val doctorId = prefs.getInt("doctor_id", -1)

            if (email != sessionEmail) {
                Toast.makeText(this, "Email must match your hospital email", Toast.LENGTH_SHORT).show()
                return
            }

            if (doctorId == -1) {
                Toast.makeText(this, "Session invalid. Please login again.", Toast.LENGTH_SHORT).show()
                return
            }

            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            val request = UpdateDoctorProfileRequest(
                id = doctorId,
                doctorId = doctorId,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                hospitalEmail = email
            )

            ApiClient.apiService.updateDoctorProfile(request).enqueue(object : Callback<UpdateDoctorProfileResponse> {
                override fun onResponse(call: Call<UpdateDoctorProfileResponse>, response: Response<UpdateDoctorProfileResponse>) {
                    btnSave.isEnabled = true
                    btnSave.text = "Save Changes"

                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = try {
                            val errorJson = response.errorBody()?.string() ?: ""
                            if (errorJson.contains("detail")) {
                                val gson = com.google.gson.Gson()
                                val errorDetail = gson.fromJson(errorJson, Map::class.java)
                                errorDetail["detail"]?.toString() ?: "Update failed"
                            } else {
                                "Update failed: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            "Update failed: ${response.code()}"
                        }
                        Toast.makeText(this@EditProfileActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UpdateDoctorProfileResponse>, t: Throwable) {
                    btnSave.isEnabled = true
                    btnSave.text = "Save Changes"
                    Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else if (userRole?.equals("Technician", ignoreCase = true) == true) {
            val sessionEmail = prefs.getString("technician_email", "") ?: ""
            
            if (email != sessionEmail) {
                Toast.makeText(this, "Email must match your account email", Toast.LENGTH_SHORT).show()
                return
            }

            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            val request = UpdateTechnicianProfileRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                email = email
            )

            ApiClient.apiService.updateTechnicianProfile(request).enqueue(object : Callback<UpdateTechnicianProfileResponse> {
                override fun onResponse(call: Call<UpdateTechnicianProfileResponse>, response: Response<UpdateTechnicianProfileResponse>) {
                    btnSave.isEnabled = true
                    btnSave.text = "Save Changes"

                    when (response.code()) {
                        200 -> {
                            Toast.makeText(this@EditProfileActivity, "Technician profile updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        422 -> Toast.makeText(this@EditProfileActivity, "Invalid input data (422)", Toast.LENGTH_LONG).show()
                        404 -> Toast.makeText(this@EditProfileActivity, "Technician not found (404)", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(this@EditProfileActivity, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateTechnicianProfileResponse>, t: Throwable) {
                    btnSave.isEnabled = true
                    btnSave.text = "Save Changes"
                    Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Profile update for $userRole is not supported yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
