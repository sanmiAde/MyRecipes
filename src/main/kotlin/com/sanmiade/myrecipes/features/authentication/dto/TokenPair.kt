package com.sanmiade.myrecipes.features.authentication.dto

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
