package com.sanmiade.myrecipes.features.recipes

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<RecipeEntity, Long> {
    fun findRecipeEntitiesByUserEntityIdAndStatus(
        userEntityId: Long,
        status: Status,
        pageable: Pageable
    ): Page<RecipeEntity>
}