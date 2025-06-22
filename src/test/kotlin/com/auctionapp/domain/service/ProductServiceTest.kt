package com.auctionapp.domain.service

import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.ProductStatus
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.exception.AlreadySoldProductException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceTest(
    @Autowired
    val productService: ProductService,
) {
    @Test
    @DisplayName("판매 완료된 상품을 등록하면 실패한다")
    fun registerSoldProductTest() {
        // given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.SOLD, user = user)

        // when & then
        assertThrows<AlreadySoldProductException> {
            productService.registerProduct(product, user)
        }
    }

    @Test
    @DisplayName("판매 완료되지 않은 상품을 등록하면 성공한다")
    fun registerAvailableProductTest() {
        // given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)

        // when
        productService.registerProduct(product, user)

        // then
        assertThat(product.status).isEqualTo(ProductStatus.AVAILABLE)
        assertThat(product.isAvailable()).isTrue
    }
}
