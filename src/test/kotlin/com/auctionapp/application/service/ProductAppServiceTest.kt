package com.auctionapp.application.service

import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.exception.AlreadySoldProductException
import com.auctionapp.domain.service.ProductService
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull

class ProductAppServiceTest {
    private val productService = mockk<ProductService>()
    private val auctionRepository = mockk<AuctionRepository>()
    private val userRepository = mockk<UserRepository>()
    private val productRepository = mockk<ProductRepository>()

    private val productAppService =
        ProductAppService(
            productService = productService,
            auctionRepository = auctionRepository,
            userRepository = userRepository,
            productRepository = productRepository,
        )

    @Test
    @DisplayName("제품 등록에 성공한다")
    fun registerProduct_success() {
        // given
        val userId = 1L
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"

        val user = mockk<User>()
        val product = mockk<Product>()

        every { product.id } returns 1L
        every { userRepository.findByIdOrNull(userId) } returns user
        every { product.isSold() } returns false
        every { productRepository.save(any()) } returns product
        every { productService.registerProduct(any(), user) } returns Unit

        // when
        val result = productAppService.registerProduct(userId, name, description, imageUrl)

        // then
        verify { userRepository.findByIdOrNull(userId) }
        verify { productService.registerProduct(any(), user) }
        verify { productRepository.save(any()) }
        assertThat(result).isEqualTo(1L)
    }

    @Test
    @DisplayName("이미 판매된 상품 등록 시 예외가 발생한다")
    fun registerProduct_alreadySold() {
        // given
        val userId = 1L
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"

        val user = mockk<User>()
        val product = mockk<Product>()

        every { userRepository.findByIdOrNull(userId) } returns user
        every { product.isSold() } returns true

        every {
            productService.registerProduct(any(), any())
        } throws AlreadySoldProductException()

        // when & then
        assertThrows<AlreadySoldProductException> {
            productAppService.registerProduct(
                userId = userId,
                name = name,
                description = description,
                imageUrl = imageUrl,
            )
        }

        verify { userRepository.findByIdOrNull(userId) }
        verify(exactly = 0) { productRepository.save(any()) }
    }
}
