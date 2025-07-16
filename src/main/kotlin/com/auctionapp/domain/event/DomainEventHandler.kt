package com.auctionapp.domain.event

import org.springframework.stereotype.Component

// todo 이벤트 핸들링에 대한 구체적인 구현은 웹 소켓 또는 스케쥴러 구현할 때 채운다.
interface DomainEventHandler<T : DomainEvent> {
    fun handle(event: T)
}

@Component
class AuctionStartEventHandler : DomainEventHandler<AuctionStartedEvent> {
    override fun handle(event: AuctionStartedEvent) {
        TODO("Not yet implemented")
    }
}

@Component
class AuctionEndEventHandler : DomainEventHandler<AuctionEndedEvent> {
    override fun handle(event: AuctionEndedEvent) {
        TODO("Not yet implemented")
    }
}

@Component
class BidPlacedEventHandler : DomainEventHandler<BidPlacedEvent> {
    override fun handle(event: BidPlacedEvent) {
        TODO("Not yet implemented")
    }
}
