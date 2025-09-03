package com.sanmiade.myrecipes.utils.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

private const val AUTHORITIES_KEY = "roles"
private const val USER_NAME_KEY = "username"

@Component
class JWTProcessor(private val jwtProperties: JWTProperties) {

    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(jwtProperties.secretKey) }

    /**
     * Decode and verify JWT token.
     * Throws JWTVerificationException if the token is invalid or expired.
     */
    fun decodeJWT(token: String): DecodedJWT =
        try {
            JWT.require(algorithm).build().verify(token)
        } catch (ex: JWTVerificationException) {
            throw IllegalArgumentException("Invalid or expired JWT token", ex)
        }

    /**
     * Issue a new JWT token with claims and expiration.
     */
    fun issueToken(
        userId: Long,
        email: String,
        authorities: List<String>,
        expiresAtMillis: Long
    ): String {
        val expiration = Date.from(Instant.now().plusMillis(expiresAtMillis))
        return JWT.create()
            .withSubject(userId.toString())
            .withClaim(USER_NAME_KEY, email)
            .withArrayClaim(AUTHORITIES_KEY, authorities.toTypedArray())
            .withExpiresAt(expiration)
            .sign(algorithm)
    }

    /**
     * Convert a decoded JWT into a UserPrincipal object.
     */
    fun toUserPrincipal(decodedJWT: DecodedJWT): UserPrincipal {
        val id = decodedJWT.subject.toLongOrNull()
            ?: throw IllegalArgumentException("JWT subject is not a valid user ID")

        val name = decodedJWT.getClaim(USER_NAME_KEY).asString()
            ?: throw IllegalArgumentException("JWT does not contain username")

        return UserPrincipal(
            id = id,
            name = name,
            authorities = decodedJWT.extractAuthorities()
        )
    }

    /**
     * Extract roles/authorities from JWT.
     */
    private fun DecodedJWT.extractAuthorities(): List<SimpleGrantedAuthority> {
        val roles = this.getClaim(AUTHORITIES_KEY).asList(String::class.java) ?: emptyList()
        return roles.map { SimpleGrantedAuthority(it) }
    }
}