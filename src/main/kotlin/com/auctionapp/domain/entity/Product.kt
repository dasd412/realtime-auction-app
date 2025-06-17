package com.auctionapp.domain.entity

import com.auctionapp.com.auctionapp.domain.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Product(
    private var name: String,
    private var description: String?,
    private var initialPrice: Long,
    private var imageUrl: String,
    @ManyToOne
    private val user: User,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long?,
) {
    init {
        if(!isValidName(name)){
            throw IllegalArgumentException("상품 명은 3자 이상 100자 이하여야 한다")
        }
        if (initialPrice < 1000) {
            throw IllegalArgumentException("초기 가격은 1000원 이상이어야 합니다")
        }
    }

    private fun isValidName(name: String): Boolean {
        return name.length in 3..100
    }
}