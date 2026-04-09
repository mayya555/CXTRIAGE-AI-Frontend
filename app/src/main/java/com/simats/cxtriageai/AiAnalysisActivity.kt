package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AiAnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_analysis)

        val patientName = intent.getStringExtra("PATIENT_NAME")
        val patientId = intent.getIntExtra("PATIENT_ID", -1)
        val fileUriString = intent.getStringExtra("FILE_URI")

        if (fileUriString != null) {
            val uri = android.net.Uri.parse(fileUriString)
            val fileName = getFileName(uri)
            findViewById<android.widget.TextView>(R.id.tv_status_subtitle).text = "Analyzing file: $fileName"
        }

        val progressBar = findViewById<ProgressBar>(R.id.analysis_progress)
        
        // Simulate progress
        var currentProgress = 30
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (currentProgress < 100) {
                    currentProgress += 5
                    progressBar.progress = currentProgress
                    handler.postDelayed(this, 200)
                } else {
                    // Navigate to Heatmap
                    val intentHeatmap = Intent(this@AiAnalysisActivity, AiHeatmapActivity::class.java)
                    intentHeatmap.putExtras(intent)
                    startActivity(intentHeatmap)
                    finish()
                }
            }
        }
        handler.postDelayed(runnable, 1000)
    }
    private fun getFileName(uri: android.net.Uri): String {
        var name = "Unknown file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
