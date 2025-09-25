import { Metadata } from 'next';
import Link from 'next/link';
import { Suspense } from 'react';
import {
  Article,
  Category,
  Tag,
  blogApi,
  blogUtils,
  ArticleSearchParams,
} from '@/shared/api/blog-client';
import BlogLayout from '@/widgets/blog-layout/BlogLayout';
import {
  ClockIcon,
  UserIcon,
  CalendarIcon,
  EyeIcon,
  HeartIcon,
  TagIcon,
  HomeIcon,
  ChevronRightIcon,
  ChevronLeftIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  BookOpenIcon,
} from '@heroicons/react/24/outline';

// Static metadata for articles listing page
export const metadata: Metadata = {
  title: '전체 글 목록 | StockQuest 블로그',
  description: '투자 전문가들이 작성한 모든 글을 확인하세요. 주식, ETF, 펀드, 부동산 투자 등 다양한 주제의 투자 인사이트와 교육 콘텐츠를 제공합니다.',
  keywords: '투자 글, 주식투자, 투자교육, 투자전략, 주식분석, ETF, 펀드투자, 부동산투자, 투자블로그',
  openGraph: {
    title: '전체 글 목록 | StockQuest 블로그',
    description: '투자 전문가들의 모든 글을 한곳에서 확인하세요.',
    url: 'https://stockquest.co.kr/blog/articles',
    siteName: 'StockQuest',
    locale: 'ko_KR',
    type: 'website',
    images: [
      {
        url: 'https://stockquest.co.kr/images/blog-articles-og.jpg',
        width: 1200,
        height: 630,
        alt: 'StockQuest 블로그 전체 글',
      },
    ],
  },
  twitter: {
    card: 'summary_large_image',
    title: '전체 글 목록 | StockQuest 블로그',
    description: '투자 전문가들의 모든 글을 한곳에서 확인하세요.',
    images: ['https://stockquest.co.kr/images/blog-articles-twitter.jpg'],
  },
  alternates: {
    canonical: 'https://stockquest.co.kr/blog/articles',
  },
};

interface ArticlesPageProps {
  searchParams: {
    page?: string;
    limit?: string;
    query?: string;
    category?: string;
    tag?: string;
    difficulty?: string;
    featured?: string;
    sort?: string;
    order?: string;
  };
}

