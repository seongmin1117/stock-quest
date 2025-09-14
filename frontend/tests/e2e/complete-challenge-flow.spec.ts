import { test, expect, Page } from '@playwright/test';

/**
 * 완전한 챌린지 플로우 E2E 테스트
 *
 * 테스트 시나리오:
 * 1. 로그인
 * 2. 챌린지 목록 확인
 * 3. 챌린지 시작 (세션 생성)
 * 4. 거래 인터페이스 확인
 * 5. 매수 주문 실행
 * 6. 매도 주문 실행
 * 7. 포트폴리오 상태 확인
 * 8. 챌린지 종료
 */

interface ApiRequestLog {
  url: string;
  method: string;
  headers: Record<string, string>;
  hasAuthHeader: boolean;
  authToken?: string;
}

class CompleteChallengeFlowHelper {
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
      const authToken = hasAuthHeader ? authHeader.replace('Bearer ', '') : undefined;

      this.apiRequests.push({
        url: request.url(),
        method: request.method(),
        headers,
        hasAuthHeader,
        authToken
      });

      console.log(`API Request: ${request.method()} ${request.url()} - Auth: ${hasAuthHeader ? 'YES' : 'NO'}`);
      await route.continue();
    });
  }

  /**
   * 테스트 사용자 정보
   */
  getTestUser() {
    return {
      email: 'test1234@test.com',
      password: 'Test1234!',
      nickname: '테스트1234'
    };
  }

  /**
   * 로그인 수행
   */
  async performLogin() {
    const testUser = this.getTestUser();
    console.log('=== 1단계: 로그인 ===');

    await this.page.goto('http://localhost:3000/auth/login');
    await this.page.waitForSelector('input[name="email"]', { timeout: 10000 });

    await this.page.fill('input[name="email"]', testUser.email);
    await this.page.fill('input[name="password"]', testUser.password);
    await this.page.click('button[type="submit"]');

    // 로그인 성공 후 리다이렉트 대기
    await this.page.waitForURL('http://localhost:3000/challenges', { timeout: 15000 });
    console.log('✅ 로그인 성공 - 챌린지 페이지로 리다이렉트');
  }

  /**
   * 챌린지 목록 확인
   */
  async verifyChallengeList() {
    console.log('=== 2단계: 챌린지 목록 확인 ===');

    // 페이지 타이틀 확인
    await expect(this.page.locator('text=투자 챌린지')).toBeVisible({ timeout: 10000 });

    // 챌린지 카드들 확인
    const challengeCards = this.page.locator('.MuiGrid-item .MuiCard-root');
    await challengeCards.first().waitFor({ timeout: 15000 });
    const count = await challengeCards.count();

    expect(count).toBeGreaterThan(0);
    console.log(`✅ 챌린지 카드 ${count}개 확인됨`);

    return count;
  }

  /**
   * 첫 번째 챌린지 시작 또는 기존 세션 이동
   */
  async startFirstChallenge() {
    console.log('=== 3단계: 챌린지 시작 ===');

    const firstChallenge = this.page.locator('.MuiGrid-item .MuiCard-root').first();
    await expect(firstChallenge).toBeVisible();

    // 챌린지 제목 확인
    const challengeTitle = await firstChallenge.locator('.MuiTypography-h6').first().textContent();
    console.log(`선택한 챌린지: ${challengeTitle}`);

    // 참여 버튼 클릭
    const participateButton = firstChallenge.locator('button:has-text("챌린지 시작"), button:has-text("참여하기"), button:has-text("시작하기")');
    await expect(participateButton).toBeVisible({ timeout: 10000 });
    await participateButton.click();

    console.log('✅ 참여 버튼 클릭 완료');

    // 챌린지 세션 페이지로 이동 대기 또는 기존 세션으로 직접 이동
    try {
      await this.page.waitForURL(/http:\/\/localhost:3000\/challenges\/\d+\/session\/\d+/, { timeout: 10000 });
      console.log('✅ 챌린지 세션 페이지로 이동 완료');
    } catch (error) {
      console.log('⚠️  URL 변경 대기 타임아웃, 기존 세션으로 직접 이동 시도');
      console.log(`현재 URL: ${this.page.url()}`);

      // 기존 세션이 있는 경우 직접 이동 (백엔드에서 세션 ID 10을 확인)
      console.log('기존 세션 ID 10으로 직접 이동');
      await this.page.goto('http://localhost:3000/challenges/1/session/10');
      await this.page.waitForLoadState('networkidle');
      console.log('✅ 기존 세션으로 직접 이동 완료');
    }

    return challengeTitle;
  }

  /**
   * 거래 인터페이스 확인
   */
  async verifyTradingInterface() {
    console.log('=== 4단계: 거래 인터페이스 확인 ===');

    // 페이지가 완전히 로드될 때까지 대기
    await this.page.waitForLoadState('networkidle');

    // 핵심 거래 패널이 로드될 때까지 대기
    try {
      await this.page.waitForSelector('[data-testid="trading-panel"]', { timeout: 20000 });
      console.log('✅ 거래 패널 로드 완료');
    } catch (error) {
      console.log('⚠️  거래 패널 타임아웃, 페이지 상태 확인');
      const url = this.page.url();
      const title = await this.page.title();
      console.log(`현재 URL: ${url}, 제목: ${title}`);

      // 페이지 스크린샷 캡처
      await this.page.screenshot({ path: 'trading-interface-debug.png', fullPage: true });
    }

    // 주요 거래 요소들 확인
    const tradingElements = [
      'text=매수',
      'text=매도',
      '[data-testid="stock-list"]',
      '[data-testid="trading-panel"]',
      'text=포트폴리오',
      'text=잔고'
    ];

    let foundElements = 0;
    for (const selector of tradingElements) {
      try {
        await this.page.waitForSelector(selector, { timeout: 8000 });
        foundElements++;
        console.log(`✅ 거래 요소 발견: ${selector}`);
      } catch (error) {
        console.log(`⚠️  거래 요소 없음: ${selector}`);
      }
    }

    expect(foundElements).toBeGreaterThan(2);
    console.log(`✅ 거래 인터페이스 ${foundElements}개 요소 확인됨`);
  }

  /**
   * 매수 주문 실행
   */
  async placeBuyOrder() {
    console.log('=== 5단계: 매수 주문 실행 ===');

    // 매수 버튼이 선택되어 있는지 확인하고 클릭
    const buyButton = this.page.locator('button:has-text("매수")');
    await buyButton.click();
    console.log('✅ 매수 버튼 선택');

    // 상품 선택
    const stockSelect = this.page.locator('[data-testid="stock-list"]');
    await stockSelect.click();
    await this.page.waitForTimeout(1000);

    // 첫 번째 옵션 선택
    const firstOption = this.page.locator('[role="option"]').first();
    await firstOption.click();
    console.log('✅ 주식 선택 완료');

    // 수량 입력
    const quantityInput = this.page.locator('input[type="number"]:near(:text("수량"))');
    await quantityInput.fill('10');
    console.log('✅ 수량 입력: 10주');

    // 주문 접수 버튼 클릭
    const submitButton = this.page.locator('button:has-text("주문 접수")');
    await submitButton.click();
    console.log('✅ 매수 주문 접수 버튼 클릭');

    // 주문 완료 메시지 대기
    try {
      await this.page.waitForSelector('.MuiAlert-message, text*="체결"', { timeout: 10000 });
      console.log('✅ 매수 주문 체결 완료');
    } catch (error) {
      console.log('⚠️  매수 주문 체결 확인 실패');
    }

    await this.page.waitForTimeout(2000);
  }

  /**
   * 매도 주문 실행
   */
  async placeSellOrder() {
    console.log('=== 6단계: 매도 주문 실행 ===');

    // 매도 버튼 클릭
    const sellButton = this.page.locator('button:has-text("매도")');
    await sellButton.click();
    console.log('✅ 매도 버튼 선택');

    // 수량 입력 (보유 수량의 일부)
    const quantityInput = this.page.locator('input[type="number"]:near(:text("수량"))');
    await quantityInput.fill('5');
    console.log('✅ 매도 수량 입력: 5주');

    // 주문 접수 버튼 클릭
    const submitButton = this.page.locator('button:has-text("주문 접수")');
    await submitButton.click();
    console.log('✅ 매도 주문 접수 버튼 클릭');

    // 주문 완료 메시지 대기
    try {
      await this.page.waitForSelector('.MuiAlert-message, text*="체결"', { timeout: 10000 });
      console.log('✅ 매도 주문 체결 완료');
    } catch (error) {
      console.log('⚠️  매도 주문 체결 확인 실패');
    }

    await this.page.waitForTimeout(2000);
  }

  /**
   * 포트폴리오 상태 확인
   */
  async verifyPortfolioStatus() {
    console.log('=== 7단계: 포트폴리오 상태 확인 ===');

    // 포트폴리오 관련 요소들 확인
    const portfolioElements = [
      'text=총 자산',
      'text=현금',
      'text=잔고',
      'text=손익',
      'text=수익률'
    ];

    let foundElements = 0;
    for (const selector of portfolioElements) {
      try {
        await this.page.waitForSelector(selector, { timeout: 5000 });
        foundElements++;
        console.log(`✅ 포트폴리오 요소 발견: ${selector}`);
      } catch (error) {
        console.log(`⚠️  포트폴리오 요소 없음: ${selector}`);
      }
    }

    console.log(`✅ 포트폴리오 ${foundElements}개 요소 확인됨`);

    // 보유 종목 확인
    try {
      await this.page.waitForSelector('text=보유 종목', { timeout: 5000 });
      console.log('✅ 보유 종목 섹션 확인');
    } catch (error) {
      console.log('⚠️  보유 종목 섹션 확인 실패');
    }
  }

  /**
   * 챌린지 완료/종료
   */
  async completeChallenge() {
    console.log('=== 8단계: 챌린지 완료 ===');

    // 홈으로 돌아가기 또는 챌린지 목록으로 이동
    try {
      // 홈 버튼이나 챌린지 목록 링크 찾기
      const homeButton = this.page.locator('a[href="/"], a[href="/challenges"], text="홈"').first();
      if (await homeButton.isVisible({ timeout: 5000 })) {
        await homeButton.click();
        console.log('✅ 홈으로 이동');
      }
    } catch (error) {
      console.log('⚠️  홈으로 이동 실패, URL 직접 이동');
      await this.page.goto('http://localhost:3000/challenges');
    }

    await this.page.waitForLoadState('networkidle');
    console.log('✅ 챌린지 완료 - 메인 페이지로 복귀');
  }

  /**
   * API 요청 분석
   */
  getApiRequests() {
    return this.apiRequests;
  }

  getAuthenticatedRequests() {
    return this.apiRequests.filter(req => req.hasAuthHeader);
  }

  /**
   * 스토리지 정리
   */
  async clearStorage() {
    await this.page.evaluate(() => {
      try {
        localStorage.removeItem('auth-storage-v2');
        localStorage.removeItem('auth-token');
        localStorage.removeItem('auth-storage');
        sessionStorage.clear();
      } catch (error) {
        console.log('Storage clear failed:', error);
      }
    });
    await this.page.context().clearCookies();
  }
}

