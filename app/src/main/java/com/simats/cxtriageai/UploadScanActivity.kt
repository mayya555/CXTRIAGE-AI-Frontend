package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.view.View

class UploadScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_scan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upload_scan_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "John Doe"
        val patientInfo = intent.getStringExtra("PATIENT_MRN") ?: "P-1024"

        findViewById<TextView>(R.id.tv_patient_name).text = patientName
        findViewById<TextView>(R.id.tv_patient_id).text = "$patientInfo • Chest PA View"

        val spinnerDoctors = findViewById<Spinner>(R.id.spinner_doctors)
        val tvNoDoctors = findViewById<TextView>(R.id.tv_no_doctors)

        var doctorsList: List<Doctor> = emptyList()

        // 🔥 Load doctors
        ApiClient.apiService.getDoctors().enqueue(object : retrofit2.Callback<List<Doctor>> {
            override fun onResponse(call: retrofit2.Call<List<Doctor>>, response: retrofit2.Response<List<Doctor>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {

                    doctorsList = response.body()!!

                    val adapter = ArrayAdapter(
                        this@UploadScanActivity,
                        android.R.layout.simple_spinner_item,
                        doctorsList.map { it.name }
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDoctors.adapter = adapter

                    tvNoDoctors.visibility = View.GONE

                } else {
                    tvNoDoctors.visibility = View.VISIBLE
                    tvNoDoctors.text = "No doctors available"
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Doctor>>, t: Throwable) {
                tvNoDoctors.visibility = View.VISIBLE
                tvNoDoctors.text = "Error loading doctors"
            }
        })

        findViewById<View>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 🔥 CAMERA FLOW
        findViewById<View>(R.id.btn_take_photo).setOnClickListener {

            val selectedPosition = spinnerDoctors.selectedItemPosition
            if (selectedPosition == -1 || doctorsList.isEmpty()) {
                Toast.makeText(this, "Select doctor first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDoctor = doctorsList[selectedPosition]

            val intentCamera = Intent(this, ScannerPreviewActivity::class.java)
            intentCamera.putExtra("SCAN_ID", intent.getIntExtra("SCAN_ID", -1))
            intentCamera.putExtra("AI_DOCTOR_ID", selectedDoctor.doctor_id)
            intentCamera.putExtra("AI_DOCTOR_NAME", selectedDoctor.name)
            intentCamera.putExtra("PATIENT_NAME", patientName)
            intentCamera.putExtra("PATIENT_MRN", patientInfo)

            startActivity(intentCamera)
        }

        // 🔥 GALLERY FLOW
        findViewById<View>(R.id.btn_browse).setOnClickListener {

            val selectedPosition = spinnerDoctors.selectedItemPosition
            if (selectedPosition == -1 || doctorsList.isEmpty()) {
                Toast.makeText(this, "Select doctor first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDoctor = doctorsList[selectedPosition]

            val intentGallery = Intent(this, ScanQualityActivity::class.java)
            intentGallery.putExtra("SCAN_ID", intent.getIntExtra("SCAN_ID", -1))
            intentGallery.putExtra("AI_DOCTOR_ID", selectedDoctor.doctor_id)
            intentGallery.putExtra("AI_DOCTOR_NAME", selectedDoctor.name)
            intentGallery.putExtra("PATIENT_NAME", patientName)
            intentGallery.putExtra("PATIENT_MRN", patientInfo)

            startActivity(intentGallery)
        }

        // ❌ REMOVED BROKEN btnProceed FLOW
        // Upload will happen ONLY after file selection
    }
}