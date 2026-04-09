package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class CreatePatientRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("mrn") val mrn: String,
    @SerializedName("reason_for_xray") val reasonForXray: String,
    @SerializedName("height") val height: String?,
    @SerializedName("weight") val weight: String?,
    @SerializedName("blood_type") val bloodType: String?,
    @SerializedName("technician_id") val technicianId: Int
)

data class CreatePatientResponse(
    @SerializedName("patient_id") val patientId: Int, // Backend now sends patient_id
    val message: String
)

data class PatientDetailResponse(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    val gender: String,
    val mrn: String,
    @SerializedName("patient_code") val patientCode: String
)
