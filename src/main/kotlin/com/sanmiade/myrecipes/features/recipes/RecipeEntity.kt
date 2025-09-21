package com.sanmiade.myrecipes.features.recipes

import com.sanmiade.myrecipes.features.profile.UserEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import kotlin.math.round

@Table(name = "recipes")
@Entity
class RecipeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne()
    @JoinColumn(name = "user_entity_id")
    var userEntity: UserEntity,
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String,
    @Column(columnDefinition = "TEXT")
    var ingredients: String,
    @Column(columnDefinition = "TEXT")
    var directions: String,
    @Column(length = 100)
    var cuisine: String,
    @Enumerated(EnumType.STRING)
    var status: Status = Status.DRAFT,
    @OneToMany(mappedBy = "recipeEntity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ratings: MutableList<RatingEntity> = mutableListOf()
)

enum class Status(val value: String) {
    SHARED("SHARED"), DRAFT("DRAFT")
}

fun RecipeEntity.toResponse(): RecipeResponse =
    RecipeResponse(
        id = this.id,
        title = this.title,
        description = this.description,
        ingredients = this.ingredients,
        directions = this.directions,
        cuisine = this.cuisine,
        status = this.status,
        rating = this.ratings
            .mapNotNull { it.data.takeIf { r -> r != Rating.NONE }?.value?.toDouble() }
            .let { values ->
                val avg = if (values.isNotEmpty()) values.average() else 0.0
                round(avg * 10) / 10
            }
    )
