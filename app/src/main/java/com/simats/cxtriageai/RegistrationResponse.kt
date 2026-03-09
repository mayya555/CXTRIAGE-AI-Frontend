package com.simats.cxtriageai

data class RegistrationResponse(
    val id: Int? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val hospital_email: String? = null,
    val employee_id: String? = null,
    val role_requested: String? = null,
    val detail: String? = null // FastAPI default error field
)
