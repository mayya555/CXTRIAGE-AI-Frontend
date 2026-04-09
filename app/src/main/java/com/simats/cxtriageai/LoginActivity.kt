package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.simats.cxtriageai.ApiClient
import com.simats.cxtriageai.LoginRequest
import com.simats.cxtriageai.LoginResponse
import com.google.gson.Gson
import android.widget.Toast
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val currentRole = intent.getStringExtra("ROLE") ?: "Doctor"
        updateUIForRole(currentRole)

        findViewById<TextView>(R.id.tv_create_account).setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            intent.putExtra("ROLE", currentRole)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_create_technician_account).setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("USER_ROLE", currentRole)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val etUsername = findViewById<EditText>(R.id.et_username)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)

        btnSignIn.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = findViewById<EditText>(R.id.et_password).text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                etUsername.error = "Please enter a valid email address"
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(this, "Invalid Password: Must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one symbol.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnSignIn.isEnabled = false
            btnSignIn.text = "Signing In..."

            if (currentRole == "Technician") {
                val request = TechnicianLoginRequest(username, password)
                ApiClient.apiService.technicianLogin(request)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Secure Login"

                            if (response.isSuccessful && response.body() != null) {
                                val body = response.body()!!
                                Toast.makeText(this@LoginActivity, body.message, Toast.LENGTH_SHORT).show()

                                val technician = body.technician
                                val techId = technician?.id
                                val techName = technician?.name ?: ""

                                if (techId != null && techId != -1) {
                                    SessionManager.clearUserData(this@LoginActivity)
                                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    prefs.edit().apply {
                                        putInt("technician_id", techId)
                                        putString("technician_name", techName)
                                        putString("technician_email", username)
                                        putString("user_role", "technician")
                                        apply()
                                    }
                                    startActivity(Intent(this@LoginActivity, TechnicianDashboardActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Login failed: No Technician ID", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(this@LoginActivity, "Invalid Technician Credentials", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Secure Login"
                            Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                val request = DoctorLoginRequest(username, password)
                ApiClient.apiService.doctorLogin(request)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Sign In"

                            if (response.isSuccessful && response.body() != null) {
                                val body = response.body()!!
                                val rawJson = Gson().toJson(body)
                                android.util.Log.d("LOGIN_DEBUG", "Success Response Raw: $rawJson")
                                Toast.makeText(this@LoginActivity, body.message, Toast.LENGTH_SHORT).show()

                                // Use top-level fields from LoginResponse
                                val doctor = body.doctor
                                val doctorId = body.doctor?.doctor_id
                                val doctorName = doctor?.name ?: ""

                                if (doctorId != null && doctorId > 0) {
                                    SessionManager.clearUserData(this@LoginActivity)
                                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    prefs.edit().apply {
                                        putInt("doctor_id", doctorId)
                                        putString("doctor_name", doctorName)
                                        putString("doctor_email", username)
                                        putString("user_role", "doctor")
                                        apply()
                                    }
                                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Login failed: Invalid Doctor ID ($doctorId) from server", Toast.LENGTH_LONG).show()
                                    Log.e("LOGIN_DEBUG", "Invalid doctor_id received: $doctorId")
                                }
                            } else {
                                val errorBodyString = response.errorBody()?.string()
                                val errorDetail = try {
                                    if (errorBodyString != null && errorBodyString.contains("detail")) {
                                        val gson = Gson()
                                        val errorJson = gson.fromJson(errorBodyString, Map::class.java)
                                        errorJson["detail"]?.toString()
                                    } else {
                                        "Invalid Doctor Credentials"
                                    }
                                } catch (e: Exception) {
                                    "Error: ${response.code()}"
                                }
                                Toast.makeText(this@LoginActivity, errorDetail, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Sign In"
                            Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }
    }

    private fun updateUIForRole(role: String) {
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        val loginIcon = findViewById<ImageView>(R.id.iv_login_icon)
        val tvWelcome = findViewById<TextView>(R.id.tv_welcome)
        val tvInstruction = findViewById<TextView>(R.id.tv_instruction)
        val tvUsernameLabel = findViewById<TextView>(R.id.tv_username_label)
        val tvPasswordLabel = findViewById<TextView>(R.id.tv_password_label)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val llDoctorRegister = findViewById<android.view.View>(R.id.ll_doctor_register)
        val llTechnicianRegister = findViewById<android.view.View>(R.id.ll_technician_register)

        if (role == "Technician") {
            headerTitle.text = "Technician Login"
            loginIcon.setImageResource(R.drawable.ic_fullscreen)
            loginIcon.setBackgroundResource(R.drawable.bg_button_blue)
            tvWelcome.text = "Welcome Back"
            tvInstruction.text = "Please sign in to continue"
            tvUsernameLabel.text = "EMAIL ID"
            tvPasswordLabel.text = "PASSWORD"
            etUsername.hint = "tech.mike@cxtriage.ai"
            btnSignIn.text = "Secure Login"
            llDoctorRegister.visibility = android.view.View.GONE
            llTechnicianRegister.visibility = android.view.View.VISIBLE
        } else {
            headerTitle.text = "Doctor Login"
            loginIcon.setImageResource(R.drawable.ic_pulse)
            loginIcon.setBackgroundResource(R.drawable.bg_circle_blue)
            tvUsernameLabel.text = "HOSPITAL EMAIL"
            tvPasswordLabel.text = "PASSWORD"
            etUsername.hint = "Ex: doctor.user@hospital.com"
            btnSignIn.text = "Sign In"
            llDoctorRegister.visibility = android.view.View.VISIBLE
            llTechnicianRegister.visibility = android.view.View.GONE
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
