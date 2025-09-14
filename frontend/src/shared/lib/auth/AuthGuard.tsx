'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from './auth-store';
import { CircularProgress, Box } from '@mui/material';

interface AuthGuardProps {
  children: React.ReactNode;
}

/**
 * 인증이 필요한 페이지를 보호하는 컴포넌트
 * 로그인되지 않은 사용자는 로그인 페이지로 리다이렉트
 */
const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [isHydrated, setIsHydrated] = useState(false);

  // 인증이 필요하지 않은 페이지들
  const publicPages = [
    '/auth/login',
    '/auth/signup',
    '/auth/forgot-password',
    '/',
  ];

  const isPublicPage = publicPages.includes(pathname);

  // Zustand persist 상태 복원 대기
  useEffect(() => {
    // 클라이언트 사이드에서만 실행되도록 보장
    setIsHydrated(true);
  }, []);

  useEffect(() => {
    // 상태가 복원된 후에만 리다이렉트 로직 실행
    if (isHydrated && !isPublicPage && !isAuthenticated) {
      const returnUrl = encodeURIComponent(pathname);
      router.push(`/auth/login?returnUrl=${returnUrl}`);
    }
  }, [isAuthenticated, isPublicPage, pathname, router, isHydrated]);

  // 상태가 아직 복원되지 않은 경우 로딩 표시
  if (!isHydrated) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  // 공개 페이지이거나 인증된 경우 children 렌더링
  if (isPublicPage || isAuthenticated) {
    return <>{children}</>;
  }

  // 인증 체크 중일 때 로딩 표시
  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
    >
      <CircularProgress />
    </Box>
  );
};

export default AuthGuard;