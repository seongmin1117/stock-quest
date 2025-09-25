'use client';

import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  ToggleButton,
  ToggleButtonGroup,
  Chip,
  IconButton,
  Paper,
  useTheme,
  Tooltip,
} from '@mui/material';
import {
  ZoomIn,
  ZoomOut,
  Fullscreen,
  Timeline,
  TrendingUp,
  ShowChart,
  Info,
  TouchApp,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';
import { motion } from 'framer-motion';
import { Company } from '@/shared/api/company-client';
import type { DCASimulationResponse, InvestmentFrequency } from '@/shared/api/types/dca-types';

interface MobileChartProps {
  result: DCASimulationResponse;
  selectedCompany: any; // Company type from API
  startDate: string;
  endDate: string;
  frequency: InvestmentFrequency;
  monthlyInvestmentAmount: number;
}

type ChartType = 'line' | 'area' | 'comparison';
type TimeRange = 'all' | '1y' | '6m' | '3m';

export default function MobileChart({ result, selectedCompany, startDate, endDate, frequency, monthlyInvestmentAmount }: MobileChartProps) {
  const theme = useTheme();
  const [chartType, setChartType] = useState<ChartType>('area');
  const [timeRange, setTimeRange] = useState<TimeRange>('all');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const chartRef = useRef<HTMLDivElement>(null);

  const formatCurrency = (amount: number) => {
    if (amount >= 1000000) {
      return `₩${(amount / 1000000).toFixed(1)}M`;
    }
    if (amount >= 1000) {
      return `₩${(amount / 1000).toFixed(0)}K`;
    }
    return `₩${amount.toLocaleString()}`;
  };

  // Generate mock chart data (replace with actual data from result.investmentRecords)
  const generateChartData = () => {
    const months = Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 60 * 60 * 24 * 30));
    const monthlyGrowth = Math.pow(result.finalPortfolioValue / result.totalInvestmentAmount, 1 / months) - 1;

    const data: Array<{
      date: string;
      dateFormatted: string;
      portfolioValue: number;
      cumulativeInvestment: number;
      benchmark1: number;
      benchmark2: number;
      profit: number;
    }> = [];
    let cumulativeInvestment = 0;
    let portfolioValue = 0;
    let benchmark1Value = 0;
    let benchmark2Value = 0;

    for (let i = 0; i <= months; i++) {
      const date = new Date(startDate);
      date.setMonth(date.getMonth() + i);

      if (i > 0) {
        cumulativeInvestment += monthlyInvestmentAmount;
        portfolioValue = cumulativeInvestment * (1 + monthlyGrowth * i + (Math.random() - 0.5) * 0.1);

        // Mock benchmark data
        const sp500Growth = 0.007; // ~8.4% annually
        const nasdaqGrowth = 0.009; // ~11.4% annually
        benchmark1Value = cumulativeInvestment * (1 + sp500Growth * i);
        benchmark2Value = cumulativeInvestment * (1 + nasdaqGrowth * i);
      }

      data.push({
        date: date.toISOString().split('T')[0],
        dateFormatted: date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short' }),
        portfolioValue: Math.max(0, portfolioValue),
        cumulativeInvestment,
        benchmark1: Math.max(0, benchmark1Value),
        benchmark2: Math.max(0, benchmark2Value),
        profit: Math.max(0, portfolioValue - cumulativeInvestment),
      });
    }

    return data;
  };

  const chartData = generateChartData();

  // Filter data based on time range
  const getFilteredData = () => {
    if (timeRange === 'all') return chartData;

    const monthsToShow = {
      '1y': 12,
      '6m': 6,
      '3m': 3,
    }[timeRange];

    return chartData.slice(-monthsToShow);
  };

  const filteredData = getFilteredData();

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <Paper sx={{ p: 2, bgcolor: 'rgba(0,0,0,0.9)', color: 'white', minWidth: 200 }}>
          <Typography variant="caption" sx={{ mb: 1, display: 'block' }}>
            {label}
          </Typography>
          {payload.map((entry: any, index: number) => (
            <Box key={index} display="flex" justifyContent="space-between" alignItems="center">
              <Box display="flex" alignItems="center" gap={1}>
                <Box
                  sx={{
                    width: 8,
                    height: 8,
                    borderRadius: '50%',
                    bgcolor: entry.color,
                  }}
                />
                <Typography variant="caption">
                  {entry.name === 'portfolioValue' ? '포트폴리오' :
                   entry.name === 'cumulativeInvestment' ? '누적 투자' :
                   entry.name === 'benchmark1' ? (selectedCompany?.exchange === 'KRX' ? 'KOSPI' : 'S&P 500') :
                   entry.name === 'benchmark2' ? (selectedCompany?.exchange === 'KRX' ? 'KOSDAQ' : 'NASDAQ') :
                   entry.name}
                </Typography>
              </Box>
              <Typography variant="caption" fontWeight="bold">
                {formatCurrency(entry.value)}
              </Typography>
            </Box>
          ))}
        </Paper>
      );
    }
    return null;
  };

  const renderChart = () => {
    const commonProps = {
      data: filteredData,
      margin: { top: 20, right: 30, left: 20, bottom: 20 },
    };

    const chartHeight = isFullscreen ? window.innerHeight - 200 : 300;

    if (chartType === 'area') {
      return (
        <ResponsiveContainer width="100%" height={chartHeight}>
          <AreaChart {...commonProps}>
            <defs>
              <linearGradient id="portfolioGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={theme.palette.primary.main} stopOpacity={0.8} />
                <stop offset="95%" stopColor={theme.palette.primary.main} stopOpacity={0.1} />
              </linearGradient>
              <linearGradient id="investmentGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={theme.palette.grey[500]} stopOpacity={0.6} />
                <stop offset="95%" stopColor={theme.palette.grey[500]} stopOpacity={0.1} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
            <XAxis
              dataKey="dateFormatted"
              stroke={theme.palette.text.secondary}
              fontSize={12}
              interval="preserveStartEnd"
            />
            <YAxis
              stroke={theme.palette.text.secondary}
              fontSize={12}
              tickFormatter={formatCurrency}
            />
            <RechartsTooltip content={<CustomTooltip />} />
            <Area
              type="monotone"
              dataKey="cumulativeInvestment"
              stroke={theme.palette.grey[500]}
              fill="url(#investmentGradient)"
              strokeWidth={2}
              name="누적 투자"
            />
            <Area
              type="monotone"
              dataKey="portfolioValue"
              stroke={theme.palette.primary.main}
              fill="url(#portfolioGradient)"
              strokeWidth={3}
              name="포트폴리오"
            />
          </AreaChart>
        </ResponsiveContainer>
      );
    }

    if (chartType === 'comparison') {
      return (
        <ResponsiveContainer width="100%" height={chartHeight}>
          <LineChart {...commonProps}>
            <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
            <XAxis
              dataKey="dateFormatted"
              stroke={theme.palette.text.secondary}
              fontSize={12}
              interval="preserveStartEnd"
            />
            <YAxis
              stroke={theme.palette.text.secondary}
              fontSize={12}
              tickFormatter={formatCurrency}
            />
            <RechartsTooltip content={<CustomTooltip />} />
            <Legend />
            <Line
              type="monotone"
              dataKey="portfolioValue"
              stroke={theme.palette.primary.main}
              strokeWidth={3}
              dot={false}
              name="포트폴리오"
            />
            <Line
              type="monotone"
              dataKey="benchmark1"
              stroke={theme.palette.success.main}
              strokeWidth={2}
              strokeDasharray="5 5"
              dot={false}
              name={selectedCompany?.exchange === 'KRX' ? 'KOSPI' : 'S&P 500'}
            />
            <Line
              type="monotone"
              dataKey="benchmark2"
              stroke={theme.palette.warning.main}
              strokeWidth={2}
              strokeDasharray="5 5"
              dot={false}
              name={selectedCompany?.exchange === 'KRX' ? 'KOSDAQ' : 'NASDAQ'}
            />
          </LineChart>
        </ResponsiveContainer>
      );
    }

    // Default line chart
    return (
      <ResponsiveContainer width="100%" height={chartHeight}>
        <LineChart {...commonProps}>
          <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
          <XAxis
            dataKey="dateFormatted"
            stroke={theme.palette.text.secondary}
            fontSize={12}
            interval="preserveStartEnd"
          />
          <YAxis
            stroke={theme.palette.text.secondary}
            fontSize={12}
            tickFormatter={formatCurrency}
          />
          <RechartsTooltip content={<CustomTooltip />} />
          <Line
            type="monotone"
            dataKey="portfolioValue"
            stroke={theme.palette.primary.main}
            strokeWidth={3}
            dot={false}
            name="포트폴리오"
          />
          <Line
            type="monotone"
            dataKey="cumulativeInvestment"
            stroke={theme.palette.grey[500]}
            strokeWidth={2}
            strokeDasharray="5 5"
            dot={false}
            name="누적 투자"
          />
        </LineChart>
      </ResponsiveContainer>
    );
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      <Card>
        <CardContent sx={{ pb: 1 }}>
          {/* Chart Header */}
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Box display="flex" alignItems="center" gap={1}>
              <Timeline color="primary" />
              <Typography variant="h6" fontWeight="bold">
                투자 성과 추이
              </Typography>
              <Tooltip title="차트를 터치하여 자세한 정보를 확인하세요">
                <Info fontSize="small" color="action" />
              </Tooltip>
            </Box>
            <IconButton
              size="small"
              onClick={() => setIsFullscreen(!isFullscreen)}
              sx={{ bgcolor: theme.palette.action.hover }}
            >
              <Fullscreen />
            </IconButton>
          </Box>

          {/* Chart Type Controls */}
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2} gap={1}>
            <ToggleButtonGroup
              value={chartType}
              exclusive
              onChange={(e, newType) => newType && setChartType(newType)}
              size="small"
            >
              <ToggleButton value="area" sx={{ px: 1.5 }}>
                <ShowChart sx={{ mr: 0.5, fontSize: '1rem' }} />
                <Typography variant="caption">영역</Typography>
              </ToggleButton>
              <ToggleButton value="line" sx={{ px: 1.5 }}>
                <Timeline sx={{ mr: 0.5, fontSize: '1rem' }} />
                <Typography variant="caption">선형</Typography>
              </ToggleButton>
              <ToggleButton value="comparison" sx={{ px: 1.5 }}>
                <TrendingUp sx={{ mr: 0.5, fontSize: '1rem' }} />
                <Typography variant="caption">비교</Typography>
              </ToggleButton>
            </ToggleButtonGroup>

            <ToggleButtonGroup
              value={timeRange}
              exclusive
              onChange={(e, newRange) => newRange && setTimeRange(newRange)}
              size="small"
            >
              <ToggleButton value="3m">3개월</ToggleButton>
              <ToggleButton value="6m">6개월</ToggleButton>
              <ToggleButton value="1y">1년</ToggleButton>
              <ToggleButton value="all">전체</ToggleButton>
            </ToggleButtonGroup>
          </Box>

          {/* Performance Indicators */}
          <Box display="flex" gap={1} mb={2} sx={{ overflowX: 'auto', pb: 1 }}>
            <Chip
              icon={<TrendingUp />}
              label={`총 수익률 ${result.totalReturnPercentage.toFixed(1)}%`}
              color={result.totalReturnPercentage >= 0 ? 'success' : 'error'}
              size="small"
              variant="outlined"
            />
            <Chip
              label={`연평균 ${result.annualizedReturn.toFixed(1)}%`}
              color="primary"
              size="small"
              variant="outlined"
            />
            <Chip
              label={`최종 가치 ${formatCurrency(result.finalPortfolioValue)}`}
              color="info"
              size="small"
              variant="outlined"
            />
          </Box>
        </CardContent>

        {/* Chart Container */}
        <Box
          ref={chartRef}
          sx={{
            position: 'relative',
            px: 1,
            ...(isFullscreen && {
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              zIndex: 9999,
              bgcolor: 'background.paper',
              p: 2,
            }),
          }}
        >
          {renderChart()}

          {/* Mobile Interaction Hint */}
          {!isFullscreen && (
            <Box
              sx={{
                position: 'absolute',
                bottom: 10,
                right: 10,
                display: 'flex',
                alignItems: 'center',
                gap: 0.5,
                bgcolor: 'rgba(0,0,0,0.6)',
                color: 'white',
                px: 1,
                py: 0.5,
                borderRadius: 1,
                fontSize: '0.75rem',
              }}
            >
              <TouchApp sx={{ fontSize: '1rem' }} />
              터치하여 상세보기
            </Box>
          )}
        </Box>

        {/* Chart Insights */}
        <CardContent sx={{ pt: 2 }}>
          <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
            <Typography variant="subtitle2" color="primary" gutterBottom>
              📊 차트 분석
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {chartType === 'area' && (
                "영역 차트는 누적 투자금액 대비 포트폴리오 가치의 성장을 시각적으로 보여줍니다."
              )}
              {chartType === 'line' && (
                "선형 차트는 시간에 따른 포트폴리오 가치와 투자금액의 변화를 명확하게 보여줍니다."
              )}
              {chartType === 'comparison' && (
                `시장 지수와의 비교를 통해 ${selectedCompany?.nameKr || result.symbol}의 상대적 성과를 확인할 수 있습니다.`
              )}
            </Typography>
          </Paper>
        </CardContent>
      </Card>
    </motion.div>
  );
}