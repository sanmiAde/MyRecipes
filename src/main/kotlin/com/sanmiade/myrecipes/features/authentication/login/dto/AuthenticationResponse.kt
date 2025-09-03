package com.sanmiade.myrecipes.features.authentication.login.dto

data class AuthenticationResponse(
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
)