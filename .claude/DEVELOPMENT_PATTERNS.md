# 🔧 StockQuest 개발 패턴 가이드

## 🎯 새 기능 개발 프로세스

### 1. Backend 기능 개발 순서 (Strict)

#### Step 1: Domain Layer 작성
```java
// 1. Entity 작성
public class Portfolio {
    private final Long id;
    private final Long sessionId;
    private final List<Position> positions;
    
    public Portfolio(Long id, Long sessionId) {
        this.id = id;
        this.sessionId = sessionId;
        this.positions = new ArrayList<>();
    }
    
    // 비즈니스 로직
    public Money calculateTotalValue(Map<String, Price> currentPrices) {
        // 포트폴리오 총 가치 계산
    }
    
    public boolean canPlaceOrder(Order order, Money availableBalance) {
        // 주문 가능 여부 검증
    }
}

// 2. Value Objects 작성
public class Position {
    private final String instrumentKey;
    private final Integer quantity;
    private final Money averagePrice;
    
    public Money calculateUnrealizedPnL(Price currentPrice) {
        // 미실현 손익 계산
    }
}

// 3. Port Interface 작성
public interface PortfolioRepository {
    Portfolio save(Portfolio portfolio);
    Optional<Portfolio> findBySessionId(Long sessionId);
    List<Portfolio> findByUserId(Long userId);
}
```

#### Step 2: Application Layer 작성
```java
// 1. Command/Query 클래스
public record GetPortfolioQuery(
    Long sessionId,
    Long userId
) {}

public record GetPortfolioResult(
    Long portfolioId,
    Long sessionId,
    Money totalValue,
    Money unrealizedPnL,
    List<PositionInfo> positions
) {}

// 2. UseCase Interface
public interface GetPortfolioUseCase {
    GetPortfolioResult getPortfolio(GetPortfolioQuery query);
}

// 3. Service Implementation
@Service
@Transactional(readOnly = true)
public class GetPortfolioService implements GetPortfolioUseCase {
    
    private final PortfolioRepository portfolioRepository;
    private final MarketDataRepository marketDataRepository;
    
    @Override
    public GetPortfolioResult getPortfolio(GetPortfolioQuery query) {
        // 1. 권한 검증
        validateUserAccess(query.sessionId(), query.userId());
        
        // 2. 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findBySessionId(query.sessionId())
            .orElseThrow(() -> new PortfolioNotFoundException());
        
        // 3. 현재 가격 조회
        Map<String, Price> currentPrices = getCurrentPrices(portfolio.getInstruments());
        
        // 4. 결과 계산
        return calculatePortfolioResult(portfolio, currentPrices);
    }
}
```

#### Step 3: Adapter Layer 작성
```java
// 1. Web Adapter (Controller)
@RestController
@RequestMapping("/api/sessions/{sessionId}/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    
    private final GetPortfolioUseCase getPortfolioUseCase;
    
    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio(
        @PathVariable Long sessionId,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var query = new GetPortfolioQuery(sessionId, userId);
        var result = getPortfolioUseCase.getPortfolio(query);
        
        return ResponseEntity.ok(PortfolioResponse.from(result));
    }
}

// 2. Persistence Adapter
@Component
public class PortfolioRepositoryAdapter implements PortfolioRepository {
    
    private final PortfolioJpaRepository jpaRepository;
    
    @Override
    public Portfolio save(Portfolio portfolio) {
        PortfolioJpaEntity entity = PortfolioJpaEntity.from(portfolio);
        PortfolioJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}
```

#### Step 4: 테스트 작성
```java
// 1. Domain Test
class PortfolioTest {
    
    @Test
    void should_calculate_total_value_correctly() {
        // given
        Portfolio portfolio = createPortfolio();
        Map<String, Price> prices = createCurrentPrices();
        
        // when
        Money totalValue = portfolio.calculateTotalValue(prices);
        
        // then
        assertThat(totalValue).isEqualTo(Money.of(1500000));
    }
}

// 2. Integration Test
@SpringBootTest
class GetPortfolioServiceIntegrationTest {
    
    @Test
    @Transactional
    void should_return_portfolio_with_current_values() {
        // given
        Portfolio portfolio = createAndSavePortfolio();
        
        // when
        GetPortfolioResult result = getPortfolioUseCase.getPortfolio(
            new GetPortfolioQuery(portfolio.getSessionId(), userId));
        
        // then
        assertThat(result.totalValue()).isPositive();
    }
}
```

### 2. Frontend 기능 개발 순서

