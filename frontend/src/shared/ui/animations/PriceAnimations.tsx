'use client';

import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Typography,
  useTheme,
  alpha,
  Fade,
  Grow,
  Slide,
  Zoom
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Remove
} from '@mui/icons-material';

interface AnimatedPriceProps {
  value: number;
  previousValue?: number;
  currency?: string;
  precision?: number;
  showChange?: boolean;
  showPercent?: boolean;
  animationType?: 'slide' | 'flash' | 'pulse' | 'scale' | 'glow';
  duration?: number;
  variant?: 'h4' | 'h5' | 'h6' | 'body1' | 'body2';
  fontFamily?: string;
}

/**
 * Animated price component with smooth transitions and visual feedback
 * 부드러운 전환과 시각적 피드백이 있는 애니메이션 가격 컴포넌트
 */
export const AnimatedPrice: React.FC<AnimatedPriceProps> = ({
  value,
  previousValue,
  currency = 'USD',
  precision = 2,
  showChange = true,
  showPercent = true,
  animationType = 'flash',
  duration = 800,
  variant = 'h5',
  fontFamily = 'monospace'
}) => {
  const theme = useTheme();
  const [isAnimating, setIsAnimating] = useState(false);
  const [direction, setDirection] = useState<'up' | 'down' | 'neutral'>('neutral');
  const prevValueRef = useRef(previousValue || value);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency,
      minimumFractionDigits: precision,
      maximumFractionDigits: precision
    }).format(price);
  };

  const formatChange = (current: number, previous: number) => {
    const change = current - previous;
    const changePercent = ((change / previous) * 100);

    return {
      change,
      changePercent,
      isPositive: change > 0,
      isNegative: change < 0
    };
  };

  useEffect(() => {
    if (previousValue !== undefined && value !== prevValueRef.current) {
      const newDirection = value > prevValueRef.current ? 'up' : value < prevValueRef.current ? 'down' : 'neutral';
      setDirection(newDirection);

      if (newDirection !== 'neutral') {
        setIsAnimating(true);
        setTimeout(() => setIsAnimating(false), duration);
      }

      prevValueRef.current = value;
    }
  }, [value, previousValue, duration]);

  const getAnimationProps = () => {
    switch (animationType) {
      case 'slide':
        return {
          transform: isAnimating
            ? direction === 'up'
              ? 'translateY(-2px)'
              : 'translateY(2px)'
            : 'translateY(0)',
          transition: `transform ${duration}ms cubic-bezier(0.4, 0, 0.2, 1)`
        };

      case 'flash':
        return {
          backgroundColor: isAnimating
            ? direction === 'up'
              ? alpha(theme.palette.success.main, 0.2)
              : alpha(theme.palette.error.main, 0.2)
            : 'transparent',
          transition: `background-color ${duration}ms ease-in-out`,
          borderRadius: 1,
          px: 1
        };

      case 'pulse':
        return {
          animation: isAnimating
            ? direction === 'up'
              ? `pulseGreen ${duration}ms ease-in-out`
              : `pulseRed ${duration}ms ease-in-out`
            : 'none',
          '@keyframes pulseGreen': {
            '0%': { boxShadow: `0 0 0 0 ${alpha(theme.palette.success.main, 0.4)}` },
            '50%': { boxShadow: `0 0 0 8px ${alpha(theme.palette.success.main, 0.1)}` },
            '100%': { boxShadow: `0 0 0 0 ${alpha(theme.palette.success.main, 0)}` }
          },
          '@keyframes pulseRed': {
            '0%': { boxShadow: `0 0 0 0 ${alpha(theme.palette.error.main, 0.4)}` },
            '50%': { boxShadow: `0 0 0 8px ${alpha(theme.palette.error.main, 0.1)}` },
            '100%': { boxShadow: `0 0 0 0 ${alpha(theme.palette.error.main, 0)}` }
          }
        };

      case 'scale':
        return {
          transform: isAnimating ? 'scale(1.05)' : 'scale(1)',
          transition: `transform ${duration}ms cubic-bezier(0.34, 1.56, 0.64, 1)`
        };

      case 'glow':
        return {
          textShadow: isAnimating
            ? direction === 'up'
              ? `0 0 8px ${theme.palette.success.main}`
              : `0 0 8px ${theme.palette.error.main}`
            : 'none',
          transition: `text-shadow ${duration}ms ease-in-out`
        };

      default:
        return {};
    }
  };

  const getPriceColor = () => {
    if (!isAnimating && direction === 'neutral') return theme.palette.text.primary;

    switch (direction) {
      case 'up':
        return theme.palette.success.main;
      case 'down':
        return theme.palette.error.main;
      default:
        return theme.palette.text.primary;
    }
  };

  const change = previousValue ? formatChange(value, previousValue) : null;

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Typography
        variant={variant}
        sx={{
          fontFamily,
          fontWeight: 700,
          color: getPriceColor(),
          ...getAnimationProps()
        }}
      >
        {formatPrice(value)}
      </Typography>

      {showChange && change && change.change !== 0 && (
        <Fade in timeout={300}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            {change.isPositive ? (
              <TrendingUp sx={{ fontSize: 16, color: theme.palette.success.main }} />
            ) : change.isNegative ? (
              <TrendingDown sx={{ fontSize: 16, color: theme.palette.error.main }} />
            ) : (
              <Remove sx={{ fontSize: 16, color: theme.palette.text.secondary }} />
            )}

            <Typography
              variant="body2"
              sx={{
                fontFamily,
                fontWeight: 600,
                color: change.isPositive
                  ? theme.palette.success.main
                  : change.isNegative
                    ? theme.palette.error.main
                    : theme.palette.text.secondary
              }}
            >
              {change.isPositive ? '+' : ''}{formatPrice(Math.abs(change.change))}
              {showPercent && ` (${change.isPositive ? '+' : ''}${change.changePercent.toFixed(2)}%)`}
            </Typography>
          </Box>
        </Fade>
      )}
    </Box>
  );
};

