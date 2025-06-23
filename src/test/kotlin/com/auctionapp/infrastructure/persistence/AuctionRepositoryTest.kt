package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.*
import com.auctionapp.domain.vo.Email
import com.auctionapp.domain.vo.Money
import jakarta.persistence.EntityManager
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.hibernate.Hibernate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
class AuctionRepositoryTest
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
    private lateinit var product1: Product
    private lateinit var product2: Product
    private lateinit var activeAuction: Auction
    private lateinit var notStartedAuction: Auction
    @Autowired
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun setup() {
        // 사용자 생성
        seller = User(Email("seller@test.com"), "Test123456!", "판매자", Role.CUSTOMER)
        bidder1 = User(Email("bidder1@test.com"), "Test123456!", "입찰자1", Role.CUSTOMER)
        bidder2 = User(Email("bidder2@test.com"), "Test123456!", "입찰자2", Role.CUSTOMER)
        userRepository.saveAll(listOf(seller, bidder1, bidder2))

        // 상품 생성
        product1 = Product("상품1", "설명1", "https://example.com/image1.jpg", ProductStatus.AVAILABLE, seller)
        product2 = Product("상품2", "설명2", "https://example.com/image2.jpg", ProductStatus.AVAILABLE, seller)
        productRepository.saveAll(listOf(product1, product2))

        // 경매 생성
        val now = LocalDateTime.now()

        // 진행 중인 경매
        activeAuction =
            Auction(
                initialPrice = Money(1000L),
                minimumBidUnit = Money(100L),
                startTime = now.minusHours(1),
                endTime = now.plusHours(1),
                status = AuctionStatus.ACTIVE,
                user = seller,
                product = product1,
            )

        // 시작 예정 경매
        notStartedAuction =
            Auction(
                initialPrice = Money(2000L),
                minimumBidUnit = Money(200L),
                startTime = now.plusHours(1),
                endTime = now.plusHours(2),
                status = AuctionStatus.NOT_STARTED,
                user = seller,
                product = product2,
            )

        auctionRepository.saveAll(listOf(activeAuction, notStartedAuction))

        // 입찰 생성
        val bid1 = Bid(Money(1500L), now.minusMinutes(30), bidder1, activeAuction)
        val bid2 = Bid(Money(2000L), now.minusMinutes(20), bidder2, activeAuction)
        val bid3 = Bid(Money(2500L), now.minusMinutes(10), bidder1, activeAuction)

        bidRepository.saveAll(listOf(bid1, bid2, bid3))

        // 경매 객체에 입찰 추가
        activeAuction.bids.addAll(listOf(bid1, bid2, bid3))
        auctionRepository.save(activeAuction)
    }

    @AfterEach
    fun clean() {
        bidRepository.deleteAll()
        auctionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("상품으로 경매를 조회한다")
    fun findByProductTest() {
        // when
        val result = auctionRepository.findByProduct(product1)

        // then
        assertThat(result).isNotNull
        assertThat(result?.initialPrice?.amount).isEqualTo(1000L)
        assertThat(result?.minimumBidUnit?.amount).isEqualTo(100L)
    }

    @Test
    @DisplayName("상태별로 경매를 조회한다")
    fun findByStatusTest() {
        // when
        val activePage = auctionRepository.findByStatus(AuctionStatus.ACTIVE, PageRequest.of(0, 10))
        val notStartedPage = auctionRepository.findByStatus(AuctionStatus.NOT_STARTED, PageRequest.of(0, 10))

        // then
        assertThat(activePage.totalElements).isEqualTo(1)
        assertThat(activePage.content[0].id).isEqualTo(activeAuction.id)

        assertThat(notStartedPage.totalElements).isEqualTo(1)
        assertThat(notStartedPage.content[0].id).isEqualTo(notStartedAuction.id)
    }

    @Test
    @DisplayName("시작 시간 순으로 경매를 조회한다")
    fun findByStatusOrderByStartTimeAscTest() {
        // when
        val page = auctionRepository.findByStatusOrderByStartTimeAsc(AuctionStatus.ACTIVE, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content[0].id).isEqualTo(activeAuction.id)
    }

    @Test
    @DisplayName("입찰 수 순으로 경매를 조회한다")
    fun findByStatusOrderByBidsCountDescTest() {
        // when
        val page = auctionRepository.findByStatusOrderByBidsCountDesc(AuctionStatus.ACTIVE, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content[0].id).isEqualTo(activeAuction.id)
        assertThat(page.content[0].bids.size).isEqualTo(3)
    }

    @Test
    @DisplayName("자동 시작 대상 경매를 조회한다")
    fun findAuctionsToStartTest() {
        // given
        val currentTime = notStartedAuction.startTime.plusMinutes(10)

        // when
        val auctions = auctionRepository.findAuctionsToStart(currentTime)

        // then
        assertThat(auctions).hasSize(1)
        assertThat(auctions[0].id).isEqualTo(notStartedAuction.id)
    }

    @Test
    @DisplayName("자동 종료 대상 경매를 조회한다")
    fun findAuctionsToEndTest() {
        // given
        val currentTime = activeAuction.endTime.minusMinutes(10)

        // when
        val auctions = auctionRepository.findAuctionsToEnd(currentTime)

        // then
        assertThat(auctions).hasSize(1)
        assertThat(auctions[0].id).isEqualTo(activeAuction.id)
    }

    @Test
    @DisplayName("사용자가 생성한 경매를 조회한다")
    fun findByUserTest() {
        // when
        val page = auctionRepository.findByUser(seller, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(2)
    }

    @Test
    @DisplayName("사용자가 입찰한 경매를 조회한다")
    fun findByBidUserTest() {
        // when
        val page = auctionRepository.findByBidUser(bidder1, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content[0].id).isEqualTo(activeAuction.id)
        assertThat(page.content[0].bids.size).isEqualTo(3)
    }
}

