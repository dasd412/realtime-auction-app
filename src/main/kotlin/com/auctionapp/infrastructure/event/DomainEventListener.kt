package com.auctionapp.infrastructure.event

import com.auctionapp.com.auctionapp.domain.event.handler.DomainEventHandler
import com.auctionapp.domain.event.AuctionEndedEvent
import com.auctionapp.domain.event.AuctionStartedEvent
import com.auctionapp.domain.event.BidPlacedEvent
import com.auctionapp.domain.event.DomainEvent
import com.auctionapp.infrastructure.event.exception.UnsupportedEventException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DomainEventListener {
    @Autowired
    private lateinit var auctionStartedEventHandler: DomainEventHandler<AuctionStartedEvent>

    @Autowired
    private lateinit var auctionEndedEventHandler: DomainEventHandler<AuctionEndedEvent>

    @Autowired
    private lateinit var bidPlacedEventHandler: DomainEventHandler<BidPlacedEvent>

    @EventListener
    fun handle(event: DomainEvent) {
        when (event) {
            is AuctionStartedEvent -> auctionStartedEventHandler.handle(event)
            is AuctionEndedEvent -> auctionEndedEventHandler.handle(event)
            is BidPlacedEvent -> bidPlacedEventHandler.handle(event)
            else -> throw UnsupportedEventException("지원하지 않는 도메인 이벤트입니다 : ${event.javaClass.name}")
        }
    }
}
