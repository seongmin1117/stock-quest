'use client';

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  LinearProgress,
  IconButton,
  Menu,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Group,
  EmojiEvents,
  Assessment,
  Timeline,
  PieChart,
  BarChart,
  ShowChart,
  FileDownload,
  Refresh,
  DateRange,
  FilterList
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart as RechartsBarChart,
  Bar,
  PieChart as RechartsPieChart,
  Cell,
  Pie,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';

interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalChallenges: number;
  activeChallenges: number;
  totalTrades: number;
  avgReturn: number;
  totalVolume: number;
  successRate: number;
}

interface TimeSeriesData {
  date: string;
  users: number;
  trades: number;
  volume: number;
  returns: number;
}

interface ChallengeStats {
  id: number;
  name: string;
  participants: number;
  completionRate: number;
  avgReturn: number;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
}

interface UserPerformanceData {
  rank: number;
  username: string;
  fullName: string;
  totalReturn: number;
  winRate: number;
  challengesCompleted: number;
  riskScore: number;
}

const mockStats: DashboardStats = {
  totalUsers: 1284,
  activeUsers: 856,
  totalChallenges: 45,
  activeChallenges: 12,
  totalTrades: 15678,
  avgReturn: 8.5,
  totalVolume: 2450000000,
  successRate: 73.2
};

const mockTimeSeriesData: TimeSeriesData[] = [
  { date: '2024-09-01', users: 750, trades: 1200, volume: 180000000, returns: 6.2 },
  { date: '2024-09-02', users: 762, trades: 1350, volume: 195000000, returns: 7.1 },
  { date: '2024-09-03', users: 778, trades: 1180, volume: 172000000, returns: 5.8 },
  { date: '2024-09-04', users: 801, trades: 1420, volume: 208000000, returns: 8.3 },
  { date: '2024-09-05', users: 823, trades: 1650, volume: 235000000, returns: 9.1 },
  { date: '2024-09-06', users: 834, trades: 1580, volume: 224000000, returns: 8.7 },
  { date: '2024-09-07', users: 856, trades: 1750, volume: 265000000, returns: 10.2 }
];

const mockChallengeStats: ChallengeStats[] = [
  { id: 1, name: '2020년 코로나 급락', participants: 145, completionRate: 78.6, avgReturn: 12.3, difficulty: 'INTERMEDIATE' },
  { id: 2, name: '상승장 전략', participants: 203, completionRate: 85.2, avgReturn: 15.7, difficulty: 'BEGINNER' },
  { id: 3, name: '고변동성 대응', participants: 89, completionRate: 62.9, avgReturn: 8.1, difficulty: 'ADVANCED' },
  { id: 4, name: 'ESG 투자', participants: 167, completionRate: 91.0, avgReturn: 11.2, difficulty: 'BEGINNER' },
  { id: 5, name: '섹터 로테이션', participants: 124, completionRate: 71.8, avgReturn: 9.8, difficulty: 'INTERMEDIATE' }
];

const mockTopPerformers: UserPerformanceData[] = [
  { rank: 1, username: 'trading_master', fullName: '김트레이더', totalReturn: 45.2, winRate: 89.3, challengesCompleted: 23, riskScore: 4.1 },
  { rank: 2, username: 'value_investor', fullName: '박가치', totalReturn: 38.7, winRate: 82.1, challengesCompleted: 19, riskScore: 2.8 },
  { rank: 3, username: 'tech_growth', fullName: '이성장', totalReturn: 35.4, winRate: 79.5, challengesCompleted: 21, riskScore: 5.2 },
  { rank: 4, username: 'dividend_king', fullName: '최배당', totalReturn: 32.1, winRate: 85.7, challengesCompleted: 17, riskScore: 2.1 },
  { rank: 5, username: 'momentum_trader', fullName: '정모멘텀', totalReturn: 29.8, winRate: 76.2, challengesCompleted: 25, riskScore: 6.3 }
];

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8'];

