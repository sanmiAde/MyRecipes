package com.sanmiade.myrecipes.features.recipes

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RecipeRepository : JpaRepository<RecipeEntity, Long> {
    fun findRecipeEntitiesByUserEntityIdAndStatus(
        userEntityId: Long,
        status: Status,
        pageable: Pageable
    ): Page<RecipeEntity>

    @Query(
        "SELECT r FROM RecipeEntity r " +
            "WHERE (:cuisine IS NULL OR LOWER(r.cuisine) = LOWER(:cuisine)) " +
            "AND (:status IS NULL OR r.status = :status)"
    )
    fun searchByCuisineAndStatus(
        @Param("cuisine") cuisine: String?,
        @Param("status") status: Status?,
        pageable: Pageable
    ): Page<RecipeEntity>
}