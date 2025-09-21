/**
 * Portfolio Optimization Feature API Client
 * Feature-sliced design pattern: features/portfolio-optimization/api
 */

// Re-export from generated API
export {
  optimizePortfolio,
  getOptimizePortfolioMutationOptions,
  useOptimizePortfolio,
  calculateEfficientFrontier,
  getCalculateEfficientFrontierMutationOptions,
  useCalculateEfficientFrontier,
  runBacktest,
  getRunBacktestMutationOptions,
  useRunBacktest,
  getRebalancingSuggestions,
  getGetRebalancingSuggestionsQueryOptions,
  useGetRebalancingSuggestions,
  getOptimizationHistory,
  getGetOptimizationHistoryQueryOptions,
  useGetOptimizationHistory,
} from '../../../shared/api/generated/portfolio-optimization-controller/portfolio-optimization-controller';

// Re-export types
export type {
  OptimizationRequest,
  PortfolioOptimizationResponse,
  EfficientFrontierRequest,
  EfficientFrontierResponse,
  BacktestRequest,
  BacktestResponse,
  RebalancingSuggestionsResponse,
  OptimizationHistoryResponse,
} from '../../../shared/api/generated/model';

// Feature-specific API hooks and utilities
import { useMutation, useQuery } from '@tanstack/react-query';
import {
  optimizePortfolio,
  calculateEfficientFrontier,
  runBacktest,
  getRebalancingSuggestions,
  getOptimizationHistory
} from '../../../shared/api/generated/portfolio-optimization-controller/portfolio-optimization-controller';
import type {
  OptimizationRequest,
  EfficientFrontierRequest,
  BacktestRequest
} from '../../../shared/api/generated/model';

/**
 * 포트폴리오 최적화 실행 훅
 */
export const usePortfolioOptimization = () => {
  return useMutation({
    mutationFn: ({ portfolioId, request }: { portfolioId: number; request: OptimizationRequest }) =>
      optimizePortfolio(portfolioId, request),
    onSuccess: (data) => {
      console.log('포트폴리오 최적화 완료:', data);
    },
    onError: (error) => {
      console.error('포트폴리오 최적화 실패:', error);
    },
  });
};

/**
 * 효율적 프론티어 계산 훅
 */
export const useEfficientFrontierCalculation = () => {
  return useMutation({
    mutationFn: ({ portfolioId, request }: { portfolioId: number; request: EfficientFrontierRequest }) =>
      calculateEfficientFrontier(portfolioId, request),
    onSuccess: (data) => {
      console.log('효율적 프론티어 계산 완료:', data);
    },
    onError: (error) => {
      console.error('효율적 프론티어 계산 실패:', error);
    },
  });
};

/**
 * 백테스팅 실행 훅
 */
export const usePortfolioBacktest = () => {
  return useMutation({
    mutationFn: ({ portfolioId, request }: { portfolioId: number; request: BacktestRequest }) =>
      runBacktest(portfolioId, request),
    onSuccess: (data) => {
      console.log('백테스팅 완료:', data);
    },
    onError: (error) => {
      console.error('백테스팅 실패:', error);
    },
  });
};

/**
 * 리밸런싱 제안 조회 훅
 */
export const useRebalancingSuggestions = (portfolioId: number) => {
  return useQuery({
    queryKey: portfolioOptimizationQueryKeys.rebalancingSuggestions(portfolioId),
    queryFn: () => getRebalancingSuggestions(portfolioId),
    enabled: !!portfolioId,
  });
};

/**
 * 최적화 히스토리 조회 훅
 */
export const useOptimizationHistory = (portfolioId: number, limit?: number) => {
  return useQuery({
    queryKey: portfolioOptimizationQueryKeys.history(portfolioId, limit),
    queryFn: () => getOptimizationHistory(portfolioId, limit ? { limit } : undefined),
    enabled: !!portfolioId,
  });
};

/**
 * Portfolio Optimization 기능별 쿼리 키
 */
export const portfolioOptimizationQueryKeys = {
  all: ['portfolio-optimization'] as const,
  optimization: (portfolioId: number, params: OptimizationRequest) =>
    ['portfolio-optimization', 'optimize', portfolioId, params] as const,
  efficientFrontier: (portfolioId: number, params: EfficientFrontierRequest) =>
    ['portfolio-optimization', 'efficient-frontier', portfolioId, params] as const,
  backtest: (portfolioId: number, params: BacktestRequest) =>
    ['portfolio-optimization', 'backtest', portfolioId, params] as const,
  rebalancingSuggestions: (portfolioId: number) =>
    ['portfolio-optimization', 'rebalancing-suggestions', portfolioId] as const,
  history: (portfolioId: number, limit?: number) =>
    ['portfolio-optimization', 'history', portfolioId, { limit }] as const,
} as const;