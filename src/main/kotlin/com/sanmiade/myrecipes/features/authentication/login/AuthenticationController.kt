package com.sanmiade.myrecipes.features.authentication.login

import com.sanmiade.myrecipes.features.authentication.login.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.login.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.login.dto.RegistrationRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/auth")
class AuthenticationController(private val authenticationService: AuthenticationService) {

    @PostMapping("/login")
    fun login(@Validated @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthenticationResponse> {
        val response = authenticationService.login(loginRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/register")
    fun register(@Validated @RequestBody registrationRequest: RegistrationRequest): ResponseEntity<AuthenticationResponse?> {
        val response = authenticationService.register(registrationRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}