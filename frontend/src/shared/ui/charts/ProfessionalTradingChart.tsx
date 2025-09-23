'use client';

import React, { useRef, useEffect, useState, useCallback, useMemo } from 'react';
import * as d3 from 'd3';
import {
  Box,
  IconButton,
  ToggleButton,
  ToggleButtonGroup,
  Paper,
  Typography,
  Tooltip,
  useTheme,
  alpha,
} from '@mui/material';
import {
  ZoomIn,
  ZoomOut,
  CenterFocusStrong,
  Timeline,
  BarChart,
  ShowChart,
  Fullscreen,
  FullscreenExit,
} from '@mui/icons-material';
import { usePerformance, useOptimizedRendering } from '../performance/PerformanceOptimizer';
import { useAccessibility } from '../accessibility/AccessibilityProvider';
import { format } from 'date-fns';

export interface CandlestickData {
  date: Date;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  symbol?: string;
}

export interface TechnicalIndicator {
  name: string;
  color: string;
  data: Array<{ date: Date; value: number }>;
}

interface ProfessionalTradingChartProps {
  data: CandlestickData[];
  width?: number;
  height?: number;
  indicators?: TechnicalIndicator[];
  showVolume?: boolean;
  showGrid?: boolean;
  showCrosshair?: boolean;
  onDataPointClick?: (data: CandlestickData) => void;
  onZoomChange?: (domain: [Date, Date]) => void;
  realTimeUpdate?: boolean;
  theme?: 'light' | 'dark';
}

/**
 * Professional-grade trading chart with D3.js
 * D3.js를 사용한 전문급 트레이딩 차트
 */
