package com.auctionapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/*
경매는 동시에 여러 사용자가 입찰하는 경쟁 상황이 발생하는 핵심 엔티티.

    경매 시스템에서의 락 전략
    경매 시스템과 같이 동시에 많은 사용자가 같은 리소스(경매 항목)에 접근하여 입찰하는 상황에서는 락 전략이 매우 중요합니다.

    [낙관적 락(Optimistic Lock)과 비관적 락(Pessimistic Lock) 비교]

    1. 낙관적 락(Optimistic Lock)
        * [장점]
        * 높은 동시성 지원
        * 데드락 발생 가능성이 낮음
        * 읽기 작업에 락을 걸지 않아 성능이 좋음
        * [단점]
        * 충돌 발생 시 재시도 로직이 필요함
        * 높은 경쟁 상황에서는 많은 트랜잭션이 실패할 수 있음
        * 사용자 경험 저하 가능성

    2. 비관적 락(Pessimistic Lock)
        * [장점]
        * 데이터 일관성 보장이 강함
        * 충돌 상황에서 더 안정적
        * 재시도 로직이 필요 없음
        * [단점]
        * 동시성 저하
        * 데드락 발생 가능성
        * 성능 오버헤드

[경매 시스템에 적합한 전략]
    경매 시스템의 경우, 특히 인기 있는 상품에 대한 경매에서는 비관적 락(Pessimistic Lock)이 더 적합할 수 있습니다.
    [이유]
    1. 경매는 동시에 여러 사용자가 입찰하는 고경쟁 상황이 발생함
    2. 입찰 처리는 정확성이 매우 중요하며, 실패한 트랜잭션의 재시도는 사용자 경험을 크게 저하시킬 수 있음
    3. 입찰은 짧은 시간 내에 처리되므로 락으로 인한 지연이 크지 않음

학습 목적이라면 두 가지 전략을 모두 구현해보고 성능과 동작을 비교해보는 것이 매우 유익합니다. 이를 통해
    1. 각 전략의 장단점을 실제로 경험할 수 있음
    2. 다양한 부하 상황에서의 동작 차이를 확인할 수 있음
    3. 사용자 경험과 시스템 안정성 간의 트레이드오프를 이해할 수 있음
특히 경매 시스템에서는 낙관적 락의 경우 입찰 충돌 시 사용자에게 "다른 사용자가 더 높은 금액으로 입찰했습니다. 다시 시도해주세요."와 같은 메시지를 보여줄 수 있고,
비관적 락의 경우 입찰 처리 중 다른 입찰을 대기시켜 순차적으로 처리할 수 있습니다.
 */
@Entity
class Auction(
    val initialPrice: Long,
    val minimumBidUnit: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    @Enumerated(EnumType.STRING)
    var status: AuctionStatus,
    @ManyToOne
    val user: User,
    @OneToOne
    val product: Product,
    @OneToMany(mappedBy = "auction", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bids: MutableList<Bid> = mutableListOf(),
    @Version
    var version: Long = 0, //학습용 낙관적 락
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        if (initialPrice < 1000) {
            throw IllegalArgumentException("초기 가격은 1000원 이상이어야 합니다")
        }
        if(minimumBidUnit<0){
            throw IllegalArgumentException("최소 입찰 단위는 음수가 될 수 없습니다")
        }
        if (endTime.isBefore(startTime.plusHours(1))) {
            throw IllegalArgumentException("종료 시각은 시작 시간보다 최소 1시간 이후여야 합니다")
        }
    }

    companion object {
        fun fixture(
            initialPrice: Long = 1000L,
            minimumBidUnit: Long = 100L,
            startTime: LocalDateTime = LocalDateTime.now().minusHours(1),
            endTime: LocalDateTime = LocalDateTime.now(),
            status: AuctionStatus = AuctionStatus.NOT_STARTED,
            user: User,
            product: Product,
        ): Auction {
            return Auction(
                initialPrice = initialPrice,
                minimumBidUnit = minimumBidUnit,
                startTime = startTime,
                endTime = endTime,
                status = status,
                user = user,
                product = product
            )
        }
    }
}