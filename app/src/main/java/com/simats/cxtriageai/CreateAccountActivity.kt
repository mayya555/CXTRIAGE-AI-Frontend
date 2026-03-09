package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
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

        val initialRole = intent.getStringExtra("ROLE") ?: "Doctor"
        etRole.setText(initialRole)
        tvHeaderTitle.text = "Create $initialRole Account"

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
            
            if (password != confirmPassword) {
                 Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }
            
            val roleFromIntent = intent.getStringExtra("ROLE") ?: "Doctor"
            
            // Call API
            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating Account..."

            val callback = object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Create Account"

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@CreateAccountActivity, "Account created successfully!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@CreateAccountActivity, RegistrationSuccessActivity::class.java)
                        intent.putExtra("ROLE", roleFromIntent)
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

            if (roleFromIntent == "Technician") {
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
                    employeeId = phone,
                    roleRequested = role.lowercase(),
                    password = password,
                    confirmPassword = confirmPassword
                )
                ApiClient.apiService.registerDoctor(request).enqueue(callback)
            }
        }
    }
}
