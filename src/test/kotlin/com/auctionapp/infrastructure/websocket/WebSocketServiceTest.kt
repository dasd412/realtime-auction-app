package com.auctionapp.infrastructure.websocket

import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.websocket.dto.WebSocketMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.messaging.simp.SimpMessageSendingOperations

class WebSocketServiceTest {
    private val messagingTemplate: SimpMessageSendingOperations = mock(SimpMessageSendingOperations::class.java)
    private val webSocketService = WebSocketService(messagingTemplate)

    @Test
    @DisplayName("입찰 이벤트 발생 시 올바른 형식의 메시지를 올바른 목적지로 전송한다")
    fun sendBidUpdate_shouldSendCorrectMessageToCorrectDestination() {
        // given
        val auctionId = 1L
        val bidId = 2L
        val amount = Money(10000L)
        val expectedDestination = "/topic/auction/$auctionId"

        val messageCaptor = ArgumentCaptor.forClass(WebSocketMessage::class.java)

        // when
        webSocketService.sendBidUpdate(auctionId, bidId, amount)

        // then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assert(capturedMessage.type == "BID_PLACED")
        assert(capturedMessage.content is Map<*, *>)
        val content = capturedMessage.content as Map<*, *>
        assert(content["auctionId"] == auctionId)
        assert(content["bidId"] == bidId)
        assert(content["amount"] == amount.amount)
    }

    @Test
    @DisplayName("경매 시작 이벤트 발생 시 올바른 형식의 메시지를 올바른 목적지로 전송한다")
    fun sendAuctionStarted_shouldSendCorrectMessageToCorrectDestination() {
        // given
        val auctionId = 1L
        val expectedDestination = "/topic/auction/$auctionId"

        // when
        webSocketService.sendAuctionStarted(auctionId)

        // then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), any(WebSocketMessage::class.java))
    }

    @Test
    @DisplayName("경매 종료 이벤트 발생 시 올바른 형식의 메시지를 올바른 목적지로 전송한다")
    fun sendAuctionEnded_shouldSendCorrectMessageToCorrectDestination() {
        // given
        val auctionId = 1L
        val winnerId = 2L
        val expectedDestination = "/topic/auction/$auctionId"

        // when
        webSocketService.sendAuctionEnded(auctionId, winnerId)

        // then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), any(WebSocketMessage::class.java))
    }
}
