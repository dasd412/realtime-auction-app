package com.auctionapp.infrastructure.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.concurrent.TimeUnit

class JwtTokenProviderTest {
    // 모킹할 객체들
    private val redisTemplate = mockk<RedisTemplate<String, String>>()
    private val valueOperations = mockk<ValueOperations<String, String>>()

    // 테스트에 사용할 상수들
    private val secretKey = "testsecretkeythatisveryverylongandsecurefortestingtestsecretkeythatisveryverylongandsecurefortesting"
    private val accessTokenValidityMs = 3600000L // 1시간
    private val refreshTokenValidityMs = 86400000L // 24시간

    // 테스트 대상 객체
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setup() {
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.set(any(), any(), any(), any<TimeUnit>()) } returns Unit

        jwtTokenProvider =
            JwtTokenProvider(
                secretKey = secretKey,
                accessTokenValidityMs = accessTokenValidityMs,
                refreshTokenValidityMs = refreshTokenValidityMs,
                redisTemplate = redisTemplate,
            )
    }

    @Test
    @DisplayName("유효한 인증 정보로 액세스 토큰 생성")
    fun createAccessToken_validData_returnsValidJwt() {
        // given
        val email = "test@example.com"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)

        // when
        val token = jwtTokenProvider.createAccessToken(authentication)

        // then
        assertThat(token).isNotBlank()

        val username = jwtTokenProvider.getUsernameFromToken(token)
        assertThat(username).isEqualTo(email)

        val isValid = jwtTokenProvider.validateToken(token)
        assertThat(isValid).isTrue()
    }

    @Test
    @DisplayName("유효한 인증 정보로 리프레시 토큰 생성")
    fun createRefreshToken_validAuthentication_returnsValidToken() {
        // given
        val email = "test@example.com"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)

        every { valueOperations.set("RT:$email", any(), refreshTokenValidityMs, TimeUnit.MILLISECONDS) } returns Unit

        // when
        val token = jwtTokenProvider.createRefreshToken(authentication)

        // then
        assertThat(token).isNotBlank()

        val username = jwtTokenProvider.getUsernameFromToken(token)
        assertThat(username).isEqualTo(email)

        val isValid = jwtTokenProvider.validateToken(token)
        assertThat(isValid).isTrue()

        verify { valueOperations.set("RT:$email", token, refreshTokenValidityMs, TimeUnit.MILLISECONDS) }
    }

    @Test
    @DisplayName("토큰에서 인증 정보 추출")
    fun getAuthentication_validToken_returnsAuthentication() {
        // given
        val email = "test@example.com"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
        val token = jwtTokenProvider.createAccessToken(authentication)

        // when
        val resultAuthentication = jwtTokenProvider.getAuthentication(token)

        // then
        assertThat(resultAuthentication).isNotNull()
        assertThat(resultAuthentication.name).isEqualTo(email)
        assertThat(resultAuthentication.authorities.map { it.authority })
            .contains("ROLE_CUSTOMER")
    }

    @Test
    @DisplayName("유효한 토큰 검증")
    fun validateToken_validToken_returnsTrue() {
        // given
        val email = "test@example.com"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
        val token = jwtTokenProvider.createAccessToken(authentication)

        // when
        val isValid = jwtTokenProvider.validateToken(token)

        // then
        assertThat(isValid).isTrue()
    }
}
