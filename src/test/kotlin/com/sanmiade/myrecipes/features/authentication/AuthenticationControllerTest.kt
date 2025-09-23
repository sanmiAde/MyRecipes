package com.sanmiade.myrecipes.features.authentication

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.sanmiade.myrecipes.features.authentication.dto.AuthenticationResponse
import com.sanmiade.myrecipes.features.authentication.dto.LoginRequest
import com.sanmiade.myrecipes.features.authentication.dto.RegistrationRequest
import com.sanmiade.myrecipes.features.authentication.dto.TokenPair
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthenticationController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockkBean(relaxed = true)
    private lateinit var authenticationService: AuthenticationService

    private val authResponse = AuthenticationResponse(
        username = "john",
        accessToken = "access-token",
        refreshToken = "refresh-token"
    )

    private val tokenPair = TokenPair(
        accessToken = "new-access",
        refreshToken = "new-refresh"
    )

    @Test
    fun `POST login returns 200 with AuthenticationResponse`() {
        val req = LoginRequest(username = "john", password = "secret")
        every { authenticationService.login(any()) } returns authResponse

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(authResponse)))

        verify(exactly = 1) { authenticationService.login(req) }
    }

    @Test
    fun `POST register returns 201 with AuthenticationResponse`() {
        val req = RegistrationRequest(
            username = "john",
            password = "secret",
            confirmPassword = "secret",
            email = "john@example.com"
        )
        every { authenticationService.register(any()) } returns authResponse

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(content().json(objectMapper.writeValueAsString(authResponse)))

        verify(exactly = 1) { authenticationService.register(req) }
    }

    @Test
    fun `POST logout with refreshToken returns 200 and calls service`() {
        every { authenticationService.logout(any()) } returns Unit

        val payload = mapOf("refreshToken" to "refresh-token")
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isOk)

        verify(exactly = 1) { authenticationService.logout("refresh-token") }
    }

    @Test
    fun `POST logout without refreshToken returns 400`() {
        val payload = emptyMap<String, String>()

        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST refresh with refreshToken returns 200 and token pair`() {
        every { authenticationService.refreshAccessToken(any()) } returns tokenPair

        val payload = mapOf("refreshToken" to "old-refresh")
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(tokenPair)))

        verify(exactly = 1) { authenticationService.refreshAccessToken("old-refresh") }
    }

    @Test
    fun `POST refresh without refreshToken returns 400`() {
        val payload = emptyMap<String, String>()

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isBadRequest)
    }
}