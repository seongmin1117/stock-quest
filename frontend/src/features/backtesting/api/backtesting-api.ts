/**
 * Backtesting Feature API Client
 * Feature-sliced design pattern: features/backtesting/api
 */

// TODO: Backtesting controller not available in generated API yet
// These functions may be part of portfolio optimization controller
/*
export {
  runBacktest1 as runBacktest,
  getRunBacktest1MutationOptions as getRunBacktestMutationOptions,
  useRunBacktest1 as useRunBacktest,
  compareBacktests,
  getCompareBacktestsMutationOptions,
  useCompareBacktests,
} from '../../../shared/api/generated/backtesting-controller/backtesting-controller';
*/

// Re-export types (these should be available)
export type { BacktestRequest } from '../../../shared/api/generated/model/backtestRequest';
export type { BacktestResponse } from '../../../shared/api/generated/model/backtestResponse';
// TODO: These specific comparison types may not exist yet
/*
export type { BacktestComparisonRequest } from '../../../shared/api/generated/model/backtestComparisonRequest';
export type { BacktestComparisonResult } from '../../../shared/api/generated/model/backtestComparisonResult';
*/

// Feature-specific API hooks and utilities
import { useMutation, useQuery } from '@tanstack/react-query';
// TODO: Temporarily commented out until backtesting endpoints are available
/*
import {
  runBacktest1 as runBacktestApi,
  compareBacktests
} from '../../../shared/api/generated/backtesting-controller/backtesting-controller';
import type { BacktestRequest } from '../../../shared/api/generated/model/backtestRequest';
import type { BacktestComparisonRequest } from '../../../shared/api/generated/model/backtestComparisonRequest';
*/

// TODO: Temporarily commenting out until backtesting endpoints are available
/*
// 백테스팅 실행 훅
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

// 백테스팅 비교 훅
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

// Backtesting 기능별 쿼리 키
export const backtestingQueryKeys = {
  all: ['backtesting'] as const,
  run: (params: BacktestRequest) => ['backtesting', 'run', params] as const,
  compare: (params: BacktestComparisonRequest) => ['backtesting', 'compare', params] as const,
  results: (backtestId: string) => ['backtesting', 'results', backtestId] as const,
} as const;
*/

// Placeholder exports to prevent import errors
export const useRunBacktest = () => { throw new Error('Backtesting endpoints not yet implemented'); };
export const useBacktestExecution = () => { throw new Error('Backtesting endpoints not yet implemented'); };
export const useBacktestComparison = () => { throw new Error('Backtesting endpoints not yet implemented'); };
export const useCompareBacktests = () => { throw new Error('Backtesting endpoints not yet implemented'); };
export type BacktestComparisonRequest = any;
export type BacktestComparisonResult = any;