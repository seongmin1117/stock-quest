/**
 * User Challenge API Client (Latest OpenAPI Integration)
 * 사용자용 챌린지 API 클라이언트 - 최신 OpenAPI 스펙 기반
 */

// Re-export from generated API
export {
  // Challenge API
  getChallengeList,
  getGetChallengeListQueryOptions,
  useGetChallengeList,
  getChallengeDetail,
  getGetChallengeDetailQueryOptions,
  useGetChallengeDetail,
  startChallenge,
  getStartChallengeMutationOptions,
  useStartChallenge as useGeneratedStartChallenge,
  getChallengeInstruments,
  getGetChallengeInstrumentsQueryOptions,
  useGetChallengeInstruments,
} from './generated/챌린지/챌린지';

// Challenge Session API
export {
  getSessionDetail,
  getGetSessionDetailQueryOptions,
  useGetSessionDetail,
  getOrders,
  getGetOrdersQueryOptions,
  useGetOrders,
  placeOrder,
  getPlaceOrderMutationOptions,
  usePlaceOrder,
  closeChallenge,
  getCloseChallengeMutationOptions,
  useCloseChallenge,
} from './generated/챌린지-세션/챌린지-세션';

// Leaderboard API
export {
  getLeaderboard as getChallengeLeaderboard,
  getGetLeaderboardQueryOptions as getGetChallengeLeaderboardQueryOptions,
  useGetLeaderboard as useGetChallengeLeaderboard,
  calculateLeaderboard,
  getCalculateLeaderboardMutationOptions,
  useCalculateLeaderboard,
} from './generated/리더보드/리더보드';

// Community API
export {
  getPostList,
  getGetPostListQueryOptions,
  useGetPostList,
  createPost,
  getCreatePostMutationOptions,
  useCreatePost,
  getCommentList,
  getGetCommentListQueryOptions,
  useGetCommentList,
  createComment,
  getCreateCommentMutationOptions,
  useCreateComment,
} from './generated/커뮤니티/커뮤니티';

// Re-export types from generated models
export type {
  ChallengeListResponse,
  ChallengeDetailResponse,
  ChallengeItem,
  ChallengeItemDifficultyLevel,
  StartChallengeResponse,
  ChallengeInstrumentsResponse,
  SessionDetailResponse,
  OrdersResponse,
  PlaceOrderRequest,
  PlaceOrderResponse,
  CloseChallengeResponse,
  LeaderboardResponse,
  PostResponse,
  CreatePostRequest,
  CommentResponse,
  CreateCommentRequest,
} from './generated/model';

// Legacy enums for backward compatibility
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
  COMMUNITY = 'COMMUNITY',
  STOCK_PICKING = 'STOCK_PICKING',
  PORTFOLIO_MANAGEMENT = 'PORTFOLIO_MANAGEMENT',
  OPTIONS_TRADING = 'OPTIONS_TRADING',
  SECTOR_ANALYSIS = 'SECTOR_ANALYSIS',
  TECHNICAL_ANALYSIS = 'TECHNICAL_ANALYSIS'
}

// Enhanced hooks with better error handling and caching
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getChallengeList as apiGetChallengeList,
  getChallengeDetail as apiGetChallengeDetail,
  startChallenge as apiStartChallenge,
  getChallengeInstruments as apiGetChallengeInstruments,
} from './generated/챌린지/챌린지';

/**
 * Enhanced Challenge List Hook with smart caching
 */
export const useChallengeList = (page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: challengeQueryKeys.list(page, size),
    queryFn: () => apiGetChallengeList({ page, size }),
    staleTime: 5 * 60 * 1000, // 5분간 캐시
    gcTime: 10 * 60 * 1000, // 10분간 보관 (React Query v5+)
  });
};

/**
 * Enhanced Active Challenges Hook
 */
export const useActiveChallenges = () => {
  return useQuery({
    queryKey: challengeQueryKeys.active(),
    queryFn: () => apiGetChallengeList({ page: 0, size: 20 }), // Active challenges with higher limit
    staleTime: 2 * 60 * 1000, // 2분간 캐시 (더 자주 갱신)
    refetchInterval: 5 * 60 * 1000, // 5분마다 자동 갱신
  });
};

/**
 * Enhanced Challenge Detail Hook
 */
export const useChallengeDetail = (challengeId: number) => {
  return useQuery({
    queryKey: challengeQueryKeys.detail(challengeId),
    queryFn: () => apiGetChallengeDetail(challengeId),
    enabled: !!challengeId && challengeId > 0,
    staleTime: 3 * 60 * 1000, // 3분간 캐시
  });
};

/**
 * Enhanced Start Challenge Hook with optimistic updates
 */
export const useStartChallenge = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ challengeId, forceRestart }: { challengeId: number; forceRestart?: boolean }) =>
      apiStartChallenge(challengeId, forceRestart ? { forceRestart } : undefined),
    onSuccess: (data, variables) => {
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: challengeQueryKeys.detail(variables.challengeId) });
      queryClient.invalidateQueries({ queryKey: challengeQueryKeys.list() });
      console.log('챌린지 시작 성공:', data);
    },
    onError: (error) => {
      console.error('챌린지 시작 실패:', error);
    },
  });
};

/**
 * Enhanced Challenge Instruments Hook
 */
export const useChallengeInstruments = (challengeId: number) => {
  return useQuery({
    queryKey: challengeQueryKeys.instruments(challengeId),
    queryFn: () => apiGetChallengeInstruments(challengeId),
    enabled: !!challengeId && challengeId > 0,
    staleTime: 10 * 60 * 1000, // 10분간 캐시 (상품 정보는 자주 변하지 않음)
  });
};

/**
 * Challenge Query Keys for better cache management
 */
export const challengeQueryKeys = {
  all: ['challenges'] as const,
  lists: () => [...challengeQueryKeys.all, 'list'] as const,
  list: (page?: number, size?: number) => [...challengeQueryKeys.lists(), { page, size }] as const,
  active: () => [...challengeQueryKeys.all, 'active'] as const,
  details: () => [...challengeQueryKeys.all, 'detail'] as const,
  detail: (id: number) => [...challengeQueryKeys.details(), id] as const,
  instruments: (challengeId: number) => [...challengeQueryKeys.all, 'instruments', challengeId] as const,
} as const;

/**
 * Utility functions for backward compatibility
 */
export const challengeUtils = {
  /**
   * Legacy challenge list fetcher (for compatibility)
   */
  getChallenges: async (params: { page?: number; size?: number } = {}) => {
    const { page = 0, size = 10 } = params;
    return apiGetChallengeList({ page, size });
  },

  /**
   * Legacy active challenges fetcher
   */
  getActiveChallenges: async () => {
    return apiGetChallengeList({ page: 0, size: 20 });
  },

  /**
   * Legacy challenge detail fetcher
   */
  getChallengeById: async (challengeId: number) => {
    return apiGetChallengeDetail(challengeId);
  },

  /**
   * Legacy challenge start
   */
  startChallenge: async (challengeId: number, forceRestart?: boolean) => {
    return apiStartChallenge(challengeId, forceRestart ? { forceRestart } : undefined);
  },

  /**
   * Legacy challenge instruments fetcher
   */
  getChallengeInstruments: async (challengeId: number) => {
    return apiGetChallengeInstruments(challengeId);
  },
};

// Default export for backward compatibility
export default challengeUtils;