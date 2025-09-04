package com.sanmiade.myrecipes.features.account

interface AccountService {
    fun getUserById(id: Long): UserResponse?
}