/**
 * Admin Challenge API Client
 * 관리자용 챌린지 CRUD API 클라이언트
 */

import apiClient from './api-client';

// Enums
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
  COMMUNITY = 'COMMUNITY',
  STOCK_PICKING = 'STOCK_PICKING',
  PORTFOLIO_MANAGEMENT = 'PORTFOLIO_MANAGEMENT',
  OPTIONS_TRADING = 'OPTIONS_TRADING',
  SECTOR_ANALYSIS = 'SECTOR_ANALYSIS',
  TECHNICAL_ANALYSIS = 'TECHNICAL_ANALYSIS'
}

export enum ChallengeStatus {
  DRAFT = 'DRAFT',
  SCHEDULED = 'SCHEDULED',
  ACTIVE = 'ACTIVE',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  ARCHIVED = 'ARCHIVED',
  CANCELLED = 'CANCELLED'
}

// Request/Response Types
export interface CreateChallengeRequest {
  title: string;
  description: string;
  categoryId: number;
  difficulty: ChallengeDifficulty;
  challengeType: ChallengeType;
  initialBalance: number;
  durationDays: number;
  maxParticipants?: number;
  availableInstruments: string[];
  tradingRestrictions?: Record<string, any>;
  successCriteria?: Record<string, any>;
  entryRequirements?: Record<string, any>;
  learningObjectives?: string;
  marketScenarioDescription?: string;
  riskLevel: number;
  estimatedTimeMinutes?: number;
  tags?: string[];
  createdBy: number;
}

export interface CreateFromTemplateRequest {
  templateId: number;
  title: string;
  createdBy: number;
  customizations?: Record<string, any>;
}

export interface UpdateChallengeRequest extends Partial<CreateChallengeRequest> {
  challengeId?: number;
}

export interface ChallengeSearchParams {
  title?: string;
  categoryId?: number;
  templateId?: number;
  difficulty?: ChallengeDifficulty;
  challengeType?: ChallengeType;
  status?: ChallengeStatus;
  createdBy?: number;
  featured?: boolean;
  tags?: string[];
  minRiskLevel?: number;
  maxRiskLevel?: number;
  minDurationDays?: number;
  maxDurationDays?: number;
  minAverageRating?: number;
  minParticipants?: number;
  maxParticipants?: number;
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
}

// Admin Challenge Response Interfaces (based on AdminChallengeResponse)
export interface InstrumentInfo {
  id: number;
  instrumentKey: string;
  actualTicker: string;
  hiddenName: string;
  actualName: string;
  type: string;
}

export interface AnalyticsInfo {
  totalParticipants: number;
  completedParticipants: number;
  averageReturnRate: number;
  successRatePercentage: number;
  ratingAverage: number;
  ratingCount: number;
  lastCalculatedAt: string;
}

export interface Challenge {
  id: number;
  title: string;
  description: string;
  categoryId?: number;
  categoryName?: string;
  templateId?: number;
  templateName?: string;
  marketPeriodId?: number;
  marketPeriodName?: string;
  difficulty: ChallengeDifficulty;
  challengeType: ChallengeType;
  status: ChallengeStatus;
  initialBalance: number;
  estimatedDurationMinutes?: number;
  periodStart?: string;
  periodEnd?: string;
  speedFactor?: number;
  tags?: string[];
  successCriteria?: Record<string, any>;
  marketScenario?: Record<string, any>;
  learningObjectives?: string;
  maxParticipants?: number;
  currentParticipants?: number;
  startDate?: string;
  endDate?: string;
  createdBy?: number;
  createdByName?: string;
  isFeatured?: boolean;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
  instruments?: InstrumentInfo[];
  analytics?: AnalyticsInfo;

  // Legacy fields for backward compatibility
  durationDays?: number;
  availableInstruments?: string[];
  tradingRestrictions?: Record<string, any>;
  entryRequirements?: Record<string, any>;
  marketScenarioDescription?: string;
  riskLevel?: number;
  estimatedTimeMinutes?: number;
  featured?: boolean;
  averageRating?: number;
  totalRatings?: number;
}

