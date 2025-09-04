package com.sanmiade.myrecipes.features.authentication.login

import com.sanmiade.myrecipes.features.authentication.login.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.login.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.login.dto.RegistrationRequest

interface AuthenticationService {

    fun login(loginRequest: LoginRequest): AuthenticationResponse

    fun register(registrationRequest: RegistrationRequest): AuthenticationResponse

    fun logout()
}