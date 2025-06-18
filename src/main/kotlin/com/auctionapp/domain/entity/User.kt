package com.auctionapp.domain.entity

import com.auctionapp.com.auctionapp.domain.exception.InvalidEmailException
import com.auctionapp.com.auctionapp.domain.exception.InvalidPasswordException
import com.auctionapp.com.auctionapp.domain.exception.InvalidUserNameException
import jakarta.persistence.*
import java.util.regex.Pattern

@Entity
class User(
    email: String,
    password: String,
    name: String,
    @Enumerated(EnumType.STRING)
    val role: Role,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val products: MutableList<Product> = mutableListOf(),
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val auctions: MutableList<Auction> = mutableListOf(),
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bids: MutableList<Bid> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    var email: String = email
        set(value) {
            if (!isValidEmail(value)) {
                throw InvalidEmailException()
            }
            field = value
        }

    var password: String = password
        set(value) {
            if (!isValidPassword(value)) {
                throw InvalidPasswordException()
            }
            field = value
        }

    var name: String = name
        set(value) {
            if (value.isBlank()) {
                throw InvalidUserNameException()
            }
            field = value
        }

    init {
        if (!isValidEmail(email)) {
            throw InvalidEmailException()
        }
        if (!isValidPassword(password)) {
            throw InvalidPasswordException()
        }
        if (name.isBlank()) {
            throw InvalidUserNameException()
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

    companion object {
        fun fixture(
            email: String = "test@test.com",
            password: String = "Test12345!",
            name: String = "test",
            role: Role = Role.CUSTOMER,
        ): User {
            return User(
                email = email,
                password = password,
                name = name,
                role = role,
            )
        }
    }
}