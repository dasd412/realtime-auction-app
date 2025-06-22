package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidPasswordException
import com.auctionapp.domain.exception.InvalidUserNameException
import com.auctionapp.domain.vo.Email
import jakarta.persistence.*
import java.util.regex.Pattern

@Entity
class User(
    @Embedded
    var email: Email,
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

    fun registerProduct(product: Product) {
        this.products.add(product)
    }

    fun registerAuction(auction: Auction) {
        this.auctions.add(auction)
    }

    fun placeBid(bid: Bid) {
        this.bids.add(bid)
    }

    fun isOwnerOf(product: Product): Boolean {
        return this.id == product.user.id
    }

    fun isOwnerOf(auction: Auction): Boolean {
        return this.id == auction.user.id
    }

    companion object {
        fun fixture(
            email: Email = Email("test@test.com"),
            password: String = "Test12345!",
            name: String = "test",
            role: Role = Role.CUSTOMER,
            id: Long? = null,
        ): User {
            return User(
                email = email,
                password = password,
                name = name,
                role = role,
                id = id,
            )
        }
    }
}
