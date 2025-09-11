# 🏗️ StockQuest 아키텍처 규칙

## 📐 Hexagonal Architecture 준수 규칙

### 🎯 레이어 의존성 규칙 (STRICT)

```
외부 시스템 → Adapter → Application → Domain ← Application ← Adapter ← 외부 시스템
                   ↑         ↑         ↑
              Framework  Use Cases  Pure Logic
```

#### 절대 규칙 (위반 시 즉시 수정)
1. **Domain → Application 의존성 금지**
2. **Domain → Adapter 의존성 금지**  
3. **Domain에 Spring 어노테이션 금지**
4. **Application → Adapter 구현체 의존성 금지**

## 📁 디렉토리 구조 규칙

### Backend 구조 (변경 금지)
```
src/main/java/com/stockquest/
├── domain/              # 순수 Java, 비즈니스 로직
│   ├── user/
│   │   ├── User.java           # 엔티티
│   │   └── port/
│   │       └── UserRepository.java  # 포트 인터페이스
│   ├── challenge/
│   ├── order/
│   └── portfolio/
│
├── application/         # 유스케이스, 서비스
│   ├── auth/
│   │   ├── LoginService.java   # 유스케이스 구현
│   │   └── port/
│   │       └── in/LoginUseCase.java  # 인바운드 포트
│   └── challenge/
│
└── adapter/             # 프레임워크 연동
    ├── in/web/          # 인바운드 어댑터 (Controller)
    │   └── auth/AuthController.java
    └── out/
        ├── persistence/ # 아웃바운드 어댑터 (Repository)
        │   └── UserRepositoryAdapter.java
        └── auth/        # 외부 서비스
            └── JwtTokenAdapter.java
```

### Frontend 구조 (Feature-Sliced Design)
```
src/
├── app/                # 페이지, 라우팅
│   ├── auth/login/page.tsx
│   └── challenges/page.tsx
│
├── widgets/            # 복합 UI 블록
│   ├── portfolio/PortfolioPanel.tsx
│   └── leaderboard/LeaderboardPanel.tsx
│
├── features/           # 사용자 기능
│   ├── place-order/PlaceOrderForm.tsx
│   └── auth/LoginForm.tsx
│
├── entities/           # 비즈니스 엔티티
│   ├── user/User.types.ts
│   └── challenge/Challenge.types.ts
│
└── shared/             # 공유 리소스
    ├── api/ApiClient.ts
    ├── ui/Button.tsx
    └── lib/utils.ts
```

## 🔧 코딩 규칙

### Domain Layer 규칙

#### ✅ Domain Layer에서 허용되는 것
```java
// 순수 Java 클래스
public class User {
    private final Long id;
    private final Email email;
    private final Nickname nickname;
    
    public User(Long id, Email email, Nickname nickname) {
        // 생성자 로직
    }
    
    public boolean canParticipateIn(Challenge challenge) {
        // 비즈니스 로직
        return true;
    }
}

// 포트 인터페이스
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
}

// Value Objects
public class Email {
    private final String value;
    
    public Email(String value) {
        validate(value);
        this.value = value;
    }
    
    private void validate(String value) {
        // 이메일 검증 로직
    }
}
```

#### ❌ Domain Layer에서 금지되는 것
```java
// ❌ Spring 어노테이션 금지
@Entity
@Service
@Component
@Autowired
public class User { }

// ❌ JPA 어노테이션 금지
@Id
@GeneratedValue
@Column
public class User { }

// ❌ Framework 의존성 금지
import org.springframework.*;
import javax.persistence.*;

// ❌ 외부 라이브러리 의존성 최소화
import com.fasterxml.jackson.*;
```

### Application Layer 규칙

#### ✅ Application Layer 패턴
```java
@Service
@Transactional
public class LoginService implements LoginUseCase {
    
    private final UserRepository userRepository; // Port 의존
    private final PasswordEncoder passwordEncoder; // Port 의존
    private final JwtTokenProvider tokenProvider; // Port 의존
    
    public LoginService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider) {
        // 의존성 주입
    }
    
    @Override
    public LoginResult login(LoginCommand command) {
        // 유스케이스 구현
        User user = userRepository.findByEmail(command.email())
            .orElseThrow(() -> new UserNotFoundException());
            
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        
        String token = tokenProvider.generateToken(user.getId());
        return new LoginResult(token, user);
    }
}
```

### Adapter Layer 규칙

#### ✅ Web Adapter (Controller) 패턴
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final LoginUseCase loginUseCase; // Port 의존
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var command = new LoginCommand(request.email(), request.password());
        var result = loginUseCase.login(command);
        return ResponseEntity.ok(LoginResponse.from(result));
    }
}
```

#### ✅ Persistence Adapter 패턴
```java
@Component
public class UserRepositoryAdapter implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    
    @Override
    public User save(User user) {
        UserJpaEntity entity = UserJpaEntity.from(user);
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
            .map(UserJpaEntity::toDomain);
    }
}
```

## 🔄 데이터 흐름 규칙

### Inbound Flow (Request)
```
HTTP Request → Controller → UseCase → Domain → Port → Adapter → External System
```

### Outbound Flow (Response)  
```
External System → Adapter → Port → Domain → UseCase → Controller → HTTP Response
```

### 매핑 규칙
1. **Controller**: DTO ↔ Command/Query
2. **UseCase**: Command/Query ↔ Domain
3. **Adapter**: Domain ↔ External Format

## 🧪 테스트 규칙

### 테스트 피라미드
```
E2E Tests (10%)           # Frontend: Playwright
Integration Tests (30%)   # Backend: @SpringBootTest
Unit Tests (60%)         # Domain: Pure Java Tests
```

### Domain 테스트
```java
class UserTest {
    
