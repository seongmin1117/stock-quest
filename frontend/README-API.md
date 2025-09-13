# API 클라이언트 가이드

이 프로젝트는 백엔드의 OpenAPI 스펙으로부터 자동으로 타입 안전한 API 클라이언트를 생성합니다.

## API 클라이언트 재생성

백엔드 API가 변경될 때마다 다음 명령어로 API 클라이언트를 재생성해야 합니다:

```bash
# 백엔드 서버가 실행 중인 상태에서
npm run generate-api
```

## 설정

### Orval 설정 (`orval.config.js`)

- **입력**: `http://localhost:8080/openapi/stockquest-api.yaml` (백엔드에서 제공하는 OpenAPI 스펙)
- **출력**: `src/shared/api/generated/` (자동 생성된 API 클라이언트)
- **클라이언트**: React Query + Axios
- **모킹**: MSW (Mock Service Worker)

## 사용 방법

### 1. 인증 API

```typescript
import { useLogin, useSignup, useGetCurrentUser } from '@/shared/api/generated/인증';

function LoginComponent() {
  const loginMutation = useLogin();
  
  const handleLogin = async (email: string, password: string) => {
    try {
      const response = await loginMutation.mutateAsync({
        data: { email, password }
      });
      // 로그인 성공
      console.log(response.accessToken);
    } catch (error) {
      // 로그인 실패
      console.error(error);
    }
  };

  return (
    // JSX...
  );
}
```

### 2. 챌린지 API

```typescript
import { 
  useGetChallenges, 
  useGetChallengeDetail, 
  useStartChallenge 
} from '@/shared/api/generated/챌린지';

function ChallengeList() {
  const { data: challenges, isLoading } = useGetChallenges({
    page: 0,
    size: 10
  });

  const startChallengeMutation = useStartChallenge();

  const handleStartChallenge = async (challengeId: number) => {
    try {
      const session = await startChallengeMutation.mutateAsync({
        challengeId
      });
      // 챌린지 시작 성공
      console.log('Session ID:', session.sessionId);
    } catch (error) {
      // 챌린지 시작 실패
      console.error(error);
    }
  };

  if (isLoading) return <div>로딩 중...</div>;

  return (
    <div>
      {challenges?.challenges.map(challenge => (
        <div key={challenge.id}>
          <h3>{challenge.title}</h3>
          <button onClick={() => handleStartChallenge(challenge.id)}>
            시작하기
          </button>
        </div>
      ))}
    </div>
  );
}
```

### 3. 세션 및 주문 API

```typescript
import { 
  useGetSessionDetail, 
  usePlaceOrder 
} from '@/shared/api/generated/챌린지세션';

function TradingView({ sessionId }: { sessionId: number }) {
  const { data: session } = useGetSessionDetail({ sessionId });
  const placeOrderMutation = usePlaceOrder();

  const handleBuyOrder = async (instrumentKey: string, quantity: number) => {
    try {
      const order = await placeOrderMutation.mutateAsync({
        sessionId,
        data: {
          instrumentKey,
          side: 'BUY',
          quantity,
          orderType: 'MARKET'
        }
      });
      // 주문 성공
      console.log('Order placed:', order);
    } catch (error) {
      // 주문 실패
      console.error(error);
    }
  };

  return (
    <div>
      <h2>포트폴리오 가치: {session?.totalValue?.toLocaleString()}원</h2>
      <div>
        {session?.positions?.map(position => (
          <div key={position.instrumentKey}>
            {position.instrumentKey}: {position.quantity}주
          </div>
        ))}
      </div>
    </div>
  );
}
```

### 4. 타입 정의 사용

```typescript
import type { 
  AuthResponse, 
  ChallengeDetailResponse, 
  PlaceOrderRequest 
} from '@/shared/api/generated/model';

// 타입 안전한 상태 관리
interface AuthState {
  user: AuthResponse | null;
  token: string | null;
}

// 폼 데이터 타입
type OrderFormData = Pick<PlaceOrderRequest, 'instrumentKey' | 'side' | 'quantity' | 'orderType'>;
```

## 에러 처리

```typescript
import { AxiosError } from 'axios';
import type { ErrorResponse } from '@/shared/api/generated/model';

function handleApiError(error: AxiosError<ErrorResponse>) {
  if (error.response?.data) {
    const errorData = error.response.data;
    console.error(`${errorData.error}: ${errorData.message}`);
    
    // 특정 에러 코드별 처리
    switch (errorData.error) {
      case 'AUTHENTICATION_REQUIRED':
        // 로그인 페이지로 리다이렉트
        break;
      case 'INSUFFICIENT_BALANCE':
        // 잔액 부족 메시지 표시
        break;
      default:
        // 일반적인 에러 메시지 표시
        break;
    }
  }
}
```

## 개발 워크플로우

1. **백엔드 API 변경**: 백엔드에서 새로운 엔드포인트를 추가하거나 기존 API를 수정
2. **OpenAPI 스펙 업데이트**: 백엔드의 `@Operation`, `@Schema` 어노테이션을 통해 자동 업데이트
3. **API 클라이언트 재생성**: `npm run generate-api` 실행
4. **타입 체크**: TypeScript가 자동으로 타입 불일치를 검출
5. **프론트엔드 코드 수정**: 필요한 경우 프론트엔드 코드를 새로운 API에 맞게 수정

## 주의사항

- 백엔드 서버가 실행 중이어야 API 클라이언트를 생성할 수 있습니다
- `src/shared/api/generated/` 폴더의 파일들은 자동 생성되므로 직접 수정하지 마세요
- API 클라이언트 생성 후 TypeScript 에러가 발생하면 기존 코드를 새로운 타입에 맞게 수정해야 합니다
- 개발 환경에서는 MSW 모킹이 활성화되어 있어 실제 백엔드 없이도 개발할 수 있습니다

## 환경별 설정

### 개발 환경
- OpenAPI 스펙: `http://localhost:8080/openapi/stockquest-api.yaml`
- API 베이스 URL: `http://localhost:8080`

### 프로덕션 환경
API 클라이언트 생성은 개발 환경에서만 수행하고, 생성된 파일을 프로덕션에 배포합니다.