// src/shared/api/apiClient.ts
console.log('[apiClient] module loaded');

import axios, {
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
  AxiosHeaders,
} from 'axios';
import { useAuthStore } from '@/shared/lib/auth/auth-store';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

const axiosInstance = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// 요청 인터셉터
axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // v2 store의 getAccessToken 메서드 사용
      const token = useAuthStore.getState().getAccessToken();

      // 디버그 로그 (개발 중에만)
      if (process.env.NODE_ENV !== 'production') {
        // eslint-disable-next-line no-console
        console.debug('[apiClient] req', config.method, config.url, 'token?', !!token);
      }

      if (token) {
        config.headers.set('Authorization', `Bearer ${token}`);
      }
      return config;
    },
    (error) => Promise.reject(error)
);

// 응답 인터셉터 - 401 에러 시 자동 로그아웃
axiosInstance.interceptors.response.use(
    (res) => res,
    (error) => {
      if (error?.response?.status === 401) {
        // 인증 실패 시 자동 로그아웃
        useAuthStore.getState().logout();
        // 로그인 페이지로 리디렉션
        if (typeof window !== 'undefined') {
          window.location.href = '/auth/login';
        }
      }
      return Promise.reject(error);
    }
);

// 기본 apiClient 함수 (Orval 호환)
export const apiClient = <T = any>(config: AxiosRequestConfig): Promise<T> =>
    axiosInstance(config).then(({ data }) => data);

// REST API 메서드들을 포함한 확장된 API 클라이언트
const extendedApiClient = Object.assign(apiClient, {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    axiosInstance.get(url, config).then(({ data }) => data),
  
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    axiosInstance.post(url, data, config).then(({ data }) => data),
  
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    axiosInstance.put(url, data, config).then(({ data }) => data),
  
  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    axiosInstance.patch(url, data, config).then(({ data }) => data),
  
  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    axiosInstance.delete(url, config).then(({ data }) => data),
});

// 기본 export를 확장된 클라이언트로 변경
export default extendedApiClient;

// 호환성을 위해 axios 인스턴스도 export
export { axiosInstance };
