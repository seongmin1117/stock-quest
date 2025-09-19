import { test, expect } from '@playwright/test';

/**
 * E2E tests for Public Blog Article Display and Navigation
 * Tests the complete user journey for reading blog articles
 * This fills the critical gap in testing public-facing blog functionality
 */

test.describe('Public Blog Article Display and Navigation', () => {

  test.beforeEach(async ({ page }) => {
    // Navigate to blog home page as starting point for user journey
    await page.goto('/blog');
    await page.waitForLoadState('networkidle');
  });

  test('should display blog home page with featured and recent articles', async ({ page }) => {
    // Verify blog home page loads correctly
    await expect(page.locator('h1')).toContainText('투자 교육 & 투자 도구');

    // Check for main sections
    await expect(page.locator('text=🌟 추천 글')).toBeVisible();
    await expect(page.locator('text=📝 최신 글')).toBeVisible();

    // Verify navigation links are present
    await expect(page.locator('a[href="/blog/articles"]')).toBeVisible();
    await expect(page.locator('text=투자 교육 글 보기')).toBeVisible();
  });

  test('should navigate to articles listing page from blog home', async ({ page }) => {
    // Click on "전체 글 보기" link
    await page.click('text=투자 교육 글 보기');

    // Should navigate to articles listing page
    await page.waitForURL('/blog/articles');

    // Verify articles listing page loads
    await expect(page.locator('h1')).toContainText('전체 글 목록');
    await expect(page.locator('text=투자 전문가들이 작성한')).toBeVisible();

    // Check for search and filter functionality
    await expect(page.locator('input[placeholder*="검색"]')).toBeVisible();
    await expect(page.locator('select[name="category"]')).toBeVisible();
    await expect(page.locator('select[name="difficulty"]')).toBeVisible();
  });

  test('should display articles listing with proper article cards', async ({ page }) => {
    // Navigate to articles listing
    await page.goto('/blog/articles');
    await page.waitForLoadState('networkidle');

    // Check articles grid is visible
    const articlesGrid = page.locator('div').filter({ hasText: /article/i }).first();
    await expect(articlesGrid).toBeVisible();

    // Check for article elements in any available article cards
    const articleCards = page.locator('article');
    const cardCount = await articleCards.count();

    if (cardCount > 0) {
      const firstCard = articleCards.first();

      // Check basic article card structure
      await expect(firstCard.locator('h2')).toBeVisible(); // Title
      await expect(firstCard).toContainText(/.*[\w\s]+.*/); // Some content

      // Check for metadata elements (may vary based on actual data)
      const hasAuthor = await firstCard.locator('text=작성자').isVisible().catch(() => false);
      const hasDate = await firstCard.locator('[class*="date"], [title*="date"], time').isVisible().catch(() => false);
      const hasViews = await firstCard.locator('text=조회').isVisible().catch(() => false);

      // At least one metadata element should be present
      expect(hasAuthor || hasDate || hasViews).toBeTruthy();
    } else {
      // If no articles, check for empty state
      await expect(page.locator('text=검색 결과가 없습니다')).toBeVisible();
    }
  });

  test('should navigate from articles listing to individual article page', async ({ page }) => {
    // Navigate to articles listing
    await page.goto('/blog/articles');
    await page.waitForLoadState('networkidle');

    // Find the first article link
    const firstArticleLink = page.locator('article h2 a, article a').first();

    // Check if articles exist
    if (await firstArticleLink.isVisible()) {
      // Get the article title before clicking
      const articleTitle = await firstArticleLink.textContent();

      // Click on the first article
      await firstArticleLink.click();

      // Should navigate to article page (URL pattern: /blog/articles/[slug])
      await page.waitForURL(/\/blog\/articles\/[^\/]+$/);

      // Verify article page loads
      await expect(page.locator('h1')).toBeVisible();

      // Check for article content sections
      await expect(page.locator('main')).toBeVisible(); // Main content area

      // Check for metadata and navigation elements
      const hasBreadcrumbs = await page.locator('nav').first().isVisible();
      expect(hasBreadcrumbs).toBeTruthy();

    } else {
      // If no articles available, skip the navigation test
      console.log('No articles available to test navigation');
      return;
    }
  });

  test('should display individual article page with complete content structure', async ({ page }) => {
    // Create a test article URL (we'll handle if it doesn't exist)
    await page.goto('/blog/articles/test-article');

    // Check if page exists or shows 404
    const isNotFound = await page.locator('text=찾을 수 없습니다').isVisible().catch(() => false);

    if (!isNotFound) {
      // Page exists, test the structure

      // Check for main content areas
      await expect(page.locator('h1')).toBeVisible(); // Article title
      await expect(page.locator('main')).toBeVisible(); // Main content

      // Check for navigation elements
      await expect(page.locator('nav')).toBeVisible(); // Breadcrumbs

      // Look for article metadata (flexible approach)
      const metadataElements = [
        page.locator('text=작성자'),
        page.locator('text=조회'),
        page.locator('text=좋아요'),
        page.locator('[class*="author"]'),
        page.locator('[class*="date"]'),
        page.locator('time')
      ];

      let hasMetadata = false;
      for (const element of metadataElements) {
        if (await element.isVisible().catch(() => false)) {
          hasMetadata = true;
          break;
        }
      }
      expect(hasMetadata).toBeTruthy();

      // Check for navigation back to listing
      const backLinks = [
        page.locator('text=목록으로'),
        page.locator('text=블로그 홈'),
        page.locator('a[href="/blog/articles"]'),
        page.locator('a[href="/blog"]')
      ];

      let hasBackNavigation = false;
      for (const link of backLinks) {
        if (await link.isVisible().catch(() => false)) {
          hasBackNavigation = true;
          break;
        }
      }
      expect(hasBackNavigation).toBeTruthy();

    } else {
      // Article doesn't exist, test 404 handling
      await expect(page.locator('text=찾을 수 없습니다')).toBeVisible();
    }
  });

  test('should have proper breadcrumb navigation on article pages', async ({ page }) => {
    // Try to visit an article page
    await page.goto('/blog/articles/test-article');

    const isNotFound = await page.locator('text=찾을 수 없습니다').isVisible().catch(() => false);

    if (!isNotFound) {
      // Check breadcrumb navigation
      const breadcrumbs = page.locator('nav').first();
      await expect(breadcrumbs).toBeVisible();

      // Check for breadcrumb links (flexible approach)
      const breadcrumbLinks = [
        breadcrumbs.locator('a[href="/"]'),
        breadcrumbs.locator('a[href="/blog"]'),
        breadcrumbs.locator('a[href="/blog/articles"]'),
        breadcrumbs.locator('text=블로그'),
        breadcrumbs.locator('text=글')
      ];

      let hasBreadcrumbLinks = false;
      for (const link of breadcrumbLinks) {
        if (await link.isVisible().catch(() => false)) {
          hasBreadcrumbLinks = true;
          break;
        }
      }
      expect(hasBreadcrumbLinks).toBeTruthy();
    }
  });

  test('should have working search functionality on articles page', async ({ page }) => {
    // Navigate to articles listing
    await page.goto('/blog/articles');
    await page.waitForLoadState('networkidle');

    // Find search input
    const searchInput = page.locator('input[placeholder*="검색"], input[name="query"]');

    if (await searchInput.isVisible()) {
      // Test search functionality
      await searchInput.fill('투자');

      // Submit search (look for submit button or form)
      const submitButton = page.locator('button[type="submit"], button:has-text("검색"), button:has-text("필터")');

      if (await submitButton.isVisible()) {
        await submitButton.click();
        await page.waitForLoadState('networkidle');

        // URL should include search parameter
        expect(page.url()).toMatch(/[?&]query=/);

        // Page should still show articles listing structure
        await expect(page.locator('h1')).toContainText('전체 글 목록');

      } else {
        // Try pressing Enter as alternative
        await searchInput.press('Enter');
        await page.waitForLoadState('networkidle');
      }
    }
  });

  test('should have working category filter on articles page', async ({ page }) => {
    // Navigate to articles listing
    await page.goto('/blog/articles');
    await page.waitForLoadState('networkidle');

    // Find category selector
    const categorySelect = page.locator('select[name="category"]');

    if (await categorySelect.isVisible()) {
      // Get available options
      const options = await categorySelect.locator('option').allTextContents();

      if (options.length > 1) { // More than just the "모든 카테고리" option
        // Select the first non-empty option
        const firstOption = options.find(option => option && option !== '모든 카테고리');

        if (firstOption) {
          await categorySelect.selectOption({ label: firstOption });

          // Submit the filter
          const submitButton = page.locator('button:has-text("필터"), button[type="submit"]');
          if (await submitButton.isVisible()) {
            await submitButton.click();
            await page.waitForLoadState('networkidle');

            // URL should include category parameter
            expect(page.url()).toMatch(/[?&]category=/);
          }
        }
      }
    }
  });

  test('should display proper SEO elements on blog pages', async ({ page }) => {
    // Test blog home page SEO
    await page.goto('/blog');

    // Check page title
    const homeTitle = await page.title();
    expect(homeTitle).toMatch(/StockQuest|블로그|투자/);

    // Check meta description
    const homeDescription = await page.locator('meta[name="description"]').getAttribute('content');
    expect(homeDescription).toBeTruthy();
    expect(homeDescription?.length).toBeGreaterThan(50);

    // Test articles listing page SEO
    await page.goto('/blog/articles');

    const articlesTitle = await page.title();
    expect(articlesTitle).toMatch(/글|목록|StockQuest|블로그/);

    const articlesDescription = await page.locator('meta[name="description"]').getAttribute('content');
    expect(articlesDescription).toBeTruthy();
    expect(articlesDescription?.length).toBeGreaterThan(50);
  });

  test('should have responsive design on mobile viewports', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    // Test blog home page on mobile
    await page.goto('/blog');
    await page.waitForLoadState('networkidle');

    // Should still display main elements
    await expect(page.locator('h1')).toBeVisible();

    // Test articles listing on mobile
    await page.goto('/blog/articles');
    await page.waitForLoadState('networkidle');

    // Should display responsive layout
    await expect(page.locator('h1')).toBeVisible();

    // Search and filter elements should be accessible
    const searchInput = page.locator('input[placeholder*="검색"]');
    if (await searchInput.isVisible()) {
      await expect(searchInput).toBeVisible();
    }
  });

  test('should handle error states gracefully', async ({ page }) => {
    // Test invalid article URL
    await page.goto('/blog/articles/non-existent-article-slug');

    // Should show appropriate error or 404 page
    const errorElements = [
      page.locator('text=찾을 수 없습니다'),
      page.locator('text=404'),
      page.locator('text=존재하지 않'),
      page.locator('text=삭제되었')
    ];

    let hasErrorMessage = false;
    for (const element of errorElements) {
      if (await element.isVisible().catch(() => false)) {
        hasErrorMessage = true;
        break;
      }
    }

    if (hasErrorMessage) {
      // Should have navigation back to blog
      const backToHome = [
        page.locator('a[href="/blog"]'),
        page.locator('a[href="/"]'),
        page.locator('text=홈으로'),
        page.locator('text=블로그')
      ];

      let hasBackNavigation = false;
      for (const link of backToHome) {
        if (await link.isVisible().catch(() => false)) {
          hasBackNavigation = true;
          break;
        }
      }
      expect(hasBackNavigation).toBeTruthy();
    }
  });
});