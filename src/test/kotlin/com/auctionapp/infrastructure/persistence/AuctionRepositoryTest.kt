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

        @Test
        @DisplayName("N+1 문제 확인 - 경매 목록 조회 후 입찰 컬렉션 접근 시 추가 쿼리 발생")
        fun nPlusOneProblemForFindByUserTest() {
            // 테스트를 위한 추가 데이터 생성
            val now = LocalDateTime.now()

            // 추가 경매 생성 (3개 더 생성)
            val additionalProducts = mutableListOf<Product>()
            val additionalAuctions = mutableListOf<Auction>()

            for (i in 1..3) {
                val product =
                    Product(
                        "추가상품$i",
                        "설명$i",
                        "https://example.com/image$i.jpg",
                        ProductStatus.AVAILABLE,
                        seller,
                    )
                additionalProducts.add(product)

                val auction =
                    Auction(
                        initialPrice = Money(1000L * i),
                        minimumBidUnit = Money(100L),
                        startTime = now.minusHours(1),
                        endTime = now.plusHours(1),
                        status = AuctionStatus.ACTIVE,
                        user = seller,
                        product = product,
                    )
                additionalAuctions.add(auction)
            }

            productRepository.saveAll(additionalProducts)
            auctionRepository.saveAll(additionalAuctions)

            // 각 경매에 입찰 추가 (각 경매당 2개씩)
            val additionalBids = mutableListOf<Bid>()

            for (auction in additionalAuctions) {
                val bid1 = Bid(Money(auction.initialPrice.amount + 500), now.minusMinutes(20), bidder1, auction)
                val bid2 = Bid(Money(auction.initialPrice.amount + 1000), now.minusMinutes(10), bidder2, auction)
                additionalBids.add(bid1)
                additionalBids.add(bid2)
                auction.bids.add(bid1)
                auction.bids.add(bid2)
            }

            bidRepository.saveAll(additionalBids)
            auctionRepository.saveAll(additionalAuctions)

            // when: 판매자의 모든 경매 조회
            entityManager.flush()
            entityManager.clear()
            val auctions = auctionRepository.findByUser(seller, PageRequest.of(0, 10))

            // then: 각 경매의 입찰 목록에 접근하면 추가 쿼리 발생
            for (auction in auctions) {
                // 이 시점에 Hibernate.isInitialized 확인
                assertFalse(
                    Hibernate.isInitialized(auction.bids),
                    "경매 ${auction.id}의 입찰 목록은 접근 전에 초기화되지 않아야 함",
                )

                // 입찰 목록에 접근 - 이 시점에 추가 쿼리 발생
                println("경매 ID: ${auction.id}, 입찰 수: ${auction.bids.size}")

                // 접근 후 초기화 확인
                assertTrue(
                    Hibernate.isInitialized(auction.bids),
                    "경매 ${auction.id}의 입찰 목록은 접근 후 초기화되어야 함",
                )
            }
        }

        @Test
        @DisplayName("N+1 문제 확인 - 사용자가 입찰한 경매 조회 시 추가 쿼리 발생")
        fun nPlusOneProblemForFindByBidUserTest() {
            // 테스트를 위한 추가 데이터 생성
            val now = LocalDateTime.now()

            // 추가 경매 생성 (3개 더 생성)
            val additionalProducts = mutableListOf<Product>()
            val additionalAuctions = mutableListOf<Auction>()

            for (i in 1..3) {
                val product =
                    Product(
                        "추가상품$i",
                        "설명$i",
                        "https://example.com/image$i.jpg",
                        ProductStatus.AVAILABLE,
                        seller,
                    )
                additionalProducts.add(product)

                val auction =
                    Auction(
                        initialPrice = Money(1000L * i),
                        minimumBidUnit = Money(100L),
                        startTime = now.minusHours(1),
                        endTime = now.plusHours(1),
                        status = AuctionStatus.ACTIVE,
                        user = seller,
                        product = product,
                    )
                additionalAuctions.add(auction)
            }

            productRepository.saveAll(additionalProducts)
            auctionRepository.saveAll(additionalAuctions)

            // 각 경매에 입찰 추가 (모두 bidder1이 입찰)
            val additionalBids = mutableListOf<Bid>()

            for (auction in additionalAuctions) {
                val bid1 = Bid(Money(auction.initialPrice.amount + 500), now.minusMinutes(20), bidder1, auction)
                val bid2 = Bid(Money(auction.initialPrice.amount + 1000), now.minusMinutes(10), bidder2, auction)
                additionalBids.add(bid1)
                additionalBids.add(bid2)
                auction.bids.add(bid1)
                auction.bids.add(bid2)
            }

            bidRepository.saveAll(additionalBids)
            auctionRepository.saveAll(additionalAuctions)

            // when: bidder1이 입찰한 모든 경매 조회
            entityManager.flush()
            entityManager.clear()
            val auctions = auctionRepository.findByBidUser(bidder1, PageRequest.of(0, 10))

            // then: 각 경매의 입찰 목록에 접근하면 추가 쿼리 발생
            for (auction in auctions) {
                // 이 시점에 Hibernate.isInitialized 확인
                assertFalse(
                    Hibernate.isInitialized(auction.bids),
                    "경매 ${auction.id}의 입찰 목록은 접근 전에 초기화되지 않아야 함",
                )

                // 입찰 목록에 접근 - 이 시점에 추가 쿼리 발생
                println("경매 ID: ${auction.id}, 입찰 수: ${auction.bids.size}")

                // 접근 후 초기화 확인
                assertTrue(
                    Hibernate.isInitialized(auction.bids),
                    "경매 ${auction.id}의 입찰 목록은 접근 후 초기화되어야 함",
                )
            }
        }

        @Test
        @DisplayName("N+1 문제 확인 - 인기순 경매 조회 시 추가 쿼리 발생")
        fun nPlusOneProblemForFindByStatusOrderByBidsCountDescTest() {
            // 테스트를 위한 추가 데이터 생성
            val now = LocalDateTime.now()

            // 추가 경매 생성 (3개 더 생성)
            val additionalProducts = mutableListOf<Product>()
            val additionalAuctions = mutableListOf<Auction>()

            for (i in 1..3) {
                val product =
                    Product(
                        "추가상품$i",
                        "설명$i",
                        "https://example.com/image$i.jpg",
                        ProductStatus.AVAILABLE,
                        seller,
                    )
                additionalProducts.add(product)

                val auction =
                    Auction(
                        initialPrice = Money(1000L * i),
                        minimumBidUnit = Money(100L),
                        startTime = now.minusHours(1),
                        endTime = now.plusHours(1),
                        status = AuctionStatus.ACTIVE,
                        user = seller,
                        product = product,
                    )
                additionalAuctions.add(auction)
            }

            productRepository.saveAll(additionalProducts)
            auctionRepository.saveAll(additionalAuctions)

            // 각 경매에 다른 수의 입찰 추가 (인기도 차이 생성)
            val additionalBids = mutableListOf<Bid>()

            for (i in additionalAuctions.indices) {
                val auction = additionalAuctions[i]
                // i+1개의 입찰 추가
                for (j in 0..i) {
                    val bid =
                        Bid(
                            Money(auction.initialPrice.amount + 100L * (j + 1)),
                            now.minusMinutes(j.toLong()),
                            if (j % 2 == 0) bidder1 else bidder2,
                            auction,
                        )
                    additionalBids.add(bid)
                    auction.bids.add(bid)
                }
            }

            bidRepository.saveAll(additionalBids)
            auctionRepository.saveAll(additionalAuctions)

            // when: 인기순으로 경매 조회
            entityManager.flush()
            entityManager.clear()
            val auctions =
                auctionRepository.findByStatusOrderByBidsCountDesc(
                    AuctionStatus.ACTIVE,
                    PageRequest.of(0, 10),
                )

            // then: 각 경매의 입찰 목록에 접근하면 추가 쿼리 발생
            for (auction in auctions) {
                // 이 시점에 Hibernate.isInitialized 확인
                assertFalse(
                    Hibernate.isInitialized(auction.bids),
                    "경매 ${auction.id}의 입찰 목록은 접근 전에 초기화되지 않아야 함",
                )

                // 입찰 목록에 접근 - 이 시점에 추가 쿼리 발생
                println("경매 ID: ${auction.id}, 입찰 수: ${auction.bids.size}")

                // 접근 후 초기화 확인
                assertTrue(
                    Hibernate.isInitialized(auction.bids),
                    "경매 ${auction.id}의 입찰 목록은 접근 후 초기화되어야 함",
                )
            }
        }

        @Test
        @DisplayName("경매 ID로 경매 상세 정보를 조회한다")
        fun findAuctionDetailByIdTest() {
            // given
            entityManager.flush()
            entityManager.clear()

            // when
            val auctionDetail = auctionRepository.findAuctionDetailById(activeAuction.id!!)

            // then
            assertThat(auctionDetail).isNotNull

            // 경매 정보 확인
            val auction = auctionDetail!!.getAuction()
            assertThat(auction.id).isEqualTo(activeAuction.id)
            assertThat(auction.initialPrice.amount).isEqualTo(1000L)

            // 입찰 수 확인
            assertThat(auctionDetail.getBidCount()).isEqualTo(3L)

            // 최고 입찰가 확인
            assertThat(auctionDetail.getHighestBidAmount()).isEqualTo(2500L) // bid3의 금액
        }

        @Test
        @DisplayName("존재하지 않는 경매 ID로 조회시 null을 반환한다")
        fun findAuctionDetailByNonExistentIdTest() {
            // given
            val nonExistentId = 9999L // 존재하지 않는 ID

            // when
            val auctionDetail = auctionRepository.findAuctionDetailById(nonExistentId)

            // then
            assertThat(auctionDetail).isNull()
        }

        @Test
        @DisplayName("입찰이 없는 경매의 상세 정보를 조회한다")
        fun findAuctionDetailByIdWithNoBidTest() {
            // given
            entityManager.flush()
            entityManager.clear()

            // when
            val auctionDetail = auctionRepository.findAuctionDetailById(notStartedAuction.id!!)

            // then
            assertThat(auctionDetail).isNotNull

            // 경매 정보 확인
            val auction = auctionDetail!!.getAuction()
            assertThat(auction.id).isEqualTo(notStartedAuction.id)

            // 입찰이 없는 경우 입찰 수는 0
            assertThat(auctionDetail.getBidCount()).isEqualTo(0L)

            // 최고 입찰가는 null (또는 0)
            assertThat(auctionDetail.getHighestBidAmount()).isNull()
        }
    }
