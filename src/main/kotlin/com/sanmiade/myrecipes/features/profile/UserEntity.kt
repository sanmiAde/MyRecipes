package com.sanmiade.myrecipes.features.profile

import com.sanmiade.myrecipes.features.recipes.RecipeEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table


@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, unique = true)
    var username: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false)
    var email: String,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "role")
    var roles: List<String> = listOf(),
    @OneToMany(mappedBy = "userEntity", cascade = [jakarta.persistence.CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var recipes: MutableList<RecipeEntity> = mutableListOf()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", allocationSize = 1)
    @Column(nullable = false)
    val id: Long? = null
}