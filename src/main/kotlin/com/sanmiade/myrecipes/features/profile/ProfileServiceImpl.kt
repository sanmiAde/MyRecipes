package com.sanmiade.myrecipes.features.profile

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileServiceImpl(
    private val userRepository: UserRepository
) : ProfileService {

    override fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User with id $id not found") }
        return user.toResponse()
    }

    @Transactional
    override fun updateUser(id: Long, profileRequest: ProfileRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User with id $id not found") }

        user.apply {
            username = profileRequest.username
            email = profileRequest.email
        }

        return user.toResponse() // no need to call save explicitly in a @Transactional method
    }
}

// Custom exception
class UserNotFoundException(message: String) : RuntimeException(message)

// Mapping extension
fun UserEntity.toResponse() = UserResponse(
    username = this.username,
    email = this.email
)
