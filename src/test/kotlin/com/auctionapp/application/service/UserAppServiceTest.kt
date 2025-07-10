package com.auctionapp.application.service

import com.auctionapp.application.dto.request.LoginRequest
import com.auctionapp.application.dto.request.SignupRequest
import com.auctionapp.application.dto.response.TokenResponse
import com.auctionapp.application.exception.DuplicateEmailException
import com.auctionapp.application.exception.LoginFailException
import com.auctionapp.application.exception.LogoutFailException
import com.auctionapp.application.exception.UnavailableRefreshTokenException
import com.auctionapp.domain.entity.Role
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import com.auctionapp.infrastructure.persistence.UserRepository
import com.auctionapp.infrastructure.security.JwtTokenProvider
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserAppServiceTest {
    // 모킹할 외부 의존성
    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val redisTemplate = mockk<RedisTemplate<String, String>>()
    private val valueOperations = mockk<ValueOperations<String, String>>()

    // 테스트 대상 서비스
    private val userAppService =
        UserAppService(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            jwtTokenProvider = jwtTokenProvider,
            redisTemplate = redisTemplate,
        )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("유효한 회원가입 요청이 성공적으로 처리된다")
    fun signup_validRequest_success() {
        // given
        val request =
            SignupRequest(
                email = "test@example.com",
                password = "Test12345!",
                name = "Test User",
            )
        val encodedPassword = "Test12345!"
        val savedUser =
            User(
                id = 1L,
                email = Email(request.email),
                password = encodedPassword,
                name = request.name,
                role = Role.CUSTOMER,
            )

        every { userRepository.findByEmail(Email(request.email)) } returns null
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userRepository.save(any()) } returns savedUser

        // when
        val result = userAppService.signup(request)

        // then
        assertThat(result).isEqualTo(1L)
        verify { userRepository.findByEmail(Email(request.email)) }
        verify { passwordEncoder.encode(request.password) }
        verify { userRepository.save(any()) }
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 예외가 발생한다")
    fun signup_duplicateEmail_throwsException() {
        // given
        val request =
            SignupRequest(
                email = "existing@example.com",
                password = "Test12345!",
                name = "Existing User",
            )
        val existingUser = mockk<User>()

        every { userRepository.findByEmail(Email(request.email)) } returns existingUser

        // when, then
        assertThrows<DuplicateEmailException> {
            userAppService.signup(request)
        }

        verify { userRepository.findByEmail(Email(request.email)) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    @DisplayName("유효한 로그인 정보로 로그인이 성공한다")
    fun login_validCredentials_success() {
        // given
        val request =
            LoginRequest(
                email = "test@example.com",
                password = "Test12345!",
            )
        val user =
            User(
                id = 1L,
                email = Email(request.email),
                password = "Test12345!",
                name = "Test User",
                role = Role.CUSTOMER,
            )
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val tokenResponse = TokenResponse(accessToken, refreshToken)

        every { userRepository.findByEmail(Email(request.email)) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns true
        every { jwtTokenProvider.createAccessToken(any()) } returns accessToken
        every { jwtTokenProvider.createRefreshToken(any()) } returns refreshToken

        // when
        val result = userAppService.login(request)

        // then
        assertThat(result).isEqualTo(tokenResponse)
        verify { userRepository.findByEmail(Email(request.email)) }
        verify { passwordEncoder.matches(request.password, user.password) }
        verify { jwtTokenProvider.createAccessToken(any()) }
        verify { jwtTokenProvider.createRefreshToken(any()) }
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    fun login_nonExistentEmail_throwsException() {
        // given
        val request =
            LoginRequest(
                email = "nonexistent@example.com",
                password = "Test12345!",
            )

        every { userRepository.findByEmail(Email(request.email)) } returns null

        // when, then
        assertThrows<LoginFailException> {
            userAppService.login(request)
        }

        verify { userRepository.findByEmail(Email(request.email)) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { jwtTokenProvider.createAccessToken(any()) }
        verify(exactly = 0) { jwtTokenProvider.createRefreshToken(any()) }
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
    fun login_invalidPassword_throwsException() {
        // given
        val request =
            LoginRequest(
                email = "test@example.com",
                password = "wrongPassword12345!",
            )
        val user =
            User(
                id = 1L,
                email = Email(request.email),
                password = "Test12345!",
                name = "Test User",
                role = Role.CUSTOMER,
            )

        every { userRepository.findByEmail(Email(request.email)) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns false

        // when, then
        assertThrows<LoginFailException> {
            userAppService.login(request)
        }

        verify { userRepository.findByEmail(Email(request.email)) }
        verify { passwordEncoder.matches(request.password, user.password) }
        verify(exactly = 0) { jwtTokenProvider.createAccessToken(any()) }
        verify(exactly = 0) { jwtTokenProvider.createRefreshToken(any()) }
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰이 갱신된다")
    fun refreshToken_validRefreshToken_success() {
        // given
        val refreshToken = "valid-refresh-token"
        val email = "test@example.com"
        val user =
            User(
                id = 1L,
                email = Email(email),
                password = "Test12345!",
                name = "Test User",
                role = Role.CUSTOMER,
            )
        val newAccessToken = "new-access-token"

        every { jwtTokenProvider.validateToken(refreshToken) } returns true
        every { jwtTokenProvider.getUsernameFromToken(refreshToken) } returns email
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get("RT:$email") } returns refreshToken
        every { userRepository.findByEmail(Email(email)) } returns user
        every { jwtTokenProvider.createAccessToken(any()) } returns newAccessToken

        // when
        val result = userAppService.refreshToken(refreshToken)

        // then
        assertThat(result.accessToken).isEqualTo(newAccessToken)
        assertThat(result.refreshToken).isEqualTo(refreshToken)
        verify { jwtTokenProvider.validateToken(refreshToken) }
        verify { jwtTokenProvider.getUsernameFromToken(refreshToken) }
        verify { redisTemplate.opsForValue() }
        verify { valueOperations.get("RT:$email") }
        verify { userRepository.findByEmail(Email(email)) }
        verify { jwtTokenProvider.createAccessToken(any()) }
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생한다")
    fun refreshToken_expiredRefreshToken_throwsException() {
        // given
        val expiredToken = "expired-refresh-token"

        every { jwtTokenProvider.validateToken(expiredToken) } returns false

        // when, then
        assertThrows<UnavailableRefreshTokenException> {
            userAppService.refreshToken(expiredToken)
        }

        verify { jwtTokenProvider.validateToken(expiredToken) }
        verify(exactly = 0) { jwtTokenProvider.getUsernameFromToken(any()) }
        verify(exactly = 0) { redisTemplate.opsForValue() }
    }

    @Test
    @DisplayName("Redis에 저장되지 않은 리프레시 토큰으로 갱신 시 예외가 발생한다")
    fun refreshToken_tokenNotInRedis_throwsException() {
        // given
        val refreshToken = "not-in-redis-token"
        val email = "test@example.com"

        every { jwtTokenProvider.validateToken(refreshToken) } returns true
        every { jwtTokenProvider.getUsernameFromToken(refreshToken) } returns email
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get("RT:$email") } returns null

        // when, then
        assertThrows<UnavailableRefreshTokenException> {
            userAppService.refreshToken(refreshToken)
        }

        verify { jwtTokenProvider.validateToken(refreshToken) }
        verify { jwtTokenProvider.getUsernameFromToken(refreshToken) }
        verify { redisTemplate.opsForValue() }
        verify { valueOperations.get("RT:$email") }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("다른 리프레시 토큰으로 갱신 시 예외가 발생한다")
    fun refreshToken_differentToken_throwsException() {
        // given
        val refreshToken = "token-1"
        val storedToken = "token-2" // 다른 토큰
        val email = "test@example.com"

        every { jwtTokenProvider.validateToken(refreshToken) } returns true
        every { jwtTokenProvider.getUsernameFromToken(refreshToken) } returns email
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get("RT:$email") } returns storedToken

        // when, then
        assertThrows<UnavailableRefreshTokenException> {
            userAppService.refreshToken(refreshToken)
        }

        verify { jwtTokenProvider.validateToken(refreshToken) }
        verify { jwtTokenProvider.getUsernameFromToken(refreshToken) }
        verify { redisTemplate.opsForValue() }
        verify { valueOperations.get("RT:$email") }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 로그아웃이 성공한다")
    fun logout_validAccessToken_success() {
        // given
        val accessToken = "valid-access-token"
        val email = "test@example.com"
        val expirationDate = Date(System.currentTimeMillis() + 3600000) // 1시간 후

        every { jwtTokenProvider.validateToken(accessToken) } returns true
        every { jwtTokenProvider.getUsernameFromToken(accessToken) } returns email
        every { jwtTokenProvider.getExpirationFromToken(accessToken) } returns expirationDate
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.set(
                "BL:$accessToken",
                "logout",
                any<Long>(),
                any(),
            )
        } returns Unit
        every { redisTemplate.delete("RT:$email") } returns true

        // when
        userAppService.logout(accessToken)

        // then
        verify { jwtTokenProvider.validateToken(accessToken) }
        verify { jwtTokenProvider.getUsernameFromToken(accessToken) }
        verify { jwtTokenProvider.getExpirationFromToken(accessToken) }
        verify { redisTemplate.opsForValue() }
        verify { valueOperations.set("BL:$accessToken", "logout", any(), any()) }
        verify { redisTemplate.delete("RT:$email") }
    }

    @Test
    @DisplayName("유효하지 않은 액세스 토큰으로 로그아웃 시 예외가 발생한다")
    fun logout_invalidAccessToken_throwsException() {
        // given
        val invalidToken = "invalid-access-token"

        every { jwtTokenProvider.validateToken(invalidToken) } returns false

        // when, then
        assertThrows<LogoutFailException> {
            userAppService.logout(invalidToken)
        }

        verify { jwtTokenProvider.validateToken(invalidToken) }
        verify(exactly = 0) { jwtTokenProvider.getUsernameFromToken(any()) }
        verify(exactly = 0) { redisTemplate.opsForValue() }
        verify(exactly = 0) { redisTemplate.delete(any<String>()) }
    }
}
