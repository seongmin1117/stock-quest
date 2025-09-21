# 📚 StockQuest API 개발 가이드 (프론트엔드용)

## 🚀 빠른 시작

### 1. API 문서 확인
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **완전한 API 스펙**: `backend/docs/openapi-complete.json`

### 2. TypeScript 클라이언트 자동 생성
```bash
# 프론트엔드 프로젝트에서 실행
npm run generate-api
```

## 🔐 인증 시스템

### JWT Bearer 토큰 인증
모든 보호된 엔드포인트는 Authorization 헤더가 필요합니다:
```typescript
headers: {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
}
```

### 인증 플로우
```typescript
// 1. 로그인
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password"
}

// 2. 응답에서 토큰 획득
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": { ... }
}

// 3. 토큰 갱신
POST /api/auth/refresh
{
  "refreshToken": "eyJ..."
}

// 4. 로그아웃
POST /api/auth/logout
```

## 📋 핵심 API 그룹

### 🔑 인증 API
```
POST /api/auth/login      - 로그인
POST /api/auth/signup     - 회원가입
POST /api/auth/refresh    - 토큰 갱신
POST /api/auth/logout     - 로그아웃
GET  /api/auth/me         - 사용자 정보 조회
```

### 🏆 챌린지 API
```
GET  /api/challenges                        - 챌린지 목록 (페이지네이션)
GET  /api/challenges/active                 - 활성 챌린지 목록
GET  /api/challenges/{challengeId}          - 챌린지 상세 정보
POST /api/challenges/{challengeId}/start    - 챌린지 시작 (forceRestart 옵션)
GET  /api/challenges/{challengeId}/leaderboard - 리더보드 조회
GET  /api/challenges/{challengeId}/instruments - 챌린지 상품 목록
```

### 📊 대시보드 API
```
GET  /api/dashboard    - 사용자 대시보드 데이터
```

**응답 예시**:
```json
{
  "totalSessions": 5,
  "activeSessions": 1,
  "completedSessions": 4,
  "averageReturn": 15.2,
  "bestReturn": 45.8,
  "worstReturn": -12.3,
  "totalReturn": 76.0,
  "winRate": 80.0
}
```

### 🎮 챌린지 세션 API
```
GET  /api/sessions/{sessionId}              - 세션 정보
GET  /api/sessions/{sessionId}/portfolio    - 포트폴리오 조회
POST /api/sessions/{sessionId}/orders       - 주문 실행
GET  /api/sessions/{sessionId}/orders       - 주문 내역
POST /api/sessions/{sessionId}/close        - 챌린지 종료
```

**주문 실행 예시**:
```json
POST /api/sessions/123/orders
{
  "instrumentKey": "005930",
  "orderType": "BUY",
  "quantity": 10,
  "price": 75000,
  "orderMode": "MARKET"
}
```

### 🏢 회사 정보 API
```
GET  /api/v1/companies/{symbol}             - 회사 상세 정보
GET  /api/v1/companies/top                  - 인기 회사 목록
GET  /api/v1/companies/categories           - 카테고리 목록
POST /api/v1/companies/search               - 회사 검색
```

**회사 검색 예시**:
```json
POST /api/v1/companies/search
{
  "query": "삼성",
  "categories": ["TECHNOLOGY"],
  "sector": "전자",
  "minPopularity": 50,
  "limit": 20
}
```

## 🔬 고급 분석 API

### 📈 DCA 시뮬레이션
```
POST /api/v1/dca/simulate
{
  "symbol": "005930",
  "monthlyInvestmentAmount": 100000,
  "startDate": "2023-01-01",
  "endDate": "2024-01-01",
  "frequency": "MONTHLY"
}
```

### 🤖 ML 포트폴리오 최적화
```
POST /api/v1/ml/portfolio-optimization/{portfolioId}/optimize
POST /api/v1/ml/portfolio-optimization/{portfolioId}/efficient-frontier
POST /api/v1/ml/portfolio-optimization/{portfolioId}/backtest
GET  /api/v1/ml/portfolio-optimization/{portfolioId}/rebalancing-suggestions
```

### 📊 백테스팅
```
POST /api/v1/backtesting/run
POST /api/v1/backtesting/compare
GET  /api/v1/backtesting/results/{backtestId}
```

### ⚡ 리스크 관리
```
POST /api/v1/risk/portfolios/{portfolioId}/var
POST /api/v1/risk/portfolios/{portfolioId}/stress-test
GET  /api/v1/risk/dashboard
GET  /api/v1/risk/alerts
```

## 🛠️ 개발 팁

### 1. 에러 핸들링
모든 API는 일관된 에러 응답 형식을 사용합니다:
```json
{
  "timestamp": "2025-09-21T12:00:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "유효하지 않은 요청입니다",
  "path": "/api/challenges/123/start",
  "traceId": "abc123"
}
```

### 2. 페이지네이션
페이지네이션이 지원되는 엔드포인트는 다음 파라미터를 사용합니다:
```typescript
{
  page: number,     // 페이지 번호 (0부터 시작)
  size: number,     // 페이지 크기 (기본값: 10)
}
```

### 3. 한국어 지원
- 모든 API는 UTF-8 인코딩을 사용합니다
- 한국 회사명과 카테고리명이 완전 지원됩니다
- 에러 메시지도 한국어로 제공됩니다

### 4. 실시간 데이터
WebSocket 연결을 통한 실시간 업데이트:
- 포트폴리오 변동사항
- 시장 데이터 스트리밍
- 주문 체결 알림

## 📚 참고 문서

### 1. **주요 참고 문서**
- `backend/docs/openapi-complete.json` - 완전한 API 스펙
- `backend/docs/API-GUIDE-FOR-FRONTEND.md` - 이 문서
- `backend/CLAUDE.md` - 백엔드 모듈 개발 가이드

### 2. **개발 환경**
- **백엔드 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 문서**: http://localhost:8080/api-docs

### 3. **TypeScript 자동 생성**
프론트엔드 프로젝트의 `package.json`에서:
```bash
npm run generate-api
```

## 🔄 개발 워크플로우

### 1. API 스펙 확인
1. Swagger UI에서 엔드포인트 탐색
2. 요청/응답 스키마 확인
3. 예시 요청으로 테스트

### 2. TypeScript 클라이언트 생성
1. `npm run generate-api` 실행
2. 자동 생성된 API 클라이언트 활용
3. 타입 안전성 확보

### 3. 에러 처리 구현
1. HTTP 상태 코드 기반 분기
2. 에러 메시지 사용자 친화적 표시
3. 인증 만료 시 자동 갱신

### 4. 실시간 기능 연동
1. WebSocket 연결 설정
2. 실시간 데이터 구독
3. UI 자동 업데이트

## 🚨 주의사항

### 1. 보안
- JWT 토큰을 안전하게 저장 (httpOnly 쿠키 권장)
- 민감한 정보는 로컬 스토리지 저장 금지
- CORS 설정 확인 (현재 localhost:3000 허용)

### 2. 성능
- API 응답 캐싱 활용
- 불필요한 요청 최소화
- 페이지네이션 적절히 활용

### 3. 사용자 경험
- 로딩 상태 표시
- 에러 상황 명확한 안내
- 실시간 피드백 제공

---

📞 **문의사항**: 백엔드 API 관련 문의는 StockQuest Team (team@stockquest.com)으로 연락주세요.