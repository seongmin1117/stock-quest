'use client';

import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
  useMemo
} from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  LinearProgress,
  Chip,
  Alert,
  Collapse,
  IconButton,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Speed,
  Memory,
  NetworkCheck,
  BatteryAlert,
  ExpandMore,
  ExpandLess,
  Warning,
  CheckCircle,
} from '@mui/icons-material';

interface PerformanceMetrics {
  fps: number;
  memoryUsage: number;
  networkLatency: number;
  batteryLevel: number;
  renderTime: number;
  interactionDelay: number;
  isLowEndDevice: boolean;
  connectionType: string;
}

interface PerformanceContextType {
  metrics: PerformanceMetrics;
  isOptimizationEnabled: boolean;
  enableOptimization: () => void;
  disableOptimization: () => void;
  measureInteraction: (name: string, fn: () => void) => void;
  optimizeFor60fps: boolean;
}

const defaultMetrics: PerformanceMetrics = {
  fps: 60,
  memoryUsage: 0,
  networkLatency: 0,
  batteryLevel: 100,
  renderTime: 0,
  interactionDelay: 0,
  isLowEndDevice: false,
  connectionType: '4g',
};

const PerformanceContext = createContext<PerformanceContextType | null>(null);

export const usePerformance = () => {
  const context = useContext(PerformanceContext);
  if (!context) {
    throw new Error('usePerformance must be used within PerformanceProvider');
  }
  return context;
};

interface PerformanceProviderProps {
  children: React.ReactNode;
  targetFPS?: number;
  enableMonitoring?: boolean;
}

/**
 * Performance optimization provider for 60fps mobile interactions
 * 60fps 모바일 상호작용을 위한 성능 최적화 프로바이더
 */
