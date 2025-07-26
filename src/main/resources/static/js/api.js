// API 관련 함수
const API_URL = '/api';
let authToken = localStorage.getItem('auth_token');

// 헤더 설정 함수
function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': authToken ? `Bearer ${authToken}` : ''
    };
}

// 로그인 API
async function login(email, password) {
    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        if (!response.ok) throw new Error('로그인 실패');
        
        const data = await response.json();
        authToken = data.accessToken;
        localStorage.setItem('auth_token', authToken);
        return data;
    } catch (error) {
        console.error('로그인 에러:', error);
        throw error;
    }
}

// 경매 목록 조회 API
async function getAuctions() {
    try {
        const response = await fetch(`${API_URL}/auctions`, {
            method: 'GET',
            headers: getHeaders()
        });
        
        if (!response.ok) throw new Error('경매 목록 조회 실패');
        
        return await response.json();
    } catch (error) {
        console.error('경매 목록 조회 에러:', error);
        throw error;
    }
}

// 경매 상세 조회 API
async function getAuctionDetail(auctionId) {
    try {
        const response = await fetch(`${API_URL}/auctions/${auctionId}`, {
            method: 'GET',
            headers: getHeaders()
        });
        
        if (!response.ok) throw new Error('경매 상세 조회 실패');
        
        return await response.json();
    } catch (error) {
        console.error('경매 상세 조회 에러:', error);
        throw error;
    }
}

// 입찰 API
async function placeBid(auctionId, amount) {
    try {
        const response = await fetch(`${API_URL}/auctions/${auctionId}/bid`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ amount })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '입찰 실패');
        }
        
        return await response.json();
    } catch (error) {
        console.error('입찰 에러:', error);
        throw error;
    }
}

// 경매 등록 API
async function createAuction(auctionData) {
    try {
        const response = await fetch(`${API_URL}/auctions`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(auctionData)
        });
        
        if (!response.ok) throw new Error('경매 등록 실패');
        
        return await response.json();
    } catch (error) {
        console.error('경매 등록 에러:', error);
        throw error;
    }
} 