# 블로그 프론트엔드 페이지 구조 설계

## 1. 전체 아키텍처 개요

StockQuest 블로그 시스템은 SEO 최적화와 Google 광고 수익화를 위한 투자 교육 콘텐츠 플랫폼입니다.

### 핵심 목표
- **SEO 최적화**: 검색 엔진 친화적 URL과 메타데이터
- **사용자 참여도**: 고품질 투자 교육 콘텐츠로 체류 시간 증가
- **광고 수익화**: 사이드바 Google 광고 최적화
- **전환율**: 블로그 방문자를 플랫폼 사용자로 전환

## 2. URL 구조 및 라우팅

### 메인 블로그 페이지
```
/blog                          # 블로그 메인 (최신글, 추천글)
/blog/articles                 # 전체 글 목록 (검색, 필터링)
/blog/articles/[slug]          # 개별 글 상세 페이지
/blog/categories               # 카테고리 목록
/blog/categories/[slug]        # 카테고리별 글 목록
/blog/tags/[slug]              # 태그별 글 목록
/blog/search                   # 검색 결과 페이지
```

### 교육 섹션 (투자 계산기 도구)
```
/education                     # 투자 교육 메인
/education/calculators         # 계산기 도구 목록
/education/calculators/dca     # DCA 계산기
/education/calculators/compound# 복리 계산기
/education/calculators/risk    # 위험도 계산기
/education/guides              # 투자 가이드
/education/guides/[slug]       # 개별 가이드 페이지
```

## 3. Feature-Sliced Design 구조

### Features Layer (`src/features/`)
```
src/features/
├── blog-content/              # 블로그 콘텐츠 관리
│   ├── api/
│   │   └── blog-api.ts       # 블로그 API 클라이언트
│   ├── components/
│   │   ├── ArticleCard.tsx   # 글 카드 컴포넌트
│   │   ├── ArticleList.tsx   # 글 목록 컴포넌트
│   │   ├── CategoryFilter.tsx# 카테고리 필터
│   │   └── TagCloud.tsx      # 태그 클라우드
│   ├── hooks/
│   │   ├── useArticles.ts    # 글 목록 hook
│   │   ├── useCategories.ts  # 카테고리 hook
│   │   └── useTags.ts        # 태그 hook
│   └── types/
│       └── blog.types.ts     # 블로그 타입 정의

├── education-tools/           # 교육 도구 (계산기)
│   ├── components/
│   │   ├── DCACalculator.tsx
│   │   ├── CompoundCalculator.tsx
│   │   └── RiskCalculator.tsx
│   ├── hooks/
│   │   └── useCalculator.ts
│   └── utils/
│       └── calculations.ts

└── seo-optimization/          # SEO 최적화
    ├── components/
    │   ├── MetaTags.tsx
    │   ├── StructuredData.tsx
    │   └── BreadcrumbNav.tsx
    └── hooks/
        └── useSEO.ts
```

### Widgets Layer (`src/widgets/`)
```
src/widgets/
├── blog-layout/               # 블로그 레이아웃
│   ├── BlogHeader.tsx        # 블로그 헤더
│   ├── BlogSidebar.tsx       # 사이드바 (광고 영역 포함)
│   ├── BlogFooter.tsx        # 블로그 푸터
│   └── BlogLayout.tsx        # 전체 레이아웃

├── content-showcase/          # 콘텐츠 쇼케이스
│   ├── FeaturedArticles.tsx  # 추천 글
│   ├── PopularArticles.tsx   # 인기 글
│   ├── RecentArticles.tsx    # 최신 글
│   └── RelatedArticles.tsx   # 관련 글

├── navigation/                # 네비게이션
│   ├── BlogNavigation.tsx    # 블로그 네비게이션
│   ├── CategoryMenu.tsx      # 카테고리 메뉴
│   └── SearchBar.tsx         # 검색바

└── monetization/              # 수익화 위젯
    ├── AdSidebar.tsx         # 광고 사이드바
    ├── NewsletterSignup.tsx  # 뉴스레터 가입
    └── CTABanner.tsx         # 행동 유도 배너
```

