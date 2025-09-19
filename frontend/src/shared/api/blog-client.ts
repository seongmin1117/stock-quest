import apiClient from './api-client';

// Blog domain types matching backend DTOs
export enum ArticleStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

export enum ArticleDifficulty {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export enum TagType {
  STOCK = 'STOCK',
  BOND = 'BOND',
  FUND = 'FUND',
  ETF = 'ETF',
  CRYPTO = 'CRYPTO',
  FOREX = 'FOREX',
  COMMODITY = 'COMMODITY',
  REAL_ESTATE = 'REAL_ESTATE',
  STRATEGY = 'STRATEGY',
  ANALYSIS = 'ANALYSIS',
  NEWS = 'NEWS',
  EDUCATION = 'EDUCATION',
  TOOL = 'TOOL',
  GENERAL = 'GENERAL'
}

export interface Tag {
  id: number;
  name: string;
  slug: string;
  description?: string;
  type: TagType;
  typeDisplay: string;
  colorCode?: string;
  usageCount: number;
  popular: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  colorCode?: string;
  icon?: string;
  articleCount: number;
  articleCountDisplay: string;
  sortOrder: number;
  active: boolean;
  featuredOnHome: boolean;
  seoTitle?: string;
  metaDescription?: string;
  seoKeywords?: string;
  children?: Category[];
  level: number;
  levelIndent: string;
  createdAt: string;
  updatedAt: string;
}

export interface Article {
  id: number;
  title: string;
  slug: string;
  summary: string;
  content: string;
  authorId: number;
  authorNickname: string;
  categoryId: number;
  categoryName?: string;
  status: ArticleStatus;
  featured: boolean;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  readingTimeMinutes: number;
  readingTimeDisplay: string;
  difficulty: ArticleDifficulty;
  difficultyDisplay: string;
  // SEO metadata
  seoTitle?: string;
  metaDescription?: string;
  seoKeywords?: string;
  canonicalUrl?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImageUrl?: string;
  twitterCardType?: string;
  twitterTitle?: string;
  twitterDescription?: string;
  twitterImageUrl?: string;
  indexable: boolean;
  followable: boolean;
  schemaType?: string;
  // Related data
  tags: Tag[];
  tagNames: string[];
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ArticleSearchParams {
  query?: string;
  categoryId?: number;
  tagIds?: number[];
  difficulty?: ArticleDifficulty;
  featured?: boolean;
  sortBy?: string;
  sortDirection?: string;
  limit?: number;
  offset?: number;
}

export interface ArticleSearchResponse {
  articles: Article[];
  totalCount: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  query?: string;
  categoryId?: number;
  tagIds?: number[];
  sortBy: string;
  sortDirection: string;
  hasNext: boolean;
  hasPrevious: boolean;
  paginationDisplay: string;
}

// Blog API client following existing patterns
export const blogApi = {
  // Article operations
  searchArticles: async (params: ArticleSearchParams = {}): Promise<ArticleSearchResponse> => {
    const response = await apiClient.get<ArticleSearchResponse>('/api/v1/content/articles', {
      params: {
        ...params,
        sortBy: params.sortBy || 'published_at',
        sortDirection: params.sortDirection || 'desc',
        limit: params.limit || 20,
        offset: params.offset || 0
      }
    });
    return response;
  },

  getArticleBySlug: async (slug: string): Promise<Article> => {
    const response = await apiClient.get<Article>(`/api/v1/content/articles/${slug}`);
    return response;
  },

  getFeaturedArticles: async (limit = 5): Promise<Article[]> => {
    const response = await apiClient.get<Article[]>('/api/v1/content/articles/featured', {
      params: { limit }
    });
    return response;
  },

  getRecentArticles: async (limit = 10): Promise<Article[]> => {
    const response = await apiClient.get<Article[]>('/api/v1/content/articles/recent', {
      params: { limit }
    });
    return response;
  },

  // Category operations
  getAllCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>('/api/v1/content/categories');
    return response;
  },

  getCategoryBySlug: async (slug: string): Promise<Category> => {
    const response = await apiClient.get<Category>(`/api/v1/content/categories/${slug}`);
    return response;
  },

  // Tag operations
  getPopularTags: async (limit = 30): Promise<Tag[]> => {
    const response = await apiClient.get<Tag[]>('/api/v1/content/tags/popular', {
      params: { limit }
    });
    return response;
  },

  getTagsByType: async (type: TagType): Promise<Tag[]> => {
    const response = await apiClient.get<Tag[]>(`/api/v1/content/tags/by-type/${type}`);
    return response;
  }
};

// Export utility functions for frontend components
export const blogUtils = {
  // Format difficulty display
  getDifficultyColor: (difficulty: ArticleDifficulty): string => {
    switch (difficulty) {
      case ArticleDifficulty.BEGINNER:
        return 'text-green-600 bg-green-100';
      case ArticleDifficulty.INTERMEDIATE:
        return 'text-yellow-600 bg-yellow-100';
      case ArticleDifficulty.ADVANCED:
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  },

  // Format tag type color
  getTagTypeColor: (type: TagType): string => {
    switch (type) {
      case TagType.STOCK:
        return 'bg-blue-100 text-blue-800';
      case TagType.BOND:
        return 'bg-green-100 text-green-800';
      case TagType.FUND:
        return 'bg-purple-100 text-purple-800';
      case TagType.ETF:
        return 'bg-indigo-100 text-indigo-800';
      case TagType.CRYPTO:
        return 'bg-orange-100 text-orange-800';
      case TagType.STRATEGY:
        return 'bg-pink-100 text-pink-800';
      case TagType.EDUCATION:
        return 'bg-cyan-100 text-cyan-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  },

  // Generate article URL
  getArticleUrl: (slug: string): string => `/blog/articles/${slug}`,

  // Generate category URL
  getCategoryUrl: (slug: string): string => `/blog/categories/${slug}`,

  // Generate tag URL
  getTagUrl: (slug: string): string => `/blog/tags/${slug}`,

  // Format reading time
  formatReadingTime: (minutes: number): string => {
    if (minutes < 1) return '1분 미만';
    return `${minutes}분 읽기`;
  },

  // Format article meta description
  formatMetaDescription: (article: Article): string => {
    return article.metaDescription ||
           article.summary ||
           `${article.title} - StockQuest 투자 교육`;
  },

  // Generate SEO title
  generateSEOTitle: (article: Article): string => {
    return article.seoTitle ||
           `${article.title} | StockQuest 투자 교육`;
  }
};

// Type guards for better TypeScript support
export const isPublishedArticle = (article: Article): boolean => {
  return article.status === ArticleStatus.PUBLISHED;
};

export const isFeaturedArticle = (article: Article): boolean => {
  return article.featured && isPublishedArticle(article);
};

// Search query builder helper
export const buildSearchQuery = (
  baseParams: Partial<ArticleSearchParams> = {}
): ArticleSearchParams => {
  return {
    sortBy: 'published_at',
    sortDirection: 'desc',
    limit: 20,
    offset: 0,
    ...baseParams
  };
};

export default blogApi;