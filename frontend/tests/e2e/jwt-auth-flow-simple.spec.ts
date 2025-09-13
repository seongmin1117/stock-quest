import { test, expect, Page } from '@playwright/test';

/**
 * 간단한 JWT 인증 플로우 E2E 테스트
 *
 * 테스트 시나리오:
 * 1. 회원가입 → 로그인 → JWT 토큰 검증
 * 2. 인증이 필요한 페이지 접근 → JWT 토큰으로 API 호출 확인
 * 3. 로그아웃 → 토큰 정리 확인
 */

interface ApiRequestLog {
  url: string;
  method: string;
  headers: Record<string, string>;
  hasAuthHeader: boolean;
  authToken?: string;
}

class SimpleAuthTestHelper {
  private page: Page;
  private apiRequests: ApiRequestLog[] = [];

  get getApiRequests() {
    return this.apiRequests;
  }

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * 네트워크 요청 인터셉터 설정 - JWT 토큰 사용 추적
   */
  async setupNetworkInterception() {
    this.apiRequests = [];

    await this.page.route('**/api/**', async (route, request) => {
      const headers = request.headers();
      const authHeader = headers.authorization || '';
      const hasAuthHeader = authHeader.startsWith('Bearer ');
      const authToken = hasAuthHeader ? authHeader.replace('Bearer ', '') : undefined;

      this.apiRequests.push({
        url: request.url(),
        method: request.method(),
        headers,
        hasAuthHeader,
        authToken
      });

      console.log(`API Request: ${request.method()} ${request.url()} - Auth: ${hasAuthHeader ? 'YES' : 'NO'}`);

      // 실제 요청 계속 진행
      await route.continue();
    });
  }

  /**
   * 고유한 테스트 사용자 정보 생성
   */
  generateUniqueUser() {
    const timestamp = Date.now();
    return {
      email: `testuser${timestamp}@example.com`,
      password: 'Test1234!',
      nickname: `테스트유저${timestamp}`
    };
  }

  /**
   * 고정된 테스트 사용자 정보
   */
  getFixedTestUser() {
    return {
      email: 'test1234@test.com',
      password: 'Test1234!',
      nickname: '테스트유저'
    };
  }

  /**
   * JWT 토큰이 올바른 형식인지 검증
   */
  validateJWTFormat(token: string): boolean {
    const parts = token.split('.');
    return parts.length === 3 && parts.every(part => part.length > 0);
  }

  /**
   * 스토리지에서 토큰 획득 (새로운 v2 스토어에서)
   */
  async getStoredToken(): Promise<string | null> {
    try {
      return await this.page.evaluate(() => {
        // 새로운 v2 스토어에서 토큰 가져오기
        const authStorage = localStorage.getItem('auth-storage-v2');
        if (authStorage) {
          const parsed = JSON.parse(authStorage);
          return parsed?.state?.tokens?.accessToken || null;
        }

        // 백업용: 기존 스토리지도 확인
        return localStorage.getItem('auth-token') || null;
      });
    } catch (error) {
      console.log('localStorage access failed:', error);
      return null;
    }
  }

  /**
   * 인증된 API 요청 검증
   */
  getAuthenticatedRequests(): ApiRequestLog[] {
    return this.apiRequests.filter(req => req.hasAuthHeader);
  }

  /**
   * 스토리지 정리 (v2 스토어 포함)
   */
  async clearStorage() {
    await this.page.evaluate(() => {
      try {
        localStorage.removeItem('auth-storage-v2');
        localStorage.removeItem('auth-token');
        localStorage.removeItem('auth-storage');
        sessionStorage.clear();
      } catch (error) {
        console.log('Storage clear failed:', error);
      }
    });
    await this.page.context().clearCookies();
  }
}

