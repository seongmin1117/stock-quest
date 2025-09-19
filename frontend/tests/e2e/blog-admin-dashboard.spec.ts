import { test, expect } from '@playwright/test';

/**
 * E2E tests for Blog Admin Dashboard Access
 * Tests the main blog management interface functionality
 */

test.describe('Blog Admin Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the admin login page first
    await page.goto('/auth/login');

    // Wait for login form to be visible
    await expect(page.locator('form')).toBeVisible();
  });

  test('should allow admin user to access blog dashboard', async ({ page }) => {
    // Fill in admin credentials (assuming admin user exists)
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');

    // Submit login form
    await page.click('button[type="submit"]');

    // Wait for redirect to admin dashboard
    await page.waitForURL('/admin');

    // Verify admin dashboard loads
    await expect(page.locator('h1')).toContainText('관리자 대시보드');

    // Look for blog management navigation
    await expect(page.locator('text=블로그 관리')).toBeVisible();

    // Click on blog management menu
    await page.click('text=블로그 관리');

    // Should navigate to blog admin page
    await page.waitForURL('/admin/blog');

    // Verify blog admin dashboard elements
    await expect(page.locator('h1')).toContainText('블로그 관리');

    // Check for main dashboard tabs
    await expect(page.locator('text=대시보드')).toBeVisible();
    await expect(page.locator('text=게시글 관리')).toBeVisible();
    await expect(page.locator('text=카테고리 관리')).toBeVisible();
    await expect(page.locator('text=태그 관리')).toBeVisible();
  });

  test('should display analytics section with stats', async ({ page }) => {
    // Perform admin login (same as above)
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin');

    // Navigate to blog management
    await page.click('text=블로그 관리');
    await page.waitForURL('/admin/blog');

    // Check analytics cards are present
    await expect(page.locator('text=전체 게시글')).toBeVisible();
    await expect(page.locator('text=게시된 글')).toBeVisible();
    await expect(page.locator('text=초안')).toBeVisible();
    await expect(page.locator('text=추천 글')).toBeVisible();

    // Check for category and tag counts
    await expect(page.locator('text=카테고리 수')).toBeVisible();
    await expect(page.locator('text=태그 수')).toBeVisible();
  });

  test('should show article management section', async ({ page }) => {
    // Perform admin login
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin');

    // Navigate to blog management
    await page.click('text=블로그 관리');
    await page.waitForURL('/admin/blog');

    // Switch to article management tab
    await page.click('text=게시글 관리');

    // Check for new article button
    await expect(page.locator('text=새 게시글 작성')).toBeVisible();

    // Check for articles table headers (even if empty)
    await expect(page.locator('text=제목')).toBeVisible();
    await expect(page.locator('text=카테고리')).toBeVisible();
    await expect(page.locator('text=상태')).toBeVisible();
    await expect(page.locator('text=작성일')).toBeVisible();

    // Should show "작성된 게시글이 없습니다" when no articles exist
    await expect(page.locator('text=작성된 게시글이 없습니다')).toBeVisible();
  });

  test('should show category management section', async ({ page }) => {
    // Perform admin login
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin');

    // Navigate to blog management
    await page.click('text=블로그 관리');
    await page.waitForURL('/admin/blog');

    // Switch to category management tab
    await page.click('text=카테고리 관리');

    // Check for new category button
    await expect(page.locator('text=새 카테고리 생성')).toBeVisible();

    // Check for categories table headers
    await expect(page.locator('text=카테고리명')).toBeVisible();
    await expect(page.locator('text=게시글 수')).toBeVisible();
    await expect(page.locator('text=생성일')).toBeVisible();

    // Should show "카테고리가 없습니다" when no categories exist
    await expect(page.locator('text=카테고리가 없습니다')).toBeVisible();
  });

  test('should show tag management section', async ({ page }) => {
    // Perform admin login
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin');

    // Navigate to blog management
    await page.click('text=블로그 관리');
    await page.waitForURL('/admin/blog');

    // Switch to tag management tab
    await page.click('text=태그 관리');

    // Check for new tag button
    await expect(page.locator('text=새 태그 생성')).toBeVisible();

    // Check for tags section
    await expect(page.locator('text=인기 태그')).toBeVisible();

    // Should show "태그가 없습니다" when no tags exist
    await expect(page.locator('text=태그가 없습니다')).toBeVisible();
  });

  test('should handle unauthorized access gracefully', async ({ page }) => {
    // Try to access blog admin without login
    await page.goto('/admin/blog');

    // Should redirect to login page
    await page.waitForURL('/auth/login');

    // Should show login form
    await expect(page.locator('form')).toBeVisible();

    // Should show appropriate message (if any)
    await expect(page.locator('input[name="email"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
  });

  test('should display navigation breadcrumbs correctly', async ({ page }) => {
    // Perform admin login
    await page.fill('[name="email"]', 'admin@example.com');
    await page.fill('[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin');

    // Navigate to blog management
    await page.click('text=블로그 관리');
    await page.waitForURL('/admin/blog');

    // Check for proper page title and breadcrumbs
    await expect(page.locator('h1')).toContainText('블로그 관리');

    // Verify we're in the admin section
    await expect(page.getByRole('navigation')).toContainText('관리자');
  });
});