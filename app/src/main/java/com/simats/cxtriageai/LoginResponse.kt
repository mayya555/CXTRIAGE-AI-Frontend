package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("doctor") val doctor: RegistrationResponse? = null,
    @SerializedName("technician") val technician: RegistrationResponse? = null,
    @SerializedName("detail") val detail: String? = null
)
