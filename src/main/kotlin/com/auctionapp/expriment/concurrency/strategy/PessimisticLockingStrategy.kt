package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.persistence.AuctionRepository

// 비관적 락 전략 구현
// DB 수준에서의 락이므로 분산 서버 환경에서도 동시성 제어가 가능
class PessimisticLockingStrategy(
    private val auctionService: AuctionService,
    private val auctionRepository: AuctionRepository,
) : ConcurrencyControlStrategy {
    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        // auction 파라미터는 일반 쿼리로 조회된 객체로, 비관적 락이 적용되어 있지 않음
        // 비관적 락은 데이터베이스 수준 락을 획득하기 위해 특별한 쿼리인 SELECT FOR UPDATE를 사용해야 함.
        // 부모 엔티티인 경매에 비관적 락을 걸면 같은 경매에 대한 다른 입찰 시도는 락에 의해 대기 됨.
        val lockedAuction = auctionRepository.findByIdWithPessimisticLock(auction.id!!) ?: throw NotFoundAuctionException()

        return auctionService.placeBid(amount, user, lockedAuction)
    }
}
