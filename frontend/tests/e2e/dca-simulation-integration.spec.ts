import { test, expect } from '@playwright/test';

test.describe('DCA Simulation Integration', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/dca-simulation');
    await page.waitForLoadState('networkidle');
  });

  test('DCA 시뮬레이션 페이지가 정상적으로 로드되어야 한다', async ({ page }) => {
    // 페이지 제목 확인
    await expect(page.locator('h1')).toContainText('DCA 시뮬레이션');

    // 설명 텍스트 확인
    await expect(page.locator('text=Dollar Cost Averaging 투자 전략 시뮬레이션')).toBeVisible();

    // 시뮬레이션 설정 섹션 확인
    await expect(page.locator('text=시뮬레이션 설정')).toBeVisible();
  });

  test('삼성전자 DCA 시뮬레이션이 정상 작동해야 한다', async ({ page }) => {
    // 회사 검색 입력
    const companyInput = page.locator('input[placeholder*="삼성전자"]');
    await companyInput.fill('삼성전자');
    await page.waitForTimeout(1000);

    // 첫 번째 검색 결과 선택
    await page.locator('[role="option"]').first().click();

    // 투자 금액 입력
    const amountInput = page.locator('input[type="number"]').first();
    await amountInput.fill('100000');

    // 시작일 설정
    const startDateInput = page.locator('input[type="date"]').first();
    await startDateInput.fill('2020-01-01');

    // 종료일 설정
    const endDateInput = page.locator('input[type="date"]').last();
    await endDateInput.fill('2020-12-31');

    // 투자 주기 선택 (월별)
    await page.locator('[role="combobox"]').click();
    await page.locator('text=월별').click();

    // 시뮬레이션 실행
    const simulateButton = page.locator('button:has-text("시뮬레이션 실행")');
    await simulateButton.click();

    // 로딩 상태 확인
    await expect(page.locator('[role="progressbar"]')).toBeVisible();

    // 결과 로딩 대기 (최대 30초)
    await page.waitForSelector('text=시뮬레이션 결과', { timeout: 30000 });

    // 결과 섹션 확인
    await expect(page.locator('text=시뮬레이션 결과')).toBeVisible();
    await expect(page.locator('text=투자 요약')).toBeVisible();
    await expect(page.locator('text=총 투자 금액')).toBeVisible();
    await expect(page.locator('text=최종 포트폴리오 가치')).toBeVisible();
  });

  test('날짜 프리셋 선택이 정상 작동해야 한다', async ({ page }) => {
    // 최근 1년 프리셋 클릭
    await page.locator('button:has-text("최근 1년")').click();

    // 날짜 입력 필드가 자동으로 채워지는지 확인
    const startDateInput = page.locator('input[type="date"]').first();
    const endDateInput = page.locator('input[type="date"]').last();

    await expect(startDateInput).toHaveValue(/\d{4}-\d{2}-\d{2}/);
    await expect(endDateInput).toHaveValue(/\d{4}-\d{2}-\d{2}/);
  });

  test('투자 금액 프리셋 선택이 정상 작동해야 한다', async ({ page }) => {
    // 10만원 프리셋 클릭
    await page.locator('button:has-text("10만원")').click();

    // 투자 금액 필드가 자동으로 채워지는지 확인
    const amountInput = page.locator('input[type="number"]').first();
    await expect(amountInput).toHaveValue('100000');
  });

  test('투자 전략 템플릿 선택이 정상 작동해야 한다', async ({ page }) => {
    // 보수적 전략 선택
    await page.locator('button:has-text("보수적")').first().click();

    // 전략이 선택되었는지 확인 (선택된 버튼의 스타일 변경)
    await expect(page.locator('button:has-text("보수적")').first()).toHaveClass(/.*selected.*/);
  });

  test('잘못된 입력에 대한 검증 메시지가 표시되어야 한다', async ({ page }) => {
    // 회사를 선택하지 않고 시뮬레이션 실행
    const simulateButton = page.locator('button:has-text("시뮬레이션 실행")');
    await simulateButton.click();

    // 검증 에러 메시지 확인
    await expect(page.locator('text=회사를 선택해주세요')).toBeVisible();
  });

  test('차트 및 상세 결과가 정상적으로 렌더링되어야 한다', async ({ page }) => {
    // 빠른 테스트를 위한 설정
    const companyInput = page.locator('input[placeholder*="삼성전자"]');
    await companyInput.fill('005930');
    await page.waitForTimeout(1000);
    await page.locator('[role="option"]').first().click();

    const amountInput = page.locator('input[type="number"]').first();
    await amountInput.fill('50000');

    const startDateInput = page.locator('input[type="date"]').first();
    await startDateInput.fill('2020-01-01');

    const endDateInput = page.locator('input[type="date"]').last();
    await endDateInput.fill('2020-06-30');

    const simulateButton = page.locator('button:has-text("시뮬레이션 실행")');
    await simulateButton.click();

    // 결과 로딩 대기
    await page.waitForSelector('text=시뮬레이션 결과', { timeout: 30000 });

    // 차트 컨테이너 확인
    await expect(page.locator('.recharts-wrapper')).toBeVisible();

    // 투자 기록 테이블 확인
    await expect(page.locator('text=투자 기록')).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // 리스크 분석 섹션 확인
    await expect(page.locator('text=리스크 분석')).toBeVisible();
    await expect(page.locator('text=변동성')).toBeVisible();
    await expect(page.locator('text=샤프 비율')).toBeVisible();
  });

  test('PDF 다운로드 기능이 작동해야 한다', async ({ page }) => {
    // 먼저 시뮬레이션 실행
    const companyInput = page.locator('input[placeholder*="삼성전자"]');
    await companyInput.fill('005930');
    await page.waitForTimeout(1000);
    await page.locator('[role="option"]').first().click();

    const amountInput = page.locator('input[type="number"]').first();
    await amountInput.fill('100000');

    const startDateInput = page.locator('input[type="date"]').first();
    await startDateInput.fill('2020-01-01');

    const endDateInput = page.locator('input[type="date"]').last();
    await endDateInput.fill('2020-03-31');

    const simulateButton = page.locator('button:has-text("시뮬레이션 실행")');
    await simulateButton.click();

    // 결과 로딩 대기
    await page.waitForSelector('text=시뮬레이션 결과', { timeout: 30000 });

    // PDF 다운로드 버튼 확인
    const pdfButton = page.locator('button:has-text("PDF 다운로드")');
    await expect(pdfButton).toBeVisible();

    // PDF 다운로드 클릭 (실제 다운로드는 테스트하지 않고 버튼 작동만 확인)
    await pdfButton.click();
  });

  test('여러 투자 주기 옵션이 정상 작동해야 한다', async ({ page }) => {
    // 회사 선택
    const companyInput = page.locator('input[placeholder*="삼성전자"]');
    await companyInput.fill('005930');
    await page.waitForTimeout(1000);
    await page.locator('[role="option"]').first().click();

    const amountInput = page.locator('input[type="number"]').first();
    await amountInput.fill('25000');

    const startDateInput = page.locator('input[type="date"]').first();
    await startDateInput.fill('2020-01-01');

    const endDateInput = page.locator('input[type="date"]').last();
    await endDateInput.fill('2020-02-29');

    // 주별 투자 주기 테스트
    await page.locator('[role="combobox"]').click();
    await page.locator('text=주별').click();

    const simulateButton = page.locator('button:has-text("시뮬레이션 실행")');
    await simulateButton.click();

    // 결과 로딩 대기
    await page.waitForSelector('text=시뮬레이션 결과', { timeout: 30000 });

    // 주별 투자 결과 확인
    await expect(page.locator('text=투자 주기: 주별')).toBeVisible();
  });

  test('한국어 회사명으로 검색이 정상 작동해야 한다', async ({ page }) => {
    // 한국어 회사명으로 검색
    const companyInput = page.locator('input[placeholder*="삼성전자"]');
    await companyInput.fill('삼성전자');
    await page.waitForTimeout(2000);

    // 검색 결과가 표시되는지 확인
    await expect(page.locator('[role="option"]')).toHaveCount.greaterThan(0);

    // 첫 번째 결과에 삼성전자가 포함되어 있는지 확인
    const firstOption = page.locator('[role="option"]').first();
    await expect(firstOption).toContainText(/삼성전자|005930/);
  });
});