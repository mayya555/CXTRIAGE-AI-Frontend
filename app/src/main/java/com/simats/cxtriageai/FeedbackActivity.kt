package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FeedbackActivity : AppCompatActivity() {

    private var isGeneralSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val tabGeneral = findViewById<TextView>(R.id.tab_general)
        val tabComplaint = findViewById<TextView>(R.id.tab_complaint)
        val etSubject = findViewById<EditText>(R.id.et_subject)
        val etDescription = findViewById<EditText>(R.id.et_description)
        val tvCharCount = findViewById<TextView>(R.id.tv_char_count)
        val btnUpload = findViewById<android.view.View>(R.id.btn_upload)
        val btnSubmit = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_submit)

        // Initial State
        updateTabs(tabGeneral, tabComplaint)

        btnBack.setOnClickListener {
            finish()
        }

        // Tab Switching Logic
        tabGeneral.setOnClickListener {
            if (!isGeneralSelected) {
                isGeneralSelected = true
                updateTabs(tabGeneral, tabComplaint)
            }
        }

        tabComplaint.setOnClickListener {
            if (isGeneralSelected) {
                isGeneralSelected = false
                updateTabs(tabGeneral, tabComplaint)
            }
        }

        // Character Counter Logic
        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length/500 characters"
                if (length > 500) {
                    tvCharCount.setTextColor(ContextCompat.getColor(this@FeedbackActivity, android.R.color.holo_red_dark))
                } else {
                    tvCharCount.setTextColor(ContextCompat.getColor(this@FeedbackActivity, R.color.text_secondary))
                }
            }
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > 500) {
                    etDescription.error = "Description too long"
                }
            }
        })

        btnUpload.setOnClickListener {
            Toast.makeText(this, "Select screenshot from gallery", Toast.LENGTH_SHORT).show()
        }

        btnSubmit.setOnClickListener {
            val subject = etSubject.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (subject.isEmpty()) {
                etSubject.error = "Subject is required"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                etDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Start backend submission
            btnSubmit.isEnabled = false
            btnSubmit.text = "Submitting..."

            val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            val technicianId = prefs.getInt("technician_id", -1)
            val feedbackType = if (isGeneralSelected) "GENERAL" else "COMPLAINT"

            if (technicianId == -1) {
                Toast.makeText(this, "Technician ID not found", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit"
                return@setOnClickListener
            }

            val request = FeedbackRequest(
                technicianId = technicianId,
                feedbackType = feedbackType,
                subject = subject,
                description = description
            )

            ApiClient.apiService.sendFeedback(request).enqueue(object : retrofit2.Callback<FeedbackResponse> {
                override fun onResponse(call: retrofit2.Call<FeedbackResponse>, response: retrofit2.Response<FeedbackResponse>) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit"
                    
                    if (response.isSuccessful) {
                        Toast.makeText(this@FeedbackActivity, "Feedback submitted successfully!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@FeedbackActivity, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<FeedbackResponse>, t: Throwable) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit"
                    Toast.makeText(this@FeedbackActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateTabs(tabGeneral: TextView, tabComplaint: TextView) {
        val darkSlate = android.graphics.Color.parseColor("#1E293B")
        val slateGray = android.graphics.Color.parseColor("#64748B")

        if (isGeneralSelected) {
            tabGeneral.setBackgroundResource(R.drawable.bg_toggle_tab)
            tabGeneral.setTextColor(darkSlate)
            
            tabComplaint.setBackgroundResource(0)
            tabComplaint.setTextColor(slateGray)
        } else {
            tabGeneral.setBackgroundResource(0)
            tabGeneral.setTextColor(slateGray)
            
            tabComplaint.setBackgroundResource(R.drawable.bg_toggle_tab)
            tabComplaint.setTextColor(darkSlate)
        }
    }
}
