package com.sanmiade.myrecipes.features.authentication

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshEntity, Long> {
    fun findByToken(token: String): RefreshEntity?
}