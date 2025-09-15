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
      await emailInput.fill('test1234@test.com');
      await passwordInput.fill('Test1234!');
      await loginButton.click();

      // 로그인 성공 여부 확인 (리다이렉트 대기)
      try {
        await page.waitForURL(/\//, { timeout: 10000 });
        console.log('✅ Login successful');
      } catch (error) {
        console.log('⚠️ Login failed or no redirect - continuing with test');
      }
    } else {
      console.log('⚠️ Login form not found - continuing with test');
    }
  });

  test.describe('DCA 시뮬레이션 페이지', () => {
    test('DCA 시뮬레이션 페이지 기본 로딩 및 표시', async ({ page }) => {
      await page.goto('/dca-simulation');

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
      await page.goto('/dca-simulation');

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
      await page.goto('/dca-simulation');

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
      await page.goto('/dca-simulation');

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
      await page.goto('/dca-simulation');

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
      await page.goto('/dca-simulation');

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
      await page.goto('/dca-simulation');

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

  test.describe('Korean Company Selection Enhancement', () => {
    test('Korean company autocomplete and selection', async ({ page }) => {
      await page.goto('/dca-simulation');

      // Mock API for company search
      await page.route('/api/v1/companies/search**', async route => {
        const url = new URL(route.request().url());
        const query = url.searchParams.get('q') || '';

        const companies = [
          { symbol: '005930', nameKr: '삼성전자', nameEn: 'Samsung Electronics', sector: '반도체', logo: '/logos/samsung.png' },
          { symbol: '000660', nameKr: 'SK하이닉스', nameEn: 'SK Hynix', sector: '반도체', logo: '/logos/sk-hynix.png' },
          { symbol: '035720', nameKr: '카카오', nameEn: 'Kakao', sector: 'IT서비스', logo: '/logos/kakao.png' },
          { symbol: '035420', nameKr: '네이버', nameEn: 'Naver', sector: 'IT서비스', logo: '/logos/naver.png' },
          { symbol: '005380', nameKr: '현대차', nameEn: 'Hyundai Motor', sector: '자동차', logo: '/logos/hyundai.png' }
        ];

        const filtered = companies.filter(company =>
          company.nameKr.includes(query) ||
          company.nameEn.toLowerCase().includes(query.toLowerCase()) ||
          company.symbol.includes(query)
        );

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ companies: filtered.slice(0, 5) })
        });
      });

      // Type in company search box
      const companySearchBox = page.getByLabel('회사 검색');
      await expect(companySearchBox).toBeVisible();

      await companySearchBox.fill('삼성');

      // Wait for autocomplete dropdown
      await expect(page.getByTestId('company-autocomplete-dropdown')).toBeVisible();

      // Check if Samsung Electronics appears in dropdown
      const samsungOption = page.getByTestId('company-option-005930');
      await expect(samsungOption).toBeVisible();
      await expect(samsungOption.getByText('삼성전자')).toBeVisible();
      await expect(samsungOption.getByText('Samsung Electronics')).toBeVisible();
      await expect(samsungOption.getByText('반도체')).toBeVisible();

      // Select Samsung Electronics
      await samsungOption.click();

      // Verify selection
      await expect(page.getByDisplayValue('005930')).toBeVisible();
      await expect(page.getByText('삼성전자 (Samsung Electronics)')).toBeVisible();
    });

    test('Company category filtering', async ({ page }) => {
      await page.goto('/dca-simulation');

      // Mock categories API
      await page.route('/api/v1/companies/categories', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([
            { id: 'tech', name: '기술', count: 15 },
            { id: 'finance', name: '금융', count: 8 },
            { id: 'automotive', name: '자동차', count: 5 },
            { id: 'semiconductor', name: '반도체', count: 3 }
          ])
        });
      });

      // Mock filtered companies API
      await page.route('/api/v1/companies**', async route => {
        const url = new URL(route.request().url());
        const category = url.searchParams.get('category');

        let companies = [];
        if (category === 'tech') {
          companies = [
            { symbol: '035720', nameKr: '카카오', nameEn: 'Kakao', sector: 'IT서비스' },
            { symbol: '035420', nameKr: '네이버', nameEn: 'Naver', sector: 'IT서비스' }
          ];
        } else if (category === 'semiconductor') {
          companies = [
            { symbol: '005930', nameKr: '삼성전자', nameEn: 'Samsung Electronics', sector: '반도체' },
            { symbol: '000660', nameKr: 'SK하이닉스', nameEn: 'SK Hynix', sector: '반도체' }
          ];
        }

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ companies })
        });
      });

      // Check category filters
      await expect(page.getByTestId('category-filter-tech')).toBeVisible();
      await expect(page.getByText('기술 (15)')).toBeVisible();

      // Click semiconductor category
      await page.getByTestId('category-filter-semiconductor').click();

      // Verify filtered results
      await expect(page.getByText('삼성전자')).toBeVisible();
      await expect(page.getByText('SK하이닉스')).toBeVisible();
      await expect(page.getByText('카카오')).not.toBeVisible();
    });

    test('Popular companies section', async ({ page }) => {
      await page.goto('/dca-simulation');

      // Mock popular companies API
      await page.route('/api/v1/companies/popular', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([
            { symbol: '005930', nameKr: '삼성전자', nameEn: 'Samsung Electronics', marketCap: '360조원' },
            { symbol: '000660', nameKr: 'SK하이닉스', nameEn: 'SK Hynix', marketCap: '70조원' },
            { symbol: '035720', nameKr: '카카오', nameEn: 'Kakao', marketCap: '25조원' },
            { symbol: '035420', nameKr: '네이버', nameEn: 'Naver', marketCap: '35조원' }
          ])
        });
      });

      // Check popular companies section
      await expect(page.getByText('인기 종목')).toBeVisible();

      // Check popular company chips
      await expect(page.getByTestId('popular-company-005930')).toBeVisible();
      await expect(page.getByText('삼성전자')).toBeVisible();
      await expect(page.getByText('360조원')).toBeVisible();

      // Click popular company chip to select
      await page.getByTestId('popular-company-005930').click();

      // Verify selection
      await expect(page.getByDisplayValue('005930')).toBeVisible();
    });

    test('Date range presets for Korean market', async ({ page }) => {
      await page.goto('/dca-simulation');

      // Check date preset buttons
      await expect(page.getByTestId('date-preset-1year')).toBeVisible();
      await expect(page.getByText('1년')).toBeVisible();
      await expect(page.getByTestId('date-preset-3years')).toBeVisible();
      await expect(page.getByText('3년')).toBeVisible();
      await expect(page.getByTestId('date-preset-5years')).toBeVisible();
      await expect(page.getByText('5년')).toBeVisible();
      await expect(page.getByTestId('date-preset-10years')).toBeVisible();
      await expect(page.getByText('10년')).toBeVisible();

      // Click 3년 preset
      await page.getByTestId('date-preset-3years').click();

      // Calculate expected dates (3 years ago to today)
      const today = new Date();
      const threeYearsAgo = new Date(today.getFullYear() - 3, today.getMonth(), today.getDate());

      const expectedStartDate = threeYearsAgo.toISOString().split('T')[0];
      const expectedEndDate = today.toISOString().split('T')[0];

      // Verify dates are set
      await expect(page.getByLabel('시작일')).toHaveValue(expectedStartDate);
      await expect(page.getByLabel('종료일')).toHaveValue(expectedEndDate);
    });

    test('Korean investment amount presets', async ({ page }) => {
      await page.goto('/dca-simulation');

      // Check investment amount presets
      await expect(page.getByTestId('amount-preset-100k')).toBeVisible();
      await expect(page.getByText('월 10만원')).toBeVisible();
      await expect(page.getByTestId('amount-preset-500k')).toBeVisible();
      await expect(page.getByText('월 50만원')).toBeVisible();
      await expect(page.getByTestId('amount-preset-1m')).toBeVisible();
      await expect(page.getByText('월 100만원')).toBeVisible();

      // Click 50만원 preset
      await page.getByTestId('amount-preset-500k').click();

      // Verify amount is set
      await expect(page.getByLabel('월 투자 금액')).toHaveValue('500000');
    });
  });

  test.describe('KOSPI Benchmark Integration', () => {
    test('KOSPI comparison instead of S&P 500 for Korean stocks', async ({ page }) => {
      await page.goto('/dca-simulation');

      // Mock DCA simulation with KOSPI data
      await page.route('/api/v1/dca/simulate', async route => {
        const mockResponse = {
          symbol: '005930',
          nameKr: '삼성전자',
          totalInvestmentAmount: 1200000,
          finalPortfolioValue: 1560000,
          totalReturnPercentage: 30.00,
          annualizedReturn: 15.00,
          investmentRecords: [
            {
              investmentDate: '2022-01-01T00:00:00',
              investmentAmount: 100000,
              stockPrice: 82000,
              sharesPurchased: 1.219,
              portfolioValue: 100000
            }
          ],
          kospiReturnAmount: 1440000,  // Korean benchmark
          sp500ReturnAmount: 1380000,
          nasdaqReturnAmount: 1420000,
          outperformanceVsKOSPI: 8.33,   // Main benchmark for Korean stocks
          outperformanceVsSP500: 13.04,
          outperformanceVsNASDAQ: 9.85,
          maxPortfolioValue: 1600000
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponse)
        });
      });

      // Select Korean stock and run simulation
      await page.getByLabel('회사 검색').fill('005930');
      await page.getByTestId('company-option-005930').click();
      await page.getByLabel('월 투자 금액').fill('100000');
      await page.getByTestId('date-preset-1year').click();

      await page.getByRole('button', { name: '시뮬레이션 실행' }).click();

      // Wait for results
      await expect(page.getByText('시뮬레이션 결과')).toBeVisible({ timeout: 10000 });

      // Check KOSPI comparison is primary for Korean stocks
      await expect(page.getByText('KOSPI 대비')).toBeVisible();
      await expect(page.getByText('+8.33%')).toBeVisible();

      // Check other benchmarks are secondary
      await expect(page.getByText('S&P 500 대비')).toBeVisible();
      await expect(page.getByText('NASDAQ 대비')).toBeVisible();

      // Verify chart includes KOSPI line
      await expect(page.getByText('KOSPI')).toBeVisible();

      // Verify Korean formatting
      await expect(page.getByText('₩1,560,000')).toBeVisible(); // Korean won formatting
      await expect(page.getByText('삼성전자')).toBeVisible(); // Korean company name
    });
  });

  test.describe('DCA 시뮬레이션 성능 및 사용성', () => {
    test('대용량 데이터 처리 성능', async ({ page }) => {
      await page.goto('/dca-simulation');

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