package com.auctionapp.infrastructure.event.handler

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.BidPlacedEvent
import org.springframework.stereotype.Component

@Component
class BidPlacedEventHandler : DomainEventHandler<BidPlacedEvent> {
    override fun handle(event: BidPlacedEvent) {
        TODO("Not yet implemented")
    }
}
