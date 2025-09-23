'use client';

import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Box, Typography, Chip, Card, CardContent } from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';
import {
  priceChangeVariants,
  portfolioValueVariants,
  orderStatusVariants,
  cardVariants,
  notificationVariants,
  pulseVariants,
} from './motionVariants';
import { useAnimation } from './AnimationProvider';

interface AnimatedPriceProps {
  price: number;
  previousPrice?: number;
  currency?: string;
  fontSize?: string | number;
  showTrend?: boolean;
}

export function AnimatedPrice({
  price,
  previousPrice = price,
  currency = '₩',
  fontSize = '1.2rem',
  showTrend = true,
}: AnimatedPriceProps) {
  const { enableAnimations } = useAnimation();
  const [priceDirection, setPriceDirection] = useState<'neutral' | 'increase' | 'decrease'>('neutral');

  useEffect(() => {
    if (price > previousPrice) {
      setPriceDirection('increase');
    } else if (price < previousPrice) {
      setPriceDirection('decrease');
    } else {
      setPriceDirection('neutral');
    }
  }, [price, previousPrice]);

  const formatPrice = (value: number) => {
    return `${currency}${value.toLocaleString()}`;
  };

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <motion.div
        animate={enableAnimations ? priceDirection : 'neutral'}
        variants={priceChangeVariants}
        style={{ fontSize }}
      >
        <Typography
          variant="h6"
          component="span"
          fontWeight="bold"
          sx={{ fontSize: 'inherit' }}
        >
          {formatPrice(price)}
        </Typography>
      </motion.div>

      {showTrend && priceDirection !== 'neutral' && enableAnimations && (
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0, opacity: 0 }}
          transition={{ duration: 0.3 }}
        >
          {priceDirection === 'increase' ? (
            <TrendingUp sx={{ color: '#4caf50', fontSize: '1.2em' }} />
          ) : (
            <TrendingDown sx={{ color: '#f44336', fontSize: '1.2em' }} />
          )}
        </motion.div>
      )}
    </Box>
  );
}

interface AnimatedPortfolioValueProps {
  currentValue: number;
  previousValue?: number;
  isUpdating?: boolean;
  currency?: string;
}

export function AnimatedPortfolioValue({
  currentValue,
  previousValue = currentValue,
  isUpdating = false,
  currency = '₩',
}: AnimatedPortfolioValueProps) {
  const { enableAnimations } = useAnimation();
  const [valueState, setValueState] = useState<'initial' | 'updating' | 'profit' | 'loss'>('initial');

  useEffect(() => {
    if (isUpdating) {
      setValueState('updating');
    } else if (currentValue > previousValue) {
      setValueState('profit');
    } else if (currentValue < previousValue) {
      setValueState('loss');
    } else {
      setValueState('initial');
    }
  }, [currentValue, previousValue, isUpdating]);

  return (
    <motion.div
      animate={enableAnimations ? valueState : 'initial'}
      variants={portfolioValueVariants}
    >
      <Typography variant="h4" component="div" fontWeight="bold">
        {currency}{currentValue.toLocaleString()}
      </Typography>
    </motion.div>
  );
}

interface AnimatedOrderStatusProps {
  status: 'PENDING' | 'FILLED' | 'CANCELLED' | 'REJECTED';
  symbol: string;
  quantity: number;
  price?: number;
}

export function AnimatedOrderStatus({
  status,
  symbol,
  quantity,
  price,
}: AnimatedOrderStatusProps) {
  const { enableAnimations } = useAnimation();
  const statusLower = status.toLowerCase() as 'pending' | 'filled' | 'cancelled' | 'rejected';

  const getStatusColor = (orderStatus: string) => {
    switch (orderStatus) {
      case 'FILLED': return 'success';
      case 'PENDING': return 'warning';
      case 'CANCELLED': return 'error';
      case 'REJECTED': return 'error';
      default: return 'default';
    }
  };

  const getStatusText = (orderStatus: string) => {
    switch (orderStatus) {
      case 'FILLED': return '체결완료';
      case 'PENDING': return '대기중';
      case 'CANCELLED': return '취소됨';
      case 'REJECTED': return '거부됨';
      default: return orderStatus;
    }
  };

  return (
    <motion.div
      animate={enableAnimations ? statusLower : 'filled'}
      variants={orderStatusVariants}
      style={{ display: 'inline-block' }}
    >
      <Card variant="outlined" sx={{ mb: 1 }}>
        <CardContent sx={{ '&:last-child': { pb: 2 } }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box>
              <Typography variant="body1" fontWeight="medium">
                {symbol} - {quantity}주
              </Typography>
              {price && (
                <Typography variant="body2" color="text.secondary">
                  ₩{price.toLocaleString()} / 주
                </Typography>
              )}
            </Box>
            <Chip
              label={getStatusText(status)}
              color={getStatusColor(status) as any}
              size="small"
              variant={status === 'PENDING' ? 'filled' : 'outlined'}
            />
          </Box>
        </CardContent>
      </Card>
    </motion.div>
  );
}

interface AnimatedConnectionStatusProps {
  isConnected: boolean;
  label?: string;
}

export function AnimatedConnectionStatus({
  isConnected,
  label = '실시간 연결',
}: AnimatedConnectionStatusProps) {
  const { enableAnimations } = useAnimation();

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <motion.div
        animate={enableAnimations && isConnected ? 'animate' : 'initial'}
        variants={pulseVariants}
        style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          backgroundColor: isConnected ? '#4caf50' : '#f44336',
        }}
      />
      <Typography variant="caption" color="text.secondary">
        {label}: {isConnected ? '연결됨' : '연결 끊김'}
      </Typography>
    </Box>
  );
}

