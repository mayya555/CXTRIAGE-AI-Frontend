package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PatientHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patient_history_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_patient_history)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val historyList = listOf(
            PatientHistoryItem("John Doe", "P-1024", "Last scan: 2 weeks ago • Dr. Bennett", "Pneumonia History", "Smoker"),
            PatientHistoryItem("Jane Smith", "P-1025", "Last scan: 2 weeks ago • Dr. Bennett", "Pneumonia History", "Smoker"),
            PatientHistoryItem("Robert Johnson", "P-1026", "Last scan: 2 weeks ago • Dr. Bennett", "Pneumonia History", "Smoker")
        )

        val adapter = PatientHistoryAdapter(historyList)
        recyclerView.adapter = adapter
    }
}
