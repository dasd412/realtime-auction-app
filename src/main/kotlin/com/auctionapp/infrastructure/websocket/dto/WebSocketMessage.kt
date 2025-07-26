package com.auctionapp.infrastructure.websocket.dto

data class WebSocketMessage<T>(
    val type: String,
    val content: T,
)
