
// 경매 등록 API
async function createAuction(auctionData) {
    try {
        const response = await fetch(`${API_URL}/auctions`, {
            method: 'POST',
            headers: window.authAPI.getHeaders(),
            body: JSON.stringify(auctionData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '경매 등록 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('경매 등록 에러:', error);
        throw error;
    }
}

// 경매 목록 조회 API
async function getAuctions(status = 'ONGOING', sortType = 'NONE', pageNumber = 0) {
    try {
        const params = new URLSearchParams({
            status: status,
            sortType: sortType,
            pageNumber: pageNumber.toString()
        });

        const response = await fetch(`${API_URL}/auctions?${params}`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '경매 목록 조회 실패');
        }

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
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '경매 상세 조회 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('경매 상세 조회 에러:', error);
        throw error;
    }
}


// 경매 취소 API
async function cancelAuction(auctionId) {
    try {
        const response = await fetch(`${API_URL}/auctions/${auctionId}`, {
            method: 'DELETE',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '경매 취소 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('경매 취소 에러:', error);
        throw error;
    }
}

// 입찰 API
async function placeBid(auctionId, amount) {
    try {
        const response = await fetch(`${API_URL}/auctions/${auctionId}/bids`, {
            method: 'POST',
            headers: window.authAPI.getHeaders(),
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



// 특정 경매의 입찰 내역 조회 API
async function getAuctionBids(auctionId, pageNumber = 0) {
    try {
        const params = new URLSearchParams({
            pageNumber: pageNumber.toString()
        });

        const response = await fetch(`${API_URL}/auctions/${auctionId}/bids?${params}`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '입찰 내역 조회 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('입찰 내역 조회 에러:', error);
        throw error;
    }
}

// 내가 생성한 경매 목록 조회 API
async function getMyAuctions(pageNumber = 0) {
    try {
        const params = new URLSearchParams({
            pageNumber: pageNumber.toString()
        });

        const response = await fetch(`${API_URL}/auctions/my-auctions?${params}`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '내 경매 목록 조회 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('내 경매 목록 조회 에러:', error);
        throw error;
    }
}

// 내가 입찰한 경매 목록 조회 API
async function getMyBiddingAuctions(pageNumber = 0) {
    try {
        const params = new URLSearchParams({
            pageNumber: pageNumber.toString()
        });

        const response = await fetch(`${API_URL}/auctions/my-bids/auctions?${params}`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '내 입찰 경매 목록 조회 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('내 입찰 경매 목록 조회 에러:', error);
        throw error;
    }
}

// 내 입찰 내역 조회 API
async function getMyBids(pageNumber = 0) {
    try {
        const params = new URLSearchParams({
            pageNumber: pageNumber.toString()
        });

        const response = await fetch(`${API_URL}/auctions/my-bids?${params}`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '내 입찰 내역 조회 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('내 입찰 내역 조회 에러:', error);
        throw error;
    }
}

// 전략 조회 API
async function getCurrentStrategy() {
    try {
        const response = await fetch(`${API_URL}/auctions/admin/strategy`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '전략 조회 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('전략 조회 에러:', error);
        throw error;
    }
}

// 전략 변경 API
async function changeStrategy(strategy) {
    try {
        const response = await fetch(`${API_URL}/auctions/admin/strategy`, {
            method: 'POST',
            headers: window.authAPI.getHeaders(),
            body: JSON.stringify({ strategy })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '전략 변경 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('전략 변경 에러:', error);
        throw error;
    }
}

// 전역으로 내보내기
window.auctionAPI = {
    getAuctions,
    getAuctionDetail,
    placeBid,
    createAuction,
    cancelAuction,
    getAuctionBids,
    getMyAuctions,
    getMyBiddingAuctions,
    getMyBids,
    getCurrentStrategy,
    changeStrategy
};
