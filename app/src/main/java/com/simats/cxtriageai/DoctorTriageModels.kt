package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

// Request Models
data class SignRequest(
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("doctor_id") val doctorId: Int
)

data class EditNotesRequest(
    @SerializedName("doctor_id") val doctorId: Int,
    @SerializedName("doctor_notes") val doctorNotes: String
)

data class ConfirmDiagnosisRequest(
    @SerializedName("doctor_name") val doctorName: String,
    val notes: String? = null
)

data class FinalizeRequest(
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("doctor_id") val doctorId: Int, // Added
    @SerializedName("impression") val impression: String,
    @SerializedName("recommendation") val recommendation: String
)

// Response Models
data class TriageCaseResponse(
    @SerializedName("case_id") val id: Int,
    @SerializedName("case_code") val caseCode: String?,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("patient_age") val patientAge: Int?,
    val diagnosis: String?,
    val priority: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("ai_findings") val aiFindings: String?,
    @SerializedName("ai_result") val aiResult: String?,
    @SerializedName("ai_confidence") val aiConfidence: String?, // Changed from Double
    @SerializedName("final_diagnosis") val finalDiagnosis: String?,
    @SerializedName("doctor_notes") val doctorNotes: String?,
    val decision: String?,
    val status: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    val gender: String?,
    val mrn: String?,
    val height: String?,
    val weight: String?,
    @SerializedName("blood_type") val bloodType: String?
)

data class AlertResponse(
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("case_code") val caseCode: String?,
    @SerializedName("patient_name") val patientName: String?,
    val priority: String?,
    @SerializedName("ai_result") val aiResult: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class ApiResponse(
    val message: String?,
    val status: String? = null
)

data class CaseActionResponse(
    val message: String
)

data class ReportPreviewResponse(
    @SerializedName("report_id") val reportId: String?,
    val patient: String?,
    val findings: String?,
    val impression: String?,
    @SerializedName("signed_by") val signedBy: String?,
    val finalized: Boolean?
)

data class TriageDashboardResponse(
    @SerializedName("total_cases") val totalCases: Int,
    @SerializedName("pending_cases") val pendingCases: Int,
    @SerializedName("completed_cases") val completedCases: Int,
    @SerializedName("critical_cases") val criticalCases: Int,
    @SerializedName("urgent_cases") val urgentCases: Int
)
