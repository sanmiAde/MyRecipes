package com.sanmiade.myrecipes.features.authentication

import com.auth0.jwt.exceptions.JWTCreationException
import com.sanmiade.myrecipes.features.profile.User
import com.sanmiade.myrecipes.features.profile.UserRepository
import com.sanmiade.myrecipes.features.authentication.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.dto.RegistrationRequest
import com.sanmiade.myrecipes.utils.security.JWTProcessor
import com.sanmiade.myrecipes.utils.security.JWTProperties
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtProcessor: JWTProcessor,
    private val jwtProperties: JWTProperties,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationService {

    private val logger = LoggerFactory.getLogger(AuthenticationServiceImpl::class.java)

    override fun login(loginRequest: LoginRequest): AuthenticationResponse {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.username,
                    loginRequest.password
                )
            )
            SecurityContextHolder.getContext().authentication = authentication
            val userPrincipal = authentication.principal as UserPrincipal

            val accessToken = issueToken(userPrincipal.id, jwtProperties.accessTokenExpiry)
            val refreshToken = issueToken(userPrincipal.id, jwtProperties.refreshTokenExpiry)

            AuthenticationResponse(
                username = userPrincipal.name,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } catch (e: BadCredentialsException) {
            logger.warn("Invalid login attempt for user: ${loginRequest.username}", e)
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Password does not match"
            )
        } catch (e: JWTCreationException) {
            logger.error("Failed to create JWT token for user: ${loginRequest.username}", e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
            )
        }
    }

    // learn how exceptions is propergated and handled in kotlin spring boot
    override fun register(registrationRequest: RegistrationRequest): AuthenticationResponse {
        if (!registrationRequest.validatePasswordsMatch()) throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Passwords do not match"
        )

        if (userRepository.existsByUsername(registrationRequest.username)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
        }

        val encodedPassword = passwordEncoder.encode(registrationRequest.password)

        val user = User(
            username = registrationRequest.username,
            password = encodedPassword,
            roles = listOf("ROLE_USER"),
            email = registrationRequest.email
        )
        userRepository.save(user)

        // Reuse login logic to authenticate and generate tokens
        return login(LoginRequest(registrationRequest.username, registrationRequest.password))
    }

    override fun logout() {
        throw NotImplementedError("Logout not yet implemented")
    }

    private fun issueToken(userId: Long, expiry: Long): String {
        return jwtProcessor.issueToken(userId, listOf("ROLE_USER"), expiry)
    }
}
