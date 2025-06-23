package com.auctionapp.com.auctionapp.application.service

import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.NotFoundUserException
import com.auctionapp.application.exception.NotProductOwnerException
import com.auctionapp.application.exception.UnavailableMethodInAuctionException
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.service.ProductService
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductAppService(
    private val productService: ProductService,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val auctionRepository: AuctionRepository,
) {
    @Transactional
    fun registerProduct(
        userId: Long,
        name: String,
        description: String?,
        imageUrl: String,
    ): Long {
        val foundUser = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()

        val product = Product(name = name, description = description, imageUrl = imageUrl, user = foundUser)
        productService.registerProduct(product, foundUser)

        val saved = productRepository.save(product)
        return saved.id!!
    }

    @Transactional(readOnly = true)
    fun getProductList(
        name: String? = null,
        pageable: Pageable,
    ): Page<Product> {
        return if (name.isNullOrBlank()) {
            productRepository.findAllByOrderByIdDesc(pageable)
        } else {
            productRepository.findByNameContainingOrderByIdDesc(name, pageable)
        }
    }

    @Transactional(readOnly = true)
    fun getProductListByUserId(userId: Long): List<Product> {
        return productRepository.findByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getProductDetail(id: Long): Product {
        val found = productRepository.findByIdOrNull(id) ?: throw NotFoundProductException()
        return found
    }

    @Transactional
    fun updateProduct(
        userId: Long,
        id: Long,
        name: String,
        description: String?,
        imageUrl: String,
    ) {
        val found = productRepository.findByIdOrNull(id) ?: throw NotFoundProductException()

        if (found.user.id != userId) {
            throw NotProductOwnerException()
        }

        val auction = auctionRepository.findByProduct(found)

        if (!found.canUpdateOrDelete(auction)) {
            throw UnavailableMethodInAuctionException()
        }

        found.name = name
        found.description = description
        found.imageUrl = imageUrl

        productRepository.save(found)
    }

    @Transactional
    fun deleteProduct(
        userId: Long,
        id: Long,
    ) {
        val found = productRepository.findByIdOrNull(id) ?: throw NotFoundProductException()

        if (found.user.id != userId) {
            throw NotProductOwnerException()
        }

        val auction = auctionRepository.findByProduct(found)

        if (!found.canUpdateOrDelete(auction)) {
            throw UnavailableMethodInAuctionException()
        }

        productRepository.delete(found)
    }
}
