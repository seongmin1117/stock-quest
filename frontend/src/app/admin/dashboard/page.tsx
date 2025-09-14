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
  Chip,
  LinearProgress
} from '@mui/material';
import {
  TrendingUp,
  People,
  EmojiEvents,
  Analytics,
  Add,
  Visibility,
  Edit
} from '@mui/icons-material';

// 임시 데이터
const dashboardStats = {
  totalChallenges: 3,
  activeChallenges: 2,
  totalUsers: 125,
  activeUsers: 89,
  completedSessions: 234,
  averageCompletion: 78
};

const recentChallenges = [
  {
    id: 1,
    title: '2020년 코로나 시장 급락',
    status: 'ACTIVE',
    participants: 45,
    completion: 67
  },
  {
    id: 2,
    title: '2021년 밈주식 광풍',
    status: 'ACTIVE',
    participants: 32,
    completion: 84
  },
  {
    id: 3,
    title: '2022년 인플레이션 우려',
    status: 'COMPLETED',
    participants: 28,
    completion: 100
  }
];

export default function AdminDashboard() {
  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          관리자 대시보드
        </Typography>
        <Typography variant="body1" color="text.secondary">
          StockQuest 플랫폼의 전반적인 현황을 확인하고 관리하세요
        </Typography>
      </Box>

      {/* 통계 카드들 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: '#e3f2fd' }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <TrendingUp color="primary" />
                <Typography variant="h6" ml={1}>
                  챌린지
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight="bold">
                {dashboardStats.totalChallenges}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                활성: {dashboardStats.activeChallenges}개
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: '#f3e5f5' }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <People color="secondary" />
                <Typography variant="h6" ml={1}>
                  사용자
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight="bold">
                {dashboardStats.totalUsers}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                활성: {dashboardStats.activeUsers}명
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: '#e8f5e8' }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <EmojiEvents color="success" />
                <Typography variant="h6" ml={1}>
                  완료 세션
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight="bold">
                {dashboardStats.completedSessions}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                이번 달 완료
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: '#fff3e0' }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <Analytics color="warning" />
                <Typography variant="h6" ml={1}>
                  평균 완료율
                </Typography>
              </Box>
              <Typography variant="h4" fontWeight="bold">
                {dashboardStats.averageCompletion}%
              </Typography>
              <LinearProgress
                variant="determinate"
                value={dashboardStats.averageCompletion}
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 최근 챌린지 */}
      <Grid container spacing={3}>
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="between" alignItems="center" mb={3}>
                <Typography variant="h6" fontWeight="bold">
                  최근 챌린지 현황
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<Add />}
                  href="/admin/challenges/new"
                  size="small"
                >
                  새 챌린지
                </Button>
              </Box>

              <Grid container spacing={2}>
                {recentChallenges.map((challenge) => (
                  <Grid item xs={12} key={challenge.id}>
                    <Card variant="outlined">
                      <CardContent>
                        <Box display="flex" justifyContent="between" alignItems="start" mb={2}>
                          <Box>
                            <Typography variant="subtitle1" fontWeight="bold">
                              {challenge.title}
                            </Typography>
                            <Box display="flex" alignItems="center" gap={1} mt={1}>
                              <Chip
                                label={challenge.status === 'ACTIVE' ? '진행중' : '완료'}
                                color={challenge.status === 'ACTIVE' ? 'primary' : 'success'}
                                size="small"
                              />
                              <Typography variant="body2" color="text.secondary">
                                참여자: {challenge.participants}명
                              </Typography>
                            </Box>
                          </Box>
                          <Typography variant="h6" color="primary">
                            {challenge.completion}%
                          </Typography>
                        </Box>

                        <LinearProgress
                          variant="determinate"
                          value={challenge.completion}
                          sx={{ mb: 2 }}
                        />

                        <CardActions sx={{ p: 0 }}>
                          <Button startIcon={<Visibility />} size="small">
                            상세보기
                          </Button>
                          <Button startIcon={<Edit />} size="small">
                            수정
                          </Button>
                        </CardActions>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} lg={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight="bold" mb={2}>
                빠른 작업
              </Typography>

              <Grid container spacing={1}>
                <Grid item xs={12}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<Add />}
                    href="/admin/challenges/new"
                    sx={{ justifyContent: 'flex-start', mb: 1 }}
                  >
                    새 챌린지 생성
                  </Button>
                </Grid>
                <Grid item xs={12}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<Add />}
                    href="/admin/templates/new"
                    sx={{ justifyContent: 'flex-start', mb: 1 }}
                  >
                    새 템플릿 생성
                  </Button>
                </Grid>
                <Grid item xs={12}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<Analytics />}
                    href="/admin/analytics"
                    sx={{ justifyContent: 'flex-start', mb: 1 }}
                  >
                    상세 분석 보기
                  </Button>
                </Grid>
                <Grid item xs={12}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<People />}
                    href="/admin/users"
                    sx={{ justifyContent: 'flex-start' }}
                  >
                    사용자 관리
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}