package com.simats.cxtriageai

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("register")
    fun registerDoctor(
        @Body request: DoctorRegisterRequest
    ): Call<RegistrationResponse>

    @POST("technician/register")
    fun registerTechnician(
        @Body request: TechnicianRegisterRequest
    ): Call<RegistrationResponse>

    @POST("doctor/login")
    fun doctorLogin(
        @Body request: DoctorLoginRequest
    ): Call<LoginResponse>

    @POST("technician/login")
    fun technicianLogin(
        @Body request: TechnicianLoginRequest
    ): Call<LoginResponse>

    @POST("login")
    fun login(
        @Body request: Map<String, String>
    ): Call<LoginResponse>

    @POST("forgot-password")
    fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Call<ForgotPasswordResponse>

    @POST("technician/forgot-password")
    fun technicianForgotPassword(
        @Body request: ForgotPasswordRequest
    ): Call<ForgotPasswordResponse>

    @POST("verify-otp")
    fun verifyOTP(
        @Body request: VerifyOTPRequest
    ): Call<VerifyOTPResponse>

    @POST("technician/verify-otp")
    fun technicianVerifyOTP(
        @Body request: VerifyOTPRequest
    ): Call<VerifyOTPResponse>

    @POST("reset-password")
    fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Call<ResetPasswordResponse>

    @POST("technician/reset-password")
    fun technicianResetPassword(
        @Body request: ResetPasswordRequest
    ): Call<ResetPasswordResponse>

}