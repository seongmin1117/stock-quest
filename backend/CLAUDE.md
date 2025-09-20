# CLAUDE.md - Backend Module

This file provides guidance to Claude Code when working with the Stock Quest backend module.

## Module Overview
Spring Boot backend application following Hexagonal Architecture with domain-driven design.

## Critical Build Configuration
**Java Version**: Java 21 (Temurin)
**JAVA_HOME**: `/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home`

## Build Commands
```bash
# Compile
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew compileJava

# Build (without tests)
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew build -x test

# Run application
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew bootRun

# Run tests
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew test

# Database migration
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew flywayMigrate
```

## Architecture Structure

### Hexagonal Architecture Layers
```
src/main/java/com/stockquest/
├── domain/                 # Core business logic (NO Spring dependencies)
│   ├── challenge/          # Challenge domain entities
│   ├── content/           # Article/Category content domain
│   ├── market/            # Market data domain
│   ├── portfolio/         # Portfolio management domain
│   ├── session/           # Challenge session domain
│   └── user/              # User domain
├── application/           # Use cases and orchestration
│   ├── challenge/         # Challenge use cases
│   ├── content/          # Content management use cases
│   └── port/             # Port interfaces (in/out)
└── adapter/              # External integrations
    ├── in/               # Inbound adapters
    │   └── web/          # REST controllers
    └── out/              # Outbound adapters
        └── persistence/  # JPA repositories
```

## System Status

### ✅ **완료된 주요 작업들 (2025-09-20)**
- **모든 Repository 어댑터 구현 완료**: 10개 어댑터로 헥사고날 아키텍처 완성
- **백엔드 서버 정상 실행**: `http://localhost:8080` (**8.11초 시작**, 안정적 운영)
- **데이터베이스 연결 안정화**: HikariCP + MySQL + Flyway 마이그레이션 완료
- **Redis Repository 설정 분리**: Spring Data 충돌 해결로 깔끔한 로그
- **Bean 의존성 문제 해결**: ChallengeRepository 도메인 구현체 생성
- **API 응답 정상**: 한국어 데이터 포함하여 모든 엔드포인트 동작
- **캐시 시스템 구성**: 20개 캐시 영역 + 성능 모니터링 활성화
- **JPQL 쿼리 검증 완료**: 모든 Repository 쿼리 안정화

### ⚠️ **개선이 필요한 영역들**

#### **🔥 1단계: 즉시 해결 (Critical)**
- [x] **Redis Repository 설정 분리** - ✅ 완료: Spring Data 충돌 해결
- [x] **백엔드 서버 실행 문제** - ✅ 완료: 모든 Bean 의존성 해결, 8.11초 안정적 시작
- [x] **Bean 의존성 문제** - ✅ 완료: ChallengeRepository 도메인 구현체 생성
- [ ] **캐시 워밍업 최적화** - 워밍업 실패는 비크리티컬하지만 개선 가능

#### **🚀 2단계: 성능 최적화 (High Impact)**
- [ ] **Hibernate Second-level Cache 활성화** - 데이터베이스 쿼리 부하 50-70% 감소 예상
- [ ] **Database Connection Pool 최적화** - HikariCP 설정 튜닝
- [ ] **실시간 성능 모니터링 강화** - 캐시 히트율, API 응답시간, 메모리 사용률

#### **🔧 3단계: 개발 효율성 (Medium-High)**
- [ ] **테스트 자동화 개선** - Repository 어댑터 테스트, API 통합 테스트
- [ ] **코드 품질 개선** - SonarQube, 코드 커버리지 80% 목표
- [ ] **개발 환경 표준화** - Docker Compose, Hot Reload, API Documentation

#### **📈 4단계: 확장성 준비 (Strategic)**
- [ ] **마이크로서비스 준비** - Domain 경계에 따른 서비스 분리 계획
- [ ] **Cloud-Native 준비** - 컨테이너화, Kubernetes, 외부 API 연동
- [ ] **성능 벤치마킹** - JMeter 부하 테스트, 메모리 프로파일링

### **📋 다음 우선순위 작업**
1. **✅ 백엔드 서버 실행 문제 완전 해결** - 완료! 8.11초 안정적 시작
2. **Hibernate Second-level Cache 활성화** - 데이터베이스 쿼리 50-70% 감소 예상
3. **캐시 워밍업 최적화** - 비크리티컬이지만 성능 향상 가능
4. **성능 모니터링 대시보드 구축** - 실시간 성능 추적

## Development Guidelines

### Domain Layer Rules
- NO Spring Framework dependencies in domain layer
- Use pure Java/Kotlin for domain models
- Domain services should be framework-agnostic
- Port interfaces define contracts for adapters

### Testing Strategy
- Unit tests for domain logic (no Spring context)
- Integration tests for adapters
- E2E tests for complete workflows
- Use @DataJpaTest for repository tests
- Use @WebMvcTest for controller tests

### Database Conventions
- Table names: snake_case (e.g., `challenge_session`)
- Column names: snake_case (e.g., `created_at`)
- Use JPA annotations for mapping
- Always include audit columns (created_at, updated_at)

### API Conventions
- RESTful endpoints following OpenAPI 3.0 spec
- Request/Response DTOs in adapter layer
- Consistent error response format
- JWT authentication for protected endpoints

## Common Pitfalls to Avoid
1. Don't add Spring dependencies to domain layer
2. Don't skip compilation check before committing
3. Don't use mock data in production code paths
4. Don't forget to handle null cases in JPA mappings
5. Always use UTF-8 encoding for Korean text support

## Quick Debugging
```bash
# Check for compilation errors
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew compileJava --console=plain

# Clean build
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew clean

# Check dependency tree
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew dependencies
```