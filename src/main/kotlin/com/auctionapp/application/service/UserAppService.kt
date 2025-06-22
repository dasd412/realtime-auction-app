package com.auctionapp.com.auctionapp.application.service

import com.auctionapp.com.auctionapp.infrastructure.persistence.UserRepository
import com.auctionapp.domain.vo.Email
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserAppService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun signUp(
        email: Email,
        password: String,
        name: String,
    ) {
    }

    @Transactional
    fun signIn(
        email: Email,
        password: String,
    ) {
    }

    fun refreshToken(accessToken: String): String {
        return ""
    }
}
