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
  Badge,
  Switch,
  FormControlLabel,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Avatar,
  Slider,
  TextField,
} from '@mui/material';
import {
  Security,
  Warning,
  Error,
  TrendingDown,
  Shield,
  Assessment,
  Refresh,
  Download,
  Settings,
  NotificationsActive,
  CheckCircle,
  Gavel,
  MonitorHeart,
  Speed,
  Analytics,
  Balance,
  Timeline,
  ShowChart,
  AccountBalance,
  HealthAndSafety,
  Report,
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
  ComposedChart,
  ScatterChart,
  Scatter,
  RadialBarChart,
  RadialBar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
  FunnelChart,
  Funnel,
  LabelList,
} from 'recharts';

const RiskManagementPage = () => {
  const [selectedTimeframe, setSelectedTimeframe] = useState('1d');
  const [selectedRiskType, setSelectedRiskType] = useState('all');
  const [alertsEnabled, setAlertsEnabled] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(new Date());
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // 리스크 관리 통계
  const riskStats = {
    overallRiskScore: 6.8, // 1-10 scale
    portfolioVaR: 125000,
    maxDrawdown: -12.4,
    sharpeRatio: 1.34,
    totalAlerts: 23,
    criticalAlerts: 3,
    riskBudgetUsed: 78.5,
    complianceScore: 94.2
  };

  // 실시간 리스크 알림
  const riskAlerts = [
    {
      id: 1,
      type: 'CRITICAL',
      category: 'Concentration Risk',
      message: 'AAPL 포지션이 포트폴리오의 15%를 초과했습니다',
      timestamp: '2024-01-15 14:25:30',
      value: 15.2,
      threshold: 15.0,
      action: 'REBALANCE'
    },
    {
      id: 2,
      type: 'HIGH',
      category: 'Market Risk',
      message: 'VIX 지수가 임계값을 초과했습니다',
      timestamp: '2024-01-15 14:20:15',
      value: 28.5,
      threshold: 25.0,
      action: 'HEDGE'
    },
    {
      id: 3,
      type: 'MEDIUM',
      category: 'Liquidity Risk',
      message: '소형주 비중이 권장 수준을 초과했습니다',
      timestamp: '2024-01-15 14:18:42',
      value: 12.8,
      threshold: 10.0,
      action: 'REVIEW'
    },
    {
      id: 4,
      type: 'LOW',
      category: 'Credit Risk',
      message: '채권 신용등급 변화 감지됨',
      timestamp: '2024-01-15 14:15:20',
      value: 0,
      threshold: 0,
      action: 'MONITOR'
    }
  ];

  // VaR 히스토리
  const varHistory = [
    { date: '01/10', VaR95: 95000, VaR99: 125000, actualPL: -45000 },
    { date: '01/11', VaR95: 102000, VaR99: 135000, actualPL: -78000 },
    { date: '01/12', VaR95: 98000, VaR99: 130000, actualPL: -32000 },
    { date: '01/13', VaR95: 110000, VaR99: 145000, actualPL: -89000 },
    { date: '01/14', VaR95: 105000, VaR99: 140000, actualPL: -56000 },
    { date: '01/15', VaR95: 125000, VaR99: 165000, actualPL: -92000 },
  ];

  // 리스크 요인 분해
  const riskFactors = [
    { factor: '시장 리스크', contribution: 45.2, volatility: 18.7, beta: 1.25 },
    { factor: '섹터 리스크', contribution: 28.9, volatility: 14.3, beta: 0.89 },
    { factor: '개별주 리스크', contribution: 15.8, volatility: 22.1, beta: 0.67 },
    { factor: '통화 리스크', contribution: 6.4, volatility: 12.5, beta: 0.34 },
    { factor: '유동성 리스크', contribution: 3.7, volatility: 8.9, beta: 0.21 },
  ];

  // 포지션별 리스크 분석
  const positionRisks = [
    {
      symbol: 'AAPL',
      position: 15200000,
      weight: 15.2,
      var: 285000,
      beta: 1.18,
      tracking_error: 8.4,
      liquidity_days: 0.5,
      risk_level: 'HIGH'
    },
    {
      symbol: 'MSFT',
      position: 12800000,
      weight: 12.8,
      var: 245000,
      beta: 0.94,
      tracking_error: 6.7,
      liquidity_days: 0.3,
      risk_level: 'MEDIUM'
    },
    {
      symbol: 'GOOGL',
      position: 10500000,
      weight: 10.5,
      var: 198000,
      beta: 1.08,
      tracking_error: 9.2,
      liquidity_days: 0.4,
      risk_level: 'MEDIUM'
    },
    {
      symbol: 'TSLA',
      position: 8900000,
      weight: 8.9,
      var: 342000,
      beta: 1.87,
      tracking_error: 15.6,
      liquidity_days: 0.8,
      risk_level: 'CRITICAL'
    },
    {
      symbol: 'BND',
      position: 18500000,
      weight: 18.5,
      var: 89000,
      beta: -0.12,
      tracking_error: 2.1,
      liquidity_days: 1.2,
      risk_level: 'LOW'
    }
  ];

  // 리스크 한도 모니터링
  const riskLimits = [
    {
      metric: 'Portfolio VaR',
      current: 125000,
      limit: 150000,
      utilization: 83.3,
      status: 'WARNING'
    },
    {
      metric: 'Single Position',
      current: 15.2,
      limit: 15.0,
      utilization: 101.3,
      status: 'BREACH'
    },
    {
      metric: 'Sector Concentration',
      current: 28.5,
      limit: 35.0,
      utilization: 81.4,
      status: 'OK'
    },
    {
      metric: 'Leverage Ratio',
      current: 1.85,
      limit: 2.0,
      utilization: 92.5,
      status: 'WARNING'
    },
    {
      metric: 'Liquidity Buffer',
      current: 5.2,
      limit: 5.0,
      utilization: 104.0,
      status: 'BREACH'
    }
  ];

  // 스트레스 테스트 시나리오
  const stressTestScenarios = [
    {
      scenario: '2008 금융위기',
      probability: 0.5,
      portfolioImpact: -38.4,
      duration: '18개월',
      recovery: '36개월'
    },
    {
      scenario: '2020 코로나 쇼크',
      probability: 2.0,
      portfolioImpact: -28.7,
      duration: '3개월',
      recovery: '12개월'
    },
    {
      scenario: '기술주 폭락',
      probability: 5.0,
      portfolioImpact: -22.1,
      duration: '6개월',
      recovery: '18개월'
    },
    {
      scenario: '금리 급등',
      probability: 10.0,
      portfolioImpact: -15.8,
      duration: '12개월',
      recovery: '24개월'
    },
    {
      scenario: '지정학적 위험',
      probability: 15.0,
      portfolioImpact: -18.9,
      duration: '9개월',
      recovery: '15개월'
    }
  ];

  // 리스크 조정 수익률
  const riskAdjustedReturns = [
    { period: '1개월', return: 2.8, sharpe: 1.45, sortino: 1.82, calmar: 2.15 },
    { period: '3개월', return: 8.9, sharpe: 1.52, sortino: 1.89, calmar: 2.34 },
    { period: '6개월', return: 15.7, sharpe: 1.48, sortino: 1.76, calmar: 2.08 },
    { period: '1년', return: 24.3, sharpe: 1.34, sortino: 1.65, calmar: 1.92 },
  ];

  // 자동 새로고침
  useEffect(() => {
    let interval: NodeJS.Timeout;
    interval = setInterval(() => {
      setLastUpdate(new Date());
    }, 30000); // 30초마다 새로고침

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, []);

  const handleExportMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setExportMenuAnchor(event.currentTarget);
  };

  const handleExportMenuClose = () => {
    setExportMenuAnchor(null);
  };

  const handleExport = (format: string) => {
    console.log(`Exporting risk management data in ${format} format`);
    handleExportMenuClose();
  };

  const getAlertColor = (type: string) => {
    switch (type) {
      case 'CRITICAL': return '#F44336';
      case 'HIGH': return '#FF5722';
      case 'MEDIUM': return '#FF9800';
      case 'LOW': return '#FFC107';
      default: return '#757575';
    }
  };

  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'CRITICAL': return <Error sx={{ color: '#F44336' }} />;
      case 'HIGH': return <Warning sx={{ color: '#FF5722' }} />;
      case 'MEDIUM': return <Warning sx={{ color: '#FF9800' }} />;
      case 'LOW': return <NotificationsActive sx={{ color: '#FFC107' }} />;
      default: return <CheckCircle sx={{ color: '#4CAF50' }} />;
    }
  };

  const getRiskLevelColor = (level: string) => {
    switch (level) {
      case 'CRITICAL': return '#F44336';
      case 'HIGH': return '#FF5722';
      case 'MEDIUM': return '#FF9800';
      case 'LOW': return '#4CAF50';
      default: return '#757575';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'BREACH': return '#F44336';
      case 'WARNING': return '#FF9800';
      case 'OK': return '#4CAF50';
      default: return '#757575';
    }
  };

  const formatCurrency = (amount: number) => {
    if (Math.abs(amount) >= 1000000) {
      return `${(amount / 1000000).toFixed(1)}백만원`;
    } else if (Math.abs(amount) >= 10000) {
      return `${(amount / 10000).toFixed(0)}만원`;
    }
    return `${amount.toLocaleString()}원`;
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            리스크 관리 대시보드
          </Typography>
          <Typography variant="body1" color="text.secondary">
            실시간 리스크 모니터링, VaR 분석, 스트레스 테스트, 리스크 한도 관리
          </Typography>
        </Box>

        {/* 컨트롤 패널 */}
        <Box display="flex" gap={2} alignItems="center">
          <Typography variant="body2" color="text.secondary">
            마지막 업데이트: {lastUpdate.toLocaleTimeString()}
          </Typography>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>기간</InputLabel>
            <Select
              value={selectedTimeframe}
              label="기간"
              onChange={(e) => setSelectedTimeframe(e.target.value)}
            >
              <MenuItem value="1h">1시간</MenuItem>
              <MenuItem value="1d">1일</MenuItem>
              <MenuItem value="1w">1주</MenuItem>
              <MenuItem value="1m">1개월</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>리스크 유형</InputLabel>
            <Select
              value={selectedRiskType}
              label="리스크 유형"
              onChange={(e) => setSelectedRiskType(e.target.value)}
            >
              <MenuItem value="all">전체</MenuItem>
              <MenuItem value="market">시장 리스크</MenuItem>
              <MenuItem value="credit">신용 리스크</MenuItem>
              <MenuItem value="liquidity">유동성 리스크</MenuItem>
              <MenuItem value="operational">운영 리스크</MenuItem>
            </Select>
          </FormControl>

          <FormControlLabel
            control={
              <Switch
                checked={alertsEnabled}
                onChange={(e) => setAlertsEnabled(e.target.checked)}
              />
            }
            label="알림"
          />

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
            <MenuItem onClick={() => handleExport('PDF')}>리스크 리포트 (PDF)</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>VaR 분석 (Excel)</MenuItem>
            <MenuItem onClick={() => handleExport('JSON')}>스트레스 테스트 (JSON)</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 전체 리스크 상태 알림 */}
      <Alert
        severity={riskStats.overallRiskScore > 7 ? 'error' :
                 riskStats.overallRiskScore > 5 ? 'warning' : 'success'}
        sx={{ mb: 3 }}
        icon={<Security />}
      >
        <Typography variant="body2">
          <strong>전체 리스크 점수: {riskStats.overallRiskScore}/10</strong>
          {' '} | 포트폴리오 VaR: {formatCurrency(riskStats.portfolioVaR)} |
          활성 알림: {riskStats.totalAlerts}개 (위험: {riskStats.criticalAlerts}개)
        </Typography>
      </Alert>

      {/* 주요 리스크 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="warning.main">
                    {riskStats.overallRiskScore}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    전체 리스크 점수
                  </Typography>
                </Box>
                <Shield color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="error.main">
                    {formatCurrency(riskStats.portfolioVaR)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    포트폴리오 VaR
                  </Typography>
                </Box>
                <TrendingDown color="error" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {riskStats.sharpeRatio}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    샤프 비율
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
                  <Typography variant="h4" color="success.main">
                    {riskStats.complianceScore}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    컴플라이언스 점수
                  </Typography>
                </Box>
                <CheckCircle color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 실시간 리스크 알림 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">
                실시간 리스크 알림
              </Typography>
              <Badge badgeContent={riskStats.criticalAlerts} color="error">
                <NotificationsActive />
              </Badge>
            </Box>
            {riskAlerts.map((alert) => (
              <Alert
                key={alert.id}
                severity={alert.type === 'CRITICAL' ? 'error' :
                         alert.type === 'HIGH' ? 'warning' : 'info'}
                sx={{ mb: 2 }}
                action={
                  <Button color="inherit" size="small">
                    {alert.action}
                  </Button>
                }
              >
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold">
                    {alert.category} - {alert.message}
                  </Typography>
                  <Typography variant="body2">
                    현재값: {alert.value || 'N/A'} | 임계값: {alert.threshold || 'N/A'} |
                    시간: {new Date(alert.timestamp).toLocaleTimeString()}
                  </Typography>
                </Box>
              </Alert>
            ))}
          </Paper>
        </Grid>

        {/* 리스크 한도 모니터링 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              리스크 한도 모니터링
            </Typography>
            <List>
              {riskLimits.map((limit, index) => (
                <React.Fragment key={limit.metric}>
                  <ListItem>
                    <ListItemIcon>
                      {limit.status === 'BREACH' ? <Error color="error" /> :
                       limit.status === 'WARNING' ? <Warning color="warning" /> :
                       <CheckCircle color="success" />}
                    </ListItemIcon>
                    <ListItemText
                      primary={limit.metric}
                      secondary={
                        <Box>
                          <Typography variant="body2" component="div">
                            현재: {typeof limit.current === 'number' && limit.current > 1000 ?
                                   formatCurrency(limit.current) : `${limit.current}${limit.metric.includes('Ratio') || limit.metric.includes('Position') ? '' : ''}`} /
                            한도: {typeof limit.limit === 'number' && limit.limit > 1000 ?
                                   formatCurrency(limit.limit) : `${limit.limit}${limit.metric.includes('Ratio') || limit.metric.includes('Position') ? '' : ''}`}
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={Math.min(limit.utilization, 100)}
                            sx={{
                              mt: 1,
                              height: 6,
                              borderRadius: 3,
                              '& .MuiLinearProgress-bar': {
                                backgroundColor: getStatusColor(limit.status)
                              }
                            }}
                          />
                          <Typography variant="caption" sx={{ color: getStatusColor(limit.status) }}>
                            사용률: {limit.utilization.toFixed(1)}%
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItem>
                  {index < riskLimits.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* VaR 추이 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Value at Risk (VaR) 추이
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <ComposedChart data={varHistory}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip
                  formatter={(value: number) => [formatCurrency(value), '']}
                />
                <Legend />
                <Bar dataKey="actualPL" fill="#f44336" name="실제 손익" />
                <Line type="monotone" dataKey="VaR95" stroke="#ff9800" strokeWidth={3} name="VaR 95%" />
                <Line type="monotone" dataKey="VaR99" stroke="#f44336" strokeWidth={2} name="VaR 99%" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 리스크 요인 분해 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              리스크 요인 분해
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <PieChart>
                <Pie
                  data={riskFactors}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="contribution"
                  label={({ factor, contribution }) => `${factor.split(' ')[0]}: ${contribution}%`}
                >
                  {riskFactors.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={['#8884d8', '#82ca9d', '#ffc658', '#ff7c7c', '#8dd1e1'][index]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 포지션별 리스크 분석 */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              포지션별 리스크 분석
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>종목</TableCell>
                    <TableCell align="center">포지션</TableCell>
                    <TableCell align="center">비중</TableCell>
                    <TableCell align="center">VaR</TableCell>
                    <TableCell align="center">베타</TableCell>
                    <TableCell align="center">추적오차</TableCell>
                    <TableCell align="center">유동성</TableCell>
                    <TableCell align="center">리스크 등급</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {positionRisks.map((position) => (
                    <TableRow key={position.symbol} hover>
                      <TableCell>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {position.symbol}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {formatCurrency(position.position)}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2" fontWeight="bold">
                          {position.weight}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2" color="error.main">
                          {formatCurrency(position.var)}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography
                          variant="body2"
                          sx={{ color: position.beta > 1 ? '#f44336' : '#4caf50' }}
                        >
                          {position.beta}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {position.tracking_error}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {position.liquidity_days}일
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={position.risk_level}
                          size="small"
                          sx={{
                            backgroundColor: getRiskLevelColor(position.risk_level),
                            color: 'white',
                            fontWeight: 'bold'
                          }}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* 스트레스 테스트 결과 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              스트레스 테스트 시나리오
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>시나리오</TableCell>
                    <TableCell align="center">발생 확률</TableCell>
                    <TableCell align="center">포트폴리오 영향</TableCell>
                    <TableCell align="center">지속 기간</TableCell>
                    <TableCell align="center">회복 기간</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {stressTestScenarios.map((scenario, index) => (
                    <TableRow key={index} hover>
                      <TableCell>
                        <Typography variant="subtitle2">
                          {scenario.scenario}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {scenario.probability}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography
                          variant="body2"
                          fontWeight="bold"
                          color="error.main"
                        >
                          {scenario.portfolioImpact}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {scenario.duration}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {scenario.recovery}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* 리스크 조정 수익률 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              리스크 조정 수익률
            </Typography>
            <List>
              {riskAdjustedReturns.map((item, index) => (
                <React.Fragment key={item.period}>
                  <ListItem>
                    <ListItemText
                      primary={item.period}
                      secondary={
                        <Box>
                          <Typography variant="body2" component="div">
                            수익률: {item.return}% | 샤프: {item.sharpe}
                          </Typography>
                          <Typography variant="body2" component="div">
                            소르티노: {item.sortino} | 칼마: {item.calmar}
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={(item.sharpe / 2.5) * 100}
                            sx={{ mt: 1, height: 4, borderRadius: 2 }}
                          />
                        </Box>
                      }
                    />
                  </ListItem>
                  {index < riskAdjustedReturns.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default RiskManagementPage;