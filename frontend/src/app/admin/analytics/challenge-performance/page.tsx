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
  LinearProgress,
  IconButton,
  Menu,
  MenuProps,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Assessment,
  People,
  Timer,
  Star,
  Download,
  FilterList,
  MoreVert,
} from '@mui/icons-material';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ScatterChart,
  Scatter,
  ComposedChart,
  RadialBarChart,
  RadialBar,
} from 'recharts';

const ChallengePerformancePage = () => {
  const [selectedPeriod, setSelectedPeriod] = useState('month');
  const [selectedChallenge, setSelectedChallenge] = useState('all');
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // 챌린지 성과 통계 데이터
  const performanceStats = {
    totalChallenges: 156,
    activeChallenges: 23,
    avgCompletionRate: 68.5,
    avgSuccessRate: 42.3,
    topPerformingChallenge: "AI 트레이딩 마스터",
    worstPerformingChallenge: "고급 옵션 전략"
  };

  // 챌린지별 성과 데이터
  const challengePerformanceData = [
    { month: '1월', 성공률: 45, 완료율: 72, 참여자수: 340 },
    { month: '2월', 성공률: 38, 완료율: 69, 참여자수: 390 },
    { month: '3월', 성공률: 52, 완료율: 74, 참여자수: 420 },
    { month: '4월', 성공률: 41, 완료율: 67, 참여자수: 380 },
    { month: '5월', 성공률: 47, 완료율: 71, 참여자수: 450 },
    { month: '6월', 성공률: 44, 완료율: 70, 참여자수: 410 },
  ];

  // 난이도별 성과 분포
  const difficultyDistribution = [
    { name: '초급', value: 35, color: '#4CAF50' },
    { name: '중급', value: 45, color: '#FF9800' },
    { name: '고급', value: 20, color: '#F44336' },
  ];

  // 챌린지 카테고리별 성과
  const categoryPerformance = [
    { category: '기본 거래', 성공률: 78, 완료율: 85, 평균점수: 4.2, 참여자: 850 },
    { category: 'AI 트레이딩', 성공률: 62, 완료율: 74, 평균점수: 4.5, 참여자: 420 },
    { category: '포트폴리오 관리', 성공률: 55, 완료율: 68, 평균점수: 3.8, 참여자: 380 },
    { category: '리스크 관리', 성공률: 48, 완료율: 65, 평균점수: 3.9, 참여자: 320 },
    { category: '고급 전략', 성공률: 35, 완료율: 52, 평균점수: 4.1, 참여자: 180 },
  ];

  // 시간대별 챌린지 활동
  const timeBasedActivity = [
    { time: '00:00', 활동량: 12, 성공률: 35 },
    { time: '03:00', 활동량: 8, 성공률: 28 },
    { time: '06:00', 활동량: 25, 성공률: 42 },
    { time: '09:00', 활동량: 85, 성공률: 58 },
    { time: '12:00', 활동량: 92, 성공률: 62 },
    { time: '15:00', 활동량: 78, 성공률: 55 },
    { time: '18:00', 활동량: 65, 성공률: 48 },
    { time: '21:00', 활동량: 45, 성공률: 38 },
  ];

  // 챌린지 성과 vs 난이도 분석
  const difficultyAnalysis = [
    { difficulty: 1, 성공률: 85, 만족도: 4.1, 참여자수: 120 },
    { difficulty: 2, 성공률: 78, 만족도: 4.3, 참여자수: 180 },
    { difficulty: 3, 성공률: 65, 만족도: 4.2, 참여자수: 220 },
    { difficulty: 4, 성공률: 52, 만족도: 4.0, 참여자수: 160 },
    { difficulty: 5, 성공률: 38, 만족도: 3.8, 참여자수: 90 },
    { difficulty: 6, 성공률: 28, 만족도: 3.9, 참여자수: 65 },
    { difficulty: 7, 성공률: 22, 만족도: 4.1, 참여자수: 45 },
    { difficulty: 8, 성공률: 18, 만족도: 4.2, 참여자수: 30 },
    { difficulty: 9, 성공률: 12, 만족도: 4.0, 참여자수: 20 },
    { difficulty: 10, 성공률: 8, 만족도: 3.9, 참여자수: 15 },
  ];

  // 상위 성과 챌린지 목록
  const topChallenges = [
    { id: 1, name: 'AI 트레이딩 마스터', category: 'AI 트레이딩', 성공률: 78.5, 완료율: 85.2, 참여자: 420, 평점: 4.8 },
    { id: 2, name: '기본 주식 거래', category: '기본 거래', 성공률: 72.1, 완료율: 89.6, 참여자: 680, 평점: 4.2 },
    { id: 3, name: '포트폴리오 다변화', category: '포트폴리오 관리', 성공률: 68.9, 완료율: 76.3, 참여자: 350, 평점: 4.1 },
    { id: 4, name: '리스크 관리 기초', category: '리스크 관리', 성공률: 65.4, 완료율: 78.9, 참여자: 280, 평점: 4.0 },
    { id: 5, name: '기술적 분석', category: '고급 전략', 성공률: 58.2, 완료율: 65.7, 참여자: 190, 평점: 4.3 },
  ];

  const handleExportMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setExportMenuAnchor(event.currentTarget);
  };

  const handleExportMenuClose = () => {
    setExportMenuAnchor(null);
  };

  const handleExport = (format: string) => {
    console.log(`Exporting challenge performance data in ${format} format`);
    handleExportMenuClose();
  };

  const getPerformanceColor = (value: number) => {
    if (value >= 70) return '#4CAF50';
    if (value >= 50) return '#FF9800';
    return '#F44336';
  };

  const getRatingStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        sx={{
          fontSize: 16,
          color: i < Math.floor(rating) ? '#FFD700' : '#E0E0E0'
        }}
      />
    ));
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            챌린지 성과 분석
          </Typography>
          <Typography variant="body1" color="text.secondary">
            챌린지별 성과 지표, 성공률, 참여도 분석
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
            <InputLabel>챌린지</InputLabel>
            <Select
              value={selectedChallenge}
              label="챌린지"
              onChange={(e) => setSelectedChallenge(e.target.value)}
            >
              <MenuItem value="all">전체</MenuItem>
              <MenuItem value="basic">기본 거래</MenuItem>
              <MenuItem value="ai">AI 트레이딩</MenuItem>
              <MenuItem value="portfolio">포트폴리오 관리</MenuItem>
              <MenuItem value="risk">리스크 관리</MenuItem>
              <MenuItem value="advanced">고급 전략</MenuItem>
            </Select>
          </FormControl>

          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={handleExportMenuOpen}
          >
            내보내기
          </Button>
          <Menu
            anchorEl={exportMenuAnchor}
            open={Boolean(exportMenuAnchor)}
            onClose={handleExportMenuClose}
          >
            <MenuItem onClick={() => handleExport('PDF')}>PDF로 내보내기</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>Excel로 내보내기</MenuItem>
            <MenuItem onClick={() => handleExport('CSV')}>CSV로 내보내기</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 주요 성과 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    {performanceStats.totalChallenges}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 챌린지 수
                  </Typography>
                </Box>
                <Assessment color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {performanceStats.activeChallenges}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    활성 챌린지
                  </Typography>
                </Box>
                <Timer color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {performanceStats.avgCompletionRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 완료율
                  </Typography>
                </Box>
                <TrendingUp color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {performanceStats.avgSuccessRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 성공률
                  </Typography>
                </Box>
                <Star color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 월별 성과 트렌드 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              월별 챌린지 성과 트렌드
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <ComposedChart data={challengePerformanceData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="left" dataKey="참여자수" fill="#8884d8" name="참여자 수" />
                <Line yAxisId="right" type="monotone" dataKey="성공률" stroke="#82ca9d" name="성공률 (%)" />
                <Line yAxisId="right" type="monotone" dataKey="완료율" stroke="#ffc658" name="완료율 (%)" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 난이도별 분포 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              난이도별 챌린지 분포
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <PieChart>
                <Pie
                  data={difficultyDistribution}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, value }) => `${name} ${value}%`}
                >
                  {difficultyDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 시간대별 활동 패턴 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              시간대별 챌린지 활동
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={timeBasedActivity}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Area yAxisId="left" type="monotone" dataKey="활동량" stackId="1" stroke="#8884d8" fill="#8884d8" />
                <Line yAxisId="right" type="monotone" dataKey="성공률" stroke="#82ca9d" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 난이도 vs 성과 분석 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              난이도별 성과 분석
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <ScatterChart data={difficultyAnalysis}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="difficulty" name="난이도" />
                <YAxis dataKey="성공률" name="성공률" />
                <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                <Scatter
                  dataKey="성공률"
                  fill="#8884d8"
                  r={(entry: any) => entry.참여자수 / 10}
                />
              </ScatterChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 카테고리별 성과 테이블 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              카테고리별 성과 분석
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>카테고리</TableCell>
                    <TableCell align="center">성공률</TableCell>
                    <TableCell align="center">완료율</TableCell>
                    <TableCell align="center">평균 점수</TableCell>
                    <TableCell align="center">참여자 수</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {categoryPerformance.map((row) => (
                    <TableRow key={row.category}>
                      <TableCell component="th" scope="row">
                        {row.category}
                      </TableCell>
                      <TableCell align="center">
                        <Box display="flex" alignItems="center" justifyContent="center">
                          <Typography
                            variant="body2"
                            sx={{ color: getPerformanceColor(row.성공률) }}
                            fontWeight="bold"
                          >
                            {row.성공률}%
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Box>
                          <Typography variant="body2" mb={0.5}>
                            {row.완료율}%
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={row.완료율}
                            sx={{ height: 6, borderRadius: 1 }}
                          />
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Box display="flex" alignItems="center" justifyContent="center" gap={0.5}>
                          <Typography variant="body2" mr={1}>
                            {row.평균점수}
                          </Typography>
                          {getRatingStars(row.평균점수)}
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {row.참여자.toLocaleString()}명
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* 상위 성과 챌린지 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              상위 성과 챌린지
            </Typography>
            {topChallenges.map((challenge, index) => (
              <Box key={challenge.id} mb={2}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <Box>
                    <Typography variant="subtitle2">
                      #{index + 1} {challenge.name}
                    </Typography>
                    <Chip
                      label={challenge.category}
                      size="small"
                      sx={{ fontSize: '0.7rem', height: 18 }}
                    />
                  </Box>
                  <Box display="flex" alignItems="center">
                    {getRatingStars(challenge.평점)}
                  </Box>
                </Box>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <Typography variant="body2" color="text.secondary">
                    성공률: {challenge.성공률}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {challenge.참여자}명 참여
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={challenge.성공률}
                  sx={{
                    height: 6,
                    borderRadius: 1,
                    backgroundColor: '#f5f5f5',
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: getPerformanceColor(challenge.성공률)
                    }
                  }}
                />
                {index < topChallenges.length - 1 && <Box sx={{ borderBottom: '1px solid #eee', mt: 2 }} />}
              </Box>
            ))}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default ChallengePerformancePage;