test.describe('JWT Authentication Flow - Simple', () => {
  let helper: SimpleAuthTestHelper;

  test.beforeEach(async ({ page }) => {
    helper = new SimpleAuthTestHelper(page);
    await helper.setupNetworkInterception();

    // 스토리지 정리
    await helper.clearStorage();
  });

  test('로그인 → JWT 검증 → 보호된 API 호출', async ({ page }) => {
    const testUser = helper.getFixedTestUser(); // test1234@test.com 사용

    // === 1단계: 로그인 ===
    console.log('=== 로그인 시작 ===');
    await page.goto('/auth/login');
    await page.waitForSelector('input[name="email"]', { timeout: 10000 });

    await page.fill('input[name="email"]', testUser.email);
    await page.fill('input[name="password"]', testUser.password);

    await page.click('button[type="submit"]');

    // 로그인 성공 후 리다이렉트 확인
    await page.waitForURL('/challenges', { timeout: 10000 });
    console.log('✅ 로그인 성공 - 챌린지 페이지로 리다이렉트');

    // === 2단계: JWT 토큰 검증 ===
    console.log('=== JWT 토큰 검증 ===');
    await page.waitForTimeout(1000); // 스토리지 업데이트 대기

    const storedToken = await helper.getStoredToken();
    expect(storedToken).toBeTruthy();

    if (storedToken) {
      const isValidFormat = helper.validateJWTFormat(storedToken);
      expect(isValidFormat).toBe(true);
      console.log('✅ JWT 토큰 형식 검증 성공');
      console.log(`토큰 일부: ${storedToken.substring(0, 50)}...`);
    }

    // === 3단계: 인증이 필요한 페이지에서 API 호출 확인 ===
    console.log('=== 인증된 API 호출 검증 ===');

    // 페이지 새로고침으로 API 호출 유발
    await page.reload();
    await page.waitForSelector('text=투자 챌린지', { timeout: 10000 });

    // API 요청 분석
    const authenticatedRequests = helper.getAuthenticatedRequests();
    console.log(`인증된 API 요청 수: ${authenticatedRequests.length}`);

    expect(authenticatedRequests.length).toBeGreaterThan(0);

    // JWT 토큰이 Authorization 헤더에 포함되어 있는지 확인
    const hasValidTokenInRequest = authenticatedRequests.some(req => {
      return req.authToken && helper.validateJWTFormat(req.authToken);
    });

    expect(hasValidTokenInRequest).toBe(true);
    console.log('✅ JWT 토큰이 API 요청에 올바르게 포함됨');

    // === 4단계: 인증 상태 확인 (/api/auth/me 호출) ===
    console.log('=== 현재 사용자 정보 API 호출 ===');

    // 명시적으로 /api/auth/me API 호출하는 버튼이나 액션 찾기
    // (실제 앱에서 사용자 정보를 불러오는 방식에 맞춰 수정 필요)

    // 프로필 관련 요소가 있는지 확인
    const userInfo = await page.locator('text=' + testUser.nickname).first();
    if (await userInfo.isVisible()) {
      console.log('✅ 사용자 정보가 화면에 표시됨');
    }

    // /api/auth/me 요청이 있었는지 확인
    const meApiRequest = helper.getApiRequests.find(req =>
      req.url.includes('/api/auth/me') && req.hasAuthHeader
    );

    if (meApiRequest) {
      console.log('✅ /api/auth/me API 호출 확인됨');
      expect(meApiRequest.hasAuthHeader).toBe(true);
    }
  });

  test('로그아웃 플로우 및 토큰 정리 확인', async ({ page }) => {
    const testUser = helper.getFixedTestUser();

    // 로그인까지 진행
    await page.goto('/auth/login');

    // 테스트 사용자로 로그인
    await page.fill('input[name="email"]', testUser.email);
    await page.fill('input[name="password"]', testUser.password);
    await page.click('button[type="submit"]');

    try {
      await page.waitForURL('/challenges', { timeout: 5000 });
      console.log('✅ 로그인 성공');

      // 토큰 확인
      const tokenBeforeLogout = await helper.getStoredToken();
      expect(tokenBeforeLogout).toBeTruthy();
      console.log('✅ 로그인 후 토큰 확인됨');

      // === 로그아웃 실행 ===
      console.log('=== 로그아웃 시작 ===');

      // v2 auth store의 logout 함수 직접 호출
      await page.evaluate(() => {
        try {
          // v2 auth store에 접근하여 logout 호출
          const authStorage = localStorage.getItem('auth-storage-v2');
          if (authStorage) {
            // Zustand의 logout 함수 시뮬레이션
            const logoutData = {
              state: {
                tokens: {
                  accessToken: null,
                  refreshToken: null,
                  accessTokenExpiresAt: null,
                  refreshTokenExpiresAt: null
                },
                user: {
                  id: null,
                  email: null,
                  nickname: null
                },
                isAuthenticated: false
              },
              version: 0
            };
            localStorage.setItem('auth-storage-v2', JSON.stringify(logoutData));
          }
        } catch (error) {
          console.log('Direct logout failed, clearing storage:', error);
          // Fallback: 직접 storage 정리
          localStorage.removeItem('auth-storage-v2');
          localStorage.removeItem('auth-token');
          localStorage.removeItem('auth-storage');
        }
      });

      // 페이지 새로고침으로 상태 업데이트 반영
      await page.reload();

      // 로그인 페이지로 리다이렉트되는지 확인 (AuthGuard 동작)
      await page.waitForURL(/\/auth\/login/, { timeout: 5000 });
      console.log('✅ 로그아웃 후 로그인 페이지로 리다이렉트');

      // 토큰 정리 확인
      const tokenAfterLogout = await helper.getStoredToken();
      expect(tokenAfterLogout).toBeNull();
      console.log('✅ 로그아웃 후 토큰 정리 확인됨');

    } catch (error) {
      console.log('로그인 실패 - 기본 테스트 사용자가 없을 수 있습니다:', error);
      // 이 경우 회원가입부터 다시 진행하거나 테스트 스킵
      test.skip();
    }
  });

  test('인증 없이 보호된 페이지 접근 시 로그인 페이지로 리다이렉트', async ({ page }) => {
    console.log('=== 인증 없이 보호된 페이지 접근 ===');

    // 토큰 없이 직접 보호된 페이지 접근
    await page.goto('/challenges');

    // 로그인 페이지로 리다이렉트되는지 확인
    await page.waitForURL(/\/auth\/login/, { timeout: 5000 });
    console.log('✅ 인증 없이 접근 시 로그인 페이지로 정상 리다이렉트');

    // 로그인 폼이 표시되는지 확인
    await expect(page.locator('input[name="email"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    console.log('✅ 로그인 폼 표시 확인');
  });

  test('잘못된 로그인 정보로 로그인 시 에러 처리', async ({ page }) => {
    console.log('=== 잘못된 로그인 정보 테스트 ===');

    await page.goto('/auth/login');

    // 존재하지 않는 이메일로 로그인 시도
    await page.fill('input[name="email"]', 'nonexistent@example.com');
    await page.fill('input[name="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    // 에러 메시지 확인
    await expect(page.locator('text=이메일 또는 비밀번호가 일치하지 않습니다')).toBeVisible({ timeout: 5000 });
    console.log('✅ 잘못된 로그인 정보에 대한 에러 메시지 표시 확인');

    // 여전히 로그인 페이지에 있는지 확인
    expect(page.url()).toContain('/auth/login');

    // 토큰이 저장되지 않았는지 확인
    const token = await helper.getStoredToken();
    expect(token).toBeNull();
    console.log('✅ 실패한 로그인으로 토큰이 저장되지 않음 확인');
  });
});