'use client';

import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Card,
  CardProps,
  Button,
  ButtonProps,
  IconButton,
  IconButtonProps,
  useTheme,
  alpha,
  Grow,
  Slide,
  Zoom,
  Fade
} from '@mui/material';
import {
  useMediaQuery
} from '@mui/material';

interface AnimatedCardProps extends CardProps {
  hoverEffect?: 'lift' | 'glow' | 'scale' | 'border' | 'none';
  clickEffect?: 'ripple' | 'scale' | 'bounce' | 'none';
  animationDuration?: number;
  glowColor?: string;
  children: React.ReactNode;
}

/**
 * Enhanced card with hover and click animations
 * 호버 및 클릭 애니메이션이 있는 향상된 카드
 */
export const AnimatedCard: React.FC<AnimatedCardProps> = ({
  hoverEffect = 'lift',
  clickEffect = 'ripple',
  animationDuration = 300,
  glowColor,
  sx,
  children,
  ...props
}) => {
  const theme = useTheme();
  const [isHovered, setIsHovered] = useState(false);
  const [isPressed, setIsPressed] = useState(false);
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const getHoverStyles = () => {
    if (!isHovered || isMobile) return {};

    switch (hoverEffect) {
      case 'lift':
        return {
          transform: 'translateY(-4px)',
          boxShadow: `0 8px 24px ${alpha(theme.palette.common.black, 0.15)}`
        };

      case 'glow':
        const color = glowColor || theme.palette.primary.main;
        return {
          boxShadow: `0 0 20px ${alpha(color, 0.3)}, 0 4px 12px ${alpha(theme.palette.common.black, 0.1)}`
        };

      case 'scale':
        return {
          transform: 'scale(1.02)'
        };

      case 'border':
        return {
          borderColor: theme.palette.primary.main,
          borderWidth: 2
        };

      default:
        return {};
    }
  };

  const getClickStyles = () => {
    if (!isPressed) return {};

    switch (clickEffect) {
      case 'scale':
        return {
          transform: 'scale(0.98)'
        };

      case 'bounce':
        return {
          transform: 'scale(1.05)',
          animation: 'bounce 0.3s ease-in-out'
        };

      default:
        return {};
    }
  };

  return (
    <Card
      {...props}
      sx={{
        position: 'relative',
        cursor: props.onClick ? 'pointer' : 'default',
        transition: `all ${animationDuration}ms cubic-bezier(0.4, 0, 0.2, 1)`,
        overflow: 'hidden',
        userSelect: 'none',
        ...getHoverStyles(),
        ...getClickStyles(),
        '@keyframes bounce': {
          '0%, 100%': { transform: 'scale(1)' },
          '50%': { transform: 'scale(1.05)' }
        },
        ...sx
      }}
      onMouseEnter={() => !isMobile && setIsHovered(true)}
      onMouseLeave={() => {
        !isMobile && setIsHovered(false);
        setIsPressed(false);
      }}
      onMouseDown={() => setIsPressed(true)}
      onMouseUp={() => setIsPressed(false)}
    >
      {children}

      {/* Ripple effect handled by Material-UI Button ripple internally */}
    </Card>
  );
};

interface AnimatedButtonProps extends ButtonProps {
  feedbackType?: 'bounce' | 'pulse' | 'shine' | 'wave' | 'glow';
  loadingAnimation?: boolean;
  successAnimation?: boolean;
  hapticFeedback?: boolean;
}

/**
 * Enhanced button with micro-interactions and feedback animations
 * 마이크로 인터랙션과 피드백 애니메이션이 있는 향상된 버튼
 */
