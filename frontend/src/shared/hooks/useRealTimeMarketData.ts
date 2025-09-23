'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { CandlestickData } from '@/shared/ui/charts/ProfessionalTradingChart';

interface MarketDataUpdate {
  instrumentKey: string;
  price: number;
  volume: number;
  timestamp: Date;
  change: number;
  changePercent: number;
}

interface RealTimeMarketDataConfig {
  instruments: string[];
  updateInterval?: number;
  bufferSize?: number;
  enabled?: boolean;
}

interface RealTimeMarketDataState {
  data: Map<string, CandlestickData[]>;
  currentPrices: Map<string, MarketDataUpdate>;
  isConnected: boolean;
  lastUpdate: Date | null;
  error: string | null;
}

/**
 * Real-time market data streaming hook
 * Simulates WebSocket connection for live market data updates
 */
export function useRealTimeMarketData({
  instruments,
  updateInterval = 1000,
  bufferSize = 100,
  enabled = true,
}: RealTimeMarketDataConfig) {
  const [state, setState] = useState<RealTimeMarketDataState>({
    data: new Map(),
    currentPrices: new Map(),
    isConnected: false,
    lastUpdate: null,
    error: null,
  });

  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const dataBufferRef = useRef<Map<string, CandlestickData[]>>(new Map());

  // Initialize data buffers for each instrument
  const initializeData = useCallback(() => {
    const newData = new Map<string, CandlestickData[]>();
    const newPrices = new Map<string, MarketDataUpdate>();

    instruments.forEach((instrument) => {
      // Generate initial historical data
      const historicalData: CandlestickData[] = [];
      const now = new Date();
      const basePrice = 100 + (instrument.charCodeAt(0) - 65) * 50;

      for (let i = bufferSize; i >= 0; i--) {
        const date = new Date(now.getTime() - (i * 60 * 1000));
        const trend = Math.sin((now.getTime() - date.getTime()) / 100000) * 20;
        const open = basePrice + trend + (Math.random() - 0.5) * 5;
        const close = open + (Math.random() - 0.5) * 10;
        const high = Math.max(open, close) + Math.random() * 5;
        const low = Math.min(open, close) - Math.random() * 5;
        const volume = Math.floor(Math.random() * 1000000) + 100000;

        historicalData.push({
          date,
          open,
          high,
          low,
          close,
          volume,
          symbol: instrument,
        });
      }

      newData.set(instrument, historicalData);
      dataBufferRef.current.set(instrument, historicalData);

      // Set current price
      const lastCandle = historicalData[historicalData.length - 1];
      if (lastCandle) {
        newPrices.set(instrument, {
          instrumentKey: instrument,
          price: lastCandle.close,
          volume: lastCandle.volume,
          timestamp: lastCandle.date,
          change: lastCandle.close - lastCandle.open,
          changePercent: ((lastCandle.close - lastCandle.open) / lastCandle.open) * 100,
        });
      }
    });

    setState(prev => ({
      ...prev,
      data: newData,
      currentPrices: newPrices,
      isConnected: true,
      error: null,
    }));
  }, [instruments, bufferSize]);

  // Generate new market data point
  const generateDataPoint = useCallback((instrument: string, prevCandle: CandlestickData): CandlestickData => {
    const now = new Date();
    const timeDiff = now.getTime() - prevCandle.date.getTime();

    // If less than 1 minute, update current candle
    if (timeDiff < 60000) {
      const volatility = 0.02; // 2% volatility
      const priceChange = (Math.random() - 0.5) * volatility * prevCandle.close;
      const newPrice = Math.max(prevCandle.close + priceChange, 0.01);

      return {
        ...prevCandle,
        close: newPrice,
        high: Math.max(prevCandle.high, newPrice),
        low: Math.min(prevCandle.low, newPrice),
        volume: prevCandle.volume + Math.floor(Math.random() * 10000),
      };
    } else {
      // Create new candle
      const volatility = 0.015; // 1.5% volatility
      const priceChange = (Math.random() - 0.5) * volatility * prevCandle.close;
      const open = prevCandle.close;
      const close = Math.max(open + priceChange, 0.01);
      const high = Math.max(open, close) + Math.random() * (close * 0.01);
      const low = Math.min(open, close) - Math.random() * (close * 0.01);
      const volume = Math.floor(Math.random() * 1000000) + 100000;

      return {
        date: new Date(prevCandle.date.getTime() + 60000), // Next minute
        open,
        high,
        low,
        close,
        volume,
        symbol: instrument,
      };
    }
  }, []);

  // Update market data
  const updateMarketData = useCallback(() => {
    setState(prev => {
      const newData = new Map(prev.data);
      const newPrices = new Map(prev.currentPrices);

      instruments.forEach((instrument) => {
        const currentData = dataBufferRef.current.get(instrument);
        if (!currentData || currentData.length === 0) return;

        const lastCandle = currentData[currentData.length - 1];
        const newCandle = generateDataPoint(instrument, lastCandle);

        // Update or add new candle
        const updatedData = [...currentData];
        const timeDiff = newCandle.date.getTime() - lastCandle.date.getTime();

        if (timeDiff < 60000) {
          // Update current candle
          updatedData[updatedData.length - 1] = newCandle;
        } else {
          // Add new candle and maintain buffer size
          updatedData.push(newCandle);
          if (updatedData.length > bufferSize) {
            updatedData.shift();
          }
        }

        dataBufferRef.current.set(instrument, updatedData);
        newData.set(instrument, updatedData);

        // Update current price
        const prevPrice = prev.currentPrices.get(instrument)?.price || lastCandle.close;
        newPrices.set(instrument, {
          instrumentKey: instrument,
          price: newCandle.close,
          volume: newCandle.volume,
          timestamp: newCandle.date,
          change: newCandle.close - prevPrice,
          changePercent: ((newCandle.close - prevPrice) / prevPrice) * 100,
        });
      });

      return {
        ...prev,
        data: newData,
        currentPrices: newPrices,
        lastUpdate: new Date(),
      };
    });
  }, [instruments, generateDataPoint, bufferSize]);

  // Start/stop real-time updates
  useEffect(() => {
    if (!enabled) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      setState(prev => ({ ...prev, isConnected: false }));
      return;
    }

    // Initialize data
    initializeData();

    // Start updates
    intervalRef.current = setInterval(updateMarketData, updateInterval);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [enabled, updateInterval, initializeData, updateMarketData]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  // Subscribe to specific instrument updates
  const subscribeToInstrument = useCallback((instrument: string, callback: (data: MarketDataUpdate) => void) => {
    // In a real implementation, this would establish WebSocket subscription
    // For now, we simulate by calling callback when data updates
    const checkForUpdates = () => {
      const currentPrice = state.currentPrices.get(instrument);
      if (currentPrice) {
        callback(currentPrice);
      }
    };

    const subscriptionInterval = setInterval(checkForUpdates, updateInterval);

    return () => clearInterval(subscriptionInterval);
  }, [state.currentPrices, updateInterval]);

  // Get data for specific instrument
  const getInstrumentData = useCallback((instrument: string): CandlestickData[] => {
    return state.data.get(instrument) || [];
  }, [state.data]);

  // Get current price for instrument
  const getCurrentPrice = useCallback((instrument: string): MarketDataUpdate | null => {
    return state.currentPrices.get(instrument) || null;
  }, [state.currentPrices]);

  // Connection status and controls
  const connect = useCallback(() => {
    setState(prev => ({ ...prev, isConnected: true, error: null }));
  }, []);

  const disconnect = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    setState(prev => ({ ...prev, isConnected: false }));
  }, []);

  return {
    // Data access
    data: state.data,
    currentPrices: state.currentPrices,
    getInstrumentData,
    getCurrentPrice,

    // Subscriptions
    subscribeToInstrument,

    // Connection management
    isConnected: state.isConnected,
    lastUpdate: state.lastUpdate,
    error: state.error,
    connect,
    disconnect,

    // State
    instruments,
    updateInterval,
    bufferSize,
  };
}

export type { MarketDataUpdate, RealTimeMarketDataConfig };