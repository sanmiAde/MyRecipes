package com.sanmiade.myrecipes.features.authentication.login.dto

import jakarta.validation.constraints.NotBlank

data class RegistrationRequest(
    @field:NotBlank(message = "Name is mandatory")
    val username: String,
    @field:NotBlank(message = "Name is mandatory")
    val password: String,
    @field:NotBlank(message = "Name is mandatory")
    val confirmPassword: String) {

    fun validatePasswordsMatch() {
        if (password != confirmPassword) {
            throw IllegalArgumentException("Passwords do not match")
        }
    }
}