package com.sanmiade.myrecipes.utils.security

import com.sanmiade.myrecipes.features.account.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class MyRecipesUserDetailService(val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserPrincipal {
        if (username == null) throw UsernameNotFoundException("Username not found")
        val user = userRepository.findByEmail(username) ?: throw UsernameNotFoundException("User not found")
        val grantedAuthorities = user.roles.map { SimpleGrantedAuthority(it) }
        return UserPrincipal(name = user.username, id = user.id!!, authorities = grantedAuthorities)
    }
}