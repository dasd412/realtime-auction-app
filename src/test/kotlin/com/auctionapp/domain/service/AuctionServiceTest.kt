package com.auctionapp.domain.service

import com.auctionapp.domain.entity.*
import com.auctionapp.domain.exception.*
import com.auctionapp.domain.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@SpringBootTest
class AuctionServiceTest(
    @Autowired
    val auctionService: AuctionService
) {
    @Test
    @DisplayName("경매 등록 시 상품 소유자가 아니면 실패한다")
    fun registerAuctionWithInvalidUserTest() {
        //given
        val user1 = User.fixture(id = 1L)
        val user2 = User.fixture(id = 2L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user2)

        //when & then
        assertThrows<UnAuthorizedProductException> {
            auctionService.registerAuction(
                Auction.fixture(product = product, user = user1),
                user1,
                product
            )
        }
    }

    @Test
    @DisplayName("상품이 이미 팔렸으면 경매 등록이 실패한다")
    fun registerAuctionWithSoldProductTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.SOLD, user = user)

        //when & then
        assertThrows<AlreadySoldProductException> {
            auctionService.registerAuction(
                Auction.fixture(product = product, user = user),
                user,
                product
            )
        }
    }

    @Test
    @DisplayName("경매 등록이 성공한다")
    fun registerAuctionTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user)

        //when
        auctionService.registerAuction(
            auction,
            user,
            product
        )

        //then
        assertThat(auction.status).isEqualTo(AuctionStatus.NOT_STARTED)
    }

    @Test
    @DisplayName("경매 시작 시간이 되지 않으면 경매가 시작되지 않는다")
    fun startAuctionAtNotStartTimeTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(
            product = product,
            user = user,
            id = 1L,
            startTime = LocalDateTime.now().minusHours(1)
        )

        //when
        auctionService.startAuction(auction, LocalDateTime.now().minusHours(2))

        //then
        assertThat(auction.status).isEqualTo(AuctionStatus.NOT_STARTED)
    }

    @Test
    @DisplayName("경매 상태 변경이 올바르지 않으면 경매 시작이 실패한다")
    fun startAuctionWithInvalidStatusTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user, status = AuctionStatus.ACTIVE)

        //when & then
        assertThrows<InvalidAuctionStatusChangeException> {
            auctionService.startAuction(auction, LocalDateTime.now())
        }
    }

    @Test
    @DisplayName("경매 시작 시간이 되면 경매가 시작된다")
    fun startAuctionAtStartTimeTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user, id = 1L)

        //when
        auctionService.startAuction(auction, LocalDateTime.now())

        //then
        assertThat(auction.status).isEqualTo(AuctionStatus.ACTIVE)
    }

    @Test
    @DisplayName("경매 종료 시간이 되지 않으면 경매가 종료되지 않는다")
    fun endAuctionAtNotEndTimeTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction =
            Auction.fixture(product = product, user = user, id = 1L, status = AuctionStatus.ACTIVE)

        //when
        auctionService.endAuction(auction, LocalDateTime.now().minusHours(1))

        //then
        assertThat(auction.status).isEqualTo(AuctionStatus.ACTIVE)
    }

    @Test
    @DisplayName("경매 상태 변경이 올바르지 않으면 경매 종료가 실패한다")
    fun endAuctionWithInvalidStatusTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction =
            Auction.fixture(product = product, user = user, status = AuctionStatus.NOT_STARTED)

        //when & then
        assertThrows<InvalidAuctionStatusChangeException> {
            auctionService.endAuction(auction, LocalDateTime.now())
        }
    }

    @Test
    @DisplayName("경매 종료 시간이 되면 경매가 종료된다")
    fun endAuctionAtEndTimeTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction =
            Auction.fixture(product = product, user = user, id = 1L, status = AuctionStatus.ACTIVE)

        //when
        auctionService.endAuction(auction, LocalDateTime.now())

        //then
        assertThat(auction.status).isEqualTo(AuctionStatus.ENDED)
    }

    @Test
    @DisplayName("경매 종료 시 최고 입찰자가 있으면 상품이 팔린다")
    fun endAuctionWithHighestBidderTest() {
        //given
        val user1 = User.fixture(id = 1L)
        val user2 = User.fixture(id = 2L)
        val user3 = User.fixture(id = 3L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user1)
        val auction =
            Auction.fixture(product = product, user = user1, id = 1L, status = AuctionStatus.ACTIVE)
        auctionService.placeBid(Money(1000L), user2, auction)
        auctionService.placeBid(Money(2000L), user3, auction)

        //when
        auctionService.endAuction(auction, LocalDateTime.now())

        //then
        assertThat(product.isSold()).isTrue()
        assertThat(auction.status).isEqualTo(AuctionStatus.ENDED)
    }

    @Test
    @DisplayName("경매 종료 시 최고 입찰자가 없으면 상품이 팔리지 않는다")
    fun endAuctionWithNoHighestBidderTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction =
            Auction.fixture(product = product, user = user, id = 1L, status = AuctionStatus.ACTIVE)

        //when
        auctionService.endAuction(auction, LocalDateTime.now())

        //then
        assertThat(product.isAvailable()).isTrue()
        assertThat(auction.status).isEqualTo(AuctionStatus.ENDED)
    }

    @Test
    @DisplayName("경매 취소 시 경매 소유자가 아니면 실패한다")
    fun cancelAuctionWithInvalidUserTest() {
        //given
        val user1 = User.fixture(id = 1L)
        val user2 = User.fixture(id = 2L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user2)
        val auction =
            Auction.fixture(product = product, user = user1, status = AuctionStatus.NOT_STARTED)

        //when & then
        assertThrows<UnAuthorizedCancelAuctionException> {
            auctionService.cancelAuction(auction, user2)
        }
    }

    @Test
    @DisplayName("경매 취소 시 이미 시작된 경매는 취소할 수 없다")
    fun cancelAuctionWithStartedAuctionTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction =
            Auction.fixture(product = product, user = user, status = AuctionStatus.ACTIVE)

        //when & then
        assertThrows<CannotCancelActiveAuctionException> {
            auctionService.cancelAuction(auction, user)
        }
    }

    @Test
    @DisplayName("경매 취소 시 경매가 취소된다")
    fun cancelAuctionTest() {
        //given
        val user = User.fixture(id = 1L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction =
            Auction.fixture(product = product, user = user, status = AuctionStatus.NOT_STARTED)

        //when
        auctionService.cancelAuction(auction, user)

        //then
        assertThat(auction.status).isEqualTo(AuctionStatus.CANCELED)
    }

    @Test
    @DisplayName("진행 중이 아닌 입찰에는 실패한다")
    fun placeBidAtNotInProgressAuctionTest() {
        //given
        val user = User.fixture()
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user, status = AuctionStatus.ENDED)

        //when & then
        assertThrows<InvalidBidException> {
            auctionService.placeBid(Money(1000L), user, auction)
        }
    }

    @Test
    @DisplayName("자신의 경매에 입찰하면 실패한다")
    fun placeBidAtOwnAuctionTest() {
        //given
        val user = User.fixture(id = 1L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user)
        val auction = Auction.fixture(product = product, user = user, status = AuctionStatus.ACTIVE)

        //when & then
        assertThrows<InvalidBidException> {
            auctionService.placeBid(Money(1000L), user, auction)
        }
    }

    @Test
    @DisplayName("최고 입찰가 없으면 최소 입찰 금액 이상의 금액으로 입찰할 수 있다")
    fun placeBidAtNoHighestBidTest() {
        //given
        val user1 = User.fixture(id = 1L)
        val user2 = User.fixture(id = 2L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user1)
        val auction =
            Auction.fixture(product = product, user = user1, status = AuctionStatus.ACTIVE)

        //when
        auctionService.placeBid(Money(1000L), user2, auction)

        //then
        assertThat(auction.getHighestBid()?.amount?.amount).isEqualTo(Money(1000L).amount)
    }

    @Test
    @DisplayName("최고 입찰가 있으면 최소 입찰 금액 이상의 금액으로 입찰할 수 있다")
    fun placeBidAtHighestBidTest() {
        //given
        val user1 = User.fixture(id = 1L)
        val user2 = User.fixture(id = 2L)
        val user3 = User.fixture(id = 3L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user1)
        val auction =
            Auction.fixture(product = product, user = user1, status = AuctionStatus.ACTIVE)

        //when
        val bid1 = auctionService.placeBid(Money(1000L), user2, auction)
        val bid2 = auctionService.placeBid(Money(2000L), user3, auction)

        //then
        assertThat(auction.getHighestBid()?.amount?.amount).isEqualTo(Money(2000L).amount)
        assertThat(auction.getHighestBidder()?.id).isEqualTo(user3.id)
        assertThat(auction.getBidCounts()).isEqualTo(2)
        assertThat(bid1.isHigherThan(bid2)).isFalse()
        assertThat(bid2.isHigherThan(bid1)).isTrue()
    }
}