export const PerformanceProvider: React.FC<PerformanceProviderProps> = ({
  children,
  targetFPS = 60,
  enableMonitoring = true,
}) => {
  const [metrics, setMetrics] = useState<PerformanceMetrics>(defaultMetrics);
  const [isOptimizationEnabled, setIsOptimizationEnabled] = useState(false);
  const frameRef = useRef<number>();
  const lastFrameTime = useRef<number>(0);
  const frameCount = useRef<number>(0);
  const fpsArray = useRef<number[]>([]);

  // Device capability detection
  const deviceCapabilities = useMemo(() => {
    if (typeof window === 'undefined') return { isLowEnd: false, cores: 4 };

    const cores = navigator.hardwareConcurrency || 4;
    const memory = (navigator as any).deviceMemory || 4;
    const connection = (navigator as any).connection;

    const isLowEnd = cores <= 2 || memory <= 2 ||
      (connection && ['slow-2g', '2g', '3g'].includes(connection.effectiveType));

    return { isLowEnd, cores, memory, connection };
  }, []);

  // FPS monitoring
  const measureFPS = useCallback(() => {
    if (!enableMonitoring) return;

    const now = performance.now();

    if (lastFrameTime.current > 0) {
      const delta = now - lastFrameTime.current;
      const fps = 1000 / delta;

      fpsArray.current.push(fps);

      // Keep only last 60 samples (1 second at 60fps)
      if (fpsArray.current.length > 60) {
        fpsArray.current.shift();
      }

      // Calculate average FPS
      const averageFPS = fpsArray.current.reduce((a, b) => a + b, 0) / fpsArray.current.length;

      setMetrics(prev => ({
        ...prev,
        fps: Math.round(averageFPS),
        renderTime: delta,
      }));

      // Auto-enable optimization if FPS drops below target
      if (averageFPS < targetFPS * 0.8 && !isOptimizationEnabled) {
        setIsOptimizationEnabled(true);
      }
    }

    lastFrameTime.current = now;
    frameRef.current = requestAnimationFrame(measureFPS);
  }, [enableMonitoring, targetFPS, isOptimizationEnabled]);

  // Memory monitoring
  const measureMemory = useCallback(() => {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      const used = memory.usedJSHeapSize / memory.totalJSHeapSize * 100;

      setMetrics(prev => ({
        ...prev,
        memoryUsage: Math.round(used),
      }));
    }
  }, []);

  // Network monitoring
  const measureNetwork = useCallback(async () => {
    if (!navigator.onLine) return;

    const start = performance.now();

    try {
      await fetch('/api/ping', {
        method: 'HEAD',
        cache: 'no-cache',
      });

      const latency = performance.now() - start;

      setMetrics(prev => ({
        ...prev,
        networkLatency: Math.round(latency),
      }));
    } catch (error) {
      // Network error - assume high latency
      setMetrics(prev => ({
        ...prev,
        networkLatency: 1000,
      }));
    }
  }, []);

  // Battery monitoring
  const measureBattery = useCallback(async () => {
    if ('getBattery' in navigator) {
      try {
        const battery = await (navigator as any).getBattery();
        setMetrics(prev => ({
          ...prev,
          batteryLevel: Math.round(battery.level * 100),
        }));
      } catch (error) {
        // Battery API not available
      }
    }
  }, []);

  // Initialize monitoring
  useEffect(() => {
    if (!enableMonitoring) return;

    // Start FPS monitoring
    frameRef.current = requestAnimationFrame(measureFPS);

    // Initial device detection
    setMetrics(prev => ({
      ...prev,
      isLowEndDevice: deviceCapabilities.isLowEnd,
      connectionType: deviceCapabilities.connection?.effectiveType || '4g',
    }));

    // Periodic monitoring
    const memoryInterval = setInterval(measureMemory, 5000);
    const networkInterval = setInterval(measureNetwork, 10000);
    const batteryInterval = setInterval(measureBattery, 30000);

    // Initial measurements
    measureMemory();
    measureNetwork();
    measureBattery();

    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current);
      }
      clearInterval(memoryInterval);
      clearInterval(networkInterval);
      clearInterval(batteryInterval);
    };
  }, [enableMonitoring, measureFPS, measureMemory, measureNetwork, measureBattery, deviceCapabilities]);

  // Performance optimization effects
  useEffect(() => {
    if (!isOptimizationEnabled) return;

    const root = document.documentElement;

    // Reduce visual effects for low-end devices
    if (deviceCapabilities.isLowEnd) {
      root.style.setProperty('--performance-animations', 'none');
      root.style.setProperty('--performance-shadows', 'none');
      root.style.setProperty('--performance-blur', 'none');
      root.style.setProperty('--performance-transforms', 'none');
    } else {
      root.style.setProperty('--performance-animations', 'auto');
      root.style.setProperty('--performance-shadows', 'auto');
      root.style.setProperty('--performance-blur', 'auto');
      root.style.setProperty('--performance-transforms', 'auto');
    }

    // Battery optimization
    if (metrics.batteryLevel < 20) {
      root.style.setProperty('--performance-refresh-rate', '30fps');
    } else {
      root.style.setProperty('--performance-refresh-rate', '60fps');
    }

    // Network optimization
    if (metrics.networkLatency > 500 || ['slow-2g', '2g'].includes(metrics.connectionType)) {
      root.style.setProperty('--performance-preload', 'none');
    } else {
      root.style.setProperty('--performance-preload', 'auto');
    }

    return () => {
      // Reset optimizations
      root.style.removeProperty('--performance-animations');
      root.style.removeProperty('--performance-shadows');
      root.style.removeProperty('--performance-blur');
      root.style.removeProperty('--performance-transforms');
      root.style.removeProperty('--performance-refresh-rate');
      root.style.removeProperty('--performance-preload');
    };
  }, [isOptimizationEnabled, deviceCapabilities.isLowEnd, metrics.batteryLevel, metrics.networkLatency, metrics.connectionType]);

  const measureInteraction = useCallback((name: string, fn: () => void) => {
    const start = performance.now();

    // Use requestAnimationFrame to ensure measurement includes paint
    requestAnimationFrame(() => {
      fn();

      requestAnimationFrame(() => {
        const end = performance.now();
        const delay = end - start;

        setMetrics(prev => ({
          ...prev,
          interactionDelay: Math.round(delay),
        }));

        // Log slow interactions
        if (delay > 100) {
          console.warn(`Slow interaction: ${name} took ${delay.toFixed(2)}ms`);
        }
      });
    });
  }, []);

  const enableOptimization = useCallback(() => {
    setIsOptimizationEnabled(true);
  }, []);

  const disableOptimization = useCallback(() => {
    setIsOptimizationEnabled(false);
  }, []);

  const contextValue: PerformanceContextType = {
    metrics,
    isOptimizationEnabled,
    enableOptimization,
    disableOptimization,
    measureInteraction,
    optimizeFor60fps: metrics.fps >= targetFPS * 0.9,
  };

  return (
    <PerformanceContext.Provider value={contextValue}>
      {children}
    </PerformanceContext.Provider>
  );
};

