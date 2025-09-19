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
  const [hasRedirected, setHasRedirected] = useState(false);

  // 인증이 필요하지 않은 페이지들
  const publicPages = [
    '/auth/login',
    '/auth/signup',
    '/auth/forgot-password',
    '/',
    '/dca-simulation',
  ];

  const isPublicPage = publicPages.includes(pathname);

  // Zustand persist 상태 복원 대기
  useEffect(() => {
    // 클라이언트 사이드에서만 실행되도록 보장
    const timer = setTimeout(() => {
      setIsHydrated(true);
    }, 100);

    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    // 상태가 복원된 후에만 리다이렉트 로직 실행
    if (isHydrated && !isPublicPage && !isAuthenticated && !hasRedirected) {
      console.log('AuthGuard: Redirecting to login', { pathname, isAuthenticated });
      setHasRedirected(true);
      const returnUrl = encodeURIComponent(pathname);
      router.replace(`/auth/login?returnUrl=${returnUrl}`);
    }
  }, [isAuthenticated, isPublicPage, pathname, router, isHydrated, hasRedirected]);

  // 상태가 아직 복원되지 않은 경우 로딩 표시
  if (!isHydrated) {
    console.log('AuthGuard: Not hydrated yet');
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
    console.log('AuthGuard: Rendering children', { isPublicPage, isAuthenticated });
    return <>{children}</>;
  }

  // 리다이렉트 중일 때 로딩 표시
  console.log('AuthGuard: Showing loading for redirect', { hasRedirected });
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