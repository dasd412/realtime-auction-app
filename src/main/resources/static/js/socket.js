// WebSocket 관련 코드
let stompClient = null;

function connectWebSocket(auctionId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // 인코딩 이슈 해결을 위해 다음 설정 추가
    stompClient.onreceive = function(frame) {
        // 원래 onreceive 동작 유지하면서 인코딩 처리
        if (frame.body) {
            // UTF-8로 적절히 인코딩되도록 처리
            frame.body = decodeURIComponent(escape(frame.body));
        }
    };
    
    // JWT 토큰 가져오기
    const token = localStorage.getItem('auth_token');
    const headers = {};
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    stompClient.connect(headers, function(frame) {
        console.log('Connected: ' + frame);
        
        // 경매 관련 이벤트 구독
        stompClient.subscribe('/topic/auction/' + auctionId, function(message) {
            try {
                const eventData = JSON.parse(message.body);
                handleAuctionEvent(eventData);
            } catch (error) {
                console.error('메시지 파싱 에러:', error);
            }
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

// 현재 가격 업데이트 함수
function updateCurrentPrice(content) {
    const priceElement = document.getElementById('currentPrice');
    if (priceElement) {
        priceElement.textContent = `₩${content.amount.toLocaleString()}`;
        
        // 최소 입찰액 업데이트
        const bidAmountInput = document.getElementById('bidAmount');
        if (bidAmountInput) {
            const minBid = content.amount + 10000; // 기본 입찰 단위
            bidAmountInput.setAttribute('min', minBid);
            bidAmountInput.setAttribute('placeholder', minBid.toString());
        }
    }
}

// 입찰 히스토리 추가 함수
function addBidToHistory(content) {
    const bidHistory = document.querySelector('.bid-history');
    if (bidHistory) {
        const bidItem = document.createElement('div');
        bidItem.className = 'bid-item';
        
        // UTF-8로 적절히 인코딩되도록 처리
        const bidderName = content.bidderName || 'Unknown';
        
        bidItem.innerHTML = `
            <div>
                <div class="bid-user">${bidderName}</div>
                <div class="bid-time">방금 전</div>
            </div>
            <div class="bid-amount">₩${content.amount.toLocaleString()}</div>
        `;
        
        // 첫번째 자식 요소 앞에 삽입 (최신순)
        bidHistory.insertBefore(bidItem, bidHistory.firstChild);
    }
}

// 경매 종료 메시지 표시
function showAuctionEndedMessage(content) {
    alert(`경매가 종료되었습니다! ${content.winnerId ? '낙찰자가 있습니다.' : '낙찰자가 없습니다.'}`);
    // 경매 종료 UI 업데이트
    document.querySelector('.status-badge').textContent = '종료';
    document.querySelector('.status-badge').className = 'status-badge status-ended';
}

// 경매 시작 메시지 표시
function showAuctionStartedMessage() {
    alert('경매가 시작되었습니다!');
    // 경매 시작 UI 업데이트
    document.querySelector('.status-badge').textContent = '진행중';
    document.querySelector('.status-badge').className = 'status-badge status-active';
} 