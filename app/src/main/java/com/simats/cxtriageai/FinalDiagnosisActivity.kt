package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinalDiagnosisActivity : AppCompatActivity() {

    private var currentCase: TriageCaseResponse? = null
    private var selectedDiagnosis: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_final_diagnosis)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.final_diagnosis_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val caseId = intent.getIntExtra("CASE_ID", -1)
        
        if (caseId <= 0) {
            Toast.makeText(this, "Invalid Case ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId <= 0) {
            Toast.makeText(this, "Invalid doctor session", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return
        }
        
        setupNavigation()
        loadCaseDetails(caseId)
        
        findViewById<android.view.View>(R.id.btn_generate_report).setOnClickListener {
            if (selectedDiagnosis == null) {
                Toast.makeText(this, "Please select an assessment option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            finalizeAndReturn(caseId)
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            startActivity(intent)
        }

        setupAssessmentOptions()
    }

    private fun setupAssessmentOptions() {
        val optionConfirm = findViewById<TextView>(R.id.option_confirm)
        val optionNormal = findViewById<TextView>(R.id.option_normal)
        val optionOther = findViewById<TextView>(R.id.option_other)
        val options = listOf(optionConfirm, optionNormal, optionOther)

        options.forEach { option ->
            option.setOnClickListener {
                selectedDiagnosis = option.text.toString()
                updateOptionStyles(option, options)
            }
        }
    }

    private fun updateOptionStyles(selectedView: TextView, options: List<TextView>) {
        options.forEach { option ->
            if (option == selectedView) {
                option.setBackgroundResource(R.drawable.bg_assessment_option)
                option.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#ECFDF5"))
                option.setTextColor(android.graphics.Color.parseColor("#059669"))
            } else {
                option.setBackgroundResource(R.drawable.bg_assessment_option)
                option.backgroundTintList = null
                option.setTextColor(android.graphics.Color.parseColor("#64748B"))
            }
        }
    }

    private fun loadCaseDetails(caseId: Int) {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)
        if (doctorId == -1) return

        ApiClient.apiService.getCaseDetails(caseId, doctorId).enqueue(object: Callback<TriageCaseResponse> {
            override fun onResponse(call: Call<TriageCaseResponse>, response: Response<TriageCaseResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    currentCase = data
                    val aiText = data.aiResult ?: "Pending AI..."
                    val confidenceText = data.aiConfidence?.toString()?.let { 
                        if (it.contains("%")) it else "$it Match"
                    } ?: "--% Match"
                    
                    findViewById<TextView>(R.id.tv_diagnosis_main).text = aiText
                    findViewById<TextView>(R.id.tv_match_badge).text = confidenceText
                }
            }
            override fun onFailure(call: Call<TriageCaseResponse>, t: Throwable) {}
        })
    }

    private fun finalizeAndReturn(caseId: Int) {
        val notes = findViewById<android.widget.EditText>(R.id.et_notes).text.toString()
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorName = prefs.getString("doctor_name", "Doctor") ?: "Doctor"

        Toast.makeText(this, "Finalizing case...", Toast.LENGTH_SHORT).show()

        // 1. Finalize and Sign
        val finalizeMap = mapOf(
            "doctor_name" to doctorName,
            "impression" to (selectedDiagnosis ?: ""),
            "recommendation" to notes
        )

        ApiClient.apiService.finalizeCase(caseId, finalizeMap).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // Background report generation attempt
                ApiClient.apiService.generateReport(caseId).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, r: Response<ResponseBody>) {
                        android.util.Log.d("FinalDiagnosis", "Report generation triggered")
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                })
                
                Toast.makeText(this@FinalDiagnosisActivity, "Case Finalized Successfully", Toast.LENGTH_SHORT).show()
                returnToDashboard()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Even on failure, we try to go back or show error
                Toast.makeText(this@FinalDiagnosisActivity, "Finalization error: ${t.message}", Toast.LENGTH_SHORT).show()
                returnToDashboard()
            }
        })
    }

    private fun returnToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
