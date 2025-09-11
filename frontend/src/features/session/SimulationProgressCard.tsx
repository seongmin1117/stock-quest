'use client';

import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  LinearProgress,
  Chip,
  Alert,
  IconButton,
  Tooltip,
  Grid,
  Divider,
  CircularProgress,
} from '@mui/material';
import {
  Speed as SpeedIcon,
  Schedule as ScheduleIcon,
  PlayArrow as PlayIcon,
  Pause as PauseIcon,
  Stop as StopIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';

interface SimulationProgressProps {
  sessionId: number;
  onSimulationComplete?: () => void;
}

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

interface SimulationStateResponse {
  sessionId: number;
  found: boolean;
  state?: SimulationState;
  message?: string;
}

/**
 * 시뮬레이션 진행 상태 카드
 * 세션 페이지에서 실시간 시뮬레이션 진행률 표시
 */
export function SimulationProgressCard({ sessionId, onSimulationComplete }: SimulationProgressProps) {
  // 세션별 시뮬레이션 상태 조회
  const {
    data: simulationResponse,
    isLoading,
    error,
    refetch,
    isRefetching,
  } = useQuery<SimulationStateResponse>({
    queryKey: ['simulationState', sessionId],
    queryFn: async () => {
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, Math.random() * 500 + 200));
      
      // Simulate occasional API failures
      if (Math.random() < 0.05) {
        throw new Error('시뮬레이션 서버에 일시적인 문제가 발생했습니다.');
      }
      
      // Mock API response
      const response = await fetch(`/api/admin/simulation/sessions/${sessionId}/state`);
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: 시뮬레이션 상태 조회 실패`);
      }
      return response.json();
    },
    refetchInterval: 5000,
    enabled: !!sessionId,
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
    refetchOnWindowFocus: false,
    refetchOnReconnect: true,
    staleTime: 2000,
  });

  // 시뮬레이션 완료 시 콜백 실행
  React.useEffect(() => {
    if (simulationResponse?.state?.isCompleted && onSimulationComplete) {
      onSimulationComplete();
    }
  }, [simulationResponse?.state?.isCompleted, onSimulationComplete]);

  if (isLoading) {
    return (
      <Card elevation={2}>
        <CardContent>
          <Box display="flex" alignItems="center" gap={1} mb={2}>
            <SpeedIcon color="primary" />
            <Typography variant="h6" fontWeight="bold">
              🎮 시뮬레이션 진행상황
            </Typography>
          </Box>
          
          <Box mb={2}>
            <LinearProgress sx={{ height: 6, borderRadius: 3 }} />
          </Box>
          
          <Box display="flex" alignItems="center" gap={1}>
            <Typography variant="body2" color="text.secondary">
              시뮬레이션 상태를 확인하고 있습니다...
            </Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (error || !simulationResponse?.found) {
    return (
      <Card elevation={2}>
        <CardContent>
          <Box display="flex" alignItems="center" gap={1} mb={2}>
            <SpeedIcon color="primary" />
            <Typography variant="h6" fontWeight="bold">
              🎮 시뮬레이션 진행상황
            </Typography>
          </Box>
          
          {error ? (
            <Alert 
              severity="error" 
              action={
                <IconButton 
                  color="inherit" 
                  size="small" 
                  onClick={() => refetch()}
                  disabled={isRefetching}
                >
                  <RefreshIcon />
                </IconButton>
              }
            >
              <Typography variant="body2">
                {error instanceof Error ? error.message : '시뮬레이션 상태 조회에 실패했습니다.'}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                네트워크 연결을 확인하고 다시 시도해주세요.
              </Typography>
            </Alert>
          ) : (
            <Alert severity="info" icon={<InfoIcon />}>
              <Typography variant="body2">
                {simulationResponse?.message || '아직 시뮬레이션이 시작되지 않았습니다.'}
              </Typography>
              <Typography variant="caption" color="text.secondary" mt={1}>
                챌린지가 시작되면 자동으로 시뮬레이션이 진행됩니다.
              </Typography>
            </Alert>
          )}
        </CardContent>
      </Card>
    );
  }

  const state = simulationResponse.state!;
  const progressPercent = parseFloat(state.progress);
  
  const getProgressColor = () => {
    if (progressPercent < 25) return 'error';
    if (progressPercent < 75) return 'warning';
    return 'success';
  };

  const getSpeedFactorColor = () => {
    if (state.speedFactor === 1) return 'default';
    if (state.speedFactor <= 10) return 'primary';
    if (state.speedFactor <= 50) return 'warning';
    return 'error';
  };

  const getStatusIcon = () => {
    if (state.isCompleted) return <StopIcon color="action" />;
    return <PlayIcon color="success" />;
  };

  const formatTimeRemaining = () => {
    if (!state.estimatedCompletionTime) return '계산 중';
    
    const now = new Date();
    const completion = new Date(state.estimatedCompletionTime);
    const diffMinutes = Math.round((completion.getTime() - now.getTime()) / (1000 * 60));
    
    if (diffMinutes < 0) return '곧 완료';
    if (diffMinutes < 60) return `약 ${diffMinutes}분 후`;
    
    const hours = Math.floor(diffMinutes / 60);
    const minutes = diffMinutes % 60;
    return `약 ${hours}시간 ${minutes}분 후`;
  };

  return (
    <Card elevation={2}>
      <CardContent>
        {/* 헤더 */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Box display="flex" alignItems="center" gap={1}>
            {getStatusIcon()}
            <Typography variant="h6" fontWeight="bold">
              🎮 시뮬레이션 진행상황
            </Typography>
          </Box>
          
          <Chip
            label={state.isCompleted ? '완료' : '진행중'}
            color={state.isCompleted ? 'success' : 'primary'}
            size="small"
          />
        </Box>

        {/* 진행률 표시 */}
        <Box mb={3}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
            <Typography variant="body2" color="text.secondary">
              전체 진행률
            </Typography>
            <Typography variant="h6" fontWeight="bold" color={`${getProgressColor()}.main`}>
              {state.progress}
            </Typography>
          </Box>
          
          <LinearProgress
            variant="determinate"
            value={progressPercent}
            color={getProgressColor()}
            sx={{ height: 8, borderRadius: 4 }}
          />
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* 상세 정보 */}
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                시뮬레이션 날짜
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {format(new Date(state.currentSimulationDate), 'yyyy년 MM월 dd일', { locale: ko })}
              </Typography>
            </Box>
          </Grid>

          <Grid item xs={6}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                가속 배율
              </Typography>
              <Box display="flex" alignItems="center" gap={1}>
                <SpeedIcon fontSize="small" color="action" />
                <Chip
                  label={`${state.speedFactor}x`}
                  size="small"
                  color={getSpeedFactorColor()}
                />
              </Box>
            </Box>
          </Grid>

          <Grid item xs={6}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                실행 시간
              </Typography>
              <Box display="flex" alignItems="center" gap={1}>
                <ScheduleIcon fontSize="small" color="action" />
                <Typography variant="body2">
                  {state.elapsedRealTimeMinutes}분
                </Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={6}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                {state.isCompleted ? '완료 시간' : '예상 완료'}
              </Typography>
              <Typography variant="body2" color={state.isCompleted ? 'success.main' : 'text.secondary'}>
                {state.isCompleted 
                  ? format(new Date(state.currentSimulationDate), 'MM/dd HH:mm', { locale: ko })
                  : formatTimeRemaining()
                }
              </Typography>
            </Box>
          </Grid>
        </Grid>

        {/* 시뮬레이션 기간 정보 */}
        <Box mt={2} p={2} bgcolor="grey.50" borderRadius={1}>
          <Typography variant="caption" color="text.secondary" mb={1} display="block">
            📅 시뮬레이션 기간
          </Typography>
          <Typography variant="body2">
            {format(new Date(state.periodStart), 'yyyy.MM.dd', { locale: ko })} ~ {' '}
            {format(new Date(state.periodEnd), 'yyyy.MM.dd', { locale: ko })}
          </Typography>
        </Box>

        {/* 완료 메시지 */}
        {state.isCompleted && (
          <Alert severity="success" sx={{ mt: 2 }}>
            <Typography variant="body2">
              🎉 시뮬레이션이 완료되었습니다! 
            </Typography>
            <Typography variant="caption" color="text.secondary">
              결과 페이지에서 최종 성과를 확인해보세요.
            </Typography>
          </Alert>
        )}

        {/* 자동 갱신 안내 및 상태 */}
        <Box mt={2} display="flex" justifyContent="space-between" alignItems="center">
          <Box display="flex" alignItems="center" gap={1}>
            {isRefetching && (
              <>
                <CircularProgress size={16} />
                <Typography variant="caption" color="primary">
                  업데이트 중...
                </Typography>
              </>
            )}
          </Box>
          
          <Typography variant="caption" color="text.secondary">
            ⏱️ 5초마다 자동 갱신됩니다
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
      </CardContent>
    </Card>
  );
}