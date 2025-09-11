# API 사용 예시

StockQuest API의 주요 엔드포인트 사용 예시와 응답 형태를 설명합니다.

## 🔐 인증 API

### 회원가입

**요청**:
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "investor@example.com",
  "password": "securepassword123",
  "nickname": "투자초보"
}
```

**응답** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "investor@example.com", 
  "nickname": "투자초보"
}
```

**에러 응답** (400 Bad Request):
```json
{
  "error": "DUPLICATE_EMAIL",
  "message": "이미 등록된 이메일입니다",
  "timestamp": "2024-08-31T10:30:00",
  "path": "/api/auth/signup"
}
```

### 로그인

**요청**:
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "investor@example.com",
  "password": "securepassword123"
}
```

**응답** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "investor@example.com",
  "nickname": "투자초보"
}
```

## 🎯 챌린지 API

### 챌린지 목록 조회

**요청**:
```http
GET /api/challenges?page=0&size=10&status=ACTIVE
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "title": "2020년 코로나 급락장 챌린지",
      "description": "2020년 3월 코로나19로 인한 급락장에서 생존하기",
      "periodStart": "2020-02-01",
      "periodEnd": "2020-05-01", 
      "speedFactor": 10,
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00"
    },
    {
      "id": 2,
      "title": "2021년 밈스톡 광풍 챌린지",
      "description": "게임스탑, AMC 등 밈스톡 열풍 시기의 변동성 대응",
      "periodStart": "2021-01-01",
      "periodEnd": "2021-03-01",
      "speedFactor": 15,
      "status": "ACTIVE", 
      "createdAt": "2024-01-02T00:00:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

### 챌린지 시작

**요청**:
```http
POST /api/challenges/1/start
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (201 Created):
```json
{
  "id": 101,
  "challengeId": 1,
  "seedBalance": 1000000.00,
  "currentBalance": 1000000.00,
  "status": "ACTIVE",
  "startedAt": "2024-08-31T10:30:00"
}
```

**에러 응답** (409 Conflict):
```json
{
  "error": "ALREADY_PARTICIPATING", 
  "message": "이미 참여 중인 챌린지입니다",
  "timestamp": "2024-08-31T10:30:00",
  "path": "/api/challenges/1/start"
}
```

### 챌린지 상품 목록 조회

**요청**:
```http
GET /api/challenges/1/instruments
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
[
  {
    "instrumentKey": "A",
    "hiddenName": "회사 A",
    "type": "STOCK"
  },
  {
    "instrumentKey": "B", 
    "hiddenName": "회사 B",
    "type": "STOCK"
  },
  {
    "instrumentKey": "C",
    "hiddenName": "회사 C",
    "type": "STOCK"
  },
  {
    "instrumentKey": "D",
    "hiddenName": "예금 상품 D", 
    "type": "DEPOSIT"
  },
  {
    "instrumentKey": "E",
    "hiddenName": "채권 상품 E",
    "type": "BOND"
  }
]
```

## 💰 거래 API

### 주문 접수

**매수 주문 요청**:
```http
POST /api/sessions/101/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "instrumentKey": "A",
  "side": "BUY",
  "quantity": 10,
  "orderType": "MARKET"
}
```

**지정가 매도 주문 요청**:
```http
POST /api/sessions/101/orders  
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "instrumentKey": "A",
  "side": "SELL", 
  "quantity": 5,
  "orderType": "LIMIT",
  "limitPrice": 125.50
}
```

**주문 체결 응답** (201 Created):
```json
{
  "id": 1001,
  "instrumentKey": "A",
  "side": "BUY", 
  "quantity": 10,
  "orderType": "MARKET",
  "limitPrice": null,
  "executedPrice": 120.75,
  "slippageRate": 0.50,
  "status": "EXECUTED",
  "orderedAt": "2024-08-31T10:35:00",
  "executedAt": "2024-08-31T10:35:01"
}
```

**에러 응답** (403 Forbidden):
```json
{
  "error": "INSUFFICIENT_BALANCE",
  "message": "잔고가 부족합니다. 필요: ₩1,207,500, 보유: ₩800,000",
  "timestamp": "2024-08-31T10:35:00", 
  "path": "/api/sessions/101/orders"
}
```

### 포트폴리오 조회

**요청**:
```http
GET /api/sessions/101/portfolio
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
{
  "sessionId": 101,
  "currentBalance": 792500.00,
  "positions": [
    {
      "instrumentKey": "A",
      "hiddenName": "회사 A",
      "quantity": 10,
      "averagePrice": 120.75,
      "totalCost": 1207.50,
      "currentPrice": 125.30,
      "currentValue": 1253.00,
      "unrealizedPnL": 45.50
    },
    {
      "instrumentKey": "B",
      "hiddenName": "회사 B", 
      "quantity": 5,
      "averagePrice": 89.20,
      "totalCost": 446.00,
      "currentPrice": 92.10,
      "currentValue": 460.50,
      "unrealizedPnL": 14.50
    }
  ],
  "totalValue": 1713.50,
  "totalPnL": 60.00,
  "returnPercentage": 6.00
}
```

