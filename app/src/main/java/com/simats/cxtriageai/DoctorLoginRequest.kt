package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class DoctorLoginRequest(
    @SerializedName("hospital_email") val hospitalEmail: String,
    @SerializedName("password") val password: String
)