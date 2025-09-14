import { test, expect } from '@playwright/test';

test.describe('User Management', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate directly to user management (skip auth for now)
    await page.goto('http://localhost:3000/admin/users');
  });

  test.describe('User List Page', () => {
    test('should display user list page correctly', async ({ page }) => {
      // Check page title and description
      await expect(page.locator('h4')).toContainText('사용자 관리');
      await expect(page.locator('text=시스템 사용자를 관리하고 역할 및 권한을 설정하세요')).toBeVisible();

      // Check statistics cards
      await expect(page.locator('text=총 사용자')).toBeVisible();
      await expect(page.locator('text=활성 사용자')).toBeVisible();
      await expect(page.locator('text=관리자')).toBeVisible();
      await expect(page.locator('text=정지된 사용자')).toBeVisible();

      // Check search and filter section
      await expect(page.locator('input[placeholder*="사용자 이름, 이메일로 검색"]')).toBeVisible();
      await expect(page.locator('text=역할')).toBeVisible();
      await expect(page.locator('text=상태')).toBeVisible();

      // Check add user button
      await expect(page.locator('button:has-text("사용자 추가")')).toBeVisible();
    });

    test('should filter users by role', async ({ page }) => {
      // Open role filter
      await page.click('text=역할');
      await page.click('text=모든 역할');

      // Select ADMIN role
      await page.click('text=관리자');

      // Check that filter is applied - admin users should be shown
      await expect(page.locator('text=관리자').first()).toBeVisible();
    });

    test('should filter users by status', async ({ page }) => {
      // Open status filter
      await page.click('text=상태');
      await page.click('text=모든 상태');

      // Select ACTIVE status
      await page.click('text=활성');

      // Check that filter is applied
      await expect(page.locator('[data-testid="status-chip"]:has-text("활성")').first()).toBeVisible();
    });

    test('should search users', async ({ page }) => {
      // Search for specific user
      await page.fill('input[placeholder*="사용자 이름, 이메일로 검색"]', '김투자');

      // Check search results
      await expect(page.locator('text=김투자').first()).toBeVisible();
    });

    test('should navigate to user detail page', async ({ page }) => {
      // Click on the first user row (excluding header)
      await page.click('tbody tr:first-child');

      // Check navigation to detail page
      await expect(page.url()).toMatch(/\/admin\/users\/\d+/);
      await expect(page.locator('h4')).toContainText('사용자 상세 정보');
    });

    test('should open user action menu', async ({ page }) => {
      // Click on more actions button for first user
      await page.click('tbody tr:first-child button[aria-label*="more"]');

      // Check menu items
      await expect(page.locator('text=수정')).toBeVisible();
      await expect(page.locator('text=정지')).toBeVisible();
      await expect(page.locator('text=삭제')).toBeVisible();
    });

    test('should toggle bulk select mode', async ({ page }) => {
      // Toggle bulk select mode
      await page.click('text=일괄 선택 모드');

      // Check that bulk select is enabled
      await expect(page.locator('input[type="checkbox"]').first()).toBeVisible();
    });
  });

  test.describe('User Detail Page', () => {
    test('should display user details correctly', async ({ page }) => {
      // Navigate to a specific user detail page
      await page.goto('http://localhost:3000/admin/users/1');

      // Check page elements
      await expect(page.locator('h4')).toContainText('사용자 상세 정보');

      // Check user avatar and basic info
      await expect(page.locator('[data-testid="user-avatar"]')).toBeVisible();

      // Check action buttons
      await expect(page.locator('button:has-text("수정")')).toBeVisible();
      await expect(page.locator('button:has-text("정지")')).toBeVisible();
      await expect(page.locator('button:has-text("삭제")')).toBeVisible();

      // Check tabs
      await expect(page.locator('text=챌린지 내역')).toBeVisible();
      await expect(page.locator('text=활동 로그')).toBeVisible();
      await expect(page.locator('text=계정 설정')).toBeVisible();
    });

    test('should switch between tabs correctly', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1');

      // Check default tab (Challenge History)
      await expect(page.locator('text=참여한 챌린지')).toBeVisible();

      // Switch to Activity Log tab
      await page.click('text=활동 로그');
      await expect(page.locator('text=최근 활동 내역')).toBeVisible();

      // Switch to Account Settings tab
      await page.click('text=계정 설정');
      await expect(page.locator('text=계정 설정 및 권한')).toBeVisible();
    });

    test('should navigate to edit page', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1');

      // Click edit button
      await page.click('button:has-text("수정")');

      // Check navigation to edit page
      await expect(page.url()).toMatch(/\/admin\/users\/\d+\/edit/);
      await expect(page.locator('h4')).toContainText('사용자 정보 수정');
    });

    test('should show suspend confirmation dialog', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1');

      // Click suspend button
      await page.click('button:has-text("정지")');

      // Check confirmation dialog
      await expect(page.locator('text=사용자 정지')).toBeVisible();
      await expect(page.locator('button:has-text("취소")')).toBeVisible();
      await expect(page.locator('button:has-text("확인")')).toBeVisible();
    });

    test('should show delete confirmation dialog', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1');

      // Click delete button
      await page.click('button:has-text("삭제")');

      // Check confirmation dialog
      await expect(page.locator('text=사용자 삭제')).toBeVisible();
      await expect(page.locator('text=되돌릴 수 없습니다')).toBeVisible();
      await expect(page.locator('button:has-text("취소")')).toBeVisible();
      await expect(page.locator('button:has-text("확인")')).toBeVisible();
    });
  });

  test.describe('User Edit Page', () => {
    test('should display user edit form with existing data', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Check page title
      await expect(page.locator('h4')).toContainText('사용자 정보 수정');

      // Check form sections
      await expect(page.locator('text=기본 정보')).toBeVisible();
      await expect(page.locator('text=계정 설정')).toBeVisible();
      await expect(page.locator('text=권한 설정')).toBeVisible();
      await expect(page.locator('text=관리자 노트')).toBeVisible();

      // Check that form is pre-populated
      await expect(page.locator('input[label="사용자 이름"]')).not.toHaveValue('');
      await expect(page.locator('input[label="이메일"]')).not.toHaveValue('');
    });

    test('should upload profile photo', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Check profile photo upload button exists
      await expect(page.locator('button:has-text("프로필 사진 변경")')).toBeVisible();
    });

    test('should validate required fields', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Clear required field
      await page.fill('input[label="사용자 이름"]', '');

      // Try to save
      await page.click('button:has-text("변경사항 저장")');

      // Check validation error
      await expect(page.locator('text=사용자 이름을 입력해주세요')).toBeVisible();
    });

    test('should validate email format', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Enter invalid email
      await page.fill('input[label="이메일"]', 'invalid-email');

      // Try to save
      await page.click('button:has-text("변경사항 저장")');

      // Check validation error
      await expect(page.locator('text=올바른 이메일 형식을 입력해주세요')).toBeVisible();
    });

    test('should change user role', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Change role to ADMIN
      await page.click('text=역할');
      await page.click('li:has-text("관리자")');

      // Check that role selection has changed
      await expect(page.locator('text=관리자').first()).toBeVisible();
    });

    test('should toggle permissions', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Toggle email verification
      const emailSwitch = page.locator('text=이메일 인증됨').locator('..').locator('input[type="checkbox"]');
      await emailSwitch.click();

      // Toggle trading permission
      const tradingSwitch = page.locator('text=거래 허용').locator('..').locator('input[type="checkbox"]');
      await tradingSwitch.click();

      // Toggle data export permission
      const exportSwitch = page.locator('text=데이터 내보내기 허용').locator('..').locator('input[type="checkbox"]');
      await exportSwitch.click();
    });

    test('should show password reset confirmation', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Click password reset button
      await page.click('button:has-text("비밀번호 재설정 이메일 발송")');

      // Check confirmation dialog
      await expect(page.locator('text=비밀번호 재설정')).toBeVisible();
      await expect(page.locator('text=이메일을 발송하시겠습니까')).toBeVisible();
    });

    test('should save changes successfully', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Make a change
      await page.fill('input[label="사용자 이름"]', 'Updated User Name');

      // Save changes
      await page.click('button:has-text("변경사항 저장")');

      // Check success message
      await expect(page.locator('text=사용자 정보가 성공적으로 수정되었습니다')).toBeVisible();
    });

    test('should show confirmation when canceling with changes', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Make a change
      await page.fill('input[label="사용자 이름"]', 'Changed Name');

      // Try to cancel
      await page.click('button:has-text("취소")');

      // Check confirmation dialog
      await expect(page.locator('text=변경사항 취소')).toBeVisible();
      await expect(page.locator('text=저장되지 않습니다')).toBeVisible();
    });
  });

  test.describe('User Creation Page', () => {
    test('should display user creation form correctly', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Check page title
      await expect(page.locator('h4')).toContainText('새 사용자 추가');

      // Check form sections
      await expect(page.locator('text=기본 정보')).toBeVisible();
      await expect(page.locator('text=비밀번호 설정')).toBeVisible();
      await expect(page.locator('text=계정 설정')).toBeVisible();
      await expect(page.locator('text=권한 설정')).toBeVisible();

      // Check quick start section
      await expect(page.locator('text=빠른 시작')).toBeVisible();
      await expect(page.locator('button:has-text("일반 사용자 예시")')).toBeVisible();
      await expect(page.locator('button:has-text("관리자 예시")')).toBeVisible();
      await expect(page.locator('button:has-text("테스트 사용자 예시")')).toBeVisible();
    });

    test('should load example user data', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Click on regular user example
      await page.click('button:has-text("일반 사용자 예시")');

      // Check that form is populated
      await expect(page.locator('input[label="사용자 이름"]')).toHaveValue('새로운사용자');
      await expect(page.locator('input[label="사용자명"]')).toHaveValue('new_user');
      await expect(page.locator('input[label="이메일"]')).toHaveValue('newuser@example.com');
    });

    test('should generate random password', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Click generate password button
      await page.click('button:has-text("자동 생성")');

      // Check that password fields are populated
      const passwordField = page.locator('input[label="비밀번호"]');
      const confirmPasswordField = page.locator('input[label="비밀번호 확인"]');

      await expect(passwordField).not.toHaveValue('');
      await expect(confirmPasswordField).not.toHaveValue('');

      // Check that passwords match
      const password = await passwordField.inputValue();
      const confirmPassword = await confirmPasswordField.inputValue();
      expect(password).toBe(confirmPassword);
    });

    test('should toggle password visibility', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Enter password
      await page.fill('input[label="비밀번호"]', 'testpassword');

      // Toggle password visibility
      await page.click('button[aria-label="toggle password visibility"]');

      // Check that password is visible
      await expect(page.locator('input[label="비밀번호"][type="text"]')).toBeVisible();
    });

    test('should validate required fields', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Try to save without filling required fields
      await page.click('button:has-text("사용자 생성")');

      // Check validation error
      await expect(page.locator('text=사용자 이름을 입력해주세요')).toBeVisible();
    });

    test('should validate password confirmation', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Fill basic info
      await page.fill('input[label="사용자 이름"]', 'Test User');
      await page.fill('input[label="사용자명"]', 'test_user');
      await page.fill('input[label="이메일"]', 'test@example.com');

      // Enter mismatched passwords
      await page.fill('input[label="비밀번호"]', 'password1');
      await page.fill('input[label="비밀번호 확인"]', 'password2');

      // Try to save
      await page.click('button:has-text("사용자 생성")');

      // Check validation error
      await expect(page.locator('text=비밀번호가 일치하지 않습니다')).toBeVisible();
    });

    test('should validate email format', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Fill form with invalid email
      await page.fill('input[label="사용자 이름"]', 'Test User');
      await page.fill('input[label="사용자명"]', 'test_user');
      await page.fill('input[label="이메일"]', 'invalid-email');
      await page.fill('input[label="비밀번호"]', 'password123');
      await page.fill('input[label="비밀번호 확인"]', 'password123');

      // Try to save
      await page.click('button:has-text("사용자 생성")');

      // Check validation error
      await expect(page.locator('text=올바른 이메일 형식을 입력해주세요')).toBeVisible();
    });

    test('should validate username format', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Fill form with invalid username
      await page.fill('input[label="사용자 이름"]', 'Test User');
      await page.fill('input[label="사용자명"]', 'invalid user@name');
      await page.fill('input[label="이메일"]', 'test@example.com');
      await page.fill('input[label="비밀번호"]', 'password123');
      await page.fill('input[label="비밀번호 확인"]', 'password123');

      // Try to save
      await page.click('button:has-text("사용자 생성")');

      // Check validation error
      await expect(page.locator('text=영문, 숫자, 언더스코어(_)만 사용할 수 있습니다')).toBeVisible();
    });

    test('should create user successfully', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Fill valid form data
      await page.fill('input[label="사용자 이름"]', 'New Test User');
      await page.fill('input[label="사용자명"]', 'new_test_user');
      await page.fill('input[label="이메일"]', 'newtest@example.com');
      await page.fill('input[label="비밀번호"]', 'password123!');
      await page.fill('input[label="비밀번호 확인"]', 'password123!');

      // Submit form
      await page.click('button:has-text("사용자 생성")');

      // Check success message
      await expect(page.locator('text=새 사용자가 성공적으로 생성되었습니다')).toBeVisible();
    });

    test('should toggle creation options', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users/new');

      // Toggle welcome email option
      const welcomeEmailSwitch = page.locator('text=환영 이메일 발송').locator('..').locator('input[type="checkbox"]');
      await welcomeEmailSwitch.click();

      // Toggle password change requirement
      const passwordChangeSwitch = page.locator('text=첫 로그인시 비밀번호 변경 요구').locator('..').locator('input[type="checkbox"]');
      await passwordChangeSwitch.click();
    });
  });

  test.describe('Navigation and Breadcrumbs', () => {
    test('should navigate between pages correctly', async ({ page }) => {
      // Start at users list
      await page.goto('http://localhost:3000/admin/users');

      // Go to create new user
      await page.click('button:has-text("사용자 추가")');
      await expect(page.url()).toContain('/admin/users/new');

      // Navigate back to users list
      await page.click('text=사용자 목록으로');
      await expect(page.url()).toContain('/admin/users');
    });

    test('should maintain filter state when navigating back', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users');

      // Apply search filter
      await page.fill('input[placeholder*="사용자 이름, 이메일로 검색"]', 'test');

      // Navigate to user detail
      await page.click('tbody tr:first-child');

      // Navigate back
      await page.click('text=사용자 목록으로');

      // Check that filter is maintained
      await expect(page.locator('input[placeholder*="사용자 이름, 이메일로 검색"]')).toHaveValue('test');
    });
  });

  test.describe('Responsive Design', () => {
    test('should work correctly on mobile devices', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 });

      await page.goto('http://localhost:3000/admin/users');

      // Check mobile layout
      await expect(page.locator('h4')).toBeVisible();
      await expect(page.locator('button:has-text("사용자 추가")')).toBeVisible();

      // Check that statistics cards stack vertically
      const cards = page.locator('[data-testid="stats-card"]');
      await expect(cards).toHaveCount(4);
    });

    test('should work correctly on tablet devices', async ({ page }) => {
      // Set tablet viewport
      await page.setViewportSize({ width: 768, height: 1024 });

      await page.goto('http://localhost:3000/admin/users/new');

      // Check tablet layout
      await expect(page.locator('h4')).toBeVisible();
      await expect(page.locator('text=기본 정보')).toBeVisible();
    });
  });

  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      // Mock API error
      await page.route('**/api/users', route => {
        route.fulfill({ status: 500, body: 'Server Error' });
      });

      await page.goto('http://localhost:3000/admin/users/new');

      // Fill form and try to save
      await page.fill('input[label="사용자 이름"]', 'Test User');
      await page.fill('input[label="사용자명"]', 'test_user');
      await page.fill('input[label="이메일"]', 'test@example.com');
      await page.fill('input[label="비밀번호"]', 'password123!');
      await page.fill('input[label="비밀번호 확인"]', 'password123!');

      await page.click('button:has-text("사용자 생성")');

      // Check error message
      await expect(page.locator('text=사용자 생성 중 오류가 발생했습니다')).toBeVisible();
    });

    test('should handle loading states correctly', async ({ page }) => {
      // Mock slow API response
      await page.route('**/api/users/1', route => {
        setTimeout(() => {
          route.fulfill({
            status: 200,
            body: JSON.stringify({ id: 1, name: 'Test User' })
          });
        }, 2000);
      });

      await page.goto('http://localhost:3000/admin/users/1/edit');

      // Check loading state
      await expect(page.locator('text=사용자 정보를 불러오는 중')).toBeVisible();
    });
  });

  test.describe('Bulk Operations', () => {
    test('should enable bulk select mode', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users');

      // Enable bulk select mode
      await page.click('text=일괄 선택 모드');

      // Check that checkboxes appear
      await expect(page.locator('input[type="checkbox"]').first()).toBeVisible();
    });

    test('should select multiple users in bulk mode', async ({ page }) => {
      await page.goto('http://localhost:3000/admin/users');

      // Enable bulk select mode
      await page.click('text=일괄 선택 모드');

      // Select first user
      await page.click('tbody tr:first-child input[type="checkbox"]');

      // Select second user
      await page.click('tbody tr:nth-child(2) input[type="checkbox"]');

      // Check that users are selected
      const selectedCheckboxes = page.locator('tbody input[type="checkbox"]:checked');
      await expect(selectedCheckboxes).toHaveCount(2);
    });
  });
});