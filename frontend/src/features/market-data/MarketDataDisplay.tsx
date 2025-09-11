'use client';

import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
  LinearProgress,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Remove as NeutralIcon,
  ShowChart as ChartIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';

// 시장 데이터 타입
interface PriceCandle {
  ticker: string;
  date: string;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
  dailyReturn?: number;
  volatility?: number;
}

interface MarketDataProps {
  sessionId?: number;
  challengeId?: number;
  instruments?: string[];
}

/**
 * 실시간 시장 데이터 표시 컴포넌트
 */
export function MarketDataDisplay({ sessionId, challengeId, instruments }: MarketDataProps) {
  const [selectedInstruments] = React.useState<string[]>(
    instruments || ['AAPL', 'MSFT', 'GOOGL', 'TSLA', 'AMZN']
  );

  // 최신 시장 데이터 조회
  const {
    data: marketData,
    isLoading,
    error,
    refetch,
    dataUpdatedAt,
    isRefetching,
  } = useQuery<Record<string, PriceCandle>>({
    queryKey: ['marketData', selectedInstruments],
    queryFn: async () => {
      // Simulate network delay and potential errors
      await new Promise(resolve => setTimeout(resolve, Math.random() * 1000 + 500));
      
      // Simulate occasional API failures
      if (Math.random() < 0.1) {
        throw new Error('시장 데이터 서버에 일시적인 문제가 발생했습니다.');
      }
      
      const mockData: Record<string, PriceCandle> = {};
      
      for (const ticker of selectedInstruments) {
        const basePrice = getBasePriceForTicker(ticker);
        const change = (Math.random() - 0.5) * 0.1; // ±5% 변동
        
        mockData[ticker] = {
          ticker,
          date: new Date().toISOString().split('T')[0],
          openPrice: basePrice * (1 + change * 0.5),
          highPrice: basePrice * (1 + Math.abs(change)),
          lowPrice: basePrice * (1 - Math.abs(change)),
          closePrice: basePrice * (1 + change),
          volume: Math.floor(Math.random() * 10000000) + 1000000,
          dailyReturn: change * 100,
          volatility: Math.abs(change) * 100,
        };
      }
      
      return mockData;
    },
    refetchInterval: 30000,
    staleTime: 25000,
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    refetchOnWindowFocus: false,
    refetchOnReconnect: true,
  });

  const getBasePriceForTicker = (ticker: string): number => {
    const basePrices: Record<string, number> = {
      AAPL: 150.0,
      MSFT: 350.0,
      GOOGL: 2800.0,
      TSLA: 200.0,
      AMZN: 3000.0,
      META: 300.0,
      NVDA: 800.0,
      NFLX: 400.0,
    };
    
    return basePrices[ticker] || 100.0;
  };

  const getTrendIcon = (dailyReturn?: number) => {
    if (!dailyReturn) return <NeutralIcon fontSize="small" />;
    
    if (dailyReturn > 0.1) {
      return <TrendingUpIcon fontSize="small" color="success" />;
    } else if (dailyReturn < -0.1) {
      return <TrendingDownIcon fontSize="small" color="error" />;
    }
    
    return <NeutralIcon fontSize="small" color="disabled" />;
  };

  const getTrendColor = (dailyReturn?: number) => {
    if (!dailyReturn) return 'text.secondary';
    return dailyReturn > 0 ? 'success.main' : 'error.main';
  };

  const getVolatilityColor = (volatility?: number) => {
    if (!volatility) return 'default';
    if (volatility < 2) return 'success';
    if (volatility < 5) return 'warning';
    return 'error';
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(price);
  };

  const formatVolume = (volume: number) => {
    if (volume >= 1000000) {
      return `${(volume / 1000000).toFixed(1)}M`;
    } else if (volume >= 1000) {
      return `${(volume / 1000).toFixed(0)}K`;
    }
    return volume.toString();
  };

  if (isLoading) {
    return (
      <Box>
        <Box display="flex" alignItems="center" gap={1} mb={2}>
          <ChartIcon color="primary" />
          <Typography variant="h6" fontWeight="bold">
            📈 실시간 시장 데이터
          </Typography>
        </Box>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
          <Box textAlign="center">
            <CircularProgress size={40} />
            <Typography variant="body2" color="text.secondary" mt={2}>
              시장 데이터를 불러오는 중...
            </Typography>
          </Box>
        </Box>
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Box display="flex" alignItems="center" gap={1} mb={2}>
          <ChartIcon color="primary" />
          <Typography variant="h6" fontWeight="bold">
            📈 실시간 시장 데이터
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
            {error instanceof Error ? error.message : '시장 데이터 조회에 실패했습니다.'}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            네트워크 연결을 확인하고 새로고침을 시도해주세요.
          </Typography>
        </Alert>
        
        {/* 오류 상태에서도 기본 레이아웃 제공 */}
        <Card elevation={1}>
          <CardContent>
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="150px">
              <Typography variant="body2" color="text.secondary">
                데이터를 불러올 수 없습니다. 새로고침 버튼을 클릭해주세요.
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    );
  }

  const dataEntries = marketData ? Object.entries(marketData) : [];

  return (
    <Box>
      {/* 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box display="flex" alignItems="center" gap={1}>
          <ChartIcon color="primary" />
          <Typography variant="h6" fontWeight="bold">
            📈 실시간 시장 데이터
          </Typography>
        </Box>
        
        <Box display="flex" alignItems="center" gap={1}>
          <Typography variant="caption" color="text.secondary">
            마지막 업데이트: {format(dataUpdatedAt, 'HH:mm:ss')}
          </Typography>
          <Tooltip title={isRefetching ? "새로고침 중..." : "데이터 새로고침"}>
            <IconButton size="small" onClick={() => refetch()} disabled={isRefetching}>
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

      {/* 요약 카드 */}
      <Grid container spacing={2} mb={3}>
        {dataEntries.slice(0, 3).map(([ticker, data]) => (
          <Grid item xs={12} sm={4} key={ticker}>
            <Card elevation={1}>
              <CardContent sx={{ pb: 2 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <Typography variant="subtitle1" fontWeight="bold">
                    {ticker}
                  </Typography>
                  {getTrendIcon(data.dailyReturn)}
                </Box>
                
                <Typography variant="h6" color={getTrendColor(data.dailyReturn)} mb={1}>
                  {formatPrice(data.closePrice)}
                </Typography>
                
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Typography variant="body2" color={getTrendColor(data.dailyReturn)}>
                    {data.dailyReturn ? `${data.dailyReturn.toFixed(2)}%` : 'N/A'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Vol: {formatVolume(data.volume)}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* 상세 테이블 */}
      <Card elevation={1}>
        <CardContent>
          <Typography variant="subtitle1" mb={2} fontWeight="medium">
            전체 종목 현황
          </Typography>
          
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>종목</TableCell>
                  <TableCell align="right">현재가</TableCell>
                  <TableCell align="right">등락률</TableCell>
                  <TableCell align="right">시가</TableCell>
                  <TableCell align="right">고가/저가</TableCell>
                  <TableCell align="right">거래량</TableCell>
                  <TableCell align="center">변동성</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {dataEntries.map(([ticker, data]) => (
                  <TableRow key={ticker} hover>
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={1}>
                        {getTrendIcon(data.dailyReturn)}
                        <Typography variant="body2" fontWeight="medium">
                          {ticker}
                        </Typography>
                      </Box>
                    </TableCell>
                    
                    <TableCell align="right">
                      <Typography 
                        variant="body2" 
                        color={getTrendColor(data.dailyReturn)}
                        fontWeight="medium"
                      >
                        {formatPrice(data.closePrice)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell align="right">
                      <Typography 
                        variant="body2" 
                        color={getTrendColor(data.dailyReturn)}
                        fontWeight="medium"
                      >
                        {data.dailyReturn ? `${data.dailyReturn.toFixed(2)}%` : 'N/A'}
                      </Typography>
                    </TableCell>
                    
                    <TableCell align="right">
                      <Typography variant="body2">
                        {formatPrice(data.openPrice)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell align="right">
                      <Typography variant="body2" color="text.secondary">
                        {formatPrice(data.highPrice)} / {formatPrice(data.lowPrice)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell align="right">
                      <Typography variant="body2">
                        {formatVolume(data.volume)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell align="center">
                      <Chip
                        label={data.volatility ? `${data.volatility.toFixed(1)}%` : 'N/A'}
                        size="small"
                        color={getVolatilityColor(data.volatility)}
                        variant="outlined"
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* 상태 표시 및 자동 갱신 안내 */}
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
          ⏱️ 데이터는 30초마다 자동 갱신됩니다
        </Typography>
      </Box>
      
      {/* 오프라인 상태 경고 */}
      {!navigator.onLine && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          <Typography variant="body2">
            네트워크 연결이 끊어졌습니다. 연결이 복구되면 자동으로 데이터를 새로고침합니다.
          </Typography>
        </Alert>
      )}
    </Box>
  );
}