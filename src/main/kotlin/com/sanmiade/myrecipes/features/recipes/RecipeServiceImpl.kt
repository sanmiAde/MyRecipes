package com.sanmiade.myrecipes.features.recipes


import com.sanmiade.myrecipes.features.profile.UserRepository
import com.sanmiade.myrecipes.utils.PagedResponse
import com.sanmiade.myrecipes.utils.UserNotFoundException
import com.sanmiade.myrecipes.utils.fromSpringPage
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class RecipeServiceImpl(
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository
) : RecipeService {

    @Transactional
    override fun createRecipe(recipeReq: RecipeReq, userId: Long): RecipeResponse {
        val userEntity =
            userRepository.findById(userId).orElseThrow { UserNotFoundException("User with id $userId not found") }
        val recipeEntity = RecipeEntity(
            userEntity = userEntity,
            title = recipeReq.title,
            description = recipeReq.description,
            ingredients = recipeReq.ingredients,
            directions = recipeReq.directions,
            cuisine = recipeReq.cuisine,
            status = recipeReq.status
        )
        return recipeRepository.save<RecipeEntity>(recipeEntity).toResponse()
    }

    @Transactional
    override fun getRecipesByUserId(
        userId: Long,
        status: Status,
        pageable: PageRequest
    ): PagedResponse<RecipeResponse> {
        val springPage = recipeRepository.findRecipeEntitiesByUserEntityIdAndStatus(
            userEntityId = userId,
            status = status,
            pageable = pageable
        ).map { it.toResponse() }
        return fromSpringPage(springPage)
    }

    @Transactional
    override fun getRecipesBy(cuisine: String?, status: Status?, pageable: Pageable): PagedResponse<RecipeResponse> {
        val springPage =
            recipeRepository.findRecipeEntitiesByCuisineIgnoreCaseAndStatus(cuisine, status, pageable)
                .map { it.toResponse() }
        return fromSpringPage(springPage)
    }

    @Transactional
    override fun rateRecipe(
        recipeId: Long,
        id: Long,
        ratingReq: RatingReq
    ): RecipeResponse {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { IllegalArgumentException("Recipe with id=$recipeId not found") }

        val rating = RatingEntity(
            data = ratingReq.rating,
            recipeEntity = recipe
        )
        recipe.ratingEntity.add(rating)
        ratingRepository.save(rating)
        return recipe.toResponse()
    }

}