package com.auctionapp.domain.event

import java.time.LocalDateTime

data class AuctionEndedEvent(
    val auctionId: Long,
    val winnerId: Long?,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
