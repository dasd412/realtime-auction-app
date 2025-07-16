package com.auctionapp.domain.event

import java.time.LocalDateTime

data class AuctionStartedEvent(
    val auctionId: Long,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
