package com.simats.cxtriageai

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileCameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_camera)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.camera_preview)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding or margin if needed, but for full screen camera, usually we want it behind bars.
            // Just handling the close button margin might be better.
            findViewById<FrameLayout>(R.id.btn_close).setPadding(0, systemBars.top, 0, 0)
            insets
        }
        
        findViewById<FrameLayout>(R.id.btn_close).setOnClickListener {
            finish()
        }

        findViewById<android.view.View>(R.id.btn_shutter).setOnClickListener {
            Toast.makeText(this, "Photo Captured!", Toast.LENGTH_SHORT).show()
            // In a real app, this would capture image and return data
            finish()
        }

        findViewById<FrameLayout>(R.id.btn_flash).setOnClickListener {
            Toast.makeText(this, "Flash Toggled", Toast.LENGTH_SHORT).show()
        }

        findViewById<FrameLayout>(R.id.btn_switch_camera).setOnClickListener {
            Toast.makeText(this, "Camera Switched", Toast.LENGTH_SHORT).show()
        }
    }
}
