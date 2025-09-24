package com.sanmiade.myrecipes.features.recipes

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.sanmiade.myrecipes.utils.PagedResponse
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(RecipeController::class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockkBean(relaxed = true)
    private lateinit var recipeService: RecipeService

    private val principal = UserPrincipal(42L, "john.doe")

    private val sampleRecipeReq = RecipeReq(
        title = "Pasta",
        description = "Tasty pasta",
        ingredients = "Noodles, Sauce",
        directions = "Boil, mix",
        cuisine = "Italian",
        status = Status.DRAFT
    )

    private val sampleRecipeRes = RecipeResponse(
        id = 100L,
        title = "Pasta",
        description = "Tasty pasta",
        ingredients = "Noodles, Sauce",
        directions = "Boil, mix",
        cuisine = "Italian",
        status = Status.DRAFT,
        rating = 0.0
    )

    @BeforeEach
    fun setupAuth() {
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken(principal, null)
    }

    @Test
    fun `POST create returns 201 with Location and body`() {
        every { recipeService.createRecipe(any(), any()) } returns sampleRecipeRes

        mockMvc.perform(
            post("/api/v1/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRecipeReq))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "/api/v1/recipes/100"))
            .andExpect(content().json(objectMapper.writeValueAsString(sampleRecipeRes)))

        verify(exactly = 1) { recipeService.createRecipe(sampleRecipeReq, 42L) }
    }

    @Test
    fun `GET mine returns 200 with paged recipes`() {
        val paged = PagedResponse(
            content = listOf(sampleRecipeRes),
            prevPage = null,
            currentPage = 0,
            nextPage = null,
            totalPages = 1,
            totalElements = 1
        )
        every { recipeService.getRecipesByUserId(any(), any(), any()) } returns paged

        mockMvc.perform(
            get("/api/v1/recipes/mine")
                .param("status", Status.DRAFT.name)
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(paged)))

        verify(exactly = 1) {
            recipeService.getRecipesByUserId(
                42L,
                Status.DRAFT,
                withArg { pr ->
                    assert(pr.pageNumber == 0)
                    assert(pr.pageSize == 10)
                }
            )
        }
    }

    @Test
    fun `GET search with filters returns 200 with paged recipes`() {
        val paged = PagedResponse(
            content = listOf(sampleRecipeRes),
            prevPage = null,
            currentPage = 1,
            nextPage = 2,
            totalPages = 4,
            totalElements = 40
        )
        every { recipeService.getRecipesBy(any(), any(), any()) } returns paged

        mockMvc.perform(
            get("/api/v1/recipes/search")
                .param("cuisine", "Italian")
                .param("status", Status.DRAFT.name)
                .param("page", "1")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(paged)))

        verify(exactly = 1) {
            recipeService.getRecipesBy(
                "Italian",
                Status.DRAFT,
                withArg { pr ->
                    assert(pr.pageNumber == 1)
                    assert(pr.pageSize == 10)
                }
            )
        }
    }

    @Test
    fun `POST rate returns 200 with updated recipe`() {
        val ratingReq = RatingReq(Rating.FIVE)
        every { recipeService.rateRecipe(any(), any(), any()) } returns sampleRecipeRes

        mockMvc.perform(
            post("/api/v1/recipes/100/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ratingReq))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(sampleRecipeRes)))

        verify(exactly = 1) { recipeService.rateRecipe(100L, 42L, ratingReq) }
    }
}
