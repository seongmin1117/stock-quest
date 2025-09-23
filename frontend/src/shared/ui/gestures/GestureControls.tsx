'use client';

import React, { useState, useRef, useCallback, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  IconButton,
  Chip,
  Alert,
  Snackbar,
  useTheme,
  alpha,
  Slide,
  Fade,
  ButtonBase,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Edit,
  Delete,
  SwipeLeft,
  SwipeRight,
  TouchApp,
  ZoomIn,
} from '@mui/icons-material';

interface SwipeableCardProps {
  children: React.ReactNode;
  onSwipeLeft?: () => void;
  onSwipeRight?: () => void;
  leftAction?: {
    icon: React.ReactNode;
    label: string;
    color: string;
  };
  rightAction?: {
    icon: React.ReactNode;
    label: string;
    color: string;
  };
  disabled?: boolean;
  hapticFeedback?: boolean;
}

/**
 * Advanced swipeable card component for mobile trading actions
 * 모바일 트레이딩 액션을 위한 고급 스와이프 카드 컴포넌트
 */
export const SwipeableCard: React.FC<SwipeableCardProps> = ({
  children,
  onSwipeLeft,
  onSwipeRight,
  leftAction,
  rightAction,
  disabled = false,
  hapticFeedback = true,
}) => {
  const theme = useTheme();
  const [dragX, setDragX] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [showHint, setShowHint] = useState(false);
  const startX = useRef<number>(0);
  const currentX = useRef<number>(0);
  const cardRef = useRef<HTMLDivElement>(null);

  const SWIPE_THRESHOLD = 120;
  const MAX_DRAG = 200;

  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    if (disabled) return;

    startX.current = e.touches[0].clientX;
    setIsDragging(true);
    setShowHint(true);

    // Haptic feedback for touch start
    if (hapticFeedback && 'vibrate' in navigator) {
      navigator.vibrate(5);
    }
  }, [disabled, hapticFeedback]);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (disabled || !isDragging) return;

    currentX.current = e.touches[0].clientX;
    const deltaX = currentX.current - startX.current;

    // Limit drag distance with resistance
    const limitedDelta = Math.sign(deltaX) * Math.min(Math.abs(deltaX), MAX_DRAG);
    setDragX(limitedDelta);
  }, [disabled, isDragging]);

  const handleTouchEnd = useCallback(() => {
    if (disabled || !isDragging) return;

    const deltaX = currentX.current - startX.current;
    const absDistance = Math.abs(deltaX);

    if (absDistance > SWIPE_THRESHOLD) {
      // Strong haptic feedback for successful swipe
      if (hapticFeedback && 'vibrate' in navigator) {
        navigator.vibrate([10, 50, 10]);
      }

      if (deltaX > 0 && onSwipeRight) {
        onSwipeRight();
      } else if (deltaX < 0 && onSwipeLeft) {
        onSwipeLeft();
      }
    }

    // Reset state
    setDragX(0);
    setIsDragging(false);
    setShowHint(false);
    startX.current = 0;
    currentX.current = 0;
  }, [disabled, isDragging, onSwipeLeft, onSwipeRight, hapticFeedback]);

  const getActionOpacity = (direction: 'left' | 'right') => {
    const distance = Math.abs(dragX);
    const maxOpacity = 0.8;

    if (direction === 'left' && dragX < 0) {
      return Math.min((distance / SWIPE_THRESHOLD) * maxOpacity, maxOpacity);
    } else if (direction === 'right' && dragX > 0) {
      return Math.min((distance / SWIPE_THRESHOLD) * maxOpacity, maxOpacity);
    }
    return 0;
  };

  const getActionScale = (direction: 'left' | 'right') => {
    const distance = Math.abs(dragX);
    const baseScale = 0.8;
    const maxScale = 1.2;

    if ((direction === 'left' && dragX < 0) || (direction === 'right' && dragX > 0)) {
      return baseScale + ((distance / SWIPE_THRESHOLD) * (maxScale - baseScale));
    }
    return baseScale;
  };

  return (
    <Box sx={{ position: 'relative', overflow: 'hidden', borderRadius: 2 }}>
      {/* Left Action Background */}
      {leftAction && (
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            backgroundColor: leftAction.color,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            pr: 3,
            opacity: getActionOpacity('left'),
            transition: isDragging ? 'none' : 'opacity 0.3s ease',
            zIndex: 1,
          }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              color: 'white',
              transform: `scale(${getActionScale('left')})`,
              transition: isDragging ? 'none' : 'transform 0.3s ease',
            }}
          >
            {leftAction.icon}
            <Typography variant="body2" fontWeight="bold">
              {leftAction.label}
            </Typography>
          </Box>
        </Box>
      )}

      {/* Right Action Background */}
      {rightAction && (
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            right: 0,
            width: '100%',
            height: '100%',
            backgroundColor: rightAction.color,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-start',
            pl: 3,
            opacity: getActionOpacity('right'),
            transition: isDragging ? 'none' : 'opacity 0.3s ease',
            zIndex: 1,
          }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              color: 'white',
              transform: `scale(${getActionScale('right')})`,
              transition: isDragging ? 'none' : 'transform 0.3s ease',
            }}
          >
            {rightAction.icon}
            <Typography variant="body2" fontWeight="bold">
              {rightAction.label}
            </Typography>
          </Box>
        </Box>
      )}

      {/* Main Card */}
      <Box
        ref={cardRef}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        sx={{
          transform: `translateX(${dragX}px)`,
          transition: isDragging ? 'none' : 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          position: 'relative',
          zIndex: 2,
          cursor: disabled ? 'default' : 'grab',
          '&:active': {
            cursor: disabled ? 'default' : 'grabbing',
          },
        }}
      >
        {children}
      </Box>

      {/* Swipe Hint */}
      <Fade in={showHint && !isDragging}>
        <Box
          sx={{
            position: 'absolute',
            bottom: 8,
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 3,
            backgroundColor: alpha(theme.palette.background.paper, 0.9),
            borderRadius: 1,
            px: 2,
            py: 0.5,
          }}
        >
          <Typography variant="caption" color="textSecondary">
            좌우로 스와이프
          </Typography>
        </Box>
      </Fade>
    </Box>
  );
};

