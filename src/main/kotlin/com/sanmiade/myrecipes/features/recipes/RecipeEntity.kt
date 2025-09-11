package com.sanmiade.myrecipes.features.recipes

import com.sanmiade.myrecipes.features.profile.UserEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Table(name = "recipes")
@Entity
class RecipeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne()
    var userEntity: UserEntity,
    var title: String,
    var description: String,
    var ingredients: String,
    var directions: String,
    var cuisine: String,
    var status: Status = Status.DRAFT,
)

enum class Rating(val value: Int) {
    NONE(-1), ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5)
}


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
        status = this.status
    )
