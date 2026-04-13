package com.simats.cxtriageai

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Query

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

    // --- TECHNICIAN SCANNING FLOW ---

    @POST("technician/register-patient")
    fun createPatient(
        @Body request: CreatePatientRequest
    ): Call<CreatePatientResponse>

    @POST("start-scan/{patient_id}")
    fun startScan(
        @Path("patient_id") patientId: Int,
        @Query("technician_id") technicianId: Int
    ): Call<StartScanResponse>

    @POST("scan-preparation/{scan_id}")
    fun saveScanPreparation(
        @Path("scan_id") scanId: Int,
        @Body request: ScanPreparationRequest
    ): Call<ScanPreparationResponse>

    @Multipart
    @POST("upload-scan/{scan_id}")
    fun uploadScan(
        @Path("scan_id") scanId: Int,
        @Query("doctor_id") doctorId: Int,
        @Part file: MultipartBody.Part
    ): Call<UploadScanResponse>

    @GET("doctors")
    fun getDoctors(): Call<List<Doctor>>

    @POST("create-study/{scan_id}")
    fun createStudy(
        @Path("scan_id") scanId: Int,
        @Query("doctor_id") doctorId: Int
    ): Call<CreateStudyResponse>

    @POST("distribute-study/{study_id}")
    fun distributeStudy(
        @Path("study_id") studyId: Int
    ): Call<DistributeStudyResponse>

    @POST("accept-scan/{scan_id}")
    fun acceptScan(
        @Path("scan_id") scanId: Int
    ): Call<ActionScanResponse>

    @POST("retake-scan/{scan_id}")
    fun retakeScan(
        @Path("scan_id") scanId: Int
    ): Call<ActionScanResponse>

    // --- DOCTOR TRIAGE FLOW ---

    @GET("critical-alerts")
    fun getCriticalAlerts(
        @Query("doctor_id") doctorId: Int? = null,
        @Query("priority") priority: String? = null
    ): Call<List<AlertResponse>>

    @GET("triage-dashboard")
    fun getTriageDashboard(
        @Query("doctor_id") doctorId: Int
    ): Call<TriageDashboardResponse>

    @GET("case-queue")
    fun getCaseQueue(
        @Query("doctor_id") doctorId: Int? = null,
        @Query("priority") priority: String? = null
    ): Call<List<TriageCaseResponse>>

    @GET("case/{case_id}")
    fun getCaseDetails(
        @Path("case_id") caseId: Int,
        @Query("doctor_id") doctorId: Int
    ): Call<TriageCaseResponse>

    @PUT("case/{case_id}/accept")
    fun acceptCase(
        @Path("case_id") caseId: Int,
        @Body body: Map<String, String>
    ): Call<ResponseBody>

    @PUT("case/{case_id}/reject")
    fun rejectCase(
        @Path("case_id") caseId: Int,
        @Body request: SignRequest
    ): Call<CaseActionResponse>

    @PUT("case/{case_id}/edit-notes")
    fun editCaseNotes(
        @Path("case_id") caseId: Int,
        @Body request: EditNotesRequest
    ): Call<CaseActionResponse>

    @PUT("case/{case_id}/finalize-sign")
    fun finalizeCase(
        @Path("case_id") caseId: Int,
        @Body body: Map<String, String>
    ): Call<ResponseBody>

    @GET("case/{case_id}/generate-report")
    fun generateReport(
        @Path("case_id") caseId: Int
    ): Call<ResponseBody>

    @GET("case/{case_id}/download-pdf")
    fun downloadPdf(
        @Path("case_id") caseId: Int
    ): Call<ResponseBody>

    @GET("case/{case_id}/download-pdf")
    fun downloadReport(
        @Path("case_id") caseId: Int
    ): Call<ResponseBody>

    @GET("case/{case_id}/report-preview")
    fun getReportPreview(
        @Path("case_id") caseId: Int
    ): Call<ReportPreviewResponse>

    @GET("case-history")
    fun getCaseHistory(
        @Query("doctor_id") doctorId: Int? = null
    ): Call<List<TriageCaseResponse>>

    @GET("case/{case_id}/patient-history")
    fun getPatientHistory(
        @Path("case_id") caseId: Int
    ): Call<List<TriageCaseResponse>>

    @GET("technician/profile/{email}")
    fun getTechnicianProfile(
        @Path("email") email: String
    ): Call<TechnicianProfileResponse>

    @PUT("technician/update-profile")
    fun updateTechnicianProfile(
        @Body request: UpdateTechnicianProfileRequest
    ): Call<UpdateTechnicianProfileResponse>

    @POST("technician/send-feedback")
    fun sendFeedback(
        @Body request: FeedbackRequest
    ): Call<FeedbackResponse>

    @POST("technician/logout/{technician_id}")
    fun logoutTechnician(
        @Path("technician_id") technicianId: Int
    ): Call<LogoutResponse>

    @GET("scan/{scan_code}")
    fun getScanDetails(
        @Path("scan_code") scanId: String
    ): Call<ScanDetailResponse>

    @GET("scan-history")
    fun getScanHistory(
        @Query("technician_id") technicianId: Int
    ): Call<List<ScanHistoryItem>>

    @GET("technician/dashboard-stats/{technician_id}")
    fun getTechnicianDashboardStats(
        @Path("technician_id") technicianId: Int
    ): Call<TechnicianDashboardStats>

    @Multipart
    @POST("technician/upload-photo/{email}")
    fun uploadTechnicianPhoto(
        @Path("email") email: String,
        @Part file: MultipartBody.Part
    ): Call<UpdateTechnicianProfileResponse>

    @POST("ai-chat")
    fun sendChatMessage(
        @Body request: AiChatRequest
    ): Call<AiChatResponse>

    @GET("view-report/{case_id}")
    fun getViewReportDetails(
        @Path("case_id") caseId: Int
    ): Call<TriageCaseResponse>

    @GET("case/{case_id}/report-sent")
    fun getReportSentDetails(
        @Path("case_id") caseId: Int
    ): Call<ReportResponse>

    @GET("doctor/profile/{email}")
    fun getDoctorProfile(
        @Path("email") email: String
    ): Call<DoctorProfileResponse>

    @PUT("doctor/update-profile")
    fun updateDoctorProfile(
        @Body request: UpdateDoctorProfileRequest
    ): Call<UpdateDoctorProfileResponse>

    @Multipart
    @POST("doctor/upload-photo/{email}")
    fun uploadDoctorPhoto(
        @Path("email") email: String,
        @Part file: MultipartBody.Part
    ): Call<UpdateDoctorProfileResponse>
}
