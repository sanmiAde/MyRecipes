package com.sanmiade.myrecipes.features.authentication

import com.sanmiade.myrecipes.features.authentication.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.dto.RegistrationRequest
import com.sanmiade.myrecipes.features.authentication.dto.TokenPair

interface AuthenticationService {

    fun login(loginRequest: LoginRequest): AuthenticationResponse

    fun register(registrationRequest: RegistrationRequest): AuthenticationResponse

    fun logout(refreshToken: String)

    fun refreshAccessToken(refreshToken: String): TokenPair
}