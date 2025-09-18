'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { blogApi, Article, Tag, TagType } from '@/shared/api/blog-client';

export default function BlogSidebar() {
  const [popularArticles, setPopularArticles] = useState<Article[]>([]);
  const [popularTags, setPopularTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadSidebarData = async () => {
      try {
        const [articles, tags] = await Promise.all([
          blogApi.getFeaturedArticles(5),
          blogApi.getPopularTags(20)
        ]);
        setPopularArticles(articles);
        setPopularTags(tags);
      } catch (error) {
        console.error('Failed to load sidebar data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadSidebarData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Google AdSense 광고 영역 */}
      <div className="bg-gray-100 border border-gray-200 rounded-lg p-4">
        <div className="text-center text-gray-500 text-sm mb-2">광고</div>
        <div className="h-64 bg-white border border-gray-300 rounded flex items-center justify-center">
          <div className="text-gray-400 text-sm">
            Google AdSense
            <br />
            300x250 광고 영역
          </div>
        </div>
      </div>

      {/* 뉴스레터 구독 */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-blue-900 mb-2">
          투자 뉴스레터 구독
        </h3>
        <p className="text-blue-700 text-sm mb-4">
          매주 엄선된 투자 인사이트와 교육 콘텐츠를 받아보세요.
        </p>
        <form className="space-y-3">
          <input
            type="email"
            placeholder="이메일 주소"
            className="w-full px-3 py-2 border border-blue-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
          >
            구독하기
          </button>
        </form>
        <p className="text-xs text-blue-600 mt-2">
          스팸 없음. 언제든 구독 취소 가능.
        </p>
      </div>

      {/* 인기 글 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          🔥 인기 글
        </h3>
        {loading ? (
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-4">
            {popularArticles.map((article, index) => (
              <div key={article.id} className="flex items-start space-x-3">
                <span className="text-blue-600 font-bold text-sm mt-1">
                  {index + 1}
                </span>
                <div className="flex-1 min-w-0">
                  <Link
                    href={`/blog/articles/${article.slug}`}
                    className="text-sm font-medium text-gray-900 hover:text-blue-600 line-clamp-2 transition-colors"
                  >
                    {article.title}
                  </Link>
                  <div className="flex items-center text-xs text-gray-500 mt-1 space-x-2">
                    <span>조회 {article.viewCount.toLocaleString()}</span>
                    <span>•</span>
                    <span>{article.readingTimeDisplay}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 광고 영역 2 */}
      <div className="bg-gray-100 border border-gray-200 rounded-lg p-4">
        <div className="text-center text-gray-500 text-sm mb-2">광고</div>
        <div className="h-96 bg-white border border-gray-300 rounded flex items-center justify-center">
          <div className="text-gray-400 text-sm text-center">
            Google AdSense
            <br />
            300x400 광고 영역
          </div>
        </div>
      </div>

      {/* 인기 태그 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          🏷️ 인기 태그
        </h3>
        {loading ? (
          <div className="flex flex-wrap gap-2">
            {[...Array(10)].map((_, i) => (
              <div
                key={i}
                className="h-6 bg-gray-200 rounded-full animate-pulse"
                style={{ width: `${50 + Math.random() * 50}px` }}
              ></div>
            ))}
          </div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {popularTags.map((tag) => (
              <Link
                key={tag.id}
                href={`/blog/tags/${tag.slug}`}
                className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium transition-colors hover:opacity-80 ${getTagColor(tag.type)}`}
              >
                #{tag.name}
                <span className="ml-1 text-xs opacity-75">
                  {tag.usageCount}
                </span>
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* StockQuest 플랫폼 홍보 */}
      <div className="bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg p-6">
        <h3 className="text-lg font-semibold mb-2">
          무료 투자 시뮬레이션 체험
        </h3>
        <p className="text-blue-100 text-sm mb-4">
          실제 돈 없이 투자 연습하고 실력을 키워보세요.
        </p>
        <Link
          href="/challenges"
          className="inline-block bg-white text-blue-600 px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-50 transition-colors"
        >
          무료 체험하기
        </Link>
      </div>

      {/* 투자 계산기 도구 홍보 */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-green-900 mb-2">
          💰 무료 투자 계산기
        </h3>
        <p className="text-green-700 text-sm mb-4">
          복리, DCA, 위험도 계산기로 투자 계획을 세워보세요.
        </p>
        <div className="space-y-2">
          <Link
            href="/education/calculators/compound"
            className="block text-green-700 hover:text-green-900 text-sm transition-colors"
          >
            → 복리 계산기
          </Link>
          <Link
            href="/education/calculators/dca"
            className="block text-green-700 hover:text-green-900 text-sm transition-colors"
          >
            → DCA 계산기
          </Link>
          <Link
            href="/education/calculators/risk"
            className="block text-green-700 hover:text-green-900 text-sm transition-colors"
          >
            → 위험도 계산기
          </Link>
        </div>
      </div>
    </div>
  );
}

// 태그 타입별 색상 반환
function getTagColor(type: TagType): string {
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
}