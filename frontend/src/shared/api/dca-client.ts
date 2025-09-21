/**
 * DCA (Dollar Cost Averaging) API Client (Latest OpenAPI Integration)
 * DCA 시뮬레이션 관련 API 클라이언트 - 최신 OpenAPI 스펙 기반
 */

// Re-export from generated API
export {
  simulate,
  getSimulateMutationOptions,
  useSimulate,
  testEndpoint,
  getTestEndpointQueryOptions,
  useTestEndpoint,
} from './generated/dca-controller/dca-controller';

// Re-export types
export type {
  DCASimulationRequest,
  Simulate200,
} from './generated/model';

// Enhanced hooks with better caching and error handling
import { useMutation, useQuery } from '@tanstack/react-query';
import { simulate as apiSimulate, testEndpoint as apiTestEndpoint } from './generated/dca-controller/dca-controller';
import type { DCASimulationRequest } from './generated/model';

/**
 * Enhanced DCA Simulation Hook with validation and caching
 */
export const useDCASimulation = () => {
  return useMutation({
    mutationFn: (request: DCASimulationRequest) => {
      // Validation
      validateDCARequest(request);
      return apiSimulate(request);
    },
    onSuccess: (data) => {
      console.log('DCA 시뮬레이션 완료:', data);
    },
    onError: (error) => {
      console.error('DCA 시뮬레이션 실패:', error);
    },
  });
};

/**
 * DCA Test Endpoint Hook
 */
export const useDCATest = () => {
  return useQuery({
    queryKey: dcaQueryKeys.test(),
    queryFn: () => apiTestEndpoint(),
    staleTime: 10 * 60 * 1000, // 10분간 캐시
  });
};

/**
 * DCA Query Keys for cache management
 */
export const dcaQueryKeys = {
  all: ['dca'] as const,
  simulation: (params: DCASimulationRequest) => ['dca', 'simulation', params] as const,
  test: () => ['dca', 'test'] as const,
} as const;

/**
 * DCA 요청 데이터 검증 함수
 */
export function validateDCARequest(request: DCASimulationRequest): void {
  // 종목 코드 검증
  if (!request.symbol || request.symbol.trim() === '') {
    throw new Error('종목 코드는 필수입니다');
  }

  // 투자 금액 검증
  if (!request.monthlyInvestmentAmount || request.monthlyInvestmentAmount <= 0) {
    throw new Error('투자 금액은 0보다 커야 합니다');
  }

  // 날짜 검증
  if (!request.startDate || !request.endDate) {
    throw new Error('시작일과 종료일은 필수입니다');
  }

  const startDate = new Date(request.startDate);
  const endDate = new Date(request.endDate);

  if (startDate >= endDate) {
    throw new Error('종료일은 시작일보다 늦어야 합니다');
  }

  // 투자 주기 검증
  const validFrequencies = ['DAILY', 'WEEKLY', 'MONTHLY'];
  if (request.frequency && !validFrequencies.includes(request.frequency)) {
    throw new Error('유효하지 않은 투자 주기입니다');
  }
}

/**
 * Legacy DCA Client for backward compatibility
 */
export class DCAClient {
  private readonly baseURL: string;

  constructor(baseURL: string = 'http://localhost:8080') {
    this.baseURL = baseURL;
  }

  /**
   * Legacy DCA 시뮬레이션 실행 (호환성을 위해 유지)
   */
  async simulate(request: DCASimulationRequest): Promise<any> {
    validateDCARequest(request);
    return apiSimulate(request);
  }

  /**
   * Legacy 요청 검증 (호환성을 위해 유지)
   */
  validateRequest(request: DCASimulationRequest): void {
    validateDCARequest(request);
  }

  /**
   * Legacy 응답 변환 (호환성을 위해 유지)
   */
  transformResponse(apiResponse: any): any {
    return apiResponse;
  }
}

/**
 * DCA 유틸리티 함수들
 */
export const dcaUtils = {
  /**
   * DCA 시뮬레이션 실행 (함수형 인터페이스)
   */
  simulate: async (request: DCASimulationRequest) => {
    validateDCARequest(request);
    return apiSimulate(request);
  },

  /**
   * DCA 테스트 엔드포인트 호출
   */
  test: async () => {
    return apiTestEndpoint();
  },

  /**
   * 요청 데이터 검증
   */
  validateRequest: validateDCARequest,
};

/**
 * 기본 DCA 클라이언트 인스턴스 (호환성을 위해 유지)
 */
export const dcaClient = new DCAClient();

// Default export for backward compatibility
export default dcaUtils;