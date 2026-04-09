package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class TechnicianProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("employee_id") val employeeId: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("profile_photo_url") val profilePhotoUrl: String?
)

data class UpdateTechnicianProfileRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("email") val email: String
)

data class UpdateTechnicianProfileResponse(
    @SerializedName("message") val message: String
)

data class FeedbackRequest(
    @SerializedName("technician_id") val technicianId: Int,
    @SerializedName("feedback_type") val feedbackType: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("description") val description: String
)

data class FeedbackResponse(
    @SerializedName("message") val message: String
)



data class LogoutResponse(
    @SerializedName("message") val message: String
)
