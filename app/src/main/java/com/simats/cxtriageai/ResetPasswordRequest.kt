package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)
