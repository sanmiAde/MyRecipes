package com.sanmiade.myrecipes.features.authentication

import com.auth0.jwt.exceptions.JWTCreationException
import com.sanmiade.myrecipes.features.profile.UserEntity
import com.sanmiade.myrecipes.features.profile.UserRepository
import com.sanmiade.myrecipes.features.authentication.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.dto.RegistrationRequest
import com.sanmiade.myrecipes.features.authentication.dto.TokenPair
import com.sanmiade.myrecipes.utils.ExpiredRefreshTokenException
import com.sanmiade.myrecipes.utils.InvalidCredentialsException
import com.sanmiade.myrecipes.utils.InvalidRefreshTokenException
import com.sanmiade.myrecipes.utils.PasswordMismatchException
import com.sanmiade.myrecipes.utils.UserAlreadyExistsException
import com.sanmiade.myrecipes.utils.security.JWTProcessor
import com.sanmiade.myrecipes.utils.security.JWTProperties
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.Date

@Component
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtProcessor: JWTProcessor,
    private val jwtProperties: JWTProperties,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
) : AuthenticationService {

    private val logger = LoggerFactory.getLogger(AuthenticationServiceImpl::class.java)

    @Transactional
    override fun login(loginRequest: LoginRequest): AuthenticationResponse {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.username,
                    loginRequest.password
                )
            )
            val userPrincipal = authentication.principal as UserPrincipal

            val accessToken = issueToken(userPrincipal.id, jwtProperties.accessTokenExpiry).first
            val (refreshToken, expiresAt) = issueToken(userPrincipal.id, jwtProperties.refreshTokenExpiry)

            refreshTokenRepository.save(
                RefreshEntity(
                    token = refreshToken,
                    userId = userPrincipal.id,
                    expiryDate = expiresAt
                )
            )

            AuthenticationResponse(
                username = userPrincipal.name,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } catch (e: BadCredentialsException) {
            logger.warn("Invalid login attempt for user: ${loginRequest.username}")
            throw InvalidCredentialsException()
        } catch (e: JWTCreationException) {
            logger.error("Failed to create JWT token for user: ${loginRequest.username}", e)
            throw e
        }
    }

    @Transactional
    override fun register(registrationRequest: RegistrationRequest): AuthenticationResponse {
        if (registrationRequest.validatePasswordsMatch()) {
            throw PasswordMismatchException()
        }

        if (userRepository.existsByUsername(registrationRequest.username)) {
            throw UserAlreadyExistsException(registrationRequest.username)
        }

        val encodedPassword = passwordEncoder.encode(registrationRequest.password)

        val userEntity = UserEntity(
            username = registrationRequest.username,
            password = encodedPassword,
            roles = listOf("ROLE_USER"),
            email = registrationRequest.email
        )
        userRepository.save(userEntity)

        return login(LoginRequest(registrationRequest.username, registrationRequest.password))
    }

    @Transactional
    override fun logout(refreshToken: String) {
        val entity = refreshTokenRepository.findByToken(refreshToken) ?: return
        refreshTokenRepository.delete(entity)
    }

    @Transactional
    override fun refreshAccessToken(refreshToken: String): TokenPair {
        val refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
            ?: throw InvalidRefreshTokenException()

        if (refreshTokenEntity.expiryDate.before(Date())) {
            refreshTokenRepository.delete(refreshTokenEntity)
            throw ExpiredRefreshTokenException()
        }

        val accessToken = issueToken(refreshTokenEntity.userId, jwtProperties.accessTokenExpiry).first
        val (newRefreshToken, newExpireAt) = issueToken(refreshTokenEntity.userId, jwtProperties.refreshTokenExpiry)

        refreshTokenEntity.token = newRefreshToken
        refreshTokenEntity.expiryDate = newExpireAt

        return TokenPair(
            accessToken = accessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun issueToken(userId: Long, expiry: Long): Pair<String, Date> {
        return jwtProcessor.issueToken(userId, expiry)
    }
}
