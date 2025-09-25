'use client';

import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  LinearProgress,
  IconButton,
  Menu,
  Slider,
  TextField,
  Switch,
  FormControlLabel,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Avatar,
  Tab,
  Tabs,
} from '@mui/material';
import {
  AccountBalance,
  TrendingUp,
  TrendingDown,
  PieChart as PieChartIcon,
  Assessment,
  Refresh,
  Download,
  Settings,
  Tune,
  Timeline,
  ShowChart,
  Security,
  Speed,
  MonetizationOn,
  Balance,
  Analytics,
  AutoGraph,
  Insights,
  Calculate,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  ScatterChart,
  Scatter,
  ComposedChart,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
  RadialBarChart,
  RadialBar,
} from 'recharts';
import {
  useGetRebalancingSuggestions,
  useGetOptimizationHistory,
  useOptimizePortfolio,
  useCalculateEfficientFrontier,
  useRunBacktest
} from '@/shared/api/generated/portfolio-optimization-controller/portfolio-optimization-controller';
import type {
  PortfolioOptimizationResponse,
  EfficientFrontierResponse,
  BacktestResponse,
  RebalancingSuggestionsResponse,
  OptimizationHistoryResponse
} from '@/shared/api/generated/model';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function CustomTabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const PortfolioOptimizationPage = () => {
  const [selectedStrategy, setSelectedStrategy] = useState('modern-portfolio');
  const [selectedObjective, setSelectedObjective] = useState('max-sharpe');
  const [riskTolerance, setRiskTolerance] = useState(5);
  const [investmentAmount, setInvestmentAmount] = useState(1000000);
  const [currentTab, setCurrentTab] = useState(0);
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // Portfolio ID - In a real app, this would come from user context or route params
  const portfolioId = 1;

  // API calls for portfolio optimization data
  const { data: rebalancingData, isLoading: rebalancingLoading, error: rebalancingError } =
    useGetRebalancingSuggestions(portfolioId);

  const { data: optimizationHistory, isLoading: historyLoading, error: historyError } =
    useGetOptimizationHistory(portfolioId);

  // Mutations for interactive features
  const optimizeMutation = useOptimizePortfolio();
  const efficientFrontierMutation = useCalculateEfficientFrontier();
  const backtestMutation = useRunBacktest();

  // Loading state for overall page
  const isLoading = rebalancingLoading || historyLoading;
  const hasError = rebalancingError || historyError;

  // Data transformation functions
  const transformOptimizationStats = () => {
    if (!optimizationHistory?.optimizations) {
      return {
        totalPortfolios: 0,
        optimizedToday: 0,
        avgImprovement: 0,
        successRate: 0,
        avgSharpeRatio: 0,
        avgVolatility: 0
      };
    }

    const optimizations = optimizationHistory.optimizations;
    const todayOptimizations = optimizations.filter(opt => {
      const optDate = new Date((opt as any).createdAt || new Date());
      const today = new Date();
      return optDate.toDateString() === today.toDateString();
    });

    return {
      totalPortfolios: optimizations.length,
      optimizedToday: todayOptimizations.length,
      avgImprovement: 23.8, // This would be calculated from actual performance data
      successRate: 91.2,
      avgSharpeRatio: 1.67,
      avgVolatility: 14.5
    };
  };

  const optimizationStats = transformOptimizationStats();

  // 최적화 전략별 성과
  const strategyPerformance = [
    {
      strategy: 'Modern Portfolio Theory',
      portfolios: 485,
      avgReturn: 18.7,
      avgSharpe: 1.85,
      avgVolatility: 12.8,
      successRate: 94.2
    },
    {
      strategy: 'Black-Litterman',
      portfolios: 312,
      avgReturn: 21.3,
      avgSharpe: 1.72,
      avgVolatility: 15.4,
      successRate: 88.9
    },
    {
      strategy: 'Risk Parity',
      portfolios: 298,
      avgReturn: 15.9,
      avgSharpe: 1.54,
      avgVolatility: 9.8,
      successRate: 96.1
    },
    {
      strategy: 'Mean Reversion',
      portfolios: 189,
      avgReturn: 24.1,
      avgSharpe: 1.91,
      avgVolatility: 18.2,
      successRate: 82.6
    },
    {
      strategy: 'Momentum Strategy',
      portfolios: 235,
      avgReturn: 26.8,
      avgSharpe: 1.63,
      avgVolatility: 21.5,
      successRate: 79.4
    }
  ];

  // 효율적 프론티어 데이터
  const efficientFrontier = [
    { risk: 8.5, return: 12.3, sharpe: 1.45 },
    { risk: 10.2, return: 14.8, sharpe: 1.65 },
    { risk: 12.1, return: 16.9, sharpe: 1.72 },
    { risk: 14.5, return: 18.7, sharpe: 1.68 },
    { risk: 16.8, return: 20.2, sharpe: 1.58 },
    { risk: 19.3, return: 21.4, sharpe: 1.42 },
    { risk: 22.1, return: 22.9, sharpe: 1.28 },
    { risk: 25.4, return: 24.1, sharpe: 1.15 },
  ];

  // 자산 클래스별 배분
  const assetAllocation = [
    { name: '국내주식', current: 45, optimized: 38, color: '#8884d8' },
    { name: '해외주식', current: 25, optimized: 32, color: '#82ca9d' },
    { name: '채권', current: 20, optimized: 18, color: '#ffc658' },
    { name: '대안투자', current: 7, optimized: 8, color: '#ff7c7c' },
    { name: '현금', current: 3, optimized: 4, color: '#8dd1e1' },
  ];

  // 섹터별 최적 배분
  const sectorAllocation = [
    { sector: '기술', weight: 28.5, expectedReturn: 22.1, risk: 18.4, contribution: 6.3 },
    { sector: '금융', weight: 18.3, expectedReturn: 15.8, risk: 14.2, contribution: 2.9 },
    { sector: '헬스케어', weight: 15.7, expectedReturn: 19.6, risk: 16.8, contribution: 3.1 },
    { sector: '소비재', weight: 12.4, expectedReturn: 14.2, risk: 12.5, contribution: 1.8 },
    { sector: '산업재', weight: 10.8, expectedReturn: 16.9, risk: 15.3, contribution: 1.8 },
    { sector: '에너지', weight: 8.9, expectedReturn: 18.7, risk: 24.1, contribution: 1.7 },
    { sector: '유틸리티', weight: 5.4, expectedReturn: 9.8, risk: 8.7, contribution: 0.5 },
  ];

  // 최적화 결과 비교
  const optimizationResults = {
    current: {
      return: 14.2,
      risk: 18.7,
      sharpe: 0.76,
      maxDrawdown: -23.4,
      var95: -18.2
    },
    optimized: {
      return: 18.9,
      risk: 14.8,
      sharpe: 1.28,
      maxDrawdown: -15.6,
      var95: -12.8
    },
    improvement: {
      return: 4.7,
      risk: -3.9,
      sharpe: 0.52,
      maxDrawdown: 7.8,
      var95: 5.4
    }
  };

  // Transform rebalancing suggestions from API data
  const transformRebalancingRecommendations = () => {
    if (!rebalancingData?.suggestions) {
      return [];
    }

    return rebalancingData.suggestions.map(suggestion => ({
      asset: suggestion.symbol || '',
      currentWeight: parseFloat((suggestion as any).currentWeight || '0'),
      targetWeight: parseFloat((suggestion as any).targetWeight || '0'),
      action: (suggestion as any).action || 'HOLD',
      amount: parseFloat(suggestion.amount || '0'),
      reason: suggestion.reason || '최적화'
    }));
  };

  const rebalancingRecommendations = transformRebalancingRecommendations();

  // 백테스팅 결과
  const backtestingData = [
    { date: '2023-01', current: 100, optimized: 100, benchmark: 100 },
    { date: '2023-02', current: 102, optimized: 104, benchmark: 101 },
    { date: '2023-03', current: 98, optimized: 103, benchmark: 99 },
    { date: '2023-04', current: 105, optimized: 111, benchmark: 104 },
    { date: '2023-05', current: 108, optimized: 116, benchmark: 107 },
    { date: '2023-06', current: 104, optimized: 113, benchmark: 105 },
    { date: '2023-07', current: 111, optimized: 121, benchmark: 110 },
    { date: '2023-08', current: 107, optimized: 119, benchmark: 108 },
    { date: '2023-09', current: 114, optimized: 127, benchmark: 113 },
    { date: '2023-10', current: 110, optimized: 124, benchmark: 111 },
    { date: '2023-11', current: 118, optimized: 135, benchmark: 117 },
    { date: '2023-12', current: 121, optimized: 141, benchmark: 120 },
  ];

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const handleExportMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setExportMenuAnchor(event.currentTarget);
  };

  const handleExportMenuClose = () => {
    setExportMenuAnchor(null);
  };

  const handleExport = (format: string) => {
    console.log(`Exporting portfolio optimization data in ${format} format`);
    handleExportMenuClose();
  };

  const handleOptimizePortfolio = async () => {
    try {
      await optimizeMutation.mutateAsync({
        portfolioId,
        data: {
          optimizationType: selectedStrategy,
          objective: selectedObjective,
          riskTolerance: riskTolerance.toString(),
          investmentAmount: investmentAmount.toString()
        } as any
      });
    } catch (error) {
      console.error('Portfolio optimization failed:', error);
    }
  };

  const handleCalculateEfficientFrontier = async () => {
    try {
      await efficientFrontierMutation.mutateAsync({
        portfolioId,
        data: {
          riskTolerance: riskTolerance.toString(),
          investmentAmount: investmentAmount.toString()
        } as any
      });
    } catch (error) {
      console.error('Efficient frontier calculation failed:', error);
    }
  };

  const handleRunBacktest = async () => {
    try {
      await backtestMutation.mutateAsync({
        portfolioId,
        data: {
          startDate: '2023-01-01',
          endDate: '2023-12-31',
          benchmark: 'S&P500'
        } as any
      });
    } catch (error) {
      console.error('Backtest execution failed:', error);
    }
  };

  const getActionColor = (action: string) => {
    switch (action) {
      case 'BUY': return '#4CAF50';
      case 'SELL': return '#F44336';
      case 'HOLD': return '#FF9800';
      default: return '#757575';
    }
  };

  const formatCurrency = (amount: number) => {
    return `${(amount / 10000).toFixed(0)}만원`;
  };

  // Show loading state
  if (isLoading) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <Box textAlign="center">
            <LinearProgress sx={{ mb: 2, width: 300 }} />
            <Typography variant="body1" color="text.secondary">
              포트폴리오 최적화 데이터를 로딩 중입니다...
            </Typography>
          </Box>
        </Box>
      </Container>
    );
  }

  // Show error state
  if (hasError) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          <Typography variant="body1">
            <strong>데이터 로딩 실패:</strong> 포트폴리오 최적화 데이터를 가져올 수 없습니다.
            서버 연결을 확인하고 다시 시도해주세요.
          </Typography>
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            고급 포트폴리오 최적화
          </Typography>
          <Typography variant="body1" color="text.secondary">
            AI 기반 포트폴리오 최적화, 효율적 프론티어 분석, 리스크 관리
          </Typography>
        </Box>

        {/* 컨트롤 패널 */}
        <Box display="flex" gap={2} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>최적화 전략</InputLabel>
            <Select
              value={selectedStrategy}
              label="최적화 전략"
              onChange={(e) => setSelectedStrategy(e.target.value)}
            >
              <MenuItem value="modern-portfolio">모던 포트폴리오</MenuItem>
              <MenuItem value="black-litterman">블랙-리터만</MenuItem>
              <MenuItem value="risk-parity">위험 균등</MenuItem>
              <MenuItem value="mean-reversion">평균 회귀</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>목표</InputLabel>
            <Select
              value={selectedObjective}
              label="목표"
              onChange={(e) => setSelectedObjective(e.target.value)}
            >
              <MenuItem value="max-return">수익 최대화</MenuItem>
              <MenuItem value="min-risk">리스크 최소화</MenuItem>
              <MenuItem value="max-sharpe">샤프비율 최대화</MenuItem>
              <MenuItem value="target-risk">목표 리스크</MenuItem>
            </Select>
          </FormControl>

          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={handleExportMenuOpen}
          >
            리포트
          </Button>
          <Menu
            anchorEl={exportMenuAnchor}
            open={Boolean(exportMenuAnchor)}
            onClose={handleExportMenuClose}
          >
            <MenuItem onClick={() => handleExport('PDF')}>최적화 리포트 (PDF)</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>포트폴리오 분석 (Excel)</MenuItem>
            <MenuItem onClick={() => handleExport('JSON')}>백테스팅 데이터 (JSON)</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 최적화 상태 알림 */}
      <Alert severity="success" sx={{ mb: 3 }} icon={<Calculate />}>
        <Typography variant="body2">
          <strong>최적화 완료:</strong> 현재 포트폴리오 대비 예상 수익률 +{optimizationResults.improvement.return}%,
          리스크 {optimizationResults.improvement.risk}% 감소, 샤프비율 +{optimizationResults.improvement.sharpe} 개선
        </Typography>
      </Alert>

      {/* 주요 최적화 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    {optimizationStats.totalPortfolios.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 최적화 포트폴리오
                  </Typography>
                </Box>
                <AccountBalance color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="success.main">
                    +{optimizationStats.avgImprovement}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 개선율
                  </Typography>
                </Box>
                <TrendingUp color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="info.main">
                    {optimizationStats.avgSharpeRatio}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 샤프비율
                  </Typography>
                </Box>
                <Assessment color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="warning.main">
                    {optimizationStats.successRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    성공률
                  </Typography>
                </Box>
                <Speed color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 탭 네비게이션 */}
      <Box sx={{ width: '100%', mb: 3 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={currentTab} onChange={handleTabChange}>
            <Tab label="자산 배분" />
            <Tab label="효율적 프론티어" />
            <Tab label="백테스팅" />
            <Tab label="리밸런싱" />
          </Tabs>
        </Box>

        {/* 자산 배분 탭 */}
        <CustomTabPanel value={currentTab} index={0}>
          <Grid container spacing={3}>
            {/* 최적화 파라미터 설정 */}
            <Grid item xs={12} lg={4}>
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  최적화 파라미터
                </Typography>
                <Box mb={3}>
                  <Typography variant="body2" gutterBottom>
                    리스크 허용도: {riskTolerance}/10
                  </Typography>
                  <Slider
                    value={riskTolerance}
                    onChange={(e, value) => setRiskTolerance(value as number)}
                    min={1}
                    max={10}
                    step={1}
                    marks
                    valueLabelDisplay="auto"
                  />
                </Box>
                <Box mb={3}>
                  <TextField
                    label="투자금액"
                    value={investmentAmount.toLocaleString()}
                    onChange={(e) => setInvestmentAmount(Number(e.target.value.replace(/,/g, '')))}
                    fullWidth
                    size="small"
                    InputProps={{
                      endAdornment: '원'
                    }}
                  />
                </Box>
                <Button
                  variant="contained"
                  startIcon={<Calculate />}
                  fullWidth
                  onClick={handleOptimizePortfolio}
                  disabled={optimizeMutation.isPending}
                >
                  {optimizeMutation.isPending ? '최적화 중...' : '최적화 실행'}
                </Button>
                {optimizeMutation.error ? (
                  <Alert severity="error" sx={{ mt: 2 }}>
                    최적화 실행 중 오류가 발생했습니다: {String(optimizeMutation.error)}
                  </Alert>
                ) : null}
              </Paper>

              {/* 최적화 결과 비교 */}
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  최적화 결과 비교
                </Typography>
                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    예상 수익률
                  </Typography>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2">
                      현재: {optimizationResults.current.return}%
                    </Typography>
                    <Typography variant="body2" color="primary" fontWeight="bold">
                      최적화: {optimizationResults.optimized.return}%
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="success.main">
                    개선: +{optimizationResults.improvement.return}%
                  </Typography>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    변동성
                  </Typography>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2">
                      현재: {optimizationResults.current.risk}%
                    </Typography>
                    <Typography variant="body2" color="primary" fontWeight="bold">
                      최적화: {optimizationResults.optimized.risk}%
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="success.main">
                    개선: {optimizationResults.improvement.risk}%
                  </Typography>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    샤프 비율
                  </Typography>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2">
                      현재: {optimizationResults.current.sharpe}
                    </Typography>
                    <Typography variant="body2" color="primary" fontWeight="bold">
                      최적화: {optimizationResults.optimized.sharpe}
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="success.main">
                    개선: +{optimizationResults.improvement.sharpe}
                  </Typography>
                </Box>
              </Paper>
            </Grid>

            {/* 자산 배분 차트 */}
            <Grid item xs={12} lg={8}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  자산 클래스별 배분 비교
                </Typography>
                <ResponsiveContainer width="100%" height={400}>
                  <BarChart data={assetAllocation}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="current" fill="#8884d8" name="현재 배분" />
                    <Bar dataKey="optimized" fill="#82ca9d" name="최적 배분" />
                  </BarChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>

            {/* 섹터별 최적 배분 */}
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  섹터별 최적 배분
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>섹터</TableCell>
                        <TableCell align="center">비중</TableCell>
                        <TableCell align="center">예상 수익률</TableCell>
                        <TableCell align="center">리스크</TableCell>
                        <TableCell align="center">기여도</TableCell>
                        <TableCell align="center">비중 조정</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {sectorAllocation.map((sector) => (
                        <TableRow key={sector.sector} hover>
                          <TableCell>{sector.sector}</TableCell>
                          <TableCell align="center">
                            <Typography variant="body2" fontWeight="bold">
                              {sector.weight}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Typography
                              variant="body2"
                              sx={{ color: sector.expectedReturn > 18 ? '#4CAF50' : '#757575' }}
                            >
                              {sector.expectedReturn}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Typography variant="body2">
                              {sector.risk}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Typography variant="body2" color="primary">
                              {sector.contribution}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <LinearProgress
                              variant="determinate"
                              value={sector.weight}
                              sx={{ height: 8, borderRadius: 4, width: 80 }}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            </Grid>
          </Grid>
        </CustomTabPanel>

        {/* 효율적 프론티어 탭 */}
        <CustomTabPanel value={currentTab} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12} lg={8}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  효율적 프론티어
                </Typography>
                <ResponsiveContainer width="100%" height={500}>
                  <ScatterChart data={efficientFrontier}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="risk" name="변동성" unit="%" />
                    <YAxis dataKey="return" name="수익률" unit="%" />
                    <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                    <Scatter
                      dataKey="return"
                      fill="#8884d8"
                      r={8}
                    />
                    <ReferenceLine x={optimizationResults.optimized.risk} stroke="red" strokeDasharray="5 5" />
                    <ReferenceLine y={optimizationResults.optimized.return} stroke="red" strokeDasharray="5 5" />
                  </ScatterChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>

            <Grid item xs={12} lg={4}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  전략별 성과
                </Typography>
                <List>
                  {strategyPerformance.map((strategy, index) => (
                    <React.Fragment key={strategy.strategy}>
                      <ListItem>
                        <ListItemText
                          primary={strategy.strategy}
                          secondary={
                            <Box>
                              <Typography variant="body2" component="div">
                                수익률: {strategy.avgReturn}% | 샤프: {strategy.avgSharpe}
                              </Typography>
                              <Typography variant="body2" component="div">
                                변동성: {strategy.avgVolatility}% | 성공률: {strategy.successRate}%
                              </Typography>
                              <LinearProgress
                                variant="determinate"
                                value={strategy.successRate}
                                sx={{ mt: 1, height: 4, borderRadius: 2 }}
                              />
                            </Box>
                          }
                        />
                      </ListItem>
                      {index < strategyPerformance.length - 1 && <Divider />}
                    </React.Fragment>
                  ))}
                </List>
              </Paper>
            </Grid>
          </Grid>
        </CustomTabPanel>

        {/* 백테스팅 탭 */}
        <CustomTabPanel value={currentTab} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  백테스팅 성과 비교
                </Typography>
                <ResponsiveContainer width="100%" height={400}>
                  <LineChart data={backtestingData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="current"
                      stroke="#8884d8"
                      strokeWidth={2}
                      name="현재 포트폴리오"
                    />
                    <Line
                      type="monotone"
                      dataKey="optimized"
                      stroke="#82ca9d"
                      strokeWidth={3}
                      name="최적화 포트폴리오"
                    />
                    <Line
                      type="monotone"
                      dataKey="benchmark"
                      stroke="#ffc658"
                      strokeWidth={2}
                      name="벤치마크"
                    />
                  </LineChart>
                </ResponsiveContainer>

                <Box mt={3}>
                  <Grid container spacing={3}>
                    <Grid item xs={4}>
                      <Box textAlign="center">
                        <Typography variant="h5" color="primary">
                          +21%
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          현재 포트폴리오
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={4}>
                      <Box textAlign="center">
                        <Typography variant="h5" color="success.main">
                          +41%
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          최적화 포트폴리오
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={4}>
                      <Box textAlign="center">
                        <Typography variant="h5" color="warning.main">
                          +20%
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          벤치마크
                        </Typography>
                      </Box>
                    </Grid>
                  </Grid>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </CustomTabPanel>

        {/* 리밸런싱 탭 */}
        <CustomTabPanel value={currentTab} index={3}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  리밸런싱 제안
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>자산</TableCell>
                        <TableCell align="center">현재 비중</TableCell>
                        <TableCell align="center">목표 비중</TableCell>
                        <TableCell align="center">액션</TableCell>
                        <TableCell align="center">거래 금액</TableCell>
                        <TableCell>사유</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rebalancingRecommendations.map((rec, index) => (
                        <TableRow key={index} hover>
                          <TableCell>
                            <Typography variant="subtitle2" fontWeight="bold">
                              {rec.asset}
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Typography variant="body2">
                              {rec.currentWeight}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Typography variant="body2" fontWeight="bold">
                              {rec.targetWeight}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Chip
                              label={rec.action}
                              size="small"
                              sx={{
                                backgroundColor: getActionColor(rec.action),
                                color: 'white',
                                fontWeight: 'bold'
                              }}
                            />
                          </TableCell>
                          <TableCell align="center">
                            <Typography
                              variant="body2"
                              sx={{ color: getActionColor(rec.action) }}
                              fontWeight="bold"
                            >
                              {formatCurrency(rec.amount)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2" color="text.secondary">
                              {rec.reason}
                            </Typography>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>

                <Box mt={3} display="flex" justifyContent="center" gap={2}>
                  <Button
                    variant="contained"
                    startIcon={<Balance />}
                    onClick={handleOptimizePortfolio}
                    disabled={optimizeMutation.isPending}
                  >
                    {optimizeMutation.isPending ? '리밸런싱 중...' : '리밸런싱 실행'}
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<Timeline />}
                    onClick={handleRunBacktest}
                    disabled={backtestMutation.isPending}
                  >
                    {backtestMutation.isPending ? '시뮬레이션 중...' : '시뮬레이션'}
                  </Button>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </CustomTabPanel>
      </Box>
    </Container>
  );
};

export default PortfolioOptimizationPage;