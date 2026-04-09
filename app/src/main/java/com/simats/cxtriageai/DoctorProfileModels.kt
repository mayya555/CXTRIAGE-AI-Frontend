package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class DoctorProfileResponse(
    @SerializedName("doctor_id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("hospital_name") val hospitalName: String?,
    @SerializedName("profile_photo_url") val profilePhotoUrl: String?
)

data class UpdateDoctorProfileRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("doctor_id") val doctorId: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("hospital_email") val hospitalEmail: String
)

data class UpdateDoctorProfileResponse(
    @SerializedName("message") val message: String
)
