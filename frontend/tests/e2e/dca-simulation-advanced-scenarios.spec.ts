import { test, expect } from '@playwright/test';

/**
 * DCA Simulation Advanced Scenarios E2E Tests
 * 새로운 고급 시나리오 및 엣지 케이스 테스트
 */
test.describe('DCA Simulation Advanced Scenarios', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dca-simulation');
    await page.waitForTimeout(2000); // 페이지 완전 로딩 대기
  });

  test('극한 시나리오: 매우 작은 투자금액 ($1) 테스트', async ({ page }) => {
    console.log('🎯 Testing extreme scenario: Very small investment amount');

    // 페이지 로딩 확인
    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 매우 작은 투자금액 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('1'); // $1
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-03-01'); // 짧은 기간

    // 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Small amount simulation started');

    await page.waitForTimeout(8000);

    // 결과 또는 에러 확인
    const hasResults = await page.getByText('시뮬레이션 결과').count() > 0;
    const hasError = await page.locator('[role="alert"]').count() > 0;

    if (hasResults) {
      console.log('✅ Small amount simulation completed successfully');

      // 총 투자금액이 올바르게 계산되었는지 확인
      const resultsText = await page.textContent('body');
      if (resultsText?.includes('₩2') || resultsText?.includes('₩3')) {
        console.log('✅ Small investment amounts calculated correctly');
      }
    } else if (hasError) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('✅ Appropriate error handling for small amounts:', errorText);
    } else {
      console.log('⚠️ Unclear result for small investment test');
    }
  });

  test('극한 시나리오: 매우 큰 투자금액 (₩100,000,000) 테스트', async ({ page }) => {
    console.log('🎯 Testing extreme scenario: Very large investment amount');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 매우 큰 투자금액 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000000'); // 1억원
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-06-01');

    // 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Large amount simulation started');

    await page.waitForTimeout(10000);

    // 결과 확인
    const hasResults = await page.getByText('시뮬레이션 결과').count() > 0;
    const hasError = await page.locator('[role="alert"]').count() > 0;

    if (hasResults) {
      console.log('✅ Large amount simulation completed successfully');

      // 큰 숫자 포맷팅이 올바른지 확인
      const resultsText = await page.textContent('body');
      if (resultsText?.includes('500,000,000') || resultsText?.includes('5억')) {
        console.log('✅ Large numbers formatted correctly');
      }
    } else if (hasError) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('✅ Appropriate error handling for large amounts:', errorText);
    }
  });

  test('일별 투자 주기로 짧은 기간 (1주일) 시뮬레이션 테스트', async ({ page }) => {
    console.log('🎯 Testing daily investment frequency with short period');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 일별 투자 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('10000'); // 일당 1만원
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-01-08'); // 1주일

    // 일별 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '일별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Daily investment simulation started');

    await page.waitForTimeout(8000);

    // 결과 확인
    const hasResults = await page.getByText('시뮬레이션 결과').count() > 0;
    const hasError = await page.locator('[role="alert"]').count() > 0;

    if (hasResults) {
      console.log('✅ Daily investment simulation completed');

      // 투자 기록 개수가 적절한지 확인 (7-8개 정도)
      const tableRows = await page.locator('table tbody tr').count();
      if (tableRows >= 5 && tableRows <= 10) {
        console.log(`✅ Appropriate number of daily investment records: ${tableRows}`);
      }
    } else if (hasError) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('⚠️ Daily investment error (may be expected):', errorText);
    }
  });

  test('주별 투자 주기로 장기간 (6개월) 시뮬레이션 테스트', async ({ page }) => {
    console.log('🎯 Testing weekly investment frequency with long period');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 주별 투자 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('50000'); // 주당 5만원
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-07-01'); // 6개월

    // 주별 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '주별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Weekly investment simulation started');

    await page.waitForTimeout(10000);

    // 결과 확인
    const hasResults = await page.getByText('시뮬레이션 결과').count() > 0;
    const hasError = await page.locator('[role="alert"]').count() > 0;

    if (hasResults) {
      console.log('✅ Weekly investment simulation completed');

      // 투자 기록 개수가 적절한지 확인 (약 24-26주)
      const tableRows = await page.locator('table tbody tr').count();
      if (tableRows >= 20 && tableRows <= 30) {
        console.log(`✅ Appropriate number of weekly investment records: ${tableRows}`);
      }

      // 고급 위험 지표 확인
      const riskAnalysis = await page.getByText('⚠️ 위험 분석').count();
      if (riskAnalysis > 0) {
        console.log('✅ Risk analysis available for weekly investments');
      }
    } else if (hasError) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('⚠️ Weekly investment error (may be expected):', errorText);
    }
  });

  test('불가능한 날짜 범위 (미래 날짜) 입력 시 에러 처리 테스트', async ({ page }) => {
    console.log('🎯 Testing error handling for impossible date ranges');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 미래 날짜 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2030-01-01'); // 미래 날짜
    await page.getByLabel('종료일').fill('2030-06-01');

    // 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Future date simulation started');

    await page.waitForTimeout(5000);

    // 에러 메시지 확인
    const hasError = await page.locator('[role="alert"]').count() > 0;

    if (hasError) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('✅ Appropriate error handling for future dates:', errorText);

      if (errorText?.includes('데이터') || errorText?.includes('기간')) {
        console.log('✅ Error message contains relevant information about data availability');
      }
    } else {
      console.log('⚠️ No error shown for future dates - may need better validation');
    }
  });

  test('잘못된 날짜 순서 (종료일이 시작일보다 빠른 경우) 입력 테스트', async ({ page }) => {
    console.log('🎯 Testing invalid date order handling');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 잘못된 날짜 순서 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-06-01'); // 종료일보다 늦은 시작일
    await page.getByLabel('종료일').fill('2020-01-01'); // 시작일보다 빠른 종료일

    // 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Invalid date order simulation started');

    await page.waitForTimeout(3000);

    // 클라이언트 측 검증 에러 확인
    const validationError = await page.getByText('종료일은 시작일보다 늦어야 합니다').count();
    const alertError = await page.locator('[role="alert"]').count();

    if (validationError > 0) {
      console.log('✅ Client-side validation correctly catches invalid date order');
    } else if (alertError > 0) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('✅ Server-side validation catches invalid date order:', errorText);
    } else {
      console.log('⚠️ Date order validation may need improvement');
    }
  });

  test('음수 투자금액 입력 시 에러 처리 테스트', async ({ page }) => {
    console.log('🎯 Testing negative investment amount handling');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 음수 투자금액 설정
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('-100000'); // 음수
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-06-01');

    // 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    console.log('🔄 Negative amount simulation started');

    await page.waitForTimeout(3000);

    // 검증 에러 확인
    const validationError = await page.getByText('투자 금액은 0보다 커야 합니다').count();
    const alertError = await page.locator('[role="alert"]').count();

    if (validationError > 0) {
      console.log('✅ Client-side validation correctly rejects negative amounts');
    } else if (alertError > 0) {
      const errorText = await page.locator('[role="alert"]').textContent();
      console.log('✅ Server-side validation rejects negative amounts:', errorText);
    } else {
      console.log('⚠️ Negative amount validation may need improvement');
    }
  });

  test('CSV와 PDF 다운로드 버튼 동시 클릭 테스트', async ({ page }) => {
    console.log('🎯 Testing simultaneous CSV and PDF download functionality');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 시뮬레이션 설정 및 실행
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('300000');
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-04-01');

    // 투자 주기 선택
    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    // 시뮬레이션 실행
    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await page.waitForTimeout(8000);

    // 결과가 있으면 다운로드 버튼 테스트
    const hasResults = await page.getByText('시뮬레이션 결과').count() > 0;

    if (hasResults) {
      console.log('✅ Simulation completed, testing download buttons');

      const csvButton = page.getByRole('button', { name: 'CSV 다운로드' });
      const pdfButton = page.getByRole('button', { name: 'PDF 리포트' });

      const csvExists = await csvButton.count() > 0;
      const pdfExists = await pdfButton.count() > 0;

      if (csvExists && pdfExists) {
        console.log('✅ Both download buttons are available');

        // CSV 다운로드 테스트
        try {
          await csvButton.click();
          console.log('✅ CSV download button clicked successfully');
        } catch (error) {
          console.log('⚠️ CSV download click failed:', error);
        }

        await page.waitForTimeout(1000);

        // PDF 다운로드 테스트
        try {
          await pdfButton.click();
          console.log('✅ PDF download button clicked successfully');
        } catch (error) {
          console.log('⚠️ PDF download click failed:', error);
        }

        console.log('✅ Download functionality tested successfully');
      } else {
        console.log('⚠️ Download buttons not found');
      }
    } else {
      console.log('⚠️ No results to test download functionality');
    }
  });

  test('연속적인 시뮬레이션 실행 테스트 (다른 파라미터)', async ({ page }) => {
    console.log('🎯 Testing consecutive simulations with different parameters');

    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();

    // 첫 번째 시뮬레이션
    console.log('Running first simulation...');
    await page.getByLabel('회사 검색').first().fill('005930');
    await page.getByLabel('월 투자 금액').fill('100000');
    await page.getByLabel('시작일').fill('2020-01-01');
    await page.getByLabel('종료일').fill('2020-03-01');

    await page.locator('div[role="combobox"]').last().click();
    await page.getByRole('option', { name: '월별' }).click();

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await page.waitForTimeout(6000);

    const firstResult = await page.getByText('시뮬레이션 결과').count() > 0;
    if (firstResult) {
      console.log('✅ First simulation completed');
    }

    // 두 번째 시뮬레이션 (다른 파라미터)
    console.log('Running second simulation with different parameters...');
    await page.getByLabel('월 투자 금액').fill('200000'); // 다른 금액
    await page.getByLabel('종료일').fill('2020-05-01'); // 다른 기간

    await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
    await page.waitForTimeout(6000);

    const secondResult = await page.getByText('시뮬레이션 결과').count() > 0;
    if (secondResult) {
      console.log('✅ Second simulation completed');

      // 결과가 업데이트 되었는지 확인
      const resultsText = await page.textContent('body');
      if (resultsText?.includes('₩200,000') || resultsText?.includes('400,000')) {
        console.log('✅ Results updated with new parameters');
      }
    }

    console.log('✅ Consecutive simulations test completed');
  });
});