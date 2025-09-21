package com.sanmiade.myrecipes.features.profile

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ProfileController::class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockkBean(relaxed = true)
    private lateinit var profileService: ProfileService

    private val testUserResponse = UserResponse(username = "john", email = "john@example.com")
    private val updatedUserResponse = UserResponse(username = "newname", email = "new@example.com")
    private val updateRequest = ProfileRequest(username = "newname", email = "new@example.com")

    @BeforeEach
    fun setupAuth() {
        val principal = com.sanmiade.myrecipes.utils.security.UserPrincipal(42L, "john.doe")
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken(principal, null)
    }

    @Test
    fun `GET account returns 200 with user`() {
        every { profileService.getUserById(42L) } returns testUserResponse

        mockMvc.perform(get("/api/v1/account"))
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(testUserResponse)))

        verify(exactly = 1) { profileService.getUserById(42L) }
    }

    @Test
    fun `PUT account updates user and returns 200`() {
        every { profileService.updateUser(42L, any()) } returns updatedUserResponse

        mockMvc.perform(
            put("/api/v1/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(updatedUserResponse)))

        verify(exactly = 1) { profileService.updateUser(42L, withArg {
            assert(it.username == "newname")
            assert(it.email == "new@example.com")
        })}
    }
}
