'use client';

import Link from 'next/link';
import { useGetApiV1ContentArticlesFeatured } from '@/shared/api/generated/블로그-콘텐츠/블로그-콘텐츠';
import type { ArticleResponse } from '@/shared/api/generated/model';

export default function FeaturedArticles() {
  const { data: articles, isLoading: loading, error } = useGetApiV1ContentArticlesFeatured(
    { limit: 6 },
    {
      staleTime: 1000 * 60 * 5, // 5 minutes
      cacheTime: 1000 * 60 * 10, // 10 minutes
    }
  );

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="bg-white rounded-lg shadow-sm p-6 animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
            <div className="h-3 bg-gray-200 rounded mb-2"></div>
            <div className="h-3 bg-gray-200 rounded mb-4"></div>
            <div className="flex justify-between">
              <div className="h-3 bg-gray-200 rounded w-20"></div>
              <div className="h-3 bg-gray-200 rounded w-16"></div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12 bg-red-50 rounded-lg">
        <p className="text-red-500">추천 글을 불러오는 중 오류가 발생했습니다.</p>
      </div>
    );
  }

  if (!articles || articles.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-50 rounded-lg">
        <p className="text-gray-500">추천 글이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {articles.map((article) => (
        <article key={article.id} className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow">
          <Link href={`/blog/articles/${article.slug}`} className="block p-6">
            <div className="mb-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-blue-600 font-medium">
                  {article.categoryName}
                </span>
                {article.featured && (
                  <span className="text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded-full">
                    추천
                  </span>
                )}
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
                {article.title}
              </h3>
              <p className="text-gray-600 text-sm line-clamp-3">
                {article.summary}
              </p>
            </div>
            <div className="flex items-center justify-between text-xs text-gray-500">
              <div className="flex items-center space-x-3">
                <span>조회 {article.viewCount.toLocaleString()}</span>
                <span>•</span>
                <span>{article.readingTimeDisplay}</span>
              </div>
              <span className={`px-2 py-1 rounded text-xs font-medium ${
                article.difficulty === 'BEGINNER' 
                  ? 'bg-green-100 text-green-700'
                  : article.difficulty === 'INTERMEDIATE'
                  ? 'bg-yellow-100 text-yellow-700'
                  : 'bg-red-100 text-red-700'
              }`}>
                {article.difficultyDisplay}
              </span>
            </div>
          </Link>
        </article>
      ))}
    </div>
  );
}