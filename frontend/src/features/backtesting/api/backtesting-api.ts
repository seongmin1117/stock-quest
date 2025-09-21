/**
 * Backtesting Feature API Client
 * Feature-sliced design pattern: features/backtesting/api
 */

// Re-export from generated API (using correct function names)
export {
  runBacktest1 as runBacktest,
  getRunBacktest1MutationOptions as getRunBacktestMutationOptions,
  useRunBacktest1 as useRunBacktest,
  compareBacktests,
  getCompareBacktestsMutationOptions,
  useCompareBacktests,
} from '../../../shared/api/generated/backtesting-controller/backtesting-controller';

// Re-export types
export type {
  BacktestRequest,
  BacktestResponse,
  BacktestComparisonRequest,
  BacktestComparisonResult,
} from '../../../shared/api/generated/model';

// Feature-specific API hooks and utilities
import { useMutation, useQuery } from '@tanstack/react-query';
import {
  runBacktest1 as runBacktestApi,
  compareBacktests
} from '../../../shared/api/generated/backtesting-controller/backtesting-controller';
import type {
  BacktestRequest,
  BacktestComparisonRequest
} from '../../../shared/api/generated/model';

/**
 * 백테스팅 실행 훅
 */
export const useBacktestExecution = () => {
  return useMutation({
    mutationFn: (request: BacktestRequest) => runBacktestApi(request),
    onSuccess: (data) => {
      console.log('백테스팅 실행 완료:', data);
    },
    onError: (error) => {
      console.error('백테스팅 실행 실패:', error);
    },
  });
};

/**
 * 백테스팅 비교 훅
 */
export const useBacktestComparison = () => {
  return useMutation({
    mutationFn: (request: BacktestComparisonRequest) => compareBacktests(request),
    onSuccess: (data) => {
      console.log('백테스팅 비교 완료:', data);
    },
    onError: (error) => {
      console.error('백테스팅 비교 실패:', error);
    },
  });
};

/**
 * Backtesting 기능별 쿼리 키
 */
export const backtestingQueryKeys = {
  all: ['backtesting'] as const,
  run: (params: BacktestRequest) => ['backtesting', 'run', params] as const,
  compare: (params: BacktestComparisonRequest) => ['backtesting', 'compare', params] as const,
  results: (backtestId: string) => ['backtesting', 'results', backtestId] as const,
} as const;