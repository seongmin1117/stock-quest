/**
 * 주문 관련 Zod 스키마
 * 백엔드 validation과 완전히 동기화된 클라이언트 사이드 검증
 */

import { z } from 'zod';

// Enum 스키마들 - 백엔드와 정확히 일치
export const OrderSideSchema = z.enum(['BUY', 'SELL'], {
  message: '매수/매도를 선택해주세요',
});

export const OrderTypeSchema = z.enum(['MARKET', 'LIMIT'], {
  message: '주문 유형을 선택해주세요',
});

export const OrderStatusSchema = z.enum(['PENDING', 'EXECUTED', 'CANCELLED']);

// InstrumentKey 검증 - 백엔드 @ValidInstrumentKey와 동일한 규칙
export const InstrumentKeySchema = z
  .string()
  .min(1, '상품을 선택해주세요')
  .regex(/^[A-Z]$/, '유효하지 않은 상품 키입니다');

// PlaceOrderRequest 스키마 - 백엔드 validation과 완전 일치
export const PlaceOrderRequestSchema = z.object({
  instrumentKey: InstrumentKeySchema,
  
  side: OrderSideSchema,
  
  quantity: z
    .number({
      message: '주문 수량은 숫자여야 합니다',
    })
    .min(0.000001, '주문 수량은 0보다 커야 합니다')
    .max(999999999, '주문 수량이 너무 큽니다')
    .refine(
      (val) => {
        // 소수점 6자리까지 허용 (backend: @Digits(integer = 9, fraction = 6))
        const str = val.toString();
        const decimalPart = str.split('.')[1];
        return !decimalPart || decimalPart.length <= 6;
      },
      { message: '주문 수량은 소수점 6자리까지만 허용됩니다' }
    ),
    
  orderType: OrderTypeSchema,
  
  limitPrice: z
    .number({
      message: '지정가는 숫자여야 합니다',
    })
    .min(0.01, '지정가는 0.01 이상이어야 합니다')
    .max(999999999, '지정가가 너무 큽니다')
    .refine(
      (val) => {
        // 소수점 2자리까지 허용 (backend: @Digits(integer = 9, fraction = 2))
        const str = val.toString();
        const decimalPart = str.split('.')[1];
        return !decimalPart || decimalPart.length <= 2;
      },
      { message: '지정가는 소수점 2자리까지만 허용됩니다' }
    )
    .optional(),
}).refine(
  (data) => {
    // LIMIT 주문의 경우 limitPrice 필수
    if (data.orderType === 'LIMIT' && (data.limitPrice === undefined || data.limitPrice <= 0)) {
      return false;
    }
    // MARKET 주문의 경우 limitPrice 불필요
    if (data.orderType === 'MARKET' && data.limitPrice !== undefined) {
      return false;
    }
    return true;
  },
  {
    message: 'LIMIT 주문은 유효한 지정가가 필요하고, MARKET 주문에는 지정가를 설정할 수 없습니다',
    path: ['limitPrice'],
  }
);

// PlaceOrderResponse 스키마
export const PlaceOrderResponseSchema = z.object({
  orderId: z.number(),
  instrumentKey: z.string(),
  side: OrderSideSchema,
  quantity: z.number(),
  executedPrice: z.number(),
  slippageRate: z.number(),
  status: OrderStatusSchema,
  executedAt: z.string().datetime(),
  newBalance: z.number(),
  message: z.string(),
});

// Auth 관련 스키마들
export const SignupRequestSchema = z.object({
  email: z
    .string()
    .min(1, '이메일은 필수입니다')
    .email('올바른 이메일 형식이 아닙니다')
    .max(255, '이메일이 너무 깁니다'),
    
  password: z
    .string()
    .min(8, '비밀번호는 8자 이상이어야 합니다')
    .max(100, '비밀번호가 너무 깁니다')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
      '비밀번호는 대문자, 소문자, 숫자를 포함해야 합니다'
    ),
    
  nickname: z
    .string()
    .min(2, '닉네임은 2자 이상이어야 합니다')
    .max(50, '닉네임이 너무 깁니다')
    .regex(
      /^[가-힣a-zA-Z0-9_-]+$/,
      '닉네임은 한글, 영문, 숫자, -, _ 만 사용 가능합니다'
    ),
});

export const LoginRequestSchema = z.object({
  email: z
    .string()
    .min(1, '이메일을 입력해주세요')
    .email('올바른 이메일 형식이 아닙니다'),
    
  password: z
    .string()
    .min(1, '비밀번호를 입력해주세요'),
});

// ErrorResponse 스키마
export const ErrorResponseSchema = z.object({
  error: z.string(),
  message: z.string(),
  timestamp: z.string().datetime(),
  path: z.string(),
});

// 타입 추출
export type PlaceOrderRequestType = z.infer<typeof PlaceOrderRequestSchema>;
export type PlaceOrderResponseType = z.infer<typeof PlaceOrderResponseSchema>;
export type SignupRequestType = z.infer<typeof SignupRequestSchema>;
export type LoginRequestType = z.infer<typeof LoginRequestSchema>;
export type ErrorResponseType = z.infer<typeof ErrorResponseSchema>;
export type OrderSideType = z.infer<typeof OrderSideSchema>;
export type OrderTypeType = z.infer<typeof OrderTypeSchema>;
export type OrderStatusType = z.infer<typeof OrderStatusSchema>;

/**
 * API 응답 검증 헬퍼 함수들
 */
export const validatePlaceOrderResponse = (data: unknown): PlaceOrderResponseType => {
  return PlaceOrderResponseSchema.parse(data);
};

export const validateErrorResponse = (data: unknown): ErrorResponseType => {
  return ErrorResponseSchema.parse(data);
};

/**
 * 폼 데이터 사전 검증 (submit 전)
 */
export const validateOrderForm = (data: unknown): PlaceOrderRequestType => {
  return PlaceOrderRequestSchema.parse(data);
};

export const validateSignupForm = (data: unknown): SignupRequestType => {
  return SignupRequestSchema.parse(data);
};

export const validateLoginForm = (data: unknown): LoginRequestType => {
  return LoginRequestSchema.parse(data);
};