package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CaseQueueActivity : AppCompatActivity() {

    private lateinit var adapter: CaseQueueAdapter
    private var allCases: List<TriageCaseResponse> = listOf()

    private val refreshHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            Log.d("CaseQueue", "Auto-refreshing case queue data")
            loadData()
            refreshHandler.postDelayed(this, 30000) // Refresh every 30 seconds
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_case_queue)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.case_queue_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupFilters()
        setupNavigation()
        loadData()
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


    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_case_queue)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CaseQueueAdapter(mutableListOf())
        recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        val tabAll = findViewById<TextView>(R.id.tab_all)
        val tabUrgent = findViewById<TextView>(R.id.tab_urgent)
        val tabRoutine = findViewById<TextView>(R.id.tab_routine)
        val tabs = listOf(tabAll, tabUrgent, tabRoutine)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                updateTabStyles(tab, tabs)
                val filter = when(tab.id) {
                    R.id.tab_urgent -> "URGENT"
                    R.id.tab_routine -> "ROUTINE"
                    else -> "ALL"
                }
                filterData(filter)
            }
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

    private fun filterData(filter: String) {
        val filteredList = if (filter == "ALL") {
            allCases
        } else {
            allCases.filter { it.priority?.uppercase() == filter || (filter == "URGENT" && it.priority?.uppercase() == "CRITICAL") }
        }
        adapter.updateData(filteredList)
    }

    private fun setupNavigation() {
        findViewById<android.view.View>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        
        findViewById<android.view.View>(R.id.nav_alerts).setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.nav_history).setOnClickListener {
            startActivity(Intent(this, PatientHistoryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Doctor")
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        // ✅ LOG doctor_id before call
        Log.d("CaseQueue", "Loading data for doctor_id = $doctorId")

        if (doctorId <= 0) {
            Toast.makeText(this, "Invalid doctor session. Please login again.", Toast.LENGTH_LONG).show()
            Log.e("CaseQueue", "Invalid doctor_id: $doctorId")
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // ✅ ALWAYS SEND doctor_id
        ApiClient.apiService.getCaseQueue(doctorId = doctorId)
            .enqueue(object : Callback<List<TriageCaseResponse>> {

                override fun onResponse(
                    call: Call<List<TriageCaseResponse>>,
                    response: Response<List<TriageCaseResponse>>
                ) {
                    if (response.isSuccessful) {
                        allCases = response.body() ?: listOf()
                        adapter.updateData(allCases)

                        if (allCases.isEmpty()) {
                            Log.d("CaseQueue", "No cases found for doctor_id = $doctorId")
                        } else {
                            Log.d("CaseQueue", "Loaded ${allCases.size} cases")
                        }

                    } else {
                        Log.e("CaseQueue", "API ERROR: ${response.code()}")
                        Toast.makeText(this@CaseQueueActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<TriageCaseResponse>>, t: Throwable) {
                    Log.e("CaseQueue", "NETWORK ERROR: ${t.message}")
                    Toast.makeText(this@CaseQueueActivity, "Network error", Toast.LENGTH_SHORT).show()
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
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
