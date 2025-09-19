/**
 * User Challenge API Client
 * 사용자용 챌린지 API 클라이언트
 */

import apiClient from './api-client';

// Enums
export enum ChallengeStatus {
  DRAFT = 'DRAFT',
  SCHEDULED = 'SCHEDULED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  ARCHIVED = 'ARCHIVED',
  CANCELLED = 'CANCELLED'
}

export enum ChallengeDifficulty {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  EXPERT = 'EXPERT'
}

export enum ChallengeType {
  MARKET_CRASH = 'MARKET_CRASH',
  BULL_MARKET = 'BULL_MARKET',
  SECTOR_ROTATION = 'SECTOR_ROTATION',
  VOLATILITY = 'VOLATILITY',
  ESG = 'ESG',
  INTERNATIONAL = 'INTERNATIONAL',
  OPTIONS = 'OPTIONS',
  RISK_MANAGEMENT = 'RISK_MANAGEMENT',
  TOURNAMENT = 'TOURNAMENT',
  EDUCATIONAL = 'EDUCATIONAL',
  COMMUNITY = 'COMMUNITY'
}

// User Session Types (based on ChallengeDetailResponse.UserSession)
export interface UserSession {
  sessionId: number;
  status: 'READY' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'ENDED';
  currentBalance: number;
  returnRate: number;
  startedAt: string;
  completedAt?: string;
}

// Types
export interface Challenge {
  id: number;
  title: string;
  description: string;
  difficulty: ChallengeDifficulty;
  challengeType?: ChallengeType;
  status: ChallengeStatus;
  initialBalance: number;
  durationDays: number;
  startDate?: string;
  endDate?: string;
  instruments?: string[];
  userSession?: UserSession;

  // Optional legacy/extended fields for backward compatibility
  categoryId?: number;
  maxParticipants?: number;
  currentParticipants?: number;
  availableInstruments?: string[];
  tradingRestrictions?: Record<string, any>;
  successCriteria?: Record<string, any>;
  entryRequirements?: Record<string, any>;
  learningObjectives?: string;
  marketScenarioDescription?: string;
  riskLevel?: number;
  estimatedTimeMinutes?: number;
  estimatedDurationMinutes?: number;
  tags?: string[];
  featured?: boolean;
  isFeatured?: boolean;
  averageRating?: number;
  totalRatings?: number;
  totalReviews?: number;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;

  // Additional backend fields
  templateId?: number;
  marketPeriodId?: number;
  periodStart?: string;
  periodEnd?: string;
  speedFactor?: number;
  marketScenario?: Record<string, any>;
  sortOrder?: number;
  lastModifiedBy?: number;
  version?: number;

  // User-specific fields (legacy)
  userParticipated?: boolean;
  userCanParticipate?: boolean;
  userActiveSession?: ChallengeSession;
}

export interface ChallengeSession {
  id: number;
  challengeId: number;
  userId: number;
  status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED';
  initialBalance: number;
  currentBalance: number;
  totalReturn: number;
  totalReturnPercentage: number;
  startTime: string;
  endTime?: string;
  rank?: number;
  portfolioPositions: PortfolioPosition[];
}

export interface PortfolioPosition {
  symbol: string;
  companyName: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  marketValue: number;
  unrealizedPnl: number;
  unrealizedPnlPercentage: number;
}

export interface ChallengeInstrument {
  symbol: string;
  companyName: string;
  currentPrice: number;
  changeAmount: number;
  changePercentage: number;
  volume: number;
  marketCap: number;
  sector: string;
  available: boolean;
}

export interface ChallengeListParams {
  page?: number;
  size?: number;
  difficulty?: ChallengeDifficulty;
  challengeType?: ChallengeType;
  status?: ChallengeStatus;
  featured?: boolean;
  tags?: string[];
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface ChallengeListResponse {
  challenges: Challenge[];
  totalCount: number;
  totalElements?: number; // Backward compatibility
  totalPages?: number;    // Backward compatibility
  page: number;
  size: number;
  number?: number;       // Backward compatibility
  first?: boolean;       // Backward compatibility
  last?: boolean;        // Backward compatibility
}

export interface StartChallengeResponse {
  sessionId: number;
  challengeId: number;
  initialBalance: number;
  startTime: string;
  message?: string;
}

export const challengeApi = {
  // 챌린지 목록 조회
  getChallenges: async (params: ChallengeListParams = {}): Promise<ChallengeListResponse> => {
    const queryParams = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (Array.isArray(value)) {
          value.forEach(item => queryParams.append(key, item.toString()));
        } else {
          queryParams.append(key, value.toString());
        }
      }
    });

    const response = await apiClient.get<ChallengeListResponse>(
      `/api/challenges?${queryParams.toString()}`
    );
    return response;
  },

  // 활성 챌린지 목록 조회
  getActiveChallenges: async (): Promise<ChallengeListResponse> => {
    const response = await apiClient.get<ChallengeListResponse>('/api/challenges/active');
    return response;
  },

  // 챌린지 상세 조회
  getChallengeById: async (challengeId: number): Promise<Challenge> => {
    const response = await apiClient.get<Challenge>(`/api/challenges/${challengeId}`);
    return response;
  },

  // 챌린지 시작
  startChallenge: async (challengeId: number): Promise<StartChallengeResponse> => {
    const response = await apiClient.post<StartChallengeResponse>(
      `/api/challenges/${challengeId}/start`
    );
    return response;
  },

  // 챌린지 상품 목록 조회
  getChallengeInstruments: async (challengeId: number): Promise<ChallengeInstrument[]> => {
    const response = await apiClient.get<{ instruments: ChallengeInstrument[] }>(
      `/api/challenges/${challengeId}/instruments`
    );
    return response.instruments || [];
  },

  // 인기 챌린지 조회 (featured 챌린지 기반)
  getPopularChallenges: async (limit: number = 5): Promise<Challenge[]> => {
    const response = await apiClient.get<ChallengeListResponse>(
      `/api/challenges?featured=true&size=${limit}&sortBy=totalRatings&sortDirection=DESC`
    );
    return response.challenges || [];
  },

  // 추천 챌린지 조회 (사용자 레벨 기반)
  getRecommendedChallenges: async (userLevel?: ChallengeDifficulty, limit: number = 5): Promise<Challenge[]> => {
    const params: ChallengeListParams = {
      status: ChallengeStatus.ACTIVE,
      size: limit,
      sortBy: 'averageRating',
      sortDirection: 'DESC'
    };

    if (userLevel) {
      params.difficulty = userLevel;
    }

    const response = await challengeApi.getChallenges(params);
    return response.challenges || [];
  },

  // 카테고리별 챌린지 조회
  getChallengesByType: async (challengeType: ChallengeType, limit: number = 10): Promise<Challenge[]> => {
    const response = await challengeApi.getChallenges({
      challengeType,
      status: ChallengeStatus.ACTIVE,
      size: limit,
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    });
    return response.challenges || [];
  },

  // 난이도별 챌린지 조회
  getChallengesByDifficulty: async (difficulty: ChallengeDifficulty, limit: number = 10): Promise<Challenge[]> => {
    const response = await challengeApi.getChallenges({
      difficulty,
      status: ChallengeStatus.ACTIVE,
      size: limit,
      sortBy: 'currentParticipants',
      sortDirection: 'DESC'
    });
    return response.challenges || [];
  }
};

export default challengeApi;