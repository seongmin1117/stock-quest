import { apiClient } from './api-client';

export interface Company {
  symbol: string;
  nameKr: string;
  nameEn: string;
  sector: string;
  logoPath?: string;
  marketCapDisplay?: string;
}

export interface PopularCompany {
  symbol: string;
  nameKr: string;
  nameEn: string;
  marketCap: string;
  sector: string;
  logoPath?: string;
  popularityScore: number;
}

export interface CompanyCategory {
  id: string;
  name: string;
  nameEn: string;
  description?: string;
  count: number;
}

export interface CompanySearchResponse {
  companies: Company[];
}

export interface CompanyDetail {
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
  popularityScore: number;
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
    category?: string;
    sector?: string;
    limit?: number;
  }): Promise<CompanySearchResponse> {
    const response = await apiClient.get('/companies/search', { params });
    return response.data;
  },

  /**
   * 인기 회사 목록 조회
   */
  async getPopular(limit: number = 10): Promise<PopularCompany[]> {
    const response = await apiClient.get('/companies/popular', {
      params: { limit }
    });
    return response.data;
  },

  /**
   * 카테고리 목록 조회
   */
  async getCategories(): Promise<CompanyCategory[]> {
    const response = await apiClient.get('/companies/categories');
    return response.data;
  },

  /**
   * 카테고리별 회사 목록 조회
   */
  async getByCategory(categoryId: string, limit: number = 50): Promise<CompanySearchResponse> {
    const response = await apiClient.get(`/companies/category/${categoryId}`, {
      params: { limit }
    });
    return response.data;
  },

  /**
   * 회사 상세 정보 조회
   */
  async getBySymbol(symbol: string): Promise<CompanyDetail> {
    const response = await apiClient.get(`/companies/${symbol}`);
    return response.data;
  },

  /**
   * 한국 시장 회사 목록 조회
   */
  async getKoreanMarket(limit: number = 100): Promise<CompanySearchResponse> {
    const response = await apiClient.get('/companies/korean-market', {
      params: { limit }
    });
    return response.data;
  },

  /**
   * 섹터별 회사 목록 조회
   */
  async getBySector(sector: string, limit: number = 50): Promise<CompanySearchResponse> {
    const response = await apiClient.get(`/companies/sector/${sector}`, {
      params: { limit }
    });
    return response.data;
  }
};