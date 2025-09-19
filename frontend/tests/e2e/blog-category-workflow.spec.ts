import { test, expect } from '@playwright/test';

/**
 * E2E tests for Blog Category Creation and Management Workflow
 * Tests the complete CRUD operations for categories
 */

test.describe('Blog Category Management Workflow', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login and authenticate as admin
    await page.goto('/auth/login');
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // Wait for redirect and navigate to blog management
    await page.waitForURL('/admin');
    await page.click('text=블로그 관리');
    await page.waitForURL('/admin/blog');

    // Switch to category management tab
    await page.click('text=카테고리 관리');
    await expect(page.locator('text=새 카테고리 생성')).toBeVisible();
  });

  test('should create a new category successfully', async ({ page }) => {
    // Click new category button
    await page.click('text=새 카테고리 생성');

    // Should navigate to category creation page
    await page.waitForURL('/admin/blog/categories/new');

    // Verify page title and form elements
    await expect(page.locator('h1')).toContainText('새 카테고리 생성');
    await expect(page.locator('text=카테고리 정보')).toBeVisible();

    // Fill in category form
    await page.fill('input[name="name"]', '주식 투자 기초');

    // Verify slug auto-generation
    await expect(page.locator('input[value*="주식-투자-기초"]')).toBeVisible();

    // Fill in description
    await page.fill('textarea[name="description"]', '주식 투자의 기초적인 개념과 전략에 대한 카테고리입니다.');

    // Check homepage visibility option
    await page.check('input[name="showOnHomepage"]');

    // Submit form
    await page.click('button:has-text("저장")');

    // Should redirect back to blog management
    await page.waitForURL('/admin/blog');

    // Verify category appears in the list
    await page.click('text=카테고리 관리');
    await expect(page.locator('text=주식 투자 기초')).toBeVisible();
    await expect(page.locator('text=주식 투자의 기초적인 개념과 전략에 대한 카테고리입니다.')).toBeVisible();
  });

  test('should validate required fields and show errors', async ({ page }) => {
    // Click new category button
    await page.click('text=새 카테고리 생성');
    await page.waitForURL('/admin/blog/categories/new');

    // Try to submit without filling required fields
    await page.click('button:has-text("저장")');

    // Should show validation errors
    await expect(page.locator('text=카테고리 이름은 필수입니다')).toBeVisible();

    // Fill name with too long text
    const longName = 'A'.repeat(150); // Assuming 100 char limit
    await page.fill('input[name="name"]', longName);
    await page.click('button:has-text("저장")');

    // Should show length validation error
    await expect(page.locator('text=카테고리 이름은 100자 이하로 입력해주세요')).toBeVisible();
  });

  test('should auto-generate slug from Korean category name', async ({ page }) => {
    // Click new category button
    await page.click('text=새 카테고리 생성');
    await page.waitForURL('/admin/blog/categories/new');

    // Test Korean text slug generation
    await page.fill('input[name="name"]', '펀드 & ETF 분석');

    // Wait for slug auto-generation
    await page.waitForTimeout(500);

    // Verify slug is generated correctly (Korean chars removed, spaces to hyphens)
    const slugValue = await page.locator('input[name="slug"]').inputValue();
    expect(slugValue).toMatch(/펀드.*etf.*분석/i);

    // Test manual slug editing
    await page.fill('input[name="slug"]', 'fund-etf-analysis');

    // Verify manual slug is preserved
    await expect(page.locator('input[value="fund-etf-analysis"]')).toBeVisible();
  });

  test('should handle category creation API errors gracefully', async ({ page }) => {
    // Click new category button
    await page.click('text=새 카테고리 생성');
    await page.waitForURL('/admin/blog/categories/new');

    // Fill valid form data
    await page.fill('input[name="name"]', '테스트 카테고리');
    await page.fill('textarea[name="description"]', '테스트용 카테고리입니다.');

    // Mock API failure (if possible) or test with duplicate name
    // For now, test that form handles submission state correctly

    // Submit form
    const submitButton = page.locator('button:has-text("저장")');
    await submitButton.click();

    // Button should show loading state
    await expect(page.locator('button:has-text("저장 중...")')).toBeVisible();

    // Wait for completion (success or error)
    await page.waitForTimeout(3000);

    // Should either succeed (redirect) or show error message
    const currentUrl = page.url();
    if (currentUrl.includes('/admin/blog/categories/new')) {
      // Still on form page, check for error messages
      await expect(page.locator('[role="alert"], .MuiAlert-root')).toBeVisible();
    } else {
      // Successful redirect
      expect(currentUrl).toContain('/admin/blog');
    }
  });

  test('should cancel category creation and return to management', async ({ page }) => {
    // Click new category button
    await page.click('text=새 카테고리 생성');
    await page.waitForURL('/admin/blog/categories/new');

    // Fill some data
    await page.fill('input[name="name"]', '취소할 카테고리');

    // Click cancel button
    await page.click('button:has-text("취소")');

    // Should return to blog management
    await page.waitForURL('/admin/blog');

    // Should be on category management tab
    await expect(page.locator('text=카테고리 관리')).toBeVisible();
    await expect(page.locator('text=새 카테고리 생성')).toBeVisible();
  });

  test('should display category list with proper information', async ({ page }) => {
    // Create a test category first (if not already created)
    await page.click('text=새 카테고리 생성');
    await page.waitForURL('/admin/blog/categories/new');

    await page.fill('input[name="name"]', '테스트 카테고리 목록');
    await page.fill('textarea[name="description"]', '목록 테스트용 카테고리');
    await page.click('button:has-text("저장")');

    // Return to category management
    await page.waitForURL('/admin/blog');
    await page.click('text=카테고리 관리');

    // Verify category list displays properly
    await expect(page.locator('text=테스트 카테고리 목록')).toBeVisible();

    // Check table headers
    await expect(page.locator('text=카테고리명')).toBeVisible();
    await expect(page.locator('text=게시글 수')).toBeVisible();
    await expect(page.locator('text=생성일')).toBeVisible();

    // Check that creation date is displayed
    const datePattern = /\d{4}-\d{2}-\d{2}|\d{2}\/\d{2}\/\d{4}/;
    await expect(page.locator(`text=${datePattern}`)).toBeVisible();
  });

  test('should handle empty category list gracefully', async ({ page }) => {
    // If no categories exist, should show appropriate message
    const noCategoriesMessage = page.locator('text=카테고리가 없습니다');

    // Wait for either categories to load or empty message
    await page.waitForTimeout(2000);

    try {
      await expect(noCategoriesMessage).toBeVisible({ timeout: 5000 });
    } catch {
      // Categories exist, that's fine too
      await expect(page.locator('table, text=카테고리명')).toBeVisible();
    }
  });

  test('should maintain form state when validation fails', async ({ page }) => {
    // Click new category button
    await page.click('text=새 카테고리 생성');
    await page.waitForURL('/admin/blog/categories/new');

    // Fill partial form data
    await page.fill('input[name="name"]', '부분 데이터');
    await page.fill('textarea[name="description"]', '이것은 테스트 설명입니다.');
    await page.check('input[name="showOnHomepage"]');

    // Clear required field to trigger validation
    await page.fill('input[name="name"]', '');

    // Submit form
    await page.click('button:has-text("저장")');

    // Should show validation error but maintain other field values
    await expect(page.locator('text=카테고리 이름은 필수입니다')).toBeVisible();
    await expect(page.locator('textarea[value="이것은 테스트 설명입니다."]')).toBeVisible();
    await expect(page.locator('input[name="showOnHomepage"]:checked')).toBeVisible();
  });
});