package com.simats.cxtriageai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView

class UploadImageActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val fileName = getFileName(it)
            findViewById<TextView>(R.id.tv_selected_filename).apply {
                text = "Selected: $fileName"
                visibility = View.VISIBLE
            }
            findViewById<AppCompatButton>(R.id.btn_proceed).apply {
                isEnabled = true
                alpha = 1.0f
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_image)

        val patientName = intent.getStringExtra("PATIENT_NAME")
        val patientMrn = intent.getStringExtra("PATIENT_MRN")
        val patientId = intent.getIntExtra("PATIENT_ID", -1)
        val scanId = intent.getIntExtra("SCAN_ID", -1)

        findViewById<TextView>(R.id.tv_patient_name).text = patientName
        findViewById<TextView>(R.id.tv_patient_mrn).text = "MRN: $patientMrn"

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_choose_file).setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        val btnProceed = findViewById<AppCompatButton>(R.id.btn_proceed)
        val spinnerDoctors = findViewById<android.widget.Spinner>(R.id.spinner_doctors)
        val tvNoDoctors = findViewById<TextView>(R.id.tv_no_doctors)
        var doctorsList: List<Doctor> = emptyList()

        // Fetch Doctors
        ApiClient.apiService.getDoctors().enqueue(object : retrofit2.Callback<List<Doctor>> {
            override fun onResponse(call: retrofit2.Call<List<Doctor>>, response: retrofit2.Response<List<Doctor>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    doctorsList = response.body()!!
                    val adapter = android.widget.ArrayAdapter(
                        this@UploadImageActivity,
                        android.R.layout.simple_spinner_item,
                        doctorsList.map { it.name }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDoctors.adapter = adapter
                    
                    spinnerDoctors.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (doctorsList.isNotEmpty()) {
                                val docId = doctorsList[position].doctor_id
                                getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putInt("selected_doctor_id", docId)
                                    .apply()
                                android.util.Log.d("UploadImagePref", "Saved selected_doctor_id: $docId")
                            }
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
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

        btnProceed.setOnClickListener {
            // Validate doctor selection
            val selectedPosition = spinnerDoctors.selectedItemPosition
            if (selectedPosition == -1 || doctorsList.isEmpty()) {
                Toast.makeText(this, "Please select a doctor first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedDoctor = doctorsList[selectedPosition]

            val intentReview = Intent(this, ProcessingStudyActivity::class.java)
            // Forward existing extras
            intent.extras?.let { intentReview.putExtras(it) }
            
            // ✅ Set explicit Doctor Data
            intentReview.putExtra("AI_DOCTOR_ID", selectedDoctor.doctor_id)
            intentReview.putExtra("AI_DOCTOR_NAME", selectedDoctor.name)
            
            intentReview.putExtra("CAPTURE_METHOD", "Upload")
            intentReview.putExtra("FILE_URI", selectedFileUri.toString())
            
            android.util.Log.d("UploadImage", "Moving to review with doctor_id: ${selectedDoctor.doctor_id}")
            startActivity(intentReview)
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
