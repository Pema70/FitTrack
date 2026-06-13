package com.fittrack.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtils(
    @Value("\${fittrack.jwt.secret}") private val secret: String,
    @Value("\${fittrack.jwt.access-token-expiration-ms}") private val accessExpMs: Long,
    @Value("\${fittrack.jwt.refresh-token-expiration-ms}") private val refreshExpMs: Long
) {
    private val key: SecretKey by lazy {
        // secret w application.yml jest Base64-encoded
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    }

    fun generateToken(email: String): String = buildToken(email, accessExpMs)
    fun generateRefreshToken(email: String): String = buildToken(email, refreshExpMs)

    private fun buildToken(email: String, expMs: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(Date(now.time + expMs))
            .signWith(key)
            .compact()
    }

    fun isValid(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    } catch (e: Exception) {
        false
    }

    fun extractEmail(token: String): String =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
}
