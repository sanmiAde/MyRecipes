package com.sanmiade.myrecipes.features.recipes

import com.sanmiade.myrecipes.features.profile.UserEntity
import com.sanmiade.myrecipes.features.profile.UserRepository
import com.sanmiade.myrecipes.utils.PagedResponse
import com.sanmiade.myrecipes.utils.UserNotFoundException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

class RecipeServiceImplTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var userRepository: UserRepository
    private lateinit var ratingRepository: RatingRepository

    private lateinit var service: RecipeServiceImpl

    @BeforeEach
    fun setup() {
        recipeRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        ratingRepository = mockk(relaxed = true)
        service = RecipeServiceImpl(recipeRepository, userRepository, ratingRepository)
    }

    private fun sampleUser() = UserEntity(
        username = "john",
        password = "pwd",
        email = "john@example.com"
    )

    private fun sampleRecipeEntity(id: Long? = 10L, user: UserEntity = sampleUser(), ratings: MutableList<RatingEntity> = mutableListOf()) =
        RecipeEntity(
            id = id,
            userEntity = user,
            title = "Pancakes",
            description = "Fluffy pancakes",
            ingredients = "Flour, Eggs",
            directions = "Mix and fry",
            cuisine = "American",
            status = Status.SHARED,
            ratings = ratings
        )

    @Test
    fun `createRecipe saves and returns response`() {
        val userId = 1L
        val req = RecipeReq(
            title = "Pancakes",
            description = "Fluffy",
            ingredients = "Flour",
            directions = "Cook",
            cuisine = "American",
            status = Status.DRAFT
        )

        every { userRepository.findById(userId) } returns Optional.of(sampleUser())
        every { recipeRepository.save(any<RecipeEntity>()) } answers { firstArg<RecipeEntity>().apply { id = 100L } }

        val resp = service.createRecipe(req, userId)

        assertEquals(100L, resp.id)
        assertEquals(req.title, resp.title)
        assertEquals(req.cuisine, resp.cuisine)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { recipeRepository.save(any<RecipeEntity>()) }
    }

    @Test
    fun `createRecipe throws when user not found`() {
        val userId = 99L
        val req = RecipeReq(
            title = "Pancakes",
            description = "Fluffy",
            ingredients = "Flour",
            directions = "Cook",
            cuisine = "American",
            status = Status.DRAFT
        )

        every { userRepository.findById(userId) } returns Optional.empty()

        val ex = assertThrows(UserNotFoundException::class.java) {
            service.createRecipe(req, userId)
        }
        assertTrue(ex.message!!.contains("$userId"))
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { recipeRepository.save(any<RecipeEntity>()) }
    }

    @Test
    fun `getRecipesByUserId maps to PagedResponse`() {
        val pageable = PageRequest.of(1, 2)
        val entities = listOf(sampleRecipeEntity(id = 1), sampleRecipeEntity(id = 2))
        val page: Page<RecipeEntity> = PageImpl(entities, pageable, 5)

        every { recipeRepository.findRecipeEntitiesByUserEntityIdAndStatus(1L, Status.SHARED, pageable) } returns page

        val resp: PagedResponse<RecipeResponse> = service.getRecipesByUserId(1L, Status.SHARED, pageable)

        assertEquals(2, resp.content.size)
        assertEquals(1, resp.currentPage)
        assertEquals(3, resp.totalPages)
        assertEquals(5, resp.totalElements)
        assertEquals(0, resp.prevPage)
        assertEquals(1 + 1, resp.nextPage)
        assertEquals(listOf(1L, 2L), resp.content.mapNotNull { it.id })

        verify { recipeRepository.findRecipeEntitiesByUserEntityIdAndStatus(1L, Status.SHARED, pageable) }
    }

    @Test
    fun `getRecipesBy filters by cuisine and status`() {
        val pageable: Pageable = PageRequest.of(0, 3)
        val entities = listOf(sampleRecipeEntity(id = 11), sampleRecipeEntity(id = 12))
        val page: Page<RecipeEntity> = PageImpl(entities, pageable, 2)

        every { recipeRepository.searchByCuisineAndStatus("Italian", Status.DRAFT, pageable) } returns page

        val resp = service.getRecipesBy("Italian", Status.DRAFT, pageable)

        assertEquals(2, resp.content.size)
        assertNull(resp.prevPage)
        assertNull(resp.nextPage)
        assertEquals(1, resp.totalPages)
        assertEquals(listOf(11L, 12L), resp.content.mapNotNull { it.id })

        verify { recipeRepository.searchByCuisineAndStatus("Italian", Status.DRAFT, pageable) }
    }

    @Test
    fun `rateRecipe adds rating and returns updated response`() {
        val recipe = sampleRecipeEntity(id = 55, ratings = mutableListOf())
        every { recipeRepository.findById(55) } returns Optional.of(recipe)
        every { ratingRepository.save(any()) } answers { firstArg() }

        val resp = service.rateRecipe(55, id = 123, ratingReq = RatingReq(Rating.FOUR))

        assertEquals(1, recipe.ratings.size)
        assertEquals(4.0, resp.rating) // average should be 4.0
        verify { ratingRepository.save(any()) }
    }

    @Test
    fun `rateRecipe throws when recipe not found`() {
        every { recipeRepository.findById(404) } returns Optional.empty()

        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.rateRecipe(404, id = 1, ratingReq = RatingReq(Rating.ONE))
        }
        assertTrue(ex.message!!.contains("404"))
        verify(exactly = 0) { ratingRepository.save(any()) }
    }
}