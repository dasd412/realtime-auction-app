package com.auctionapp.application.service

import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.NotFoundUserException
import com.auctionapp.application.exception.NotProductOwnerException
import com.auctionapp.application.exception.UnauthorizedException
import com.auctionapp.application.exception.UnavailableMethodInAuctionException
import com.auctionapp.com.auctionapp.utils.SecurityUtil
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.exception.AlreadySoldProductException
import com.auctionapp.domain.service.ProductService
import com.auctionapp.domain.vo.Email
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull

class ProductAppServiceTest {
    private val productService = mockk<ProductService>()
    private val auctionRepository = mockk<AuctionRepository>()
    private val userRepository = mockk<UserRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val securityUtilMockObject = mockkObject(SecurityUtil) // 지우면 모킹 실패하니 지우지 마세요.

    private val productAppService =
        ProductAppService(
            productService = productService,
            auctionRepository = auctionRepository,
            userRepository = userRepository,
            productRepository = productRepository,
        )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("제품 등록에 성공한다")
    fun registerProduct_success() {
        // given
        val email = "test@example.com"
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"

        val user = mockk<User>()
        val product = mockk<Product>()

        every { product.id } returns 1L
        every { userRepository.findByEmail(Email(email)) } returns user
        every { product.isSold() } returns false
        every { productRepository.save(any()) } returns product
        every { productService.registerProduct(any(), user) } returns Unit
        every { SecurityUtil.getCurrentUsername() } returns email
        // when
        val result = productAppService.registerProduct(name, description, imageUrl)

        // then
        verify { SecurityUtil.getCurrentUsername() }
        verify { userRepository.findByEmail(Email(email)) }
        verify { productService.registerProduct(any(), user) }
        verify { productRepository.save(any()) }
        assertThat(result).isEqualTo(1L)
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 제품 등록에 실패한다")
    fun registerProduct_unauthorized() {
        // given
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"

        every { SecurityUtil.getCurrentUsername() } returns null

        // when & then
        assertThrows<UnauthorizedException> {
            productAppService.registerProduct(name, description, imageUrl)
        }

        verify { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 0) { productRepository.save(any()) }
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 상품 등록에서 예외가 발생한다")
    fun registerProduct_userNotFound() {
        // given
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"
        val email = "test@example.com"

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when & then
        assertThrows<NotFoundUserException> {
            productAppService.registerProduct(name, description, imageUrl)
        }
    }

    @Test
    @DisplayName("이미 판매된 상품 등록 시 예외가 발생한다")
    fun registerProduct_alreadySold() {
        // given
        val email = "test@example.com"
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"

        val user = mockk<User>()
        val product = mockk<Product>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { product.isSold() } returns true

        every {
            productService.registerProduct(any(), any())
        } throws AlreadySoldProductException()

        // when & then
        assertThrows<AlreadySoldProductException> {
            productAppService.registerProduct(
                name = name,
                description = description,
                imageUrl = imageUrl,
            )
        }

        verify { SecurityUtil.getCurrentUsername() }
        verify { userRepository.findByEmail(Email(email)) }
        verify(exactly = 0) { productRepository.save(any()) }
    }

    @Test
    @DisplayName("상품 이름이 비어 있으면 모든 상품을 id 내림차순으로 조회한다")
    fun getProductList_emptyName() {
        // given
        val pageNumber = 0

        val product = mockk<Product>()

        every { productRepository.findAllByOrderByIdDesc(any()) } returns PageImpl(listOf(product))

        // when
        val result = productAppService.getProductList(pageNumber = pageNumber)

        // then
        verify(exactly = 1) { productRepository.findAllByOrderByIdDesc(any()) }
        assertThat(result.content).isEqualTo(listOf(product))
    }

    @Test
    @DisplayName("상품 이름이 비어 있지 않으면 상품 이름에 대한 검색 결과를 id 내림차순으로 조회한다")
    fun getProductList_notEmptyName() {
        // given
        val pageNumber = 0
        val name = "테스트 상품"

        val product = mockk<Product>()

        every { productRepository.findByNameContainingOrderByIdDesc(any(), any()) } returns PageImpl(listOf(product))

        // when
        val result = productAppService.getProductList(name = name, pageNumber = pageNumber)

        // then
        verify(exactly = 1) { productRepository.findByNameContainingOrderByIdDesc(any(), any()) }
        assertThat(result.content).isEqualTo(listOf(product))
    }

    @Test
    @DisplayName("사용자의 상품 목록 조회에 성공한다")
    fun getProductListOfUser_success() {
        // given
        val email = "test@example.com"
        val user = mockk<User>()
        val userId = 1L
        val product = mockk<Product>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByUserId(any()) } returns listOf(product)
        every { user.id } returns userId

        // when
        val result = productAppService.getProductListOfUser()

        // then
        verify(exactly = 1) { productRepository.findByUserId(any()) }
        assertThat(result).isEqualTo(listOf(product))
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 사용자의 상품 목록 조회에 실패한다")
    fun getProductListOfUser_unauthorized() {
        // given
        every { SecurityUtil.getCurrentUsername() } returns null

        // when & then
        assertThrows<UnauthorizedException> {
            productAppService.getProductListOfUser()
        }

        verify { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 0) { productRepository.findByUserId(any()) }
    }

    @Test
    @DisplayName("상품 상세 조회에 실패한다")
    fun getProductDetail_notFound() {
        // given
        val productId = 1L

        every { productRepository.findByIdOrNull(productId) } returns null

        // when & then
        assertThrows<NotFoundProductException> {
            productAppService.getProductDetail(productId)
        }
    }

    @Test
    @DisplayName("상품 상세 조회에 성공한다")
    fun getProductDetail_success() {
        // given
        val productId = 1L

        val product = mockk<Product>()

        every { productRepository.findByIdOrNull(productId) } returns product

        // when
        val result = productAppService.getProductDetail(productId)

        // then
        verify(exactly = 1) { productRepository.findByIdOrNull(productId) }
        assertThat(result).isEqualTo(product)
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 상품 수정에 실패한다")
    fun updateProduct_unauthorized() {
        // given
        val productId = 1L
        val name = "테스트 상품"
        val description = "설명"
        val imageUrl = "http://example.com/image.jpg"

        every { SecurityUtil.getCurrentUsername() } returns null

        // when & then
        assertThrows<UnauthorizedException> {
            productAppService.updateProduct(productId, name, description, imageUrl)
        }

        verify { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 0) { productRepository.findByIdOrNull(any()) }
    }

    @Test
    @DisplayName("존재하지 않는 상품이면 수정에 실패한다")
    fun updateProduct_notFound() {
        // given
        val email = "test@example.com"
        val productId = 1L
        val user = mockk<User>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns null

        // when & then
        assertThrows<NotFoundProductException> {
            productAppService.updateProduct(productId, "테스트 상품", "설명", "http://example.com/image.jpg")
        }
    }

    @Test
    @DisplayName("상품의 소유자가 아니면 수정에 실패한다")
    fun updateProduct_notOwner() {
        // given
        val email = "test@example.com"
        val productId = 1L
        val userId = 1L
        val user = mockk<User>()
        val product = mockk<Product>()

        every { product.user.id } returns 3L
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { user.id } returns userId

        // when & then
        assertThrows<NotProductOwnerException> {
            productAppService.updateProduct(productId, "테스트 상품", "설명", "http://example.com/image.jpg")
        }
    }

    @Test
    @DisplayName("진행 중인 경매의 상품이면 수정에 실패한다")
    fun updateProduct_inAuction() {
        // given
        val productId = 1L
        val userId = 1L
        val email = "test@example.com"
        val user = mockk<User>()
        val product = mockk<Product>()
        val auction = mockk<Auction>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { user.id } returns userId
        every { productRepository.findByIdOrNull(productId) } returns product
        every { auctionRepository.findByProduct(product) } returns auction
        every { product.canUpdateOrDelete(auction) } returns false
        every { product.user.id } returns userId

        // when & then
        assertThrows<UnavailableMethodInAuctionException> {
            productAppService.updateProduct(productId, "테스트 상품", "설명", "http://example.com/image.jpg")
        }
    }

    @Test
    @DisplayName("수정에 성공한다")
    fun updateProduct_success() {
        // given
        val productId = 1L
        val userId = 1L
        val email = "test@example.com"
        val user = mockk<User>()
        val auction = mockk<Auction>()
        val product = mockk<Product>(relaxed = true)
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { auctionRepository.findByProduct(product) } returns auction
        every { product.user.id } returns userId
        every { product.canUpdateOrDelete(auction) } returns true
        every { user.id } returns userId

        // when
        productAppService.updateProduct(productId, "테스트 상품", "설명", "http://example.com/image.jpg")

        // then
        verify(exactly = 1) { productRepository.findByIdOrNull(productId) }
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 상품 삭제에 실패한다")
    fun deleteProduct_unauthorized() {
        // given
        val productId = 1L

        every { SecurityUtil.getCurrentUsername() } returns null

        // when & then
        assertThrows<UnauthorizedException> {
            productAppService.deleteProduct(productId)
        }

        verify { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 0) { productRepository.findByIdOrNull(any()) }
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 삭제에 실패한다")
    fun deleteProduct_notFound() {
        // given
        val email = "test@example.com"
        val user = mockk<User>()
        val productId = 1L

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns null

        // when & then
        assertThrows<NotFoundProductException> {
            productAppService.deleteProduct(productId)
        }
    }

    @Test
    @DisplayName("상품의 소유자가 아니면 삭제에 실패한다")
    fun deleteProduct_notOwner() {
        // given
        val productId = 1L
        val userId = 2L
        val email = "test@example.com"
        val user = mockk<User>()
        val product = mockk<Product>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { product.user.id } returns 3L
        every { user.id } returns userId

        // when & then
        assertThrows<NotProductOwnerException> {
            productAppService.deleteProduct(productId)
        }
    }

    @Test
    @DisplayName("진행 중인 경매의 상품이면 삭제에 실패한다")
    fun deleteProduct_inAuction() {
        // given
        val productId = 1L
        val userId = 1L
        val email = "test@example.com"
        val user = mockk<User>()
        val product = mockk<Product>()
        val auction = mockk<Auction>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { auctionRepository.findByProduct(product) } returns auction
        every { product.canUpdateOrDelete(auction) } returns false
        every { product.user.id } returns userId
        every { user.id } returns userId

        // when & then
        assertThrows<UnavailableMethodInAuctionException> {
            productAppService.deleteProduct(productId)
        }
    }

    @Test
    @DisplayName("삭제에 성공한다")
    fun deleteProduct_success() {
        // given
        val productId = 1L
        val userId = 1L
        val email = "test@example.com"
        val user = mockk<User>()
        val product = mockk<Product>()
        val auction = mockk<Auction>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { auctionRepository.findByProduct(product) } returns auction
        every { product.canUpdateOrDelete(auction) } returns true
        every { product.user.id } returns userId
        every { productRepository.delete(any()) } returns Unit
        every { user.id } returns userId

        // when
        productAppService.deleteProduct(productId)

        // then
        verify(exactly = 1) { productRepository.findByIdOrNull(productId) }
        verify(exactly = 1) { productRepository.delete(any()) }
    }
}