interface TouchGestureProps {
  onTap?: () => void;
  onDoubleTap?: () => void;
  onLongPress?: () => void;
  onPinch?: (scale: number) => void;
  onPan?: (deltaX: number, deltaY: number) => void;
  children: React.ReactNode;
  hapticFeedback?: boolean;
  disabled?: boolean;
}

/**
 * Advanced touch gesture handler for chart and trading interactions
 * 차트 및 트레이딩 상호작용을 위한 고급 터치 제스처 핸들러
 */
export const TouchGestureHandler: React.FC<TouchGestureProps> = ({
  onTap,
  onDoubleTap,
  onLongPress,
  onPinch,
  onPan,
  children,
  hapticFeedback = true,
  disabled = false,
}) => {
  const [touches, setTouches] = useState<React.TouchList | null>(null);
  const [lastTap, setLastTap] = useState<number>(0);
  const longPressTimer = useRef<NodeJS.Timeout | null>(null);
  const initialDistance = useRef<number>(0);
  const initialPan = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  const DOUBLE_TAP_DELAY = 300;
  const LONG_PRESS_DELAY = 500;
  const PINCH_THRESHOLD = 10;
  const PAN_THRESHOLD = 5;

  const getDistance = (touch1: React.Touch, touch2: React.Touch) => {
    return Math.sqrt(
      Math.pow(touch2.clientX - touch1.clientX, 2) +
      Math.pow(touch2.clientY - touch1.clientY, 2)
    );
  };

  const handleTouchStart = (e: React.TouchEvent) => {
    if (disabled) return;

    const touchList = e.touches;
    setTouches(touchList);

    if (touchList.length === 1) {
      const touch = touchList[0];
      initialPan.current = { x: touch.clientX, y: touch.clientY };

      // Long press detection
      if (onLongPress) {
        longPressTimer.current = setTimeout(() => {
          if (hapticFeedback && 'vibrate' in navigator) {
            navigator.vibrate([20, 100, 20]);
          }
          onLongPress();
        }, LONG_PRESS_DELAY);
      }

      // Double tap detection
      if (onDoubleTap) {
        const now = Date.now();
        if (now - lastTap < DOUBLE_TAP_DELAY) {
          if (hapticFeedback && 'vibrate' in navigator) {
            navigator.vibrate(15);
          }
          onDoubleTap();
          setLastTap(0);
        } else {
          setLastTap(now);
        }
      }
    } else if (touchList.length === 2 && onPinch) {
      // Pinch gesture setup
      initialDistance.current = getDistance(touchList[0], touchList[1]);
    }

    // Clear long press timer on multi-touch
    if (touchList.length > 1 && longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (disabled || !touches) return;

    const touchList = e.touches;

    if (touchList.length === 1 && onPan) {
      // Pan gesture
      const touch = touchList[0];
      const deltaX = touch.clientX - initialPan.current.x;
      const deltaY = touch.clientY - initialPan.current.y;

      if (Math.abs(deltaX) > PAN_THRESHOLD || Math.abs(deltaY) > PAN_THRESHOLD) {
        // Clear long press on movement
        if (longPressTimer.current) {
          clearTimeout(longPressTimer.current);
          longPressTimer.current = null;
        }
        onPan(deltaX, deltaY);
      }
    } else if (touchList.length === 2 && onPinch) {
      // Pinch gesture
      const currentDistance = getDistance(touchList[0], touchList[1]);
      const scale = currentDistance / initialDistance.current;

      if (Math.abs(scale - 1) > PINCH_THRESHOLD / 100) {
        onPinch(scale);
      }
    }
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    if (disabled) return;

    // Clear long press timer
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
    }

    // Single tap
    if (e.changedTouches.length === 1 && onTap && !onDoubleTap) {
      if (hapticFeedback && 'vibrate' in navigator) {
        navigator.vibrate(5);
      }
      onTap();
    }

    setTouches(null);
  };

  return (
    <Box
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
      sx={{
        touchAction: 'none',
        userSelect: 'none',
        WebkitUserSelect: 'none',
        WebkitTouchCallout: 'none',
      }}
    >
      {children}
    </Box>
  );
};

