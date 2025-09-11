# StockQuest 아키텍처 가이드

## 🏗️ 전체 시스템 아키텍처

StockQuest는 **헥사고날 아키텍처**(포트 & 어댑터)를 기반으로 한 백엔드와 **Feature-Sliced Design**을 적용한 프론트엔드로 구성됩니다.

```
┌─────────────────────────────────────────────────────────────┐
│                    StockQuest Platform                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Frontend (Next.js 14)          Backend (Spring Boot)      │
│  ┌─────────────────────┐        ┌─────────────────────┐     │
│  │   Feature-Sliced    │◄──────►│   Hexagonal Arch    │     │
│  │      Design         │  HTTP  │   (Ports & Adapters)│     │
│  └─────────────────────┘        └─────────────────────┘     │
│           │                               │                 │
│           ▼                               ▼                 │
│  ┌─────────────────────┐        ┌─────────────────────┐     │
│  │  MSW (Development)  │        │     MySQL + Redis   │     │
│  │   Mock Server       │        │      Database       │     │
│  └─────────────────────┘        └─────────────────────┘     │
│                                           │                 │
│                                           ▼                 │
│                                 ┌─────────────────────┐     │
│                                 │   Yahoo Finance     │     │
│                                 │   External API      │     │
│                                 └─────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## 🔷 백엔드 아키텍처 (Hexagonal Architecture)

### 계층별 책임

#### 1. Domain Layer (도메인 계층)
```
📁 domain/
├── 📁 user/           # 사용자 도메인
├── 📁 challenge/      # 챌린지 도메인
├── 📁 session/        # 세션 도메인
├── 📁 order/          # 주문 도메인
├── 📁 portfolio/      # 포트폴리오 도메인
├── 📁 market/         # 시장 데이터 도메인
└── 📁 community/      # 커뮤니티 도메인
```

**특징**:
- ✅ **순수 비즈니스 로직**: Spring 프레임워크 의존성 없음
- ✅ **도메인 규칙 구현**: 비즈니스 규칙과 제약조건 포함
- ✅ **포트 인터페이스 정의**: 외부 의존성에 대한 추상화

**예시**: `User` 도메인 엔티티
```java
public class User {
    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다");
        }
    }
}
```

#### 2. Application Layer (애플리케이션 계층)
```
📁 application/
├── 📁 auth/           # 인증 서비스
├── 📁 challenge/      # 챌린지 서비스
├── 📁 order/          # 주문 서비스
├── 📁 session/        # 세션 서비스
└── 📁 common/         # 공통 서비스
```

**특징**:
- ✅ **유스케이스 구현**: 비즈니스 시나리오 처리
- ✅ **트랜잭션 경계**: @Transactional 적용
- ✅ **포트 조합**: 도메인 포트들을 조합하여 복잡한 로직 처리

**예시**: 챌린지 시작 유스케이스
```java
@Service
@Transactional
public class StartChallengeService implements StartChallengeUseCase {
    // 포트 의존성 주입
    private final ChallengeRepository challengeRepository;
    private final ChallengeSessionRepository sessionRepository;
    
    // 비즈니스 로직 조합
    public StartChallengeResult start(StartChallengeCommand command) {
        // 1. 챌린지 검증
        // 2. 세션 생성  
        // 3. 결과 반환
    }
}
```

#### 3. Adapter Layer (어댑터 계층)

##### 입력 어댑터 (Inbound Adapters)
```
📁 adapter/in/
├── 📁 web/           # REST API 컨트롤러
└── 📁 scheduler/     # 배치 작업 스케줄러
```

**웹 어댑터 특징**:
- ✅ **DTO 변환**: 도메인 객체 ↔ API DTO 변환
- ✅ **HTTP 프로토콜 처리**: 상태 코드, 헤더, 검증
- ✅ **OpenAPI 문서화**: Swagger 어노테이션

##### 출력 어댑터 (Outbound Adapters)  
```
📁 adapter/out/
├── 📁 persistence/   # JPA 데이터베이스 구현
├── 📁 auth/          # JWT, 암호화 구현
├── 📁 marketdata/    # Yahoo Finance API 클라이언트
└── 📁 cache/         # Redis 캐시 구현
```

**JPA 어댑터 특징**:
- ✅ **엔티티 매핑**: 도메인 객체 ↔ JPA 엔티티 변환
- ✅ **쿼리 최적화**: 인덱스 활용, N+1 문제 방지
- ✅ **트랜잭션 지원**: 데이터 일관성 보장

### 의존성 규칙

```
Domain Layer (순수 Java)
    ↑
