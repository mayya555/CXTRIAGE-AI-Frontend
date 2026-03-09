package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrepareProcedureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prepare_procedure)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.prepare_procedure_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val checkStates = BooleanArray(6) { false }
        val checkViews = arrayOf(
            findViewById<android.view.View>(R.id.cb_1),
            findViewById<android.view.View>(R.id.cb_2),
            findViewById<android.view.View>(R.id.cb_3),
            findViewById<android.view.View>(R.id.cb_4),
            findViewById<android.view.View>(R.id.cb_5),
            findViewById<android.view.View>(R.id.cb_6)
        )
        val cards = arrayOf(
            findViewById<android.view.View>(R.id.card_item_1),
            findViewById<android.view.View>(R.id.card_item_2),
            findViewById<android.view.View>(R.id.card_item_3),
            findViewById<android.view.View>(R.id.card_item_4),
            findViewById<android.view.View>(R.id.card_item_5),
            findViewById<android.view.View>(R.id.card_item_6)
        )

        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                checkStates[index] = !checkStates[index]
                checkViews[index].setBackgroundResource(
                    if (checkStates[index]) R.drawable.bg_checkbox_selected 
                    else R.drawable.bg_checkbox_unselected
                )
            }
        }

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<AppCompatButton>(R.id.btn_start_scan).setOnClickListener {
            if (checkStates.all { it }) {
                val intentToUpload = Intent(this, UploadScanActivity::class.java)
                // Pass patient data forward
                intentToUpload.putExtra("PATIENT_NAME", intent.getStringExtra("PATIENT_NAME"))
                intentToUpload.putExtra("PATIENT_DOB", intent.getStringExtra("PATIENT_DOB"))
                intentToUpload.putExtra("PATIENT_MRN", intent.getStringExtra("PATIENT_MRN"))
                startActivity(intentToUpload)
            } else {
                Toast.makeText(this, "Please complete all preparation steps first.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
