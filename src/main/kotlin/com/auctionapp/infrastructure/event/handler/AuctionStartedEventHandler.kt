package com.auctionapp.infrastructure.event.handler

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.AuctionStartedEvent
import org.springframework.stereotype.Component

@Component
class AuctionStartedEventHandler : DomainEventHandler<AuctionStartedEvent> {
    override fun handle(event: AuctionStartedEvent) {
        TODO("Not yet implemented")
    }
}
