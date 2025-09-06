package com.sanmiade.myrecipes.utils

import java.time.Instant

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val path: String? = null
)