### App Router Pages (`src/app/`)
```
src/app/
├── blog/
│   ├── page.tsx              # 블로그 메인 페이지
│   ├── layout.tsx            # 블로그 레이아웃
│   ├── articles/
│   │   ├── page.tsx          # 글 목록 페이지
│   │   └── [slug]/
│   │       └── page.tsx      # 글 상세 페이지
│   ├── categories/
│   │   ├── page.tsx          # 카테고리 목록
│   │   └── [slug]/
│   │       └── page.tsx      # 카테고리별 글 목록
│   ├── tags/
│   │   └── [slug]/
│   │       └── page.tsx      # 태그별 글 목록
│   └── search/
│       └── page.tsx          # 검색 결과

└── education/
    ├── page.tsx              # 교육 메인 페이지
    ├── layout.tsx            # 교육 레이아웃
    ├── calculators/
    │   ├── page.tsx          # 계산기 목록
    │   ├── dca/
    │   │   └── page.tsx      # DCA 계산기
    │   ├── compound/
    │   │   └── page.tsx      # 복리 계산기
    │   └── risk/
    │       └── page.tsx      # 위험도 계산기
    └── guides/
        ├── page.tsx          # 가이드 목록
        └── [slug]/
            └── page.tsx      # 개별 가이드
```

## 4. API 클라이언트 설계

### Blog API Client (`src/shared/api/blog-client.ts`)
```typescript
// 기존 패턴을 따른 블로그 API 클라이언트
export interface Article {
  id: number;
  title: string;
  slug: string;
  summary: string;
  content: string;
  authorNickname: string;
  categoryId: number;
  categoryName: string;
  status: ArticleStatus;
  featured: boolean;
  viewCount: number;
  likeCount: number;
  readingTimeMinutes: number;
  difficulty: ArticleDifficulty;
  tags: Tag[];
  publishedAt: string;
  createdAt: string;
  // SEO 메타데이터
  seoTitle?: string;
  metaDescription?: string;
  canonicalUrl?: string;
  ogImageUrl?: string;
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
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export const blogApi = {
  // 글 목록 및 검색
  searchArticles: (params: ArticleSearchParams): Promise<ArticleSearchResponse> =>
    apiClient.get('/api/v1/content/articles', { params }),

  // 개별 글 조회
  getArticleBySlug: (slug: string): Promise<Article> =>
    apiClient.get(`/api/v1/content/articles/${slug}`),

  // 추천 글
  getFeaturedArticles: (limit = 5): Promise<Article[]> =>
    apiClient.get('/api/v1/content/articles/featured', { params: { limit } }),

  // 최신 글
  getRecentArticles: (limit = 10): Promise<Article[]> =>
    apiClient.get('/api/v1/content/articles/recent', { params: { limit } }),

  // 카테고리
  getAllCategories: (): Promise<Category[]> =>
    apiClient.get('/api/v1/content/categories'),

  getCategoryBySlug: (slug: string): Promise<Category> =>
    apiClient.get(`/api/v1/content/categories/${slug}`),

  // 태그
  getPopularTags: (limit = 30): Promise<Tag[]> =>
    apiClient.get('/api/v1/content/tags/popular', { params: { limit } }),

  getTagsByType: (type: TagType): Promise<Tag[]> =>
    apiClient.get(`/api/v1/content/tags/by-type/${type}`),
};
```

## 5. SEO 최적화 전략

### 메타데이터 시스템
```typescript
// src/features/seo-optimization/components/MetaTags.tsx
export interface SEOProps {
  title: string;
  description: string;
  keywords?: string;
  canonicalUrl?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImageUrl?: string;
  twitterTitle?: string;
  twitterDescription?: string;
  twitterImageUrl?: string;
  articleData?: {
    publishedTime: string;
    modifiedTime: string;
    author: string;
    section: string;
    tags: string[];
  };
}

// Next.js 14 Metadata API 활용
export function generateMetadata({ params }: { params: { slug: string } }): Metadata {
  // 블로그 글의 SEO 메타데이터 동적 생성
}
```

### 구조화된 데이터 (Schema.org)
```typescript
// 블로그 글용 JSON-LD 구조화된 데이터
const articleStructuredData = {
  '@context': 'https://schema.org',
  '@type': 'Article',
  headline: article.title,
  description: article.metaDescription,
  author: {
    '@type': 'Person',
    name: article.authorNickname,
  },
  datePublished: article.publishedAt,
  dateModified: article.updatedAt,
  mainEntityOfPage: {
    '@type': 'WebPage',
    '@id': canonicalUrl,
  },
  // 투자 교육 콘텐츠 특화 스키마
  educationalAlignment: {
    '@type': 'AlignmentObject',
    alignmentType: 'educationalSubject',
    targetName: '투자 교육',
  },
};
```

