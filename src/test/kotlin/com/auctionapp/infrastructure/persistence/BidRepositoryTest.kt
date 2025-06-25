package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.*
import com.auctionapp.domain.vo.Email
import com.auctionapp.domain.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BidRepositoryTest
@Autowired
constructor(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
) {
    private lateinit var seller: User
    private lateinit var bidder1: User
    private lateinit var bidder2: User
    private lateinit var product: Product
    private lateinit var auction: Auction
    private lateinit var bid1: Bid
    private lateinit var bid2: Bid
    private lateinit var bid3: Bid
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setup() {
        // 사용자 생성
        seller = User(Email("seller@test.com"), "Test123456!", "판매자", Role.CUSTOMER)
        bidder1 = User(Email("bidder1@test.com"), "Test123456!", "입찰자1", Role.CUSTOMER)
        bidder2 = User(Email("bidder2@test.com"), "Test123456!", "입찰자2", Role.CUSTOMER)
        userRepository.saveAll(listOf(seller, bidder1, bidder2))

        // 상품 생성
        product = Product("테스트상품", "설명", "https://example.com/image.jpg", ProductStatus.AVAILABLE, seller)
        productRepository.save(product)

        // 경매 생성
        auction = Auction(
            initialPrice = Money(1000L),
            minimumBidUnit = Money(100L),
            startTime = now.minusHours(2),
            endTime = now.plusHours(2),
            status = AuctionStatus.ACTIVE,
            user = seller,
            product = product
        )
        auctionRepository.save(auction)

        // 입찰 생성
        bid1 = Bid(Money(1500L), now.minusMinutes(30), bidder1, auction)
        bid2 = Bid(Money(2000L), now.minusMinutes(20), bidder2, auction)
        bid3 = Bid(Money(2500L), now.minusMinutes(10), bidder1, auction)

        bidRepository.saveAll(listOf(bid1, bid2, bid3))

        // 경매에 입찰 추가
        auction.bids.addAll(listOf(bid1, bid2, bid3))
        auctionRepository.save(auction)
    }

    @AfterEach
    fun clean() {
        bidRepository.deleteAll()
        auctionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("경매별 입찰 내역을 최신순으로 조회한다")
    fun findByAuctionOrderByCreatedAtDescTest() {
        // when
        val bidPage = bidRepository.findByAuctionOrderByCreatedAtDesc(auction, PageRequest.of(0, 20))

        // then
        assertThat(bidPage.totalElements).isEqualTo(3)
        assertThat(bidPage.content[0].id).isEqualTo(bid3.id) // 가장 최신 입찰
        assertThat(bidPage.content[1].id).isEqualTo(bid2.id)
        assertThat(bidPage.content[2].id).isEqualTo(bid1.id) // 가장 오래된 입찰
    }

    @Test
    @DisplayName("사용자별 입찰 내역을 최신순으로 조회한다")
    fun findByUserOrderByCreatedAtDescTest() {
        // when
        val bidPage = bidRepository.findByUserOrderByCreatedAtDesc(bidder1, PageRequest.of(0, 20))

        // then
        assertThat(bidPage.totalElements).isEqualTo(2) // bidder1은 2번 입찰함
        assertThat(bidPage.content[0].id).isEqualTo(bid3.id) // 가장 최신 입찰
        assertThat(bidPage.content[1].id).isEqualTo(bid1.id) // 이전 입찰
    }

    @Test
    @DisplayName("경매의 최고 입찰가를 조회한다")
    fun findHighestAmountByAuctionIdTest() {
        // when
        val highestAmount = bidRepository.findHighestAmountByAuctionId(auction.id!!)

        // then
        assertThat(highestAmount).isEqualTo(2500L) // bid3의 금액
    }

    @Test
    @DisplayName("경매의 입찰 수를 조회한다")
    fun countByAuctionIdTest() {
        // when
        val count = bidRepository.countByAuctionId(auction.id!!)

        // then
        assertThat(count).isEqualTo(3)
    }

    @Test
    @DisplayName("입찰이 없는 경매의 최고 입찰가는 null을 반환한다")
    fun findHighestAmountByAuctionIdWithNoBidTest() {
        // given
        val emptyAuction = Auction(
            initialPrice = Money(2000L),
            minimumBidUnit = Money(200L),
            startTime = now.minusHours(1),
            endTime = now.plusHours(1),
            status = AuctionStatus.ACTIVE,
            user = seller,
            product = product
        )
        auctionRepository.save(emptyAuction)

        // when
        val highestAmount = bidRepository.findHighestAmountByAuctionId(emptyAuction.id!!)

        // then
        assertThat(highestAmount).isNull()
    }

    @Test
    @DisplayName("페이지 크기를 제한하여 경매별 입찰 내역을 조회한다")
    fun findByAuctionWithPageSizeLimitTest() {
        // when
        val bidPage = bidRepository.findByAuctionOrderByCreatedAtDesc(auction, PageRequest.of(0, 2))

        // then
        assertThat(bidPage.totalElements).isEqualTo(3) // 전체 요소 수
        assertThat(bidPage.content.size).isEqualTo(2) // 페이지 크기
        assertThat(bidPage.content[0].id).isEqualTo(bid3.id)
        assertThat(bidPage.content[1].id).isEqualTo(bid2.id)
    }
}