interface PerformanceMonitorProps {
  show?: boolean;
  position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
  minimal?: boolean;
}

/**
 * Performance monitoring overlay for development and debugging
 * 개발 및 디버깅을 위한 성능 모니터링 오버레이
 */
export const PerformanceMonitor: React.FC<PerformanceMonitorProps> = ({
  show = false,
  position = 'top-right',
  minimal = false,
}) => {
  const theme = useTheme();
  const { metrics, isOptimizationEnabled, enableOptimization, disableOptimization } = usePerformance();
  const [expanded, setExpanded] = useState(!minimal);

  if (!show) return null;

  const getPositionStyles = () => {
    const base = {
      position: 'fixed' as const,
      zIndex: 9999,
      width: minimal ? 'auto' : 280,
      minWidth: minimal ? 120 : 280,
    };

    switch (position) {
      case 'top-left':
        return { ...base, top: 16, left: 16 };
      case 'top-right':
        return { ...base, top: 16, right: 16 };
      case 'bottom-left':
        return { ...base, bottom: 16, left: 16 };
      case 'bottom-right':
        return { ...base, bottom: 16, right: 16 };
      default:
        return { ...base, top: 16, right: 16 };
    }
  };

  const getStatusColor = (value: number, thresholds: { good: number; warning: number }) => {
    if (value >= thresholds.good) return theme.palette.success.main;
    if (value >= thresholds.warning) return theme.palette.warning.main;
    return theme.palette.error.main;
  };

  const formatMetric = (value: number, unit: string, decimals = 0) => {
    return `${value.toFixed(decimals)}${unit}`;
  };

  return (
    <Card
      sx={{
        ...getPositionStyles(),
        backgroundColor: alpha(theme.palette.background.paper, 0.95),
        backdropFilter: 'blur(10px)',
        border: `1px solid ${alpha(theme.palette.divider, 0.3)}`,
      }}
    >
      <CardContent sx={{ p: minimal ? 1 : 2, '&:last-child': { pb: minimal ? 1 : 2 } }}>
        {/* Header */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: minimal ? 0 : 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Speed fontSize="small" color="primary" />
            <Typography variant="subtitle2" sx={{ fontSize: minimal ? '0.75rem' : '0.875rem' }}>
              성능
            </Typography>
          </Box>

          {!minimal && (
            <IconButton
              size="small"
              onClick={() => setExpanded(!expanded)}
              sx={{ ml: 1 }}
            >
              {expanded ? <ExpandLess /> : <ExpandMore />}
            </IconButton>
          )}
        </Box>

        {/* Key metrics (always visible) */}
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          <Chip
            label={`${metrics.fps} FPS`}
            size="small"
            sx={{
              backgroundColor: alpha(getStatusColor(metrics.fps, { good: 55, warning: 30 }), 0.1),
              color: getStatusColor(metrics.fps, { good: 55, warning: 30 }),
              fontSize: '0.75rem',
            }}
          />

          {metrics.memoryUsage > 0 && (
            <Chip
              label={`${metrics.memoryUsage}% RAM`}
              size="small"
              sx={{
                backgroundColor: alpha(getStatusColor(100 - metrics.memoryUsage, { good: 50, warning: 20 }), 0.1),
                color: getStatusColor(100 - metrics.memoryUsage, { good: 50, warning: 20 }),
                fontSize: '0.75rem',
              }}
            />
          )}
        </Box>

        {/* Detailed metrics */}
        <Collapse in={expanded && !minimal}>
          <Box sx={{ mt: 2 }}>
            {/* Performance bars */}
            <Box sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                <Typography variant="caption">프레임률</Typography>
                <Typography variant="caption" fontWeight="bold">
                  {formatMetric(metrics.fps, ' FPS')}
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={Math.min((metrics.fps / 60) * 100, 100)}
                sx={{
                  height: 6,
                  borderRadius: 3,
                  backgroundColor: alpha(theme.palette.action.hover, 0.3),
                  '& .MuiLinearProgress-bar': {
                    backgroundColor: getStatusColor(metrics.fps, { good: 55, warning: 30 }),
                  },
                }}
              />
            </Box>

            {metrics.memoryUsage > 0 && (
              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                  <Typography variant="caption">메모리 사용률</Typography>
                  <Typography variant="caption" fontWeight="bold">
                    {formatMetric(metrics.memoryUsage, '%')}
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={metrics.memoryUsage}
                  sx={{
                    height: 6,
                    borderRadius: 3,
                    backgroundColor: alpha(theme.palette.action.hover, 0.3),
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: getStatusColor(100 - metrics.memoryUsage, { good: 50, warning: 20 }),
                    },
                  }}
                />
              </Box>
            )}

            {/* Device info */}
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
              {metrics.isLowEndDevice && (
                <Chip
                  label="저사양 기기"
                  size="small"
                  icon={<Warning />}
                  color="warning"
                  variant="outlined"
                />
              )}

              {metrics.batteryLevel < 20 && (
                <Chip
                  label={`배터리 ${metrics.batteryLevel}%`}
                  size="small"
                  icon={<BatteryAlert />}
                  color="error"
                  variant="outlined"
                />
              )}

              {metrics.networkLatency > 0 && (
                <Chip
                  label={`${metrics.networkLatency}ms`}
                  size="small"
                  icon={<NetworkCheck />}
                  color={metrics.networkLatency > 500 ? 'error' : 'default'}
                  variant="outlined"
                />
              )}
            </Box>

            {/* Optimization status */}
            <Alert
              severity={isOptimizationEnabled ? 'info' : 'success'}
              sx={{ py: 0.5 }}
              action={
                <IconButton
                  size="small"
                  onClick={isOptimizationEnabled ? disableOptimization : enableOptimization}
                  sx={{ color: 'inherit' }}
                >
                  {isOptimizationEnabled ? <ExpandLess /> : <ExpandMore />}
                </IconButton>
              }
            >
              <Typography variant="caption">
                {isOptimizationEnabled ? '성능 최적화 활성' : '성능 최적화 비활성'}
              </Typography>
            </Alert>

            {/* Performance tips */}
            {metrics.fps < 50 && (
              <Alert severity="warning" sx={{ mt: 1, py: 0.5 }}>
                <Typography variant="caption">
                  프레임률이 낮습니다. 애니메이션을 줄이거나 백그라운드 앱을 종료해보세요.
                </Typography>
              </Alert>
            )}

            {metrics.memoryUsage > 80 && (
              <Alert severity="error" sx={{ mt: 1, py: 0.5 }}>
                <Typography variant="caption">
                  메모리 사용률이 높습니다. 브라우저 탭을 줄이거나 페이지를 새로고침해보세요.
                </Typography>
              </Alert>
            )}
          </Box>
        </Collapse>
      </CardContent>
    </Card>
  );
};

