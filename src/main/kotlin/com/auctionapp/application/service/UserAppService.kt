package com.auctionapp.application.service

import com.auctionapp.application.exception.DuplicateEmailException
import com.auctionapp.application.exception.LoginFailException
import com.auctionapp.domain.entity.Role
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import com.auctionapp.infrastructure.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.redis.core.RedisTemplate
import com.auctionapp.application.dto.SignupRequest
import com.auctionapp.application.dto.LoginRequest
import com.auctionapp.application.dto.TokenResponse
import com.auctionapp.application.exception.LogoutFailException
import com.auctionapp.application.exception.UnavailableRefreshTokenException
import com.auctionapp.infrastructure.security.JwtTokenProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class UserAppService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    @Transactional
    fun signup(
        request: SignupRequest,
    ): Long {
        val found = userRepository.findByEmail(Email(request.email))

        if (found != null) {
            throw DuplicateEmailException()
        }

        val user = User(
            email = Email(request.email),
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = Role.CUSTOMER
        )

        return userRepository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun login(
        request: LoginRequest,
    ): TokenResponse {
        val user = userRepository.findByEmail(Email(request.email)) ?: throw LoginFailException()

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw LoginFailException()
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        val authentication = UsernamePasswordAuthenticationToken(user.email.value, null, authorities)

        val accessToken = jwtTokenProvider.createAccessToken(authentication)
        val refreshToken = jwtTokenProvider.createRefreshToken(authentication)

        return TokenResponse(accessToken, refreshToken)
    }

    @Transactional
    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw UnavailableRefreshTokenException()
        }

        val email = jwtTokenProvider.getUsernameFromToken(refreshToken)
        val redisRefreshToken = redisTemplate.opsForValue().get("RT:$email")

        if (redisRefreshToken == null || redisRefreshToken != refreshToken) {
            throw UnavailableRefreshTokenException()
        }

        val user = userRepository.findByEmail(Email(email)) ?: throw LoginFailException()

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)

        val newAccessToken = jwtTokenProvider.createAccessToken(authentication)

        return TokenResponse(newAccessToken, refreshToken)
    }


    fun logout(accessToken: String) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw LogoutFailException()
        }

        val username = jwtTokenProvider.getUsernameFromToken(accessToken)

        val expiration = jwtTokenProvider.getExpirationFromToken(accessToken)
        val timeToLive = expiration.time - Date().time

        //access token을 블랙 리스트에 추가
        redisTemplate.opsForValue().set(
            "BL:$accessToken",
            "logout",
            timeToLive,
            TimeUnit.MILLISECONDS,
        )

        //리프레시 토큰 제거
        redisTemplate.delete("RT:$username")
    }
}
