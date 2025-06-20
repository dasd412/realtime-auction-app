package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidAuctionTimeException
import com.auctionapp.domain.exception.InvalidInitialPriceException
import com.auctionapp.domain.exception.InvalidMinimumBidUnitException
import com.auctionapp.domain.vo.Money
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
        val initialPrice = Money(999L)
        val user = User.fixture()
        val product = Product.fixture(user = user)

        //when & then
        assertThrows<InvalidInitialPriceException> {
            Auction.fixture(initialPrice = initialPrice, user = user, product = product)
        }
    }

    @Test
    @DisplayName("최소 입찰 단위가 0원 미만이면 실패한다")
    fun auctionInvalidMinimumBidUnitTest() {
        //given
        val minimumBidUnit = Money(0L)
        val user = User.fixture()
        val product = Product.fixture(user = user)

        //when & then
        assertThrows<InvalidMinimumBidUnitException> {
            Auction.fixture(minimumBidUnit = minimumBidUnit, user = user, product = product)
        }
    }

    @Test
    @DisplayName("종료 시각이 시작 시간보다 1시간 이후가 아니면 실패한다")
    fun auctionInvalidTimeSequenceTest() {
        //given
        val startTime = LocalDateTime.now()
        val endTime = startTime.minusHours(1)
        val user = User.fixture()
        val product = Product.fixture(user = user)

        //when & then
        assertThrows<InvalidAuctionTimeException> {
            Auction.fixture(
                startTime = startTime,
                endTime = endTime,
                user = user,
                product = product
            )
        }
    }

    @Test
    @DisplayName("적절한 초기 가격, 최소 입찰 단위, 시작 시각, 종료 시각이면 성공한다")
    fun auctionTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(user = user)
        val initialPrice = Money(1000L)
        val minimumBidUnit = Money(100L)
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)

        //when
        val auction = Auction.fixture(
            initialPrice = initialPrice,
            minimumBidUnit = minimumBidUnit,
            startTime = startTime,
            endTime = endTime,
            user = user,
            product = product
        )

        //then
        assertThat(auction.initialPrice).isEqualTo(initialPrice)
        assertThat(auction.minimumBidUnit).isEqualTo(minimumBidUnit)
        assertThat(auction.startTime).isEqualTo(startTime)
        assertThat(auction.endTime).isEqualTo(endTime)
    }
}