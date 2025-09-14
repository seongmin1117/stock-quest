'use client';

import React, { useState } from 'react';
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
  IconButton,
  Menu,
  MenuProps,
  Alert,
  Divider,
  LinearProgress,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Assessment,
  AttachMoney,
  ShowChart,
  PieChart,
  Download,
  FilterList,
  Compare,
  Timeline,
  AccountBalance,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  ComposedChart,
  ScatterChart,
  Scatter,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
  Brush,
} from 'recharts';

const ReturnsAnalysisPage = () => {
  const [selectedPeriod, setSelectedPeriod] = useState('month');
  const [selectedMetric, setSelectedMetric] = useState('returns');
  const [selectedBenchmark, setSelectedBenchmark] = useState('sp500');
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // 수익률 통계 데이터
  const returnsStats = {
    avgReturns: 8.45,
    totalProfit: 2850000,
    winRate: 67.2,
    sharpeRatio: 1.34,
    maxDrawdown: -12.8,
    volatility: 18.2,
    bestPerformer: "김트레이더",
    bestReturn: 45.2
  };

  // 월별 수익률 데이터
  const monthlyReturns = [
    {
      month: '1월',
      포트폴리오: 5.2,
      KOSPI: 3.1,
      SP500: 4.8,
      참여자수: 340,
      승률: 68.5,
      평균수익: 1250000
    },
    {
      month: '2월',
      포트폴리오: -2.1,
      KOSPI: -1.5,
      SP500: 1.2,
      참여자수: 390,
      승률: 45.2,
      평균수익: -850000
    },
    {
      month: '3월',
      포트폴리오: 12.8,
      KOSPI: 8.4,
      SP500: 9.1,
      참여자수: 420,
      승률: 78.9,
      평균수익: 2850000
    },
    {
      month: '4월',
      포트폴리오: 3.7,
      KOSPI: 2.8,
      SP500: 3.2,
      참여자수: 380,
      승률: 62.1,
      평균수익: 980000
    },
    {
      month: '5월',
      포트폴리오: 7.9,
      KOSPI: 5.2,
      SP500: 6.8,
      참여자수: 450,
      승률: 72.4,
      평균수익: 1680000
    },
    {
      month: '6월',
      포트폴리오: 4.3,
      KOSPI: 3.9,
      SP500: 4.1,
      참여자수: 410,
      승률: 65.7,
      평균수익: 1150000
    },
  ];

  // 수익률 분포 데이터
  const returnsDistribution = [
    { range: '-20% 이하', count: 15, percentage: 3.2 },
    { range: '-20% ~ -10%', count: 45, percentage: 9.6 },
    { range: '-10% ~ 0%', count: 125, percentage: 26.8 },
    { range: '0% ~ 10%', count: 180, percentage: 38.5 },
    { range: '10% ~ 20%', count: 78, percentage: 16.7 },
    { range: '20% ~ 30%', count: 21, percentage: 4.5 },
    { range: '30% 이상', count: 4, percentage: 0.7 },
  ];

  // 섹터별 수익률
  const sectorReturns = [
    { sector: '기술주', 수익률: 15.8, 변동성: 24.5, 샤프비율: 0.64, 비중: 35 },
    { sector: '금융주', 수익률: 8.2, 변동성: 18.9, 샤프비율: 0.43, 비중: 25 },
    { sector: '바이오', 수익률: 22.1, 변동성: 32.8, 샤프비율: 0.67, 비중: 15 },
    { sector: '에너지', 수익률: 12.4, 변동성: 28.1, 샤프비율: 0.44, 비중: 12 },
    { sector: '소비재', 수익률: 6.8, 변동성: 15.2, 샤프비율: 0.45, 비중: 13 },
  ];

  // 리스크-수익률 분석
  const riskReturnAnalysis = [
    { 수익률: 2.1, 변동성: 8.5, 샤프비율: 0.25, 포트폴리오: '안전형', 참여자: 120 },
    { 수익률: 5.8, 변동성: 12.3, 샤프비율: 0.47, 포트폴리오: '보수형', 참여자: 180 },
    { 수익률: 8.9, 변동성: 16.8, 샤프비율: 0.53, 포트폴리오: '적극형', 참여자: 220 },
    { 수익률: 12.4, 변동성: 22.1, 샤프비율: 0.56, 포트폴리오: '공격형', 참여자: 160 },
    { 수익률: 18.7, 변동성: 31.5, 샤프비율: 0.59, 포트폴리오: '초공격형', 참여자: 90 },
  ];

  // 시간대별 거래 성과
  const timeBasedPerformance = [
    { time: '09:00-10:00', 수익률: 2.1, 거래량: 25, 성공확률: 62 },
    { time: '10:00-11:00', 수익률: 1.8, 거래량: 35, 성공확률: 58 },
    { time: '11:00-12:00', 수익률: 0.9, 거래량: 28, 성공확률: 52 },
    { time: '12:00-13:00', 수익률: -0.3, 거래량: 15, 성공확률: 48 },
    { time: '13:00-14:00', 수익률: 1.2, 거래량: 32, 성공확률: 55 },
    { time: '14:00-15:00', 수익률: 2.8, 거래량: 42, 성공확률: 65 },
    { time: '15:00-15:30', 수익률: 3.2, 거래량: 38, 성공확률: 68 },
  ];

  // 상위 수익률 포트폴리오
  const topPerformers = [
    {
      rank: 1,
      user: '김트레이더',
      총수익률: 45.2,
      월평균: 7.5,
      샤프비율: 1.85,
      최대낙폭: -8.2,
      거래횟수: 128,
      승률: 78.9
    },
    {
      rank: 2,
      user: '박투자자',
      총수익률: 38.9,
      월평균: 6.5,
      샤프비율: 1.62,
      최대낙폭: -12.1,
      거래횟수: 95,
      승률: 72.1
    },
    {
      rank: 3,
      user: '이애널리스트',
      총수익률: 35.6,
      월평균: 5.9,
      샤프비율: 1.48,
      최대낙폭: -9.8,
      거래횟수: 156,
      승률: 69.8
    },
    {
      rank: 4,
      user: '정퀀트',
      총수익률: 32.1,
      월평균: 5.4,
      샤프비율: 1.35,
      최대낙폭: -15.6,
      거래횟수: 203,
      승률: 65.4
    },
    {
      rank: 5,
      user: '최전략가',
      총수익률: 29.8,
      월평균: 5.0,
      샤프비율: 1.28,
      최대낙폭: -11.3,
      거래횟수: 89,
      승률: 71.2
    },
  ];

  const handleExportMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setExportMenuAnchor(event.currentTarget);
  };

  const handleExportMenuClose = () => {
    setExportMenuAnchor(null);
  };

  const handleExport = (format: string) => {
    console.log(`Exporting returns analysis data in ${format} format`);
    handleExportMenuClose();
  };

  const getReturnColor = (value: number) => {
    if (value > 0) return '#4CAF50';
    if (value < 0) return '#F44336';
    return '#757575';
  };

  const formatCurrency = (amount: number) => {
    if (Math.abs(amount) >= 1000000) {
      return `${(amount / 1000000).toFixed(1)}백만원`;
    } else if (Math.abs(amount) >= 10000) {
      return `${(amount / 10000).toFixed(0)}만원`;
    }
    return `${amount.toLocaleString()}원`;
  };

  const formatPercent = (value: number) => {
    return `${value > 0 ? '+' : ''}${value.toFixed(1)}%`;
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            수익률 분석 및 리포트
          </Typography>
          <Typography variant="body1" color="text.secondary">
            포트폴리오 수익률, 리스크 지표, 벤치마크 비교 분석
          </Typography>
        </Box>

        {/* 필터 및 내보내기 */}
        <Box display="flex" gap={2}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>기간</InputLabel>
            <Select
              value={selectedPeriod}
              label="기간"
              onChange={(e) => setSelectedPeriod(e.target.value)}
            >
              <MenuItem value="week">최근 1주</MenuItem>
              <MenuItem value="month">최근 1개월</MenuItem>
              <MenuItem value="quarter">최근 3개월</MenuItem>
              <MenuItem value="year">최근 1년</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>지표</InputLabel>
            <Select
              value={selectedMetric}
              label="지표"
              onChange={(e) => setSelectedMetric(e.target.value)}
            >
              <MenuItem value="returns">수익률</MenuItem>
              <MenuItem value="risk">리스크</MenuItem>
              <MenuItem value="sharpe">샤프비율</MenuItem>
              <MenuItem value="drawdown">최대낙폭</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>벤치마크</InputLabel>
            <Select
              value={selectedBenchmark}
              label="벤치마크"
              onChange={(e) => setSelectedBenchmark(e.target.value)}
            >
              <MenuItem value="kospi">KOSPI</MenuItem>
              <MenuItem value="sp500">S&P 500</MenuItem>
              <MenuItem value="nasdaq">NASDAQ</MenuItem>
            </Select>
          </FormControl>

          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={handleExportMenuOpen}
          >
            리포트 생성
          </Button>
          <Menu
            anchorEl={exportMenuAnchor}
            open={Boolean(exportMenuAnchor)}
            onClose={handleExportMenuClose}
          >
            <MenuItem onClick={() => handleExport('PDF')}>PDF 리포트</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>Excel 분석</MenuItem>
            <MenuItem onClick={() => handleExport('PowerPoint')}>PPT 프레젠테이션</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 주요 수익률 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography
                    variant="h4"
                    sx={{ color: getReturnColor(returnsStats.avgReturns) }}
                  >
                    {formatPercent(returnsStats.avgReturns)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 수익률
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
                  <Typography variant="h4" color="primary.main">
                    {formatCurrency(returnsStats.totalProfit)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 수익
                  </Typography>
                </Box>
                <AttachMoney color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {returnsStats.winRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    승률
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
                    {returnsStats.sharpeRatio}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    샤프 비율
                  </Typography>
                </Box>
                <ShowChart color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 성과 알림 */}
      <Alert severity="info" sx={{ mb: 3 }}>
        <Typography variant="body2">
          <strong>이번 달 최고 성과:</strong> {returnsStats.bestPerformer}님이 {formatPercent(returnsStats.bestReturn)} 수익률을 달성했습니다.
          벤치마크 대비 {formatPercent(returnsStats.bestReturn - 4.1)} 초과 수익을 기록하고 있습니다.
        </Typography>
      </Alert>

      <Grid container spacing={3}>
        {/* 월별 수익률 트렌드 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              월별 수익률 및 벤치마크 비교
            </Typography>
            <ResponsiveContainer width="100%" height={400}>
              <ComposedChart data={monthlyReturns}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip
                  formatter={(value: any, name: string) => {
                    if (name.includes('수익')) {
                      return [formatCurrency(value), name];
                    }
                    return [name.includes('률') ? `${value}%` : value, name];
                  }}
                />
                <Legend />
                <ReferenceLine y={0} stroke="#666" strokeDasharray="2 2" />
                <Bar yAxisId="right" dataKey="참여자수" fill="#e3f2fd" name="참여자 수" />
                <Line yAxisId="left" type="monotone" dataKey="포트폴리오" stroke="#2196f3" strokeWidth={3} name="포트폴리오 수익률" />
                <Line yAxisId="left" type="monotone" dataKey="KOSPI" stroke="#ff9800" strokeWidth={2} name="KOSPI" />
                <Line yAxisId="left" type="monotone" dataKey="SP500" stroke="#4caf50" strokeWidth={2} name="S&P 500" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 리스크 지표 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              리스크 지표
            </Typography>
            <Box mb={3}>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="body2">변동성 (표준편차)</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {returnsStats.volatility}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={returnsStats.volatility}
                sx={{ height: 8, borderRadius: 1 }}
              />
            </Box>

            <Box mb={3}>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="body2">최대 낙폭 (MDD)</Typography>
                <Typography variant="body2" fontWeight="bold" color="error.main">
                  {returnsStats.maxDrawdown}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={Math.abs(returnsStats.maxDrawdown)}
                color="error"
                sx={{ height: 8, borderRadius: 1 }}
              />
            </Box>

            <Divider sx={{ my: 2 }} />

            <Box>
              <Typography variant="subtitle2" gutterBottom>
                리스크 조정 수익률
              </Typography>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Typography variant="body2">샤프 비율</Typography>
                <Chip
                  label={returnsStats.sharpeRatio}
                  color={returnsStats.sharpeRatio > 1 ? "success" : "warning"}
                  size="small"
                />
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* 수익률 분포 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              수익률 분포
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={returnsDistribution}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="range" angle={-45} textAnchor="end" height={80} />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#8884d8" name="참여자 수" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 리스크-수익률 분석 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              리스크-수익률 분석
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <ScatterChart data={riskReturnAnalysis}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="변동성" name="변동성" unit="%" />
                <YAxis dataKey="수익률" name="수익률" unit="%" />
                <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                <Scatter
                  dataKey="수익률"
                  fill="#8884d8"
                  r={(entry: any) => 4 + (entry.참여자 / 50)}
                />
              </ScatterChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 섹터별 성과 */}
        <Grid item xs={12} lg={7}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              섹터별 투자 성과
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>섹터</TableCell>
                    <TableCell align="center">수익률</TableCell>
                    <TableCell align="center">변동성</TableCell>
                    <TableCell align="center">샤프비율</TableCell>
                    <TableCell align="center">포트폴리오 비중</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sectorReturns.map((sector) => (
                    <TableRow key={sector.sector}>
                      <TableCell component="th" scope="row">
                        {sector.sector}
                      </TableCell>
                      <TableCell align="center">
                        <Typography
                          variant="body2"
                          sx={{ color: getReturnColor(sector.수익률) }}
                          fontWeight="bold"
                        >
                          {formatPercent(sector.수익률)}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {sector.변동성}%
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={sector.샤프비율}
                          size="small"
                          color={sector.샤프비율 > 0.5 ? "success" : "warning"}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Box>
                          <Typography variant="body2" mb={0.5}>
                            {sector.비중}%
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={sector.비중}
                            sx={{ height: 4, borderRadius: 1 }}
                          />
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* 상위 수익률 포트폴리오 */}
        <Grid item xs={12} lg={5}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              상위 수익률 랭킹
            </Typography>
            {topPerformers.map((performer) => (
              <Box key={performer.rank} mb={2.5}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Chip
                      label={`#${performer.rank}`}
                      size="small"
                      color="primary"
                    />
                    <Typography variant="subtitle2">
                      {performer.user}
                    </Typography>
                  </Box>
                  <Typography
                    variant="h6"
                    sx={{ color: getReturnColor(performer.총수익률) }}
                    fontWeight="bold"
                  >
                    {formatPercent(performer.총수익률)}
                  </Typography>
                </Box>

                <Grid container spacing={2} sx={{ fontSize: '0.75rem' }}>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      월평균: {formatPercent(performer.월평균)}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      샤프비율: {performer.샤프비율}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      승률: {performer.승률}%
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      거래: {performer.거래횟수}회
                    </Typography>
                  </Grid>
                </Grid>

                <Box mt={1}>
                  <LinearProgress
                    variant="determinate"
                    value={Math.min(performer.총수익률, 50)}
                    sx={{
                      height: 6,
                      borderRadius: 1,
                      '& .MuiLinearProgress-bar': {
                        backgroundColor: getReturnColor(performer.총수익률)
                      }
                    }}
                  />
                </Box>

                {performer.rank < topPerformers.length && (
                  <Box sx={{ borderBottom: '1px solid #eee', mt: 2 }} />
                )}
              </Box>
            ))}
          </Paper>
        </Grid>

        {/* 시간대별 거래 성과 */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              시간대별 거래 성과 분석
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <ComposedChart data={timeBasedPerformance}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="right" dataKey="거래량" fill="#e3f2fd" name="거래량" />
                <Line yAxisId="right" type="monotone" dataKey="성공확률" stroke="#ff9800" name="성공 확률 (%)" />
                <Line yAxisId="left" type="monotone" dataKey="수익률" stroke="#4caf50" strokeWidth={3} name="평균 수익률 (%)" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default ReturnsAnalysisPage;