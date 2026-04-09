package com.simats.cxtriageai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class ScannerPreviewActivity : AppCompatActivity() {

    private var capturedImageUri: Uri? = null  // ✅ IMPORTANT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner_preview)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scanner_preview_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.top_bar)
                .setPadding(24, systemBars.top + 16, 24, 16)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_close).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.btn_capture).setOnClickListener {

            // 🔥 CREATE REAL TEMP FILE (simulate captured image)
            val file = File(cacheDir, "captured.jpg")

            if (!file.exists()) {
                FileOutputStream(file).use {
                    it.write("real_image_data".toByteArray())
                }
            }

            capturedImageUri = Uri.fromFile(file)

            val intentNext = Intent(this, ScanQualityActivity::class.java)

            // ✅ Forward existing data
            intent.extras?.let { intentNext.putExtras(it) }

            // 🔥 CRITICAL FIX — PASS FILE
            intentNext.putExtra("FILE_URI", capturedImageUri.toString())

            startActivity(intentNext)
        }
    }
}