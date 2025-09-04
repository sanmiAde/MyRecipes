package com.sanmiade.myrecipes.features.account

import org.springframework.stereotype.Component

@Component
class AccountServiceImpl(private val userRepository: UserRepository) : AccountService {
    override fun getUserById(id: Long): UserResponse {
        return userRepository.findById(id).map {
            UserResponse(
                username = it.username,
                email = it.email,
            )
        }.orElseThrow { throw Exception("user not found") }
    }
}