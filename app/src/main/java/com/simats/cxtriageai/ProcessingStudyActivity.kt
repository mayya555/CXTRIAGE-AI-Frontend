package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProcessingStudyActivity : AppCompatActivity() {

    private lateinit var progressBarDicom: ProgressBar
    private lateinit var progressCircular: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private val handler = Handler(Looper.getMainLooper())

    private var dicomProgress = 0
    private var aiProgress = 0

    private val dicomRunnable = object : Runnable {
        override fun run() {
            if (dicomProgress < 100) {
                dicomProgress += 5
                progressBarDicom.progress = dicomProgress
                handler.postDelayed(this, 100)
            } else {
                handler.post(aiRunnable)
            }
        }
    }

    private val aiRunnable = object : Runnable {
        override fun run() {
            if (aiProgress < 100) {
                aiProgress += 2
                progressCircular.progress = aiProgress
                tvProgressPercent.text = "$aiProgress%"
                handler.postDelayed(this, 50)
            } else {
                navigateToSuccess()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_processing_study)

        val root = findViewById<android.view.View>(R.id.processing_study_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBarDicom = findViewById(R.id.progress_bar_dicom)
        progressCircular = findViewById(R.id.progress_circular)
        tvProgressPercent = findViewById(R.id.tv_progress_percent)

        handler.post(dicomRunnable)
    }

    private fun navigateToSuccess() {
        val intent = Intent(this, ProcessingSuccessActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
