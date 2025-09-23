'use client';

import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  LinearProgress,
  Chip,
  IconButton,
  Paper,
  Alert,
  useTheme,
  Collapse,
  Divider,
} from '@mui/material';
import {
  Speed,
  Warning,
  CheckCircle,
  Error,
  TrendingDown,
  ShowChart,
  ExpandMore,
  ExpandLess,
  Info,
  Security,
  Assessment,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import type { DCASimulationResponse } from '@/shared/api/types/dca-types';

interface MobileRiskMetricsProps {
  result: DCASimulationResponse;
  expandedSections: Set<string>;
  onToggleSection: (sectionId: string) => void;
}

interface RiskMetrics {
  volatility: number;
  sharpeRatio: number;
  maxDrawdown: number;
  beta: number;
  riskScore: number;
}

export default function MobileRiskMetrics({
  result,
  expandedSections,
  onToggleSection,
}: MobileRiskMetricsProps) {
  const theme = useTheme();

  const formatPercent = (percent: number) => {
    return `${percent.toFixed(1)}%`;
  };

  // Calculate risk metrics (mock implementation - replace with actual calculation)
  const calculateRiskMetrics = (): RiskMetrics => {
    // Mock calculation based on total return
    const volatility = Math.abs(result.totalReturnPercentage) * 0.8 + Math.random() * 10;
    const sharpeRatio = result.annualizedReturn / Math.max(volatility / 4, 1);
    const maxDrawdown = Math.abs(Math.min(0, result.totalReturnPercentage * 0.3 + (Math.random() - 0.5) * 10));
    const beta = 0.8 + Math.random() * 0.6; // Between 0.8 and 1.4

    // Risk score: weighted combination of metrics (0-100)
    const riskScore = Math.min(100, Math.max(0,
      (volatility * 0.4) +
      (maxDrawdown * 0.3) +
      (Math.max(0, 2 - sharpeRatio) * 20 * 0.2) +
      (Math.abs(beta - 1) * 50 * 0.1)
    ));

    return {
      volatility,
      sharpeRatio,
      maxDrawdown,
      beta,
      riskScore,
    };
  };

  const riskMetrics = calculateRiskMetrics();

  const getRiskLevel = (score: number) => {
    if (score <= 30) return { level: '낮음', color: 'success.main', icon: <CheckCircle /> };
    if (score <= 60) return { level: '보통', color: 'warning.main', icon: <Warning /> };
    return { level: '높음', color: 'error.main', icon: <Error /> };
  };

  const getVolatilityLevel = (volatility: number) => {
    if (volatility <= 15) return { level: '낮음', color: 'success.main' };
    if (volatility <= 25) return { level: '보통', color: 'warning.main' };
    return { level: '높음', color: 'error.main' };
  };

  const getSharpeLevel = (sharpe: number) => {
    if (sharpe >= 1) return { level: '우수', color: 'success.main' };
    if (sharpe >= 0) return { level: '양호', color: 'warning.main' };
    return { level: '부족', color: 'error.main' };
  };

  const getDrawdownLevel = (drawdown: number) => {
    if (drawdown <= 15) return { level: '안전', color: 'success.main' };
    if (drawdown <= 30) return { level: '주의', color: 'warning.main' };
    return { level: '위험', color: 'error.main' };
  };

  const riskLevel = getRiskLevel(riskMetrics.riskScore);
  const volatilityLevel = getVolatilityLevel(riskMetrics.volatility);
  const sharpeLevel = getSharpeLevel(riskMetrics.sharpeRatio);
  const drawdownLevel = getDrawdownLevel(riskMetrics.maxDrawdown);

  return (
    <Box sx={{ pb: 2 }}>
      {/* Overall Risk Score */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <Card
          sx={{
            mb: 3,
            background: `linear-gradient(135deg, ${riskLevel.color}, ${theme.palette.grey[800]})`,
            color: 'white',
            position: 'relative',
            overflow: 'hidden',
          }}
        >
          <CardContent sx={{ position: 'relative', zIndex: 1 }}>
            <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
              <Box>
                <Typography variant="h6" sx={{ opacity: 0.9 }}>
                  종합 위험 점수
                </Typography>
                <Chip
                  icon={riskLevel.icon}
                  label={`위험도 ${riskLevel.level}`}
                  size="small"
                  sx={{
                    mt: 1,
                    bgcolor: 'rgba(255,255,255,0.2)',
                    color: 'white',
                    '& .MuiChip-icon': { color: 'white' }
                  }}
                />
              </Box>
              <Security sx={{ fontSize: 40, opacity: 0.7 }} />
            </Box>

            <Typography variant="h2" fontWeight="bold" gutterBottom>
              {riskMetrics.riskScore.toFixed(0)}
              <Typography component="span" variant="h5" sx={{ opacity: 0.7, ml: 1 }}>
                /100
              </Typography>
            </Typography>

            <Box sx={{ mt: 2 }}>
              <LinearProgress
                variant="determinate"
                value={riskMetrics.riskScore}
                sx={{
                  height: 8,
                  borderRadius: 4,
                  bgcolor: 'rgba(255,255,255,0.2)',
                  '& .MuiLinearProgress-bar': {
                    bgcolor: 'white',
                    borderRadius: 4,
                  }
                }}
              />
              <Typography variant="caption" sx={{ mt: 1, display: 'block', opacity: 0.9 }}>
                0: 매우 안전 → 100: 매우 위험
              </Typography>
            </Box>
          </CardContent>

          {/* Background decoration */}
          <Box
            sx={{
              position: 'absolute',
              top: -30,
              right: -30,
              width: 80,
              height: 80,
              borderRadius: '50%',
              bgcolor: 'rgba(255,255,255,0.1)',
              zIndex: 0,
            }}
          />
        </Card>
      </motion.div>

      {/* Detailed Risk Metrics */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
      >
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box
              display="flex"
              justifyContent="space-between"
              alignItems="center"
              onClick={() => onToggleSection('detailed')}
              sx={{ cursor: 'pointer' }}
            >
              <Box display="flex" alignItems="center" gap={1}>
                <Assessment color="primary" />
                <Typography variant="h6" fontWeight="bold">
                  세부 위험 지표
                </Typography>
              </Box>
              <IconButton size="small">
                {expandedSections.has('detailed') ? <ExpandLess /> : <ExpandMore />}
              </IconButton>
            </Box>

            <Collapse in={expandedSections.has('detailed')}>
              <Box sx={{ mt: 2 }}>
                <Grid container spacing={2}>
                  {/* Volatility */}
                  <Grid item xs={12} sm={6}>
                    <Paper
                      sx={{
                        p: 2,
                        textAlign: 'center',
                        bgcolor: volatilityLevel.color,
                        color: 'white',
                        borderRadius: 2,
                      }}
                    >
                      <ShowChart sx={{ fontSize: 30, mb: 1 }} />
                      <Typography variant="h5" fontWeight="bold">
                        {formatPercent(riskMetrics.volatility)}
                      </Typography>
                      <Typography variant="caption" display="block">
                        변동성 (연간)
                      </Typography>
                      <Typography variant="caption" sx={{ opacity: 0.9 }}>
                        {volatilityLevel.level}
                      </Typography>
                    </Paper>
                  </Grid>

                  {/* Sharpe Ratio */}
                  <Grid item xs={12} sm={6}>
                    <Paper
                      sx={{
                        p: 2,
                        textAlign: 'center',
                        bgcolor: sharpeLevel.color,
                        color: 'white',
                        borderRadius: 2,
                      }}
                    >
                      <Speed sx={{ fontSize: 30, mb: 1 }} />
                      <Typography variant="h5" fontWeight="bold">
                        {riskMetrics.sharpeRatio.toFixed(2)}
                      </Typography>
                      <Typography variant="caption" display="block">
                        샤프 비율
                      </Typography>
                      <Typography variant="caption" sx={{ opacity: 0.9 }}>
                        {sharpeLevel.level}
                      </Typography>
                    </Paper>
                  </Grid>

                  {/* Max Drawdown */}
                  <Grid item xs={12} sm={6}>
                    <Paper
                      sx={{
                        p: 2,
                        textAlign: 'center',
                        bgcolor: drawdownLevel.color,
                        color: 'white',
                        borderRadius: 2,
                      }}
                    >
                      <TrendingDown sx={{ fontSize: 30, mb: 1 }} />
                      <Typography variant="h5" fontWeight="bold">
                        -{formatPercent(riskMetrics.maxDrawdown)}
                      </Typography>
                      <Typography variant="caption" display="block">
                        최대 낙폭
                      </Typography>
                      <Typography variant="caption" sx={{ opacity: 0.9 }}>
                        {drawdownLevel.level}
                      </Typography>
                    </Paper>
                  </Grid>

                  {/* Beta */}
                  <Grid item xs={12} sm={6}>
                    <Paper
                      sx={{
                        p: 2,
                        textAlign: 'center',
                        bgcolor: theme.palette.info.main,
                        color: 'white',
                        borderRadius: 2,
                      }}
                    >
                      <Assessment sx={{ fontSize: 30, mb: 1 }} />
                      <Typography variant="h5" fontWeight="bold">
                        {riskMetrics.beta.toFixed(2)}
                      </Typography>
                      <Typography variant="caption" display="block">
                        베타 (시장 연동성)
                      </Typography>
                      <Typography variant="caption" sx={{ opacity: 0.9 }}>
                        {riskMetrics.beta > 1 ? '시장보다 변동' : '시장보다 안정'}
                      </Typography>
                    </Paper>
                  </Grid>
                </Grid>
              </Box>
            </Collapse>
          </CardContent>
        </Card>
      </motion.div>

      {/* Risk Interpretation Guide */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.2 }}
      >
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box
              display="flex"
              justifyContent="space-between"
              alignItems="center"
              onClick={() => onToggleSection('guide')}
              sx={{ cursor: 'pointer' }}
            >
              <Box display="flex" alignItems="center" gap={1}>
                <Info color="primary" />
                <Typography variant="h6" fontWeight="bold">
                  위험 지표 가이드
                </Typography>
              </Box>
              <IconButton size="small">
                {expandedSections.has('guide') ? <ExpandLess /> : <ExpandMore />}
              </IconButton>
            </Box>

            <Collapse in={expandedSections.has('guide')}>
              <Box sx={{ mt: 2 }}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" color="primary" gutterBottom>
                    📊 변동성 (Volatility)
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    투자 수익률의 변동 정도를 나타냅니다. 낮을수록 안정적입니다.
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    • 낮음 (0-15%): 안정적 투자 • 보통 (15-25%): 적당한 위험 • 높음 (25%+): 고위험 고수익
                  </Typography>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" color="primary" gutterBottom>
                    ⚡ 샤프 비율 (Sharpe Ratio)
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    위험 대비 수익률을 나타냅니다. 1 이상이면 우수한 투자입니다.
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    • 우수 (1.0+): 위험 대비 좋은 수익 • 양호 (0-1.0): 평균적 성과 • 부족 (0 미만): 개선 필요
                  </Typography>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" color="primary" gutterBottom>
                    📉 최대 낙폭 (Max Drawdown)
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    투자 기간 중 최대 손실 구간을 나타냅니다. 낮을수록 안전합니다.
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    • 안전 (0-15%): 안정적 투자 • 주의 (15-30%): 주의 필요 • 위험 (30%+): 고위험
                  </Typography>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Box>
                  <Typography variant="subtitle2" color="primary" gutterBottom>
                    📈 베타 (Beta)
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    시장과의 연동성을 나타냅니다. 1이면 시장과 동일한 변동성을 가집니다.
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    • 베타 &gt; 1: 시장보다 큰 변동 • 베타 = 1: 시장과 동일 • 베타 &lt; 1: 시장보다 안정적
                  </Typography>
                </Box>
              </Box>
            </Collapse>
          </CardContent>
        </Card>
      </motion.div>

      {/* Risk Recommendation */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.3 }}
      >
        <Alert
          severity={riskMetrics.riskScore <= 30 ? 'success' : riskMetrics.riskScore <= 60 ? 'warning' : 'error'}
          sx={{ borderRadius: 2 }}
        >
          <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
            투자 위험도 평가
          </Typography>
          <Typography variant="body2">
            {riskMetrics.riskScore <= 30 && (
              "이 투자는 비교적 안전한 수준의 위험도를 보여줍니다. 안정적인 장기 투자에 적합합니다."
            )}
            {riskMetrics.riskScore > 30 && riskMetrics.riskScore <= 60 && (
              "이 투자는 보통 수준의 위험도를 보여줍니다. 적절한 분산투자를 고려해보세요."
            )}
            {riskMetrics.riskScore > 60 && (
              "이 투자는 높은 위험도를 보여줍니다. 투자 금액을 조절하거나 다른 자산과의 분산투자를 강력히 권장합니다."
            )}
          </Typography>
        </Alert>
      </motion.div>
    </Box>
  );
}