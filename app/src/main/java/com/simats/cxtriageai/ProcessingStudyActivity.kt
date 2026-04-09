package com.simats.cxtriageai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProcessingStudyActivity : AppCompatActivity() {

    private lateinit var progressBarDicom: ProgressBar
    private lateinit var progressCircular: ProgressBar
    private lateinit var tvProgressPercent: TextView

    private val handler = Handler(Looper.getMainLooper())

    private var dicomProgress = 0
    private var caseId: Int = -1
    private var aiResponse: UploadScanResponse? = null

    private val dicomRunnable = object : Runnable {
        override fun run() {
            if (dicomProgress < 100) {
                dicomProgress += 5
                progressBarDicom.progress = dicomProgress
                handler.postDelayed(this, 100)
            } else {
                performUpload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_processing_study)

        val root = findViewById<android.view.View>(R.id.processing_study_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        progressBarDicom = findViewById(R.id.progress_bar_dicom)
        progressCircular = findViewById(R.id.progress_circular)
        tvProgressPercent = findViewById(R.id.tv_progress_percent)

        val doctorName = intent.getStringExtra("AI_DOCTOR_NAME") ?: "selected doctor"
        findViewById<TextView>(R.id.tv_forward_info).text = 
            "Technician view does not display diagnostic results.\nData is encrypted and forwarded to $doctorName for review."

        handler.post(dicomRunnable)
    }

    private fun performUpload() {

        val scanId = intent.getIntExtra("SCAN_ID", -1)
        if (scanId == -1) {
            Toast.makeText(this, "Missing Scan ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val doctorId = intent.getIntExtra("AI_DOCTOR_ID", -1)
        if (doctorId <= 0) {
            Toast.makeText(this, "Invalid Doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ GET REAL FILE FROM INTENT
        val fileUriString = intent.getStringExtra("FILE_URI")

        if (fileUriString == null) {
            Toast.makeText(this, "File missing", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse(fileUriString)
        val file = getFileFromUri(uri)

        Log.d("FILE_CHECK", "file size = ${file.length()}")

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        Log.d("UPLOAD_DEBUG", "scanId=$scanId doctorId=$doctorId")

        ApiClient.apiService.uploadScan(scanId, doctorId, body)
            .enqueue(object : Callback<UploadScanResponse> {

                override fun onResponse(
                    call: Call<UploadScanResponse>,
                    response: Response<UploadScanResponse>
                ) {

                    if (!response.isSuccessful) {
                        Toast.makeText(this@ProcessingStudyActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                        Log.e("UPLOAD_ERROR", "Code: ${response.code()}")
                        return
                    }

                    val bodyData = response.body()

                    if (bodyData == null) {
                        Toast.makeText(this@ProcessingStudyActivity, "Empty response", Toast.LENGTH_SHORT).show()
                        return
                    }

                    aiResponse = bodyData
                    caseId = bodyData.caseId

                    Log.d("CASE_DEBUG", "caseId = $caseId")
                    Log.d("DEBUG", "STEP1 caseId: $caseId")

                    if (caseId <= 0) {
                        Toast.makeText(this@ProcessingStudyActivity, "Invalid Case ID", Toast.LENGTH_SHORT).show()
                        return
                    }

                    progressCircular.progress = 100
                    tvProgressPercent.text = "100%"

                    goToNextScreen()
                }

                override fun onFailure(call: Call<UploadScanResponse>, t: Throwable) {
                    Toast.makeText(this@ProcessingStudyActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    Log.e("UPLOAD_ERROR", t.message ?: "")
                }
            })
    }

    private fun goToNextScreen() {

        val nextIntent = Intent(this, ProcessingSuccessActivity::class.java)

        nextIntent.putExtra("SCAN_ID", intent.getIntExtra("SCAN_ID", -1))
        nextIntent.putExtra("CASE_ID", caseId)

        nextIntent.putExtra("AI_DISEASE", aiResponse?.disease)
        nextIntent.putExtra("AI_CONFIDENCE", aiResponse?.confidence)
        nextIntent.putExtra("AI_PRIORITY", aiResponse?.priority)

        nextIntent.putExtra("AI_DOCTOR_NAME", intent.getStringExtra("AI_DOCTOR_NAME"))
        nextIntent.putExtra("AI_DOCTOR_ID", intent.getIntExtra("AI_DOCTOR_ID", -1))

        startActivity(nextIntent)
        finish()
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)

        return file
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}