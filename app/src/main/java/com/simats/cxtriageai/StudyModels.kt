package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class CreateStudyResponse(
    val message: String? = null,
    @SerializedName("study_id") val studyId: Int? = null,
    @SerializedName("session_id") val sessionId: String? = null,
    val progress: Int? = null,
    val status: String? = null
)

data class DistributeStudyResponse(
    val message: String? = null,
    @SerializedName("study_id") val studyId: Int? = null,
    val status: String? = null
)
