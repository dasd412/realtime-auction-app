package com.auctionapp.infrastructure.event.handler

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.AuctionEndedEvent
import com.auctionapp.infrastructure.websocket.WebSocketService
import org.springframework.stereotype.Component

@Component
class AuctionEndedEventHandler(
    private val webSocketService: WebSocketService,
) : DomainEventHandler<AuctionEndedEvent> {
    override fun handle(event: AuctionEndedEvent) {
        webSocketService.sendAuctionEnded(event.auctionId, event.winnerId)
    }
}