interface QuickOrderGestureProps {
  symbol: string;
  currentPrice: number;
  onBuyOrder: (quantity: number) => void;
  onSellOrder: (quantity: number) => void;
  disabled?: boolean;
}

/**
 * Quick order gesture component for rapid trading
 * 빠른 거래를 위한 퀵 오더 제스처 컴포넌트
 */
export const QuickOrderGesture: React.FC<QuickOrderGestureProps> = ({
  symbol,
  currentPrice,
  onBuyOrder,
  onSellOrder,
  disabled = false,
}) => {
  const theme = useTheme();
  const [showFeedback, setShowFeedback] = useState<{
    type: 'buy' | 'sell' | null;
    quantity: number;
  }>({ type: null, quantity: 0 });

  const handleQuickBuy = (quantity: number) => {
    if (disabled) return;

    setShowFeedback({ type: 'buy', quantity });
    onBuyOrder(quantity);

    setTimeout(() => {
      setShowFeedback({ type: null, quantity: 0 });
    }, 2000);
  };

  const handleQuickSell = (quantity: number) => {
    if (disabled) return;

    setShowFeedback({ type: 'sell', quantity });
    onSellOrder(quantity);

    setTimeout(() => {
      setShowFeedback({ type: null, quantity: 0 });
    }, 2000);
  };

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h6" gutterBottom align="center">
        {symbol} 퀵 주문
      </Typography>

      <Typography variant="body2" color="textSecondary" align="center" sx={{ mb: 2 }}>
        현재가: ₩{currentPrice.toLocaleString()}
      </Typography>

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: 2,
          mb: 2,
        }}
      >
        {/* Quick Buy Buttons */}
        <Box>
          <Typography variant="subtitle2" color="success.main" sx={{ mb: 1 }}>
            매수
          </Typography>
          {[10, 50, 100].map((quantity) => (
            <TouchGestureHandler
              key={`buy-${quantity}`}
              onTap={() => handleQuickBuy(quantity)}
              hapticFeedback={true}
            >
              <ButtonBase
                sx={{
                  width: '100%',
                  mb: 1,
                  p: 1.5,
                  borderRadius: 2,
                  backgroundColor: alpha(theme.palette.success.main, 0.1),
                  border: `1px solid ${theme.palette.success.main}`,
                  transition: 'all 0.2s ease',
                  '&:active': {
                    transform: 'scale(0.95)',
                    backgroundColor: alpha(theme.palette.success.main, 0.2),
                  },
                }}
                disabled={disabled}
              >
                <Box sx={{ textAlign: 'center' }}>
                  <TrendingUp sx={{ color: 'success.main', mb: 0.5 }} />
                  <Typography variant="body2" color="success.main" fontWeight="bold">
                    {quantity}주
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    ₩{(quantity * currentPrice).toLocaleString()}
                  </Typography>
                </Box>
              </ButtonBase>
            </TouchGestureHandler>
          ))}
        </Box>

        {/* Quick Sell Buttons */}
        <Box>
          <Typography variant="subtitle2" color="error.main" sx={{ mb: 1 }}>
            매도
          </Typography>
          {[10, 50, 100].map((quantity) => (
            <TouchGestureHandler
              key={`sell-${quantity}`}
              onTap={() => handleQuickSell(quantity)}
              hapticFeedback={true}
            >
              <ButtonBase
                sx={{
                  width: '100%',
                  mb: 1,
                  p: 1.5,
                  borderRadius: 2,
                  backgroundColor: alpha(theme.palette.error.main, 0.1),
                  border: `1px solid ${theme.palette.error.main}`,
                  transition: 'all 0.2s ease',
                  '&:active': {
                    transform: 'scale(0.95)',
                    backgroundColor: alpha(theme.palette.error.main, 0.2),
                  },
                }}
                disabled={disabled}
              >
                <Box sx={{ textAlign: 'center' }}>
                  <TrendingDown sx={{ color: 'error.main', mb: 0.5 }} />
                  <Typography variant="body2" color="error.main" fontWeight="bold">
                    {quantity}주
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    ₩{(quantity * currentPrice).toLocaleString()}
                  </Typography>
                </Box>
              </ButtonBase>
            </TouchGestureHandler>
          ))}
        </Box>
      </Box>

      {/* Order Feedback */}
      <Snackbar
        open={showFeedback.type !== null}
        autoHideDuration={2000}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity={showFeedback.type === 'buy' ? 'success' : 'error'}
          variant="filled"
          sx={{ width: '100%' }}
        >
          {showFeedback.type === 'buy' ? '매수' : '매도'} 주문 접수: {showFeedback.quantity}주
        </Alert>
      </Snackbar>

      {/* Gesture Hints */}
      <Box
        sx={{
          mt: 2,
          p: 2,
          backgroundColor: alpha(theme.palette.info.main, 0.1),
          borderRadius: 2,
          border: `1px solid ${alpha(theme.palette.info.main, 0.3)}`,
        }}
      >
        <Typography variant="caption" color="info.main" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <TouchApp fontSize="small" />
          터치로 빠른 주문, 길게 눌러서 상세 설정
        </Typography>
      </Box>
    </Box>
  );
};

