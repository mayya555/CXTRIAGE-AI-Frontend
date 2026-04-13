package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TechnicianDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_technician_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.technician_root)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.header_bg).layoutParams.height = (180 * resources.displayMetrics.density).toInt() + systemBars.top
            
            // Instead of padding the root, let's ensure the bottom nav is above the system bars
            val bottomNav = findViewById<View>(R.id.bottom_nav)
            bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            
            insets
        }

        findViewById<View>(R.id.iv_profile_header).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
        }
        
        // Quick Actions Navigation
        findViewById<View>(R.id.btn_new_patient).setOnClickListener {
             startActivity(Intent(this, RegistrationActivity::class.java))
        }

        findViewById<View>(R.id.btn_start_scan).setOnClickListener {
            val intent = Intent(this, PrepareProcedureActivity::class.java)
            // Mock data for demo purposes when starting directly from dashboard
            intent.putExtra("PATIENT_ID", 101) 
            intent.putExtra("PATIENT_NAME", "Sarah Wilson")
            intent.putExtra("PATIENT_MRN", "MRN-4521")
            startActivity(intent)
        }

        // Bottom Navigation
        findViewById<View>(R.id.btn_nav_home).setOnClickListener {
            // Already here
        }

        findViewById<View>(R.id.btn_nav_scan).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        findViewById<View>(R.id.btn_nav_history).setOnClickListener {
            startActivity(Intent(this, ScanHistoryActivity::class.java))
        }

        findViewById<View>(R.id.btn_nav_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", "Technician")
            startActivity(intent)
        }
        
        // Start with empty state
        loadRecentScans()
    }


    override fun onResume() {
        super.onResume()
        loadSavedImage()
        loadProfileData() // Fetch latest profile info including photo URL
        loadRecentScans()
    }

    private fun loadDashboardStats() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val technicianId = prefs.getInt("technician_id", -1)

        if (technicianId == -1) return

        ApiClient.apiService.getTechnicianDashboardStats(technicianId).enqueue(object : Callback<TechnicianDashboardStats> {
            override fun onResponse(call: Call<TechnicianDashboardStats>, response: Response<TechnicianDashboardStats>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<TextView>(R.id.tv_stats_today).text = stats.todayCount.toString()
                    findViewById<TextView>(R.id.tv_stats_pending).text = stats.pendingCount.toString()
                    findViewById<TextView>(R.id.tv_stats_total).text = stats.totalCount.toString()
                }
            }

            override fun onFailure(call: Call<TechnicianDashboardStats>, t: Throwable) {
                android.util.Log.e("Dashboard", "Failed to load dashboard stats: ${t.message}")
            }
        })
    }

    private fun loadRecentScans() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val technicianId = prefs.getInt("technician_id", -1)

        if (technicianId == -1) {
            return
        }

        ApiClient.apiService.getScanHistory(technicianId).enqueue(object : Callback<List<ScanHistoryItem>> {
            override fun onResponse(call: Call<List<ScanHistoryItem>>, response: Response<List<ScanHistoryItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    val scans = response.body()!!.sortedByDescending { it.id }
                    updateRecentScansUI(scans)
                }
            }

            override fun onFailure(call: Call<List<ScanHistoryItem>>, t: Throwable) {
                android.util.Log.e("Dashboard", "Failed to load recent scans: ${t.message}")
            }
        })
    }

    private fun updateRecentScansUI(scans: List<ScanHistoryItem>) {
        val scan1View = findViewById<View>(R.id.recent_scan_1)
        val scan2View = findViewById<View>(R.id.recent_scan_2)
        val llEmptyState = findViewById<View>(R.id.ll_scan_empty_state)

        // Update Stats based on History
        updateStatsFromHistory(scans)

        if (scans.isEmpty()) {
            scan1View.visibility = View.GONE
            scan2View.visibility = View.GONE
            llEmptyState.visibility = View.VISIBLE
        } else {
            llEmptyState.visibility = View.GONE
            
            populateScanView(scan1View, scans[0])
            scan1View.visibility = View.VISIBLE

            if (scans.size > 1) {
                populateScanView(scan2View, scans[1])
                scan2View.visibility = View.VISIBLE
            } else {
                scan2View.visibility = View.GONE
            }
        }
    }

    private fun updateStatsFromHistory(scans: List<ScanHistoryItem>) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        
        val todayCount = scans.count { it.date.startsWith(today) }
        val pendingCount = scans.count { 
            it.status.equals("Started", ignoreCase = true) || 
            it.status.equals("Processing", ignoreCase = true) 
        }
        val totalCount = scans.size

        findViewById<TextView>(R.id.tv_stats_today).text = todayCount.toString()
        findViewById<TextView>(R.id.tv_stats_pending).text = pendingCount.toString()
        findViewById<TextView>(R.id.tv_stats_total).text = totalCount.toString()
        
        android.util.Log.d("Dashboard", "Stats Calculated - Today: $todayCount, Pending: $pendingCount, Total: $totalCount")
    }

    private fun populateScanView(view: View, scan: ScanHistoryItem) {
        view.findViewById<TextView>(R.id.tv_patient_name).text = scan.patientName
        view.findViewById<TextView>(R.id.tv_mrn).text = scan.mrn
        val tvStatus = view.findViewById<TextView>(R.id.tv_status)
        tvStatus.text = scan.status
        
        if (scan.status.equals("Processing", ignoreCase = true)) {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_light_orange)
            tvStatus.setTextColor(android.graphics.Color.parseColor("#B45309"))
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_light_green)
            tvStatus.setTextColor(android.graphics.Color.parseColor("#065F46"))
        }
        
        view.findViewById<TextView>(R.id.tv_time).text = scan.date
        
        view.setOnClickListener {
            openScanDetails(scan.id, scan.patientName ?: "", scan.mrn ?: "")
        }
    }

    private fun loadSavedImage() {
        val uriStr = getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
            .getString("profile_image_uri", null)
        val imageView = findViewById<ImageView>(R.id.iv_profile_header)
        if (uriStr != null) {
            try {
                val uri = android.net.Uri.parse(uriStr)
                imageView.setImageURI(uri)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.imageTintList = null
            } catch (e: SecurityException) {
                e.printStackTrace()
                loadDefaultPlaceholder(imageView)
            }
        } else {
            loadDefaultPlaceholder(imageView)
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val technicianEmail = prefs.getString("technician_email", null)
        if (technicianEmail == null) return

        ApiClient.apiService.getTechnicianProfile(technicianEmail).enqueue(object : Callback<TechnicianProfileResponse> {
            override fun onResponse(call: Call<TechnicianProfileResponse>, response: Response<TechnicianProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    if (!profile.profilePhotoUrl.isNullOrEmpty()) {
                        val fullUrl = "${ApiClient.GET_STATIC_URL}${profile.profilePhotoUrl}"
                        loadProfilePhotoFromServer(fullUrl)
                    }
                }
            }
            override fun onFailure(call: Call<TechnicianProfileResponse>, t: Throwable) {}
        })
    }

    private fun loadProfilePhotoFromServer(url: String) {
        val imageView = findViewById<ImageView>(R.id.iv_profile_header)
        Thread {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.doInput = true
                connection.connect()
                val bitmap = android.graphics.BitmapFactory.decodeStream(connection.inputStream)
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.imageTintList = null
                }
            } catch (e: Exception) {
                android.util.Log.e("Dashboard", "Failed to load header photo: ${e.message}")
            }
        }.start()
    }

    private fun loadDefaultPlaceholder(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_person)
        imageView.scaleType = ImageView.ScaleType.CENTER
        imageView.imageTintList = android.content.res.ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.white)
        )
    }

    private fun openScanDetails(id: String, name: String, mrn: String) {
        val intent = Intent(this, ScanDetailsActivity::class.java)
        intent.putExtra("SCAN_CODE", id)
        intent.putExtra("PATIENT_NAME", name)
        intent.putExtra("MRN", mrn)
        startActivity(intent)
    }
}
