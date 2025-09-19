/**
 * Dashboard API Client
 * 사용자 대시보드 API 클라이언트
 */

import apiClient from './api-client';

// Types
export interface RecentSession {
  id: number;
  challengeTitle: string;
  status: SessionStatus;
  progress: number;
  currentBalance: number;
  returnRate: number;
  startedAt: string;
  completedAt?: string;
  challengeId?: number;
}

export enum SessionStatus {
  READY = 'READY',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  ENDED = 'ENDED'
}

export interface UserStats {
  totalSessions: number;
  activeSessions: number;
  completedSessions: number;
  averageReturn: number;
  bestReturn: number;
  worstReturn: number;
  totalReturn: number;
  winRate: number;
}

export interface DashboardData {
  userStats: UserStats;
  recentSessions: RecentSession[];
}

export interface DashboardSummary {
  totalActiveSessions: number;
  totalCompletedSessions: number;
  averageReturnRate: number;
  bestSession: {
    id: number;
    challengeTitle: string;
    returnRate: number;
  } | null;
}

// Response types for future API integration
export interface DashboardResponse {
  userStats: UserStats;
  recentSessions: RecentSession[];
  summary: DashboardSummary;
}

export interface RecentSessionsResponse {
  sessions: RecentSession[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface UserStatsResponse {
  stats: UserStats;
  updatedAt: string;
}

// API Client
export const dashboardApi = {
  // 대시보드 전체 데이터 조회
  getDashboardData: async (): Promise<DashboardData> => {
    const response = await apiClient.get<DashboardData>('/api/dashboard');
    return response.data || response;
  },

  // 최근 세션 목록 조회
  getRecentSessions: async (limit: number = 5): Promise<RecentSession[]> => {
    // TODO: 실제 API 엔드포인트 구현 시 교체
    // const response = await apiClient.get<RecentSessionsResponse>(
    //   `/api/dashboard/sessions?limit=${limit}`
    // );
    // return response.sessions;

    const data = await dashboardApi.getDashboardData();
    return data.recentSessions.slice(0, limit);
  },

  // 사용자 통계 조회
  getUserStats: async (): Promise<UserStats> => {
    // TODO: 실제 API 엔드포인트 구현 시 교체
    // const response = await apiClient.get<UserStatsResponse>('/api/dashboard/stats');
    // return response.stats;

    const data = await dashboardApi.getDashboardData();
    return data.userStats;
  },

  // 활성 세션 조회
  getActiveSessions: async (): Promise<RecentSession[]> => {
    // TODO: 실제 API 엔드포인트 구현 시 교체
    // const response = await apiClient.get<RecentSessionsResponse>(
    //   '/api/dashboard/sessions?status=ACTIVE'
    // );
    // return response.sessions;

    const data = await dashboardApi.getDashboardData();
    return data.recentSessions.filter(session => session.status === SessionStatus.ACTIVE);
  },

  // 완료된 세션 조회 (페이징 지원)
  getCompletedSessions: async (page: number = 0, size: number = 10): Promise<RecentSessionsResponse> => {
    // TODO: 실제 API 엔드포인트 구현 시 교체
    // const response = await apiClient.get<RecentSessionsResponse>(
    //   `/api/dashboard/sessions?status=COMPLETED&page=${page}&size=${size}`
    // );
    // return response;

    const data = await dashboardApi.getDashboardData();
    const completedSessions = data.recentSessions.filter(
      session => session.status === SessionStatus.COMPLETED
    );

    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedSessions = completedSessions.slice(startIndex, endIndex);

    return {
      sessions: paginatedSessions,
      totalElements: completedSessions.length,
      totalPages: Math.ceil(completedSessions.length / size),
      size: size,
      number: page
    };
  },

  // 대시보드 요약 정보 조회
  getDashboardSummary: async (): Promise<DashboardSummary> => {
    // TODO: 실제 API 엔드포인트 구현 시 교체
    // const response = await apiClient.get<{ summary: DashboardSummary }>('/api/dashboard/summary');
    // return response.summary;

    const data = await dashboardApi.getDashboardData();
    const { recentSessions } = data;

    const activeSessions = recentSessions.filter(s => s.status === SessionStatus.ACTIVE);
    const completedSessions = recentSessions.filter(s => s.status === SessionStatus.COMPLETED);

    const totalReturn = completedSessions.reduce((sum, session) => sum + session.returnRate, 0);
    const averageReturn = completedSessions.length > 0 ? totalReturn / completedSessions.length : 0;

    const bestSession = completedSessions.reduce((best, current) => {
      return !best || current.returnRate > best.returnRate ? current : best;
    }, null as RecentSession | null);

    return {
      totalActiveSessions: activeSessions.length,
      totalCompletedSessions: completedSessions.length,
      averageReturnRate: averageReturn,
      bestSession: bestSession ? {
        id: bestSession.id,
        challengeTitle: bestSession.challengeTitle,
        returnRate: bestSession.returnRate
      } : null
    };
  },

  // 세션 상태별 조회 헬퍼
  getSessionsByStatus: async (status: SessionStatus): Promise<RecentSession[]> => {
    // TODO: 실제 API 엔드포인트 구현 시 교체
    // const response = await apiClient.get<RecentSessionsResponse>(
    //   `/api/dashboard/sessions?status=${status}`
    // );
    // return response.sessions;

    const data = await dashboardApi.getDashboardData();
    return data.recentSessions.filter(session => session.status === status);
  }
};

// Utility functions
export const dashboardUtils = {
  // 세션 상태를 한국어로 변환
  getStatusText: (status: SessionStatus): string => {
    switch (status) {
      case SessionStatus.READY:
        return '준비';
      case SessionStatus.ACTIVE:
        return '진행중';
      case SessionStatus.COMPLETED:
        return '완료';
      case SessionStatus.CANCELLED:
        return '취소';
      case SessionStatus.ENDED:
        return '종료';
      default:
        return status;
    }
  },

  // 세션 상태 색상 반환
  getStatusColor: (status: SessionStatus): 'primary' | 'success' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case SessionStatus.READY:
        return 'warning';
      case SessionStatus.ACTIVE:
        return 'primary';
      case SessionStatus.COMPLETED:
        return 'success';
      case SessionStatus.CANCELLED:
        return 'error';
      case SessionStatus.ENDED:
        return 'default';
      default:
        return 'default';
    }
  },

  // 수익률 포맷팅
  formatReturnRate: (rate: number) => {
    const isPositive = rate > 0;
    return {
      value: `${isPositive ? '+' : ''}${rate.toFixed(1)}%`,
      color: isPositive ? '#4CAF50' : rate < 0 ? '#F44336' : '#78828A',
      isPositive
    };
  },

  // 진행률 계산 (활성 세션용)
  calculateProgress: (session: RecentSession): number => {
    if (session.status === SessionStatus.COMPLETED) return 100;
    if (session.status === SessionStatus.ACTIVE) {
      // TODO: 실제 진행률 계산 로직 구현 (예: 남은 시간 기반)
      return session.progress || 0;
    }
    return 0;
  }
};

export default dashboardApi;