'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { MiniChart } from '../Charts/MobileChart';

interface PortfolioHolding {
  symbol: string;
  name: string;
  shares: number;
  avgPrice: number;
  currentPrice: number;
  totalValue: number;
  dailyChange: number;
  dailyChangePercent: number;
  totalReturn: number;
  totalReturnPercent: number;
  chartData: number[];
  weight: number;
}

interface PortfolioMetrics {
  totalValue: number;
  totalInvested: number;
  totalReturn: number;
  totalReturnPercent: number;
  dailyChange: number;
  dailyChangePercent: number;
  diversification: number;
  riskScore: number;
  holdings: PortfolioHolding[];
}

interface MobilePortfolioProps {
  userId?: string;
}

/**
 * Mobile-optimized portfolio view with performance metrics
 * 모바일 최적화된 포트폴리오 뷰 (성능 지표 포함)
 */
export const MobilePortfolio: React.FC<MobilePortfolioProps> = ({ userId }) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'holdings' | 'performance'>('overview');
  const [sortBy, setSortBy] = useState<'value' | 'return' | 'change'>('value');
  const [filterType, setFilterType] = useState<'all' | 'winners' | 'losers'>('all');

  // Mock portfolio data
  const portfolioMetrics: PortfolioMetrics = {
    totalValue: 125430.50,
    totalInvested: 108500.00,
    totalReturn: 16930.50,
    totalReturnPercent: 15.6,
    dailyChange: 2341.25,
    dailyChangePercent: 1.9,
    diversification: 0.75,
    riskScore: 6.2,
    holdings: [
      {
        symbol: 'AAPL',
        name: 'Apple Inc.',
        shares: 50,
        avgPrice: 165.32,
        currentPrice: 175.43,
        totalValue: 8771.50,
        dailyChange: 107.50,
        dailyChangePercent: 1.24,
        totalReturn: 505.50,
        totalReturnPercent: 6.12,
        chartData: [165, 168, 172, 169, 175, 178, 175],
        weight: 0.07
      },
      {
        symbol: 'TSLA',
        name: 'Tesla, Inc.',
        shares: 25,
        avgPrice: 238.45,
        currentPrice: 248.87,
        totalValue: 6221.75,
        dailyChange: -208.00,
        dailyChangePercent: -3.24,
        totalReturn: 260.50,
        totalReturnPercent: 4.37,
        chartData: [240, 245, 250, 255, 252, 248, 249],
        weight: 0.05
      },
      {
        symbol: 'MSFT',
        name: 'Microsoft Corporation',
        shares: 30,
        avgPrice: 348.22,
        currentPrice: 378.85,
        totalValue: 11365.50,
        dailyChange: 170.10,
        dailyChangePercent: 1.52,
        totalReturn: 918.90,
        totalReturnPercent: 8.79,
        chartData: [350, 355, 368, 375, 380, 378, 379],
        weight: 0.09
      },
      {
        symbol: 'GOOGL',
        name: 'Alphabet Inc.',
        shares: 15,
        avgPrice: 138.67,
        currentPrice: 142.56,
        totalValue: 2138.40,
        dailyChange: -18.45,
        dailyChangePercent: -0.86,
        totalReturn: 58.35,
        totalReturnPercent: 2.81,
        chartData: [140, 142, 145, 143, 141, 142, 143],
        weight: 0.017
      },
      {
        symbol: 'NVDA',
        name: 'NVIDIA Corporation',
        shares: 8,
        avgPrice: 421.33,
        currentPrice: 498.21,
        totalValue: 3985.68,
        dailyChange: 79.68,
        dailyChangePercent: 2.04,
        totalReturn: 615.04,
        totalReturnPercent: 18.27,
        chartData: [420, 445, 465, 480, 495, 498, 498],
        weight: 0.032
      }
    ]
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount * 1300); // Convert to KRW for display
  };

  const formatUSD = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const getFilteredHoldings = () => {
    let filtered = [...portfolioMetrics.holdings];

    // Apply filter
    switch (filterType) {
      case 'winners':
        filtered = filtered.filter(h => h.totalReturnPercent > 0);
        break;
      case 'losers':
        filtered = filtered.filter(h => h.totalReturnPercent < 0);
        break;
    }

    // Apply sort
    switch (sortBy) {
      case 'value':
        filtered.sort((a, b) => b.totalValue - a.totalValue);
        break;
      case 'return':
        filtered.sort((a, b) => b.totalReturnPercent - a.totalReturnPercent);
        break;
      case 'change':
        filtered.sort((a, b) => b.dailyChangePercent - a.dailyChangePercent);
        break;
    }

    return filtered;
  };

  const getRiskLevel = (score: number): { label: string; color: string } => {
    if (score <= 3) return { label: '낮음', color: 'text-green-400' };
    if (score <= 7) return { label: '보통', color: 'text-yellow-400' };
    return { label: '높음', color: 'text-red-400' };
  };

  const getDiversificationLevel = (score: number): { label: string; color: string } => {
    if (score >= 0.8) return { label: '우수', color: 'text-green-400' };
    if (score >= 0.6) return { label: '양호', color: 'text-blue-400' };
    return { label: '개선 필요', color: 'text-yellow-400' };
  };

  return (
    <div className="bg-slate-900 min-h-screen pb-20">
      {/* Header */}
      <div className="bg-gradient-to-r from-slate-800 to-slate-700 p-4 pt-12 sticky top-0 z-30">
        <h1 className="text-xl font-bold text-white mb-4">내 포트폴리오 📊</h1>

        {/* Portfolio summary */}
        <div className="bg-slate-700 rounded-lg p-4 mb-4">
          <div className="text-center mb-4">
            <div className="text-3xl font-bold text-white mono-font">
              {formatCurrency(portfolioMetrics.totalValue)}
            </div>
            <div className={`text-lg font-medium ${
              portfolioMetrics.totalReturnPercent >= 0 ? 'text-green-400' : 'text-red-400'
            }`}>
              {portfolioMetrics.totalReturnPercent >= 0 ? '+' : ''}{formatCurrency(portfolioMetrics.totalReturn)}
              ({portfolioMetrics.totalReturnPercent >= 0 ? '+' : ''}{portfolioMetrics.totalReturnPercent.toFixed(2)}%)
            </div>
            <div className="text-gray-300 text-sm">총 수익률</div>
          </div>

          <div className="grid grid-cols-2 gap-4 text-center">
            <div>
              <div className={`text-lg font-bold ${
                portfolioMetrics.dailyChange >= 0 ? 'text-green-400' : 'text-red-400'
              }`}>
                {portfolioMetrics.dailyChange >= 0 ? '+' : ''}{formatCurrency(portfolioMetrics.dailyChange)}
              </div>
              <div className="text-xs text-gray-400">오늘 수익</div>
            </div>
            <div>
              <div className="text-lg font-bold text-blue-400">
                {portfolioMetrics.holdings.length}개
              </div>
              <div className="text-xs text-gray-400">보유 종목</div>
            </div>
          </div>
        </div>

        {/* Tab navigation */}
        <div className="flex bg-slate-700 rounded-lg p-1">
          {[
            { key: 'overview', label: '개요', icon: '📋' },
            { key: 'holdings', label: '보유종목', icon: '📈' },
            { key: 'performance', label: '성과', icon: '🎯' }
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as typeof activeTab)}
              className={`flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all duration-200 ${
                activeTab === tab.key
                  ? 'bg-blue-500 text-white shadow-md'
                  : 'text-gray-300 hover:text-white'
              }`}
            >
              <span className="mr-1">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        {/* Overview Tab */}
        {activeTab === 'overview' && (
          <div>
            {/* Key metrics */}
            <div className="grid grid-cols-2 gap-3 mb-6">
              <div className="bg-slate-800 rounded-lg p-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-white mono-font">
                    {formatCurrency(portfolioMetrics.totalInvested)}
                  </div>
                  <div className="text-gray-400 text-sm mt-1">총 투자금액</div>
                </div>
              </div>
              <div className="bg-slate-800 rounded-lg p-4">
                <div className="text-center">
                  <div className={`text-2xl font-bold mono-font ${
                    portfolioMetrics.dailyChangePercent >= 0 ? 'text-green-400' : 'text-red-400'
                  }`}>
                    {portfolioMetrics.dailyChangePercent >= 0 ? '+' : ''}{portfolioMetrics.dailyChangePercent.toFixed(2)}%
                  </div>
                  <div className="text-gray-400 text-sm mt-1">일일 수익률</div>
                </div>
              </div>
            </div>

            {/* Risk and diversification */}
            <div className="bg-slate-800 rounded-lg p-4 mb-6">
              <h3 className="text-white font-semibold mb-4">위험도 및 분산투자</h3>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-gray-300">위험도</span>
                  <div className="flex items-center space-x-2">
                    <div className="w-24 h-2 bg-slate-700 rounded-full">
                      <div 
                        className="h-2 bg-gradient-to-r from-green-500 via-yellow-500 to-red-500 rounded-full"
                        style={{ width: `${(portfolioMetrics.riskScore / 10) * 100}%` }}
                      />
                    </div>
                    <span className={`text-sm font-medium ${getRiskLevel(portfolioMetrics.riskScore).color}`}>
                      {getRiskLevel(portfolioMetrics.riskScore).label}
                    </span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-300">분산투자</span>
                  <div className="flex items-center space-x-2">
                    <div className="w-24 h-2 bg-slate-700 rounded-full">
                      <div 
                        className="h-2 bg-blue-500 rounded-full"
                        style={{ width: `${portfolioMetrics.diversification * 100}%` }}
                      />
                    </div>
                    <span className={`text-sm font-medium ${getDiversificationLevel(portfolioMetrics.diversification).color}`}>
                      {getDiversificationLevel(portfolioMetrics.diversification).label}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Top performers */}
            <div className="bg-slate-800 rounded-lg p-4">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-white font-semibold">상위 보유종목</h3>
                <Link href="/portfolio/holdings" className="text-blue-400 text-sm hover:text-blue-300">
                  전체보기
                </Link>
              </div>
              <div className="space-y-3">
                {portfolioMetrics.holdings.slice(0, 3).map((holding) => (
                  <div key={holding.symbol} className="flex items-center space-x-3">
                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="text-white font-medium">{holding.symbol}</div>
                          <div className="text-gray-400 text-sm">{holding.shares}주</div>
                        </div>
                        <div className="text-right">
                          <div className="text-white font-medium mono-font">
                            {formatUSD(holding.totalValue)}
                          </div>
                          <div className={`text-sm ${
                            holding.totalReturnPercent >= 0 ? 'text-green-400' : 'text-red-400'
                          }`}>
                            {holding.totalReturnPercent >= 0 ? '+' : ''}{holding.totalReturnPercent.toFixed(2)}%
                          </div>
                        </div>
                      </div>
                    </div>
                    <MiniChart 
                      data={holding.chartData}
                      color={holding.totalReturnPercent >= 0 ? 'green' : 'red'}
                      width={50}
                      height={25}
                    />
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Holdings Tab */}
        {activeTab === 'holdings' && (
          <div>
            {/* Filter and sort controls */}
            <div className="flex space-x-2 mb-4">
              <div className="flex-1">
                <select
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value as typeof filterType)}
                  className="w-full bg-slate-800 text-white rounded-lg px-3 py-2 text-sm border border-slate-600"
                >
                  <option value="all">전체</option>
                  <option value="winners">수익 종목</option>
                  <option value="losers">손실 종목</option>
                </select>
              </div>
              <div className="flex-1">
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value as typeof sortBy)}
                  className="w-full bg-slate-800 text-white rounded-lg px-3 py-2 text-sm border border-slate-600"
                >
                  <option value="value">평가금액순</option>
                  <option value="return">수익률순</option>
                  <option value="change">일일변동순</option>
                </select>
              </div>
            </div>

            {/* Holdings list */}
            <div className="space-y-3">
              {getFilteredHoldings().map((holding) => (
                <div key={holding.symbol} className="bg-slate-800 rounded-lg p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <div className="text-white font-bold text-lg">{holding.symbol}</div>
                      <div className="text-gray-400 text-sm truncate max-w-40">{holding.name}</div>
                      <div className="text-gray-500 text-xs mt-1">
                        {holding.shares}주 × {formatUSD(holding.currentPrice)}
                      </div>
                    </div>
                    <MiniChart 
                      data={holding.chartData}
                      color={holding.dailyChangePercent >= 0 ? 'green' : 'red'}
                      width={60}
                      height={30}
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4 mb-3">
                    <div>
                      <div className="text-gray-400 text-xs">평가금액</div>
                      <div className="text-white font-bold mono-font">
                        {formatUSD(holding.totalValue)}
                      </div>
                    </div>
                    <div>
                      <div className="text-gray-400 text-xs">일일변동</div>
                      <div className={`font-bold mono-font ${
                        holding.dailyChangePercent >= 0 ? 'text-green-400' : 'text-red-400'
                      }`}>
                        {holding.dailyChangePercent >= 0 ? '+' : ''}{holding.dailyChangePercent.toFixed(2)}%
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between border-t border-slate-700 pt-3">
                    <div>
                      <div className="text-gray-400 text-xs">총 수익</div>
                      <div className={`font-bold ${
                        holding.totalReturnPercent >= 0 ? 'text-green-400' : 'text-red-400'
                      }`}>
                        {holding.totalReturnPercent >= 0 ? '+' : ''}{formatUSD(holding.totalReturn)}
                        ({holding.totalReturnPercent >= 0 ? '+' : ''}{holding.totalReturnPercent.toFixed(2)}%)
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-gray-400 text-xs">비중</div>
                      <div className="text-white font-medium">
                        {(holding.weight * 100).toFixed(1)}%
                      </div>
                    </div>
                  </div>

                  {/* Action buttons */}
                  <div className="flex space-x-2 mt-3">
                    <Link
                      href={`/trading/${holding.symbol}`}
                      className="flex-1 bg-blue-500 hover:bg-blue-600 py-2 px-4 rounded-lg text-white text-center text-sm font-medium transition-colors"
                    >
                      거래
                    </Link>
                    <Link
                      href={`/portfolio/holding/${holding.symbol}`}
                      className="flex-1 bg-slate-700 hover:bg-slate-600 py-2 px-4 rounded-lg text-white text-center text-sm font-medium transition-colors"
                    >
                      상세
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Performance Tab */}
        {activeTab === 'performance' && (
          <div>
            {/* Performance summary */}
            <div className="bg-slate-800 rounded-lg p-4 mb-6">
              <h3 className="text-white font-semibold mb-4">성과 요약</h3>
              <div className="grid grid-cols-1 gap-4">
                <div className="text-center p-4 bg-slate-700 rounded-lg">
                  <div className={`text-3xl font-bold mono-font ${
                    portfolioMetrics.totalReturnPercent >= 0 ? 'text-green-400' : 'text-red-400'
                  }`}>
                    {portfolioMetrics.totalReturnPercent >= 0 ? '+' : ''}{portfolioMetrics.totalReturnPercent.toFixed(2)}%
                  </div>
                  <div className="text-gray-400 text-sm mt-1">전체 수익률</div>
                </div>
                
                <div className="grid grid-cols-2 gap-3">
                  <div className="text-center p-3 bg-slate-700 rounded-lg">
                    <div className="text-xl font-bold text-white mono-font">
                      {formatUSD(portfolioMetrics.totalReturn)}
                    </div>
                    <div className="text-gray-400 text-xs mt-1">총 수익금액</div>
                  </div>
                  <div className="text-center p-3 bg-slate-700 rounded-lg">
                    <div className={`text-xl font-bold mono-font ${
                      portfolioMetrics.dailyChangePercent >= 0 ? 'text-green-400' : 'text-red-400'
                    }`}>
                      {portfolioMetrics.dailyChangePercent >= 0 ? '+' : ''}{portfolioMetrics.dailyChangePercent.toFixed(2)}%
                    </div>
                    <div className="text-gray-400 text-xs mt-1">일일 수익률</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Top performers */}
            <div className="bg-slate-800 rounded-lg p-4 mb-6">
              <h3 className="text-white font-semibold mb-4">최고 수익 종목 🏆</h3>
              <div className="space-y-3">
                {portfolioMetrics.holdings
                  .sort((a, b) => b.totalReturnPercent - a.totalReturnPercent)
                  .slice(0, 3)
                  .map((holding, index) => (
                  <div key={holding.symbol} className="flex items-center space-x-3">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${
                      index === 0 ? 'bg-yellow-500 text-black' :
                      index === 1 ? 'bg-gray-400 text-black' :
                      'bg-amber-600 text-white'
                    }`}>
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <div className="text-white font-medium">{holding.symbol}</div>
                      <div className="text-gray-400 text-sm">{formatUSD(holding.totalValue)}</div>
                    </div>
                    <div className="text-right">
                      <div className="text-green-400 font-bold">
                        +{holding.totalReturnPercent.toFixed(2)}%
                      </div>
                      <div className="text-green-400 text-sm">
                        +{formatUSD(holding.totalReturn)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Performance metrics */}
            <div className="bg-slate-800 rounded-lg p-4">
              <h3 className="text-white font-semibold mb-4">상세 지표</h3>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">총 투자 원금</span>
                  <span className="text-white font-medium mono-font">
                    {formatUSD(portfolioMetrics.totalInvested)}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">현재 평가 금액</span>
                  <span className="text-white font-medium mono-font">
                    {formatUSD(portfolioMetrics.totalValue)}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">실현 수익</span>
                  <span className="text-green-400 font-medium mono-font">
                    {formatUSD(portfolioMetrics.totalReturn)}
                  </span>
                </div>
                <div className="border-t border-slate-700 pt-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-300">포트폴리오 위험도</span>
                    <span className={`font-medium ${getRiskLevel(portfolioMetrics.riskScore).color}`}>
                      {portfolioMetrics.riskScore}/10 ({getRiskLevel(portfolioMetrics.riskScore).label})
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="fixed bottom-20 left-4 right-4 bg-slate-800 rounded-lg border border-slate-600 p-3 lg:hidden">
        <div className="grid grid-cols-3 gap-2">
          <Link
            href="/trading"
            className="bg-blue-500 hover:bg-blue-600 py-2 px-3 rounded-lg text-white text-center text-sm font-medium transition-colors"
          >
            📈 거래
          </Link>
          <Link
            href="/portfolio/rebalance"
            className="bg-purple-500 hover:bg-purple-600 py-2 px-3 rounded-lg text-white text-center text-sm font-medium transition-colors"
          >
            ⚖️ 리밸런싱
          </Link>
          <Link
            href="/portfolio/analysis"
            className="bg-green-500 hover:bg-green-600 py-2 px-3 rounded-lg text-white text-center text-sm font-medium transition-colors"
          >
            📊 분석
          </Link>
        </div>
      </div>
    </div>
  );
};