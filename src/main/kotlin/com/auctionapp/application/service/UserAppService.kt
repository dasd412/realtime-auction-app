package com.auctionapp.application.service

import com.auctionapp.application.exception.DuplicateEmailException
import com.auctionapp.application.exception.LoginFailException
import com.auctionapp.domain.entity.Role
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import com.auctionapp.infrastructure.persistence.UserRepository
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
    ): User {
        val found = userRepository.findByEmail(email)

        if (found != null) {
            throw DuplicateEmailException()
        }

        val user = User(email = email, password = password, name = name, role = Role.CUSTOMER)

        return userRepository.save(user)
    }

    @Transactional
    fun signIn(
        email: Email,
        password: String,
    ): String {
        val found = userRepository.findByEmailAndPassword(email, password) ?: throw LoginFailException()
        return ""
    }

    fun refreshToken(accessToken: String): String {
        return ""
    }
}