## 6. 광고 수익화 최적화

### 광고 배치 전략
```typescript
// src/widgets/monetization/AdSidebar.tsx
export const AdSidebar = () => {
  return (
    <aside className="sticky top-4 space-y-6">
      {/* Google AdSense 디스플레이 광고 */}
      <div className="ad-unit">
        <GoogleAd
          adSlot="1234567890"
          adFormat="rectangle"
          className="w-full h-64"
        />
      </div>

      {/* 뉴스레터 가입 CTA */}
      <NewsletterSignup />

      {/* 인기 글 위젯 */}
      <PopularArticlesWidget />

      {/* 하단 광고 */}
      <div className="ad-unit">
        <GoogleAd
          adSlot="0987654321"
          adFormat="vertical"
          className="w-full h-96"
        />
      </div>
    </aside>
  );
};
```

### 사용자 참여도 향상
- **관련 글 추천**: 태그 기반 자동 추천 시스템
- **카테고리 네비게이션**: 직관적인 투자 주제별 분류
- **검색 기능**: 전문 용어 및 한국어 검색 최적화
- **읽기 시간 표시**: 사용자 경험 개선
- **소셜 공유**: 바이럴 확산을 위한 공유 버튼

## 7. 성능 최적화

### Next.js 14 최적화 기능
- **App Router**: 최신 Next.js 라우팅 시스템
- **Server Components**: 서버 사이드 렌더링 최적화
- **Static Generation**: 블로그 글 정적 생성
- **Image Optimization**: Next.js Image 컴포넌트 활용
- **Code Splitting**: 동적 import를 통한 번들 크기 최적화

### 캐싱 전략
```typescript
// Next.js 캐싱 설정
export const revalidate = 3600; // 1시간마다 재검증

// ISR (Incremental Static Regeneration)
export async function generateStaticParams() {
  const articles = await blogApi.getFeaturedArticles(50);
  return articles.map((article) => ({
    slug: article.slug,
  }));
}
```

## 8. 접근성 및 사용자 경험

### 웹 접근성 (WCAG 2.1 AA)
- **시맨틱 HTML**: 스크린 리더 최적화
- **키보드 내비게이션**: 키보드만으로 완전한 사이트 이용 가능
- **색상 대비**: 4.5:1 이상의 색상 대비율
- **대체 텍스트**: 모든 이미지에 적절한 alt 텍스트

### 모바일 최적화
- **반응형 디자인**: 모든 디바이스에서 최적화된 경험
- **터치 친화적**: 최소 44px 터치 타겟 크기
- **빠른 로딩**: Core Web Vitals 최적화

## 9. 개발 우선순위

### Phase 1: 기본 블로그 구조 (현재)
- [x] 백엔드 도메인 설계
- [x] REST API 구현
- [ ] 프론트엔드 페이지 구조 설계 ← **현재 단계**

### Phase 2: 핵심 기능 구현
- [ ] 블로그 API 클라이언트 개발
- [ ] 기본 페이지 컴포넌트 구현
- [ ] SEO 메타데이터 시스템

### Phase 3: 교육 도구 개발
- [ ] 투자 계산기 백엔드 로직
- [ ] 계산기 프론트엔드 UI
- [ ] 교육 콘텐츠 관리

### Phase 4: 최적화 및 수익화
- [ ] 광고 배치 최적화
- [ ] 성능 튜닝
- [ ] 분석 및 모니터링 시스템

## 10. 예상 효과

### SEO 및 트래픽
- **검색 엔진 노출**: 투자 관련 키워드 검색 시 상위 노출
- **백링크 생성**: 고품질 교육 콘텐츠로 자연스러운 백링크 획득
- **재방문률 증가**: 정기적인 콘텐츠 업데이트로 재방문 유도

### 광고 수익화
- **높은 CTR**: 타겟팅된 투자 관련 광고
- **긴 체류 시간**: 교육성 콘텐츠로 인한 높은 사용자 참여도
- **프리미엄 광고**: 금융 분야의 높은 광고 단가

### 사용자 전환
- **플랫폼 가입**: 블로그 방문자의 투자 시뮬레이션 플랫폼 가입
- **브랜드 인지도**: 투자 교육 전문 플랫폼으로서의 브랜드 구축
- **커뮤니티 형성**: 투자 학습자들의 커뮤니티 플랫폼 역할