package com.simats.cxtriageai

import com.google.gson.annotations.SerializedName

data class TechnicianDashboardStats(
    @SerializedName("today_count") val todayCount: Int,
    @SerializedName("pending_count") val pendingCount: Int,
    @SerializedName("total_count") val totalCount: Int
)
