package com.auctionapp.domain.event

import com.auctionapp.domain.vo.Money
import java.time.LocalDateTime

interface DomainEvent {
    val occurredOn: LocalDateTime
}

data class AuctionStartedEvent(
    val auctionId: Long,
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : DomainEvent

data class AuctionEndedEvent(
    val auctionId: Long,
    val winnerId: Long?,
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : DomainEvent

data class BidPlacedEvent(
    val auctionId: Long,
    val bidId: Long,
    val money: Money,
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : DomainEvent