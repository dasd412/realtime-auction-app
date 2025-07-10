package com.auctionapp.application.dto.response

import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.ProductStatus
import org.springframework.data.domain.Page

data class ProductRegisterResponse(
    val productId: Long
)

data class ProductUpdateResponse(
    val productId: Long
)

data class ProductDeleteResponse(
    val productId: Long
)

data class ProductDetailResponse(
    val productId: Long,
    val name: String,
    val description: String?,
    val imageUrl: String,
    val status: ProductStatus,
    val sellerId: Long,
    val sellerName: String,
)

fun Product.toDetailResponse(): ProductDetailResponse {
    return ProductDetailResponse(
        productId = this.id!!,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl,
        status = this.status,
        sellerId = this.user.id!!,
        sellerName = this.user.name,
    )
}

data class ProductSimpleResponse(
    val productId: Long,
    val name: String,
    val imageUrl: String,
    val status: ProductStatus
)

fun Product.toSimpleResponse(): ProductSimpleResponse {
    return ProductSimpleResponse(
        productId = this.id!!,
        name = this.name,
        imageUrl = this.imageUrl,
        status = this.status
    )
}

data class ProductListResponse(
    val products: List<ProductSimpleResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val hasNext: Boolean,
)

fun Page<Product>.toListResponse(): ProductListResponse {
    return ProductListResponse(
        products = this.content.map { it.toSimpleResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        currentPage = this.number,
        hasNext = this.hasNext()
    )
}


