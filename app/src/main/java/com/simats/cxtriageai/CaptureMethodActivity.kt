package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CaptureMethodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_capture_method)

        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Patient"
        val patientMrn = intent.getStringExtra("PATIENT_MRN") ?: "P-0000"
        val patientId = intent.getIntExtra("PATIENT_ID", -1)
        val scanId = intent.getIntExtra("SCAN_ID", -1)

        findViewById<TextView>(R.id.tv_patient_name).text = patientName
        findViewById<TextView>(R.id.tv_patient_mrn).text = "MRN: $patientMrn"

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val spinnerDoctors = findViewById<android.widget.Spinner>(R.id.spinner_doctors)
        val tvNoDoctors = findViewById<android.widget.TextView>(R.id.tv_no_doctors)
        var doctorsList: List<Doctor> = emptyList()

        // Fetch Doctors
        ApiClient.apiService.getDoctors().enqueue(object : retrofit2.Callback<List<Doctor>> {
            override fun onResponse(call: retrofit2.Call<List<Doctor>>, response: retrofit2.Response<List<Doctor>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    doctorsList = response.body()!!
                    val adapter = android.widget.ArrayAdapter(
                        this@CaptureMethodActivity,
                        android.R.layout.simple_spinner_item,
                        doctorsList.map { it.name }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDoctors.adapter = adapter
                    
                    spinnerDoctors.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            if (doctorsList.isNotEmpty()) {
                                val docId = doctorsList[position].doctor_id
                                getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putInt("selected_doctor_id", docId)
                                    .apply()
                                android.util.Log.d("CaptureMethodPref", "Saved selected_doctor_id: $docId")
                            }
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                    tvNoDoctors.visibility = android.view.View.GONE
                } else {
                    tvNoDoctors.visibility = android.view.View.VISIBLE
                    tvNoDoctors.text = "No doctors available"
                }
            }
            override fun onFailure(call: retrofit2.Call<List<Doctor>>, t: Throwable) {
                tvNoDoctors.visibility = android.view.View.VISIBLE
                tvNoDoctors.text = "Error loading doctors"
            }
        })

        findViewById<CardView>(R.id.card_camera).setOnClickListener {
            // Validate doctor selection
            val selectedPosition = spinnerDoctors.selectedItemPosition
            if (selectedPosition == -1 || doctorsList.isEmpty()) {
                android.widget.Toast.makeText(this, "Please select a doctor first", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedDoctor = doctorsList[selectedPosition]

            val intentCamera = Intent(this, CameraCaptureActivity::class.java)
            intentCamera.putExtra("PATIENT_NAME", patientName)
            intentCamera.putExtra("PATIENT_MRN", patientMrn)
            intentCamera.putExtra("PATIENT_ID", patientId)
            intentCamera.putExtra("SCAN_ID", scanId)
            intentCamera.putExtra("AI_DOCTOR_ID", selectedDoctor.doctor_id)
            intentCamera.putExtra("AI_DOCTOR_NAME", selectedDoctor.name)
            startActivity(intentCamera)
        }

        findViewById<CardView>(R.id.card_upload).setOnClickListener {
            // Validate doctor selection
            val selectedPosition = spinnerDoctors.selectedItemPosition
            if (selectedPosition == -1 || doctorsList.isEmpty()) {
                android.widget.Toast.makeText(this, "Please select a doctor first", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedDoctor = doctorsList[selectedPosition]

            val intentUpload = Intent(this, UploadImageActivity::class.java)
            intentUpload.putExtra("PATIENT_NAME", patientName)
            intentUpload.putExtra("PATIENT_MRN", patientMrn)
            intentUpload.putExtra("PATIENT_ID", patientId)
            intentUpload.putExtra("SCAN_ID", scanId)
            intentUpload.putExtra("AI_DOCTOR_ID", selectedDoctor.doctor_id)
            intentUpload.putExtra("AI_DOCTOR_NAME", selectedDoctor.name)
            startActivity(intentUpload)
        }
    }
}
