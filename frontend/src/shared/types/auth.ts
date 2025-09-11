/**
 * 인증 관련 공통 타입 정의
 */

export interface User {
  id: number;
  email: string;
  nickname: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  setToken: (token: string | null) => void;
  setUser: (user: User | null) => void;
  login: (token: string, user: User) => void;
  logout: () => void;
}