package com.auctionapp.domain.entity

import jakarta.persistence.*

@Entity
class Product(
    var name: String,
    var description: String? = null,
    imageUrl: String,
    @Enumerated(EnumType.STRING)
    var status: ProductStatus,
    @ManyToOne
    val user: User,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    var imageUrl: String = imageUrl
        set(value) {
            if (!isValidImageUrl(imageUrl)) {
                throw IllegalArgumentException("적절한 image url이 아닙니다")
            }
            field = value
        }

    init {
        if (!isValidName(name)) {
            throw IllegalArgumentException("상품 명은 3자 이상 100자 이하여야 한다")
        }
        if (!isValidImageUrl(imageUrl)) {
            throw IllegalArgumentException("적절한 image url이 아닙니다")
        }
    }

    private fun isValidName(name: String): Boolean {
        return name.length in 3..100
    }

    private fun isValidImageUrl(url: String): Boolean {
        val imageExtensions = listOf(".png", ".jpg", ".jpeg", ".gif", ".webp")
        return url.startsWith("http://") || url.startsWith("https://") &&
                imageExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    companion object {
        fun fixture(
            name: String = "product",
            description: String? = "test",
            imageUrl: String = "https://example.com/image.png",
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