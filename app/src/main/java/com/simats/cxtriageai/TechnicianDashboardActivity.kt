package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TechnicianDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_technician_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.technician_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_bg).layoutParams.height = (180 * resources.displayMetrics.density).toInt() + systemBars.top
             // Adjust header height safely? Or just padding. 
             // Let's stick to standard padding for now to match other screens if needed, 
             // but here the header is big (180dp). 
             // Actually, usually we pad the root or the toolbars. 
             // Let's leave just resizing valid for now from standard template
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        
        // Quick Actions Navigation
        findViewById<android.view.View>(R.id.btn_new_patient).setOnClickListener {
             startActivity(Intent(this, RegistrationActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_history).setOnClickListener {
             startActivity(Intent(this, PatientHistoryActivity::class.java))
        }
        
        findViewById<android.view.View>(R.id.btn_start_scan).setOnClickListener {
            startActivity(Intent(this, PrepareProcedureActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.btn_ai_chat).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
        }

        // Bottom Navigation
        findViewById<android.view.View>(R.id.btn_nav_register).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_nav_history).setOnClickListener {
            startActivity(Intent(this, PatientHistoryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
        }
    }
}
