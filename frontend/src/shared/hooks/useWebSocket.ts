'use client';

import { useEffect, useState, useRef, useCallback } from 'react';
import WebSocketManager, { 
  WebSocketService, 
  MarketDataUpdate, 
  PortfolioUpdate, 
  TradingNotification, 
  ChallengeUpdate 
} from '../services/websocket/WebSocketService';

export interface WebSocketConnectionState {
  connected: boolean;
  connecting: boolean;
  reconnecting: boolean;
  reconnectAttempts: number;
  error: string | null;
  subscriptions: string[];
}

/**
 * Hook for managing WebSocket connection state
 * WebSocket 연결 상태 관리를 위한 훅
 */
export const useWebSocketConnection = (userId?: string) => {
  const [connectionState, setConnectionState] = useState<WebSocketConnectionState>({
    connected: false,
    connecting: false,
    reconnecting: false,
    reconnectAttempts: 0,
    error: null,
    subscriptions: []
  });

  const wsService = useRef<WebSocketService | null>(null);
  const connectionAttempted = useRef(false);

  // Initialize WebSocket service
  useEffect(() => {
    wsService.current = WebSocketManager.getInstance();
    
    const handleConnected = () => {
      setConnectionState(prev => ({
        ...prev,
        connected: true,
        connecting: false,
        reconnecting: false,
        error: null,
        subscriptions: wsService.current?.getConnectionStatus().subscriptions || []
      }));
    };

    const handleDisconnected = () => {
      setConnectionState(prev => ({
        ...prev,
        connected: false,
        connecting: false
      }));
    };

    const handleReconnecting = (data: { attempt: number; maxAttempts: number; delay: number }) => {
      setConnectionState(prev => ({
        ...prev,
        reconnecting: true,
        reconnectAttempts: data.attempt,
        error: null
      }));
    };

    const handleError = (error: Error) => {
      setConnectionState(prev => ({
        ...prev,
        connecting: false,
        reconnecting: false,
        error: error.message
      }));
    };

    const handleReconnectFailed = () => {
      setConnectionState(prev => ({
        ...prev,
        reconnecting: false,
        error: 'Failed to reconnect after multiple attempts'
      }));
    };

    // Setup event listeners
    wsService.current.on('connected', handleConnected);
    wsService.current.on('disconnected', handleDisconnected);
    wsService.current.on('reconnecting', handleReconnecting);
    wsService.current.on('error', handleError);
    wsService.current.on('reconnectFailed', handleReconnectFailed);

    return () => {
      if (wsService.current) {
        wsService.current.off('connected', handleConnected);
        wsService.current.off('disconnected', handleDisconnected);
        wsService.current.off('reconnecting', handleReconnecting);
        wsService.current.off('error', handleError);
        wsService.current.off('reconnectFailed', handleReconnectFailed);
      }
    };
  }, []);

  // Auto-connect when component mounts
  useEffect(() => {
    if (!connectionAttempted.current && wsService.current) {
      connectionAttempted.current = true;
      
      setConnectionState(prev => ({ ...prev, connecting: true }));
      
      wsService.current.connect(userId).catch((error) => {
        console.error('Failed to connect to WebSocket:', error);
        setConnectionState(prev => ({
          ...prev,
          connecting: false,
          error: error.message
        }));
      });
    }
  }, [userId]);

  const connect = useCallback(() => {
    if (wsService.current && !connectionState.connected && !connectionState.connecting) {
      setConnectionState(prev => ({ ...prev, connecting: true, error: null }));
      
      wsService.current.connect(userId).catch((error) => {
        console.error('Manual connection failed:', error);
        setConnectionState(prev => ({
          ...prev,
          connecting: false,
          error: error.message
        }));
      });
    }
  }, [userId, connectionState.connected, connectionState.connecting]);

  const disconnect = useCallback(() => {
    if (wsService.current) {
      wsService.current.disconnect();
    }
  }, []);

  return {
    ...connectionState,
    connect,
    disconnect,
    service: wsService.current
  };
};

