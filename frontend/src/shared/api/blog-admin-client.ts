import apiClient from './api-client';
import { Article, Category, Tag, ArticleStatus, ArticleDifficulty, TagType } from './blog-client';

// Admin-specific DTOs matching backend Command patterns
export interface CreateArticleRequest {
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  tagIds?: number[];
  status?: ArticleStatus;
  featured?: boolean;
  difficulty?: ArticleDifficulty;
  // SEO fields
  seoTitle?: string;
  metaDescription?: string;
  seoKeywords?: string;
  canonicalUrl?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImageUrl?: string;
  twitterTitle?: string;
  twitterDescription?: string;
  twitterImageUrl?: string;
  indexable?: boolean;
  followable?: boolean;
  schemaType?: string;
}

export interface UpdateArticleRequest {
  title?: string;
  content?: string;
  summary?: string;
  categoryId?: number;
  tagIds?: number[];
  status?: ArticleStatus;
  featured?: boolean;
  difficulty?: ArticleDifficulty;
  // SEO fields
  seoTitle?: string;
  metaDescription?: string;
  seoKeywords?: string;
  canonicalUrl?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImageUrl?: string;
  twitterTitle?: string;
  twitterDescription?: string;
  twitterImageUrl?: string;
  indexable?: boolean;
  followable?: boolean;
  schemaType?: string;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  slug?: string;
  showOnHomepage?: boolean;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  slug?: string;
  showOnHomepage?: boolean;
}

export interface CreateTagRequest {
  name: string;
}

export interface UpdateTagRequest {
  name?: string;
}

// Analytics response types
export interface BlogAnalytics {
  totalArticles: number;
  publishedArticles: number;
  draftArticles: number;
  featuredArticles: number;
  totalCategories: number;
  totalTags: number;
  totalViews: number;
  totalLikes: number;
  totalComments: number;
  popularArticles: Array<{
    id: number;
    title: string;
    viewCount: number;
    likeCount: number;
  }>;
  recentActivity: Array<{
    id: number;
    title: string;
    action: string;
    timestamp: string;
  }>;
  categoryDistribution: Array<{
    categoryName: string;
    articleCount: number;
  }>;
  tagUsage: Array<{
    tagName: string;
    usageCount: number;
  }>;
}

// Blog Admin API client for CRUD operations
export const blogAdminApi = {
  // Article Admin Operations
  createArticle: async (request: CreateArticleRequest): Promise<Article> => {
    const response = await apiClient.post<Article>('/api/v1/admin/content/articles', request);
    return response;
  },

  updateArticle: async (id: number, request: UpdateArticleRequest): Promise<Article> => {
    const response = await apiClient.put<Article>(`/api/v1/admin/content/articles/${id}`, request);
    return response;
  },

  deleteArticle: async (id: number): Promise<void> => {
    await apiClient.delete(`/api/v1/admin/content/articles/${id}`);
  },

  getAllArticles: async (): Promise<Article[]> => {
    const response = await apiClient.get<Article[]>('/api/v1/admin/content/articles');
    return response;
  },

  getArticleById: async (id: number): Promise<Article> => {
    const response = await apiClient.get<Article>(`/api/v1/admin/content/articles/${id}`);
    return response;
  },

  publishArticle: async (id: number): Promise<Article> => {
    const response = await apiClient.patch<Article>(`/api/v1/admin/content/articles/${id}/publish`);
    return response;
  },

  unpublishArticle: async (id: number): Promise<Article> => {
    const response = await apiClient.patch<Article>(`/api/v1/admin/content/articles/${id}/unpublish`);
    return response;
  },

  // Category Admin Operations
  createCategory: async (request: CreateCategoryRequest): Promise<Category> => {
    const response = await apiClient.post<Category>('/api/v1/admin/content/categories', request);
    return response;
  },

  updateCategory: async (id: number, request: UpdateCategoryRequest): Promise<Category> => {
    const response = await apiClient.put<Category>(`/api/v1/admin/content/categories/${id}`, request);
    return response;
  },

  deleteCategory: async (id: number): Promise<void> => {
    await apiClient.delete(`/api/v1/admin/content/categories/${id}`);
  },

  getAllCategoriesAdmin: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>('/api/v1/admin/content/categories');
    return response;
  },

  getCategoryByIdAdmin: async (id: number): Promise<Category> => {
    const response = await apiClient.get<Category>(`/api/v1/admin/content/categories/${id}`);
    return response;
  },

  // Tag Admin Operations
  createTag: async (request: CreateTagRequest): Promise<Tag> => {
    const response = await apiClient.post<Tag>('/api/v1/admin/content/tags', request);
    return response;
  },

  updateTag: async (id: number, request: UpdateTagRequest): Promise<Tag> => {
    const response = await apiClient.put<Tag>(`/api/v1/admin/content/tags/${id}`, request);
    return response;
  },

  deleteTag: async (id: number): Promise<void> => {
    await apiClient.delete(`/api/v1/admin/content/tags/${id}`);
  },

  getAllTagsAdmin: async (): Promise<Tag[]> => {
    const response = await apiClient.get<Tag[]>('/api/v1/admin/content/tags');
    return response;
  },

  getTagByIdAdmin: async (id: number): Promise<Tag> => {
    const response = await apiClient.get<Tag>(`/api/v1/admin/content/tags/${id}`);
    return response;
  },

  // Analytics Operations
  getBlogAnalytics: async (): Promise<BlogAnalytics> => {
    const response = await apiClient.get<BlogAnalytics>('/api/v1/admin/content/analytics');
    return response;
  }
};

