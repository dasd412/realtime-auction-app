// 상품 등록 API
async function createProduct(productData) {
    try {
        const response = await fetch(`${API_URL}/products`, {
            method: 'POST',
            headers: window.authAPI.getHeaders(),
            body: JSON.stringify(productData)
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '상품 등록 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('상품 등록 에러:', error);
        throw error;
    }
}

// 상품 목록 조회 API
async function getProducts() {
    try {
        const response = await fetch(`${API_URL}/products`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '상품 목록 조회 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('상품 목록 조회 에러:', error);
        throw error;
    }
}

// 내 상품 목록 조회 API
async function getMyProducts() {
    try {
        const response = await fetch(`${API_URL}/products/my-products`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '내 상품 목록 조회 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('내 상품 목록 조회 에러:', error);
        throw error;
    }
}

// 상품 상세 조회 API
async function getProductDetail(productId) {
    try {
        const response = await fetch(`${API_URL}/products/${productId}`, {
            method: 'GET',
            headers: window.authAPI.getHeaders()
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '상품 상세 조회 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('상품 상세 조회 에러:', error);
        throw error;
    }
}

// 상품 갱신 API
async function updateProduct(productId, productData) {
    try {
        const response = await fetch(`${API_URL}/products/${productId}`, {
            method: 'PUT',
            headers: window.authAPI.getHeaders(),
            body: JSON.stringify(productData)
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '상품 갱신 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('상품 갱신 에러:', error);
        throw error;
    }
}

// 상품 삭제 API
async function deleteProduct(productId) {
    try {
        const response = await fetch(`${API_URL}/products/${productId}`, {
            method: 'DELETE',
            headers: window.authAPI.getHeaders()
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '상품 삭제 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('상품 삭제 에러:', error);
        throw error;
    }
}

// 전역으로 내보내기
window.productAPI = {
    createProduct,
    getProducts,
    getMyProducts,
    getProductDetail,
    updateProduct,
    deleteProduct
};
