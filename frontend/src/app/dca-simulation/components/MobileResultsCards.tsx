'use client';

import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  IconButton,
  Divider,
  Paper,
  useTheme,
  Collapse,
  Button,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  MonetizationOn,
  Assessment,
  Timeline,
  CompareArrows,
  ExpandMore,
  ExpandLess,
  Info,
  CheckCircle,
  Warning,
  Error,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Company } from '@/shared/api/company-client';
import type { DCASimulationResponse, InvestmentFrequency } from '@/shared/api/types/dca-types';

interface MobileResultsCardsProps {
  result: DCASimulationResponse;
  selectedCompany: Company | null;
  expandedSections: Set<string>;
  onToggleSection: (sectionId: string) => void;
  startDate: string;
  endDate: string;
  frequency: InvestmentFrequency;
  monthlyInvestmentAmount: number;
}

export default function MobileResultsCards({
  result,
  selectedCompany,
  expandedSections,
  onToggleSection,
  startDate,
  endDate,
  frequency,
  monthlyInvestmentAmount,
}: MobileResultsCardsProps) {
  const theme = useTheme();

  const formatCurrency = (amount: number) => {
    return `₩${amount.toLocaleString()}`;
  };

  const formatPercent = (percent: number) => {
    return `${percent >= 0 ? '+' : ''}${percent.toFixed(1)}%`;
  };

  const getPerformanceColor = (value: number) => {
    if (value > 0) return theme.palette.success.main;
    if (value < 0) return theme.palette.error.main;
    return theme.palette.text.secondary;
  };

  const getPerformanceIcon = (value: number) => {
    return value >= 0 ? <TrendingUp /> : <TrendingDown />;
  };

  const getReturnCategory = (percent: number) => {
    if (percent >= 20) return { label: '매우 우수', color: 'success.main', icon: <CheckCircle /> };
    if (percent >= 10) return { label: '우수', color: 'success.light', icon: <CheckCircle /> };
    if (percent >= 5) return { label: '양호', color: 'warning.main', icon: <Info /> };
    if (percent >= 0) return { label: '보통', color: 'warning.light', icon: <Info /> };
    return { label: '손실', color: 'error.main', icon: <Error /> };
  };

  const returnCategory = getReturnCategory(result.totalReturnPercentage);

  return (
    <Box sx={{ pb: 2 }}>
      {/* Main Performance Card */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <Card
          sx={{
            mb: 3,
            background: `linear-gradient(135deg, ${getPerformanceColor(result.finalPortfolioValue - result.totalInvestmentAmount)}, ${getPerformanceColor(result.finalPortfolioValue - result.totalInvestmentAmount)}dd)`,
            color: 'white',
            position: 'relative',
            overflow: 'hidden',
          }}
        >
          <CardContent sx={{ position: 'relative', zIndex: 1 }}>
            <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
              <Box>
                <Typography variant="h6" sx={{ opacity: 0.9 }}>
                  {selectedCompany?.nameKr || result.symbol} DCA 결과
                </Typography>
                <Chip
                  icon={returnCategory.icon}
                  label={returnCategory.label}
                  size="small"
                  sx={{
                    mt: 1,
                    bgcolor: 'rgba(255,255,255,0.2)',
                    color: 'white',
                    '& .MuiChip-icon': { color: 'white' }
                  }}
                />
              </Box>
              {getPerformanceIcon(result.finalPortfolioValue - result.totalInvestmentAmount)}
            </Box>

            <Typography variant="h3" fontWeight="bold" gutterBottom>
              {formatPercent(result.totalReturnPercentage)}
            </Typography>

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  수익금액
                </Typography>
                <Typography variant="h6" fontWeight="bold">
                  {formatCurrency(result.finalPortfolioValue - result.totalInvestmentAmount)}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  연평균 수익률
                </Typography>
                <Typography variant="h6" fontWeight="bold">
                  {formatPercent(result.annualizedReturn)}
                </Typography>
              </Grid>
            </Grid>
          </CardContent>

          {/* Background decoration */}
          <Box
            sx={{
              position: 'absolute',
              top: -50,
              right: -50,
              width: 120,
              height: 120,
              borderRadius: '50%',
              bgcolor: 'rgba(255,255,255,0.1)',
              zIndex: 0,
            }}
          />
        </Card>
      </motion.div>

      {/* Investment Summary Section */}
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
              onClick={() => onToggleSection('investment')}
              sx={{ cursor: 'pointer' }}
            >
              <Box display="flex" alignItems="center" gap={1}>
                <MonetizationOn color="primary" />
                <Typography variant="h6" fontWeight="bold">
                  투자 현황
                </Typography>
              </Box>
              <IconButton size="small">
                {expandedSections.has('investment') ? <ExpandLess /> : <ExpandMore />}
              </IconButton>
            </Box>

            <Collapse in={expandedSections.has('investment')}>
              <Box sx={{ mt: 2 }}>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'primary.light', color: 'white' }}>
                      <Typography variant="caption">총 투자금액</Typography>
                      <Typography variant="h6" fontWeight="bold">
                        {formatCurrency(result.totalInvestmentAmount)}
                      </Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={6}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'success.light', color: 'white' }}>
                      <Typography variant="caption">최종 포트폴리오 가치</Typography>
                      <Typography variant="h6" fontWeight="bold">
                        {formatCurrency(result.finalPortfolioValue)}
                      </Typography>
                    </Paper>
                  </Grid>
                </Grid>

                <Divider sx={{ my: 2 }} />

                <Box>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    투자 설정
                  </Typography>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="body2">투자 주기</Typography>
                    <Chip
                      label={frequency === 'MONTHLY' ? '월간' : '주간'}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  </Box>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="body2">기간</Typography>
                    <Typography variant="body2" fontWeight="bold">
                      {startDate} ~ {endDate}
                    </Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2">
                      {frequency === 'MONTHLY' ? '월' : '주'} 투자금액
                    </Typography>
                    <Typography variant="body2" fontWeight="bold">
                      {formatCurrency(monthlyInvestmentAmount)}
                    </Typography>
                  </Box>
                </Box>
              </Box>
            </Collapse>
          </CardContent>
        </Card>
      </motion.div>

      {/* Market Comparison Section */}
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
              onClick={() => onToggleSection('comparison')}
              sx={{ cursor: 'pointer' }}
            >
              <Box display="flex" alignItems="center" gap={1}>
                <CompareArrows color="primary" />
                <Typography variant="h6" fontWeight="bold">
                  시장 대비 성과
                </Typography>
              </Box>
              <IconButton size="small">
                {expandedSections.has('comparison') ? <ExpandLess /> : <ExpandMore />}
              </IconButton>
            </Box>

            <Collapse in={expandedSections.has('comparison')}>
              <Box sx={{ mt: 2 }}>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Paper
                      sx={{
                        p: 2,
                        textAlign: 'center',
                        bgcolor: result.outperformanceVsSP500 >= 0 ? 'success.light' : 'error.light',
                        color: 'white'
                      }}
                    >
                      <Typography variant="caption">
                        {selectedCompany?.exchange === 'KRX' ? 'KOSPI 대비' : 'S&P 500 대비'}
                      </Typography>
                      <Typography variant="h6" fontWeight="bold">
                        {formatPercent(result.outperformanceVsSP500)}
                      </Typography>
                      <Box display="flex" alignItems="center" justifyContent="center" mt={1}>
                        {getPerformanceIcon(result.outperformanceVsSP500)}
                        <Typography variant="caption" sx={{ ml: 0.5 }}>
                          {result.outperformanceVsSP500 >= 0 ? '초과수익' : '저조'}
                        </Typography>
                      </Box>
                    </Paper>
                  </Grid>
                  <Grid item xs={6}>
                    <Paper
                      sx={{
                        p: 2,
                        textAlign: 'center',
                        bgcolor: result.outperformanceVsNASDAQ >= 0 ? 'success.light' : 'error.light',
                        color: 'white'
                      }}
                    >
                      <Typography variant="caption">
                        {selectedCompany?.exchange === 'KRX' ? 'KOSDAQ 대비' : 'NASDAQ 대비'}
                      </Typography>
                      <Typography variant="h6" fontWeight="bold">
                        {formatPercent(result.outperformanceVsNASDAQ)}
                      </Typography>
                      <Box display="flex" alignItems="center" justifyContent="center" mt={1}>
                        {getPerformanceIcon(result.outperformanceVsNASDAQ)}
                        <Typography variant="caption" sx={{ ml: 0.5 }}>
                          {result.outperformanceVsNASDAQ >= 0 ? '초과수익' : '저조'}
                        </Typography>
                      </Box>
                    </Paper>
                  </Grid>
                </Grid>

                <Box sx={{ mt: 2, p: 2, bgcolor: 'background.default', borderRadius: 1 }}>
                  <Typography variant="caption" color="text.secondary">
                    💡 <strong>해석 가이드:</strong> 양수는 시장 지수보다 더 좋은 성과를,
                    음수는 시장 지수보다 저조한 성과를 의미합니다.
                  </Typography>
                </Box>
              </Box>
            </Collapse>
          </CardContent>
        </Card>
      </motion.div>

      {/* Quick Actions */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.3 }}
      >
        <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>
            빠른 액션
          </Typography>
          <Grid container spacing={1}>
            <Grid item xs={4}>
              <Button
                variant="outlined"
                size="small"
                fullWidth
                sx={{ py: 1.5, fontSize: '0.75rem' }}
              >
                다른 종목
              </Button>
            </Grid>
            <Grid item xs={4}>
              <Button
                variant="outlined"
                size="small"
                fullWidth
                sx={{ py: 1.5, fontSize: '0.75rem' }}
              >
                기간 변경
              </Button>
            </Grid>
            <Grid item xs={4}>
              <Button
                variant="outlined"
                size="small"
                fullWidth
                sx={{ py: 1.5, fontSize: '0.75rem' }}
              >
                금액 변경
              </Button>
            </Grid>
          </Grid>
        </Paper>
      </motion.div>
    </Box>
  );
}