### 챌린지 종료

**요청**:
```http
POST /api/sessions/101/close
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
{
  "sessionId": 101,
  "finalBalance": 792500.00,
  "totalPnL": 60000.00,
  "returnPercentage": 6.00,
  "rank": 5,
  "revealedInstruments": [
    {
      "instrumentKey": "A",
      "actualTicker": "AAPL", 
      "actualName": "Apple Inc."
    },
    {
      "instrumentKey": "B",
      "actualTicker": "MSFT",
      "actualName": "Microsoft Corporation"
    },
    {
      "instrumentKey": "C", 
      "actualTicker": "GOOGL",
      "actualName": "Alphabet Inc."
    }
  ],
  "portfolio": {
    "sessionId": 101,
    "currentBalance": 792500.00,
    "positions": [
      {
        "instrumentKey": "A",
        "hiddenName": "회사 A",
        "quantity": 10,
        "averagePrice": 120.75,
        "currentPrice": 125.30,
        "currentValue": 1253.00,
        "unrealizedPnL": 45.50
      }
    ],
    "totalValue": 1045.50,
    "totalPnL": 60000.00,
    "returnPercentage": 6.00
  }
}
```

## 🏆 리더보드 API

### 리더보드 조회

**요청**:
```http
GET /api/challenges/1/leaderboard?limit=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
[
  {
    "rank": 1,
    "userId": 15,
    "nickname": "투자고수",
    "returnPercentage": 25.80,
    "pnl": 258000.00,
    "calculatedAt": "2024-08-31T10:40:00"
  },
  {
    "rank": 2,
    "userId": 8,
    "nickname": "주식왕", 
    "returnPercentage": 18.30,
    "pnl": 183000.00,
    "calculatedAt": "2024-08-31T10:40:00"
  },
  {
    "rank": 3,
    "userId": 22,
    "nickname": "코인러버",
    "returnPercentage": 12.70,
    "pnl": 127000.00,
    "calculatedAt": "2024-08-31T10:40:00"
  }
]
```

## 💬 커뮤니티 API

### 게시글 목록 조회

**요청**:
```http
GET /api/challenges/1/posts?page=0&size=20
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
{
  "content": [
    {
      "id": 501,
      "authorNickname": "투자고수",
      "content": "A 회사가 기술적으로 강한 지지선을 보이고 있네요. 매수 타이밍인 것 같습니다.",
      "createdAt": "2024-08-31T10:25:00",
      "commentCount": 3
    },
    {
      "id": 502,
      "authorNickname": "주식왕",
      "content": "전체적으로 시장이 하락 추세인데, 현금 비중을 늘려야 할까요?",
      "createdAt": "2024-08-31T10:20:00", 
      "commentCount": 7
    }
  ],
  "totalElements": 15,
  "totalPages": 1
}
```

### 게시글 작성

**요청**:
```http
POST /api/challenges/1/posts
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "content": "오늘 B 회사 실적 발표가 있었는데, 예상보다 좋은 결과가 나왔네요. 주가 반응을 지켜봐야겠습니다."
}
```

**응답** (201 Created):
```json
{
  "id": 503,
  "authorNickname": "투자초보",
  "content": "오늘 B 회사 실적 발표가 있었는데, 예상보다 좋은 결과가 나왔네요. 주가 반응을 지켜봐야겠습니다.",
  "createdAt": "2024-08-31T10:45:00",
  "commentCount": 0
}
```

## 🏦 적금 상품 API

### 상품 목록 조회

**요청**:
```http
GET /api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "type": "DEPOSIT",
    "name": "안전 예금 3개월",
    "rateApy": 3.00,
    "tenorDays": 90,
    "minAmount": 10000.00,
    "maxAmount": 50000000.00,
    "description": "원금 보장 단기 예금 상품입니다. 3% 연이율이 적용됩니다."
  },
  {
    "id": 2,
    "type": "BOND", 
    "name": "국고채 1년",
    "rateApy": 4.20,
    "tenorDays": 365,
    "minAmount": 100000.00,
    "maxAmount": 100000000.00,
    "description": "국가가 보증하는 1년 만기 채권입니다. 4.2% 연이율이 적용됩니다."
  }
]
```

### 상품 가입

**요청**:
```http
POST /api/products/1/subscribe
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "amount": 500000.00
}
```

**응답** (201 Created):
```json
{
  "id": 301,
  "productName": "안전 예금 3개월",
  "principal": 500000.00,
  "startDate": "2024-08-31",
  "maturityDate": "2024-11-29", 
  "accruedInterest": 0.00,
  "status": "ACTIVE"
}
```

## 📈 실시간 데이터 API 패턴

### 포트폴리오 실시간 업데이트

프론트엔드에서 포트폴리오 실시간 업데이트를 위한 폴링 패턴:

```typescript
// React Query를 사용한 실시간 데이터 페칭
const { data: portfolio } = useQuery({
  queryKey: ['portfolio', sessionId],
  queryFn: () => fetchPortfolio(sessionId),
  refetchInterval: 5000, // 5초마다 업데이트
  enabled: !!sessionId,
});
```

