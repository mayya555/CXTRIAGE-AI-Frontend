package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPasswordActivity : AppCompatActivity() {

    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.new_password_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val ivToggleNew = findViewById<ImageView>(R.id.iv_toggle_new)
        val ivToggleConfirm = findViewById<ImageView>(R.id.iv_toggle_confirm)

        ivToggleNew.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            if (isNewPasswordVisible) {
                etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleNew.setImageResource(R.drawable.ic_visibility) // In a real app, use visibility_off
            } else {
                etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleNew.setImageResource(R.drawable.ic_visibility)
            }
            etNewPassword.setSelection(etNewPassword.text.length)
        }

        ivToggleConfirm.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleConfirm.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleConfirm.setImageResource(R.drawable.ic_visibility)
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_reset_password).setOnClickListener {
            val newPass = etNewPassword.text.toString()
            val confirmPass = etConfirmPassword.text.toString()
            val email = intent.getStringExtra("EMAIL") ?: ""

            if (newPass.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Email error, please go back.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userRole = intent.getStringExtra("USER_ROLE") ?: "Doctor"
            val request = ResetPasswordRequest(email, newPass, confirmPass)
            
            val call = if (userRole == "Technician") {
                ApiClient.apiService.technicianResetPassword(request)
            } else {
                ApiClient.apiService.resetPassword(request)
            }
            
            call.enqueue(object : retrofit2.Callback<ResetPasswordResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ResetPasswordResponse>,
                    response: retrofit2.Response<ResetPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@NewPasswordActivity, "Password Reset Successful!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to Success Screen
                        val intent = Intent(this@NewPasswordActivity, PasswordResetSuccessActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Failed to reset password"
                        Toast.makeText(this@NewPasswordActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResetPasswordResponse>, t: Throwable) {
                    Toast.makeText(this@NewPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