export const AnimatedButton: React.FC<AnimatedButtonProps> = ({
  feedbackType = 'bounce',
  loadingAnimation = false,
  successAnimation = false,
  hapticFeedback = true,
  sx,
  children,
  onClick,
  ...props
}) => {
  const theme = useTheme();
  const [isActive, setIsActive] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (successAnimation) {
      setShowSuccess(true);
      const timer = setTimeout(() => setShowSuccess(false), 1500);
      return () => clearTimeout(timer);
    }
  }, [successAnimation]);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setIsActive(true);

    // Haptic feedback for mobile devices
    if (hapticFeedback && 'vibrate' in navigator) {
      navigator.vibrate(10);
    }

    // Reset animation after duration
    setTimeout(() => setIsActive(false), 300);

    if (onClick) {
      onClick(event);
    }
  };

  const getAnimationStyles = () => {
    const styles: any = {
      position: 'relative',
      overflow: 'hidden',
      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
      '&:hover:not(:disabled)': {
        transform: 'translateY(-1px)',
        boxShadow: `0 4px 12px ${alpha(theme.palette.primary.main, 0.3)}`
      },
      '&:active:not(:disabled)': {
        transform: 'translateY(0)'
      }
    };

    if (isActive) {
      switch (feedbackType) {
        case 'bounce':
          styles.animation = 'buttonBounce 0.3s ease-in-out';
          styles['@keyframes buttonBounce'] = {
            '0%, 100%': { transform: 'scale(1)' },
            '50%': { transform: 'scale(1.05)' }
          };
          break;

        case 'pulse':
          styles.animation = 'buttonPulse 0.4s ease-in-out';
          styles['@keyframes buttonPulse'] = {
            '0%': { boxShadow: `0 0 0 0 ${alpha(theme.palette.primary.main, 0.7)}` },
            '70%': { boxShadow: `0 0 0 10px ${alpha(theme.palette.primary.main, 0)}` },
            '100%': { boxShadow: `0 0 0 0 ${alpha(theme.palette.primary.main, 0)}` }
          };
          break;

        case 'wave':
          styles.animation = 'buttonWave 0.6s ease-out';
          styles['@keyframes buttonWave'] = {
            '0%': { transform: 'scale(1)', boxShadow: `0 0 0 0 ${alpha(theme.palette.primary.main, 0.5)}` },
            '50%': { transform: 'scale(1.02)', boxShadow: `0 0 0 15px ${alpha(theme.palette.primary.main, 0.1)}` },
            '100%': { transform: 'scale(1)', boxShadow: `0 0 0 30px ${alpha(theme.palette.primary.main, 0)}` }
          };
          break;

        case 'glow':
          styles.boxShadow = `0 0 20px ${theme.palette.primary.main}`;
          break;
      }
    }

    if (feedbackType === 'shine') {
      styles['&::before'] = {
        content: '""',
        position: 'absolute',
        top: 0,
        left: '-100%',
        width: '100%',
        height: '100%',
        background: `linear-gradient(90deg, transparent, ${alpha(theme.palette.common.white, 0.2)}, transparent)`,
        transition: 'left 0.6s ease-in-out'
      };

      if (isActive) {
        styles['&::before'].left = '100%';
      }
    }

    if (loadingAnimation) {
      styles.animation = 'buttonLoading 1s ease-in-out infinite';
      styles['@keyframes buttonLoading'] = {
        '0%, 100%': { opacity: 1 },
        '50%': { opacity: 0.7 }
      };
    }

    if (showSuccess) {
      styles.backgroundColor = theme.palette.success.main;
      styles.animation = 'buttonSuccess 1.5s ease-in-out';
      styles['@keyframes buttonSuccess'] = {
        '0%': { transform: 'scale(1)', backgroundColor: theme.palette.primary.main },
        '20%': { transform: 'scale(1.1)', backgroundColor: theme.palette.success.main },
        '40%': { transform: 'scale(1)', backgroundColor: theme.palette.success.main },
        '100%': { transform: 'scale(1)', backgroundColor: theme.palette.primary.main }
      };
    }

    return styles;
  };

  return (
    <Button
      {...props}
      ref={buttonRef}
      onClick={handleClick}
      sx={{
        ...getAnimationStyles(),
        ...sx
      }}
    >
      {children}
    </Button>
  );
};

/**
 * Floating Action Button with enhanced animations
 * 향상된 애니메이션이 있는 플로팅 액션 버튼
 */
export const AnimatedFAB: React.FC<IconButtonProps & {
  animationType?: 'pulse' | 'bounce' | 'rotate' | 'scale';
  autoAnimate?: boolean;
  interval?: number;
}> = ({
  animationType = 'pulse',
  autoAnimate = false,
  interval = 3000,
  sx,
  children,
  ...props
}) => {
  const theme = useTheme();
  const [isAnimating, setIsAnimating] = useState(false);

  useEffect(() => {
    if (autoAnimate) {
      const timer = setInterval(() => {
        setIsAnimating(true);
        setTimeout(() => setIsAnimating(false), 1000);
      }, interval);

      return () => clearInterval(timer);
    }
  }, [autoAnimate, interval]);

  const getAnimationStyles = () => {
    if (!isAnimating) return {};

    switch (animationType) {
      case 'pulse':
        return {
          animation: 'fabPulse 1s ease-in-out',
          '@keyframes fabPulse': {
            '0%': { transform: 'scale(1)', boxShadow: `0 0 0 0 ${alpha(theme.palette.primary.main, 0.7)}` },
            '70%': { transform: 'scale(1.05)', boxShadow: `0 0 0 20px ${alpha(theme.palette.primary.main, 0)}` },
            '100%': { transform: 'scale(1)', boxShadow: `0 0 0 0 ${alpha(theme.palette.primary.main, 0)}` }
          }
        };

      case 'bounce':
        return {
          animation: 'fabBounce 1s ease-in-out',
          '@keyframes fabBounce': {
            '0%, 20%, 50%, 80%, 100%': { transform: 'translateY(0)' },
            '40%': { transform: 'translateY(-10px)' },
            '60%': { transform: 'translateY(-5px)' }
          }
        };

      case 'rotate':
        return {
          animation: 'fabRotate 1s ease-in-out',
          '@keyframes fabRotate': {
            '0%': { transform: 'rotate(0deg)' },
            '100%': { transform: 'rotate(360deg)' }
          }
        };

      case 'scale':
        return {
          animation: 'fabScale 1s ease-in-out',
          '@keyframes fabScale': {
            '0%, 100%': { transform: 'scale(1)' },
            '50%': { transform: 'scale(1.2)' }
          }
        };

      default:
        return {};
    }
  };

  return (
    <IconButton
      {...props}
      sx={{
        position: 'fixed',
        bottom: 24,
        right: 24,
        backgroundColor: theme.palette.primary.main,
        color: theme.palette.primary.contrastText,
        width: 56,
        height: 56,
        boxShadow: theme.shadows[6],
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
          backgroundColor: theme.palette.primary.dark,
          transform: 'scale(1.1)',
          boxShadow: theme.shadows[12]
        },
        ...getAnimationStyles(),
        ...sx
      }}
    >
      {children}
    </IconButton>
  );
};

