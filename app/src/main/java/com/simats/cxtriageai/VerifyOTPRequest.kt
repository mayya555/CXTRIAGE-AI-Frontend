package com.simats.cxtriageai

data class VerifyOTPRequest(
    val email: String,
    val otp: String
)
