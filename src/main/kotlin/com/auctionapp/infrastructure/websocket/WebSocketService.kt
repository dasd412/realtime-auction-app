package com.auctionapp.infrastructure.websocket

import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.websocket.dto.WebSocketMessage
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class WebSocketService(
    private val messagingTemplate: SimpMessageSendingOperations,
) {
    fun sendAuctionStarted(auctionId: Long) {
        val destination = "/topic/auction/$auctionId"

        val message =
            WebSocketMessage(
                type = "AUCTION_STARTED",
                content =
                    mapOf(
                        "auctionId" to auctionId,
                    ),
            )

        messagingTemplate.convertAndSend(destination, message)
    }

    fun sendAuctionEnded(
        auctionId: Long,
        winnerId: Long?,
    ) {
        val destination = "/topic/auction/$auctionId"

        val message =
            WebSocketMessage(
                type = "AUCTION_ENDED",
                content =
                    mapOf(
                        "auctionId" to auctionId,
                        "winnerId" to winnerId,
                    ),
            )

        messagingTemplate.convertAndSend(destination, message)
    }

    fun sendBidUpdate(
        auctionId: Long,
        bidId: Long,
        amount: Money,
    ) {
        val destination = "/topic/auction/$auctionId"

        val message =
            WebSocketMessage(
                type = "BID_PLACED",
                content =
                    mapOf(
                        "auctionId" to auctionId,
                        "bidId" to bidId,
                        "amount" to amount.amount,
                    ),
            )

        messagingTemplate.convertAndSend(destination, message)
    }
}