/**
 * List item with staggered animation entrance
 * 지연된 애니메이션 입장이 있는 리스트 아이템
 */
export const StaggeredListItem: React.FC<{
  children: React.ReactNode;
  index: number;
  delay?: number;
  animation?: 'slideUp' | 'slideLeft' | 'fadeIn' | 'scale';
}> = ({ children, index, delay = 100, animation = 'slideUp' }) => {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setIsVisible(true), index * delay);
    return () => clearTimeout(timer);
  }, [index, delay]);

  const getTransitionComponent = () => {
    switch (animation) {
      case 'slideUp':
        return (
          <Slide in={isVisible} direction="up" timeout={600}>
            <Box>{children}</Box>
          </Slide>
        );

      case 'slideLeft':
        return (
          <Slide in={isVisible} direction="left" timeout={600}>
            <Box>{children}</Box>
          </Slide>
        );

      case 'fadeIn':
        return (
          <Fade in={isVisible} timeout={600}>
            <Box>{children}</Box>
          </Fade>
        );

      case 'scale':
        return (
          <Zoom in={isVisible} timeout={600}>
            <Box>{children}</Box>
          </Zoom>
        );

      default:
        return <Box>{children}</Box>;
    }
  };

  return getTransitionComponent();
};

/**
 * Progress indicator with smooth animation
 * 부드러운 애니메이션이 있는 진행률 표시기
 */
export const AnimatedProgress: React.FC<{
  value: number;
  max?: number;
  height?: number;
  color?: string;
  backgroundColor?: string;
  duration?: number;
  showPercentage?: boolean;
}> = ({
  value,
  max = 100,
  height = 8,
  color,
  backgroundColor,
  duration = 1000,
  showPercentage = false
}) => {
  const theme = useTheme();
  const [animatedValue, setAnimatedValue] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setAnimatedValue(value);
    }, 100);

    return () => clearTimeout(timer);
  }, [value]);

  const percentage = Math.min((animatedValue / max) * 100, 100);

  return (
    <Box sx={{ position: 'relative', width: '100%' }}>
      <Box
        sx={{
          width: '100%',
          height,
          backgroundColor: backgroundColor || alpha(theme.palette.primary.main, 0.2),
          borderRadius: height / 2,
          overflow: 'hidden',
          position: 'relative'
        }}
      >
        <Box
          sx={{
            width: `${percentage}%`,
            height: '100%',
            backgroundColor: color || theme.palette.primary.main,
            borderRadius: height / 2,
            transition: `width ${duration}ms cubic-bezier(0.4, 0, 0.2, 1)`,
            position: 'relative',
            '&::after': {
              content: '""',
              position: 'absolute',
              top: 0,
              left: 0,
              bottom: 0,
              right: 0,
              background: `linear-gradient(45deg, transparent 33%, ${alpha(theme.palette.common.white, 0.2)} 33%, ${alpha(theme.palette.common.white, 0.2)} 66%, transparent 66%)`,
              backgroundSize: '20px 20px',
              animation: 'progressShine 2s linear infinite'
            },
            '@keyframes progressShine': {
              '0%': { transform: 'translateX(-100%)' },
              '100%': { transform: 'translateX(100%)' }
            }
          }}
        />
      </Box>

      {showPercentage && (
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            fontSize: '0.75rem',
            fontWeight: 'bold',
            color: theme.palette.text.primary,
            textShadow: '0 0 4px rgba(0,0,0,0.5)'
          }}
        >
          {Math.round(percentage)}%
        </Box>
      )}
    </Box>
  );
};