# StockQuest - 모의 투자 챌린지 학습 플랫폼 📈

<div align="center">
  
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14.2.5-blue.svg)](https://nextjs.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.5.4-blue.svg)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

과거 시장 데이터를 활용한 모의 투자 챌린지 플랫폼입니다. 실제 역사적 주식 데이터를 빠르게 재생하여 안전한 환경에서 투자 경험을 쌓을 수 있습니다.

## 📊 프로젝트 상태
- **개발 단계**: Alpha (v0.1.0)
- **백엔드**: Java 21 + Spring Boot 3.5.0 + Hexagonal Architecture
- **프론트엔드**: Next.js 14 + React 18 + TypeScript + Feature-Sliced Design
- **데이터베이스**: MySQL 8.0 + Redis
- **인프라**: Docker Compose 지원

## 🎯 주요 기능

### 📈 챌린지 시스템
- **과거 데이터 재생**: 실제 역사적 시장 데이터를 10-100배속으로 압축 재생
- **회사명 숨김**: 챌린지 진행 중에는 회사명이 'A, B, C'로 표시되어 편견 없는 투자 연습
- **결과 공개**: 챌린지 종료 후 실제 회사명과 티커 공개
- **시드머니**: 기본 100만원의 가상 자금으로 안전한 투자 연습

### 💰 거래 시스템
- **실시간 주문**: 시장가/지정가 주문 지원
- **슬리피지 시뮬레이션**: 실제 거래와 유사한 1-2% 슬리피지 적용
- **포트폴리오 관리**: 실시간 포지션 추적 및 손익 계산

### 🏆 경쟁 & 커뮤니티
- **실시간 리더보드**: 수익률 기준 순위 경쟁
- **커뮤니티 게시판**: 투자 전략 공유 및 토론
- **성과 분석**: 상세한 거래 내역 및 수익률 분석

### 🏦 안전 자산 비교
- **예금 상품**: 3-3.5% 연이율 단기/중기 예금
- **채권 상품**: 4-6% 연이율 국고채/회사채
- **위험 대비 수익**: 주식 투자 결과와 안전 자산 수익률 비교 학습

## 🏗️ 시스템 아키텍처

### 전체 아키텍처 다이어그램
```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (Next.js)                       │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌──────────┐ │
│  │   Pages    │  │  Features  │  │  Entities  │  │  Shared  │ │
│  │            │  │            │  │            │  │          │ │
│  │ App Router │  │ Trading    │  │ User       │  │ API      │ │
│  │ Auth       │  │ Portfolio  │  │ Challenge  │  │ UI       │ │
│  │ Challenges │  │ Market     │  │ Order      │  │ Store    │ │
│  └────────────┘  └────────────┘  └────────────┘  └──────────┘ │
└─────────────────────────────┬───────────────────────────────────┘
                              │ REST API / WebSocket
┌─────────────────────────────┴───────────────────────────────────┐
│                     Backend (Spring Boot)                        │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Adapter Layer (in/out)                  │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │   REST   │  │    JPA   │  │   JWT    │  │  Yahoo   │  │ │
│  │  │Controller│  │Repository│  │  Auth    │  │ Finance  │  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                  Application Layer (Use Cases)             │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │   Auth   │  │Challenge │  │  Order   │  │Community │  │ │
│  │  │ Service  │  │ Service  │  │ Service  │  │ Service  │  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              Domain Layer (Pure Business Logic)            │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │   User   │  │Challenge │  │  Order   │  │Portfolio │  │ │
│  │  │  Entity  │  │  Entity  │  │  Entity  │  │  Entity  │  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────┬───────────────────────────────────┘
                              │
          ┌───────────────────┴──────────────────┐
          │            Data Layer                │
          │  ┌────────────┐  ┌────────────┐     │
          │  │   MySQL    │  │   Redis    │     │
          │  │            │  │            │     │
          │  │ Persistent │  │   Cache    │     │
          │  │   Data     │  │Leaderboard │     │
          │  └────────────┘  └────────────┘     │
          └──────────────────────────────────────┘
```

## 🏗️ 기술 스택

### 백엔드 (Spring Boot + Hexagonal Architecture)
```
📦 backend/
├── 📁 src/main/java/com/stockquest/
│   ├── 📁 domain/              # 순수 도메인 로직 (Spring 의존성 없음)
│   │   ├── 📁 user/            # 사용자 도메인
│   │   ├── 📁 challenge/       # 챌린지 도메인  
│   │   ├── 📁 session/         # 세션 도메인
│   │   ├── 📁 order/           # 주문 도메인
│   │   ├── 📁 portfolio/       # 포트폴리오 도메인
│   │   ├── 📁 market/          # 시장 데이터 도메인
│   │   └── 📁 community/       # 커뮤니티 도메인
│   ├── 📁 application/         # 애플리케이션 서비스 (유스케이스)
│   │   ├── 📁 auth/            # 인증 서비스
│   │   ├── 📁 challenge/       # 챌린지 서비스
│   │   ├── 📁 order/           # 주문 서비스
│   │   └── 📁 common/          # 공통 서비스
│   ├── 📁 adapter/             # 어댑터 구현체
│   │   ├── 📁 in/web/          # REST API 컨트롤러
│   │   ├── 📁 out/persistence/ # JPA 저장소 구현
│   │   ├── 📁 out/auth/        # JWT, 암호화 구현
│   │   └── 📁 out/marketdata/  # Yahoo Finance 클라이언트
│   └── 📁 config/              # 설정 클래스들
└── 📁 src/main/resources/
    ├── 📁 db/migration/        # Flyway DB 마이그레이션
    └── 📄 application.yml      # 애플리케이션 설정
```

**주요 기술**:
- Java 17 + Spring Boot 3.5.x
- MySQL 8.0 (Flyway 마이그레이션)
- Redis (캐싱 & 리더보드)
- JWT 인증
- OpenAPI 3.0 + Swagger UI

### 프론트엔드 (Next.js + Feature-Sliced Design)
```
📦 frontend/
├── 📁 src/
│   ├── 📁 app/                 # Next.js 14 App Router
│   │   ├── 📁 auth/            # 인증 페이지들
│   │   ├── 📁 challenges/      # 챌린지 관련 페이지
│   │   └── 📄 layout.tsx       # 루트 레이아웃
│   ├── 📁 shared/              # 공유 리소스
│   │   ├── 📁 api/             # API 클라이언트 & 모킹
│   │   ├── 📁 ui/              # 공통 UI 컴포넌트
│   │   └── 📁 lib/             # 유틸리티 & 스토어
│   ├── 📁 entities/            # 비즈니스 엔티티
│   ├── 📁 features/            # 기능별 모듈
│   │   └── 📁 place-order/     # 주문 접수 기능
│   └── 📁 widgets/             # 복합 위젯
│       ├── 📁 portfolio/       # 포트폴리오 패널
│       ├── 📁 leaderboard/     # 리더보드 패널
│       └── 📁 market-data/     # 시장 데이터 패널
└── 📁 tests/e2e/              # Playwright E2E 테스트
```

**주요 기술**:
- Next.js 14 (App Router) + React 18 + TypeScript
- Material-UI (MUI) 컴포넌트
- TanStack Query (서버 상태 관리)
- Zustand (클라이언트 상태 관리)
- MSW (Mock Service Worker) - API 모킹
- Orval - OpenAPI 기반 타입/훅 자동 생성

## 🚀 빠른 시작

### 사전 요구사항
- **Java 21** 이상
- **Node.js 18.x** 이상 + **pnpm**
- **Docker** & **Docker Compose**
- **MySQL 8.0** (Docker로 실행)
- **Redis** (Docker로 실행)

### 전체 시스템 실행 (권장)

1. **저장소 클론 및 의존성 설치**
   ```bash
   git clone https://github.com/yourusername/stock-quest.git
   cd stock-quest
   
   # 백엔드 의존성 (Gradle)
   cd backend && ./gradlew build
   
   # 프론트엔드 의존성 (pnpm 설치 필요: npm install -g pnpm)
   cd ../frontend && pnpm install
   ```

2. **데이터베이스 및 캐시 실행**
   ```bash
   # 프로젝트 루트에서
   docker-compose up mysql redis -d
   ```

3. **백엔드 실행**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

4. **프론트엔드 실행 (별도 터미널)**
   ```bash
   cd frontend
   pnpm dev
   ```

5. **접속**
   - 프론트엔드: http://localhost:3000
   - 백엔드 API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

### 프론트엔드만 실행 (모킹 모드)

백엔드 없이 프론트엔드만 개발하고 싶다면:

```bash
cd frontend
NEXT_PUBLIC_MOCK_API=true pnpm dev
```

MSW(Mock Service Worker)가 자동으로 API를 모킹하여 독립적인 개발이 가능합니다.

## 📋 개발 가이드

### 백엔드 개발

#### 헥사고날 아키텍처 규칙
1. **Domain Layer**: 순수 비즈니스 로직, Spring 의존성 금지
2. **Application Layer**: 유스케이스 구현, 트랜잭션 경계
3. **Adapter Layer**: 외부 시스템 연동 (DB, API, 웹)

#### 새로운 기능 추가 절차
1. `domain/` 에서 엔티티 및 포트 인터페이스 정의
2. `application/` 에서 유스케이스 구현
3. `adapter/` 에서 구체적인 구현체 작성
4. `config/` 에서 빈 등록 및 설정

#### 데이터베이스 변경
```bash
# 새 마이그레이션 파일 생성
touch src/main/resources/db/migration/V10__Add_new_feature.sql

# 마이그레이션 실행
./gradlew flywayMigrate
```

### 프론트엔드 개발

#### Feature-Sliced Design 구조
- `shared/`: 재사용 가능한 공통 모듈
- `entities/`: 비즈니스 엔티티 (user, challenge 등)
- `features/`: 사용자 기능 (place-order, start-challenge 등)
- `widgets/`: 복합 UI 블록 (portfolio, leaderboard 등)
- `app/`: 페이지 및 라우팅

#### API 개발 워크플로
1. `docs/openapi.yml` 에서 API 스펙 정의
2. `pnpm generate-api` 로 TypeScript 타입/훅 생성
3. MSW 핸들러로 모킹 구현
4. 프론트엔드 개발
5. 백엔드 구현 후 실제 API로 전환

#### 새 컴포넌트 추가
```bash
# 예: 새로운 기능 추가
mkdir -p src/features/new-feature
touch src/features/new-feature/NewFeature.tsx
touch src/features/new-feature/index.ts
```

## 🧪 테스트

### 단위 테스트
```bash
# 백엔드 테스트
cd backend && ./gradlew test

# 프론트엔드 테스트
cd frontend && pnpm test
```

### E2E 테스트
```bash
cd frontend

# 테스트 실행 (헤드리스)
pnpm test:e2e

# 테스트 UI로 실행
pnpm test:e2e:ui
```

### 테스트 시나리오
- ✅ 회원가입 → 로그인 → 챌린지 시작 → 주문 → 종료 → 티커 공개
- ✅ 포트폴리오 실시간 업데이트
- ✅ 리더보드 순위 확인
- ✅ 주문 실패 케이스 (잔고 부족 등)

## 🔧 설정 및 환경 변수

### 백엔드 환경 변수 (application.yml)
```yaml
# 데이터베이스
spring.datasource.url: jdbc:mysql://localhost:3306/stockquest
spring.datasource.username: stockquest
spring.datasource.password: stockquest123

# Redis
spring.data.redis.host: localhost
spring.data.redis.port: 6379

# JWT
jwt.secret: your-secret-key
jwt.expiration: 86400000

# Yahoo Finance API
yahoo-finance.base-url: https://query1.finance.yahoo.com
yahoo-finance.timeout: 5000

# 챌린지 설정
stockquest.challenge.max-speed-factor: 100
stockquest.challenge.default-seed-balance: 1000000
```

### 프론트엔드 환경 변수 (.env.local)
```bash
# API 서버 주소
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# 모킹 활성화 여부 (개발 시 true)
NEXT_PUBLIC_MOCK_API=true
```

## 📊 데이터베이스 스키마

### 핵심 테이블 구조

#### 사용자 (user)
- 이메일, 암호화된 비밀번호, 닉네임
- 고유 제약조건: email, nickname

#### 챌린지 (challenge)
- 제목, 설명, 시뮬레이션 기간, 속도 배율
- 상태: DRAFT, ACTIVE, COMPLETED

#### 챌린지 상품 (challenge_instrument)  
- 챌린지별 상품 정보
- 실제 티커와 숨겨진 이름 매핑
- 타입: STOCK, DEPOSIT, BOND

#### 세션 (challenge_session)
- 사용자의 챌린지 참여 세션
- 시드머니, 현재 잔고, 상태 관리
- 고유 제약조건: (challenge_id, user_id)

#### 주문 (order_history)
- 모든 거래 주문 기록
- 주문 타입, 체결가, 슬리피지 정보

#### 포지션 (portfolio_position)
- 현재 보유 포지션
- 평균 매입가, 수량, 총 비용

#### 시장 데이터 (price_candle)
- OHLC 가격 데이터
- Yahoo Finance에서 수집한 역사적 데이터

## 🔌 API 명세

### 인증 API
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인

### 챌린지 API  
- `GET /api/challenges` - 챌린지 목록 조회
- `POST /api/challenges/{id}/start` - 챌린지 시작
- `GET /api/challenges/{id}/instruments` - 챌린지 상품 목록 (숨겨진 이름)

### 거래 API
- `POST /api/sessions/{sessionId}/orders` - 주문 접수
- `GET /api/sessions/{sessionId}/portfolio` - 포트폴리오 조회
- `POST /api/sessions/{sessionId}/close` - 챌린지 종료

### 리더보드 API
- `GET /api/challenges/{id}/leaderboard` - 리더보드 조회

### 커뮤니티 API
- `GET/POST /api/challenges/{id}/posts` - 게시글 조회/작성

**전체 API 문서**: http://localhost:8080/swagger-ui.html

## 📱 사용자 가이드

### 1단계: 회원가입 및 로그인
1. 메인 페이지에서 '회원가입' 클릭
2. 이메일, 비밀번호(8자 이상), 닉네임 입력
3. 자동 로그인 후 챌린지 페이지로 이동

### 2단계: 챌린지 선택 및 시작
1. 원하는 챌린지 선택 (진행중 상태만 참여 가능)
2. '챌린지 시작' 버튼 클릭
3. 100만원 시드머니와 함께 세션 생성

### 3단계: 거래 실행
1. **상품 분석**: 차트와 지표 분석 (회사명은 숨겨짐)
2. **주문 접수**: 매수/매도, 수량, 주문 타입 선택
3. **포트폴리오 관리**: 실시간 손익 및 포지션 확인

### 4단계: 챌린지 종료 및 결과 확인
1. '챌린지 종료하기' 버튼 클릭
2. 최종 손익 및 수익률 확인
3. **티커 공개**: 실제 회사명과 티커 공개
4. 리더보드에서 순위 확인

## 🔧 운영 및 배포

### 로컬 개발 환경
```bash
# 데이터베이스 시작
docker-compose up mysql redis -d

# 백엔드 실행
cd backend && ./gradlew bootRun

# 프론트엔드 실행 
cd frontend && pnpm dev
```

### 전체 시스템 배포
```bash
# 전체 스택 실행 (운영 모드)
docker-compose --profile backend --profile frontend up -d
```

### 데이터베이스 관리
```bash
# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 정보 확인
./gradlew flywayInfo

# 마이그레이션 롤백 (주의!)
./gradlew flywayClean
```

## 🎮 사용 예시

### 개발 환경 테스트 계정
- **이메일**: test@example.com  
- **비밀번호**: password123
- **닉네임**: 테스트사용자

### 샘플 챌린지 시나리오
1. **2020년 코로나 급락장**: Apple, Microsoft, Google 등 빅테크 주식의 급락과 회복
2. **2021년 밈스톡 광풍**: GameStop, AMC 등 밈스톡의 극심한 변동성
3. **2022년 금리 인상**: 연준 금리 인상으로 인한 성장주 조정

### 투자 전략 예시
- **장기 투자**: 우량 대형주 매수 후 보유
- **단기 매매**: 변동성을 이용한 스윙 트레이딩  
- **안전 투자**: 예금/채권과 주식 투자 비교
- **위험 관리**: 포지션 크기 조절 및 손절매

## 🛠️ 문제 해결

### 자주 발생하는 문제

#### "데이터베이스 연결 실패"
```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# MySQL 재시작
docker-compose restart mysql

# 로그 확인
docker-compose logs mysql
```

#### "포트 이미 사용 중" 오류
```bash
# 포트 사용 프로세스 확인
lsof -i :3000  # 프론트엔드
lsof -i :8080  # 백엔드  
lsof -i :3306  # MySQL

# 프로세스 종료
kill -9 <PID>
```

#### "JWT 토큰 오류"
- 브라우저 로컬 스토리지에서 'auth-token' 키 삭제
- 다시 로그인 시도

#### "Flyway 마이그레이션 실패"
```bash
# 마이그레이션 상태 확인
./gradlew flywayInfo

# 실패한 마이그레이션 복구
./gradlew flywayRepair
```

### 개발 도구

#### 데이터베이스 접속
```bash
# MySQL 컨테이너 접속
docker exec -it stockquest-mysql mysql -u stockquest -p stockquest

# 주요 테이블 확인
SHOW TABLES;
DESCRIBE user;
SELECT * FROM challenge;
```

#### Redis 접속
```bash
# Redis 컨테이너 접속
docker exec -it stockquest-redis redis-cli

# 캐시 데이터 확인
KEYS *
GET leaderboard:challenge:1
```

## 🚀 프로덕션 배포

### 환경 설정 체크리스트
- [ ] JWT 시크릿 키 변경
- [ ] 데이터베이스 비밀번호 변경
- [ ] CORS 도메인 설정
- [ ] SSL/TLS 인증서 설정
- [ ] 로깅 및 모니터링 설정

### 성능 최적화
- **데이터베이스**: 인덱스 최적화, 커넥션 풀 튜닝
- **캐시**: Redis 클러스터링, 만료 정책
- **프론트엔드**: 번들 크기 최적화, 이미지 압축
- **API**: 응답 압축, 페이지네이션

## 🔒 보안 고려사항

### 환경 변수 보안
- ⚠️ `.env` 파일은 절대 커밋하지 마세요
- JWT 시크릿 키는 최소 32자 이상의 랜덤 문자열 사용
- 프로덕션 환경에서는 환경 변수 관리 서비스 사용 권장

### API 보안
- **Rate Limiting**: Bucket4j를 통한 요청 제한 구현
- **CORS**: 허용된 도메인만 접근 가능하도록 설정
- **JWT 토큰**: 만료 시간 설정 및 Refresh Token 구현
- **SQL Injection 방지**: JPA/Hibernate 파라미터 바인딩 사용

### 데이터 보안
- 비밀번호는 BCrypt로 암호화
- 민감한 정보는 Redis에 임시 저장 후 자동 삭제
- 로그에 민감한 정보 노출 방지

## 🤝 기여 가이드

### 개발 프로세스
1. **Issue 생성**: 버그 리포트나 기능 제안을 Issue로 등록
2. **Fork & Clone**: 저장소를 Fork하고 로컬에 Clone
3. **Branch 생성**: `feature/기능명` 또는 `fix/버그명` 형식
4. **개발 & 테스트**: 코드 작성 및 테스트 실행
5. **Commit**: [Conventional Commits](https://www.conventionalcommits.org/) 규칙 따르기
6. **Pull Request**: 상세한 설명과 함께 PR 생성

### 커밋 메시지 규칙
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅, 세미콜론 누락 등
refactor: 코드 리팩토링
test: 테스트 코드 추가
chore: 빌드 업무, 패키지 매니저 설정 등
```

### 코드 스타일
- **Java**: IntelliJ IDEA 기본 포맷터 사용
- **TypeScript**: Prettier 설정 적용
- **테스트 커버리지**: 최소 80% 유지

## 🤖 AI 개발 지원 (Claude)

이 프로젝트는 **Claude AI와의 협업 개발**을 위한 컨텍스트 관리 시스템을 포함합니다.

### Claude 설정 디렉토리
```
.claude/
├── AUTOSTART.md              # 작업 시작 시 필수 읽기
├── PROJECT_CONTEXT.md        # 프로젝트 전체 맥락
├── ARCHITECTURE_RULES.md     # 헥사고날 아키텍처 규칙  
├── DEVELOPMENT_PATTERNS.md   # 개발 패턴과 템플릿
├── CURRENT_STATE.md          # 현재 프로젝트 상태
└── REFRESH_CONTEXT.md        # 컨텍스트 갱신 방법
```

### Claude 사용 가이드
Claude와 작업할 때는 다음 명령어를 사용하세요:
- `--refresh` : 전체 컨텍스트 갱신
- `--status` : 현재 상태 요약
- `--check` : 아키텍처 규칙 준수 확인

이를 통해 **일관된 코드 품질**과 **아키텍처 무결성**을 보장합니다.

## 📞 지원 및 문의

### 문의사항
- **이슈 등록**: [GitHub Issues](https://github.com/seongmin1117/stock-quest/issues)
- **디스커션**: [GitHub Discussions](https://github.com/seongmin1117/stock-quest/discussions)
- **이메일**: seongmin1117@gmail.com

### 유용한 링크
- [프로젝트 위키](https://github.com/seongmin1117/stock-quest/wiki)
- [API 문서](http://localhost:8080/swagger-ui.html)
- [개발 로드맵](https://github.com/seongmin1117/stock-quest/projects)

## 📄 라이센스
MIT License - 자유롭게 사용, 수정, 배포 가능합니다. [LICENSE](LICENSE) 파일 참조

---

**StockQuest Team** - 투자 교육을 통한 금융 리터러시 향상을 목표로 합니다 📈