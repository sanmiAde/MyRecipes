package com.sanmiade.myrecipes.features.authentication

import com.sanmiade.myrecipes.features.profile.UserEntity
import com.sanmiade.myrecipes.features.profile.UserRepository
import com.sanmiade.myrecipes.features.authentication.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.dto.RegistrationRequest
import com.sanmiade.myrecipes.utils.InvalidCredentialsException
import com.sanmiade.myrecipes.utils.UserAlreadyExistsException
import com.sanmiade.myrecipes.utils.security.JWTProcessor
import com.sanmiade.myrecipes.utils.security.JWTProperties
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthenticationServiceImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var jwtProcessor: JWTProcessor
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var jwtProperties: JWTProperties

    private lateinit var authService: AuthenticationServiceImpl

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        jwtProcessor = mockk()
        authenticationManager = mockk()
        passwordEncoder = mockk()
        refreshTokenRepository = mockk(relaxed = true)
        jwtProperties = JWTProperties().apply {
            accessTokenExpiry = 1000L * 60
            refreshTokenExpiry = 1000L * 60 * 60
        }


        authService = AuthenticationServiceImpl(
            userRepository,
            authenticationManager,
            jwtProcessor,
            jwtProperties,
            passwordEncoder,
            refreshTokenRepository
        )
    }

    @Test
    fun `register throws UserAlreadyExistsException if username exists`() {
        val request = RegistrationRequest(
            username = "john",
            password = "password",
            confirmPassword = "password",
            email = "john@example.com"
        )

        every { userRepository.existsByUsername("john") } returns true

        assertThrows(UserAlreadyExistsException::class.java) {
            authService.register(request)
        }
    }

    @Test
    fun `login throws InvalidCredentialsException when authentication fails`() {
        val loginRequest = LoginRequest(username = "john", password = "wrongpass")

        every { authenticationManager.authenticate(any()) } throws org.springframework.security.authentication.BadCredentialsException(
            "Bad credentials"
        )

        assertThrows(InvalidCredentialsException::class.java) {
            authService.login(loginRequest)
        }
    }

    @Test
    fun `register succeeds when username does not exist`() {
        val request = RegistrationRequest(
            username = "john",
            password = "password",
            confirmPassword = "password",
            email = "john@example.com"
        )

        every { userRepository.existsByUsername("john") } returns false
        every { passwordEncoder.encode("password") } returns "encoded-password"

        val savedUser = slot<UserEntity>()
        every { userRepository.save(capture(savedUser)) } answers { firstArg() }

        every { jwtProcessor.issueToken(any(), any()) } returns Pair("access-token", Date())

        // Mock AuthenticationManager
        val userPrincipal = UserPrincipal(1L, "john")
        val authToken = UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)
        every { authenticationManager.authenticate(any()) } returns authToken

        // Ensure RefreshTokenRepository.save returns a RefreshEntity (not Object)
        val savedRefresh = slot<RefreshEntity>()
        every { refreshTokenRepository.save(capture(savedRefresh)) } answers { firstArg<RefreshEntity>() }
        // Optional: if your flow later queries it
        every { refreshTokenRepository.findByToken(any()) } answers { savedRefresh.captured }

        val response = authService.register(request)

        assertEquals("john", response.username)
        assertEquals("access-token", response.accessToken)
        assertEquals("access-token", response.refreshToken) // simple mock returns same token
        assertEquals("encoded-password", savedUser.captured.password)
    }
}
