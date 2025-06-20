package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidProductImageUrlException
import com.auctionapp.domain.exception.InvalidProductNameException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {
    @Test
    @DisplayName("상품 몀 길이가 3자 미만이면 실패한다")
    fun productInvalidNameLengthTest1() {
        //given
        val name = "ab"
        val user = User.fixture()

        //when & then
        assertThrows<InvalidProductNameException> {
            Product.fixture(name = name, user = user)
        }
    }

    @Test
    @DisplayName("상품 몀 길이가 100자 초과면 실패한다")
    fun productInvalidNameLengthTest2() {
        //given
        val name = "a".repeat(101)
        val user = User.fixture()

        //when & then
        assertThrows<InvalidProductNameException> {
            Product.fixture(name = name, user = user)
        }
    }

    @Test
    @DisplayName("상품 몀 길이가 3자 이상 100자 이하면 성공한다")
    fun productValidNameLengthTest() {
        //given
        val name = "a".repeat(50)
        val user = User.fixture()

        //when & then
        val product = Product.fixture(name = name, user = user)
        assertThat(product.name).isEqualTo(name)
    }

    @Test
    @DisplayName("부적절한 이름으로 변경하면 실패한다")
    fun productInvalidNameChangeTest() {
        //given
        val name = "a".repeat(50)
        val user = User.fixture()
        val product = Product.fixture(name = name, user = user)

        //when & then
        assertThrows<InvalidProductNameException> {
            product.name = "a".repeat(101)
        }
    }

    @Test
    @DisplayName("적절한 이름으로 변경하면 성공한다")
    fun productValidNameChangeTest() {
        //given
        val name = "a".repeat(50)
        val user = User.fixture()
        val product = Product.fixture(name = name, user = user)

        //when
        product.name = "b".repeat(50)

        //then
        assertThat(product.name).isEqualTo("b".repeat(50))
    }

    @Test
    @DisplayName("부적절한 이미지 url이면 실패한다")
    fun productInvalidImageUrlTest() {
        //given
        val imageUrl = "test"
        val user = User.fixture()

        //when & then
        assertThrows<InvalidProductImageUrlException> {
            Product.fixture(imageUrl = imageUrl, user = user)
        }
    }

    @Test
    @DisplayName("적절한 이미지 url이면 성공한다")
    fun productValidImageUrlTest() {
        //given
        val imageUrl = "https://test.com/test.jpg"
        val user = User.fixture()

        //when 
        val product = Product.fixture(imageUrl = imageUrl, user = user)

        //then
        assertThat(product.imageUrl).isEqualTo(imageUrl)
    }

    @Test
    @DisplayName("부적절한 image url로 변경하면 실패한다")
    fun productInvalidImageUrlChangeTest() {
        //given
        val name = "a".repeat(50)
        val imageUrl = "https://test.com/test.jpg"
        val user = User.fixture()
        val product = Product.fixture(name = name, imageUrl = imageUrl, user = user)

        //when & then
        assertThrows<InvalidProductImageUrlException> {
            product.imageUrl = "test"
        }
    }

    @Test
    @DisplayName("적절한 image url로 변경하면 성공한다")
    fun productValidImageUrlChangeTest() {
        //given
        val name = "a".repeat(50)
        val imageUrl = "https://test.com/test.jpg"
        val user = User.fixture()
        val product = Product.fixture(name = name, imageUrl = imageUrl, user = user)

        //when
        product.imageUrl = "https://test.com/test2.jpg"

        //then
        assertThat(product.imageUrl).isEqualTo("https://test.com/test2.jpg")
    }

    @Test
    @DisplayName("경매가 진행중이면 상품 상태를 변경할 수 없다")
    fun productStatusChangeWithActiveAuctionTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user, status = AuctionStatus.ACTIVE)

        //when & then
        assertThat(product.canUpdateOrDelete(auction)).isFalse()
    }

    @Test
    @DisplayName("경매가 진행중이 아니면 상품 상태를 변경할 수 있다")
    fun productStatusChangeWithNotActiveAuctionTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user, status = AuctionStatus.NOT_STARTED)

        //when & then
        assertThat(product.canUpdateOrDelete(auction)).isTrue()
    }
}