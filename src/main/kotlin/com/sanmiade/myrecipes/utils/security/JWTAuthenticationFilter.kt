package com.sanmiade.myrecipes.utils.security

import com.auth0.jwt.exceptions.JWTVerificationException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JWTAuthenticationFilter(
    private val jwtProcessor: JWTProcessor
) : OncePerRequestFilter() {

    private val excludedPaths = setOf(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh"
    )

    override fun shouldNotFilter(request: HttpServletRequest) =
        excludedPaths.any { request.requestURI.startsWith(it) }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            if (SecurityContextHolder.getContext().authentication == null) {
                val principal = extractAndDecodeJWT(request)
                val authToken = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
                SecurityContextHolder.getContext().authentication = authToken
            }
            filterChain.doFilter(request, response)
        } catch (e: BadCredentialsException) {
            response.apply {
                status = HttpServletResponse.SC_UNAUTHORIZED
                contentType = "application/json"
                writer.write(
                    """{"message":"${e.message}","timestamp":${System.currentTimeMillis()}}"""
                )
            }
        }
    }

    private fun extractAndDecodeJWT(request: HttpServletRequest): UserPrincipal {
        val header = request.getHeader("Authorization")
            ?: throw BadCredentialsException("Authorization header missing")
        if (!header.startsWith("Bearer ")) throw BadCredentialsException("Malformed token")

        val token = header.removePrefix("Bearer ").trim()
        return try {
            jwtProcessor.toUserPrincipal(jwtProcessor.decodeJWT(token))
        } catch (ex: Exception) {
            throw BadCredentialsException("Token verification failed")
        }
    }
}