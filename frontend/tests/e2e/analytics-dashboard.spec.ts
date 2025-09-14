import { test, expect } from '@playwright/test';

test.describe('Analytics Dashboard E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    // 로그인 전 상태 설정 - 실제로는 인증 토큰을 설정하거나 로그인 과정을 수행
    await page.goto('/admin/analytics');
  });

  test.describe('메인 분석 대시보드', () => {
    test('대시보드 페이지 로드 및 주요 요소 표시', async ({ page }) => {
      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '분석 대시보드' })).toBeVisible();

      // 주요 통계 카드들 확인
      await expect(page.getByText('총 사용자')).toBeVisible();
      await expect(page.getByText('활성 사용자')).toBeVisible();
      await expect(page.getByText('이번 달 가입')).toBeVisible();
      await expect(page.getByText('평균 세션 시간')).toBeVisible();

      // 차트 컨테이너들 확인
      await expect(page.getByText('사용자 증가 트렌드')).toBeVisible();
      await expect(page.getByText('트래픽 분석')).toBeVisible();
      await expect(page.getByText('디바이스별 접속')).toBeVisible();
    });

    test('필터 기능 테스트', async ({ page }) => {
      // 기간 필터 선택
      await page.getByLabel('기간').click();
      await page.getByRole('option', { name: '최근 3개월' }).click();

      // 카테고리 필터 선택
      await page.getByLabel('카테고리').click();
      await page.getByRole('option', { name: '챌린지' }).click();

      // 새로고침 버튼 클릭
      await page.getByRole('button', { name: '새로고침' }).click();

      // 로딩 상태 확인 (잠시 후)
      await page.waitForTimeout(500);
    });

    test('내보내기 기능 테스트', async ({ page }) => {
      // 내보내기 버튼 클릭
      await page.getByRole('button', { name: '내보내기' }).click();

      // 메뉴 옵션들 확인
      await expect(page.getByRole('menuitem', { name: 'PDF로 내보내기' })).toBeVisible();
      await expect(page.getByRole('menuitem', { name: 'Excel로 내보내기' })).toBeVisible();
      await expect(page.getByRole('menuitem', { name: 'CSV로 내보내기' })).toBeVisible();

      // PDF 내보내기 선택
      await page.getByRole('menuitem', { name: 'PDF로 내보내기' }).click();

      // 내보내기 다이얼로그 확인은 실제 구현에 따라 달라질 수 있음
      await page.waitForTimeout(1000);
    });
  });

  test.describe('사용자 행동 분석 페이지', () => {
    test('사용자 행동 분석 페이지 네비게이션', async ({ page }) => {
      // 사용자 행동 분석 페이지로 이동
      await page.goto('/admin/analytics/user-behavior');

      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '사용자 행동 분석' })).toBeVisible();

      // 주요 섹션들 확인
      await expect(page.getByText('사용자 참여 지표')).toBeVisible();
      await expect(page.getByText('코호트 분석')).toBeVisible();
      await expect(page.getByText('사용자 여정 분석')).toBeVisible();
      await expect(page.getByText('디바이스별 사용 현황')).toBeVisible();
    });

    test('사용자 세그먼트 필터링', async ({ page }) => {
      await page.goto('/admin/analytics/user-behavior');

      // 사용자 세그먼트 선택
      await page.getByLabel('사용자 세그먼트').click();
      await page.getByRole('option', { name: '활성 사용자' }).click();

      // 차트가 업데이트되었는지 확인 (실제로는 데이터 변화를 확인)
      await page.waitForTimeout(1000);

      // 새로운 필터에 따른 데이터 표시 확인
      await expect(page.getByText('활성 사용자')).toBeVisible();
    });
  });

  test.describe('챌린지 성과 분석 페이지', () => {
    test('챌린지 성과 페이지 기본 요소', async ({ page }) => {
      await page.goto('/admin/analytics/challenge-performance');

      // 페이지 제목과 설명 확인
      await expect(page.getByRole('heading', { name: '챌린지 성과 분석' })).toBeVisible();
      await expect(page.getByText('챌린지별 성과 지표, 성공률, 참여도 분석')).toBeVisible();

      // 주요 성과 지표 카드들 확인
      await expect(page.getByText('총 챌린지 수')).toBeVisible();
      await expect(page.getByText('활성 챌린지')).toBeVisible();
      await expect(page.getByText('평균 완료율')).toBeVisible();
      await expect(page.getByText('평균 성공률')).toBeVisible();

      // 차트 섹션들 확인
      await expect(page.getByText('월별 챌린지 성과 트렌드')).toBeVisible();
      await expect(page.getByText('난이도별 챌린지 분포')).toBeVisible();
      await expect(page.getByText('카테고리별 성과 분석')).toBeVisible();
    });

    test('챌린지 필터링 및 검색', async ({ page }) => {
      await page.goto('/admin/analytics/challenge-performance');

      // 챌린지 카테고리 필터
      await page.getByLabel('챌린지').click();
      await page.getByRole('option', { name: 'AI 트레이딩' }).click();

      // 기간 필터 변경
      await page.getByLabel('기간').click();
      await page.getByRole('option', { name: '최근 1년' }).click();

      // 필터 적용 후 결과 확인
      await page.waitForTimeout(500);
      await expect(page.getByText('AI 트레이딩')).toBeVisible();
    });

    test('상위 성과 챌린지 목록 확인', async ({ page }) => {
      await page.goto('/admin/analytics/challenge-performance');

      // 상위 성과 챌린지 섹션 확인
      await expect(page.getByText('상위 성과 챌린지')).toBeVisible();

      // 챌린지 카드들 확인 (실제 데이터에 따라 달라질 수 있음)
      const challengeCards = page.locator('[data-testid="top-challenge-card"]');
      const cardCount = await challengeCards.count();

      // 최소 1개 이상의 챌린지 카드가 있는지 확인
      expect(cardCount).toBeGreaterThan(0);
    });
  });

  test.describe('수익률 분석 페이지', () => {
    test('수익률 분석 페이지 로드', async ({ page }) => {
      await page.goto('/admin/analytics/returns');

      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '수익률 분석 및 리포트' })).toBeVisible();

      // 주요 수익률 지표 카드들 확인
      await expect(page.getByText('평균 수익률')).toBeVisible();
      await expect(page.getByText('총 수익')).toBeVisible();
      await expect(page.getByText('승률')).toBeVisible();
      await expect(page.getByText('샤프 비율')).toBeVisible();

      // 차트 섹션들 확인
      await expect(page.getByText('월별 수익률 및 벤치마크 비교')).toBeVisible();
      await expect(page.getByText('리스크 지표')).toBeVisible();
      await expect(page.getByText('섹터별 투자 성과')).toBeVisible();
    });

    test('벤치마크 비교 기능', async ({ page }) => {
      await page.goto('/admin/analytics/returns');

      // 벤치마크 선택
      await page.getByLabel('벤치마크').click();
      await page.getByRole('option', { name: 'NASDAQ' }).click();

      // 지표 선택
      await page.getByLabel('지표').click();
      await page.getByRole('option', { name: '샤프비율' }).click();

      // 변경 사항 적용 확인
      await page.waitForTimeout(500);
    });

    test('상위 수익률 랭킹 테이블', async ({ page }) => {
      await page.goto('/admin/analytics/returns');

      // 상위 수익률 랭킹 섹션 확인
      await expect(page.getByText('상위 수익률 랭킹')).toBeVisible();

      // 랭킹 리스트 항목들 확인
      const rankingItems = page.locator('[data-testid="ranking-item"]');
      const itemCount = await rankingItems.count();

      // 최소 3개 이상의 랭킹 항목이 있는지 확인
      expect(itemCount).toBeGreaterThanOrEqual(3);
    });
  });

  test.describe('시스템 모니터링 페이지', () => {
    test('시스템 모니터링 페이지 기본 요소', async ({ page }) => {
      await page.goto('/admin/analytics/system');

      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '시스템 통계 및 모니터링' })).toBeVisible();

      // 시스템 상태 알림 확인
      await expect(page.getByRole('alert')).toBeVisible();

      // 주요 시스템 지표 카드들 확인
      await expect(page.getByText('시스템 가동률')).toBeVisible();
      await expect(page.getByText('평균 응답시간')).toBeVisible();
      await expect(page.getByText('활성 사용자')).toBeVisible();
      await expect(page.getByText('에러율')).toBeVisible();

      // 모니터링 차트들 확인
      await expect(page.getByText('실시간 시스템 메트릭')).toBeVisible();
      await expect(page.getByText('리소스 사용량')).toBeVisible();
      await expect(page.getByText('서비스 상태')).toBeVisible();
    });

    test('자동 새로고침 기능', async ({ page }) => {
      await page.goto('/admin/analytics/system');

      // 자동새로고침 버튼 확인
      const refreshButton = page.getByRole('button', { name: /자동새로고침|수동모드/ });
      await expect(refreshButton).toBeVisible();

      // 자동새로고침 토글
      await refreshButton.click();

      // 상태 변경 확인 (텍스트가 변경되었는지)
      await page.waitForTimeout(500);
    });

    test('시스템 알림 및 로그', async ({ page }) => {
      await page.goto('/admin/analytics/system');

      // 최근 시스템 로그 섹션 확인
      await expect(page.getByText('최근 시스템 로그')).toBeVisible();

      // 로그 항목들 확인
      const logItems = page.locator('[data-testid="log-item"]');
      const logCount = await logItems.count();

      // 로그가 있는지 확인 (실제 데이터에 따라 달라질 수 있음)
      if (logCount > 0) {
        // 로그 레벨 태그들 확인 (ERROR, WARNING, INFO 등)
        await expect(page.getByText(/ERROR|WARNING|INFO/)).toBeVisible();
      }
    });
  });

  test.describe('통합 내보내기 기능 테스트', () => {
    test('내보내기 다이얼로그 기본 기능', async ({ page }) => {
      await page.goto('/admin/analytics');

      // 내보내기 버튼 클릭
      await page.getByRole('button', { name: '내보내기' }).click();
      await page.getByRole('menuitem', { name: 'PDF로 내보내기' }).click();

      // 내보내기 다이얼로그가 열렸다면 확인 (실제 구현에 따라)
      // 이 부분은 ExportDialog 컴포넌트가 실제로 사용될 때 테스트 가능
      await page.waitForTimeout(1000);
    });

    test('여러 페이지에서 내보내기 기능 일관성', async ({ page }) => {
      const analyticsPages = [
        '/admin/analytics',
        '/admin/analytics/user-behavior',
        '/admin/analytics/challenge-performance',
        '/admin/analytics/returns',
        '/admin/analytics/system'
      ];

      for (const pagePath of analyticsPages) {
        await page.goto(pagePath);

        // 내보내기 버튼이 모든 페이지에 있는지 확인
        await expect(page.getByRole('button', { name: /내보내기|리포트/ })).toBeVisible();
      }
    });
  });

  test.describe('반응형 디자인 테스트', () => {
    test('모바일 뷰포트에서 대시보드 표시', async ({ page }) => {
      // 모바일 뷰포트 설정
      await page.setViewportSize({ width: 375, height: 667 });
      await page.goto('/admin/analytics');

      // 모바일에서도 주요 요소들이 표시되는지 확인
      await expect(page.getByRole('heading', { name: '분석 대시보드' })).toBeVisible();
      await expect(page.getByText('총 사용자')).toBeVisible();

      // 차트들이 모바일에서도 표시되는지 확인
      await expect(page.getByText('사용자 증가 트렌드')).toBeVisible();
    });

    test('태블릿 뷰포트에서 차트 표시', async ({ page }) => {
      // 태블릿 뷰포트 설정
      await page.setViewportSize({ width: 768, height: 1024 });
      await page.goto('/admin/analytics/challenge-performance');

      // 태블릿에서 차트들이 적절하게 표시되는지 확인
      await expect(page.getByText('월별 챌린지 성과 트렌드')).toBeVisible();
      await expect(page.getByText('난이도별 챌린지 분포')).toBeVisible();
    });
  });

  test.describe('접근성 테스트', () => {
    test('키보드 네비게이션 지원', async ({ page }) => {
      await page.goto('/admin/analytics');

      // 탭 키를 사용한 네비게이션
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');

      // 포커스된 요소가 있는지 확인
      const focusedElement = page.locator(':focus');
      await expect(focusedElement).toBeVisible();
    });

    test('ARIA 라벨 및 역할 확인', async ({ page }) => {
      await page.goto('/admin/analytics');

      // 주요 UI 요소들의 접근성 속성 확인
      await expect(page.getByRole('heading', { name: '분석 대시보드' })).toBeVisible();
      await expect(page.getByRole('button', { name: '내보내기' })).toBeVisible();
      await expect(page.getByRole('combobox', { name: '기간' })).toBeVisible();
    });
  });

  test.describe('성능 테스트', () => {
    test('페이지 로드 시간 측정', async ({ page }) => {
      const startTime = Date.now();
      await page.goto('/admin/analytics');
      await page.waitForSelector('[data-testid="dashboard-loaded"]');
      const endTime = Date.now();

      const loadTime = endTime - startTime;
      console.log(`Dashboard load time: ${loadTime}ms`);

      // 로드 시간이 합리적인 범위 내에 있는지 확인 (5초 이하)
      expect(loadTime).toBeLessThan(5000);
    });

    test('차트 렌더링 성능', async ({ page }) => {
      await page.goto('/admin/analytics');

      // 차트 컨테이너들이 로드될 때까지 대기
      await page.waitForSelector('svg'); // Recharts는 SVG로 렌더링됨

      // 차트가 실제로 렌더링되었는지 확인
      const charts = page.locator('svg');
      const chartCount = await charts.count();

      expect(chartCount).toBeGreaterThan(0);
    });
  });
});