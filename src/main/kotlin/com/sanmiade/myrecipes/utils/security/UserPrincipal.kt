package com.sanmiade.myrecipes.utils.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    val name: String,
    val encodedPassword: String? = null,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return emptyList()
    }

    override fun getPassword(): String? {
        return encodedPassword
    }

    override fun getUsername(): String {
        return name
    }
}