package com.sanmiade.myrecipes.features.recipes

import com.sanmiade.myrecipes.utils.PagedResponse
import org.springframework.data.domain.PageRequest


interface RecipeService {
    fun createRecipe(recipeReq: RecipeReq, userId: Long) : RecipeResponse
    fun getRecipesByUserId(userId: Long, status: Status, pageable: PageRequest) : PagedResponse<RecipeResponse>
}