// Animation Provider and Context
export { AnimationProvider, useAnimation, AnimationContext } from './AnimationProvider';

// Motion Variants
export * from './motionVariants';

// Trading-Specific Animated Components
export {
  AnimatedPrice,
  AnimatedPortfolioValue,
  AnimatedOrderStatus,
  AnimatedConnectionStatus,
  TradingNotification,
  AnimatedTradingCard,
  AnimatedCounter,
} from './TradingAnimations';

// Animation Hooks
export {
  usePriceDirection,
  useScrollAnimation,
  useStaggeredAnimation,
  useHoverAnimation,
  useLoadingAnimation,
  useNotificationAnimation,
  useChartAnimation,
  useRealTimeAnimation,
  usePageTransition,
  useGestureAnimation,
  useAnimationPerformance,
} from './useAnimationHooks';

// Re-export framer-motion components with consistent naming
export {
  motion,
  AnimatePresence,
  MotionConfig,
  useSpring,
  useInView,
  useAnimation as useFramerAnimation,
  useMotionValue,
  useTransform,
  useScroll,
  useDragControls,
} from 'framer-motion';

// Animation system types
export interface AnimationConfig {
  enableAnimations: boolean;
  reduceMotion: boolean;
  animationSpeed: number;
}

export interface AnimationMetrics {
  fps: number;
  frameDrops: number;
  animationCount: number;
}

export interface NotificationData {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
}

export interface PriceDirection {
  direction: 'up' | 'down' | 'neutral';
  changeAmount: number;
  changePercent: number;
  isPositive: boolean;
  isNegative: boolean;
  isNeutral: boolean;
}

export interface GestureState {
  isDragging: boolean;
  isSweping: boolean;
  direction: 'left' | 'right' | 'up' | 'down' | null;
}