#### Step 1: Entities Layer
```typescript
// types.ts
export interface Portfolio {
  portfolioId: number;
  sessionId: number;
  totalValue: number;
  unrealizedPnL: number;
  positions: Position[];
}

export interface Position {
  instrumentKey: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  unrealizedPnL: number;
}

// api.ts
export const portfolioApi = {
  getPortfolio: (sessionId: number): Promise<Portfolio> =>
    api.get(`/sessions/${sessionId}/portfolio`),
};
```

#### Step 2: Features Layer
```typescript
// hooks/usePortfolio.ts
export const usePortfolio = (sessionId: number) => {
  return useQuery({
    queryKey: ['portfolio', sessionId],
    queryFn: () => portfolioApi.getPortfolio(sessionId),
    refetchInterval: 5000, // 5초마다 갱신
  });
};

// components/PortfolioSummary.tsx
export const PortfolioSummary: FC<PortfolioSummaryProps> = ({ portfolio }) => {
  return (
    <Card>
      <Typography variant="h6">포트폴리오 요약</Typography>
      <Grid container spacing={2}>
        <Grid item xs={6}>
          <StatCard 
            label="총 가치" 
            value={formatCurrency(portfolio.totalValue)} 
          />
        </Grid>
        <Grid item xs={6}>
          <StatCard 
            label="미실현 손익" 
            value={formatCurrency(portfolio.unrealizedPnL)}
            color={portfolio.unrealizedPnL >= 0 ? 'success' : 'error'}
          />
        </Grid>
      </Grid>
    </Card>
  );
};
```

#### Step 3: Widgets Layer
```typescript
// PortfolioPanel.tsx
export const PortfolioPanel: FC<PortfolioPanelProps> = ({ sessionId }) => {
  const { data: portfolio, isLoading, error } = usePortfolio(sessionId);
  
  if (isLoading) return <PortfolioSkeleton />;
  if (error) return <ErrorMessage error={error} />;
  if (!portfolio) return <EmptyPortfolio />;
  
  return (
    <Paper elevation={2}>
      <PortfolioSummary portfolio={portfolio} />
      <PositionList positions={portfolio.positions} />
      <PortfolioChart data={portfolio} />
    </Paper>
  );
};
```

#### Step 4: App Layer (페이지)
```typescript
// app/sessions/[sessionId]/portfolio/page.tsx
export default function PortfolioPage({ params }: { params: { sessionId: string } }) {
  const sessionId = Number(params.sessionId);
  
  return (
    <Container maxWidth="lg">
      <Typography variant="h4" gutterBottom>
        포트폴리오
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <PortfolioPanel sessionId={sessionId} />
        </Grid>
      </Grid>
    </Container>
  );
}
```

## 🔄 공통 개발 패턴

### 1. 에러 처리 패턴

#### Backend 예외 처리
```java
// 1. Domain Exception
public class PortfolioNotFoundException extends DomainException {
    public PortfolioNotFoundException() {
        super("Portfolio not found", "PORTFOLIO_NOT_FOUND");
    }
}

// 2. Global Exception Handler
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePortfolioNotFound(
        PortfolioNotFoundException ex) {
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
```

#### Frontend 에러 처리
```typescript
// ErrorBoundary.tsx
export class ErrorBoundary extends Component<Props, State> {
  
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // 에러 로깅 서비스로 전송
    errorReportingService.reportError(error, errorInfo);
  }
  
  render() {
    if (this.state.hasError) {
      return <ErrorFallback onReset={() => this.setState({ hasError: false })} />;
    }
    
    return this.props.children;
  }
}

// API 에러 처리
export const handleApiError = (error: AxiosError) => {
  if (error.response?.status === 401) {
    // 인증 만료
    authService.logout();
    router.push('/auth/login');
  } else if (error.response?.status === 403) {
    // 권한 없음
    toast.error('접근 권한이 없습니다.');
  } else {
    // 일반 에러
    toast.error(error.response?.data?.message || '오류가 발생했습니다.');
  }
};
```

### 2. 데이터 검증 패턴

#### Backend 검증
```java
// 1. Request DTO Validation
public record CreateChallengeRequest(
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    String title,
    
    @NotBlank(message = "설명은 필수입니다")
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    String description,
    
    @NotNull(message = "시작일은 필수입니다")
    @Future(message = "시작일은 현재보다 미래여야 합니다")
    LocalDateTime startDate,
    
    @Min(value = 1, message = "배속은 1 이상이어야 합니다")
    @Max(value = 100, message = "배속은 100 이하여야 합니다")
    Integer speedFactor
) {}

// 2. Domain Validation
public class Challenge {
    public Challenge(String title, String description, LocalDateTime startDate) {
        validateTitle(title);
        validateDescription(description);
        validateStartDate(startDate);
        // ...
    }
    
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidChallengeException("제목은 필수입니다");
        }
        if (title.length() > 100) {
            throw new InvalidChallengeException("제목은 100자를 초과할 수 없습니다");
        }
    }
}
```

