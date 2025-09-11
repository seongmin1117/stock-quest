'use client';

import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
  LinearProgress,
  Divider,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  AccountBalance as BalanceIcon,
  ShowChart as ChartIcon,
  Assessment as AssessmentIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';

// 포트폴리오 평가 타입
interface PortfolioPosition {
  instrumentKey: string;
  quantity: number;
  averagePrice: number;
  marketPrice: number;
  positionValue: number;
  unrealizedPnL: number;
  realName?: string;
}

interface PortfolioEvaluation {
  sessionId: number;
  simulationDate: string;
  positionCount: number;
  totalValue: number;
  cashBalance: number;
  totalAssets: number;
  totalPnL: number;
  returnRate: number;
  positions: PortfolioPosition[];
  marketPrices: Record<string, number>;
}

interface PortfolioEvaluationProps {
  sessionId: number;
  challengeId?: number;
}

/**
 * 포트폴리오 평가 컴포넌트
 * 현재 보유 포지션과 시뮬레이션 날짜 기준 평가금액 표시
 */
export function PortfolioEvaluation({ sessionId, challengeId }: PortfolioEvaluationProps) {
  // 포트폴리오 평가 데이터 조회
  const {
    data: portfolio,
    isLoading,
    error,
    refetch,
    dataUpdatedAt,
    isRefetching,
  } = useQuery<PortfolioEvaluation>({
    queryKey: ['portfolioEvaluation', sessionId],
    queryFn: async () => {
      // Simulate network delay and potential errors
      await new Promise(resolve => setTimeout(resolve, Math.random() * 800 + 500));
      
      // Simulate occasional API failures
      if (Math.random() < 0.08) {
        throw new Error('포트폴리오 서버에 일시적인 문제가 발생했습니다.');
      }
      
      const positions: PortfolioPosition[] = [
        {
          instrumentKey: 'AAPL',
          quantity: 100,
          averagePrice: 145.50,
          marketPrice: 150.25 * (1 + (Math.random() - 0.5) * 0.02), // ±1% 램덤 변동
          positionValue: 15025,
          unrealizedPnL: 475,
          realName: 'Apple Inc.',
        },
        {
          instrumentKey: 'MSFT', 
          quantity: 50,
          averagePrice: 340.00,
          marketPrice: 355.75 * (1 + (Math.random() - 0.5) * 0.02),
          positionValue: 17787.5,
          unrealizedPnL: 787.5,
          realName: 'Microsoft Corporation',
        },
        {
          instrumentKey: 'GOOGL',
          quantity: 10,
          averagePrice: 2750.00,
          marketPrice: 2820.00 * (1 + (Math.random() - 0.5) * 0.02),
          positionValue: 28200,
          unrealizedPnL: 700,
          realName: 'Alphabet Inc.',
        },
      ];
      
      // Recalculate values based on updated market prices
      const updatedPositions = positions.map(pos => {
        const positionValue = pos.quantity * pos.marketPrice;
        const unrealizedPnL = (pos.marketPrice - pos.averagePrice) * pos.quantity;
        return {
          ...pos,
          positionValue,
          unrealizedPnL,
        };
      });
      
      const totalValue = updatedPositions.reduce((sum, pos) => sum + pos.positionValue, 0);
      const totalPnL = updatedPositions.reduce((sum, pos) => sum + pos.unrealizedPnL, 0);
      const cashBalance = 938037.5;
      const totalAssets = totalValue + cashBalance;
      const initialBalance = 1000000;
      const returnRate = ((totalAssets - initialBalance) / initialBalance) * 100;

      return {
        sessionId,
        simulationDate: new Date().toISOString().split('T')[0],
        positionCount: updatedPositions.length,
        totalValue,
        cashBalance,
        totalAssets,
        totalPnL,
        returnRate,
        positions: updatedPositions,
        marketPrices: updatedPositions.reduce((acc, pos) => {
          acc[pos.instrumentKey] = pos.marketPrice;
          return acc;
        }, {} as Record<string, number>),
      };
    },
    refetchInterval: 30000,
    enabled: !!sessionId,
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 15000),
    refetchOnWindowFocus: false,
    refetchOnReconnect: true,
    staleTime: 25000,
  });

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(value);
  };

  const formatPercent = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
  };

  const getTrendColor = (value: number) => {
    return value >= 0 ? 'success.main' : 'error.main';
  };

  const getTrendIcon = (value: number) => {
    return value >= 0 
      ? <TrendingUpIcon fontSize="small" color="success" />
      : <TrendingDownIcon fontSize="small" color="error" />;
  };

  if (isLoading) {
    return (
      <Box>
        <Box display="flex" alignItems="center" gap={1} mb={3}>
          <AssessmentIcon color="primary" />
          <Typography variant="h6" fontWeight="bold">
            📈 포트폴리오 평가
          </Typography>
        </Box>
        
        <Box display="flex" flex="column" alignItems="center" minHeight="300px" gap={2}>
          <CircularProgress size={60} />
          <Typography variant="body2" color="text.secondary">
            포트폴리오를 평가하고 있습니다...
          </Typography>
        </Box>
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Box display="flex" alignItems="center" gap={1} mb={3}>
          <AssessmentIcon color="primary" />
          <Typography variant="h6" fontWeight="bold">
            📈 포트폴리오 평가
          </Typography>
        </Box>
        
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
          sx={{ mb: 2 }}
        >
          <Typography variant="body2">
            {error instanceof Error ? error.message : '포트폴리오 평가 데이터 조회에 실패했습니다.'}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            네트워크 연결을 확인하고 다시 시도해주세요.
          </Typography>
        </Alert>
        
        {/* 오류 상태에서도 기본 레이아웃 제공 */}
        <Card elevation={1}>
          <CardContent>
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
              <Typography variant="body2" color="text.secondary">
                포트폴리오 데이터를 불러올 수 없습니다. 새로고침 버튼을 클릭해주세요.
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    );
  }

  if (!portfolio) {
    return (
      <Alert severity="info">
        아직 포트폴리오 데이터가 없습니다. 주문을 체결해보세요!
      </Alert>
    );
  }

  return (
    <Box>
      {/* 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box display="flex" alignItems="center" gap={1}>
          <AssessmentIcon color="primary" />
          <Typography variant="h6" fontWeight="bold">
            📊 포트폴리오 평가
          </Typography>
        </Box>
        
        <Box display="flex" alignItems="center" gap={1}>
          <Typography variant="caption" color="text.secondary">
            평가일: {format(new Date(portfolio.simulationDate), 'yyyy-MM-dd')}
          </Typography>
          <Tooltip title={isRefetching ? "새로고침 중..." : "데이터 새로고침"}>
            <IconButton 
              size="small" 
              onClick={() => refetch()} 
              disabled={isRefetching}
            >
              <RefreshIcon 
                fontSize="small" 
                sx={{ 
                  animation: isRefetching ? 'spin 1s linear infinite' : 'none',
                  '@keyframes spin': {
                    '0%': { transform: 'rotate(0deg)' },
                    '100%': { transform: 'rotate(360deg)' },
                  },
                }}
              />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* 요약 카드들 */}
      <Grid container spacing={3} mb={4}>
        {/* 총 자산 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <BalanceIcon color="primary" sx={{ mr: 1 }} />
                <Typography variant="subtitle2">총 자산</Typography>
              </Box>
              <Typography variant="h5" fontWeight="bold" color="primary.main">
                {formatCurrency(portfolio.totalAssets)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                현금 + 주식 평가금액
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* 수익률 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                {getTrendIcon(portfolio.returnRate)}
                <Typography variant="subtitle2" sx={{ ml: 1 }}>수익률</Typography>
              </Box>
              <Typography 
                variant="h5" 
                fontWeight="bold" 
                color={getTrendColor(portfolio.returnRate)}
              >
                {formatPercent(portfolio.returnRate)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {formatCurrency(portfolio.totalPnL)} {portfolio.totalPnL >= 0 ? '수익' : '손실'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* 주식 평가금액 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <ChartIcon color="info" sx={{ mr: 1 }} />
                <Typography variant="subtitle2">주식 평가금액</Typography>
              </Box>
              <Typography variant="h5" fontWeight="bold" color="info.main">
                {formatCurrency(portfolio.totalValue)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {portfolio.positionCount}개 종목 보유
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* 현금 잔고 */}
        <Grid item xs={12} sm={6} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <BalanceIcon color="success" sx={{ mr: 1 }} />
                <Typography variant="subtitle2">현금 잔고</Typography>
              </Box>
              <Typography variant="h5" fontWeight="bold" color="success.main">
                {formatCurrency(portfolio.cashBalance)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                투자 가능 자금
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 자산 배분 차트 (간단한 진행바로 표시) */}
      <Card elevation={1} sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="subtitle1" mb={2} fontWeight="medium">
            💎 자산 배분
          </Typography>
          
          <Box mb={2}>
            <Box display="flex" justifyContent="space-between" mb={1}>
              <Typography variant="body2">주식</Typography>
              <Typography variant="body2" fontWeight="medium">
                {((portfolio.totalValue / portfolio.totalAssets) * 100).toFixed(1)}%
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={(portfolio.totalValue / portfolio.totalAssets) * 100}
              color="primary"
              sx={{ height: 8, borderRadius: 4, mb: 1 }}
            />
          </Box>
          
          <Box>
            <Box display="flex" justifyContent="space-between" mb={1}>
              <Typography variant="body2">현금</Typography>
              <Typography variant="body2" fontWeight="medium">
                {((portfolio.cashBalance / portfolio.totalAssets) * 100).toFixed(1)}%
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={(portfolio.cashBalance / portfolio.totalAssets) * 100}
              color="success"
              sx={{ height: 8, borderRadius: 4 }}
            />
          </Box>
        </CardContent>
      </Card>

      {/* 보유 종목 상세 */}
      <Card elevation={1}>
        <CardContent>
          <Typography variant="subtitle1" mb={2} fontWeight="medium">
            📋 보유 종목 상세 ({portfolio.positionCount}개)
          </Typography>

          {portfolio.positions.length === 0 ? (
            <Alert severity="info">
              아직 보유한 종목이 없습니다. 첫 거래를 시작해보세요!
            </Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>종목</TableCell>
                    <TableCell align="right">보유수량</TableCell>
                    <TableCell align="right">평균단가</TableCell>
                    <TableCell align="right">현재가</TableCell>
                    <TableCell align="right">평가금액</TableCell>
                    <TableCell align="right">평가손익</TableCell>
                    <TableCell align="right">수익률</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {portfolio.positions.map((position) => {
                    const returnRate = ((position.marketPrice - position.averagePrice) / position.averagePrice) * 100;
                    
                    return (
                      <TableRow key={position.instrumentKey} hover>
                        <TableCell>
                          <Box>
                            <Typography variant="body2" fontWeight="medium">
                              {position.instrumentKey}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {position.realName || '알 수 없음'}
                            </Typography>
                          </Box>
                        </TableCell>
                        
                        <TableCell align="right">
                          <Typography variant="body2">
                            {position.quantity.toLocaleString()}주
                          </Typography>
                        </TableCell>
                        
                        <TableCell align="right">
                          <Typography variant="body2">
                            {formatCurrency(position.averagePrice)}
                          </Typography>
                        </TableCell>
                        
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight="medium">
                            {formatCurrency(position.marketPrice)}
                          </Typography>
                        </TableCell>
                        
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight="medium">
                            {formatCurrency(position.positionValue)}
                          </Typography>
                        </TableCell>
                        
                        <TableCell align="right">
                          <Box display="flex" alignItems="center" justifyContent="flex-end" gap={0.5}>
                            {getTrendIcon(position.unrealizedPnL)}
                            <Typography 
                              variant="body2" 
                              color={getTrendColor(position.unrealizedPnL)}
                              fontWeight="medium"
                            >
                              {formatCurrency(Math.abs(position.unrealizedPnL))}
                            </Typography>
                          </Box>
                        </TableCell>
                        
                        <TableCell align="right">
                          <Chip
                            label={formatPercent(returnRate)}
                            size="small"
                            color={returnRate >= 0 ? 'success' : 'error'}
                            variant="outlined"
                          />
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* 상태 표시 및 자동 갱신 안내 */}
      <Box mt={2} display="flex" justifyContent="space-between" alignItems="center">
        <Box display="flex" alignItems="center" gap={1}>
          {isRefetching && (
            <>
              <CircularProgress size={16} />
              <Typography variant="caption" color="primary">
                평가 업데이트 중...
              </Typography>
            </>
          )}
        </Box>
        
        <Typography variant="caption" color="text.secondary">
          ⏱️ 포트폴리오는 30초마다 자동 평가됩니다
        </Typography>
      </Box>
      
      {/* 오프라인 상태 경고 */}
      {!navigator.onLine && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          <Typography variant="body2">
            네트워크 연결이 끊어졌습니다. 포트폴리오 평가가 실시간으로 업데이트되지 않을 수 있습니다.
          </Typography>
        </Alert>
      )}
    </Box>
  );
}