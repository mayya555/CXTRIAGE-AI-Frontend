package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PatientRegistrationSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_registration_success)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Retrieve Data
        val name = intent.getStringExtra("PATIENT_NAME") ?: "N/A"
        val dob = intent.getStringExtra("PATIENT_DOB") ?: "N/A"
        val mrn = intent.getStringExtra("PATIENT_MRN") ?: "N/A"
        val gender = intent.getStringExtra("PATIENT_GENDER") ?: "N/A"
        val patientId = intent.getIntExtra("PATIENT_ID", -1)

        // Set Data
        findViewById<TextView>(R.id.tv_display_name).text = name
        findViewById<TextView>(R.id.tv_display_dob).text = dob
        findViewById<TextView>(R.id.tv_display_mrn).text = mrn
        findViewById<TextView>(R.id.tv_display_gender).text = gender
        findViewById<TextView>(R.id.tv_display_patient_id).text = patientId.toString()

        // Click Listeners
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<AppCompatButton>(R.id.btn_start_xray).setOnClickListener {
            val intent = Intent(this, PrepareProcedureActivity::class.java)
            intent.putExtra("PATIENT_ID", patientId)
            intent.putExtra("PATIENT_NAME", name)
            intent.putExtra("PATIENT_DOB", dob)
            intent.putExtra("PATIENT_MRN", mrn)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_register_another).setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        findViewById<TextView>(R.id.btn_dashboard).setOnClickListener {
            val intent = Intent(this, TechnicianDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
