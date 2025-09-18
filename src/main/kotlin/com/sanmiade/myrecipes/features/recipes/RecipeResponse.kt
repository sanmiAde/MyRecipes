package com.sanmiade.myrecipes.features.recipes

import java.io.Serializable

/**
 * DTO for {@link com.sanmiade.myrecipes.features.recipes.RecipeEntity}
 */
data class RecipeResponse(
    val id: Long?,
    val title: String,
    val description: String,
    val ingredients: String,
    val directions: String,
    val cuisine: String,
    val status: Status,
    val rating: Double
) : Serializable {

}