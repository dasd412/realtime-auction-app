package com.auctionapp.infrastructure.event.handler

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.AuctionStartedEvent
import com.auctionapp.infrastructure.websocket.WebSocketService
import org.springframework.stereotype.Component

@Component
class AuctionStartedEventHandler(
    private val webSocketService: WebSocketService,
) : DomainEventHandler<AuctionStartedEvent> {
    override fun handle(event: AuctionStartedEvent) {
        webSocketService.sendAuctionStarted(event.auctionId)
    }
}
