package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class DashboardActivity : AppCompatActivity() {
    private lateinit var adapter: DashboardAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var llEmptyState: android.widget.LinearLayout
    private var allCases: List<TriageCaseResponse> = listOf()

    private val refreshHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            Log.d("Dashboard", "Auto-refreshing dashboard data")
            loadData()
            refreshHandler.postDelayed(this, 10000) // Refresh every 10 seconds
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        llEmptyState = findViewById(R.id.ll_empty_state)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            loadData()
        }

        setupRecyclerView()
        setupNavigation()
        setupFilters()
        loadDoctorProfile()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.postDelayed(refreshRunnable, 10000)
    }

    private fun stopAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable)
    }


    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_priority_queue)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = DashboardAdapter(mutableListOf())
        recyclerView.adapter = adapter
    }

    private fun setupNavigation() {
        findViewById<android.view.View>(R.id.nav_cases).setOnClickListener {
            startActivity(Intent(this, CaseQueueActivity::class.java))
        }



        findViewById<android.view.View>(R.id.nav_alerts).setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.nav_history).setOnClickListener {
            startActivity(Intent(this, PatientHistoryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupFilters() {
        findViewById<TextView>(R.id.tab_all).setOnClickListener {
            updateTabStyles("All")
            adapter.updateData(allCases)
        }
        findViewById<TextView>(R.id.tab_pending).setOnClickListener {
            updateTabStyles("Pending")
            adapter.updateData(allCases.filter { it.status?.uppercase() == "PENDING" })
        }
    }

    private fun loadDoctorProfile() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorName = prefs.getString("doctor_name", "Doctor")
        findViewById<TextView>(R.id.tv_dashboard_title).text = "Welcome, Dr. $doctorName"
    }

    private fun updateTabStyles(selected: String) {
        val tabAll = findViewById<TextView>(R.id.tab_all)
        val tabPending = findViewById<TextView>(R.id.tab_pending)

        if (selected == "All") {
            tabAll.setBackgroundResource(R.drawable.bg_chip_selected)
            tabAll.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
            tabAll.setTextColor(android.graphics.Color.WHITE)
            tabAll.setTypeface(null, android.graphics.Typeface.BOLD)

            tabPending.setBackground(null)
            tabPending.setTextColor(android.graphics.Color.parseColor("#64748B"))
            tabPending.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            tabPending.setBackgroundResource(R.drawable.bg_chip_selected)
            tabPending.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
            tabPending.setTextColor(android.graphics.Color.WHITE)
            tabPending.setTypeface(null, android.graphics.Typeface.BOLD)

            tabAll.setBackground(null)
            tabAll.setTextColor(android.graphics.Color.parseColor("#64748B"))
            tabAll.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId <= 0) {
            swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Invalid doctor session. Please login again.", Toast.LENGTH_LONG).show()
            Log.e("Dashboard", "Invalid doctor_id: $doctorId")
            
            // Redirect to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // ✅ ALWAYS LOG doctor_id
        Log.d("Dashboard", "Loading data for doctor_id = $doctorId")

        swipeRefresh.isRefreshing = true
        
        // 1. Fetch Dashboard Stats
        // ✅ LOG doctor_id before dashboard fetch
        android.util.Log.d("Dashboard", "Fetching triage-dashboard for doctor_id = $doctorId")

        if (doctorId <= 0) {
            android.widget.Toast.makeText(this, "Invalid doctor session. Please login again.", android.widget.Toast.LENGTH_LONG).show()
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        ApiClient.apiService.getTriageDashboard(doctorId).enqueue(object : retrofit2.Callback<TriageDashboardResponse> {
            override fun onResponse(call: retrofit2.Call<TriageDashboardResponse>, response: retrofit2.Response<TriageDashboardResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<TextView>(R.id.tv_critical_count).text = stats.criticalCases.toString()
                    findViewById<TextView>(R.id.tv_urgent_count).text = stats.urgentCases.toString()
                    Log.d("Dashboard", "Stats loaded successfully: ${stats.criticalCases} critical")
                } else {
                    Log.e("Dashboard", "Stats error: ${response.code()} ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: retrofit2.Call<TriageDashboardResponse>, t: Throwable) {
                Log.e("Dashboard", "Stats network error: ${t.message}")
            }
        })

        // 2. Fetch Case Queue
        ApiClient.apiService.getCaseQueue(doctorId = doctorId).enqueue(object : retrofit2.Callback<List<TriageCaseResponse>> {
            override fun onResponse(call: retrofit2.Call<List<TriageCaseResponse>>, response: retrofit2.Response<List<TriageCaseResponse>>) {
                swipeRefresh.isRefreshing = false
                if (response.isSuccessful) {
                    allCases = response.body() ?: listOf()
                    adapter.updateData(allCases)
                    updateStats(allCases)
                    
                    if (allCases.isEmpty()) {
                        llEmptyState.visibility = View.VISIBLE
                    } else {
                        llEmptyState.visibility = View.GONE
                    }
                } else {
                    Log.e("Dashboard", "API error ${response.code()}: ${response.errorBody()?.string()}")
                    Toast.makeText(this@DashboardActivity, "Failed to load dashboard data (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<TriageCaseResponse>>, t: Throwable) {
                swipeRefresh.isRefreshing = false
                Log.e("Dashboard", "Network Error: ${t.message}")
                llEmptyState.visibility = View.VISIBLE
            }
        })
    }

    private fun updateStats(cases: List<TriageCaseResponse>) {
        // Use contains to handle both "CRITICAL" and "PRIORITYENUM.CRITICAL" formats
        val criticalCount = cases.count { it.priority?.contains("CRITICAL", ignoreCase = true) == true }
        val urgentCount = cases.count { it.priority?.contains("URGENT", ignoreCase = true) == true }
        
        findViewById<TextView>(R.id.tv_critical_count).text = criticalCount.toString()
        findViewById<TextView>(R.id.tv_urgent_count).text = urgentCount.toString()
        
        Log.d("Dashboard", "Stats updated from queue: Critical=$criticalCount, Urgent=$urgentCount")
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            // Clear all session data
            SessionManager.clearUserData(this@DashboardActivity)
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
