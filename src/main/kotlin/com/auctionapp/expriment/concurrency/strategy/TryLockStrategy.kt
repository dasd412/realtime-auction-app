package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class TryLockStrategy(
    private val auctionService: AuctionService,
) : ConcurrencyControlStrategy {
    // 경매 ID별로 별도의 락을 사용해서 서로 다른 경매는 독립적으로 처리하고, 같은 경매인 경우 동기화
    private val locks = ConcurrentHashMap<Long, ReentrantLock>()

    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        val lock = locks.computeIfAbsent(auction.id!!) { ReentrantLock() }

        val acquired = lock.tryLock(100, TimeUnit.MILLISECONDS)

        if (!acquired) {
            throw BidConflictException()
        }

        try {
            return auctionService.placeBid(amount, user, auction)
        } finally {
            lock.unlock()
        }
    }
}
