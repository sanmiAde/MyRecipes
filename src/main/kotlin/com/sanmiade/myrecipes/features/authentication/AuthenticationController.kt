package com.sanmiade.myrecipes.features.authentication

import com.sanmiade.myrecipes.features.authentication.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.dto.RegistrationRequest
import com.sanmiade.myrecipes.features.authentication.dto.TokenPair
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/v1/auth")
class AuthenticationController(private val authenticationService: AuthenticationService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthenticationResponse> {
        val response = authenticationService.login(loginRequest)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registrationRequest: RegistrationRequest): ResponseEntity<AuthenticationResponse> {
        val response = authenticationService.register(registrationRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/logout")
    fun logout(@RequestBody payload: Map<String, String>): ResponseEntity<Void> {
        val refreshToken = payload["refreshToken"]
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required")
        authenticationService.logout(refreshToken)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody payload: Map<String, String>): ResponseEntity<TokenPair> {
        val refreshToken = payload["refreshToken"]
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required")
        val tokens = authenticationService.refreshAccessToken(refreshToken)
        return ResponseEntity.ok(tokens)
    }
}