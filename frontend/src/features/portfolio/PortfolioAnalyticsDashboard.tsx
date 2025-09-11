'use client';

import React, { useState, useMemo } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Tabs,
  Tab,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  Button,
  Tooltip,
  IconButton,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Assessment as AssessmentIcon,
  PieChart as PieChartIcon,
  ShowChart as ShowChartIcon,
  Warning as WarningIcon,
  Lightbulb as LightbulbIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { 
  PortfolioAnalyticsService, 
  PortfolioHolding,
  PortfolioAnalytics,
  PortfolioRecommendation,
  RiskFactor,
  Opportunity
} from '../../shared/services/analytics/PortfolioAnalyticsService';

interface PortfolioAnalyticsDashboardProps {
  sessionId: number;
  challengeId?: number;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`analytics-tabpanel-${index}`}
      aria-labelledby={`analytics-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

/**
 * Professional Portfolio Analytics Dashboard
 * 전문가급 포트폴리오 분석 대시보드
 */
export function PortfolioAnalyticsDashboard({ sessionId, challengeId }: PortfolioAnalyticsDashboardProps) {
  const [selectedTab, setSelectedTab] = useState(0);
  const [timeframe, setTimeframe] = useState<'1M' | '3M' | '6M' | '1Y' | '2Y'>('1Y');

  // Mock portfolio holdings data
  const mockHoldings: PortfolioHolding[] = useMemo(() => [
    {
      symbol: 'AAPL',
      name: 'Apple Inc.',
      shares: 100,
      averagePrice: 145.50,
      currentPrice: 150.25,
      sector: 'Technology',
      marketCap: 2400000000000,
      beta: 1.2,
      dividendYield: 0.5,
      peRatio: 28.5,
      weekHigh52: 182.94,
      weekLow52: 124.17,
      historicalPrices: []
    },
    {
      symbol: 'MSFT',
      name: 'Microsoft Corporation',
      shares: 75,
      averagePrice: 280.00,
      currentPrice: 290.15,
      sector: 'Technology',
      marketCap: 2200000000000,
      beta: 0.9,
      dividendYield: 0.7,
      peRatio: 32.1,
      weekHigh52: 348.10,
      weekLow52: 213.43,
      historicalPrices: []
    },
    {
      symbol: 'GOOGL',
      name: 'Alphabet Inc.',
      shares: 50,
      averagePrice: 2350.00,
      currentPrice: 2420.50,
      sector: 'Technology',
      marketCap: 1600000000000,
      beta: 1.1,
      dividendYield: 0.0,
      peRatio: 25.8,
      weekHigh52: 2925.07,
      weekLow52: 1992.19,
      historicalPrices: []
    },
    {
      symbol: 'JPM',
      name: 'JPMorgan Chase & Co.',
      shares: 120,
      averagePrice: 135.00,
      currentPrice: 142.30,
      sector: 'Financial',
      marketCap: 420000000000,
      beta: 1.4,
      dividendYield: 2.4,
      peRatio: 12.3,
      weekHigh52: 172.96,
      weekLow52: 118.27,
      historicalPrices: []
    },
    {
      symbol: 'JNJ',
      name: 'Johnson & Johnson',
      shares: 85,
      averagePrice: 165.00,
      currentPrice: 168.75,
      sector: 'Healthcare',
      marketCap: 440000000000,
      beta: 0.7,
      dividendYield: 2.8,
      peRatio: 16.2,
      weekHigh52: 181.83,
      weekLow52: 155.72,
      historicalPrices: []
    }
  ], []);

  // Calculate analytics using the service
  const analytics = useMemo(() => {
    return PortfolioAnalyticsService.calculatePortfolioAnalytics(mockHoldings, timeframe);
  }, [mockHoldings, timeframe]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setSelectedTab(newValue);
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'CRITICAL':
        return 'error';
      case 'HIGH':
        return 'warning';
      case 'MEDIUM':
        return 'info';
      case 'LOW':
        return 'success';
      default:
        return 'default';
    }
  };

  const getRiskLevelColor = (level: string) => {
    switch (level) {
      case 'CRITICAL':
        return '#f44336';
      case 'HIGH':
        return '#ff9800';
      case 'MEDIUM':
        return '#2196f3';
      case 'LOW':
        return '#4caf50';
      default:
        return '#9e9e9e';
    }
  };

  return (
    <Box sx={{ width: '100%' }}>
      {/* Header with Controls */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 600 }}>
          📊 Portfolio Analytics Dashboard
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>시간 범위</InputLabel>
            <Select
              value={timeframe}
              label="시간 범위"
              onChange={(e) => setTimeframe(e.target.value as any)}
            >
              <MenuItem value="1M">1개월</MenuItem>
              <MenuItem value="3M">3개월</MenuItem>
              <MenuItem value="6M">6개월</MenuItem>
              <MenuItem value="1Y">1년</MenuItem>
              <MenuItem value="2Y">2년</MenuItem>
            </Select>
          </FormControl>
          
          <Tooltip title="데이터 새로고침">
            <IconButton>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Key Metrics Overview */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <AssessmentIcon color="primary" sx={{ mr: 1 }} />
                <Typography variant="h6">총 자산가치</Typography>
              </Box>
              <Typography variant="h4" color="primary" sx={{ fontWeight: 600 }}>
                ₩{analytics.totalValue.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                원금: ₩{analytics.totalCost.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                {analytics.totalReturn >= 0 ? (
                  <TrendingUpIcon color="success" sx={{ mr: 1 }} />
                ) : (
                  <TrendingDownIcon color="error" sx={{ mr: 1 }} />
                )}
                <Typography variant="h6">총 수익률</Typography>
              </Box>
              <Typography 
                variant="h4" 
                color={analytics.totalReturnPercent >= 0 ? 'success.main' : 'error.main'}
                sx={{ fontWeight: 600 }}
              >
                {analytics.totalReturnPercent >= 0 ? '+' : ''}{analytics.totalReturnPercent.toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {analytics.totalReturn >= 0 ? '+' : ''}₩{analytics.totalReturn.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <ShowChartIcon color="info" sx={{ mr: 1 }} />
                <Typography variant="h6">샤프 비율</Typography>
              </Box>
              <Typography variant="h4" color="info.main" sx={{ fontWeight: 600 }}>
                {analytics.sharpeRatio.toFixed(2)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                위험 대비 수익률
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card elevation={2}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <WarningIcon color="warning" sx={{ mr: 1 }} />
                <Typography variant="h6">최대 낙폭</Typography>
              </Box>
              <Typography variant="h4" color="warning.main" sx={{ fontWeight: 600 }}>
                {(analytics.maxDrawdown * 100).toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                위험 수준
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs Navigation */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={selectedTab} onChange={handleTabChange} aria-label="analytics tabs">
            <Tab label="위험 분석" />
            <Tab label="자산 배분" />
            <Tab label="추천사항" />
            <Tab label="성과 지표" />
            <Tab label="위험 요소" />
          </Tabs>
        </Box>

        {/* Risk Analysis Tab */}
        <TabPanel value={selectedTab} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>위험 지표</Typography>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2">변동성 (연환산)</Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={Math.min(analytics.volatility * 100, 100)} 
                  color="warning"
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption">{(analytics.volatility * 100).toFixed(1)}%</Typography>
              </Box>
              
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2">포트폴리오 베타</Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={Math.min(analytics.beta * 50, 100)} 
                  color={analytics.beta > 1 ? "error" : "success"}
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption">{analytics.beta.toFixed(2)}</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2">위험가치 (VaR 95%)</Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={Math.min(analytics.valueAtRisk * 1000, 100)} 
                  color="error"
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption">{(analytics.valueAtRisk * 100).toFixed(2)}%</Typography>
              </Box>
            </Grid>

            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>성과 지표</Typography>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2">연환산 수익률</Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={Math.min(Math.abs(analytics.annualizedReturn * 100), 100)} 
                  color={analytics.annualizedReturn >= 0 ? "success" : "error"}
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption">{(analytics.annualizedReturn * 100).toFixed(2)}%</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2">승률</Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={analytics.winRate} 
                  color="info"
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption">{analytics.winRate.toFixed(1)}%</Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2">칼마 비율</Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={Math.min(Math.abs(analytics.calmarRatio * 10), 100)} 
                  color="primary"
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption">{analytics.calmarRatio.toFixed(2)}</Typography>
              </Box>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Asset Allocation Tab */}
        <TabPanel value={selectedTab} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>섹터별 배분</Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>섹터</TableCell>
                      <TableCell align="right">비중</TableCell>
                      <TableCell align="right">수익률</TableCell>
                      <TableCell align="right">목표 비중</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {analytics.sectorAllocation.map((sector) => (
                      <TableRow key={sector.sector}>
                        <TableCell component="th" scope="row">
                          {sector.sector}
                        </TableCell>
                        <TableCell align="right">
                          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
                            <LinearProgress
                              variant="determinate"
                              value={sector.percentage}
                              sx={{ width: 60, mr: 1 }}
                            />
                            <Typography variant="caption">
                              {sector.percentage.toFixed(1)}%
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Chip
                            label={`${sector.return >= 0 ? '+' : ''}${sector.return.toFixed(1)}%`}
                            color={sector.return >= 0 ? 'success' : 'error'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="caption" color="text.secondary">
                            {sector.targetPercentage}%
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Grid>

            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>자산클래스별 배분</Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>자산클래스</TableCell>
                      <TableCell align="right">비중</TableCell>
                      <TableCell align="right">수익률</TableCell>
                      <TableCell align="right">위험도</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {analytics.assetAllocation.map((asset) => (
                      <TableRow key={asset.assetClass}>
                        <TableCell component="th" scope="row">
                          {asset.assetClass}
                        </TableCell>
                        <TableCell align="right">
                          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
                            <LinearProgress
                              variant="determinate"
                              value={asset.percentage}
                              sx={{ width: 60, mr: 1 }}
                            />
                            <Typography variant="caption">
                              {asset.percentage.toFixed(1)}%
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Chip
                            label={`${asset.return >= 0 ? '+' : ''}${asset.return.toFixed(1)}%`}
                            color={asset.return >= 0 ? 'success' : 'error'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="caption" color="text.secondary">
                            {asset.risk.toFixed(1)}%
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Recommendations Tab */}
        <TabPanel value={selectedTab} index={2}>
          <Typography variant="h6" gutterBottom>포트폴리오 개선 추천</Typography>
          <Grid container spacing={2}>
            {analytics.recommendations.map((rec, index) => (
              <Grid item xs={12} key={index}>
                <Alert 
                  severity={getPriorityColor(rec.priority) as any}
                  icon={<LightbulbIcon />}
                  sx={{ mb: 2 }}
                >
                  <Box>
                    <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
                      {rec.title}
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      {rec.description}
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>권장 조치:</strong> {rec.action}
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>예상 효과:</strong> {rec.impact}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      근거: {rec.reasoning}
                    </Typography>
                    {rec.symbols && rec.symbols.length > 0 && (
                      <Box sx={{ mt: 1 }}>
                        {rec.symbols.map((symbol) => (
                          <Chip
                            key={symbol}
                            label={symbol}
                            size="small"
                            sx={{ mr: 0.5 }}
                          />
                        ))}
                      </Box>
                    )}
                  </Box>
                </Alert>
              </Grid>
            ))}
          </Grid>
        </TabPanel>

        {/* Performance Metrics Tab */}
        <TabPanel value={selectedTab} index={3}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>수익성 지표</Typography>
              <Card variant="outlined" sx={{ p: 2, mb: 2 }}>
                <Typography variant="subtitle1">연환산 수익률</Typography>
                <Typography variant="h5" color={analytics.annualizedReturn >= 0 ? 'success.main' : 'error.main'}>
                  {(analytics.annualizedReturn * 100).toFixed(2)}%
                </Typography>
              </Card>
              
              <Card variant="outlined" sx={{ p: 2, mb: 2 }}>
                <Typography variant="subtitle1">수익 인수 (Profit Factor)</Typography>
                <Typography variant="h5" color="primary.main">
                  {analytics.profitFactor.toFixed(2)}
                </Typography>
              </Card>

              <Card variant="outlined" sx={{ p: 2 }}>
                <Typography variant="subtitle1">트레이너 비율</Typography>
                <Typography variant="h5" color="info.main">
                  {analytics.treynorRatio.toFixed(2)}
                </Typography>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>위험 조정 지표</Typography>
              <Card variant="outlined" sx={{ p: 2, mb: 2 }}>
                <Typography variant="subtitle1">샤프 비율</Typography>
                <Typography variant="h5" color="primary.main">
                  {analytics.sharpeRatio.toFixed(2)}
                </Typography>
              </Card>
              
              <Card variant="outlined" sx={{ p: 2, mb: 2 }}>
                <Typography variant="subtitle1">소르티노 비율</Typography>
                <Typography variant="h5" color="secondary.main">
                  {analytics.sortinoRatio.toFixed(2)}
                </Typography>
              </Card>

              <Card variant="outlined" sx={{ p: 2 }}>
                <Typography variant="subtitle1">정보 비율</Typography>
                <Typography variant="h5" color="info.main">
                  {analytics.informationRatio.toFixed(2)}
                </Typography>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Risk Factors Tab */}
        <TabPanel value={selectedTab} index={4}>
          <Typography variant="h6" gutterBottom>위험 요소 분석</Typography>
          {analytics.riskFactors.length === 0 ? (
            <Alert severity="success">
              현재 포트폴리오에서 주요 위험 요소가 발견되지 않았습니다.
            </Alert>
          ) : (
            <Grid container spacing={2}>
              {analytics.riskFactors.map((risk, index) => (
                <Grid item xs={12} key={index}>
                  <Card variant="outlined" sx={{ borderLeft: `4px solid ${getRiskLevelColor(risk.level)}` }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                        <Typography variant="h6" component="h3">
                          {risk.factor}
                        </Typography>
                        <Chip
                          label={risk.level}
                          size="small"
                          sx={{ 
                            backgroundColor: getRiskLevelColor(risk.level),
                            color: 'white'
                          }}
                        />
                      </Box>
                      
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        <strong>설명:</strong> {risk.description}
                      </Typography>
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        <strong>영향:</strong> {risk.impact}
                      </Typography>
                      <Typography variant="body2" sx={{ mb: 2 }}>
                        <strong>완화 방안:</strong> {risk.mitigation}
                      </Typography>
                      
                      {risk.affectedSymbols.length > 0 && (
                        <Box>
                          <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                            영향받는 종목:
                          </Typography>
                          {risk.affectedSymbols.map((symbol) => (
                            <Chip
                              key={symbol}
                              label={symbol}
                              size="small"
                              variant="outlined"
                              sx={{ mr: 0.5 }}
                            />
                          ))}
                        </Box>
                      )}
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </TabPanel>
      </Card>
    </Box>
  );
}