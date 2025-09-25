'use client';

import React, { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Snackbar,
  Alert,
  useTheme,
  alpha,
  Fab,
  useMediaQuery,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Edit,
  Delete,
  Help,
  Speed,
} from '@mui/icons-material';
import {
  SwipeableCard,
  QuickOrderGesture,
  GestureHints,
  portfolioGestureHints,
  tradingGestureHints,
} from '@/shared/ui/gestures/GestureControls';
import { PortfolioSkeleton } from '@/shared/ui/skeleton/SkeletonLoader';
import { AnimatedPrice } from '@/shared/ui/animations/PriceAnimations';
import { EnhancedTooltip } from '@/shared/ui/feedback/EnhancedTooltip';
import { useGetSessionDetail } from '@/shared/api/challenge-client';
import type { PortfolioItem } from '@/shared/api/generated/model';

interface EnhancedMobilePortfolioProps {
  sessionId: number;
  onSellPosition?: (instrumentKey: string, quantity: number) => Promise<void>;
  onEditPosition?: (instrumentKey: string) => void;
}

interface SellDialogState {
  open: boolean;
  position: PortfolioItem | null;
  quantity: number;
  loading: boolean;
}

export function EnhancedMobilePortfolio({
  sessionId,
  onSellPosition,
  onEditPosition
}: EnhancedMobilePortfolioProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [showGestureHints, setShowGestureHints] = useState(false);
  const [showQuickOrder, setShowQuickOrder] = useState(false);
  const [sellDialog, setSellDialog] = useState<SellDialogState>({
    open: false,
    position: null,
    quantity: 0,
    loading: false,
  });
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'info' });

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

  const handleSellPosition = useCallback(async (position: PortfolioItem) => {
    setSellDialog({
      open: true,
      position,
      quantity: position.quantity || 0,
      loading: false,
    });
  }, []);

  const handleEditPosition = useCallback((position: PortfolioItem) => {
    if (onEditPosition) {
      onEditPosition(position.instrumentKey || '');
    }
    setSnackbar({
      open: true,
      message: `${position.instrumentKey} 포지션 편집 모드`,
      severity: 'info',
    });
  }, [onEditPosition]);

  const confirmSell = useCallback(async () => {
    if (!sellDialog.position || !onSellPosition) return;

    setSellDialog(prev => ({ ...prev, loading: true }));

    try {
      await onSellPosition(
        sellDialog.position.instrumentKey || '',
        sellDialog.quantity
      );

      setSnackbar({
        open: true,
        message: `${sellDialog.position.instrumentKey} ${sellDialog.quantity}주 매도 주문 접수`,
        severity: 'success',
      });

      setSellDialog({ open: false, position: null, quantity: 0, loading: false });
    } catch (error) {
      setSnackbar({
        open: true,
        message: '매도 주문 실패',
        severity: 'error',
      });
      setSellDialog(prev => ({ ...prev, loading: false }));
    }
  }, [sellDialog, onSellPosition]);

  const handleQuickBuy = useCallback(async (quantity: number) => {
    // Mock implementation - integrate with actual order API
    setSnackbar({
      open: true,
      message: `${quantity}주 매수 주문 접수`,
      severity: 'success',
    });
  }, []);

  const handleQuickSell = useCallback(async (quantity: number) => {
    // Mock implementation - integrate with actual order API
    setSnackbar({
      open: true,
      message: `${quantity}주 매도 주문 접수`,
      severity: 'success',
    });
  }, []);

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
        <Typography variant="h6" sx={{ color: '#FFFFFF', mb: 2 }}>
          포트폴리오
        </Typography>
        <Typography variant="body2" sx={{ color: '#78828A' }}>
          {error ? '포트폴리오 정보를 불러오는데 실패했습니다' : '포트폴리오 정보를 불러올 수 없습니다'}
        </Typography>
      </Box>
    );
  }

  const currentBalance = sessionData.currentBalance || 0;
  const initialBalance = sessionData.seedBalance || 0;
  const profitLoss = sessionData.profitLoss || (currentBalance - initialBalance);
  const profitLossPercent = sessionData.profitLossPercent || 0;
  const positions = sessionData.positions || [];

  return (
    <Box sx={{ position: 'relative' }}>
      {/* Enhanced Portfolio Summary */}
      <Box
        sx={{
          backgroundColor: '#1A1F2E',
          border: `1px solid ${profitLoss >= 0 ? '#4CAF50' : '#F44336'}`,
          borderRadius: 2,
          p: 3,
          mb: 2,
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
          {isMobile && (
            <Typography variant="caption" sx={{ ml: 'auto', color: '#78828A' }}>
              스와이프로 액션
            </Typography>
          )}
        </Typography>

        {/* Portfolio Metrics with Enhanced Animation */}
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 2 }}>
          <Box>
            <Typography variant="caption" sx={{ color: '#78828A' }}>
              총 자산
            </Typography>
            <AnimatedPrice
              value={currentBalance}
              currency="KRW"
              showChange={false}
              showPercent={false}
              animationType="pulse"
              variant="h6"
              fontFamily='"Roboto Mono", monospace'
            />
          </Box>
          <Box>
            <Typography variant="caption" sx={{ color: '#78828A' }}>
              손익
            </Typography>
            <EnhancedTooltip
              title="실시간 손익"
              description="초기 투자 대비 현재 수익/손실"
              value={formatCurrency(profitLoss)}
              change={profitLoss}
              changePercent={profitLossPercent}
              variant="trading"
            >
              <Box>
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
                  <Typography
                    variant="caption"
                    sx={{
                      color: profitLossPercent >= 0 ? '#4CAF50' : '#F44336',
                      fontFamily: '"Roboto Mono", monospace',
                      fontWeight: 600,
                      ml: 1,
                    }}
                  >
                    ({formatPercentage(profitLossPercent)})
                  </Typography>
                </Box>
              </Box>
            </EnhancedTooltip>
          </Box>
        </Box>
      </Box>

      {/* Enhanced Positions with Swipe Actions */}
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
        보유 종목 {positions.length > 0 && `(${positions.length})`}
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
          <Typography variant="body2" sx={{ color: '#78828A', mb: 2 }}>
            보유 중인 포지션이 없습니다
          </Typography>
          {isMobile && (
            <Button
              variant="outlined"
              startIcon={<Speed />}
              onClick={() => setShowQuickOrder(true)}
              sx={{ borderColor: '#2196F3', color: '#2196F3' }}
            >
              퀵 주문
            </Button>
          )}
        </Box>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {positions.map((position) => (
            <SwipeableCard
              key={position.instrumentKey}
              onSwipeLeft={() => handleSellPosition(position)}
              onSwipeRight={() => handleEditPosition(position)}
              leftAction={{
                icon: <TrendingDown />,
                label: '매도',
                color: theme.palette.error.main,
              }}
              rightAction={{
                icon: <Edit />,
                label: '편집',
                color: theme.palette.info.main,
              }}
              hapticFeedback={true}
            >
              <Box
                sx={{
                  backgroundColor: '#0A0E18',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  p: 2,
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                  <Box>
                    <Typography
                      variant="subtitle1"
                      sx={{ color: '#FFFFFF', fontWeight: 600, fontSize: '0.875rem' }}
                    >
                      {position.instrumentKey}
                    </Typography>
                    <Typography
                      variant="caption"
                      sx={{ color: '#78828A', fontFamily: '"Roboto Mono", monospace' }}
                    >
                      {(position.quantity || 0).toLocaleString()}주 @ {formatCurrency(position.averagePrice || 0)}
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'right' }}>
                    <Typography
                      variant="caption"
                      sx={{ color: '#78828A', display: 'block' }}
                    >
                      현재가
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{
                        color: '#FFFFFF',
                        fontFamily: '"Roboto Mono", monospace',
                        fontWeight: 600,
                      }}
                    >
                      {formatCurrency(position.currentValue || 0)}
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box>
                    <Typography variant="caption" sx={{ color: '#78828A' }}>
                      평가금액
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{
                        color: '#2196F3',
                        fontFamily: '"Roboto Mono", monospace',
                        fontWeight: 600,
                      }}
                    >
                      {formatCurrency((position.quantity || 0) * (position.currentValue || 0))}
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'right' }}>
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
                  </Box>
                </Box>
              </Box>
            </SwipeableCard>
          ))}
        </Box>
      )}

      {/* Mobile FABs */}
      {isMobile && (
        <>
          <Fab
            size="medium"
            color="primary"
            onClick={() => setShowQuickOrder(true)}
            sx={{
              position: 'fixed',
              bottom: 80,
              right: 16,
              zIndex: 1000,
            }}
          >
            <Speed />
          </Fab>

          <Fab
            size="small"
            onClick={() => setShowGestureHints(true)}
            sx={{
              position: 'fixed',
              bottom: 16,
              right: 16,
              backgroundColor: alpha(theme.palette.info.main, 0.8),
              color: theme.palette.info.contrastText,
              zIndex: 1000,
            }}
          >
            <Help />
          </Fab>
        </>
      )}

      {/* Sell Position Dialog */}
      <Dialog
        open={sellDialog.open}
        onClose={() => setSellDialog({ open: false, position: null, quantity: 0, loading: false })}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>
          {sellDialog.position?.instrumentKey} 매도
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="textSecondary">
              보유 수량: {sellDialog.position?.quantity?.toLocaleString()}주
            </Typography>
            <Typography variant="body2" color="textSecondary">
              현재가: {formatCurrency(sellDialog.position?.currentValue || 0)}
            </Typography>
          </Box>
          <TextField
            label="매도 수량"
            type="number"
            fullWidth
            value={sellDialog.quantity}
            onChange={(e) => setSellDialog(prev => ({
              ...prev,
              quantity: Math.min(Number(e.target.value), sellDialog.position?.quantity || 0)
            }))}
            inputProps={{
              min: 1,
              max: sellDialog.position?.quantity || 0,
            }}
            sx={{ mb: 2 }}
          />
          <Typography variant="body2" color="textSecondary">
            예상 수령액: {formatCurrency(sellDialog.quantity * (sellDialog.position?.currentValue || 0))}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setSellDialog({ open: false, position: null, quantity: 0, loading: false })}
            disabled={sellDialog.loading}
          >
            취소
          </Button>
          <Button
            onClick={confirmSell}
            variant="contained"
            color="error"
            disabled={sellDialog.loading || sellDialog.quantity <= 0}
          >
            {sellDialog.loading ? '처리 중...' : '매도'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Quick Order Dialog */}
      <Dialog
        open={showQuickOrder}
        onClose={() => setShowQuickOrder(false)}
        fullWidth
        maxWidth="sm"
        fullScreen={isMobile}
      >
        <DialogTitle>
          퀵 주문
        </DialogTitle>
        <DialogContent>
          <QuickOrderGesture
            symbol="SAMPLE"
            currentPrice={50000}
            onBuyOrder={handleQuickBuy}
            onSellOrder={handleQuickSell}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowQuickOrder(false)}>
            닫기
          </Button>
        </DialogActions>
      </Dialog>

      {/* Gesture Hints */}
      <GestureHints
        hints={[...portfolioGestureHints, ...tradingGestureHints]}
        show={showGestureHints}
        onDismiss={() => setShowGestureHints(false)}
      />

      {/* Snackbar for Feedback */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity={snackbar.severity}
          onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
          variant="filled"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}