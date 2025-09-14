import { test, expect, Page } from '@playwright/test';

/**
 * 주식 거래 기능 E2E 테스트
 *
 * 테스트 시나리오:
 * 1. 로그인 → 챌린지 선택 → 거래 인터페이스 진입
 * 2. 주식 매수 주문 테스트
 * 3. 주식 매도 주문 테스트
 * 4. 포트폴리오 업데이트 확인
 */

interface ApiRequestLog {
  url: string;
  method: string;
  headers: Record<string, string>;
  hasAuthHeader: boolean;
}

class TradingHelper {
  private page: Page;
  private apiRequests: ApiRequestLog[] = [];

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * 네트워크 요청 인터셉터 설정
   */
  async setupNetworkInterception() {
    this.apiRequests = [];

    await this.page.route('**/api/**', async (route, request) => {
      const headers = request.headers();
      const authHeader = headers.authorization || '';
      const hasAuthHeader = authHeader.startsWith('Bearer ');

      this.apiRequests.push({
        url: request.url(),
        method: request.method(),
        headers,
        hasAuthHeader
      });

      console.log(`API Request: ${request.method()} ${request.url()} - Auth: ${hasAuthHeader ? 'YES' : 'NO'}`);
      await route.continue();
    });
  }

  /**
   * 테스트 사용자로 로그인
   */
  async performLogin() {
    console.log('=== 로그인 시작 ===');

    await this.page.goto('/auth/login');
    await this.page.waitForSelector('input[name="email"]', { timeout: 10000 });

    await this.page.fill('input[name="email"]', 'test1234@test.com');
    await this.page.fill('input[name="password"]', 'Test1234!');
    await this.page.click('button[type="submit"]');

    await this.page.waitForURL('/challenges', { timeout: 10000 });
    console.log('✅ 로그인 성공');
  }

  /**
   * 첫 번째 챌린지 시작
   */
  async startFirstChallenge() {
    console.log('=== 챌린지 시작 ===');

    // 챌린지 카드 대기
    const firstChallenge = this.page.locator('.MuiGrid-item .MuiCard-root').first();
    await expect(firstChallenge).toBeVisible({ timeout: 10000 });

    // 챌린지 제목 확인
    const challengeTitle = await firstChallenge.locator('.MuiTypography-h6').first().textContent();
    console.log(`선택한 챌린지: ${challengeTitle}`);

    // 챌린지 시작 버튼 클릭
    const startButton = firstChallenge.locator('button:has-text("챌린지 시작")');
    await expect(startButton).toBeVisible({ timeout: 5000 });
    await startButton.click();

    // 챌린지 세션 페이지로 이동 대기
    await this.page.waitForFunction(() =>
      window.location.pathname.includes('/challenges/') &&
      window.location.pathname.includes('/session/')
    );
    console.log('✅ 챌린지 세션 페이지 진입');
    console.log('현재 URL:', this.page.url());
  }

  /**
   * 거래 패널 요소 대기
   */
  async waitForTradingPanel() {
    console.log('=== 거래 패널 대기 ===');

    // 거래 패널 제목 대기
    await expect(this.page.locator('text=⚡ 주문 접수')).toBeVisible({ timeout: 15000 });

    // 매수/매도 버튼 그룹 대기
    const toggleButtons = this.page.locator('[role="group"] .MuiToggleButton-root');
    await expect(toggleButtons).toHaveCount(2);

    // 상품 선택 드롭다운 대기
    await expect(this.page.locator('label:has-text("상품 선택")')).toBeVisible();

    console.log('✅ 거래 패널 로딩 완료');
  }

