'use client';

import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  LinearProgress,
} from '@mui/material';
import {
  Assessment,
  TrendingUp,
  EmojiEvents,
  Timeline,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/shared/lib/auth/auth-store';
import dashboardApi from '@/shared/api/dashboard-client';

interface UserStats {
  totalSessions: number;
  activeSessions: number;
  completedSessions: number;
  averageReturn: number;
  bestReturn: number;
  worstReturn: number;
  totalReturn: number;
  winRate: number;
}

/**
 * 사용자 통계 카드 위젯
 * 개인 투자 성과를 요약해서 표시
 */
export function UserStatsCard() {
  const { user } = useAuth();

  const { data: dashboardData, isLoading } = useQuery({
    queryKey: ['dashboard', user?.id],
    queryFn: async () => {
      const data = await dashboardApi.getDashboardData();
      return data;
    },
    enabled: !!user,
    staleTime: 300000, // 5분
  });

  const stats = dashboardData?.userStats;

  if (!user) {
    return null;
  }

  if (isLoading || !stats) {
    return (
      <Box>
        <Grid container spacing={2}>
          {[1, 2, 3, 4].map((i) => (
            <Grid item xs={12} sm={6} md={3} key={i}>
              <Card sx={{ 
                height: 120,
                backgroundColor: '#1A1F2E',
                border: '1px solid #2A3441',
              }}>
                <CardContent>
                  <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <Typography sx={{ color: '#78828A' }}>로딩 중...</Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }

  const completionRate = stats.totalSessions > 0 ? (stats.completedSessions / stats.totalSessions) * 100 : 0;

  return (
    <Box>
      <Grid container spacing={2}>
        {/* 완료한 챌린지 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ 
            height: 120,
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            '&:hover': {
              border: '1px solid #2196F3',
            }
          }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Assessment sx={{ color: '#2196F3', fontSize: 28 }} />
                <Box>
                  <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                    {stats.completedSessions}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#78828A' }}>
                    완료한 세션
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#2196F3' }}>
                    / {stats.totalSessions}개 참여
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 평균 수익률 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ 
            height: 120,
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            '&:hover': {
              border: '1px solid #4CAF50',
            }
          }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <TrendingUp sx={{ color: '#4CAF50', fontSize: 28 }} />
                <Box>
                  <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                    {stats.averageReturn.toFixed(1)}%
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#78828A' }}>
                    평균 수익률
                  </Typography>
                  <LinearProgress
                    variant="determinate"
                    value={Math.min(stats.averageReturn + 50, 100)} // -50% ~ 50% 범위로 정규화
                    sx={{
                      mt: 0.5,
                      height: 3,
                      backgroundColor: '#2A3441',
                      '& .MuiLinearProgress-bar': {
                        backgroundColor: stats.averageReturn >= 0 ? '#4CAF50' : '#F44336',
                      }
                    }}
                  />
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 최고 수익률 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ 
            height: 120,
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            '&:hover': {
              border: '1px solid #FFD700',
            }
          }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <EmojiEvents sx={{ color: '#FFD700', fontSize: 28 }} />
                <Box>
                  <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                    {stats.bestReturn.toFixed(1)}%
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#78828A' }}>
                    최고 수익률
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#FFD700' }}>
                    개인 기록
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 승률 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{
            height: 120,
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            '&:hover': {
              border: '1px solid #FF9800',
            }
          }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Timeline sx={{ color: '#FF9800', fontSize: 28 }} />
                <Box sx={{ width: '100%' }}>
                  <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                    {stats.winRate.toFixed(1)}%
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#78828A' }}>
                    승률
                  </Typography>
                  <LinearProgress
                    variant="determinate"
                    value={stats.winRate}
                    sx={{
                      mt: 0.5,
                      height: 3,
                      backgroundColor: '#2A3441',
                      '& .MuiLinearProgress-bar': {
                        backgroundColor: stats.winRate >= 50 ? '#4CAF50' : '#FF9800',
                      }
                    }}
                  />
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 완료율 표시 */}
      <Box sx={{ mt: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Typography variant="body2" sx={{ color: '#78828A' }}>
            세션 완료율
          </Typography>
          <Typography variant="body2" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
            {completionRate.toFixed(0)}%
          </Typography>
        </Box>
        <LinearProgress
          variant="determinate"
          value={completionRate}
          sx={{
            height: 6,
            backgroundColor: '#2A3441',
            '& .MuiLinearProgress-bar': {
              backgroundColor: completionRate >= 70 ? '#4CAF50' : completionRate >= 40 ? '#FF9800' : '#F44336',
            }
          }}
        />
      </Box>
    </Box>
  );
}