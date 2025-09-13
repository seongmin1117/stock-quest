'use client';

import React, { useEffect } from 'react';
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

  // 인증이 필요하지 않은 페이지들
  const publicPages = [
    '/auth/login',
    '/auth/signup',
    '/auth/forgot-password',
    '/',
  ];

  const isPublicPage = publicPages.includes(pathname);

  useEffect(() => {
    // 공개 페이지가 아니고 인증되지 않은 경우 로그인 페이지로 리다이렉트
    if (!isPublicPage && !isAuthenticated) {
      const returnUrl = encodeURIComponent(pathname);
      router.push(`/auth/login?returnUrl=${returnUrl}`);
    }
  }, [isAuthenticated, isPublicPage, pathname, router]);

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