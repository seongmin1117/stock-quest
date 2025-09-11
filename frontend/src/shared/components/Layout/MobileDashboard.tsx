'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';

interface MobileDashboardProps {
  user?: {
    name: string;
    portfolio: {
      totalValue: number;
      dailyChange: number;
      dailyChangePercent: number;
    };
    stats: {
      rank: number;
      winRate: number;
      completedChallenges: number;
    };
  };
}

/**
 * Mobile-optimized dashboard with touch-friendly design
 * 모바일 최적화된 대시보드 레이아웃
 */
export const MobileDashboard: React.FC<MobileDashboardProps> = ({ user }) => {
  const [activeTab, setActiveTab] = useState<'portfolio' | 'challenges' | 'stats'>('portfolio');

  // Mock data for development
  const mockUser = user || {
    name: '트레이더',
    portfolio: {
      totalValue: 1250000,
      dailyChange: 45000,
      dailyChangePercent: 3.73
    },
    stats: {
      rank: 47,
      winRate: 68.5,
      completedChallenges: 12
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  const quickActions = [
    { href: '/trading', label: '거래하기', icon: '📈', color: 'bg-blue-500' },
    { href: '/challenges', label: '챌린지', icon: '🎯', color: 'bg-green-500' },
    { href: '/portfolio', label: '포트폴리오', icon: '💼', color: 'bg-purple-500' },
    { href: '/leaderboard', label: '랭킹', icon: '🏆', color: 'bg-yellow-500' },
  ];

  const recentActivities = [
    { type: 'trade', symbol: 'AAPL', action: 'BUY', amount: '10주', time: '2시간 전', profit: '+₩15,000' },
    { type: 'challenge', name: 'Weekly Bulls', status: 'completed', time: '1일 전', profit: '+₩8,500' },
    { type: 'trade', symbol: 'TSLA', action: 'SELL', amount: '5주', time: '2일 전', profit: '-₩3,200' },
  ];

  return (
    <div className="min-h-screen bg-slate-900 pb-20">
      {/* Header with user greeting */}
      <div className="bg-gradient-to-r from-slate-800 to-slate-700 p-4 pt-12">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-xl font-bold text-white">
              안녕하세요, {mockUser.name}님! 👋
            </h1>
            <p className="text-sm text-gray-300">
              오늘도 성공적인 거래 되세요
            </p>
          </div>
          <div className="text-right">
            <div className="text-xs text-gray-300">현재 랭킹</div>
            <div className="text-lg font-bold text-yellow-400">#{mockUser.stats.rank}</div>
          </div>
        </div>

        {/* Portfolio summary card */}
        <div className="bg-slate-700 rounded-lg p-4 mb-4 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-xs text-gray-300 mb-1">총 자산</div>
              <div className="text-2xl font-bold text-white mono-font">
                {formatCurrency(mockUser.portfolio.totalValue)}
              </div>
            </div>
            <div className="text-right">
              <div className={`text-lg font-bold mono-font ${
                mockUser.portfolio.dailyChange >= 0 ? 'text-green-400' : 'text-red-400'
              }`}>
                {mockUser.portfolio.dailyChange >= 0 ? '+' : ''}{formatCurrency(mockUser.portfolio.dailyChange)}
              </div>
              <div className={`text-sm ${
                mockUser.portfolio.dailyChangePercent >= 0 ? 'text-green-400' : 'text-red-400'
              }`}>
                ({mockUser.portfolio.dailyChangePercent >= 0 ? '+' : ''}{mockUser.portfolio.dailyChangePercent.toFixed(2)}%)
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick actions grid */}
      <div className="p-4">
        <h2 className="text-lg font-semibold text-white mb-3">빠른 실행</h2>
        <div className="grid grid-cols-2 gap-3 mb-6">
          {quickActions.map((action) => (
            <Link
              key={action.href}
              href={action.href}
              className="block bg-slate-800 rounded-lg p-4 text-center transition-all duration-200 hover:bg-slate-700 active:scale-95"
            >
              <div className={`w-12 h-12 ${action.color} rounded-full flex items-center justify-center mx-auto mb-2`}>
                <span className="text-2xl">{action.icon}</span>
              </div>
              <span className="text-sm font-medium text-white">{action.label}</span>
            </Link>
          ))}
        </div>

        {/* Stats tabs */}
        <div className="bg-slate-800 rounded-lg overflow-hidden mb-6">
          <div className="flex border-b border-slate-700">
            {[
              { key: 'portfolio', label: '포트폴리오', icon: '💼' },
              { key: 'challenges', label: '챌린지', icon: '🎯' },
              { key: 'stats', label: '통계', icon: '📊' }
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key as typeof activeTab)}
                className={`flex-1 py-3 px-4 text-sm font-medium transition-colors ${
                  activeTab === tab.key
                    ? 'bg-blue-500 text-white'
                    : 'text-gray-300 hover:text-white hover:bg-slate-700'
                }`}
              >
                <span className="mr-1">{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </div>

          <div className="p-4">
            {activeTab === 'portfolio' && (
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">총 수익률</span>
                  <span className="text-green-400 font-bold">+15.2%</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">보유 종목</span>
                  <span className="text-white">8개</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">평균 수익률</span>
                  <span className="text-blue-400 font-bold">+8.7%</span>
                </div>
              </div>
            )}

            {activeTab === 'challenges' && (
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">완료한 챌린지</span>
                  <span className="text-white font-bold">{mockUser.stats.completedChallenges}개</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">진행 중</span>
                  <span className="text-yellow-400 font-bold">3개</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">성공률</span>
                  <span className="text-green-400 font-bold">{mockUser.stats.winRate}%</span>
                </div>
              </div>
            )}

            {activeTab === 'stats' && (
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">전체 랭킹</span>
                  <span className="text-yellow-400 font-bold">#{mockUser.stats.rank}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">승률</span>
                  <span className="text-green-400 font-bold">{mockUser.stats.winRate}%</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">거래 횟수</span>
                  <span className="text-white font-bold">156회</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Recent activities */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-semibold text-white">최근 활동</h2>
            <Link href="/history" className="text-sm text-blue-400 hover:text-blue-300">
              전체보기
            </Link>
          </div>
          <div className="space-y-3">
            {recentActivities.map((activity, index) => (
              <div
                key={index}
                className="bg-slate-800 rounded-lg p-3 flex items-center justify-between"
              >
                <div className="flex items-center space-x-3">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                    activity.type === 'trade' ? 'bg-blue-500' : 'bg-green-500'
                  }`}>
                    <span className="text-xs text-white">
                      {activity.type === 'trade' ? '📈' : '🎯'}
                    </span>
                  </div>
                  <div>
                    <div className="text-sm text-white font-medium">
                      {activity.type === 'trade' 
                        ? `${activity.symbol} ${activity.action} ${activity.amount}`
                        : activity.name
                      }
                    </div>
                    <div className="text-xs text-gray-400">{activity.time}</div>
                  </div>
                </div>
                <div className={`text-sm font-bold ${
                  activity.profit.startsWith('+') ? 'text-green-400' : 'text-red-400'
                }`}>
                  {activity.profit}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

/**
 * Mobile dashboard floating action button
 * 모바일 대시보드 플로팅 액션 버튼
 */
export const MobileFAB: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);

  const actions = [
    { href: '/trading', label: '거래', icon: '📈', color: 'bg-blue-500' },
    { href: '/challenges/new', label: '챌린지', icon: '🎯', color: 'bg-green-500' },
    { href: '/portfolio/add', label: '종목 추가', icon: '➕', color: 'bg-purple-500' },
  ];

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* FAB Actions */}
      <div className={`fixed bottom-20 right-4 z-50 lg:hidden ${isOpen ? 'block' : 'hidden'}`}>
        <div className="space-y-3 mb-3">
          {actions.map((action, index) => (
            <Link
              key={action.href}
              href={action.href}
              className={`block ${action.color} w-12 h-12 rounded-full shadow-lg flex items-center justify-center text-white transform transition-all duration-200 ${
                isOpen ? 'scale-100 opacity-100' : 'scale-0 opacity-0'
              }`}
              style={{ transitionDelay: `${index * 50}ms` }}
              onClick={() => setIsOpen(false)}
            >
              <span className="text-lg">{action.icon}</span>
            </Link>
          ))}
        </div>
      </div>

      {/* Main FAB */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`fixed bottom-20 right-4 w-14 h-14 bg-blue-500 rounded-full shadow-lg flex items-center justify-center z-50 lg:hidden transition-transform duration-200 ${
          isOpen ? 'rotate-45' : 'rotate-0'
        }`}
        aria-label="빠른 실행 메뉴"
      >
        <span className="text-white text-2xl">+</span>
      </button>
    </>
  );
};