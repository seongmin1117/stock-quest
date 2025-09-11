# 🔄 Claude 컨텍스트 갱신 시스템

**목적**: Claude가 프로젝트 상태를 실시간으로 파악하고 일관된 개발을 수행할 수 있도록 자동화

## 📋 컨텍스트 갱신 체크리스트

### 🚀 세션 시작 시 (MANDATORY)

#### 1. 기본 파일 읽기 (순서대로)
```bash
# 필수 읽기 파일 - 절대 생략 금지
1. .claude/AUTOSTART.md       # 시작 가이드
2. .claude/PROJECT_CONTEXT.md  # 프로젝트 맥락
3. .claude/ARCHITECTURE_RULES.md # 아키텍처 규칙
4. .claude/DEVELOPMENT_PATTERNS.md # 개발 패턴
5. .claude/CURRENT_STATE.md   # 현재 상태
```

#### 2. 프로젝트 상태 체크
```bash
# Git 상태 확인
git status
git branch
git log --oneline -5

# 빌드 상태 확인
cd backend && ./gradlew build --dry-run
cd frontend && pnpm install --dry-run

# 도커 서비스 상태 확인
docker-compose ps
```

#### 3. 최근 변경사항 파악
```bash
# 최근 커밋 분석
git log --since="1 week ago" --oneline

# 최근 수정 파일 확인
git diff HEAD~5 --name-only

# 브랜치 비교 (develop이 있다면)
git diff main..develop --name-only
```

### 🔧 작업 시작 전 (EVERY TASK)

#### 1. 도메인 컨텍스트 확인
- **현재 작업 도메인** 파악 (user, challenge, order, portfolio 등)
- **관련 도메인 모델** 읽기
- **의존성 관계** 확인

#### 2. 아키텍처 준수 확인
```java
// 예: 새로운 기능 추가 시 확인할 것들
1. Domain Layer에 Spring 의존성 없는가?
2. 의존성 방향이 올바른가? (Domain ← Application ← Adapter)
3. Port/Adapter 패턴이 적용되었는가?
4. 네이밍 컨벤션을 따르는가?
```

#### 3. 기존 패턴 분석
```bash
# 유사한 기능 찾기
find backend/src -name "*Service.java" | head -5
find frontend/src -name "*Panel.tsx" | head -5

# 패턴 일관성 확인
grep -r "public class.*Controller" backend/src/main/java
grep -r "export const.*Panel" frontend/src
```

## 🔄 자동 갱신 트리거

### Git Hook 기반 갱신
```bash
# .git/hooks/post-checkout (새 브랜치 체크아웃 시)
#!/bin/bash
echo "🔄 Updating Claude context..."
echo "Branch: $(git branch --show-current)" > .claude/LAST_BRANCH
echo "Date: $(date)" >> .claude/LAST_BRANCH
echo "Commit: $(git rev-parse HEAD)" >> .claude/LAST_BRANCH
```

### 파일 변경 기반 갱신
```bash
# 중요 파일 변경 시 알림
WATCH_FILES=(
  "backend/build.gradle"
  "frontend/package.json" 
  "docker-compose.yml"
  "README.md"
  ".claude/*.md"
)
```

## 📊 상태 추적 시스템

### 1. 프로젝트 메트릭 수집
```bash
# 코드 통계
find backend/src/main/java -name "*.java" | wc -l
find frontend/src -name "*.tsx" -o -name "*.ts" | wc -l

# 테스트 통계
find backend/src/test/java -name "*Test.java" | wc -l
find frontend/tests -name "*.test.ts*" -o -name "*.spec.ts*" | wc -l

# 도메인 엔티티 수
find backend/src/main/java/com/stockquest/domain -name "*.java" -not -path "*/port/*" | wc -l
```

### 2. 기능 완성도 체크
```bash
# 컨트롤러별 엔드포인트 수
grep -r "@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" backend/src/main/java/com/stockquest/adapter/in/web/ | wc -l

# 페이지 수
find frontend/src/app -name "page.tsx" | wc -l

# 컴포넌트 수
find frontend/src -name "*.tsx" -not -path "*/node_modules/*" | wc -l
```

### 3. 품질 메트릭 추적
```bash
# 테스트 커버리지 (추정)
TOTAL_CLASSES=$(find backend/src/main/java -name "*.java" | wc -l)
TEST_CLASSES=$(find backend/src/test/java -name "*Test.java" | wc -l)
COVERAGE=$((TEST_CLASSES * 100 / TOTAL_CLASSES))

echo "Test Coverage: ${COVERAGE}%"
```

## 🎯 컨텍스트 갱신 패턴

### 패턴 1: 기능 추가 시
```bash
# 1. 도메인 분석
echo "새 기능: ${FEATURE_NAME}"
echo "도메인: ${DOMAIN}"

# 2. 기존 패턴 참고
find backend/src -path "*${DOMAIN}*" -name "*.java"
find frontend/src -path "*${DOMAIN}*" -name "*.tsx"

# 3. 의존성 영향도 분석
grep -r "${DOMAIN}" backend/src/main/java/com/stockquest/

# 4. 테스트 전략 수립
ls backend/src/test/java/com/stockquest/${DOMAIN}/
```

### 패턴 2: 버그 수정 시
```bash
# 1. 영향 범위 분석
git log --grep="${BUG_KEYWORD}" --oneline

# 2. 관련 테스트 확인
find backend/src/test -name "*${MODULE}*Test.java"

# 3. 회귀 테스트 계획
echo "Regression test plan for ${MODULE}"
```

