# 📝 StockQuest 커밋 가이드라인

## 🎯 목적

이 가이드라인은 StockQuest 프로젝트의 **일관된 Git 히스토리**를 유지하고, **변경 추적을 용이**하게 하며, **유지보수성을 향상**시키기 위함입니다.

## 📋 기본 원칙

### 1. **원자적 커밋 (Atomic Commits)**
- 하나의 커밋은 하나의 논리적 변경만 포함
- 관련 없는 변경사항은 별도 커밋으로 분리
- 각 커밋은 독립적으로 빌드 및 테스트 가능해야 함

### 2. **작은 단위 커밋 (Small Commits)**
- **최대 5-10개 파일**까지만 한 번에 커밎
- 대량 변경은 여러 커밋으로 분할
- 리뷰하기 쉬운 크기로 유지

### 3. **의미있는 커밋 메시지**
- 변경 이유와 영향을 명확히 설명
- 코드를 보지 않고도 변경사항 이해 가능
- 미래의 개발자(자신 포함)를 위한 문서

## 🏗️ 커밋 메시지 구조

### 기본 형식
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 1. Type (필수)
| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat(domain/user): add email validation` |
| `fix` | 버그 수정 | `fix(adapter/web): handle null pointer exception` |
| `docs` | 문서 수정 | `docs(readme): update API documentation` |
| `style` | 코드 스타일 변경 | `style(domain/order): fix indentation` |
| `refactor` | 리팩토링 | `refactor(app/auth): extract token validation` |
| `test` | 테스트 추가/수정 | `test(domain/user): add validation tests` |
| `chore` | 빌드/설정 변경 | `chore(deps): update spring boot version` |
| `perf` | 성능 개선 | `perf(app/portfolio): optimize calculation` |
| `ci` | CI/CD 설정 | `ci(github): add automated testing` |
| `revert` | 커밋 되돌리기 | `revert: feat(domain/user): add email validation` |

### 2. Scope (선택사항, 권장)

#### Backend Scopes
```
domain/<entity>     - 도메인 엔티티
├── domain/user     - 사용자 도메인
├── domain/challenge - 챌린지 도메인
├── domain/order    - 주문 도메인
├── domain/portfolio - 포트폴리오 도메인
└── domain/market   - 시장 데이터 도메인

app/<service>       - 애플리케이션 서비스
├── app/auth        - 인증 서비스
├── app/trading     - 거래 서비스
├── app/leaderboard - 리더보드 서비스
└── app/community   - 커뮤니티 서비스

adapter/<type>      - 어댑터 구현
├── adapter/web     - REST API 컨트롤러
├── adapter/persistence - JPA 저장소
├── adapter/auth    - JWT, 암호화
└── adapter/market  - 외부 시장 데이터

config              - 설정 및 구성
migration          - 데이터베이스 마이그레이션
```

#### Frontend Scopes
```
app/<page>          - 페이지 컴포넌트
├── app/auth        - 인증 페이지
├── app/challenges  - 챌린지 페이지
├── app/portfolio   - 포트폴리오 페이지
└── app/community   - 커뮤니티 페이지

widget/<name>       - 복합 위젯
├── widget/portfolio - 포트폴리오 패널
├── widget/leaderboard - 리더보드 패널
└── widget/market-data - 시장 데이터 패널

feature/<name>      - 기능 모듈
├── feature/place-order - 주문 접수
├── feature/auth    - 인증 기능
└── feature/simulation - 시뮬레이션

entity/<name>       - 비즈니스 엔티티
├── entity/user     - 사용자 타입
├── entity/challenge - 챌린지 타입
└── entity/order    - 주문 타입

shared/<type>       - 공유 리소스
├── shared/api      - API 클라이언트
├── shared/ui       - 공통 컴포넌트
└── shared/lib      - 유틸리티
```

### 3. Subject (필수)
- **50자 이하**로 요약
- **명령형 현재시제** 사용 ("add", "fix", "update")
- **첫 글자 소문자**
- **마침표 없음**
- **영어 권장** (한글도 허용)

#### 좋은 예시
```
add user email validation
fix authentication token expiration
update portfolio calculation logic
remove deprecated API endpoint
```

#### 나쁜 예시
```
Added user email validation.  // 과거시제 + 마침표
Fix Authentication Token      // 대문자 시작
updated portfolio calc logic and also fixed bug  // 너무 길고 여러 작업
bug fix                       // 너무 모호함
```

### 4. Body (선택사항, 권장)
- **72자마다 줄바꿈**
- **"무엇을"** 보다 **"왜"** 변경했는지 설명
- 이전 동작과 새로운 동작 비교
- 복잡한 변경사항에 대한 상세 설명

#### 예시
```
feat(domain/user): add email validation

