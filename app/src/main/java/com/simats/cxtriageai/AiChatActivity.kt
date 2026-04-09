package com.simats.cxtriageai

import android.content.Intent
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

        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val doctorId = prefs.getInt("doctor_id", -1)

        if (doctorId <= 0) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        android.util.Log.d("AiChat", "Doctor session active: $doctorId")

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
        // Add user message to UI
        addMessage(ChatMessage(text, true))
        etInput.text.clear()

        // Local keyword check for app-related questions
        val lowerText = text.lowercase()
        val faqs = mapOf(
            listOf("app", "cxtriage", "about you") to "CX Triage AI is an advanced medical imaging analysis application designed to assist radiologists and healthcare professionals.",
            listOf("how to use", "guide", "help", "tutorial") to "To use the app, simply upload an X-ray scan or take a picture of one. The AI will analyze it in seconds and highlight any critical abnormalities.",
            listOf("cost", "price", "subscription", "free") to "CX Triage AI offers different subscription tiers for clinics and hospitals. Please contact our sales team for detailed pricing.",
            listOf("accuracy", "reliable", "trust") to "Our AI models are trained on millions of clinical images and achieve over 95% accuracy in detecting critical respiratory and thoracic conditions.",
            listOf("privacy", "secure", "data", "hipaa") to "We take patient privacy very seriously. All scans are end-to-end encrypted and completely anonymized to ensure full HIPAA compliance.",
            listOf("who", "developed", "creator", "simats") to "CX Triage AI was developed by a specialized team of AI engineers and medical professionals at SIMATS.",
            listOf("contact", "support", "email", "phone") to "You can reach our 24/7 technical support team at support@cxtriageai.com or call our toll-free hotline from the Help & Support menu.",
            listOf("doctor", "radiologist", "replace") to "Our AI is designed to assist, not replace, doctors. It helps prioritize critical cases so radiologists can review them faster.",
            listOf("time", "speed", "fast", "how long") to "The AI analysis is extremely fast and typically returns detailed triage results within 3 to 5 seconds per X-ray.",
            listOf("format", "dicom", "jpeg", "png", "jpg") to "We support standard medical DICOM files as well as high-resolution JPEG and PNG images for analysis.",
            listOf("limit", "maximum", "how many") to "There is no strict limit on the number of scans. Enterprise accounts enjoy unlimited bulk uploads.",
            listOf("offline", "internet", "wifi") to "CX Triage AI requires an active internet connection to securely process images through our cloud-based neural networks.",
            listOf("update", "version", "latest") to "We regularly push over-the-air updates to improve our AI models and add new features. You'll be notified when a new version is available.",
            listOf("language", "english", "spanish", "translate") to "Currently, the app interface and reports are generated in English, but we plan to add multilingual support soon.",
            listOf("password", "reset", "forgot") to "If you forgot your password, please use the 'Forgot Password' link on the login screen to receive a secure reset email.",
            listOf("export", "download", "pdf", "report", "print") to "You can easily export any generated AI report as a secure PDF and print or share it directly from the case dashboard.",
            listOf("error", "bug", "issue", "crash") to "If the app crashes or shows an error, please navigate to Help & Support > Report an Issue, and our team will fix it promptly.",
            listOf("integration", "api", "emr", "ehr", "hospital") to "We offer seamless integration with most standard EMR and EHR hospital systems via our secure REST API.",
            listOf("disease", "conditions", "detect", "abnormalities", "what can you") to "The AI can detect a wide range of thoracic abnormalities including pneumonia, pneumothorax, pleural effusion, and nodules.",
            listOf("legal", "fda", "ce", "approved", "certified") to "Our software undergoes rigorous clinical validation and adheres to strict medical device regulatory standards."
        )

        for ((keywords, response) in faqs) {
            if (keywords.any { lowerText.contains(it) }) {
                Handler(Looper.getMainLooper()).postDelayed({
                    addMessage(ChatMessage(response, false))
                }, 500)
                return
            }
        }

        // Show typing indicator
        val typingMessage = ChatMessage("Typing...", false)
        messages.add(typingMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.smoothScrollToPosition(messages.size - 1)

        val request = AiChatRequest(message = text)
        ApiClient.apiService.sendChatMessage(request).enqueue(object : retrofit2.Callback<AiChatResponse> {
            override fun onResponse(call: retrofit2.Call<AiChatResponse>, response: retrofit2.Response<AiChatResponse>) {
                removeTypingIndicator()

                if (response.isSuccessful && response.body() != null) {
                    val aiResponse = response.body()!!.response
                    addMessage(ChatMessage(aiResponse, false))
                } else {
                    // Fail-safe: Provide a simulated response if API fails
                    addMessage(ChatMessage(getFallbackResponse(text), false))
                }
            }

            override fun onFailure(call: retrofit2.Call<AiChatResponse>, t: Throwable) {
                removeTypingIndicator()
                // Fail-safe: Provide a simulated response if network fails
                addMessage(ChatMessage(getFallbackResponse(text), false))
            }
        })
    }

    private fun removeTypingIndicator() {
        if (messages.isNotEmpty() && messages.last().message == "Typing...") {
            val position = messages.size - 1
            messages.removeAt(position)
            chatAdapter.notifyItemRemoved(position)
        }
    }

    private fun getFallbackResponse(userInput: String): String {
        val fallbacks = listOf(
            "Cortex AI: Analysis of medical cases is currently being processed. I am here to assist with your triage needs.",
            "Cortex AI: I am specialized in thoracic X-ray analysis. Please specify if you need help with a particular scan or diagnosis.",
            "Cortex AI: Triage prioritization is key. I've noted your query and will prioritize critical case summaries accordingly.",
            "Cortex AI: I can assist with identifying abnormalities like Pneumothorax, Pneumonia, or Pleural Effusion.",
            "Cortex AI: Secure data handling is my priority. Your scans and messages are processed with medical-grade encryption."
        )
        return fallbacks.random()
    }


    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.smoothScrollToPosition(messages.size - 1)
    }
}
