package com.auctionapp.infrastructure.websocket

import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.websocket.dto.WebSocketMessage
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

/*
도메인 이벤트가 발생하면 → 이벤트 핸들러가 처리 → 웹소켓으로 클라이언트에게 알림
클라이언트의 입찰이나 다른 작업은 일반 HTTP API로 처리 (RESTful API)

클라이언트가 직접 웹소켓을 통해 서버로 메시지를 보낼 필요가 있을 때만 웹소켓 컨트롤러가 필요합니다.
현재는 서버에서 클라이언트로 메시지를 보내기만 하면 되므로 웹 소켓 컨트롤러가 필요없음.

실시간 경매 시스템의 일반적인 구현 방식은
1. 입찰 등 액션: HTTP API로 처리
2. 실시간 업데이트: 웹소켓으로 클라이언트에게 알림
 */
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
