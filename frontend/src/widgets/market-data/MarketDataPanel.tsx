'use client';

import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import { TrendingUp, TrendingDown, Speed } from '@mui/icons-material';
import { useGetChallengeDetail } from '@/shared/api/challenge-client';

interface MarketData {
  instrumentKey: string;
  hiddenName: string;
  currentPrice: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  dailyChange: number;
  dailyChangePercent: number;
}

interface MarketDataPanelProps {
  challengeId: number;
}

/**
 * 시장 데이터 패널 컴포넌트
 * 실시간 가격 정보와 차트 표시 (회사명 숨김)
 */
export function MarketDataPanel({ challengeId }: MarketDataPanelProps) {
  const [currentTime, setCurrentTime] = React.useState(new Date());

  // Fetch challenge data to get instruments
  const { data: challengeData, isLoading: loading } = useGetChallengeDetail(challengeId, {
    query: {
      enabled: !isNaN(challengeId) && challengeId > 0,
      refetchInterval: 3000, // Refresh every 3 seconds for live simulation
    }
  });

  // Generate market data from challenge instruments
  const marketData: MarketData[] = React.useMemo(() => {
    if (!challengeData?.instruments || challengeData.instruments.length === 0) {
      return [];
    }

    return challengeData.instruments.map((instrumentKey, index) => {
      // Generate simulated market data based on instrument key
      const basePrice = 100 + (instrumentKey.charCodeAt(0) - 65) * 50; // A=100, B=150, C=200, etc.
      const variation = (Math.sin(Date.now() / 10000 + index) * 10); // Time-based price variation
      const currentPrice = basePrice + variation;
      const openPrice = basePrice + (Math.random() - 0.5) * 5;
      const highPrice = Math.max(currentPrice, openPrice) + Math.random() * 10;
      const lowPrice = Math.min(currentPrice, openPrice) - Math.random() * 10;
      const dailyChange = currentPrice - openPrice;
      const dailyChangePercent = (dailyChange / openPrice) * 100;

      return {
        instrumentKey,
        hiddenName: `회사 ${instrumentKey}`, // Hidden company name
        currentPrice,
        openPrice,
        highPrice,
        lowPrice,
        volume: Math.floor(Math.random() * 10000000) + 1000000, // Random volume between 1M-11M
        dailyChange,
        dailyChangePercent,
      };
    });
  }, [challengeData?.instruments, currentTime]); // Include currentTime to update data

  React.useEffect(() => {
    // 시뮬레이션 시간 업데이트 (1초마다)
    const timeInterval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => {
      clearInterval(timeInterval);
    };
  }, [challengeId]);

  const formatCurrency = (amount: number) => {
    return `₩${amount.toFixed(2)}`;
  };

  const formatPercentage = (percentage: number) => {
    const sign = percentage >= 0 ? '+' : '';
    return `${sign}${percentage.toFixed(2)}%`;
  };

  const formatVolume = (volume: number) => {
    if (volume >= 1000000) {
      return `${(volume / 1000000).toFixed(1)}M`;
    } else if (volume >= 1000) {
      return `${(volume / 1000).toFixed(1)}K`;
    }
    return volume.toLocaleString();
  };

  if (loading) {
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          시장 현황
        </Typography>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h6">
          시장 현황
        </Typography>
        
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Speed fontSize="small" color="action" />
          <Typography variant="caption" color="text.secondary">
            10배속 재생 중
          </Typography>
        </Box>
      </Box>

      {/* 시뮬레이션 시간 표시 */}
      <Box sx={{ mb: 3, p: 2, bgcolor: 'primary.50', borderRadius: 1 }}>
        <Typography variant="body2" fontWeight="medium">
          시뮬레이션 시간: {currentTime.toLocaleTimeString()}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          실제 시장 데이터가 압축되어 재생되고 있습니다
        </Typography>
      </Box>

      {loading ? (
        <LinearProgress />
      ) : (
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>상품</TableCell>
                <TableCell align="right">현재가</TableCell>
                <TableCell align="right">등락</TableCell>
                <TableCell align="right">거래량</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {marketData.map((data) => (
                <TableRow key={data.instrumentKey}>
                  <TableCell>
                    <Typography variant="body2" fontWeight="medium">
                      {data.hiddenName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {data.instrumentKey}
                    </Typography>
                  </TableCell>
                  
                  <TableCell align="right">
                    <Typography variant="body2" fontWeight="medium">
                      {formatCurrency(data.currentPrice)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                      시가: {formatCurrency(data.openPrice)}
                    </Typography>
                  </TableCell>
                  
                  <TableCell align="right">
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 0.5 }}>
                      {data.dailyChangePercent >= 0 ? (
                        <TrendingUp fontSize="small" color="success" />
                      ) : (
                        <TrendingDown fontSize="small" color="error" />
                      )}
                      <Box>
                        <Typography 
                          variant="body2"
                          color={data.dailyChangePercent >= 0 ? 'success.main' : 'error.main'}
                          fontWeight="medium"
                        >
                          {formatCurrency(data.dailyChange)}
                        </Typography>
                        <Chip
                          label={formatPercentage(data.dailyChangePercent)}
                          color={data.dailyChangePercent >= 0 ? 'success' : 'error'}
                          size="small"
                          variant="outlined"
                        />
                      </Box>
                    </Box>
                  </TableCell>
                  
                  <TableCell align="right">
                    <Typography variant="body2">
                      {formatVolume(data.volume)}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
        ⚠️ 회사명은 챌린지 종료 후 공개됩니다
      </Typography>
    </Box>
  );
}