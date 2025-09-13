'use client';

import React from 'react';
import {
  Container,
  Paper,
  Box,
  Typography,
  TextField,
  Button,
  Alert,
  Link as MuiLink,
} from '@mui/material';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthActions } from '@/shared/lib/auth/auth-store';
import { useRouter } from 'next/navigation';
import apiClient from '@/shared/api/api-client';

// 로그인 폼 검증 스키마
const loginSchema = z.object({
  email: z.string().email('유효한 이메일을 입력해주세요'),
  password: z.string().min(1, '비밀번호를 입력해주세요'),
});

type LoginForm = z.infer<typeof loginSchema>;

/**
 * 로그인 페이지
 */
export default function LoginPage() {
  const { login, setLoading, setError } = useAuthActions();
  const router = useRouter();
  const [localError, setLocalError] = React.useState<string | null>(null);
  const [localLoading, setLocalLoading] = React.useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginForm) => {
    try {
      setLocalLoading(true);
      setLocalError(null);
      setLoading(true);

      // 새로운 API 클라이언트 사용
      const response = await apiClient.post('/api/auth/login', data);

      // 응답 데이터에서 필요한 정보 추출 (백엔드 스키마에 맞춤)
      const authResponse = response.data;

      if (!authResponse.accessToken) {
        throw new Error('토큰을 받지 못했습니다');
      }

      // 새로운 인증 스토어로 로그인 처리
      login(authResponse);

      // 리다이렉트 URL이 있으면 해당 URL로, 없으면 챌린지 페이지로
      const redirectUrl = authResponse.redirectUrl || '/challenges';
      router.push(redirectUrl);

    } catch (err: any) {
      const errorMessage = err?.response?.data?.message ||
                          err?.message ||
                          '이메일 또는 비밀번호가 일치하지 않습니다';

      setLocalError(errorMessage);
      setError(errorMessage);

      console.error('로그인 오류:', err);
    } finally {
      setLocalLoading(false);
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ padding: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            로그인
          </Typography>
          
          <Typography variant="body2" align="center" color="text.secondary" sx={{ mb: 3 }}>
            StockQuest 계정으로 로그인하세요
          </Typography>

          {localError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {localError}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <TextField
              {...register('email')}
              margin="normal"
              required
              fullWidth
              id="email"
              label="이메일"
              name="email"
              autoComplete="email"
              autoFocus
              error={!!errors.email}
              helperText={errors.email?.message}
            />
            
            <TextField
              {...register('password')}
              margin="normal"
              required
              fullWidth
              name="password"
              label="비밀번호"
              type="password"
              id="password"
              autoComplete="current-password"
              error={!!errors.password}
              helperText={errors.password?.message}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              sx={{ mt: 3, mb: 2 }}
              disabled={localLoading}
            >
              {localLoading ? '로그인 중...' : '로그인'}
            </Button>

            <Box textAlign="center">
              <MuiLink component={Link} href="/auth/signup" variant="body2">
                계정이 없으신가요? 회원가입
              </MuiLink>
            </Box>
          </Box>

          {/* 개발용 테스트 계정 안내 */}
          {process.env.NODE_ENV === 'development' && (
            <Alert severity="info" sx={{ mt: 2 }}>
              <Typography variant="body2">
                <strong>테스트 계정:</strong><br />
                이메일: test1234@test.com<br />
                비밀번호: Test1234!
              </Typography>
            </Alert>
          )}
        </Paper>
      </Box>
    </Container>
  );
}