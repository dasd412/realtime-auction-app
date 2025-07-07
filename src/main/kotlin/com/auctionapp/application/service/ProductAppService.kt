package com.auctionapp.application.service

import com.auctionapp.application.constant.DEFAULT_PRODUCT_PAGE_SIZE
import com.auctionapp.application.exception.*
import com.auctionapp.com.auctionapp.utils.SecurityUtil
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.service.ProductService
import com.auctionapp.domain.vo.Email
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductAppService(
    private val productService: ProductService,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val auctionRepository: AuctionRepository,
) {
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    fun registerProduct(
        name: String,
        description: String?,
        imageUrl: String,
    ): Long {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()

        val foundUser = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()

        val product = Product(name = name, description = description, imageUrl = imageUrl, user = foundUser)
        productService.registerProduct(product, foundUser)

        val saved = productRepository.save(product)
        return saved.id!!
    }

    @Transactional(readOnly = true)
    fun getProductList(
        name: String? = null,
        pageNumber: Int,
    ): Page<Product> {
        return if (name.isNullOrBlank()) {
            productRepository.findAllByOrderByIdDesc(PageRequest.of(pageNumber, DEFAULT_PRODUCT_PAGE_SIZE))
        } else {
            productRepository.findByNameContainingOrderByIdDesc(
                name,
                PageRequest.of(pageNumber, DEFAULT_PRODUCT_PAGE_SIZE),
            )
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(readOnly = true)
    fun getProductListOfUser(): List<Product> {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
        return productRepository.findByUserId(user.id!!)
    }

    @Transactional(readOnly = true)
    fun getProductDetail(productId: Long): Product {
        val found = productRepository.findByIdOrNull(productId) ?: throw NotFoundProductException()
        return found
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    fun updateProduct(
        productId: Long,
        name: String,
        description: String?,
        imageUrl: String,
    ) {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()

        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()

        val found = productRepository.findByIdOrNull(productId) ?: throw NotFoundProductException()

        if (found.user.id != user.id) {
            throw NotProductOwnerException()
        }

        val auction = auctionRepository.findByProduct(found)

        if (!found.canUpdateOrDelete(auction)) {
            throw UnavailableMethodInAuctionException()
        }

        found.name = name
        found.description = description
        found.imageUrl = imageUrl
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    fun deleteProduct(productId: Long) {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()

        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()

        val found = productRepository.findByIdOrNull(productId) ?: throw NotFoundProductException()

        if (found.user.id != user.id) {
            throw NotProductOwnerException()
        }

        val auction = auctionRepository.findByProduct(found)

        if (!found.canUpdateOrDelete(auction)) {
            throw UnavailableMethodInAuctionException()
        }

        productRepository.delete(found)
    }
}
