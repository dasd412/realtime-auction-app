package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money

// todo synchronized, tryLock, semaphore는 분산 서버에선 동시성 제어가 안되므로 레디스 분산락을 활용함.
class RedisDistributeLockStrategy (
    private val auctionService: AuctionService,
): ConcurrencyControlStrategy {
    override fun placeBid(auction: Auction, user: User, amount: Money): Bid {
        TODO("Not yet implemented")
    }
}
