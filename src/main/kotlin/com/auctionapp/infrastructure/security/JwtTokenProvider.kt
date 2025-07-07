package com.auctionapp.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")private val secretKey: String,
    @Value("\${jwt.expiration}")private val accessTokenValidityMs: Long,
    @Value("\${jwt.refresh-expiration}")private val refreshTokenValidityMs: Long,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val key: SecretKey

    init {
        key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    fun createAccessToken(authentication: Authentication): String {
        val now = Date()
        val validity = Date(now.time + accessTokenValidityMs)

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim("authorities", authentication.authorities.joinToString(",") { it.authority })
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun createRefreshToken(authentication: Authentication): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidityMs)

        val refreshToken =
            Jwts.builder()
                .setSubject(authentication.name)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact()

        // redis 저장
        redisTemplate.opsForValue().set(
            "RT:" + authentication.name,
            refreshToken,
            refreshTokenValidityMs,
            TimeUnit.MILLISECONDS,
        )

        return refreshToken
    }

    fun getAuthentication(token: String): Authentication {
        val claims = getClaims(token)

        val authorities =
            claims.get("authorities", String::class.java)
                ?.split(",")
                ?.map { SimpleGrantedAuthority(it) }
                ?: listOf()

        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims = getClaims(token)
            val expiration = claims.expiration
            return expiration.after(Date())
        } catch (e: Exception) {
            return false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return getClaims(token).subject
    }

    fun getExpirationFromToken(token: String): Date {
        return getClaims(token).expiration
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJwt(token)
            .body
    }
}
