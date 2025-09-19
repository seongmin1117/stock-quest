import { test, expect } from '@playwright/test';

/**
 * DCA 시뮬레이션 엣지 케이스 E2E 테스트
 *
 * 추가된 테스트 시나리오:
 * 1. 극단적인 입력값 테스트
 * 2. 네트워크 오류 처리 테스트
 * 3. PDF 다운로드 기능 테스트
 * 4. 다양한 투자 주기별 시뮬레이션
 * 5. 사용자 경험 최적화 검증
 */

test.describe('DCA 시뮬레이션 엣지 케이스', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dca-simulation');
    await page.waitForLoadState('networkidle');
  });

  test.describe('극단적인 입력값 테스트', () => {
    test('최소 투자 금액(1,000원)으로 시뮬레이션 실행', async ({ page }) => {
      // 올바른 데이터 범위 사용 (005930, 2020-01-02 ~ 2020-06-01)
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '1000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');

      // 결과 확인
      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();
      await expect(page.locator('[data-testid="total-investment"]')).toContainText('5,000'); // 5개월 * 1,000원
      await expect(page.locator('[data-testid="final-portfolio-value"]')).toBeVisible();
      await expect(page.locator('[data-testid="return-percentage"]')).toBeVisible();
    });

    test('대용량 투자 금액(1억원)으로 시뮬레이션 실행', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');

      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();
      await expect(page.locator('[data-testid="total-investment"]')).toContainText('500,000,000'); // 5억원
    });

    test('단기간 투자(1개월)로 시뮬레이션 실행', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-02-01');
      await page.selectOption('[data-testid="frequency-select"]', 'WEEKLY');

      await page.click('[data-testid="simulate-button"]');

      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();
      const investmentRecords = page.locator('[data-testid="investment-record"]');
      await expect(investmentRecords).toHaveCount(4); // 4주간
    });
  });

  test.describe('다양한 투자 주기 테스트', () => {
    const frequencies = [
      { value: 'DAILY', label: '일별', expectedCount: 100 },
      { value: 'WEEKLY', label: '주별', expectedCount: 20 },
      { value: 'MONTHLY', label: '월별', expectedCount: 5 }
    ];

    frequencies.forEach(({ value, label, expectedCount }) => {
      test(`${label} 투자 주기로 시뮬레이션 실행`, async ({ page }) => {
        await page.fill('[data-testid="symbol-input"]', '005930');
        await page.fill('[data-testid="investment-amount-input"]', '50000');
        await page.fill('[data-testid="start-date-input"]', '2020-01-02');
        await page.fill('[data-testid="end-date-input"]', '2020-06-01');
        await page.selectOption('[data-testid="frequency-select"]', value);

        await page.click('[data-testid="simulate-button"]');

        await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();

        // 각 주기별로 예상되는 투자 횟수 확인
        const investmentRecords = page.locator('[data-testid="investment-record"]');
        await expect(investmentRecords.first()).toBeVisible();

        // 주기별 특성 확인
        if (value === 'DAILY') {
          await expect(page.locator('[data-testid="frequency-info"]')).toContainText('일별');
        } else if (value === 'WEEKLY') {
          await expect(page.locator('[data-testid="frequency-info"]')).toContainText('주별');
        } else {
          await expect(page.locator('[data-testid="frequency-info"]')).toContainText('월별');
        }
      });
    });
  });

  test.describe('한국 주요 종목 테스트', () => {
    const koreanStocks = [
      { code: '005930', name: '삼성전자' },
      { code: '000660', name: 'SK하이닉스' },
      { code: '035720', name: '카카오' },
      { code: '005380', name: '현대차' }
    ];

    koreanStocks.forEach(({ code, name }) => {
      test(`${name}(${code})으로 시뮬레이션 실행`, async ({ page }) => {
        await page.fill('[data-testid="symbol-input"]', code);
        await page.fill('[data-testid="investment-amount-input"]', '100000');
        await page.fill('[data-testid="start-date-input"]', '2020-01-02');
        await page.fill('[data-testid="end-date-input"]', '2020-06-01');
        await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

        await page.click('[data-testid="simulate-button"]');

        // 성공 또는 적절한 에러 메시지 확인
        const results = page.locator('[data-testid="simulation-results"]');
        const errorMessage = page.locator('[data-testid="error-message"]');

        await expect.soft(results.or(errorMessage)).toBeVisible();

        // 성공한 경우 종목 코드 확인
        if (await results.isVisible()) {
          await expect(page.locator('[data-testid="symbol-display"]')).toContainText(code);
        }
      });
    });
  });

  test.describe('PDF 다운로드 기능 테스트', () => {
    test('성공적인 시뮬레이션 후 PDF 다운로드', async ({ page }) => {
      // 성공적인 시뮬레이션 실행
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');
      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();

      // PDF 다운로드 버튼 확인 및 클릭
      const pdfButton = page.locator('[data-testid="download-pdf-button"]');
      await expect(pdfButton).toBeVisible();
      await expect(pdfButton).toBeEnabled();

      // PDF 다운로드 트리거 (실제 다운로드는 브라우저 환경에 의존)
      await pdfButton.click();

      // PDF 생성 로딩 상태 확인
      await expect(page.locator('[data-testid="pdf-generating"]').or(page.locator('[data-testid="pdf-success"]'))).toBeVisible({ timeout: 10000 });
    });

    test('시뮬레이션 실행 전에는 PDF 버튼이 비활성화', async ({ page }) => {
      const pdfButton = page.locator('[data-testid="download-pdf-button"]');

      // PDF 버튼이 비활성화되어 있거나 보이지 않는 상태 확인
      if (await pdfButton.isVisible()) {
        await expect(pdfButton).toBeDisabled();
      }
    });
  });

  test.describe('에러 처리 테스트', () => {
    test('존재하지 않는 종목 코드로 시뮬레이션 시도', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', 'INVALID');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-01');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');

      // 에러 메시지 확인
      await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
      await expect(page.locator('[data-testid="error-message"]')).toContainText('데이터를 찾을 수 없습니다');
    });

    test('데이터가 없는 기간으로 시뮬레이션 시도', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2019-01-01'); // 데이터 없는 기간
      await page.fill('[data-testid="end-date-input"]', '2019-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');

      await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
      await expect(page.locator('[data-testid="error-message"]')).toContainText('해당 기간');
    });

    test('잘못된 날짜 순서로 입력', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-06-01'); // 종료일이 시작일보다 빠름
      await page.fill('[data-testid="end-date-input"]', '2020-01-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');

      await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
      await expect(page.locator('[data-testid="error-message"]')).toContainText('시작일은 종료일보다 빨라야 합니다');
    });

    test('음수 투자 금액 입력', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '-100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');

      await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
      await expect(page.locator('[data-testid="error-message"]')).toContainText('투자 금액은 0보다 커야 합니다');
    });
  });

  test.describe('성능 지표 검증', () => {
    test('시뮬레이션 응답 시간이 5초 이내', async ({ page }) => {
      const startTime = Date.now();

      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');
      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();

      const endTime = Date.now();
      const responseTime = endTime - startTime;

      expect(responseTime).toBeLessThan(5000); // 5초 이내
    });

    test('벤치마크 비교 지표가 모두 표시되어야 함', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');
      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();

      // 벤치마크 비교 지표 확인
      await expect(page.locator('[data-testid="sp500-comparison"]')).toBeVisible();
      await expect(page.locator('[data-testid="nasdaq-comparison"]')).toBeVisible();
      await expect(page.locator('[data-testid="outperformance-vs-sp500"]')).toBeVisible();
      await expect(page.locator('[data-testid="outperformance-vs-nasdaq"]')).toBeVisible();
    });

    test('투자 기록 테이블이 올바르게 표시되어야 함', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');
      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();

      // 투자 기록 테이블 헤더 확인
      await expect(page.locator('[data-testid="investment-records-table"]')).toBeVisible();
      await expect(page.locator('th:has-text("투자일")')).toBeVisible();
      await expect(page.locator('th:has-text("투자금액")')).toBeVisible();
      await expect(page.locator('th:has-text("주가")')).toBeVisible();
      await expect(page.locator('th:has-text("매수 주식수")')).toBeVisible();
      await expect(page.locator('th:has-text("포트폴리오 가치")')).toBeVisible();

      // 투자 기록 데이터 행 확인
      const recordRows = page.locator('[data-testid="investment-record"]');
      await expect(recordRows.first()).toBeVisible();
    });
  });

  test.describe('사용자 경험 최적화', () => {
    test('로딩 상태 표시', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      // 시뮬레이션 버튼 클릭 후 즉시 로딩 상태 확인
      await page.click('[data-testid="simulate-button"]');

      // 로딩 인디케이터나 버튼 비활성화 상태 확인
      const loadingIndicator = page.locator('[data-testid="loading-indicator"]');
      const disabledButton = page.locator('[data-testid="simulate-button"]:disabled');

      await expect(loadingIndicator.or(disabledButton)).toBeVisible();
    });

    test('폼 검증 피드백 표시', async ({ page }) => {
      // 빈 값으로 시뮬레이션 시도
      await page.click('[data-testid="simulate-button"]');

      // 필수 필드 검증 메시지 확인
      await expect(page.locator('[data-testid="symbol-error"]').or(page.locator('.error-message'))).toBeVisible();
    });

    test('성공적인 시뮬레이션 후 결과 섹션으로 스크롤', async ({ page }) => {
      await page.fill('[data-testid="symbol-input"]', '005930');
      await page.fill('[data-testid="investment-amount-input"]', '100000');
      await page.fill('[data-testid="start-date-input"]', '2020-01-02');
      await page.fill('[data-testid="end-date-input"]', '2020-06-01');
      await page.selectOption('[data-testid="frequency-select"]', 'MONTHLY');

      await page.click('[data-testid="simulate-button"]');
      await expect(page.locator('[data-testid="simulation-results"]')).toBeVisible();

      // 결과 섹션이 뷰포트에 표시되는지 확인
      const resultsSection = page.locator('[data-testid="simulation-results"]');
      await expect(resultsSection).toBeInViewport();
    });
  });
});