User registration was accepting invalid email formats,
causing downstream issues with email notifications
and authentication.

- Add EmailValidator class with regex pattern
- Validate email format in User constructor
- Throw InvalidEmailException for invalid formats

This ensures data integrity and prevents notification
failures in production environment.
```

### 5. Footer (선택사항)
- **관련 이슈 참조**: `Closes #123`, `Fixes #456`
- **Breaking Changes**: `BREAKING CHANGE: <description>`
- **Co-authored-by**: `Co-authored-by: Name <email>`

## 🔧 커밋 분할 전략

### 1. 레이어별 분할
```bash
# ❌ 잘못된 예시 - 한 번에 너무 많은 레이어 변경
git add .
git commit -m "feat: add user management feature"

# ✅ 올바른 예시 - 레이어별로 분할
git add backend/src/main/java/com/stockquest/domain/user/
git commit -m "feat(domain/user): add User entity with validation"

git add backend/src/main/java/com/stockquest/application/user/
git commit -m "feat(app/user): add user registration service"

git add backend/src/main/java/com/stockquest/adapter/in/web/user/
git commit -m "feat(adapter/web): add user registration endpoint"

git add backend/src/test/java/com/stockquest/domain/user/
git commit -m "test(domain/user): add User entity validation tests"
```

### 2. 기능별 분할
```bash
# ✅ 기능의 핵심 로직
git commit -m "feat(domain/portfolio): add position calculation logic"

# ✅ 기능의 API 인터페이스
git commit -m "feat(adapter/web): add portfolio value endpoint"

# ✅ 기능의 프론트엔드
git commit -m "feat(widget/portfolio): display real-time values"

# ✅ 기능의 테스트
git commit -m "test(domain/portfolio): add position calculation tests"
```

### 3. 파일 수 기준
```bash
# ✅ 좋은 예시 - 3-5개 파일
modified:   User.java
modified:   UserService.java  
modified:   UserController.java
new file:   UserValidationTest.java

# ⚠️ 주의 - 8-10개 파일 (허용되지만 분할 고려)
modified:   User.java
modified:   UserService.java
modified:   UserController.java
modified:   UserRepository.java
modified:   UserResponse.java
modified:   UserRequest.java  
new file:   UserTest.java
new file:   UserServiceTest.java
new file:   UserControllerTest.java

# ❌ 나쁜 예시 - 15개 이상 파일
(너무 많은 파일 변경 - 반드시 분할 필요)
```

## 📊 커밋 품질 메트릭

### 좋은 커밋의 지표
- **원자성**: 하나의 논리적 변경
- **완전성**: 빌드 가능하고 테스트 통과
- **독립성**: 다른 커밋에 의존하지 않음
- **명확성**: 메시지만으로 변경사항 이해 가능
- **적정 크기**: 5-10개 파일 이하

### 나쁜 커밋의 징후
- **"WIP", "temp", "fix"** 같은 모호한 메시지
- **20개 이상 파일** 변경
- **여러 도메인**이 함께 변경
- **테스트 없이** 기능 추가
- **Breaking Change 없이** API 변경

## 🛠️ 실제 적용 예시

### Backend 개발 시나리오
```bash
# 1. 도메인 엔티티 추가
git add backend/src/main/java/com/stockquest/domain/notification/
git commit -m "feat(domain/notification): add Notification entity

- Add notification types (EMAIL, PUSH, SMS)
- Add validation for recipient and content
- Include timestamp and status tracking"

# 2. 포트 인터페이스 추가  
git add backend/src/main/java/com/stockquest/domain/notification/port/
git commit -m "feat(domain/notification): add NotificationRepository port"

# 3. 애플리케이션 서비스 추가
git add backend/src/main/java/com/stockquest/application/notification/
git commit -m "feat(app/notification): add notification sending service

- Implement SendNotificationUseCase
- Add notification templates
- Handle delivery status tracking"

# 4. 어댑터 구현
git add backend/src/main/java/com/stockquest/adapter/out/notification/
git commit -m "feat(adapter/notification): add email notification adapter

- Integrate with Spring Mail
- Add HTML email templates
- Handle connection failures gracefully"

# 5. 웹 컨트롤러 추가
git add backend/src/main/java/com/stockquest/adapter/in/web/notification/
git commit -m "feat(adapter/web): add notification management endpoints

- POST /api/notifications - send notification
- GET /api/notifications/{id} - get status
- GET /api/users/{userId}/notifications - list user notifications"

# 6. 테스트 추가
git add backend/src/test/java/com/stockquest/domain/notification/
git commit -m "test(domain/notification): add Notification entity tests

- Test validation rules
- Test notification type constraints
- Test timestamp handling"
```

