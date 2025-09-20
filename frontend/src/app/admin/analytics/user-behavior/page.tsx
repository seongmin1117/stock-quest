'use client';

import React, { useState } from 'react';
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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  LinearProgress
} from '@mui/material';
import {
  ArrowBack,
  Person,
  AccessTime,
  Visibility,
  TrendingUp,
  TrendingDown,
  Schedule,
  TouchApp,
  DevicesOther
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';
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
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';

interface UserEngagementData {
  date: string;
  dailyActiveUsers: number;
  sessionDuration: number;
  pageViews: number;
  challenges_started: number;
  challenges_completed: number;
  bounce_rate: number;
}

interface DeviceData {
  device: string;
  users: number;
  percentage: number;
}

interface TimeSpentData {
  hour: number;
  users: number;
  engagement: number;
}

interface UserJourneyStep {
  step: string;
  users: number;
  conversion: number;
  dropOff: number;
}

interface CohortData {
  month: string;
  week1: number;
  week2: number;
  week3: number;
  week4: number;
}

const mockEngagementData: UserEngagementData[] = [
  { date: '2024-09-01', dailyActiveUsers: 234, sessionDuration: 18.5, pageViews: 1456, challenges_started: 67, challenges_completed: 45, bounce_rate: 32.1 },
  { date: '2024-09-02', dailyActiveUsers: 267, sessionDuration: 22.3, pageViews: 1632, challenges_started: 78, challenges_completed: 52, bounce_rate: 28.4 },
  { date: '2024-09-03', dailyActiveUsers: 198, sessionDuration: 15.7, pageViews: 1203, challenges_started: 45, challenges_completed: 31, bounce_rate: 38.9 },
  { date: '2024-09-04', dailyActiveUsers: 289, sessionDuration: 25.1, pageViews: 1789, challenges_started: 89, challenges_completed: 67, bounce_rate: 25.7 },
  { date: '2024-09-05', dailyActiveUsers: 312, sessionDuration: 27.8, pageViews: 1923, challenges_started: 95, challenges_completed: 71, bounce_rate: 23.2 },
  { date: '2024-09-06', dailyActiveUsers: 278, sessionDuration: 21.4, pageViews: 1678, challenges_started: 82, challenges_completed: 58, bounce_rate: 29.8 },
  { date: '2024-09-07', dailyActiveUsers: 345, sessionDuration: 29.6, pageViews: 2134, challenges_started: 102, challenges_completed: 79, bounce_rate: 21.5 }
];

const mockDeviceData: DeviceData[] = [
  { device: 'Desktop', users: 485, percentage: 56.7 },
  { device: 'Mobile', users: 312, percentage: 36.4 },
  { device: 'Tablet', users: 59, percentage: 6.9 }
];

const mockTimeSpentData: TimeSpentData[] = [
  { hour: 0, users: 12, engagement: 45 },
  { hour: 1, users: 8, engagement: 32 },
  { hour: 2, users: 5, engagement: 28 },
  { hour: 3, users: 3, engagement: 25 },
  { hour: 4, users: 4, engagement: 30 },
  { hour: 5, users: 9, engagement: 38 },
  { hour: 6, users: 23, engagement: 52 },
  { hour: 7, users: 45, engagement: 68 },
  { hour: 8, users: 78, engagement: 82 },
  { hour: 9, users: 156, engagement: 95 },
  { hour: 10, users: 189, engagement: 88 },
  { hour: 11, users: 167, engagement: 91 },
  { hour: 12, users: 145, engagement: 76 },
  { hour: 13, users: 178, engagement: 84 },
  { hour: 14, users: 198, engagement: 89 },
  { hour: 15, users: 201, engagement: 92 },
  { hour: 16, users: 187, engagement: 85 },
  { hour: 17, users: 156, engagement: 79 },
  { hour: 18, users: 123, engagement: 72 },
  { hour: 19, users: 98, engagement: 68 },
  { hour: 20, users: 87, engagement: 65 },
  { hour: 21, users: 76, engagement: 61 },
  { hour: 22, users: 54, engagement: 58 },
  { hour: 23, users: 32, engagement: 51 }
];

const mockUserJourney: UserJourneyStep[] = [
  { step: '방문', users: 1000, conversion: 100, dropOff: 0 },
  { step: '회원가입', users: 650, conversion: 65, dropOff: 35 },
  { step: '첫 챌린지 선택', users: 520, conversion: 52, dropOff: 13 },
  { step: '챌린지 시작', users: 450, conversion: 45, dropOff: 7 },
  { step: '첫 거래 실행', users: 380, conversion: 38, dropOff: 7 },
  { step: '챌린지 완료', users: 290, conversion: 29, dropOff: 9 }
];

const mockCohortData: CohortData[] = [
  { month: '2024-06', week1: 100, week2: 75, week3: 58, week4: 45 },
  { month: '2024-07', week1: 100, week2: 78, week3: 62, week4: 48 },
  { month: '2024-08', week1: 100, week2: 82, week3: 67, week4: 52 },
  { month: '2024-09', week1: 100, week2: 85, week3: 71, week4: 0 }
];

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

export default function UserBehaviorAnalyticsPage() {
  const router = useRouter();
  const [dateRange, setDateRange] = useState<'7d' | '30d' | '90d'>('30d');
  const [engagementData] = useState<UserEngagementData[]>(mockEngagementData);
  const [deviceData] = useState<DeviceData[]>(mockDeviceData);
  const [timeSpentData] = useState<TimeSpentData[]>(mockTimeSpentData);
  const [userJourney] = useState<UserJourneyStep[]>(mockUserJourney);
  const [cohortData] = useState<CohortData[]>(mockCohortData);

  const formatTime = (minutes: number) => {
    if (minutes < 60) return `${minutes.toFixed(1)}분`;
    return `${(minutes / 60).toFixed(1)}시간`;
  };

  const pieChartData = deviceData.map((device, index) => ({
    name: device.device,
    value: device.users,
    percentage: device.percentage,
    fill: COLORS[index % COLORS.length]
  }));

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Button
          variant="text"
          startIcon={<ArrowBack />}
          onClick={() => router.push('/admin/analytics')}
          sx={{ mb: 2 }}
        >
          분석 대시보드로
        </Button>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
              사용자 행동 분석
            </Typography>
            <Typography variant="body1" color="text.secondary">
              사용자 참여도, 행동 패턴, 디바이스 사용량을 상세히 분석합니다
            </Typography>
          </Box>
          <FormControl size="small">
            <InputLabel>분석 기간</InputLabel>
            <Select
              value={dateRange}
              onChange={(e) => setDateRange(e.target.value as typeof dateRange)}
              label="분석 기간"
            >
              <MenuItem value="7d">최근 7일</MenuItem>
              <MenuItem value="30d">최근 30일</MenuItem>
              <MenuItem value="90d">최근 90일</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Box>

      {/* 주요 지표 */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    267
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    일일 활성 사용자
                  </Typography>
                  <Typography variant="caption" color="success.main">
                    +14% 증가
                  </Typography>
                </Box>
                <Person color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    22.3
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 세션 시간 (분)
                  </Typography>
                  <Typography variant="caption" color="success.main">
                    +8% 증가
                  </Typography>
                </Box>
                <AccessTime color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    1,632
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    페이지뷰
                  </Typography>
                  <Typography variant="caption" color="success.main">
                    +12% 증가
                  </Typography>
                </Box>
                <Visibility color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    66.7%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    챌린지 완료율
                  </Typography>
                  <Typography variant="caption" color="success.main">
                    +3% 증가
                  </Typography>
                </Box>
                <TrendingUp color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 사용자 참여도 추이 */}
        <Grid item xs={12} lg={8}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                사용자 참여도 추이
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={engagementData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(value) => new Date(value).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                  />
                  <YAxis />
                  <Tooltip
                    labelFormatter={(value) => new Date(value).toLocaleDateString('ko-KR')}
                    formatter={(value: number, name: string) => [
                      name === 'dailyActiveUsers' ? `${value}명` :
                      name === 'sessionDuration' ? `${value}분` :
                      name === 'pageViews' ? `${value}회` :
                      `${value}%`,
                      name === 'dailyActiveUsers' ? '일일 활성 사용자' :
                      name === 'sessionDuration' ? '세션 시간' :
                      name === 'pageViews' ? '페이지뷰' :
                      '이탈률'
                    ]}
                  />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="dailyActiveUsers"
                    stroke="#8884d8"
                    strokeWidth={2}
                    name="일일 활성 사용자"
                  />
                  <Line
                    type="monotone"
                    dataKey="sessionDuration"
                    stroke="#82ca9d"
                    strokeWidth={2}
                    name="세션 시간"
                  />
                  <Line
                    type="monotone"
                    dataKey="bounce_rate"
                    stroke="#ff7300"
                    strokeWidth={2}
                    name="이탈률"
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* 시간대별 사용자 활동 */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                시간대별 사용자 활동
              </Typography>
              <ResponsiveContainer width="100%" height={250}>
                <AreaChart data={timeSpentData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="hour"
                    tickFormatter={(value) => `${value}시`}
                  />
                  <YAxis />
                  <Tooltip
                    labelFormatter={(value) => `${value}시`}
                    formatter={(value: number, name: string) => [
                      name === 'users' ? `${value}명` : `${value}%`,
                      name === 'users' ? '활성 사용자' : '참여도'
                    ]}
                  />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="users"
                    stackId="1"
                    stroke="#8884d8"
                    fill="#8884d8"
                    fillOpacity={0.6}
                    name="활성 사용자"
                  />
                  <Area
                    type="monotone"
                    dataKey="engagement"
                    stackId="2"
                    stroke="#82ca9d"
                    fill="#82ca9d"
                    fillOpacity={0.4}
                    name="참여도"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* 디바이스 분석 */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">
                  디바이스별 사용자
                </Typography>
                <DevicesOther color="primary" />
              </Box>
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie
                    data={pieChartData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percentage }) => `${name} ${percentage.toFixed(1)}%`}
                    outerRadius={70}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {pieChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value: number) => [`${value}명`, '사용자']} />
                </PieChart>
              </ResponsiveContainer>
              <Box mt={2}>
                {deviceData.map((device, index) => (
                  <Box key={device.device} display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Box display="flex" alignItems="center">
                      <Box
                        width={12}
                        height={12}
                        bgcolor={COLORS[index % COLORS.length]}
                        borderRadius="50%"
                        mr={1}
                      />
                      <Typography variant="body2">{device.device}</Typography>
                    </Box>
                    <Typography variant="body2" fontWeight="bold">
                      {device.users}명
                    </Typography>
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>

          {/* 사용자 여정 분석 */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                사용자 여정 분석
              </Typography>
              <Box>
                {userJourney.map((step, index) => (
                  <Box key={step.step} mb={2}>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
                      <Typography variant="body2">
                        {index + 1}. {step.step}
                      </Typography>
                      <Typography variant="body2" fontWeight="bold">
                        {step.users}명 ({step.conversion}%)
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={step.conversion}
                      sx={{
                        height: 8,
                        borderRadius: 1,
                        backgroundColor: 'grey.200',
                        '& .MuiLinearProgress-bar': {
                          backgroundColor: index === 0 ? '#8884d8' :
                                         index <= 2 ? '#82ca9d' :
                                         index <= 4 ? '#ffc658' : '#ff7c7c'
                        }
                      }}
                    />
                    {index < userJourney.length - 1 && step.dropOff > 0 && (
                      <Typography variant="caption" color="error.main">
                        ↳ {step.dropOff}% 이탈
                      </Typography>
                    )}
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 챌린지 참여 패턴 */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                챌린지 참여 패턴
              </Typography>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={engagementData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(value) => new Date(value).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                  />
                  <YAxis />
                  <Tooltip
                    labelFormatter={(value) => new Date(value).toLocaleDateString('ko-KR')}
                    formatter={(value: number, name: string) => [
                      `${value}건`,
                      name === 'challenges_started' ? '시작된 챌린지' : '완료된 챌린지'
                    ]}
                  />
                  <Legend />
                  <Bar dataKey="challenges_started" fill="#8884d8" name="시작된 챌린지" />
                  <Bar dataKey="challenges_completed" fill="#82ca9d" name="완료된 챌린지" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* 코호트 분석 */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                코호트 분석 (사용자 유지율)
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>가입월</TableCell>
                      <TableCell align="center">1주차</TableCell>
                      <TableCell align="center">2주차</TableCell>
                      <TableCell align="center">3주차</TableCell>
                      <TableCell align="center">4주차</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {cohortData.map((cohort) => (
                      <TableRow key={cohort.month}>
                        <TableCell>{cohort.month}</TableCell>
                        <TableCell align="center">
                          <Chip
                            label={`${cohort.week1}%`}
                            size="small"
                            color="success"
                            variant="filled"
                          />
                        </TableCell>
                        <TableCell align="center">
                          <Chip
                            label={`${cohort.week2}%`}
                            size="small"
                            color={cohort.week2 > 70 ? 'success' : 'warning'}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell align="center">
                          <Chip
                            label={`${cohort.week3}%`}
                            size="small"
                            color={cohort.week3 > 60 ? 'warning' : 'error'}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell align="center">
                          {cohort.week4 > 0 ? (
                            <Chip
                              label={`${cohort.week4}%`}
                              size="small"
                              color={cohort.week4 > 50 ? 'warning' : 'error'}
                              variant="outlined"
                            />
                          ) : (
                            <Typography variant="caption" color="text.secondary">
                              N/A
                            </Typography>
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
    </Box>
  );
}