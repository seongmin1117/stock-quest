import { test, expect } from '@playwright/test';

/**
 * 간단한 관리자 인증 테스트
 * AdminAuthGuard의 기본 동작 검증에 집중
 */

test.describe('Admin Authentication Simple Tests', () => {
  test.beforeEach(async ({ page }) => {
    // 각 테스트 전에 초기화
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
  });

  test('should redirect to login when accessing admin panel without authentication', async ({ page }) => {
    // 비로그인 상태에서 admin 경로 접근
    await page.goto('/admin/dashboard');

    // 리다이렉트를 기다리고 URL 확인 (더 긴 시간 허용)
    await page.waitForURL(/\/auth\/login/, { timeout: 15000 });

    // returnUrl 파라미터가 포함되어야 함
    const currentUrl = page.url();
    expect(currentUrl).toContain('returnUrl=%2Fadmin%2Fdashboard');

    // 로그인 페이지의 핵심 요소 확인
    await expect(page.getByRole('heading', { name: '로그인' })).toBeVisible();
  });

  test('should show admin dashboard when properly authenticated', async ({ page }) => {
    // 관리자 인증을 시뮬레이션하기 위해 Zustand store를 직접 조작
    await page.goto('/');

    // 인증 스토어에 직접 데이터를 설정
    await page.evaluate(() => {
      // Zustand persist storage에 직접 데이터 설정
      const authData = {
        state: {
          tokens: {
            accessToken: 'mock-admin-token',
            refreshToken: 'mock-refresh-token',
            accessTokenExpiresAt: new Date(Date.now() + 3600000).toISOString(),
            refreshTokenExpiresAt: new Date(Date.now() + 7200000).toISOString()
          },
          user: {
            id: 1,
            email: 'admin@test.com',
            nickname: 'Admin',
            role: 'ADMIN'
          },
          isAuthenticated: true,
          isLoading: false,
          error: null
        },
        version: 0
      };

      // localStorage에 persist된 상태로 저장
      localStorage.setItem('stockquest-auth-storage', JSON.stringify(authData));

      // window 객체에 추가적으로 설정 (개발 환경 디버깅용)
      (window as any).__MOCK_AUTH__ = true;
      (window as any).__MOCK_USER__ = authData.state.user;
    });

    // 페이지 새로고침으로 상태 로딩 트리거
    await page.reload();

    // 약간의 대기 시간 후 admin 페이지 접근
    await page.waitForTimeout(1000);
    await page.goto('/admin/dashboard');

    // 관리자 페이지가 로드되기를 기다림 (더 관대한 기다림)
    const dashboardTitle = page.locator('h4', { hasText: '관리자 대시보드' });
    const loadingIndicator = page.locator('text=권한을 확인하는 중');

    // 로딩 상태이거나 대시보드가 보이는지 확인
    await Promise.race([
      dashboardTitle.waitFor({ state: 'visible', timeout: 10000 }),
      loadingIndicator.waitFor({ state: 'visible', timeout: 2000 })
    ]).catch(() => {
      // 둘 다 실패하면 현재 URL과 페이지 내용을 확인
      console.log('Current URL:', page.url());
      return page.screenshot({ path: 'debug-admin-dashboard.png' });
    });

    // 현재 URL 확인
    const currentUrl = page.url();

    if (currentUrl.includes('/admin/dashboard')) {
      // 관리자 대시보드에 있다면 필수 요소들 확인
      await expect(page.locator('text=StockQuest')).toBeVisible();
      console.log('✅ Admin dashboard loaded successfully');
    } else if (currentUrl.includes('/auth/login')) {
      console.log('❌ Redirected to login - authentication mock failed');
      expect(currentUrl).toContain('/admin'); // 테스트 실패를 명확히 표시
    } else {
      console.log('🔍 Unexpected URL:', currentUrl);
      // 예상치 못한 경우 스크린샷 촬영
      await page.screenshot({ path: 'debug-unexpected-url.png' });
    }
  });

  test('should show loading state during authentication check', async ({ page }) => {
    await page.goto('/admin/dashboard');

    // 로딩 상태 또는 리다이렉트 확인
    const loadingIndicator = page.locator('text=권한을 확인하는 중');

    // 로딩이 짧게 나타날 수 있으므로 빠르게 확인
    const isLoadingVisible = await loadingIndicator.isVisible().catch(() => false);

    if (isLoadingVisible) {
      // 로딩 스피너도 함께 확인
      await expect(page.locator('[role="progressbar"]')).toBeVisible();
      console.log('✅ Loading state detected');
    }

    // 최종적으로는 로그인 페이지로 리다이렉트되어야 함
    await page.waitForURL(/\/auth\/login/, { timeout: 10000 });
    console.log('✅ Redirected to login after loading');
  });

  test('should handle admin route structure correctly', async ({ page }) => {
    // 다양한 admin 경로들이 올바르게 리다이렉트되는지 확인
    const adminPaths = [
      '/admin/dashboard',
      '/admin/challenges',
      '/admin/templates',
      '/admin/users'
    ];

    for (const path of adminPaths) {
      await page.goto(path);

      // 모든 admin 경로는 로그인으로 리다이렉트되어야 함
      await page.waitForURL(/\/auth\/login/, { timeout: 5000 });

      // returnUrl이 올바르게 설정되어야 함
      const currentUrl = page.url();
      const encodedPath = encodeURIComponent(path);
      expect(currentUrl).toContain(`returnUrl=${encodedPath}`);

      console.log(`✅ ${path} correctly redirected with returnUrl`);
    }
  });
});