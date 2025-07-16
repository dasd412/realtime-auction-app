package com.auctionapp.domain.event

import com.auctionapp.domain.vo.Money
import java.time.LocalDateTime

data class BidPlacedEvent(
    val auctionId: Long,
    val bidId: Long,
    val money: Money,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
