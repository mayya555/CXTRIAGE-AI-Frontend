package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AiHeatmapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_heatmap)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.heatmap_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val heatmapOverlay = findViewById<ImageView>(R.id.iv_heatmap_overlay)
        val opacitySeekBar = findViewById<SeekBar>(R.id.seekbar_opacity)
        val tvOpacityValue = findViewById<TextView>(R.id.tv_opacity_value)

        // Set initial opacity
        heatmapOverlay.alpha = 0.6f
        opacitySeekBar.progress = 60
        tvOpacityValue.text = "60%"

        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val alpha = progress / 100f
                heatmapOverlay.alpha = alpha
                tvOpacityValue.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<android.view.View>(R.id.btn_confirm_diagnosis).setOnClickListener {
            startActivity(Intent(this, FinalDiagnosisActivity::class.java))
        }
        
    }
}
