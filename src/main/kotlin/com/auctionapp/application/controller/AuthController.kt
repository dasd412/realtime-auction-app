package com.auctionapp.application.controller

import com.auctionapp.application.dto.request.LoginRequest
import com.auctionapp.application.dto.request.SignupRequest
import com.auctionapp.application.dto.response.LogoutResponse
import com.auctionapp.application.dto.response.SignupResponse
import com.auctionapp.application.dto.response.TokenResponse
import com.auctionapp.application.service.AuthAppService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
    ): ResponseEntity<SignupResponse> {
        val userId = authAppService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(SignupResponse(userId))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = authAppService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestHeader("Authorization") refreshToken: String,
    ): ResponseEntity<TokenResponse> {
        val token = refreshToken.replace("Bearer ", "")
        val tokenResponse = authAppService.refreshToken(token)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") accessToken: String,
    ): ResponseEntity<LogoutResponse> {
        val token = accessToken.replace("Bearer ", "")
        authAppService.logout(token)
        return ResponseEntity.ok(LogoutResponse(token))
    }
}
