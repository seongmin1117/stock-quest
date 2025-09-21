/**
 * Unified API Client Index
 * 통합 API 클라이언트 인덱스 - Feature-sliced design 지원
 */

// =============================================================================
// Core API Infrastructure
// =============================================================================

// API Client Base
export { default as apiClient } from './api-client';

// Generated API Models (Types) - Core types only
// NOTE: Avoid re-exporting types that conflict with legacy client exports

// =============================================================================
// Domain API Clients (Feature-specific)
// =============================================================================

// Challenge API (Export specific functions to avoid conflicts)
export {
  useChallengeList,
  useActiveChallenges,
  useChallengeDetail,
  useStartChallenge,
  useChallengeInstruments,
  challengeQueryKeys,
  challengeUtils,
  // Legacy enums for backward compatibility
  ChallengeStatus,
  ChallengeDifficulty,
  ChallengeType
} from './challenge-client';

// DCA (Dollar Cost Averaging) API
export * from './dca-client';

// Company & Market Data API
export * from './company-client';

// Dashboard API
export * from './dashboard-client';

// Authentication API
export * from './auth';

// Admin API
export * from './admin-challenge-client';
export * from './blog-admin-client';

// Blog Content API
export * from './blog-client';

// =============================================================================
// Feature-specific API Clients (Advanced Features)
// =============================================================================

// DCA Feature API
export * as DCAFeatureAPI from '../../features/dca/api/dca-api';

// Portfolio Optimization Feature API
export * as PortfolioOptimizationAPI from '../../features/portfolio-optimization/api/portfolio-optimization-api';

// ML Trading Feature API
export * as MLTradingAPI from '../../features/ml-trading/api/ml-trading-api';

// Risk Management Feature API
export * as RiskManagementAPI from '../../features/risk-management/api/risk-management-api';

// Backtesting Feature API
export * as BacktestingAPI from '../../features/backtesting/api/backtesting-api';

// =============================================================================
// Generated API Controllers (Direct Access)
// =============================================================================

// Authentication
export * as AuthAPI from './generated/인증/인증';

// Challenges
export * as ChallengeAPI from './generated/챌린지/챌린지';
export * as ChallengeSessionAPI from './generated/챌린지-세션/챌린지-세션';

// Leaderboard
export * as LeaderboardAPI from './generated/리더보드/리더보드';

// Community
export * as CommunityAPI from './generated/커뮤니티/커뮤니티';

// DCA Controller
export * as DCAControllerAPI from './generated/dca-controller/dca-controller';

// ML Signals
export * as MLSignalsAPI from './generated/ml-signals-controller/ml-signals-controller';

// Portfolio Optimization
export * as PortfolioOptimizationControllerAPI from './generated/portfolio-optimization-controller/portfolio-optimization-controller';

// Risk Management
export * as RiskManagementControllerAPI from './generated/risk-management-controller/risk-management-controller';

// Backtesting
export * as BacktestingControllerAPI from './generated/backtesting-controller/backtesting-controller';

// Companies
export * as CompaniesAPI from './generated/companies/companies';
export * as CompanySyncAPI from './generated/company-sync/company-sync';

// Admin
export * as AdminChallengeAPI from './generated/admin-challenge-controller/admin-challenge-controller';
export * as SessionManagementAPI from './generated/session-management/session-management';

// Dashboard
export * as DashboardAPI from './generated/dashboard-web-adapter/dashboard-web-adapter';

// Simulation
export * as SimulationAPI from './generated/simulation-controller/simulation-controller';

// =============================================================================
// API Utilities & Constants
// =============================================================================

/**
 * API Endpoints Configuration
 */
export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    LOGIN: '/api/auth/login',
    SIGNUP: '/api/auth/signup',
    REFRESH: '/api/auth/refresh',
    LOGOUT: '/api/auth/logout',
    ME: '/api/auth/me',
  },

  // Challenges
  CHALLENGES: {
    LIST: '/api/challenges',
    ACTIVE: '/api/challenges/active',
    DETAIL: (id: number) => `/api/challenges/${id}`,
    START: (id: number) => `/api/challenges/${id}/start`,
    INSTRUMENTS: (id: number) => `/api/challenges/${id}/instruments`,
    LEADERBOARD: (id: number) => `/api/challenges/${id}/leaderboard`,
  },

  // Challenge Sessions
  SESSIONS: {
    DETAIL: (id: number) => `/api/sessions/${id}`,
    PORTFOLIO: (id: number) => `/api/sessions/${id}/portfolio`,
    ORDERS: (id: number) => `/api/sessions/${id}/orders`,
    CLOSE: (id: number) => `/api/sessions/${id}/close`,
  },

  // Advanced Features
  DCA: {
    SIMULATE: '/api/v1/dca/simulate',
    TEST: '/api/v1/dca/test',
  },

  ML: {
    SIGNALS: {
      GENERATE: (symbol: string) => `/api/v1/ml/signals/generate/${symbol}`,
      ACTIVE: '/api/v1/ml/signals/active',
      BATCH: '/api/v1/ml/signals/generate/batch',
      FILTER: '/api/v1/ml/signals/filter',
    },
    PORTFOLIO: {
      OPTIMIZE: (id: number) => `/api/v1/ml/portfolio-optimization/${id}/optimize`,
      EFFICIENT_FRONTIER: (id: number) => `/api/v1/ml/portfolio-optimization/${id}/efficient-frontier`,
      BACKTEST: (id: number) => `/api/v1/ml/portfolio-optimization/${id}/backtest`,
      SUGGESTIONS: (id: number) => `/api/v1/ml/portfolio-optimization/${id}/rebalancing-suggestions`,
      HISTORY: (id: number) => `/api/v1/ml/portfolio-optimization/${id}/history`,
    },
  },

  RISK: {
    DASHBOARD: '/api/v1/risk/dashboard',
    ALERTS: '/api/v1/risk/alerts',
    VAR: (portfolioId: string) => `/api/v1/risk/portfolios/${portfolioId}/var`,
    STRESS_TEST: (portfolioId: string) => `/api/v1/risk/portfolios/${portfolioId}/stress-test`,
  },

  BACKTESTING: {
    RUN: '/api/v1/backtesting/run',
    COMPARE: '/api/v1/backtesting/compare',
    RESULTS: (id: string) => `/api/v1/backtesting/results/${id}`,
  },

  // Companies
  COMPANIES: {
    SEARCH: '/api/v1/companies/search',
    DETAIL: (symbol: string) => `/api/v1/companies/${symbol}`,
    TOP: '/api/v1/companies/top',
    CATEGORIES: '/api/v1/companies/categories',
    SYNC: {
      SINGLE: (symbol: string) => `/api/v1/companies/sync/${symbol}`,
      ALL: '/api/v1/companies/sync/all',
      SCHEDULED: '/api/v1/companies/sync/scheduled',
    },
  },

  // Dashboard
  DASHBOARD: '/api/dashboard',

  // Admin
  ADMIN: {
    CHALLENGES: '/api/admin/challenges',
    CHALLENGE_DETAIL: (id: number) => `/api/admin/challenges/${id}`,
    SESSIONS: {
      FORCE_END: (userId: number, challengeId: number) =>
        `/api/admin/sessions/users/${userId}/challenges/${challengeId}/force-end`,
      FORCE_END_ALL: (userId: number) =>
        `/api/admin/sessions/users/${userId}/force-end-all`,
      CLEANUP: '/api/admin/sessions/cleanup/stale-ready',
    },
  },
} as const;

