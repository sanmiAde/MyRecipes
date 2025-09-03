package com.sanmiade.myrecipes.features.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class User(
    @Column(unique = true)
    val username: String,
    val password: String,
    val email: String,
    val roles: List<String>
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    val id: Long? = null
}