interface GestureHintProps {
  hints: Array<{
    gesture: string;
    description: string;
    icon: React.ReactNode;
  }>;
  show: boolean;
  onDismiss: () => void;
}

/**
 * Gesture hints overlay for user education
 * 사용자 교육을 위한 제스처 힌트 오버레이
 */
export const GestureHints: React.FC<GestureHintProps> = ({
  hints,
  show,
  onDismiss,
}) => {
  const theme = useTheme();

  return (
    <Slide direction="up" in={show} mountOnEnter unmountOnExit>
      <Box
        sx={{
          position: 'fixed',
          bottom: 0,
          left: 0,
          right: 0,
          zIndex: 1300,
          backgroundColor: theme.palette.background.paper,
          borderTopLeftRadius: 16,
          borderTopRightRadius: 16,
          boxShadow: theme.shadows[16],
          p: 3,
          maxHeight: '50vh',
          overflow: 'auto',
        }}
      >
        <Typography variant="h6" gutterBottom>
          제스처 가이드
        </Typography>

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mb: 3 }}>
          {hints.map((hint, index) => (
            <Box
              key={index}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 2,
                p: 2,
                backgroundColor: alpha(theme.palette.primary.main, 0.05),
                borderRadius: 2,
                border: `1px solid ${alpha(theme.palette.primary.main, 0.1)}`,
              }}
            >
              <Box sx={{ color: 'primary.main' }}>
                {hint.icon}
              </Box>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  {hint.gesture}
                </Typography>
                <Typography variant="caption" color="textSecondary">
                  {hint.description}
                </Typography>
              </Box>
            </Box>
          ))}
        </Box>

        <ButtonBase
          onClick={onDismiss}
          sx={{
            width: '100%',
            p: 2,
            borderRadius: 2,
            backgroundColor: theme.palette.primary.main,
            color: theme.palette.primary.contrastText,
            fontWeight: 'bold',
          }}
        >
          확인
        </ButtonBase>
      </Box>
    </Slide>
  );
};

// Export common gesture hint configurations
export const tradingGestureHints = [
  {
    gesture: '좌우 스와이프',
    description: '종목 카드에서 빠른 매수/매도',
    icon: <SwipeLeft />,
  },
  {
    gesture: '더블 탭',
    description: '차트 확대/축소 리셋',
    icon: <TouchApp />,
  },
  {
    gesture: '핀치',
    description: '차트 확대/축소',
    icon: <ZoomIn />,
  },
  {
    gesture: '길게 누르기',
    description: '상세 정보 표시',
    icon: <TouchApp />,
  },
];

export const portfolioGestureHints = [
  {
    gesture: '좌로 스와이프',
    description: '포지션 매도',
    icon: <SwipeLeft />,
  },
  {
    gesture: '우로 스와이프',
    description: '포지션 편집',
    icon: <SwipeRight />,
  },
  {
    gesture: '탭',
    description: '포지션 상세 보기',
    icon: <TouchApp />,
  },
];