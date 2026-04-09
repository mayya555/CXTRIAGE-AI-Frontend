package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanHistoryActivity : AppCompatActivity() {

    private lateinit var rvScans: RecyclerView
    private lateinit var adapter: ScanHistoryAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private var allScans = mutableListOf<ScanHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        setupViews()
        setupMockData() // Populate with mock data initially
        setupBottomNavigation()
        loadScansFromApi()
    }

    private fun setupBottomNavigation() {
        findViewById<android.view.View>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, TechnicianDashboardActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.btn_nav_register).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.btn_nav_history).setOnClickListener {
            // Already here
        }

        findViewById<android.view.View>(R.id.btn_nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
            finish()
        }
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        rvScans = findViewById(R.id.rv_scans)
        rvScans.layoutManager = LinearLayoutManager(this)
        
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        tvEmptyState = findViewById<TextView>(R.id.tv_empty_state)
        
        adapter = ScanHistoryAdapter(allScans) { scan ->
            val intent = Intent(this, ScanDetailsActivity::class.java)
            intent.putExtra("SCAN_CODE", scan.id)
            intent.putExtra("PATIENT_NAME", scan.patientName)
            intent.putExtra("MRN", scan.mrn)
            intent.putExtra("DOB", scan.dateOfBirth)
            intent.putExtra("GENDER", scan.gender)
            intent.putExtra("SCAN_DATE", scan.scanDate)
            intent.putExtra("TECHNICIAN", scan.technician)
            intent.putExtra("STATUS", scan.status)
            intent.putExtra("DATE", scan.date)
            intent.putExtra("VIEW_TYPE", scan.viewType)
            intent.putExtra("ORIENTATION", scan.orientation)
            intent.putExtra("STUDY_ID", scan.studyId)
            startActivity(intent)
        }
        rvScans.adapter = adapter

        // Filter Tabs
        findViewById<TextView>(R.id.tab_all).setOnClickListener { filterScans("All") }
        findViewById<TextView>(R.id.tab_completed).setOnClickListener { filterScans("Completed") }
        findViewById<TextView>(R.id.tab_pending).setOnClickListener { filterScans("Pending") }
        findViewById<TextView>(R.id.tab_retake).setOnClickListener { filterScans("Retake") }
        
        // Manual Refresh via Search Icon
        findViewById<ImageView>(R.id.iv_search).setOnClickListener {
            Toast.makeText(this, "Refreshing scan history...", Toast.LENGTH_SHORT).show()
            loadScansFromApi()
        }

        // Initial style
        updateTabStyles("All")
    }

    private fun setupMockData() {
        allScans.clear()
        adapter.updateData(allScans)
    }

    override fun onResume() {
        super.onResume()
        loadScansFromApi()
    }

    private fun loadScansFromApi() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val technicianId = prefs.getInt("technician_id", -1)

        if (technicianId == -1) {
            tvEmptyState.visibility = android.view.View.VISIBLE
            tvEmptyState.text = "Error: Session information missing"
            return
        }

        progressBar.visibility = android.view.View.VISIBLE
        tvEmptyState.visibility = android.view.View.GONE

        ApiClient.apiService.getScanHistory(technicianId).enqueue(object : Callback<List<ScanHistoryItem>> {
            override fun onResponse(call: Call<List<ScanHistoryItem>>, response: Response<List<ScanHistoryItem>>) {
                progressBar.visibility = android.view.View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val apiScans = response.body()!!
                    allScans.clear()
                    // Sort descending by ID or Date to show latest first
                    allScans.addAll(apiScans.sortedByDescending { it.id }) 
                    adapter.updateData(allScans)
                    
                    if (apiScans.isEmpty()) {
                        tvEmptyState.visibility = android.view.View.VISIBLE
                        tvEmptyState.text = "No scan history available"
                    } else {
                        tvEmptyState.visibility = android.view.View.GONE
                    }
                } else {
                    Toast.makeText(this@ScanHistoryActivity, "Failed to load scan history", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ScanHistoryItem>>, t: Throwable) {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@ScanHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterScans(category: String) {
        val filtered = when (category) {
            "All" -> allScans
            "Completed" -> allScans.filter { it.status.equals("Completed", ignoreCase = true) }
            "Pending" -> allScans.filter { 
                val s = it.status.lowercase()
                s == "pending" || s == "processing" || s == "started" 
            }
            "Retake" -> allScans.filter { it.status.equals("Retake", ignoreCase = true) }
            else -> allScans
        }
        adapter.updateData(filtered)
        updateTabStyles(category)
    }

    private fun updateTabStyles(selected: String) {
        val tabAll = findViewById<TextView>(R.id.tab_all)
        val tabCompleted = findViewById<TextView>(R.id.tab_completed)
        val tabPending = findViewById<TextView>(R.id.tab_pending)
        val tabRetake = findViewById<TextView>(R.id.tab_retake)

        val tabs = listOf(
            tabAll to "All", 
            tabCompleted to "Completed", 
            tabPending to "Pending", 
            tabRetake to "Retake"
        )
        
        tabs.forEach { (view, name) ->
            if (name == selected) {
                view.setBackgroundResource(R.drawable.bg_tab_selected)
                view.setTextColor(getColor(R.color.white))
                view.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                view.setBackground(null)
                view.setTextColor(getColor(R.color.white))
                view.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
}
