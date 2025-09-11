package com.sanmiade.myrecipes.features.recipes

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


/**
 * DTO for {@link com.sanmiade.myrecipes.features.recipes.RecipeEntity}
 */
data class RecipeReq(
    @field:NotBlank(message = "title is mandatory")
    val title: String,
    @field:NotBlank(message = "description is mandatory")
    val description: String,
    @field:NotBlank(message = "ingredients is mandatory")
    val ingredients: String,
    @field:NotBlank(message = "directions is mandatory")
    val directions: String,
    @field:NotBlank(message = "cuisine is mandatory")
    val cuisine: String,
    @field:NotNull(message = "status is mandatory")
    var status: Status
)