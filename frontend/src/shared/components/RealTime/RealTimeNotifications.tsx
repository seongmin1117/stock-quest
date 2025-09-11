'use client';

import React, { useState, useEffect, useRef } from 'react';
import { useTradingNotifications, useWebSocketConnection } from '../../hooks/useWebSocket';
import { TradingNotification } from '../../services/websocket/WebSocketService';

interface RealTimeNotificationsProps {
  maxVisible?: number;
  showDetails?: boolean;
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left';
  autoHide?: boolean;
  autoHideDelay?: number;
}

/**
 * Real-time notifications system with toast-style alerts
 * 토스트 스타일 알림을 포함한 실시간 알림 시스템
 */
export const RealTimeNotifications: React.FC<RealTimeNotificationsProps> = ({
  maxVisible = 5,
  showDetails = true,
  position = 'top-right',
  autoHide = true,
  autoHideDelay = 5000
}) => {
  const { notifications, unreadCount, markAsRead, clearNotifications } = useTradingNotifications();
  const { connected } = useWebSocketConnection();
  const [visibleNotifications, setVisibleNotifications] = useState<TradingNotification[]>([]);
  const [isExpanded, setIsExpanded] = useState(false);
  const notificationRefs = useRef<Map<string, NodeJS.Timeout>>(new Map());

  // Manage visible notifications
  useEffect(() => {
    const recentNotifications = notifications.slice(0, maxVisible);
    setVisibleNotifications(recentNotifications);

    // Set auto-hide timers for new notifications
    recentNotifications.forEach((notification) => {
      if (autoHide && notification.priority !== 'CRITICAL' && !notificationRefs.current.has(notification.id)) {
        const timer = setTimeout(() => {
          setVisibleNotifications(prev => prev.filter(n => n.id !== notification.id));
          notificationRefs.current.delete(notification.id);
        }, autoHideDelay);
        
        notificationRefs.current.set(notification.id, timer);
      }
    });

    return () => {
      // Cleanup timers
      notificationRefs.current.forEach((timer) => {
        clearTimeout(timer);
      });
    };
  }, [notifications, maxVisible, autoHide, autoHideDelay]);

  const handleDismiss = (notificationId: string) => {
    // Clear auto-hide timer
    const timer = notificationRefs.current.get(notificationId);
    if (timer) {
      clearTimeout(timer);
      notificationRefs.current.delete(notificationId);
    }

    // Remove from visible notifications
    setVisibleNotifications(prev => prev.filter(n => n.id !== notificationId));
    markAsRead(notificationId);
  };

  const handleDismissAll = () => {
    // Clear all timers
    notificationRefs.current.forEach((timer) => {
      clearTimeout(timer);
    });
    notificationRefs.current.clear();

    setVisibleNotifications([]);
    clearNotifications();
  };

  const formatTime = (timestamp: number) => {
    const now = Date.now();
    const diff = now - timestamp;
    
    if (diff < 60000) { // Less than 1 minute
      return '방금 전';
    } else if (diff < 3600000) { // Less than 1 hour
      return `${Math.floor(diff / 60000)}분 전`;
    } else if (diff < 86400000) { // Less than 1 day
      return `${Math.floor(diff / 3600000)}시간 전`;
    } else {
      return new Date(timestamp).toLocaleDateString('ko-KR');
    }
  };

  const getNotificationIcon = (type: TradingNotification['type']) => {
    switch (type) {
      case 'ORDER_FILLED':
        return '✅';
      case 'ORDER_PARTIAL':
        return '🔄';
      case 'ORDER_CANCELLED':
        return '❌';
      case 'POSITION_UPDATE':
        return '📈';
      case 'ALERT':
        return '⚠️';
      default:
        return '📢';
    }
  };

  const getPriorityColor = (priority: TradingNotification['priority']) => {
    switch (priority) {
      case 'CRITICAL':
        return 'border-red-500 bg-red-500 bg-opacity-10';
      case 'HIGH':
        return 'border-orange-500 bg-orange-500 bg-opacity-10';
      case 'MEDIUM':
        return 'border-yellow-500 bg-yellow-500 bg-opacity-10';
      case 'LOW':
        return 'border-blue-500 bg-blue-500 bg-opacity-10';
      default:
        return 'border-gray-500 bg-gray-500 bg-opacity-10';
    }
  };

  const getPositionClasses = () => {
    const base = 'fixed z-50 space-y-2';
    switch (position) {
      case 'top-right':
        return `${base} top-4 right-4`;
      case 'top-left':
        return `${base} top-4 left-4`;
      case 'bottom-right':
        return `${base} bottom-4 right-4`;
      case 'bottom-left':
        return `${base} bottom-4 left-4`;
      default:
        return `${base} top-4 right-4`;
    }
  };

  return (
    <>
      {/* Notification Toast Container */}
      <div className={getPositionClasses()}>
        {visibleNotifications.map((notification, index) => (
          <div
            key={notification.id}
            className={`max-w-sm w-80 p-4 rounded-lg shadow-lg border-l-4 transition-all duration-300 transform ${
              getPriorityColor(notification.priority)
            } bg-slate-800 animate-slide-in-right`}
            style={{
              animationDelay: `${index * 100}ms`,
              zIndex: 1000 - index
            }}
          >
            <div className="flex items-start justify-between">
              <div className="flex items-start space-x-3 flex-1">
                <div className="text-2xl flex-shrink-0">
                  {getNotificationIcon(notification.type)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="text-white font-medium text-sm leading-tight">
                    {notification.title}
                  </div>
                  <div className="text-gray-300 text-xs mt-1 leading-relaxed">
                    {notification.message}
                  </div>
                  {showDetails && (notification.symbol || notification.orderId) && (
                    <div className="text-gray-400 text-xs mt-2 flex items-center space-x-3">
                      {notification.symbol && (
                        <span className="bg-slate-700 px-2 py-1 rounded font-mono">
                          {notification.symbol}
                        </span>
                      )}
                      {notification.orderId && (
                        <span className="text-blue-400 font-mono">
                          #{notification.orderId.slice(-8)}
                        </span>
                      )}
                    </div>
                  )}
                  <div className="text-gray-500 text-xs mt-2">
                    {formatTime(notification.timestamp)}
                  </div>
                </div>
              </div>
              <button
                onClick={() => handleDismiss(notification.id)}
                className="text-gray-400 hover:text-white transition-colors ml-2 flex-shrink-0"
                aria-label="알림 닫기"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        ))}

        {/* Clear All Button */}
        {visibleNotifications.length > 1 && (
          <button
            onClick={handleDismissAll}
            className="w-full mt-2 p-2 bg-slate-700 hover:bg-slate-600 text-white text-sm rounded-lg transition-colors"
          >
            모든 알림 닫기
          </button>
        )}
      </div>

      {/* Mobile Notification Indicator */}
      {unreadCount > 0 && (
        <div className="lg:hidden fixed bottom-20 right-4 z-40">
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="bg-red-500 text-white rounded-full w-12 h-12 flex items-center justify-center shadow-lg animate-pulse"
          >
            <span className="text-sm font-bold">{unreadCount}</span>
          </button>
        </div>
      )}
    </>
  );
};

/**
 * Notification center panel for managing all notifications
 * 모든 알림 관리를 위한 알림 센터 패널
 */
export const NotificationCenter: React.FC<{
  isOpen: boolean;
  onClose: () => void;
}> = ({ isOpen, onClose }) => {
  const { notifications, unreadCount, markAsRead, clearNotifications } = useTradingNotifications();
  const { connected } = useWebSocketConnection();
  const [filter, setFilter] = useState<'all' | 'unread' | 'orders' | 'alerts'>('all');

  const filteredNotifications = notifications.filter((notification) => {
    switch (filter) {
      case 'unread':
        return !notification.read;
      case 'orders':
        return ['ORDER_FILLED', 'ORDER_PARTIAL', 'ORDER_CANCELLED'].includes(notification.type);
      case 'alerts':
        return notification.type === 'ALERT';
      default:
        return true;
    }
  });

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getNotificationIcon = (type: TradingNotification['type']) => {
    switch (type) {
      case 'ORDER_FILLED':
        return '✅';
      case 'ORDER_PARTIAL':
        return '🔄';
      case 'ORDER_CANCELLED':
        return '❌';
      case 'POSITION_UPDATE':
        return '📈';
      case 'ALERT':
        return '⚠️';
      default:
        return '📢';
    }
  };

  const getPriorityColor = (priority: TradingNotification['priority']) => {
    switch (priority) {
      case 'CRITICAL':
        return 'border-l-red-500 bg-red-500 bg-opacity-5';
      case 'HIGH':
        return 'border-l-orange-500 bg-orange-500 bg-opacity-5';
      case 'MEDIUM':
        return 'border-l-yellow-500 bg-yellow-500 bg-opacity-5';
      case 'LOW':
        return 'border-l-blue-500 bg-blue-500 bg-opacity-5';
      default:
        return 'border-l-gray-500';
    }
  };

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40"
          onClick={onClose}
        />
      )}

      {/* Notification Panel */}
      <div className={`fixed top-0 right-0 h-full w-96 max-w-[90vw] bg-slate-800 shadow-2xl transform transition-transform duration-300 z-50 ${
        isOpen ? 'translate-x-0' : 'translate-x-full'
      }`}>
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-700 bg-slate-900">
          <div>
            <h2 className="text-lg font-bold text-white">알림 센터</h2>
            <div className="flex items-center space-x-2 text-sm text-gray-400">
              <div className={`w-2 h-2 rounded-full ${
                connected ? 'bg-green-400' : 'bg-red-400'
              }`} />
              <span>{connected ? '실시간 연결됨' : '연결 끊김'}</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-full bg-slate-700 hover:bg-slate-600 transition-colors"
            aria-label="알림 센터 닫기"
          >
            <span className="text-white text-lg">×</span>
          </button>
        </div>

        {/* Filter Tabs */}
        <div className="p-4 border-b border-slate-700">
          <div className="flex space-x-1 bg-slate-700 rounded-lg p-1">
            {[
              { key: 'all', label: '전체', count: notifications.length },
              { key: 'unread', label: '읽지 않음', count: unreadCount },
              { key: 'orders', label: '주문', count: notifications.filter(n => ['ORDER_FILLED', 'ORDER_PARTIAL', 'ORDER_CANCELLED'].includes(n.type)).length },
              { key: 'alerts', label: '알림', count: notifications.filter(n => n.type === 'ALERT').length }
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setFilter(tab.key as typeof filter)}
                className={`flex-1 py-2 px-3 text-xs font-medium rounded-md transition-all duration-200 ${
                  filter === tab.key
                    ? 'bg-blue-500 text-white'
                    : 'text-gray-300 hover:text-white hover:bg-slate-600'
                }`}
              >
                {tab.label}
                {tab.count > 0 && (
                  <span className={`ml-1 ${
                    filter === tab.key ? 'text-blue-200' : 'text-gray-500'
                  }`}>
                    ({tab.count})
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* Notifications List */}
        <div className="flex-1 overflow-y-auto">
          {filteredNotifications.length === 0 ? (
            <div className="p-8 text-center text-gray-400">
              <div className="text-4xl mb-4">📭</div>
              <div className="text-lg mb-2">알림이 없습니다</div>
              <div className="text-sm">
                {filter === 'unread' ? '읽지 않은 알림이 없습니다' : '아직 알림이 없습니다'}
              </div>
            </div>
          ) : (
            <div className="space-y-1 p-2">
              {filteredNotifications.map((notification) => (
                <div
                  key={notification.id}
                  onClick={() => markAsRead(notification.id)}
                  className={`p-4 rounded-lg cursor-pointer transition-all duration-200 border-l-4 hover:bg-slate-700 ${
                    getPriorityColor(notification.priority)
                  } ${
                    !notification.read ? 'bg-slate-700' : ''
                  }`}
                >
                  <div className="flex items-start space-x-3">
                    <div className="text-xl flex-shrink-0">
                      {getNotificationIcon(notification.type)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between">
                        <div className="text-white font-medium text-sm">
                          {notification.title}
                        </div>
                        {!notification.read && (
                          <div className="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0 mt-1"></div>
                        )}
                      </div>
                      <div className="text-gray-300 text-sm mt-1 leading-relaxed">
                        {notification.message}
                      </div>
                      <div className="flex items-center justify-between mt-2">
                        <div className="flex items-center space-x-2">
                          {notification.symbol && (
                            <span className="bg-slate-600 px-2 py-1 rounded text-xs font-mono text-gray-300">
                              {notification.symbol}
                            </span>
                          )}
                          {notification.priority === 'CRITICAL' && (
                            <span className="bg-red-500 px-2 py-1 rounded text-xs font-medium text-white">
                              긴급
                            </span>
                          )}
                        </div>
                        <div className="text-gray-500 text-xs">
                          {formatTime(notification.timestamp)}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer Actions */}
        {notifications.length > 0 && (
          <div className="p-4 border-t border-slate-700 bg-slate-900">
            <div className="flex space-x-2">
              {unreadCount > 0 && (
                <button
                  onClick={() => markAsRead()}
                  className="flex-1 py-2 px-4 bg-blue-500 hover:bg-blue-600 text-white text-sm rounded-lg font-medium transition-colors"
                >
                  모두 읽음 표시
                </button>
              )}
              <button
                onClick={clearNotifications}
                className="flex-1 py-2 px-4 bg-slate-600 hover:bg-slate-500 text-white text-sm rounded-lg font-medium transition-colors"
              >
                모두 삭제
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

/**
 * Notification badge component for showing unread count
 * 읽지 않은 알림 수를 표시하는 배지 컴포넌트
 */
export const NotificationBadge: React.FC<{
  onClick?: () => void;
  showCount?: boolean;
}> = ({ onClick, showCount = true }) => {
  const { unreadCount } = useTradingNotifications();
  const { connected } = useWebSocketConnection();

  return (
    <button
      onClick={onClick}
      className="relative p-2 text-gray-400 hover:text-white transition-colors"
      aria-label={`알림 (읽지 않음: ${unreadCount})`}
    >
      <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
              d="M15 17h5l-5-5V9a9 9 0 10-18 0v3l-5 5h5m0 0v1a3 3 0 006 0v-1m-6 0h6" />
      </svg>

      {/* Connection indicator */}
      <div className={`absolute -top-1 -right-1 w-3 h-3 rounded-full ${
        connected ? 'bg-green-400' : 'bg-red-400'
      }`} />

      {/* Unread count badge */}
      {showCount && unreadCount > 0 && (
        <div className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold animate-pulse">
          {unreadCount > 99 ? '99+' : unreadCount}
        </div>
      )}
    </button>
  );
};