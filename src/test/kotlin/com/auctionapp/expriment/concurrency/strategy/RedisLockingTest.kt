package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.TestRedisConfig
import com.auctionapp.application.service.AuctionAppService
import com.auctionapp.domain.entity.*
import com.auctionapp.domain.vo.Email
import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.BidRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfig::class)
class RedisLockingTest(
    @Autowired private val auctionAppService: AuctionAppService,
    @Autowired private val auctionRepository: AuctionRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val bidRepository: BidRepository,
) {
    @AfterEach
    fun setup() {
        bidRepository.deleteAll()
        auctionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("Redis 분산 락을 통해 입찰이 성공적으로 처리된다")
    fun shouldPlaceBidWithRedisLock() {
        // given
        val user = userRepository.save(User.fixture())
        val product = productRepository.save(Product.fixture(user = user, status = ProductStatus.AVAILABLE))
        val auction =
            auctionRepository.save(
                Auction.fixture(
                    initialPrice = Money(1000),
                    product = product,
                    user = user,
                    status = AuctionStatus.ACTIVE,
                ),
            )
        val bidAmount = 2000L
        val bidder = userRepository.save(User.fixture(email = Email("test@example.com")))
        val authentication =
            UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                listOf(SimpleGrantedAuthority("ROLE_CUSTOMER")),
            )
        SecurityContextHolder.getContext().authentication = authentication

        // when
        val bidId = auctionAppService.placeBidWithRedisLock(auction.id!!, bidAmount)

        // then
        val savedBid = bidRepository.findById(bidId).orElse(null)
        assertThat(savedBid).isNotNull
        assertThat(savedBid.amount).isEqualTo(Money(bidAmount))
        assertThat(savedBid.user.id).isEqualTo(bidder.id)
    }

    // @RepeatedTest(70)
    @Test
    @DisplayName("Redis 분산 락 - 동시 입찰인 경우 최고 입찰가가 선택된다")
    fun concurrentBiddingWithRedisLock_success() {
        // given
        val user = userRepository.save(User.fixture())
        val product = productRepository.save(Product.fixture(user = user))
        val auction =
            auctionRepository.save(
                Auction.fixture(
                    initialPrice = Money(1000),
                    product = product,
                    user = user,
                    status = AuctionStatus.ACTIVE,
                ),
            )

        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(threadCount)
        val countDownLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val testStartTime = System.currentTimeMillis()
        val bidResults = Collections.synchronizedList(mutableListOf<Long>())

        // when
        for (i in 1..threadCount) {
            val bidAmount = 1000L + (i * 100)
            userRepository.save(User.fixture(id = i + 1000L, email = Email("test_${i + 1000L}@example.com")))

            executorService.submit {
                try {
                    val authentication =
                        UsernamePasswordAuthenticationToken(
                            "test_${i + 1000L}@example.com",
                            null,
                            listOf(SimpleGrantedAuthority("ROLE_CUSTOMER")),
                        )
                    SecurityContextHolder.getContext().authentication = authentication
                    auctionAppService.placeBidWithRedisLock(
                        auction.id!!,
                        bidAmount,
                    )
                    successCount.incrementAndGet()
                    bidResults.add(bidAmount)
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("실패: ${e.javaClass.simpleName} - ${e.message}")
                } finally {
                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await(1000, TimeUnit.MILLISECONDS)
        executorService.shutdown()

        // then
        // 최고가 입찰 확인
        println("Redis 분산 락 테스트 결과:")

        println("성공 횟수: ${successCount.get()}")
        println("실패 횟수: ${failCount.get()}")
        println("총 소요 시간: ${System.currentTimeMillis() - testStartTime}ms")
        println("처리 순서: $bidResults")
        val highestBidByAmount = bidRepository.findTopByAuctionOrderByAmountDesc(auction)
        assertThat(highestBidByAmount.amount.amount).isEqualTo(1000L + (threadCount * 100))
        println("최고 입찰액: ${highestBidByAmount.amount.amount}")
    }

    // @RepeatedTest(70)
    @Test
    @DisplayName("Redis 분산 락 - 같은 금액으로 동시 입찰 시 하나만 성공한다")
    fun concurrentBiddingWithRedisLock_onlyOneSuccess() {
        // given
        val user = userRepository.save(User.fixture())
        val product = productRepository.save(Product.fixture(user = user))
        val auction =
            auctionRepository.save(
                Auction.fixture(
                    initialPrice = Money(1000),
                    product = product,
                    user = user,
                    status = AuctionStatus.ACTIVE,
                ),
            )

        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(threadCount)
        val countDownLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val testStartTime = System.currentTimeMillis()
        val bidResults = Collections.synchronizedList(mutableListOf<Long>())

        // when
        for (i in 1..threadCount) {
            val bidAmount = 1000L
            userRepository.save(User.fixture(id = i + 1000L, email = Email("test_${i + 1100L}@example.com")))
            executorService.submit {
                try {
                    val authentication =
                        UsernamePasswordAuthenticationToken(
                            "test_${i + 1100L}@example.com",
                            null,
                            listOf(SimpleGrantedAuthority("ROLE_CUSTOMER")),
                        )
                    SecurityContextHolder.getContext().authentication = authentication
                    auctionAppService.placeBidWithRedisLock(
                        auction.id!!,
                        bidAmount,
                    )
                    successCount.incrementAndGet()
                    bidResults.add(bidAmount)
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("실패: ${e.javaClass.simpleName} - ${e.message}")
                } finally {
                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await(1000, TimeUnit.MILLISECONDS)
        executorService.shutdown()

        // then
        println("Redis 분산 락 테스트 결과:")
        println("성공 횟수: ${successCount.get()}")
        println("실패 횟수: ${failCount.get()}")
        println("총 소요 시간: ${System.currentTimeMillis() - testStartTime}ms")
        println("처리 순서: $bidResults")
        val highestBidByAmount = bidRepository.findTopByAuctionOrderByAmountDesc(auction)
        assertThat(highestBidByAmount.amount.amount).isEqualTo(1000L)
        println("최고 입찰액: ${highestBidByAmount.amount.amount}")
        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failCount.get()).isEqualTo(threadCount - 1)
    }
}
