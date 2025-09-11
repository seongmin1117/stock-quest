'use client';

import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Divider,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Speed as SpeedIcon,
  TrendingUp as TrendingUpIcon,
  Schedule as ScheduleIcon,
  Group as GroupIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';
import { ErrorBoundary, TableLoading } from '../../shared/components';
import { useErrorHandling } from '../../shared/hooks';

// 시뮬레이션 상태 타입
interface SimulationState {
  sessionId: number;
  challengeId: number;
  speedFactor: number;
  periodStart: string;
  periodEnd: string;
  currentSimulationDate: string;
  progress: string;
  isCompleted: boolean;
  simulationStartedAt: string;
  elapsedRealTimeMinutes: number;
  estimatedCompletionTime?: string;
  summary: string;
}

interface SimulationStatistics {
  activeSessions: number;
  averageProgress: string;
  speedFactorDistribution: Record<string, number>;
  averageElapsedMinutes: string;
  fastestSession?: {
    sessionId: number;
    progress: string;
  };
  slowestSession?: {
    sessionId: number;
    progress: string;
  };
}

interface SimulationStatesResponse {
  totalSessions: number;
  states: Record<string, SimulationState>;
}

/**
 * 시뮬레이션 모니터링 대시보드
 * 관리자용 실시간 시뮬레이션 상태 조회
 */
