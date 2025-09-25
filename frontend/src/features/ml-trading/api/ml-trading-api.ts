/**
 * ML Trading & Signals Feature API Client
 * Feature-sliced design pattern: features/ml-trading/api
 */

// Re-export from generated API (avoiding conflicts)
export {
  generateSignal,
  getGenerateSignalQueryOptions,
  useGenerateSignal as useGeneratedSignal,
  getActiveSignals,
  getGetActiveSignalsQueryOptions,
  useGetActiveSignals,
  updateSignalPerformance,
  getUpdateSignalPerformanceMutationOptions,
  useUpdateSignalPerformance,
  generateBatchSignals,
  getGenerateBatchSignalsMutationOptions,
  useGenerateBatchSignals,
  filterSignalsByMarketCondition,
  getFilterSignalsByMarketConditionMutationOptions,
  useFilterSignalsByMarketCondition,
} from '../../../shared/api/generated/ml-시그널/ml-시그널';

// Re-export types
export type {
  TradingSignalResponse,
  ActiveSignalsResponse,
  PerformanceUpdateRequest,
  PerformanceUpdateResponse,
  BatchSignalRequest,
  BatchSignalResponse,
  FilterSignalsRequest,
  FilteredSignalResponse,
} from '../../../shared/api/generated/model';

// Feature-specific API hooks and utilities
import { useMutation, useQuery } from '@tanstack/react-query';
import {
  generateSignal,
  getActiveSignals,
  updateSignalPerformance,
  generateBatchSignals,
  filterSignalsByMarketCondition
} from '../../../shared/api/generated/ml-시그널/ml-시그널';
import type {
  PerformanceUpdateRequest,
  BatchSignalRequest,
  FilterSignalsRequest
} from '../../../shared/api/generated/model';

/**
 * 개별 종목 시그널 생성 훅
 */
export const useGenerateSignal = (symbol: string) => {
  return useQuery({
    queryKey: mlTradingQueryKeys.signal(symbol),
    queryFn: () => generateSignal(symbol),
    enabled: !!symbol,
    staleTime: 5 * 60 * 1000, // 5분간 캐시
  });
};

/**
 * 활성 시그널 목록 조회 훅
 */
export const useActiveSignals = (limit?: number) => {
  return useQuery({
    queryKey: mlTradingQueryKeys.activeSignals(limit),
    queryFn: () => getActiveSignals(limit ? { limit } : undefined),
    refetchInterval: 30 * 1000, // 30초마다 갱신
  });
};

/**
 * 시그널 성과 업데이트 훅
 */
export const useSignalPerformanceUpdate = () => {
  return useMutation({
    mutationFn: ({ signalId, request }: { signalId: string; request: PerformanceUpdateRequest }) =>
      updateSignalPerformance(signalId, request),
    onSuccess: (data) => {
      console.log('시그널 성과 업데이트 완료:', data);
    },
    onError: (error) => {
      console.error('시그널 성과 업데이트 실패:', error);
    },
  });
};

/**
 * 배치 시그널 생성 훅
 */
export const useBatchSignalGeneration = () => {
  return useMutation({
    mutationFn: (request: BatchSignalRequest) => generateBatchSignals(request),
    onSuccess: (data) => {
      console.log('배치 시그널 생성 완료:', data);
    },
    onError: (error) => {
      console.error('배치 시그널 생성 실패:', error);
    },
  });
};

/**
 * 시장 조건별 시그널 필터링 훅
 */
export const useSignalFiltering = () => {
  return useMutation({
    mutationFn: (request: FilterSignalsRequest) => filterSignalsByMarketCondition(request),
    onSuccess: (data) => {
      console.log('시그널 필터링 완료:', data);
    },
    onError: (error) => {
      console.error('시그널 필터링 실패:', error);
    },
  });
};

/**
 * ML Trading 기능별 쿼리 키
 */
export const mlTradingQueryKeys = {
  all: ['ml-trading'] as const,
  signals: () => ['ml-trading', 'signals'] as const,
  signal: (symbol: string) => ['ml-trading', 'signal', symbol] as const,
  activeSignals: (limit?: number) => ['ml-trading', 'active-signals', { limit }] as const,
  performance: (signalId: string) => ['ml-trading', 'performance', signalId] as const,
  batchSignals: (params: BatchSignalRequest) => ['ml-trading', 'batch-signals', params] as const,
  filteredSignals: (params: FilterSignalsRequest) => ['ml-trading', 'filtered-signals', params] as const,
} as const;