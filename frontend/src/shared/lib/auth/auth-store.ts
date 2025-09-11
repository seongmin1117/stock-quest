import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthState, User } from '@/shared/types/auth';

/**
 * 인증 상태 관리 스토어
 * JWT 토큰과 사용자 정보를 안전하게 관리
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      setToken: (t) => {
        set({ token: t, isAuthenticated: !!t });
        // 토큰을 별도로 localStorage에 저장 (개발 편의성을 위해)
        // 실제 프로덕션에서는 httpOnly 쿠키 사용 권장
        if (typeof window !== 'undefined') {
          if (t) {
            localStorage.setItem('auth-token', t);
          } else {
            localStorage.removeItem('auth-token');
          }
        }
      },

      setUser: (u) => set({ user: u }),

      login: (token: string, user: User) => {
        set({
          user,
          token,
          isAuthenticated: true,
        });
        
        // 토큰을 localStorage에 저장
        if (typeof window !== 'undefined') {
          localStorage.setItem('auth-token', token);
        }
      },
      
      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });
        
        // localStorage에서 토큰 제거
        if (typeof window !== 'undefined') {
          localStorage.removeItem('auth-token');
        }
      },
    }),

    {
      name: 'auth-storage',
      // 사용자 정보만 persist 저장소에 저장
      // 토큰은 별도 관리로 보안성 향상
      partialize: (state) => ({
        user: state.user,
      }),
    }
  )
);