export default function AnalyticsPage() {
  const [dateRange, setDateRange] = useState<'7d' | '30d' | '90d' | '1y'>('30d');
  const [loading, setLoading] = useState(false);
  const [exportAnchorEl, setExportAnchorEl] = useState<null | HTMLElement>(null);
  const [stats] = useState<DashboardStats>(mockStats);
  const [timeSeriesData] = useState<TimeSeriesData[]>(mockTimeSeriesData);
  const [challengeStats] = useState<ChallengeStats[]>(mockChallengeStats);
  const [topPerformers] = useState<UserPerformanceData[]>(mockTopPerformers);

  const handleRefresh = async () => {
    setLoading(true);
    // TODO: 실제 데이터 새로고침 API 호출
    await new Promise(resolve => setTimeout(resolve, 1000));
    setLoading(false);
  };

  const handleExportClick = (event: React.MouseEvent<HTMLElement>) => {
    setExportAnchorEl(event.currentTarget);
  };

  const handleExportClose = () => {
    setExportAnchorEl(null);
  };

  const handleExport = (format: 'pdf' | 'excel' | 'csv') => {
    // TODO: 실제 내보내기 구현
    console.log(`Exporting analytics in ${format} format`);
    handleExportClose();
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      notation: 'compact',
      maximumFractionDigits: 1
    }).format(value);
  };

  const formatPercent = (value: number) => {
    return `${value > 0 ? '+' : ''}${value.toFixed(1)}%`;
  };

  const getDifficultyChip = (difficulty: ChallengeStats['difficulty']) => {
    const config = {
      BEGINNER: { label: '초급', color: 'success' as const },
      INTERMEDIATE: { label: '중급', color: 'warning' as const },
      ADVANCED: { label: '고급', color: 'error' as const }
    };

    return (
      <Chip
        label={config[difficulty].label}
        color={config[difficulty].color}
        size="small"
        variant="outlined"
      />
    );
  };

  const pieChartData = challengeStats.map(challenge => ({
    name: challenge.name,
    value: challenge.participants,
    fill: COLORS[challengeStats.indexOf(challenge) % COLORS.length]
  }));

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
              분석 대시보드
            </Typography>
            <Typography variant="body1" color="text.secondary">
              플랫폼 성과와 사용자 활동을 실시간으로 분석하고 모니터링하세요
            </Typography>
          </Box>
          <Box display="flex" gap={1}>
            <FormControl size="small">
              <InputLabel>기간</InputLabel>
              <Select
                value={dateRange}
                onChange={(e) => setDateRange(e.target.value as typeof dateRange)}
                label="기간"
                startAdornment={<DateRange sx={{ mr: 1 }} />}
              >
                <MenuItem value="7d">최근 7일</MenuItem>
                <MenuItem value="30d">최근 30일</MenuItem>
                <MenuItem value="90d">최근 90일</MenuItem>
                <MenuItem value="1y">최근 1년</MenuItem>
              </Select>
            </FormControl>
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={handleRefresh}
              disabled={loading}
            >
              새로고침
            </Button>
            <Button
              variant="contained"
              startIcon={<FileDownload />}
              onClick={handleExportClick}
            >
              내보내기
            </Button>
          </Box>
        </Box>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* 주요 지표 카드 */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    {stats.totalUsers.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 사용자
                  </Typography>
                  <Typography variant="caption" color="success.main">
                    +{stats.activeUsers} 활성
                  </Typography>
                </Box>
                <Group color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {stats.totalChallenges}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 챌린지
                  </Typography>
                  <Typography variant="caption" color="primary.main">
                    {stats.activeChallenges}개 진행중
                  </Typography>
                </Box>
                <EmojiEvents color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {stats.totalTrades.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 거래 수
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {formatCurrency(stats.totalVolume)} 거래량
                  </Typography>
                </Box>
                <Timeline color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color={stats.avgReturn > 0 ? 'success.main' : 'error.main'}>
                    {formatPercent(stats.avgReturn)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 수익률
                  </Typography>
                  <Typography variant="caption" color="success.main">
                    성공률 {stats.successRate}%
                  </Typography>
                </Box>
                {stats.avgReturn > 0 ? (
                  <TrendingUp color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
                ) : (
                  <TrendingDown color="error" sx={{ fontSize: 40, opacity: 0.7 }} />
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 시계열 차트 */}
        <Grid item xs={12} lg={8}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">
                  사용자 활동 추이
                </Typography>
                <ShowChart color="primary" />
              </Box>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={timeSeriesData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(value) => new Date(value).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                  />
                  <YAxis />
                  <Tooltip
                    labelFormatter={(value) => new Date(value).toLocaleDateString('ko-KR')}
                    formatter={(value: number, name: string) => [
                      name === 'users' ? `${value}명` :
                      name === 'trades' ? `${value}건` :
                      name === 'volume' ? formatCurrency(value) :
                      formatPercent(value),
                      name === 'users' ? '활성 사용자' :
                      name === 'trades' ? '거래 수' :
                      name === 'volume' ? '거래량' :
                      '수익률'
                    ]}
                  />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="users"
                    stroke="#8884d8"
                    strokeWidth={2}
                    name="활성 사용자"
                  />
                  <Line
                    type="monotone"
                    dataKey="trades"
                    stroke="#82ca9d"
                    strokeWidth={2}
                    name="거래 수"
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* 수익률 차트 */}
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">
                  수익률 및 거래량 추이
                </Typography>
                <BarChart color="primary" />
              </Box>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={timeSeriesData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(value) => new Date(value).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                  />
                  <YAxis />
                  <Tooltip
                    labelFormatter={(value) => new Date(value).toLocaleDateString('ko-KR')}
                    formatter={(value: number, name: string) => [
                      name === 'returns' ? formatPercent(value) : formatCurrency(value),
                      name === 'returns' ? '평균 수익률' : '거래량'
                    ]}
                  />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="returns"
                    stackId="1"
                    stroke="#8884d8"
                    fill="#8884d8"
                    fillOpacity={0.6}
                    name="평균 수익률"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* 사이드 패널 */}
        <Grid item xs={12} lg={4}>
          {/* 챌린지 참여 분포 */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">
                  챌린지 참여 분포
                </Typography>
                <PieChart color="primary" />
              </Box>
              <ResponsiveContainer width="100%" height={250}>
                <RechartsPieChart>
                  <Pie
                    data={pieChartData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {pieChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </RechartsPieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* 상위 성과자 */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                상위 성과자 (Top 5)
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>순위</TableCell>
                      <TableCell>사용자</TableCell>
                      <TableCell align="right">수익률</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {topPerformers.map((user) => (
                      <TableRow key={user.rank}>
                        <TableCell>
                          <Chip
                            label={user.rank}
                            size="small"
                            color={
                              user.rank === 1 ? 'error' :
                              user.rank === 2 ? 'warning' :
                              user.rank === 3 ? 'success' : 'default'
                            }
                          />
                        </TableCell>
                        <TableCell>
                          <Box>
                            <Typography variant="subtitle2">
                              {user.fullName}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              @{user.username}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Typography
                            variant="subtitle2"
                            color={user.totalReturn > 0 ? 'success.main' : 'error.main'}
                          >
                            {formatPercent(user.totalReturn)}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" display="block">
                            승률 {user.winRate.toFixed(1)}%
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* 챌린지 성과 테이블 */}
        <Grid item xs={12}>
          <Card sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                챌린지 성과 분석
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>챌린지명</TableCell>
                      <TableCell>난이도</TableCell>
                      <TableCell align="right">참여자</TableCell>
                      <TableCell align="right">완료율</TableCell>
                      <TableCell align="right">평균 수익률</TableCell>
                      <TableCell align="center">성과</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {challengeStats.map((challenge) => (
                      <TableRow key={challenge.id} hover>
                        <TableCell>
                          <Typography variant="subtitle2">
                            {challenge.name}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          {getDifficultyChip(challenge.difficulty)}
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2">
                            {challenge.participants}명
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Box>
                            <Typography variant="body2">
                              {challenge.completionRate.toFixed(1)}%
                            </Typography>
                            <LinearProgress
                              variant="determinate"
                              value={challenge.completionRate}
                              sx={{ width: 60, mt: 0.5 }}
                            />
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Typography
                            variant="body2"
                            color={challenge.avgReturn > 0 ? 'success.main' : 'error.main'}
                          >
                            {formatPercent(challenge.avgReturn)}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          {challenge.avgReturn > 10 ? (
                            <TrendingUp color="success" />
                          ) : challenge.avgReturn > 5 ? (
                            <TrendingUp color="primary" />
                          ) : (
                            <TrendingDown color="warning" />
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 내보내기 메뉴 */}
      <Menu
        anchorEl={exportAnchorEl}
        open={Boolean(exportAnchorEl)}
        onClose={handleExportClose}
      >
        <MenuItem onClick={() => handleExport('pdf')}>
          <ListItemIcon>
            <FileDownload fontSize="small" />
          </ListItemIcon>
          <ListItemText>PDF 리포트</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleExport('excel')}>
          <ListItemIcon>
            <Assessment fontSize="small" />
          </ListItemIcon>
          <ListItemText>Excel 파일</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleExport('csv')}>
          <ListItemIcon>
            <FileDownload fontSize="small" />
          </ListItemIcon>
          <ListItemText>CSV 데이터</ListItemText>
        </MenuItem>
      </Menu>
    </Box>
  );
}