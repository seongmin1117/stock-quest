'use client';

import React, { useState, useRef, useEffect, useCallback, useMemo } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  ToggleButtonGroup,
  ToggleButton,
  Tooltip,
  useTheme,
  alpha,
  Fade,
  Zoom
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Timeline,
  TouchApp,
  ZoomIn
} from '@mui/icons-material';

interface ChartDataPoint {
  timestamp: number;
  price: number;
  volume: number;
  open?: number;
  high?: number;
  low?: number;
  close?: number;
}

interface EnhancedChartProps {
  data: ChartDataPoint[];
  symbol: string;
  timeRange: '1D' | '1W' | '1M' | '3M' | '1Y';
  onTimeRangeChange: (range: '1D' | '1W' | '1M' | '3M' | '1Y') => void;
  height?: number;
  showVolume?: boolean;
  showTooltips?: boolean;
  enableZoom?: boolean;
  enablePinch?: boolean;
  style?: 'line' | 'candlestick' | 'area';
}

interface TouchState {
  x: number;
  y: number;
  isActive: boolean;
  startDistance?: number;
  scale: number;
  panX: number;
}

/**
 * Enhanced chart component with advanced mobile interactions
 * 고급 모바일 상호작용이 있는 향상된 차트 컴포넌트
 */
export const EnhancedChart: React.FC<EnhancedChartProps> = ({
  data,
  symbol,
  timeRange,
  onTimeRangeChange,
  height = 320,
  showVolume = true,
  showTooltips = true,
  enableZoom = true,
  enablePinch = true,
  style = 'line'
}) => {
  const theme = useTheme();
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const overlayRef = useRef<HTMLDivElement>(null);

  const [touchState, setTouchState] = useState<TouchState>({
    x: 0,
    y: 0,
    isActive: false,
    scale: 1,
    panX: 0
  });

  const [selectedDataPoint, setSelectedDataPoint] = useState<ChartDataPoint | null>(null);
  const [showCrosshair, setShowCrosshair] = useState(false);
  const [hoverPosition, setHoverPosition] = useState<{ x: number; y: number } | null>(null);

  // Chart configuration
  const padding = { top: 20, right: 60, bottom: showVolume ? 80 : 40, left: 60 };
  const chartWidth = 360; // Mobile optimized
  const mainChartHeight = showVolume ? height * 0.7 : height - padding.top - padding.bottom;
  const volumeHeight = showVolume ? height * 0.2 : 0;

  // Generate enhanced mock data if needed
  const chartData = useMemo(() => {
    if (data.length > 0) return data;
    return generateEnhancedMockData(timeRange);
  }, [data, timeRange]);

  // Calculate price and volume ranges
  const priceStats = useMemo(() => {
    const prices = chartData.map(d => d.price);
    const volumes = chartData.map(d => d.volume);

    return {
      minPrice: Math.min(...prices),
      maxPrice: Math.max(...prices),
      priceRange: Math.max(...prices) - Math.min(...prices),
      maxVolume: Math.max(...volumes),
      currentPrice: prices[prices.length - 1] || 0,
      previousPrice: prices[prices.length - 2] || 0
    };
  }, [chartData]);

  // Enhanced touch handling with gesture support
  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    e.preventDefault();
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const touches = e.touches;

    if (touches.length === 1) {
      // Single touch - crosshair
      const touch = touches[0];
      const x = (touch.clientX - rect.left) * touchState.scale - touchState.panX;
      const y = touch.clientY - rect.top;

      setTouchState(prev => ({ ...prev, x, y, isActive: true }));
      setShowCrosshair(true);
      updateSelectedDataPoint(x);
    } else if (touches.length === 2 && enablePinch) {
      // Pinch gesture
      const touch1 = touches[0];
      const touch2 = touches[1];
      const distance = Math.sqrt(
        Math.pow(touch2.clientX - touch1.clientX, 2) +
        Math.pow(touch2.clientY - touch1.clientY, 2)
      );

      setTouchState(prev => ({
        ...prev,
        startDistance: distance,
        isActive: true
      }));
    }
  }, [touchState.scale, touchState.panX, enablePinch]);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (!touchState.isActive) return;
    e.preventDefault();

    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const touches = e.touches;

    if (touches.length === 1) {
      // Single touch move - crosshair tracking
      const touch = touches[0];
      const x = (touch.clientX - rect.left) * touchState.scale - touchState.panX;
      const y = touch.clientY - rect.top;

      setTouchState(prev => ({ ...prev, x, y }));
      updateSelectedDataPoint(x);
    } else if (touches.length === 2 && enablePinch && touchState.startDistance) {
      // Pinch zoom
      const touch1 = touches[0];
      const touch2 = touches[1];
      const distance = Math.sqrt(
        Math.pow(touch2.clientX - touch1.clientX, 2) +
        Math.pow(touch2.clientY - touch1.clientY, 2)
      );

      const scale = Math.max(0.5, Math.min(3, distance / touchState.startDistance));
      setTouchState(prev => ({ ...prev, scale }));
    }
  }, [touchState.isActive, touchState.startDistance, touchState.scale, touchState.panX, enablePinch]);

  const handleTouchEnd = useCallback(() => {
    setTouchState(prev => ({ ...prev, isActive: false, startDistance: undefined }));

    // Hide crosshair after delay
    setTimeout(() => {
      setShowCrosshair(false);
      setSelectedDataPoint(null);
      setHoverPosition(null);
    }, 2000);
  }, []);

  const updateSelectedDataPoint = useCallback((x: number) => {
    const dataIndex = Math.round(
      ((x - padding.left) / (chartWidth - padding.left - padding.right)) * (chartData.length - 1)
    );

    if (dataIndex >= 0 && dataIndex < chartData.length) {
      setSelectedDataPoint(chartData[dataIndex]);

      // Update hover position for tooltip
      const tooltipX = padding.left + (dataIndex / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
      const tooltipY = padding.top + ((priceStats.maxPrice - chartData[dataIndex].price) / priceStats.priceRange) * mainChartHeight;

      setHoverPosition({ x: tooltipX, y: tooltipY });
    }
  }, [chartData, padding, chartWidth, mainChartHeight, priceStats]);

  // Enhanced drawing function
  const drawChart = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Setup canvas
    const dpr = window.devicePixelRatio || 1;
    canvas.width = chartWidth * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${chartWidth}px`;
    canvas.style.height = `${height}px`;
    ctx.scale(dpr, dpr);

    // Clear canvas
    ctx.clearRect(0, 0, chartWidth, height);

    // Background gradient
    const bgGradient = ctx.createLinearGradient(0, 0, 0, height);
    bgGradient.addColorStop(0, theme.palette.background.paper);
    bgGradient.addColorStop(1, alpha(theme.palette.background.paper, 0.8));
    ctx.fillStyle = bgGradient;
    ctx.fillRect(0, 0, chartWidth, height);

    // Grid lines with improved styling
    drawGrid(ctx);

    // Price chart based on style
    switch (style) {
      case 'line':
        drawLineChart(ctx);
        break;
      case 'area':
        drawAreaChart(ctx);
        break;
      case 'candlestick':
        drawCandlestickChart(ctx);
        break;
    }

    // Volume chart
    if (showVolume) {
      drawVolumeChart(ctx);
    }

    // Crosshair and interactive elements
    if (showCrosshair && touchState.isActive && selectedDataPoint) {
      drawCrosshair(ctx);
    }

    // Price line indicator
    drawCurrentPriceIndicator(ctx);

  }, [
    chartWidth, height, theme, style, showVolume, showCrosshair,
    touchState, selectedDataPoint, chartData, priceStats, mainChartHeight, padding
  ]);

  const drawGrid = (ctx: CanvasRenderingContext2D) => {
    ctx.strokeStyle = alpha(theme.palette.divider, 0.3);
    ctx.lineWidth = 0.5;

    // Horizontal grid lines (price levels)
    for (let i = 0; i <= 5; i++) {
      const y = padding.top + (mainChartHeight / 5) * i;
      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(chartWidth - padding.right, y);
      ctx.stroke();

      // Price labels with better formatting
      const price = priceStats.maxPrice - (priceStats.priceRange / 5) * i;
      ctx.fillStyle = theme.palette.text.secondary;
      ctx.font = '11px Inter, sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(`$${price.toFixed(2)}`, padding.left - 8, y + 4);
    }

    // Vertical grid lines (time)
    const timeSteps = Math.min(4, chartData.length - 1);
    for (let i = 0; i <= timeSteps; i++) {
      const x = padding.left + ((chartWidth - padding.left - padding.right) / timeSteps) * i;
      ctx.beginPath();
      ctx.moveTo(x, padding.top);
      ctx.lineTo(x, padding.top + mainChartHeight);
      ctx.stroke();

      // Time labels
      if (i < chartData.length) {
        const dataIndex = Math.floor((chartData.length - 1) / timeSteps * i);
        const timestamp = chartData[dataIndex]?.timestamp || Date.now();
        const timeLabel = formatTimeLabel(new Date(timestamp), timeRange);

        ctx.fillStyle = theme.palette.text.secondary;
        ctx.font = '10px Inter, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(timeLabel, x, padding.top + mainChartHeight + 20);
      }
    }
  };

  const drawLineChart = (ctx: CanvasRenderingContext2D) => {
    if (chartData.length === 0) return;

    // Create gradient for line
    const lineGradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + mainChartHeight);
    const isPositive = priceStats.currentPrice >= priceStats.previousPrice;
    const color = isPositive ? theme.palette.success.main : theme.palette.error.main;

    lineGradient.addColorStop(0, color);
    lineGradient.addColorStop(1, alpha(color, 0.6));

    // Draw price line with smooth curves
    ctx.strokeStyle = lineGradient;
    ctx.lineWidth = 2.5;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.beginPath();

    chartData.forEach((point, index) => {
      const x = padding.left + (index / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
      const y = padding.top + ((priceStats.maxPrice - point.price) / priceStats.priceRange) * mainChartHeight;

      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        // Smooth curve using quadratic curves
        const prevX = padding.left + ((index - 1) / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
        const prevY = padding.top + ((priceStats.maxPrice - chartData[index - 1].price) / priceStats.priceRange) * mainChartHeight;
        const cpX = (prevX + x) / 2;

        ctx.quadraticCurveTo(cpX, prevY, x, y);
      }
    });

    ctx.stroke();

    // Add glow effect
    ctx.shadowColor = color;
    ctx.shadowBlur = 3;
    ctx.stroke();
    ctx.shadowBlur = 0;
  };

  const drawAreaChart = (ctx: CanvasRenderingContext2D) => {
    if (chartData.length === 0) return;

    const isPositive = priceStats.currentPrice >= priceStats.previousPrice;
    const color = isPositive ? theme.palette.success.main : theme.palette.error.main;

    // Area fill gradient
    const areaGradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + mainChartHeight);
    areaGradient.addColorStop(0, alpha(color, 0.3));
    areaGradient.addColorStop(1, alpha(color, 0.05));

    ctx.fillStyle = areaGradient;
    ctx.beginPath();

    chartData.forEach((point, index) => {
      const x = padding.left + (index / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
      const y = padding.top + ((priceStats.maxPrice - point.price) / priceStats.priceRange) * mainChartHeight;

      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });

    // Close area to bottom
    ctx.lineTo(chartWidth - padding.right, padding.top + mainChartHeight);
    ctx.lineTo(padding.left, padding.top + mainChartHeight);
    ctx.closePath();
    ctx.fill();

    // Draw line on top
    drawLineChart(ctx);
  };

  const drawCandlestickChart = (ctx: CanvasRenderingContext2D) => {
    const candleWidth = Math.max(2, (chartWidth - padding.left - padding.right) / chartData.length * 0.7);

    chartData.forEach((point, index) => {
      const x = padding.left + (index / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);

      const open = point.open || point.price;
      const high = point.high || point.price * 1.02;
      const low = point.low || point.price * 0.98;
      const close = point.close || point.price;

      const openY = padding.top + ((priceStats.maxPrice - open) / priceStats.priceRange) * mainChartHeight;
      const highY = padding.top + ((priceStats.maxPrice - high) / priceStats.priceRange) * mainChartHeight;
      const lowY = padding.top + ((priceStats.maxPrice - low) / priceStats.priceRange) * mainChartHeight;
      const closeY = padding.top + ((priceStats.maxPrice - close) / priceStats.priceRange) * mainChartHeight;

      const isPositive = close >= open;
      const color = isPositive ? theme.palette.success.main : theme.palette.error.main;

      // High-low line
      ctx.strokeStyle = color;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(x, highY);
      ctx.lineTo(x, lowY);
      ctx.stroke();

      // Open-close body
      const bodyTop = Math.min(openY, closeY);
      const bodyHeight = Math.abs(openY - closeY) || 1;

      ctx.fillStyle = isPositive ? color : theme.palette.background.paper;
      ctx.strokeStyle = color;
      ctx.lineWidth = 1;

      ctx.fillRect(x - candleWidth / 2, bodyTop, candleWidth, bodyHeight);
      ctx.strokeRect(x - candleWidth / 2, bodyTop, candleWidth, bodyHeight);
    });
  };

  const drawVolumeChart = (ctx: CanvasRenderingContext2D) => {
    const volumeTop = padding.top + mainChartHeight + 10;
    const barWidth = Math.max(1, (chartWidth - padding.left - padding.right) / chartData.length * 0.8);

    chartData.forEach((point, index) => {
      const x = padding.left + (index / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
      const barHeight = (point.volume / priceStats.maxVolume) * volumeHeight;

      ctx.fillStyle = alpha(theme.palette.primary.main, 0.6);
      ctx.fillRect(x - barWidth / 2, volumeTop + volumeHeight - barHeight, barWidth, barHeight);
    });

    // Volume label
    ctx.fillStyle = theme.palette.text.secondary;
    ctx.font = '10px Inter, sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText('Volume', padding.left, volumeTop - 5);
  };

  const drawCrosshair = (ctx: CanvasRenderingContext2D) => {
    if (!selectedDataPoint) return;

    const dataIndex = chartData.indexOf(selectedDataPoint);
    const x = padding.left + (dataIndex / (chartData.length - 1)) * (chartWidth - padding.left - padding.right);
    const y = padding.top + ((priceStats.maxPrice - selectedDataPoint.price) / priceStats.priceRange) * mainChartHeight;

    // Crosshair lines
    ctx.strokeStyle = alpha(theme.palette.primary.main, 0.8);
    ctx.lineWidth = 1;
    ctx.setLineDash([3, 3]);

    // Vertical line
    ctx.beginPath();
    ctx.moveTo(x, padding.top);
    ctx.lineTo(x, padding.top + mainChartHeight);
    ctx.stroke();

    // Horizontal line
    ctx.beginPath();
    ctx.moveTo(padding.left, y);
    ctx.lineTo(chartWidth - padding.right, y);
    ctx.stroke();

    ctx.setLineDash([]);

    // Intersection point
    ctx.fillStyle = theme.palette.primary.main;
    ctx.beginPath();
    ctx.arc(x, y, 4, 0, 2 * Math.PI);
    ctx.fill();

    ctx.strokeStyle = theme.palette.background.paper;
    ctx.lineWidth = 2;
    ctx.stroke();
  };

  const drawCurrentPriceIndicator = (ctx: CanvasRenderingContext2D) => {
    const y = padding.top + ((priceStats.maxPrice - priceStats.currentPrice) / priceStats.priceRange) * mainChartHeight;
    const isPositive = priceStats.currentPrice >= priceStats.previousPrice;
    const color = isPositive ? theme.palette.success.main : theme.palette.error.main;

    // Price line
    ctx.strokeStyle = alpha(color, 0.7);
    ctx.lineWidth = 1;
    ctx.setLineDash([5, 5]);
    ctx.beginPath();
    ctx.moveTo(padding.left, y);
    ctx.lineTo(chartWidth - padding.right, y);
    ctx.stroke();
    ctx.setLineDash([]);

    // Price label
    ctx.fillStyle = color;
    ctx.fillRect(chartWidth - padding.right + 2, y - 10, 50, 20);

    ctx.fillStyle = theme.palette.background.paper;
    ctx.font = 'bold 11px Inter, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(`$${priceStats.currentPrice.toFixed(2)}`, chartWidth - padding.right + 27, y + 3);
  };

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

  // Draw chart when data or settings change
  useEffect(() => {
    drawChart();
  }, [drawChart]);

  const priceChange = priceStats.currentPrice - priceStats.previousPrice;
  const priceChangePercent = ((priceChange / priceStats.previousPrice) * 100);
  const isPositive = priceChange >= 0;

  return (
    <Card sx={{
      backgroundColor: theme.palette.background.paper,
      border: `1px solid ${theme.palette.divider}`,
      borderRadius: 2,
      overflow: 'hidden',
      position: 'relative'
    }}>
      {/* Chart header */}
      <CardContent sx={{ pb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              {symbol}
            </Typography>
            {isPositive ? (
              <TrendingUp sx={{ color: theme.palette.success.main }} />
            ) : (
              <TrendingDown sx={{ color: theme.palette.error.main }} />
            )}
          </Box>

          <Box sx={{ textAlign: 'right' }}>
            <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>
              {formatCurrency(priceStats.currentPrice)}
            </Typography>
            <Typography
              variant="body2"
              sx={{
                color: isPositive ? theme.palette.success.main : theme.palette.error.main,
                fontWeight: 600,
                fontFamily: 'monospace'
              }}
            >
              {isPositive ? '+' : ''}{formatCurrency(priceChange)}
              ({isPositive ? '+' : ''}{priceChangePercent.toFixed(2)}%)
            </Typography>
          </Box>
        </Box>

        {/* Time range selector */}
        <ToggleButtonGroup
          value={timeRange}
          exclusive
          onChange={(_, newRange) => newRange && onTimeRangeChange(newRange)}
          size="small"
          sx={{ mb: 2 }}
        >
          {['1D', '1W', '1M', '3M', '1Y'].map((range) => (
            <ToggleButton
              key={range}
              value={range}
              sx={{
                px: 2,
                py: 0.5,
                fontSize: '0.75rem',
                fontWeight: 600,
                border: `1px solid ${theme.palette.divider}`,
                '&.Mui-selected': {
                  backgroundColor: theme.palette.primary.main,
                  color: theme.palette.primary.contrastText,
                  '&:hover': {
                    backgroundColor: theme.palette.primary.dark,
                  }
                }
              }}
            >
              {range}
            </ToggleButton>
          ))}
        </ToggleButtonGroup>

        {/* Style selector */}
        <ToggleButtonGroup
          value={style}
          exclusive
          onChange={(_, newStyle) => {
            // Style change would need to be handled by parent component
            // For now, this chart uses the style prop value
          }}
          size="small"
        >
          <ToggleButton value="line">
            <Timeline />
          </ToggleButton>
          <ToggleButton value="area">
            <TrendingUp />
          </ToggleButton>
          <ToggleButton value="candlestick">
            <Box sx={{ width: 16, height: 16, backgroundColor: 'currentColor' }} />
          </ToggleButton>
        </ToggleButtonGroup>
      </CardContent>

      {/* Chart canvas container */}
      <Box sx={{ position: 'relative', p: 2, pt: 0 }}>
        <canvas
          ref={canvasRef}
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          className="w-full touch-none"
          style={{
            maxWidth: '100%',
            cursor: enableZoom ? 'crosshair' : 'default'
          }}
        />

        {/* Tooltip */}
        {showTooltips && selectedDataPoint && hoverPosition && showCrosshair && (
          <Zoom in>
            <Box
              sx={{
                position: 'absolute',
                left: hoverPosition.x > chartWidth / 2 ? hoverPosition.x - 140 : hoverPosition.x + 10,
                top: hoverPosition.y - 80,
                backgroundColor: alpha(theme.palette.background.default, 0.95),
                border: `1px solid ${theme.palette.divider}`,
                borderRadius: 2,
                p: 2,
                minWidth: 120,
                backdropFilter: 'blur(10px)',
                zIndex: 10,
                pointerEvents: 'none'
              }}
            >
              <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>
                {formatCurrency(selectedDataPoint.price)}
              </Typography>
              <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                {new Date(selectedDataPoint.timestamp).toLocaleString('ko-KR')}
              </Typography>
              <Typography variant="caption" sx={{ display: 'block', color: theme.palette.text.secondary }}>
                거래량: {selectedDataPoint.volume.toLocaleString()}
              </Typography>
            </Box>
          </Zoom>
        )}

        {/* Touch indicator */}
        {enableZoom && (
          <Box
            sx={{
              position: 'absolute',
              bottom: 8,
              right: 8,
              display: 'flex',
              alignItems: 'center',
              gap: 0.5,
              backgroundColor: alpha(theme.palette.background.default, 0.8),
              borderRadius: 1,
              px: 1,
              py: 0.5,
              fontSize: '0.75rem',
              color: theme.palette.text.secondary
            }}
          >
            <TouchApp sx={{ fontSize: 16 }} />
            <span>터치하여 상세보기</span>
          </Box>
        )}
      </Box>

      {/* Chart statistics */}
      <CardContent sx={{
        pt: 0,
        backgroundColor: alpha(theme.palette.background.default, 0.3),
        borderTop: `1px solid ${theme.palette.divider}`
      }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2, textAlign: 'center' }}>
          <Box>
            <Typography variant="caption" sx={{ color: theme.palette.text.secondary, display: 'block' }}>
              고가
            </Typography>
            <Typography variant="body2" sx={{
              fontWeight: 700,
              fontFamily: 'monospace',
              color: theme.palette.success.main
            }}>
              {formatCurrency(priceStats.maxPrice)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" sx={{ color: theme.palette.text.secondary, display: 'block' }}>
              저가
            </Typography>
            <Typography variant="body2" sx={{
              fontWeight: 700,
              fontFamily: 'monospace',
              color: theme.palette.error.main
            }}>
              {formatCurrency(priceStats.minPrice)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" sx={{ color: theme.palette.text.secondary, display: 'block' }}>
              거래량
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 700 }}>
              {priceStats.maxVolume.toLocaleString()}
            </Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

/**
 * Generate enhanced mock data with OHLCV
 */
function generateEnhancedMockData(timeRange: string): ChartDataPoint[] {
  const now = Date.now();
  const dataPoints = getDataPointsCount(timeRange);
  const timeInterval = getTimeInterval(timeRange);

  let basePrice = 150 + Math.random() * 100;
  const data: ChartDataPoint[] = [];

  for (let i = 0; i < dataPoints; i++) {
    const timestamp = now - (dataPoints - i - 1) * timeInterval;

    // Generate realistic OHLCV data
    const volatility = 0.02;
    const trend = (Math.random() - 0.5) * 0.001;
    const change = (Math.random() - 0.5) * volatility + trend;

    const open = basePrice;
    const close = Math.max(basePrice * (1 + change), 1);
    const high = Math.max(open, close) * (1 + Math.random() * 0.01);
    const low = Math.min(open, close) * (1 - Math.random() * 0.01);

    basePrice = close;

    const volume = Math.floor(Math.random() * 1000000) + 100000;

    data.push({
      timestamp,
      price: close,
      volume,
      open,
      high,
      low,
      close
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