// WebSocket 관련 코드
let stompClient = null;

function connectWebSocket(auctionId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        // 경매 관련 이벤트 구독
        stompClient.subscribe('/topic/auction/' + auctionId, function(message) {
            const eventData = JSON.parse(message.body);
            handleAuctionEvent(eventData);
        });
        
        document.querySelector('.realtime-indicator span').textContent = '실시간 입찰 연결됨 (WebSocket)';
    }, function(error) {
        console.error('WebSocket 연결 실패:', error);
        document.querySelector('.realtime-indicator span').textContent = '연결 실패! 재연결 시도 중...';
        document.querySelector('.realtime-indicator span').style.color = '#ff6b6b';
        
        // 재연결 시도
        setTimeout(() => connectWebSocket(auctionId), 3000);
    });
}

function handleAuctionEvent(event) {
    if (event.type === 'BID_PLACED') {
        // 입찰 이벤트 처리
        updateCurrentPrice(event.content);
        addBidToHistory(event.content);
    } else if (event.type === 'AUCTION_ENDED') {
        // 경매 종료 이벤트 처리
        showAuctionEndedMessage(event.content);
    } else if (event.type === 'AUCTION_STARTED') {
        // 경매 시작 이벤트 처리
        showAuctionStartedMessage();
    }
}

function disconnectWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log('WebSocket 연결 해제');
    }
} 