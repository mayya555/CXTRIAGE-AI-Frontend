package com.simats.cxtriageai

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfilePhotoActivity : AppCompatActivity() {

    private var userRole: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            saveImageUri(it)
            updateProfileImage(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                saveImageUri(it)
                updateProfileImage(it)
                Toast.makeText(this, "Captured Photo Successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_photo)

        userRole = intent.getStringExtra("ROLE")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_photo_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.header_container).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        applyRoleBranding()

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_header_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iv_header_logout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<android.view.View>(R.id.btn_take_photo).setOnClickListener {
            try {
                cameraLauncher.launch(Intent(this, ProfileCameraActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<android.view.View>(R.id.btn_choose_library).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<android.view.View>(R.id.btn_remove_photo).setOnClickListener {
            removeImageUri()
            updateProfileImage(null)
            Toast.makeText(this, "Photo Removed", Toast.LENGTH_SHORT).show()
        }

        loadSavedImage()
        loadProfileData()
    }

    private fun applyRoleBranding() {
        val headerBg = findViewById<android.view.View>(R.id.header_bg)
        val btnTake = findViewById<android.view.View>(R.id.btn_take_photo)
        
        if (userRole?.equals("Doctor", ignoreCase = true) == true || userRole?.equals("Radiologist", ignoreCase = true) == true) {
            headerBg?.setBackgroundColor(Color.parseColor("#10B981"))
            btnTake?.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4F46E5"))
        } else {
            headerBg?.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green))
        }
    }

    private fun showLogoutDialog() {
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_sign_out, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<android.widget.Button>(R.id.btn_confirm_logout).setOnClickListener {
            dialog.dismiss()
            // Clear all session data
            SessionManager.clearUserData(this@ProfilePhotoActivity)
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btn_cancel_logout).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveImageUri(uri: Uri) {
        val fileName = "profile_image.jpg"
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val localFile = File(filesDir, fileName)
            val localUri = Uri.fromFile(localFile)
            
            getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile_image_uri", localUri.toString())
                .apply()

            // Also upload to server for persistence
            uploadPhotoToServer(localFile)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image locally", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPhotoToServer(file: File) {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        if (userRole?.equals("Doctor", ignoreCase = true) == true) {
            val email = prefs.getString("doctor_email", null)
            if (email == null) return

            ApiClient.apiService.uploadDoctorPhoto(email, body).enqueue(object : Callback<UpdateDoctorProfileResponse> {
                override fun onResponse(call: Call<UpdateDoctorProfileResponse>, response: Response<UpdateDoctorProfileResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfilePhotoActivity, "Doctor photo synced!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UpdateDoctorProfileResponse>, t: Throwable) {}
            })
        } else {
            val email = prefs.getString("technician_email", null)
            if (email == null) return

            ApiClient.apiService.uploadTechnicianPhoto(email, body).enqueue(object : Callback<UpdateTechnicianProfileResponse> {
                override fun onResponse(call: Call<UpdateTechnicianProfileResponse>, response: Response<UpdateTechnicianProfileResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfilePhotoActivity, "Technician photo synced!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UpdateTechnicianProfileResponse>, t: Throwable) {}
            })
        }
    }

    private fun removeImageUri() {
        val file = File(filesDir, "profile_image.jpg")
        if (file.exists()) file.delete()
        
        getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .edit()
            .remove("profile_image_uri")
            .apply()
    }

    private fun loadSavedImage() {
        val uriStr = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .getString("profile_image_uri", null)
        if (uriStr != null) {
            updateProfileImage(Uri.parse(uriStr))
        } else {
            updateProfileImage(null)
        }
    }

    private fun updateProfileImage(uri: Uri?) {
        val imageView = findViewById<ImageView>(R.id.iv_profile_large)
        if (uri != null) {
            imageView.setImageURI(uri)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.imageTintList = null
        } else {
            imageView.setImageResource(R.drawable.ic_person)
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.imageTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#CBD5E1")
            )
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        if (userRole?.equals("Doctor", ignoreCase = true) == true) {
            val email = prefs.getString("doctor_email", null)
            if (email == null) return

            ApiClient.apiService.getDoctorProfile(email).enqueue(object : Callback<DoctorProfileResponse> {
                override fun onResponse(call: Call<DoctorProfileResponse>, response: Response<DoctorProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        if (!profile.profilePhotoUrl.isNullOrEmpty()) {
                            val fullUrl = "${ApiClient.GET_STATIC_URL}${profile.profilePhotoUrl}"
                            loadProfilePhotoFromServer(fullUrl)
                        }
                    }
                }
                override fun onFailure(call: Call<DoctorProfileResponse>, t: Throwable) {}
            })
        } else {
            val email = prefs.getString("technician_email", null)
            if (email == null) return

            ApiClient.apiService.getTechnicianProfile(email).enqueue(object : Callback<TechnicianProfileResponse> {
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
    }

    private fun loadProfilePhotoFromServer(url: String) {
        val imageView = findViewById<ImageView>(R.id.iv_profile_large)
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
            } catch (e: Exception) {}
        }.start()
    }
}
