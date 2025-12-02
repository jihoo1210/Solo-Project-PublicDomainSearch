import './App.css';
import { useState, useEffect } from 'react';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);

  // 페이지 로드 시 쿠키에서 토큰 확인
  useEffect(() => {
    const token = getCookie('ACCESS_TOKEN');
    if (token) {
      setIsLoggedIn(true);
      // 실제로는 토큰으로 사용자 정보를 서버에서 가져와야 함
      setUser({ token });
    }

    // 콜백 파라미터 확인 (OAuth 리다이렉트 후)
    const params = new URLSearchParams(window.location.search);
    if (params.get('callback') === 'success') {
      setIsLoggedIn(true);
      // URL 정리
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  // 쿠키에서 토큰 가져오기
  const getCookie = (name) => {
    const nameEQ = name + '=';
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
      cookie = cookie.trim();
      if (cookie.indexOf(nameEQ) === 0) {
        return cookie.substring(nameEQ.length);
      }
    }
    return null;
  };

  // Naver OAuth 로그인 URL 생성
  const getNaverLoginUrl = () => {
    const clientId = process.env.REACT_APP_NAVER_CLIENT_ID;
    const redirectUri = process.env.REACT_APP_NAVER_REDIRECT_URI;
    const state = generateRandomState();
    const responseType = 'code';
    
    return `https://nid.naver.com/oauth2.0/authorize?response_type=${responseType}&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
  };

  // Google OAuth 로그인 URL 생성
  const getGoogleLoginUrl = () => {
    const clientId = process.env.REACT_APP_GOOGLE_CLIENT_ID;
    const redirectUri = process.env.REACT_APP_GOOGLE_REDIRECT_URI;
    const scope = encodeURIComponent('openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email');
    const responseType = 'code';
    const state = generateRandomState();
    
    return `https://accounts.google.com/o/oauth2/v2/auth?response_type=${responseType}&client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}&state=${state}`;
  };

  // 랜덤 state 생성 (CSRF 방지)
  const generateRandomState = () => {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
  };

  // Naver 로그인 핸들러
  const handleNaverLogin = () => {
    setLoading(true);
    const loginUrl = getNaverLoginUrl();
    // 페이지 언로드 감지 - 로딩 유지
    window.addEventListener('beforeunload', () => {
      // 로딩 상태 유지
    });
    setTimeout(() => {
      window.location.href = loginUrl;
    }, 300); // 약간의 딜레이 후 리다이렉트
  };

  // Google 로그인 핸들러
  const handleGoogleLogin = () => {
    setLoading(true);
    const loginUrl = getGoogleLoginUrl();
    // 페이지 언로드 감지 - 로딩 유지
    window.addEventListener('beforeunload', () => {
      // 로딩 상태 유지
    });
    setTimeout(() => {
      window.location.href = loginUrl;
    }, 300); // 약간의 딜레이 후 리다이렉트
  };

  // 로그아웃 핸들러
  const handleLogout = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${process.env.REACT_APP_API_BASE_URL}/api/auth/logout`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (response.ok) {
        setIsLoggedIn(false);
        setUser(null);
        // 쿠키 삭제
        document.cookie = 'ACCESS_TOKEN=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
      }
    } catch (error) {
      console.error('로그아웃 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="App">
      {loading && (
        <div className="loading-overlay">
          <div className="loading-container">
            <div className="spinner"></div>
            <p className="loading-text">처리 중입니다...</p>
          </div>
        </div>
      )}
      <header className="App-header">
        <h1>OAuth 로그인 테스트</h1>

        {isLoggedIn ? (
          <div className="logout-section">
            <p className="success-message">✓ 로그인 성공!</p>
            {user && (
              <div className="user-info">
                <p>토큰이 쿠키에 저장되었습니다.</p>
                <p className="token-info">ACCESS_TOKEN: {user.token ? user.token.substring(0, 20) + '...' : 'N/A'}</p>
              </div>
            )}
            <button
              className="logout-button"
              onClick={handleLogout}
              disabled={loading}
            >
              {loading ? '로그아웃 중...' : '로그아웃'}
            </button>
          </div>
        ) : (
          <div className="login-section">
            <p className="subtitle">OAuth 2.0을 사용한 소셜 로그인</p>
            <div className="button-group">
              <button
                className="login-button naver-button"
                onClick={handleNaverLogin}
                disabled={loading}
              >
                {loading ? '처리 중...' : '네이버로 로그인'}
              </button>
              <button
                className="login-button google-button"
                onClick={handleGoogleLogin}
                disabled={loading}
              >
                {loading ? '처리 중...' : 'Google로 로그인'}
              </button>
            </div>
            <div className="info-box">
              <h3>로그인 흐름:</h3>
              <ol>
                <li>버튼을 클릭하면 OAuth 제공자 로그인 페이지로 이동</li>
                <li>사용자 인증 후 백엔드 콜백 URL로 리다이렉트</li>
                <li>백엔드에서 토큰 발급 및 사용자 정보 조회</li>
                <li>JWT 토큰을 쿠키에 저장 후 프론트로 리다이렉트</li>
                <li>로그인 완료!</li>
              </ol>
            </div>
          </div>
        )}
      </header>
    </div>
  );
}

export default App;
