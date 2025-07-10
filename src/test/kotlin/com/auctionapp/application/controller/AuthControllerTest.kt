package com.auctionapp.application.controller

import com.auctionapp.TestSecurityConfig
import com.auctionapp.application.dto.request.LoginRequest
import com.auctionapp.application.dto.request.SignupRequest
import com.auctionapp.application.dto.response.TokenResponse
import com.auctionapp.application.exception.DuplicateEmailException
import com.auctionapp.application.exception.LoginFailException
import com.auctionapp.application.exception.LogoutFailException
import com.auctionapp.application.exception.UnavailableRefreshTokenException
import com.auctionapp.application.service.AuthAppService
import com.auctionapp.domain.entity.Role
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Import(TestSecurityConfig::class)
@WebMvcTest(controllers = [AuthController::class])
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authAppService: AuthAppService

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun signupSuccess() {
        // given
        val request =
            SignupRequest(
                email = "test@example.com",
                password = "Test1234!@#$",
                name = "테스터",
                role = Role.CUSTOMER,
            )
        val userId = 1L

        given(authAppService.signup(request)).willReturn(userId)

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userId").value(userId))
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    fun signupFailDuplicateEmail() {
        // given
        val request =
            SignupRequest(
                email = "duplicate@example.com",
                password = "Test1234!@#$",
                name = "중복유저",
                role = Role.CUSTOMER,
            )

        given(authAppService.signup(request)).willThrow(DuplicateEmailException())

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검사 실패")
    fun signupFailValidation() {
        // given
        val request =
            SignupRequest(
                email = "invalid-email",
                password = "short",
                name = "",
                role = Role.CUSTOMER,
            )

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors").exists())
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    fun loginSuccess() {
        // given
        val request =
            LoginRequest(
                email = "test@example.com",
                password = "Test1234!@#$",
            )
        val tokenResponse =
            TokenResponse(
                accessToken = "test.access.token",
                refreshToken = "test.refresh.token",
            )

        given(authAppService.login(request)).willReturn(tokenResponse)

        // when & then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value(tokenResponse.accessToken))
            .andExpect(jsonPath("$.refreshToken").value(tokenResponse.refreshToken))
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격 증명")
    fun loginFailInvalidCredentials() {
        // given
        val request =
            LoginRequest(
                email = "wrong@example.com",
                password = "WrongPass123!",
            )

        given(authAppService.login(request)).willThrow(LoginFailException())

        // when & then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    fun refreshTokenSuccess() {
        // given
        val refreshToken = "test.refresh.token"
        val tokenResponse =
            TokenResponse(
                accessToken = "new.access.token",
                refreshToken = refreshToken,
            )

        given(authAppService.refreshToken(refreshToken)).willReturn(tokenResponse)

        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .header("Authorization", "Bearer $refreshToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value(tokenResponse.accessToken))
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰")
    fun refreshTokenFailInvalidToken() {
        // given
        val invalidToken = "invalid.refresh.token"

        given(authAppService.refreshToken(invalidToken)).willThrow(UnavailableRefreshTokenException())

        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .header("Authorization", "Bearer $invalidToken"),
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    fun logoutSuccess() {
        // given
        val accessToken = "test.access.token"

        doNothing().`when`(authAppService).logout(accessToken)

        // when & then
        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value(accessToken))
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효하지 않은 액세스 토큰")
    fun logoutFailInvalidToken() {
        // given
        val invalidToken = "invalid.access.token"

        doThrow(LogoutFailException()).`when`(authAppService).logout(invalidToken)

        // when & then
        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "Bearer $invalidToken"),
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }
}
