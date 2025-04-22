package edu.ap.be.replenishmachine.model

data class ApiResponse<T>(
    val appCode: String,
    val success: Boolean,
    val errorMessage: String?,
    val result: T,
    val date: String
)