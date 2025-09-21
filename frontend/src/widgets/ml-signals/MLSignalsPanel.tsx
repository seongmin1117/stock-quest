'use client';

import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Chip,
  Button,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  IconButton,
  Tooltip,
  Alert,
  Stack,
  Divider,
} from '@mui/material';
import {
  Psychology,
  TrendingUp,
  TrendingDown,
  ShowChart,
  Refresh,
  FilterList,
  Info,
} from '@mui/icons-material';
import {
  useGetApiV1MlSignalsActive,
  usePostApiV1MlSignalsGenerateBatch,
} from '@/shared/api/generated/ml-시그널/ml-시그널';
import { useGetChallengeDetail } from '@/shared/api/challenge-client';
import type { TradingSignalResponse } from '@/shared/api/generated/model';

interface MLSignalsPanelProps {
  challengeId: number;
}

/**
 * ML 트레이딩 시그널 패널 컴포넌트
 * AI 기반 투자 시그널 분석 및 표시
 */
export function MLSignalsPanel({ challengeId }: MLSignalsPanelProps) {
  const [selectedMarketRegime, setSelectedMarketRegime] = React.useState<string>('');
  const [refreshing, setRefreshing] = React.useState(false);

  // Fetch challenge data to get instruments
  const { data: challengeData, isLoading: challengeLoading } = useGetChallengeDetail(challengeId, {
    query: {
      enabled: !isNaN(challengeId) && challengeId > 0,
    }
  });

  // Fetch active ML signals
  const { data: activeSignalsData, isLoading: signalsLoading, refetch: refetchSignals } = useGetApiV1MlSignalsActive(
    { limit: 10 },
    {
      query: {
        refetchInterval: 30000, // Refresh every 30 seconds
      }
    }
  );

  // Batch signal generation mutation
  const { mutate: generateBatchSignals, isPending: generatingSignals } = usePostApiV1MlSignalsGenerateBatch({
    mutation: {
      onSuccess: () => {
        refetchSignals();
        setRefreshing(false);
      },
      onError: (error) => {
        console.error('Failed to generate batch signals:', error);
        setRefreshing(false);
      },
    },
  });

  const handleGenerateSignals = async () => {
    if (!challengeData?.instruments || challengeData.instruments.length === 0) {
      return;
    }

    setRefreshing(true);
    generateBatchSignals({
      data: {
        symbols: challengeData.instruments,
      },
    });
  };

  const getSignalTypeIcon = (signalType: string) => {
    switch (signalType) {
      case 'BUY':
        return <TrendingUp color="success" />;
      case 'SELL':
        return <TrendingDown color="error" />;
      default:
        return <ShowChart color="action" />;
    }
  };

  const getSignalTypeColor = (signalType: string): 'success' | 'error' | 'default' => {
    switch (signalType) {
      case 'BUY':
        return 'success';
      case 'SELL':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStrengthColor = (strength: string): 'success' | 'warning' | 'default' => {
    switch (strength) {
      case 'STRONG':
        return 'success';
      case 'MODERATE':
        return 'warning';
      default:
        return 'default';
    }
  };

  const formatPercentage = (value: string) => {
    const num = parseFloat(value);
    return `${num >= 0 ? '+' : ''}${num.toFixed(1)}%`;
  };

  const formatPrice = (price: string) => {
    return `₩${parseFloat(price).toLocaleString()}`;
  };

  const signals = activeSignalsData?.activeSignals || [];
  const isLoading = challengeLoading || signalsLoading;

  if (isLoading) {
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          ML 트레이딩 시그널
        </Typography>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Psychology color="primary" />
          <Typography variant="h6">
            ML 트레이딩 시그널
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>시장 상황</InputLabel>
            <Select
              value={selectedMarketRegime}
              label="시장 상황"
              onChange={(e) => setSelectedMarketRegime(e.target.value)}
            >
              <MenuItem value="">전체</MenuItem>
              <MenuItem value="BULL">상승장</MenuItem>
              <MenuItem value="BEAR">하락장</MenuItem>
              <MenuItem value="SIDEWAYS">횡보장</MenuItem>
              <MenuItem value="VOLATILE">변동장</MenuItem>
            </Select>
          </FormControl>

          <Tooltip title="새 시그널 생성">
            <IconButton
              onClick={handleGenerateSignals}
              disabled={refreshing || generatingSignals}
              color="primary"
            >
              <Refresh />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* AI Status Banner */}
      <Alert
        severity="info"
        sx={{ mb: 2 }}
        icon={<Psychology />}
      >
        AI 기반 시그널 분석이 활성화되었습니다. 시그널은 실시간으로 업데이트됩니다.
      </Alert>

      {/* Loading State */}
      {(refreshing || generatingSignals) && (
        <Box sx={{ mb: 2 }}>
          <LinearProgress />
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            ML 모델이 새로운 시그널을 생성하고 있습니다...
          </Typography>
        </Box>
      )}

      {/* No Signals State */}
      {signals.length === 0 && !isLoading ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 4 }}>
            <Psychology sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" color="text.secondary" gutterBottom>
              활성 시그널이 없습니다
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              새로운 ML 시그널을 생성해보세요
            </Typography>
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={handleGenerateSignals}
              disabled={!challengeData?.instruments}
            >
              시그널 생성
            </Button>
          </CardContent>
        </Card>
      ) : (
        /* Signals Table */
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>상품</TableCell>
                <TableCell>시그널</TableCell>
                <TableCell align="right">신뢰도</TableCell>
                <TableCell align="right">예상수익률</TableCell>
                <TableCell align="right">목표가</TableCell>
                <TableCell align="center">상세정보</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {signals.map((signal: TradingSignalResponse) => (
                <TableRow key={signal.signalId}>
                  <TableCell>
                    <Box>
                      <Typography variant="body2" fontWeight="medium">
                        회사 {signal.symbol || 'N/A'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {signal.symbol || 'N/A'}
                      </Typography>
                    </Box>
                  </TableCell>

                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {getSignalTypeIcon(signal.signalType || '')}
                      <Stack spacing={0.5}>
                        <Chip
                          label={signal.signalType || 'UNKNOWN'}
                          color={getSignalTypeColor(signal.signalType || '')}
                          size="small"
                        />
                        <Chip
                          label={signal.strength || 'UNKNOWN'}
                          color={getStrengthColor(signal.strength || '')}
                          size="small"
                          variant="outlined"
                        />
                      </Stack>
                    </Box>
                  </TableCell>

                  <TableCell align="right">
                    <Typography variant="body2" fontWeight="medium">
                      {parseFloat(signal.confidence || '0').toFixed(1)}%
                    </Typography>
                  </TableCell>

                  <TableCell align="right">
                    <Typography
                      variant="body2"
                      fontWeight="medium"
                      color={parseFloat(signal.expectedReturn || '0') >= 0 ? 'success.main' : 'error.main'}
                    >
                      {formatPercentage(signal.expectedReturn || '0')}
                    </Typography>
                  </TableCell>

                  <TableCell align="right">
                    {signal.targetPrice ? (
                      <Typography variant="body2">
                        {formatPrice(signal.targetPrice || '0')}
                      </Typography>
                    ) : (
                      <Typography variant="body2" color="text.secondary">
                        -
                      </Typography>
                    )}
                  </TableCell>

                  <TableCell align="center">
                    <Tooltip title="시그널 상세 정보">
                      <IconButton size="small">
                        <Info fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Signal Statistics */}
      {signals.length > 0 && (
        <Box sx={{ mt: 3 }}>
          <Divider sx={{ mb: 2 }} />
          <Grid container spacing={2}>
            <Grid item xs={6} md={3}>
              <Card>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="success.main">
                    {signals.filter(s => s.signalType === 'BUY').length}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    매수 시그널
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={6} md={3}>
              <Card>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="error.main">
                    {signals.filter(s => s.signalType === 'SELL').length}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    매도 시그널
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={6} md={3}>
              <Card>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="primary.main">
                    {signals.filter(s => s.strength === 'STRONG').length}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    강한 시그널
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={6} md={3}>
              <Card>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Typography variant="h4">
                    {signals.length > 0 ? (signals.reduce((sum, s) => sum + parseFloat(s.confidence || '0'), 0) / signals.length).toFixed(1) : 0}%
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    평균 신뢰도
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>
      )}

      <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
        ⚠️ ML 시그널은 참고용이며, 투자 결정은 신중히 하시기 바랍니다.
      </Typography>
    </Box>
  );
}