'use client';

import React, { useState, useRef, useEffect } from 'react';
import {
  Tooltip,
  TooltipProps,
  Box,
  Typography,
  Paper,
  Fade,
  Zoom,
  Popper,
  useTheme,
  alpha,
  ClickAwayListener
} from '@mui/material';
import {
  Info,
  TrendingUp,
  TrendingDown,
  Timeline,
  AccountBalance,
  ShowChart,
  Speed,
  Security
} from '@mui/icons-material';

interface EnhancedTooltipProps extends Omit<TooltipProps, 'title'> {
  title: React.ReactNode;
  description?: string;
  value?: string | number;
  change?: number;
  changePercent?: number;
  icon?: React.ReactNode;
  variant?: 'simple' | 'detailed' | 'trading' | 'performance';
  showArrow?: boolean;
  interactive?: boolean;
  delay?: number;
  maxWidth?: number;
}

/**
 * Enhanced tooltip with rich content and animations
 * 풍부한 콘텐츠와 애니메이션이 있는 향상된 툴팁
 */
export const EnhancedTooltip: React.FC<EnhancedTooltipProps> = ({
  title,
  description,
  value,
  change,
  changePercent,
  icon,
  variant = 'simple',
  showArrow = true,
  interactive = false,
  delay = 500,
  maxWidth = 320,
  children,
  placement = 'top',
  ...props
}) => {
  const theme = useTheme();

  const formatValue = (val: string | number) => {
    if (typeof val === 'number') {
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2
      }).format(val);
    }
    return val;
  };

  const formatChange = (changeValue: number, percent?: number) => {
    const isPositive = changeValue >= 0;
    const color = isPositive ? theme.palette.success.main : theme.palette.error.main;

    return (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color }}>
        {isPositive ? <TrendingUp sx={{ fontSize: 16 }} /> : <TrendingDown sx={{ fontSize: 16 }} />}
        <Typography variant="body2" sx={{ fontWeight: 600, fontFamily: 'monospace' }}>
          {isPositive ? '+' : ''}{formatValue(changeValue)}
          {percent && ` (${isPositive ? '+' : ''}${percent.toFixed(2)}%)`}
        </Typography>
      </Box>
    );
  };

  const getTooltipContent = () => {
    switch (variant) {
      case 'detailed':
        return (
          <Box sx={{ p: 2, maxWidth }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              {icon}
              <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                {title}
              </Typography>
            </Box>
            {description && (
              <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 1 }}>
                {description}
              </Typography>
            )}
            {value && (
              <Typography variant="h6" sx={{ fontWeight: 700, fontFamily: 'monospace', mb: 1 }}>
                {formatValue(value)}
              </Typography>
            )}
            {change !== undefined && formatChange(change, changePercent)}
          </Box>
        );

      case 'trading':
        return (
          <Box sx={{ p: 2, maxWidth }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <ShowChart sx={{ color: theme.palette.primary.main }} />
              <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                {title}
              </Typography>
            </Box>

            {value && (
              <Box sx={{ mb: 1 }}>
                <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                  현재 가격
                </Typography>
                <Typography variant="h6" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>
                  {formatValue(value)}
                </Typography>
              </Box>
            )}

            {change !== undefined && (
              <Box>
                <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                  변동
                </Typography>
                {formatChange(change, changePercent)}
              </Box>
            )}

            {description && (
              <Typography variant="body2" sx={{
                color: theme.palette.text.secondary,
                mt: 1,
                fontSize: '0.75rem'
              }}>
                {description}
              </Typography>
            )}
          </Box>
        );

      case 'performance':
        return (
          <Box sx={{ p: 2, maxWidth }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <Speed sx={{ color: theme.palette.info.main }} />
              <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                {title}
              </Typography>
            </Box>

            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
              {value && (
                <Box>
                  <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                    값
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 600, fontFamily: 'monospace' }}>
                    {formatValue(value)}
                  </Typography>
                </Box>
              )}

              {change !== undefined && (
                <Box>
                  <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                    변화
                  </Typography>
                  {formatChange(change, changePercent)}
                </Box>
              )}
            </Box>

            {description && (
              <Typography variant="body2" sx={{
                color: theme.palette.text.secondary,
                mt: 2,
                fontSize: '0.75rem',
                fontStyle: 'italic'
              }}>
                💡 {description}
              </Typography>
            )}
          </Box>
        );

      default:
        return (
          <Box sx={{ p: 1.5 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {icon}
              <Typography variant="body2">{title}</Typography>
            </Box>
            {description && (
              <Typography variant="caption" sx={{ color: theme.palette.text.secondary, mt: 0.5, display: 'block' }}>
                {description}
              </Typography>
            )}
          </Box>
        );
    }
  };

  return (
    <Tooltip
      {...props}
      title={getTooltipContent()}
      placement={placement}
      arrow={showArrow}
      enterDelay={delay}
      componentsProps={{
        tooltip: {
          sx: {
            backgroundColor: alpha(theme.palette.background.paper, 0.95),
            color: theme.palette.text.primary,
            border: `1px solid ${theme.palette.divider}`,
            borderRadius: 2,
            backdropFilter: 'blur(10px)',
            maxWidth: 'none',
            p: 0,
            '& .MuiTooltip-arrow': {
              color: alpha(theme.palette.background.paper, 0.95),
              '&::before': {
                border: `1px solid ${theme.palette.divider}`,
              }
            }
          }
        }
      }}
      TransitionComponent={variant === 'detailed' ? Zoom : Fade}
    >
      {children}
    </Tooltip>
  );
};