### 패턴 3: 리팩토링 시
```bash
# 1. 현재 구조 분석
tree backend/src/main/java/com/stockquest/${MODULE}

# 2. 의존성 매트릭스 생성
grep -r "import.*${MODULE}" backend/src/main/java

# 3. 테스트 영향도 확인
grep -r "${MODULE}" backend/src/test/java
```

## 📝 자동 문서 업데이트

### CURRENT_STATE.md 자동 갱신
```bash
#!/bin/bash
# update_state.sh

# 1. Git 정보 업데이트
LAST_COMMIT=$(git log -1 --format="%h %s")
BRANCH=$(git branch --show-current)
echo "Last commit: ${LAST_COMMIT}" > .claude/STATUS

# 2. 코드 메트릭 업데이트
BACKEND_LINES=$(find backend/src/main -name "*.java" -exec cat {} \; | wc -l)
FRONTEND_LINES=$(find frontend/src -name "*.ts*" -exec cat {} \; | wc -l)

# 3. 기능 완성도 계산
CONTROLLERS=$(find backend/src/main/java -name "*Controller.java" | wc -l)
PAGES=$(find frontend/src/app -name "page.tsx" | wc -l)

# 4. CURRENT_STATE.md 업데이트
sed -i "s/Backend Lines: .*/Backend Lines: ${BACKEND_LINES}/" .claude/CURRENT_STATE.md
sed -i "s/Frontend Lines: .*/Frontend Lines: ${FRONTEND_LINES}/" .claude/CURRENT_STATE.md
```

### 진행도 추적
```bash
# 완료된 기능 체크
COMPLETED_FEATURES=(
  "user-authentication"
  "challenge-system"  
  "trading-system"
  "portfolio-tracking"
  "leaderboard"
)

# 미완성 기능 체크
TODO_FEATURES=(
  "websocket-realtime"
  "error-handling"
  "monitoring"
  "performance-optimization"
)
```

## 🚨 컨텍스트 무효화 감지

### Critical Changes (즉시 갱신 필요)
```bash
CRITICAL_FILES=(
  ".claude/ARCHITECTURE_RULES.md"
  "backend/build.gradle"
  "frontend/package.json"
  "docker-compose.yml"
)

# 변경 감지 시 알림
for file in "${CRITICAL_FILES[@]}"; do
  if [[ $(git diff HEAD~1 --name-only | grep "$file") ]]; then
    echo "⚠️ Critical file changed: $file"
    echo "🔄 Context refresh required!"
  fi
done
```

### Schema Changes (데이터 모델 변경)
```bash
# DB 마이그레이션 파일 변경 감지
if [[ $(git diff HEAD~1 --name-only | grep "db/migration") ]]; then
  echo "🗄️ Database schema changed"
  echo "📋 Update domain models and DTOs"
fi

# 도메인 엔티티 변경 감지
if [[ $(git diff HEAD~1 --name-only | grep "domain.*\.java") ]]; then
  echo "🏗️ Domain model changed"
  echo "🔄 Update related adapters and tests"
fi
```

## 🎮 사용자 가이드

### Claude에게 컨텍스트 갱신 요청하기
```
💬 사용자 명령어:
"--refresh" : 전체 컨텍스트 갱신
"--status"  : 현재 상태 요약
"--check"   : 아키텍처 규칙 준수 확인
"--sync"    : 코드와 문서 동기화
```

### 예시 대화
```
👤 User: --refresh
🤖 Claude: 
1. Reading .claude/AUTOSTART.md...
2. Checking git status...
3. Analyzing recent changes...
4. Current branch: feature/websocket
5. Last commit: feat: add websocket server
6. Ready for development! 🚀

👤 User: --status
🤖 Claude:
📊 Project Status:
- Progress: 68/100
- Tests: 35% coverage  
- Features: 7/12 completed
- Current focus: WebSocket implementation
```

## 🔧 디버깅 및 트러블슈팅

### 컨텍스트 불일치 감지
```bash
# 예상과 다른 동작 시 체크할 것들
1. .claude/*.md 파일들이 최신인가?
2. git branch가 올바른가?
3. 최근 변경사항을 놓쳤는가?
4. 의존성 버전이 맞는가?
```

### 복구 절차
```bash
# 1. 컨텍스트 완전 재설정
git status
git log --oneline -10
cat .claude/AUTOSTART.md

# 2. 프로젝트 상태 재확인  
docker-compose ps
./gradlew build --dry-run
pnpm install --dry-run

# 3. 패턴 일관성 재확인
grep -r "public class.*Service" backend/src
grep -r "export const.*Panel" frontend/src
```

## 📅 정기 갱신 스케줄

### 일일 체크 (개발 시작 시)
- [ ] .claude/AUTOSTART.md 읽기
- [ ] git status 확인
- [ ] docker 서비스 상태 확인

### 주간 갱신 (매주 월요일)
- [ ] CURRENT_STATE.md 업데이트
- [ ] 메트릭 재계산
- [ ] 완료 기능 체크리스트 업데이트

### 월간 검토 (매월 첫째 주)
- [ ] 아키텍처 규칙 검토
- [ ] 개발 패턴 검토
- [ ] 문서 정확성 검증

---

**🎯 목표**: Claude가 세션이 바뀌어도 일관된 개발을 수행할 수 있는 자동화 시스템 구축

**📅 마지막 업데이트**: 2025-09-11  
**🔄 다음 검토**: 2025-10-11  
**👤 관리자**: seongmin1117