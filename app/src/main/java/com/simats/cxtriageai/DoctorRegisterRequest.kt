package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class DoctorRegisterRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("hospital_email") val hospitalEmail: String,
    @SerializedName("employee_id") val employeeId: String,
    @SerializedName("role_requested") val roleRequested: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirm_password") val confirmPassword: String
)