interface TradingNotificationProps {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  isVisible: boolean;
  onClose?: () => void;
}

export function TradingNotification({
  id,
  type,
  title,
  message,
  isVisible,
  onClose,
}: TradingNotificationProps) {
  const { enableAnimations } = useAnimation();

  const getNotificationColor = (notificationType: string) => {
    switch (notificationType) {
      case 'success': return '#4caf50';
      case 'error': return '#f44336';
      case 'warning': return '#ff9800';
      case 'info': return '#2196f3';
      default: return '#666';
    }
  };

  const getNotificationIcon = (notificationType: string) => {
    switch (notificationType) {
      case 'success': return '✓';
      case 'error': return '✕';
      case 'warning': return '⚠';
      case 'info': return 'ℹ';
      default: return '';
    }
  };

  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          key={id}
          variants={notificationVariants}
          initial="initial"
          animate="animate"
          exit="exit"
          style={{
            position: 'fixed',
            top: 20,
            right: 20,
            zIndex: 9999,
            maxWidth: 400,
          }}
        >
          <Card
            sx={{
              borderLeft: `4px solid ${getNotificationColor(type)}`,
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Typography
                    sx={{
                      fontSize: '1.2rem',
                      color: getNotificationColor(type),
                      lineHeight: 1,
                    }}
                  >
                    {getNotificationIcon(type)}
                  </Typography>
                  <Box>
                    <Typography variant="subtitle2" fontWeight="bold">
                      {title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {message}
                    </Typography>
                  </Box>
                </Box>
                {onClose && (
                  <Typography
                    sx={{
                      cursor: 'pointer',
                      color: 'text.secondary',
                      fontSize: '1.2rem',
                      lineHeight: 1,
                      '&:hover': { color: 'text.primary' },
                    }}
                    onClick={onClose}
                  >
                    ×
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

interface AnimatedTradingCardProps {
  children: React.ReactNode;
  isLoading?: boolean;
  elevation?: number;
  onClick?: () => void;
}

export function AnimatedTradingCard({
  children,
  isLoading = false,
  elevation = 1,
  onClick,
}: AnimatedTradingCardProps) {
  const { enableAnimations } = useAnimation();

  return (
    <motion.div
      variants={cardVariants}
      initial="initial"
      animate="animate"
      whileHover={enableAnimations && !isLoading ? "hover" : undefined}
      whileTap={enableAnimations && onClick && !isLoading ? "tap" : undefined}
      style={{ cursor: onClick ? 'pointer' : 'default' }}
      onClick={onClick}
    >
      <Card elevation={elevation} sx={{ height: '100%', position: 'relative' }}>
        {isLoading && (
          <Box
            sx={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              backgroundColor: 'rgba(0,0,0,0.05)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1,
            }}
          >
            <motion.div
              animate={{ rotate: 360 }}
              transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
              style={{
                width: 24,
                height: 24,
                border: '2px solid #e0e0e0',
                borderTop: '2px solid #1976d2',
                borderRadius: '50%',
              }}
            />
          </Box>
        )}
        {children}
      </Card>
    </motion.div>
  );
}

interface AnimatedCounterProps {
  value: number;
  previousValue?: number;
  duration?: number;
  suffix?: string;
  prefix?: string;
}

export function AnimatedCounter({
  value,
  previousValue = 0,
  duration = 1000,
  suffix = '',
  prefix = '',
}: AnimatedCounterProps) {
  const { enableAnimations } = useAnimation();
  const [displayValue, setDisplayValue] = useState(previousValue);

  useEffect(() => {
    if (!enableAnimations) {
      setDisplayValue(value);
      return;
    }

    const startTime = Date.now();
    const startValue = displayValue;
    const endValue = value;
    const difference = endValue - startValue;

    const updateValue = () => {
      const now = Date.now();
      const progress = Math.min((now - startTime) / duration, 1);
      const easedProgress = 1 - Math.pow(1 - progress, 3); // Ease out cubic
      const currentValue = startValue + (difference * easedProgress);

      setDisplayValue(Math.round(currentValue));

      if (progress < 1) {
        requestAnimationFrame(updateValue);
      }
    };

    requestAnimationFrame(updateValue);
  }, [value, enableAnimations, duration, displayValue]);

  return (
    <motion.span
      key={value}
      initial={{ scale: 0.9 }}
      animate={{ scale: 1 }}
      transition={{ duration: 0.3 }}
    >
      {prefix}{displayValue.toLocaleString()}{suffix}
    </motion.span>
  );
}