/**
 * Query Key Factories for React Query
 */
export const queryKeys = {
  // Authentication
  auth: {
    me: () => ['auth', 'me'] as const,
  },

  // Challenges
  challenges: {
    all: () => ['challenges'] as const,
    lists: () => [...queryKeys.challenges.all(), 'list'] as const,
    list: (params?: any) => [...queryKeys.challenges.lists(), params] as const,
    active: () => [...queryKeys.challenges.all(), 'active'] as const,
    details: () => [...queryKeys.challenges.all(), 'detail'] as const,
    detail: (id: number) => [...queryKeys.challenges.details(), id] as const,
    instruments: (id: number) => [...queryKeys.challenges.all(), 'instruments', id] as const,
    leaderboard: (id: number) => [...queryKeys.challenges.all(), 'leaderboard', id] as const,
  },

  // Sessions
  sessions: {
    all: () => ['sessions'] as const,
    detail: (id: number) => [...queryKeys.sessions.all(), 'detail', id] as const,
    portfolio: (id: number) => [...queryKeys.sessions.all(), 'portfolio', id] as const,
    orders: (id: number) => [...queryKeys.sessions.all(), 'orders', id] as const,
  },

  // Advanced Features
  dca: {
    all: () => ['dca'] as const,
    simulation: (params: any) => [...queryKeys.dca.all(), 'simulation', params] as const,
    test: () => [...queryKeys.dca.all(), 'test'] as const,
  },

  ml: {
    signals: {
      all: () => ['ml', 'signals'] as const,
      generate: (symbol: string) => [...queryKeys.ml.signals.all(), 'generate', symbol] as const,
      active: (params?: any) => [...queryKeys.ml.signals.all(), 'active', params] as const,
    },
    portfolio: {
      all: () => ['ml', 'portfolio'] as const,
      optimize: (id: number, params: any) => [...queryKeys.ml.portfolio.all(), 'optimize', id, params] as const,
      suggestions: (id: number) => [...queryKeys.ml.portfolio.all(), 'suggestions', id] as const,
      history: (id: number, params?: any) => [...queryKeys.ml.portfolio.all(), 'history', id, params] as const,
    },
  },

  risk: {
    all: () => ['risk'] as const,
    dashboard: (params?: any) => [...queryKeys.risk.all(), 'dashboard', params] as const,
    alerts: (params?: any) => [...queryKeys.risk.all(), 'alerts', params] as const,
    var: (portfolioId: string, params: any) => [...queryKeys.risk.all(), 'var', portfolioId, params] as const,
  },

  backtesting: {
    all: () => ['backtesting'] as const,
    run: (params: any) => [...queryKeys.backtesting.all(), 'run', params] as const,
    results: (id: string) => [...queryKeys.backtesting.all(), 'results', id] as const,
  },

  // Companies
  companies: {
    all: () => ['companies'] as const,
    search: (params?: any) => [...queryKeys.companies.all(), 'search', params] as const,
    detail: (symbol: string) => [...queryKeys.companies.all(), 'detail', symbol] as const,
    top: (params?: any) => [...queryKeys.companies.all(), 'top', params] as const,
    categories: () => [...queryKeys.companies.all(), 'categories'] as const,
  },

  // Dashboard
  dashboard: {
    all: () => ['dashboard'] as const,
  },
} as const;

/**
 * API Response Types for better type safety
 */
export interface APIResponse<T = any> {
  data: T;
  success: boolean;
  message?: string;
  timestamp?: string;
}

export interface APIError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
  traceId?: string;
}

export interface PaginatedResponse<T = any> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Common API Configuration
 */
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080',
  TIMEOUT: 30000, // 30 seconds
  RETRY_ATTEMPTS: 3,
  CACHE_TIME: 5 * 60 * 1000, // 5 minutes
  STALE_TIME: 2 * 60 * 1000, // 2 minutes
} as const;