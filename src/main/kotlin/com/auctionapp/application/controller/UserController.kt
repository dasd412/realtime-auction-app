package com.auctionapp.application.controller

import com.auctionapp.application.dto.LoginRequest
import com.auctionapp.application.dto.SignupRequest
import com.auctionapp.application.dto.TokenResponse
import com.auctionapp.application.service.UserAppService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class UserController(
    private val userAppService: UserAppService,
) {

    @PostMapping("signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<*>  {
        val userId = userAppService.signup(request)
        return ResponseEntity.ok(mapOf("userId" to userId, "message" to "회원가입이 성공적으로 완료되었습니다."))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = userAppService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestHeader("Authorization") refreshToken: String,
    ): ResponseEntity<TokenResponse> {
        val token = refreshToken.replace("Bearer ", "")
        val tokenResponse = userService.refreshToken(token)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") accessToken: String,
    ): ResponseEntity<*> {
        val token = accessToken.replace("Bearer ", "")
        userService.logout(token)
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다."))
    }
}
