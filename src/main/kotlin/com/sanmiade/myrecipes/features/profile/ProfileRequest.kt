package com.sanmiade.myrecipes.features.profile

import jakarta.validation.constraints.NotBlank

class ProfileRequest(
    @field:NotBlank(message = "username is mandatory")
    val username: String,
    @field:NotBlank(message = "username is mandatory")
    val email: String
)