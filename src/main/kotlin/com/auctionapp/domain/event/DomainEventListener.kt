package com.auctionapp.domain.event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.context.event.EventListener

@Component
class DomainEventListener{

    @Autowired
    private lateinit var handlers: Map<Class<out DomainEvent>, DomainEventHandler<DomainEvent>>
    
    @EventListener
    fun handle(event: DomainEvent){
        handlers[event.javaClass]?.handle(event)
    }
}