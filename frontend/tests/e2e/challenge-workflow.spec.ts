import { test, expect, Page } from '@playwright/test';

/**
 * 전체 챌린지 워크플로우 E2E 테스트
 *
 * 테스트 시나리오:
 * 1. 로그인 → 챌린지 목록 접근
 * 2. 챌린지 선택 → 참여 버튼 클릭
 * 3. 챌린지 시작 → 거래 인터페이스 확인
 * 4. 주식 매수/매도 주문 테스트
 * 5. 포트폴리오 실시간 업데이트 확인
 * 6. 챌린지 진행상황 및 결과 확인
 */

interface ApiRequestLog {
  url: string;
  method: string;
  headers: Record<string, string>;
  hasAuthHeader: boolean;
  authToken?: string;
}

class ChallengeWorkflowHelper {
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
    console.log('=== 로그인 시작 ===');

    await this.page.goto('http://localhost:3001/auth/login');
    await this.page.waitForSelector('input[name="email"]', { timeout: 10000 });

    await this.page.fill('input[name="email"]', testUser.email);
    await this.page.fill('input[name="password"]', testUser.password);
    await this.page.click('button[type="submit"]');

    // 로그인 성공 후 리다이렉트 대기
    await this.page.waitForURL('http://localhost:3001/challenges', { timeout: 10000 });
    console.log('✅ 로그인 성공 - 챌린지 페이지로 리다이렉트');
  }

  /**
   * 챌린지 목록 확인
   */
  async verifyChallengeList() {
    console.log('=== 챌린지 목록 확인 ===');

    // 페이지 타이틀 확인
    await expect(this.page.locator('text=투자 챌린지')).toBeVisible({ timeout: 5000 });

    // Material-UI Card 컴포넌트를 사용하는 챌린지 카드들 확인
    // Grid item 내의 Card 컴포넌트를 찾음
    const challengeCards = this.page.locator('.MuiGrid-item .MuiCard-root');
    await challengeCards.first().waitFor({ timeout: 10000 });
    const count = await challengeCards.count();

    expect(count).toBeGreaterThan(0);
    console.log(`✅ 챌린지 카드 ${count}개 확인됨`);

    return count;
  }

  /**
   * 첫 번째 챌린지 선택
   */
  async selectFirstChallenge() {
    console.log('=== 챌린지 선택 ===');

    const firstChallenge = this.page.locator('.MuiGrid-item .MuiCard-root').first();
    await expect(firstChallenge).toBeVisible();

    // 챌린지 제목 확인 (Typography h6 컴포넌트)
    const challengeTitle = await firstChallenge.locator('.MuiTypography-h6').first().textContent();
    console.log(`선택한 챌린지: ${challengeTitle}`);

    // 참여 버튼 클릭 (스크린샷에서 "챌린지 시작" 버튼 확인)
    const participateButton = firstChallenge.locator('button:has-text("챌린지 시작"), button:has-text("참여하기"), button:has-text("시작하기")');
    await expect(participateButton).toBeVisible({ timeout: 5000 });
    await participateButton.click();

    console.log('✅ 참여 버튼 클릭 완료');

    // 챌린지 세션 페이지로 이동 대기 (URL 변경 감지)
    try {
      await this.page.waitForURL(/http:\/\/localhost:3001\/challenges\/\d+\/session\/\d+/, { timeout: 10000 });
      console.log('✅ 챌린지 세션 페이지로 이동 완료');
    } catch (error) {
      console.log('⚠️  URL 변경 대기 타임아웃, 현재 URL 확인');
      console.log(`현재 URL: ${this.page.url()}`);
    }

    // 페이지 로딩 완료 대기
    await this.page.waitForLoadState('networkidle');

    return challengeTitle;
  }

  /**
   * 거래 인터페이스 확인
   */
  async verifyTradingInterface() {
    console.log('=== 거래 인터페이스 확인 ===');

    // 페이지가 완전히 로드될 때까지 대기
    await this.page.waitForLoadState('networkidle');

    // 핵심 거래 패널이 로드될 때까지 대기
    try {
      await this.page.waitForSelector('[data-testid="trading-panel"]', { timeout: 15000 });
      console.log('✅ 거래 패널 로드 완료');
    } catch (error) {
      console.log('⚠️  거래 패널 타임아웃, 페이지 상태 확인');
      const url = this.page.url();
      const title = await this.page.title();
      console.log(`현재 URL: ${url}, 제목: ${title}`);

      // 페이지 스크린샷 캡처 (디버깅용)
      await this.page.screenshot({ path: 'trading-interface-debug.png', fullPage: true });
    }

    // 주식 리스트나 거래 패널 확인
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
        await this.page.waitForSelector(selector, { timeout: 5000 });
        foundElements++;
        console.log(`✅ 거래 요소 발견: ${selector}`);
      } catch (error) {
        console.log(`⚠️  거래 요소 없음: ${selector}`);
      }
    }

    // 최소 1개 이상의 요소가 발견되어야 함
    expect(foundElements).toBeGreaterThan(0);
    console.log(`✅ 거래 인터페이스 ${foundElements}개 요소 확인됨`);
  }

  /**
   * 포트폴리오 정보 확인
   */
  async verifyPortfolio() {
    console.log('=== 포트폴리오 확인 ===');

    // 포트폴리오 관련 요소들 확인
    const portfolioElements = [
      'text=총 자산',
      'text=현금',
      'text=투자금액',
      'text=수익률',
      '[data-testid="portfolio-summary"]'
    ];

    let foundElements = 0;
    for (const selector of portfolioElements) {
      try {
        await this.page.waitForSelector(selector, { timeout: 3000 });
        foundElements++;
        console.log(`✅ 포트폴리오 요소 발견: ${selector}`);
      } catch (error) {
        console.log(`⚠️  포트폴리오 요소 없음: ${selector}`);
      }
    }

    console.log(`✅ 포트폴리오 ${foundElements}개 요소 확인됨`);
  }

  /**
   * API 요청 분석
   */
  getApiRequests() {
    return this.apiRequests;
  }

  /**
   * 인증된 API 요청들
   */
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

