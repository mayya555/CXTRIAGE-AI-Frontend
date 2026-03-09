package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class RadiologistDashboardActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_radiologist_dashboard)

        val headerContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.header_container)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.radiologist_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener(this)

        setupRecyclerView()
        
        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
             val intent = Intent(this, SettingsActivity::class.java)
             intent.putExtra("ROLE", "Doctor")
             startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
             showLogoutDialog()
        }

        // Removed the FloatingActionButton click listener that launched AiChatActivity
        // findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_ai_chat).setOnClickListener {
        //     startActivity(Intent(this, AiChatActivity::class.java))
        // }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_priority_queue)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val caseList = listOf(
            CaseItem("John Doe", "Pneumothorax", "CRITICAL", "12m", 80),
            CaseItem("Jane Smith", "Pleural Effusion", "CRITICAL", "24m", 75),
            CaseItem("Robert Johnson", "Nodule L.U.L", "URGENT", "45m", 50),
            CaseItem("Emily Davis", "Normal", "ROUTINE", "1h", 20)
        )

        val adapter = CaseAdapter(caseList)
        recyclerView.adapter = adapter
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on home screen
                return true
            }
            R.id.nav_cases -> {
                startActivity(Intent(this, CaseQueueActivity::class.java))
                return true
            }
            R.id.nav_alerts -> {
                startActivity(Intent(this, AlertsActivity::class.java))
                return true
            }
            R.id.nav_history -> {
                startActivity(Intent(this, PatientHistoryActivity::class.java))
                return true
            }
        }
        return false
    }
}