#### Frontend 검증
```typescript
// Zod Schema
export const createChallengeSchema = z.object({
  title: z.string()
    .min(1, '제목은 필수입니다')
    .max(100, '제목은 100자를 초과할 수 없습니다'),
  
  description: z.string()
    .min(1, '설명은 필수입니다')
    .max(1000, '설명은 1000자를 초과할 수 없습니다'),
    
  startDate: z.date()
    .min(new Date(), '시작일은 현재보다 미래여야 합니다'),
    
  speedFactor: z.number()
    .min(1, '배속은 1 이상이어야 합니다')
    .max(100, '배속은 100 이하여야 합니다')
});

// React Hook Form with Zod
export const useCreateChallengeForm = () => {
  return useForm<CreateChallengeFormData>({
    resolver: zodResolver(createChallengeSchema),
    defaultValues: {
      title: '',
      description: '',
      startDate: new Date(),
      speedFactor: 1
    }
  });
};
```

### 3. 상태 관리 패턴

#### Global State (Zustand)
```typescript
// authStore.ts
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  
  login: async (credentials) => {
    const response = await authApi.login(credentials);
    const { token, user } = response.data;
    
    tokenStorage.setToken(token);
    set({ user, isAuthenticated: true });
  },
  
  logout: () => {
    tokenStorage.removeToken();
    set({ user: null, isAuthenticated: false });
  }
}));

// sessionStore.ts
interface SessionState {
  currentSession: ChallengeSession | null;
  setCurrentSession: (session: ChallengeSession) => void;
  clearSession: () => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  currentSession: null,
  setCurrentSession: (session) => set({ currentSession: session }),
  clearSession: () => set({ currentSession: null })
}));
```

### 4. API 호출 패턴

#### React Query 패턴
```typescript
// queries/challengeQueries.ts
export const challengeQueries = {
  all: ['challenges'] as const,
  lists: () => [...challengeQueries.all, 'list'] as const,
  list: (filters: ChallengeFilters) => 
    [...challengeQueries.lists(), filters] as const,
  details: () => [...challengeQueries.all, 'detail'] as const,
  detail: (id: number) => [...challengeQueries.details(), id] as const,
};

// hooks/useChallenges.ts
export const useChallenges = (filters?: ChallengeFilters) => {
  return useQuery({
    queryKey: challengeQueries.list(filters || {}),
    queryFn: () => challengeApi.getChallenges(filters),
    staleTime: 5 * 60 * 1000, // 5분
  });
};

export const useCreateChallenge = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: challengeApi.createChallenge,
    onSuccess: () => {
      // 챌린지 목록 캐시 무효화
      queryClient.invalidateQueries({ queryKey: challengeQueries.lists() });
    },
    onError: (error) => {
      toast.error('챌린지 생성에 실패했습니다.');
    }
  });
};
```

### 5. 컴포넌트 패턴

#### Compound Component 패턴
```typescript
// ChallengeCard.tsx
interface ChallengeCardProps {
  challenge: Challenge;
  children: ReactNode;
}

export const ChallengeCard: FC<ChallengeCardProps> = ({ challenge, children }) => {
  return (
    <Card>
      <CardContent>
        {children}
      </CardContent>
    </Card>
  );
};

ChallengeCard.Header = ({ title, status }: { title: string; status: string }) => (
  <Box display="flex" justifyContent="space-between" alignItems="center">
    <Typography variant="h6">{title}</Typography>
    <Chip label={status} />
  </Box>
);

ChallengeCard.Body = ({ description }: { description: string }) => (
  <Typography variant="body2" color="text.secondary">
    {description}
  </Typography>
);

ChallengeCard.Actions = ({ children }: { children: ReactNode }) => (
  <CardActions>
    {children}
  </CardActions>
);

// 사용
<ChallengeCard challenge={challenge}>
  <ChallengeCard.Header title={challenge.title} status={challenge.status} />
  <ChallengeCard.Body description={challenge.description} />
  <ChallengeCard.Actions>
    <Button>참여하기</Button>
  </ChallengeCard.Actions>
</ChallengeCard>
```