    @Test
    void should_create_user_with_valid_email() {
        // given
        Email email = new Email("test@example.com");
        Nickname nickname = new Nickname("testuser");
        
        // when
        User user = new User(1L, email, nickname);
        
        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
    }
    
    @Test
    void should_throw_exception_when_email_is_invalid() {
        // given
        String invalidEmail = "invalid-email";
        
        // when & then
        assertThatThrownBy(() -> new Email(invalidEmail))
            .isInstanceOf(InvalidEmailException.class);
    }
}
```

### Integration 테스트
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class LoginServiceIntegrationTest {
    
    @Autowired
    private LoginUseCase loginUseCase;
    
    @Test
    @Transactional
    @Rollback
    void should_login_successfully_with_valid_credentials() {
        // 통합 테스트 로직
    }
}
```

## 📝 네이밍 규칙

### Java 클래스 네이밍
- **Entity**: `User`, `Challenge`, `Order`
- **Value Object**: `Email`, `Nickname`, `Money`
- **UseCase**: `LoginUseCase`, `CreateChallengeUseCase`
- **Service**: `LoginService`, `CreateChallengeService`
- **Repository**: `UserRepository`, `ChallengeRepository`
- **Adapter**: `UserRepositoryAdapter`, `JwtTokenAdapter`
- **Controller**: `AuthController`, `ChallengeController`

### 메서드 네이밍
- **Command**: `create`, `update`, `delete`, `execute`
- **Query**: `find`, `get`, `search`, `list`, `count`
- **Validation**: `validate`, `check`, `verify`, `ensure`
- **Business Logic**: `can`, `should`, `is`, `has`

### Frontend 네이밍
- **Components**: PascalCase (`LoginForm`, `UserProfile`)
- **Props**: camelCase (`isLoading`, `onSubmit`)
- **Files**: kebab-case (`login-form.tsx`, `user-profile.tsx`)
- **Directories**: kebab-case (`place-order`, `user-profile`)

## 🔐 보안 규칙

### 인증/인가
```java
// ✅ Good - JWT 토큰 검증
@PreAuthorize("hasRole('USER')")
public class SessionController {
    
    @PostMapping("/{sessionId}/orders")
    public ResponseEntity<?> placeOrder(
        @PathVariable Long sessionId,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        // 세션 소유권 검증
        validateSessionOwnership(sessionId, userId);
    }
}

// ❌ Bad - 권한 검증 없음
@PostMapping("/{sessionId}/orders")  
public ResponseEntity<?> placeOrder(@PathVariable Long sessionId) {
    // 권한 검증 없이 처리
}
```

### 데이터 검증
```java
// ✅ Good - 입력 검증
@PostMapping("/orders")
public ResponseEntity<?> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
    // Bean Validation 사용
}

// Request DTO
public record PlaceOrderRequest(
    @NotNull @Positive Long sessionId,
    @NotBlank String instrumentKey,
    @NotNull OrderSide side,
    @Positive Integer quantity,
    @NotNull OrderType orderType,
    @Positive BigDecimal limitPrice
) {}
```

## 🚫 안티패턴 (절대 금지)

### 1. Anemic Domain Model
```java
// ❌ Bad - 로직이 없는 단순 데이터 홀더
public class User {
    private Long id;
    private String email;
    // getter/setter만 존재
}

// ✅ Good - 비즈니스 로직을 포함한 Rich Domain Model
public class User {
    private final Long id;
    private final Email email;
    
    public boolean canParticipateIn(Challenge challenge) {
        // 비즈니스 로직
    }
}
```

### 2. Circular Dependency
```java
// ❌ Bad - 순환 의존성
class UserService {
    @Autowired
    private ChallengeService challengeService; // A → B
}

class ChallengeService {
    @Autowired  
    private UserService userService; // B → A (순환!)
}
```

### 3. God Class
```java
// ❌ Bad - 너무 많은 책임
class ChallengeService {
    public void createChallenge() { }
    public void deleteChallenge() { }
    public void sendEmail() { }
    public void calculateTax() { }
    public void generateReport() { }
    // ... 100개의 메서드
}
```

## 📋 Code Review 체크리스트

### Architecture 검증
- [ ] Domain에 Spring 의존성 없음
- [ ] 의존성 방향 준수 (Domain ← Application ← Adapter)
- [ ] Port/Adapter 패턴 올바른 사용
- [ ] Single Responsibility Principle 준수

### Code Quality 검증  
- [ ] 네이밍 컨벤션 준수
- [ ] 적절한 추상화 레벨
- [ ] 예외 처리 적절함
- [ ] 테스트 코드 작성됨

### 보안 검증
- [ ] 인증/인가 처리됨
- [ ] 입력 검증 처리됨
- [ ] SQL Injection 방지됨
- [ ] 민감한 정보 로깅 없음

---

**⚠️ 중요**: 이 규칙들은 프로젝트의 일관성과 품질을 보장하기 위한 것입니다. 새로운 기능을 추가할 때는 반드시 이 규칙들을 확인하고 따라주세요.

**📅 마지막 업데이트**: 2024-09-11  
**🔄 검토 주기**: 매월