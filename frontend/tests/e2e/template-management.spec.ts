import { test, expect } from '@playwright/test';

test.describe('Template Management', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to admin login and login
    await page.goto('http://localhost:3000/auth/login');

    // Login with admin credentials
    await page.fill('input[type="email"]', 'admin@example.com');
    await page.fill('input[type="password"]', 'password');
    await page.click('button[type="submit"]');

    // Wait for navigation to admin dashboard
    await page.waitForURL('**/admin');
  });

  test.describe('Template List Page', () => {
    test('should display template list page correctly', async ({ page }) => {
      // Navigate to templates page
      await page.goto('http://localhost:3000/admin/templates');

      // Check page title and description
      await expect(page.locator('h4')).toContainText('템플릿 관리');
      await expect(page.locator('text=챌린지 템플릿을 관리하고 새로운 시나리오를 만들어보세요')).toBeVisible();

      // Check action buttons
      await expect(page.locator('text=새 템플릿 생성')).toBeVisible();
      await expect(page.locator('button:has-text("새 템플릿")')).toBeVisible();
    });

    test('should switch between all view and category view', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates');

      // Check initial state (All View)
      await expect(page.locator('text=전체 보기')).toBeVisible();

      // Switch to Category View
      await page.click('text=카테고리별 보기');
      await expect(page.locator('text=카테고리별 보기')).toBeVisible();

      // Check category sections are visible
      await expect(page.locator('text=시장 급락 시나리오')).toBeVisible();
      await expect(page.locator('text=상승장 전략')).toBeVisible();

      // Switch back to All View
      await page.click('text=전체 보기');
      await expect(page.locator('text=전체 보기')).toBeVisible();
    });

    test('should filter templates by category', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates');

      // Open category filter
      await page.click('button:has-text("카테고리")');

      // Select Market Crash category
      await page.click('text=시장 급락 시나리오');

      // Check that filtering is applied
      await expect(page.locator('text=시장 급락 시나리오').first()).toBeVisible();
    });

    test('should search templates', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates');

      // Search for specific template
      await page.fill('input[placeholder*="템플릿 검색"]', '코로나');

      // Check search results
      await expect(page.locator('text=코로나').first()).toBeVisible();
    });

    test('should navigate to template detail page', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates');

      // Click on the first template card
      await page.click('[data-testid="template-card"]');

      // Check navigation to detail page
      await expect(page.url()).toMatch(/\/admin\/templates\/\d+/);
      await expect(page.locator('h4')).toContainText('템플릿 상세');
    });
  });

  test.describe('Template Detail Page', () => {
    test('should display template details correctly', async ({ page }) => {
      // Navigate directly to a template detail page
      await page.goto('http://localhost:3000/admin/templates/1');

      // Check page elements
      await expect(page.locator('h4')).toContainText('템플릿 상세');
      await expect(page.locator('text=템플릿 정보')).toBeVisible();
      await expect(page.locator('text=생성된 챌린지')).toBeVisible();
      await expect(page.locator('text=설정 및 관리')).toBeVisible();

      // Check action buttons
      await expect(page.locator('button:has-text("챌린지 생성")')).toBeVisible();
      await expect(page.locator('button:has-text("템플릿 수정")')).toBeVisible();
    });

    test('should switch between tabs correctly', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/1');

      // Check default tab (Template Info)
      await expect(page.locator('text=시장 시나리오')).toBeVisible();
      await expect(page.locator('text=학습 목표')).toBeVisible();

      // Switch to Generated Challenges tab
      await page.click('text=생성된 챌린지');
      await expect(page.locator('text=이 템플릿으로 생성된 챌린지')).toBeVisible();

      // Switch to Settings tab
      await page.click('text=설정 및 관리');
      await expect(page.locator('text=템플릿 관리')).toBeVisible();
      await expect(page.locator('button:has-text("복제")')).toBeVisible();
      await expect(page.locator('button:has-text("수정")')).toBeVisible();
    });

    test('should navigate to template edit page', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/1');

      // Click edit button
      await page.click('button:has-text("템플릿 수정")');

      // Check navigation to edit page
      await expect(page.url()).toMatch(/\/admin\/templates\/\d+\/edit/);
      await expect(page.locator('h4')).toContainText('템플릿 수정');
    });
  });

  test.describe('Template Creation Page', () => {
    test('should display template creation form correctly', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/new');

      // Check page title
      await expect(page.locator('h4')).toContainText('새 템플릿 생성');

      // Check form sections
      await expect(page.locator('text=기본 정보')).toBeVisible();
      await expect(page.locator('text=학습 내용')).toBeVisible();
      await expect(page.locator('text=성공 기준')).toBeVisible();
      await expect(page.locator('text=챌린지 기본 설정')).toBeVisible();

      // Check required fields
      await expect(page.locator('input[label="템플릿 이름"]')).toBeVisible();
      await expect(page.locator('textarea[label="템플릿 설명"]')).toBeVisible();
      await expect(page.locator('select[label="카테고리"]')).toBeVisible();
    });

    test('should load example templates', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/new');

      // Click on market crash example
      await page.click('button:has-text("시장 급락 예시")');

      // Check that form is populated
      await expect(page.locator('input[label="템플릿 이름"]')).toHaveValue('2020년 코로나 시장 급락 대응');
      await expect(page.locator('textarea[label="템플릿 설명"]')).toContainText('코로나19로 인한');
    });

    test('should validate required fields', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/new');

      // Try to save without filling required fields
      await page.click('button:has-text("템플릿 생성")');

      // Check validation error
      await expect(page.locator('text=템플릿 이름을 입력해주세요')).toBeVisible();
    });

    test('should create template successfully', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/new');

      // Fill required fields
      await page.fill('input[label="템플릿 이름"]', 'Test Template');
      await page.fill('textarea[label="템플릿 설명"]', 'Test description for template');
      await page.fill('textarea[label="시장 시나리오"]', 'Test market scenario');
      await page.fill('textarea[label="학습 목표"]', '• Test objective 1\n• Test objective 2');

      // Set category
      await page.click('button[role="combobox"]:has-text("시장 급락 시나리오")');
      await page.click('li:has-text("상승장 전략")');

      // Submit form
      await page.click('button:has-text("템플릿 생성")');

      // Check success message
      await expect(page.locator('text=템플릿이 성공적으로 생성되었습니다')).toBeVisible();
    });

    test('should manage tags correctly', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/new');

      // Add a tag
      await page.fill('input[label="태그 추가"]', 'test-tag');
      await page.keyboard.press('Enter');

      // Check tag is added
      await expect(page.locator('text=test-tag')).toBeVisible();

      // Remove the tag
      await page.click('button[aria-label="delete"]:near(:text("test-tag"))');

      // Check tag is removed
      await expect(page.locator('text=test-tag')).not.toBeVisible();
    });

    test('should manage investment instruments correctly', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/new');

      // Check default instruments are selected
      await expect(page.locator('input[type="checkbox"]:checked').first()).toBeVisible();

      // Toggle an instrument
      await page.click('text=채권');

      // Toggle another instrument
      await page.click('text=옵션');
    });
  });

  test.describe('Template Edit Page', () => {
    test('should display template edit form with existing data', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/1/edit');

      // Check page title
      await expect(page.locator('h4')).toContainText('템플릿 수정');

      // Check that form is pre-populated
      await expect(page.locator('input[label="템플릿 이름"]')).not.toHaveValue('');
      await expect(page.locator('textarea[label="템플릿 설명"]')).not.toHaveValue('');
    });

    test('should save template changes successfully', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/1/edit');

      // Modify template name
      await page.fill('input[label="템플릿 이름"]', 'Updated Template Name');

      // Save changes
      await page.click('button:has-text("변경사항 저장")');

      // Check success message
      await expect(page.locator('text=템플릿이 성공적으로 수정되었습니다')).toBeVisible();
    });

    test('should validate form before saving', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/1/edit');

      // Clear required field
      await page.fill('input[label="템플릿 이름"]', '');

      // Try to save
      await page.click('button:has-text("변경사항 저장")');

      // Check validation error
      await expect(page.locator('text=템플릿 이름을 입력해주세요')).toBeVisible();
    });

    test('should cancel editing and return to detail page', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates/1/edit');

      // Click cancel
      await page.click('button:has-text("취소")');

      // Check navigation back to detail page
      await expect(page.url()).toMatch(/\/admin\/templates\/1$/);
      await expect(page.locator('h4')).toContainText('템플릿 상세');
    });
  });

  test.describe('Navigation and Breadcrumbs', () => {
    test('should navigate between pages correctly', async ({ page }) => {
      // Start at templates list
      await page.goto('http://localhost:3000/admin/templates');

      // Go to create new template
      await page.click('button:has-text("새 템플릿")');
      await expect(page.url()).toContain('/admin/templates/new');

      // Navigate back to templates list
      await page.click('text=템플릿 목록으로');
      await expect(page.url()).toContain('/admin/templates');
    });

    test('should maintain navigation state', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/templates');

      // Apply some filters
      await page.fill('input[placeholder*="템플릿 검색"]', 'test');

      // Navigate to detail page
      await page.click('[data-testid="template-card"]');

      // Navigate back
      await page.click('text=템플릿 목록으로');

      // Check that filters are maintained
      await expect(page.locator('input[placeholder*="템플릿 검색"]')).toHaveValue('test');
    });
  });

  test.describe('Responsive Design', () => {
    test('should work correctly on mobile devices', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 });

      await page.goto('http://localhost:3000/admin/templates');

      // Check mobile layout
      await expect(page.locator('h4')).toBeVisible();
      await expect(page.locator('button:has-text("새 템플릿")')).toBeVisible();
    });

    test('should work correctly on tablet devices', async ({ page }) => {
      // Set tablet viewport
      await page.setViewportSize({ width: 768, height: 1024 });

      await page.goto('http://localhost:3000/admin/templates/new');

      // Check tablet layout
      await expect(page.locator('h4')).toBeVisible();
      await expect(page.locator('text=기본 정보')).toBeVisible();
    });
  });

  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      // Mock API error
      await page.route('**/api/templates', route => {
        route.fulfill({ status: 500, body: 'Server Error' });
      });

      await page.goto('http://localhost:3000/admin/templates/new');

      // Fill form and try to save
      await page.fill('input[label="템플릿 이름"]', 'Test Template');
      await page.fill('textarea[label="템플릿 설명"]', 'Test description');
      await page.fill('textarea[label="시장 시나리오"]', 'Test scenario');
      await page.fill('textarea[label="학습 목표"]', '• Test objective');

      await page.click('button:has-text("템플릿 생성")');

      // Check error message
      await expect(page.locator('text=템플릿 생성 중 오류가 발생했습니다')).toBeVisible();
    });

    test('should handle loading states correctly', async ({ page }) => {
      // Mock slow API response
      await page.route('**/api/templates/1', route => {
        setTimeout(() => {
          route.fulfill({
            status: 200,
            body: JSON.stringify({ id: 1, name: 'Test Template' })
          });
        }, 2000);
      });

      await page.goto('http://localhost:3000/admin/templates/1/edit');

      // Check loading state
      await expect(page.locator('text=템플릿 데이터를 불러오는 중')).toBeVisible();
    });
  });
});