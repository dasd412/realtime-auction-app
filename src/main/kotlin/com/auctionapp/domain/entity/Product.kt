package com.auctionapp.domain.entity

import jakarta.persistence.*

@Entity
class Product(
    var name: String,
    var description: String? = null,
    var imageUrl: String,
    @Enumerated(EnumType.STRING)
    var status: ProductStatus,
    @ManyToOne
    val user: User,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        if (!isValidName(name)) {
            throw IllegalArgumentException("상품 명은 3자 이상 100자 이하여야 한다")
        }
    }

    private fun isValidName(name: String): Boolean {
        return name.length in 3..100
    }

    companion object {
        fun fixture(
            name: String = "product",
            description: String? = "test",
            imageUrl: String = "test@test.com",
            status: ProductStatus = ProductStatus.AVAILABLE,
            user: User,
        ): Product {
            return Product(
                name = name,
                description = description,
                imageUrl = imageUrl,
                status = status,
                user = user,
            )
        }
    }
}