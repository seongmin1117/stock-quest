'use client';

import React, { useState, useRef, useEffect, useCallback } from 'react';

interface ChartDataPoint {
  timestamp: number;
  price: number;
  volume: number;
}

interface MobileChartProps {
  data: ChartDataPoint[];
  symbol: string;
  timeRange: '1D' | '1W' | '1M' | '3M' | '1Y';
  onTimeRangeChange: (range: '1D' | '1W' | '1M' | '3M' | '1Y') => void;
  height?: number;
}

/**
 * Mobile-optimized chart component with touch interactions
 * 모바일 최적화된 차트 컴포넌트 (터치 상호작용 지원)
 */
export const MobileChart: React.FC<MobileChartProps> = ({
  data,
  symbol,
  timeRange,
  onTimeRangeChange,
  height = 280
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isTouch, setIsTouch] = useState(false);
  const [touchPoint, setTouchPoint] = useState<{ x: number; y: number } | null>(null);
  const [selectedDataPoint, setSelectedDataPoint] = useState<ChartDataPoint | null>(null);

  // Generate mock data if not provided
  const chartData = data.length > 0 ? data : generateMockData(timeRange);

  // Calculate chart dimensions and scales
  const padding = { top: 20, right: 20, bottom: 40, left: 60 };
  const chartWidth = 320; // Mobile optimized width
  const chartHeight = height - padding.top - padding.bottom;

  const minPrice = Math.min(...chartData.map(d => d.price));
  const maxPrice = Math.max(...chartData.map(d => d.price));
  const priceRange = maxPrice - minPrice;

  // Touch handling
  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    e.preventDefault();
    setIsTouch(true);
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const touch = e.touches[0];
    const x = touch.clientX - rect.left;
    const y = touch.clientY - rect.top;

    setTouchPoint({ x, y });
    
    // Find closest data point
    const dataIndex = Math.round((x - padding.left) / (chartWidth / (chartData.length - 1)));
    if (dataIndex >= 0 && dataIndex < chartData.length) {
      setSelectedDataPoint(chartData[dataIndex]);
    }
  }, [chartData, chartWidth, padding.left]);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (!isTouch) return;
    e.preventDefault();
    
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const touch = e.touches[0];
    const x = touch.clientX - rect.left;
    const y = touch.clientY - rect.top;

    setTouchPoint({ x, y });

    // Find closest data point
    const dataIndex = Math.round((x - padding.left) / (chartWidth / (chartData.length - 1)));
    if (dataIndex >= 0 && dataIndex < chartData.length) {
      setSelectedDataPoint(chartData[dataIndex]);
    }
  }, [isTouch, chartData, chartWidth, padding.left]);

  const handleTouchEnd = useCallback(() => {
    setIsTouch(false);
    setTimeout(() => {
      setTouchPoint(null);
      setSelectedDataPoint(null);
    }, 2000);
  }, []);

  // Draw chart
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Set canvas size for mobile
    const dpr = window.devicePixelRatio || 1;
    canvas.width = chartWidth * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${chartWidth}px`;
    canvas.style.height = `${height}px`;
    ctx.scale(dpr, dpr);

    // Clear canvas
    ctx.clearRect(0, 0, chartWidth, height);

    // Background
    ctx.fillStyle = '#1e293b';
    ctx.fillRect(0, 0, chartWidth, height);

    // Grid lines
    ctx.strokeStyle = '#374151';
    ctx.lineWidth = 0.5;

    // Horizontal grid lines (price levels)
    for (let i = 0; i <= 5; i++) {
      const y = padding.top + (chartHeight / 5) * i;
      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(chartWidth - padding.right, y);
      ctx.stroke();

      // Price labels
      const price = maxPrice - (priceRange / 5) * i;
      ctx.fillStyle = '#9ca3af';
      ctx.font = '10px monospace';
      ctx.textAlign = 'right';
      ctx.fillText(`$${price.toFixed(2)}`, padding.left - 5, y + 3);
    }

    // Vertical grid lines (time)
    const timeSteps = Math.min(5, chartData.length - 1);
    for (let i = 0; i <= timeSteps; i++) {
      const x = padding.left + (chartWidth - padding.left - padding.right) / timeSteps * i;
      ctx.beginPath();
      ctx.moveTo(x, padding.top);
      ctx.lineTo(x, height - padding.bottom);
      ctx.stroke();

      // Time labels
      if (i < chartData.length) {
        const dataIndex = Math.floor((chartData.length - 1) / timeSteps * i);
        const timestamp = chartData[dataIndex]?.timestamp || Date.now();
        const date = new Date(timestamp);
        const timeLabel = formatTimeLabel(date, timeRange);
        
        ctx.fillStyle = '#9ca3af';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(timeLabel, x, height - padding.bottom + 15);
      }
    }

    // Price line
    ctx.strokeStyle = '#3b82f6';
    ctx.lineWidth = 2;
    ctx.beginPath();

    chartData.forEach((point, index) => {
      const x = padding.left + (index / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
      const y = padding.top + ((maxPrice - point.price) / priceRange) * chartHeight;

      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });

    ctx.stroke();

    // Area fill
    ctx.fillStyle = 'rgba(59, 130, 246, 0.1)';
    ctx.lineTo(chartWidth - padding.right, height - padding.bottom);
    ctx.lineTo(padding.left, height - padding.bottom);
    ctx.closePath();
    ctx.fill();

    // Touch indicator
    if (touchPoint && selectedDataPoint) {
      const dataIndex = chartData.indexOf(selectedDataPoint);
      const x = padding.left + (dataIndex / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
      const y = padding.top + ((maxPrice - selectedDataPoint.price) / priceRange) * chartHeight;

      // Crosshair lines
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)';
      ctx.lineWidth = 1;
      ctx.setLineDash([3, 3]);

      ctx.beginPath();
      ctx.moveTo(x, padding.top);
      ctx.lineTo(x, height - padding.bottom);
      ctx.stroke();

      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(chartWidth - padding.right, y);
      ctx.stroke();

      ctx.setLineDash([]);

      // Touch point
      ctx.fillStyle = '#3b82f6';
      ctx.beginPath();
      ctx.arc(x, y, 4, 0, 2 * Math.PI);
      ctx.fill();

      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2;
      ctx.stroke();
    }

  }, [chartData, touchPoint, selectedDataPoint, timeRange, height, chartWidth, chartHeight, padding, minPrice, maxPrice, priceRange]);

  const formatTimeLabel = (date: Date, range: string): string => {
    switch (range) {
      case '1D':
        return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
      case '1W':
        return date.toLocaleDateString('en-US', { weekday: 'short' });
      case '1M':
      case '3M':
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      case '1Y':
        return date.toLocaleDateString('en-US', { month: 'short' });
      default:
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
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

  const currentPrice = chartData[chartData.length - 1]?.price || 0;
  const previousPrice = chartData[chartData.length - 2]?.price || currentPrice;
  const priceChange = currentPrice - previousPrice;
  const priceChangePercent = ((priceChange / previousPrice) * 100);

  return (
    <div className="bg-slate-800 rounded-lg overflow-hidden">
      {/* Chart header */}
      <div className="p-4 border-b border-slate-700">
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-lg font-bold text-white">{symbol}</h3>
          <div className="text-right">
            <div className="text-xl font-bold text-white mono-font">
              {formatCurrency(currentPrice)}
            </div>
            <div className={`text-sm font-medium ${
              priceChange >= 0 ? 'text-green-400' : 'text-red-400'
            }`}>
              {priceChange >= 0 ? '+' : ''}{formatCurrency(priceChange)} 
              ({priceChange >= 0 ? '+' : ''}{priceChangePercent.toFixed(2)}%)
            </div>
          </div>
        </div>

        {/* Time range selector */}
        <div className="flex bg-slate-700 rounded-lg p-1">
          {['1D', '1W', '1M', '3M', '1Y'].map((range) => (
            <button
              key={range}
              onClick={() => onTimeRangeChange(range as typeof timeRange)}
              className={`flex-1 py-2 px-3 text-xs font-medium rounded-md transition-all duration-200 ${
                timeRange === range
                  ? 'bg-blue-500 text-white'
                  : 'text-gray-300 hover:text-white hover:bg-slate-600'
              }`}
            >
              {range}
            </button>
          ))}
        </div>
      </div>

      {/* Chart canvas */}
      <div className="p-4">
        <canvas
          ref={canvasRef}
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          className="w-full touch-none"
          style={{ maxWidth: '100%' }}
        />
      </div>

      {/* Touch data display */}
      {selectedDataPoint && touchPoint && (
        <div className="absolute bg-slate-900 border border-slate-600 rounded-lg p-3 shadow-lg z-10 pointer-events-none"
             style={{
               left: touchPoint.x > 160 ? touchPoint.x - 120 : touchPoint.x + 10,
               top: touchPoint.y - 60
             }}>
          <div className="text-white font-bold mono-font">
            {formatCurrency(selectedDataPoint.price)}
          </div>
          <div className="text-gray-400 text-sm">
            {new Date(selectedDataPoint.timestamp).toLocaleString('ko-KR')}
          </div>
          <div className="text-gray-400 text-xs">
            거래량: {selectedDataPoint.volume.toLocaleString()}
          </div>
        </div>
      )}

      {/* Chart stats */}
      <div className="p-4 border-t border-slate-700 bg-slate-700">
        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-xs text-gray-400 mb-1">고가</div>
            <div className="text-sm font-bold text-green-400 mono-font">
              {formatCurrency(maxPrice)}
            </div>
          </div>
          <div>
            <div className="text-xs text-gray-400 mb-1">저가</div>
            <div className="text-sm font-bold text-red-400 mono-font">
              {formatCurrency(minPrice)}
            </div>
          </div>
          <div>
            <div className="text-xs text-gray-400 mb-1">거래량</div>
            <div className="text-sm font-bold text-white">
              {chartData[chartData.length - 1]?.volume.toLocaleString() || '0'}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

/**
 * Generate mock chart data for demonstration
 */
function generateMockData(timeRange: string): ChartDataPoint[] {
  const now = Date.now();
  const dataPoints = getDataPointsCount(timeRange);
  const timeInterval = getTimeInterval(timeRange);
  
  let basePrice = 150 + Math.random() * 100;
  const data: ChartDataPoint[] = [];

  for (let i = 0; i < dataPoints; i++) {
    const timestamp = now - (dataPoints - i - 1) * timeInterval;
    
    // Generate realistic price movement
    const volatility = 0.02;
    const trend = (Math.random() - 0.5) * 0.001;
    const change = (Math.random() - 0.5) * volatility + trend;
    
    basePrice = Math.max(basePrice * (1 + change), 1);
    
    const volume = Math.floor(Math.random() * 1000000) + 100000;
    
    data.push({
      timestamp,
      price: basePrice,
      volume
    });
  }

  return data;
}

function getDataPointsCount(timeRange: string): number {
  switch (timeRange) {
    case '1D': return 24;
    case '1W': return 7 * 24;
    case '1M': return 30;
    case '3M': return 90;
    case '1Y': return 365;
    default: return 30;
  }
}

function getTimeInterval(timeRange: string): number {
  switch (timeRange) {
    case '1D': return 60 * 60 * 1000; // 1 hour
    case '1W': return 60 * 60 * 1000; // 1 hour
    case '1M': return 24 * 60 * 60 * 1000; // 1 day
    case '3M': return 24 * 60 * 60 * 1000; // 1 day
    case '1Y': return 24 * 60 * 60 * 1000; // 1 day
    default: return 24 * 60 * 60 * 1000; // 1 day
  }
}

/**
 * Mini chart component for mobile dashboard cards
 * 모바일 대시보드 카드용 미니 차트
 */
export const MiniChart: React.FC<{
  data: number[];
  color?: 'green' | 'red' | 'blue';
  width?: number;
  height?: number;
}> = ({ 
  data, 
  color = 'blue', 
  width = 60, 
  height = 30 
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || data.length === 0) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Set canvas size
    const dpr = window.devicePixelRatio || 1;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    ctx.scale(dpr, dpr);

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    // Calculate scales
    const minValue = Math.min(...data);
    const maxValue = Math.max(...data);
    const valueRange = maxValue - minValue || 1;

    // Draw line
    const colors = {
      green: '#10b981',
      red: '#ef4444',
      blue: '#3b82f6'
    };

    ctx.strokeStyle = colors[color];
    ctx.lineWidth = 2;
    ctx.beginPath();

    data.forEach((value, index) => {
      const x = (index / (data.length - 1)) * width;
      const y = height - ((value - minValue) / valueRange) * height;

      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });

    ctx.stroke();

    // Area fill
    ctx.fillStyle = `${colors[color]}20`;
    ctx.lineTo(width, height);
    ctx.lineTo(0, height);
    ctx.closePath();
    ctx.fill();

  }, [data, color, width, height]);

  return (
    <canvas
      ref={canvasRef}
      className="rounded"
      style={{ width, height }}
    />
  );
};