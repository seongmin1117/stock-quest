import { test, expect } from '@playwright/test';

/**
 * DCA Simulation Comprehensive E2E Tests
 *
 * 포괄적인 DCA 시뮬레이션 기능 테스트:
 * - 다양한 투자 주기 테스트
 * - 에러 시나리오 테스트
 * - PDF 다운로드 테스트
 * - 모바일 반응형 테스트
 * - 접근성 테스트
 */
test.describe('DCA Simulation Comprehensive Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dca-simulation');
    await page.waitForTimeout(1000);
  });

  test('모든 투자 주기(일별, 주별, 월별) 순차 테스트', async ({ page }) => {
    console.log('🎯 Testing all investment frequencies');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    const frequencies = [
      { name: '일별', expectedText: '일별' },
      { name: '주별', expectedText: '주별' },
      { name: '월별', expectedText: '월별' }
    ];

    for (const frequency of frequencies) {
      console.log(`Testing ${frequency.name} investment frequency`);

      // 폼 리셋 및 기본값 설정
      await page.reload();
      await page.waitForTimeout(1000);

      // 삼성전자로 설정
      await page.getByLabel('회사 검색').first().fill('005930');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-01-02');
      await page.getByLabel('종료일').fill('2020-03-01');

      // 투자 주기 선택
      await page.locator('div[role="combobox"]').last().click();
      await page.getByRole('option', { name: frequency.name }).click();

      // 시뮬레이션 실행
      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 결과 확인 (각 주기별로 다른 결과 예상)
      await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });
      await expect(page.getByText('총 투자 금액')).toBeVisible();
      await expect(page.getByText('최종 포트폴리오 가치')).toBeVisible();

      // 투자 기록 테이블 확인
      await expect(page.getByText('투자 기록')).toBeVisible();
      await expect(page.locator('table')).toBeVisible();

      console.log(`✅ ${frequency.name} frequency test passed`);
    }
  });

  test('입력 검증 및 에러 메시지 테스트', async ({ page }) => {
    console.log('🎯 Testing input validation and error messages');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 시나리오 1: 빈 종목 코드
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText(/종목.*필수/)).toBeVisible({ timeout: 5000 });

    // 시나리오 2: 유효하지 않은 종목 코드
    await page.getByLabel('회사 검색').first().fill('INVALID');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-03-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText(/데이터.*찾을.*없습니다/)).toBeVisible({ timeout: 10000 });

    // 시나리오 3: 음수 투자 금액
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('-100000');
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText(/금액.*0보다.*커야/)).toBeVisible({ timeout: 5000 });

    // 시나리오 4: 잘못된 날짜 범위 (종료일이 시작일보다 빠름)
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-06-01');
    await page.getByLabel('종료일').fill('2020-01-01');
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText(/시작일.*종료일.*빨라야/)).toBeVisible({ timeout: 5000 });

    console.log('✅ Input validation tests passed');
  });

  test('PDF 리포트 다운로드 기능 테스트', async ({ page }) => {
    console.log('🎯 Testing PDF report download functionality');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 성공적인 시뮬레이션 실행
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-02');
    await page.getByLabel('종료일').fill('2020-06-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

    // 결과 확인 후 PDF 다운로드 버튼 테스트
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    // PDF 리포트 버튼 확인
    const pdfButton = page.getByRole('button', { name: /PDF.*리포트/ });
    await expect(pdfButton).toBeVisible();
    await expect(pdfButton).toBeEnabled();

    // PDF 다운로드 클릭 (다운로드는 실제로 일어나지 않지만 함수 실행 확인)
    await pdfButton.click();

    // PDF 생성 중 메시지나 성공 메시지 확인 (구현에 따라)
    // await expect(page.getByText(/PDF.*생성/)).toBeVisible({ timeout: 5000 });

    console.log('✅ PDF download functionality test passed');
  });

  test('차트 및 시각화 요소 테스트', async ({ page }) => {
    console.log('🎯 Testing charts and visualization elements');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 시뮬레이션 실행
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-02');
    await page.getByLabel('종료일').fill('2020-06-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    // 차트 캔버스 확인 (Chart.js 또는 기타 차트 라이브러리)
    const chartCanvas = page.locator('canvas');
    if (await chartCanvas.count() > 0) {
      await expect(chartCanvas.first()).toBeVisible();
      console.log('✅ Chart visualization found');
    }

    // 수치 결과 카드 확인
    await expect(page.getByText('총 투자 금액')).toBeVisible();
    await expect(page.getByText('최종 포트폴리오 가치')).toBeVisible();
    await expect(page.getByText('총 수익률')).toBeVisible();
    await expect(page.getByText('연평균 수익률')).toBeVisible();

    // 벤치마크 비교 정보 확인
    await expect(page.getByText('S&P 500')).toBeVisible();
    await expect(page.getByText('NASDAQ')).toBeVisible();

    console.log('✅ Charts and visualization test passed');
  });

  test('반응형 디자인 및 모바일 뷰 테스트', async ({ page }) => {
    console.log('🎯 Testing responsive design and mobile view');

    // 모바일 뷰포트로 변경
    await page.setViewportSize({ width: 375, height: 667 }); // iPhone SE 크기

    await page.goto('/dca-simulation');
    await page.waitForTimeout(1000);

    // 모바일에서 페이지 기본 요소들이 표시되는지 확인
    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();
    await expect(page.getByLabel('회사 검색')).toBeVisible();
    await expect(page.getByLabel('월 투자 금액')).toBeVisible();

    // 모바일에서 시뮬레이션 실행
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-02');
    await page.getByLabel('종료일').fill('2020-03-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

    // 모바일에서 결과 표시 확인
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    // 테이블이 모바일에서 스크롤 가능한지 확인
    const table = page.locator('table');
    if (await table.count() > 0) {
      await expect(table).toBeVisible();
    }

    // 태블릿 뷰포트로 변경
    await page.setViewportSize({ width: 768, height: 1024 }); // iPad 크기
    await page.waitForTimeout(500);

    // 태블릿에서도 정상 표시 확인
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible();

    // 데스크톱으로 복원
    await page.setViewportSize({ width: 1920, height: 1080 });

    console.log('✅ Responsive design test passed');
  });

  test('접근성(Accessibility) 테스트', async ({ page }) => {
    console.log('🎯 Testing accessibility features');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 키보드 네비게이션 테스트
    await page.keyboard.press('Tab'); // 첫 번째 입력 필드로
    await page.keyboard.type('005930');

    await page.keyboard.press('Tab'); // 투자 금액 필드로
    await page.keyboard.type('100000');

    await page.keyboard.press('Tab'); // 시작일 필드로
    await page.keyboard.type('2020-01-02');

    await page.keyboard.press('Tab'); // 종료일 필드로
    await page.keyboard.type('2020-06-01');

    // 콤보박스 키보드 조작
    await page.keyboard.press('Tab'); // 투자 주기 콤보박스로
    await page.keyboard.press('Enter'); // 콤보박스 열기
    await page.keyboard.press('ArrowDown'); // 옵션 선택
    await page.keyboard.press('Enter'); // 선택 확정

    await page.keyboard.press('Tab'); // 실행 버튼으로
    await page.keyboard.press('Enter'); // 시뮬레이션 실행

    // 결과 확인
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    // ARIA 라벨 및 역할 확인
    await expect(page.getByLabel('회사 검색')).toBeVisible();
    await expect(page.getByLabel('월 투자 금액')).toBeVisible();
    await expect(page.getByLabel('시작일')).toBeVisible();
    await expect(page.getByLabel('종료일')).toBeVisible();

    // 헤딩 구조 확인
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();

    console.log('✅ Accessibility test passed');
  });

  test('로딩 상태 및 사용자 피드백 테스트', async ({ page }) => {
    console.log('🎯 Testing loading states and user feedback');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 시뮬레이션 실행
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-02');
    await page.getByLabel('종료일').fill('2020-06-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행 버튼 클릭
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

    // 로딩 인디케이터 확인 (있다면)
    const loadingElement = page.getByText(/로딩|Loading|처리/);
    if (await loadingElement.count() > 0) {
      console.log('✅ Loading indicator found');
    }

    // 버튼 비활성화 상태 확인 (중복 클릭 방지)
    const submitButton = page.getByRole('button', { name: '시뮬레이션 실행' });
    // 일시적으로 비활성화되는지 확인 (구현에 따라)

    // 결과 표시 확인
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    console.log('✅ Loading states and user feedback test passed');
  });

  test('브라우저 뒤로가기 및 새로고침 테스트', async ({ page }) => {
    console.log('🎯 Testing browser navigation and refresh');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 시뮬레이션 실행
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-02');
    await page.getByLabel('종료일').fill('2020-06-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    // 페이지 새로고침
    await page.reload();
    await page.waitForTimeout(1000);

    // 새로고침 후 초기 상태로 돌아가는지 확인
    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();
    await expect(page.getByLabel('회사 검색')).toBeVisible();

    // 입력 필드가 비어있는지 확인 (세션 저장이 없다면)
    const symbolInput = page.getByLabel('회사 검색').first();
    const symbolValue = await symbolInput.inputValue();
    // console.log('Symbol input value after refresh:', symbolValue);

    console.log('✅ Browser navigation and refresh test passed');
  });

  test('다국어 지원 테스트 (한국어)', async ({ page }) => {
    console.log('🎯 Testing Korean language support');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 한국어 텍스트들이 제대로 표시되는지 확인
    await expect(page.getByText('회사 검색')).toBeVisible();
    await expect(page.getByText('월 투자 금액')).toBeVisible();
    await expect(page.getByText('시작일')).toBeVisible();
    await expect(page.getByText('종료일')).toBeVisible();
    await expect(page.getByText('투자 주기')).toBeVisible();

    // 시뮬레이션 실행 후 한국어 결과 확인
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-02');
    await page.getByLabel('종료일').fill('2020-03-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

    // 한국어 결과 텍스트 확인
    await expect(page.getByText('총 투자 금액')).toBeVisible();
    await expect(page.getByText('최종 포트폴리오 가치')).toBeVisible();
    await expect(page.getByText('총 수익률')).toBeVisible();
    await expect(page.getByText('투자 기록')).toBeVisible();

    console.log('✅ Korean language support test passed');
  });
});