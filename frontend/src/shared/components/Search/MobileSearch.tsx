'use client';

import React, { useState, useRef, useEffect, useCallback } from 'react';

interface SearchResult {
  id: string;
  type: 'stock' | 'user' | 'challenge' | 'community';
  symbol?: string;
  name: string;
  subtitle?: string;
  price?: number;
  change?: number;
  changePercent?: number;
  avatar?: string;
  verified?: boolean;
  participants?: number;
  reward?: number;
}

interface MobileSearchProps {
  placeholder?: string;
  onSearch?: (query: string) => void;
  onResultSelect?: (result: SearchResult) => void;
  initialQuery?: string;
  categories?: ('stock' | 'user' | 'challenge' | 'community')[];
}

/**
 * Mobile-optimized search component with intelligent filtering
 * 모바일 최적화된 검색 컴포넌트 (지능형 필터링)
 */
export const MobileSearch: React.FC<MobileSearchProps> = ({
  placeholder = "주식, 유저, 챌린지 검색...",
  onSearch,
  onResultSelect,
  initialQuery = "",
  categories = ['stock', 'user', 'challenge', 'community']
}) => {
  const [query, setQuery] = useState(initialQuery);
  const [isActive, setIsActive] = useState(false);
  const [results, setResults] = useState<SearchResult[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [recentSearches, setRecentSearches] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const inputRef = useRef<HTMLInputElement>(null);
  const searchTimeout = useRef<NodeJS.Timeout>();

  // Mock search results
  const mockResults: SearchResult[] = [
    {
      id: '1',
      type: 'stock',
      symbol: 'AAPL',
      name: 'Apple Inc.',
      subtitle: 'Technology',
      price: 175.43,
      change: 2.15,
      changePercent: 1.24
    },
    {
      id: '2',
      type: 'stock',
      symbol: 'TSLA',
      name: 'Tesla, Inc.',
      subtitle: 'Electric Vehicle',
      price: 248.87,
      change: -8.32,
      changePercent: -3.24
    },
    {
      id: '3',
      type: 'user',
      name: '트레이딩마스터',
      subtitle: '포트폴리오 수익률 +24.5%',
      verified: true
    },
    {
      id: '4',
      type: 'challenge',
      name: 'Weekly Bulls Challenge',
      subtitle: '단기 투자 챌린지',
      participants: 1247,
      reward: 50000
    },
    {
      id: '5',
      type: 'community',
      name: 'Tech Stock 투자 전략',
      subtitle: '기술주 투자 커뮤니티',
      participants: 3241
    },
    {
      id: '6',
      type: 'stock',
      symbol: 'MSFT',
      name: 'Microsoft Corporation',
      subtitle: 'Technology',
      price: 378.85,
      change: 5.67,
      changePercent: 1.52
    }
  ];

  // Debounced search
  const performSearch = useCallback((searchQuery: string) => {
    if (searchTimeout.current) {
      clearTimeout(searchTimeout.current);
    }

    if (!searchQuery.trim()) {
      setResults([]);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);

    searchTimeout.current = setTimeout(() => {
      // Filter results based on query and category
      const filteredResults = mockResults.filter((result) => {
        const matchesQuery = 
          result.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          result.symbol?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          result.subtitle?.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesCategory = 
          selectedCategory === 'all' || 
          result.type === selectedCategory;

        return matchesQuery && matchesCategory;
      });

      setResults(filteredResults);
      setIsLoading(false);
      onSearch?.(searchQuery);
    }, 300);
  }, [selectedCategory, onSearch]);

  // Handle input change
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setQuery(value);
    performSearch(value);
  };

  // Handle search focus
  const handleFocus = () => {
    setIsActive(true);
    if (!query && recentSearches.length > 0) {
      // Show recent searches when focused with empty query
      setResults([]);
    }
  };

  // Handle search blur
  const handleBlur = () => {
    // Delay to allow result selection
    setTimeout(() => {
      setIsActive(false);
    }, 200);
  };

  // Handle result selection
  const handleResultSelect = (result: SearchResult) => {
    setQuery(result.name);
    setIsActive(false);
    
    // Add to recent searches
    const newRecent = [query, ...recentSearches.filter(s => s !== query)].slice(0, 5);
    setRecentSearches(newRecent);
    localStorage.setItem('recentSearches', JSON.stringify(newRecent));
    
    onResultSelect?.(result);
  };

  // Handle recent search selection
  const handleRecentSelect = (recentQuery: string) => {
    setQuery(recentQuery);
    performSearch(recentQuery);
    inputRef.current?.focus();
  };

  // Clear search
  const handleClear = () => {
    setQuery('');
    setResults([]);
    setIsActive(false);
    inputRef.current?.focus();
  };

  // Load recent searches on mount
  useEffect(() => {
    const saved = localStorage.getItem('recentSearches');
    if (saved) {
      try {
        setRecentSearches(JSON.parse(saved));
      } catch (e) {
        console.error('Failed to parse recent searches:', e);
      }
    }
  }, []);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (searchTimeout.current) {
        clearTimeout(searchTimeout.current);
      }
    };
  }, []);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const categoryLabels = {
    all: '전체',
    stock: '주식',
    user: '유저',
    challenge: '챌린지',
    community: '커뮤니티'
  };

  const categoryIcons = {
    all: '🔍',
    stock: '📈',
    user: '👤',
    challenge: '🎯',
    community: '👥'
  };

  const resultIcons = {
    stock: '📈',
    user: '👤',
    challenge: '🎯',
    community: '👥'
  };

  return (
    <div className="relative">
      {/* Search input */}
      <div className={`relative transition-all duration-300 ${
        isActive ? 'transform -translate-y-1 shadow-lg' : ''
      }`}>
        <div className="relative">
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={handleInputChange}
            onFocus={handleFocus}
            onBlur={handleBlur}
            placeholder={placeholder}
            className={`w-full bg-slate-800 text-white rounded-xl px-12 py-4 text-base outline-none transition-all duration-300 ${
              isActive 
                ? 'border-2 border-blue-500 bg-slate-700' 
                : 'border border-slate-600 hover:border-slate-500'
            }`}
          />
          
          {/* Search icon */}
          <div className="absolute left-4 top-1/2 transform -translate-y-1/2">
            <span className="text-gray-400 text-xl">🔍</span>
          </div>

          {/* Clear button */}
          {query && (
            <button
              onClick={handleClear}
              className="absolute right-4 top-1/2 transform -translate-y-1/2 w-6 h-6 bg-slate-600 rounded-full flex items-center justify-center text-white text-sm hover:bg-slate-500 transition-colors"
            >
              ×
            </button>
          )}

          {/* Loading indicator */}
          {isLoading && (
            <div className="absolute right-4 top-1/2 transform -translate-y-1/2">
              <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
          )}
        </div>
      </div>

      {/* Search overlay */}
      {isActive && (
        <>
          {/* Backdrop */}
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 z-40"
            onClick={() => setIsActive(false)}
          />

          {/* Results container */}
          <div className="absolute top-full left-0 right-0 mt-2 bg-slate-800 rounded-xl shadow-2xl border border-slate-600 z-50 max-h-96 overflow-hidden">
            {/* Category filters */}
            {categories.length > 1 && (
              <div className="p-3 border-b border-slate-700">
                <div className="flex space-x-2 overflow-x-auto pb-1">
                  {['all', ...categories].map((category) => (
                    <button
                      key={category}
                      onClick={() => {
                        setSelectedCategory(category);
                        if (query) performSearch(query);
                      }}
                      className={`flex-shrink-0 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                        selectedCategory === category
                          ? 'bg-blue-500 text-white'
                          : 'bg-slate-700 text-gray-300 hover:bg-slate-600 hover:text-white'
                      }`}
                    >
                      <span className="mr-1">{categoryIcons[category as keyof typeof categoryIcons]}</span>
                      {categoryLabels[category as keyof typeof categoryLabels]}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Search results */}
            <div className="max-h-80 overflow-y-auto">
              {!query && recentSearches.length > 0 && (
                <div className="p-3">
                  <div className="text-gray-400 text-sm mb-2">최근 검색</div>
                  {recentSearches.map((recent, index) => (
                    <button
                      key={index}
                      onClick={() => handleRecentSelect(recent)}
                      className="w-full text-left p-2 hover:bg-slate-700 rounded-lg transition-colors"
                    >
                      <div className="flex items-center space-x-3">
                        <span className="text-gray-400">⏱️</span>
                        <span className="text-white">{recent}</span>
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {query && results.length > 0 && (
                <div className="p-2">
                  {results.map((result) => (
                    <button
                      key={result.id}
                      onClick={() => handleResultSelect(result)}
                      className="w-full text-left p-3 hover:bg-slate-700 rounded-lg transition-colors"
                    >
                      <div className="flex items-center space-x-3">
                        <div className="text-xl">
                          {resultIcons[result.type]}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center space-x-2">
                            <div className="text-white font-medium">
                              {result.symbol && (
                                <span className="text-blue-400 font-bold mr-2">
                                  {result.symbol}
                                </span>
                              )}
                              {result.name}
                            </div>
                            {result.verified && (
                              <span className="text-blue-400">✓</span>
                            )}
                          </div>
                          {result.subtitle && (
                            <div className="text-gray-400 text-sm truncate">
                              {result.subtitle}
                            </div>
                          )}
                        </div>
                        <div className="text-right flex-shrink-0">
                          {result.type === 'stock' && result.price && (
                            <div>
                              <div className="text-white font-medium mono-font">
                                {formatCurrency(result.price)}
                              </div>
                              {result.changePercent !== undefined && (
                                <div className={`text-sm ${
                                  result.changePercent >= 0 ? 'text-green-400' : 'text-red-400'
                                }`}>
                                  {result.changePercent >= 0 ? '+' : ''}{result.changePercent.toFixed(2)}%
                                </div>
                              )}
                            </div>
                          )}
                          {(result.type === 'challenge' || result.type === 'community') && result.participants && (
                            <div className="text-gray-400 text-sm">
                              {result.participants.toLocaleString()}명
                            </div>
                          )}
                          {result.type === 'challenge' && result.reward && (
                            <div className="text-green-400 text-sm font-medium">
                              ₩{result.reward.toLocaleString()}
                            </div>
                          )}
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {query && !isLoading && results.length === 0 && (
                <div className="p-6 text-center">
                  <div className="text-gray-400 text-lg mb-2">🔍</div>
                  <div className="text-gray-400 text-sm">
                    '{query}'에 대한 검색 결과가 없습니다
                  </div>
                  <div className="text-gray-500 text-xs mt-1">
                    다른 검색어를 시도해보세요
                  </div>
                </div>
              )}

              {!query && recentSearches.length === 0 && (
                <div className="p-6 text-center">
                  <div className="text-gray-400 text-lg mb-2">🔍</div>
                  <div className="text-gray-400 text-sm">
                    주식, 유저, 챌린지를 검색해보세요
                  </div>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

/**
 * Mobile filter component for advanced filtering options
 * 모바일 필터 컴포넌트 (고급 필터링 옵션)
 */
export interface FilterOption {
  id: string;
  label: string;
  value: string | number;
  type: 'select' | 'range' | 'checkbox' | 'radio';
  options?: { value: string | number; label: string }[];
  min?: number;
  max?: number;
  step?: number;
}

interface MobileFilterProps {
  isOpen: boolean;
  onClose: () => void;
  filters: FilterOption[];
  values: Record<string, any>;
  onChange: (filterId: string, value: any) => void;
  onReset: () => void;
  onApply: () => void;
}

export const MobileFilter: React.FC<MobileFilterProps> = ({
  isOpen,
  onClose,
  filters,
  values,
  onChange,
  onReset,
  onApply
}) => {
  const renderFilter = (filter: FilterOption) => {
    const value = values[filter.id];

    switch (filter.type) {
      case 'select':
        return (
          <select
            value={value || ''}
            onChange={(e) => onChange(filter.id, e.target.value)}
            className="w-full bg-slate-700 text-white rounded-lg px-3 py-3 border border-slate-600 focus:border-blue-500 outline-none"
          >
            <option value="">선택하세요</option>
            {filter.options?.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        );

      case 'range':
        return (
          <div className="space-y-2">
            <input
              type="range"
              min={filter.min || 0}
              max={filter.max || 100}
              step={filter.step || 1}
              value={value || filter.min || 0}
              onChange={(e) => onChange(filter.id, parseFloat(e.target.value))}
              className="w-full h-2 bg-slate-600 rounded-lg appearance-none cursor-pointer slider"
            />
            <div className="flex justify-between text-sm text-gray-400">
              <span>{filter.min || 0}</span>
              <span className="text-white font-medium">{value || filter.min || 0}</span>
              <span>{filter.max || 100}</span>
            </div>
          </div>
        );

      case 'checkbox':
        return (
          <div className="space-y-2">
            {filter.options?.map((option) => (
              <label key={option.value} className="flex items-center space-x-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={Array.isArray(value) ? value.includes(option.value) : false}
                  onChange={(e) => {
                    const currentValues = Array.isArray(value) ? value : [];
                    if (e.target.checked) {
                      onChange(filter.id, [...currentValues, option.value]);
                    } else {
                      onChange(filter.id, currentValues.filter((v: any) => v !== option.value));
                    }
                  }}
                  className="w-5 h-5 text-blue-500 bg-slate-700 border-slate-600 rounded focus:ring-blue-500"
                />
                <span className="text-white text-sm">{option.label}</span>
              </label>
            ))}
          </div>
        );

      case 'radio':
        return (
          <div className="space-y-2">
            {filter.options?.map((option) => (
              <label key={option.value} className="flex items-center space-x-3 cursor-pointer">
                <input
                  type="radio"
                  name={filter.id}
                  value={option.value}
                  checked={value === option.value}
                  onChange={(e) => onChange(filter.id, e.target.value)}
                  className="w-5 h-5 text-blue-500 bg-slate-700 border-slate-600 focus:ring-blue-500"
                />
                <span className="text-white text-sm">{option.label}</span>
              </label>
            ))}
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-50"
          onClick={onClose}
        />
      )}

      {/* Filter panel */}
      <div className={`fixed top-0 right-0 h-full w-80 max-w-[85vw] bg-slate-800 shadow-2xl transform transition-transform duration-300 z-50 ${
        isOpen ? 'translate-x-0' : 'translate-x-full'
      }`}>
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-700 bg-slate-900">
          <h2 className="text-lg font-bold text-white">필터</h2>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-full bg-slate-700 hover:bg-slate-600 transition-colors"
            aria-label="필터 닫기"
          >
            <span className="text-white text-lg">×</span>
          </button>
        </div>

        {/* Filter content */}
        <div className="flex-1 overflow-y-auto p-4">
          <div className="space-y-6">
            {filters.map((filter) => (
              <div key={filter.id}>
                <label className="block text-white font-medium mb-3">
                  {filter.label}
                </label>
                {renderFilter(filter)}
              </div>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="p-4 border-t border-slate-700 bg-slate-900">
          <div className="flex space-x-3">
            <button
              onClick={onReset}
              className="flex-1 py-3 px-4 bg-slate-700 hover:bg-slate-600 text-white rounded-lg font-medium transition-colors"
            >
              초기화
            </button>
            <button
              onClick={() => {
                onApply();
                onClose();
              }}
              className="flex-1 py-3 px-4 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition-colors"
            >
              적용
            </button>
          </div>
        </div>
      </div>
    </>
  );
};