/**
 * Hook for real-time market data
 * 실시간 시장 데이터를 위한 훅
 */
export const useMarketData = (symbols: string[]) => {
  const [marketData, setMarketData] = useState<Map<string, MarketDataUpdate>>(new Map());
  const { connected, service } = useWebSocketConnection();
  const previousSymbols = useRef<string[]>([]);

  useEffect(() => {
    if (!connected || !service || symbols.length === 0) return;

    const handleMarketData = (data: MarketDataUpdate) => {
      setMarketData(prev => new Map(prev.set(data.symbol, data)));
    };

    service.on('marketData', handleMarketData);

    // Subscribe to new symbols
    const newSymbols = symbols.filter(s => !previousSymbols.current.includes(s));
    if (newSymbols.length > 0) {
      service.subscribeToMarketData(newSymbols);
    }

    // Unsubscribe from removed symbols
    const removedSymbols = previousSymbols.current.filter(s => !symbols.includes(s));
    if (removedSymbols.length > 0) {
      service.unsubscribeFromMarketData(removedSymbols);
      setMarketData(prev => {
        const newMap = new Map(prev);
        removedSymbols.forEach(symbol => newMap.delete(symbol));
        return newMap;
      });
    }

    previousSymbols.current = [...symbols];

    return () => {
      service.off('marketData', handleMarketData);
    };
  }, [connected, service, symbols]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (service && previousSymbols.current.length > 0) {
        service.unsubscribeFromMarketData(previousSymbols.current);
      }
    };
  }, [service]);

  const getMarketData = useCallback((symbol: string) => {
    return marketData.get(symbol) || null;
  }, [marketData]);

  const getMarketDataArray = useCallback(() => {
    return Array.from(marketData.values());
  }, [marketData]);

  return {
    marketData: Object.fromEntries(marketData),
    getMarketData,
    getMarketDataArray,
    isLoading: !connected,
    symbolCount: marketData.size
  };
};

/**
 * Hook for real-time portfolio updates
 * 실시간 포트폴리오 업데이트를 위한 훅
 */
export const usePortfolioUpdates = () => {
  const [portfolioData, setPortfolioData] = useState<PortfolioUpdate | null>(null);
  const [lastUpdate, setLastUpdate] = useState<number>(0);
  const { connected, service } = useWebSocketConnection();

  useEffect(() => {
    if (!connected || !service) return;

    const handlePortfolioUpdate = (data: PortfolioUpdate) => {
      setPortfolioData(data);
      setLastUpdate(Date.now());
    };

    service.on('portfolioUpdate', handlePortfolioUpdate);
    service.subscribeToPortfolio();

    return () => {
      service.off('portfolioUpdate', handlePortfolioUpdate);
    };
  }, [connected, service]);

  return {
    portfolioData,
    lastUpdate,
    isLoading: !connected || !portfolioData
  };
};

/**
 * Hook for trading notifications
 * 거래 알림을 위한 훅
 */
export const useTradingNotifications = () => {
  const [notifications, setNotifications] = useState<TradingNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const { connected, service } = useWebSocketConnection();

  useEffect(() => {
    if (!connected || !service) return;

    const handleTradingNotification = (notification: TradingNotification) => {
      setNotifications(prev => [notification, ...prev.slice(0, 49)]); // Keep last 50
      setUnreadCount(prev => prev + 1);
      
      // Show browser notification for high priority alerts
      if (notification.priority === 'HIGH' || notification.priority === 'CRITICAL') {
        showBrowserNotification(notification);
      }
    };

    service.on('tradingNotification', handleTradingNotification);
    service.subscribeToNotifications();

    return () => {
      service.off('tradingNotification', handleTradingNotification);
    };
  }, [connected, service]);

  const markAsRead = useCallback((notificationId?: string) => {
    if (notificationId) {
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } else {
      // Mark all as read
      setUnreadCount(0);
    }
  }, []);

  const clearNotifications = useCallback(() => {
    setNotifications([]);
    setUnreadCount(0);
  }, []);

  return {
    notifications,
    unreadCount,
    markAsRead,
    clearNotifications,
    isLoading: !connected
  };
};

