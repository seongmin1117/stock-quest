# StockQuest API Guide for Frontend Developers

## 📋 개요

StockQuest는 주식 트레이딩 시뮬레이션 플랫폼의 백엔드 API입니다. 이 가이드는 프론트엔드 개발자가 백엔드 API를 효과적으로 활용할 수 있도록 작성되었습니다.

## 🔗 API 문서 링크

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## 🚀 시작하기

### Base URL
```
Development: http://localhost:8080
Production: https://api.stockquest.com
```

### 인증 방식
JWT Bearer Token을 사용합니다.
```
Authorization: Bearer <your-jwt-token>
```

## 📖 주요 API 카테고리

### 1. 🔐 인증 API (`/api/auth`)

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "redirectUrl": "optional-redirect-url"
}
```

**응답 예시:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "username": "사용자명",
    "role": "USER"
  }
}
```

#### 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "username": "새로운사용자",
  "confirmPassword": "securePassword123"
}
```

#### 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### 2. 🏢 회사 정보 API (`/api/v1/companies`)

#### 개별 회사 조회
```http
GET /api/v1/companies/005930
```

**응답 예시:**
```json
{
  "id": 1,
  "symbol": "005930",
  "nameKr": "삼성전자",
  "nameEn": "Samsung Electronics",
  "sector": "반도체",
  "marketCap": 360000000000000,
  "marketCapDisplay": "360조원",
  "logoPath": "/logos/samsung.png",
  "descriptionKr": "글로벌 반도체, 스마트폰 제조업체",
  "descriptionEn": "Global semiconductor and smartphone manufacturer",
  "exchange": "KRX",
  "currency": "KRW",
  "isActive": true,
  "popularityScore": 100,
  "categories": ["semiconductor", "tech"]
}
```

#### 인기 회사 목록
```http
GET /api/v1/companies/top?limit=10
```

#### 회사 검색
```http
GET /api/v1/companies/search?q=삼성&limit=10&offset=0
```

또는 POST 방식:
```http
POST /api/v1/companies/search
Content-Type: application/json

{
  "query": "삼성",
  "categories": ["tech", "semiconductor"],
  "sector": "반도체",
  "minPopularity": 50,
  "sort": "popularityScore",
  "order": "desc",
  "limit": 20,
  "offset": 0
}
```

#### 카테고리 목록
```http
GET /api/v1/companies/categories
```

### 3. 🎯 챌린지 API (`/api/challenges`)

#### 챌린지 목록 조회
```http
GET /api/challenges
```

**응답 예시:**
```json
{
  "challenges": [
    {
      "id": 1,
      "title": "2020년 코로나 급락장 챌린지",
      "description": "2020년 3월 코로나19로 인한 급락장에서 생존하기",
      "difficulty": "BEGINNER",
      "status": "ACTIVE",
      "initialBalance": 1000000.00,
      "durationDays": 30,
      "startDate": "2024-01-01T09:00:00",
      "endDate": "2024-02-01T08:59:59"
    }
  ],
  "totalCount": 12,
  "page": 0,
  "size": 10
}
```

#### 챌린지 시작
```http
POST /api/challenges/{challengeId}/start?forceRestart=false
Authorization: Bearer <token>
```

**응답 예시:**
```json
{
  "sessionId": 123,
  "challengeId": 1,
  "status": "READY",
  "initialBalance": 1000000.00,
  "currentBalance": 1000000.00,
  "startedAt": "2024-01-15T10:30:00"
}
```

### 4. 💼 챌린지 세션 API (`/api/sessions`)

#### 주문 실행
```http
POST /api/sessions/{sessionId}/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "symbol": "005930",
  "orderType": "BUY",
  "quantity": 10,
  "price": 75000.0,
  "orderMethod": "MARKET"
}
```

**응답 예시:**
```json
{
  "orderId": 456,
  "status": "EXECUTED",
  "executedPrice": 75100.0,
  "executedQuantity": 10,
  "totalAmount": 751000.0,
  "commission": 1877.5,
  "slippage": 100.0,
  "executedAt": "2024-01-15T10:35:00"
}
```

#### 주문 내역 조회
```http
GET /api/sessions/{sessionId}/orders
Authorization: Bearer <token>
```

#### 챌린지 종료
```http
POST /api/sessions/{sessionId}/close
Authorization: Bearer <token>
```

### 5. 🏆 리더보드 API (`/api/challenges/{challengeId}/leaderboard`)

#### 리더보드 조회
```http
GET /api/challenges/{challengeId}/leaderboard
```

#### 리더보드 계산 (관리자)
```http
POST /api/challenges/{challengeId}/leaderboard/calculate
Authorization: Bearer <admin-token>
```

### 6. 📊 고급 분석 API

#### VaR 계산
```http
POST /api/v1/risk/portfolios/{portfolioId}/var
Authorization: Bearer <token>
Content-Type: application/json

{
  "confidenceLevel": 0.95,
  "timeHorizon": 1,
  "method": "HISTORICAL_SIMULATION"
}
```

#### 포트폴리오 최적화
```http
POST /api/v1/ml/portfolio-optimization/{portfolioId}/optimize
Authorization: Bearer <token>
Content-Type: application/json

