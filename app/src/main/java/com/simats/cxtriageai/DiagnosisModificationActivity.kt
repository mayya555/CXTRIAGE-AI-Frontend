package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DiagnosisModificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diagnosis_modification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mod_diagnosis_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(16, systemBars.top + 16, 16, 16)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_save_changes).setOnClickListener {
            val revised = findViewById<EditText>(R.id.et_revised_diagnosis).text.toString()
            val reason = findViewById<EditText>(R.id.et_modification_reason).text.toString()

            if (revised.isNotEmpty() && reason.isNotEmpty()) {
                // Save changes and return to Report Generation or Final Diagnosis
                Toast.makeText(this, "Diagnosis Updated", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ReportGenerationActivity::class.java))
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
