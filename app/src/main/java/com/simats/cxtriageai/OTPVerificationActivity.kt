package com.simats.cxtriageai

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OTPVerificationActivity : AppCompatActivity() {

    private lateinit var otpFields: Array<EditText>
    private lateinit var tvResendTimer: TextView
    private var resendEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otp_verification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.otp_verification_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        tvResendTimer = findViewById(R.id.tv_resend_timer)
        
        otpFields = arrayOf(
            findViewById(R.id.et_otp1),
            findViewById(R.id.et_otp2),
            findViewById(R.id.et_otp3),
            findViewById(R.id.et_otp4),
            findViewById(R.id.et_otp5),
            findViewById(R.id.et_otp6)
        )

        setupOtpLogic()
        startResendTimer()

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_verify).setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }
            val email = intent.getStringExtra("EMAIL") ?: ""
            val userRole = intent.getStringExtra("USER_ROLE") ?: "Doctor"

            if (otp.length == 6 && email.isNotEmpty()) {
                val request = VerifyOTPRequest(email, otp)
                
                val call = if (userRole == "Technician") {
                    ApiClient.apiService.technicianVerifyOTP(request)
                } else {
                    ApiClient.apiService.verifyOTP(request)
                }
                
                call.enqueue(object : retrofit2.Callback<VerifyOTPResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<VerifyOTPResponse>,
                        response: retrofit2.Response<VerifyOTPResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@OTPVerificationActivity, "OTP Verified Successfully", Toast.LENGTH_SHORT).show()
                            val newIntent = Intent(this@OTPVerificationActivity, NewPasswordActivity::class.java)
                            newIntent.putExtra("EMAIL", email)
                            newIntent.putExtra("USER_ROLE", userRole)
                            startActivity(newIntent)
                            finish()
                        } else {
                            val errorMessage = response.errorBody()?.string() ?: "Invalid OTP"
                            Toast.makeText(this@OTPVerificationActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<VerifyOTPResponse>, t: Throwable) {
                        Toast.makeText(this@OTPVerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Email error, please go back.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter complete 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpLogic() {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < otpFields.size - 1) {
                            otpFields[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            otpFields[i - 1].requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Handle backspace when empty
            otpFields[i].setOnKeyListener { v, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    event.action == android.view.KeyEvent.ACTION_DOWN && 
                    otpFields[i].text.isEmpty() && i > 0) {
                    otpFields[i - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun startResendTimer() {
        resendEnabled = false
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendTimer.text = "Didn't receive the code? Resend in ${seconds}s"
            }

            override fun onFinish() {
                resendEnabled = true
                val fullText = "Didn't receive the code? Resend now"
                val spannableString = SpannableString(fullText)
                val resendClickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (resendEnabled) {
                            Toast.makeText(this@OTPVerificationActivity, "New code sent!", Toast.LENGTH_SHORT).show()
                            startResendTimer()
                        }
                    }
                }
                
                val startIndex = fullText.indexOf("Resend now")
                spannableString.setSpan(resendClickable, startIndex, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#1E62F0")), startIndex, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                
                tvResendTimer.text = spannableString
                tvResendTimer.movementMethod = LinkMovementMethod.getInstance()
            }
        }.start()
    }
}
