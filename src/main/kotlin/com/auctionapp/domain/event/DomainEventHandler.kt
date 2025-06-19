package com.auctionapp.com.auctionapp.domain.event

import com.auctionapp.domain.event.AuctionEndedEvent
import com.auctionapp.domain.event.AuctionStartedEvent
import com.auctionapp.domain.event.BidPlacedEvent
import com.auctionapp.domain.event.DomainEvent

//todo 이벤트 핸들링에 대한 구체적인 구현은 웹 소켓 또는 스케쥴러 구현할 때 채운다.
interface DomainEventHandler<T : DomainEvent> {
    fun handle(event: T)
}

class AuctionStartEventHandler : DomainEventHandler<AuctionStartedEvent> {
    override fun handle(event: AuctionStartedEvent) {
        TODO("Not yet implemented")
    }
}

class AuctionEndEventHandler : DomainEventHandler<AuctionEndedEvent> {
    override fun handle(event: AuctionEndedEvent) {
        TODO("Not yet implemented")
    }
}

class BidPlacedEventHandler : DomainEventHandler<BidPlacedEvent> {
    override fun handle(event: BidPlacedEvent) {
        TODO("Not yet implemented")
    }
}