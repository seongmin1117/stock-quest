'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';
import { Bars3Icon, XMarkIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';

const navigation = [
  { name: '홈', href: '/blog' },
  { name: '투자 기초', href: '/blog/categories/investment-basics' },
  { name: '주식 투자', href: '/blog/categories/stock-investment' },
  { name: '채권 투자', href: '/blog/categories/bond-investment' },
  { name: '펀드/ETF', href: '/blog/categories/fund-etf' },
  { name: '투자 도구', href: '/education/calculators' },
  { name: '가이드', href: '/education/guides' },
];

export default function BlogHeader() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const pathname = usePathname();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      window.location.href = `/blog/search?q=${encodeURIComponent(searchQuery.trim())}`;
    }
  };

  const isActive = (href: string) => {
    if (href === '/blog') {
      return pathname === '/blog';
    }
    return pathname.startsWith(href);
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      {/* 상단 브랜드 바 */}
      <div className="bg-blue-600 text-white py-2">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between text-sm">
            <div className="flex items-center space-x-4">
              <Link href="/" className="hover:text-blue-200 transition-colors">
                StockQuest 메인으로
              </Link>
              <span className="text-blue-200">|</span>
              <span>투자 시뮬레이션으로 안전하게 투자 연습하기</span>
            </div>
            <div className="hidden md:block">
              <span>무료 투자 교육 · 실전 시뮬레이션</span>
            </div>
          </div>
        </div>
      </div>

      {/* 메인 헤더 */}
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* 로고 */}
          <div className="flex items-center">
            <Link href="/blog" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">SQ</span>
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">StockQuest 블로그</h1>
                <p className="text-xs text-gray-500">투자 교육 & 투자 도구</p>
              </div>
            </Link>
          </div>

          {/* 검색바 (데스크톱) */}
          <div className="hidden md:block flex-1 max-w-lg mx-8">
            <form onSubmit={handleSearch} className="relative">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="투자 주제, 용어 검색..."
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <MagnifyingGlassIcon className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
            </form>
          </div>

          {/* 데스크톱 네비게이션 */}
          <nav className="hidden lg:flex items-center space-x-1">
            {navigation.map((item) => (
              <Link
                key={item.name}
                href={item.href}
                className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive(item.href)
                    ? 'text-blue-600 bg-blue-50'
                    : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                }`}
              >
                {item.name}
              </Link>
            ))}
          </nav>

          {/* 모바일 메뉴 버튼 */}
          <div className="lg:hidden">
            <button
              type="button"
              className="p-2 rounded-md text-gray-700 hover:text-blue-600 hover:bg-gray-50"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              {mobileMenuOpen ? (
                <XMarkIcon className="h-6 w-6" />
              ) : (
                <Bars3Icon className="h-6 w-6" />
              )}
            </button>
          </div>
        </div>

        {/* 모바일 검색바 */}
        <div className="md:hidden pb-4">
          <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="투자 주제, 용어 검색..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
            <MagnifyingGlassIcon className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
          </form>
        </div>
      </div>

      {/* 모바일 메뉴 */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-gray-200 bg-white">
          <nav className="container mx-auto px-4 py-4 space-y-2">
            {navigation.map((item) => (
              <Link
                key={item.name}
                href={item.href}
                className={`block px-3 py-2 rounded-md text-base font-medium transition-colors ${
                  isActive(item.href)
                    ? 'text-blue-600 bg-blue-50'
                    : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                }`}
                onClick={() => setMobileMenuOpen(false)}
              >
                {item.name}
              </Link>
            ))}
          </nav>
        </div>
      )}
    </header>
  );
}