package com.auctionapp.domain.event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DomainEventPublisher {
    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    fun publish(event:DomainEvent){
        applicationEventPublisher.publishEvent(event)
    }
}