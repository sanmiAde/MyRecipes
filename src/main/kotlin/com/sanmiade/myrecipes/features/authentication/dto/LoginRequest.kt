package com.sanmiade.myrecipes.features.authentication.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "username is mandatory")
    val username: String,
    @field:NotBlank(message = "password is mandatory")
    val password: String
)