/**
 * Trading-specific tooltip for stock prices
 * 주식 가격용 트레이딩 전용 툴팁
 */
export const TradingTooltip: React.FC<{
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  volume?: number;
  children: React.ReactElement;
}> = ({ symbol, price, change, changePercent, volume, children }) => {
  return (
    <EnhancedTooltip
      variant="trading"
      title={symbol}
      value={price}
      change={change}
      changePercent={changePercent}
      description={volume ? `거래량: ${volume.toLocaleString()}` : undefined}
      icon={<ShowChart />}
      maxWidth={280}
    >
      {children}
    </EnhancedTooltip>
  );
};

/**
 * Portfolio metric tooltip
 * 포트폴리오 지표 툴팁
 */
export const PortfolioTooltip: React.FC<{
  metric: string;
  value: number;
  description: string;
  change?: number;
  changePercent?: number;
  children: React.ReactElement;
}> = ({ metric, value, description, change, changePercent, children }) => {
  return (
    <EnhancedTooltip
      variant="performance"
      title={metric}
      value={value}
      change={change}
      changePercent={changePercent}
      description={description}
      icon={<AccountBalance />}
      maxWidth={300}
    >
      {children}
    </EnhancedTooltip>
  );
};

/**
 * Risk indicator tooltip
 * 위험도 지표 툴팁
 */
export const RiskTooltip: React.FC<{
  riskLevel: 'low' | 'medium' | 'high';
  score: number;
  description: string;
  children: React.ReactElement;
}> = ({ riskLevel, score, description, children }) => {
  const theme = useTheme();

  const getRiskConfig = () => {
    switch (riskLevel) {
      case 'low':
        return {
          color: theme.palette.success.main,
          label: '낮은 위험도',
          icon: <Security sx={{ color: theme.palette.success.main }} />
        };
      case 'medium':
        return {
          color: theme.palette.warning.main,
          label: '보통 위험도',
          icon: <Info sx={{ color: theme.palette.warning.main }} />
        };
      case 'high':
        return {
          color: theme.palette.error.main,
          label: '높은 위험도',
          icon: <TrendingDown sx={{ color: theme.palette.error.main }} />
        };
    }
  };

  const riskConfig = getRiskConfig();

  return (
    <EnhancedTooltip
      variant="detailed"
      title={riskConfig.label}
      value={`${score}/10`}
      description={description}
      icon={riskConfig.icon}
      maxWidth={250}
    >
      {children}
    </EnhancedTooltip>
  );
};

/**
 * Interactive popover tooltip for complex content
 * 복잡한 콘텐츠용 상호작용 팝오버 툴팁
 */
export const InteractiveTooltip: React.FC<{
  content: React.ReactNode;
  children: React.ReactElement;
  maxWidth?: number;
}> = ({ content, children, maxWidth = 400 }) => {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const theme = useTheme();

  const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);

  return (
    <>
      {React.cloneElement(children, {
        onMouseEnter: handleOpen,
        onMouseLeave: handleClose,
        onClick: handleOpen
      })}

      <Popper
        open={open}
        anchorEl={anchorEl}
        placement="top"
        transition
        modifiers={[
          {
            name: 'flip',
            enabled: true,
            options: {
              altBoundary: true,
              rootBoundary: 'document',
              padding: 8,
            },
          },
          {
            name: 'preventOverflow',
            enabled: true,
            options: {
              altAxis: true,
              altBoundary: true,
              tether: true,
              rootBoundary: 'document',
              padding: 8,
            },
          },
        ]}
      >
        {({ TransitionProps }) => (
          <Fade {...TransitionProps} timeout={200}>
            <Paper
              sx={{
                backgroundColor: alpha(theme.palette.background.paper, 0.95),
                border: `1px solid ${theme.palette.divider}`,
                borderRadius: 2,
                backdropFilter: 'blur(10px)',
                maxWidth,
                zIndex: theme.zIndex.tooltip,
                boxShadow: theme.shadows[8]
              }}
            >
              <ClickAwayListener onClickAway={handleClose}>
                <Box>
                  {content}
                </Box>
              </ClickAwayListener>
            </Paper>
          </Fade>
        )}
      </Popper>
    </>
  );
};