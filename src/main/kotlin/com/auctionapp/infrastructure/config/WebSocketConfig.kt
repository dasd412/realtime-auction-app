package com.auctionapp.com.auctionapp.infrastructure.config

import com.auctionapp.infrastructure.security.JwtTokenProvider
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        logger.info("Configuring client inbound channel")

        registration.interceptors(
            object : ChannelInterceptor {
                override fun preSend(
                    message: Message<*>,
                    channel: MessageChannel,
                ): Message<*>? {
                    val accessor = StompHeaderAccessor.wrap(message)

                    logger.info("Received message: command=${accessor.command}, messageType=${accessor.messageType}, sessionId=${accessor.sessionId}")

                    if (StompCommand.CONNECT == accessor.command) {
                        logger.info("Processing CONNECT command")
                        val authorization = accessor.getFirstNativeHeader("Authorization")

                        if (authorization != null && authorization.startsWith("Bearer ")) {
                            val token = authorization.substring(7)
                            logger.info("Found Authorization header with token")

                            try {
                                val authentication = jwtTokenProvider.getAuthentication(token)
                                SecurityContextHolder.getContext().authentication = authentication
                                accessor.user = authentication
                                logger.info("Authentication successful for user: ${authentication.name}")
                            } catch (e: Exception) {
                                logger.error("Authentication failed: ${e.message}")
                            }
                        } else {
                            logger.warn("No valid Authorization header found")
                        }
                    } else if (StompCommand.SUBSCRIBE == accessor.command) {
                        logger.info("Processing SUBSCRIBE command to destination: ${accessor.destination}")
                    } else if (StompCommand.SEND == accessor.command) {
                        logger.info("Processing SEND command to destination: ${accessor.destination}")
                    } else if (StompCommand.DISCONNECT == accessor.command) {
                        logger.info("Processing DISCONNECT command")
                    }

                    return message
                }
            },
        )
    }
}
