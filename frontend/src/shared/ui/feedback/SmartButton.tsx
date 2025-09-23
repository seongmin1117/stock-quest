'use client';

import React, { useState, useCallback } from 'react';
import {
  Button,
  ButtonProps,
  CircularProgress,
  Box,
  Fade,
  useTheme,
  alpha
} from '@mui/material';
import {
  CheckCircle,
  ErrorOutline,
  TrendingUp,
  TrendingDown,
  Refresh
} from '@mui/icons-material';

interface SmartButtonProps extends Omit<ButtonProps, 'onClick'> {
  isLoading?: boolean;
  loadingText?: string;
  successText?: string;
  errorText?: string;
  showFeedback?: boolean;
  feedbackDuration?: number;
  onClick?: () => Promise<void> | void;
  icon?: React.ReactNode;
  loadingIcon?: React.ReactNode;
  successIcon?: React.ReactNode;
  errorIcon?: React.ReactNode;
}

type ButtonState = 'idle' | 'loading' | 'success' | 'error';

/**
 * Smart button with loading states, success/error feedback, and smooth animations
 * 로딩 상태, 성공/오류 피드백, 부드러운 애니메이션이 있는 스마트 버튼
 */
export const SmartButton: React.FC<SmartButtonProps> = ({
  isLoading: externalLoading = false,
  loadingText,
  successText,
  errorText,
  showFeedback = true,
  feedbackDuration = 2000,
  onClick,
  icon,
  loadingIcon,
  successIcon = <CheckCircle />,
  errorIcon = <ErrorOutline />,
  children,
  disabled,
  sx,
  ...props
}) => {
  const theme = useTheme();
  const [internalState, setInternalState] = useState<ButtonState>('idle');
  const [isInternalLoading, setIsInternalLoading] = useState(false);

  const isLoading = externalLoading || isInternalLoading;
  const currentState = isLoading ? 'loading' : internalState;

  const handleClick = useCallback(async () => {
    if (!onClick || isLoading || disabled) return;

    try {
      setIsInternalLoading(true);
      setInternalState('loading');

      const result = onClick();

      if (result instanceof Promise) {
        await result;
      }

      if (showFeedback) {
        setInternalState('success');
        setTimeout(() => {
          setInternalState('idle');
        }, feedbackDuration);
      } else {
        setInternalState('idle');
      }
    } catch (error) {
      console.error('Button action failed:', error);

      if (showFeedback) {
        setInternalState('error');
        setTimeout(() => {
          setInternalState('idle');
        }, feedbackDuration);
      } else {
        setInternalState('idle');
      }
    } finally {
      setIsInternalLoading(false);
    }
  }, [onClick, isLoading, disabled, showFeedback, feedbackDuration]);

  const getButtonContent = () => {
    switch (currentState) {
      case 'loading':
        return (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {loadingIcon || (
              <CircularProgress
                size={16}
                sx={{ color: 'inherit' }}
                thickness={4}
              />
            )}
            <span>{loadingText || children}</span>
          </Box>
        );

      case 'success':
        return (
          <Fade in timeout={300}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {successIcon}
              <span>{successText || '완료!'}</span>
            </Box>
          </Fade>
        );

      case 'error':
        return (
          <Fade in timeout={300}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {errorIcon}
              <span>{errorText || '오류 발생'}</span>
            </Box>
          </Fade>
        );

      default:
        return (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {icon}
            <span>{children}</span>
          </Box>
        );
    }
  };

  const getButtonColor = () => {
    switch (currentState) {
      case 'success':
        return 'success';
      case 'error':
        return 'error';
      default:
        return props.color || 'primary';
    }
  };

  return (
    <Button
      {...props}
      onClick={handleClick}
      disabled={disabled || isLoading}
      color={getButtonColor()}
      sx={{
        minHeight: 44, // Ensure touch-friendly height
        position: 'relative',
        overflow: 'hidden',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        transform: isLoading ? 'scale(0.98)' : 'scale(1)',
        '&:hover:not(:disabled)': {
          transform: 'translateY(-1px)',
          boxShadow: `0 4px 12px ${alpha(theme.palette.primary.main, 0.3)}`,
        },
        '&:active:not(:disabled)': {
          transform: 'translateY(0)',
        },
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: '-100%',
          width: '100%',
          height: '100%',
          background: `linear-gradient(90deg, transparent, ${alpha(theme.palette.common.white, 0.1)}, transparent)`,
          transition: 'left 0.6s ease-in-out',
        },
        '&:hover::before': {
          left: '100%',
        },
        ...sx
      }}
    >
      {getButtonContent()}
    </Button>
  );
};

