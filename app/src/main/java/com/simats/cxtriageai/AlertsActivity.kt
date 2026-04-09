package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlertsActivity : AppCompatActivity() {

    private lateinit var adapter: AlertAdapter
    private val refreshHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            android.util.Log.d("Alerts", "Auto-refreshing alerts data")
            loadAlerts()
            refreshHandler.postDelayed(this, 10000) // Refresh every 10 seconds
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alerts)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.alerts_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupRecyclerView()
        setupFilters()
        loadAlerts()
    }

    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.postDelayed(refreshRunnable, 30000)
    }

    private fun stopAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable)
    }


    private fun setupFilters() {
        val tabAll = findViewById<TextView>(R.id.tab_all)
        val tabUrgent = findViewById<TextView>(R.id.tab_urgent)
        val tabRoutine = findViewById<TextView>(R.id.tab_routine)
        val tabs = listOf(tabAll, tabUrgent, tabRoutine)

        tabAll.setOnClickListener {
            updateTabStyles(tabAll, tabs)
            // Filter logic would go here
        }
        tabUrgent.setOnClickListener {
            updateTabStyles(tabUrgent, tabs)
        }
        tabRoutine.setOnClickListener {
            updateTabStyles(tabRoutine, tabs)
        }
    }

    private fun updateTabStyles(selectedView: TextView, tabs: List<TextView>) {
        tabs.forEach { tab ->
            if (tab == selectedView) {
                tab.setBackgroundResource(R.drawable.bg_chip_selected)
                tab.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
                tab.setTextColor(android.graphics.Color.WHITE)
                tab.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tab.setBackgroundResource(R.drawable.bg_chip_outline_gray)
                tab.backgroundTintList = null
                tab.setTextColor(android.graphics.Color.parseColor("#64748B"))
                tab.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun setupNavigation() {
        findViewById<android.view.View>(R.id.nav_home).setOnClickListener {
            val intent = android.content.Intent(this, DashboardActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.nav_cases).setOnClickListener {
            val intent = android.content.Intent(this, CaseQueueActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // nav_alerts_active is already selected
        
        findViewById<android.view.View>(R.id.nav_history).setOnClickListener {
            val intent = android.content.Intent(this, PatientHistoryActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.nav_settings).setOnClickListener {
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<android.widget.ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<android.widget.ImageView>(R.id.iv_settings).setOnClickListener {
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_alerts)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = AlertAdapter(mutableListOf()) { alert ->
            val intent = android.content.Intent(this, CaseReviewActivity::class.java)
            intent.putExtra("CASE_ID", alert.caseId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun loadAlerts() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId <= 0) {
            android.widget.Toast.makeText(this, "Service unavailable: Invalid session", android.widget.Toast.LENGTH_SHORT).show()
            android.util.Log.e("Alerts", "Invalid doctor_id: $doctorId")
            
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // ✅ LOG doctor_id before call
        android.util.Log.d("Alerts", "Loading alerts for doctor_id = $doctorId")

        ApiClient.apiService.getCriticalAlerts(doctorId = doctorId, priority = "HIGH").enqueue(object : retrofit2.Callback<List<AlertResponse>> {
            override fun onResponse(call: retrofit2.Call<List<AlertResponse>>, response: retrofit2.Response<List<AlertResponse>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: listOf())
                } else {
                    android.util.Log.e("Alerts", "API Error ${response.code()}: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<AlertResponse>>, t: Throwable) {
                android.util.Log.e("Alerts", "Failure: ${t.message}")
            }
        })
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            SessionManager.clearUserData(this@AlertsActivity)
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
