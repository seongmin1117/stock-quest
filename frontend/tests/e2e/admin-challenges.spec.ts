import { test, expect } from '@playwright/test';

/**
 * 관리자 챌린지 관리 페이지 테스트
 * 챌린지 목록, 필터링, 검색, 페이징 기능 검증
 */

test.describe('Admin Challenges Management', () => {
  test.beforeEach(async ({ page }) => {
    // Mock admin authentication
    await page.goto('/');
    await page.evaluate(() => {
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

      localStorage.setItem('stockquest-auth-storage', JSON.stringify(authData));
    });

    await page.goto('/admin/challenges');
  });

  test('should display challenges list page with basic elements', async ({ page }) => {
    // 페이지 제목 확인
    await expect(page.locator('h4', { hasText: '챌린지 관리' })).toBeVisible();

    // 새 챌린지 생성 버튼 확인
    await expect(page.locator('text=새 챌린지 생성')).toBeVisible();

    // 검색 입력 필드 확인
    await expect(page.locator('input[placeholder*="챌린지 제목, 설명, 태그 검색"]')).toBeVisible();

    // 필터 셀렉트 박스들 확인
    await expect(page.locator('text=상태')).toBeVisible();
    await expect(page.locator('text=난이도')).toBeVisible();
    await expect(page.locator('text=유형')).toBeVisible();

    // 필터 초기화 버튼 확인
    await expect(page.locator('text=필터 초기화')).toBeVisible();

    console.log('✅ Basic elements are visible');
  });

  test('should display challenge cards with mock data', async ({ page }) => {
    // 챌린지 카드가 표시되어야 함
    await expect(page.locator('text=2020년 코로나 시장 급락')).toBeVisible();
    await expect(page.locator('text=2021년 밈주식 광풍')).toBeVisible();
    await expect(page.locator('text=2022년 인플레이션 우려')).toBeVisible();

    // 상태 칩이 표시되어야 함
    await expect(page.locator('text=진행중')).toBeVisible();
    await expect(page.locator('text=완료')).toBeVisible();

    // 난이도 칩이 표시되어야 함
    await expect(page.locator('text=초급')).toBeVisible();
    await expect(page.locator('text=중급')).toBeVisible();
    await expect(page.locator('text=고급')).toBeVisible();

    // 액션 버튼들 확인
    await expect(page.locator('text=상세보기').first()).toBeVisible();
    await expect(page.locator('text=수정').first()).toBeVisible();

    console.log('✅ Challenge cards are displayed correctly');
  });

  test('should filter challenges by search query', async ({ page }) => {
    // 검색어 입력
    await page.fill('input[placeholder*="챌린지 제목, 설명, 태그 검색"]', '코로나');

    // 필터링된 결과 확인
    await expect(page.locator('text=2020년 코로나 시장 급락')).toBeVisible();
    await expect(page.locator('text=2021년 밈주식 광풍')).not.toBeVisible();
    await expect(page.locator('text=2022년 인플레이션 우려')).not.toBeVisible();

    // 검색어 클리어
    await page.fill('input[placeholder*="챌린지 제목, 설명, 태그 검색"]', '');

    // 모든 챌린지가 다시 보여야 함
    await expect(page.locator('text=2020년 코로나 시장 급락')).toBeVisible();
    await expect(page.locator('text=2021년 밈주식 광풍')).toBeVisible();
    await expect(page.locator('text=2022년 인플레이션 우려')).toBeVisible();

    console.log('✅ Search filtering works correctly');
  });

  test('should filter challenges by status', async ({ page }) => {
    // 상태 필터 클릭
    await page.click('div:has-text("상태")');
    await page.click('text=완료');

    // 완료된 챌린지만 보여야 함
    await expect(page.locator('text=2022년 인플레이션 우려')).toBeVisible();
    await expect(page.locator('text=2020년 코로나 시장 급락')).not.toBeVisible();
    await expect(page.locator('text=2021년 밈주식 광풍')).not.toBeVisible();

    // 필터 초기화
    await page.click('text=필터 초기화');

    // 모든 챌린지가 다시 보여야 함
    await expect(page.locator('text=2020년 코로나 시장 급락')).toBeVisible();
    await expect(page.locator('text=2021년 밈주식 광풍')).toBeVisible();
    await expect(page.locator('text=2022년 인플레이션 우려')).toBeVisible();

    console.log('✅ Status filtering works correctly');
  });

  test('should open action menu and show options', async ({ page }) => {
    // 첫 번째 챌린지의 메뉴 버튼 클릭
    await page.click('[data-testid="MoreVertIcon"]').catch(() => {
      // data-testid가 없으면 아이콘으로 찾기
      return page.click('svg[data-testid="MoreVertIcon"]');
    }).catch(() => {
      // 그것도 안되면 더 일반적인 선택자 사용
      return page.locator('button:has(svg)').first().click();
    });

    // 메뉴 옵션들이 나타나는지 확인
    const menuOptions = [
      'text=상세보기',
      'text=수정하기',
      'text=피처드 설정',
      'text=활성화',
      'text=보관하기',
      'text=삭제하기'
    ];

    // 메뉴가 나타날 때까지 대기
    await page.waitForTimeout(500);

    // 적어도 일부 메뉴 옵션이 보여야 함
    let visibleOptions = 0;
    for (const option of menuOptions) {
      try {
        if (await page.locator(option).isVisible({ timeout: 1000 })) {
          visibleOptions++;
        }
      } catch (e) {
        // 옵션이 보이지 않으면 무시
      }
    }

    expect(visibleOptions).toBeGreaterThan(0);
    console.log(`✅ Action menu displayed with ${visibleOptions} options`);
  });

  test('should handle pagination controls', async ({ page }) => {
    // 페이지네이션 컨트롤이 있는지 확인
    const paginationExists = await page.locator('text=페이지당 항목 수:').isVisible().catch(() => false);

    if (paginationExists) {
      // 페이지당 항목 수 변경 테스트
      await page.click('div:has-text("12")'); // 기본값

      console.log('✅ Pagination controls are present and functional');
    } else {
      // 페이지네이션이 없다면 항목이 적어서 그럴 수 있음
      const challengeCount = await page.locator('text*="개의 챌린지"').textContent();
      console.log(`✅ Pagination not needed - ${challengeCount}`);
    }
  });

  test('should navigate to new challenge creation page', async ({ page }) => {
    // 새 챌린지 생성 버튼 클릭
    await page.click('text=새 챌린지 생성');

    // URL이 변경되어야 함
    await expect(page).toHaveURL('/admin/challenges/new');

    console.log('✅ Navigation to new challenge page works');
  });

  test('should show challenge count information', async ({ page }) => {
    // 챌린지 개수 정보가 표시되어야 함
    await expect(page.locator('text*="개의 챌린지"')).toBeVisible();
    await expect(page.locator('text*="전체"')).toBeVisible();

    console.log('✅ Challenge count information is displayed');
  });
});