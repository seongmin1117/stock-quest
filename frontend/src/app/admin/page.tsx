'use client';

import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Container,
} from '@mui/material';
import {
  Dashboard,
  TrendingUp,
  People,
  Analytics,
  Article,
  Settings,
  Assessment,
  TrendingDown,
  School,
} from '@mui/icons-material';
import Link from 'next/link';

/**
 * Admin 메인 페이지
 * 모든 관리 기능에 대한 중앙 진입점 제공
 */
export default function AdminPage() {
  const adminSections = [
    {
      title: '대시보드',
      description: '전체 시스템 현황과 주요 지표를 확인하세요',
      icon: <Dashboard sx={{ fontSize: 40 }} />,
      href: '/admin/dashboard',
      color: '#2196F3',
    },
    {
      title: '블로그 관리',
      description: '게시글, 카테고리, 태그를 관리하세요',
      icon: <Article sx={{ fontSize: 40 }} />,
      href: '/admin/blog',
      color: '#4CAF50',
    },
    {
      title: '챌린지 관리',
      description: '투자 챌린지를 생성하고 관리하세요',
      icon: <TrendingUp sx={{ fontSize: 40 }} />,
      href: '/admin/challenges',
      color: '#FF9800',
    },
    {
      title: '사용자 관리',
      description: '사용자 계정과 권한을 관리하세요',
      icon: <People sx={{ fontSize: 40 }} />,
      href: '/admin/users',
      color: '#9C27B0',
    },
    {
      title: '분석 리포트',
      description: '사용자 행동과 시스템 성능을 분석하세요',
      icon: <Analytics sx={{ fontSize: 40 }} />,
      href: '/admin/analytics',
      color: '#F44336',
    },
    {
      title: 'ML 트레이딩',
      description: '머신러닝 기반 트레이딩 시스템을 관리하세요',
      icon: <Assessment sx={{ fontSize: 40 }} />,
      href: '/admin/ml-trading',
      color: '#607D8B',
    },
    {
      title: '포트폴리오 최적화',
      description: '포트폴리오 최적화 도구를 관리하세요',
      icon: <TrendingDown sx={{ fontSize: 40 }} />,
      href: '/admin/portfolio-optimization',
      color: '#795548',
    },
    {
      title: '투자 추천',
      description: 'AI 기반 투자 추천 시스템을 관리하세요',
      icon: <School sx={{ fontSize: 40 }} />,
      href: '/admin/recommendations',
      color: '#009688',
    },
  ];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom sx={{ fontWeight: 'bold' }}>
          StockQuest 관리자 센터
        </Typography>
        <Typography variant="h6" color="text.secondary">
          시스템의 모든 관리 기능에 접근할 수 있습니다
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {adminSections.map((section) => (
          <Grid item xs={12} sm={6} md={4} key={section.href}>
            <Card
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'all 0.3s ease-in-out',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
              }}
            >
              <CardContent sx={{ flexGrow: 1, textAlign: 'center', p: 3 }}>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'center',
                    mb: 2,
                    color: section.color,
                  }}
                >
                  {section.icon}
                </Box>
                <Typography variant="h6" component="h2" gutterBottom sx={{ fontWeight: 'bold' }}>
                  {section.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {section.description}
                </Typography>
              </CardContent>
              <CardActions sx={{ p: 2, pt: 0 }}>
                <Button
                  component={Link}
                  href={section.href}
                  variant="contained"
                  fullWidth
                  sx={{
                    backgroundColor: section.color,
                    '&:hover': {
                      backgroundColor: section.color,
                      filter: 'brightness(0.9)',
                    },
                  }}
                >
                  관리하기
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Box sx={{ mt: 6, p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
        <Typography variant="h6" gutterBottom>
          시스템 정보
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="body2" color="text.secondary">
              환경
            </Typography>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
              개발 환경
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="body2" color="text.secondary">
              버전
            </Typography>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
              v1.0.0
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="body2" color="text.secondary">
              마지막 배포
            </Typography>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
              {new Date().toLocaleDateString('ko-KR')}
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="body2" color="text.secondary">
              상태
            </Typography>
            <Typography variant="body1" sx={{ fontWeight: 'bold', color: 'success.main' }}>
              정상 운영
            </Typography>
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
}