package com.sanmiade.myrecipes.features.recipes

import com.sanmiade.myrecipes.utils.PagedResponse
import com.sanmiade.myrecipes.utils.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/recipes")
class RecipeController(private val recipeService: RecipeService) {

    @PostMapping()
    fun createRecipe(
        @Valid @RequestBody recipeReq: RecipeReq,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<RecipeResponse> {
        val response = recipeService.createRecipe(recipeReq, user.id)
        val location = URI.create("/api/v1/recipes/${response.id}")
        return ResponseEntity.created(location).body(response)
    }

    @GetMapping("/mine")
    fun getRecipesByUser(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestParam status: Status,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<RecipeResponse>?> {
        val pageable = PageRequest.of(page, size)
        val recipes = recipeService.getRecipesByUserId(user.id, status, pageable)

        return ResponseEntity.ok(recipes)
    }

    @GetMapping("/search")
    fun getRecipesByCuisine(
        @RequestParam(required = false) cuisine: String?,
        @RequestParam(required = false) status: Status?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<RecipeResponse>?> {
        val pageable = PageRequest.of(page, size)
        val recipes = recipeService.getRecipesBy(cuisine, status, pageable)

        return ResponseEntity.ok(recipes)
    }

    @PostMapping("/{recipeId}/rate")
    fun rateRecipe(
        @PathVariable recipeId: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody ratingReq: RatingReq
    ): RecipeResponse {
       return recipeService.rateRecipe(recipeId, user.id, ratingReq)
    }
}