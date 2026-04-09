package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class ScanDetailResponse(
    @SerializedName("scan_id") val scanId: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("mrn") val mrn: String,
    @SerializedName("date_of_birth") val dob: String,
    val gender: String,
    @SerializedName("scan_date") val scanDate: String,
    val technician: String,
    @SerializedName("view_type") val viewType: String,
    val orientation: String,
    @SerializedName("study_id") val studyId: String,
    val status: String,
    val disease: String? = null,
    val confidence: String? = null,
    val timeline: List<TimelineStep>? = null
)

data class TimelineStep(
    val title: String,
    val timestamp: String,
    val status: String // "Completed", "In Progress", "Pending"
)
