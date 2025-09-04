package com.sanmiade.myrecipes.features.authentication.login

import com.sanmiade.myrecipes.features.authentication.login.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.login.dto.LoginRequest

interface AuthenticationService {

    fun login(loginRequest: LoginRequest): AuthenticationResponse

    fun register(loginRequest: LoginRequest): AuthenticationResponse

    fun logout()
}