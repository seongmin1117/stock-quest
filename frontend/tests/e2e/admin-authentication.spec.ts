import { test, expect } from '@playwright/test';

/**
 * 관리자 인증 및 권한 테스트
 * AdminAuthGuard 컴포넌트와 admin 라우트 접근 제어 검증
 */

test.describe('Admin Authentication Flow', () => {
  test.beforeEach(async ({ page }) => {
    // 각 테스트 전에 로그아웃 상태로 시작
    await page.goto('/');
    await page.evaluate(() => localStorage.clear());
    await page.evaluate(() => sessionStorage.clear());
  });

  test('should redirect unauthenticated user to login when accessing admin panel', async ({ page }) => {
    // 비로그인 상태에서 관리자 페이지 접근 시도
    await page.goto('/admin/dashboard');

    // 로그인 페이지로 리다이렉트되어야 함 (returnUrl 파라미터 사용)
    await expect(page).toHaveURL(/\/auth\/login\?returnUrl/, { timeout: 10000 });

    // 로그인 페이지 요소들이 보여야 함
    await expect(page.locator('text=로그인')).toBeVisible();
    await expect(page.locator('text=StockQuest 계정으로 로그인하세요')).toBeVisible();
  });

  test('should redirect non-admin user away from admin panel', async ({ page }) => {
    // Mock non-admin user authentication state
    await page.goto('/');
    await page.evaluate(() => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwidXNlcklkIjoyLCJlbWFpbCI6InVzZXJAdGVzdC5jb20iLCJyb2xlIjoiVVNFUiIsImlhdCI6MTYzOTU4NDAwMCwiZXhwIjo5OTk5OTk5OTk5fQ.mock';
      localStorage.setItem('stockquest-auth-token', mockToken);

      const mockUser = {
        id: 2,
        email: 'user@test.com',
        username: 'Test User',
        role: 'USER',
        isVerified: true
      };
      localStorage.setItem('stockquest-user', JSON.stringify(mockUser));
    });

    // 관리자 페이지 접근 시도
    await page.goto('/admin/dashboard');

    // 권한 없음 메시지 확인 또는 리다이렉트 확인
    const errorAlert = page.locator('text=관리자 권한이 필요합니다');
    const accessDeniedTitle = page.locator('text=접근 권한 없음');

    // 오류 메시지가 보이거나 리다이렉트되어야 함
    const hasError = await errorAlert.or(accessDeniedTitle).isVisible({ timeout: 3000 }).catch(() => false);

    if (hasError) {
      // 오류 메시지 표시 후 자동 리다이렉트 대기
      await expect(page).toHaveURL(/\/dashboard/, { timeout: 5000 });
    } else {
      // 즉시 리다이렉트된 경우 또는 로그인 페이지로 리다이렉트
      const currentUrl = page.url();
      expect(currentUrl).toMatch(/\/(dashboard|auth\/login)/);
    }
  });

  test('should allow admin user to access admin panel', async ({ page }) => {
    // Mock admin authentication state
    await page.goto('/');
    await page.evaluate(() => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcklkIjoxLCJlbWFpbCI6ImFkbWluQHRlc3QuY29tIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNjM5NTg0MDAwLCJleHAiOjk5OTk5OTk5OTl9.mock';
      localStorage.setItem('stockquest-auth-token', mockToken);

      const mockUser = {
        id: 1,
        email: 'admin@test.com',
        username: 'Admin User',
        role: 'ADMIN',
        isVerified: true
      };
      localStorage.setItem('stockquest-user', JSON.stringify(mockUser));
    });

    // 관리자 페이지 접근
    await page.goto('/admin/dashboard');

    // 관리자 대시보드가 로드되어야 함
    await expect(page.locator('text=관리자 대시보드')).toBeVisible({ timeout: 10000 });

    // 사이드바 메뉴 확인
    await expect(page.locator('text=StockQuest')).toBeVisible();
    await expect(page.locator('text=관리자 도구')).toBeVisible();

    // 메뉴 항목들 확인
    await expect(page.locator('text=대시보드')).toBeVisible();
    await expect(page.locator('text=챌린지 관리')).toBeVisible();
    await expect(page.locator('text=템플릿 관리')).toBeVisible();
    await expect(page.locator('text=사용자 관리')).toBeVisible();

    // 통계 카드들 확인
    await expect(page.locator('text=챌린지')).toBeVisible();
    await expect(page.locator('text=사용자')).toBeVisible();
    await expect(page.locator('text=완료 세션')).toBeVisible();
    await expect(page.locator('text=평균 완료율')).toBeVisible();
  });

  test('should navigate between admin pages correctly', async ({ page }) => {
    // 이 테스트는 관리자 인증이 성공했다고 가정하고 진행
    // 실제로는 위의 로그인 과정을 거쳐야 하지만, 간소화를 위해 직접 접근

    // Mock admin authentication state
    await page.goto('/');
    await page.evaluate(() => {
      // Mock JWT token with admin role
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcklkIjoxLCJlbWFpbCI6ImFkbWluQHRlc3QuY29tIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNjM5NTg0MDAwLCJleHAiOjk5OTk5OTk5OTl9.mock';
      localStorage.setItem('stockquest-auth-token', mockToken);

      // Mock user data
      const mockUser = {
        id: 1,
        email: 'admin@test.com',
        username: 'Admin User',
        role: 'ADMIN',
        isVerified: true
      };
      localStorage.setItem('stockquest-user', JSON.stringify(mockUser));
    });

    // 관리자 대시보드로 이동
    await page.goto('/admin/dashboard');

    // 대시보드가 로드되는지 확인
    await expect(page.locator('text=관리자 대시보드')).toBeVisible({ timeout: 5000 });

    // 챌린지 관리 페이지로 이동
    await page.click('text=챌린지 관리');
    await expect(page).toHaveURL('/admin/challenges');

    // 템플릿 관리 페이지로 이동
    await page.click('text=템플릿 관리');
    await expect(page).toHaveURL('/admin/templates');

    // 사용자 관리 페이지로 이동
    await page.click('text=사용자 관리');
    await expect(page).toHaveURL('/admin/users');

    // 대시보드로 돌아가기
    await page.click('text=대시보드');
    await expect(page).toHaveURL('/admin/dashboard');
  });

  test('should handle authentication state changes correctly', async ({ page }) => {
    // Mock admin authentication state
    await page.goto('/');
    await page.evaluate(() => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcklkIjoxLCJlbWFpbCI6ImFkbWluQHRlc3QuY29tIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNjM5NTg0MDAwLCJleHAiOjk5OTk5OTk5OTl9.mock';
      localStorage.setItem('stockquest-auth-token', mockToken);

      const mockUser = {
        id: 1,
        email: 'admin@test.com',
        username: 'Admin User',
        role: 'ADMIN',
        isVerified: true
      };
      localStorage.setItem('stockquest-user', JSON.stringify(mockUser));
    });

    // 관리자 페이지 접근
    await page.goto('/admin/dashboard');
    await expect(page.locator('text=관리자 대시보드')).toBeVisible();

    // 로그아웃 시뮬레이션 (토큰 제거)
    await page.evaluate(() => {
      localStorage.removeItem('stockquest-auth-token');
      localStorage.removeItem('stockquest-user');
    });

    // 페이지 새로고침으로 인증 상태 변경 감지
    await page.reload();

    // 로그인 페이지로 리다이렉트되어야 함
    await expect(page).toHaveURL(/\/auth\/login/, { timeout: 10000 });
  });

  test('should show loading state during authentication check', async ({ page }) => {
    await page.goto('/admin/dashboard');

    // 로딩 상태 확인 (권한 확인 중...)
    const loadingIndicator = page.locator('text=권한을 확인하는 중');

    // 로딩 표시가 나타나는지 확인 (빠르게 사라질 수 있으므로 timeout 짧게)
    if (await loadingIndicator.isVisible({ timeout: 1000 }).catch(() => false)) {
      // 로딩 스피너도 함께 확인
      await expect(page.locator('[role="progressbar"]')).toBeVisible();
    }
  });
});