export const ProfessionalTradingChart: React.FC<ProfessionalTradingChartProps> = ({
  data = [],
  width = 800,
  height = 500,
  indicators = [],
  showVolume = true,
  showGrid = true,
  showCrosshair = true,
  onDataPointClick,
  onZoomChange,
  realTimeUpdate = false,
  theme: chartTheme = 'dark',
}) => {
  const theme = useTheme();
  const svgRef = useRef<SVGSVGElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const { measureInteraction } = usePerformance();
  const { shouldReduceMotion, shouldReduceEffects, optimizationLevel } = useOptimizedRendering();
  const { announce } = useAccessibility();

  // Chart state
  const [chartType, setChartType] = useState<'candlestick' | 'line' | 'area'>('candlestick');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [zoomLevel, setZoomLevel] = useState(1);
  const [selectedDataPoint, setSelectedDataPoint] = useState<CandlestickData | null>(null);
  const [currentDomain, setCurrentDomain] = useState<[Date, Date] | null>(null);

  // Chart dimensions and margins
  const margin = useMemo(() => ({
    top: 20,
    right: 60,
    bottom: showVolume ? 120 : 60,
    left: 60,
  }), [showVolume]);

  const dimensions = useMemo(() => ({
    width: width - margin.left - margin.right,
    height: height - margin.top - margin.bottom,
    volumeHeight: showVolume ? 80 : 0,
  }), [width, height, margin, showVolume]);

  // Performance-optimized data processing
  const processedData = useMemo(() => {
    if (!data.length) return [];

    // Apply data filtering based on optimization level
    let filteredData = data;

    if (optimizationLevel === 'aggressive' && data.length > 500) {
      // For low-end devices, sample data to reduce rendering load
      const step = Math.ceil(data.length / 500);
      filteredData = data.filter((_, index) => index % step === 0);
    }

    return filteredData.sort((a, b) => a.date.getTime() - b.date.getTime());
  }, [data, optimizationLevel]);

  // Scales
  const scales = useMemo(() => {
    if (!processedData.length) return null;

    const xExtent = d3.extent(processedData, d => d.date) as [Date, Date];
    const yExtent = d3.extent(processedData, d => Math.max(d.high, d.low)) as [number, number];
    const volumeExtent = d3.extent(processedData, d => d.volume) as [number, number];

    const xScale = d3.scaleTime()
      .domain(currentDomain || xExtent)
      .range([0, dimensions.width]);

    const yScale = d3.scaleLinear()
      .domain([yExtent[0] * 0.95, yExtent[1] * 1.05])
      .range([dimensions.height - dimensions.volumeHeight - 20, 0]);

    const volumeScale = d3.scaleLinear()
      .domain([0, volumeExtent[1]])
      .range([dimensions.height - dimensions.volumeHeight, dimensions.height - 20]);

    return { xScale, yScale, volumeScale, xExtent, yExtent };
  }, [processedData, dimensions, currentDomain]);

  // Color scheme based on theme
  const colors = useMemo(() => {
    const baseColors = chartTheme === 'dark' ? {
      background: '#0A0E18',
      grid: '#2A3441',
      text: '#FFFFFF',
      textSecondary: '#78828A',
      bullish: '#4CAF50',
      bearish: '#F44336',
      volume: '#2196F3',
      crosshair: '#FFC107',
    } : {
      background: '#FFFFFF',
      grid: '#E0E0E0',
      text: '#000000',
      textSecondary: '#666666',
      bullish: '#4CAF50',
      bearish: '#F44336',
      volume: '#2196F3',
      crosshair: '#FF9800',
    };

    return baseColors;
  }, [chartTheme]);

  // Draw candlestick chart
  const drawCandlesticks = useCallback((svg: d3.Selection<SVGGElement, unknown, null, undefined>) => {
    if (!scales || !processedData.length) return;

    const { xScale, yScale } = scales;
    const candleWidth = Math.max(2, Math.min(10, dimensions.width / processedData.length * 0.8));

    // Remove existing candlesticks
    svg.selectAll('.candlestick-group').remove();

    const candleGroup = svg.append('g').attr('class', 'candlestick-group');

    const candlesticks = candleGroup.selectAll('.candlestick')
      .data(processedData)
      .enter()
      .append('g')
      .attr('class', 'candlestick')
      .attr('transform', d => `translate(${xScale(d.date)}, 0)`);

    // High-low lines
    candlesticks.append('line')
      .attr('class', 'high-low-line')
      .attr('x1', 0)
      .attr('x2', 0)
      .attr('y1', d => yScale(d.high))
      .attr('y2', d => yScale(d.low))
      .attr('stroke', d => d.close >= d.open ? colors.bullish : colors.bearish)
      .attr('stroke-width', 1);

    // Candle bodies
    candlesticks.append('rect')
      .attr('class', 'candle-body')
      .attr('x', -candleWidth / 2)
      .attr('y', d => yScale(Math.max(d.open, d.close)))
      .attr('width', candleWidth)
      .attr('height', d => Math.max(1, Math.abs(yScale(d.open) - yScale(d.close))))
      .attr('fill', d => d.close >= d.open ? colors.bullish : colors.bearish)
      .attr('stroke', d => d.close >= d.open ? colors.bullish : colors.bearish)
      .attr('stroke-width', 1);

    // Interactive overlay
    if (!shouldReduceEffects) {
      candlesticks.append('rect')
        .attr('class', 'interaction-overlay')
        .attr('x', -candleWidth)
        .attr('y', d => yScale(d.high) - 5)
        .attr('width', candleWidth * 2)
        .attr('height', d => yScale(d.low) - yScale(d.high) + 10)
        .attr('fill', 'transparent')
        .style('cursor', 'pointer')
        .on('click', function(event, d) {
          measureInteraction('chart-datapoint-click', () => {
            setSelectedDataPoint(d);
            onDataPointClick?.(d);
            announce(`데이터 포인트 선택: ${format(d.date, 'yyyy-MM-dd')}, 종가 ${d.close}`, 'polite');
          });
        })
        .on('mouseover', function(event, d) {
          if (!shouldReduceMotion) {
            d3.select((this as any).parentNode).select('.candle-body')
              .transition()
              .duration(150)
              .attr('stroke-width', 2);
          }
          setSelectedDataPoint(d);
        })
        .on('mouseout', function(event, d) {
          if (!shouldReduceMotion) {
            d3.select((this as any).parentNode).select('.candle-body')
              .transition()
              .duration(150)
              .attr('stroke-width', 1);
          }
        });
    }
  }, [scales, processedData, dimensions, colors, shouldReduceEffects, shouldReduceMotion, measureInteraction, onDataPointClick, announce]);

  // Draw volume bars
  const drawVolume = useCallback((svg: d3.Selection<SVGGElement, unknown, null, undefined>) => {
    if (!scales || !processedData.length || !showVolume) return;

    const { xScale, volumeScale } = scales;
    const barWidth = Math.max(1, Math.min(8, dimensions.width / processedData.length * 0.6));

    // Remove existing volume bars
    svg.selectAll('.volume-group').remove();

    const volumeGroup = svg.append('g').attr('class', 'volume-group');

    volumeGroup.selectAll('.volume-bar')
      .data(processedData)
      .enter()
      .append('rect')
      .attr('class', 'volume-bar')
      .attr('x', d => xScale(d.date) - barWidth / 2)
      .attr('y', d => volumeScale(d.volume))
      .attr('width', barWidth)
      .attr('height', d => dimensions.height - 20 - volumeScale(d.volume))
      .attr('fill', alpha(colors.volume, 0.6))
      .attr('stroke', colors.volume)
      .attr('stroke-width', 0.5);
  }, [scales, processedData, dimensions, colors, showVolume]);

  // Draw grid
  const drawGrid = useCallback((svg: d3.Selection<SVGGElement, unknown, null, undefined>) => {
    if (!scales || !showGrid) return;

    const { xScale, yScale } = scales;

    // Remove existing grid
    svg.selectAll('.grid-group').remove();

    const gridGroup = svg.append('g').attr('class', 'grid-group');

    // Horizontal grid lines
    const yTicks = yScale.ticks(8);
    gridGroup.selectAll('.grid-line-horizontal')
      .data(yTicks)
      .enter()
      .append('line')
      .attr('class', 'grid-line-horizontal')
      .attr('x1', 0)
      .attr('x2', dimensions.width)
      .attr('y1', d => yScale(d))
      .attr('y2', d => yScale(d))
      .attr('stroke', colors.grid)
      .attr('stroke-width', 0.5)
      .attr('stroke-dasharray', '2,2');

    // Vertical grid lines
    const xTicks = xScale.ticks(10);
    gridGroup.selectAll('.grid-line-vertical')
      .data(xTicks)
      .enter()
      .append('line')
      .attr('class', 'grid-line-vertical')
      .attr('x1', d => xScale(d))
      .attr('x2', d => xScale(d))
      .attr('y1', 0)
      .attr('y2', dimensions.height - (showVolume ? dimensions.volumeHeight : 0))
      .attr('stroke', colors.grid)
      .attr('stroke-width', 0.5)
      .attr('stroke-dasharray', '2,2');
  }, [scales, dimensions, colors, showGrid, showVolume]);

  // Draw axes
  const drawAxes = useCallback((svg: d3.Selection<SVGGElement, unknown, null, undefined>) => {
    if (!scales) return;

    const { xScale, yScale, volumeScale } = scales;

    // Remove existing axes
    svg.selectAll('.axis-group').remove();

    const axisGroup = svg.append('g').attr('class', 'axis-group');

    // X-axis
    const xAxis = d3.axisBottom(xScale)
      .tickFormat(d => format(d as Date, 'MM/dd'))
      .ticks(8);

    axisGroup.append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0, ${dimensions.height - (showVolume ? dimensions.volumeHeight : 20)})`)
      .call(xAxis)
      .selectAll('text')
      .attr('fill', colors.text)
      .style('font-size', '12px');

    // Y-axis (price)
    const yAxis = d3.axisRight(yScale)
      .tickFormat(d => `₩${d.toLocaleString()}`)
      .ticks(8);

    axisGroup.append('g')
      .attr('class', 'y-axis')
      .attr('transform', `translate(${dimensions.width}, 0)`)
      .call(yAxis)
      .selectAll('text')
      .attr('fill', colors.text)
      .style('font-size', '12px');

    // Volume Y-axis
    if (showVolume) {
      const volumeAxis = d3.axisRight(volumeScale)
        .tickFormat((d: any) => `${(Number(d) / 1000).toFixed(0)}K`)
        .ticks(4);

      axisGroup.append('g')
        .attr('class', 'volume-axis')
        .attr('transform', `translate(${dimensions.width}, 0)`)
        .call(volumeAxis)
        .selectAll('text')
        .attr('fill', colors.textSecondary)
        .style('font-size', '10px');
    }

    // Style axis lines
    axisGroup.selectAll('.domain')
      .attr('stroke', colors.grid);

    axisGroup.selectAll('.tick line')
      .attr('stroke', colors.grid);
  }, [scales, dimensions, colors, showVolume]);

  // Draw technical indicators
  const drawIndicators = useCallback((svg: d3.Selection<SVGGElement, unknown, null, undefined>) => {
    if (!scales || !indicators.length) return;

    const { xScale, yScale } = scales;

    // Remove existing indicators
    svg.selectAll('.indicator-group').remove();

    const indicatorGroup = svg.append('g').attr('class', 'indicator-group');

    indicators.forEach((indicator, index) => {
      const line = d3.line<{ date: Date; value: number }>()
        .x(d => xScale(d.date))
        .y(d => yScale(d.value))
        .curve(d3.curveMonotoneX);

      indicatorGroup.append('path')
        .datum(indicator.data)
        .attr('class', `indicator-line indicator-${index}`)
        .attr('fill', 'none')
        .attr('stroke', indicator.color)
        .attr('stroke-width', 2)
        .attr('d', line);
    });
  }, [scales, indicators]);

  // Draw crosshair
  const drawCrosshair = useCallback((svg: d3.Selection<SVGGElement, unknown, null, undefined>) => {
    if (!showCrosshair || !selectedDataPoint || !scales) return;

    const { xScale, yScale } = scales;

    // Remove existing crosshair
    svg.selectAll('.crosshair-group').remove();

    const crosshairGroup = svg.append('g').attr('class', 'crosshair-group');

    const x = xScale(selectedDataPoint.date);
    const y = yScale(selectedDataPoint.close);

    // Vertical line
    crosshairGroup.append('line')
      .attr('class', 'crosshair-vertical')
      .attr('x1', x)
      .attr('x2', x)
      .attr('y1', 0)
      .attr('y2', dimensions.height - (showVolume ? dimensions.volumeHeight : 20))
      .attr('stroke', colors.crosshair)
      .attr('stroke-width', 1)
      .attr('stroke-dasharray', '3,3');

    // Horizontal line
    crosshairGroup.append('line')
      .attr('class', 'crosshair-horizontal')
      .attr('x1', 0)
      .attr('x2', dimensions.width)
      .attr('y1', y)
      .attr('y2', y)
      .attr('stroke', colors.crosshair)
      .attr('stroke-width', 1)
      .attr('stroke-dasharray', '3,3');

    // Price label
    crosshairGroup.append('rect')
      .attr('class', 'price-label-bg')
      .attr('x', dimensions.width + 5)
      .attr('y', y - 10)
      .attr('width', 80)
      .attr('height', 20)
      .attr('fill', colors.crosshair)
      .attr('rx', 3);

    crosshairGroup.append('text')
      .attr('class', 'price-label')
      .attr('x', dimensions.width + 45)
      .attr('y', y + 4)
      .attr('text-anchor', 'middle')
      .attr('fill', colors.background)
      .style('font-size', '12px')
      .style('font-weight', 'bold')
      .text(`₩${selectedDataPoint.close.toLocaleString()}`);
  }, [showCrosshair, selectedDataPoint, scales, dimensions, colors, showVolume]);

  // Main render function
  const renderChart = useCallback(() => {
    if (!svgRef.current || !processedData.length || !scales) return;

    measureInteraction('chart-render', () => {
      const svg = d3.select(svgRef.current!);

      // Clear previous content
      svg.selectAll('*').remove();

      // Set up main group with margins
      const mainGroup = svg.append('g')
        .attr('transform', `translate(${margin.left}, ${margin.top})`);

      // Draw chart elements in order
      drawGrid(mainGroup);
      drawVolume(mainGroup);

      if (chartType === 'candlestick') {
        drawCandlesticks(mainGroup);
      }

      drawIndicators(mainGroup);
      drawAxes(mainGroup);
      drawCrosshair(mainGroup);
    });
  }, [processedData, scales, margin, chartType, drawGrid, drawVolume, drawCandlesticks, drawIndicators, drawAxes, drawCrosshair, measureInteraction]);

  // Zoom functionality
  const handleZoom = useCallback((direction: 'in' | 'out' | 'reset') => {
    if (!scales) return;

    measureInteraction('chart-zoom', () => {
      const { xExtent } = scales;
      const currentRange = currentDomain || xExtent;
      const duration = currentRange[1].getTime() - currentRange[0].getTime();

      let newDomain: [Date, Date];

      switch (direction) {
        case 'in':
          const zoomFactor = 0.8;
          const center = new Date((currentRange[0].getTime() + currentRange[1].getTime()) / 2);
          const newDuration = duration * zoomFactor;
          newDomain = [
            new Date(center.getTime() - newDuration / 2),
            new Date(center.getTime() + newDuration / 2)
          ];
          setZoomLevel(prev => prev * 1.25);
          break;
        case 'out':
          const expandFactor = 1.25;
          const centerOut = new Date((currentRange[0].getTime() + currentRange[1].getTime()) / 2);
          const expandedDuration = duration * expandFactor;
          newDomain = [
            new Date(Math.max(xExtent[0].getTime(), centerOut.getTime() - expandedDuration / 2)),
            new Date(Math.min(xExtent[1].getTime(), centerOut.getTime() + expandedDuration / 2))
          ];
          setZoomLevel(prev => Math.max(1, prev * 0.8));
          break;
        case 'reset':
          newDomain = xExtent;
          setZoomLevel(1);
          break;
        default:
          return;
      }

      setCurrentDomain(newDomain);
      onZoomChange?.(newDomain);
      announce(`차트 ${direction === 'reset' ? '초기화' : direction === 'in' ? '확대' : '축소'}`, 'polite');
    });
  }, [scales, currentDomain, onZoomChange, measureInteraction, announce]);

  // Real-time updates
  useEffect(() => {
    if (!realTimeUpdate) return;

    const interval = setInterval(() => {
      // Trigger re-render for real-time data updates
      renderChart();
    }, 1000);

    return () => clearInterval(interval);
  }, [realTimeUpdate, renderChart]);

  // Initial render and data changes
  useEffect(() => {
    renderChart();
  }, [renderChart]);

  // Fullscreen toggle
  const toggleFullscreen = useCallback(() => {
    if (!containerRef.current) return;

    if (!isFullscreen) {
      containerRef.current.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
    setIsFullscreen(!isFullscreen);
  }, [isFullscreen]);

  return (
    <Paper
      ref={containerRef}
      sx={{
        backgroundColor: colors.background,
        border: `1px solid ${colors.grid}`,
        borderRadius: 2,
        overflow: 'hidden',
        position: 'relative',
      }}
    >
      {/* Chart Controls */}
      <Box
        sx={{
          position: 'absolute',
          top: 8,
          left: 8,
          zIndex: 10,
          display: 'flex',
          gap: 1,
          flexWrap: 'wrap',
        }}
      >
        {/* Chart Type Selector */}
        <ToggleButtonGroup
          value={chartType}
          exclusive
          onChange={(_, value) => value && setChartType(value)}
          size="small"
          sx={{
            backgroundColor: alpha(colors.background, 0.8),
            borderRadius: 1,
          }}
        >
          <ToggleButton value="candlestick" aria-label="캔들차트">
            <Tooltip title="캔들스틱 차트">
              <BarChart fontSize="small" />
            </Tooltip>
          </ToggleButton>
          <ToggleButton value="line" aria-label="선차트">
            <Tooltip title="선 차트">
              <ShowChart fontSize="small" />
            </Tooltip>
          </ToggleButton>
          <ToggleButton value="area" aria-label="영역차트">
            <Tooltip title="영역 차트">
              <Timeline fontSize="small" />
            </Tooltip>
          </ToggleButton>
        </ToggleButtonGroup>

        {/* Zoom Controls */}
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="확대">
            <IconButton
              size="small"
              onClick={() => handleZoom('in')}
              sx={{ backgroundColor: alpha(colors.background, 0.8) }}
            >
              <ZoomIn fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="축소">
            <IconButton
              size="small"
              onClick={() => handleZoom('out')}
              sx={{ backgroundColor: alpha(colors.background, 0.8) }}
            >
              <ZoomOut fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="초기화">
            <IconButton
              size="small"
              onClick={() => handleZoom('reset')}
              sx={{ backgroundColor: alpha(colors.background, 0.8) }}
            >
              <CenterFocusStrong fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={isFullscreen ? "전체화면 해제" : "전체화면"}>
            <IconButton
              size="small"
              onClick={toggleFullscreen}
              sx={{ backgroundColor: alpha(colors.background, 0.8) }}
            >
              {isFullscreen ? <FullscreenExit fontSize="small" /> : <Fullscreen fontSize="small" />}
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Data Point Info */}
      {selectedDataPoint && (
        <Box
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            zIndex: 10,
            backgroundColor: alpha(colors.background, 0.9),
            border: `1px solid ${colors.grid}`,
            borderRadius: 1,
            p: 1,
            minWidth: 200,
          }}
        >
          <Typography variant="caption" sx={{ color: colors.textSecondary }}>
            {format(selectedDataPoint.date, 'yyyy-MM-dd HH:mm')}
          </Typography>
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1, mt: 0.5 }}>
            <Typography variant="body2" sx={{ color: colors.text }}>
              시가: ₩{selectedDataPoint.open.toLocaleString()}
            </Typography>
            <Typography variant="body2" sx={{ color: colors.text }}>
              고가: ₩{selectedDataPoint.high.toLocaleString()}
            </Typography>
            <Typography variant="body2" sx={{ color: colors.text }}>
              저가: ₩{selectedDataPoint.low.toLocaleString()}
            </Typography>
            <Typography variant="body2" sx={{ color: colors.text }}>
              종가: ₩{selectedDataPoint.close.toLocaleString()}
            </Typography>
          </Box>
          <Typography variant="caption" sx={{ color: colors.textSecondary, mt: 0.5, display: 'block' }}>
            거래량: {selectedDataPoint.volume.toLocaleString()}
          </Typography>
        </Box>
      )}

      {/* Main Chart SVG */}
      <svg
        ref={svgRef}
        width={width}
        height={height}
        style={{
          background: colors.background,
          display: 'block',
        }}
      />

      {/* Performance indicator */}
      {optimizationLevel === 'aggressive' && (
        <Box
          sx={{
            position: 'absolute',
            bottom: 8,
            left: 8,
            backgroundColor: alpha(theme.palette.warning.main, 0.8),
            color: theme.palette.warning.contrastText,
            px: 1,
            py: 0.5,
            borderRadius: 1,
            fontSize: '0.75rem',
          }}
        >
          성능 최적화 모드
        </Box>
      )}
    </Paper>
  );
};