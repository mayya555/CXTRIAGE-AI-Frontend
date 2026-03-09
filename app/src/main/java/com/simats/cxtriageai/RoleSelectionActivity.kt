package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RoleSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_role_selection)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.role_selection)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply top inset to header background and back button
            val headerBg = findViewById<android.view.View>(R.id.header_bg)
            val backIcon = findViewById<android.view.View>(R.id.iv_back_header)
            
            val params = headerBg.layoutParams
            params.height = (resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android")) + 100 * resources.displayMetrics.density).toInt()
            headerBg.layoutParams = params
            
            headerBg.setPadding(0, systemBars.top, 0, 0)
            backIcon.setPadding(0, systemBars.top, 0, 0)
            
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }


        findViewById<ImageView>(R.id.iv_back_header).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<LinearLayout>(R.id.ll_back_navigation).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ConstraintLayout>(R.id.card_doctor).setOnClickListener {
            navigateToLogin("Doctor")
        }

        findViewById<ConstraintLayout>(R.id.card_technician).setOnClickListener {
            navigateToLogin("Technician")
        }

    }

    fun navigateToLogin(role: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("ROLE", role)
        startActivity(intent)
    }
}
