package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class ActionScanResponse(
    val message: String,
    @SerializedName("scan_id") val scanId: Int? = null,
    val status: String? = null
)