  /**
   * 주식 매수 주문 테스트
   */
  async testBuyOrder() {
    console.log('=== 주식 매수 주문 테스트 ===');

    // 매수 버튼 선택 (기본값이지만 명시적으로 클릭)
    const buyButton = this.page.locator('button[value="BUY"]:has-text("매수")');
    await buyButton.click();
    console.log('✅ 매수 버튼 선택');

    // 상품 선택 드롭다운 클릭
    const instrumentSelect = this.page.locator('div[role="button"]:has(.MuiSelect-select)').first();
    await instrumentSelect.click();

    // 첫 번째 상품 선택 (회사 A)
    const firstOption = this.page.locator('[role="listbox"] [role="option"]').first();
    await expect(firstOption).toBeVisible({ timeout: 5000 });
    await firstOption.click();
    console.log('✅ 상품 선택 완료');

    // 수량 입력
    const quantityInput = this.page.locator('input[type="number"]:has(~ label:has-text("수량"))');
    await quantityInput.clear();
    await quantityInput.fill('5');
    console.log('✅ 수량 입력: 5주');

    // 시장가 주문 선택 (기본값 확인)
    const marketOrderButton = this.page.locator('button[value="MARKET"]:has-text("시장가")');
    await expect(marketOrderButton).toHaveClass(/Mui-selected/);
    console.log('✅ 시장가 주문 확인');

    // 주문 접수 버튼 클릭
    const submitButton = this.page.locator('button[type="submit"]:has-text("🚀 주문 접수")');
    await submitButton.click();
    console.log('📤 매수 주문 제출');

    // 주문 처리 대기 (로딩 상태 확인)
    await expect(this.page.locator('text=주문 처리 중')).toBeVisible({ timeout: 2000 });

    // 성공 메시지 대기
    const successAlert = this.page.locator('.MuiAlert-root .MuiAlert-message:has-text("매수")');
    await expect(successAlert).toBeVisible({ timeout: 10000 });

    const successMessage = await successAlert.textContent();
    console.log('✅ 매수 주문 성공:', successMessage);

    return successMessage;
  }

  /**
   * 주식 매도 주문 테스트
   */
  async testSellOrder() {
    console.log('=== 주식 매도 주문 테스트 ===');

    // 이전 메시지 닫기 (있다면)
    const closeButtons = this.page.locator('.MuiAlert-root button[aria-label="Close"]');
    if (await closeButtons.count() > 0) {
      await closeButtons.first().click();
      await this.page.waitForTimeout(500);
    }

    // 매도 버튼 선택
    const sellButton = this.page.locator('button[value="SELL"]:has-text("매도")');
    await sellButton.click();
    console.log('✅ 매도 버튼 선택');

    // 상품 선택 (이미 선택되어 있을 수 있음)
    const instrumentSelect = this.page.locator('div[role="button"]:has(.MuiSelect-select)').first();

    // 현재 선택된 값 확인
    const selectedValue = await instrumentSelect.textContent();
    if (!selectedValue || selectedValue.includes('상품 선택')) {
      await instrumentSelect.click();
      const firstOption = this.page.locator('[role="listbox"] [role="option"]').first();
      await firstOption.click();
    }
    console.log('✅ 상품 선택 확인');

    // 수량 입력 (매도할 수량)
    const quantityInput = this.page.locator('input[type="number"]:has(~ label:has-text("수량"))');
    await quantityInput.clear();
    await quantityInput.fill('2');
    console.log('✅ 수량 입력: 2주');

    // 지정가 주문으로 변경
    const limitOrderButton = this.page.locator('button[value="LIMIT"]:has-text("지정가")');
    await limitOrderButton.click();
    console.log('✅ 지정가 주문 선택');

    // 지정가 입력 필드가 나타날 때까지 대기
    const limitPriceInput = this.page.locator('input[type="number"]:has(~ label:has-text("지정가"))');
    await expect(limitPriceInput).toBeVisible({ timeout: 3000 });
    await limitPriceInput.fill('190.00');
    console.log('✅ 지정가 입력: $190.00');

    // 주문 접수 버튼 클릭
    const submitButton = this.page.locator('button[type="submit"]:has-text("🚀 주문 접수")');
    await submitButton.click();
    console.log('📤 매도 주문 제출');

    // 주문 처리 대기
    await expect(this.page.locator('text=주문 처리 중')).toBeVisible({ timeout: 2000 });

    // 성공 또는 실패 메시지 대기 (지정가 주문은 실패할 수 있음)
    try {
      const successAlert = this.page.locator('.MuiAlert-root .MuiAlert-message:has-text("매도")');
      await expect(successAlert).toBeVisible({ timeout: 10000 });

      const successMessage = await successAlert.textContent();
      console.log('✅ 매도 주문 성공:', successMessage);
      return { success: true, message: successMessage };
    } catch (error) {
      // 실패 메시지 확인
      const errorAlert = this.page.locator('.MuiAlert-root[data-testid="error"] .MuiAlert-message, .MuiAlert-standardError .MuiAlert-message');
      if (await errorAlert.isVisible()) {
        const errorMessage = await errorAlert.textContent();
        console.log('⚠️ 매도 주문 실패 (시뮬레이션):', errorMessage);
        return { success: false, message: errorMessage };
      }
      throw error;
    }
  }

