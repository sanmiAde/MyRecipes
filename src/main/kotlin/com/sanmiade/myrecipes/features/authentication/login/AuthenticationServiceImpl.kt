package com.sanmiade.myrecipes.features.authentication.login

import com.auth0.jwt.exceptions.JWTCreationException
import com.sanmiade.myrecipes.features.account.UserRepository
import com.sanmiade.myrecipes.features.authentication.login.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.login.dto.LoginRequest
import com.sanmiade.myrecipes.utils.security.JWTProcessor
import com.sanmiade.myrecipes.utils.security.JWTProperties
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtProcessor: JWTProcessor,
    private val jwtProperties: JWTProperties
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
            throw e
        } catch (e: JWTCreationException) {
            logger.error("Failed to create JWT token for user: ${loginRequest.username}", e)
            throw e
        }
    }

    override fun register(loginRequest: LoginRequest): AuthenticationResponse {
        throw NotImplementedError("Registration not yet implemented")
    }

    override fun logout() {
        throw NotImplementedError("Logout not yet implemented")
    }

    private fun issueToken(userId: Long, expiry: Long): String {
        return jwtProcessor.issueToken(userId, listOf("ROLE_USER"), expiry)
    }
}
