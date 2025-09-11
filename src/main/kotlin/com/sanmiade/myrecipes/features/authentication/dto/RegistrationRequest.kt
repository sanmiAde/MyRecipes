package com.sanmiade.myrecipes.features.authentication.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegistrationRequest(
    @field:NotBlank(message = "Name is mandatory")
    val username: String,
    @field:NotBlank(message = "Name is mandatory")
    val password: String,
    @field:NotBlank(message = "Name is mandatory")
    val confirmPassword: String,
    @field:Email(message = "Email is mandatory")
    val email: String
) {
    fun validatePasswordsMatch() = password != confirmPassword
}