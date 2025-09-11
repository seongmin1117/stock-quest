# 🤖 Claude 자동 시작 가이드

**⚠️ IMPORTANT: 이 파일을 항상 먼저 읽고 작업을 시작하세요!**

## 🎯 프로젝트 정체성
**StockQuest** - 과거 시장 데이터를 활용한 모의 투자 챌린지 교육 플랫폼

## 📍 현재 위치
- **Repository**: `https://github.com/seongmin1117/stock-quest.git`
- **Version**: v0.1.0-alpha
- **Status**: 초기 개발 완료, GitHub 배포 준비 완료

## 🏗️ 아키텍처 원칙 (절대 변경 금지)

### Backend (Spring Boot)
- **패턴**: Hexagonal Architecture (Clean Architecture)
- **계층**: Domain → Application → Adapter
- **규칙**: Domain은 순수 Java (Spring 의존성 금지)

### Frontend (Next.js)
- **패턴**: Feature-Sliced Design  
- **구조**: app/ → widgets/ → features/ → entities/ → shared/
- **규칙**: 계층 간 import 방향성 엄격 준수

## 📋 작업 시작 전 체크리스트

### 1. 필수 읽기 파일 (순서대로)
1. `.claude/PROJECT_CONTEXT.md` - 프로젝트 전체 맥락
2. `.claude/ARCHITECTURE_RULES.md` - 아키텍처 규칙
3. `.claude/DEVELOPMENT_PATTERNS.md` - 개발 패턴
4. `.claude/CURRENT_STATE.md` - 현재 상태

### 2. 작업 전 확인사항
- [ ] 현재 브랜치 확인: `main` 또는 `feature/*`
- [ ] 최신 상태 업데이트: `git pull origin main`
- [ ] 테스트 상태 확인: `./gradlew test` (backend), `pnpm test` (frontend)
- [ ] 도커 서비스 실행 상태 확인: `docker-compose ps`

### 3. 새 기능 개발 시 패턴
1. **Backend**: Domain Entity → Port → Use Case → Adapter → Controller
2. **Frontend**: Entity → Feature → Widget → Page
3. **테스트**: Domain 테스트 → Integration 테스트 → E2E 테스트

## 🚫 절대 금지사항

1. **아키텍처 변경**: Hexagonal, FSD 패턴 변경 금지
2. **의존성 방향 위반**: Domain → Application → Adapter 순서 준수
3. **Spring 의존성 in Domain**: Domain 레이어에 Spring 코드 금지
4. **직접 DB 접근**: Repository Port를 통해서만 접근
5. **환경 변수 하드코딩**: 모든 설정은 application.yml 또는 .env

## 🎯 개발 우선순위 (항상 이 순서)

### Phase 1: 안정화 (현재)
1. 테스트 커버리지 80% 달성
2. 에러 처리 시스템 완성
3. 보안 취약점 제거

### Phase 2: 실시간 기능
1. WebSocket 서버 구현
2. 실시간 가격 업데이트
3. 실시간 리더보드

### Phase 3: 확장 기능
1. 소셜 로그인 (OAuth2)
2. AI 투자 조언
3. 모바일 대응

## 🔧 개발 환경

### 필수 도구
- Java 21, Node.js 18+, Docker
- IntelliJ IDEA (backend), VSCode (frontend)
- MySQL 8.0, Redis 7

### 포트 정보
- Backend: 8080
- Frontend: 3000  
- MySQL: 3306
- Redis: 6379

## 📝 코딩 컨벤션

### Java
```java
// ✅ Good
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    
    public Challenge createChallenge(CreateChallengeCommand command) {
        // 구현
    }
}

// ❌ Bad - Domain에 Spring 의존성
@Service
public class Challenge {
    @Autowired
    private SomeService service;
}
```

### TypeScript
```typescript
// ✅ Good - FSD 패턴
import { User } from '@/entities/user'
import { placeOrder } from '@/features/place-order'

// ❌ Bad - 역방향 import
import { SomeWidget } from '@/widgets/some-widget'
```

## 🚀 Quick Commands

### 전체 시스템 시작
```bash
# 1. Docker 서비스 시작
docker-compose up -d mysql redis

# 2. Backend 시작 (터미널 1)
cd backend && ./gradlew bootRun

# 3. Frontend 시작 (터미널 2)
cd frontend && pnpm dev
```

### 테스트 실행
```bash
# Backend 테스트
cd backend && ./gradlew test

# Frontend 테스트  
cd frontend && pnpm test

# E2E 테스트
cd frontend && pnpm test:e2e
```

## 🤝 협업 규칙

### Git Workflow
- `main`: 프로덕션 브랜치
- `develop`: 개발 통합 브랜치  
- `feature/*`: 기능 개발 브랜치
- `hotfix/*`: 긴급 수정 브랜치

### Commit 메시지
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드 설정 변경
```

## ⚡ 성능 목표

- API 응답시간: < 200ms (95%)
- 테스트 커버리지: > 80%
- 빌드 시간: < 3분
- 페이지 로딩: < 3초 (3G)

## 📞 문제 해결

1. **MySQL 연결 실패**: `docker-compose restart mysql`
2. **Redis 연결 실패**: `docker-compose restart redis`  
3. **포트 충돌**: `lsof -i :[PORT]` → `kill -9 [PID]`
4. **의존성 문제**: `./gradlew clean build`, `pnpm install`

---

**🎯 핵심 메시지**: 이 프로젝트는 교육 목적의 투자 시뮬레이션 플랫폼입니다. 사용자가 안전하게 투자 경험을 쌓을 수 있도록 견고하고 확장 가능한 시스템을 구축하는 것이 목표입니다.

**📅 마지막 업데이트**: 2025-09-11  
**👤 관리자**: seongmin1117