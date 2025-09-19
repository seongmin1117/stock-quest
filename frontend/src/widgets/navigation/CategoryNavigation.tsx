'use client';

import Link from 'next/link';
import { useGetApiV1ContentCategories } from '@/shared/api/generated/블로그-콘텐츠/블로그-콘텐츠';
import type { CategoryResponse } from '@/shared/api/generated/model';

export default function CategoryNavigation() {
  const { data: categories, isLoading: loading, error } = useGetApiV1ContentCategories(
    undefined,
    {
      staleTime: 1000 * 60 * 10, // 10 minutes
      cacheTime: 1000 * 60 * 30, // 30 minutes
    }
  );

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="h-20 bg-gray-200 rounded-lg"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6">
        <p className="text-center text-red-500">카테고리를 불러오는 중 오류가 발생했습니다.</p>
      </div>
    );
  }

  if (!categories || categories.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6">
        <p className="text-center text-gray-500">카테고리가 없습니다.</p>
      </div>
    );
  }

  // 주요 투자 카테고리 아이콘 매핑
  const getCategoryIcon = (slug: string): string => {
    switch (slug) {
      case 'investment-basics':
        return '📚';
      case 'stock-investment':
        return '📈';
      case 'bond-investment':
        return '📊';
      case 'fund-etf':
        return '💼';
      case 'cryptocurrency':
        return '🪙';
      case 'real-estate':
        return '🏠';
      case 'forex':
        return '💱';
      case 'commodity':
        return '🏭';
      case 'strategy':
        return '🎯';
      case 'analysis':
        return '📉';
      default:
        return '📁';
    }
  };

  const getCategoryColor = (slug: string): string => {
    switch (slug) {
      case 'investment-basics':
        return 'bg-blue-50 hover:bg-blue-100 text-blue-700 border-blue-200';
      case 'stock-investment':
        return 'bg-green-50 hover:bg-green-100 text-green-700 border-green-200';
      case 'bond-investment':
        return 'bg-yellow-50 hover:bg-yellow-100 text-yellow-700 border-yellow-200';
      case 'fund-etf':
        return 'bg-purple-50 hover:bg-purple-100 text-purple-700 border-purple-200';
      case 'cryptocurrency':
        return 'bg-orange-50 hover:bg-orange-100 text-orange-700 border-orange-200';
      case 'real-estate':
        return 'bg-indigo-50 hover:bg-indigo-100 text-indigo-700 border-indigo-200';
      default:
        return 'bg-gray-50 hover:bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">
        투자 카테고리 둘러보기
      </h2>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
        {categories.map((category) => (
          <Link
            key={category.id}
            href={`/blog/categories/${category.slug}`}
            className={`relative group block p-4 rounded-lg border transition-all duration-200 ${getCategoryColor(category.slug)}`}
          >
            <div className="text-center">
              <div className="text-2xl mb-2 group-hover:scale-110 transition-transform">
                {getCategoryIcon(category.slug)}
              </div>
              <h3 className="font-medium text-sm mb-1">
                {category.name}
              </h3>
              <span className="text-xs opacity-75">
                {category.articleCount}개 글
              </span>
              {category.featuredOnHome && (
                <span className="absolute top-2 right-2">
                  <span className="w-2 h-2 bg-red-400 rounded-full block"></span>
                </span>
              )}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}