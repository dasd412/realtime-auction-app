package com.auctionapp.com.auctionapp.domain.entity

import jakarta.persistence.*
import java.util.regex.Pattern

@Entity
class User(
    private var email: String,
    private var password: String,
    private var name: String,
    @Enumerated(EnumType.STRING)
    private val role: Role,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long?,
) {
    init {
        if (!isValidEmail(email)) {
            throw IllegalArgumentException("적절한 이메일 형식이 아닙니다")
        }
        if (!isValidPassword(password)) {
            throw IllegalArgumentException("적절한 비밀번호 형식이 아닙니다")
        }
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }

    private fun isValidPassword(password: String): Boolean {
        // 숫자, 소문자, 대문자, 특수문자(@#$%^&*()_+=!~) 각각 1개 이상 포함, 8~16자
        val regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\\$%^&*()_+=!~]).{8,16}$"
        return Pattern.matches(regex, password)
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return Regex(pattern).matches(email)
    }
}