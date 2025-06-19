package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidProductImageUrlException
import com.auctionapp.domain.exception.InvalidProductNameException
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
            if (!isValidImageUrl(value)) {
                throw InvalidProductImageUrlException()
            }
            field = value
        }

    init {
        if (!isValidName(name)) {
            throw InvalidProductNameException()
        }
        if (!isValidImageUrl(imageUrl)) {
            throw InvalidProductImageUrlException()
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

    fun markAsSold() {
        this.status = ProductStatus.SOLD
    }

    fun markAsAvailable() {
        this.status = ProductStatus.AVAILABLE
    }

    fun canUpdateOrDelete(auction: Auction?): Boolean {
        return auction == null || auction.status == AuctionStatus.NOT_STARTED
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