Application Layer (Spring Boot)
    ↑  
Adapter Layer (JPA, Redis, etc.)
```

**핵심 원칙**:
- **의존성 역전**: 도메인이 인프라를 모름
- **포트를 통한 소통**: 추상화된 인터페이스 사용
- **테스트 용이성**: 모킹 가능한 포트 설계

## 🎨 프론트엔드 아키텍처 (Feature-Sliced Design)

### FSD 계층 구조

```
📁 src/
├── 📁 app/                    # 페이지 & 라우팅
│   ├── 📁 auth/               # 인증 관련 페이지
│   ├── 📁 challenges/         # 챌린지 관련 페이지
│   ├── 📄 layout.tsx          # 루트 레이아웃
│   └── 📄 page.tsx            # 메인 페이지
│
├── 📁 shared/                 # 공유 리소스 (최하위 계층)
│   ├── 📁 api/                # API 클라이언트 & 타입
│   ├── 📁 ui/                 # 재사용 가능한 UI 컴포넌트
│   ├── 📁 lib/                # 유틸리티 & 공통 로직
│   └── 📁 config/             # 설정 파일들
│
├── 📁 entities/               # 비즈니스 엔티티
│   ├── 📁 user/               # 사용자 엔티티
│   ├── 📁 challenge/          # 챌린지 엔티티
│   └── 📁 portfolio/          # 포트폴리오 엔티티
│
├── 📁 features/               # 사용자 기능
│   ├── 📁 place-order/        # 주문 접수 기능
│   ├── 📁 start-challenge/    # 챌린지 시작 기능
│   └── 📁 reveal-result/      # 결과 공개 기능
│
└── 📁 widgets/                # 복합 UI 블록
    ├── 📁 portfolio/          # 포트폴리오 패널
    ├── 📁 leaderboard/        # 리더보드 패널
    └── 📁 market-data/        # 시장 데이터 패널
```

### 계층별 역할

#### App Layer (앱 계층)
- **페이지 컴포넌트**: 라우팅과 레이아웃
- **전역 설정**: 테마, 프로바이더, 미들웨어

#### Widgets Layer (위젯 계층)
- **복합 UI 블록**: 여러 기능을 조합한 완성형 컴포넌트
- **비즈니스 로직 포함**: 데이터 fetching, 상태 관리

#### Features Layer (기능 계층)
- **사용자 기능**: 특정 사용자 액션 처리
- **독립적 구현**: 다른 feature에 의존하지 않음

#### Entities Layer (엔티티 계층)
- **비즈니스 엔티티**: 도메인 개념의 프론트엔드 표현
- **상태 관리**: 엔티티별 상태 및 액션

#### Shared Layer (공유 계층)
- **공통 모듈**: 모든 계층에서 재사용 가능
- **기술적 관심사**: API, UI 컴포넌트, 유틸리티

### 의존성 규칙 (FSD)

```
App → Widgets → Features → Entities → Shared
```

**핵심 원칙**:
- **하위 계층만 의존**: 상위 계층은 하위 계층을 모름
- **수평 격리**: 같은 계층의 모듈끼리는 의존하지 않음
- **Public API**: index.ts를 통한 명시적 export

## 🔄 데이터 플로우

### 전체 데이터 흐름

```
┌─────────────┐    HTTP/REST    ┌─────────────┐    JPA/JDBC    ┌─────────────┐
│             │◄───────────────►│             │◄──────────────►│             │
│  Frontend   │                 │   Backend   │                │   Database  │
│  (Next.js)  │                 │(Spring Boot)│                │   (MySQL)   │
│             │                 │             │                │             │
└─────────────┘                 └─────────────┘                └─────────────┘
       │                               │                               
       ▼                               ▼                               
┌─────────────┐                 ┌─────────────┐                        
│     MSW     │                 │    Redis    │                        
│ (Mock API)  │                 │   (Cache)   │                        
└─────────────┘                 └─────────────┘                        
                                       │                               
                                       ▼                               
                                ┌─────────────┐                        
                                │Yahoo Finance│                        
                                │ External API│                        
                                └─────────────┘                        
```

### 주요 데이터 흐름 시나리오

#### 1. 챌린지 시작 플로우
```
사용자 클릭 → Frontend → REST API → UseCase → Domain → Repository → Database
                   ↓
              세션 생성 ← Application ← Domain ← JPA Entity ← Database
```

#### 2. 주문 처리 플로우
```
주문 요청 → Controller → UseCase → Domain Service → 
                                      ↓
