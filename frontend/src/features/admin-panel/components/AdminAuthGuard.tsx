'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Box, CircularProgress, Typography, Alert } from '@mui/material';
import { useAuthStore } from '@/shared/lib/auth/auth-store';

interface AdminAuthGuardProps {
  children: React.ReactNode;
}

export default function AdminAuthGuard({ children }: AdminAuthGuardProps) {
  const router = useRouter();
  const { user, isAuthenticated, isLoading } = useAuthStore();
  const [isChecking, setIsChecking] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const checkAdminAccess = async () => {
      try {
        // 로딩 중이면 대기
        if (isLoading) {
          return;
        }

        // 인증되지 않은 경우 로그인 페이지로 리다이렉트
        if (!isAuthenticated || !user) {
          router.push('/auth/login?returnUrl=/admin');
          return;
        }

        // 관리자 권한 확인
        if (user.role !== 'ADMIN') {
          setError('관리자 권한이 필요합니다.');
          setTimeout(() => {
            router.push('/dashboard');
          }, 3000);
          return;
        }

        // 관리자 권한 확인됨
        setIsChecking(false);
        setError(null);
      } catch (err) {
        console.error('Admin access check failed:', err);
        setError('권한 확인 중 오류가 발생했습니다.');
        setTimeout(() => {
          router.push('/auth/login');
        }, 3000);
      }
    };

    checkAdminAccess();
  }, [isAuthenticated, isLoading, user, router]);

  // 로딩 중
  if (isLoading || isChecking) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
        gap={2}
      >
        <CircularProgress size={40} />
        <Typography variant="body2" color="text.secondary">
          권한을 확인하는 중...
        </Typography>
      </Box>
    );
  }

  // 오류 발생
  if (error) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
        gap={2}
        px={3}
      >
        <Alert severity="error" sx={{ maxWidth: 400 }}>
          <Typography variant="h6" gutterBottom>
            접근 권한 없음
          </Typography>
          <Typography variant="body2">
            {error}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            잠시 후 메인 페이지로 이동합니다.
          </Typography>
        </Alert>
      </Box>
    );
  }

  // 권한 확인 완료, 관리자 패널 렌더링
  return <>{children}</>;
}