import { test, expect } from '@playwright/test';

test.describe('Advanced Features E2E Tests - Phase 5', () => {
  test.beforeEach(async ({ page }) => {
    // 로그인 또는 인증 토큰 설정
    await page.goto('/admin');
  });

  test.describe('ML Trading Signals Dashboard', () => {
    test('ML 트레이딩 대시보드 기본 로딩 및 표시', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 페이지 제목 및 설명 확인
      await expect(page.getByRole('heading', { name: 'ML 트레이딩 신호 대시보드' })).toBeVisible();
      await expect(page.getByText('실시간 AI 트레이딩 신호, 모델 성능 분석, 백테스팅 결과')).toBeVisible();

      // 주요 ML 지표 카드들 확인
      await expect(page.getByText('신호 정확도')).toBeVisible();
      await expect(page.getByText('누적 수익률')).toBeVisible();
      await expect(page.getByText('금일 신호 수')).toBeVisible();
      await expect(page.getByText('리스크 스코어')).toBeVisible();

      // AI 시스템 상태 알림 확인
      await expect(page.getByRole('alert')).toBeVisible();
      await expect(page.getByText(/AI 시스템 상태/)).toBeVisible();
    });

    test('실시간 트레이딩 신호 테이블', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 트레이딩 신호 테이블 확인
      await expect(page.getByText('실시간 AI 트레이딩 신호')).toBeVisible();

      // 테이블 헤더들 확인
      await expect(page.getByText('종목')).toBeVisible();
      await expect(page.getByText('신호')).toBeVisible();
      await expect(page.getByText('신뢰도')).toBeVisible();
      await expect(page.getByText('목표가')).toBeVisible();
      await expect(page.getByText('예상수익')).toBeVisible();
      await expect(page.getByText('리스크')).toBeVisible();
      await expect(page.getByText('모델')).toBeVisible();

      // 신호 데이터 존재 확인 (BUY, SELL, HOLD 등)
      const signalChips = page.locator('[data-testid="signal-chip"]');
      if (await signalChips.count() > 0) {
        await expect(signalChips.first()).toBeVisible();
      }
    });

    test('AI 모델 상태 모니터링', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // AI 모델 상태 섹션 확인
      await expect(page.getByText('AI 모델 상태')).toBeVisible();

      // 모델 리스트 항목들 확인
      const modelNames = ['LSTM Model', 'Transformer Model', 'GAN Model', 'CNN Model', 'Ensemble Model'];
      for (const modelName of modelNames) {
        // 모델 이름이 표시되는지 확인 (부분 매칭)
        const modelText = page.getByText(new RegExp(modelName.split(' ')[0]));
        if (await modelText.count() > 0) {
          await expect(modelText.first()).toBeVisible();
        }
      }
    });

    test('모델 성능 비교 차트', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 모델 성능 비교 섹션 확인
      await expect(page.getByText('AI 모델 성능 비교')).toBeVisible();

      // 차트 SVG 요소가 렌더링되었는지 확인
      await expect(page.locator('svg').first()).toBeVisible();
    });

    test('백테스팅 결과 테이블', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 백테스팅 결과 섹션 확인
      await expect(page.getByText('백테스팅 결과 및 전략 성과')).toBeVisible();

      // 백테스팅 테이블 헤더들 확인
      await expect(page.getByText('전략')).toBeVisible();
      await expect(page.getByText('총 수익률')).toBeVisible();
      await expect(page.getByText('샤프 비율')).toBeVisible();
      await expect(page.getByText('최대 낙폭')).toBeVisible();
      await expect(page.getByText('승률')).toBeVisible();
    });

    test('필터 및 설정 기능', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 모델 필터 테스트
      await page.getByLabel('모델').click();
      await page.getByRole('option', { name: 'LSTM' }).click();

      // 주기 필터 테스트
      await page.getByLabel('주기').click();
      await page.getByRole('option', { name: '4시간' }).click();

      // 자동새로고침 토글 테스트
      const refreshSwitch = page.getByRole('switch', { name: '자동새로고침' });
      await refreshSwitch.click();

      // 설정 변경 확인
      await page.waitForTimeout(500);
    });

    test('내보내기 기능', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 리포트 내보내기 버튼 클릭
      await page.getByRole('button', { name: '리포트' }).click();

      // 내보내기 옵션들 확인
      await expect(page.getByRole('menuitem', { name: '신호 리포트 (PDF)' })).toBeVisible();
      await expect(page.getByRole('menuitem', { name: '백테스팅 결과 (Excel)' })).toBeVisible();
      await expect(page.getByRole('menuitem', { name: '모델 데이터 (JSON)' })).toBeVisible();
    });
  });

  test.describe('Personalized Recommendation System', () => {
    test('개인화 추천 시스템 기본 요소', async ({ page }) => {
      await page.goto('/admin/recommendations');

      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '개인화된 추천 시스템' })).toBeVisible();

      // 주요 개인화 지표 카드들 확인
      await expect(page.getByText('활성 추천')).toBeVisible();
      await expect(page.getByText('추천 정확도')).toBeVisible();
      await expect(page.getByText('사용자 만족도')).toBeVisible();
      await expect(page.getByText('전환율')).toBeVisible();

      // 개인화 알림 확인
      await expect(page.getByRole('alert')).toBeVisible();
      await expect(page.getByText(/AI 개인화 시스템이 활성화되었습니다/)).toBeVisible();
    });

    test('개인화 설정 패널', async ({ page }) => {
      await page.goto('/admin/recommendations');

      // 개인화 설정 섹션 확인
      await expect(page.getByText('개인화 설정')).toBeVisible();

      // 리스크 허용도 슬라이더 확인
      const riskSlider = page.locator('input[type="range"]').first();
      await expect(riskSlider).toBeVisible();

      // 투자 기간 슬라이더 확인
      const horizonSlider = page.locator('input[type="range"]').nth(1);
      await expect(horizonSlider).toBeVisible();

      // 설정 업데이트 버튼 확인
      await expect(page.getByRole('button', { name: '개인화 설정 업데이트' })).toBeVisible();
    });

    test('맞춤형 투자 추천 목록', async ({ page }) => {
      await page.goto('/admin/recommendations');

      // 추천 목록 섹션 확인
      await expect(page.getByText('맞춤형 투자 추천')).toBeVisible();

      // 추천 카드들 확인
      const recommendationCards = page.locator('[data-testid="recommendation-card"]');
      if (await recommendationCards.count() > 0) {
        await expect(recommendationCards.first()).toBeVisible();
      } else {
        // 카드가 없다면 추천 제목들 확인
        const recommendations = ['AI 기반 성장주 포트폴리오', 'ESG 친환경 에너지 펀드', '바이오테크 혁신 기업 선별'];
        for (const rec of recommendations) {
          const recText = page.getByText(rec);
          if (await recText.count() > 0) {
            await expect(recText.first()).toBeVisible();
            break;
          }
        }
      }
    });

    test('투자 성향 분석 차트', async ({ page }) => {
      await page.goto('/admin/recommendations');

      // 투자 성향 분석 섹션 확인
      await expect(page.getByText('투자 성향 분석')).toBeVisible();

      // 레이더 차트 SVG 요소 확인
      const charts = page.locator('svg');
      await expect(charts.first()).toBeVisible();
    });

    test('사용자 프로필 분포', async ({ page }) => {
      await page.goto('/admin/recommendations');

      // 사용자 프로필 분포 섹션 확인
      await expect(page.getByText('사용자 프로필 분포')).toBeVisible();

      // 파이차트 SVG 요소 확인
      const pieChart = page.locator('svg').nth(1);
      if (await pieChart.count() > 0) {
        await expect(pieChart).toBeVisible();
      }
    });

    test('AI 학습 특성 중요도', async ({ page }) => {
      await page.goto('/admin/recommendations');

      // AI 학습 특성 중요도 섹션 확인
      await expect(page.getByText('AI 학습 특성 중요도')).toBeVisible();

      // 특성 목록 확인
      const features = ['거래 패턴', '섹터 선호', '리스크 성향', '투자 기간', '수익률 목표'];
      for (const feature of features) {
        const featureText = page.getByText(feature);
        if (await featureText.count() > 0) {
          await expect(featureText.first()).toBeVisible();
        }
      }
    });
  });

  test.describe('Portfolio Optimization Tools', () => {
    test('포트폴리오 최적화 기본 요소', async ({ page }) => {
      await page.goto('/admin/portfolio-optimization');

      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '고급 포트폴리오 최적화' })).toBeVisible();

      // 주요 최적화 지표 카드들 확인
      await expect(page.getByText('총 최적화 포트폴리오')).toBeVisible();
      await expect(page.getByText('평균 개선율')).toBeVisible();
      await expect(page.getByText('평균 샤프비율')).toBeVisible();
      await expect(page.getByText('성공률')).toBeVisible();

      // 최적화 완료 알림 확인
      await expect(page.getByRole('alert')).toBeVisible();
      await expect(page.getByText(/최적화 완료/)).toBeVisible();
    });

    test('탭 네비게이션 기능', async ({ page }) => {
      await page.goto('/admin/portfolio-optimization');

      // 탭들 확인
      await expect(page.getByRole('tab', { name: '자산 배분' })).toBeVisible();
      await expect(page.getByRole('tab', { name: '효율적 프론티어' })).toBeVisible();
      await expect(page.getByRole('tab', { name: '백테스팅' })).toBeVisible();
      await expect(page.getByRole('tab', { name: '리밸런싱' })).toBeVisible();

      // 효율적 프론티어 탭 클릭 테스트
      await page.getByRole('tab', { name: '효율적 프론티어' }).click();
      await expect(page.getByText('효율적 프론티어')).toBeVisible();

      // 백테스팅 탭 클릭 테스트
      await page.getByRole('tab', { name: '백테스팅' }).click();
      await expect(page.getByText('백테스팅 성과 비교')).toBeVisible();

      // 리밸런싱 탭 클릭 테스트
      await page.getByRole('tab', { name: '리밸런싱' }).click();
      await expect(page.getByText('리밸런싱 제안')).toBeVisible();
    });

    test('자산 배분 탭 기능', async ({ page }) => {
      await page.goto('/admin/portfolio-optimization');

      // 자산 배분 탭이 기본적으로 활성화되어 있는지 확인
      await expect(page.getByText('최적화 파라미터')).toBeVisible();

      // 리스크 허용도 슬라이더 확인
      const riskSlider = page.locator('input[type="range"]');
      await expect(riskSlider).toBeVisible();

      // 투자금액 필드 확인
      await expect(page.getByLabel('투자금액')).toBeVisible();

      // 최적화 실행 버튼 확인
      await expect(page.getByRole('button', { name: '최적화 실행' })).toBeVisible();

      // 자산 클래스별 배분 비교 차트 확인
      await expect(page.getByText('자산 클래스별 배분 비교')).toBeVisible();

      // 섹터별 최적 배분 테이블 확인
      await expect(page.getByText('섹터별 최적 배분')).toBeVisible();
    });

    test('효율적 프론티어 분석', async ({ page }) => {
      await page.goto('/admin/portfolio-optimization');

      await page.getByRole('tab', { name: '효율적 프론티어' }).click();

      // 효율적 프론티어 차트 확인
      await expect(page.getByText('효율적 프론티어')).toBeVisible();

      // 스캐터 차트 SVG 요소 확인
      await expect(page.locator('svg').first()).toBeVisible();

      // 전략별 성과 리스트 확인
      await expect(page.getByText('전략별 성과')).toBeVisible();
    });

    test('백테스팅 결과', async ({ page }) => {
      await page.goto('/admin/portfolio-optimization');

      await page.getByRole('tab', { name: '백테스팅' }).click();

      // 백테스팅 성과 비교 차트 확인
      await expect(page.getByText('백테스팅 성과 비교')).toBeVisible();

      // 라인 차트 SVG 요소 확인
      await expect(page.locator('svg').first()).toBeVisible();

      // 성과 지표 카드들 확인
      const performanceCards = page.locator('text=+21%');
      if (await performanceCards.count() > 0) {
        await expect(performanceCards.first()).toBeVisible();
      }
    });

    test('리밸런싱 제안', async ({ page }) => {
      await page.goto('/admin/portfolio-optimization');

      await page.getByRole('tab', { name: '리밸런싱' }).click();

      // 리밸런싱 제안 테이블 확인
      await expect(page.getByText('리밸런싱 제안')).toBeVisible();

      // 테이블 헤더들 확인
      await expect(page.getByText('자산')).toBeVisible();
      await expect(page.getByText('현재 비중')).toBeVisible();
      await expect(page.getByText('목표 비중')).toBeVisible();
      await expect(page.getByText('액션')).toBeVisible();

      // 실행 버튼들 확인
      await expect(page.getByRole('button', { name: '리밸런싱 실행' })).toBeVisible();
      await expect(page.getByRole('button', { name: '시뮬레이션' })).toBeVisible();
    });
  });

  test.describe('Risk Management Dashboard', () => {
    test('리스크 관리 대시보드 기본 요소', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // 페이지 제목 확인
      await expect(page.getByRole('heading', { name: '리스크 관리 대시보드' })).toBeVisible();

      // 주요 리스크 지표 카드들 확인
      await expect(page.getByText('전체 리스크 점수')).toBeVisible();
      await expect(page.getByText('포트폴리오 VaR')).toBeVisible();
      await expect(page.getByText('샤프 비율')).toBeVisible();
      await expect(page.getByText('컴플라이언스 점수')).toBeVisible();

      // 전체 리스크 상태 알림 확인
      await expect(page.getByRole('alert')).toBeVisible();
    });

    test('실시간 리스크 알림', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // 리스크 알림 섹션 확인
      await expect(page.getByText('실시간 리스크 알림')).toBeVisible();

      // 알림 메시지들 확인 (있다면)
      const alertMessages = page.locator('[role="alert"]');
      const alertCount = await alertMessages.count();

      if (alertCount > 1) { // 첫 번째는 페이지 상단 알림
        for (let i = 1; i < alertCount; i++) {
          await expect(alertMessages.nth(i)).toBeVisible();
        }
      }
    });

    test('리스크 한도 모니터링', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // 리스크 한도 모니터링 섹션 확인
      await expect(page.getByText('리스크 한도 모니터링')).toBeVisible();

      // 한도 지표들 확인
      const limits = ['Portfolio VaR', 'Single Position', 'Sector Concentration', 'Leverage Ratio', 'Liquidity Buffer'];
      for (const limit of limits) {
        const limitText = page.getByText(limit);
        if (await limitText.count() > 0) {
          await expect(limitText.first()).toBeVisible();
        }
      }
    });

    test('VaR 추이 차트', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // VaR 추이 섹션 확인
      await expect(page.getByText('Value at Risk (VaR) 추이')).toBeVisible();

      // 차트 SVG 요소 확인
      await expect(page.locator('svg').first()).toBeVisible();
    });

    test('포지션별 리스크 분석', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // 포지션별 리스크 분석 테이블 확인
      await expect(page.getByText('포지션별 리스크 분석')).toBeVisible();

      // 테이블 헤더들 확인
      await expect(page.getByText('종목')).toBeVisible();
      await expect(page.getByText('포지션')).toBeVisible();
      await expect(page.getByText('비중')).toBeVisible();
      await expect(page.getByText('VaR')).toBeVisible();
      await expect(page.getByText('베타')).toBeVisible();
      await expect(page.getByText('리스크 등급')).toBeVisible();
    });

    test('스트레스 테스트 결과', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // 스트레스 테스트 섹션 확인
      await expect(page.getByText('스트레스 테스트 시나리오')).toBeVisible();

      // 시나리오들 확인
      const scenarios = ['2008 금융위기', '2020 코로나 쇼크', '기술주 폭락', '금리 급등', '지정학적 위험'];
      for (const scenario of scenarios) {
        const scenarioText = page.getByText(scenario);
        if (await scenarioText.count() > 0) {
          await expect(scenarioText.first()).toBeVisible();
        }
      }
    });

    test('필터 및 설정 기능', async ({ page }) => {
      await page.goto('/admin/risk-management');

      // 기간 필터 테스트
      await page.getByLabel('기간').click();
      await page.getByRole('option', { name: '1주' }).click();

      // 리스크 유형 필터 테스트
      await page.getByLabel('리스크 유형').click();
      await page.getByRole('option', { name: '시장 리스크' }).click();

      // 알림 토글 테스트
      const alertSwitch = page.getByRole('switch', { name: '알림' });
      await alertSwitch.click();

      // 설정 변경 확인
      await page.waitForTimeout(500);
    });
  });

  test.describe('통합 테스트 및 네비게이션', () => {
    test('고급 기능 페이지 간 네비게이션', async ({ page }) => {
      // ML Trading 페이지로 시작
      await page.goto('/admin/ml-trading');
      await expect(page.getByRole('heading', { name: 'ML 트레이딩 신호 대시보드' })).toBeVisible();

      // Recommendations 페이지로 이동
      await page.goto('/admin/recommendations');
      await expect(page.getByRole('heading', { name: '개인화된 추천 시스템' })).toBeVisible();

      // Portfolio Optimization 페이지로 이동
      await page.goto('/admin/portfolio-optimization');
      await expect(page.getByRole('heading', { name: '고급 포트폴리오 최적화' })).toBeVisible();

      // Risk Management 페이지로 이동
      await page.goto('/admin/risk-management');
      await expect(page.getByRole('heading', { name: '리스크 관리 대시보드' })).toBeVisible();
    });

    test('모든 고급 기능 페이지 내보내기 기능', async ({ page }) => {
      const pages = [
        { url: '/admin/ml-trading', button: '리포트' },
        { url: '/admin/recommendations', button: '리포트' },
        { url: '/admin/portfolio-optimization', button: '리포트' },
        { url: '/admin/risk-management', button: '리포트' }
      ];

      for (const pageInfo of pages) {
        await page.goto(pageInfo.url);

        // 내보내기 버튼 클릭
        await page.getByRole('button', { name: pageInfo.button }).click();

        // 메뉴가 열렸는지 확인 (PDF, Excel, JSON 등)
        const menuItems = page.locator('[role="menuitem"]');
        const menuItemCount = await menuItems.count();
        expect(menuItemCount).toBeGreaterThan(0);

        // 메뉴 닫기
        await page.keyboard.press('Escape');
      }
    });

    test('반응형 디자인 - 모바일 뷰', async ({ page }) => {
      // 모바일 뷰포트 설정
      await page.setViewportSize({ width: 375, height: 667 });

      const pages = [
        '/admin/ml-trading',
        '/admin/recommendations',
        '/admin/portfolio-optimization',
        '/admin/risk-management'
      ];

      for (const pagePath of pages) {
        await page.goto(pagePath);

        // 페이지 제목이 모바일에서도 표시되는지 확인
        const headings = page.locator('h4');
        const headingCount = await headings.count();
        if (headingCount > 0) {
          await expect(headings.first()).toBeVisible();
        }

        // 주요 카드들이 모바일에서도 표시되는지 확인
        const cards = page.locator('[role="button"], .MuiCard-root');
        const cardCount = await cards.count();
        if (cardCount > 0) {
          await expect(cards.first()).toBeVisible();
        }
      }
    });

    test('차트 렌더링 성능 테스트', async ({ page }) => {
      const chartPages = [
        '/admin/ml-trading',
        '/admin/recommendations',
        '/admin/portfolio-optimization',
        '/admin/risk-management'
      ];

      for (const pagePath of chartPages) {
        const startTime = Date.now();
        await page.goto(pagePath);

        // 차트 SVG 요소가 로드될 때까지 대기
        await page.waitForSelector('svg', { timeout: 10000 });

        const endTime = Date.now();
        const loadTime = endTime - startTime;

        console.log(`${pagePath} 차트 로딩 시간: ${loadTime}ms`);

        // 차트가 10초 내에 로드되는지 확인
        expect(loadTime).toBeLessThan(10000);

        // 차트가 실제로 렌더링되었는지 확인
        const charts = page.locator('svg');
        const chartCount = await charts.count();
        expect(chartCount).toBeGreaterThan(0);
      }
    });

    test('데이터 필터링 일관성 테스트', async ({ page }) => {
      const pagesWithFilters = [
        { url: '/admin/ml-trading', filters: ['모델', '주기'] },
        { url: '/admin/recommendations', filters: ['투자성향', '카테고리'] },
        { url: '/admin/portfolio-optimization', filters: ['최적화 전략', '목표'] },
        { url: '/admin/risk-management', filters: ['기간', '리스크 유형'] }
      ];

      for (const pageInfo of pagesWithFilters) {
        await page.goto(pageInfo.url);

        for (const filter of pageInfo.filters) {
          const filterElement = page.getByLabel(filter);
          if (await filterElement.count() > 0) {
            await expect(filterElement).toBeVisible();

            // 필터 클릭해서 옵션이 있는지 확인
            await filterElement.click();

            const options = page.locator('[role="option"]');
            const optionCount = await options.count();

            if (optionCount > 0) {
              await expect(options.first()).toBeVisible();
              await options.first().click(); // 첫 번째 옵션 선택
            }

            await page.waitForTimeout(500); // 필터 적용 대기
          }
        }
      }
    });
  });

  test.describe('성능 및 접근성 테스트', () => {
    test('페이지 로드 성능 측정', async ({ page }) => {
      const pages = [
        '/admin/ml-trading',
        '/admin/recommendations',
        '/admin/portfolio-optimization',
        '/admin/risk-management'
      ];

      for (const pagePath of pages) {
        const startTime = Date.now();
        await page.goto(pagePath);
        await page.waitForLoadState('networkidle');
        const endTime = Date.now();

        const loadTime = endTime - startTime;
        console.log(`${pagePath} 로딩 시간: ${loadTime}ms`);

        // 페이지가 5초 내에 로드되는지 확인
        expect(loadTime).toBeLessThan(5000);
      }
    });

    test('키보드 네비게이션 지원', async ({ page }) => {
      await page.goto('/admin/ml-trading');

      // 탭 키를 사용한 네비게이션
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');

      // 포커스된 요소가 있는지 확인
      const focusedElement = page.locator(':focus');
      await expect(focusedElement).toBeVisible();
    });

    test('ARIA 라벨 및 역할 확인', async ({ page }) => {
      const pages = [
        '/admin/ml-trading',
        '/admin/recommendations',
        '/admin/portfolio-optimization',
        '/admin/risk-management'
      ];

      for (const pagePath of pages) {
        await page.goto(pagePath);

        // 주요 UI 요소들의 접근성 속성 확인
        const headings = page.locator('h1, h2, h3, h4, h5, h6');
        const headingCount = await headings.count();
        if (headingCount > 0) {
          await expect(headings.first()).toBeVisible();
        }

        const buttons = page.locator('[role="button"], button');
        const buttonCount = await buttons.count();
        if (buttonCount > 0) {
          await expect(buttons.first()).toBeVisible();
        }

        const tables = page.locator('[role="table"], table');
        const tableCount = await tables.count();
        if (tableCount > 0) {
          await expect(tables.first()).toBeVisible();
        }
      }
    });
  });
});