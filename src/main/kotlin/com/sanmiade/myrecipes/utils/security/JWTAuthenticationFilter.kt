package com.sanmiade.myrecipes.utils.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


class JWTAuthenticationFilter(
    private val jwtProcessor: JWTProcessor
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        // Only validate if a Bearer token is present
        if (header != null && header.startsWith("Bearer ")) {
            try {
                val token = header.removePrefix("Bearer ").trim()
                val principal = jwtProcessor.toUserPrincipal(jwtProcessor.decodeJWT(token))
                val authToken = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
                SecurityContextHolder.getContext().authentication = authToken
            } catch (ex: Exception) {
                response.apply {
                    status = HttpServletResponse.SC_UNAUTHORIZED
                    contentType = "application/json"
                    writer.write("""{"message":"Token verification failed","timestamp":${System.currentTimeMillis()}}""")
                    return
                }
            }
        }

        // If no token, just continue the filter chain
        filterChain.doFilter(request, response)
    }
}
