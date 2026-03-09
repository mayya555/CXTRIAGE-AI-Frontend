package com.simats.cxtriageai

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AiChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var rvChat: RecyclerView
    private lateinit var etInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_chat)

        val rootView = findViewById<View>(R.id.rv_chat_messages).parent as View
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, ime.bottom + systemBars.bottom)
            insets
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvChat = findViewById(R.id.rv_chat_messages)
        etInput = findViewById(R.id.et_message_input)
        val btnSend = findViewById<ImageButton>(R.id.btn_send)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Initial welcome message if list is empty
        if (messages.isEmpty()) {
            addMessage(ChatMessage("Hello! I am Cortex AI. How can I assist you with your cases today?", false))
        }

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(text: String) {
        addMessage(ChatMessage(text, true))
        etInput.text.clear()

        // Simulate AI thinking and typing
        Handler(Looper.getMainLooper()).postDelayed({
            val response = getMockResponse(text)
            addMessage(ChatMessage(response, false))
        }, 1500)
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.smoothScrollToPosition(messages.size - 1)
    }

    private fun getMockResponse(input: String): String {
        val lowerInput = input.lowercase()
        return when {
            lowerInput.contains("hello") || lowerInput.contains("hi") -> "Hello there! Ready to review some scans?"
            lowerInput.contains("pneumothorax") -> "Cases with Pneumothorax are flagged as CRITICAL. We have 2 pending cases with high confidence."
            lowerInput.contains("waiting") || lowerInput.contains("time") -> "Average waiting time for critical cases is currently 12 minutes."
            lowerInput.contains("case") -> "I can help you prioritize cases. Would you like to see the Urgency Queue?"
            else -> "I understand. I'm analyzing the latest data on that. Please check the dashboard for real-time metrics."
        }
    }
}
