// 메인 앱 로직
document.addEventListener('DOMContentLoaded', function() {
    // 초기 화면 설정
    checkAuthStatus();

    // 이벤트 리스너 설정
    setupEventListeners();
});

function checkAuthStatus() {
    const token = localStorage.getItem('auth_token');
    if (token) {
        // 이미 로그인된 상태면 경매 목록 화면으로
        showScreen('list');
        loadAuctions();
    } else {
        // 로그인되지 않은 상태면 로그인 화면으로
        showScreen('login');
    }
}

function setupEventListeners() {
    // 로그인 폼 제출 이벤트
    document.querySelector('#login form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        if (!email || !password) {
            alert('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        try {
            await login(email, password);
            showScreen('list');
            loadAuctions();
        } catch (error) {
            console.error('로그인 실패:', error);  // 디버깅용 로그
            alert('로그인에 실패했습니다: ' + error.message);
        }
    });

    // 회원가입 폼 제출 이벤트
    document.querySelector('#register form').addEventListener('submit', async function(e) {
        e.preventDefault();
        // 회원가입 로직 구현...
    });

    // 경매 등록 폼 제출 이벤트
    document.querySelector('#create form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const auctionData = {
            title: document.getElementById('title').value,
            description: document.getElementById('description').value,
            startPrice: parseInt(document.getElementById('startPrice').value),
            bidUnit: parseInt(document.getElementById('bidUnit').value),
            startTime: document.getElementById('startTime').value,
            endTime: document.getElementById('endTime').value
        };

        try {
            await createAuction(auctionData);
            alert('경매가 성공적으로 등록되었습니다!');
            showScreen('list');
            loadAuctions();
        } catch (error) {
            alert('경매 등록에 실패했습니다: ' + error.message);
        }
    });

    // 입찰 폼 제출 이벤트 (경매 상세 화면)
    document.querySelector('.bid-form button').addEventListener('click', async function() {
        const auctionId = getCurrentAuctionId();
        const bidAmount = parseInt(document.getElementById('bidAmount').value);

        try {
            await placeBid(auctionId, bidAmount);
            // 웹소켓으로 실시간 업데이트가 올 것이므로 여기서는 UI 업데이트 불필요
        } catch (error) {
            alert('입찰에 실패했습니다: ' + error.message);
        }
    });
}

// 경매 목록 로드 및 화면에 표시
async function loadAuctions() {
    try {
        const auctions = await getAuctions();
        displayAuctions(auctions);
    } catch (error) {
        console.error('경매 목록 로드 실패:', error);
    }
}

// 경매 목록 화면에 표시
function displayAuctions(auctions) {
    const auctionGrid = document.querySelector('.auction-grid');
    auctionGrid.innerHTML = '';

    auctions.forEach(auction => {
        const auctionCard = document.createElement('div');
        auctionCard.className = 'auction-card';
        auctionCard.innerHTML = `
            <div class="auction-image">📱</div>
            <div class="auction-content">
                <div class="auction-title">${auction.title}</div>
                <div class="auction-price">₩${auction.currentPrice.toLocaleString()}</div>
                <div class="auction-status">
                    <span class="status-badge status-${auction.status.toLowerCase()}">${getStatusText(auction.status)}</span>
                    <span>${getRemainingTimeText(auction.endTime)}</span>
                </div>
                <button class="btn btn-secondary" style="width: 100%;" onclick="showAuctionDetail(${auction.id})">입찰하기</button>
            </div>
        `;
        auctionGrid.appendChild(auctionCard);
    });
}

// 경매 상세 화면 표시
async function showAuctionDetail(auctionId) {
    try {
        const auction = await getAuctionDetail(auctionId);

        // 경매 상세 정보 화면 설정
        document.querySelector('.screen-title').textContent = auction.title;
        document.getElementById('currentPrice').textContent = `₩${auction.currentPrice.toLocaleString()}`;

        // 입찰 폼 설정
        const minBidAmount = auction.currentPrice + auction.bidUnit;
        document.getElementById('bidAmount').setAttribute('min', minBidAmount);
        document.getElementById('bidAmount').setAttribute('placeholder', minBidAmount.toString());

        // 웹소켓 연결
        connectWebSocket(auctionId);

        // 화면 전환
        showScreen('detail');
    } catch (error) {
        console.error('경매 상세 정보 로드 실패:', error);
    }
}

// 화면 전환 함수
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(screen => {
        screen.classList.remove('active');
    });
    document.getElementById(screenId).classList.add('active');
}

// 유틸리티 함수들
function getStatusText(status) {
    switch (status) {
        case 'ONGOING': return '진행중';
        case 'ENDED': return '종료';
        case 'NOT_STARTED': return '시작 전';
        default: return status;
    }
}

function getRemainingTimeText(endTimeStr) {
    const endTime = new Date(endTimeStr);
    const now = new Date();
    const diff = endTime - now;

    if (diff < 0) return '종료됨';

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (days > 0) return `${days}일 ${hours}시간 남음`;
    if (hours > 0) return `${hours}시간 ${minutes}분 남음`;
    return `${minutes}분 남음`;
}
