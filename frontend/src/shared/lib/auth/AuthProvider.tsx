'use client';

import React, { createContext, useContext, useEffect } from 'react';
import { useAuthStore } from './auth-store';
import type { User } from '@/shared/types/auth';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * 인증 컨텍스트 제공자
 * 전역 인증 상태 관리
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated, login, logout } = useAuthStore();
  
  // 페이지 로드 시 토큰 복원
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const savedToken = localStorage.getItem('auth-token');
      if (savedToken && !isAuthenticated) {
        // 저장된 토큰을 스토어에 복원
        // 토큰 유효성은 API 호출 시 401 인터셉터에서 검증됨
        const { setToken } = useAuthStore.getState();
        setToken(savedToken);
      }
    }
  }, [isAuthenticated]);
  
  const contextValue: AuthContextType = {
    user,
    isAuthenticated,
    login,
    logout,
  };
  
  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * 인증 컨텍스트 훅
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth는 AuthProvider 내에서 사용해야 합니다');
  }
  return context;
}