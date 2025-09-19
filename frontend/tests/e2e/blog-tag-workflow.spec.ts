import { test, expect } from '@playwright/test';

/**
 * E2E tests for Blog Tag Creation and Management Workflow
 * Tests the complete CRUD operations for tags
 */

test.describe('Blog Tag Management Workflow', () => {
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

    // Switch to tag management tab
    await page.click('text=태그 관리');
    await expect(page.locator('text=새 태그')).toBeVisible();
  });

  test('should create a new tag successfully', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');

    // Should navigate to tag creation page
    await page.waitForURL('/admin/blog/tags/new');

    // Verify page title and form elements
    await expect(page.locator('h1')).toContainText('새 태그 생성');
    await expect(page.locator('text=태그 정보')).toBeVisible();

    // Fill in tag form
    await page.fill('input[name="name"]', '주식투자');

    // Submit form
    await page.click('button:has-text("저장")');

    // Should redirect back to blog management
    await page.waitForURL('/admin/blog');

    // Verify tag appears in the list
    await page.click('text=태그 관리');
    await expect(page.locator('text=주식투자')).toBeVisible();
  });

  test('should validate required fields and show errors', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    // Try to submit without filling required fields
    await page.click('button:has-text("저장")');

    // Should show validation errors
    await expect(page.locator('text=태그 이름은 필수입니다')).toBeVisible();

    // Fill name with too long text (assuming 50 char limit)
    const longName = 'A'.repeat(60);
    await page.fill('input[name="name"]', longName);
    await page.click('button:has-text("저장")');

    // Should show length validation error
    await expect(page.locator('text=태그 이름은 50자 이하로 입력해주세요')).toBeVisible();
  });

  test('should handle tag creation with Korean text', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    // Test Korean text input
    await page.fill('input[name="name"]', '펀드투자');

    // Verify input accepts Korean characters
    await expect(page.locator('input[value="펀드투자"]')).toBeVisible();

    // Submit form
    await page.click('button:has-text("저장")');

    // Should redirect back to blog management
    await page.waitForURL('/admin/blog');

    // Verify Korean tag appears correctly
    await page.click('text=태그 관리');
    await expect(page.locator('text=펀드투자')).toBeVisible();
  });

  test('should handle tag creation API errors gracefully', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    // Fill valid form data
    await page.fill('input[name="name"]', '테스트태그');

    // Submit form
    const submitButton = page.locator('button:has-text("저장")');
    await submitButton.click();

    // Button should show loading state
    await expect(page.locator('button:has-text("저장 중...")')).toBeVisible();

    // Wait for completion (success or error)
    await page.waitForTimeout(3000);

    // Should either succeed (redirect) or show error message
    const currentUrl = page.url();
    if (currentUrl.includes('/admin/blog/tags/new')) {
      // Still on form page, check for error messages
      await expect(page.locator('[role="alert"], .MuiAlert-root')).toBeVisible();
    } else {
      // Successful redirect
      expect(currentUrl).toContain('/admin/blog');
    }
  });

  test('should cancel tag creation and return to management', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    // Fill some data
    await page.fill('input[name="name"]', '취소할태그');

    // Click cancel button
    await page.click('button:has-text("취소")');

    // Should return to blog management
    await page.waitForURL('/admin/blog');

    // Should be on tag management tab
    await expect(page.locator('text=태그 관리')).toBeVisible();
    await expect(page.locator('text=새 태그')).toBeVisible();
  });

  test('should display tag list with proper information', async ({ page }) => {
    // Create a test tag first (if not already created)
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    await page.fill('input[name="name"]', '테스트태그목록');
    await page.click('button:has-text("저장")');

    // Return to tag management
    await page.waitForURL('/admin/blog');
    await page.click('text=태그 관리');

    // Verify tag list displays properly
    await expect(page.locator('text=테스트태그목록')).toBeVisible();

    // Check for popular tags section
    await expect(page.locator('text=인기 태그')).toBeVisible();
  });

  test('should handle empty tag list gracefully', async ({ page }) => {
    // If no tags exist, should show appropriate message
    const noTagsMessage = page.locator('text=태그가 없습니다');

    // Wait for either tags to load or empty message
    await page.waitForTimeout(2000);

    try {
      await expect(noTagsMessage).toBeVisible({ timeout: 5000 });
    } catch {
      // Tags exist, that's fine too
      await expect(page.locator('text=인기 태그')).toBeVisible();
    }
  });

  test('should maintain form state when validation fails', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    // Fill form data
    await page.fill('input[name="name"]', '부분데이터태그');

    // Clear required field to trigger validation
    await page.fill('input[name="name"]', '');

    // Submit form
    await page.click('button:has-text("저장")');

    // Should show validation error
    await expect(page.locator('text=태그 이름은 필수입니다')).toBeVisible();

    // Form should still be on the page
    await expect(page.locator('text=새 태그 생성')).toBeVisible();
    await expect(page.locator('input[name="name"]')).toBeVisible();
  });

  test('should display form help text and placeholders', async ({ page }) => {
    // Click new tag button
    await page.click('text=새 태그');
    await page.waitForURL('/admin/blog/tags/new');

    // Check for help text and placeholder
    await expect(page.locator('text=투자 주제나 카테고리를 나타내는 키워드를 입력하세요')).toBeVisible();

    // Check for placeholder in input field
    const nameInput = page.locator('input[name="name"]');
    await expect(nameInput).toHaveAttribute('placeholder', '예: 주식투자');

    // Check required field indicator
    await expect(page.locator('text=* 표시된 필드는 필수 입력 항목입니다.')).toBeVisible();
  });

  test('should create multiple tags and display them in list', async ({ page }) => {
    const tagNames = ['주식', 'ETF', '펀드', 'REIT'];

    // Create multiple tags
    for (const tagName of tagNames) {
      await page.click('text=새 태그');
      await page.waitForURL('/admin/blog/tags/new');

      await page.fill('input[name="name"]', tagName);
      await page.click('button:has-text("저장")');
      await page.waitForURL('/admin/blog');

      // Return to tag management for next iteration
      await page.click('text=태그 관리');
    }

    // Verify all tags appear in the list
    for (const tagName of tagNames) {
      await expect(page.locator(`text=${tagName}`)).toBeVisible();
    }

    // Check that tag count is updated
    await expect(page.locator('text=태그 목록')).toBeVisible();
  });
});