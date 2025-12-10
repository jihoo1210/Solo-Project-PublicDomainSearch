import axios from 'axios';

export const axiosApi = axios.create({
    baseURL: `${process.env.REACT_APP_API_URL}`, // backend API 주소
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // 쿠키 전송 설정
    timeout: 50000, // 요청 타임아웃 설정
});

axiosApi.interceptors.response.use((response) => { // 응답 인터셉터
    return response.data.result;   
}, (error) => {
    if(error.response?.status === 401) { // 인증 오류
        console.error("인증 오류:",error.response.data.message);
    } else if(error.response?.status === 403) { // 권한 오류
        console.error("권한 오류:",error.response.data.message);
        window.location.href = '/login';
    } else if(error.response?.status === 404) { // 존재하지 않는 리소스 오류
        console.error("존재하지 않는 리소스 오류:",error.response.data.message);
    }
    return Promise.reject(error);
});

export default axiosApi;