Portfolio 업데이트 ← Order 생성 ← 가격 조회 ← Market Data
```

#### 3. 실시간 데이터 업데이트
```
Schedule Job → Yahoo Finance API → Price Update → Redis Cache →
                                                      ↓
WebSocket/SSE → Frontend Update ← REST API Polling ←
```

## 🔒 보안 아키텍처

### 인증 & 인가

```
┌─────────────┐    JWT Token    ┌─────────────┐
│  Frontend   │◄───────────────►│   Backend   │
│             │                 │             │
│ localStorage│                 │JWT Validate │
└─────────────┘                 └─────────────┘
                                       │
                                       ▼
                                ┌─────────────┐
                                │  Security   │
                                │   Filter    │
                                └─────────────┘
```

**보안 원칙**:
- ✅ **JWT 기반 인증**: Stateless 인증 방식
- ✅ **CORS 설정**: 허용된 도메인만 접근 가능
- ✅ **비밀번호 암호화**: BCrypt 해싱
- ✅ **SQL Injection 방지**: JPA Prepared Statement
- ✅ **XSS 방지**: 입력값 검증 및 이스케이핑

### API 보안

```yaml
Public Endpoints:
  - POST /api/auth/signup    # 회원가입
  - POST /api/auth/login     # 로그인
  - GET  /swagger-ui.html    # API 문서

