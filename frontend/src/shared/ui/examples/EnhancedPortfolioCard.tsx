'use client';

import React, { useState } from 'react';
import {
  Box,
  CardContent,
  Typography,
  Chip,
  IconButton,
  Divider
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  MoreVert,
  Refresh
} from '@mui/icons-material';

// Import our new enhanced components
import {
  AnimatedCard,
  AnimatedPrice,
  EnhancedTooltip,
  TradingTooltip,
  BuyButton,
  SellButton,
  RefreshButton
} from '../';

interface PortfolioCardProps {
  symbol: string;
  name: string;
  currentPrice: number;
  previousPrice: number;
  shares: number;
  totalValue: number;
  dailyChange: number;
  dailyChangePercent: number;
  totalReturn: number;
  totalReturnPercent: number;
  isLoading?: boolean;
  onBuy?: () => Promise<void>;
  onSell?: () => Promise<void>;
  onRefresh?: () => Promise<void>;
}

/**
 * Enhanced portfolio card demonstrating the new UI improvements
 * 새로운 UI 개선사항을 보여주는 향상된 포트폴리오 카드
 */
export const EnhancedPortfolioCard: React.FC<PortfolioCardProps> = ({
  symbol,
  name,
  currentPrice,
  previousPrice,
  shares,
  totalValue,
  dailyChange,
  dailyChangePercent,
  totalReturn,
  totalReturnPercent,
  isLoading = false,
  onBuy,
  onSell,
  onRefresh
}) => {
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = async () => {
    if (!onRefresh) return;

    setIsRefreshing(true);
    try {
      await onRefresh();
    } finally {
      setIsRefreshing(false);
    }
  };

  const isPositive = dailyChange >= 0;
  const isTotalPositive = totalReturn >= 0;

  return (
    <AnimatedCard
      hoverEffect="lift"
      clickEffect="ripple"
      sx={{
        height: '100%',
        background: 'linear-gradient(135deg, #1A1F2E 0%, #2A3441 100%)',
        border: '1px solid #2A3441',
        borderRadius: 3
      }}
    >
      <CardContent sx={{ p: 3 }}>
        {/* Header with symbol and actions */}
        <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 2 }}>
          <Box>
            <TradingTooltip
              symbol={symbol}
              price={currentPrice}
              change={dailyChange}
              changePercent={dailyChangePercent}
            >
              <Typography variant="h5" sx={{ fontWeight: 700, cursor: 'pointer' }}>
                {symbol}
              </Typography>
            </TradingTooltip>

            <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
              {name}
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <EnhancedTooltip title="새로고침" variant="simple">
              <RefreshButton
                size="small"
                onClick={handleRefresh}
                isLoading={isRefreshing}
                sx={{ minWidth: 'auto', p: 1 }}
              />
            </EnhancedTooltip>

            <EnhancedTooltip title="더 많은 옵션" variant="simple">
              <IconButton size="small" sx={{ color: 'text.secondary' }}>
                <MoreVert />
              </IconButton>
            </EnhancedTooltip>
          </Box>
        </Box>

        {/* Current price with animation */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
            현재 가격
          </Typography>
          <AnimatedPrice
            value={currentPrice}
            previousValue={previousPrice}
            variant="h4"
            animationType="glow"
            showChange={true}
            showPercent={true}
            duration={1000}
          />
        </Box>

        {/* Holdings info */}
        <Box sx={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: 2,
          mb: 2,
          p: 2,
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          borderRadius: 2
        }}>
          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
              보유 주식
            </Typography>
            <Typography variant="h6" sx={{ fontFamily: 'monospace', fontWeight: 600 }}>
              {shares.toLocaleString()}주
            </Typography>
          </Box>

          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
              평가 금액
            </Typography>
            <AnimatedPrice
              value={totalValue}
              variant="h6"
              animationType="flash"
              showChange={false}
            />
          </Box>
        </Box>

        <Divider sx={{ my: 2, borderColor: 'rgba(255, 255, 255, 0.1)' }} />

        {/* Performance metrics */}
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              일일 변동
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              {isPositive ? (
                <TrendingUp sx={{ fontSize: 16, color: 'success.main' }} />
              ) : (
                <TrendingDown sx={{ fontSize: 16, color: 'error.main' }} />
              )}
              <Typography
                variant="body2"
                sx={{
                  fontFamily: 'monospace',
                  fontWeight: 600,
                  color: isPositive ? 'success.main' : 'error.main'
                }}
              >
                {isPositive ? '+' : ''}${Math.abs(dailyChange).toFixed(2)}
                ({isPositive ? '+' : ''}{dailyChangePercent.toFixed(2)}%)
              </Typography>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              총 수익/손실
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Chip
                label={`${isTotalPositive ? '+' : ''}${totalReturnPercent.toFixed(2)}%`}
                size="small"
                color={isTotalPositive ? 'success' : 'error'}
                sx={{ fontFamily: 'monospace', fontWeight: 600 }}
              />
              <Typography
                variant="body2"
                sx={{
                  fontFamily: 'monospace',
                  fontWeight: 600,
                  color: isTotalPositive ? 'success.main' : 'error.main'
                }}
              >
                {isTotalPositive ? '+' : ''}${Math.abs(totalReturn).toFixed(2)}
              </Typography>
            </Box>
          </Box>
        </Box>

        {/* Action buttons */}
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
          <BuyButton
            symbol={symbol}
            onClick={onBuy}
            disabled={isLoading}
            fullWidth
            variant="contained"
            size="medium"
          >
            매수
          </BuyButton>

          <SellButton
            symbol={symbol}
            onClick={onSell}
            disabled={isLoading || shares === 0}
            fullWidth
            variant="contained"
            size="medium"
          >
            매도
          </SellButton>
        </Box>

        {/* Additional info tooltip */}
        <Box sx={{ mt: 2, textAlign: 'center' }}>
          <EnhancedTooltip
            variant="detailed"
            title="포트폴리오 정보"
            description={`${symbol} 주식 ${shares}주를 보유하고 있으며, 현재 시가총액은 $${totalValue.toLocaleString()}입니다.`}
            icon={<TrendingUp />}
          >
            <Typography
              variant="caption"
              sx={{
                color: 'text.secondary',
                cursor: 'help',
                textDecoration: 'underline',
                textDecorationStyle: 'dotted'
              }}
            >
              상세 정보 보기
            </Typography>
          </EnhancedTooltip>
        </Box>
      </CardContent>
    </AnimatedCard>
  );
};