/**
 * Hook for optimized component rendering
 * 최적화된 컴포넌트 렌더링을 위한 훅
 */
export const useOptimizedRendering = () => {
  const { metrics, isOptimizationEnabled } = usePerformance();

  const shouldReduceMotion = useMemo(() => {
    return isOptimizationEnabled && (
      metrics.isLowEndDevice ||
      metrics.fps < 45 ||
      metrics.batteryLevel < 20
    );
  }, [isOptimizationEnabled, metrics.isLowEndDevice, metrics.fps, metrics.batteryLevel]);

  const shouldReduceEffects = useMemo(() => {
    return isOptimizationEnabled && (
      metrics.memoryUsage > 75 ||
      metrics.isLowEndDevice
    );
  }, [isOptimizationEnabled, metrics.memoryUsage, metrics.isLowEndDevice]);

  const shouldPreload = useMemo(() => {
    return !isOptimizationEnabled || (
      metrics.networkLatency < 300 &&
      !['slow-2g', '2g'].includes(metrics.connectionType)
    );
  }, [isOptimizationEnabled, metrics.networkLatency, metrics.connectionType]);

  return {
    shouldReduceMotion,
    shouldReduceEffects,
    shouldPreload,
    optimizationLevel: isOptimizationEnabled ?
      (metrics.isLowEndDevice ? 'aggressive' : 'balanced') : 'none',
  };
};