'use client';

import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Chip,
  Button,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Tooltip,
  Alert,
  Stack,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  Shield,
  Warning,
  TrendingDown,
  Assessment,
  Refresh,
  Settings,
  NotificationsActive,
  Security,
  ShowChart,
  Info,
  Close,
} from '@mui/icons-material';
import {
  useGetApiV1RiskDashboard,
  useGetApiV1RiskAlerts,
  usePostApiV1RiskAlertsConfigure,
  usePutApiV1RiskLimitsPortfolioId,
} from '@/shared/api/generated/리스크-관리/리스크-관리';
import type { RiskAlert } from '@/shared/api/generated/model';

interface RiskDashboardProps {
  portfolioId?: number;
}

/**
 * 리스크 관리 대시보드 컴포넌트
 * VaR, 리스크 알림, 포트폴리오 위험도 분석 및 관리
 */
export function RiskDashboard({ portfolioId }: RiskDashboardProps) {
  const [refreshing, setRefreshing] = React.useState(false);
  const [alertConfigOpen, setAlertConfigOpen] = React.useState(false);
  const [selectedAlertType, setSelectedAlertType] = React.useState<string>('VAR');
  const [alertThreshold, setAlertThreshold] = React.useState<string>('');
  const [riskLimitsOpen, setRiskLimitsOpen] = React.useState(false);
  const [maxDrawdownLimit, setMaxDrawdownLimit] = React.useState<string>('');
  const [concentrationLimit, setConcentrationLimit] = React.useState<string>('');

  // Fetch risk dashboard data
  const { data: dashboardData, isLoading: dashboardLoading, refetch: refetchDashboard } = useGetApiV1RiskDashboard({
    query: {
      refetchInterval: 60000, // Refresh every minute
    }
  });

  // Fetch risk alerts
  const { data: alertsData, isLoading: alertsLoading, refetch: refetchAlerts } = useGetApiV1RiskAlerts({}, {
    query: {
      refetchInterval: 30000, // Refresh every 30 seconds
    }
  });

  // Risk alert configuration mutation
  const { mutate: configureAlert, isPending: configuringAlert } = usePostApiV1RiskAlertsConfigure({
    mutation: {
      onSuccess: () => {
        refetchAlerts();
        setAlertConfigOpen(false);
        setAlertThreshold('');
      },
      onError: (error) => {
        console.error('Failed to configure alert:', error);
      },
    },
  });

  // Risk limits update mutation
  const { mutate: updateRiskLimits, isPending: updatingLimits } = usePutApiV1RiskLimitsPortfolioId({
    mutation: {
      onSuccess: () => {
        refetchDashboard();
        setRiskLimitsOpen(false);
        setMaxDrawdownLimit('');
        setConcentrationLimit('');
      },
      onError: (error) => {
        console.error('Failed to update risk limits:', error);
      },
    },
  });

  const handleRefresh = () => {
    setRefreshing(true);
    Promise.all([refetchDashboard(), refetchAlerts()]).finally(() => {
      setRefreshing(false);
    });
  };

  const handleConfigureAlert = () => {
    if (!alertThreshold) return;

    const alertData: any = {
      notificationEnabled: true,
      portfolioId,
    };

    // Map selectedAlertType to correct API property
    switch (selectedAlertType) {
      case 'VAR':
        alertData.varThreshold = alertThreshold;
        break;
      case 'CONCENTRATION':
        alertData.concentrationThreshold = alertThreshold;
        break;
      case 'VOLATILITY':
        alertData.volatilityThreshold = alertThreshold;
        break;
      default:
        // Default to VAR threshold
        alertData.varThreshold = alertThreshold;
    }

    configureAlert({
      data: alertData,
    });
  };

  const handleUpdateRiskLimits = () => {
    if (!portfolioId || (!maxDrawdownLimit && !concentrationLimit)) return;

    updateRiskLimits({
      portfolioId,
      data: {
        maxDrawdown: maxDrawdownLimit || undefined,
        maxConcentration: concentrationLimit || undefined,
      },
    });
  };

  const getRiskRatingColor = (rating: string): 'success' | 'warning' | 'error' | 'default' => {
    switch (rating?.toUpperCase()) {
      case 'LOW':
        return 'success';
      case 'MEDIUM':
        return 'warning';
      case 'HIGH':
        return 'error';
      default:
        return 'default';
    }
  };

  const getAlertSeverityColor = (severity: string): 'success' | 'warning' | 'error' | 'default' => {
    switch (severity?.toUpperCase()) {
      case 'LOW':
        return 'success';
      case 'MEDIUM':
        return 'warning';
      case 'HIGH':
        return 'error';
      default:
        return 'default';
    }
  };

  const formatPercentage = (value: string | undefined) => {
    if (!value) return '-';
    const num = parseFloat(value);
    return `${num >= 0 ? '+' : ''}${num.toFixed(2)}%`;
  };

  const formatCurrency = (value: string | undefined) => {
    if (!value) return '-';
    return `₩${parseFloat(value).toLocaleString()}`;
  };

  const formatRatio = (value: string | undefined) => {
    if (!value) return '-';
    return parseFloat(value).toFixed(3);
  };

  const alerts = alertsData?.alerts || [];
  const isLoading = dashboardLoading || alertsLoading;

  if (isLoading) {
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          리스크 관리 대시보드
        </Typography>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Shield color="primary" />
          <Typography variant="h6">
            리스크 관리 대시보드
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <Tooltip title="알림 설정">
            <IconButton
              onClick={() => setAlertConfigOpen(true)}
              color="primary"
            >
              <NotificationsActive />
            </IconButton>
          </Tooltip>

          {portfolioId && (
            <Tooltip title="리스크 한도 설정">
              <IconButton
                onClick={() => setRiskLimitsOpen(true)}
                color="primary"
              >
                <Settings />
              </IconButton>
            </Tooltip>
          )}

          <Tooltip title="새로고침">
            <IconButton
              onClick={handleRefresh}
              disabled={refreshing}
              color="primary"
            >
              <Refresh />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Risk Overview Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Security sx={{ fontSize: 32, color: 'primary.main', mb: 1 }} />
              <Typography variant="h5" fontWeight="bold">
                {dashboardData?.overallRiskScore || '-'}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                종합 리스크 점수
              </Typography>
              {dashboardData?.riskRating && (
                <Box sx={{ mt: 1 }}>
                  <Chip
                    label={dashboardData.riskRating}
                    color={getRiskRatingColor(dashboardData.riskRating)}
                    size="small"
                  />
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <TrendingDown sx={{ fontSize: 32, color: 'error.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {formatCurrency(dashboardData?.var95)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                95% VaR (일일)
              </Typography>
              <Typography variant="body2" sx={{ mt: 0.5 }}>
                99% VaR: {formatCurrency(dashboardData?.var99)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <ShowChart sx={{ fontSize: 32, color: 'warning.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {formatPercentage(dashboardData?.portfolioVolatility)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                포트폴리오 변동성
              </Typography>
              <Typography variant="body2" sx={{ mt: 0.5 }}>
                샤프 비율: {formatRatio(dashboardData?.sharpeRatio)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Assessment sx={{ fontSize: 32, color: 'success.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {formatCurrency(dashboardData?.totalPortfolioValue)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                총 포트폴리오 가치
              </Typography>
              <Typography variant="body2" sx={{ mt: 0.5 }}>
                최대 손실: {formatPercentage(dashboardData?.maxDrawdown)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Risk Alerts */}
      {alerts.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Warning color="warning" />
              활성 리스크 알림
            </Typography>

            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>유형</TableCell>
                    <TableCell>메시지</TableCell>
                    <TableCell align="right">현재값</TableCell>
                    <TableCell align="right">임계값</TableCell>
                    <TableCell align="center">심각도</TableCell>
                    <TableCell align="center">발생시간</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {alerts.map((alert: RiskAlert) => (
                    <TableRow key={alert.id}>
                      <TableCell>
                        <Chip
                          label={alert.alertType || 'UNKNOWN'}
                          size="small"
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>{alert.message}</TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" fontWeight="medium">
                          {alert.currentValue || '-'}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2">
                          {alert.thresholdValue || '-'}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={alert.severity || 'UNKNOWN'}
                          color={getAlertSeverityColor(alert.severity || '')}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="caption">
                          {alert.triggeredAt ? new Date(alert.triggeredAt).toLocaleString() : '-'}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {/* Risk Breakdown */}
      {dashboardData?.riskBreakdown && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              리스크 분석
            </Typography>

            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Box>
                  <Typography variant="subtitle2" gutterBottom>
                    집중 리스크
                  </Typography>
                  <Typography variant="h6" color="warning.main">
                    {formatPercentage(dashboardData.concentrationRisk)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    단일 종목 최대 비중
                  </Typography>
                </Box>
              </Grid>

              <Grid item xs={12} md={6}>
                <Box>
                  <Typography variant="subtitle2" gutterBottom>
                    최대 손실률 (MDD)
                  </Typography>
                  <Typography variant="h6" color="error.main">
                    {formatPercentage(dashboardData.maxDrawdown)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    역사적 최대 낙폭
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
      {refreshing && (
        <Box sx={{ mb: 2 }}>
          <LinearProgress />
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            리스크 데이터를 업데이트하고 있습니다...
          </Typography>
        </Box>
      )}

      {/* Risk Alert Configuration Dialog */}
      <Dialog open={alertConfigOpen} onClose={() => setAlertConfigOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          리스크 알림 설정
          <IconButton
            onClick={() => setAlertConfigOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <Close />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Stack spacing={3} sx={{ mt: 1 }}>
            <FormControl fullWidth>
              <InputLabel>알림 유형</InputLabel>
              <Select
                value={selectedAlertType}
                label="알림 유형"
                onChange={(e) => setSelectedAlertType(e.target.value)}
              >
                <MenuItem value="VAR">VaR 초과</MenuItem>
                <MenuItem value="DRAWDOWN">최대 손실률</MenuItem>
                <MenuItem value="CONCENTRATION">집중 리스크</MenuItem>
                <MenuItem value="VOLATILITY">변동성</MenuItem>
              </Select>
            </FormControl>

            <TextField
              label="임계값"
              value={alertThreshold}
              onChange={(e) => setAlertThreshold(e.target.value)}
              placeholder="예: 5.0 (5%)"
              fullWidth
              helperText="백분율로 입력하세요 (예: 5.0 = 5%)"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAlertConfigOpen(false)}>
            취소
          </Button>
          <Button
            onClick={handleConfigureAlert}
            variant="contained"
            disabled={!alertThreshold || configuringAlert}
          >
            {configuringAlert ? '설정 중...' : '설정'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Risk Limits Configuration Dialog */}
      <Dialog open={riskLimitsOpen} onClose={() => setRiskLimitsOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          리스크 한도 설정
          <IconButton
            onClick={() => setRiskLimitsOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <Close />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Stack spacing={3} sx={{ mt: 1 }}>
            <TextField
              label="최대 손실률 한도"
              value={maxDrawdownLimit}
              onChange={(e) => setMaxDrawdownLimit(e.target.value)}
              placeholder="예: 20.0 (20%)"
              fullWidth
              helperText="백분율로 입력하세요"
            />

            <TextField
              label="집중 리스크 한도"
              value={concentrationLimit}
              onChange={(e) => setConcentrationLimit(e.target.value)}
              placeholder="예: 30.0 (30%)"
              fullWidth
              helperText="단일 종목 최대 비중 한도"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRiskLimitsOpen(false)}>
            취소
          </Button>
          <Button
            onClick={handleUpdateRiskLimits}
            variant="contained"
            disabled={(!maxDrawdownLimit && !concentrationLimit) || updatingLimits}
          >
            {updatingLimits ? '업데이트 중...' : '업데이트'}
          </Button>
        </DialogActions>
      </Dialog>

      <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
        ⚠️ 리스크 지표는 참고용이며, 실제 투자 결정 시 신중히 검토하시기 바랍니다.
      </Typography>
    </Box>
  );
}