### 리더보드 실시간 업데이트

```typescript
// 리더보드 실시간 업데이트 (10초 간격)
const { data: leaderboard } = useQuery({
  queryKey: ['leaderboard', challengeId],
  queryFn: () => fetchLeaderboard(challengeId),
  refetchInterval: 10000, // 10초마다 업데이트
  enabled: !!challengeId,
});
```

## 🔄 비즈니스 플로우 예시

### 완전한 챌린지 플로우

```mermaid
sequenceDiagram
    participant U as 사용자
    participant F as Frontend
    participant B as Backend  
    participant D as Database
    participant Y as Yahoo Finance

    Note over U,Y: 1. 챌린지 시작
    U->>F: 챌린지 선택
    F->>B: POST /api/challenges/1/start
    B->>D: 세션 생성
    B->>F: 세션 정보 반환
    F->>U: 거래 화면 표시

    Note over U,Y: 2. 주문 처리  
    U->>F: 매수 주문 (회사 A, 10주)
    F->>B: POST /api/sessions/101/orders
    B->>D: 주문 저장 & 포지션 업데이트
    B->>F: 체결 정보 반환
    F->>U: 체결 완료 알림

    Note over U,Y: 3. 실시간 업데이트
    loop 5초마다
        F->>B: GET /api/sessions/101/portfolio
        B->>D: 포트폴리오 조회
        B->>F: 현재 손익 반환
        F->>U: UI 업데이트
    end

    Note over U,Y: 4. 챌린지 종료
    U->>F: 챌린지 종료 버튼
    F->>B: POST /api/sessions/101/close
    B->>D: 세션 종료 & 순위 계산
    B->>F: 최종 결과 + 티커 공개
    F->>U: 결과 화면 표시
```

## ⚙️ 개발 환경별 API 설정

### 로컬 개발 (MSW 모킹)
```typescript
// 프론트엔드에서 MSW 핸들러 사용
if (process.env.NEXT_PUBLIC_MOCK_API === 'true') {
  // MSW 워커 시작
  worker.start();
}

// API 호출은 동일하게 유지
const response = await fetch('/api/auth/login', {
  method: 'POST',
  body: JSON.stringify({ email, password }),
});
```

### 통합 개발 (실제 백엔드)
```typescript
// 환경 변수로 API 서버 변경
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL; // http://localhost:8080

// 실제 백엔드 API 호출
const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  },
  body: JSON.stringify({ email, password }),
});
```

### 운영 환경
```typescript
// 운영 환경 설정
const API_BASE_URL = 'https://api.stockquest.com';

// HTTPS 강제, 보안 헤더 추가
const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
    'X-Requested-With': 'XMLHttpRequest',
  },
  body: JSON.stringify({ email, password }),
});
```

## 🚨 에러 처리 패턴

### 공통 에러 응답 형식

모든 API 에러는 다음 형식을 따릅니다:

```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 에러 메시지",
  "timestamp": "2024-08-31T10:30:00",
  "path": "/api/endpoint/path"
}
```

### 주요 에러 코드

| 에러 코드 | HTTP 상태 | 설명 | 해결 방법 |
|-----------|-----------|------|-----------|
| `VALIDATION_ERROR` | 400 | 입력값 검증 실패 | 요청 데이터 확인 |
| `DUPLICATE_EMAIL` | 400 | 이메일 중복 | 다른 이메일 사용 |
| `AUTHENTICATION_FAILED` | 401 | 인증 실패 | 로그인 정보 확인 |
| `TOKEN_EXPIRED` | 401 | 토큰 만료 | 재로그인 필요 |
| `ACCESS_DENIED` | 403 | 권한 없음 | 권한 확인 |
| `CHALLENGE_NOT_FOUND` | 404 | 챌린지 없음 | 챌린지 ID 확인 |
| `ALREADY_PARTICIPATING` | 409 | 중복 참여 | 기존 세션 확인 |
| `INSUFFICIENT_BALANCE` | 403 | 잔고 부족 | 주문 금액 조정 |
| `INVALID_ORDER` | 400 | 잘못된 주문 | 주문 파라미터 확인 |
| `SESSION_ENDED` | 403 | 세션 종료됨 | 새 챌린지 시작 |

### 프론트엔드 에러 처리

```typescript
// API 호출 에러 처리 패턴
try {
  const response = await apiCall();
  return response.data;
} catch (error) {
  if (error.response?.status === 401) {
    // 인증 오류 - 로그인 페이지로 리다이렉트
    logout();
    router.push('/auth/login');
  } else if (error.response?.status === 403) {
    // 권한 오류 - 사용자에게 알림
    showError('권한이 없습니다');
  } else {
    // 기타 오류 - 일반적인 에러 메시지
    showError(error.response?.data?.message || '오류가 발생했습니다');
  }
}
```

---

이 문서는 StockQuest API를 효과적으로 사용하기 위한 실용적인 가이드입니다. 추가 질문이나 상세한 구현 예시가 필요하시면 언제든 문의해주세요.