import { test, expect } from '@playwright/test';

/**
 * E2E tests for Blog Article Editing and Publishing Workflow
 * Tests editing existing articles and publishing state management
 */

test.describe('Blog Article Editing and Publishing Workflow', () => {
  let articleId: string;

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

    // Create a test article first for editing
    await page.click('text=게시글 관리');
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Create a basic article
    await page.fill('input[name="title"]', '편집 테스트용 게시글');
    await page.fill('textarea[name="summary"]', '이 글은 편집 기능을 테스트하기 위한 글입니다.');
    await page.fill('textarea[name="content"]', '# 초기 내용\n\n이것은 편집될 내용입니다.');

    // Set category (assuming exists from previous tests)
    await page.click('text=카테고리 선택');
    await page.click('text=주식 투자 기초');

    // Save as draft
    await page.click('button:has-text("임시저장")');

    // Extract article ID from URL
    await page.waitForURL(/\/admin\/blog\/articles\/(\d+)\/edit/);
    const url = page.url();
    const match = url.match(/\/admin\/blog\/articles\/(\d+)\/edit/);
    articleId = match ? match[1] : '1';
  });

  test('should edit article content and save changes', async ({ page }) => {
    // Should already be on edit page from beforeEach
    await expect(page.locator('h1')).toContainText('게시글 편집');

    // Verify initial content is loaded
    await expect(page.locator('input[value="편집 테스트용 게시글"]')).toBeVisible();

    // Edit the title
    await page.fill('input[name="title"]', '편집된 제목으로 변경됨');

    // Edit the content
    const newContent = `
# 편집된 내용

## 새로 추가된 섹션

이것은 편집된 내용입니다.

### 추가 정보
- 항목 1
- 항목 2
- 항목 3

**중요한 정보**가 추가되었습니다.
    `.trim();

    await page.fill('textarea[name="content"]', newContent);

    // Edit summary
    await page.fill('textarea[name="summary"]', '편집된 요약 내용입니다. 더 상세한 정보가 포함되었습니다.');

    // Save changes
    await page.click('button:has-text("저장")');

    // Verify changes are saved
    await page.waitForTimeout(2000);
    await expect(page.locator('input[value="편집된 제목으로 변경됨"]')).toBeVisible();
    await expect(page.locator('textarea:has-text("편집된 요약 내용입니다")')).toBeVisible();
  });

  test('should change article status from draft to published', async ({ page }) => {
    // Should already be on edit page
    await expect(page.locator('text=초안')).toBeVisible();

    // Change status to published
    await page.click('[name="status"]');
    await page.click('text=게시됨');

    // Save changes
    await page.click('button:has-text("저장")');

    // Verify status changed
    await page.waitForTimeout(2000);
    await expect(page.locator('text=게시됨')).toBeVisible();
  });

  test('should publish article directly with publish button', async ({ page }) => {
    // Should already be on edit page with draft status
    await expect(page.locator('text=초안')).toBeVisible();

    // Click publish button directly
    await page.click('button:has-text("게시하기")');

    // Verify status changed to published
    await page.waitForTimeout(2000);
    await expect(page.locator('text=게시됨')).toBeVisible();

    // Verify publish button changed to unpublish
    await expect(page.locator('button:has-text("게시 취소")')).toBeVisible();
  });

  test('should unpublish published article', async ({ page }) => {
    // First publish the article
    await page.click('button:has-text("게시하기")');
    await page.waitForTimeout(2000);
    await expect(page.locator('text=게시됨')).toBeVisible();

    // Now unpublish it
    await page.click('button:has-text("게시 취소")');

    // Verify status changed back to draft
    await page.waitForTimeout(2000);
    await expect(page.locator('text=초안')).toBeVisible();
    await expect(page.locator('button:has-text("게시하기")')).toBeVisible();
  });

  test('should edit SEO fields and save', async ({ page }) => {
    // Expand SEO section
    await page.click('text=SEO 설정 (Google AdSense 최적화)');

    // Fill SEO fields
    await page.fill('input[name="seoTitle"]', '편집된 SEO 제목 | 투자 가이드');
    await page.fill('textarea[name="metaDescription"]', '편집된 메타 설명입니다. 150자 내외로 최적화된 설명입니다.');
    await page.fill('input[name="seoKeywords"]', '편집, 테스트, 투자, 주식');

    // Fill Open Graph fields
    await page.fill('input[name="ogTitle"]', '편집된 OG 제목');
    await page.fill('textarea[name="ogDescription"]', '소셜 미디어용 편집된 설명');

    // Save changes
    await page.click('button:has-text("저장")');

    // Verify SEO fields are saved
    await page.waitForTimeout(2000);
    await page.click('text=SEO 설정 (Google AdSense 최적화)');
    await expect(page.locator('input[value="편집된 SEO 제목 | 투자 가이드"]')).toBeVisible();
  });

  test('should add and remove tags', async ({ page }) => {
    // Find tag input
    const tagInput = page.locator('input[placeholder="태그를 검색하고 선택하세요"]');

    // Add a tag (assuming tags exist from previous tests)
    await tagInput.fill('주식');
    await page.click('text=주식투자');

    // Verify tag chip appears
    await expect(page.locator('.MuiChip-root:has-text("주식투자")')).toBeVisible();

    // Add another tag
    await tagInput.fill('ETF');
    // If tag doesn't exist, it should still be selectable
    await page.click('text=ETF'); // Or create if needed

    // Save changes
    await page.click('button:has-text("저장")');

    // Remove a tag by clicking its delete button
    await page.click('.MuiChip-root:has-text("주식투자") .MuiChip-deleteIcon');

    // Verify tag is removed
    await expect(page.locator('.MuiChip-root:has-text("주식투자")')).not.toBeVisible();

    // Save again
    await page.click('button:has-text("저장")');
  });

  test('should toggle featured status', async ({ page }) => {
    // Initially should not be featured
    await expect(page.locator('input[name="featured"]:checked')).not.toBeVisible();

    // Check featured
    await page.check('input[name="featured"]');

    // Save changes
    await page.click('button:has-text("저장")');

    // Verify featured is saved
    await page.waitForTimeout(2000);
    await expect(page.locator('input[name="featured"]:checked')).toBeVisible();

    // Uncheck featured
    await page.uncheck('input[name="featured"]');

    // Save again
    await page.click('button:has-text("저장")');

    // Verify not featured
    await page.waitForTimeout(2000);
    await expect(page.locator('input[name="featured"]:checked')).not.toBeVisible();
  });

  test('should change difficulty level', async ({ page }) => {
    // Change difficulty from beginner to advanced
    await page.click('[name="difficulty"]');
    await page.click('text=고급');

    // Save changes
    await page.click('button:has-text("저장")');

    // Verify difficulty changed
    await page.waitForTimeout(2000);
    await expect(page.locator('text=고급')).toBeVisible();
  });

  test('should delete article with confirmation', async ({ page }) => {
    // Click delete button
    await page.click('button:has-text("삭제")');

    // Should show confirmation dialog
    await expect(page.locator('text="편집 테스트용 게시글" 게시글을 정말 삭제하시겠습니까?')).toBeVisible();
    await expect(page.locator('text=이 작업은 되돌릴 수 없습니다.')).toBeVisible();

    // Cancel first
    await page.click('button:has-text("취소")');

    // Dialog should close
    await expect(page.locator('text="편집 테스트용 게시글" 게시글을 정말 삭제하시겠습니까?')).not.toBeVisible();

    // Try delete again and confirm
    await page.click('button:has-text("삭제")');
    await page.click('button:has-text("삭제"):last-child'); // Confirm button

    // Should redirect to blog management
    await page.waitForURL('/admin/blog');

    // Verify we're back on article management
    await expect(page.locator('text=게시글 관리')).toBeVisible();
  });

  test('should validate required fields during edit', async ({ page }) => {
    // Clear title (required field)
    await page.fill('input[name="title"]', '');

    // Try to save
    await page.click('button:has-text("저장")');

    // Should show validation error
    await expect(page.locator('text=제목은 필수입니다')).toBeVisible();

    // Clear content (required field)
    await page.fill('textarea[name="content"]', '');

    // Try to save
    await page.click('button:has-text("저장")');

    // Should show both validation errors
    await expect(page.locator('text=제목은 필수입니다')).toBeVisible();
    await expect(page.locator('text=내용은 필수입니다')).toBeVisible();
  });

  test('should handle Korean content editing', async ({ page }) => {
    // Edit with Korean content
    await page.fill('input[name="title"]', '한국어 제목으로 편집됨');

    const koreanContent = `
# 한국어 컨텐츠 편집

## 수정된 내용

이것은 **한국어**로 편집된 내용입니다.

### 목록
1. 첫 번째 항목
2. 두 번째 항목
3. 세 번째 항목

*기울임체* 및 **굵은 글씨** 테스트
    `;

    await page.fill('textarea[name="content"]', koreanContent);

    // Save changes
    await page.click('button:has-text("저장")');

    // Verify Korean content is saved properly
    await page.waitForTimeout(2000);
    await expect(page.locator('input[value="한국어 제목으로 편집됨"]')).toBeVisible();
  });

  test('should return to article list from edit page', async ({ page }) => {
    // Click back/return button (assuming there's a way to navigate back)
    await page.click('text=목록으로', { timeout: 5000 }).catch(async () => {
      // If no direct back button, navigate via breadcrumb or menu
      await page.click('text=게시글 관리');
    });

    // Should be back on blog management page
    await page.waitForURL('/admin/blog');
    await expect(page.locator('text=게시글 관리')).toBeVisible();

    // Should see the article in the list
    await expect(page.locator('text=편집 테스트용 게시글')).toBeVisible();
  });
});