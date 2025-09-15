/**
 * DCA 시뮬레이션 API 타입 정의
 */

export type InvestmentFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

/**
 * DCA 시뮬레이션 요청 타입
 */
export interface DCASimulationRequest {
  /** 종목 코드 */
  symbol: string;
  /** 월별 투자 금액 */
  monthlyInvestmentAmount: number;
  /** 시작일 (ISO 8601 형식) */
  startDate: string;
  /** 종료일 (ISO 8601 형식) */
  endDate: string;
  /** 투자 주기 */
  frequency: InvestmentFrequency;
}

/**
 * 월별 투자 기록 타입
 */
export interface DCAMonthlyRecord {
  /** 투자일 */
  investmentDate: string;
  /** 투자 금액 */
  investmentAmount: number;
  /** 주식 가격 */
  stockPrice: number;
  /** 매수 주식 수 */
  sharesPurchased: number;
  /** 포트폴리오 가치 */
  portfolioValue: number;
}

/**
 * DCA 시뮬레이션 응답 타입
 */
export interface DCASimulationResponse {
  /** 종목 코드 */
  symbol: string;
  /** 총 투자 금액 */
  totalInvestmentAmount: number;
  /** 최종 포트폴리오 가치 */
  finalPortfolioValue: number;
  /** 총 수익률 (%) */
  totalReturnPercentage: number;
  /** 연평균 수익률 (%) */
  annualizedReturn: number;
  /** 월별 투자 기록 */
  investmentRecords: DCAMonthlyRecord[];
  /** S&P 500 수익 금액 */
  sp500ReturnAmount: number;
  /** NASDAQ 수익 금액 */
  nasdaqReturnAmount: number;
  /** S&P 500 대비 초과 수익률 (%) */
  outperformanceVsSP500: number;
  /** NASDAQ 대비 초과 수익률 (%) */
  outperformanceVsNASDAQ: number;
  /** 최대 포트폴리오 가치 */
  maxPortfolioValue: number;
}

/**
 * DCA API 에러 응답 타입
 */
export interface DCAApiError {
  /** 에러 메시지 */
  message: string;
  /** HTTP 상태 코드 */
  status?: number;
  /** 추가 에러 정보 */
  details?: any;
}