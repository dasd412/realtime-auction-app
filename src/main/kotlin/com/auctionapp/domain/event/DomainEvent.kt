package com.auctionapp.domain.event

import java.time.LocalDateTime

interface DomainEvent {
    val occurredOn: LocalDateTime
}
