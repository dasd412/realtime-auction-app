package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

// synchronized, tryLock, semaphore는 분산 서버에선 동시성 제어가 안되고
// 비관적 락은 분산 DB에서 동시성 제어가 안되므로 레디스 분산 락을 활용함.
class RedisDistributeLockStrategy(
    private val auctionService: AuctionService,
    private val redissonClient: RedissonClient,
) : ConcurrencyControlStrategy {
    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        if (auction.id == null) {
            throw NotFoundAuctionException()
        }

        val lockKey = "auction:lock:${auction.id}"
        val lock = redissonClient.getLock(lockKey)

        try {
            // 1초 타임 아웃으로 락 획득 시도
            val isLockAcquired = lock.tryLock(1, 10, TimeUnit.SECONDS)

            if (!isLockAcquired) {
                throw BidConflictException()
            }

            return auctionService.placeBid(amount, user, auction)
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}
