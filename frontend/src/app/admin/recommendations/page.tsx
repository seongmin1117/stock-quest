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
  Alert,
  LinearProgress,
  IconButton,
  Menu,
  Badge,
  Avatar,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemAvatar,
  Slider,
  TextField,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  PersonalVideo,
  TrendingUp,
  TrendingDown,
  Psychology,
  Assessment,
  Refresh,
  Download,
  Settings,
  Tune,
  AutoAwesome,
  SmartToy,
  Lightbulb,
  Star,
  Timeline,
  ShowChart,
  AccountBalance,
  MonetizationOn,
  Security,
  Speed,
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
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ScatterChart,
  Scatter,
} from 'recharts';

const RecommendationSystemPage = () => {
  const [selectedProfile, setSelectedProfile] = useState('aggressive');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [riskTolerance, setRiskTolerance] = useState(7);
  const [investmentHorizon, setInvestmentHorizon] = useState(12);
  const [personalizedMode, setPersonalizedMode] = useState(true);
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // 개인화 통계
  const personalizationStats = {
    totalUsers: 1245,
    activeRecommendations: 2840,
    avgAccuracy: 84.3,
    userSatisfaction: 4.2,
    conversationRate: 23.5,
    avgReturn: 15.8
  };

  // 사용자 프로필 분석
  const userProfiles = [
    { type: '보수형', users: 385, color: '#4CAF50', accuracy: 87.2 },
    { type: '안정형', users: 420, color: '#2196F3', accuracy: 84.8 },
    { type: '적극형', users: 290, color: '#FF9800', accuracy: 82.1 },
    { type: '공격형', users: 150, color: '#F44336', accuracy: 79.5 },
  ];

  // 개인화된 추천 항목
  const personalizedRecommendations = [
    {
      id: 1,
      title: 'AI 기반 성장주 포트폴리오',
      category: '포트폴리오',
      riskLevel: 'Medium',
      expectedReturn: 18.5,
      timeHorizon: '12개월',
      confidence: 92.4,
      reasoning: '사용자의 기술주 선호도와 중기 투자 성향을 고려한 AI 추천',
      matchScore: 94,
      tags: ['AI', '성장주', '기술', '분산투자']
    },
    {
      id: 2,
      title: 'ESG 친환경 에너지 펀드',
      category: '펀드',
      riskLevel: 'Low',
      expectedReturn: 12.3,
      timeHorizon: '24개월',
      confidence: 87.9,
      reasoning: '지속가능한 투자 선호와 장기 투자 패턴 분석 결과',
      matchScore: 89,
      tags: ['ESG', '친환경', '장기', '안정성']
    },
    {
      id: 3,
      title: '바이오테크 혁신 기업 선별',
      category: '개별주식',
      riskLevel: 'High',
      expectedReturn: 25.7,
      timeHorizon: '6개월',
      confidence: 76.2,
      reasoning: '최근 바이오 섹터 관심도 증가와 단기 수익 추구 성향',
      matchScore: 82,
      tags: ['바이오', '혁신', '단기', '고수익']
    },
    {
      id: 4,
      title: '글로벌 리츠 분산투자',
      category: '리츠',
      riskLevel: 'Medium',
      expectedReturn: 14.8,
      timeHorizon: '18개월',
      confidence: 89.5,
      reasoning: '부동산 투자 관심과 배당 선호 패턴 기반 맞춤 추천',
      matchScore: 91,
      tags: ['리츠', '배당', '글로벌', '부동산']
    },
    {
      id: 5,
      title: '금리 상승기 대응 채권',
      category: '채권',
      riskLevel: 'Low',
      expectedReturn: 8.9,
      timeHorizon: '36개월',
      confidence: 93.1,
      reasoning: '안정성 중시와 금리 변화 대응 포트폴리오 필요성 분석',
      matchScore: 88,
      tags: ['채권', '안정', '금리대응', '장기']
    }
  ];

  // 추천 성과 추적
  const recommendationPerformance = [
    { month: '1월', 추천정확도: 82.5, 사용자만족: 4.1, 수익률: 12.8, 전환율: 18.5 },
    { month: '2월', 추천정확도: 85.2, 사용자만족: 4.3, 수익률: 15.2, 전환율: 21.8 },
    { month: '3월', 추천정확도: 87.9, 사용자만족: 4.2, 수익률: 18.7, 전환율: 25.3 },
    { month: '4월', 추천정확도: 84.6, 사용자만족: 4.4, 수익률: 14.9, 전환율: 22.7 },
    { month: '5월', 추천정확도: 89.1, 사용자만족: 4.5, 수익률: 19.8, 전환율: 28.2 },
    { month: '6월', 추천정확도: 86.3, 사용자만족: 4.3, 수익률: 16.4, 전환율: 24.9 },
  ];

  // 카테고리별 성과
  const categoryPerformance = [
    { name: '개별주식', value: 35, accuracy: 82.4, return: 22.3 },
    { name: '포트폴리오', value: 28, accuracy: 87.9, return: 18.7 },
    { name: '펀드', value: 20, accuracy: 91.2, return: 14.2 },
    { name: '리츠', value: 10, accuracy: 85.6, return: 16.8 },
    { name: '채권', value: 7, accuracy: 93.1, return: 9.4 },
  ];

  // 사용자 행동 패턴 분석
  const behaviorAnalysis = {
    investmentStyle: [
      { subject: '장기투자', A: 85, fullMark: 100 },
      { subject: '분산투자', A: 72, fullMark: 100 },
      { subject: '가치투자', A: 68, fullMark: 100 },
      { subject: '성장투자', A: 91, fullMark: 100 },
      { subject: '배당투자', A: 45, fullMark: 100 },
      { subject: '모멘텀', A: 76, fullMark: 100 },
    ]
  };

  // AI 학습 데이터
  const aiLearningData = [
    { feature: '거래 패턴', importance: 0.28, accuracy: 89.2 },
    { feature: '섹터 선호', importance: 0.24, accuracy: 85.7 },
    { feature: '리스크 성향', importance: 0.19, accuracy: 92.1 },
    { feature: '투자 기간', importance: 0.15, accuracy: 87.4 },
    { feature: '수익률 목표', importance: 0.14, accuracy: 83.8 },
  ];

  const handleExportMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setExportMenuAnchor(event.currentTarget);
  };

  const handleExportMenuClose = () => {
    setExportMenuAnchor(null);
  };

  const handleExport = (format: string) => {
    console.log(`Exporting recommendation data in ${format} format`);
    handleExportMenuClose();
  };

  const getRiskColor = (risk: string) => {
    switch (risk) {
      case 'Low': return '#4CAF50';
      case 'Medium': return '#FF9800';
      case 'High': return '#F44336';
      default: return '#757575';
    }
  };

  const getRiskIcon = (risk: string) => {
    switch (risk) {
      case 'Low': return <Security />;
      case 'Medium': return <Assessment />;
      case 'High': return <Speed />;
      default: return <Assessment />;
    }
  };

  const getMatchScoreColor = (score: number) => {
    if (score >= 90) return '#4CAF50';
    if (score >= 80) return '#2196F3';
    if (score >= 70) return '#FF9800';
    return '#F44336';
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            개인화된 추천 시스템
          </Typography>
          <Typography variant="body1" color="text.secondary">
            AI 기반 맞춤형 투자 추천, 사용자 행동 분석, 성과 추적
          </Typography>
        </Box>

        {/* 컨트롤 패널 */}
        <Box display="flex" gap={2} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>투자성향</InputLabel>
            <Select
              value={selectedProfile}
              label="투자성향"
              onChange={(e) => setSelectedProfile(e.target.value)}
            >
              <MenuItem value="conservative">보수형</MenuItem>
              <MenuItem value="stable">안정형</MenuItem>
              <MenuItem value="aggressive">적극형</MenuItem>
              <MenuItem value="speculative">공격형</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>카테고리</InputLabel>
            <Select
              value={selectedCategory}
              label="카테고리"
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <MenuItem value="all">전체</MenuItem>
              <MenuItem value="stocks">개별주식</MenuItem>
              <MenuItem value="portfolio">포트폴리오</MenuItem>
              <MenuItem value="funds">펀드</MenuItem>
              <MenuItem value="reits">리츠</MenuItem>
              <MenuItem value="bonds">채권</MenuItem>
            </Select>
          </FormControl>

          <FormControlLabel
            control={
              <Switch
                checked={personalizedMode}
                onChange={(e) => setPersonalizedMode(e.target.checked)}
              />
            }
            label="개인화 모드"
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
            <MenuItem onClick={() => handleExport('PDF')}>추천 리포트 (PDF)</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>성과 분석 (Excel)</MenuItem>
            <MenuItem onClick={() => handleExport('JSON')}>사용자 데이터 (JSON)</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 개인화 알림 */}
      <Alert severity="info" sx={{ mb: 3 }} icon={<PersonalVideo />}>
        <Typography variant="body2">
          <strong>AI 개인화 시스템이 활성화되었습니다.</strong>
          {' '}사용자의 투자 패턴, 선호도, 위험 성향을 분석하여 맞춤형 추천을 제공합니다.
          현재 추천 정확도: {personalizationStats.avgAccuracy}%
        </Typography>
      </Alert>

      {/* 주요 개인화 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    {personalizationStats.activeRecommendations.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    활성 추천
                  </Typography>
                </Box>
                <AutoAwesome color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {personalizationStats.avgAccuracy}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    추천 정확도
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
                  <Typography variant="h4" color="info.main">
                    {personalizationStats.userSatisfaction}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    사용자 만족도
                  </Typography>
                </Box>
                <Star color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {personalizationStats.conversationRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    전환율
                  </Typography>
                </Box>
                <TrendingUp color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 개인화 설정 패널 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              개인화 설정
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
              <Typography variant="body2" gutterBottom>
                투자 기간: {investmentHorizon}개월
              </Typography>
              <Slider
                value={investmentHorizon}
                onChange={(e, value) => setInvestmentHorizon(value as number)}
                min={3}
                max={60}
                step={3}
                marks={[
                  { value: 3, label: '3개월' },
                  { value: 12, label: '1년' },
                  { value: 36, label: '3년' },
                  { value: 60, label: '5년' }
                ]}
                valueLabelDisplay="auto"
              />
            </Box>
            <Button variant="contained" startIcon={<Tune />} fullWidth>
              개인화 설정 업데이트
            </Button>
          </Paper>

          {/* 사용자 투자 성향 분석 */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              투자 성향 분석
            </Typography>
            <ResponsiveContainer width="100%" height={250}>
              <RadarChart data={behaviorAnalysis.investmentStyle}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" />
                <PolarRadiusAxis angle={30} domain={[0, 100]} />
                <Radar
                  name="투자성향"
                  dataKey="A"
                  stroke="#8884d8"
                  fill="#8884d8"
                  fillOpacity={0.6}
                />
              </RadarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 개인화된 추천 목록 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">
                맞춤형 투자 추천
              </Typography>
              <Badge badgeContent={personalizedRecommendations.length} color="primary">
                <Lightbulb />
              </Badge>
            </Box>
            {personalizedRecommendations.map((rec) => (
              <Card key={rec.id} sx={{ mb: 2, border: '1px solid #e0e0e0' }}>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Box flex={1}>
                      <Box display="flex" alignItems="center" gap={1} mb={1}>
                        <Typography variant="h6">
                          {rec.title}
                        </Typography>
                        <Chip
                          label={rec.category}
                          size="small"
                          color="primary"
                          variant="outlined"
                        />
                      </Box>
                      <Typography variant="body2" color="text.secondary" mb={2}>
                        {rec.reasoning}
                      </Typography>
                      <Box display="flex" gap={1} flexWrap="wrap">
                        {rec.tags.map((tag, index) => (
                          <Chip
                            key={index}
                            label={tag}
                            size="small"
                            variant="outlined"
                          />
                        ))}
                      </Box>
                    </Box>
                    <Box textAlign="right" ml={2}>
                      <Typography
                        variant="h5"
                        sx={{ color: getMatchScoreColor(rec.matchScore) }}
                        fontWeight="bold"
                      >
                        {rec.matchScore}%
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        매치 스코어
                      </Typography>
                    </Box>
                  </Box>

                  <Divider sx={{ my: 2 }} />

                  <Grid container spacing={2}>
                    <Grid item xs={3}>
                      <Box display="flex" alignItems="center" gap={1}>
                        {getRiskIcon(rec.riskLevel)}
                        <Box>
                          <Typography variant="caption" color="text.secondary">
                            리스크
                          </Typography>
                          <Typography
                            variant="body2"
                            sx={{ color: getRiskColor(rec.riskLevel) }}
                            fontWeight="bold"
                          >
                            {rec.riskLevel}
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <MonetizationOn color="success" />
                        <Box>
                          <Typography variant="caption" color="text.secondary">
                            예상수익
                          </Typography>
                          <Typography variant="body2" color="success.main" fontWeight="bold">
                            {rec.expectedReturn}%
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Timeline color="info" />
                        <Box>
                          <Typography variant="caption" color="text.secondary">
                            투자기간
                          </Typography>
                          <Typography variant="body2" color="info.main" fontWeight="bold">
                            {rec.timeHorizon}
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <SmartToy color="warning" />
                        <Box>
                          <Typography variant="caption" color="text.secondary">
                            신뢰도
                          </Typography>
                          <Typography variant="body2" color="warning.main" fontWeight="bold">
                            {rec.confidence}%
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                  </Grid>

                  <Box mt={2}>
                    <LinearProgress
                      variant="determinate"
                      value={rec.matchScore}
                      sx={{
                        height: 6,
                        borderRadius: 3,
                        '& .MuiLinearProgress-bar': {
                          backgroundColor: getMatchScoreColor(rec.matchScore)
                        }
                      }}
                    />
                  </Box>
                </CardContent>
              </Card>
            ))}
          </Paper>
        </Grid>

        {/* 사용자 프로필 분포 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              사용자 프로필 분포
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={userProfiles}
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="users"
                  label={({ type, users, accuracy }) => `${type}: ${users}명 (${accuracy}%)`}
                >
                  {userProfiles.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 카테고리별 성과 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              카테고리별 추천 성과
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <ScatterChart data={categoryPerformance}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="accuracy" name="정확도" unit="%" />
                <YAxis dataKey="return" name="수익률" unit="%" />
                <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                <Scatter
                  dataKey="return"
                  fill="#8884d8"
                  r={(entry: any) => entry.value}
                />
              </ScatterChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 추천 성과 추적 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              월별 추천 성과 추적
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <AreaChart data={recommendationPerformance}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Area yAxisId="left" type="monotone" dataKey="추천정확도" stackId="1" stroke="#8884d8" fill="#8884d8" />
                <Line yAxisId="right" type="monotone" dataKey="수익률" stroke="#82ca9d" strokeWidth={3} />
                <Line yAxisId="right" type="monotone" dataKey="전환율" stroke="#ffc658" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* AI 학습 데이터 중요도 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              AI 학습 특성 중요도
            </Typography>
            <List>
              {aiLearningData.map((item, index) => (
                <React.Fragment key={item.feature}>
                  <ListItem>
                    <ListItemIcon>
                      <Psychology color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary={item.feature}
                      secondary={
                        <Box>
                          <Typography variant="body2" component="div">
                            중요도: {(item.importance * 100).toFixed(0)}% | 정확도: {item.accuracy}%
                          </Typography>
                          <LinearProgress
                            variant="determinate"
                            value={item.importance * 100}
                            sx={{ mt: 1, height: 6, borderRadius: 3 }}
                          />
                        </Box>
                      }
                    />
                  </ListItem>
                  {index < aiLearningData.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default RecommendationSystemPage;