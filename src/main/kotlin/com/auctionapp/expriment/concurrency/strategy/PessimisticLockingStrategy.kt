package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money

// 비관적 락 전략 구현
// DB 수준에서의 락이므로 분산 서버 환경에서도 동시성 제어가 가능
class PessimisticLockingStrategy(
    private val auctionService: AuctionService,
) : ConcurrencyControlStrategy {
    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        TODO("Not yet implemented")
    }
}
