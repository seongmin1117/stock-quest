'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { useMarketData, useWebSocketConnection } from '../../hooks/useWebSocket';
import { MarketDataUpdate } from '../../services/websocket/WebSocketService';

interface RealTimeMarketDataProps {
  symbols: string[];
  onSymbolClick?: (symbol: string) => void;
  compact?: boolean;
  showChart?: boolean;
}

/**
 * Real-time market data component with live price updates
 * 실시간 가격 업데이트가 있는 시장 데이터 컴포넌트
 */
export const RealTimeMarketData: React.FC<RealTimeMarketDataProps> = ({
  symbols,
  onSymbolClick,
  compact = false,
  showChart = false
}) => {
  const { marketData, isLoading } = useMarketData(symbols);
  const { connected, connecting } = useWebSocketConnection();
  const [animatingSymbols, setAnimatingSymbols] = useState<Set<string>>(new Set());
  const [sortBy, setSortBy] = useState<'symbol' | 'price' | 'change' | 'volume'>('symbol');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // Animate price changes
  useEffect(() => {
    const newAnimatingSymbols = new Set<string>();
    
    Object.values(marketData).forEach((data) => {
      if (data && Date.now() - data.timestamp < 1000) { // Within last second
        newAnimatingSymbols.add(data.symbol);
      }
    });

    if (newAnimatingSymbols.size > 0) {
      setAnimatingSymbols(newAnimatingSymbols);
      
      // Clear animations after 2 seconds
      const timeout = setTimeout(() => {
        setAnimatingSymbols(new Set());
      }, 2000);

      return () => clearTimeout(timeout);
    }
  }, [marketData]);

  // Sort market data
  const sortedMarketData = useMemo(() => {
    const dataArray = Object.values(marketData).filter(Boolean) as MarketDataUpdate[];
    
    return dataArray.sort((a, b) => {
      let aValue: any, bValue: any;
      
      switch (sortBy) {
        case 'symbol':
          aValue = a.symbol;
          bValue = b.symbol;
          break;
        case 'price':
          aValue = a.price;
          bValue = b.price;
          break;
        case 'change':
          aValue = a.changePercent;
          bValue = b.changePercent;
          break;
        case 'volume':
          aValue = a.volume;
          bValue = b.volume;
          break;
        default:
          return 0;
      }

      if (typeof aValue === 'string' && typeof bValue === 'string') {
        return sortDirection === 'asc' 
          ? aValue.localeCompare(bValue)
          : bValue.localeCompare(aValue);
      }

      return sortDirection === 'asc' 
        ? aValue - bValue
        : bValue - aValue;
    });
  }, [marketData, sortBy, sortDirection]);

  const handleSort = (column: typeof sortBy) => {
    if (sortBy === column) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDirection('asc');
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const formatVolume = (volume: number) => {
    if (volume >= 1000000) {
      return (volume / 1000000).toFixed(1) + 'M';
    } else if (volume >= 1000) {
      return (volume / 1000).toFixed(1) + 'K';
    }
    return volume.toString();
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  const getChangeColor = (changePercent: number) => {
    if (changePercent > 0) return 'text-green-400';
    if (changePercent < 0) return 'text-red-400';
    return 'text-gray-400';
  };

  const getChangeIcon = (changePercent: number) => {
    if (changePercent > 0) return '▲';
    if (changePercent < 0) return '▼';
    return '━';
  };

  if (isLoading && sortedMarketData.length === 0) {
    return (
      <div className="bg-slate-800 rounded-lg p-6">
        <div className="flex items-center justify-center space-x-2 text-gray-400">
          <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <span>실시간 데이터 로딩 중...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-slate-800 rounded-lg overflow-hidden">
      {/* Header with connection status */}
      <div className="p-4 border-b border-slate-700 bg-slate-900">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-white">실시간 시장 데이터</h3>
          <div className="flex items-center space-x-2">
            <div className={`w-2 h-2 rounded-full ${
              connected ? 'bg-green-400' : connecting ? 'bg-yellow-400' : 'bg-red-400'
            }`} />
            <span className="text-sm text-gray-400">
              {connected ? 'Live' : connecting ? '연결 중...' : '연결 끊김'}
            </span>
            <span className="text-xs text-gray-500">
              {formatTime(Date.now())}
            </span>
          </div>
        </div>
        
        {sortedMarketData.length > 0 && (
          <div className="text-xs text-gray-400 mt-1">
            {sortedMarketData.length}개 종목 실시간 업데이트
          </div>
        )}
      </div>

      {/* Market data table */}
      {sortedMarketData.length > 0 ? (
        <div className="overflow-x-auto">
          {compact ? (
            // Compact view
            <div className="p-2 space-y-2 max-h-80 overflow-y-auto">
              {sortedMarketData.map((data) => (
                <div
                  key={data.symbol}
                  onClick={() => onSymbolClick?.(data.symbol)}
                  className={`p-3 bg-slate-700 rounded-lg transition-all duration-500 ${
                    onSymbolClick ? 'cursor-pointer hover:bg-slate-600' : ''
                  } ${
                    animatingSymbols.has(data.symbol) 
                      ? data.changePercent >= 0 
                        ? 'bg-green-500 bg-opacity-20 shadow-green-500/20 shadow-lg' 
                        : 'bg-red-500 bg-opacity-20 shadow-red-500/20 shadow-lg'
                      : ''
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-white font-bold">{data.symbol}</div>
                      <div className="text-xs text-gray-400">
                        Vol: {formatVolume(data.volume)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-white font-bold mono-font">
                        {formatCurrency(data.price)}
                      </div>
                      <div className={`text-xs font-medium ${getChangeColor(data.changePercent)}`}>
                        {getChangeIcon(data.changePercent)} {data.changePercent >= 0 ? '+' : ''}{data.changePercent.toFixed(2)}%
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            // Full table view
            <table className="w-full">
              <thead className="bg-slate-900 text-gray-300 text-sm">
                <tr>
                  <th 
                    className="text-left p-3 cursor-pointer hover:bg-slate-700 transition-colors"
                    onClick={() => handleSort('symbol')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>종목</span>
                      {sortBy === 'symbol' && (
                        <span className="text-blue-400">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="text-right p-3 cursor-pointer hover:bg-slate-700 transition-colors"
                    onClick={() => handleSort('price')}
                  >
                    <div className="flex items-center justify-end space-x-1">
                      <span>현재가</span>
                      {sortBy === 'price' && (
                        <span className="text-blue-400">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="text-right p-3 cursor-pointer hover:bg-slate-700 transition-colors"
                    onClick={() => handleSort('change')}
                  >
                    <div className="flex items-center justify-end space-x-1">
                      <span>변동</span>
                      {sortBy === 'change' && (
                        <span className="text-blue-400">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="text-right p-3 cursor-pointer hover:bg-slate-700 transition-colors"
                    onClick={() => handleSort('volume')}
                  >
                    <div className="flex items-center justify-end space-x-1">
                      <span>거래량</span>
                      {sortBy === 'volume' && (
                        <span className="text-blue-400">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th className="text-center p-3">시간</th>
                </tr>
              </thead>
              <tbody>
                {sortedMarketData.map((data) => (
                  <tr
                    key={data.symbol}
                    onClick={() => onSymbolClick?.(data.symbol)}
                    className={`border-b border-slate-700 transition-all duration-500 ${
                      onSymbolClick ? 'cursor-pointer hover:bg-slate-700' : ''
                    } ${
                      animatingSymbols.has(data.symbol) 
                        ? data.changePercent >= 0 
                          ? 'bg-green-500 bg-opacity-10' 
                          : 'bg-red-500 bg-opacity-10'
                        : ''
                    }`}
                  >
                    <td className="p-3">
                      <div className="text-white font-bold">{data.symbol}</div>
                      {data.bid && data.ask && (
                        <div className="text-xs text-gray-400">
                          Bid: {formatCurrency(data.bid)} | Ask: {formatCurrency(data.ask)}
                        </div>
                      )}
                    </td>
                    <td className="p-3 text-right">
                      <div className="text-white font-bold mono-font">
                        {formatCurrency(data.price)}
                      </div>
                      <div className={`text-sm mono-font ${getChangeColor(data.change)}`}>
                        {data.change >= 0 ? '+' : ''}{formatCurrency(data.change)}
                      </div>
                    </td>
                    <td className="p-3 text-right">
                      <div className={`font-bold mono-font ${getChangeColor(data.changePercent)}`}>
                        {getChangeIcon(data.changePercent)} {data.changePercent >= 0 ? '+' : ''}{data.changePercent.toFixed(2)}%
                      </div>
                      {data.high && data.low && (
                        <div className="text-xs text-gray-400 mono-font">
                          H: {formatCurrency(data.high)} L: {formatCurrency(data.low)}
                        </div>
                      )}
                    </td>
                    <td className="p-3 text-right">
                      <div className="text-white font-medium">
                        {formatVolume(data.volume)}
                      </div>
                    </td>
                    <td className="p-3 text-center">
                      <div className="text-xs text-gray-400 mono-font">
                        {formatTime(data.timestamp)}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ) : (
        <div className="p-8 text-center text-gray-400">
          <div className="text-4xl mb-4">📊</div>
          <div className="text-lg mb-2">데이터 없음</div>
          <div className="text-sm">
            실시간 시장 데이터를 기다리는 중입니다...
          </div>
        </div>
      )}

      {/* Footer with stats */}
      {sortedMarketData.length > 0 && (
        <div className="p-3 border-t border-slate-700 bg-slate-900">
          <div className="flex justify-between items-center text-xs text-gray-400">
            <div className="flex space-x-4">
              <span>
                상승: <span className="text-green-400 font-medium">
                  {sortedMarketData.filter(d => d.changePercent > 0).length}
                </span>
              </span>
              <span>
                하락: <span className="text-red-400 font-medium">
                  {sortedMarketData.filter(d => d.changePercent < 0).length}
                </span>
              </span>
              <span>
                보합: <span className="text-gray-400 font-medium">
                  {sortedMarketData.filter(d => d.changePercent === 0).length}
                </span>
              </span>
            </div>
            <div>
              마지막 업데이트: {formatTime(Math.max(...sortedMarketData.map(d => d.timestamp)))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

/**
 * Single stock real-time ticker component
 * 단일 주식 실시간 티커 컴포넌트
 */
export const RealTimeTicker: React.FC<{
  symbol: string;
  onClick?: () => void;
  showDetails?: boolean;
}> = ({ symbol, onClick, showDetails = false }) => {
  const { marketData } = useMarketData([symbol]);
  const data = marketData[symbol];
  const [isAnimating, setIsAnimating] = useState(false);

  useEffect(() => {
    if (data && Date.now() - data.timestamp < 1000) {
      setIsAnimating(true);
      const timeout = setTimeout(() => setIsAnimating(false), 2000);
      return () => clearTimeout(timeout);
    }
  }, [data]);

  if (!data) {
    return (
      <div className="bg-slate-800 rounded-lg p-4">
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 border border-gray-400 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-gray-400 font-bold">{symbol}</span>
          <span className="text-gray-500">로딩 중...</span>
        </div>
      </div>
    );
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const getChangeColor = (changePercent: number) => {
    if (changePercent > 0) return 'text-green-400';
    if (changePercent < 0) return 'text-red-400';
    return 'text-gray-400';
  };

  return (
    <div
      onClick={onClick}
      className={`bg-slate-800 rounded-lg p-4 transition-all duration-500 ${
        onClick ? 'cursor-pointer hover:bg-slate-700' : ''
      } ${
        isAnimating 
          ? data.changePercent >= 0 
            ? 'bg-green-500 bg-opacity-20 shadow-green-500/20 shadow-lg' 
            : 'bg-red-500 bg-opacity-20 shadow-red-500/20 shadow-lg'
          : ''
      }`}
    >
      <div className="flex items-center justify-between">
        <div>
          <div className="text-white font-bold text-lg">{symbol}</div>
          {showDetails && data.volume && (
            <div className="text-xs text-gray-400">
              거래량: {(data.volume / 1000000).toFixed(1)}M
            </div>
          )}
        </div>
        <div className="text-right">
          <div className="text-white font-bold text-xl mono-font">
            {formatCurrency(data.price)}
          </div>
          <div className={`text-sm font-medium ${getChangeColor(data.changePercent)}`}>
            {data.changePercent >= 0 ? '▲' : '▼'} {data.changePercent >= 0 ? '+' : ''}{data.changePercent.toFixed(2)}%
          </div>
          {showDetails && (
            <div className={`text-xs mono-font ${getChangeColor(data.change)}`}>
              {data.change >= 0 ? '+' : ''}{formatCurrency(data.change)}
            </div>
          )}
        </div>
      </div>
      
      {showDetails && data.high && data.low && (
        <div className="mt-2 pt-2 border-t border-slate-700">
          <div className="flex justify-between text-xs text-gray-400">
            <span>고가: <span className="text-green-400 mono-font">{formatCurrency(data.high)}</span></span>
            <span>저가: <span className="text-red-400 mono-font">{formatCurrency(data.low)}</span></span>
          </div>
          {data.bid && data.ask && (
            <div className="flex justify-between text-xs text-gray-400 mt-1">
              <span>매수: <span className="text-blue-400 mono-font">{formatCurrency(data.bid)}</span></span>
              <span>매도: <span className="text-orange-400 mono-font">{formatCurrency(data.ask)}</span></span>
            </div>
          )}
        </div>
      )}
    </div>
  );
};