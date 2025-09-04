package com.sanmiade.myrecipes.features.authentication.login

import com.auth0.jwt.exceptions.JWTCreationException
import com.sanmiade.myrecipes.features.account.User
import com.sanmiade.myrecipes.features.account.UserRepository
import com.sanmiade.myrecipes.features.authentication.login.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.login.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.login.dto.RegistrationRequest
import com.sanmiade.myrecipes.utils.security.JWTProcessor
import com.sanmiade.myrecipes.utils.security.JWTProperties
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
            logger.warn("Invalid login attempt for user: ${loginRequest.username}")
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
        try {
            if (userRepository.existsByUsername(registrationRequest.username)) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists"
                )
            }

            val encodedPassword = passwordEncoder.encode(registrationRequest.password)

            val user = User(
                username = registrationRequest.username,
                password = encodedPassword,
                roles = listOf("ROLE_USER"),
                email = registrationRequest.email
            )
            userRepository.save(user)

            val userPrincipal = UserPrincipal(
                name = user.username,
                id = user.id!!,
                authorities = user.roles.map { SimpleGrantedAuthority(it) }
            )

            val authentication = UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.authorities
            )
            SecurityContextHolder.getContext().authentication = authentication

            val accessToken = issueToken(userPrincipal.id, jwtProperties.accessTokenExpiry)
            val refreshToken = issueToken(userPrincipal.id, jwtProperties.refreshTokenExpiry)

            return AuthenticationResponse(
                username = userPrincipal.name,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } catch (e: JWTCreationException) {
            logger.error("Failed to create JWT token for user: ${registrationRequest.username}", e)
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "User could not be registered"
            )
        } catch (e: Exception) {
            logger.error("Unexpected error during registration for user: ${registrationRequest.username}", e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
            )
        }
    }


    override fun logout() {
        throw NotImplementedError("Logout not yet implemented")
    }

    private fun issueToken(userId: Long, expiry: Long): String {
        return jwtProcessor.issueToken(userId, listOf("ROLE_USER"), expiry)
    }
}
