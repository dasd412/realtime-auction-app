package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidBidAmountException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class BidTest {

    @Test
    @DisplayName("입찰 금액이 음수면 실패한다")
    fun bidMinusAmountTest() {
        //given
        val amount = -1000L
        val user = User.fixture()
        val product = Product.fixture(user = user)
        val auction = Auction.fixture(user = user, product = product)

        //when & then
        assertThrows<InvalidBidAmountException> {
            Bid.fixture(
                amount = amount,
                user = user,
                auction = auction,
                createdAt = LocalDateTime.now()
            )
        }
    }

    @Test
    @DisplayName("정상적인 입찰 금액이면 성공한다")
    fun bidTest() {
        //given
        val amount = 1000L
        val user = User.fixture()
        val product = Product.fixture(user = user)
        val auction = Auction.fixture(user = user, product = product)

        //when 
        val bid = Bid.fixture(
            amount = amount,
            user = user,
            auction = auction,
            createdAt = LocalDateTime.now()
        )

        //then
        assertThat(bid.amount).isEqualTo(amount)
    }
}