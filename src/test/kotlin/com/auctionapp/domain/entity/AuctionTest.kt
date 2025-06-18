package com.auctionapp.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class AuctionTest {
    @Test
    @DisplayName("초기 가격이 1000원 미만이면 실패한다")
    fun auctionInvalidInitialPriceTest() {
        //given
        val initialPrice = 999L
        val user = User.fixture()
        val product=Product.fixture(user = user)

        //when & then
        assertThrows<IllegalArgumentException> {
            Auction.fixture(initialPrice = initialPrice, user = user,product=product)
        }.apply {
            assertThat(message).isEqualTo("초기 가격은 1000원 이상이어야 합니다")
        }
    }

    @Test
    @DisplayName("최소 입찰 단위가 음수면 실패한다")
    fun auctionInvalidMinimumBidUnitPriceTest() {
        //given
        val minimumBidUnit = -100L
        val user = User.fixture()
        val product=Product.fixture(user = user)

        //when & then
        assertThrows<IllegalArgumentException> {
            Auction.fixture(minimumBidUnit = minimumBidUnit, user = user,product=product)
        }.apply {
            assertThat(message).isEqualTo("최소 입찰 단위는 음수가 될 수 없습니다")
        }
    }

    @Test
    @DisplayName("종료 시각이 시작 시간보다 1시간 이후가 아니면 실패한다")
    fun auctionInvalidTimeSequenceTest() {
        //given
        val startTime = LocalDateTime.now()
        val endTime = startTime.minusHours(1)
        val user = User.fixture()
        val product=Product.fixture(user = user)

        //when & then
        assertThrows<IllegalArgumentException> {
            Auction.fixture(startTime = startTime, endTime = endTime, user = user,product=product)
        }.apply {
            assertThat(message).isEqualTo("종료 시각은 시작 시간보다 최소 1시간 이후여야 합니다")
        }
    }

    @Test
    @DisplayName("적절한 초기 가격, 최소 입찰 단위, 시작 시각, 종료 시각이면 성공한다")
    fun auctionTest() {
        //given
        val user = User.fixture()
        val product=Product.fixture(user = user)
        val initialPrice = 1000L
        val minimumBidUnit = 100L
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)

        //when
        val auction = Auction.fixture(initialPrice = initialPrice, minimumBidUnit = minimumBidUnit, startTime = startTime, endTime = endTime, user = user,product=product)

        //then
        assertThat(auction.initialPrice).isEqualTo(initialPrice)
        assertThat(auction.minimumBidUnit).isEqualTo(minimumBidUnit)
        assertThat(auction.startTime).isEqualTo(startTime)
        assertThat(auction.endTime).isEqualTo(endTime)
    }
}