package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String,
    val role: String,
    val doctor: Doctor?,
    val technician: Technician?
)

data class Doctor(
    @SerializedName(value = "doctor_id", alternate = ["id"]) val doctor_id: Int,
    val name: String
)

data class Technician(
    @SerializedName(value = "id", alternate = ["technician_id"]) val id: Int,
    val name: String
)