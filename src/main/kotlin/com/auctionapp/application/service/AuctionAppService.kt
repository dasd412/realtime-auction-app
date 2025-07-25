package com.auctionapp.application.service

import com.auctionapp.application.constant.DEFAULT_AUCTION_PAGE_SIZE
import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.NotFoundUserException
import com.auctionapp.application.exception.UnauthorizedException
import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import com.auctionapp.com.auctionapp.utils.SecurityUtil
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Email
import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.persistence.*
import com.auctionapp.infrastructure.scheduler.AuctionSchedulerService
import org.redisson.api.RedissonClient
import org.redisson.client.RedisException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

enum class AuctionSortType {
    NONE,
    TIME_ASC, // 시간순
    POPULARITY, // 인기순(입찰 수)
}

const val REDIS_LOCK_WAIT_TIME_SECOND = 3L // 락 획득 시도 시간
const val REDIS_LOCK_LEASE_TIME_SECOND = 5L // 락 획득 성공 후 보유 시간
const val TRANSACTION_TIMEOUT_SECOND = 3 // 트랜잭션 타임아웃, 트랜잭션 타임 아웃 <  락 획득 성공 후 보유 시간여야 함.

@Service
class AuctionAppService(
    private val auctionService: AuctionService,
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val strategyRegistry: ConcurrencyControlStrategyRegistry,
    private val redissonClient: RedissonClient,
    private val transactionManager: PlatformTransactionManager,
    private val auctionSchedulerService: AuctionSchedulerService,
) {
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    fun registerAuction(
        productId: Long,
        initialPrice: Long,
        minimumBidUnit: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Long {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()

        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()

        val product = productRepository.findByIdOrNull(productId) ?: throw NotFoundProductException()

        val auction =
            Auction(
                initialPrice = Money(initialPrice),
                minimumBidUnit = Money(minimumBidUnit),
                user = user,
                product = product,
                startTime = startTime,
                endTime = endTime,
            )

        auctionService.registerAuction(auction, user, product)

        val saved = auctionRepository.save(auction)

        auctionSchedulerService.scheduleAuctionJobs(saved)

        return saved.id!!
    }

    @Transactional(readOnly = true)
    fun getAuctionList(
        status: AuctionStatus,
        sortType: AuctionSortType,
        pageNumber: Int,
    ): Page<Auction> {
        return when (sortType) {
            AuctionSortType.NONE ->
                auctionRepository.findByStatus(
                    status,
                    PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
                )

            AuctionSortType.TIME_ASC ->
                auctionRepository.findByStatusOrderByStartTimeAsc(
                    status,
                    PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
                )

            AuctionSortType.POPULARITY ->
                auctionRepository.findByStatusOrderByBidsCountDesc(
                    status,
                    PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
                )
        }
    }

    @Transactional(readOnly = true)
    fun getAuctionDetail(auctionId: Long): AuctionDetail {
        return auctionRepository.findAuctionDetailById(auctionId) ?: throw NotFoundAuctionException()
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    fun cancelAuction(auctionId: Long) {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()

        auctionService.cancelAuction(auction, user)

        auctionSchedulerService.unScheduleAuctionJobs(auctionId)
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    fun placeBid(
        auctionId: Long,
        amount: Long,
    ): Long {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
        val money = Money(amount)

        val strategy = strategyRegistry.getCurrentStrategy()
        val bid = strategy.placeBid(auction, user, money)

        val savedBid = bidRepository.save(bid)

        auction.addBidEvent(savedBid)

        return savedBid.id!!
    }

    // Redis 분산 락을 위한 새로운 메서드
    // synchronized, tryLock, semaphore는 분산 서버에선 동시성 제어가 안되고
    // 비관적 락은 분산 DB에서 동시성 제어가 안되므로 레디스 분산 락을 활용함.
    // 분산락과 트랜잭션을 함께 사용할 경우, 락 획득 → 트랜잭션 시작 → 비즈니스 로직 → 트랜잭션 커밋 또는 롤백 → 락 해제 순서로 진행되야 함
    @PreAuthorize("hasRole('CUSTOMER')")
    fun placeBidWithRedisLock(
        auctionId: Long,
        amount: Long,
    ): Long {
        val lockKey = "auction:Lock:$auctionId"
        val lock = redissonClient.getLock(lockKey)
        val transactionTemplate = TransactionTemplate(transactionManager)

        try {
            val isLockAcquired = lock.tryLock(REDIS_LOCK_WAIT_TIME_SECOND, REDIS_LOCK_LEASE_TIME_SECOND, TimeUnit.SECONDS)

            if (!isLockAcquired) {
                throw RedisException("분산 락 획득에 실패했습니다")
            }

            // 트랜잭션 내에서 비즈니스 로직 실행
            // @Transactional 어노테이션 대신 TransactionTemplate을 사용하는 이유:
            // 1. 트랜잭션과 락의 생명주기 제어:
            // - @Transactional은 메서드 시작 시 트랜잭션이 시작되고, 메서드 종료 시 커밋됩니다.
            // - TransactionTemplate은 락 획득 후 명시적으로 트랜잭션을 시작하고 종료할 수 있습니다.
            // 2. 프록시 기반 AOP의 한계:
            // - @Transactional은 Spring AOP 프록시를 통해 작동하며 같은 클래스 내 호출 시 적용되지 않을 수 있습니다.
            // - TransactionTemplate은 이러한 제약 없이 직접 트랜잭션을 관리합니다.
            // 3. 락과 트랜잭션 순서 보장:
            // - 분산 락에서는 '락 획득 → 트랜잭션 시작 → 비즈니스 로직 → 트랜잭션 커밋 → 락 해제' 순서가 중요합니다.
            // - @Transactional에서는 트랜잭션이 먼저 시작되어 락 해제 시점이 잘못될 수 있습니다.
            // 4. 예외 처리와 락 해제 보장:
            // - TransactionTemplate에서는 예외 발생으로 롤백되더라도 finally 블록에서 락 해제가 보장됩니다.
            // - @Transactional에서는 예외 시 트랜잭션 롤백과 락 해제 순서가 명확하지 않을 수 있습니다.
            return transactionTemplate.execute {
                val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
                val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
                val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
                val money = Money(amount)

                val savedBid = placeBidInTransaction(money, user, auction)
                auction.addBidEvent(savedBid)

                savedBid.id!!
            }!!
        } finally {
            // 트랜잭션이 커밋 또는 롤백이 끝나면 레디스 분산 락을 해제
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    // 반드시 트랜잭션이 끝나는게 확정되고 나서 레디스 분산 락을 해제해야 하므로
    // 트랜잭션의 타임 아웃을 레디스 분산 락 타임아웃보다 더 짧게 함.
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = TRANSACTION_TIMEOUT_SECOND)
    private fun placeBidInTransaction(
        money: Money,
        user: User,
        auction: Auction,
    ): Bid {
        val bid = auctionService.placeBid(money, user, auction)
        val savedBid = bidRepository.save(bid)

        return savedBid
    }

    @Transactional(readOnly = true)
    fun getBidsOfAuction(
        auctionId: Long,
        pageNumber: Int,
    ): Page<Bid> {
        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
        return bidRepository.findByAuctionOrderByCreatedAtDesc(
            auction,
            PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
        )
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(readOnly = true)
    fun getBidsOfUser(pageNumber: Int): Page<Bid> {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
        return bidRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(readOnly = true)
    fun getAuctionsOfAuctionOwner(pageNumber: Int): Page<Auction> {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
        return auctionRepository.findByUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(readOnly = true)
    fun getAuctionsOfBidder(pageNumber: Int): Page<Auction> {
        val username = SecurityUtil.getCurrentUsername() ?: throw UnauthorizedException()
        val user = userRepository.findByEmail(Email(username)) ?: throw NotFoundUserException()
        return auctionRepository.findByBidUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
    }
}
