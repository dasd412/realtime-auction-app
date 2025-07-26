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
        console.log('로그인 시도:', { email, password });
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        console.log('로그인 응답:', response);

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '로그인 실패');
        }

        const data = await response.json();
        console.log('로그인 성공:', data);
        authToken = data.accessToken;
        localStorage.setItem('auth_token', authToken);
        return data;
    } catch (error) {
        console.error('로그인 에러:', error);
        throw error;
    }
}

// 회원가입 API
async function signup(email, password, name) {
    try {
        const response = await fetch(`${API_URL}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, name })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '회원가입 실패');
        }
        return await response.json();
    } catch (error) {
        console.error('회원가입 에러:', error);
        throw error;
    }
}

// 로그아웃 API
async function logout() {
    try {
        const response = await fetch(`${API_URL}/auth/logout`, {
            method: 'POST',
            headers: getHeaders()
        });

        if (!response.ok) throw new Error('로그아웃 실패');

        // 로컬 스토리지에서 토큰 제거
        localStorage.removeItem('auth_token');
        authToken = null;

        return await response.json();
    } catch (error) {
        console.error('로그아웃 에러:', error);
        throw error;
    }
}

// 리프레시 토큰 API
async function refreshToken(refreshToken) {
    const token = refreshToken.replace("Bearer ", "")
    try {
        const response = await fetch(`${API_URL}/auth/refresh`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) throw new Error('리프레시 토큰 실패');

        const data = await response.json();
        authToken = data.accessToken;
        localStorage.setItem('auth_token', authToken);
        return data;
    } catch (error) {
        console.error('리프레시 토큰 에러:', error);
        throw error;
    }
}

// 토큰 가져오기 (다른 모듈에서 사용)
function getAuthToken() {
    return authToken;
}

// 전역으로 내보내기
window.authAPI = {
    login,
    signup,
    logout,
    refreshToken,
    getAuthToken,
    getHeaders
};
