package com.auctionapp.application.controller

import com.auctionapp.application.dto.request.LoginRequest
import com.auctionapp.application.dto.request.SignupRequest
import com.auctionapp.application.dto.response.AuthResponse
import com.auctionapp.application.service.AuthAppService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authAppService: AuthAppService,
) {
    @PostMapping("signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<*> {
        val userId = authAppService.signup(request)
        return ResponseEntity.ok(mapOf("userId" to userId, "message" to "회원가입이 성공적으로 완료되었습니다."))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthResponse> {
        val tokenResponse = authAppService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestHeader("Authorization") refreshToken: String,
    ): ResponseEntity<AuthResponse> {
        val token = refreshToken.replace("Bearer ", "")
        val tokenResponse = authAppService.refreshToken(token)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") accessToken: String,
    ): ResponseEntity<*> {
        val token = accessToken.replace("Bearer ", "")
        authAppService.logout(token)
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다."))
    }
}
