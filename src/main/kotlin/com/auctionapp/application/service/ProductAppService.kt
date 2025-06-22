package com.auctionapp.com.auctionapp.application.service

import com.auctionapp.domain.service.ProductService
import com.auctionapp.infrastructure.persistence.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductAppService(
    private val productService: ProductService,
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun registerProduct(
        userId: Long,
        name: String,
        description: String?,
        imageUrl: String,
    ): Long {
        return 1L
    }

    @Transactional(readOnly = true)
    fun getProductList() {
    }

    @Transactional(readOnly = true)
    fun getProductDetail(id: Long) {
    }

    @Transactional
    fun updateProduct(
        userId: Long,
        id: Long,
        name: String,
        description: String?,
        imageUrl: String,
    ) {
    }

    @Transactional
    fun deleteProduct(
        userId: Long,
        id: Long,
    ) {
    }
}
