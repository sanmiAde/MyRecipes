package com.sanmiade.myrecipes.features.recipes

import org.springframework.data.jpa.repository.JpaRepository

interface RatingRepository : JpaRepository<RatingEntity, Long>