Protected Endpoints:
  - ALL /api/challenges/**   # JWT 토큰 필수
  - ALL /api/sessions/**     # JWT 토큰 필수
  - ALL /api/products/**     # JWT 토큰 필수
```

## 📊 데이터 아키텍처

### 데이터베이스 설계 원칙

#### 정규화 및 성능 최적화
```sql
-- 인덱스 전략
user: (email), (nickname)                    # 로그인 및 중복 체크
challenge: (status), (period_start, period_end) # 챌린지 필터링
price_candle: (ticker, date), (ticker, date_range) # 가격 조회
order_history: (session_id), (status)        # 주문 내역
portfolio_position: (session_id, instrument_key) # 포지션 조회
```

#### 데이터 일관성 보장
- **외래 키 제약조건**: 참조 무결성 유지
- **트랜잭션 격리**: READ_COMMITTED 레벨
- **낙관적 락**: 동시성 문제 방지

### 캐시 전략 (Redis)

```
📁 Cache Structure:
├── 🔑 leaderboard:challenge:{id}     # 리더보드 (TTL: 30초)
├── 🔑 prices:ticker:{symbol}         # 최신 가격 (TTL: 5분)  
├── 🔑 session:user:{userId}          # 사용자 세션 (TTL: 24시간)
└── 🔑 instruments:challenge:{id}     # 챌린지 상품 (TTL: 1시간)
```

**캐싱 정책**:
- **Write-Through**: 데이터 변경 시 DB와 캐시 동시 업데이트
- **TTL 관리**: 데이터 특성에 따른 적절한 만료 시간
- **Cache-Aside**: 캐시 미스 시 DB에서 조회 후 캐시 저장

## ⚡ 성능 아키텍처

### 백엔드 성능 최적화

#### 1. 데이터베이스 최적화
```sql
-- 복합 인덱스 활용
CREATE INDEX idx_price_lookup ON price_candle(ticker, date, timeframe);

-- 파티셔닝 (대용량 데이터 시)
PARTITION BY RANGE (YEAR(date)) (
    PARTITION p2020 VALUES LESS THAN (2021),
    PARTITION p2021 VALUES LESS THAN (2022),
    PARTITION p2022 VALUES LESS THAN (2023)
);
```

#### 2. 애플리케이션 레벨 최적화
- **지연 로딩**: JPA @Lazy 적용
- **배치 처리**: 대량 데이터 처리 시 batch insert
- **커넥션 풀**: HikariCP 최적화

#### 3. 캐시 최적화
- **Redis 클러스터링**: 확장성 확보
- **메모리 최적화**: 적절한 데이터 구조 선택
- **캐시 워밍**: 자주 사용되는 데이터 미리 로드

### 프론트엔드 성능 최적화

#### 1. 번들 최적화
```javascript
// Next.js 최적화 설정
const nextConfig = {
  // 트리 쉐이킹
  experimental: {
    optimizePackageImports: ['@mui/material', '@mui/icons-material'],
  },
  
  // 코드 스플리팅
  webpack: (config) => {
    config.optimization.splitChunks.chunks = 'all';
    return config;
  }
};
```

#### 2. 렌더링 최적화
- **React.memo**: 불필요한 리렌더링 방지
- **useMemo/useCallback**: 계산 비용이 높은 로직 메모이제이션
- **가상화**: 대용량 리스트 렌더링 최적화

#### 3. 데이터 페칭 최적화
- **TanStack Query**: 캐싱, 백그라운드 업데이트, 중복 요청 제거
- **Optimistic Updates**: 사용자 경험 향상
- **실시간 업데이트**: WebSocket 또는 폴링

## 🔄 확장성 고려사항

### 수평 확장 (Scale-Out)

#### 백엔드 확장
```yaml
확장 포인트:
  - API 서버: 로드 밸런서 + 멀티 인스턴스
  - 데이터베이스: Read Replica, 샤딩
  - 캐시: Redis Cluster, Sentinel
  - 메시지 큐: RabbitMQ/Kafka (비동기 처리)
```

#### 프론트엔드 확장
```yaml
확장 포인트:
  - CDN: 정적 리소스 배포
  - SSR/ISR: Next.js 렌더링 최적화
  - 마이크로프론트엔드: 기능별 독립 배포
```

### 수직 확장 (Scale-Up)

#### 성능 모니터링 지표
```yaml
백엔드 메트릭:
  - API 응답 시간: < 200ms (P95)
  - 데이터베이스 쿼리: < 100ms (P95)  
  - 메모리 사용량: < 80%
  - CPU 사용량: < 70%

프론트엔드 메트릭:
  - 페이지 로드 시간: < 3초 (3G)
  - FCP (First Contentful Paint): < 1.5초
  - LCP (Largest Contentful Paint): < 2.5초
  - 번들 크기: < 500KB (initial)
```

## 🏭 배포 아키텍처

### 개발 환경
```
Developer Machine:
├── 🐳 Docker Compose (MySQL + Redis)
├── 🌐 Backend (localhost:8080)
└── ⚛️  Frontend (localhost:3000) + MSW
```

### 스테이징 환경  
```
Staging Server:
├── 🐳 Docker Containers
├── 🔄 CI/CD Pipeline (GitHub Actions)
├── 🧪 E2E Tests (Playwright)
└── 📊 Monitoring (Health checks)
```

### 프로덕션 환경
```
Production Infrastructure:
├── ☁️  Cloud Provider (AWS/GCP/Azure)
├── 🚀 Container Orchestration (K8s/ECS)
├── 💾 Managed Database (RDS/Cloud SQL)
├── 🔴 Redis Cluster
├── 📈 Monitoring & Logging
└── 🔒 SSL/TLS + WAF
```

## 🔍 모니터링 & 관찰가능성

### 로깅 전략
```yaml
Backend Logging:
  - Application Logs: Logback + JSON 형식
  - Access Logs: HTTP 요청/응답 추적
  - Error Logs: 예외 스택 트레이스
  - Business Logs: 주요 비즈니스 이벤트

Frontend Logging:
  - User Actions: 사용자 행동 추적
  - API Errors: 네트워크 오류 로깅
  - Performance: Core Web Vitals
```

### 헬스체크
```yaml
Backend Health:
  - /actuator/health: 애플리케이션 상태
  - Database: 커넥션 풀 상태
  - Redis: 캐시 서버 연결 상태
  - External API: Yahoo Finance 연결 상태

Frontend Health:
  - API Connectivity: 백엔드 API 연결 상태
  - Client Performance: 렌더링 성능 지표
  - Error Rate: JavaScript 오류 발생률
```

## 🔮 미래 확장 계획

### Phase 2: 고급 기능
- **실시간 차트**: WebSocket 기반 실시간 캔들차트
- **기술적 분석**: 이동평균, RSI, MACD 등 지표
- **포트폴리오 분석**: 샤프 비율, 최대 낙폭 등 고급 지표
- **소셜 기능**: 팔로우, 투자 전략 공유

### Phase 3: AI 통합
- **AI 투자 조언**: 머신러닝 기반 투자 추천
- **리스크 분석**: 포트폴리오 위험도 자동 분석
- **시장 분석**: 뉴스 감정 분석, 트렌드 예측

### Phase 4: 엔터프라이즈
- **기관 계정**: 학교, 회사 단체 계정
- **커스텀 챌린지**: 사용자 정의 챌린지 생성
- **고급 분석**: 상세한 성과 분석 및 리포트

---

이 아키텍처는 **확장성**, **유지보수성**, **테스트 용이성**을 중점으로 설계되었으며, 각 계층의 독립성을 통해 지속적인 개발과 개선이 가능합니다.