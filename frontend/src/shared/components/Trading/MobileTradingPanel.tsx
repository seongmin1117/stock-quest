'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useGetApiChallengesChallengeId } from '@/shared/api/generated/챌린지/챌린지';

interface Stock {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  marketCap: number;
}

interface MobileTradingPanelProps {
  challengeId: number;
  selectedStock?: Stock;
  onStockSelect: (stock: Stock) => void;
  onTrade: (type: 'BUY' | 'SELL', stock: Stock, quantity: number, price: number) => void;
}

/**
 * Mobile-optimized trading panel with swipe gestures and touch-friendly controls
 * 모바일 최적화된 거래 패널 (스와이프 제스처 및 터치 친화적 컨트롤)
 */
export const MobileTradingPanel: React.FC<MobileTradingPanelProps> = ({
  challengeId,
  selectedStock,
  onStockSelect,
  onTrade
}) => {
  const [activeTab, setActiveTab] = useState<'market' | 'order' | 'portfolio'>('market');
  const [orderType, setOrderType] = useState<'market' | 'limit'>('market');
  const [tradeType, setTradeType] = useState<'BUY' | 'SELL'>('BUY');
  const [quantity, setQuantity] = useState<number>(1);
  const [limitPrice, setLimitPrice] = useState<string>('');
  const [totalAmount, setTotalAmount] = useState<number>(0);

  // Touch gesture handling
  const panelRef = useRef<HTMLDivElement>(null);
  const [touchStart, setTouchStart] = useState<{ x: number; y: number } | null>(null);
  const [currentTranslateX, setCurrentTranslateX] = useState(0);

  // Fetch challenge data to get instruments
  const { data: challengeData, isLoading: loadingChallenge } = useGetApiChallengesChallengeId(challengeId, {
    query: {
      enabled: !isNaN(challengeId) && challengeId > 0,
      refetchInterval: 5000, // Refresh every 5 seconds for live data
    }
  });

  // Convert challenge instruments to Stock format with simulated market data
  const marketStocks: Stock[] = React.useMemo(() => {
    if (!challengeData?.instruments || challengeData.instruments.length === 0) {
      return [];
    }

    return challengeData.instruments.map((instrumentKey, index) => {
      // Generate simulated market data based on instrument key
      const basePrice = 100 + (instrumentKey.charCodeAt(0) - 65) * 50; // A=100, B=150, C=200, etc.
      const variation = (Math.sin(Date.now() / 10000 + index) * 10); // Time-based price variation
      const currentPrice = basePrice + variation;
      const openPrice = basePrice + (Math.random() - 0.5) * 5;
      const change = currentPrice - openPrice;
      const changePercent = (change / openPrice) * 100;

      return {
        symbol: instrumentKey,
        name: `회사 ${instrumentKey}`, // Hidden company name
        price: currentPrice,
        change: change,
        changePercent: changePercent,
        volume: Math.floor(Math.random() * 10000000) + 1000000, // Random volume between 1M-11M
        marketCap: Math.floor(Math.random() * 1000000000000) + 100000000000 // Random market cap
      };
    });
  }, [challengeData?.instruments]);

  const currentStock = selectedStock || marketStocks[0];

  // Calculate total amount
  useEffect(() => {
    if (orderType === 'market') {
      setTotalAmount(currentStock.price * quantity);
    } else if (limitPrice) {
      setTotalAmount(parseFloat(limitPrice) * quantity);
    }
  }, [orderType, quantity, limitPrice, currentStock.price]);

  // Touch gesture handlers
  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    const touch = e.touches[0];
    setTouchStart({ x: touch.clientX, y: touch.clientY });
  }, []);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (!touchStart) return;

    const touch = e.touches[0];
    const deltaX = touch.clientX - touchStart.x;
    const deltaY = touch.clientY - touchStart.y;

    // Only handle horizontal swipes (ignore vertical scrolling)
    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
      e.preventDefault();
      setCurrentTranslateX(deltaX);
    }
  }, [touchStart]);

  const handleTouchEnd = useCallback(() => {
    if (!touchStart) return;

    const threshold = 80;
    const tabs = ['market', 'order', 'portfolio'];
    const currentIndex = tabs.indexOf(activeTab);

    if (currentTranslateX > threshold && currentIndex > 0) {
      // Swipe right - previous tab
      setActiveTab(tabs[currentIndex - 1] as typeof activeTab);
    } else if (currentTranslateX < -threshold && currentIndex < tabs.length - 1) {
      // Swipe left - next tab
      setActiveTab(tabs[currentIndex + 1] as typeof activeTab);
    }

    setTouchStart(null);
    setCurrentTranslateX(0);
  }, [touchStart, currentTranslateX, activeTab]);

  const formatCurrency = (amount: number) => {
    return `₩${amount.toLocaleString()}`;
  };

  const formatVolume = (volume: number) => {
    if (volume >= 1000000) {
      return (volume / 1000000).toFixed(1) + 'M';
    } else if (volume >= 1000) {
      return (volume / 1000).toFixed(1) + 'K';
    }
    return volume.toString();
  };

  const handleQuantityChange = (delta: number) => {
    setQuantity(Math.max(1, quantity + delta));
  };

  const handleTrade = () => {
    const price = orderType === 'market' ? currentStock.price : parseFloat(limitPrice);
    onTrade(tradeType, currentStock, quantity, price);
  };

  // Show loading state
  if (loadingChallenge || marketStocks.length === 0) {
    return (
      <div className="bg-slate-900 min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-white">시장 데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-slate-900 min-h-screen pb-20">
      {/* Header with stock info */}
      <div className="bg-gradient-to-r from-slate-800 to-slate-700 p-4 pt-12 sticky top-0 z-30">
        <div className="flex items-center justify-between mb-2">
          <div>
            <h1 className="text-lg font-bold text-white">{currentStock.symbol}</h1>
            <p className="text-sm text-gray-300 truncate max-w-48">{currentStock.name}</p>
          </div>
          <div className="text-right">
            <div className="text-xl font-bold text-white mono-font">
              {formatCurrency(currentStock.price)}
            </div>
            <div className={`text-sm font-medium ${
              currentStock.change >= 0 ? 'text-green-400' : 'text-red-400'
            }`}>
              {currentStock.change >= 0 ? '+' : ''}{formatCurrency(currentStock.change)} 
              ({currentStock.changePercent >= 0 ? '+' : ''}{currentStock.changePercent.toFixed(2)}%)
            </div>
          </div>
        </div>

        {/* Tab navigation */}
        <div className="flex bg-slate-700 rounded-lg p-1 mt-4">
          {[
            { key: 'market', label: '마켓', icon: '📊' },
            { key: 'order', label: '주문', icon: '💹' },
            { key: 'portfolio', label: '포트폴리오', icon: '💼' }
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

      {/* Content area with swipe support */}
      <div
        ref={panelRef}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        className="p-4"
        style={{
          transform: `translateX(${currentTranslateX}px)`,
          transition: currentTranslateX === 0 ? 'transform 0.3s ease-out' : 'none'
        }}
      >
        {/* Market Tab */}
        {activeTab === 'market' && (
          <div>
            <h2 className="text-lg font-semibold text-white mb-4">실시간 시세</h2>
            <div className="space-y-3">
              {marketStocks.map((stock) => (
                <div
                  key={stock.symbol}
                  onClick={() => onStockSelect(stock)}
                  className={`bg-slate-800 rounded-lg p-4 transition-all duration-200 active:scale-95 ${
                    currentStock.symbol === stock.symbol ? 'border-2 border-blue-500' : 'border border-slate-600'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <div className="text-white font-bold text-base">{stock.symbol}</div>
                      <div className="text-gray-400 text-sm truncate max-w-40">{stock.name}</div>
                    </div>
                    <div className="text-right">
                      <div className="text-white font-bold mono-font">
                        {formatCurrency(stock.price)}
                      </div>
                      <div className={`text-sm font-medium ${
                        stock.change >= 0 ? 'text-green-400' : 'text-red-400'
                      }`}>
                        {stock.change >= 0 ? '+' : ''}{formatCurrency(stock.change)}
                      </div>
                    </div>
                  </div>
                  <div className="flex justify-between items-center text-xs text-gray-400">
                    <span>거래량: {formatVolume(stock.volume)}</span>
                    <span className={stock.changePercent >= 0 ? 'text-green-400' : 'text-red-400'}>
                      {stock.changePercent >= 0 ? '+' : ''}{stock.changePercent.toFixed(2)}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Order Tab */}
        {activeTab === 'order' && (
          <div>
            <h2 className="text-lg font-semibold text-white mb-4">주문하기</h2>
            
            {/* Buy/Sell Toggle */}
            <div className="bg-slate-800 rounded-lg p-1 mb-4 flex">
              <button
                onClick={() => setTradeType('BUY')}
                className={`flex-1 py-3 rounded-md font-medium transition-all duration-200 ${
                  tradeType === 'BUY' 
                    ? 'bg-green-500 text-white shadow-md' 
                    : 'text-gray-300 hover:text-white'
                }`}
              >
                🟢 매수
              </button>
              <button
                onClick={() => setTradeType('SELL')}
                className={`flex-1 py-3 rounded-md font-medium transition-all duration-200 ${
                  tradeType === 'SELL' 
                    ? 'bg-red-500 text-white shadow-md' 
                    : 'text-gray-300 hover:text-white'
                }`}
              >
                🔴 매도
              </button>
            </div>

            {/* Order Type */}
            <div className="bg-slate-800 rounded-lg p-4 mb-4">
              <h3 className="text-white font-medium mb-3">주문 타입</h3>
              <div className="flex space-x-2">
                <button
                  onClick={() => setOrderType('market')}
                  className={`flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all ${
                    orderType === 'market'
                      ? 'bg-blue-500 text-white'
                      : 'bg-slate-700 text-gray-300 hover:text-white'
                  }`}
                >
                  시장가
                </button>
                <button
                  onClick={() => setOrderType('limit')}
                  className={`flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all ${
                    orderType === 'limit'
                      ? 'bg-blue-500 text-white'
                      : 'bg-slate-700 text-gray-300 hover:text-white'
                  }`}
                >
                  지정가
                </button>
              </div>
            </div>

            {/* Quantity Selector */}
            <div className="bg-slate-800 rounded-lg p-4 mb-4">
              <h3 className="text-white font-medium mb-3">수량</h3>
              <div className="flex items-center justify-between">
                <button
                  onClick={() => handleQuantityChange(-1)}
                  disabled={quantity <= 1}
                  className="w-12 h-12 bg-slate-700 rounded-full flex items-center justify-center text-white font-bold text-xl hover:bg-slate-600 disabled:opacity-50 active:scale-95 transition-all"
                >
                  −
                </button>
                <div className="text-center">
                  <input
                    type="number"
                    value={quantity}
                    onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                    className="w-20 bg-transparent text-white text-2xl font-bold text-center border-none outline-none"
                    min="1"
                  />
                  <div className="text-gray-400 text-sm">주</div>
                </div>
                <button
                  onClick={() => handleQuantityChange(1)}
                  className="w-12 h-12 bg-slate-700 rounded-full flex items-center justify-center text-white font-bold text-xl hover:bg-slate-600 active:scale-95 transition-all"
                >
                  +
                </button>
              </div>
            </div>

            {/* Limit Price Input */}
            {orderType === 'limit' && (
              <div className="bg-slate-800 rounded-lg p-4 mb-4">
                <h3 className="text-white font-medium mb-3">지정가격</h3>
                <input
                  type="number"
                  value={limitPrice}
                  onChange={(e) => setLimitPrice(e.target.value)}
                  placeholder={formatCurrency(currentStock.price)}
                  className="w-full bg-slate-700 text-white rounded-md px-4 py-3 text-lg font-medium mono-font outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                  step="0.01"
                />
              </div>
            )}

            {/* Order Summary */}
            <div className="bg-slate-800 rounded-lg p-4 mb-6">
              <h3 className="text-white font-medium mb-3">주문 요약</h3>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-400">종목</span>
                  <span className="text-white font-medium">{currentStock.symbol}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">수량</span>
                  <span className="text-white font-medium">{quantity}주</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">가격</span>
                  <span className="text-white font-medium mono-font">
                    {orderType === 'market' 
                      ? formatCurrency(currentStock.price) 
                      : limitPrice ? formatCurrency(parseFloat(limitPrice)) : '미설정'
                    }
                  </span>
                </div>
                <div className="border-t border-slate-600 pt-2 mt-2">
                  <div className="flex justify-between text-lg">
                    <span className="text-gray-300">총 금액</span>
                    <span className="text-white font-bold mono-font">
                      {formatCurrency(totalAmount)}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Trade Button */}
            <button
              onClick={handleTrade}
              disabled={orderType === 'limit' && !limitPrice}
              className={`w-full py-4 rounded-lg font-bold text-lg transition-all duration-200 active:scale-95 ${
                tradeType === 'BUY'
                  ? 'bg-green-500 hover:bg-green-600 text-white'
                  : 'bg-red-500 hover:bg-red-600 text-white'
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              {tradeType === 'BUY' ? '🟢 매수 주문' : '🔴 매도 주문'}
            </button>
          </div>
        )}

        {/* Portfolio Tab */}
        {activeTab === 'portfolio' && (
          <div>
            <h2 className="text-lg font-semibold text-white mb-4">내 포트폴리오</h2>
            <div className="bg-slate-800 rounded-lg p-4 mb-4">
              <div className="text-center mb-4">
                <div className="text-3xl font-bold text-white mono-font">$125,430.50</div>
                <div className="text-green-400 text-lg font-medium">+$4,230.50 (+3.49%)</div>
                <div className="text-gray-400 text-sm">총 자산 가치</div>
              </div>
            </div>
            
            <div className="space-y-3">
              {marketStocks.slice(0, 2).map((stock) => (
                <div key={stock.symbol} className="bg-slate-800 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <div className="text-white font-bold">{stock.symbol}</div>
                      <div className="text-gray-400 text-sm">5주 보유</div>
                    </div>
                    <div className="text-right">
                      <div className="text-white font-bold mono-font">
                        {formatCurrency(stock.price * 5)}
                      </div>
                      <div className="text-green-400 text-sm">+12.5%</div>
                    </div>
                  </div>
                  <div className="bg-slate-700 rounded-full h-2">
                    <div className="bg-blue-500 h-2 rounded-full" style={{ width: '35%' }}></div>
                  </div>
                  <div className="text-xs text-gray-400 mt-1">포트폴리오의 35%</div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Quick Action Buttons */}
      <div className="fixed bottom-20 left-0 right-0 bg-slate-800 border-t border-slate-700 p-4 lg:hidden">
        <div className="flex space-x-3">
          <button
            onClick={() => {
              setTradeType('BUY');
              setActiveTab('order');
            }}
            className="flex-1 bg-green-500 hover:bg-green-600 py-3 rounded-lg font-medium text-white transition-all duration-200 active:scale-95"
          >
            🟢 빠른 매수
          </button>
          <button
            onClick={() => {
              setTradeType('SELL');
              setActiveTab('order');
            }}
            className="flex-1 bg-red-500 hover:bg-red-600 py-3 rounded-lg font-medium text-white transition-all duration-200 active:scale-95"
          >
            🔴 빠른 매도
          </button>
        </div>
      </div>
    </div>
  );
};