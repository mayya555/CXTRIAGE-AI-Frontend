package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
             onBackPressedDispatcher.onBackPressed()
        }

        setupBottomNavigation()

        val etName = findViewById<EditText>(R.id.et_patient_name)
        val etDob = findViewById<EditText>(R.id.et_dob)
        val etGender = findViewById<EditText>(R.id.et_gender)
        val etMrn = findViewById<EditText>(R.id.et_mrn)
        val etReason = findViewById<EditText>(R.id.et_reason)
        val btnRegister = findViewById<Button>(R.id.btn_register_patient)

        // Rule 1: InputFilter to allow only alphabets and spaces
        val alphabetFilter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Character.isLetter(source[i]) && source[i] != ' ') {
                    return@InputFilter ""
                }
            }
            null
        }
        etName.filters = arrayOf(alphabetFilter)

        // Real-time error reporting
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.any { it.isDigit() }) {
                    etName.error = "Numbers are not allowed"
                } else if (input.isNotEmpty() && !input.matches("^[a-zA-Z\\s]*$".toRegex())) {
                    etName.error = "Only alphabets are allowed"
                } else {
                    etName.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etDob.setOnClickListener {
            showDatePicker(etDob)
        }

        etGender.setOnClickListener {
            val popupMenu = androidx.appcompat.widget.PopupMenu(this, it)
            popupMenu.menu.add("Male")
            popupMenu.menu.add("Female")
            popupMenu.menu.add("Other")

            popupMenu.setOnMenuItemClickListener { item ->
                etGender.setText(item.title)
                true
            }
            popupMenu.show()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val mrn = etMrn.text.toString().trim()
            val genderDisp = etGender.text.toString().trim()
            val dob = etDob.tag?.toString() ?: "" // Use the tag we store in showDatePicker

            if (name.isEmpty() || mrn.isEmpty() || genderDisp.isEmpty() || dob.isEmpty()) {
                android.widget.Toast.makeText(this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Rule 2: Regex validation for only alphabets
            val nameRegex = "^[a-zA-Z\\s]+$".toRegex()
            if (!name.matches(nameRegex)) {
                etName.error = "Name should only contain alphabets"
                return@setOnClickListener
            }

            // Map gender to lowercase for API compatibility
            val gender = genderDisp.lowercase()

            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val technicianId = prefs.getInt("technician_id", -1)

            val request = CreatePatientRequest(
                fullName = name,
                mrn = mrn,
                dateOfBirth = dob,
                gender = gender,
                reasonForXray = etReason.text.toString(),
                height = null, // Vitals initially null
                weight = null,
                bloodType = null,
                technicianId = technicianId
            )
            
            // Show loading state
            btnRegister.isEnabled = false
            btnRegister.text = "Registering..."

            ApiClient.apiService.createPatient(request).enqueue(object : retrofit2.Callback<CreatePatientResponse> {
                override fun onResponse(call: retrofit2.Call<CreatePatientResponse>, response: retrofit2.Response<CreatePatientResponse>) {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Register Patient"
                    
                    if (response.isSuccessful && response.body() != null) {
                        val patientResponse = response.body()!!
                        val successIntent = Intent(this@RegistrationActivity, PatientRegistrationSuccessActivity::class.java)
                        successIntent.putExtra("PATIENT_ID", patientResponse.patientId)
                        successIntent.putExtra("PATIENT_NAME", name)
                        successIntent.putExtra("PATIENT_DOB", dob)
                        successIntent.putExtra("PATIENT_MRN", mrn)
                        successIntent.putExtra("PATIENT_GENDER", genderDisp)
                        startActivity(successIntent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("Registration", "Error ${response.code()}: $errorBody")
                        android.widget.Toast.makeText(this@RegistrationActivity, "Registration failed: ${response.code()} - $errorBody", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<CreatePatientResponse>, t: Throwable) {
                    Toast.makeText(this@RegistrationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupBottomNavigation() {
        findViewById<android.view.View>(R.id.btn_nav_home).setOnClickListener {
            startActivity(Intent(this, TechnicianDashboardActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.btn_nav_scan).setOnClickListener {
            // Already here
        }

        findViewById<android.view.View>(R.id.btn_nav_history).setOnClickListener {
            startActivity(Intent(this, ScanHistoryActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.btn_nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
            finish()
        }
    }

    private fun showDatePicker(editText: android.widget.EditText) {
        val calendar = java.util.Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                editText.setText(selectedDate)
                editText.tag = selectedDate // Store for API request
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
