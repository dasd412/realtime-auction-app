package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money

// 서버 인스턴스 수준에서의 락이므로 분산 서버 환경에서도 동시성 제어가 불가능
class SynchronizedStrategy(
    private val auctionService: AuctionService,
) : ConcurrencyControlStrategy {
    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        // 각 경매 별로 독립적인 락을 사용하여, 서로 다른 경매의 입찰은 병렬 처리되면서 같은 경매에 대한 입찰만 순차 처리된다.
        synchronized(auction) {
            return auctionService.placeBid(amount, user, auction)
        }
    }
}
