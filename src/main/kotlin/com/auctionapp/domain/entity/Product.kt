package com.auctionapp.domain.entity

import com.auctionapp.com.auctionapp.domain.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
class Product(
    private var name: String,
    private var description: String? = null,
    private var imageUrl: String,
    @ManyToOne
    private val user: User,
    @OneToOne
    private val auction: Auction,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long?,
) {
    init {
        if (!isValidName(name)) {
            throw IllegalArgumentException("상품 명은 3자 이상 100자 이하여야 한다")
        }
    }

    private fun isValidName(name: String): Boolean {
        return name.length in 3..100
    }
}