package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
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

        setupHospitalCard(R.id.card_simats, "SIMATS", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_saveetha, "Saveetha Dental College", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_saveetha_medical, "Saveetha Medical College", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_apollo, "Apollo Hospitals", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_fortis, "Fortis Healthcare", "Mumbai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_max, "Max Healthcare", "Delhi, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_manipal, "Manipal Hospitals", "Bangalore, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_aiims, "AIIMS Delhi", "New Delhi, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_srm, "SRM Medical College", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_mgm, "MGM Healthcare", "Chennai, India", R.drawable.ic_apartment_theme)
        setupHospitalCard(R.id.card_cmc, "CMC Vellore", "Vellore, India", R.drawable.ic_apartment_theme)
        
        setupAddHospitalCard()
    }

    private fun setupAddHospitalCard() {
        val card = findViewById<View>(R.id.card_add_hospital)
        card.findViewById<TextView>(R.id.tv_hospital_name).text = "Other / Add Facility"
        card.findViewById<TextView>(R.id.tv_hospital_location).text = "Your custom location"
        card.findViewById<ImageView>(R.id.iv_hospital_icon).setImageResource(R.drawable.ic_apartment_theme)

        card.setOnClickListener {
            showAddHospitalDialog()
        }
    }

    private fun showAddHospitalDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Your Hospital")
        builder.setMessage("Please enter the name of your medical facility:")

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setPadding(40, 20, 40, 20)
        builder.setView(input)

        builder.setPositiveButton("Continue") { dialog, _ ->
            val customName = input.text.toString().trim()
            if (customName.isNotEmpty()) {
                val intent = Intent(this, RoleSelectionActivity::class.java)
                intent.putExtra("HOSPITAL_NAME", customName)
                startActivity(intent)
            } else {
                input.error = "Name cannot be empty"
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
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
