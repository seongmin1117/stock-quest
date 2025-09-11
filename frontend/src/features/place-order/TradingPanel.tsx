'use client';

import React from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  ToggleButton,
  ToggleButtonGroup,
  Alert,
  Divider,
  CircularProgress,
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { 
  PlaceOrderRequestSchema,
  type PlaceOrderRequestType,
} from '../../shared/validation/order-schemas';

type OrderForm = PlaceOrderRequestType;

interface TradingPanelProps {
  sessionId: number;
}

/**
 * 거래 패널 컴포넌트
 * 주문 접수 및 거래 내역 표시
 */
export function TradingPanel({ sessionId }: TradingPanelProps) {
  const [instruments, setInstruments] = React.useState<Array<{
    instrumentKey: string;
    hiddenName: string;
    type: string;
    realPrice: number;
  }>>([]);
  
  const [loading, setLoading] = React.useState(false);
  const [instrumentsLoading, setInstrumentsLoading] = React.useState(true);
  const [currentPrices, setCurrentPrices] = React.useState<Record<string, number>>({});
  const [error, setError] = React.useState<string | null>(null);
  const [success, setSuccess] = React.useState<string | null>(null);

  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors },
    reset,
  } = useForm<OrderForm>({
    resolver: zodResolver(PlaceOrderRequestSchema),
    defaultValues: {
      instrumentKey: '',
      side: 'BUY',
      orderType: 'MARKET',
      quantity: 1,
      limitPrice: undefined,
    },
  });

  const orderType = watch('orderType');

  // Load instruments from session/challenge API
  React.useEffect(() => {
    const loadInstruments = async () => {
      try {
        setInstrumentsLoading(true);
        
        // TODO: Replace with actual API call to get session instruments
        // const response = await getApiSessionsSessionId(sessionId);
        // setInstruments(response.data.challenge.instruments);
        
        // Temporary mock data for development
        await new Promise(resolve => setTimeout(resolve, 800));
        setInstruments([
          { instrumentKey: 'A', hiddenName: '회사 A', type: 'STOCK', realPrice: 180.00 },
          { instrumentKey: 'B', hiddenName: '회사 B', type: 'STOCK', realPrice: 420.00 },
          { instrumentKey: 'C', hiddenName: '회사 C', type: 'STOCK', realPrice: 140.00 },
          { instrumentKey: 'D', hiddenName: '회사 D', type: 'STOCK', realPrice: 250.00 },
          { instrumentKey: 'E', hiddenName: '회사 E', type: 'STOCK', realPrice: 150.00 },
        ]);
      } catch (error) {
        console.error('Failed to load instruments:', error);
        setError('상품 목록을 불러오는데 실패했습니다.');
      } finally {
        setInstrumentsLoading(false);
      }
    };

    loadInstruments();
  }, [sessionId]);

  const onSubmit = async (data: OrderForm) => {
    try {
      setLoading(true);
      setError(null);
      setSuccess(null);
      
      // Validate form data
      if (!data.instrumentKey) {
        throw new Error('상품을 선택해주세요.');
      }
      
      if (data.quantity < 1) {
        throw new Error('수량은 1 이상이어야 합니다.');
      }
      
      if (data.orderType === 'LIMIT' && (!data.limitPrice || data.limitPrice <= 0)) {
        throw new Error('지정가를 올바르게 입력해주세요.');
      }
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, Math.random() * 1000 + 500));
      
      // Simulate order processing failures
      const failureRate = data.orderType === 'LIMIT' ? 0.15 : 0.08;
      if (Math.random() < failureRate) {
        const errors = [
          '주문 수량이 부족합니다.',
          '시장 마감 시간입니다.',
          '주문 서버에 일시적인 문제가 발생했습니다.',
          '지정가가 현재 시세와 너무 많이 차이납니다.',
        ];
        throw new Error(errors[Math.floor(Math.random() * errors.length)]);
      }
      
      // Mock successful order execution
      const instrument = instruments.find(i => i.instrumentKey === data.instrumentKey);
      const executedPrice = data.orderType === 'LIMIT' 
        ? data.limitPrice! 
        : instrument!.realPrice * (1 + (Math.random() - 0.5) * 0.01); // ±0.5% 슬리페이지
      
      // Simulate API call
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/sessions/${sessionId}/orders`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
          body: JSON.stringify({
            ...data,
            executedPrice,
            executedAt: new Date().toISOString(),
          }),
        }
      ).catch(() => {
        // Mock successful response if API is not available
        return {
          ok: true,
          status: 200,
          json: async () => ({
            orderId: Math.random().toString(36).substr(2, 9),
            executedPrice,
            executedQuantity: data.quantity,
            executedAt: new Date().toISOString(),
          })
        };
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: 주문 접수에 실패했습니다`);
      }

      const result = await response.json();
      const orderTypeText = data.orderType === 'MARKET' ? '시장가' : '지정가';
      const sideText = data.side === 'BUY' ? '매수' : '매도';
      
      setSuccess(
        `${sideText} ${orderTypeText} 주문이 체결되었습니다. ` +
        `체결가: $${executedPrice.toFixed(2)} (${data.quantity}주)`
      );
      reset();
      
      // Auto-clear success message after 10 seconds
      setTimeout(() => setSuccess(null), 10000);
      
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '주문 접수에 실패했습니다';
      setError(errorMessage);
      
      // Auto-clear error message after 8 seconds
      setTimeout(() => setError(null), 8000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box 
      className="glass"
      sx={{ 
        p: 3,
        borderRadius: 3,
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: '4px',
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        }
      }}
    >
      <Typography 
        variant="h5" 
        gutterBottom
        sx={{ 
          fontWeight: 700,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          mb: 3
        }}
      >
        ⚡ 주문 접수
      </Typography>

      {error && (
        <Alert 
          severity="error" 
          onClose={() => setError(null)}
          sx={{ 
            mb: 2,
            background: 'rgba(244, 63, 94, 0.1)',
            border: '1px solid rgba(244, 63, 94, 0.2)',
            borderRadius: 2,
          }}
        >
          <Typography variant="body2">{error}</Typography>
          <Typography variant="caption" color="text.secondary">
            잘못된 주문 정보를 확인하거나 잠시 후 다시 시도해주세요.
          </Typography>
        </Alert>
      )}

      {success && (
        <Alert 
          severity="success" 
          onClose={() => setSuccess(null)}
          sx={{ 
            mb: 2,
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            borderRadius: 2,
          }}
        >
          <Typography variant="body2">{success}</Typography>
          <Typography variant="caption" color="text.secondary">
            주문 내역은 포트폴리오에서 확인할 수 있습니다.
          </Typography>
        </Alert>
      )}

      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* 매수/매도 선택 */}
          <Controller
            name="side"
            control={control}
            render={({ field }) => (
              <ToggleButtonGroup
                {...field}
                exclusive
                fullWidth
                onChange={(_, value) => field.onChange(value)}
                sx={{
                  '& .MuiToggleButton-root': {
                    py: 1.5,
                    fontSize: '1rem',
                    fontWeight: 600,
                    border: '2px solid rgba(99, 102, 241, 0.2)',
                    borderRadius: 2,
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                      transform: 'scale(1.02)',
                    },
                    '&.Mui-selected': {
                      color: 'white',
                      '&[value="BUY"]': {
                        background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                        border: '2px solid rgba(16, 185, 129, 0.6)',
                      },
                      '&[value="SELL"]': {
                        background: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
                        border: '2px solid rgba(244, 63, 94, 0.6)',
                      }
                    }
                  }
                }}
              >
                <ToggleButton value="BUY">
                  📈 매수
                </ToggleButton>
                <ToggleButton value="SELL">
                  📉 매도
                </ToggleButton>
              </ToggleButtonGroup>
            )}
          />

          {/* 상품 선택 */}
          <FormControl fullWidth error={!!errors.instrumentKey}>
            <InputLabel>상품 선택</InputLabel>
            <Select
              {...register('instrumentKey')}
              label="상품 선택"
              defaultValue=""
            >
              {instruments.map((instrument) => (
                <MenuItem key={instrument.instrumentKey} value={instrument.instrumentKey}>
                  {instrument.hiddenName} ({instrument.type})
                </MenuItem>
              ))}
            </Select>
            {errors.instrumentKey && (
              <Typography variant="caption" color="error">
                {errors.instrumentKey.message}
              </Typography>
            )}
          </FormControl>

          {/* 수량 입력 */}
          <TextField
            {...register('quantity', { valueAsNumber: true })}
            label="수량"
            type="number"
            fullWidth
            error={!!errors.quantity}
            helperText={errors.quantity?.message}
            inputProps={{ min: 1, step: 1 }}
          />

          {/* 주문 유형 선택 */}
          <Controller
            name="orderType"
            control={control}
            render={({ field }) => (
              <ToggleButtonGroup
                {...field}
                exclusive
                fullWidth
                onChange={(_, value) => field.onChange(value)}
              >
                <ToggleButton value="MARKET">
                  시장가
                </ToggleButton>
                <ToggleButton value="LIMIT">
                  지정가
                </ToggleButton>
              </ToggleButtonGroup>
            )}
          />

          {/* 지정가 입력 (지정가 주문 시만 표시) */}
          {orderType === 'LIMIT' && (
            <TextField
              {...register('limitPrice', { valueAsNumber: true })}
              label="지정가 (₩)"
              type="number"
              fullWidth
              error={!!errors.limitPrice}
              helperText={errors.limitPrice?.message || '원하는 체결 가격을 입력하세요'}
              inputProps={{ min: 0, step: 0.01 }}
            />
          )}

          <Divider 
            sx={{ 
              my: 2,
              background: 'linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.3), transparent)',
            }}
          />

          {/* 주문 버튼 */}
          <Button
            type="submit"
            variant="contained"
            size="large"
            fullWidth
            disabled={loading}
            className="btn-hover"
            sx={{
              py: 2,
              fontSize: '1.1rem',
              fontWeight: 700,
              borderRadius: 3,
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              boxShadow: '0 8px 25px rgba(99, 102, 241, 0.4)',
              '&:hover:not(:disabled)': {
                background: 'linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%)',
                transform: 'translateY(-2px)',
                boxShadow: '0 12px 35px rgba(99, 102, 241, 0.5)',
              },
              '&:disabled': {
                background: 'rgba(99, 102, 241, 0.3)',
                color: 'rgba(255, 255, 255, 0.7)',
              }
            }}
          >
            {loading ? (
              <Box display="flex" alignItems="center" gap={1}>
                <CircularProgress size={20} color="inherit" />
                주문 처리 중...
              </Box>
            ) : (
              '🚀 주문 접수'
            )}
          </Button>
        </Box>
      </Box>
    </Box>
  );
}