package com.auctionapp.com.auctionapp.domain.event.handler

import com.auctionapp.domain.event.DomainEvent

interface DomainEventHandler<T : DomainEvent> {
    fun handle(event: T)
}