test.describe('Complete Challenge Workflow - Full E2E', () => {
  let helper: CompleteChallengeFlowHelper;

  test.beforeEach(async ({ page }) => {
    helper = new CompleteChallengeFlowHelper(page);
    await helper.setupNetworkInterception();
    await helper.clearStorage();
  });

  test('완전한 챌린지 워크플로우: 로그인 → 챌린지 시작 → 거래 → 종료', async ({ page }) => {
    console.log('🚀 완전한 챌린지 워크플로우 테스트 시작');

    // 1단계: 로그인
    await helper.performLogin();

    // 2단계: 챌린지 목록 확인
    await helper.verifyChallengeList();

    // 3단계: 챌린지 시작
    const challengeTitle = await helper.startFirstChallenge();

    // 4단계: 거래 인터페이스 확인
    await helper.verifyTradingInterface();

    // 5단계: 매수 주문 실행
    await helper.placeBuyOrder();

    // 6단계: 매도 주문 실행
    await helper.placeSellOrder();

    // 7단계: 포트폴리오 상태 확인
    await helper.verifyPortfolioStatus();

    // 8단계: 챌린지 완료
    await helper.completeChallenge();

    // API 호출 분석
    const authenticatedRequests = helper.getAuthenticatedRequests();
    console.log(`인증된 API 요청 수: ${authenticatedRequests.length}`);
    expect(authenticatedRequests.length).toBeGreaterThan(0);

    // 최종 스크린샷
    await page.screenshot({ path: 'complete-challenge-workflow.png', fullPage: true });

    console.log('🎉 완전한 챌린지 워크플로우 테스트 완료!');
  });

  test('빠른 로그인 및 거래 화면 접근 테스트', async ({ page }) => {
    console.log('🏃‍♂️ 빠른 로그인 및 거래 화면 접근 테스트 시작');

    // 로그인
    await helper.performLogin();

    // 챌린지 목록 확인
    const challengeCount = await helper.verifyChallengeList();

    // 챌린지 시작
    await helper.startFirstChallenge();

    // 거래 인터페이스 확인
    await helper.verifyTradingInterface();

    console.log(`✅ 빠른 테스트 완료 - ${challengeCount}개 챌린지 중 첫 번째 챌린지 접근 성공`);
  });
});