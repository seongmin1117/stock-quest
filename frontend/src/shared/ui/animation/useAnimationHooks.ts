'use client';

import { useEffect, useRef, useState } from 'react';
import { useAnimation as useFramerAnimation, useInView } from 'framer-motion';
import { useAnimation } from './AnimationProvider';

// Hook for managing price direction changes
export function usePriceDirection(currentPrice: number, previousPrice?: number) {
  const [direction, setDirection] = useState<'up' | 'down' | 'neutral'>('neutral');
  const [changeAmount, setChangeAmount] = useState(0);
  const [changePercent, setChangePercent] = useState(0);

  useEffect(() => {
    if (previousPrice === undefined) {
      setDirection('neutral');
      setChangeAmount(0);
      setChangePercent(0);
      return;
    }

    const change = currentPrice - previousPrice;
    const percent = previousPrice !== 0 ? (change / previousPrice) * 100 : 0;

    setChangeAmount(change);
    setChangePercent(percent);

    if (change > 0) {
      setDirection('up');
    } else if (change < 0) {
      setDirection('down');
    } else {
      setDirection('neutral');
    }
  }, [currentPrice, previousPrice]);

  return {
    direction,
    changeAmount,
    changePercent,
    isPositive: direction === 'up',
    isNegative: direction === 'down',
    isNeutral: direction === 'neutral',
  };
}

// Hook for scroll-based animations
export function useScrollAnimation(threshold = 0.1) {
  const { enableAnimations } = useAnimation();
  const controls = useFramerAnimation();
  const ref = useRef(null);
  const inView = useInView(ref, {
    amount: threshold,
    once: true, // Only trigger once
  });

  useEffect(() => {
    if (enableAnimations && inView) {
      controls.start('animate');
    } else if (enableAnimations) {
      controls.start('initial');
    }
  }, [controls, inView, enableAnimations]);

  return { ref, controls, inView };
}

// Hook for staggered list animations
export function useStaggeredAnimation(
  itemCount: number,
  staggerDelay = 0.1,
  threshold = 0.1
) {
  const { enableAnimations } = useAnimation();
  const controls = useFramerAnimation();
  const ref = useRef(null);
  const inView = useInView(ref, { amount: threshold });

  useEffect(() => {
    if (enableAnimations && inView) {
      controls.start((i: number) => ({
        opacity: 1,
        y: 0,
        transition: {
          delay: i * staggerDelay,
          duration: 0.3,
          ease: [0.22, 1, 0.36, 1],
        },
      }));
    }
  }, [controls, inView, itemCount, staggerDelay, enableAnimations]);

  return { ref, controls, inView };
}

// Hook for hover animations with performance optimization
export function useHoverAnimation() {
  const { enableAnimations } = useAnimation();
  const [isHovered, setIsHovered] = useState(false);

  const hoverProps = enableAnimations ? {
    onMouseEnter: () => setIsHovered(true),
    onMouseLeave: () => setIsHovered(false),
    onFocus: () => setIsHovered(true),
    onBlur: () => setIsHovered(false),
  } : {};

  return {
    isHovered: enableAnimations ? isHovered : false,
    hoverProps,
  };
}

// Hook for managing loading animations
export function useLoadingAnimation(isLoading: boolean) {
  const { enableAnimations } = useAnimation();
  const [shouldShowLoading, setShouldShowLoading] = useState(isLoading);

  useEffect(() => {
    if (isLoading) {
      setShouldShowLoading(true);
    } else {
      // Delay hiding to allow for smooth transition
      const timeout = setTimeout(() => {
        setShouldShowLoading(false);
      }, enableAnimations ? 300 : 0);

      return () => clearTimeout(timeout);
    }
  }, [isLoading, enableAnimations]);

  return {
    shouldShowLoading,
    loadingVariant: isLoading ? 'loading' : 'loaded',
  };
}

// Hook for managing notification animations
export function useNotificationAnimation() {
  const { enableAnimations } = useAnimation();
  const [notifications, setNotifications] = useState<Array<{
    id: string;
    type: 'success' | 'error' | 'warning' | 'info';
    title: string;
    message: string;
    duration?: number;
  }>>([]);

  const addNotification = (notification: {
    type: 'success' | 'error' | 'warning' | 'info';
    title: string;
    message: string;
    duration?: number;
  }) => {
    const id = Math.random().toString(36).substr(2, 9);
    const newNotification = { ...notification, id };

    setNotifications(prev => [...prev, newNotification]);

    // Auto-remove after duration
    const duration = notification.duration || 5000;
    setTimeout(() => {
      removeNotification(id);
    }, duration);

    return id;
  };

  const removeNotification = (id: string) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  const clearAllNotifications = () => {
    setNotifications([]);
  };

  return {
    notifications,
    addNotification,
    removeNotification,
    clearAllNotifications,
  };
}

