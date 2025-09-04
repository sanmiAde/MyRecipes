package com.sanmiade.myrecipes.utils.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JWTAuthenticationFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity, authManager: AuthenticationManager): SecurityFilterChain {
        http {
            csrf { disable() }
            cors { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            formLogin { disable() }

            authorizeHttpRequests {
                authorize("/api/v1/auth/**", permitAll)
                authorize("/", permitAll)
                authorize(anyRequest, authenticated)
            }

            authenticationManager = authManager

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }

        return http.build()
    }

    // Modern way to get AuthenticationManager
    @Bean
    fun authenticationManager(authConfig: org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }
}