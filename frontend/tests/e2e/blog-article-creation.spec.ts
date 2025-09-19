import { test, expect } from '@playwright/test';

/**
 * E2E tests for Blog Article Creation with SEO Fields
 * Tests the comprehensive article creation workflow including SEO optimization
 */

test.describe('Blog Article Creation with SEO Fields', () => {
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

    // Switch to article management tab
    await page.click('text=게시글 관리');
    await expect(page.locator('text=새 게시글 작성')).toBeVisible();
  });

  test('should create a complete article with all fields', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');

    // Should navigate to article creation page
    await page.waitForURL('/admin/blog/articles/new');

    // Verify page title
    await expect(page.locator('h1')).toContainText('새 게시글 작성');

    // Fill basic information
    await page.fill('input[name="title"]', '주식 투자 초보자를 위한 완벽 가이드');
    await page.fill('textarea[name="summary"]', '주식 투자를 시작하는 초보자들을 위한 종합적인 가이드입니다. 기본 개념부터 실전 투자까지 모든 것을 다룹니다.');

    // Fill content with markdown
    const articleContent = `
# 주식 투자 초보자 가이드

## 1. 주식 투자의 기본 개념

주식 투자는 기업의 소유권을 구매하는 것입니다.

## 2. 투자 전 준비사항

- 투자 목표 설정
- 리스크 관리 계획
- 기본 지식 습득

## 3. 실전 투자 방법

### 3.1 기업 분석

기본적 분석과 기술적 분석을 병행하세요.

### 3.2 포트폴리오 구성

분산 투자를 통해 위험을 관리하세요.
    `.trim();

    await page.fill('textarea[name="content"]', articleContent);

    // Set category (assuming category exists)
    await page.click('text=카테고리 선택');
    await page.click('text=주식 투자 기초'); // Assuming this category exists from previous tests

    // Set difficulty
    await page.click('[name="difficulty"]');
    await page.click('text=초급');

    // Check featured
    await page.check('input[name="featured"]');

    // Test SEO fields
    await page.click('text=SEO 설정 (Google AdSense 최적화)');

    // Fill SEO title
    await page.fill('input[name="seoTitle"]', '주식 투자 초보자 가이드 | 2025년 완벽 투자 전략');

    // Fill meta description
    await page.fill('textarea[name="metaDescription"]', '2025년 주식 투자 초보자를 위한 완벽한 가이드. 기본 개념부터 실전 투자 전략까지 전문가가 직접 설명하는 투자 교육 콘텐츠입니다.');

    // Fill SEO keywords
    await page.fill('input[name="seoKeywords"]', '주식투자, 초보자, 투자가이드, 주식분석, 포트폴리오');

    // Fill canonical URL
    await page.fill('input[name="canonicalUrl"]', 'https://stockquest.com/blog/beginner-stock-investment-guide');

    // Fill schema type
    await page.fill('input[name="schemaType"]', 'Article');

    // Open Graph settings
    await page.fill('input[name="ogTitle"]', '주식 투자 초보자를 위한 완벽 가이드');
    await page.fill('input[name="ogImageUrl"]', 'https://stockquest.com/images/stock-guide-og.jpg');
    await page.fill('textarea[name="ogDescription"]', '주식 투자 기초부터 실전까지 모든 것을 담은 초보자 가이드');

    // Twitter Card settings
    await page.fill('input[name="twitterTitle"]', '주식 투자 완벽 가이드');
    await page.fill('input[name="twitterImageUrl"]', 'https://stockquest.com/images/stock-guide-twitter.jpg');
    await page.fill('input[name="twitterDescription"]', '초보자를 위한 주식 투자 가이드');

    // Verify SEO checkboxes are checked by default
    await expect(page.locator('input[name="indexable"]:checked')).toBeVisible();
    await expect(page.locator('input[name="followable"]:checked')).toBeVisible();

    // Save as draft first
    await page.click('button:has-text("임시저장")');

    // Should redirect to edit page
    await page.waitForURL(/\/admin\/blog\/articles\/\d+\/edit/);

    // Verify article was created and all fields are preserved
    await expect(page.locator('input[value="주식 투자 초보자를 위한 완벽 가이드"]')).toBeVisible();
  });

  test('should validate required fields', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Try to submit without required fields
    await page.click('button:has-text("게시하기")');

    // Should show validation errors
    await expect(page.locator('text=제목은 필수입니다')).toBeVisible();
    await expect(page.locator('text=내용은 필수입니다')).toBeVisible();
    await expect(page.locator('text=카테고리를 선택해주세요')).toBeVisible();
  });

  test('should handle tag selection with autocomplete', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Fill required fields
    await page.fill('input[name="title"]', '태그 테스트 글');
    await page.fill('textarea[name="content"]', '태그 선택 테스트를 위한 글입니다.');

    // Test tag autocomplete (assuming tags exist from previous tests)
    const tagInput = page.locator('input[placeholder="태그를 검색하고 선택하세요"]');
    await tagInput.fill('주식');

    // Should show tag suggestions
    await expect(page.locator('text=주식투자')).toBeVisible();

    // Select a tag
    await page.click('text=주식투자');

    // Verify tag chip appears
    await expect(page.locator('.MuiChip-root:has-text("주식투자")')).toBeVisible();

    // Add another tag
    await tagInput.fill('펀드');
    await page.click('text=펀드투자');
    await expect(page.locator('.MuiChip-root:has-text("펀드투자")')).toBeVisible();
  });

  test('should validate SEO field lengths', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Fill required fields
    await page.fill('input[name="title"]', 'A'.repeat(250)); // Too long (>200 chars)
    await page.fill('textarea[name="summary"]', 'A'.repeat(600)); // Too long (>500 chars)
    await page.fill('textarea[name="content"]', '내용');

    // Try to submit
    await page.click('button:has-text("임시저장")');

    // Should show validation errors
    await expect(page.locator('text=제목은 200자 이하로 입력해주세요')).toBeVisible();
    await expect(page.locator('text=요약은 500자 이하로 입력해주세요')).toBeVisible();
  });

  test('should handle Korean content properly', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Fill with Korean content
    await page.fill('input[name="title"]', '한국 주식시장 투자 전략');
    await page.fill('textarea[name="summary"]', '한국 주식시장의 특성과 투자 전략에 대해 알아봅시다.');

    const koreanContent = `
# 한국 주식시장 특징

## KOSPI와 KOSDAQ의 차이점

### KOSPI (한국종합주가지수)
- 대기업 중심의 시장
- 안정성이 높음

### KOSDAQ (코스닥)
- 중소기업 및 벤처기업 중심
- 성장성이 높지만 변동성도 큼

## 투자 시 고려사항

1. **시장 상황 분석**
2. **기업 펀더멘털 검토**
3. **리스크 관리**
    `;

    await page.fill('textarea[name="content"]', koreanContent);

    // Verify Korean text is properly handled
    await expect(page.locator('input[value="한국 주식시장 투자 전략"]')).toBeVisible();

    // Fill SEO fields with Korean
    await page.click('text=SEO 설정 (Google AdSense 최적화)');
    await page.fill('input[name="seoKeywords"]', '한국주식, 코스피, 코스닥, 투자전략');

    // Submit
    await page.click('button:has-text("임시저장")');

    // Should handle Korean content without issues
    await page.waitForURL(/\/admin\/blog\/articles\/\d+\/edit/);
  });

  test('should publish article immediately', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Fill required fields
    await page.fill('input[name="title"]', '즉시 게시 테스트');
    await page.fill('textarea[name="content"]', '즉시 게시 기능을 테스트합니다.');

    // Select category (assuming exists)
    await page.click('text=카테고리 선택');
    await page.click('text=주식 투자 기초');

    // Click publish immediately
    await page.click('button:has-text("게시하기")');

    // Should redirect to edit page
    await page.waitForURL(/\/admin\/blog\/articles\/\d+\/edit/);

    // Verify status is published
    await expect(page.locator('text=게시됨')).toBeVisible();
  });

  test('should handle form cancel properly', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Fill some data
    await page.fill('input[name="title"]', '취소할 게시글');
    await page.fill('textarea[name="content"]', '이 내용은 저장되지 않아야 합니다.');

    // Click cancel
    await page.click('button:has-text("취소")');

    // Should return to blog management
    await page.waitForURL('/admin/blog');

    // Should be on article management tab
    await expect(page.locator('text=게시글 관리')).toBeVisible();
  });

  test('should maintain form state during validation errors', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Fill partial form
    await page.fill('input[name="title"]', '부분적으로 채운 글');
    await page.fill('textarea[name="summary"]', '이 요약은 유지되어야 합니다.');

    // Expand SEO section and fill some fields
    await page.click('text=SEO 설정 (Google AdSense 최적화)');
    await page.fill('input[name="seoTitle"]', 'SEO 제목도 유지되어야 함');

    // Check featured
    await page.check('input[name="featured"]');

    // Try to submit without content (required field)
    await page.click('button:has-text("게시하기")');

    // Should show validation error but preserve filled fields
    await expect(page.locator('text=내용은 필수입니다')).toBeVisible();
    await expect(page.locator('input[value="부분적으로 채운 글"]')).toBeVisible();
    await expect(page.locator('textarea[value="이 요약은 유지되어야 합니다."]')).toBeVisible();
    await expect(page.locator('input[value="SEO 제목도 유지되어야 함"]')).toBeVisible();
    await expect(page.locator('input[name="featured"]:checked')).toBeVisible();
  });

  test('should display help text for SEO optimization', async ({ page }) => {
    // Click new article button
    await page.click('text=새 게시글 작성');
    await page.waitForURL('/admin/blog/articles/new');

    // Expand SEO section
    await page.click('text=SEO 설정 (Google AdSense 최적화)');

    // Check for help texts
    await expect(page.locator('text=비워두면 기본 제목을 사용합니다')).toBeVisible();
    await expect(page.locator('text=150-160자 권장')).toBeVisible();
    await expect(page.locator('text=쉼표로 구분하여 입력')).toBeVisible();
    await expect(page.locator('text=중복 콘텐츠 방지')).toBeVisible();

    // Check SEO help section
    await expect(page.locator('text=SEO 최적화 팁:')).toBeVisible();
    await expect(page.locator('text=제목에 주요 키워드를 포함하세요')).toBeVisible();
    await expect(page.locator('text=메타 설명은 150-160자로 작성하세요')).toBeVisible();
  });
});