## 📝 File Templates

### Backend File Templates

#### 1. Entity Template
```java
package com.stockquest.domain.{domain};

import java.time.LocalDateTime;
import java.util.Objects;

public class {EntityName} {
    private final Long id;
    private final {Type} {field};
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public {EntityName}(Long id, {Type} {field}) {
        this.id = id;
        this.{field} = Objects.requireNonNull({field}, "{field} cannot be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean {businessMethod}() {
        // Business logic here
        return true;
    }
    
    // Getters
    public Long getId() { return id; }
    public {Type} get{Field}() { return {field}; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        {EntityName} that = ({EntityName}) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

#### 2. UseCase Template
```java
package com.stockquest.application.{domain}.port.in;

public interface {Action}{Entity}UseCase {
    {Action}{Entity}Result {action}{Entity}({Action}{Entity}Command command);
}

// Command
public record {Action}{Entity}Command(
    Long {field1},
    String {field2}
) {}

// Result  
public record {Action}{Entity}Result(
    Long id,
    String message
) {}
```

### Frontend File Templates

#### 1. Component Template
```typescript
// {ComponentName}.tsx
import { FC } from 'react';
import { Box, Typography } from '@mui/material';

interface {ComponentName}Props {
  {prop}: {PropType};
}

export const {ComponentName}: FC<{ComponentName}Props> = ({ {prop} }) => {
  return (
    <Box>
      <Typography variant="h6">
        {ComponentName}
      </Typography>
      {/* Component content */}
    </Box>
  );
};

// {ComponentName}.test.tsx
import { render, screen } from '@testing-library/react';
import { {ComponentName} } from './{ComponentName}';

describe('{ComponentName}', () => {
  it('should render component', () => {
    render(<{ComponentName} {prop}="test" />);
    
    expect(screen.getByText('{ComponentName}')).toBeInTheDocument();
  });
});
```

#### 2. Hook Template
```typescript
// use{HookName}.ts
import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';

interface Use{HookName}Options {
  {option}: {OptionType};
}

export const use{HookName} = (options: Use{HookName}Options) => {
  const [state, setState] = useState<{StateType}>(initialState);
  
  const query = useQuery({
    queryKey: ['{hookName}', options],
    queryFn: () => api.{apiMethod}(options),
    enabled: !!options.{option},
  });
  
  useEffect(() => {
    // Effect logic
  }, [options]);
  
  return {
    data: query.data,
    isLoading: query.isLoading,
    error: query.error,
    state,
    setState,
  };
};
```

## 🚀 성능 최적화 패턴

### Backend 최적화
```java
// 1. N+1 쿼리 방지
@Query("SELECT c FROM Challenge c JOIN FETCH c.instruments WHERE c.status = :status")
List<Challenge> findActiveWithInstruments(@Param("status") ChallengeStatus status);

// 2. 캐시 활용
@Cacheable(value = "challenges", key = "#id")
public Challenge findById(Long id) {
    return challengeRepository.findById(id)
        .orElseThrow(ChallengeNotFoundException::new);
}

// 3. 페이지네이션
public Page<Challenge> findChallenges(Pageable pageable) {
    return challengeRepository.findAll(pageable);
}
```

### Frontend 최적화
```typescript
// 1. React.memo 사용
export const ChallengeCard = memo<ChallengeCardProps>(({ challenge }) => {
  return (
    <Card>
      {/* Component content */}
    </Card>
  );
});

// 2. useMemo/useCallback 사용
const MemoizedComponent: FC<Props> = ({ items, onSelect }) => {
  const expensiveValue = useMemo(() => {
    return items.reduce((acc, item) => acc + item.value, 0);
  }, [items]);
  
  const handleSelect = useCallback((id: number) => {
    onSelect(id);
  }, [onSelect]);
  
  return <div>{/* Component content */}</div>;
};

// 3. 가상화 (긴 목록)
import { FixedSizeList } from 'react-window';

const VirtualizedList: FC<{ items: Item[] }> = ({ items }) => {
  return (
    <FixedSizeList
      height={400}
      itemCount={items.length}
      itemSize={60}
    >
      {({ index, style }) => (
        <div style={style}>
          <ItemComponent item={items[index]} />
        </div>
      )}
    </FixedSizeList>
  );
};
```

---

**📋 체크리스트**: 새 기능 개발 시 이 패턴들을 따르고 있는지 확인하세요.

**📅 마지막 업데이트**: 2024-09-11  
**🔄 검토 주기**: 매월