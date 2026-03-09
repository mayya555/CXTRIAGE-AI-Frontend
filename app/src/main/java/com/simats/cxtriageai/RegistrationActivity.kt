package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
             onBackPressedDispatcher.onBackPressed()
        }

        val etName = findViewById<EditText>(R.id.et_patient_name)
        val etDob = findViewById<EditText>(R.id.et_dob)
        val etGender = findViewById<EditText>(R.id.et_gender)
        val etMrn = findViewById<EditText>(R.id.et_mrn)
        val etReason = findViewById<EditText>(R.id.et_reason)
        val btnRegister = findViewById<Button>(R.id.btn_register_patient)

        etDob.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, year, month, day ->
                etDob.setText("$day/${month + 1}/$year")
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        btnRegister.setOnClickListener {
            // Basic Validation
            if (etName.text.isEmpty() || etMrn.text.isEmpty()) {
                Toast.makeText(this, "Please fill in Name and MRN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navigate to Registration Success
            val intent = Intent(this, PatientRegistrationSuccessActivity::class.java)
            intent.putExtra("PATIENT_NAME", etName.text.toString())
            intent.putExtra("PATIENT_DOB", etDob.text.toString())
            intent.putExtra("PATIENT_GENDER", etGender.text.toString())
            intent.putExtra("PATIENT_MRN", etMrn.text.toString())
            startActivity(intent)
        }
    }
}
