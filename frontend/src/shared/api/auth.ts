import axios from 'axios';
import { AuthResponse } from './generated/model/authResponse';

/**
 * 리프레시 토큰 요청 인터페이스
 */
interface RefreshTokenRequest {
  refreshToken: string;
}

/**
 * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
 */
export const refreshAuthToken = async (request: RefreshTokenRequest) => {
  // 별도의 Axios 인스턴스 사용 (인터셉터 없는)
  const response = await axios.post<AuthResponse>(
    `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/auth/refresh`,
    request,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  return response;
};