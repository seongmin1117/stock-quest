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
  LinearProgress,
  Stack,
} from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';

interface Position {
  instrumentKey: string;
  hiddenName: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  currentValue: number;
  unrealizedPnL: number;
}

interface Portfolio {
  sessionId: number;
  currentBalance: number;
  positions: Position[];
  totalValue: number;
  totalPnL: number;
  returnPercentage: number;
}

interface PortfolioPanelProps {
  sessionId: number;
}

/**
 * Professional Trading Portfolio Panel Component
 * Displays current holdings and performance metrics
 */
export function PortfolioPanel({ sessionId }: PortfolioPanelProps) {
  const [portfolio, setPortfolio] = React.useState<Portfolio | null>(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    loadPortfolio();
    
    // Real-time updates (every 5 seconds)
    const interval = setInterval(loadPortfolio, 5000);
    return () => clearInterval(interval);
  }, [sessionId]);

  const loadPortfolio = async () => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/sessions/${sessionId}/portfolio`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        setPortfolio(data);
      }
    } catch (err) {
      console.error('Portfolio load error:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return `₩${amount.toLocaleString()}`;
  };

  const formatPercentage = (percentage: number) => {
    const sign = percentage >= 0 ? '+' : '';
    return `${sign}${percentage.toFixed(2)}%`;
  };

  if (loading) {
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
          PORTFOLIO
        </Typography>
        <LinearProgress 
          sx={{
            backgroundColor: '#2A3441',
            '& .MuiLinearProgress-bar': {
              backgroundColor: '#2196F3',
            },
          }}
        />
      </Box>
    );
  }

  if (!portfolio) {
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
          PORTFOLIO
        </Typography>
        <Typography 
          variant="body2" 
          sx={{ 
            color: '#78828A',
            fontSize: '0.875rem',
          }}
        >
          포트폴리오 정보를 불러올 수 없습니다
        </Typography>
      </Box>
    );
  }

  return (
    <Box 
      sx={{ 
        backgroundColor: '#1A1F2E',
        border: `1px solid ${portfolio.totalPnL >= 0 ? '#4CAF50' : '#F44336'}`,
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
          background: portfolio.totalPnL >= 0 ? '#4CAF50' : '#F44336',
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
        PORTFOLIO
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
              CASH BALANCE
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
              {formatCurrency(portfolio.currentBalance)}
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
              TOTAL VALUE
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
              {formatCurrency(portfolio.totalValue)}
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
              TOTAL P&L
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
                  backgroundColor: portfolio.totalPnL >= 0 ? '#4CAF50' : '#F44336',
                }}
              >
                {portfolio.totalPnL >= 0 ? (
                  <TrendingUp sx={{ color: 'white', fontSize: '16px' }} />
                ) : (
                  <TrendingDown sx={{ color: 'white', fontSize: '16px' }} />
                )}
              </Box>
              <Typography 
                variant="h6" 
                sx={{
                  fontWeight: 600,
                  color: portfolio.totalPnL >= 0 ? '#4CAF50' : '#F44336',
                  fontFamily: '"Roboto Mono", monospace',
                  fontSize: '1rem',
                }}
              >
                {formatCurrency(portfolio.totalPnL)}
              </Typography>
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
              RETURN
            </Typography>
            <Chip
              label={formatPercentage(portfolio.returnPercentage)}
              size="small"
              sx={{
                fontWeight: 600,
                fontSize: '0.75rem',
                height: 24,
                backgroundColor: portfolio.returnPercentage >= 0
                  ? 'rgba(76, 175, 80, 0.15)'
                  : 'rgba(244, 67, 54, 0.15)',
                color: portfolio.returnPercentage >= 0 ? '#4CAF50' : '#F44336',
                border: `1px solid ${portfolio.returnPercentage >= 0 ? '#4CAF50' : '#F44336'}`,
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
        POSITIONS
      </Typography>
      
      {portfolio.positions.length === 0 ? (
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
                  INSTRUMENT
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
                  QUANTITY
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
                  AVG PRICE
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
                  P&L
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {portfolio.positions.map((position) => (
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
                      {position.hiddenName}
                    </Typography>
                    <Typography 
                      variant="caption" 
                      sx={{ 
                        color: '#78828A',
                        fontSize: '0.625rem',
                        fontFamily: '"Roboto Mono", monospace',
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
                      {position.quantity.toLocaleString()}
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
                      {formatCurrency(position.averagePrice)}
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
                      Current: {formatCurrency(position.currentPrice)}
                    </Typography>
                  </TableCell>
                  <TableCell align="right" sx={{ borderColor: '#2A3441' }}>
                    <Typography 
                      variant="body2"
                      sx={{
                        fontWeight: 600,
                        color: position.unrealizedPnL >= 0 ? '#4CAF50' : '#F44336',
                        fontSize: '0.75rem',
                        fontFamily: '"Roboto Mono", monospace',
                      }}
                    >
                      {formatCurrency(position.unrealizedPnL)}
                    </Typography>
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