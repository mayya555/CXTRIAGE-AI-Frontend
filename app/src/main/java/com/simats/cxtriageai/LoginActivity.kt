package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
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

        val role = intent.getStringExtra("ROLE") ?: "Doctor"
        
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

        findViewById<TextView>(R.id.tv_create_account).setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_create_technician_account).setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("USER_ROLE", role)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_sign_in).setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = findViewById<EditText>(R.id.et_password).text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSignIn.isEnabled = false
            btnSignIn.text = "Signing In..."

            if (role == "Technician") {

                val request = TechnicianLoginRequest(username, password)

                ApiClient.apiService.technicianLogin(request)
                    .enqueue(object : Callback<LoginResponse> {

                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Secure Login"

                            if (response.isSuccessful && response.body() != null) {

                                Toast.makeText(
                                    this@LoginActivity,
                                    response.body()!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this@LoginActivity, TermsPrivacyActivity::class.java)
                                intent.putExtra("ROLE", "Technician")
                                startActivity(intent)
                                finish()

                            } else {

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Invalid Technician Login",
                                    Toast.LENGTH_LONG
                                ).show()

                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Secure Login"

                            Toast.makeText(
                                this@LoginActivity,
                                "Network Error: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    })

            } else {

                val request = mutableMapOf<String, String>()
                request["username"] = username
                request["email"] = username // Try both email and username
                request["password"] = password
                request["role"] = "doctor"

                ApiClient.apiService.login(request)
                    .enqueue(object : Callback<LoginResponse> {

                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Sign In"

                            if (response.isSuccessful && response.body() != null) {

                                Toast.makeText(
                                    this@LoginActivity,
                                    response.body()!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                startActivity(Intent(this@LoginActivity, RadiologistDashboardActivity::class.java))
                                finish()

                            } else {
                                val errorBodyString = response.errorBody()?.string()
                                val errorDetail = try {
                                    if (errorBodyString != null && errorBodyString.contains("detail")) {
                                        val gson = Gson()
                                        val errorJson = gson.fromJson(errorBodyString, Map::class.java)
                                        val detail = errorJson["detail"]
                                        if (detail is List<*>) {
                                            val firstError = detail.firstOrNull() as? Map<*, *>
                                            val loc = firstError?.get("loc") as? List<*>
                                            val msg = firstError?.get("msg")?.toString()
                                            if (loc != null && loc.size > 1) {
                                                "Required: ${loc[1]} ($msg)"
                                            } else {
                                                msg
                                            }
                                        } else {
                                            detail?.toString()
                                        }
                                    } else if (response.code() == 404) {
                                         "Login Endpoint Not Found (404)"
                                    } else if (response.code() == 422) {
                                         "Schema Error (422): $errorBodyString"
                                    } else {
                                        "Invalid Doctor Credentials (${response.code()})"
                                    }
                                } catch (e: Exception) {
                                    "Error: ${response.code()}"
                                }

                                Toast.makeText(
                                    this@LoginActivity,
                                    errorDetail ?: "Invalid Doctor Credentials",
                                    Toast.LENGTH_LONG
                                ).show()

                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

                            btnSignIn.isEnabled = true
                            btnSignIn.text = "Sign In"

                            Toast.makeText(
                                this@LoginActivity,
                                "Network Error: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    })
            }
        }
    }
}
