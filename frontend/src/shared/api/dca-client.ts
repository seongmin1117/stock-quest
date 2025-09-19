/**
 * DCA 시뮬레이션 API 클라이언트
 * 백엔드 DCA 시뮬레이션 API와 통신하는 클라이언트
 */

import axios, { AxiosResponse } from 'axios';
import {
  DCASimulationRequest,
  DCASimulationResponse,
  DCASimulationErrorResponse,
  DCAApiError,
  InvestmentFrequency
} from './types/dca-types';

const VALID_FREQUENCIES: InvestmentFrequency[] = ['DAILY', 'WEEKLY', 'MONTHLY'];

/**
 * DCA 시뮬레이션 API 클라이언트 클래스
 */
export class DCAClient {
  private readonly baseURL: string;

  constructor(baseURL: string = 'http://localhost:8080') {
    this.baseURL = baseURL;
  }

  /**
   * DCA 시뮬레이션 실행
   *
   * @param request DCA 시뮬레이션 요청 데이터
   * @returns 시뮬레이션 결과
   */
  async simulate(request: DCASimulationRequest): Promise<DCASimulationResponse> {
    try {
      // 요청 데이터 검증
      this.validateRequest(request);

      // API 호출
      const response: AxiosResponse<DCASimulationResponse> = await axios.post(
        `${this.baseURL}/api/v1/dca/simulate`,
        request
      );

      // 응답 데이터 검증
      if (!response.data) {
        throw new Error('응답 데이터가 없습니다');
      }

      // 응답 변환 및 반환
      return this.transformResponse(response.data);

    } catch (error: any) {
      // 에러 처리 및 재던지기
      throw this.handleError(error);
    }
  }

  /**
   * 요청 데이터 검증
   *
   * @param request 검증할 요청 데이터
   */
  validateRequest(request: DCASimulationRequest): void {
    // 종목 코드 검증
    if (!request.symbol || request.symbol.trim() === '') {
      throw new Error('종목 코드는 필수입니다');
    }

    // 투자 금액 검증
    if (request.monthlyInvestmentAmount <= 0) {
      throw new Error('투자 금액은 0보다 커야 합니다');
    }

    // 투자 주기 검증
    if (!VALID_FREQUENCIES.includes(request.frequency)) {
      throw new Error('유효하지 않은 투자 주기입니다');
    }

    // 날짜 검증
    const startDate = new Date(request.startDate);
    const endDate = new Date(request.endDate);

    if (startDate >= endDate) {
      throw new Error('종료일은 시작일보다 늦어야 합니다');
    }
  }

  /**
   * API 응답을 클라이언트 형식으로 변환
   *
   * @param apiResponse API에서 받은 응답
   * @returns 변환된 응답 데이터
   */
  transformResponse(apiResponse: DCASimulationResponse): DCASimulationResponse {
    // 현재는 1:1 매핑이지만, 필요시 변환 로직 추가 가능
    return {
      ...apiResponse,
      // 날짜 형식이나 숫자 형식 변환 로직을 여기에 추가할 수 있음
    };
  }

  /**
   * API 에러 처리 (새로운 구조화된 에러 응답 형식 지원)
   *
   * @param error 발생한 에러
   * @returns 처리된 에러 객체
   */
  private handleError(error: any): Error {
    if (error.response) {
      // HTTP 에러 응답
      const status = error.response.status;
      const errorData = error.response.data;

      // 새로운 구조화된 에러 응답 형식 확인
      if (this.isStructuredErrorResponse(errorData)) {
        const structuredError = errorData as DCASimulationErrorResponse;
        return new Error(structuredError.message);
      }

      // 기존 에러 응답 형식 지원
      const message = errorData?.message || `HTTP Error: ${status}`;
      return new Error(message);
    } else if (error.request) {
      // 네트워크 에러
      return new Error('네트워크 연결을 확인해주세요');
    } else {
      // 기타 에러
      return error instanceof Error ? error : new Error('알 수 없는 오류가 발생했습니다');
    }
  }

  /**
   * 구조화된 에러 응답인지 확인
   *
   * @param errorData 에러 데이터
   * @returns 구조화된 에러 응답 여부
   */
  private isStructuredErrorResponse(errorData: any): boolean {
    return errorData &&
           typeof errorData.errorCode === 'string' &&
           typeof errorData.message === 'string' &&
           typeof errorData.timestamp === 'string' &&
           typeof errorData.path === 'string';
  }
}

/**
 * 기본 DCA 클라이언트 인스턴스
 */
export const dcaClient = new DCAClient();