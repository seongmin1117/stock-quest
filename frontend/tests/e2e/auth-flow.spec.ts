import { test, expect } from '@playwright/test';

/**
 * 인증 플로우 E2E 테스트
 * 회원가입 → 로그인 → 챌린지 목록 접근 테스트
 */
test.describe('Auth Flow', () => {
  
  test.beforeEach(async ({ page }) => {
    // 로컬 스토리지 초기화
    await page.context().clearCookies();
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
  });

  test('회원가입 후 로그인하여 챌린지 목록에 접근할 수 있어야 한다', async ({ page }) => {
    // 회원가입 페이지로 이동
    await page.goto('/auth/signup');
    
    // 회원가입 폼 작성
    await page.fill('input[name="email"]', 'newuser@example.com');
    await page.fill('input[name="password"]', 'password123');
    await page.fill('input[name="nickname"]', '새로운사용자');
    
    // 회원가입 버튼 클릭
    await page.click('button[type="submit"]');
    
    // 성공 메시지 확인
    await expect(page.locator('text=회원가입이 완료되었습니다')).toBeVisible();
    
    // 로그인 페이지로 리다이렉트 대기
    await page.waitForURL('/auth/login', { timeout: 5000 });
    
    // 로그인 폼 작성
    await page.fill('input[name="email"]', 'test@example.com');
    await page.fill('input[name="password"]', 'password123');
    
    // 로그인 버튼 클릭
    await page.click('button[type="submit"]');
    
    // 챌린지 목록 페이지로 리다이렉트 확인
    await page.waitForURL('/challenges', { timeout: 5000 });
    
    // 챌린지 목록이 로드되는지 확인
    await expect(page.locator('text=챌린지 목록')).toBeVisible();
    await expect(page.locator('text=코로나 급락장 챌린지')).toBeVisible();
  });

  test('로그인 후 인증이 필요한 페이지에 접근할 수 있어야 한다', async ({ page }) => {
    // 로그인 페이지로 이동
    await page.goto('/auth/login');
    
    // 로그인 폼 작성
    await page.fill('input[name="email"]', 'test@example.com');
    await page.fill('input[name="password"]', 'password123');
    
    // 로그인 버튼 클릭
    await page.click('button[type="submit"]');
    
    // 챌린지 목록 페이지 접근 확인
    await page.waitForURL('/challenges');
    await expect(page.locator('text=챌린지 목록')).toBeVisible();
    
    // 인증 상태에서 브라우저 새로고침 후에도 접근 가능한지 확인
    await page.reload();
    await expect(page.locator('text=챌린지 목록')).toBeVisible();
  });

  test('잘못된 로그인 정보로 로그인 시 에러가 표시되어야 한다', async ({ page }) => {
    // 로그인 페이지로 이동
    await page.goto('/auth/login');
    
    // 잘못된 로그인 정보 입력
    await page.fill('input[name="email"]', 'wrong@example.com');
    await page.fill('input[name="password"]', 'wrongpassword');
    
    // 로그인 버튼 클릭
    await page.click('button[type="submit"]');
    
    // 에러 메시지 확인
    await expect(page.locator('text=이메일 또는 비밀번호가 일치하지 않습니다')).toBeVisible();
    
    // 로그인 페이지에 그대로 있는지 확인
    await expect(page).toHaveURL('/auth/login');
  });

  test('인증 없이 보호된 페이지 접근 시 로그인 페이지로 리다이렉트되어야 한다', async ({ page }) => {
    // 인증 없이 챌린지 페이지에 직접 접근 시도
    await page.goto('/challenges');
    
    // 로그인 페이지로 리다이렉트되는지 확인
    await page.waitForURL('/auth/login');
    await expect(page.locator('text=로그인')).toBeVisible();
  });
});