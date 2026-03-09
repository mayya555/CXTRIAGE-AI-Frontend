package com.simats.cxtriageai

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThemeActivity : AppCompatActivity() {

    private lateinit var containerLight: android.view.View
    private lateinit var containerDark: android.view.View
    private lateinit var containerSystem: android.view.View
    private lateinit var ivCheckLight: ImageView
    private lateinit var ivCheckDark: ImageView
    private lateinit var ivCheckSystem: ImageView
    private lateinit var tvLabelLight: android.widget.TextView
    private lateinit var tvLabelDark: android.widget.TextView
    private lateinit var tvLabelSystem: android.widget.TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_theme)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.theme_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Init Views
        containerLight = findViewById(R.id.container_light)
        containerDark = findViewById(R.id.container_dark)
        containerSystem = findViewById(R.id.container_system)
        ivCheckLight = findViewById(R.id.iv_check_light)
        ivCheckDark = findViewById(R.id.iv_check_dark)
        ivCheckSystem = findViewById(R.id.iv_check_system)
        tvLabelLight = findViewById(R.id.tv_label_light)
        tvLabelDark = findViewById(R.id.tv_label_dark)
        tvLabelSystem = findViewById(R.id.tv_label_system)

        // Load current theme
        loadCurrentTheme()

        // Set Click Listeners
        findViewById<android.widget.LinearLayout>(R.id.card_light).setOnClickListener {
            setAppTheme(AppCompatDelegate.MODE_NIGHT_NO, "light")
        }

        findViewById<android.widget.LinearLayout>(R.id.card_dark).setOnClickListener {
            setAppTheme(AppCompatDelegate.MODE_NIGHT_YES, "dark")
        }

        findViewById<android.widget.LinearLayout>(R.id.card_system).setOnClickListener {
            setAppTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, "system")
        }

        // Header Navigation
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadCurrentTheme() {
        val theme = sharedPreferences.getString("theme", "system")
        updateCheckmarks(theme)
    }

    private fun setAppTheme(mode: Int, themeName: String) {
        // Save preference
        sharedPreferences.edit().putString("theme", themeName).apply()

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(mode)

        // Update UI
        updateCheckmarks(themeName)
    }

    private fun updateCheckmarks(theme: String?) {
        // Reset all
        containerLight.setBackgroundResource(R.drawable.bg_theme_card_unselected)
        containerDark.setBackgroundResource(R.drawable.bg_theme_card_unselected)
        containerSystem.setBackgroundResource(R.drawable.bg_theme_card_unselected)
        
        ivCheckLight.visibility = View.GONE
        ivCheckDark.visibility = View.GONE
        ivCheckSystem.visibility = View.GONE

        tvLabelLight.setTextColor(android.graphics.Color.parseColor("#64748B"))
        tvLabelDark.setTextColor(android.graphics.Color.parseColor("#64748B"))
        tvLabelSystem.setTextColor(android.graphics.Color.parseColor("#64748B"))
        tvLabelLight.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvLabelDark.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvLabelSystem.setTypeface(null, android.graphics.Typeface.NORMAL)

        val tealColor = android.graphics.Color.parseColor("#0F766E")

        when (theme) {
            "light" -> {
                containerLight.setBackgroundResource(R.drawable.bg_theme_card_selected)
                ivCheckLight.visibility = View.VISIBLE
                tvLabelLight.setTextColor(tealColor)
                tvLabelLight.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            "dark" -> {
                containerDark.setBackgroundResource(R.drawable.bg_theme_card_selected)
                ivCheckDark.visibility = View.VISIBLE
                tvLabelDark.setTextColor(tealColor)
                tvLabelDark.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            "system" -> {
                containerSystem.setBackgroundResource(R.drawable.bg_theme_card_selected)
                ivCheckSystem.visibility = View.VISIBLE
                tvLabelSystem.setTextColor(tealColor)
                tvLabelSystem.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
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
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
