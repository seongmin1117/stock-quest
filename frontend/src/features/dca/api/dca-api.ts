/**
 * DCA (Dollar Cost Averaging) Feature API Client
 * Feature-sliced design pattern: features/dca/api
 */

// Re-export from generated API
export {
  simulate,
  getSimulateMutationOptions,
  useSimulate,
  getTestEndpointQueryOptions,
  useTestEndpoint,
} from '../../../shared/api/generated/dca-controller/dca-controller';

// Re-export types
export type {
  DCASimulationRequest,
  Simulate200,
} from '../../../shared/api/generated/model';

// Feature-specific API hooks and utilities
import { useMutation, useQuery } from '@tanstack/react-query';
import { simulate } from '../../../shared/api/generated/dca-controller/dca-controller';
import type { DCASimulationRequest } from '../../../shared/api/generated/model';

/**
 * DCA 시뮬레이션 실행 훅 (Feature-specific wrapper)
 */
export const useDCASimulation = () => {
  return useMutation({
    mutationFn: (request: DCASimulationRequest) => simulate(request),
    onSuccess: (data) => {
      console.log('DCA 시뮬레이션 완료:', data);
    },
    onError: (error) => {
      console.error('DCA 시뮬레이션 실패:', error);
    },
  });
};

/**
 * DCA 기능별 쿼리 키
 */
export const dcaQueryKeys = {
  all: ['dca'] as const,
  simulation: (params: DCASimulationRequest) => ['dca', 'simulation', params] as const,
  test: () => ['dca', 'test'] as const,
} as const;