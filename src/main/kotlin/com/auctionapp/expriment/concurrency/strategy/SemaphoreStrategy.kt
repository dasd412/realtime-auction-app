package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class SemaphoreStrategy(
    private val auctionService: AuctionService,
) : ConcurrencyControlStrategy {
    private val locks = ConcurrentHashMap<Long, Semaphore>()

    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        val semaphore = locks.computeIfAbsent(auction.id!!) { Semaphore(1) }

        val acquired = semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)

        if (!acquired) {
            throw BidConflictException()
        }

        try {
            return auctionService.placeBid(amount, user, auction)
        } finally {
            semaphore.release()
        }
    }
}
