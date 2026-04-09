package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class StartScanResponse(
    @SerializedName("scan_id") val scanId: Int,
    @SerializedName("scan_code") val scanCode: String,
    val status: String
)

data class ScanPreparationRequest(
    @SerializedName("position_patient") val positionPatient: Boolean,
    @SerializedName("proper_distance") val properDistance: Boolean,
    @SerializedName("radiation_safety") val radiationSafety: Boolean,
    @SerializedName("remove_metal") val removeMetal: Boolean,
    @SerializedName("calibration_verified") val calibrationVerified: Boolean,
    @SerializedName("exposure_settings") val exposureSettings: Boolean
)

data class ScanPreparationResponse(
    val message: String,
    @SerializedName("scan_id") val scanId: Int
)

data class UploadScanResponse(
    @SerializedName("case_id")
    val caseId: Int,   // ✅ FIXED
    val message: String,
    val disease: String? = null,
    val confidence: String? = null,
    val priority: String? = null,
    @SerializedName("assigned_doctor_name") val assignedDoctorName: String? = null,
    @SerializedName("assigned_doctor_id") val assignedDoctorId: Int? = null
)

// --- AI Chat Models ---
data class AiChatRequest(
    val message: String
)

data class AiChatResponse(
    val response: String
)
