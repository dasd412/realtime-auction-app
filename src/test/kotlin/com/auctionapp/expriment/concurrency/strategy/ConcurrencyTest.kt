package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.*
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import io.mockk.InternalPlatformDsl.toStr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyTest(
    @Autowired
    private val auctionService: AuctionService,
) {
    private lateinit var user1: User
    private lateinit var auction: Auction

    @BeforeEach
    fun setup() {
        user1 = User.fixture(id = 1L)
        val product = Product.fixture(status = ProductStatus.AVAILABLE, user = user1, id = 1L)
        auction = Auction.fixture(product = product, user = user1, status = AuctionStatus.ACTIVE, id = 1L)
    }

    @Test
    @DisplayName("동시 입찰인 경우 최고 입찰가가 중요하다(synchronized 키워드)")
    fun synchronized_success() {
        success(SynchronizedStrategy(auctionService))
    }

    @Test
    @DisplayName("같은 금액으로 동시 입찰을 할 경우, 단 하나만 성공한다(synchronized 키워드)")
    fun synchronized_only_one_success() {
        onlyOneSuccess(SynchronizedStrategy(auctionService))
    }

    @Test
    @DisplayName("동시 입찰인 경우 최고 입찰가가 중요하다(tryLock)")
    fun tryLock_success() {
        success(TryLockStrategy(auctionService))
    }

    @Test
    @DisplayName("같은 금액으로 동시 입찰을 할 경우, 단 하나만 성공한다(tryLock)")
    fun tryLock_only_one_success() {
        onlyOneSuccess(TryLockStrategy(auctionService))
    }

    @Test
    @DisplayName("동시 입찰인 경우 최고 입찰가가 중요하다(semaphore)")
    fun semaphore_success() {
        success(SemaphoreStrategy(auctionService))
    }

    @Test
    @DisplayName("같은 금액으로 동시 입찰을 할 경우, 단 하나만 성공한다(semaphore)")
    fun semaphore_only_one_success() {
        onlyOneSuccess(SemaphoreStrategy(auctionService))
    }

    /*
        1. executors.submit으로 제출된 태스크에서 발생하는 예외는 해당 스레드 내에서만 발생하고 메인 테스트 스레드로 자동 전파되지 않습니다.
        2. assertThrows는 같은 스레드에서 즉시 발생하는 예외만 감지할 수 있습니다.
     */
    private fun success(strategy: ConcurrencyControlStrategy) {
        // given
        val threadCount = 1000
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<*>>()
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        val testStartTime = System.currentTimeMillis()

        // when
        for (i in 0 until threadCount) {
            val user = User.fixture(id = i + 10L)
            val future =
                executor.submit {
                    // 스레드 내부에서 try ~ catch를 활용하는 이유
                    // 각 스레드에서 발생한 예외는 해당 스레드에 격리되며, 다른 스레드나 메인 스레드로 전파되지 않기 떄문.

                    try {
                        strategy.placeBid(auction, user, Money(1000L * (i + 1)))
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        failCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            futures.add(future)
        }

        futures.forEach { it.get(1, TimeUnit.SECONDS) }
        latch.await()
        executor.shutdown()

        val testEndTime = System.currentTimeMillis()
        val totalTime = testEndTime - testStartTime

        // then
        assertThat(auction.getHighestBid()?.amount?.amount).isEqualTo(1000000L)
        println("==== ${strategy.toStr()} 전략 성능 측정 결과 ====")
        println("전체 실행 시간: ${totalTime}ms")
        println("입찰 성공 횟수 (입찰 순서에 따라 결과가 달라질 수 있음): ${successCount.get()}회")
        println("입찰 실패 횟수 (입찰 순서에 따라 결과가 달라질 수 있음): ${failCount.get()}회")
        println("======================================")
    }

    private fun onlyOneSuccess(strategy: ConcurrencyControlStrategy) {
        // given
        val threadCount = 1000
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<*>>()
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        for (i in 0 until threadCount) {
            val user = User.fixture(id = i + 10L)
            val future =
                executor.submit {
                    try {
                        strategy.placeBid(auction, user, Money(1000L))
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        failCount.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            futures.add(future)
        }

        futures.forEach { it.get(1, TimeUnit.SECONDS) }
        latch.await()
        executor.shutdown()

        // then
        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failCount.get()).isEqualTo(threadCount - 1)
        assertThat(auction.bids.size).isEqualTo(1)
        assertThat(auction.getHighestBid()?.amount?.amount).isEqualTo(1000L)
    }
}