// Hook for managing chart animation states
export function useChartAnimation(data: any[], isLoading = false) {
  const { enableAnimations } = useAnimation();
  const [animationKey, setAnimationKey] = useState(0);
  const [shouldAnimate, setShouldAnimate] = useState(false);

  useEffect(() => {
    if (data.length > 0 && !isLoading) {
      setShouldAnimate(true);
      setAnimationKey(prev => prev + 1);
    }
  }, [data, isLoading]);

  const getChartAnimationProps = () => {
    if (!enableAnimations || !shouldAnimate) {
      return { animate: false };
    }

    return {
      animate: true,
      animationDuration: 800,
      animationEasing: 'easeOutCubic',
    };
  };

  return {
    animationKey,
    shouldAnimate: enableAnimations && shouldAnimate,
    getChartAnimationProps,
  };
}

// Hook for managing real-time data animations
export function useRealTimeAnimation(data: any, updateInterval = 1000) {
  const { enableAnimations } = useAnimation();
  const [isUpdating, setIsUpdating] = useState(false);
  const [hasNewData, setHasNewData] = useState(false);
  const previousDataRef = useRef(data);

  useEffect(() => {
    // Check if data has changed
    if (JSON.stringify(data) !== JSON.stringify(previousDataRef.current)) {
      setHasNewData(true);
      setIsUpdating(true);

      // Reset animation state after a short delay
      const timeout = setTimeout(() => {
        setIsUpdating(false);
        setHasNewData(false);
        previousDataRef.current = data;
      }, enableAnimations ? 500 : 0);

      return () => clearTimeout(timeout);
    }
  }, [data, enableAnimations]);

  return {
    isUpdating: enableAnimations ? isUpdating : false,
    hasNewData: enableAnimations ? hasNewData : false,
    animationVariant: isUpdating ? 'updating' : 'stable',
  };
}

// Hook for managing page transition animations
export function usePageTransition() {
  const { enableAnimations } = useAnimation();
  const [isTransitioning, setIsTransitioning] = useState(false);

  const startTransition = () => {
    if (enableAnimations) {
      setIsTransitioning(true);
    }
  };

  const endTransition = () => {
    setIsTransitioning(false);
  };

  useEffect(() => {
    if (isTransitioning) {
      const timeout = setTimeout(endTransition, 300);
      return () => clearTimeout(timeout);
    }
  }, [isTransitioning]);

  return {
    isTransitioning: enableAnimations ? isTransitioning : false,
    startTransition,
    endTransition,
  };
}

// Hook for managing gesture-based animations (mobile)
export function useGestureAnimation() {
  const { enableAnimations } = useAnimation();
  const [gestureState, setGestureState] = useState<{
    isDragging: boolean;
    isSweping: boolean;
    direction: 'left' | 'right' | 'up' | 'down' | null;
  }>({
    isDragging: false,
    isSweping: false,
    direction: null,
  });

  const onDragStart = () => {
    if (enableAnimations) {
      setGestureState(prev => ({ ...prev, isDragging: true }));
    }
  };

  const onDragEnd = () => {
    setGestureState(prev => ({ ...prev, isDragging: false }));
  };

  const onSwipeStart = (direction: 'left' | 'right' | 'up' | 'down') => {
    if (enableAnimations) {
      setGestureState(prev => ({
        ...prev,
        isSweping: true,
        direction
      }));
    }
  };

  const onSwipeEnd = () => {
    setGestureState(prev => ({
      ...prev,
      isSweping: false,
      direction: null
    }));
  };

  return {
    gestureState: enableAnimations ? gestureState : { isDragging: false, isSweping: false, direction: null },
    onDragStart,
    onDragEnd,
    onSwipeStart,
    onSwipeEnd,
  };
}

// Hook for performance monitoring of animations
export function useAnimationPerformance() {
  const [metrics, setMetrics] = useState({
    fps: 60,
    frameDrops: 0,
    animationCount: 0,
  });

  useEffect(() => {
    let frameCount = 0;
    let lastTime = performance.now();
    let animationFrame: number;

    const measurePerformance = (currentTime: number) => {
      frameCount++;

      if (currentTime - lastTime >= 1000) {
        const fps = Math.round((frameCount * 1000) / (currentTime - lastTime));
        const frameDrops = Math.max(0, 60 - fps);

        setMetrics(prev => ({
          ...prev,
          fps,
          frameDrops,
        }));

        frameCount = 0;
        lastTime = currentTime;
      }

      animationFrame = requestAnimationFrame(measurePerformance);
    };

    animationFrame = requestAnimationFrame(measurePerformance);

    return () => {
      cancelAnimationFrame(animationFrame);
    };
  }, []);

  const incrementAnimationCount = () => {
    setMetrics(prev => ({ ...prev, animationCount: prev.animationCount + 1 }));
  };

  const decrementAnimationCount = () => {
    setMetrics(prev => ({ ...prev, animationCount: Math.max(0, prev.animationCount - 1) }));
  };

  return {
    metrics,
    incrementAnimationCount,
    decrementAnimationCount,
    isPerformanceGood: metrics.fps >= 55 && metrics.frameDrops < 5,
  };
}