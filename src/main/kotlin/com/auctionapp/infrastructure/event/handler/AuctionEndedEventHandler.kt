package com.auctionapp.infrastructure.event.handler

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.AuctionEndedEvent
import org.springframework.stereotype.Component

@Component
class AuctionEndedEventHandler : DomainEventHandler<AuctionEndedEvent> {
    override fun handle(event: AuctionEndedEvent) {
        TODO("Not yet implemented")
    }
}
