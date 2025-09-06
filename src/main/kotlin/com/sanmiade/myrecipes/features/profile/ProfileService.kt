package com.sanmiade.myrecipes.features.profile

interface ProfileService {
    fun getUserById(id: Long): UserResponse?
    fun updateUser(id: Long, profileRequest: ProfileRequest): UserResponse
}