package com.simats.cxtriageai

data class ReportResponse(
    val file_name: String,
    val file_size_bytes: Long,
    val download_url: String,
    val generated_at: String
)
