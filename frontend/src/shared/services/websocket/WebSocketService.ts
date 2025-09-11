import { EventEmitter } from 'events';

export interface MarketDataUpdate {
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: number;
  bid?: number;
  ask?: number;
  high?: number;
  low?: number;
  open?: number;
}

export interface PortfolioUpdate {
  userId: string;
  totalValue: number;
  dailyChange: number;
  dailyChangePercent: number;
  positions: PortfolioPosition[];
  timestamp: number;
}

export interface PortfolioPosition {
  symbol: string;
  shares: number;
  averagePrice: number;
  currentPrice: number;
  totalValue: number;
  unrealizedGainLoss: number;
  unrealizedGainLossPercent: number;
}

export interface TradingNotification {
  id: string;
  type: 'ORDER_FILLED' | 'ORDER_PARTIAL' | 'ORDER_CANCELLED' | 'POSITION_UPDATE' | 'ALERT';
  title: string;
  message: string;
  timestamp: number;
  orderId?: string;
  symbol?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface ChallengeUpdate {
  challengeId: string;
  userId: string;
  rank: number;
  score: number;
  participants: number;
  timeRemaining: number;
  leaderboard: ChallengeParticipant[];
  timestamp: number;
}

export interface ChallengeParticipant {
  userId: string;
  username: string;
  rank: number;
  score: number;
  return: number;
  returnPercent: number;
}

export type WebSocketMessage = 
  | { type: 'MARKET_DATA'; data: MarketDataUpdate }
  | { type: 'PORTFOLIO_UPDATE'; data: PortfolioUpdate }
  | { type: 'TRADING_NOTIFICATION'; data: TradingNotification }
  | { type: 'CHALLENGE_UPDATE'; data: ChallengeUpdate }
  | { type: 'USER_ACTIVITY'; data: any }
  | { type: 'SYSTEM_STATUS'; data: { status: 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING'; message?: string } };

export interface WebSocketServiceConfig {
  url: string;
  reconnectInterval: number;
  maxReconnectAttempts: number;
  heartbeatInterval: number;
  timeout: number;
}

/**
 * WebSocket service for real-time trading data and notifications
 * 실시간 거래 데이터 및 알림을 위한 WebSocket 서비스
 */
export class WebSocketService extends EventEmitter {
  private ws: WebSocket | null = null;
  private config: WebSocketServiceConfig;
  private reconnectAttempts = 0;
  private reconnectTimeout: NodeJS.Timeout | null = null;
  private heartbeatInterval: NodeJS.Timeout | null = null;
  private isConnected = false;
  private subscriptions = new Set<string>();
  private messageQueue: string[] = [];
  private userId: string | null = null;

  constructor(config: WebSocketServiceConfig) {
    super();
    this.config = config;
    this.setupEventListeners();
  }

  /**
   * Connect to WebSocket server
   * WebSocket 서버에 연결
   */
  connect(userId?: string): Promise<void> {
    if (this.userId !== userId) {
      this.userId = userId || null;
    }

    return new Promise((resolve, reject) => {
      try {
        const wsUrl = this.userId 
          ? `${this.config.url}?userId=${this.userId}`
          : this.config.url;
        
        this.ws = new WebSocket(wsUrl);
        
        const timeout = setTimeout(() => {
          reject(new Error('WebSocket connection timeout'));
        }, this.config.timeout);

        this.ws.onopen = () => {
          clearTimeout(timeout);
          this.isConnected = true;
          this.reconnectAttempts = 0;
          
          // Process queued messages
          this.processMessageQueue();
          
          // Start heartbeat
          this.startHeartbeat();
          
          // Re-subscribe to previous subscriptions
          this.resubscribe();
          
          this.emit('connected');
          resolve();
        };

        this.ws.onmessage = (event) => {
          this.handleMessage(event.data);
        };

        this.ws.onclose = (event) => {
          this.handleDisconnect(event);
        };

        this.ws.onerror = (error) => {
          clearTimeout(timeout);
          this.emit('error', error);
          reject(error);
        };

      } catch (error) {
        reject(error);
      }
    });
  }

  /**
   * Disconnect from WebSocket server
   * WebSocket 서버 연결 해제
   */
  disconnect(): void {
    this.isConnected = false;
    
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }

    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }

    if (this.ws) {
      this.ws.close(1000, 'Client disconnect');
      this.ws = null;
    }

    this.emit('disconnected');
  }

  /**
   * Subscribe to market data for specific symbols
   * 특정 심볼의 시장 데이터 구독
   */
  subscribeToMarketData(symbols: string[]): void {
    const message = {
      type: 'SUBSCRIBE_MARKET_DATA',
      symbols: symbols
    };

    symbols.forEach(symbol => this.subscriptions.add(`market_${symbol}`));
    this.sendMessage(JSON.stringify(message));
  }

  /**
   * Unsubscribe from market data
   * 시장 데이터 구독 해제
   */
  unsubscribeFromMarketData(symbols: string[]): void {
    const message = {
      type: 'UNSUBSCRIBE_MARKET_DATA',
      symbols: symbols
    };

    symbols.forEach(symbol => this.subscriptions.delete(`market_${symbol}`));
    this.sendMessage(JSON.stringify(message));
  }

  /**
   * Subscribe to portfolio updates
   * 포트폴리오 업데이트 구독
   */
  subscribeToPortfolio(): void {
    if (!this.userId) {
      console.warn('Cannot subscribe to portfolio without user ID');
      return;
    }

    const message = {
      type: 'SUBSCRIBE_PORTFOLIO',
      userId: this.userId
    };

    this.subscriptions.add('portfolio');
    this.sendMessage(JSON.stringify(message));
  }

  /**
   * Subscribe to trading notifications
   * 거래 알림 구독
   */
  subscribeToNotifications(): void {
    if (!this.userId) {
      console.warn('Cannot subscribe to notifications without user ID');
      return;
    }

    const message = {
      type: 'SUBSCRIBE_NOTIFICATIONS',
      userId: this.userId
    };

    this.subscriptions.add('notifications');
    this.sendMessage(JSON.stringify(message));
  }

  /**
   * Subscribe to challenge updates
   * 챌린지 업데이트 구독
   */
  subscribeToChallenges(challengeIds: string[]): void {
    const message = {
      type: 'SUBSCRIBE_CHALLENGES',
      challengeIds: challengeIds
    };

    challengeIds.forEach(id => this.subscriptions.add(`challenge_${id}`));
    this.sendMessage(JSON.stringify(message));
  }

  /**
   * Send trading order via WebSocket for real-time processing
   * 실시간 처리를 위한 거래 주문 WebSocket 전송
   */
  sendTradeOrder(orderData: any): void {
    const message = {
      type: 'TRADE_ORDER',
      userId: this.userId,
      orderData: orderData,
      timestamp: Date.now()
    };

    this.sendMessage(JSON.stringify(message));
  }

  /**
   * Get connection status
   * 연결 상태 확인
   */
  getConnectionStatus(): { 
    connected: boolean; 
    reconnectAttempts: number; 
    subscriptions: string[] 
  } {
    return {
      connected: this.isConnected,
      reconnectAttempts: this.reconnectAttempts,
      subscriptions: Array.from(this.subscriptions)
    };
  }

  /**
   * Handle incoming WebSocket messages
   * 수신된 WebSocket 메시지 처리
   */
  private handleMessage(data: string): void {
    try {
      const message: WebSocketMessage = JSON.parse(data);

      switch (message.type) {
        case 'MARKET_DATA':
          this.emit('marketData', message.data);
          break;
        
        case 'PORTFOLIO_UPDATE':
          this.emit('portfolioUpdate', message.data);
          break;
        
        case 'TRADING_NOTIFICATION':
          this.emit('tradingNotification', message.data);
          break;
        
        case 'CHALLENGE_UPDATE':
          this.emit('challengeUpdate', message.data);
          break;
        
        case 'USER_ACTIVITY':
          this.emit('userActivity', message.data);
          break;
        
        case 'SYSTEM_STATUS':
          this.emit('systemStatus', message.data);
          break;
        
        default:
          console.warn('Unknown message type:', message);
      }

    } catch (error) {
      console.error('Failed to parse WebSocket message:', error);
      this.emit('error', new Error('Failed to parse message'));
    }
  }

  /**
   * Handle WebSocket disconnect
   * WebSocket 연결 해제 처리
   */
  private handleDisconnect(event: CloseEvent): void {
    this.isConnected = false;
    
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }

    this.emit('disconnected', {
      code: event.code,
      reason: event.reason,
      wasClean: event.wasClean
    });

    // Attempt reconnection if not a clean close
    if (event.code !== 1000 && this.reconnectAttempts < this.config.maxReconnectAttempts) {
      this.scheduleReconnect();
    }
  }

