package com.sanmiade.myrecipes.features.recipes

import com.sanmiade.myrecipes.utils.PagedResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable


interface RecipeService {
    fun createRecipe(recipeReq: RecipeReq, userId: Long) : RecipeResponse
    fun getRecipesByUserId(userId: Long, status: Status, pageable: PageRequest) : PagedResponse<RecipeResponse>
    fun getRecipesBy(cuisine: String?, status: Status?, pageable: Pageable) : PagedResponse<RecipeResponse>
}