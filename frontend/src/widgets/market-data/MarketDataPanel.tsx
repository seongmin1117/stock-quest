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
  const [marketData, setMarketData] = React.useState<MarketData[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [currentTime, setCurrentTime] = React.useState(new Date());

  React.useEffect(() => {
    loadMarketData();
    
    // 시뮬레이션 시간 업데이트 (1초마다)
    const timeInterval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    
    // 시장 데이터 업데이트 (3초마다 - 빠른 재생 시뮬레이션)
    const dataInterval = setInterval(loadMarketData, 3000);
    
    return () => {
      clearInterval(timeInterval);
      clearInterval(dataInterval);
    };
  }, [challengeId]);

  const loadMarketData = async () => {
    try {
      // 실제로는 실시간 가격 API를 호출하지만, 여기서는 모킹 데이터 생성
      const mockData: MarketData[] = [
        {
          instrumentKey: 'A',
          hiddenName: '회사 A',
          currentPrice: 120.50 + (Math.random() - 0.5) * 10,
          openPrice: 118.20,
          highPrice: 125.80,
          lowPrice: 116.90,
          volume: 1234567,
          dailyChange: 2.30,
          dailyChangePercent: 1.95,
        },
        {
          instrumentKey: 'B',
          hiddenName: '회사 B',
          currentPrice: 89.20 + (Math.random() - 0.5) * 5,
          openPrice: 87.50,
          highPrice: 92.10,
          lowPrice: 85.30,
          volume: 987654,
          dailyChange: 1.70,
          dailyChangePercent: 1.94,
        },
        {
          instrumentKey: 'C',
          hiddenName: '회사 C',
          currentPrice: 45.80 + (Math.random() - 0.5) * 3,
          openPrice: 47.20,
          highPrice: 48.50,
          lowPrice: 44.10,
          volume: 567890,
          dailyChange: -1.40,
          dailyChangePercent: -2.97,
        },
      ];
      
      setMarketData(mockData);
    } catch (err) {
      console.error('시장 데이터 로드 오류:', err);
    } finally {
      setLoading(false);
    }
  };

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