export function SimulationDashboard() {
  // 에러 처리 훅
  const { withErrorHandling } = useErrorHandling({
    onError: (error) => console.error('Simulation dashboard error:', error),
    autoRetry: true,
    retryConfig: { maxRetries: 3, retryDelay: 2000 },
  });

  // 시뮬레이션 통계 조회
  const {
    data: statistics,
    isLoading: statsLoading,
    error: statsError,
    refetch: refetchStats,
    isRefetching: statsRefetching,
  } = useQuery<SimulationStatistics>({
    queryKey: ['simulationStatistics'],
    queryFn: withErrorHandling(async () => {
      // Simulate network delay and potential errors
      await new Promise(resolve => setTimeout(resolve, Math.random() * 800 + 300));
      
      // Simulate occasional API failures
      if (Math.random() < 0.1) {
        throw new Error('시뮬레이션 통계 서버에 일시적인 문제가 발생했습니다.');
      }
      
      const response = await fetch('/api/admin/simulation/statistics');
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: 통계 조회 실패`);
      }
      return response.json();
    }),
    refetchInterval: 10000,
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(2000 * 2 ** attemptIndex, 10000),
    refetchOnWindowFocus: false,
    refetchOnReconnect: true,
  });

  // 모든 시뮬레이션 상태 조회
  const {
    data: allStates,
    isLoading: statesLoading,
    error: statesError,
    refetch: refetchStates,
    isRefetching: statesRefetching,
  } = useQuery<SimulationStatesResponse>({
    queryKey: ['simulationStates'],
    queryFn: withErrorHandling(async () => {
      // Simulate network delay and potential errors
      await new Promise(resolve => setTimeout(resolve, Math.random() * 600 + 200));
      
      // Simulate occasional API failures
      if (Math.random() < 0.08) {
        throw new Error('시뮬레이션 상태 서버에 일시적인 문제가 발생했습니다.');
      }
      
      const response = await fetch('/api/admin/simulation/states');
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: 상태 조회 실패`);
      }
      return response.json();
    }),
    refetchInterval: 5000,
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(2000 * 2 ** attemptIndex, 8000),
    refetchOnWindowFocus: false,
    refetchOnReconnect: true,
  });

  const handleRefresh = () => {
    refetchStats();
    refetchStates();
  };

  const getSpeedFactorColor = (factor: number) => {
    if (factor === 1) return 'default';
    if (factor <= 10) return 'primary';
    if (factor <= 50) return 'warning';
    return 'error';
  };

  const getProgressColor = (progress: string) => {
    const percent = parseFloat(progress);
    if (percent < 25) return 'error';
    if (percent < 75) return 'warning';
    return 'success';
  };

  if (statsLoading || statesLoading) {
    return (
      <Box sx={{ p: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h4" component="h1" fontWeight="bold">
            🎮 시뮬레이션 대시보드
          </Typography>
        </Box>
        
        {/* 로딩 스켈레톤 */}
        <Grid container spacing={3} mb={4}>
          {[...Array(4)].map((_, i) => (
            <Grid item xs={12} sm={6} md={3} key={i}>
              <Card elevation={2}>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} mb={2}>
                    <CircularProgress size={20} />
                    <Typography variant="body2" color="text.secondary">
                      로딩 중...
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
        
        <Card elevation={2}>
          <CardContent>
            <TableLoading rows={5} columns={8} />
          </CardContent>
        </Card>
      </Box>
    );
  }

  if (statsError || statesError) {
    return (
      <Box sx={{ p: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h4" component="h1" fontWeight="bold">
            🎮 시뮬레이션 대시보드
          </Typography>
        </Box>
        
        <Alert 
          severity="error" 
          action={
            <IconButton 
              color="inherit" 
              size="small" 
              onClick={handleRefresh}
              disabled={statsRefetching || statesRefetching}
            >
              <RefreshIcon />
            </IconButton>
          }
          sx={{ mb: 2 }}
        >
          <Typography variant="body2">
            {statsError instanceof Error ? statsError.message : 
             statesError instanceof Error ? statesError.message :
             '시뮬레이션 데이터 조회에 실패했습니다.'}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            네트워크 연결을 확인하고 새로고침을 시도해주세요.
          </Typography>
        </Alert>
        
        {/* 오류 상태에서도 기본 레이아웃 제공 */}
        <Card elevation={2}>
          <CardContent>
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="300px">
              <Typography variant="body2" color="text.secondary">
                데이터를 불러올 수 없습니다. 새로고침 버튼을 클릭해주세요.
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    );
  }

  const sessionStates = allStates?.states ? Object.entries(allStates.states) : [];

  return (
    <Box sx={{ p: 3 }}>
      {/* 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1" fontWeight="bold">
          🎮 시뮬레이션 대시보드
        </Typography>
        <Tooltip title={statsRefetching || statesRefetching ? "새로고침 중..." : "데이터 새로고침"}>
          <IconButton 
            onClick={handleRefresh} 
            color="primary"
            disabled={statsRefetching || statesRefetching}
          >
            <RefreshIcon 
              sx={{ 
                animation: (statsRefetching || statesRefetching) ? 'spin 1s linear infinite' : 'none',
                '@keyframes spin': {
                  '0%': { transform: 'rotate(0deg)' },
                  '100%': { transform: 'rotate(360deg)' },
                },
              }}
            />
          </IconButton>
        </Tooltip>
      </Box>

      {/* 통계 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <GroupIcon color="primary" sx={{ mr: 1 }} />
                <Typography variant="h6">활성 세션</Typography>
              </Box>
              <Typography variant="h3" color="primary" fontWeight="bold">
                {statistics?.activeSessions || 0}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                개의 세션이 실행 중
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <TrendingUpIcon color="success" sx={{ mr: 1 }} />
                <Typography variant="h6">평균 진행률</Typography>
              </Box>
              <Typography variant="h3" color="success.main" fontWeight="bold">
                {statistics?.averageProgress || '0.0%'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                전체 세션 평균
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <ScheduleIcon color="warning" sx={{ mr: 1 }} />
                <Typography variant="h6">평균 실행시간</Typography>
              </Box>
              <Typography variant="h3" color="warning.main" fontWeight="bold">
                {statistics?.averageElapsedMinutes ? `${statistics.averageElapsedMinutes}분` : '0분'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                실제 경과 시간
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <SpeedIcon color="info" sx={{ mr: 1 }} />
                <Typography variant="h6">Speed Factor</Typography>
              </Box>
              <Box display="flex" flexWrap="wrap" gap={0.5} mt={1}>
                {statistics?.speedFactorDistribution && 
                 Object.entries(statistics.speedFactorDistribution).map(([factor, count]) => (
                  <Chip
                    key={factor}
                    label={`${factor}x (${count})`}
                    size="small"
                    color={getSpeedFactorColor(parseInt(factor))}
                    variant="outlined"
                  />
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 상위/하위 세션 정보 */}
      {(statistics?.fastestSession || statistics?.slowestSession) && (
        <Grid container spacing={2} mb={4}>
          {statistics.fastestSession && (
            <Grid item xs={12} md={6}>
              <Alert severity="success">
                <Typography variant="subtitle2">
                  🚀 가장 빠른 진행: 세션 #{statistics.fastestSession.sessionId}
                </Typography>
                <Typography variant="body2">
                  진행률: {statistics.fastestSession.progress}
                </Typography>
              </Alert>
            </Grid>
          )}
          
          {statistics.slowestSession && (
            <Grid item xs={12} md={6}>
              <Alert severity="info">
                <Typography variant="subtitle2">
                  🐌 가장 느린 진행: 세션 #{statistics.slowestSession.sessionId}
                </Typography>
                <Typography variant="body2">
                  진행률: {statistics.slowestSession.progress}
                </Typography>
              </Alert>
            </Grid>
          )}
        </Grid>
      )}

      {/* 세션 상세 테이블 */}
      <Card elevation={2}>
        <CardContent>
          <Typography variant="h6" mb={2}>
            🔍 세션별 상세 현황 ({allStates?.totalSessions || 0}개)
          </Typography>

          {sessionStates.length === 0 ? (
            <Alert severity="info">
              현재 실행 중인 시뮬레이션 세션이 없습니다.
            </Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>세션 ID</TableCell>
                    <TableCell>챌린지 ID</TableCell>
                    <TableCell>진행률</TableCell>
                    <TableCell>Speed Factor</TableCell>
                    <TableCell>시뮬레이션 날짜</TableCell>
                    <TableCell>실행시간</TableCell>
                    <TableCell>예상완료</TableCell>
                    <TableCell>상태</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sessionStates.map(([sessionId, state]) => (
                    <TableRow key={sessionId} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          #{sessionId}
                        </Typography>
                      </TableCell>
                      
                      <TableCell>{state.challengeId}</TableCell>
                      
                      <TableCell>
                        <Box display="flex" alignItems="center" gap={1}>
                          <LinearProgress
                            variant="determinate"
                            value={parseFloat(state.progress)}
                            sx={{ width: 60, height: 6, borderRadius: 3 }}
                            color={getProgressColor(state.progress)}
                          />
                          <Typography variant="body2">
                            {state.progress}
                          </Typography>
                        </Box>
                      </TableCell>
                      
                      <TableCell>
                        <Chip
                          label={`${state.speedFactor}x`}
                          size="small"
                          color={getSpeedFactorColor(state.speedFactor)}
                        />
                      </TableCell>
                      
                      <TableCell>
                        <Typography variant="body2">
                          {format(new Date(state.currentSimulationDate), 'MM/dd', { locale: ko })}
                        </Typography>
                      </TableCell>
                      
                      <TableCell>
                        <Typography variant="body2">
                          {state.elapsedRealTimeMinutes}분
                        </Typography>
                      </TableCell>
                      
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {state.estimatedCompletionTime 
                            ? format(new Date(state.estimatedCompletionTime), 'HH:mm', { locale: ko })
                            : '계산 중'
                          }
                        </Typography>
                      </TableCell>
                      
                      <TableCell>
                        <Chip
                          label={state.isCompleted ? '완료' : '진행중'}
                          size="small"
                          color={state.isCompleted ? 'success' : 'primary'}
                          variant={state.isCompleted ? 'filled' : 'outlined'}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* 상태 표시 및 자동 갱신 안내 */}
      <Box mt={2} display="flex" justifyContent="space-between" alignItems="center">
        <Box display="flex" alignItems="center" gap={1}>
          {(statsRefetching || statesRefetching) && (
            <>
              <CircularProgress size={16} />
              <Typography variant="caption" color="primary">
                데이터 업데이트 중...
              </Typography>
            </>
          )}
        </Box>
        
        <Typography variant="caption" color="text.secondary">
          ⏱️ 데이터는 5-10초마다 자동 갱신됩니다
        </Typography>
      </Box>
      
      {/* 오프라인 상태 경고 */}
      {!navigator.onLine && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          <Typography variant="body2">
            네트워크 연결이 끊어졌습니다. 시뮬레이션 데이터가 실시간으로 업데이트되지 않을 수 있습니다.
          </Typography>
        </Alert>
      )}
    </Box>
  );
}