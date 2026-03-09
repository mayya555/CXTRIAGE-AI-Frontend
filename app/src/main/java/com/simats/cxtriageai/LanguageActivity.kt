package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LanguageActivity : AppCompatActivity() {

    // Maps to hold views for easier iteration
    private val languageCards = mutableMapOf<String, CardView>()
    private val checkMarks = mutableMapOf<String, ImageView>()
    private val badgeBgs = mutableMapOf<String, View>()
    private val badgeTexts = mutableMapOf<String, android.widget.TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_language)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.language_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Header Navigation
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize Views according to the design list
        setupLanguageItem("english", R.id.card_english, R.id.iv_check_english, R.id.iv_badge_bg_english, R.id.tv_badge_english)
        setupLanguageItem("spanish", R.id.card_spanish, R.id.iv_check_spanish, R.id.iv_badge_bg_spanish, R.id.tv_badge_spanish)
        setupLanguageItem("chinese", R.id.card_chinese, R.id.iv_check_chinese, R.id.iv_badge_bg_chinese, R.id.tv_badge_chinese)
        setupLanguageItem("french", R.id.card_french, R.id.iv_check_french, R.id.iv_badge_bg_french, R.id.tv_badge_french)
        setupLanguageItem("german", R.id.card_german, R.id.iv_check_german, R.id.iv_badge_bg_german, R.id.tv_badge_german)
        setupLanguageItem("hindi", R.id.card_hindi, R.id.iv_check_hindi, R.id.iv_badge_bg_hindi, R.id.tv_badge_hindi)
        setupLanguageItem("japanese", R.id.card_japanese, R.id.iv_check_japanese, R.id.iv_badge_bg_japanese, R.id.tv_badge_japanese)
        setupLanguageItem("korean", R.id.card_korean, R.id.iv_check_korean, R.id.iv_badge_bg_korean, R.id.tv_badge_korean)

        // Initial Selection
        selectLanguage("english")

        // Search Logic
        setupSearch()
    }

    private fun setupSearch() {
        val etSearch = findViewById<android.widget.EditText>(R.id.et_search)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLanguages(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterLanguages(query: String) {
        val lowerQuery = query.lowercase()
        for ((key, card) in languageCards) {
            val langName = getLanguageName(key).lowercase()
            if (langName.contains(lowerQuery)) {
                card.visibility = View.VISIBLE
            } else {
                card.visibility = View.GONE
            }
        }
    }

    private fun getLanguageName(key: String): String {
        return when (key) {
            "english" -> "English (US)"
            "spanish" -> "Spanish (Español)"
            "chinese" -> "Chinese (Simplified)"
            "french" -> "French (Français)"
            "german" -> "German (Deutsch)"
            "hindi" -> "Hindi (हिंदी)"
            "japanese" -> "Japanese (日本語)"
            "korean" -> "Korean (한국어)"
            else -> key
        }
    }

    private fun setupLanguageItem(key: String, cardId: Int, checkId: Int, badgeBgId: Int, badgeTextId: Int?) {
        val card = findViewById<CardView>(cardId)
        val check = findViewById<ImageView>(checkId)
        val badgeBg = findViewById<View>(badgeBgId)
        val badgeText = badgeTextId?.let { findViewById<android.widget.TextView>(it) }

        languageCards[key] = card
        checkMarks[key] = check
        badgeBgs[key] = badgeBg
        if (badgeText != null) badgeTexts[key] = badgeText

        card.setOnClickListener {
            selectLanguage(key)
        }
    }

    private fun selectLanguage(selectedKey: String) {
        // Reset all
        for (key in languageCards.keys) {
            checkMarks[key]?.visibility = View.INVISIBLE
            languageCards[key]?.setCardElevation(1f)
            badgeBgs[key]?.setBackgroundResource(R.drawable.bg_circle_gray_light)
            badgeTexts[key]?.setTextColor(android.graphics.Color.parseColor("#64748B"))
            
            // For languages without specific badgeText IDs in the setup, 
            // we'll try to find the TextView inside the badgeBg container if needed, 
            // but for now, we'll keep it simple as per setup.
        }

        // Set Selected
        checkMarks[selectedKey]?.visibility = View.VISIBLE
        languageCards[selectedKey]?.setCardElevation(4f)
        badgeBgs[selectedKey]?.setBackgroundResource(R.drawable.bg_circle_teal_light)
        badgeTexts[selectedKey]?.setTextColor(android.graphics.Color.parseColor("#0F766E"))
        
        // Toast the selection
        val langName = when(selectedKey) {
            "english" -> "English"
            "spanish" -> "Spanish"
            "chinese" -> "Chinese"
            "french" -> "French"
            "german" -> "German"
            "hindi" -> "Hindi"
            "japanese" -> "Japanese"
            "korean" -> "Korean"
            else -> selectedKey
        }
        Toast.makeText(this, "$langName selected", Toast.LENGTH_SHORT).show()
    }
}
