import apiClient from './api-client';

export interface Company {
  id: number;
  symbol: string;
  nameKr: string;
  nameEn: string;
  sector: string;
  marketCap?: number;
  marketCapDisplay?: string;
  logoPath?: string;
  descriptionKr?: string;
  descriptionEn?: string;
  exchange: string;
  currency: string;
  isActive: boolean;
  popularityScore: number;
  categories: string[];
}

export interface PopularCompany {
  id: number;
  symbol: string;
  nameKr: string;
  nameEn: string;
  marketCap?: number;
  marketCapDisplay: string;
  sector: string;
  logoPath?: string;
  popularityScore: number;
  categories: string[];
}

export interface CompanyCategory {
  id: number;
  categoryId: string;
  nameKr: string;
  nameEn: string;
  descriptionKr?: string;
  descriptionEn?: string;
  sortOrder: number;
  isActive: boolean;
  companyCount: number;
}

export interface CompanySearchResponse {
  companies: Company[];
  totalCount: number;
  limit: number;
  offset: number;
  hasMore: boolean;
  currentPage: number;
  totalPages: number;
  searchQuery?: string;
  appliedCategories?: string[];
}

export interface CompanyDetail {
  id: number;
  symbol: string;
  nameKr: string;
  nameEn: string;
  sector: string;
  marketCap?: number;
  marketCapDisplay?: string;
  logoPath?: string;
  descriptionKr?: string;
  descriptionEn?: string;
  exchange: string;
  currency: string;
  isActive: boolean;
  popularityScore: number;
  categories: string[];
}

/**
 * 회사 정보 API 클라이언트
 */
export const companyClient = {
  /**
   * 회사 검색 (자동완성용)
   */
  async search(params: {
    q?: string;
    categories?: string[];
    sector?: string;
    minPopularity?: number;
    limit?: number;
    offset?: number;
  }): Promise<CompanySearchResponse> {
    console.log('🌐 [company-client] Searching companies with params:', params);
    const response = await apiClient.get('/api/v1/companies/search', { params });

    // API client가 이미 .data를 추출했으므로 response 자체가 데이터
    const data = response.data || response;
    console.log('✅ [company-client] Search response:', data);
    return data;
  },

  /**
   * 인기 회사 목록 조회 (Top companies by popularity)
   */
  async getPopular(limit: number = 10): Promise<PopularCompany[]> {
    console.log('🌐 [company-client] Getting popular companies with limit:', limit);
    const response = await apiClient.get('/api/v1/companies/top', {
      params: { limit }
    });

    // API client가 이미 .data를 추출했으므로 response 자체가 데이터
    const data = response.data || response;
    console.log('✅ [company-client] Popular companies response:', Array.isArray(data) ? data.length : 0, 'companies');

    // Convert Company[] to PopularCompany[] for backward compatibility
    if (!Array.isArray(data)) {
      console.warn('⚠️ [company-client] Invalid popular companies response data:', data);
      return [];
    }
    return data.map((company: Company): PopularCompany => ({
      id: company.id,
      symbol: company.symbol,
      nameKr: company.nameKr,
      nameEn: company.nameEn,
      marketCap: company.marketCap,
      marketCapDisplay: company.marketCapDisplay || `${company.marketCap?.toLocaleString()}원`,
      sector: company.sector,
      logoPath: company.logoPath,
      popularityScore: company.popularityScore,
      categories: company.categories
    }));
  },

  /**
   * 카테고리 목록 조회
   */
  async getCategories(): Promise<CompanyCategory[]> {
    console.log('🌐 [company-client] Getting categories');
    const response = await apiClient.get('/api/v1/companies/categories');

    // API client가 이미 .data를 추출했으므로 response 자체가 데이터
    const data = response.data || response;
    console.log('✅ [company-client] Categories response:', Array.isArray(data) ? data.length : 0, 'categories');

    if (!Array.isArray(data)) {
      console.warn('⚠️ [company-client] Invalid categories response data:', data);
      return [];
    }
    return data;
  },

  /**
   * 카테고리별 회사 목록 조회
   */
  async getByCategory(categoryId: string, limit: number = 50): Promise<Company[]> {
    const response = await apiClient.get(`/api/v1/companies/category/${categoryId}`);
    return response.data;
  },

  /**
   * 회사 상세 정보 조회
   */
  async getBySymbol(symbol: string): Promise<CompanyDetail> {
    const response = await apiClient.get(`/api/v1/companies/${symbol}`);
    return response.data;
  },

  /**
   * 한국 시장 회사 목록 조회 (Search with Korean market filter)
   */
  async getKoreanMarket(limit: number = 100): Promise<CompanySearchResponse> {
    const response = await apiClient.get('/api/v1/companies/search', {
      params: {
        limit,
        q: '', // Empty query to get all companies
        sort: 'popularityScore',
        order: 'desc'
      }
    });
    return response.data;
  },

  /**
   * 섹터별 회사 목록 조회 (Search by sector)
   */
  async getBySector(sector: string, limit: number = 50): Promise<CompanySearchResponse> {
    const response = await apiClient.get('/api/v1/companies/search', {
      params: {
        sector,
        limit,
        sort: 'popularityScore',
        order: 'desc'
      }
    });
    return response.data;
  }
};