/**
 * Animated portfolio value with enhanced visual feedback
 * 향상된 시각적 피드백이 있는 애니메이션 포트폴리오 값
 */
export const AnimatedPortfolioValue: React.FC<{
  value: number;
  previousValue?: number;
  totalReturn: number;
  totalReturnPercent: number;
  dailyChange: number;
  dailyChangePercent: number;
}> = ({
  value,
  previousValue,
  totalReturn,
  totalReturnPercent,
  dailyChange,
  dailyChangePercent
}) => {
  const theme = useTheme();

  return (
    <Box sx={{ textAlign: 'center', py: 2 }}>
      <Typography variant="caption" sx={{ color: theme.palette.text.secondary, mb: 1, display: 'block' }}>
        총 포트폴리오 가치
      </Typography>

      <AnimatedPrice
        value={value}
        previousValue={previousValue}
        variant="h4"
        animationType="glow"
        showChange={false}
        duration={1000}
      />

      <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center', gap: 3 }}>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="caption" sx={{ color: theme.palette.text.secondary, display: 'block' }}>
            총 수익
          </Typography>
          <Typography
            variant="h6"
            sx={{
              fontFamily: 'monospace',
              fontWeight: 700,
              color: totalReturn >= 0 ? theme.palette.success.main : theme.palette.error.main
            }}
          >
            {totalReturn >= 0 ? '+' : ''}${totalReturn.toLocaleString()}
          </Typography>
          <Typography
            variant="body2"
            sx={{
              fontFamily: 'monospace',
              color: totalReturn >= 0 ? theme.palette.success.main : theme.palette.error.main
            }}
          >
            ({totalReturn >= 0 ? '+' : ''}{totalReturnPercent.toFixed(2)}%)
          </Typography>
        </Box>

        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="caption" sx={{ color: theme.palette.text.secondary, display: 'block' }}>
            일일 변동
          </Typography>
          <AnimatedPrice
            value={dailyChange}
            variant="h6"
            animationType="pulse"
            showPercent={false}
            showChange={false}
            duration={600}
          />
          <Typography
            variant="body2"
            sx={{
              fontFamily: 'monospace',
              color: dailyChange >= 0 ? theme.palette.success.main : theme.palette.error.main
            }}
          >
            ({dailyChange >= 0 ? '+' : ''}{dailyChangePercent.toFixed(2)}%)
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};

/**
 * Animated chart price ticker for real-time updates
 * 실시간 업데이트용 애니메이션 차트 가격 티커
 */
export const PriceTicker: React.FC<{
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  volume?: number;
  size?: 'small' | 'medium' | 'large';
}> = ({
  symbol,
  price,
  change,
  changePercent,
  volume,
  size = 'medium'
}) => {
  const theme = useTheme();
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    // Simulate price update animation
    setIsVisible(false);
    const timer = setTimeout(() => setIsVisible(true), 100);
    return () => clearTimeout(timer);
  }, [price, change]);

  const sizeConfig = {
    small: { variant: 'body2' as const, iconSize: 16 },
    medium: { variant: 'h6' as const, iconSize: 20 },
    large: { variant: 'h5' as const, iconSize: 24 }
  };

  const config = sizeConfig[size];
  const isPositive = change >= 0;

  return (
    <Zoom in={isVisible} timeout={200}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          p: 2,
          backgroundColor: alpha(
            isPositive ? theme.palette.success.main : theme.palette.error.main,
            0.1
          ),
          borderLeft: `4px solid ${isPositive ? theme.palette.success.main : theme.palette.error.main}`,
          borderRadius: 1,
          transition: 'all 0.3s ease-in-out'
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {isPositive ? (
            <TrendingUp sx={{ fontSize: config.iconSize, color: theme.palette.success.main }} />
          ) : (
            <TrendingDown sx={{ fontSize: config.iconSize, color: theme.palette.error.main }} />
          )}

          <Typography variant={config.variant} sx={{ fontWeight: 700 }}>
            {symbol}
          </Typography>
        </Box>

        <Box sx={{ textAlign: 'right', flex: 1 }}>
          <AnimatedPrice
            value={price}
            variant={config.variant}
            animationType="flash"
            showChange={false}
          />

          <Typography
            variant="body2"
            sx={{
              fontFamily: 'monospace',
              fontWeight: 600,
              color: isPositive ? theme.palette.success.main : theme.palette.error.main
            }}
          >
            {isPositive ? '+' : ''}${change.toFixed(2)} ({isPositive ? '+' : ''}{changePercent.toFixed(2)}%)
          </Typography>

          {volume && (
            <Typography variant="caption" sx={{ color: theme.palette.text.secondary, display: 'block' }}>
              Vol: {volume.toLocaleString()}
            </Typography>
          )}
        </Box>
      </Box>
    </Zoom>
  );
};