// Server Component - fetches data based on search parameters
export default async function ArticlesPage({ searchParams }: ArticlesPageProps) {
  try {
    // Parse search parameters
    const currentPage = parseInt(searchParams.page || '1', 10);
    const pageSize = parseInt(searchParams.limit || '12', 10);
    const offset = (currentPage - 1) * pageSize;

    // Build search parameters
    const searchRequest: ArticleSearchParams = {
      limit: pageSize,
      offset,
      query: searchParams.query,
      sortBy: searchParams.sort || 'published_at',
      sortDirection: searchParams.order || 'desc',
    };

    // Add filters if provided
    // Note: For now using query params directly,
    // TODO: Convert category/tag slugs to IDs when backend supports slug-based filtering
    if (searchParams.category) {
      // searchRequest.categoryId = searchParams.category; // Will implement slug-to-ID conversion later
    }
    if (searchParams.tag) {
      // searchRequest.tagIds = [searchParams.tag]; // Will implement slug-to-ID conversion later
    }
    if (searchParams.difficulty) {
      searchRequest.difficulty = searchParams.difficulty as any;
    }
    if (searchParams.featured === 'true') {
      searchRequest.featured = true;
    }

    // Fetch data in parallel
    const [searchResponse, categories, popularTags] = await Promise.all([
      blogApi.searchArticles(searchRequest),
      blogApi.getAllCategories(),
      blogApi.getPopularTags(20),
    ]);

    const { articles, totalCount, totalPages, hasNext, hasPrevious } = searchResponse;

    return (
      <BlogLayout>
        <div className="max-w-7xl mx-auto p-6">
          {/* Breadcrumb Navigation */}
          <nav className="flex items-center space-x-2 text-sm text-gray-500 mb-6">
            <Link href="/" className="hover:text-blue-600 flex items-center">
              <HomeIcon className="w-4 h-4" />
            </Link>
            <ChevronRightIcon className="w-4 h-4" />
            <Link href="/blog" className="hover:text-blue-600">
              블로그
            </Link>
            <ChevronRightIcon className="w-4 h-4" />
            <span className="text-blue-600 font-medium">전체 글</span>
          </nav>

          {/* Page Header */}
          <header className="mb-8">
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              전체 글 목록
            </h1>
            <p className="text-lg text-gray-600 mb-6">
              투자 전문가들이 작성한 {totalCount.toLocaleString()}개의 글을 확인하세요.
            </p>

            {/* Search and Filter Bar */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
              <form className="flex flex-col lg:flex-row gap-4">
                {/* Search Input */}
                <div className="flex-1">
                  <div className="relative">
                    <MagnifyingGlassIcon className="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      name="query"
                      placeholder="제목, 내용, 작성자로 검색..."
                      defaultValue={searchParams.query || ''}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>

                {/* Category Filter */}
                <div className="lg:w-48">
                  <select
                    name="category"
                    defaultValue={searchParams.category || ''}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">모든 카테고리</option>
                    {categories.map((category) => (
                      <option key={category.id} value={category.slug}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Difficulty Filter */}
                <div className="lg:w-32">
                  <select
                    name="difficulty"
                    defaultValue={searchParams.difficulty || ''}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">모든 난이도</option>
                    <option value="BEGINNER">초급</option>
                    <option value="INTERMEDIATE">중급</option>
                    <option value="ADVANCED">고급</option>
                  </select>
                </div>

                {/* Sort Order */}
                <div className="lg:w-40">
                  <select
                    name="sort"
                    defaultValue={`${searchParams.sort || 'published_at'}_${searchParams.order || 'desc'}`}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="published_at_desc">최신순</option>
                    <option value="published_at_asc">오래된순</option>
                    <option value="view_count_desc">조회수순</option>
                    <option value="like_count_desc">좋아요순</option>
                    <option value="title_asc">제목순</option>
                  </select>
                </div>

                {/* Search Button */}
                <button
                  type="submit"
                  className="lg:w-auto px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center"
                >
                  <FunnelIcon className="w-4 h-4 mr-2" />
                  필터 적용
                </button>
              </form>
            </div>

            {/* Active Filters Display */}
            {(searchParams.query || searchParams.category || searchParams.tag || searchParams.difficulty || searchParams.featured) && (
              <div className="flex flex-wrap items-center gap-2 mb-4">
                <span className="text-sm text-gray-500">활성 필터:</span>
                {searchParams.query && (
                  <span className="inline-flex items-center px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
                    검색: &ldquo;{searchParams.query}&rdquo;
                    <Link href="/blog/articles" className="ml-2 text-blue-600 hover:text-blue-800">×</Link>
                  </span>
                )}
                {searchParams.category && (
                  <span className="inline-flex items-center px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm">
                    카테고리: {categories.find(c => c.slug === searchParams.category)?.name || searchParams.category}
                    <Link href="/blog/articles" className="ml-2 text-green-600 hover:text-green-800">×</Link>
                  </span>
                )}
                {searchParams.difficulty && (
                  <span className="inline-flex items-center px-3 py-1 bg-purple-100 text-purple-800 rounded-full text-sm">
                    난이도: {searchParams.difficulty === 'BEGINNER' ? '초급' : searchParams.difficulty === 'INTERMEDIATE' ? '중급' : '고급'}
                    <Link href="/blog/articles" className="ml-2 text-purple-600 hover:text-purple-800">×</Link>
                  </span>
                )}
                {searchParams.featured === 'true' && (
                  <span className="inline-flex items-center px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm">
                    추천글만
                    <Link href="/blog/articles" className="ml-2 text-yellow-600 hover:text-yellow-800">×</Link>
                  </span>
                )}
              </div>
            )}

            {/* Results Summary */}
            <div className="flex items-center justify-between text-sm text-gray-600">
              <span>
                총 {totalCount.toLocaleString()}개 글 중 {((currentPage - 1) * pageSize) + 1}-{Math.min(currentPage * pageSize, totalCount)}번째
              </span>
              <span>
                {currentPage} / {totalPages} 페이지
              </span>
            </div>
          </header>

          {/* Articles Grid */}
          {articles.length > 0 ? (
            <section className="mb-12">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {articles.map((article) => (
                  <article key={article.id} className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
                    <div className="p-6">
                      {/* Article Badges */}
                      <div className="flex items-center gap-2 mb-3">
                        {article.featured && (
                          <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs font-medium">
                            ⭐ 추천
                          </span>
                        )}
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${blogUtils.getDifficultyColor(article.difficulty)}`}>
                          {article.difficultyDisplay}
                        </span>
                        {article.categoryName && (
                          <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">
                            {article.categoryName}
                          </span>
                        )}
                      </div>

                      {/* Article Title */}
                      <h2 className="text-lg font-bold text-gray-900 mb-2 line-clamp-2">
                        <Link href={blogUtils.getArticleUrl(article.slug)} className="hover:text-blue-600 transition-colors">
                          {article.title}
                        </Link>
                      </h2>

                      {/* Article Summary */}
                      <p className="text-gray-600 text-sm mb-4 line-clamp-3">
                        {article.summary}
                      </p>

                      {/* Article Meta */}
                      <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mb-4">
                        <div className="flex items-center">
                          <UserIcon className="w-3 h-3 mr-1" />
                          <span>{article.authorNickname}</span>
                        </div>
                        {article.publishedAt && (
                          <div className="flex items-center">
                            <CalendarIcon className="w-3 h-3 mr-1" />
                            <span>{new Date(article.publishedAt).toLocaleDateString('ko-KR')}</span>
                          </div>
                        )}
                        <div className="flex items-center">
                          <ClockIcon className="w-3 h-3 mr-1" />
                          <span>{article.readingTimeDisplay}</span>
                        </div>
                        <div className="flex items-center">
                          <EyeIcon className="w-3 h-3 mr-1" />
                          <span>{article.viewCount.toLocaleString()}</span>
                        </div>
                        {article.likeCount > 0 && (
                          <div className="flex items-center">
                            <HeartIcon className="w-3 h-3 mr-1" />
                            <span>{article.likeCount.toLocaleString()}</span>
                          </div>
                        )}
                      </div>

                      {/* Article Tags */}
                      {article.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1">
                          {article.tags.slice(0, 3).map((tag) => (
                            <Link
                              key={tag.id}
                              href={blogUtils.getTagUrl(tag.slug)}
                              className={`inline-flex items-center px-2 py-1 rounded-md text-xs font-medium hover:opacity-80 transition-opacity ${blogUtils.getTagTypeColor(tag.type)}`}
                            >
                              <TagIcon className="w-3 h-3 mr-1" />
                              {tag.name}
                            </Link>
                          ))}
                          {article.tags.length > 3 && (
                            <span className="text-xs text-gray-500 px-2 py-1">
                              +{article.tags.length - 3}개
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </article>
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <nav className="flex items-center justify-center space-x-2" aria-label="Pagination">
                  {/* Previous Page */}
                  {hasPrevious && (
                    <Link
                      href={`/blog/articles?page=${currentPage - 1}${searchParams.query ? `&query=${encodeURIComponent(searchParams.query)}` : ''}${searchParams.category ? `&category=${searchParams.category}` : ''}${searchParams.difficulty ? `&difficulty=${searchParams.difficulty}` : ''}`}
                      className="flex items-center px-3 py-2 text-sm text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:text-gray-700"
                    >
                      <ChevronLeftIcon className="w-4 h-4 mr-1" />
                      이전
                    </Link>
                  )}

                  {/* Page Numbers */}
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const startPage = Math.max(1, currentPage - 2);
                    const pageNum = startPage + i;

                    if (pageNum > totalPages) return null;

                    const isActive = pageNum === currentPage;

                    return (
                      <Link
                        key={pageNum}
                        href={`/blog/articles?page=${pageNum}${searchParams.query ? `&query=${encodeURIComponent(searchParams.query)}` : ''}${searchParams.category ? `&category=${searchParams.category}` : ''}${searchParams.difficulty ? `&difficulty=${searchParams.difficulty}` : ''}`}
                        className={`px-3 py-2 text-sm border rounded-lg ${
                          isActive
                            ? 'bg-blue-600 text-white border-blue-600'
                            : 'text-gray-500 bg-white border-gray-300 hover:bg-gray-50 hover:text-gray-700'
                        }`}
                      >
                        {pageNum}
                      </Link>
                    );
                  })}

                  {/* Next Page */}
                  {hasNext && (
                    <Link
                      href={`/blog/articles?page=${currentPage + 1}${searchParams.query ? `&query=${encodeURIComponent(searchParams.query)}` : ''}${searchParams.category ? `&category=${searchParams.category}` : ''}${searchParams.difficulty ? `&difficulty=${searchParams.difficulty}` : ''}`}
                      className="flex items-center px-3 py-2 text-sm text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:text-gray-700"
                    >
                      다음
                      <ChevronRightIcon className="w-4 h-4 ml-1" />
                    </Link>
                  )}
                </nav>
              )}
            </section>
          ) : (
            // No Results State
            <section className="text-center py-12">
              <BookOpenIcon className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                검색 결과가 없습니다
              </h2>
              <p className="text-gray-600 mb-6">
                다른 검색어나 필터 조건을 시도해보세요.
              </p>
              <Link
                href="/blog/articles"
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors inline-flex items-center"
              >
                <FunnelIcon className="w-5 h-5 mr-2" />
                필터 초기화
              </Link>
            </section>
          )}

          {/* Popular Tags Section */}
          {popularTags.length > 0 && (
            <section className="mt-12 pt-8 border-t border-gray-200">
              <h2 className="text-xl font-bold text-gray-900 mb-4">인기 태그</h2>
              <div className="flex flex-wrap gap-2">
                {popularTags.map((tag) => (
                  <Link
                    key={tag.id}
                    href={blogUtils.getTagUrl(tag.slug)}
                    className={`inline-flex items-center px-3 py-2 rounded-lg text-sm font-medium hover:opacity-80 transition-opacity ${blogUtils.getTagTypeColor(tag.type)}`}
                  >
                    <TagIcon className="w-3 h-3 mr-1" />
                    {tag.name}
                    <span className="ml-2 text-xs opacity-75">({tag.usageCount})</span>
                  </Link>
                ))}
              </div>
            </section>
          )}
        </div>
      </BlogLayout>
    );
  } catch (error) {
    // Error fallback for data fetching issues
    return (
      <BlogLayout>
        <div className="max-w-4xl mx-auto p-6">
          <div className="text-center py-12">
            <h1 className="text-3xl font-bold text-gray-900 mb-4">전체 글 목록</h1>
            <p className="text-gray-600 mb-8">
              현재 글 목록을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.
            </p>
            <Link
              href="/blog"
              className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors inline-flex items-center"
            >
              <HomeIcon className="w-5 h-5 mr-2" />
              블로그 홈으로
            </Link>
          </div>
        </div>
      </BlogLayout>
    );
  }
}