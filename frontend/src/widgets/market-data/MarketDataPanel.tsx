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
  ToggleButton,
  ToggleButtonGroup,
  Paper,
} from '@mui/material';
import { TrendingUp, TrendingDown, Speed, TableView, ShowChart } from '@mui/icons-material';
import { useGetChallengeDetail } from '@/shared/api/challenge-client';
import { ProfessionalTradingChart, CandlestickData } from '@/shared/ui/charts/ProfessionalTradingChart';
import { useRealTimeMarketData } from '@/shared/hooks/useRealTimeMarketData';

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
  const [viewMode, setViewMode] = React.useState<'table' | 'chart'>('table');
  const [selectedInstrument, setSelectedInstrument] = React.useState<string>('');

  // Fetch challenge data to get instruments
  const { data: challengeData, isLoading: loading } = useGetChallengeDetail(challengeId, {
    query: {
      enabled: !isNaN(challengeId) && challengeId > 0,
      refetchInterval: 10000, // Reduced frequency since we have real-time data
    }
  });

  // Real-time market data
  const {
    currentPrices,
    getInstrumentData,
    isConnected,
    lastUpdate,
    error: realTimeError,
  } = useRealTimeMarketData({
    instruments: challengeData?.instruments || [],
    updateInterval: 1000, // Update every second
    bufferSize: 100,
    enabled: !!challengeData?.instruments && challengeData.instruments.length > 0,
  });

  // Generate market data from real-time prices
  const marketData: MarketData[] = React.useMemo(() => {
    if (!challengeData?.instruments || challengeData.instruments.length === 0) {
      return [];
    }

    return challengeData.instruments.map((instrumentKey) => {
      const priceUpdate = currentPrices.get(instrumentKey);

      if (!priceUpdate) {
        // Fallback to default values if no real-time data yet
        const basePrice = 100 + (instrumentKey.charCodeAt(0) - 65) * 50;
        return {
          instrumentKey,
          hiddenName: `회사 ${instrumentKey}`,
          currentPrice: basePrice,
          openPrice: basePrice,
          highPrice: basePrice,
          lowPrice: basePrice,
          volume: 1000000,
          dailyChange: 0,
          dailyChangePercent: 0,
        };
      }

      // Use real-time data
      const instrumentData = getInstrumentData(instrumentKey);
      const todayData = instrumentData.slice(-24); // Last 24 data points (approx 1 day)

      const openPrice = todayData.length > 0 ? todayData[0].open : priceUpdate.price;
      const highPrice = todayData.length > 0 ? Math.max(...todayData.map(d => d.high)) : priceUpdate.price;
      const lowPrice = todayData.length > 0 ? Math.min(...todayData.map(d => d.low)) : priceUpdate.price;
      const dailyChange = priceUpdate.price - openPrice;
      const dailyChangePercent = openPrice > 0 ? (dailyChange / openPrice) * 100 : 0;

      return {
        instrumentKey,
        hiddenName: `회사 ${instrumentKey}`,
        currentPrice: priceUpdate.price,
        openPrice,
        highPrice,
        lowPrice,
        volume: priceUpdate.volume,
        dailyChange,
        dailyChangePercent,
      };
    });
  }, [challengeData?.instruments, currentPrices, getInstrumentData]);

  // Get candlestick data for selected instrument from real-time data
  const candlestickData = React.useMemo(() => {
    if (!selectedInstrument || !challengeData?.instruments) return [];
    return getInstrumentData(selectedInstrument);
  }, [selectedInstrument, challengeData?.instruments, getInstrumentData]);

  // No need for manual time updates since real-time data handles it

  // Set default selected instrument when data loads
  React.useEffect(() => {
    if (challengeData?.instruments && challengeData.instruments.length > 0 && !selectedInstrument) {
      setSelectedInstrument(challengeData.instruments[0]);
    }
  }, [challengeData?.instruments, selectedInstrument]);

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
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Typography variant="h6">
          시장 현황
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={(event, newViewMode) => {
              if (newViewMode !== null) {
                setViewMode(newViewMode);
              }
            }}
            size="small"
          >
            <ToggleButton value="table" aria-label="테이블 뷰">
              <TableView fontSize="small" />
            </ToggleButton>
            <ToggleButton value="chart" aria-label="차트 뷰">
              <ShowChart fontSize="small" />
            </ToggleButton>
          </ToggleButtonGroup>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
              sx={{
                width: 8,
                height: 8,
                borderRadius: '50%',
                backgroundColor: isConnected ? 'success.main' : 'error.main',
                animation: isConnected ? 'pulse 2s infinite' : 'none',
                '@keyframes pulse': {
                  '0%': { opacity: 1 },
                  '50%': { opacity: 0.5 },
                  '100%': { opacity: 1 },
                },
              }}
            />
            <Typography variant="caption" color="text.secondary">
              {isConnected ? '실시간 연결됨' : '연결 끊김'}
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* 실시간 연결 상태 */}
      <Box sx={{ mb: 3, p: 2, bgcolor: isConnected ? 'success.50' : 'error.50', borderRadius: 1 }}>
        <Typography variant="body2" fontWeight="medium">
          {isConnected ? '실시간 데이터 스트리밍 활성화' : '실시간 연결 끊김'}
          {lastUpdate && (
            <Typography component="span" sx={{ ml: 1, fontSize: '0.875rem', color: 'text.secondary' }}>
              • 마지막 업데이트: {lastUpdate.toLocaleTimeString()}
            </Typography>
          )}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {isConnected
            ? '실시간 시장 데이터가 1초마다 업데이트되고 있습니다'
            : '연결을 확인하고 다시 시도해주세요'
          }
          {realTimeError && (
            <Typography component="span" sx={{ ml: 1, color: 'error.main' }}>
              • 오류: {realTimeError}
            </Typography>
          )}
        </Typography>
      </Box>

      {loading ? (
        <LinearProgress />
      ) : viewMode === 'table' ? (
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>상품</TableCell>
                <TableCell align="right">현재가</TableCell>
                <TableCell align="right">등락</TableCell>
                <TableCell align="right">거래량</TableCell>
                <TableCell align="center">차트</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {marketData.map((data) => (
                <TableRow
                  key={data.instrumentKey}
                  sx={{
                    backgroundColor: selectedInstrument === data.instrumentKey ? 'action.selected' : 'transparent',
                    cursor: 'pointer',
                    '&:hover': {
                      backgroundColor: 'action.hover',
                    },
                  }}
                  onClick={() => setSelectedInstrument(data.instrumentKey)}
                >
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

                  <TableCell align="center">
                    <Chip
                      label={selectedInstrument === data.instrumentKey ? "선택됨" : "차트 보기"}
                      size="small"
                      variant={selectedInstrument === data.instrumentKey ? "filled" : "outlined"}
                      color={selectedInstrument === data.instrumentKey ? "primary" : "default"}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        // Chart view
        <Box>
          {selectedInstrument && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="h6" gutterBottom>
                {marketData.find(d => d.instrumentKey === selectedInstrument)?.hiddenName || selectedInstrument} - 실시간 차트
              </Typography>

              {/* Instrument selector for chart view */}
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                {challengeData?.instruments?.map((instrument) => (
                  <Chip
                    key={instrument}
                    label={`회사 ${instrument}`}
                    onClick={() => setSelectedInstrument(instrument)}
                    color={selectedInstrument === instrument ? 'primary' : 'default'}
                    variant={selectedInstrument === instrument ? 'filled' : 'outlined'}
                    size="small"
                  />
                ))}
              </Box>

              <Paper sx={{ p: 2 }}>
                <ProfessionalTradingChart
                  data={candlestickData}
                  width={800}
                  height={500}
                  showVolume={true}
                  showGrid={true}
                  showCrosshair={true}
                  realTimeUpdate={true}
                  theme="dark"
                />
              </Paper>
            </Box>
          )}
        </Box>
      )}

      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
        ⚠️ 회사명은 챌린지 종료 후 공개됩니다
      </Typography>
    </Box>
  );
}