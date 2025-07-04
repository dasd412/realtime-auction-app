package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import org.springframework.orm.ObjectOptimisticLockingFailureException

// 낙관적 락 구현 (@Version 필드 활용)
// DB 수준에서의 락이므로 분산 서버 환경에서도 동시성 제어가 가능
/*
엔티티에 @Version 필드가 있더라도 비관적 락을 구현하는 데 문제가 없습니다. 두 기능은 독립적으로 작동합니다.
    1. @Version 필드
        * 낙관적 락을 위한 버전 관리용
        * 모든 엔티티 수정 시 자동으로 증가함
        * 비관적 락을 사용하든 안 하든 항상 작동
    2. 비관적 락
        * 데이터베이스 수준의 락(SELECT FOR UPDATE 등)
        * JPA의 LockModeType 사용하여 적용
        * 버전 필드 존재 여부와 관계없이 작동
 */
class OptimisticLockingStrategy(
    private val auctionService: AuctionService,
) : ConcurrencyControlStrategy {
    override fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid {
        try {
            return auctionService.placeBid(amount, user, auction)
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw BidConflictException()
        }
    }
}