/**
 * Hook for challenge updates
 * 챌린지 업데이트를 위한 훅
 */
export const useChallengeUpdates = (challengeIds: string[]) => {
  const [challengeUpdates, setChallengeUpdates] = useState<Map<string, ChallengeUpdate>>(new Map());
  const { connected, service } = useWebSocketConnection();
  const previousChallengeIds = useRef<string[]>([]);

  useEffect(() => {
    if (!connected || !service || challengeIds.length === 0) return;

    const handleChallengeUpdate = (update: ChallengeUpdate) => {
      setChallengeUpdates(prev => new Map(prev.set(update.challengeId, update)));
    };

    service.on('challengeUpdate', handleChallengeUpdate);

    // Subscribe to new challenges
    const newChallengeIds = challengeIds.filter(id => !previousChallengeIds.current.includes(id));
    if (newChallengeIds.length > 0) {
      service.subscribeToChallenges(newChallengeIds);
    }

    previousChallengeIds.current = [...challengeIds];

    return () => {
      service.off('challengeUpdate', handleChallengeUpdate);
    };
  }, [connected, service, challengeIds]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      // WebSocket service handles subscription cleanup internally
    };
  }, []);

  const getChallengeUpdate = useCallback((challengeId: string) => {
    return challengeUpdates.get(challengeId) || null;
  }, [challengeUpdates]);

  return {
    challengeUpdates: Object.fromEntries(challengeUpdates),
    getChallengeUpdate,
    isLoading: !connected,
    updateCount: challengeUpdates.size
  };
};

/**
 * Hook for sending trading orders via WebSocket
 * WebSocket을 통한 거래 주문 전송을 위한 훅
 */
export const useWebSocketTrading = () => {
  const { connected, service } = useWebSocketConnection();
  const [pendingOrders, setPendingOrders] = useState<Set<string>>(new Set());

  const sendTradeOrder = useCallback(async (orderData: any) => {
    if (!connected || !service) {
      throw new Error('WebSocket not connected');
    }

    const orderId = `order_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    setPendingOrders(prev => new Set(prev.add(orderId)));
    
    try {
      service.sendTradeOrder({
        ...orderData,
        orderId,
        timestamp: Date.now()
      });
      
      return orderId;
    } catch (error) {
      setPendingOrders(prev => {
        const newSet = new Set(prev);
        newSet.delete(orderId);
        return newSet;
      });
      throw error;
    }
  }, [connected, service]);

  // Listen for order confirmations to remove from pending
  useEffect(() => {
    if (!service) return;

    const handleTradingNotification = (notification: TradingNotification) => {
      if (notification.orderId && notification.type === 'ORDER_FILLED') {
        setPendingOrders(prev => {
          const newSet = new Set(prev);
          newSet.delete(notification.orderId!);
          return newSet;
        });
      }
    };

    service.on('tradingNotification', handleTradingNotification);

    return () => {
      service.off('tradingNotification', handleTradingNotification);
    };
  }, [service]);

  return {
    sendTradeOrder,
    pendingOrders: Array.from(pendingOrders),
    canTrade: connected
  };
};

/**
 * Show browser notification for important trading alerts
 * 중요한 거래 알림에 대한 브라우저 알림 표시
 */
const showBrowserNotification = (notification: TradingNotification) => {
  if (!('Notification' in window)) {
    return;
  }

  if (Notification.permission === 'granted') {
    const browserNotification = new Notification(notification.title, {
      body: notification.message,
      icon: '/icon-192x192.png',
      tag: notification.id,
      requireInteraction: notification.priority === 'CRITICAL'
    });

    // Auto-close after 5 seconds for non-critical notifications
    if (notification.priority !== 'CRITICAL') {
      setTimeout(() => {
        browserNotification.close();
      }, 5000);
    }
  } else if (Notification.permission !== 'denied') {
    Notification.requestPermission().then((permission) => {
      if (permission === 'granted') {
        showBrowserNotification(notification);
      }
    });
  }
};