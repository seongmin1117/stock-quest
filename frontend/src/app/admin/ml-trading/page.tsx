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
  CircularProgress,
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
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Psychology,
  Speed,
  Assessment,
  Refresh,
  Download,
  Settings,
  NotificationsActive,
  CheckCircle,
  Warning,
  Error,
  Autorenew,
  Analytics,
  Timeline,
  ShowChart,
  SmartToy,
  Insights,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
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
  Cell,
} from 'recharts';

const MLTradingDashboard = () => {
  const [selectedModel, setSelectedModel] = useState('ensemble');
  const [selectedTimeframe, setSelectedTimeframe] = useState('1h');
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(new Date());
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // ML 모델 상태 데이터
  const modelStatus = {
    overallHealth: 'healthy', // healthy, warning, critical
    activeModels: 8,
    totalSignals: 156,
    accuracyRate: 78.5,
    profitability: 12.8,
    riskScore: 2.4,
    lastTraining: '2024-01-15 03:00:00'
  };

  // 실시간 AI 트레이딩 신호
  const tradingSignals = [
    {
      id: 1,
      symbol: 'AAPL',
      signal: 'BUY',
      confidence: 85.2,
      targetPrice: 195.50,
      currentPrice: 189.25,
      expectedReturn: 3.3,
      riskLevel: 'Low',
      model: 'LSTM',
      timestamp: '2024-01-15 14:23:45',
      reasoning: '기술적 지표 상승 패턴 및 거래량 증가'
    },
    {
      id: 2,
      symbol: 'TSLA',
      signal: 'SELL',
      confidence: 72.8,
      targetPrice: 210.00,
      currentPrice: 238.75,
      expectedReturn: -12.0,
      riskLevel: 'Medium',
      model: 'Transformer',
      timestamp: '2024-01-15 14:20:12',
      reasoning: '과매수 상태 및 모멘텀 약화 신호'
    },
    {
      id: 3,
      symbol: 'NVDA',
      signal: 'HOLD',
      confidence: 68.4,
      targetPrice: 875.00,
      currentPrice: 874.50,
      expectedReturn: 0.1,
      riskLevel: 'Low',
      model: 'GAN',
      timestamp: '2024-01-15 14:18:30',
      reasoning: '중립적 시장 상황, 변동성 대기'
    },
    {
      id: 4,
      symbol: 'MSFT',
      signal: 'BUY',
      confidence: 91.7,
      targetPrice: 425.00,
      currentPrice: 408.30,
      expectedReturn: 4.1,
      riskLevel: 'Low',
      model: 'Ensemble',
      timestamp: '2024-01-15 14:15:22',
      reasoning: '강력한 펀더멘털 및 기술적 돌파 신호'
    },
    {
      id: 5,
      symbol: 'GOOGL',
      signal: 'BUY',
      confidence: 76.3,
      targetPrice: 152.00,
      currentPrice: 145.80,
      expectedReturn: 4.3,
      riskLevel: 'Medium',
      model: 'CNN',
      timestamp: '2024-01-15 14:12:18',
      reasoning: '검색 트렌드 상승 및 광고 수익 개선'
    }
  ];

  // 모델 성능 비교 데이터
  const modelPerformance = [
    {
      period: '1월 1주',
      LSTM: 12.5,
      Transformer: 8.9,
      GAN: 15.2,
      CNN: 6.7,
      Ensemble: 18.3
    },
    {
      period: '1월 2주',
      LSTM: 14.8,
      Transformer: 11.2,
      GAN: 9.8,
      CNN: 13.5,
      Ensemble: 21.7
    },
    {
      period: '1월 3주',
      LSTM: 8.2,
      Transformer: 15.6,
      GAN: 12.4,
      CNN: 9.1,
      Ensemble: 16.8
    },
    {
      period: '1월 4주',
      LSTM: 16.9,
      Transformer: 13.4,
      GAN: 18.7,
      CNN: 11.3,
      Ensemble: 22.1
    }
  ];

  // 신호 정확도 추적
  const accuracyTracking = [
    { time: '09:00', 정확도: 82.5, 신호수: 12, 수익률: 2.8 },
    { time: '10:00', 정확도: 78.9, 신호수: 18, 수익률: 1.9 },
    { time: '11:00', 정확도: 85.2, 신호수: 15, 수익률: 3.4 },
    { time: '12:00', 정확도: 73.4, 신호수: 22, 수익률: 0.8 },
    { time: '13:00', 정확도: 89.1, 신호수: 14, 수익률: 4.2 },
    { time: '14:00', 정확도: 81.7, 신호수: 19, 수익률: 2.6 },
  ];

  // 백테스팅 결과
  const backtestingResults = [
    {
      strategy: 'Momentum Strategy',
      period: '3개월',
      totalReturn: 24.5,
      sharpeRatio: 1.68,
      maxDrawdown: -8.2,
      winRate: 67.8,
      trades: 145,
      model: 'LSTM'
    },
    {
      strategy: 'Mean Reversion',
      period: '3개월',
      totalReturn: 18.9,
      sharpeRatio: 1.45,
      maxDrawdown: -6.4,
      winRate: 72.3,
      trades: 198,
      model: 'Transformer'
    },
    {
      strategy: 'Arbitrage Detection',
      period: '3개월',
      totalReturn: 31.2,
      sharpeRatio: 2.14,
      maxDrawdown: -4.8,
      winRate: 84.6,
      trades: 89,
      model: 'GAN'
    },
    {
      strategy: 'Pattern Recognition',
      period: '3개월',
      totalReturn: 15.6,
      sharpeRatio: 1.32,
      maxDrawdown: -12.1,
      winRate: 59.4,
      trades: 267,
      model: 'CNN'
    }
  ];

  // AI 모델 상태
  const aiModels = [
    {
      name: 'LSTM Model',
      status: 'active',
      accuracy: 85.2,
      lastTrained: '2024-01-14 22:30:00',
      dataPoints: 125000,
      version: 'v2.1.3'
    },
    {
      name: 'Transformer Model',
      status: 'active',
      accuracy: 82.7,
      lastTrained: '2024-01-15 01:15:00',
      dataPoints: 98000,
      version: 'v1.8.2'
    },
    {
      name: 'GAN Model',
      status: 'training',
      accuracy: 88.4,
      lastTrained: '2024-01-15 03:45:00',
      dataPoints: 156000,
      version: 'v3.0.1'
    },
    {
      name: 'CNN Model',
      status: 'active',
      accuracy: 79.1,
      lastTrained: '2024-01-14 20:20:00',
      dataPoints: 87000,
      version: 'v1.5.4'
    },
    {
      name: 'Ensemble Model',
      status: 'active',
      accuracy: 91.3,
      lastTrained: '2024-01-15 04:00:00',
      dataPoints: 200000,
      version: 'v2.3.1'
    }
  ];

  // 자동 새로고침
  useEffect(() => {
    let interval: NodeJS.Timeout;

    if (autoRefresh) {
      interval = setInterval(() => {
        setLastUpdate(new Date());
      }, 15000); // 15초마다 새로고침
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [autoRefresh]);

  const handleExportMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setExportMenuAnchor(event.currentTarget);
  };

  const handleExportMenuClose = () => {
    setExportMenuAnchor(null);
  };

  const handleExport = (format: string) => {
    console.log(`Exporting ML trading data in ${format} format`);
    handleExportMenuClose();
  };

  const getSignalColor = (signal: string) => {
    switch (signal) {
      case 'BUY': return '#4CAF50';
      case 'SELL': return '#F44336';
      case 'HOLD': return '#FF9800';
      default: return '#757575';
    }
  };

  const getSignalIcon = (signal: string) => {
    switch (signal) {
      case 'BUY': return <TrendingUp sx={{ color: '#4CAF50' }} />;
      case 'SELL': return <TrendingDown sx={{ color: '#F44336' }} />;
      case 'HOLD': return <TrendingUp sx={{ color: '#FF9800', transform: 'rotate(90deg)' }} />;
      default: return <TrendingUp />;
    }
  };

  const getRiskColor = (risk: string) => {
    switch (risk) {
      case 'Low': return '#4CAF50';
      case 'Medium': return '#FF9800';
      case 'High': return '#F44336';
      default: return '#757575';
    }
  };

  const getModelStatusColor = (status: string) => {
    switch (status) {
      case 'active': return '#4CAF50';
      case 'training': return '#2196F3';
      case 'error': return '#F44336';
      case 'inactive': return '#757575';
      default: return '#757575';
    }
  };

  const getModelStatusIcon = (status: string) => {
    switch (status) {
      case 'active': return <CheckCircle sx={{ color: '#4CAF50' }} />;
      case 'training': return <Autorenew sx={{ color: '#2196F3' }} />;
      case 'error': return <Error sx={{ color: '#F44336' }} />;
      case 'inactive': return <Warning sx={{ color: '#757575' }} />;
      default: return <CheckCircle />;
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            ML 트레이딩 신호 대시보드
          </Typography>
          <Typography variant="body1" color="text.secondary">
            실시간 AI 트레이딩 신호, 모델 성능 분석, 백테스팅 결과
          </Typography>
        </Box>

        {/* 컨트롤 패널 */}
        <Box display="flex" gap={2} alignItems="center">
          <Typography variant="body2" color="text.secondary">
            마지막 업데이트: {lastUpdate.toLocaleTimeString()}
          </Typography>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>모델</InputLabel>
            <Select
              value={selectedModel}
              label="모델"
              onChange={(e) => setSelectedModel(e.target.value)}
            >
              <MenuItem value="all">전체</MenuItem>
              <MenuItem value="lstm">LSTM</MenuItem>
              <MenuItem value="transformer">Transformer</MenuItem>
              <MenuItem value="gan">GAN</MenuItem>
              <MenuItem value="cnn">CNN</MenuItem>
              <MenuItem value="ensemble">Ensemble</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 100 }}>
            <InputLabel>주기</InputLabel>
            <Select
              value={selectedTimeframe}
              label="주기"
              onChange={(e) => setSelectedTimeframe(e.target.value)}
            >
              <MenuItem value="5m">5분</MenuItem>
              <MenuItem value="15m">15분</MenuItem>
              <MenuItem value="1h">1시간</MenuItem>
              <MenuItem value="4h">4시간</MenuItem>
              <MenuItem value="1d">1일</MenuItem>
            </Select>
          </FormControl>

          <FormControlLabel
            control={
              <Switch
                checked={autoRefresh}
                onChange={(e) => setAutoRefresh(e.target.checked)}
              />
            }
            label="자동새로고침"
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
            <MenuItem onClick={() => handleExport('PDF')}>신호 리포트 (PDF)</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>백테스팅 결과 (Excel)</MenuItem>
            <MenuItem onClick={() => handleExport('JSON')}>모델 데이터 (JSON)</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 시스템 상태 알림 */}
      <Alert
        severity={modelStatus.overallHealth === 'healthy' ? 'success' :
                 modelStatus.overallHealth === 'warning' ? 'warning' : 'error'}
        sx={{ mb: 3 }}
        icon={<SmartToy />}
      >
        <Typography variant="body2">
          <strong>AI 시스템 상태: {modelStatus.overallHealth === 'healthy' ? '정상' :
                                    modelStatus.overallHealth === 'warning' ? '주의' : '위험'}</strong>
          {' '} | 활성 모델: {modelStatus.activeModels}개 | 금일 신호: {modelStatus.totalSignals}개 |
          평균 정확도: {modelStatus.accuracyRate}%
        </Typography>
      </Alert>

      {/* 주요 ML 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="success.main">
                    {modelStatus.accuracyRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    신호 정확도
                  </Typography>
                </Box>
                <Assessment color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    +{modelStatus.profitability}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    누적 수익률
                  </Typography>
                </Box>
                <TrendingUp color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {modelStatus.totalSignals}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    금일 신호 수
                  </Typography>
                </Box>
                <NotificationsActive color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {modelStatus.riskScore}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    리스크 스코어
                  </Typography>
                </Box>
                <Speed color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 실시간 트레이딩 신호 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">
                실시간 AI 트레이딩 신호
              </Typography>
              <Badge badgeContent={tradingSignals.length} color="primary">
                <NotificationsActive />
              </Badge>
            </Box>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>종목</TableCell>
                    <TableCell align="center">신호</TableCell>
                    <TableCell align="center">신뢰도</TableCell>
                    <TableCell align="center">목표가</TableCell>
                    <TableCell align="center">예상수익</TableCell>
                    <TableCell align="center">리스크</TableCell>
                    <TableCell align="center">모델</TableCell>
                    <TableCell>시간</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {tradingSignals.map((signal) => (
                    <TableRow key={signal.id} hover>
                      <TableCell>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {signal.symbol}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          ${signal.currentPrice}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Box display="flex" alignItems="center" justifyContent="center" gap={1}>
                          {getSignalIcon(signal.signal)}
                          <Chip
                            label={signal.signal}
                            size="small"
                            sx={{
                              backgroundColor: getSignalColor(signal.signal),
                              color: 'white',
                              fontWeight: 'bold'
                            }}
                          />
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Box>
                          <Typography variant="body2" fontWeight="bold">
                            {signal.confidence}%
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={signal.confidence}
                            sx={{ height: 4, borderRadius: 2, mt: 0.5 }}
                          />
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          ${signal.targetPrice}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography
                          variant="body2"
                          fontWeight="bold"
                          sx={{ color: signal.expectedReturn > 0 ? '#4CAF50' : '#F44336' }}
                        >
                          {signal.expectedReturn > 0 ? '+' : ''}{signal.expectedReturn}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={signal.riskLevel}
                          size="small"
                          sx={{
                            backgroundColor: getRiskColor(signal.riskLevel),
                            color: 'white'
                          }}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2" color="primary">
                          {signal.model}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption">
                          {new Date(signal.timestamp).toLocaleTimeString()}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* AI 모델 상태 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              AI 모델 상태
            </Typography>
            <List>
              {aiModels.map((model, index) => (
                <React.Fragment key={model.name}>
                  <ListItem>
                    <ListItemIcon>
                      {getModelStatusIcon(model.status)}
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box display="flex" justifyContent="space-between" alignItems="center">
                          <Typography variant="subtitle2">
                            {model.name}
                          </Typography>
                          <Typography variant="body2" color="primary">
                            {model.version}
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" component="div">
                            정확도: {model.accuracy}% | 데이터: {model.dataPoints.toLocaleString()}개
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            마지막 학습: {new Date(model.lastTrained).toLocaleString()}
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={model.accuracy}
                            sx={{ mt: 1, height: 4, borderRadius: 2 }}
                          />
                        </Box>
                      }
                    />
                  </ListItem>
                  {index < aiModels.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* 모델 성능 비교 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              AI 모델 성능 비교 (수익률 %)
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <ComposedChart data={modelPerformance}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="period" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="LSTM" fill="#8884d8" name="LSTM" />
                <Line type="monotone" dataKey="Transformer" stroke="#82ca9d" strokeWidth={3} name="Transformer" />
                <Line type="monotone" dataKey="GAN" stroke="#ffc658" strokeWidth={3} name="GAN" />
                <Line type="monotone" dataKey="CNN" stroke="#ff7c7c" strokeWidth={2} name="CNN" />
                <Line type="monotone" dataKey="Ensemble" stroke="#8dd1e1" strokeWidth={4} name="Ensemble" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 실시간 정확도 추적 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              실시간 정확도 추적
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <ComposedChart data={accuracyTracking}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="right" dataKey="신호수" fill="#e3f2fd" name="신호 수" />
                <Line yAxisId="left" type="monotone" dataKey="정확도" stroke="#2196f3" strokeWidth={3} name="정확도 (%)" />
                <Line yAxisId="left" type="monotone" dataKey="수익률" stroke="#4caf50" strokeWidth={3} name="수익률 (%)" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 백테스팅 결과 */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              백테스팅 결과 및 전략 성과
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>전략</TableCell>
                    <TableCell align="center">기간</TableCell>
                    <TableCell align="center">총 수익률</TableCell>
                    <TableCell align="center">샤프 비율</TableCell>
                    <TableCell align="center">최대 낙폭</TableCell>
                    <TableCell align="center">승률</TableCell>
                    <TableCell align="center">거래 수</TableCell>
                    <TableCell align="center">AI 모델</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {backtestingResults.map((result, index) => (
                    <TableRow key={index} hover>
                      <TableCell>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {result.strategy}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {result.period}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography
                          variant="body2"
                          fontWeight="bold"
                          sx={{ color: result.totalReturn > 0 ? '#4CAF50' : '#F44336' }}
                        >
                          +{result.totalReturn}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {result.sharpeRatio}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography
                          variant="body2"
                          sx={{ color: '#F44336' }}
                        >
                          {result.maxDrawdown}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Box>
                          <Typography variant="body2" fontWeight="bold">
                            {result.winRate}%
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={result.winRate}
                            sx={{ height: 4, borderRadius: 2, mt: 0.5 }}
                          />
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {result.trades}회
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={result.model}
                          size="small"
                          color="primary"
                          variant="outlined"
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
    </Container>
  );
};

export default MLTradingDashboard;