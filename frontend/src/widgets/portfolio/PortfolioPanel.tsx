'use client';

import React from 'react';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Stack,
} from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';
import { PortfolioSkeleton } from '@/shared/ui/skeleton/SkeletonLoader';
import { AnimatedPrice } from '@/shared/ui/animations/PriceAnimations';
import { EnhancedTooltip } from '@/shared/ui/feedback/EnhancedTooltip';
import { useGetSessionDetail } from '@/shared/api/challenge-client';
import type { PortfolioItem } from '@/shared/api/generated/model/portfolioItem';

interface PortfolioPanelProps {
  sessionId: number;
}

export function PortfolioPanel({ sessionId }: PortfolioPanelProps) {
  const { data: sessionData, isLoading: loading, error } = useGetSessionDetail(
    sessionId,
    {
      query: {
        refetchInterval: 5000,
        enabled: !isNaN(sessionId) && sessionId > 0,
      },
    }
  );

  const formatCurrency = (amount: number) => {
    return `₩${amount.toLocaleString()}`;
  };

  const formatPercentage = (percentage: number) => {
    const sign = percentage >= 0 ? '+' : '';
    return `${sign}${percentage.toFixed(2)}%`;
  };

  if (loading) {
    return <PortfolioSkeleton />;
  }

  if (error || !sessionData) {
    return (
      <Box
        sx={{
          backgroundColor: '#1A1F2E',
          border: '1px solid #2A3441',
          borderRadius: 2,
          p: 3,
        }}
      >
        <Typography
          variant="h6"
          sx={{
            fontWeight: 600,
            color: '#FFFFFF',
            mb: 2,
            fontSize: '0.875rem',
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
          }}
        >
          포트폴리오
        </Typography>
        <Typography
          variant="body2"
          sx={{
            color: '#78828A',
            fontSize: '0.875rem',
          }}
        >
          {error ? '포트폴리오 정보를 불러오는데 실패했습니다' : '포트폴리오 정보를 불러올 수 없습니다'}
        </Typography>
      </Box>
    );
  }

  const currentBalance = sessionData.currentBalance || 0;
  const initialBalance = sessionData.initialBalance || 0;
  const totalValue = currentBalance;
  const profitLoss = currentBalance - initialBalance;
  const profitLossPercent = sessionData.returnRate || 0;
  const positions = sessionData.portfolio || [];

  return (
    <Box
      sx={{
        backgroundColor: '#1A1F2E',
        border: `1px solid ${profitLoss >= 0 ? '#4CAF50' : '#F44336'}`,
        borderRadius: 2,
        p: 3,
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: '3px',
          background: profitLoss >= 0 ? '#4CAF50' : '#F44336',
        }
      }}
    >
      <Typography
        variant="h6"
        gutterBottom
        sx={{
          fontWeight: 600,
          color: '#FFFFFF',
          mb: 3,
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          fontSize: '0.875rem',
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
        }}
      >
        <Box component="span" sx={{ fontSize: '1rem' }}>💰</Box>
        포트폴리오
      </Typography>

      {/* Portfolio Summary */}
      <Box
        sx={{
          mb: 3,
          p: 3,
          backgroundColor: '#0A0E18',
          borderRadius: 2,
          border: '1px solid #2A3441',
        }}
      >
        <Stack spacing={2}>
          {/* Cash Balance */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              variant="body2"
              sx={{
                fontWeight: 500,
                color: '#78828A',
                fontSize: '0.75rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              잔고
            </Typography>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 600,
                color: '#2196F3',
                fontFamily: '"Roboto Mono", monospace',
                fontSize: '1rem',
              }}
            >
              {formatCurrency(currentBalance)}
            </Typography>
          </Box>

          {/* Total Value */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              variant="body2"
              sx={{
                fontWeight: 500,
                color: '#78828A',
                fontSize: '0.75rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              총 자산
            </Typography>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 600,
                color: '#FFFFFF',
                fontFamily: '"Roboto Mono", monospace',
                fontSize: '1rem',
              }}
            >
              {formatCurrency(totalValue)}
            </Typography>
          </Box>

          {/* Total P&L */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              variant="body2"
              sx={{
                fontWeight: 500,
                color: '#78828A',
                fontSize: '0.75rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              손익
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Box
                sx={{
                  width: 24,
                  height: 24,
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor: profitLoss >= 0 ? '#4CAF50' : '#F44336',
                }}
              >
                {profitLoss >= 0 ? (
                  <TrendingUp sx={{ color: 'white', fontSize: '16px' }} />
                ) : (
                  <TrendingDown sx={{ color: 'white', fontSize: '16px' }} />
                )}
              </Box>
              <EnhancedTooltip
                title="실시간 손익"
                description="초기 투자 대비 현재 수익/손실"
                value={formatCurrency(profitLoss)}
                change={profitLoss}
                changePercent={profitLossPercent}
                variant="trading"
              >
                <Box>
                  <AnimatedPrice
                    value={profitLoss}
                    currency="KRW"
                    showChange={false}
                    showPercent={false}
                    animationType="flash"
                    variant="h6"
                    fontFamily='"Roboto Mono", monospace'
                  />
                </Box>
              </EnhancedTooltip>
            </Box>
          </Box>

          {/* Return Percentage */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              variant="body2"
              sx={{
                fontWeight: 500,
                color: '#78828A',
                fontSize: '0.75rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              수익률
            </Typography>
            <Chip
              label={formatPercentage(profitLossPercent)}
              size="small"
              sx={{
                fontWeight: 600,
                fontSize: '0.75rem',
                height: 24,
                backgroundColor: profitLossPercent >= 0
                  ? 'rgba(76, 175, 80, 0.15)'
                  : 'rgba(244, 67, 54, 0.15)',
                color: profitLossPercent >= 0 ? '#4CAF50' : '#F44336',
                border: `1px solid ${profitLossPercent >= 0 ? '#4CAF50' : '#F44336'}`,
                fontFamily: '"Roboto Mono", monospace',
              }}
            />
          </Box>
        </Stack>
      </Box>

      {/* Positions Table */}
      <Typography
        variant="subtitle1"
        sx={{
          fontWeight: 600,
          color: '#FFFFFF',
          mb: 2,
          fontSize: '0.875rem',
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
        }}
      >
        보유 종목
      </Typography>

      {positions.length === 0 ? (
        <Box
          sx={{
            textAlign: 'center',
            py: 4,
            backgroundColor: '#0A0E18',
            borderRadius: 2,
            border: '1px solid #2A3441',
          }}
        >
          <Typography
            variant="body2"
            sx={{
              color: '#78828A',
              fontSize: '0.875rem',
            }}
          >
            보유 중인 포지션이 없습니다
          </Typography>
        </Box>
      ) : (
        <TableContainer
          sx={{
            backgroundColor: '#0A0E18',
            border: '1px solid #2A3441',
            borderRadius: 2,
          }}
        >
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell
                  sx={{
                    color: '#78828A',
                    fontWeight: 600,
                    fontSize: '0.625rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    borderColor: '#2A3441',
                  }}
                >
                  종목
                </TableCell>
                <TableCell
                  align="right"
                  sx={{
                    color: '#78828A',
                    fontWeight: 600,
                    fontSize: '0.625rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    borderColor: '#2A3441',
                  }}
                >
                  수량
                </TableCell>
                <TableCell
                  align="right"
                  sx={{
                    color: '#78828A',
                    fontWeight: 600,
                    fontSize: '0.625rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    borderColor: '#2A3441',
                  }}
                >
                  평균단가
                </TableCell>
                <TableCell
                  align="right"
                  sx={{
                    color: '#78828A',
                    fontWeight: 600,
                    fontSize: '0.625rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    borderColor: '#2A3441',
                  }}
                >
                  손익
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {positions.map((position: PortfolioItem) => (
                <TableRow
                  key={position.instrumentKey}
                  sx={{
                    '&:hover': {
                      backgroundColor: 'rgba(33, 150, 243, 0.05)',
                    },
                  }}
                >
                  <TableCell sx={{ borderColor: '#2A3441' }}>
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: 500,
                        color: '#FFFFFF',
                        fontSize: '0.75rem',
                      }}
                    >
                      {position.instrumentKey}
                    </Typography>
                  </TableCell>
                  <TableCell align="right" sx={{ borderColor: '#2A3441' }}>
                    <Typography
                      variant="body2"
                      sx={{
                        color: '#B0BEC5',
                        fontSize: '0.75rem',
                        fontFamily: '"Roboto Mono", monospace',
                      }}
                    >
                      {(position.quantity || 0).toLocaleString()}
                    </Typography>
                  </TableCell>
                  <TableCell align="right" sx={{ borderColor: '#2A3441' }}>
                    <Typography
                      variant="body2"
                      sx={{
                        color: '#B0BEC5',
                        fontSize: '0.75rem',
                        fontFamily: '"Roboto Mono", monospace',
                      }}
                    >
                      {formatCurrency(position.averagePrice || 0)}
                    </Typography>
                    <Typography
                      variant="caption"
                      display="block"
                      sx={{
                        color: '#78828A',
                        fontSize: '0.625rem',
                        fontFamily: '"Roboto Mono", monospace',
                      }}
                    >
                      현재: {formatCurrency(position.currentValue || 0)}
                    </Typography>
                  </TableCell>
                  <TableCell align="right" sx={{ borderColor: '#2A3441' }}>
                    <EnhancedTooltip
                      title={`${position.instrumentKey} 손익`}
                      description="미실현 손익"
                      value={formatCurrency(position.unrealizedPnl || 0)}
                      change={position.unrealizedPnl || 0}
                      variant="simple"
                    >
                      <Box>
                        <AnimatedPrice
                          value={position.unrealizedPnl || 0}
                          currency="KRW"
                          showChange={false}
                          showPercent={false}
                          animationType="pulse"
                          variant="body2"
                          fontFamily='"Roboto Mono", monospace'
                        />
                      </Box>
                    </EnhancedTooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}