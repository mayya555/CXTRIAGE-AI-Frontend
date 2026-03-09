package com.simats.cxtriageai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AiPerformanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_performance)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ai_performance_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<TextView>(R.id.tv_header_title).setPadding(0, systemBars.top, 0, 0)
            findViewById<ImageView>(R.id.iv_back).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Chatbot Logic
        val chatPanel = findViewById<android.view.View>(R.id.chat_panel)
        val chatContainer = findViewById<android.widget.LinearLayout>(R.id.chat_messages_container)
        val chatInput = findViewById<android.widget.EditText>(R.id.et_chat_input)
        val scrollMessages = findViewById<android.widget.ScrollView>(R.id.scroll_messages)

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_chatbot).setOnClickListener {
            chatPanel.visibility = android.view.View.VISIBLE
        }

        findViewById<ImageView>(R.id.iv_close_chat).setOnClickListener {
            chatPanel.visibility = android.view.View.GONE
        }

        findViewById<TextView>(R.id.btn_explain_metric).setOnClickListener {
            addUserMessage("Explain this metric", chatContainer, scrollMessages)
            addBotMessage("This card shows AI sensitivity (ability to detect positive cases) and NPV (confidence when AI says 'Normal'). Your current sensitivity of 94.2% is above the clinical threshold.", chatContainer, scrollMessages)
        }

        findViewById<TextView>(R.id.btn_why_flagged).setOnClickListener {
            addUserMessage("Why is this flagged?", chatContainer, scrollMessages)
            addBotMessage("The system detected motion blur on 2 recent scans. This can decrease AI accuracy, so a repeat image is recommended to ensure patient safety.", chatContainer, scrollMessages)
        }

        findViewById<ImageView>(R.id.iv_send_chat).setOnClickListener {
            val text = chatInput.text.toString()
            if (text.isNotBlank()) {
                addUserMessage(text, chatContainer, scrollMessages)
                chatInput.text.clear()
                
                // Mock generic response
                chatContainer.postDelayed({
                    addBotMessage("I understand your query about '$text'. Based on system logs, all inference services are currently optimal. Would you like me to connect you with IT support?", chatContainer, scrollMessages)
                }, 1000)
            }
        }
    }

    private fun addUserMessage(text: String, container: android.widget.LinearLayout, scroll: android.widget.ScrollView) {
        val bubble = android.widget.TextView(this)
        bubble.text = text
        bubble.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_chat_bubble_user)
        bubble.setPadding(32, 24, 32, 24)
        bubble.setTextColor(android.graphics.Color.parseColor("#1E40AF"))
        bubble.textSize = 13f
        
        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.END
        params.setMargins(80, 16, 0, 16)
        bubble.layoutParams = params
        
        container.addView(bubble)
        scroll.post { scroll.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    private fun addBotMessage(text: String, container: android.widget.LinearLayout, scroll: android.widget.ScrollView) {
        val bubble = android.widget.TextView(this)
        bubble.text = text
        bubble.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_chat_bubble_bot)
        bubble.setPadding(32, 24, 32, 24)
        bubble.setTextColor(android.graphics.Color.parseColor("#334155"))
        bubble.textSize = 13f

        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.START
        params.setMargins(0, 16, 80, 16)
        bubble.layoutParams = params

        container.addView(bubble)
        scroll.post { scroll.fullScroll(android.view.View.FOCUS_DOWN) }
    }
}
