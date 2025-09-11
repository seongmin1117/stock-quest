# Stock Quest 개선 구현을 위한 Claude Code SuperClaude 프롬프트

## 🚀 SuperClaude 활용 가이드

### 초기 설정
```bash
# 프로젝트 컨텍스트 로드 (첫 세션 시작 시)
/load @/Users/seongmin/project/stock-quest --delegate auto --wave-mode auto

# 프로젝트 전체 분석 (아키텍처 이해)
/analyze --ultrathink --seq --persona-architect --focus architecture
```

---

## Phase 1: 보안 강화 구현 (1-2주)

### 1.1 JWT 비밀키 관리 개선
```bash
# JWT 보안 개선 구현
/implement JWT 보안 강화: 256비트 키 생성, 환경변수 관리, Refresh Token 메커니즘 추가 --persona-security --seq --validate --wave-mode

# 단계별 구현
/task JWT security implementation --steps "1. SecureKeyGenerator 유틸리티 생성, 2. application-prod.yml 분리, 3. RefreshToken 엔티티 및 리포지토리, 4. TokenService 리팩토링, 5. 인증 컨트롤러 업데이트"

# 구체적 구현 예시
다음과 같이 JWT 보안을 개선해줘:
1. SecureKeyGenerator 클래스 생성 (256비트 키 생성)
2. RefreshToken 도메인 모델 추가 (유효기간 7일)
3. TokenPair (access + refresh) 반환 구조 구현
4. 토큰 재발급 엔드포인트 추가 (/api/auth/refresh)
5. 환경별 키 관리 설정 (dev/prod 분리)
--persona-security --think-hard --validate
```

### 1.2 비밀번호 정책 구현
```bash
# 비밀번호 정책 및 계정 보안
/implement 비밀번호 정책: 복잡도 검증, 이력 관리, 계정 잠금 기능 --persona-security --seq --wave-mode

# 구체적 구현
PasswordPolicy 도메인 서비스를 구현해줘:
- 최소 8자, 대소문자/숫자/특수문자 필수
- 최근 5개 비밀번호 재사용 금지
- 5회 로그인 실패 시 30분 계정 잠금
- PasswordHistory 엔티티 추가
- LoginAttempt 추적 기능
--persona-security --think --test
```

### 1.3 Rate Limiting 고도화
```bash
# Rate Limiting 개선
/improve Rate Limiting: 사용자별/IP별 세분화, DDoS 방어, 동적 임계값 --persona-backend --seq

구체적 구현:
1. RateLimitStrategy 인터페이스 설계
2. UserBasedRateLimiter, IpBasedRateLimiter 구현
3. Redis 기반 분산 Rate Limiting
4. 엔드포인트별 다른 제한 설정
5. Rate Limit 초과 시 적절한 에러 응답
--think --validate
```

---

## Phase 2: 성능 최적화 (3-4주)

### 2.1 데이터베이스 최적화
```bash
# DB 성능 분석 및 최적화
/analyze DB 성능: 쿼리 분석, N+1 문제, 인덱스 전략 --persona-performance --ultrathink --seq

# 인덱스 및 쿼리 최적화
/implement DB 최적화:
1. 복합 인덱스 추가 (challenge_id + user_id, created_at + status)
2. @EntityGraph로 N+1 해결
3. QueryDSL 도입으로 동적 쿼리 최적화
4. 슬로우 쿼리 로깅 설정
5. DB 커넥션 풀 튜닝
--persona-performance --wave-mode --validate

# Flyway 마이그레이션 생성
V3__add_performance_indexes.sql 마이그레이션 파일을 생성해줘:
- challenge_sessions 테이블 인덱스
- orders 테이블 인덱스
- portfolio_positions 테이블 인덱스
--think
```

### 2.2 캐싱 전략 구현
```bash
# Redis 캐싱 고도화
/implement Redis 캐싱 전략:
1. CacheConfig 클래스로 중앙화
2. 데이터별 TTL 설정 (시세: 1분, 리더보드: 5분, 세션: 30분)
3. 캐시 워밍업 스케줄러
4. 캐시 무효화 전략
5. Redis Sentinel 설정
--persona-performance --seq --wave-mode

# 캐시 어노테이션 개선
모든 Service 클래스의 캐싱 어노테이션을 검토하고 개선해줘:
- TTL 적절성 검증
- 캐시 키 전략 표준화
- 조건부 캐싱 적용
--improve --loop --iterations 3
```

### 2.3 비동기 처리 확대
```bash
# 이벤트 기반 아키텍처 도입
/implement 이벤트 기반 아키텍처:
1. Spring Events 활용한 도메인 이벤트
2. @Async 메소드 확대 (리더보드 계산, 알림)
3. CompletableFuture 활용
4. ThreadPoolTaskExecutor 설정 최적화
5. 이벤트 소싱 패턴 검토
--persona-architect --seq --wave-mode

# 시뮬레이션 비동기화
ChallengeSimulationService를 비동기 처리로 개선해줘:
- 병렬 세션 처리
- 배치 처리 최적화
- 실패 시 재시도 로직
--persona-performance --think-hard
```

---

## Phase 3: 테스트 커버리지 (5-6주)