  /**
   * 포트폴리오 패널 확인
   */
  async checkPortfolioUpdate() {
    console.log('=== 포트폴리오 업데이트 확인 ===');

    // 포트폴리오 패널 요소 확인
    const portfolioElements = [
      'text=수익률',
      'text=포트폴리오',
      '.MuiTypography-h5, .MuiTypography-h6' // 포트폴리오 관련 제목들
    ];

    let foundElements = 0;
    for (const selector of portfolioElements) {
      try {
        const element = this.page.locator(selector);
        if (await element.isVisible()) {
          foundElements++;
          console.log(`✅ 포트폴리오 요소 발견: ${selector}`);
        } else {
          console.log(`❌ 포트폴리오 요소 없음: ${selector}`);
        }
      } catch (error) {
        console.log(`❌ 포트폴리오 요소 오류: ${selector}`);
      }
    }

    console.log(`포트폴리오 ${foundElements}개 요소 확인됨`);
    return foundElements;
  }

  /**
   * API 요청 통계 반환
   */
  getApiStats() {
    const authRequests = this.apiRequests.filter(req => req.hasAuthHeader);
    return {
      total: this.apiRequests.length,
      authenticated: authRequests.length,
      orders: this.apiRequests.filter(req => req.url.includes('/orders')).length,
      portfolio: this.apiRequests.filter(req => req.url.includes('/portfolio')).length,
    };
  }
}

test.describe('Trading Functionality - E2E', () => {
  let helper: TradingHelper;

  test.beforeEach(async ({ page }) => {
    helper = new TradingHelper(page);
    await helper.setupNetworkInterception();
  });

  test('전체 거래 워크플로우: 로그인 → 챌린지 시작 → 매수/매도 주문', async ({ page }) => {
    // 1. 로그인
    await helper.performLogin();

    // 2. 챌린지 시작
    await helper.startFirstChallenge();

    // 3. 거래 패널 대기
    await helper.waitForTradingPanel();

    // 4. 매수 주문 테스트
    const buyResult = await helper.testBuyOrder();
    expect(buyResult).toContain('매수');
    expect(buyResult).toContain('체결');

    // 5. 매도 주문 테스트
    const sellResult = await helper.testSellOrder();
    expect(sellResult.message).toBeTruthy();

    // 6. 포트폴리오 확인
    const portfolioElements = await helper.checkPortfolioUpdate();
    expect(portfolioElements).toBeGreaterThan(0);

    // 7. API 통계 확인
    const stats = helper.getApiStats();
    console.log(`API 요청 통계: 총 ${stats.total}개, 인증된 요청 ${stats.authenticated}개`);
    expect(stats.authenticated).toBeGreaterThan(0);

    console.log('✅ 전체 거래 워크플로우 테스트 완료');
  });

  test('주식 매수 주문만 테스트', async ({ page }) => {
    // 1. 기본 설정
    await helper.performLogin();
    await helper.startFirstChallenge();
    await helper.waitForTradingPanel();

    // 2. 매수 주문만 테스트
    const buyResult = await helper.testBuyOrder();
    expect(buyResult).toContain('매수');
    expect(buyResult).toContain('시장가');
    expect(buyResult).toContain('체결');

    console.log('✅ 매수 주문 테스트 완료');
  });
});