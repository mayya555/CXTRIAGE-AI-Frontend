package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reset_password_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Header Navigation
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Send Verification Code Action
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_send_code).setOnClickListener {
            val email = findViewById<android.widget.EditText>(R.id.et_email).text.toString().trim()
            val userRole = intent.getStringExtra("USER_ROLE") ?: "Doctor"
            
            if (email.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    val request = ForgotPasswordRequest(email)
                    
                    val call = if (userRole == "Technician") {
                        ApiClient.apiService.technicianForgotPassword(request)
                    } else {
                        ApiClient.apiService.forgotPassword(request)
                    }
                    
                    call.enqueue(object : retrofit2.Callback<ForgotPasswordResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<ForgotPasswordResponse>,
                            response: retrofit2.Response<ForgotPasswordResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ResetPasswordActivity, "Verification code sent to $email", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@ResetPasswordActivity, OTPVerificationActivity::class.java)
                                intent.putExtra("EMAIL", email)
                                intent.putExtra("USER_ROLE", userRole)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@ResetPasswordActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<ForgotPasswordResponse>, t: Throwable) {
                            Toast.makeText(this@ResetPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
