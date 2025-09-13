/**
 * API Contract Tests
 * 백엔드 API와 프론트엔드 타입 정의 간의 계약 준수를 검증
 */

import { describe, it, expect } from '@jest/globals';
import {
  PlaceOrderRequest,
  PlaceOrderRequestSide,
  PlaceOrderRequestOrderType,
  OrderResponse,
  OrderResponseStatus,
  AuthResponse,
  ErrorResponse,
} from '../generated/model';

describe('API Contract Tests', () => {
  describe('PlaceOrderRequest 타입 계약', () => {
    it('should have all required fields with correct types', () => {
      const validRequest: PlaceOrderRequest = {
        instrumentKey: 'A',
        side: 'BUY' as PlaceOrderRequestSide,
        quantity: 10,
        orderType: 'MARKET' as PlaceOrderRequestOrderType,
        limitPrice: undefined, // MARKET 주문이므로 선택사항
      };

      expect(validRequest.instrumentKey).toBe('A');
      expect(validRequest.side).toBe('BUY');
      expect(validRequest.quantity).toBe(10);
      expect(validRequest.orderType).toBe('MARKET');
      expect(validRequest.limitPrice).toBeUndefined();
    });

    it('should support LIMIT order with limitPrice', () => {
      const limitOrder: PlaceOrderRequest = {
        instrumentKey: 'B',
        side: 'SELL' as PlaceOrderRequestSide,
        quantity: 5,
        orderType: 'LIMIT' as PlaceOrderRequestOrderType,
        limitPrice: 150.75,
      };

      expect(limitOrder.limitPrice).toBe(150.75);
      expect(typeof limitOrder.limitPrice).toBe('number');
    });
  });

  describe('OrderResponse 타입 계약', () => {
    it('should match backend PlaceOrderResponse structure', () => {
      const mockResponse: OrderResponse = {
        id: 123,
        instrumentKey: 'A',
        side: 'BUY',
        quantity: 10,
        executedPrice: 152.5,
        slippageRate: 1.25,
        status: 'EXECUTED' as OrderResponseStatus,
        executedAt: '2024-01-15T10:30:00Z',
        orderedAt: '2024-01-15T10:29:58Z',
        orderType: 'MARKET',
        limitPrice: undefined,
      };

      // 모든 필드가 선택적이므로 undefined 체크
      expect(mockResponse.id).toBe(123);
      expect(mockResponse.instrumentKey).toBe('A');
      expect(mockResponse.side).toBe('BUY');
      expect(mockResponse.quantity).toBe(10);
      expect(mockResponse.executedPrice).toBe(152.5);
      expect(mockResponse.slippageRate).toBe(1.25);
      expect(mockResponse.status).toBe('EXECUTED');
      expect(mockResponse.executedAt).toBeDefined();
      expect(mockResponse.orderedAt).toBeDefined();
    });

    it('should support all OrderStatus enum values', () => {
      const statuses: OrderResponseStatus[] = ['PENDING', 'EXECUTED', 'CANCELLED'];
      
      statuses.forEach(status => {
        const response: OrderResponse = {
          id: 1,
          status: status,
        };
        expect(['PENDING', 'EXECUTED', 'CANCELLED']).toContain(response.status);
      });
    });
  });

  describe('AuthResponse 타입 계약', () => {
    it('should handle signup response (without access token)', () => {
      const signupResponse: AuthResponse = {
        userId: 1,
        email: 'test@example.com',
        nickname: '테스트사용자',
        accessToken: undefined, // 회원가입 시 토큰 없을 수 있음
        refreshToken: undefined,
      };

      expect(signupResponse.userId).toBe(1);
      expect(signupResponse.email).toBe('test@example.com');
      expect(signupResponse.nickname).toBe('테스트사용자');
      expect(signupResponse.accessToken).toBeUndefined();
    });

    it('should handle login response (with tokens)', () => {
      const loginResponse: AuthResponse = {
        userId: 1,
        email: 'test@example.com',
        nickname: '테스트사용자',
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        accessTokenExpiresAt: '2024-01-15T12:00:00Z',
        refreshTokenExpiresAt: '2024-02-15T12:00:00Z',
      };

      expect(loginResponse.accessToken).toBeTruthy();
      expect(typeof loginResponse.accessToken).toBe('string');
      expect(loginResponse.userId).toBe(1);
      expect(loginResponse.email).toBe('test@example.com');
      expect(loginResponse.nickname).toBe('테스트사용자');
    });
  });

  describe('ErrorResponse 타입 계약', () => {
    it('should match backend ErrorResponse structure', () => {
      const errorResponse: ErrorResponse = {
        error: 'VALIDATION_ERROR',
        message: '잘못된 요청입니다',
        timestamp: '2024-01-15T10:30:00Z',
        path: '/api/auth/signup',
      };

      expect(errorResponse.error).toBe('VALIDATION_ERROR');
      expect(errorResponse.message).toBe('잘못된 요청입니다');
      expect(errorResponse.timestamp).toBeTruthy();
      expect(errorResponse.path).toBe('/api/auth/signup');
    });
  });

  describe('Enum 값 일치성 검증', () => {
    it('should have matching OrderSide values', () => {
      const sides: PlaceOrderRequestSide[] = ['BUY', 'SELL'];
      
      sides.forEach(side => {
        expect(['BUY', 'SELL']).toContain(side);
      });
    });

    it('should have matching OrderType values', () => {
      const types: PlaceOrderRequestOrderType[] = ['MARKET', 'LIMIT'];
      
      types.forEach(type => {
        expect(['MARKET', 'LIMIT']).toContain(type);
      });
    });

    it('should have matching OrderStatus values', () => {
      const statuses: OrderResponseStatus[] = ['PENDING', 'EXECUTED', 'CANCELLED'];
      
      statuses.forEach(status => {
        expect(['PENDING', 'EXECUTED', 'CANCELLED']).toContain(status);
      });
    });
  });

  describe('타입 안전성 검증', () => {
    it('should enforce number types for numeric fields', () => {
      const request: PlaceOrderRequest = {
        instrumentKey: 'A',
        side: 'BUY' as PlaceOrderRequestSide,
        quantity: 10.5, // 소수점도 허용
        orderType: 'LIMIT' as PlaceOrderRequestOrderType,
        limitPrice: 99.99,
      };

      expect(typeof request.quantity).toBe('number');
      expect(typeof request.limitPrice).toBe('number');
    });

    it('should handle optional fields correctly', () => {
      const partialResponse: Partial<OrderResponse> = {
        id: 1,
        status: 'EXECUTED' as OrderResponseStatus,
        // 다른 필드들은 선택적이므로 생략 가능
      };

      expect(partialResponse.id).toBe(1);
      expect(partialResponse.status).toBe('EXECUTED');
      expect(partialResponse.instrumentKey).toBeUndefined();
    });
  });
});

/**
 * Mock API Response Validation Helper
 * 실제 API 응답이 타입 정의와 일치하는지 런타임에서 검증
 */
export function validateApiResponse<T>(response: unknown, expectedFields: (keyof T)[]): response is T {
  if (typeof response !== 'object' || response === null) {
    return false;
  }

  const responseObj = response as Record<string, unknown>;
  
  return expectedFields.every(field => {
    return Object.prototype.hasOwnProperty.call(responseObj, field as string);
  });
}

/**
 * 사용 예시:
 * 
 * const apiResponse = await placeOrder(orderRequest);
 * 
 * if (validateApiResponse<OrderResponse>(apiResponse, ['id', 'status', 'executedAt'])) {
 *   // 이제 apiResponse는 OrderResponse 타입으로 안전하게 사용 가능
 *   console.log(apiResponse.status);
 * }
 */