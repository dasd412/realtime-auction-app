package com.auctionapp.com.auctionapp.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 클라이언트에게 메시지를 보낼 때 사용할 prefix.
        // 이 prefix로 시작하는 목적지로 메시지가 전송되면, 메시지 브로커가 해당 목적지를 구독하고 있는 모든 클라이언트에게 메시지를 전달함. 클라이언트가 아무도 없으면 메시지는 그냥 버려짐.
        registry.enableSimpleBroker("/topic", "/queue")
        // 클라이언트가 서버에게 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 웹 소켓 연겷을 위한 엔드포인트 설정
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }
}