/**
 * Smooth number counter animation
 * 부드러운 숫자 카운터 애니메이션
 */
export const CountUp: React.FC<{
  value: number;
  duration?: number;
  precision?: number;
  prefix?: string;
  suffix?: string;
  variant?: 'h4' | 'h5' | 'h6' | 'body1' | 'body2';
  sx?: object;
}> = ({
  value,
  duration = 1000,
  precision = 0,
  prefix = '',
  suffix = '',
  variant = 'h5',
  sx = {}
}) => {
  const [currentValue, setCurrentValue] = useState(0);
  const startTimeRef = useRef<number | null>(null);
  const animationRef = useRef<number | null>(null);

  useEffect(() => {
    const startValue = currentValue;
    const difference = value - startValue;

    const animate = (timestamp: number) => {
      if (!startTimeRef.current) {
        startTimeRef.current = timestamp;
      }

      const elapsed = timestamp - startTimeRef.current;
      const progress = Math.min(elapsed / duration, 1);

      // Easing function for smooth animation
      const easeOutCubic = 1 - Math.pow(1 - progress, 3);

      setCurrentValue(startValue + (difference * easeOutCubic));

      if (progress < 1) {
        animationRef.current = requestAnimationFrame(animate);
      } else {
        setCurrentValue(value);
        startTimeRef.current = null;
      }
    };

    animationRef.current = requestAnimationFrame(animate);

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [value, duration]);

  const formatValue = (val: number) => {
    return val.toFixed(precision);
  };

  return (
    <Typography
      variant={variant}
      sx={{
        fontFamily: 'monospace',
        fontWeight: 700,
        ...sx
      }}
    >
      {prefix}{formatValue(currentValue)}{suffix}
    </Typography>
  );
};

/**
 * Loading pulse animation for price placeholders
 * 가격 플레이스홀더용 로딩 펄스 애니메이션
 */
export const PricePlaceholder: React.FC<{
  width?: number | string;
  height?: number;
  variant?: 'price' | 'change' | 'volume';
}> = ({ width = 120, height = 24, variant = 'price' }) => {
  const theme = useTheme();

  return (
    <Box
      sx={{
        width,
        height,
        backgroundColor: alpha(theme.palette.text.primary, 0.1),
        borderRadius: 1,
        animation: 'shimmer 1.5s ease-in-out infinite',
        '@keyframes shimmer': {
          '0%': {
            opacity: 0.5,
            transform: 'scale(1)'
          },
          '50%': {
            opacity: 0.8,
            transform: 'scale(1.02)'
          },
          '100%': {
            opacity: 0.5,
            transform: 'scale(1)'
          }
        }
      }}
    />
  );
};