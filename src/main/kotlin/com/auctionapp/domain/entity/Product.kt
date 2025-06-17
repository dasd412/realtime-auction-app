package com.auctionapp.domain.entity

import jakarta.persistence.*

@Entity
class Product(
    private var name: String,
    private var description: String? = null,
    private var imageUrl: String,
    @Enumerated(EnumType.STRING)
    private var status: ProductStatus,
    @ManyToOne
    private val user: User,
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