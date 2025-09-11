import { http, HttpResponse } from 'msw';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

/**
 * MSW 모킹 핸들러
 * 백엔드 API가 준비되기 전까지 프론트엔드 개발용 모킹 데이터
 */
export const handlers = [
  // 회원가입 API
  http.post(`${API_BASE_URL}/api/auth/signup`, async ({ request }) => {
    const body = await request.json() as any;
    
    // 간단한 유효성 검사
    if (!body.email || !body.password || !body.nickname) {
      return HttpResponse.json(
        {
          error: 'VALIDATION_ERROR',
          message: '필수 항목이 누락되었습니다',
          timestamp: new Date().toISOString(),
          path: '/api/auth/signup'
        },
        { status: 400 }
      );
    }
    
    // 이메일 중복 시뮬레이션
    if (body.email === 'duplicate@example.com') {
      return HttpResponse.json(
        {
          error: 'DUPLICATE_EMAIL',
          message: '이미 등록된 이메일입니다',
          timestamp: new Date().toISOString(),
          path: '/api/auth/signup'
        },
        { status: 400 }
      );
    }
    
    // 성공 응답
    return HttpResponse.json({
      accessToken: null,  // 회원가입 시에는 토큰 발급 안함
      userId: Math.floor(Math.random() * 1000) + 1,
      email: body.email,
      nickname: body.nickname,
    }, { status: 201 });
  }),

  // 로그인 API
  http.post(`${API_BASE_URL}/api/auth/login`, async ({ request }) => {
    const body = await request.json() as any;
    
    // 로그인 실패 시뮬레이션
    if (body.email !== 'test@example.com' || body.password !== 'password123') {
      return HttpResponse.json(
        {
          error: 'AUTHENTICATION_FAILED',
          message: '이메일 또는 비밀번호가 일치하지 않습니다',
          timestamp: new Date().toISOString(),
          path: '/api/auth/login'
        },
        { status: 401 }
      );
    }
    
    // 성공 응답
    return HttpResponse.json({
      accessToken: 'mock-jwt-token-' + Date.now(),
      userId: 1,
      email: body.email,
      nickname: '테스트사용자',
    });
  }),

  // 현재 사용자 조회 API
  http.get(`${API_BASE_URL}/api/auth/me`, ({ request }) => {
    const authHeader = request.headers.get('Authorization');
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return HttpResponse.json(
        {
          error: 'UNAUTHORIZED',
          message: '인증이 필요합니다',
          timestamp: new Date().toISOString(),
          path: '/api/auth/me'
        },
        { status: 401 }
      );
    }
    
    // 성공 응답
    return HttpResponse.json({
      accessToken: null,
      userId: 1,
      email: 'test@example.com',
      nickname: '테스트사용자',
    });
  }),

  // 챌린지 목록 조회 API
  http.get(`${API_BASE_URL}/api/challenges`, () => {
    return HttpResponse.json({
      content: [
        {
          id: 1,
          title: '2020년 코로나 급락장 챌린지',
          description: '2020년 3월 코로나19로 인한 급락장에서 생존하기',
          periodStart: '2020-02-01',
          periodEnd: '2020-05-01',
          speedFactor: 10,
          status: 'ACTIVE',
          createdAt: '2024-01-01T00:00:00'
        },
        {
          id: 2,
          title: '2021년 밈스톡 광풍 챌린지',
          description: '게임스탑, AMC 등 밈스톡 열풍 시기의 변동성 대응',
          periodStart: '2021-01-01',
          periodEnd: '2021-03-01',
          speedFactor: 15,
          status: 'ACTIVE',
          createdAt: '2024-01-02T00:00:00'
        },
        {
          id: 3,
          title: '2022년 금리 인상 챌린지',
          description: '연준의 공격적 금리 인상 시기 포트폴리오 관리',
          periodStart: '2022-03-01',
          periodEnd: '2022-12-01',
          speedFactor: 20,
          status: 'COMPLETED',
          createdAt: '2024-01-03T00:00:00'
        }
      ],
      totalElements: 3,
      totalPages: 1
    });
  }),

  // 챌린지 시작 API
  http.post(`${API_BASE_URL}/api/challenges/:challengeId/start`, ({ params }) => {
    const challengeId = params.challengeId;
    
    return HttpResponse.json({
      id: Math.floor(Math.random() * 1000) + 1,
      challengeId: Number(challengeId),
      seedBalance: 1000000,
      currentBalance: 1000000,
      status: 'ACTIVE',
      startedAt: new Date().toISOString()
    }, { status: 201 });
  }),

  // 챌린지 상품 목록 조회 API
  http.get(`${API_BASE_URL}/api/challenges/:challengeId/instruments`, () => {
    return HttpResponse.json([
      {
        instrumentKey: 'A',
        hiddenName: '회사 A',
        type: 'STOCK'
      },
      {
        instrumentKey: 'B', 
        hiddenName: '회사 B',
        type: 'STOCK'
      },
      {
        instrumentKey: 'C',
        hiddenName: '회사 C', 
        type: 'STOCK'
      },
      {
        instrumentKey: 'D',
        hiddenName: '예금 상품 D',
        type: 'DEPOSIT'
      },
      {
        instrumentKey: 'E',
        hiddenName: '채권 상품 E',
        type: 'BOND'
      }
    ]);
  }),

  // 주문 접수 API
  http.post(`${API_BASE_URL}/api/sessions/:sessionId/orders`, async ({ request, params }) => {
    const body = await request.json() as any;
    const sessionId = params.sessionId;
    
    // 잔고 부족 시뮬레이션
    if (body.side === 'BUY' && body.quantity * 100 > 500000) {
      return HttpResponse.json(
        {
          error: 'INSUFFICIENT_BALANCE',
          message: '잔고가 부족합니다',
          timestamp: new Date().toISOString(),
          path: `/api/sessions/${sessionId}/orders`
        },
        { status: 403 }
      );
    }
    
    // 성공 응답
    return HttpResponse.json({
      id: Math.floor(Math.random() * 1000) + 1,
      instrumentKey: body.instrumentKey,
      side: body.side,
      quantity: body.quantity,
      orderType: body.orderType,
      executedPrice: 100 + Math.random() * 50, // 모킹 가격
      slippageRate: 0.5,
      status: 'EXECUTED',
      orderedAt: new Date().toISOString(),
      executedAt: new Date().toISOString()
    }, { status: 201 });
  }),

  // 포트폴리오 조회 API
  http.get(`${API_BASE_URL}/api/sessions/:sessionId/portfolio`, () => {
    return HttpResponse.json({
      sessionId: 1,
      currentBalance: 850000,
      positions: [
        {
          instrumentKey: 'A',
          hiddenName: '회사 A',
          quantity: 10,
          averagePrice: 120.50,
          totalCost: 1205,
          currentPrice: 125.30,
          currentValue: 1253,
          unrealizedPnL: 48
        },
        {
          instrumentKey: 'B',
          hiddenName: '회사 B', 
          quantity: 5,
          averagePrice: 89.20,
          totalCost: 446,
          currentPrice: 92.10,
          currentValue: 460.50,
          unrealizedPnL: 14.50
        }
      ],
      totalValue: 1713.50,
      totalPnL: 62.50,
      returnPercentage: 6.25
    });
  }),

  // 리더보드 조회 API
  http.get(`${API_BASE_URL}/api/challenges/:challengeId/leaderboard`, () => {
    return HttpResponse.json([
      {
        rank: 1,
        userId: 1,
        nickname: '투자고수',
        returnPercentage: 15.8,
        pnl: 158000,
        calculatedAt: new Date().toISOString()
      },
      {
        rank: 2,
        userId: 2,
        nickname: '주식왕',
        returnPercentage: 12.3,
        pnl: 123000,
        calculatedAt: new Date().toISOString()
      },
      {
        rank: 3,
        userId: 3,
        nickname: '챌린저',
        returnPercentage: 8.7,
        pnl: 87000,
        calculatedAt: new Date().toISOString()
      }
    ]);
  }),
];

export default handlers;