### 3.1 단위 테스트 대량 생성
```bash
# 테스트 자동 생성 및 구현
/implement 단위 테스트 80% 커버리지:
1. 모든 Service 클래스 테스트 생성
2. Repository 레이어 테스트
3. Controller MockMvc 테스트
4. 도메인 모델 테스트
5. 유틸리티 클래스 테스트
--persona-qa --wave-mode --delegate auto

# Service 테스트 일괄 생성
src/main/java/com/stockquest/application 디렉토리의 모든 Service 클래스에 대한 테스트를 생성해줘:
- Given-When-Then 패턴
- 성공/실패 케이스 모두 포함
- Mockito 활용
- 테스트 커버리지 90% 이상
--persona-qa --parallel --loop

# Repository 테스트
@DataJpaTest를 활용한 Repository 테스트를 모두 생성해줘:
- 커스텀 쿼리 메소드 테스트
- 페이징 테스트
- 트랜잭션 롤백 검증
--think --validate
```

### 3.2 통합 테스트 구현
```bash
# E2E 통합 테스트
/implement 통합 테스트:
1. Testcontainers로 MySQL, Redis 구성
2. 주요 비즈니스 플로우 E2E 테스트
3. REST Assured로 API 테스트
4. 트랜잭션 경계 테스트
5. 동시성 테스트
--persona-qa --seq --wave-mode

# 구체적 시나리오 테스트
다음 시나리오에 대한 통합 테스트를 작성해줘:
- 사용자 가입 → 로그인 → 챌린지 시작 → 주문 실행 → 리더보드 확인
- 동시 주문 처리 (100개 동시 요청)
- 세션 만료 및 재인증
--think-hard --validate
```

### 3.3 성능 테스트
```bash
# JMeter 테스트 스크립트 생성
/implement JMeter 성능 테스트:
1. 테스트 시나리오 정의 (동시 사용자 1000명)
2. API 엔드포인트별 부하 테스트
3. 리소스 모니터링 설정
4. 성능 기준선 설정
5. 병목 지점 분석
--persona-performance --seq
```

---

## Phase 4: 코드 품질 개선 (7-8주)

### 4.1 에러 처리 표준화
```bash
# 예외 계층 구조 설계
/design 도메인 예외 계층:
1. BusinessException 기본 클래스
2. 도메인별 예외 (ChallengeException, OrderException 등)
3. 에러 코드 enum (ErrorCode)
4. 에러 응답 표준화
5. 예외 번역 메커니즘
--persona-architect --seq --wave-mode

# GlobalExceptionHandler 개선
GlobalExceptionHandler를 개선해줘:
- 모든 예외 타입 처리
- 로깅 레벨 구분 (ERROR, WARN, INFO)
- 클라이언트 친화적 메시지
- 국제화 지원
--improve --validate
```

### 4.2 로깅 전략 구현
```bash
# 구조화된 로깅 시스템
/implement 로깅 시스템:
1. MDC를 활용한 TraceId 추적
2. JSON 포맷 로거 설정 (Logback)
3. 비즈니스 이벤트 로깅 (AuditLog)
4. 로그 레벨 동적 변경
5. 로그 집계 설정 (ELK Stack 준비)
--persona-devops --seq

# AOP 로깅 구현
@LogExecution 어노테이션과 AOP를 활용한 메소드 실행 로깅을 구현해줘:
- 실행 시간 측정
- 파라미터/결과 로깅 (민감정보 마스킹)
- 예외 발생 시 상세 로깅
--think --test
```

### 4.3 문서화
```bash
# JavaDoc 및 API 문서화
/document 전체 프로젝트:
1. 핵심 비즈니스 로직 JavaDoc
2. OpenAPI 스펙 완성
3. README 업데이트
4. 아키텍처 다이어그램
5. API 사용 가이드
--persona-scribe --wave-mode --delegate auto

# 아키텍처 결정 기록
ADR (Architecture Decision Records) 문서를 생성해줘:
- 헥사고날 아키텍처 선택 이유
- 기술 스택 결정 근거
- 주요 설계 패턴 설명
--persona-architect --think
```

---

## 🔄 지속적 개선 프롬프트

### 일일 작업 시작
```bash
# 매일 작업 시작 시
/analyze --focus quality --seq
/test --coverage
/git status

# 문제 발견 시
/troubleshoot [문제 설명] --persona-analyzer --think-hard
```

### 코드 리뷰 및 개선
```bash
# 구현 후 자동 리뷰
/improve --quality --loop --iterations 3 --persona-refactorer

# 성능 분석
/analyze --focus performance --persona-performance --seq

# 보안 감사
/analyze --focus security --persona-security --validate
```

### 배포 전 체크리스트
```bash
# 배포 준비
/task deployment-checklist --steps "테스트 실행, 보안 스캔, 성능 테스트, 문서 업데이트, 마이그레이션 검증"

# 최종 검증
/validate --all --wave-mode
```

---

## 💡 Pro Tips

1. **Wave Mode 활용**: 복잡한 작업은 `--wave-mode`로 단계적 실행
2. **Persona 조합**: 작업 성격에 맞는 persona 활성화
3. **Sequential 사용**: 복잡한 분석은 `--seq`로 체계적 접근
4. **Loop 활용**: 반복 개선이 필요한 작업은 `--loop`
5. **Delegation**: 대규모 변경은 `--delegate auto`로 병렬 처리
6. **Validation**: 모든 보안/성능 작업은 `--validate` 필수

## 🎯 목표 달성 체크포인트

```bash
# 주간 진행 상황 체크
/analyze 프로젝트 전체 개선 진행 상황 --seq --wave-mode

# 메트릭 확인
- 테스트 커버리지: 80% 이상
- 보안 취약점: 0개
- 성능: API 응답 200ms 이하
- 코드 품질: SonarQube A 등급
```