### Frontend 개발 시나리오
```bash
# 1. 타입 정의
git add frontend/src/entities/notification/
git commit -m "feat(entity/notification): add notification types

- Define Notification interface
- Add notification status enum
- Add API response types"

# 2. API 클라이언트
git add frontend/src/shared/api/notificationApi.ts
git commit -m "feat(shared/api): add notification API client

- Add CRUD operations
- Include error handling
- Add TypeScript strict types"

# 3. 기능 컴포넌트
git add frontend/src/features/notifications/
git commit -m "feat(feature/notifications): add notification list component

- Display notifications with status
- Add mark as read functionality  
- Include real-time updates"

# 4. 위젯 통합
git add frontend/src/widgets/notifications/
git commit -m "feat(widget/notifications): add notification panel

- Integrate with notification feature
- Add notification bell icon
- Show unread count badge"

# 5. 페이지 통합
git add frontend/src/app/notifications/
git commit -m "feat(app/notifications): add notifications page

- Create full notification management UI
- Add filtering and searching
- Include pagination"
```

## ⚡ Git 설정

### 커밋 템플릿 설정
```bash
# 프로젝트 루트에서 실행
git config commit.template .gitmessage
```

### 유용한 Git Aliases
```bash
# 자주 사용하는 명령어들
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status
git config --global alias.unstage 'reset HEAD --'
git config --global alias.last 'log -1 HEAD'
git config --global alias.visual '!gitk'

# StockQuest 전용
git config alias.domain-commit 'commit -m "feat(domain/\$1): \$2"'
git config alias.fix-commit 'commit -m "fix(\$1): \$2"'
```

## 🔍 커밋 검증 체크리스트

### 커밋 전 자체 검토
- [ ] **원자성**: 하나의 논리적 변경만 포함되는가?
- [ ] **범위**: 5-10개 파일 이하인가?
- [ ] **빌드**: 빌드가 성공하는가?
- [ ] **테스트**: 관련 테스트가 통과하는가?
- [ ] **메시지**: 명확하고 의미있는 메시지인가?
- [ ] **타입**: 올바른 커밋 타입을 사용했는가?
- [ ] **스코프**: 적절한 스코프를 지정했는가?

### 푸시 전 전체 검토
- [ ] **히스토리**: 커밋 히스토리가 깔끔한가?
- [ ] **관련성**: 각 커밋이 독립적으로 이해되는가?
- [ ] **순서**: 논리적 순서로 커밋되었는가?
- [ ] **완성도**: 각 커밋이 완전한 상태인가?

## 🚨 안티패턴 (피해야 할 것들)

### 1. 메가 커밋 (Mega Commit)
```bash
# ❌ 잘못된 예시
modified:   20 files changed, 500 insertions(+), 200 deletions(-)
git commit -m "feat: implement entire user management system"
```

### 2. WIP 커밋 (Work In Progress)
```bash
# ❌ 잘못된 예시  
git commit -m "WIP"
git commit -m "temp fix"
git commit -m "debugging"
git commit -m "forgot to add files"
```

### 3. 혼재 커밋 (Mixed Commit)
```bash
# ❌ 잘못된 예시 - 버그 수정과 기능 추가가 함께
git commit -m "fix login bug and add user profile feature"
```

### 4. 무의미한 커밋
```bash
# ❌ 잘못된 예시
git commit -m "update"
git commit -m "fix stuff"  
git commit -m "changes"
```

## 🎯 커밋 품질 향상 팁

### 1. 작은 단위로 자주 커밋
- 기능 완성을 기다리지 말고 의미있는 단위로 커밋
- 하루에 3-5개의 작은 커밋이 1개의 큰 커밋보다 좋음

### 2. 커밋 메시지는 팀원을 위한 문서
- 6개월 후의 자신이 이해할 수 있도록 작성
- 코드 리뷰어가 쉽게 이해할 수 있도록 작성

### 3. 테스트와 함께 커밋
- 기능 커밋 후 바로 테스트 커밋
- 테스트 없는 기능은 완성되지 않은 것으로 간주

### 4. Breaking Change 명시
```bash
# ✅ Breaking Change 명시
feat(adapter/web): change user API response format

BREAKING CHANGE: User API now returns nested user object
instead of flat structure. Update frontend accordingly.

Old: { id: 1, name: "John", email: "john@example.com" }
New: { user: { id: 1, name: "John", email: "john@example.com" } }
```

---

**🎯 목표**: 이 가이드라인을 통해 StockQuest의 Git 히스토리를 **읽기 쉽고**, **추적 가능하며**, **유지보수하기 쉬운** 상태로 유지합니다.

**📅 마지막 업데이트**: 2025-09-11  
**🔄 검토 주기**: 월간