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
import com.simats.cxtriageai.ApiClient
import com.simats.cxtriageai.RegistrationResponse
import com.simats.cxtriageai.DoctorRegisterRequest
import com.simats.cxtriageai.TechnicianRegisterRequest
import com.google.gson.Gson


class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_account_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<ImageView>(R.id.iv_back_arrow).setPadding(0, systemBars.top, 0, 0)
            findViewById<TextView>(R.id.tv_header_title).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<TextView>(R.id.tv_terms_link).setOnClickListener {
            val intent = Intent(this, TermsPrivacyActivity::class.java)
            startActivity(intent)
        }
        
        val etFirstName = findViewById<EditText>(R.id.et_first_name)
        val etLastName = findViewById<EditText>(R.id.et_last_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val etRole = findViewById<EditText>(R.id.et_role)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        val tvHeaderTitle = findViewById<TextView>(R.id.tv_header_title)
        val tvEmailLabel = findViewById<TextView>(R.id.tv_email_label)
        val currentRole = intent.getStringExtra("ROLE") ?: "Doctor"

        fun updateUI(role: String) {
            etRole.setText(role)
            tvHeaderTitle.text = "Create $role Account"
            if (role == "Technician") {
                tvEmailLabel.text = "Email Address"
                etEmail.hint = "jane.doe@example.com"
            } else {
                tvEmailLabel.text = "Hospital Email"
                etEmail.hint = "jane.doe@hospital.org"
            }
        }

        updateUI(currentRole)


        // Rule 1: InputFilter to allow only alphabets and spaces
        val alphabetFilter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Character.isLetter(source[i]) && source[i] != ' ') {
                    return@InputFilter ""
                }
            }
            null
        }
        etFirstName.filters = arrayOf(alphabetFilter)
        etLastName.filters = arrayOf(alphabetFilter)

        // Real-time error reporting
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val et = if (etFirstName.hasFocus()) etFirstName else etLastName
                val input = s.toString()
                if (input.any { it.isDigit() }) {
                    et.error = "Numbers are not allowed"
                } else if (input.isNotEmpty() && !input.matches("^[a-zA-Z\\s]*$".toRegex())) {
                    et.error = "Only alphabets are allowed"
                } else {
                    et.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etFirstName.addTextChangedListener(nameWatcher)
        etLastName.addTextChangedListener(nameWatcher)

        // Password validation logic
        val ivUppercase = findViewById<ImageView>(R.id.iv_validation_uppercase)
        val tvUppercase = findViewById<TextView>(R.id.tv_validation_uppercase)
        val ivLowercase = findViewById<ImageView>(R.id.iv_validation_lowercase)
        val tvLowercase = findViewById<TextView>(R.id.tv_validation_lowercase)
        val ivNumber = findViewById<ImageView>(R.id.iv_validation_number)
        val tvNumber = findViewById<TextView>(R.id.tv_validation_number)
        val ivSymbol = findViewById<ImageView>(R.id.iv_validation_symbol)
        val tvSymbol = findViewById<TextView>(R.id.tv_validation_symbol)
        val ivLength = findViewById<ImageView>(R.id.iv_validation_length)
        val tvLength = findViewById<TextView>(R.id.tv_validation_length)

        fun updateItem(isMet: Boolean, imageView: ImageView, textView: TextView) {
            if (isMet) {
                imageView.setImageResource(R.drawable.ic_check_circle_green)
                imageView.setColorFilter(android.graphics.Color.parseColor("#10B981"))
                textView.setTextColor(android.graphics.Color.parseColor("#10B981"))
            } else {
                imageView.setImageResource(R.drawable.ic_circle_outline)
                imageView.setColorFilter(android.graphics.Color.parseColor("#CBD5E1"))
                textView.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
            }
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s?.toString() ?: ""
                updateItem(password.any { it.isUpperCase() }, ivUppercase, tvUppercase)
                updateItem(password.any { it.isLowerCase() }, ivLowercase, tvLowercase)
                updateItem(password.any { it.isDigit() }, ivNumber, tvNumber)
                updateItem(password.any { !it.isLetterOrDigit() }, ivSymbol, tvSymbol)
                updateItem(password.length >= 8, ivLength, tvLength)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSubmit.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val role = etRole.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || role.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Rule 2: Regex validation for only alphabets
            val nameRegex = "^[a-zA-Z\\s]+$".toRegex()
            if (!firstName.matches(nameRegex)) {
                etFirstName.error = "First name should only contain alphabets"
                return@setOnClickListener
            }
            if (!lastName.matches(nameRegex)) {
                etLastName.error = "Last name should only contain alphabets"
                return@setOnClickListener
            }

            // Rule 3: Email validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email address"
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                 Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(this, "Invalid Password: Must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one symbol.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            // Call API
            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating Account..."

            val callback = object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Create Account"

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        
                        // Clear existing data before saving new session
                        SessionManager.clearUserData(this@CreateAccountActivity)
                        
                        // Save the ID to user_prefs for both roles
                        if (body.id != null) {
                            val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                            val key = if (currentRole == "Technician") "technician_id" else "doctor_id"
                            prefs.edit().apply {
                                putInt(key, body.id)
                                putString("user_role", currentRole.lowercase())
                                apply()
                            }
                        }

                        Toast.makeText(this@CreateAccountActivity, "Account created successfully!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@CreateAccountActivity, RegistrationSuccessActivity::class.java)
                        intent.putExtra("ROLE", currentRole)
                        startActivity(intent)
                        finish()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val detailMessage = if (errorBody != null && errorBody.contains("detail")) {
                                val gson = Gson()
                                val errorObj = gson.fromJson(errorBody, RegistrationResponse::class.java)
                                errorObj.detail ?: "Registration failed"
                            } else {
                                "Error: ${response.code()}"
                            }
                            Toast.makeText(this@CreateAccountActivity, detailMessage, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@CreateAccountActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Create Account"
                    Toast.makeText(this@CreateAccountActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }

            if (currentRole == "Technician") {
                val request = TechnicianRegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = phone,
                    roleRequested = role.lowercase(),
                    password = password,
                    confirmPassword = confirmPassword
                )
                ApiClient.apiService.registerTechnician(request).enqueue(callback)
            } else {
                val request = DoctorRegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    hospitalEmail = email,
                    phoneNumber = phone,
                    roleRequested = role.lowercase(),
                    password = password,
                    confirmPassword = confirmPassword
                )
                ApiClient.apiService.registerDoctor(request).enqueue(callback)
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        val hasLength = password.length >= 8
        return hasUpper && hasLower && hasDigit && hasSymbol && hasLength
    }
}
