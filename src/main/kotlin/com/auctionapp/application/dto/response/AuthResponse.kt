package com.auctionapp.application.dto.response

data class SignupResponse(
    val userId: Long,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class LogoutResponse(
    val token: String,
)