/**
 * Trading-specific buy button with enhanced feedback
 * 향상된 피드백이 있는 트레이딩 전용 매수 버튼
 */
export const BuyButton: React.FC<Omit<SmartButtonProps, 'icon' | 'color'> & {
  symbol?: string;
  quantity?: number;
  price?: number;
}> = ({ symbol, quantity, price, ...props }) => {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  const totalValue = quantity && price ? quantity * price : 0;

  return (
    <SmartButton
      {...props}
      icon={<TrendingUp />}
      color="success"
      loadingText={`${symbol} 매수 중...`}
      successText={`${symbol} 매수 완료`}
      errorText="매수 실패"
      sx={{
        backgroundColor: '#4CAF50',
        '&:hover': {
          backgroundColor: '#388E3C',
        },
        ...props.sx
      }}
    >
      {totalValue > 0 ? `${formatCurrency(totalValue)} 매수` : props.children || '매수'}
    </SmartButton>
  );
};

/**
 * Trading-specific sell button with enhanced feedback
 * 향상된 피드백이 있는 트레이딩 전용 매도 버튼
 */
export const SellButton: React.FC<Omit<SmartButtonProps, 'icon' | 'color'> & {
  symbol?: string;
  quantity?: number;
  price?: number;
}> = ({ symbol, quantity, price, ...props }) => {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  const totalValue = quantity && price ? quantity * price : 0;

  return (
    <SmartButton
      {...props}
      icon={<TrendingDown />}
      color="error"
      loadingText={`${symbol} 매도 중...`}
      successText={`${symbol} 매도 완료`}
      errorText="매도 실패"
      sx={{
        backgroundColor: '#F44336',
        '&:hover': {
          backgroundColor: '#D32F2F',
        },
        ...props.sx
      }}
    >
      {totalValue > 0 ? `${formatCurrency(totalValue)} 매도` : props.children || '매도'}
    </SmartButton>
  );
};

/**
 * Refresh button with rotation animation
 * 회전 애니메이션이 있는 새로고침 버튼
 */
export const RefreshButton: React.FC<SmartButtonProps> = ({ sx, ...props }) => {
  const theme = useTheme();

  return (
    <SmartButton
      {...props}
      icon={<Refresh />}
      loadingIcon={
        <Refresh
          sx={{
            animation: 'spin 1s linear infinite',
            '@keyframes spin': {
              '0%': { transform: 'rotate(0deg)' },
              '100%': { transform: 'rotate(360deg)' }
            }
          }}
        />
      }
      loadingText="새로고침 중..."
      successText="업데이트 완료"
      sx={{
        minWidth: 44,
        padding: theme.spacing(1),
        '& .MuiButton-startIcon': {
          margin: 0
        },
        ...sx
      }}
    >
      {props.children || '새로고침'}
    </SmartButton>
  );
};

/**
 * Submit button with progress indicator
 * 진행률 표시기가 있는 제출 버튼
 */
export const SubmitButton: React.FC<SmartButtonProps & {
  progress?: number;
  showProgress?: boolean;
}> = ({
  progress = 0,
  showProgress = false,
  sx,
  ...props
}) => {
  const theme = useTheme();

  return (
    <Box sx={{ position: 'relative', width: '100%' }}>
      <SmartButton
        {...props}
        sx={{
          width: '100%',
          position: 'relative',
          overflow: 'hidden',
          ...sx
        }}
        loadingIcon={
          showProgress ? (
            <CircularProgress
              variant="determinate"
              value={progress}
              size={16}
              sx={{ color: 'inherit' }}
              thickness={4}
            />
          ) : undefined
        }
      >
        {props.children || '제출'}
      </SmartButton>

      {showProgress && (
        <Box
          sx={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            width: `${progress}%`,
            height: 2,
            backgroundColor: theme.palette.primary.light,
            transition: 'width 0.3s ease-in-out',
            borderRadius: '0 0 4px 4px'
          }}
        />
      )}
    </Box>
  );
};