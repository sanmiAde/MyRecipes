package com.sanmiade.myrecipes.utils.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security.jwt")
open class JWTProperties {
    lateinit var secretKey: String
    var accessTokenExpiry: Long = 0
    var refreshTokenExpiry: Long = 0
}