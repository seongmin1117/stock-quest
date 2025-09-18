'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { blogApi, Article } from '@/shared/api/blog-client';

export default function RecentArticles() {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadRecentArticles = async () => {
      try {
        const data = await blogApi.getRecentArticles(10);
        setArticles(data);
      } catch (error) {
        console.error('Failed to load recent articles:', error);
        setArticles([]);
      } finally {
        setLoading(false);
      }
    };

    loadRecentArticles();
  }, []);

  if (loading) {
    return (
      <div className="space-y-4">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="bg-white rounded-lg shadow-sm p-4 animate-pulse">
            <div className="flex items-start space-x-4">
              <div className="flex-1">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded mb-2"></div>
                <div className="flex space-x-4">
                  <div className="h-3 bg-gray-200 rounded w-20"></div>
                  <div className="h-3 bg-gray-200 rounded w-16"></div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!articles || articles.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-50 rounded-lg">
        <p className="text-gray-500">최신 글이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {articles.map((article) => (
        <article key={article.id} className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow">
          <Link href={`/blog/articles/${article.slug}`} className="block p-4">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-2 mb-2">
                  <span className="text-sm text-blue-600 font-medium">
                    {article.categoryName}
                  </span>
                  {article.tags && article.tags.length > 0 && (
                    <>
                      <span className="text-gray-400">•</span>
                      {article.tags.slice(0, 3).map((tag) => (
                        <span key={tag.id} className="text-xs text-gray-500">
                          #{tag.name}
                        </span>
                      ))}
                    </>
                  )}
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-1 line-clamp-2">
                  {article.title}
                </h3>
                <p className="text-gray-600 text-sm mb-3 line-clamp-2">
                  {article.summary}
                </p>
                <div className="flex items-center space-x-4 text-xs text-gray-500">
                  <span>{article.authorNickname}</span>
                  <span>•</span>
                  <span>조회 {article.viewCount.toLocaleString()}</span>
                  <span>•</span>
                  <span>{article.readingTimeDisplay}</span>
                  {article.publishedAt && (
                    <>
                      <span>•</span>
                      <span>{new Date(article.publishedAt).toLocaleDateString('ko-KR')}</span>
                    </>
                  )}
                </div>
              </div>
              <div className="ml-4 flex-shrink-0">
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
            </div>
          </Link>
        </article>
      ))}
    </div>
  );
}