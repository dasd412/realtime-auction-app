package com.auctionapp.infrastructure.config

import com.auctionapp.domain.event.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainEventConfig {

    @Bean
    fun auctionStartEventHandler(): DomainEventHandler<AuctionStartedEvent> {
        return AuctionStartEventHandler()
    }

    @Bean
    fun auctionEndEventHandler(): DomainEventHandler<AuctionEndedEvent> {
        return AuctionEndEventHandler()
    }

    @Bean
    fun bidPlacedEventHandler(): DomainEventHandler<BidPlacedEvent> {
        return BidPlacedEventHandler()
    }
}
