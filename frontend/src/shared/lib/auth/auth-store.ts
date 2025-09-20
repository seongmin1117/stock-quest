import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { useShallow } from 'zustand/react/shallow';
import { AuthResponse } from '@/shared/api/generated/model/authResponse';

/**
 * 토큰 상태 인터페이스
 */
interface TokenState {
  accessToken: string | null;
  refreshToken: string | null;
  accessTokenExpiresAt: Date | null;
  refreshTokenExpiresAt: Date | null;
}

/**
 * 사용자 정보 인터페이스
 */
interface UserInfo {
  id: number;
  email: string;
  nickname: string;
  role: 'USER' | 'ADMIN';
}

/**
 * 인증 상태 인터페이스
 */
interface AuthState {
  // 상태
  tokens: TokenState;
  user: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // 액션
  setTokens: (tokens: Partial<TokenState>) => void;
  setUser: (user: UserInfo | null) => void;
  login: (response: AuthResponse) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearAuth: () => void;

  // 헬퍼 메서드
  isTokenExpired: () => boolean;
  isRefreshTokenExpired: () => boolean;
  getAccessToken: () => string | null;
  getRefreshToken: () => string | null;
}

/**
 * 토큰 만료 체크 유틸리티
 */
const isTokenExpired = (expiresAt: Date | null): boolean => {
  if (!expiresAt) return true;

  // 5분 전에 만료되는 것으로 간주 (버퍼 시간)
  const bufferTime = 5 * 60 * 1000; // 5 minutes
  return new Date().getTime() >= new Date(expiresAt).getTime() - bufferTime;
};

/**
 * 초기 상태
 */
const initialState = {
  tokens: {
    accessToken: null,
    refreshToken: null,
    accessTokenExpiresAt: null,
    refreshTokenExpiresAt: null,
  },
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

/**
 * 최신 Zustand 기반 인증 스토어
 * - 토큰과 사용자 정보를 메모리와 localStorage에 저장
 * - 토큰 만료 체크 및 자동 갱신 지원
 * - React Query와의 통합을 위한 헬퍼 메서드 제공
 */
export const useAuthStore = create<AuthState>()(
  persist(
    immer((set, get) => ({
      ...initialState,

      setTokens: (tokens) =>
        set((state) => {
          if (tokens.accessToken !== undefined) {
            state.tokens.accessToken = tokens.accessToken;
          }
          if (tokens.refreshToken !== undefined) {
            state.tokens.refreshToken = tokens.refreshToken;
          }
          if (tokens.accessTokenExpiresAt !== undefined) {
            state.tokens.accessTokenExpiresAt = tokens.accessTokenExpiresAt
              ? new Date(tokens.accessTokenExpiresAt)
              : null;
          }
          if (tokens.refreshTokenExpiresAt !== undefined) {
            state.tokens.refreshTokenExpiresAt = tokens.refreshTokenExpiresAt
              ? new Date(tokens.refreshTokenExpiresAt)
              : null;
          }

          // 토큰이 설정되면 인증 상태 업데이트
          state.isAuthenticated = !!state.tokens.accessToken;
        }),

      setUser: (user) =>
        set((state) => {
          state.user = user;
        }),

      login: (response) =>
        set((state) => {
          // 토큰 설정
          state.tokens.accessToken = response.accessToken || null;
          state.tokens.refreshToken = response.refreshToken || null;
          state.tokens.accessTokenExpiresAt = response.accessTokenExpiresAt
            ? new Date(response.accessTokenExpiresAt)
            : null;
          state.tokens.refreshTokenExpiresAt = response.refreshTokenExpiresAt
            ? new Date(response.refreshTokenExpiresAt)
            : null;

          // 사용자 정보 설정
          if (response.userId && response.email && response.nickname) {
            // 관리자 여부 판단 (Navbar.tsx와 동일한 로직)
            const isAdmin = response.email === 'admin@example.com' ||
                          response.email === 'admin@stockquest.com' ||
                          response.nickname?.toLowerCase().includes('admin');

            state.user = {
              id: response.userId,
              email: response.email,
              nickname: response.nickname,
              role: isAdmin ? 'ADMIN' : 'USER',
            };
          }

          // 상태 업데이트
          state.isAuthenticated = true;
          state.isLoading = false;
          state.error = null;
        }),

      logout: () =>
        set((state) => {
          // 모든 상태 초기화
          Object.assign(state, initialState);
        }),

      setLoading: (loading) =>
        set((state) => {
          state.isLoading = loading;
        }),

      setError: (error) =>
        set((state) => {
          state.error = error;
          state.isLoading = false;
        }),

      clearAuth: () =>
        set((state) => {
          Object.assign(state, initialState);
        }),

      isTokenExpired: () => {
        const state = get();
        return isTokenExpired(state.tokens.accessTokenExpiresAt);
      },

      isRefreshTokenExpired: () => {
        const state = get();
        return isTokenExpired(state.tokens.refreshTokenExpiresAt);
      },

      getAccessToken: () => {
        const state = get();

        // 토큰이 만료되었으면 null 반환
        if (state.isTokenExpired()) {
          return null;
        }

        return state.tokens.accessToken;
      },

      getRefreshToken: () => {
        const state = get();

        // 리프레시 토큰이 만료되었으면 null 반환
        if (state.isRefreshTokenExpired()) {
          return null;
        }

        return state.tokens.refreshToken;
      },
    })),
    {
      name: 'auth-storage-v2',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        tokens: state.tokens,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

/**
 * 인증 상태 셀렉터 (useShallow로 최적화)
 */
export const useAuth = () => {
  return useAuthStore(
    useShallow((state) => ({
      isAuthenticated: state.isAuthenticated,
      user: state.user,
      tokens: state.tokens,
      isLoading: state.isLoading,
      error: state.error,
    }))
  );
};

export const useAuthActions = () => {
  return useAuthStore(
    useShallow((state) => ({
      login: state.login,
      logout: state.logout,
      setTokens: state.setTokens,
      setUser: state.setUser,
      setLoading: state.setLoading,
      setError: state.setError,
    }))
  );
};

export const useAuthTokens = () => {
  return useAuthStore(
    useShallow((state) => ({
      getAccessToken: state.getAccessToken,
      getRefreshToken: state.getRefreshToken,
      isTokenExpired: state.isTokenExpired,
      isRefreshTokenExpired: state.isRefreshTokenExpired,
    }))
  );
};