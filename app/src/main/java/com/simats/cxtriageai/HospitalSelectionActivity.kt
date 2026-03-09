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

class HospitalSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hospital_selection)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.hospital_selection_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupHospitalCard(R.id.card_apollo, "Apollo Hospitals", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_fortis, "Fortis Healthcare", "Mumbai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_max, "Max Healthcare", "Delhi, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_manipal, "Manipal Hospitals", "Bangalore, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_aiims, "AIIMS Delhi", "New Delhi, India", R.drawable.ic_apartment_theme)
    }

    private fun setupHospitalCard(cardId: Int, name: String, location: String, iconRes: Int) {
        val card = findViewById<View>(cardId)
        card.findViewById<TextView>(R.id.tv_hospital_name).text = name
        card.findViewById<TextView>(R.id.tv_hospital_location).text = location
        card.findViewById<ImageView>(R.id.iv_hospital_icon).setImageResource(iconRes)
        
        card.setOnClickListener {
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.putExtra("HOSPITAL_NAME", name)
            startActivity(intent)
        }
    }
}
