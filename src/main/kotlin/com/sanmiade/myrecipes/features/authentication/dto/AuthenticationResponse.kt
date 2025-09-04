package com.sanmiade.myrecipes.features.authentication.dto

data class AuthenticationResponse(
    val username: String,
    val accessToken: String,
    val refreshToken: String,
)