// Utility functions for admin operations
export const blogAdminUtils = {
  // Validate article data before submission
  validateArticle: (article: CreateArticleRequest | UpdateArticleRequest): string[] => {
    const errors: string[] = [];

    if ('title' in article && (!article.title || article.title.trim().length === 0)) {
      errors.push('제목은 필수입니다.');
    }

    if ('content' in article && (!article.content || article.content.trim().length === 0)) {
      errors.push('내용은 필수입니다.');
    }

    if ('categoryId' in article && (!article.categoryId || article.categoryId <= 0)) {
      errors.push('카테고리를 선택해주세요.');
    }

    if ('title' in article && article.title && article.title.length > 200) {
      errors.push('제목은 200자 이하로 입력해주세요.');
    }

    if ('summary' in article && article.summary && article.summary.length > 500) {
      errors.push('요약은 500자 이하로 입력해주세요.');
    }

    return errors;
  },

  // Validate category data
  validateCategory: (category: CreateCategoryRequest | UpdateCategoryRequest): string[] => {
    const errors: string[] = [];

    if ('name' in category && (!category.name || category.name.trim().length === 0)) {
      errors.push('카테고리 이름은 필수입니다.');
    }

    if ('name' in category && category.name && category.name.length > 100) {
      errors.push('카테고리 이름은 100자 이하로 입력해주세요.');
    }

    if ('description' in category && category.description && category.description.length > 500) {
      errors.push('카테고리 설명은 500자 이하로 입력해주세요.');
    }

    return errors;
  },

  // Validate tag data
  validateTag: (tag: CreateTagRequest | UpdateTagRequest): string[] => {
    const errors: string[] = [];

    if ('name' in tag && (!tag.name || tag.name.trim().length === 0)) {
      errors.push('태그 이름은 필수입니다.');
    }

    if ('name' in tag && tag.name && tag.name.length > 50) {
      errors.push('태그 이름은 50자 이하로 입력해주세요.');
    }

    return errors;
  },

  // Generate slug from title
  generateSlug: (title: string): string => {
    return title
      .toLowerCase()
      .replace(/[^\w\s가-힣]/g, '') // Remove special characters except Korean
      .replace(/\s+/g, '-') // Replace spaces with hyphens
      .replace(/^-+|-+$/g, ''); // Remove leading/trailing hyphens
  },

  // Format status display
  getStatusDisplay: (status: ArticleStatus): { label: string; color: string } => {
    switch (status) {
      case ArticleStatus.PUBLISHED:
        return { label: '게시됨', color: 'success' };
      case ArticleStatus.DRAFT:
        return { label: '초안', color: 'warning' };
      case ArticleStatus.ARCHIVED:
        return { label: '보관됨', color: 'secondary' };
      default:
        return { label: '알 수 없음', color: 'default' };
    }
  },

  // Format difficulty display
  getDifficultyDisplay: (difficulty: ArticleDifficulty): { label: string; color: string } => {
    switch (difficulty) {
      case ArticleDifficulty.BEGINNER:
        return { label: '초급', color: 'success' };
      case ArticleDifficulty.INTERMEDIATE:
        return { label: '중급', color: 'warning' };
      case ArticleDifficulty.ADVANCED:
        return { label: '고급', color: 'error' };
      default:
        return { label: '설정 안됨', color: 'default' };
    }
  },

  // Calculate reading time from content
  calculateReadingTime: (content: string): number => {
    const wordsPerMinute = 200; // Korean reading speed
    const wordCount = content.replace(/<[^>]*>/g, '').length; // Remove HTML tags
    return Math.max(1, Math.ceil(wordCount / wordsPerMinute));
  },

  // Format article summary for display
  formatSummary: (content: string, maxLength: number = 150): string => {
    const plainText = content.replace(/<[^>]*>/g, ''); // Remove HTML tags
    if (plainText.length <= maxLength) {
      return plainText;
    }
    return plainText.substring(0, maxLength).replace(/\s+\S*$/, '') + '...';
  },

  // Check if user can edit article (based on role and ownership)
  canEditArticle: (article: Article, currentUserId: number, isAdmin: boolean): boolean => {
    return isAdmin || article.authorId === currentUserId;
  },

  // Check if user can delete article
  canDeleteArticle: (article: Article, currentUserId: number, isAdmin: boolean): boolean => {
    return isAdmin || (article.authorId === currentUserId && article.status === ArticleStatus.DRAFT);
  }
};

// Error handling utilities
export class BlogAdminError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public fieldErrors?: Record<string, string[]>
  ) {
    super(message);
    this.name = 'BlogAdminError';
  }
}

// API error handler
export const handleBlogAdminError = (error: any): BlogAdminError => {
  if (error.response) {
    const { status, data } = error.response;
    const message = data.message || '서버 오류가 발생했습니다.';
    const fieldErrors = data.fieldErrors || {};
    return new BlogAdminError(message, status, fieldErrors);
  }

  if (error.request) {
    return new BlogAdminError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.');
  }

  return new BlogAdminError(error.message || '알 수 없는 오류가 발생했습니다.');
};

export default blogAdminApi;