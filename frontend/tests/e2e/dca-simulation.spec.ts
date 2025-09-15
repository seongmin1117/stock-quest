import { test, expect } from '@playwright/test';

test.describe('DCA Simulation E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    // 로그인 처리
    await page.goto('/auth/login');

    // 로그인 폼 확인 및 테스트 계정으로 로그인
    const emailInput = page.getByLabel('이메일');
    const passwordInput = page.getByLabel('비밀번호');
    const loginButton = page.getByRole('button', { name: /로그인/ });

    if (await emailInput.count() > 0) {
      // 테스트용 계정으로 로그인 시도
      await emailInput.fill('test@example.com');
      await passwordInput.fill('test123');
      await loginButton.click();

      // 로그인 성공 여부 확인 (리다이렉트 대기)
      try {
        await page.waitForURL(/\/admin/, { timeout: 10000 });
        console.log('✅ Login successful - redirected to admin');
      } catch (error) {
        console.log('⚠️ Login failed or no redirect - continuing with test');
        // 로그인 없이 직접 페이지 접근 시도 (개발 환경에서 인증 우회 가능)
        await page.goto('/admin');
      }
    } else {
      console.log('⚠️ Login form not found - accessing admin directly');
      await page.goto('/admin');
    }
  });

  test.describe('DCA 시뮬레이션 페이지', () => {
    test('DCA 시뮬레이션 페이지 기본 로딩 및 표시', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // 페이지 제목 및 설명 확인
      await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();
      await expect(page.getByText('Dollar Cost Averaging 투자 전략 시뮬레이션')).toBeVisible();

      // 입력 폼 요소들 확인
      await expect(page.getByLabel('종목 코드')).toBeVisible();
      await expect(page.getByLabel('월 투자 금액')).toBeVisible();
      await expect(page.getByLabel('시작일')).toBeVisible();
      await expect(page.getByLabel('종료일')).toBeVisible();
      await expect(page.getByLabel('투자 주기')).toBeVisible();

      // 시뮬레이션 버튼 확인
      await expect(page.getByRole('button', { name: '시뮬레이션 실행' })).toBeVisible();
    });

    test('DCA 시뮬레이션 폼 입력 및 검증', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // 폼 입력
      await page.getByLabel('종목 코드').fill('AAPL');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-01-01');
      await page.getByLabel('종료일').fill('2020-06-01');
      await page.getByLabel('투자 주기').selectOption('MONTHLY');

      // 입력 값 확인
      await expect(page.getByLabel('종목 코드')).toHaveValue('AAPL');
      await expect(page.getByLabel('월 투자 금액')).toHaveValue('100000');
      await expect(page.getByLabel('시작일')).toHaveValue('2020-01-01');
      await expect(page.getByLabel('종료일')).toHaveValue('2020-06-01');
      await expect(page.getByLabel('투자 주기')).toHaveValue('MONTHLY');
    });

    test('DCA 시뮬레이션 실행 및 결과 표시', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // Mock API 응답 설정
      await page.route('/api/v1/dca/simulate', async route => {
        const mockResponse = {
          symbol: 'AAPL',
          totalInvestmentAmount: 500000,
          finalPortfolioValue: 650000,
          totalReturnPercentage: 30.00,
          annualizedReturn: 12.00,
          investmentRecords: [
            {
              investmentDate: '2020-01-01T00:00:00',
              investmentAmount: 100000,
              stockPrice: 100,
              sharesPurchased: 1000.00,
              portfolioValue: 100000
            },
            {
              investmentDate: '2020-02-01T00:00:00',
              investmentAmount: 100000,
              stockPrice: 110,
              sharesPurchased: 909.09,
              portfolioValue: 200000
            }
          ],
          sp500ReturnAmount: 600000,
          nasdaqReturnAmount: 650000,
          outperformanceVsSP500: 10.00,
          outperformanceVsNASDAQ: 0.00,
          maxPortfolioValue: 700000
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponse)
        });
      });

      // 폼 입력
      await page.getByLabel('종목 코드').fill('AAPL');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-01-01');
      await page.getByLabel('종료일').fill('2020-06-01');
      await page.getByLabel('투자 주기').selectOption('MONTHLY');

      // 시뮬레이션 실행
      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 로딩 상태 확인
      await expect(page.getByText('시뮬레이션 진행 중...')).toBeVisible();

      // 결과 표시 대기 및 확인
      await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

      // 주요 지표 확인
      await expect(page.getByText('총 투자금액: ₩500,000')).toBeVisible();
      await expect(page.getByText('최종 가치: ₩650,000')).toBeVisible();
      await expect(page.getByText('총 수익률: 30.00%')).toBeVisible();
      await expect(page.getByText('연평균 수익률: 12.00%')).toBeVisible();

      // 벤치마크 비교 확인
      await expect(page.getByText('S&P 500 대비: +10.00%')).toBeVisible();
      await expect(page.getByText('NASDAQ 대비: 0.00%')).toBeVisible();

      // 차트 영역 확인
      await expect(page.locator('[data-testid="dca-chart"]')).toBeVisible();
    });

    test('DCA 시뮬레이션 에러 처리', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // 에러 응답 Mock
      await page.route('/api/v1/dca/simulate', async route => {
        await route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            message: '잘못된 종목 코드입니다'
          })
        });
      });

      // 잘못된 데이터로 폼 입력
      await page.getByLabel('종목 코드').fill('INVALID');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-01-01');
      await page.getByLabel('종료일').fill('2020-06-01');
      await page.getByLabel('투자 주기').selectOption('MONTHLY');

      // 시뮬레이션 실행
      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 에러 메시지 확인
      await expect(page.getByText('잘못된 종목 코드입니다')).toBeVisible({ timeout: 5000 });
      await expect(page.getByRole('alert')).toHaveClass(/error/);
    });

    test('DCA 시뮬레이션 폼 검증', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // 빈 값으로 시뮬레이션 실행 시도
      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 필수 입력 검증 메시지 확인
      await expect(page.getByText('종목 코드는 필수입니다')).toBeVisible();
      await expect(page.getByText('투자 금액은 필수입니다')).toBeVisible();
      await expect(page.getByText('시작일은 필수입니다')).toBeVisible();
      await expect(page.getByText('종료일은 필수입니다')).toBeVisible();

      // 잘못된 날짜 범위 검증
      await page.getByLabel('종목 코드').fill('AAPL');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-06-01');
      await page.getByLabel('종료일').fill('2020-01-01'); // 종료일이 시작일보다 이전
      await page.getByLabel('투자 주기').selectOption('MONTHLY');

      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 날짜 범위 검증 메시지 확인
      await expect(page.getByText('종료일은 시작일보다 늦어야 합니다')).toBeVisible();
    });
  });

  test.describe('DCA 시뮬레이션 차트 및 데이터 시각화', () => {
    test('DCA 결과 차트 렌더링 및 상호작용', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // Mock API 응답 설정 (더 많은 데이터 포인트)
      await page.route('/api/v1/dca/simulate', async route => {
        const mockResponse = {
          symbol: 'AAPL',
          totalInvestmentAmount: 1200000,
          finalPortfolioValue: 1560000,
          totalReturnPercentage: 30.00,
          annualizedReturn: 15.00,
          investmentRecords: Array.from({ length: 12 }, (_, i) => ({
            investmentDate: `2020-${String(i + 1).padStart(2, '0')}-01T00:00:00`,
            investmentAmount: 100000,
            stockPrice: 100 + (i * 10),
            sharesPurchased: 1000 - (i * 50),
            portfolioValue: (i + 1) * 110000
          })),
          sp500ReturnAmount: 1440000,
          nasdaqReturnAmount: 1500000,
          outperformanceVsSP500: 5.00,
          outperformanceVsNASDAQ: 2.00,
          maxPortfolioValue: 1600000
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponse)
        });
      });

      // 시뮬레이션 실행
      await page.getByLabel('종목 코드').fill('AAPL');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-01-01');
      await page.getByLabel('종료일').fill('2020-12-31');
      await page.getByLabel('투자 주기').selectOption('MONTHLY');

      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 결과 대기
      await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

      // 차트 컨테이너 확인
      const chartContainer = page.locator('[data-testid="dca-chart"]');
      await expect(chartContainer).toBeVisible();

      // 차트 범례 확인
      await expect(page.getByText('포트폴리오 가치')).toBeVisible();
      await expect(page.getByText('누적 투자금액')).toBeVisible();
      await expect(page.getByText('S&P 500')).toBeVisible();
      await expect(page.getByText('NASDAQ')).toBeVisible();

      // 투자 기록 테이블 확인
      await expect(page.getByRole('table')).toBeVisible();
      await expect(page.getByRole('columnheader', { name: '투자일' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: '투자금액' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: '주식가격' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: '매수주식수' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: '포트폴리오가치' })).toBeVisible();

      // 12개 월별 기록 확인
      const tableRows = page.locator('tbody tr');
      await expect(tableRows).toHaveCount(12);
    });

    test('DCA 시뮬레이션 결과 내보내기', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // Mock API 설정
      await page.route('/api/v1/dca/simulate', async route => {
        const mockResponse = {
          symbol: 'AAPL',
          totalInvestmentAmount: 600000,
          finalPortfolioValue: 780000,
          totalReturnPercentage: 30.00,
          annualizedReturn: 15.00,
          investmentRecords: [
            {
              investmentDate: '2020-01-01T00:00:00',
              investmentAmount: 100000,
              stockPrice: 100,
              sharesPurchased: 1000.00,
              portfolioValue: 100000
            }
          ],
          sp500ReturnAmount: 720000,
          nasdaqReturnAmount: 750000,
          outperformanceVsSP500: 5.00,
          outperformanceVsNASDAQ: 2.00,
          maxPortfolioValue: 800000
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponse)
        });
      });

      // 시뮬레이션 실행
      await page.getByLabel('종목 코드').fill('AAPL');
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByLabel('시작일').fill('2020-01-01');
      await page.getByLabel('종료일').fill('2020-06-01');
      await page.getByLabel('투자 주기').selectOption('MONTHLY');

      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 결과 대기
      await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

      // 내보내기 버튼들 확인
      await expect(page.getByRole('button', { name: 'CSV 다운로드' })).toBeVisible();
      await expect(page.getByRole('button', { name: 'PDF 리포트' })).toBeVisible();

      // CSV 다운로드 클릭 (다운로드 시뮬레이션)
      const downloadPromise = page.waitForEvent('download');
      await page.getByRole('button', { name: 'CSV 다운로드' }).click();
      const download = await downloadPromise;

      // 다운로드 파일명 확인
      expect(download.suggestedFilename()).toMatch(/dca-simulation-AAPL-\d+\.csv/);
    });
  });

  test.describe('DCA 시뮬레이션 성능 및 사용성', () => {
    test('대용량 데이터 처리 성능', async ({ page }) => {
      await page.goto('/admin/dca-simulation');

      // 대용량 데이터 Mock (5년치 일별 데이터)
      await page.route('/api/v1/dca/simulate', async route => {
        const records = Array.from({ length: 1260 }, (_, i) => ({ // 5년 x 252 거래일
          investmentDate: new Date(2019, 0, 1 + i).toISOString(),
          investmentAmount: 10000,
          stockPrice: 100 + Math.random() * 100,
          sharesPurchased: 100 / (100 + Math.random() * 100),
          portfolioValue: (i + 1) * 10000 + Math.random() * 50000
        }));

        const mockResponse = {
          symbol: 'AAPL',
          totalInvestmentAmount: 12600000,
          finalPortfolioValue: 18900000,
          totalReturnPercentage: 50.00,
          annualizedReturn: 8.45,
          investmentRecords: records,
          sp500ReturnAmount: 15120000,
          nasdaqReturnAmount: 16380000,
          outperformanceVsSP500: 12.50,
          outperformanceVsNASDAQ: 7.50,
          maxPortfolioValue: 19500000
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponse)
        });
      });

      // 일별 투자 시뮬레이션 실행
      await page.getByLabel('종목 코드').fill('AAPL');
      await page.getByLabel('월 투자 금액').fill('10000');
      await page.getByLabel('시작일').fill('2019-01-01');
      await page.getByLabel('종료일').fill('2023-12-31');
      await page.getByLabel('투자 주기').selectOption('DAILY');

      // 시뮬레이션 실행 시간 측정
      const startTime = Date.now();
      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // 결과 대기 (최대 30초)
      await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 30000 });
      const endTime = Date.now();

      console.log(`DCA 시뮬레이션 처리 시간: ${endTime - startTime}ms`);

      // 대용량 데이터에서도 모든 요소가 정상 렌더링되는지 확인
      await expect(page.getByText('총 투자금액: ₩12,600,000')).toBeVisible();
      await expect(page.locator('[data-testid="dca-chart"]')).toBeVisible();

      // 테이블이 가상화되어 렌더링되는지 확인 (전체 1260개 행을 다 렌더링하지 않음)
      const visibleRows = page.locator('tbody tr:visible');
      const rowCount = await visibleRows.count();
      expect(rowCount).toBeLessThan(100); // 가상 스크롤링으로 일부만 렌더링
    });
  });
});