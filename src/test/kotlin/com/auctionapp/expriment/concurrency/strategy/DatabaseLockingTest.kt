package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.*
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.BidRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import jakarta.annotation.PostConstruct
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class DatabaseLockingTest {
    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var bidRepository: BidRepository

    @Autowired
    private lateinit var auctionService: AuctionService

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @PostConstruct
    fun setupTransactionTemplate() {
        transactionTemplate = TransactionTemplate(transactionManager)
    }

    @AfterEach
    fun clean() {
        bidRepository.deleteAll()
        auctionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    // @RepeatedTest(50)
    @Test
    @DisplayName("비관적 락 테스트 - 동시 입찰인 경우 최고 입찰가가 선택된다")
    fun pessimisticLocking_success() {
        // given
        val user = userRepository.save(User.fixture())
        val product = productRepository.save(Product.fixture(user = user, status = ProductStatus.AVAILABLE))
        val auction =
            auctionRepository.save(
                Auction.fixture(
                    product = product,
                    user = user,
                    status = AuctionStatus.ACTIVE,
                ),
            )
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<*>>()

        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val processedBids = Collections.synchronizedList(mutableListOf<Pair<Long, Long>>()) // userId, amount

        val strategy = PessimisticLockingStrategy(auctionService, auctionRepository)
        val testStartTime = System.currentTimeMillis()

        // when
        for (i in 0 until threadCount) {
            val userId = i + 1000L
            val user = userRepository.save(User.fixture(id = userId))
            val bidAmount = 1000L * (i + 1) // 서로 다른 금액으로 입찰

            val future =
                executor.submit {
                    try {
                        transactionTemplate.execute {
                            val freshAuction = auctionRepository.findById(auction.id!!).get()
                            val bid = strategy.placeBid(freshAuction, user, Money(bidAmount))
                            processedBids.add(Pair(userId, bidAmount))
                            successCount.incrementAndGet()
                            return@execute bid
                        }
                    } catch (e: Exception) {
                        failCount.incrementAndGet()
                        println("실패: ${e.javaClass.simpleName} - ${e.message}")
                    } finally {
                        latch.countDown()
                    }
                }
            futures.add(future)
        }

        // 결과 확인
        latch.await(20, TimeUnit.SECONDS)
        val testEndTime = System.currentTimeMillis()

        // then
        // 최고가 입찰 확인
        val highestBidByAmount = bidRepository.findTopByAuctionOrderByAmountDesc(auction)
        assertThat(highestBidByAmount.amount.amount).isEqualTo(10000L)
        println("최고 입찰액: ${highestBidByAmount.amount.amount}")
        println("비관적 락 테스트 결과 (최고가 선택):")
        println("총 실행 시간: ${testEndTime - testStartTime}ms")
        println("성공: ${successCount.get()}, 실패: ${failCount.get()}")
        println("처리 순서: $processedBids")
    }

    // @RepeatedTest(50)
    @Test
    @DisplayName("비관적 락 테스트 - 같은 금액으로 동시 입찰 시 하나만 성공한다")
    fun pessimisticLocking_onlyOneSuccess() {
        // given
        val user = userRepository.save(User.fixture())
        val product = productRepository.save(Product.fixture(user = user, status = ProductStatus.AVAILABLE))
        val auction =
            auctionRepository.save(
                Auction.fixture(
                    product = product,
                    user = user,
                    status = AuctionStatus.ACTIVE,
                ),
            )
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<*>>()

        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        val strategy = PessimisticLockingStrategy(auctionService, auctionRepository)
        val testStartTime = System.currentTimeMillis()

        // when
        for (i in 0 until threadCount) {
            val userId = i + 1200L
            val user = userRepository.save(User.fixture(id = userId))
            val bidAmount = 1000L // 모두 동일한 금액으로 입찰

            val future =
                executor.submit {
                    try {
                        transactionTemplate.execute {
                            val freshAuction = auctionRepository.findById(auction.id!!).get()
                            strategy.placeBid(freshAuction, user, Money(bidAmount))
                            successCount.incrementAndGet()
                            return@execute null
                        }
                    } catch (e: Exception) {
                        failCount.incrementAndGet()
                        println("실패: ${e.javaClass.simpleName} - ${e.message}")
                    } finally {
                        latch.countDown()
                    }
                }
            futures.add(future)
        }

        // 결과 확인
        latch.await(20, TimeUnit.SECONDS)
        val testEndTime = System.currentTimeMillis()

        // then
        // 동일 금액 입찰은 첫 번째만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failCount.get()).isEqualTo(threadCount - 1)

        val highestBidByAmount = bidRepository.findTopByAuctionOrderByAmountDesc(auction)
        assertThat(highestBidByAmount.amount.amount).isEqualTo(1000L)
        println("최고 입찰액: ${highestBidByAmount.amount.amount}")
        println("비관적 락 테스트 결과 (동일 금액):")
        println("총 실행 시간: ${testEndTime - testStartTime}ms")
        println("성공: ${successCount.get()}, 실패: ${failCount.get()}")
    }
}
