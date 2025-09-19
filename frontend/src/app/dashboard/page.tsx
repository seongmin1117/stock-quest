'use client';

import React from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Avatar,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  TrendingUp,
  TrendingDown,
  EmojiEvents,
  Speed,
  Timeline,
  Assessment,
  PlayArrow,
  Calculate,
} from '@mui/icons-material';
import Link from 'next/link';
import { useAuth } from '@/shared/lib/auth/auth-store';
import { UserStatsCard } from '@/widgets/dashboard';
import { GlobalLeaderboard } from '@/widgets/leaderboard';
import { CommunityFeed } from '@/widgets/community';
import dashboardApi, {
  RecentSession,
  SessionStatus,
  dashboardUtils
} from '@/shared/api/dashboard-client';

/**
 * 사용자 대시보드 페이지
 * 개인 통계, 진행 중인 세션, 최근 결과 등을 종합적으로 표시
 */
export default function DashboardPage() {
  const { user } = useAuth();
  const [recentSessions, setRecentSessions] = React.useState<RecentSession[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (user) {
      loadDashboardData();
    }
  }, [user]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // 대시보드 API 클라이언트를 통해 실제 데이터 로드
      const dashboardData = await dashboardApi.getDashboardData();
      setRecentSessions(dashboardData.recentSessions);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || '대시보드 데이터를 불러오는데 실패했습니다';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: SessionStatus) => {
    return dashboardUtils.getStatusColor(status);
  };

  const getStatusText = (status: SessionStatus) => {
    return dashboardUtils.getStatusText(status);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
    }).format(amount);
  };

  const formatPercentage = (rate: number) => {
    const formatted = dashboardUtils.formatReturnRate(rate);
    return {
      value: formatted.value,
      color: formatted.color,
      icon: formatted.isPositive ? <TrendingUp sx={{ fontSize: 16 }} /> :
            rate < 0 ? <TrendingDown sx={{ fontSize: 16 }} /> : null,
    };
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (!user) {
    return (
      <Container maxWidth="lg">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <Alert severity="warning">
            대시보드를 보려면 로그인이 필요합니다.
          </Alert>
        </Box>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#0A0E18', pt: 4 }}>
      <Container maxWidth="xl">
        <Box sx={{ py: 4 }}>
          {/* 헤더 */}
          <Box sx={{ mb: 4 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, mb: 2 }}>
              <Avatar
                sx={{
                  width: 64,
                  height: 64,
                  backgroundColor: '#2196F3',
                  fontSize: '1.5rem',
                  fontWeight: 'bold',
                }}
              >
                {user.nickname?.charAt(0)?.toUpperCase() || 'U'}
              </Avatar>
              
              <Box>
                <Typography 
                  variant="h3" 
                  component="h1" 
                  sx={{ 
                    color: '#FFFFFF',
                    fontWeight: 'bold',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                  }}
                >
                  <DashboardIcon sx={{ fontSize: 40, color: '#2196F3' }} />
                  {user.nickname}님의 대시보드
                </Typography>
                <Typography variant="body1" sx={{ color: '#78828A', mt: 1 }}>
                  투자 성과를 확인하고 새로운 챌린지에 도전하세요
                </Typography>
              </Box>
            </Box>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {/* 통계 카드 - 위젯 사용 */}
          <Box sx={{ mb: 4 }}>
            <UserStatsCard />
          </Box>

          {/* 최근 세션 */}
          <Grid container spacing={3}>
            <Grid item xs={12} lg={8}>
              <Card sx={{ 
                backgroundColor: '#1A1F2E',
                border: '1px solid #2A3441',
              }}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                      최근 세션
                    </Typography>
                    <Button
                      component={Link}
                      href="/challenges"
                      variant="outlined"
                      startIcon={<PlayArrow />}
                      sx={{
                        color: '#2196F3',
                        borderColor: '#2196F3',
                        '&:hover': {
                          backgroundColor: 'rgba(33, 150, 243, 0.08)',
                        }
                      }}
                    >
                      새 챌린지 시작
                    </Button>
                  </Box>

                  <TableContainer component={Paper} sx={{ backgroundColor: 'transparent' }}>
                    <Table>
                      <TableHead>
                        <TableRow sx={{ backgroundColor: '#0A0E18' }}>
                          <TableCell sx={{ color: '#78828A', fontWeight: 'bold', borderBottom: '1px solid #2A3441' }}>
                            챌린지
                          </TableCell>
                          <TableCell sx={{ color: '#78828A', fontWeight: 'bold', borderBottom: '1px solid #2A3441' }}>
                            상태
                          </TableCell>
                          <TableCell align="right" sx={{ color: '#78828A', fontWeight: 'bold', borderBottom: '1px solid #2A3441' }}>
                            수익률
                          </TableCell>
                          <TableCell align="right" sx={{ color: '#78828A', fontWeight: 'bold', borderBottom: '1px solid #2A3441' }}>
                            잔고
                          </TableCell>
                          <TableCell sx={{ color: '#78828A', fontWeight: 'bold', borderBottom: '1px solid #2A3441' }}>
                            시작일
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {recentSessions.map((session) => {
                          const percentage = formatPercentage(session.returnRate);
                          return (
                            <TableRow
                              key={session.id}
                              sx={{
                                '&:hover': {
                                  backgroundColor: 'rgba(33, 150, 243, 0.08)',
                                },
                                borderBottom: '1px solid #2A3441',
                              }}
                            >
                              <TableCell sx={{ color: '#FFFFFF' }}>
                                <Box>
                                  <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                    {session.challengeTitle}
                                  </Typography>
                                  {session.status === SessionStatus.ACTIVE && (
                                    <LinearProgress
                                      variant="determinate"
                                      value={session.progress}
                                      sx={{
                                        mt: 1,
                                        height: 4,
                                        backgroundColor: '#2A3441',
                                        '& .MuiLinearProgress-bar': {
                                          backgroundColor: '#2196F3',
                                        }
                                      }}
                                    />
                                  )}
                                </Box>
                              </TableCell>
                              
                              <TableCell sx={{ color: '#FFFFFF' }}>
                                <Chip
                                  label={getStatusText(session.status)}
                                  color={getStatusColor(session.status) as any}
                                  size="small"
                                  sx={{ fontWeight: 'bold' }}
                                />
                              </TableCell>
                              
                              <TableCell align="right">
                                <Box sx={{ 
                                  display: 'flex', 
                                  alignItems: 'center', 
                                  justifyContent: 'flex-end',
                                  gap: 0.5,
                                  color: percentage.color,
                                }}>
                                  {percentage.icon}
                                  <Typography 
                                    variant="body2" 
                                    sx={{ 
                                      fontFamily: 'monospace', 
                                      fontWeight: 'bold',
                                      color: percentage.color,
                                    }}
                                  >
                                    {percentage.value}
                                  </Typography>
                                </Box>
                              </TableCell>
                              
                              <TableCell align="right" sx={{ color: '#FFFFFF' }}>
                                <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                                  {formatCurrency(session.currentBalance)}
                                </Typography>
                              </TableCell>
                              
                              <TableCell sx={{ color: '#78828A' }}>
                                <Typography variant="body2">
                                  {formatDate(session.startedAt)}
                                </Typography>
                              </TableCell>
                            </TableRow>
                          );
                        })}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} lg={4}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                {/* 빠른 액션 */}
                <Card sx={{ 
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold', mb: 2 }}>
                      빠른 액션
                    </Typography>
                    
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Button
                        component={Link}
                        href="/challenges"
                        variant="contained"
                        fullWidth
                        startIcon={<PlayArrow />}
                        sx={{
                          backgroundColor: '#2196F3',
                          '&:hover': {
                            backgroundColor: '#1976D2',
                          }
                        }}
                      >
                        새 챌린지 시작
                      </Button>
                      
                      <Button
                        component={Link}
                        href="/leaderboard"
                        variant="outlined"
                        fullWidth
                        startIcon={<EmojiEvents />}
                        sx={{
                          color: '#78828A',
                          borderColor: '#2A3441',
                          '&:hover': {
                            borderColor: '#2196F3',
                            backgroundColor: 'rgba(33, 150, 243, 0.08)',
                          }
                        }}
                      >
                        리더보드 확인
                      </Button>
                      
                      <Button
                        component={Link}
                        href="/community"
                        variant="outlined"
                        fullWidth
                        startIcon={<Timeline />}
                        sx={{
                          color: '#78828A',
                          borderColor: '#2A3441',
                          '&:hover': {
                            borderColor: '#2196F3',
                            backgroundColor: 'rgba(33, 150, 243, 0.08)',
                          }
                        }}
                      >
                        커뮤니티 참여
                      </Button>

                      <Button
                        component={Link}
                        href="/dca-simulation"
                        variant="outlined"
                        fullWidth
                        startIcon={<Calculate />}
                        sx={{
                          color: '#4CAF50',
                          borderColor: '#4CAF50',
                          '&:hover': {
                            borderColor: '#66BB6A',
                            backgroundColor: 'rgba(76, 175, 80, 0.08)',
                          }
                        }}
                      >
                        DCA 시뮬레이션
                      </Button>
                    </Box>
                  </CardContent>
                </Card>

                {/* 리더보드 위젯 */}
                <GlobalLeaderboard />

                {/* 커뮤니티 위젯 */}
                <CommunityFeed challengeId={1} maxPosts={3} showCreatePost={false} />
              </Box>
            </Grid>
          </Grid>
        </Box>
      </Container>
    </Box>
  );
}