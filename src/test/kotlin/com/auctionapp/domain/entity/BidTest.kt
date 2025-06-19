package com.auctionapp.domain.entity

import com.auctionapp.domain.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BidTest {
    @Test
    @DisplayName("정상적인 입찰 금액이면 성공한다")
    fun bidTest() {
        //given
        val amount = Money(1000L)
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