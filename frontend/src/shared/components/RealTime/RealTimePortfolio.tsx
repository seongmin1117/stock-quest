'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { usePortfolioUpdates, useWebSocketConnection } from '../../hooks/useWebSocket';
import { PortfolioUpdate, PortfolioPosition } from '../../services/websocket/WebSocketService';

interface RealTimePortfolioProps {
  userId?: string;
  onPositionClick?: (symbol: string) => void;
  showDetails?: boolean;
  compact?: boolean;
}

/**
 * Real-time portfolio component with live updates
 * 실시간 업데이트가 있는 포트폴리오 컴포넌트
 */
export const RealTimePortfolio: React.FC<RealTimePortfolioProps> = ({
  userId,
  onPositionClick,
  showDetails = true,
  compact = false
}) => {
  const { portfolioData, lastUpdate, isLoading } = usePortfolioUpdates();
  const { connected, connecting } = useWebSocketConnection();
  const [animatingPositions, setAnimatingPositions] = useState<Set<string>>(new Set());
  const [previousPortfolioData, setPreviousPortfolioData] = useState<PortfolioUpdate | null>(null);

  // Detect changes and animate
  useEffect(() => {
    if (portfolioData && previousPortfolioData) {
      const newAnimatingPositions = new Set<string>();
      
      // Check for overall portfolio value changes
      if (portfolioData.totalValue !== previousPortfolioData.totalValue) {
        newAnimatingPositions.add('total-value');
      }

      // Check for position changes
      portfolioData.positions.forEach((position) => {
        const previousPosition = previousPortfolioData.positions.find(
          p => p.symbol === position.symbol
        );
        
        if (!previousPosition || 
            position.currentPrice !== previousPosition.currentPrice ||
            position.totalValue !== previousPosition.totalValue) {
          newAnimatingPositions.add(position.symbol);
        }
      });

      if (newAnimatingPositions.size > 0) {
        setAnimatingPositions(newAnimatingPositions);
        
        // Clear animations after 2 seconds
        const timeout = setTimeout(() => {
          setAnimatingPositions(new Set());
        }, 2000);

        return () => clearTimeout(timeout);
      }
    }
    
    if (portfolioData) {
      setPreviousPortfolioData(portfolioData);
    }
  }, [portfolioData, previousPortfolioData]);

  // Calculate portfolio metrics
  const portfolioMetrics = useMemo(() => {
    if (!portfolioData) return null;

    const sortedPositions = [...portfolioData.positions].sort(
      (a, b) => b.totalValue - a.totalValue
    );

    const gainers = sortedPositions.filter(p => p.unrealizedGainLossPercent > 0);
    const losers = sortedPositions.filter(p => p.unrealizedGainLossPercent < 0);
    const unchanged = sortedPositions.filter(p => p.unrealizedGainLossPercent === 0);

    const totalUnrealizedGainLoss = sortedPositions.reduce(
      (sum, p) => sum + p.unrealizedGainLoss, 0
    );

    const averageReturn = sortedPositions.length > 0
      ? sortedPositions.reduce((sum, p) => sum + p.unrealizedGainLossPercent, 0) / sortedPositions.length
      : 0;

    return {
      sortedPositions,
      gainers: gainers.length,
      losers: losers.length,
      unchanged: unchanged.length,
      totalUnrealizedGainLoss,
      averageReturn,
      topGainer: gainers.length > 0 ? gainers[0] : null,
      topLoser: losers.length > 0 ? losers.sort((a, b) => a.unrealizedGainLossPercent - b.unrealizedGainLossPercent)[0] : null
    };
  }, [portfolioData]);

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

  const formatPercent = (percent: number) => {
    return `${percent >= 0 ? '+' : ''}${percent.toFixed(2)}%`;
  };

  const getChangeColor = (changePercent: number) => {
    if (changePercent > 0) return 'text-green-400';
    if (changePercent < 0) return 'text-red-400';
    return 'text-gray-400';
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  if (isLoading && !portfolioData) {
    return (
      <div className="bg-slate-800 rounded-lg p-6">
        <div className="flex items-center justify-center space-x-2 text-gray-400">
          <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <span>실시간 포트폴리오 로딩 중...</span>
        </div>
      </div>
    );
  }

  if (!portfolioData || !portfolioMetrics) {
    return (
      <div className="bg-slate-800 rounded-lg p-8 text-center text-gray-400">
        <div className="text-4xl mb-4">💼</div>
        <div className="text-lg mb-2">포트폴리오 데이터 없음</div>
        <div className="text-sm">
          실시간 포트폴리오 데이터를 기다리는 중입니다...
        </div>
      </div>
    );
  }

  return (
    <div className="bg-slate-800 rounded-lg overflow-hidden">
      {/* Header with connection status */}
      <div className="p-4 border-b border-slate-700 bg-slate-900">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-white">실시간 포트폴리오</h3>
          <div className="flex items-center space-x-2">
            <div className={`w-2 h-2 rounded-full ${
              connected ? 'bg-green-400' : connecting ? 'bg-yellow-400' : 'bg-red-400'
            }`} />
            <span className="text-sm text-gray-400">
              {connected ? 'Live' : connecting ? '연결 중...' : '연결 끊김'}
            </span>
          </div>
        </div>
        {lastUpdate > 0 && (
          <div className="text-xs text-gray-400 mt-1">
            마지막 업데이트: {formatTime(lastUpdate)}
          </div>
        )}
      </div>

      {/* Portfolio summary */}
      <div className={`p-4 bg-slate-700 transition-all duration-500 ${
        animatingPositions.has('total-value') 
          ? portfolioData.dailyChangePercent >= 0 
            ? 'bg-green-500 bg-opacity-10' 
            : 'bg-red-500 bg-opacity-10'
          : ''
      }`}>
        <div className="text-center mb-4">
          <div className="text-3xl font-bold text-white mono-font mb-2">
            {formatCurrency(portfolioData.totalValue)}
          </div>
          <div className={`text-lg font-medium ${getChangeColor(portfolioData.dailyChangePercent)}`}>
            {formatPercent(portfolioData.dailyChangePercent)} ({formatCurrency(portfolioData.dailyChange)})
          </div>
          <div className="text-gray-400 text-sm">일일 수익률</div>
        </div>

        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-green-400 font-bold text-lg">{portfolioMetrics.gainers}</div>
            <div className="text-gray-400 text-xs">상승</div>
          </div>
          <div>
            <div className="text-red-400 font-bold text-lg">{portfolioMetrics.losers}</div>
            <div className="text-gray-400 text-xs">하락</div>
          </div>
          <div>
            <div className="text-gray-400 font-bold text-lg">{portfolioMetrics.unchanged}</div>
            <div className="text-gray-400 text-xs">보합</div>
          </div>
        </div>
      </div>

      {/* Quick stats */}
      {showDetails && (
        <div className="p-4 bg-slate-700 border-t border-slate-600">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <div className="text-gray-400 text-sm mb-1">평균 수익률</div>
              <div className={`font-bold mono-font ${getChangeColor(portfolioMetrics.averageReturn)}`}>
                {formatPercent(portfolioMetrics.averageReturn)}
              </div>
            </div>
            <div>
              <div className="text-gray-400 text-sm mb-1">미실현 손익</div>
              <div className={`font-bold mono-font ${getChangeColor(portfolioMetrics.totalUnrealizedGainLoss)}`}>
                {formatUSD(portfolioMetrics.totalUnrealizedGainLoss)}
              </div>
            </div>
          </div>

          {/* Top gainer/loser */}
          {(portfolioMetrics.topGainer || portfolioMetrics.topLoser) && (
            <div className="mt-4 pt-4 border-t border-slate-600">
              <div className="grid grid-cols-2 gap-4 text-sm">
                {portfolioMetrics.topGainer && (
                  <div>
                    <div className="text-gray-400 mb-1">최고 상승</div>
                    <div className="text-green-400 font-medium">
                      {portfolioMetrics.topGainer.symbol} +{portfolioMetrics.topGainer.unrealizedGainLossPercent.toFixed(2)}%
                    </div>
                  </div>
                )}
                {portfolioMetrics.topLoser && (
                  <div>
                    <div className="text-gray-400 mb-1">최대 하락</div>
                    <div className="text-red-400 font-medium">
                      {portfolioMetrics.topLoser.symbol} {portfolioMetrics.topLoser.unrealizedGainLossPercent.toFixed(2)}%
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Positions list */}
      <div className={compact ? "max-h-80 overflow-y-auto" : ""}>
        {portfolioMetrics.sortedPositions.map((position) => (
          <div
            key={position.symbol}
            onClick={() => onPositionClick?.(position.symbol)}
            className={`p-4 border-b border-slate-700 transition-all duration-500 ${
              onPositionClick ? 'cursor-pointer hover:bg-slate-700' : ''
            } ${
              animatingPositions.has(position.symbol) 
                ? position.unrealizedGainLossPercent >= 0 
                  ? 'bg-green-500 bg-opacity-10' 
                  : 'bg-red-500 bg-opacity-10'
                : ''
            }`}
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-2 mb-1">
                  <div className="text-white font-bold text-lg">{position.symbol}</div>
                  <div className="text-gray-400 text-sm">
                    {position.shares}주
                  </div>
                </div>
                <div className="text-gray-400 text-sm">
                  평균 단가: {formatUSD(position.averagePrice)}
                </div>
              </div>
              
              <div className="text-right">
                <div className="text-white font-bold text-lg mono-font mb-1">
                  {formatUSD(position.totalValue)}
                </div>
                <div className="text-gray-400 text-sm mono-font mb-1">
                  @{formatUSD(position.currentPrice)}
                </div>
                <div className={`font-medium mono-font ${getChangeColor(position.unrealizedGainLossPercent)}`}>
                  {formatPercent(position.unrealizedGainLossPercent)}
                  <div className="text-xs">
                    ({position.unrealizedGainLoss >= 0 ? '+' : ''}{formatUSD(position.unrealizedGainLoss)})
                  </div>
                </div>
              </div>
            </div>

            {showDetails && (
              <div className="mt-3 pt-3 border-t border-slate-600">
                <div className="grid grid-cols-3 gap-2 text-xs text-gray-400">
                  <div>
                    <span className="block">투자금액</span>
                    <span className="text-white mono-font">
                      {formatUSD(position.averagePrice * position.shares)}
                    </span>
                  </div>
                  <div>
                    <span className="block">현재 가치</span>
                    <span className="text-white mono-font">
                      {formatUSD(position.totalValue)}
                    </span>
                  </div>
                  <div>
                    <span className="block">수익률</span>
                    <span className={`mono-font ${getChangeColor(position.unrealizedGainLossPercent)}`}>
                      {formatPercent(position.unrealizedGainLossPercent)}
                    </span>
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Footer */}
      <div className="p-3 bg-slate-900 text-xs text-gray-400">
        <div className="flex justify-between items-center">
          <span>총 {portfolioData.positions.length}개 종목 보유</span>
          <span>실시간 업데이트 중</span>
        </div>
      </div>
    </div>
  );
};

/**
 * Compact portfolio performance widget
 * 간소화된 포트폴리오 성과 위젯
 */
export const RealTimePortfolioWidget: React.FC<{
  userId?: string;
  onClick?: () => void;
}> = ({ userId, onClick }) => {
  const { portfolioData, lastUpdate, isLoading } = usePortfolioUpdates();
  const { connected } = useWebSocketConnection();

  if (isLoading && !portfolioData) {
    return (
      <div className="bg-slate-800 rounded-lg p-4">
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 border border-gray-400 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-gray-400 font-bold">포트폴리오 로딩...</span>
        </div>
      </div>
    );
  }

  if (!portfolioData) {
    return (
      <div className="bg-slate-800 rounded-lg p-4 text-gray-400">
        <div className="text-sm">포트폴리오 데이터 없음</div>
      </div>
    );
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount * 1300);
  };

  const getChangeColor = (changePercent: number) => {
    if (changePercent > 0) return 'text-green-400';
    if (changePercent < 0) return 'text-red-400';
    return 'text-gray-400';
  };

  return (
    <div
      onClick={onClick}
      className={`bg-slate-800 rounded-lg p-4 transition-all duration-200 ${
        onClick ? 'cursor-pointer hover:bg-slate-700' : ''
      }`}
    >
      <div className="flex items-center justify-between mb-2">
        <div className="text-white font-bold">내 포트폴리오</div>
        <div className={`w-2 h-2 rounded-full ${
          connected ? 'bg-green-400' : 'bg-red-400'
        }`} />
      </div>
      
      <div className="text-right">
        <div className="text-white font-bold text-xl mono-font">
          {formatCurrency(portfolioData.totalValue)}
        </div>
        <div className={`text-sm font-medium ${getChangeColor(portfolioData.dailyChangePercent)}`}>
          {portfolioData.dailyChangePercent >= 0 ? '▲' : '▼'} {portfolioData.dailyChangePercent >= 0 ? '+' : ''}{portfolioData.dailyChangePercent.toFixed(2)}%
        </div>
      </div>

      {lastUpdate > 0 && (
        <div className="text-xs text-gray-400 mt-2">
          {new Date(lastUpdate).toLocaleTimeString('ko-KR')} 업데이트
        </div>
      )}
    </div>
  );
};