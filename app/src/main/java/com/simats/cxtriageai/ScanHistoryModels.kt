package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class ScanHistoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("mrn") val mrn: String?,
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String,
    // Extra fields used in ScanHistoryActivity/Details
    val dateOfBirth: String = "Mar 15, 1985",
    val gender: String = "Female",
    val scanDate: String = "Today, 11:20 AM",
    val technician: String = "James Chen",
    val viewType: String = "PA Chest",
    val orientation: String = "Anterior",
    val studyId: String = "ST-82941"
)