{
  "optimizationType": "MEAN_VARIANCE",
  "targetReturn": 0.12,
  "constraints": {
    "maxWeight": 0.3,
    "minWeight": 0.05
  }
}
```

#### ML 트레이딩 신호
```http
GET /api/v1/ml/signals/generate/005930
Authorization: Bearer <token>
```

#### DCA 시뮬레이션
```http
POST /api/v1/dca/simulate
Content-Type: application/json

{
  "symbol": "005930",
  "monthlyAmount": 100000,
  "startDate": "2020-01-01",
  "endDate": "2023-12-31",
  "frequency": "MONTHLY"
}
```

### 7. 🛠 관리자 API (`/api/admin`)

#### 챌린지 관리
```http
GET /api/admin/challenges?title=코로나&difficulty=BEGINNER&page=0&size=10
Authorization: Bearer <admin-token>
```

```http
POST /api/admin/challenges
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "title": "새로운 챌린지",
  "description": "챌린지 설명",
  "difficulty": "INTERMEDIATE",
  "initialBalance": 5000000,
  "durationDays": 90,
  "startDate": "2024-06-01T09:00:00",
  "endDate": "2024-08-30T18:00:00",
  "createdBy": 1
}
```

#### 회사 데이터 동기화
```http
POST /api/v1/companies/sync/005930
Authorization: Bearer <admin-token>
```

```http
POST /api/v1/companies/sync/all
Authorization: Bearer <admin-token>
```

## 🔧 에러 처리

### 표준 에러 응답 형식
```json
{
  "timestamp": "2024-01-15T10:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "잘못된 요청입니다. 입력값을 확인해주세요.",
  "path": "/api/challenges/999/start",
  "traceId": "abc123def456"
}
```

### 주요 HTTP 상태 코드
- `200`: 성공
- `201`: 생성 성공
- `400`: 잘못된 요청
- `401`: 인증 필요
- `403`: 권한 없음
- `404`: 리소스 없음
- `500`: 서버 내부 오류

## 🔄 실시간 기능 (WebSocket)

### 연결 엔드포인트
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, function(frame) {
  console.log('Connected: ' + frame);
});
```

### 주요 구독 토픽
- `/topic/market-data/{symbol}`: 실시간 시장 데이터
- `/topic/portfolio/{portfolioId}`: 포트폴리오 업데이트
- `/topic/orders/{sessionId}`: 주문 실행 알림
- `/topic/risk-alerts/{portfolioId}`: 리스크 알림

## 🎯 프론트엔드 개발 팁

### 1. API 클라이언트 생성
OpenAPI 스펙을 사용하여 자동으로 클라이언트 코드를 생성할 수 있습니다:

```bash
# TypeScript 클라이언트 생성
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/api-docs \
  -g typescript-axios \
  -o ./src/generated/api
```

### 2. 인증 토큰 관리
```javascript
// localStorage에 토큰 저장
localStorage.setItem('accessToken', response.data.accessToken);
localStorage.setItem('refreshToken', response.data.refreshToken);

// Axios 인터셉터로 자동 토큰 첨부
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 3. 에러 처리
```javascript
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      // 토큰 갱신 시도
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/api/auth/refresh', {
          refreshToken
        });
        localStorage.setItem('accessToken', response.data.accessToken);
        // 원래 요청 재시도
        return axios.request(error.config);
      } catch (refreshError) {
        // 로그인 페이지로 리다이렉트
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

### 4. 환경별 설정
```javascript
const API_BASE_URL = {
  development: 'http://localhost:8080',
  production: 'https://api.stockquest.com'
}[process.env.NODE_ENV || 'development'];
```

## 📈 성능 최적화

### 1. 페이지네이션 활용
많은 데이터를 다룰 때는 항상 페이지네이션을 사용하세요:
```javascript
const fetchChallenges = async (page = 0, size = 20) => {
  const response = await axios.get(`/api/challenges?page=${page}&size=${size}`);
  return response.data;
};
```

### 2. 캐싱 전략
정적 데이터(회사 정보, 카테고리 등)는 클라이언트에서 캐싱하세요:
```javascript
// React Query 예시
const { data: companies } = useQuery(
  ['companies', 'top'],
  () => fetchTopCompanies(),
  {
    staleTime: 5 * 60 * 1000, // 5분
    cacheTime: 10 * 60 * 1000 // 10분
  }
);
```

### 3. WebSocket 연결 관리
WebSocket 연결은 필요할 때만 생성하고 정리하세요:
```javascript
useEffect(() => {
  const socket = new SockJS('/ws');
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/market-data/005930', (message) => {
      setMarketData(JSON.parse(message.body));
    });
  });

  return () => {
    stompClient.disconnect();
  };
}, []);
```

## 🔍 디버깅

### 개발 도구
1. **Swagger UI**: API 테스트 및 문서 확인
2. **브라우저 DevTools**: 네트워크 탭에서 API 호출 모니터링
3. **Postman**: API 테스트 및 자동화

### 로그 확인
백엔드 로그에서 `traceId`를 사용하여 특정 요청을 추적할 수 있습니다.

## 📞 지원

### 개발 관련 문의
- GitHub Issues: [Repository Issues](https://github.com/your-repo/issues)
- 이메일: dev@stockquest.com

### API 버전 관리
현재 API 버전: `v1.0.0`

새로운 기능이나 변경사항은 별도 공지를 통해 안내됩니다.

---

이 가이드가 프론트엔드 개발에 도움이 되기를 바랍니다. 추가 질문이나 개선사항이 있으면 언제든 연락해 주세요! 🚀