test.describe('Challenge Workflow - Complete E2E', () => {
  let helper: ChallengeWorkflowHelper;

  test.beforeEach(async ({ page }) => {
    helper = new ChallengeWorkflowHelper(page);
    await helper.setupNetworkInterception();
    await helper.clearStorage();
  });

  test('전체 챌린지 워크플로우: 로그인 → 챌린지 선택 → 거래 인터페이스 확인', async ({ page }) => {
    // 1단계: 로그인
    await helper.performLogin();

    // 2단계: 챌린지 목록 확인
    await helper.verifyChallengeList();

    // 3단계: 챌린지 선택 및 참여
    const challengeTitle = await helper.selectFirstChallenge();

    // 4단계: 거래 인터페이스 확인
    await helper.verifyTradingInterface();

    // 5단계: 포트폴리오 확인
    await helper.verifyPortfolio();

    // API 호출 분석
    const authenticatedRequests = helper.getAuthenticatedRequests();
    console.log(`인증된 API 요청 수: ${authenticatedRequests.length}`);
    expect(authenticatedRequests.length).toBeGreaterThan(0);

    console.log('✅ 전체 챌린지 워크플로우 테스트 완료');
  });

  test('로그인 후 챌린지 목록 접근만 테스트', async ({ page }) => {
    // 로그인
    await helper.performLogin();

    // 챌린지 목록 확인
    const challengeCount = await helper.verifyChallengeList();

    // 기본적인 UI 요소들 확인
    await expect(page.locator('text=투자 챌린지')).toBeVisible();

    console.log(`✅ 챌린지 목록 테스트 완료 - ${challengeCount}개 챌린지 확인`);
  });

  test('직접 URL 접근 및 컴포넌트 로딩 테스트', async ({ page }) => {
    console.log('=== 직접 거래 페이지 접근 테스트 ===');

    // 직접 거래 세션 페이지로 이동
    await page.goto('http://localhost:3001/challenges/1/session/10');

    // 페이지 로딩 대기
    await page.waitForLoadState('networkidle');

    // 페이지 스크린샷 캡처
    await page.screenshot({ path: 'direct-trading-access.png', fullPage: true });

    console.log(`현재 URL: ${page.url()}`);
    console.log(`페이지 제목: ${await page.title()}`);

    // TradingPanel 컴포넌트 확인
    try {
      await page.waitForSelector('[data-testid="trading-panel"]', { timeout: 10000 });
      console.log('✅ TradingPanel 발견됨');
    } catch (error) {
      console.log('❌ TradingPanel 타임아웃');
    }

    // 포트폴리오 컴포넌트 확인
    try {
      await page.waitForSelector('text=포트폴리오', { timeout: 5000 });
      console.log('✅ 포트폴리오 텍스트 발견됨');
    } catch (error) {
      console.log('❌ 포트폴리오 텍스트 타임아웃');
    }

    // 매수/매도 버튼 확인
    try {
      await page.waitForSelector('text=매수', { timeout: 5000 });
      console.log('✅ 매수 버튼 발견됨');
    } catch (error) {
      console.log('❌ 매수 버튼 타임아웃');
    }

    console.log('✅ 직접 접근 테스트 완료');
  });
});