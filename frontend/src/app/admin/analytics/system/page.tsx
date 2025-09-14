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
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
} from '@mui/material';
import {
  Memory,
  Storage,
  Speed,
  NetworkCheck,
  Security,
  Error,
  Warning,
  CheckCircle,
  TrendingUp,
  TrendingDown,
  Refresh,
  Download,
  Settings,
  MonitorHeart,
  DataUsage,
  CloudQueue,
  Dashboard,
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
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ComposedChart,
  ReferenceLine,
} from 'recharts';

const SystemMonitoringPage = () => {
  const [selectedPeriod, setSelectedPeriod] = useState('hour');
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [lastRefresh, setLastRefresh] = useState(new Date());
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // 시스템 상태 데이터
  const systemStatus = {
    overall: 'healthy', // healthy, warning, critical
    uptime: '99.85%',
    responseTime: 145,
    throughput: 1250,
    errorRate: 0.12,
    activeUsers: 847,
    totalRequests: 125840,
    serverLoad: 68.5
  };

  // 실시간 메트릭 데이터
  const realtimeMetrics = [
    {
      time: '10:00',
      CPU: 45.2,
      Memory: 67.8,
      Disk: 34.5,
      Network: 23.1,
      ActiveUsers: 320,
      ResponseTime: 120
    },
    {
      time: '10:05',
      CPU: 52.1,
      Memory: 71.2,
      Disk: 35.8,
      Network: 31.2,
      ActiveUsers: 385,
      ResponseTime: 145
    },
    {
      time: '10:10',
      CPU: 48.9,
      Memory: 69.5,
      Disk: 36.2,
      Network: 28.7,
      ActiveUsers: 402,
      ResponseTime: 138
    },
    {
      time: '10:15',
      CPU: 61.3,
      Memory: 74.1,
      Disk: 37.1,
      Network: 45.8,
      ActiveUsers: 456,
      ResponseTime: 162
    },
    {
      time: '10:20',
      CPU: 58.7,
      Memory: 72.3,
      Disk: 38.4,
      Network: 41.2,
      ActiveUsers: 489,
      ResponseTime: 158
    },
    {
      time: '10:25',
      CPU: 55.2,
      Memory: 70.8,
      Disk: 39.1,
      Network: 38.9,
      ActiveUsers: 467,
      ResponseTime: 152
    },
  ];

  // 에러 로그 데이터
  const errorLogs = [
    {
      id: 1,
      timestamp: '2024-01-15 10:23:45',
      level: 'ERROR',
      service: 'Authentication',
      message: 'JWT token validation failed',
      count: 12
    },
    {
      id: 2,
      timestamp: '2024-01-15 10:18:32',
      level: 'WARNING',
      service: 'Market Data',
      message: 'API rate limit approaching',
      count: 5
    },
    {
      id: 3,
      timestamp: '2024-01-15 10:12:18',
      level: 'ERROR',
      service: 'Database',
      message: 'Connection pool exhausted',
      count: 3
    },
    {
      id: 4,
      timestamp: '2024-01-15 10:08:45',
      level: 'WARNING',
      service: 'WebSocket',
      message: 'High connection latency detected',
      count: 8
    },
    {
      id: 5,
      timestamp: '2024-01-15 10:05:12',
      level: 'INFO',
      service: 'System',
      message: 'Scheduled backup completed',
      count: 1
    },
  ];

  // 서비스 상태
  const servicesStatus = [
    { name: 'Web Server', status: 'healthy', uptime: 99.95, responseTime: 85 },
    { name: 'API Gateway', status: 'healthy', uptime: 99.87, responseTime: 120 },
    { name: 'Database', status: 'warning', uptime: 99.72, responseTime: 245 },
    { name: 'Redis Cache', status: 'healthy', uptime: 99.98, responseTime: 15 },
    { name: 'Market Data', status: 'healthy', uptime: 99.82, responseTime: 180 },
    { name: 'WebSocket', status: 'warning', uptime: 99.65, responseTime: 95 },
    { name: 'File Storage', status: 'healthy', uptime: 99.91, responseTime: 65 },
    { name: 'ML Service', status: 'healthy', uptime: 99.78, responseTime: 320 },
  ];

  // 리소스 사용량 분포
  const resourceUsage = [
    { name: 'CPU', used: 68.5, total: 100, unit: '%' },
    { name: 'Memory', used: 12.8, total: 16, unit: 'GB' },
    { name: 'Disk', used: 245, total: 500, unit: 'GB' },
    { name: 'Network', used: 2.8, total: 10, unit: 'Gbps' },
  ];

  // 트래픽 분포
  const trafficDistribution = [
    { name: 'Web UI', value: 45, color: '#8884d8' },
    { name: 'Mobile App', value: 30, color: '#82ca9d' },
    { name: 'API Calls', value: 20, color: '#ffc658' },
    { name: 'WebSocket', value: 5, color: '#ff7c7c' },
  ];

  // 데이터베이스 성능
  const dbPerformance = [
    {
      time: '10:00',
      Queries: 1250,
      AvgResponseTime: 45,
      SlowQueries: 12,
      Connections: 85
    },
    {
      time: '10:10',
      Queries: 1380,
      AvgResponseTime: 52,
      SlowQueries: 18,
      Connections: 92
    },
    {
      time: '10:20',
      Queries: 1456,
      AvgResponseTime: 48,
      SlowQueries: 15,
      Connections: 89
    },
    {
      time: '10:30',
      Queries: 1298,
      AvgResponseTime: 41,
      SlowQueries: 8,
      Connections: 78
    },
  ];

  // 자동 새로고침
  useEffect(() => {
    let interval: NodeJS.Timeout;

    if (autoRefresh) {
      interval = setInterval(() => {
        setLastRefresh(new Date());
      }, 30000); // 30초마다 새로고침
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
    console.log(`Exporting system monitoring data in ${format} format`);
    handleExportMenuClose();
  };

  const handleRefresh = () => {
    setLastRefresh(new Date());
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy': return '#4CAF50';
      case 'warning': return '#FF9800';
      case 'critical': return '#F44336';
      default: return '#757575';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'healthy': return <CheckCircle sx={{ color: '#4CAF50' }} />;
      case 'warning': return <Warning sx={{ color: '#FF9800' }} />;
      case 'critical': return <Error sx={{ color: '#F44336' }} />;
      default: return <CheckCircle sx={{ color: '#757575' }} />;
    }
  };

  const getLogLevelColor = (level: string) => {
    switch (level) {
      case 'ERROR': return 'error';
      case 'WARNING': return 'warning';
      case 'INFO': return 'info';
      default: return 'default';
    }
  };

  const formatUptime = (uptime: number) => {
    return `${uptime.toFixed(2)}%`;
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            시스템 통계 및 모니터링
          </Typography>
          <Typography variant="body1" color="text.secondary">
            실시간 시스템 상태, 성능 지표, 리소스 사용량 모니터링
          </Typography>
        </Box>

        {/* 컨트롤 패널 */}
        <Box display="flex" gap={2} alignItems="center">
          <Typography variant="body2" color="text.secondary">
            마지막 업데이트: {lastRefresh.toLocaleTimeString()}
          </Typography>

          <FormControl size="small" sx={{ minWidth: 100 }}>
            <InputLabel>주기</InputLabel>
            <Select
              value={selectedPeriod}
              label="주기"
              onChange={(e) => setSelectedPeriod(e.target.value)}
            >
              <MenuItem value="minute">1분</MenuItem>
              <MenuItem value="hour">1시간</MenuItem>
              <MenuItem value="day">1일</MenuItem>
            </Select>
          </FormControl>

          <Button
            variant="outlined"
            size="small"
            onClick={() => setAutoRefresh(!autoRefresh)}
            color={autoRefresh ? "primary" : "inherit"}
          >
            {autoRefresh ? '자동새로고침' : '수동모드'}
          </Button>

          <IconButton onClick={handleRefresh}>
            <Refresh />
          </IconButton>

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
            <MenuItem onClick={() => handleExport('PDF')}>시스템 리포트 (PDF)</MenuItem>
            <MenuItem onClick={() => handleExport('Excel')}>성능 데이터 (Excel)</MenuItem>
            <MenuItem onClick={() => handleExport('JSON')}>로그 데이터 (JSON)</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 시스템 상태 알림 */}
      <Alert
        severity={systemStatus.overall === 'healthy' ? 'success' : systemStatus.overall === 'warning' ? 'warning' : 'error'}
        sx={{ mb: 3 }}
      >
        <Typography variant="body2">
          <strong>시스템 상태: {systemStatus.overall === 'healthy' ? '정상' : systemStatus.overall === 'warning' ? '주의' : '위험'}</strong>
          {' '} | 가동률: {systemStatus.uptime} | 평균 응답시간: {systemStatus.responseTime}ms | 활성 사용자: {systemStatus.activeUsers.toLocaleString()}명
        </Typography>
      </Alert>

      {/* 주요 시스템 지표 카드 */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="success.main">
                    {systemStatus.uptime}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    시스템 가동률
                  </Typography>
                </Box>
                <MonitorHeart color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {systemStatus.responseTime}ms
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    평균 응답시간
                  </Typography>
                </Box>
                <Speed color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {systemStatus.activeUsers.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    활성 사용자
                  </Typography>
                </Box>
                <DataUsage color="info" sx={{ fontSize: 40, opacity: 0.7 }} />
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
                    {systemStatus.errorRate}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    에러율
                  </Typography>
                </Box>
                <Error color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* 실시간 시스템 메트릭 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              실시간 시스템 메트릭
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <ComposedChart data={realtimeMetrics}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="right" dataKey="ActiveUsers" fill="#e3f2fd" name="활성 사용자" />
                <Line yAxisId="left" type="monotone" dataKey="CPU" stroke="#f44336" name="CPU 사용률 (%)" />
                <Line yAxisId="left" type="monotone" dataKey="Memory" stroke="#2196f3" name="메모리 사용률 (%)" />
                <Line yAxisId="right" type="monotone" dataKey="ResponseTime" stroke="#ff9800" name="응답시간 (ms)" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 트래픽 분포 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              트래픽 소스 분포
            </Typography>
            <ResponsiveContainer width="100%" height={350}>
              <PieChart>
                <Pie
                  data={trafficDistribution}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, value }) => `${name} ${value}%`}
                >
                  {trafficDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 리소스 사용량 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              리소스 사용량
            </Typography>
            {resourceUsage.map((resource) => (
              <Box key={resource.name} mb={3}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <Typography variant="body2">
                    {resource.name}
                  </Typography>
                  <Typography variant="body2" fontWeight="bold">
                    {resource.used}{resource.unit} / {resource.total}{resource.unit}
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={(resource.used / resource.total) * 100}
                  sx={{
                    height: 8,
                    borderRadius: 1,
                    backgroundColor: '#f5f5f5',
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: (resource.used / resource.total) > 0.8 ? '#f44336' :
                                     (resource.used / resource.total) > 0.6 ? '#ff9800' : '#4caf50'
                    }
                  }}
                />
              </Box>
            ))}
          </Paper>
        </Grid>

        {/* 서비스 상태 */}
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              서비스 상태
            </Typography>
            <List>
              {servicesStatus.map((service, index) => (
                <React.Fragment key={service.name}>
                  <ListItem>
                    <ListItemIcon>
                      {getStatusIcon(service.status)}
                    </ListItemIcon>
                    <ListItemText
                      primary={service.name}
                      secondary={
                        <Box>
                          <Typography variant="body2" component="span">
                            가동률: {formatUptime(service.uptime)} | 응답시간: {service.responseTime}ms
                          </Typography>
                        </Box>
                      }
                    />
                    <Chip
                      label={service.status === 'healthy' ? '정상' : service.status === 'warning' ? '주의' : '위험'}
                      size="small"
                      sx={{
                        backgroundColor: getStatusColor(service.status),
                        color: 'white',
                        fontWeight: 'bold'
                      }}
                    />
                  </ListItem>
                  {index < servicesStatus.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* 데이터베이스 성능 */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              데이터베이스 성능
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <ComposedChart data={dbPerformance}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="left" dataKey="Queries" fill="#8884d8" name="쿼리 수" />
                <Line yAxisId="right" type="monotone" dataKey="AvgResponseTime" stroke="#82ca9d" name="평균 응답시간 (ms)" />
                <Line yAxisId="left" type="monotone" dataKey="SlowQueries" stroke="#ffc658" name="느린 쿼리 수" />
                <Line yAxisId="right" type="monotone" dataKey="Connections" stroke="#ff7c7c" name="연결 수" />
              </ComposedChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* 최근 에러 로그 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              최근 시스템 로그
            </Typography>
            <List sx={{ maxHeight: 300, overflow: 'auto' }}>
              {errorLogs.map((log) => (
                <React.Fragment key={log.id}>
                  <ListItem alignItems="flex-start">
                    <ListItemText
                      primary={
                        <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                          <Chip
                            label={log.level}
                            size="small"
                            color={getLogLevelColor(log.level) as any}
                          />
                          <Typography variant="body2" color="text.secondary">
                            {log.service}
                          </Typography>
                          {log.count > 1 && (
                            <Chip
                              label={`${log.count}회`}
                              size="small"
                              variant="outlined"
                            />
                          )}
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" mb={0.5}>
                            {log.message}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {log.timestamp}
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItem>
                  {log.id !== errorLogs[errorLogs.length - 1].id && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default SystemMonitoringPage;