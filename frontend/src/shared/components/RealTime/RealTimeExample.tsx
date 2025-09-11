'use client';

import React, { useState } from 'react';
import { RealTimeMarketData, RealTimeTicker } from './RealTimeMarketData';
import { RealTimePortfolio, RealTimePortfolioWidget } from './RealTimePortfolio';
import { RealTimeNotifications, NotificationCenter, NotificationBadge } from './RealTimeNotifications';
import { useWebSocketConnection } from '../../hooks/useWebSocket';

/**
 * Example component demonstrating WebSocket real-time features integration
 * WebSocket 실시간 기능 통합을 보여주는 예제 컴포넌트
 */
export const RealTimeExample: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'market' | 'portfolio' | 'notifications'>('market');
  const [selectedSymbol, setSelectedSymbol] = useState<string>('AAPL');
  const [isNotificationCenterOpen, setIsNotificationCenterOpen] = useState(false);
  const { connected, connecting, error, connect, disconnect } = useWebSocketConnection('user123');

  const watchedSymbols = ['AAPL', 'GOOGL', 'TSLA', 'MSFT', 'NVDA', 'AMZN'];

  const handleSymbolClick = (symbol: string) => {
    setSelectedSymbol(symbol);
    console.log(`Selected symbol: ${symbol}`);
  };

  const handlePortfolioPositionClick = (symbol: string) => {
    setSelectedSymbol(symbol);
    setActiveTab('market');
  };

  return (
    <div className="min-h-screen bg-slate-900 p-4">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold text-white">실시간 거래 시스템</h1>
          <div className="flex items-center space-x-4">
            {/* Connection Status */}
            <div className="flex items-center space-x-2 text-sm">
              <div className={`w-3 h-3 rounded-full ${
                connected ? 'bg-green-400' : connecting ? 'bg-yellow-400' : 'bg-red-400'
              }`} />
              <span className="text-gray-400">
                {connected ? '실시간 연결됨' : connecting ? '연결 중...' : '연결 끊김'}
              </span>
              {error && (
                <span className="text-red-400 text-xs">({error})</span>
              )}
            </div>

            {/* Connection Controls */}
            <div className="flex space-x-2">
              {!connected && !connecting && (
                <button
                  onClick={() => connect()}
                  className="px-3 py-1 bg-green-500 hover:bg-green-600 text-white text-sm rounded-lg font-medium transition-colors"
                >
                  연결
                </button>
              )}
              {connected && (
                <button
                  onClick={() => disconnect()}
                  className="px-3 py-1 bg-red-500 hover:bg-red-600 text-white text-sm rounded-lg font-medium transition-colors"
                >
                  연결 해제
                </button>
              )}
            </div>

            {/* Notification Badge */}
            <NotificationBadge onClick={() => setIsNotificationCenterOpen(true)} />
          </div>
        </div>

        {/* Current Symbol Display */}
        <div className="bg-slate-800 rounded-lg p-4 mb-4">
          <h2 className="text-lg font-semibold text-white mb-3">현재 선택된 종목</h2>
          <RealTimeTicker 
            symbol={selectedSymbol}
            showDetails={true}
            onClick={() => console.log(`Clicked on ${selectedSymbol}`)}
          />
        </div>

        {/* Navigation Tabs */}
        <div className="flex bg-slate-800 rounded-lg p-1">
          {[
            { key: 'market', label: '시장 데이터', icon: '📊' },
            { key: 'portfolio', label: '포트폴리오', icon: '💼' },
            { key: 'notifications', label: '알림', icon: '🔔' }
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as typeof activeTab)}
              className={`flex-1 py-3 px-4 rounded-md font-medium transition-all duration-200 ${
                activeTab === tab.key
                  ? 'bg-blue-500 text-white shadow-lg'
                  : 'text-gray-300 hover:text-white hover:bg-slate-700'
              }`}
            >
              <span className="mr-2">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Primary Content */}
        <div className="lg:col-span-2">
          {activeTab === 'market' && (
            <div className="space-y-6">
              <RealTimeMarketData 
                symbols={watchedSymbols}
                onSymbolClick={handleSymbolClick}
                showChart={true}
              />
              
              {/* Market Overview */}
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                {watchedSymbols.slice(0, 6).map((symbol) => (
                  <RealTimeTicker
                    key={symbol}
                    symbol={symbol}
                    onClick={() => handleSymbolClick(symbol)}
                    showDetails={false}
                  />
                ))}
              </div>
            </div>
          )}

          {activeTab === 'portfolio' && (
            <RealTimePortfolio
              userId="user123"
              onPositionClick={handlePortfolioPositionClick}
              showDetails={true}
            />
          )}

          {activeTab === 'notifications' && (
            <div className="bg-slate-800 rounded-lg p-6">
              <h2 className="text-xl font-bold text-white mb-4">알림 관리</h2>
              <div className="text-gray-400 mb-6">
                실시간 거래 알림과 시스템 메시지를 확인하세요.
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-slate-700 rounded-lg p-4">
                  <h3 className="text-lg font-semibold text-white mb-2">알림 설정</h3>
                  <div className="space-y-3">
                    <label className="flex items-center space-x-3">
                      <input type="checkbox" defaultChecked className="rounded" />
                      <span className="text-gray-300">주문 체결 알림</span>
                    </label>
                    <label className="flex items-center space-x-3">
                      <input type="checkbox" defaultChecked className="rounded" />
                      <span className="text-gray-300">포트폴리오 변동 알림</span>
                    </label>
                    <label className="flex items-center space-x-3">
                      <input type="checkbox" defaultChecked className="rounded" />
                      <span className="text-gray-300">가격 경고 알림</span>
                    </label>
                    <label className="flex items-center space-x-3">
                      <input type="checkbox" className="rounded" />
                      <span className="text-gray-300">브라우저 알림</span>
                    </label>
                  </div>
                </div>

                <div className="bg-slate-700 rounded-lg p-4">
                  <h3 className="text-lg font-semibold text-white mb-2">시스템 상태</h3>
                  <div className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-300">WebSocket 연결</span>
                      <div className={`w-3 h-3 rounded-full ${
                        connected ? 'bg-green-400' : 'bg-red-400'
                      }`} />
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-gray-300">실시간 데이터</span>
                      <div className={`w-3 h-3 rounded-full ${
                        connected ? 'bg-green-400' : 'bg-gray-400'
                      }`} />
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-gray-300">알림 서비스</span>
                      <div className={`w-3 h-3 rounded-full ${
                        connected ? 'bg-green-400' : 'bg-gray-400'
                      }`} />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Portfolio Widget */}
          <RealTimePortfolioWidget 
            userId="user123"
            onClick={() => setActiveTab('portfolio')}
          />

          {/* Quick Market Overview */}
          <div className="bg-slate-800 rounded-lg p-4">
            <h3 className="text-lg font-semibold text-white mb-4">시장 현황</h3>
            <RealTimeMarketData 
              symbols={['AAPL', 'GOOGL', 'TSLA']}
              onSymbolClick={handleSymbolClick}
              compact={true}
            />
          </div>

          {/* WebSocket Status */}
          <div className="bg-slate-800 rounded-lg p-4">
            <h3 className="text-lg font-semibold text-white mb-4">연결 정보</h3>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between items-center">
                <span className="text-gray-400">상태</span>
                <span className={`font-medium ${
                  connected ? 'text-green-400' : connecting ? 'text-yellow-400' : 'text-red-400'
                }`}>
                  {connected ? '연결됨' : connecting ? '연결 중' : '연결 끊김'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-400">구독 중인 종목</span>
                <span className="text-white font-medium">{watchedSymbols.length}개</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-400">마지막 업데이트</span>
                <span className="text-white font-medium mono-font">
                  {new Date().toLocaleTimeString('ko-KR')}
                </span>
              </div>
            </div>
          </div>

          {/* Demo Controls */}
          <div className="bg-slate-800 rounded-lg p-4">
            <h3 className="text-lg font-semibold text-white mb-4">데모 컨트롤</h3>
            <div className="space-y-2">
              <button
                onClick={() => {
                  // Simulate order notification
                  console.log('Simulating order notification...');
                }}
                className="w-full py-2 px-3 bg-blue-500 hover:bg-blue-600 text-white text-sm rounded-lg transition-colors"
              >
                주문 알림 테스트
              </button>
              <button
                onClick={() => {
                  // Simulate price alert
                  console.log('Simulating price alert...');
                }}
                className="w-full py-2 px-3 bg-orange-500 hover:bg-orange-600 text-white text-sm rounded-lg transition-colors"
              >
                가격 경고 테스트
              </button>
              <button
                onClick={() => {
                  // Switch to random symbol
                  const randomSymbol = watchedSymbols[Math.floor(Math.random() * watchedSymbols.length)];
                  setSelectedSymbol(randomSymbol);
                }}
                className="w-full py-2 px-3 bg-purple-500 hover:bg-purple-600 text-white text-sm rounded-lg transition-colors"
              >
                랜덤 종목 선택
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Real-time Toast Notifications */}
      <RealTimeNotifications 
        position="top-right"
        maxVisible={5}
        autoHide={true}
        showDetails={true}
      />

      {/* Notification Center */}
      <NotificationCenter 
        isOpen={isNotificationCenterOpen}
        onClose={() => setIsNotificationCenterOpen(false)}
      />

      {/* Usage Instructions */}
      <div className="mt-8 bg-slate-800 rounded-lg p-6">
        <h2 className="text-xl font-bold text-white mb-4">실시간 시스템 사용법</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
          <div>
            <h3 className="text-lg font-semibold text-green-400 mb-2">🟢 기능</h3>
            <ul className="space-y-1 text-gray-300">
              <li>• 실시간 주가 데이터 수신</li>
              <li>• 포트폴리오 자동 업데이트</li>
              <li>• 거래 알림 및 주문 상태</li>
              <li>• 가격 변동 시각적 표시</li>
              <li>• 자동 연결 복구</li>
              <li>• 모바일 터치 최적화</li>
            </ul>
          </div>
          <div>
            <h3 className="text-lg font-semibold text-blue-400 mb-2">💡 사용 팁</h3>
            <ul className="space-y-1 text-gray-300">
              <li>• 종목 클릭으로 상세 정보 확인</li>
              <li>• 알림 배지로 새 소식 확인</li>
              <li>• 연결 상태를 항상 체크</li>
              <li>• 포트폴리오 위젯으로 빠른 확인</li>
              <li>• 탭을 통한 효율적 탐색</li>
              <li>• 실시간 정렬 및 필터링</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};