package com.auctionapp.com.auctionapp.infrastructure.config

import com.auctionapp.infrastructure.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

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

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(
            object : ChannelInterceptor {
                override fun preSend(
                    message: Message<*>,
                    channel: MessageChannel,
                ): Message<*>? {
                    val accessor = StompHeaderAccessor.wrap(message)

                    if (StompCommand.CONNECT == accessor.command) {
                        val authorization = accessor.getFirstNativeHeader("Authorization")

                        if (authorization != null && authorization.startsWith("Bearer ")) {
                            val token = authorization.substring(7)
                            val authentication = jwtTokenProvider.getAuthentication(token)
                            SecurityContextHolder.getContext().authentication = authentication
                            accessor.user = authentication
                        }
                    }
                    return message
                }
            },
        )
    }
}
