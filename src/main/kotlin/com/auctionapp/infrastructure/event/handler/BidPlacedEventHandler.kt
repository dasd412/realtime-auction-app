package com.auctionapp.infrastructure.event.handler

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.BidPlacedEvent
import com.auctionapp.infrastructure.websocket.WebSocketService
import org.springframework.stereotype.Component

@Component
class BidPlacedEventHandler(
    private val webSocketService: WebSocketService,
) : DomainEventHandler<BidPlacedEvent> {
    override fun handle(event: BidPlacedEvent) {
        webSocketService.sendBidUpdate(event.auctionId, event.bidId, event.money)
    }
}
