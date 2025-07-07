package com.auctionapp.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // redis 블랙 리스트에 토큰이 있는 지 확인 (로그 아웃 여부)
            val isLoggedOut = redisTemplate.opsForValue().get("BL:$token")

            if (isLoggedOut == null) {
                // 로그아웃되지 않은 상태라면
                val authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }

        return null
    }
}
