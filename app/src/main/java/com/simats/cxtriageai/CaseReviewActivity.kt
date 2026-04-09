package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import okhttp3.ResponseBody

class CaseReviewActivity : AppCompatActivity() {

    private var currentCase: TriageCaseResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_case_review)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.case_review_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val caseId = intent.getIntExtra("CASE_ID", -1)
        Log.d("DEBUG", "CASE ID: $caseId")

        if (caseId <= 0) {
            Toast.makeText(this, "Invalid Case ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupNavigation(caseId)
        loadCaseDetails(caseId)
    }

    private fun setupNavigation(caseId: Int) {

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<View>(R.id.btn_history).setOnClickListener {
            val intent = Intent(this, PatientHistoryActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tv_edit).setOnClickListener {
            Toast.makeText(this, "Edit Mode", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.btn_sign_report).setOnClickListener {
            val intent = Intent(this, FinalDiagnosisActivity::class.java)
            intent.putExtra("CASE_ID", caseId)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_accept).setOnClickListener {
            acceptCase(caseId)
        }

        findViewById<View>(R.id.btn_reject).setOnClickListener {
            rejectCase(caseId)
        }
    }

    // =========================
    // LOAD CASE DETAILS
    // =========================
    private fun loadCaseDetails(caseId: Int) {

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId == -1) return

        ApiClient.apiService.getCaseDetails(caseId, doctorId)
            .enqueue(object : Callback<TriageCaseResponse> {

                override fun onResponse(
                    call: Call<TriageCaseResponse>,
                    response: Response<TriageCaseResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val caseData = response.body()!!
                        currentCase = caseData

                        // ✅ SAFE DISPLAY
                        val aiText = caseData.aiFindings ?: caseData.aiResult ?: "Pending AI..."
                        findViewById<TextView>(R.id.tv_ai_findings_content).text = aiText

                    } else {
                        Log.e("DEBUG", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TriageCaseResponse>, t: Throwable) {
                    Log.e("DEBUG", "Failure: ${t.message}")
                }
            })
    }

    // =========================
    // ACCEPT CASE
    // =========================
    private fun acceptCase(caseId: Int) {

        Log.d("DEBUG", "Accept Case ID: $caseId")

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val doctorName = prefs.getString("doctor_name", "Doctor") ?: "Doctor"
        val doctorId = prefs.getInt("doctor_id", -1)

        ApiClient.apiService.acceptCase(caseId, mapOf("doctor_name" to doctorName, "doctor_id" to doctorId.toString()))
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CaseReviewActivity, "Case Accepted", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CaseReviewActivity, FinalDiagnosisActivity::class.java)
                        intent.putExtra("CASE_ID", caseId)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("DEBUG", "Accept Error: ${response.code()}")
                        Toast.makeText(this@CaseReviewActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("DEBUG", "Accept Fail: ${t.message}")
                    Toast.makeText(this@CaseReviewActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // =========================
    // REJECT CASE
    // =========================
    private fun rejectCase(caseId: Int) {

        Log.d("DEBUG", "Reject Case ID: $caseId")

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val doctorName = prefs.getString("doctor_name", "Doctor") ?: "Doctor"
        val doctorId = prefs.getInt("doctor_id", -1)

        ApiClient.apiService.rejectCase(caseId, SignRequest(doctorName, doctorId))
            .enqueue(object : Callback<CaseActionResponse> {

                override fun onResponse(
                    call: Call<CaseActionResponse>,
                    response: Response<CaseActionResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CaseReviewActivity, "Rejected", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CaseReviewActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("DEBUG", "Reject Error: ${response.code()}")
                        Toast.makeText(this@CaseReviewActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                    Log.e("DEBUG", "Reject Fail: ${t.message}")
                    Toast.makeText(this@CaseReviewActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            SessionManager.clearUserData(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}