  /**
   * Schedule reconnection attempt
   * 재연결 시도 예약
   */
  private scheduleReconnect(): void {
    this.reconnectAttempts++;
    
    const delay = Math.min(
      this.config.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1),
      30000 // Max 30 seconds
    );

    this.emit('reconnecting', {
      attempt: this.reconnectAttempts,
      maxAttempts: this.config.maxReconnectAttempts,
      delay: delay
    });

    this.reconnectTimeout = setTimeout(() => {
      this.connect(this.userId || undefined).catch((error) => {
        console.error('Reconnection failed:', error);
        
        if (this.reconnectAttempts < this.config.maxReconnectAttempts) {
          this.scheduleReconnect();
        } else {
          this.emit('reconnectFailed');
        }
      });
    }, delay);
  }

  /**
   * Send message to WebSocket server
   * WebSocket 서버에 메시지 전송
   */
  private sendMessage(message: string): void {
    if (this.isConnected && this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(message);
    } else {
      // Queue message for when connection is restored
      this.messageQueue.push(message);
    }
  }

  /**
   * Process queued messages
   * 대기 중인 메시지 처리
   */
  private processMessageQueue(): void {
    while (this.messageQueue.length > 0 && this.isConnected) {
      const message = this.messageQueue.shift();
      if (message && this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(message);
      }
    }
  }

  /**
   * Re-subscribe to previous subscriptions
   * 이전 구독 재등록
   */
  private resubscribe(): void {
    const marketSymbols: string[] = [];
    const challengeIds: string[] = [];

    this.subscriptions.forEach(subscription => {
      if (subscription.startsWith('market_')) {
        marketSymbols.push(subscription.replace('market_', ''));
      } else if (subscription.startsWith('challenge_')) {
        challengeIds.push(subscription.replace('challenge_', ''));
      } else if (subscription === 'portfolio') {
        this.subscribeToPortfolio();
      } else if (subscription === 'notifications') {
        this.subscribeToNotifications();
      }
    });

    if (marketSymbols.length > 0) {
      this.subscribeToMarketData(marketSymbols);
    }

    if (challengeIds.length > 0) {
      this.subscribeToChallenges(challengeIds);
    }
  }

  /**
   * Start heartbeat to keep connection alive
   * 연결 유지를 위한 하트비트 시작
   */
  private startHeartbeat(): void {
    this.heartbeatInterval = setInterval(() => {
      if (this.isConnected && this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'PING', timestamp: Date.now() }));
      }
    }, this.config.heartbeatInterval);
  }

  /**
   * Setup global event listeners
   * 전역 이벤트 리스너 설정
   */
  private setupEventListeners(): void {
    // Handle page visibility changes
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        // Page is hidden, reduce activity
        this.emit('backgrounded');
      } else {
        // Page is visible, resume full activity
        this.emit('foregrounded');
        
        // Reconnect if needed
        if (!this.isConnected) {
          this.connect(this.userId || undefined);
        }
      }
    });

    // Handle online/offline events
    window.addEventListener('online', () => {
      this.emit('networkOnline');
      if (!this.isConnected) {
        this.connect(this.userId || undefined);
      }
    });

    window.addEventListener('offline', () => {
      this.emit('networkOffline');
    });
  }
}

/**
 * Singleton WebSocket service instance
 * WebSocket 서비스 싱글톤 인스턴스
 */
class WebSocketManager {
  private static instance: WebSocketService | null = null;
  
  static getInstance(config?: WebSocketServiceConfig): WebSocketService {
    if (!WebSocketManager.instance) {
      const defaultConfig: WebSocketServiceConfig = {
        url: process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws',
        reconnectInterval: 1000,
        maxReconnectAttempts: 5,
        heartbeatInterval: 30000,
        timeout: 10000
      };
      
      WebSocketManager.instance = new WebSocketService(config || defaultConfig);
    }
    
    return WebSocketManager.instance;
  }
  
  static destroy(): void {
    if (WebSocketManager.instance) {
      WebSocketManager.instance.disconnect();
      WebSocketManager.instance.removeAllListeners();
      WebSocketManager.instance = null;
    }
  }
}

export default WebSocketManager;