/**
 * Risk Management Feature API Client
 * Feature-sliced design pattern: features/risk-management/api
 */

// Re-export from generated API
export {
  calculatePortfolioVaR,
  getCalculatePortfolioVaRMutationOptions,
  useCalculatePortfolioVaR,
  runStressTest,
  getRunStressTestMutationOptions,
  useRunStressTest,
  getRiskDashboard,
  getGetRiskDashboardQueryOptions,
  useGetRiskDashboard,
  getRiskAlerts,
  getGetRiskAlertsQueryOptions,
  useGetRiskAlerts,
  getRiskAlert,
  getGetRiskAlertQueryOptions,
  useGetRiskAlert,
  updateAlertStatus,
  getUpdateAlertStatusMutationOptions,
  useUpdateAlertStatus,
  resendAlert,
  getResendAlertMutationOptions,
  useResendAlert,
  getVaRHistory,
  getGetVaRHistoryQueryOptions,
  useGetVaRHistory,
} from '../../../shared/api/generated/risk-management-controller/risk-management-controller';

// Re-export types
export type {
  VaRCalculationRequest,
  VaRCalculationResponse,
  StressTestRequest,
  StressTestResponse,
  RiskDashboardResponse,
  RiskAlertResponse,
  AlertStatusUpdateRequest,
  ResendNotificationRequest,
  NotificationResponse,
  PageVaRCalculationResponse,
  PageRiskAlertResponse,
} from '../../../shared/api/generated/model';

// Feature-specific API hooks and utilities
import { useMutation, useQuery } from '@tanstack/react-query';
import {
  calculatePortfolioVaR,
  runStressTest,
  getRiskDashboard,
  getRiskAlerts,
  getRiskAlert,
  updateAlertStatus,
  resendAlert,
  getVaRHistory
} from '../../../shared/api/generated/risk-management-controller/risk-management-controller';
import type {
  VaRCalculationRequest,
  StressTestRequest,
  AlertStatusUpdateRequest,
  ResendNotificationRequest,
  GetRiskAlertsSeverity,
  GetRiskAlertsStatus,
  GetVaRHistoryMethod
} from '../../../shared/api/generated/model';

/**
 * VaR (Value at Risk) 계산 훅
 */
export const useVaRCalculation = () => {
  return useMutation({
    mutationFn: ({ portfolioId, request }: { portfolioId: string; request: VaRCalculationRequest }) =>
      calculatePortfolioVaR(portfolioId, request),
    onSuccess: (data) => {
      console.log('VaR 계산 완료:', data);
    },
    onError: (error) => {
      console.error('VaR 계산 실패:', error);
    },
  });
};

/**
 * 스트레스 테스트 실행 훅
 */
export const useStressTest = () => {
  return useMutation({
    mutationFn: ({ portfolioId, request }: { portfolioId: string; request: StressTestRequest }) =>
      runStressTest(portfolioId, request),
    onSuccess: (data) => {
      console.log('스트레스 테스트 완료:', data);
    },
    onError: (error) => {
      console.error('스트레스 테스트 실패:', error);
    },
  });
};

/**
 * 리스크 대시보드 조회 훅
 */
export const useRiskDashboard = (portfolioId?: string, days: number = 7) => {
  return useQuery({
    queryKey: riskManagementQueryKeys.dashboard(portfolioId, days),
    queryFn: () => getRiskDashboard({ portfolioId, days }),
    refetchInterval: 5 * 60 * 1000, // 5분마다 갱신
  });
};

/**
 * 리스크 알림 목록 조회 훅
 */
export const useRiskAlerts = (
  page: number = 0,
  size: number = 20,
  portfolioId?: string,
  severity?: GetRiskAlertsSeverity,
  status?: GetRiskAlertsStatus
) => {
  return useQuery({
    queryKey: riskManagementQueryKeys.alerts(page, size, portfolioId, severity, status),
    queryFn: () => getRiskAlerts({ page, size, portfolioId, severity, status }),
    refetchInterval: 30 * 1000, // 30초마다 갱신
  });
};

/**
 * 개별 리스크 알림 조회 훅
 */
export const useRiskAlert = (alertId: string) => {
  return useQuery({
    queryKey: riskManagementQueryKeys.alert(alertId),
    queryFn: () => getRiskAlert(alertId),
    enabled: !!alertId,
  });
};

/**
 * 알림 상태 업데이트 훅
 */
export const useAlertStatusUpdate = () => {
  return useMutation({
    mutationFn: ({ alertId, request }: { alertId: string; request: AlertStatusUpdateRequest }) =>
      updateAlertStatus(alertId, request),
    onSuccess: (data) => {
      console.log('알림 상태 업데이트 완료:', data);
    },
    onError: (error) => {
      console.error('알림 상태 업데이트 실패:', error);
    },
  });
};

/**
 * 알림 재전송 훅
 */
export const useAlertResend = () => {
  return useMutation({
    mutationFn: ({ alertId, request }: { alertId: string; request: ResendNotificationRequest }) =>
      resendAlert(alertId, request),
    onSuccess: (data) => {
      console.log('알림 재전송 완료:', data);
    },
    onError: (error) => {
      console.error('알림 재전송 실패:', error);
    },
  });
};

/**
 * VaR 히스토리 조회 훅
 */
export const useVaRHistory = (
  portfolioId: string,
  page: number = 0,
  size: number = 20,
  method?: GetVaRHistoryMethod
) => {
  return useQuery({
    queryKey: riskManagementQueryKeys.varHistory(portfolioId, page, size, method),
    queryFn: () => getVaRHistory(portfolioId, { page, size, method }),
    enabled: !!portfolioId,
  });
};

/**
 * Risk Management 기능별 쿼리 키
 */
export const riskManagementQueryKeys = {
  all: ['risk-management'] as const,
  var: (portfolioId: string, params: VaRCalculationRequest) =>
    ['risk-management', 'var', portfolioId, params] as const,
  stressTest: (portfolioId: string, params: StressTestRequest) =>
    ['risk-management', 'stress-test', portfolioId, params] as const,
  dashboard: (portfolioId?: string, days?: number) =>
    ['risk-management', 'dashboard', { portfolioId, days }] as const,
  alerts: (page?: number, size?: number, portfolioId?: string, severity?: GetRiskAlertsSeverity, status?: GetRiskAlertsStatus) =>
    ['risk-management', 'alerts', { page, size, portfolioId, severity, status }] as const,
  alert: (alertId: string) => ['risk-management', 'alert', alertId] as const,
  varHistory: (portfolioId: string, page?: number, size?: number, method?: GetVaRHistoryMethod) =>
    ['risk-management', 'var-history', portfolioId, { page, size, method }] as const,
} as const;