export interface ChallengePage {
  content: Challenge[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const adminChallengeApi = {
  // 챌린지 CRUD 작업
  createChallenge: async (data: CreateChallengeRequest): Promise<Challenge> => {
    const response = await apiClient.post<Challenge>('/api/admin/challenges', data);
    return response;
  },

  createChallengeFromTemplate: async (data: CreateFromTemplateRequest): Promise<Challenge> => {
    const response = await apiClient.post<Challenge>('/api/admin/challenges/from-template', data);
    return response;
  },

  updateChallenge: async (challengeId: number, data: UpdateChallengeRequest): Promise<Challenge> => {
    const response = await apiClient.put<Challenge>(`/api/admin/challenges/${challengeId}`, data);
    return response;
  },

  // 챌린지 상태 관리
  changeStatus: async (challengeId: number, status: ChallengeStatus, modifiedBy: number): Promise<Challenge> => {
    const response = await apiClient.patch<Challenge>(
      `/api/admin/challenges/${challengeId}/status?status=${status}&modifiedBy=${modifiedBy}`
    );
    return response;
  },

  activateChallenge: async (challengeId: number, modifiedBy: number): Promise<Challenge> => {
    const response = await apiClient.post<Challenge>(
      `/api/admin/challenges/${challengeId}/activate?modifiedBy=${modifiedBy}`
    );
    return response;
  },

  completeChallenge: async (challengeId: number, modifiedBy: number): Promise<Challenge> => {
    const response = await apiClient.post<Challenge>(
      `/api/admin/challenges/${challengeId}/complete?modifiedBy=${modifiedBy}`
    );
    return response;
  },

  archiveChallenge: async (challengeId: number, modifiedBy: number): Promise<Challenge> => {
    const response = await apiClient.post<Challenge>(
      `/api/admin/challenges/${challengeId}/archive?modifiedBy=${modifiedBy}`
    );
    return response;
  },

  setFeaturedChallenge: async (challengeId: number, featured: boolean, modifiedBy: number): Promise<Challenge> => {
    const response = await apiClient.patch<Challenge>(
      `/api/admin/challenges/${challengeId}/featured?featured=${featured}&modifiedBy=${modifiedBy}`
    );
    return response;
  },

  // 챌린지 조회
  getChallenges: async (params: ChallengeSearchParams = {}): Promise<ChallengePage> => {
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

    const response = await apiClient.get<ChallengePage>(`/api/admin/challenges?${queryParams.toString()}`);
    return response;
  },

  getChallengeById: async (challengeId: number): Promise<Challenge> => {
    const response = await apiClient.get<Challenge>(`/api/admin/challenges/${challengeId}`);
    return response;
  },

  getChallengesByCategory: async (categoryId: number): Promise<Challenge[]> => {
    const response = await apiClient.get<Challenge[]>(`/api/admin/challenges/category/${categoryId}`);
    return response;
  },

  getChallengesByTemplate: async (templateId: number): Promise<Challenge[]> => {
    const response = await apiClient.get<Challenge[]>(`/api/admin/challenges/template/${templateId}`);
    return response;
  },

  getPopularChallenges: async (limit: number = 10): Promise<Challenge[]> => {
    const response = await apiClient.get<Challenge[]>(`/api/admin/challenges/popular?limit=${limit}`);
    return response;
  },

  getFeaturedChallenges: async (): Promise<Challenge[]> => {
    const response = await apiClient.get<Challenge[]>('/api/admin/challenges/featured');
    return response;
  },

  // 챌린지 복제
  cloneChallenge: async (challengeId: number, newTitle: string, createdBy: number): Promise<Challenge> => {
    const response = await apiClient.post<Challenge>(
      `/api/admin/challenges/${challengeId}/clone?newTitle=${encodeURIComponent(newTitle)}&createdBy=${createdBy}`
    );
    return response;
  },
};

export default adminChallengeApi;